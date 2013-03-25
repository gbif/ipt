package org.gbif.provider.service.impl;

import java.util.HashSet;
import java.util.Set;

import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.service.ExtensionPropertyManager;
import org.gbif.provider.tapir.ParseException;
import org.gbif.provider.tapir.TapirException;
import org.gbif.provider.tapir.filter.BooleanOperator;
import org.gbif.provider.tapir.filter.ComparisonOperator;
import org.gbif.provider.tapir.filter.Filter;

public class ExtensionPropertyManagerHibernate extends GenericManagerHibernate<ExtensionProperty> implements ExtensionPropertyManager{

	public ExtensionPropertyManagerHibernate() {
		super(ExtensionProperty.class);
	}

	public ExtensionProperty getByQualName(String qName, ExtensionType type) {
		// FROM ExtensionProperty p WHERE p.qualName=:qName and (p.type=:type or (p.type is null and p.extension.id=:coreId))
		return (ExtensionProperty) getSession().createQuery("select p FROM ExtensionProperty p join p.extension e WHERE p.qualName=:qName and e.type is null and e.id=:coreId) ")
		.setParameter("qName", qName)
		.setLong("coreId", type.extensionID)
		.uniqueResult();
	}

	public ExtensionProperty getByName(String name, ExtensionType type) {
		return (ExtensionProperty) getSession().createQuery("select p FROM ExtensionProperty p join p.extension e WHERE p.name=:name and e.type is null and e.id=:coreId) ")
		.setParameter("name", name)
		.setLong("coreId", type.extensionID)
		.uniqueResult();
	}

	public Set<ExtensionProperty> lookupFilterProperties(Filter filter, ExtensionType type) throws ParseException{
		Set<ExtensionProperty> props = new HashSet<ExtensionProperty>();
		for (BooleanOperator op : filter){
			if (ComparisonOperator.class.isAssignableFrom(op.getClass())){
				ComparisonOperator cop = (ComparisonOperator)op;
				ExtensionProperty prop = cop.getProperty();
				// try to lookup by qualname
				ExtensionProperty persistentProp = this.getByQualName(prop.getQualName(), type);
				if (persistentProp==null){
					// nothing found? then try via alias name
					persistentProp = this.getByName(prop.getName(), type);
				}				
				if (persistentProp==null){
					// still nothing found? cant deal with this filter then. throw exception
					throw new ParseException("Filter contains the unknown concept "+prop.getQualName());
				}				
				cop.setProperty(persistentProp);
				props.add(persistentProp);
			}
		}
		return props;
	}

}
