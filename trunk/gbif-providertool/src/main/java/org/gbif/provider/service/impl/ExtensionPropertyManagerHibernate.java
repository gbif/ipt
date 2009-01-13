package org.gbif.provider.service.impl;

import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.service.ExtensionPropertyManager;

public class ExtensionPropertyManagerHibernate extends GenericManagerHibernate<ExtensionProperty> implements ExtensionPropertyManager{

	public ExtensionPropertyManagerHibernate() {
		super(ExtensionProperty.class);
	}

	public ExtensionProperty getByQualName(String qName, ExtensionType type) {
		// FROM ExtensionProperty p WHERE p.qualName=:qName and (p.type=:type or (p.type is null and p.extension.id=:coreId))
		return (ExtensionProperty) getSession().createQuery("FROM ExtensionProperty p WHERE p.qualName=:qName and p.type is null and p.extension.id=:coreId) ")
		.setParameter("qName", qName)
		.setLong("coreId", type.extensionID)
		.uniqueResult();
	}

	public ExtensionProperty getByName(String name, ExtensionType type) {
		return (ExtensionProperty) getSession().createQuery("FROM ExtensionProperty p WHERE p.name=:name and p.type is null and p.extension.id=:coreId) ")
		.setParameter("name", name)
		.setLong("coreId", type.extensionID)
		.uniqueResult();
	}

}
