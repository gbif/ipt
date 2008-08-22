package org.gbif.provider.upload;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.gbif.logging.log.I18nDatabaseAppender;
import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.JobUtils;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class TaskBase{
	protected final Log log = LogFactory.getLog(getClass());
	protected final I18nLog logdb = I18nLogFactory.getLog(getClass());
	@Autowired
	private AppConfig cfg;
	// needs manual setting via prepare()
	private Long userId;
	private Long resourceId;
	private OccurrenceResource resource;
	@Autowired
	protected OccResourceManager occResourceManager;

	
	
	protected void initLogging(Integer SourceTypeId){
		log.info(String.format("Starting %s for resource %s", getClass().getSimpleName(), resourceId));
		//TODO: set this in the scheduler constructor???
//		MDC.put(I18nDatabaseAppender.MDC_INSTANCE_ID, null);
		MDC.put(I18nDatabaseAppender.MDC_USER, userId);
		MDC.put(I18nDatabaseAppender.MDC_GROUP_ID, JobUtils.getJobGroup(resourceId));
		MDC.put(I18nDatabaseAppender.MDC_SOURCE_ID, this.hashCode());
		MDC.put(I18nDatabaseAppender.MDC_SOURCE_TYPE, SourceTypeId);		
	}
	
	
	private void setUserId(Long userId) {
		if (userId == null){
			throw new NullPointerException();
		}
		if (this.userId != null){
			throw new IllegalStateException("UserId is already set for this builder!");
		}
		this.userId = userId;
	}
	public Long getUserId() {
		return userId;
	}

	private void setResourceId(Long resourceId) {
		if (this.resourceId != null){
			throw new IllegalStateException("Resource is already set for this builder!");
		}
		if (resourceId == null){
			throw new NullPointerException();
		}
		this.resourceId=resourceId;
	}

	public Long getResourceId() {
		return resourceId;
	}

	public OccurrenceResource getResource() {
		if (resourceId == null){
			throw new IllegalStateException("Resource is not yet set for this builder!");
		}
		if (resource == null){
			resource = occResourceManager.get(resourceId);
		}
		return resource;
	}


	
	public void init(Long resourceId, Long userId) {
		setResourceId(resourceId);
		setUserId(userId);
	}
	
}
