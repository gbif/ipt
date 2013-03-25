package org.gbif.provider.service;

import java.util.List;
import java.util.Map;

import org.gbif.provider.model.TermMapping;

public interface TermMappingManager extends GenericManager<TermMapping>{
	public List<TermMapping> getTermMappings(Long transformationId);
	public Map<String, String> getMappingMap(Long transformationId);
}
