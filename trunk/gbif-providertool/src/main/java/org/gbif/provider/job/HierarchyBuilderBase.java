package org.gbif.provider.job;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.appfuse.model.User;
import org.gbif.logging.log.I18nDatabaseAppender;
import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.TreeNode;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.service.ResourceManager;
import org.gbif.provider.service.TreeNodeManager;
import org.gbif.scheduler.model.Job;
import org.gbif.scheduler.scheduler.Launchable;
import org.gbif.util.JSONUtils;

public abstract class HierarchyBuilderBase<T extends TreeNode<T>> implements org.gbif.provider.job.Job {
    protected final Log log = LogFactory.getLog(getClass());
	protected final I18nLog logdb = I18nLogFactory.getLog(getClass());

	public static final String RESOURCE_ID = "resourceId";
	public static final String USER_ID = "userId";
	public static String JOB_NAME = "Hierarchy builder";
	public static String JOB_DESCRIPTION = "Build hierarchy";

	protected DarwinCoreManager darwinCoreManager;
	protected ResourceManager<OccurrenceResource> occResourceManager;
	protected TreeNodeManager<T> treeNodeManager;

	public HierarchyBuilderBase(ResourceManager<OccurrenceResource> occResourceManager, DarwinCoreManager darwinCoreManager, TreeNodeManager<T> treeNodeManager, 
			final String jobName, final String jobDesciption) {
		super();
		JOB_NAME=jobName;
		JOB_DESCRIPTION = jobDesciption;
		this.darwinCoreManager = darwinCoreManager;
		this.occResourceManager = occResourceManager;
		this.treeNodeManager = treeNodeManager;
	}
	
	public static Job newHierarchyBuilderJob(OccurrenceResource resource, User user, int repeatInDays){
		// create job data
		Map<String, Object> seed = getSeed(resource.getId(), user.getId());
		// create upload job
		Job job = new Job();
		job.setJobClassName(GeographyBuilder.class.getName());
		job.setDataAsJSON(JSONUtils.jsonFromMap(seed));
		job.setRepeatInDays(repeatInDays);
		job.setJobGroup(JobUtils.getJobGroup(resource));
		job.setRunningGroup(JobUtils.getJobGroup(resource));
		job.setName(JOB_NAME);
		job.setDescription(String.format(JOB_DESCRIPTION, resource.getTitle()));
		return job;				
	}
	
	public static Map<String, Object> getSeed(Long resourceId, Long userId){
		Map<String, Object> seed = new HashMap<String, Object>();
		seed.put(RESOURCE_ID, resourceId);
		seed.put(USER_ID, userId);
		return seed;
	}
	

	public void launch(Map<String, Object> seed) throws Exception {
		Long resourceId = Long.valueOf(seed.get(RESOURCE_ID).toString());
		try{
			Long userId = Long.valueOf(seed.get(USER_ID).toString());
			MDC.put(I18nDatabaseAppender.MDC_USER, userId);
		} catch (NumberFormatException e) {
			String[] params = {RESOURCE_ID, USER_ID, seed.toString()};
			logdb.error("{0} or {1} in seed is no Integer {2}", params, e);
		}
		MDC.put(I18nDatabaseAppender.MDC_GROUP_ID, JobUtils.getJobGroup(resourceId));

		// set sourceId to jobID
		Integer sourceId = null;
		if (seed.get(Launchable.JOB_ID) != null && !seed.get(Launchable.JOB_ID).equals("null")){
			try{
				sourceId = Integer.valueOf(seed.get(Launchable.JOB_ID).toString());
				MDC.put(I18nDatabaseAppender.MDC_SOURCE_ID, sourceId);
			} catch (NumberFormatException e) {
				String[] params = {Launchable.JOB_ID, seed.toString()};
				logdb.warn("{0} in seed is no Integer {1}", params, e);
			}
		}
		MDC.put(I18nDatabaseAppender.MDC_SOURCE_TYPE, getSourceType());

		// get resource
		OccurrenceResource resource = occResourceManager.get(resourceId);
		
		// create unique, naturally sorted regions from dwc records
		SortedSet<T> hierarchy = extractHierarchy(resource, true);
		
		// assign nested set indices and save hierarchy
		calcNestedSetIndices(hierarchy, true);
		
		// cache resource statistics
		calcStats(resource, hierarchy, true);
	}
	
	protected abstract SortedSet<T> extractHierarchy(OccurrenceResource resource, boolean persist);

	protected abstract void calcStats(OccurrenceResource resource, SortedSet<T> hierarchy, boolean persist);

	public void calcNestedSetIndices(SortedSet<T> hierarchy, boolean persist){
		log.info("Calculating nested set indices for %s hierarchy with %s nodes"+hierarchy.size());
		Stack<T> parentStack = new Stack<T>();
		Long idx = 0l;
		for (T node : hierarchy){
			// process right values for taxa on stack. But only ...
			// if stack has parents at all and if new taxon is either 
			// a) a root taxon (parent==null)
			// b) or the last stack taxon is not the parent of this taxon
			while (parentStack.size()>0 && (node.getParent() == null || !node.getParent().equals(parentStack.peek()))){
				T nonParent = parentStack.pop();
				nonParent.setRgt(idx++);				
				if (persist){
					treeNodeManager.save(nonParent);
				}
			}
			// the last taxon on stack is the parent or stack is empty. 
			// Next taxon might be a child, so dont set rgt index yet, but put onto stack
			node.setLft(idx++);
			parentStack.push(node);
			
			// flush to database from time to time
			if (idx % 1000 == 0){
				darwinCoreManager.flush();
			}
			
		}
		// finally empty the stack, assign rgt value and persist
		for (T t : parentStack){
			t.setRgt(idx++);				
			if (persist){
				treeNodeManager.save(t);
			}
		}
	}	
}
