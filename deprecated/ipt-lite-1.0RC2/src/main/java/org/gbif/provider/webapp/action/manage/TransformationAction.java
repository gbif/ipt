package org.gbif.provider.webapp.action.manage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.ListUtils;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.SourceBase;
import org.gbif.provider.model.SourceFile;
import org.gbif.provider.model.SourceSql;
import org.gbif.provider.model.Transformation;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.TransformationType;
import org.gbif.provider.service.GenericResourceRelatedManager;
import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.SourceManager;
import org.gbif.provider.service.TransformationManager;
import org.gbif.provider.webapp.action.BaseAction;
import org.gbif.provider.webapp.action.BaseDataResourceAction;
import org.gbif.provider.webapp.action.BaseResourceAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.Preparable;


public class TransformationAction extends BaseDataResourceAction implements Preparable{
	private static final long serialVersionUID = -3698914322584074200L;
	
	@Autowired
	private TransformationManager transformationManager;
	private Long tid;
	private final Map<Integer, String> transformationTypes = TransformationType.htmlSelectMap;
	private List<Transformation> transformations;
	
    @Override
	public void prepare() {
		super.prepare();
	}

	/**
     * Default method - returns "input"
     * @return "input"
     */
    public String execute() {
    	transformations = transformationManager.getAll(resource_id);
    	Collections.sort(transformations);
        return SUCCESS;
    }
    
    public String delete() {
    	transformationManager.remove(tid);
        return execute();
    }

	public Map<Integer, String> getTransformationTypes() {
		return transformationTypes;
	}

	public Long getTid() {
		return tid;
	}

	public void setTid(Long tid) {
		this.tid = tid;
	}

	public List<Transformation> getTransformations() {
		return transformations;
	}

}
