package org.gbif.provider.service;

import java.util.List;
import java.util.Map;

public interface ResourceKeywordManager {

	public List<String> getAlphabet();
	public Map<String, Integer> getCloud();
	public List<String> getKeywords(String prefix);
}
