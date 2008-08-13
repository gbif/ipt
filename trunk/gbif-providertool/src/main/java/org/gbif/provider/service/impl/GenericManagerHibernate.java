package org.gbif.provider.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.BaseObject;
import org.gbif.provider.service.GenericManager;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

	/**
	 * This class serves as the Base class for all other Managers - namely to hold
	 * common CRUD methods that they might all use. You should only need to extend
	 * this class when your require custom CRUD logic.
	 *
	 * <p>To register this class in your Spring context file, use the following XML.
	 * <pre>
	 *     &lt;bean id="userManager" class="com.yasasu.service.impl.GenericManagerImpl"&gt;
	 *         &lt;constructor-arg&gt;
	 *             &lt;constructor-arg value="com.yasasu.model.User"/&gt;
	 *         &lt;/constructor-arg&gt;
	 *     &lt;/bean&gt;
	 * </pre>
	 *
	 * @param <T> a type variable
	 */
	public class GenericManagerHibernate<T extends BaseObject> extends HibernateDaoSupport implements GenericManager<T> {		
		public static int MAX_SEARCH_RESULTS = 50;
	    /**
	     * Log variable for all child classes. Uses LogFactory.getLog(getClass()) from Commons Logging
	     */
	    protected final Log log = LogFactory.getLog(getClass());
	    protected Class<T> persistentClass;

	    /**
	     * Constructor that takes in a class to see which type of entity to persist
	     * @param persistentClass the class type you'd like to persist
	     */
	    public GenericManagerHibernate(final Class<T> persistentClass) {
//	    	super(persistentClass);
	        this.persistentClass = persistentClass;
	    }

	    /**
	     * {@inheritDoc}
	     */
	    public List<T> getAll() {
	        return getSession().createQuery(String.format("from %s", persistentClass.getName()))
	        		.list();
	    }

	    public List<T> getTop(int maxResults) {
	        return getSession().createQuery(String.format("from %s", persistentClass.getName()))
	        		.setMaxResults(maxResults)
	        		.list();
	    }
	    /**
	     * {@inheritDoc}
	     */
	    public List<T> getAllDistinct() {
	        Collection result = new LinkedHashSet(getAll());
	        return new ArrayList(result);
	    }
	    
	    /**
	     * {@inheritDoc}
	     */
	    public T get(Long id) {
	    	return (T) getSession().get(persistentClass, id);
	    }

	    /**
	     * {@inheritDoc}
	     */
	    public boolean exists(Long id) {
	        T entity = get(id);
	        return entity != null;
	    }

	    /**
	     * {@inheritDoc}
	     */
	    public T save(T object) {
	    	getSession().saveOrUpdate(object);
	    	return object;
	    }

	    /**
	     * {@inheritDoc}
	     */
	    public void remove(Long id) {
			T obj = get(id);
			if (obj != null) {
		    	getSession().delete(obj);
			}
	    }
	    
	    /**
	     * {@inheritDoc}
	     */
		public void remove(T obj) {
	    	getSession().delete(obj);
		}

		
		public void flush() {
			getSession().flush();
		}
	}
