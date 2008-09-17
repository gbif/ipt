package org.gbif.provider.upload;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.gbif.logging.log.I18nDatabaseAppender;
import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class TaskBase{
	protected final Log log = LogFactory.getLog(getClass());
	protected final I18nLog logdb = I18nLogFactory.getLog(getClass());
	@Autowired
	protected AppConfig cfg;
	// needs manual setting when task is created
	private Long userId;
	private OccurrenceResource resource;
	@Autowired
	protected OccResourceManager occResourceManager;

	
	
	protected void initLogging(Integer SourceTypeId){
		log.info(String.format("Starting %s for resource %s", getClass().getSimpleName(), getResourceId()));
		//TODO: set this in the scheduler constructor???
//		MDC.put(I18nDatabaseAppender.MDC_INSTANCE_ID, null);
		if (userId==null){
			log.warn("No user set for this task");			
		}else{
			MDC.put(I18nDatabaseAppender.MDC_USER, userId);			
		}
		MDC.put(I18nDatabaseAppender.MDC_GROUP_ID, getResourceId().intValue());
		MDC.put(I18nDatabaseAppender.MDC_SOURCE_ID, this.hashCode());
		MDC.put(I18nDatabaseAppender.MDC_SOURCE_TYPE, SourceTypeId);
	}
	
	
	private void setUserId(Long userId) {
		if (userId == null){
			throw new NullPointerException();
		}
		this.userId = userId;
	}
	public Long getUserId() {
		return userId;
	}

	private void setResource(OccurrenceResource resource) {
		if (resource == null){
			throw new NullPointerException();
		}
		this.resource=resource;
	}

	public Long getResourceId() {
		return resource.getId();
	}

	public OccurrenceResource getResource() {
		if (resource == null){
			throw new IllegalStateException("Resource is not yet set for this builder!");
		}
		return resource;
	}


	
	public void init(OccurrenceResource res, Long userId) {
		setResource(res);
		setUserId(userId);
	}
	
}
