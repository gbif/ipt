package org.gbif.provider.task;

import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Callable;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Resource;

/**
 * Extended interface for resource related providertool tasks.
 * Each tasks needs to be initialised via the init method first before it can be submitted to an executor.
 * The constructor is avoided for this to allow tasks being declared as Spring prototype beans
 * thereby being able to use DI
 * @author markus
 *
 * @param <T>
 */
public interface Task<T> extends Callable<T>{
	/**
	 * Instead of constructor call this method once before using a Task bean
	 * @param resourceId the resource this tasks will work on. Not NULL
	 * @param userId the user that has submitted this task. Optional, maybe also be null 
	 */
	void init(Long resourceId);
	Long getResourceId();
	DataResource loadResource();
	String status();
	int taskTypeId();
}
