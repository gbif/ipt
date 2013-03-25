package org.gbif.provider.service.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gbif.provider.service.ResourceKeywordManager;

public class ResourceKeywordManagerJDBC extends BaseManagerJDBC implements ResourceKeywordManager{

	public List<String> getAlphabet() {
		String sql = "SELECT distinct upper(left(keywords_element,1)) FROM resource_keywords join resource res on resource_fk=res.id WHERE res.status>=1 order by 1";
		return executeListAsString(sql);
	}

	public Map<String, Integer> getCloud() {
		String sql = "SELECT count(*), keywords_element FROM resource_keywords join resource res on resource_fk=res.id WHERE res.status>=1 GROUP BY keywords_element order by 1 desc limit 1";
		double maxCnt = executeCount(sql);
		if (maxCnt<1.0){
			maxCnt=1.0;
		}
		sql = String.format("SELECT keywords_element, count(*)*%s FROM resource_keywords join resource res on resource_fk=res.id WHERE res.status>=1 GROUP BY keywords_element order by 2 desc limit 50", 9.0/maxCnt);
		Map<String, Integer> map = executeMap(sql);
		List<String> keys = new ArrayList<String>(map.keySet());
		Collections.sort(keys);
		Map<String, Integer> sortedmap = new LinkedHashMap<String, Integer>();
		for (String k : keys){
			sortedmap.put(k, map.get(k));
		}
		return sortedmap;
	}
	
	public List<String> getKeywords(String prefix) {
		String sql = "SELECT keywords_element FROM resource_keywords join resource res on resource_fk=res.id WHERE res.status>=1 and keywords_element like '"+prefix+"%' order by 1";
		return executeListAsString(sql);
	}
}
