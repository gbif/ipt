package org.gbif.provider.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gbif.provider.model.TermMapping;
import org.gbif.provider.service.TermMappingManager;

public class TermMappingManagerHibernate extends GenericManagerHibernate<TermMapping> implements TermMappingManager{

	public TermMappingManagerHibernate() {
		super(TermMapping.class);
	}

	public List<TermMapping> getTermMappings(Long transformationId) {
        return query("select tm from TermMapping tm WHERE tm.transformation.id=:transformationId ORDER by tm.term")
        .setLong("transformationId", transformationId)
		.list();
	}

	public Map<String, String> getMappingMap(Long transformationId) {
		Map<String, String> map = new HashMap<String, String>();
		if (transformationId!=null){
	        List<Object[]> terms = query("select tm.term, tm.targetTerm from TermMapping tm WHERE tm.transformation.id=:transformationId")
		        .setLong("transformationId", transformationId)
				.list();
	        for (Object[]m : terms){
	        	map.put((String)m[0], (String)m[1]);
	        }
		}
        return map;
	}

}
