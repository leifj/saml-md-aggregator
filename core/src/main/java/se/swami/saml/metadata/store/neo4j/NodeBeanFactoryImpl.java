package se.swami.saml.metadata.store.neo4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.util.index.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class NodeBeanFactoryImpl<T> implements NodeBeanFactory<T> {

	@Autowired
	private NeoService neoService;
	@Autowired
	private IndexService indexService;
	
	private Class<? extends T> beanClass;
	
	public NodeBeanFactoryImpl(Class<? extends T> beanClass) {
		this.beanClass = beanClass;
	}
	
	@SuppressWarnings("unchecked")
	public NodeBeanFactoryImpl(String beanClassName) throws ClassNotFoundException {
		this.beanClass = (Class<T>)Class.forName(beanClassName);
	}
	
	@Transactional
	public T findOrCreate(String pn, String pv) 
		throws IllegalAccessException, InvocationTargetException, InstantiationException, IllegalArgumentException, SecurityException, NoSuchMethodException {
		T o = getByProperty(pn, pv);
		if (o == null) {
			o = create();
			BeanUtils.setProperty(o, pn, pv);
		}
		return o;
	}
	
	@Transactional
	public T create() throws InstantiationException, IllegalAccessException, IllegalArgumentException, SecurityException, InvocationTargetException, NoSuchMethodException {
		return newInstance(neoService.createNode());
	}
	
	/* (non-Javadoc)
	 * @see se.swami.saml.metadata.store.neo4j.domain.NodeBeanFactory#newInstance(org.neo4j.api.core.Node)
	 */
	@SuppressWarnings("unchecked")
	public T newInstance(Node n) 
		throws InstantiationException, IllegalAccessException, IllegalArgumentException, SecurityException, InvocationTargetException, NoSuchMethodException {
		if (n == null)
			return null;
		
		NodeBean bean = (NodeBean)beanClass.getConstructor(NeoService.class,Node.class).newInstance(neoService,n);
		
		if (indexService != null) {
			bean.setIndexService(indexService);
		}
		return (T)bean;
	}
	
	/* (non-Javadoc)
	 * @see se.swami.saml.metadata.store.neo4j.domain.NodeBeanFactory#findByProperty(java.lang.String, java.lang.String)
	 */
	public Collection<T> findByProperty(String pn, String pv) throws InstantiationException, IllegalAccessException, IllegalArgumentException, SecurityException, InvocationTargetException, NoSuchMethodException {
		
		LinkedList<T> list = new LinkedList<T>(); 
		for (Node n : indexService.getNodes(pn,pv)) {
			list.add(newInstance(n));
		}
		
		return list;
	}
	
	/* (non-Javadoc)
	 * @see se.swami.saml.metadata.store.neo4j.domain.NodeBeanFactory#getByProperty(java.lang.String, java.lang.String)
	 */
	public T getByProperty(String pn, String pv) throws InstantiationException, IllegalAccessException, IllegalArgumentException, SecurityException, InvocationTargetException, NoSuchMethodException {
		return newInstance(indexService.getSingleNode(pn, pv));
	}

	public Class<?> instanceClass() throws Exception {
		return beanClass.getClass();
	}
	
}
