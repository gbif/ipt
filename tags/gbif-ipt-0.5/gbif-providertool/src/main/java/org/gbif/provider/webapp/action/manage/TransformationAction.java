package org.gbif.provider.webapp.action.manage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.GenericResourceRelatedManager;
import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.SourceManager;
import org.gbif.provider.webapp.action.BaseAction;
import org.gbif.provider.webapp.action.BaseDataResourceAction;
import org.gbif.provider.webapp.action.BaseResourceAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.Preparable;


public class TransformationAction extends BaseDataResourceAction implements Preparable{
	private static final long serialVersionUID = -3698914322584074200L;
	private final Map<String, String> transformationTypes = new HashMap<String, String>()   
    {  
        {  
            put("union", "Union of columns");
            put("hierarchy", "Hierarchy normalisation");
            put("lookup", "ID lookup");
            put("vocabulary", "Vocabulary translation");  
            put("sql", "SQL view");  
        }  
    };  	
	
    @Override
	public void prepare() {
		super.prepare();
	}

	/**
     * Default method - returns "input"
     * @return "input"
     */
    public String execute() {
        return SUCCESS;
    }

	public Map<String, String> getTransformationTypes() {
		return transformationTypes;
	}

}
