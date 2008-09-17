/***************************************************************************
* Copyright (C) 2008 Global Biodiversity Information Facility Secretariat.
* All Rights Reserved.
*
* The contents of this file are subject to the Mozilla Public
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.

***************************************************************************/

package org.gbif.provider.webapp.action.manage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.struts2.interceptor.SessionAware;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.ViewExtensionMapping;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.DatasourceInspectionManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.Preparable;

public class SourceMappingAction extends BaseOccurrenceResourceAction implements Preparable, SessionAware{
	private static Integer FIXED_TERMS_IDX = 1000;
	@Autowired
    private DatasourceInspectionManager datasourceInspectionManager;
	@Autowired
	@Qualifier("extensionManager")
    private GenericManager<Extension> extensionManager;
	@Autowired
	@Qualifier("viewMappingManager")
    private GenericManager<ViewMappingBase> viewMappingManager;
	// persistent stuff
	private Long mapping_id;
	private Long extension_id;
	private ViewMappingBase view;
	private OccurrenceResource resource;
	// temp stuff
    private SortedMap<String, String> columnOptions;
    private Set<ViewMappingBase> existingDbViews;
    private Map session;
    // file upload
    private File file;
    private String fileContentType;
    private String fileFileName;

    
	@SuppressWarnings("unchecked")
	public void prepare() throws Exception {
		assert(resource_id!=null && (mapping_id!=null || extension_id!=null));
		// get resource
		resource = occResourceManager.get(resource_id);

		// get/create view mapping
        if (mapping_id != null) {
        	view = viewMappingManager.get(mapping_id);
        }else if (extension_id != null) {
        	view = new ViewExtensionMapping();
        	view.setResource(resource);
        	view.setExtension(extensionManager.get(extension_id));
        	viewMappingManager.save(view);
        	mapping_id = view.getId();
        }
    	// prepareExistingDbViews
		existingDbViews = new HashSet<ViewMappingBase>();
		for (ViewMappingBase vm : resource.getAllMappings()){
			if (!vm.isMappedToFile() && vm.hasValidSource()){
				existingDbViews.add(vm);
			}
		}
		
        // generate basic column mapping options
		if (view.hasValidSource()){
	        columnOptions = new TreeMap<String, String>();
	    	List<String> viewColumnHeaders;
			try {
				viewColumnHeaders = datasourceInspectionManager.getHeader(view);
				for (String head : viewColumnHeaders){
					columnOptions.put(head, head);
				}
			} catch (Exception e) {
				log.debug("Cant read datasource column headers", e);
			}
		}
	}
	
    public String execute() {
		return SUCCESS;
    }

    
    public String upload() throws Exception {
        if (this.cancel != null) {
            return "cancel";
        }
        if (delete != null) {
            return delete();
        }
		assert(resource_id!=null && mapping_id!=null && file!=null);
        // the directory to upload to
		File targetFile = cfg.getSourceFile(resource_id, view.getExtension());
		log.debug(String.format("Uploading source file for resource %s to file %s",resource.getId(), targetFile.getAbsolutePath()));
        //retrieve the file data
        InputStream stream = new FileInputStream(file);

        //write the file to the file specified
        OutputStream bos = new FileOutputStream(targetFile);
        int bytesRead;
        byte[] buffer = new byte[8192];

        while ((bytesRead = stream.read(buffer, 0, 8192)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }

        bos.close();
        stream.close();

        // place the data into the request for retrieval on next page
        getRequest().setAttribute("location", targetFile.getAbsolutePath());
        
        // process file
		view.setSourceFile(targetFile);
		List<String> headers = datasourceInspectionManager.getHeader(view);
		log.info(String.format("Tab file %s uploaded with %s columns", targetFile.getAbsolutePath(), headers .size()));
		if (headers.size() > 1){
			// save file in view mapping
	        viewMappingManager.save(view);
	        saveMessage(getText("mapping.sourceFileUploaded", String.valueOf(headers.size())));
		}else{
			view.setSourceFile(null);
	        viewMappingManager.save(view);
	        saveMessage(getText("mapping.sourceFileBroken", String.valueOf(headers.size())));
		}
		
		prepare();
		return SUCCESS;
    }

    public String save() throws Exception {
        if (cancel != null) {
        	if (view.hasValidSource()){
        		return SUCCESS;
        	}
            return "cancel";
        }
        if (delete != null) {
            return delete();
        }
        
        // cascade-save view mapping
        boolean isNew = (view.getId() == null);
        view = viewMappingManager.save(view);
        mapping_id = view.getId(); 
        String key = (isNew) ? "mapping.added" : "mapping.updated";
        saveMessage(getText(key));

		return SUCCESS;
    }
    
    
    public String delete() {
    	resource.removeExtensionMapping(view);
    	viewMappingManager.remove(view.getId());
        saveMessage(getText("viewMapping.deleted"));
        return CANCEL;
    }

    
    
    
    
    
	public OccurrenceResource getResource() {
		return resource;
	}

	public Long getMapping_id() {
		return mapping_id;
	}

	public void setMapping_id(Long mapping_id) {
		this.mapping_id = mapping_id;
	}

	public Long getExtension_id() {
		return extension_id;
	}

	public void setExtension_id(Long extension_id) {
		this.extension_id = extension_id;
	}
	
	public SortedMap<String, String> getColumnOptions() {
		return columnOptions;
	}

	public void setSession(Map session) {
		this.session=session;
	}

    public void setFile(File file) {
        this.file = file;
    }

    public void setFileContentType(String fileContentType) {
        this.fileContentType = fileContentType;
    }

    public void setFileFileName(String fileFileName) {
        this.fileFileName = fileFileName;
    }

    public File getFile() {
        return file;
    }

    public String getFileContentType() {
        return fileContentType;
    }

    public String getFileFileName() {
        return fileFileName;
    }

	public Set<ViewMappingBase> getExistingDbViews() {
		return existingDbViews;
	}

	public ViewMappingBase getView() {
		return view;
	}

	public void setView(ViewMappingBase view) {
		this.view = view;
	}
    
}
