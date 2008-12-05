package org.gbif.provider.task;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.gbif.logging.log.I18nDatabaseAppender;
import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.impl.DataResourceManagerHibernate;
import org.gbif.provider.util.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class TaskBase<T, R extends DataResource> implements Task<T>{
	protected final Log log = LogFactory.getLog(getClass());
	protected final I18nLog logdb = I18nLogFactory.getLog(getClass());
	@Autowired
	protected AppConfig cfg;
	// needs manual setting when task is created
	private Long userId;
	private Long resourceId;
	private String title;
	
	protected GenericResourceManager<R> resourceManager;

	
	
	public TaskBase(GenericResourceManager<R> resourceManager) {
		super();
		this.resourceManager = resourceManager;
	}


	public void init(Long resourceId, Long userId) {
		if (resourceId == null){
			throw new NullPointerException("ResourceID required");
		}
		this.resourceId=resourceId;
		this.userId = userId;
		initLogging();
	}

	
	private void initLogging(){
		log.info(String.format("Starting %s for resource %s", getClass().getSimpleName(), resourceId));
		//TODO: set this in the scheduler constructor???
//		MDC.put(I18nDatabaseAppender.MDC_INSTANCE_ID, null);
		if (userId==null){
			log.warn("No user set for this task");			
		}else{
			MDC.put(I18nDatabaseAppender.MDC_USER, userId);			
		}
		MDC.put(I18nDatabaseAppender.MDC_GROUP_ID, resourceId.intValue());
		MDC.put(I18nDatabaseAppender.MDC_SOURCE_ID, this.hashCode());
		MDC.put(I18nDatabaseAppender.MDC_SOURCE_TYPE, this.taskTypeId());
	}
	
	
	public Long getUserId() {
		return userId;
	}

	public Long getResourceId() {
		return resourceId;
	}
	public R loadResource() {
		return resourceManager.get(resourceId);
	}

	public String getTitle(){
		if (title==null){
			// lazy load title
			title = StringUtils.trimToEmpty(loadResource().getTitle());
		}
		return title;
	}
}
