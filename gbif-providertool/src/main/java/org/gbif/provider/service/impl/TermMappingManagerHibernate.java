package org.gbif.provider.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gbif.provider.model.SourceColumn;
import org.gbif.provider.model.TermMapping;
import org.gbif.provider.service.TermMappingManager;

public class TermMappingManagerHibernate extends GenericManagerHibernate<TermMapping> implements TermMappingManager{

	public TermMappingManagerHibernate() {
		super(TermMapping.class);
	}

	public List<TermMapping> getTermMappings(Long sourceId, String column) {
        return query("select tm from TermMapping tm WHERE tm.source.id=:sourceId and tm.column.columnName=:column")
        .setLong("sourceId", sourceId)
        .setString("column", column)
		.list();
	}

	public Map<String, String> getMappingMap(Long sourceId, String column) {
		Map<String, String> map = new HashMap<String, String>();
        List<Object[]> terms = query("select tm.term, tm.targetTerm from TermMapping tm WHERE tm.source.id=:sourceId and tm.column.columnName=:column")
	        .setLong("sourceId", sourceId)
	        .setString("column", column)
			.list();
        for (Object[]m : terms){
        	map.put((String)m[0], (String)m[1]);
        }
        return map;
	}

}
