package org.gbif.provider.service.impl;

import java.util.List;

import org.gbif.provider.model.BBox;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.HqlTester;

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
	public class HqlTesterHibernate extends BaseManager implements HqlTester{

		public void runHql() {
			List<Object[]> objs = getSession().createQuery("select s from OccStatByRegionAndTaxon s WHERE s.resource.id=0 ") 
			.list();
		}
	}
