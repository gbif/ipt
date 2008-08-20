package org.gbif.provider.upload;

import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Callable;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;

public interface Task<T> extends Callable<T>{
	public void setResourceId(Long resourceId);
	public Long getResourceId();
	public OccurrenceResource getResource();
	public void setUserId(Long userId);
	String status();
}
