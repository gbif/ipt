package org.gbif.provider.task;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.util.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class TaskBase<T, R extends DataResource> implements Task<T>{
	protected final Log log = LogFactory.getLog(getClass());
	@Autowired
	protected AppConfig cfg;
	@Autowired
	protected AnnotationManager annotationManager;
	// needs manual setting when task is created
	private Long resourceId;
	private String title;
	
	protected GenericResourceManager<R> resourceManager;

	
	
	public TaskBase(GenericResourceManager<R> resourceManager) {
		super();
		this.resourceManager = resourceManager;
	}


	public void init(Long resourceId) {
		if (resourceId == null){
			throw new NullPointerException("ResourceID required");
		}
		this.resourceId=resourceId;
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
