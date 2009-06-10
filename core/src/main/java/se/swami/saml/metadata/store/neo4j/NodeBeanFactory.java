package se.swami.saml.metadata.store.neo4j;

import java.util.Collection;
import java.util.Set;

import org.neo4j.api.core.Node;
import org.neo4j.api.core.RelationshipType;

public interface NodeBeanFactory<T> {

	public Class<?> instanceClass() throws Exception;
	public abstract T newInstance(Node n) throws Exception;
	public abstract T create() throws Exception;
	public abstract Collection<? extends T> findByProperty(String pn,String pv) throws Exception;
	public abstract T getByProperty(String pn, String pv) throws Exception;
	public abstract T findOrCreate(String pn, String pv) throws Exception;

}