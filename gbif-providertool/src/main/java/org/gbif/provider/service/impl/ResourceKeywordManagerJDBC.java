package org.gbif.provider.service.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gbif.provider.service.ResourceKeywordManager;

public class ResourceKeywordManagerJDBC extends BaseManagerJDBC implements ResourceKeywordManager{

	public List<String> getAlphabet() {
		String sql = "SELECT distinct upper(left(keywords_element,1)) FROM RESOURCE_KEYWORDS order by 1";
		return executeListAsString(sql);
	}

	public Map<String, Integer> getCloud() {
		String sql = "SELECT count(*), keywords_element FROM RESOURCE_KEYWORDS group by keywords_element order by 2 desc limit 1";
		double maxCnt = executeCount(sql);
		sql = String.format("SELECT keywords_element, count(*)*%s FROM RESOURCE_KEYWORDS group by keywords_element order by 2 desc limit 20", 10.0/maxCnt);
		log.debug(sql);
		return executeMap(sql);
	}
	
	public List<String> getKeywords(String prefix) {
		String sql = "SELECT keywords_element FROM RESOURCE_KEYWORDS where keywords_element like '"+prefix+"%' order by 1";
		return executeListAsString(sql);
	}
}
