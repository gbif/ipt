package org.gbif.provider.service.impl;

import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.service.ExtensionPropertyManager;

public class ExtensionPropertyManagerHibernate extends GenericManagerHibernate<ExtensionProperty> implements ExtensionPropertyManager{

	public ExtensionPropertyManagerHibernate() {
		super(ExtensionProperty.class);
	}

	public ExtensionProperty getCorePropertyByQualName(String qName) {
		// use ExtensionProperty to parse qualname intp pieces for querying 
		ExtensionProperty prop = new ExtensionProperty(qName);
		return (ExtensionProperty) getSession().createQuery("select p FROM ExtensionProperty p join p.extension e WHERE p.name=:name and (p.namespace=:namespace or p.namespace=:namespace2) and e.core=true) ")
		.setParameter("name", prop.getName())
		.setParameter("namespace", prop.getNamespace())
		.setParameter("namespace2", prop.getNamespace()+"/")  // some namespaces like darwin core use a trailing slash which gets lost when parsing a qualified concept name
		.uniqueResult();
	}

	public ExtensionProperty getCorePropertyByName(String name) {
		return (ExtensionProperty) getSession().createQuery("select p FROM ExtensionProperty p join p.extension e WHERE p.name=:name and e.core=true) ")
		.setParameter("name", name)
		.uniqueResult();
	}

}
