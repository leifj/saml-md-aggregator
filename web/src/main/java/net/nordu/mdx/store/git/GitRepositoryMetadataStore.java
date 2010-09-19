package net.nordu.mdx.store.git;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import net.nordu.mdx.scanner.MetadataChange;
import net.nordu.mdx.scanner.MetadataChangeType;
import net.nordu.mdx.store.MetadataStore;
import net.nordu.mdx.store.MetadataStoreContext;
import net.nordu.mdx.store.fs.URLMetadataStoreContext;

import org.eclipse.jgit.lib.Commit;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.MutableObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.oasis.saml.metadata.EntityDescriptorDocument;
import org.oasis.saml.metadata.EntityDescriptorType;

public class GitRepositoryMetadataStore implements MetadataStore {

	/**
	 * Git branch to use (refs/heads/branchname).
	 */
	private String refName;
	/**
	 * Object ID of the last HEAD.
	 */
	private ObjectId lastRef;
	/**
	 * Git repository.
	 */
	private Repository repository;
	/**
	 * Maps blobs to entity identifiers.
	 */
	private final Map<String, RepositoryEntry> blobMap;
	
	private URLMetadataStoreContext context;
		
	@Override
	public void setContext(MetadataStoreContext c) throws Exception {
		if (c instanceof URLMetadataStoreContext) {
			context = (URLMetadataStoreContext)c;
			// git://local/path.git!master
			String path = context.getUrl().getPath();
			String[] pathParts = path.split("!");
			String repositoryPath = pathParts[0];
			String branch = "master";
			if (pathParts.length == 2)
				branch = pathParts[1];
			this.refName = "refs/heads/" + branch;
			this.repository = new Repository(new File(repositoryPath));
		} else {
			throw new IllegalArgumentException("Bad MetadataStoreContext implementation");
		}
	}
	
	public GitRepositoryMetadataStore()
		throws IOException 
	{
		this.blobMap = new HashMap<String, RepositoryEntry>();
	}

	/**
	 * Scan repository for changes files.
	 * 
	 */
	synchronized void doRepoScan() {
		try {
			repository.incrementOpen();
			repository.scanForRepoChanges();
			ObjectId currentRef = repository.resolve(refName);
			if (currentRef == null) {
				throw new IllegalStateException("Cannot resolve ref");
			}
			if (currentRef.equals(lastRef)) {
				return;
			}
			// debug("Repository ref changed to '{}'", currentRef.getName());

			Commit currentCommit = repository.mapCommit(currentRef);

			String message = null;
			Date timeStamp = new Date();
			ObjectId oldTreeId = null;

			if (lastRef != null) {
				Commit oldCommit = repository.mapCommit(lastRef);
				if (Arrays.asList(currentCommit.getParentIds()).contains(
						oldCommit.getCommitId())) {
					// todo else?
					message = currentCommit.getMessage();
					timeStamp = currentCommit.getCommitter().getWhen();
				}
				oldTreeId = oldCommit.getTreeId();
			}
			diffTrees(oldTreeId, currentCommit.getTreeId(), message, timeStamp);

			lastRef = currentRef;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			repository.close();
		}
	}

	/**
	 * Diff two trees, and emit metadata change notifications.
	 * 
	 * @param oldTree
	 * @param newTree
	 * @param message
	 * @param timeStamp
	 * @throws IOException
	 */
	private void diffTrees(ObjectId oldTree, ObjectId newTree, String message,
			Date timeStamp) throws IOException {
		TreeWalk tw = new TreeWalk(repository);
		int cur = tw.addTree(newTree);
		int old = 0;
		boolean initialScan = oldTree == null;
		if (!initialScan) {
			old = tw.addTree(oldTree);
		}
		tw.setFilter(TreeFilter.ANY_DIFF);
		tw.setRecursive(true);

		while (tw.next()) {
			if (initialScan || !tw.idEqual(old, cur)) {
				String changedFile = tw.getNameString();
				int pfix = changedFile.lastIndexOf(".xml");
				if (pfix == -1) {
					continue;
				}
				String identifier = changedFile.substring(0, pfix);
				MetadataChangeType chT = null;
				if (FileMode.MISSING.equals(tw.getFileMode(cur))) {
					// debug("Entity deleted: '{}'", identifier);
					synchronized (blobMap) {
						blobMap.remove(identifier);
					}
					chT = MetadataChangeType.REMOVE;
				} else {
					MutableObjectId newEntry = new MutableObjectId();
					tw.getObjectId(newEntry, cur);
					ObjectId blobId = newEntry.copy();
					// debug("Entity updated or added: '{}', blob is '{}'",
					// identifier, blobId.getName());
					synchronized (blobMap) {
						blobMap.put(identifier, new RepositoryEntry(blobId,
								timeStamp));
					}
					if (initialScan
							|| FileMode.MISSING.equals(tw.getFileMode(old))) {
						chT = MetadataChangeType.ADD;
					} else {
						chT = MetadataChangeType.MODIFY;
					}
				}
				MetadataChange metadataChange = new MetadataChange(identifier,
						chT, message, timeStamp);
				context.getChangeNotifier().notifyChange(metadataChange);
			}
		}
	}

	@Override
	public List<String> listIDs() throws Exception {
		synchronized (blobMap) {
			List<String> ids = new ArrayList<String>(blobMap.keySet().size());
			for (String id : blobMap.keySet()) {
				ids.add(id);
			}
			return ids;
		}
	}

	@Override
	public boolean exists(String id) throws Exception {
		synchronized (blobMap) {
			return blobMap.containsKey(id);
		}
	}

	@Override
	public EntityDescriptorType load(String id) throws Exception {
		RepositoryEntry rEntry = null;
		synchronized (blobMap) {
			rEntry = blobMap.get(id);
		}

		if (rEntry == null) {
			// warning("Missing metadata entry for id '{}'", id);
			return null;
		}
		try {
			// debug("Trying to open blob '{}'", blobId.getName());
			repository.incrementOpen();
			ObjectLoader loader = repository.openBlob(rEntry.getBlobId());

			ByteArrayInputStream bais = new ByteArrayInputStream(loader
					.getCachedBytes());
			EntityDescriptorDocument doc = EntityDescriptorDocument.Factory
					.parse(bais);

			return doc.getEntityDescriptor();
		} catch (IOException e) {
			throw new IllegalStateException("Cannot load metadata instance", e);
		} finally {
			repository.close();
		}
	}

	@Override
	public Calendar lastModified(String id) {
		RepositoryEntry rEntry = null;
		synchronized (blobMap) {
			rEntry = blobMap.get(id);
		}

		if (rEntry == null) {
			// warning("Missing metadata entry for id '{}'", id);
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(rEntry.getLastModified());

		return c;
	}

	/**
	 * Helper class to hold state.
	 */
	private static class RepositoryEntry {

		private ObjectId blobId;
		private Date lastModified;

		public RepositoryEntry(ObjectId blobId, Date lastModified) {
			this.blobId = blobId;
			this.lastModified = lastModified;
		}

		public RepositoryEntry(ObjectId blobId) {
			this(blobId, new Date());
		}

		public ObjectId getBlobId() {
			return blobId;
		}

		public Date getLastModified() {
			return lastModified;
		}
	}
	
	@Override
	public TimerTask scanner() {
		return new TimerTask() {
			@Override
			public void run() {
				doRepoScan();
			}
		};
	}
}
