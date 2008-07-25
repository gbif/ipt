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

package org.gbif.provider.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.job.OccDbUploadJob;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.CoreViewMapping;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ViewMapping;


/**
 * Constant values used throughout the application.
 */
public class PathUtil {
	protected static final Log log = LogFactory.getLog(PathUtil.class);
    public static String WEBAPP_DIR;
    private static File WEBAPP_DIR_FILE;
    
    private PathUtil(){
    }
    
    /**
     * only sets base dir once when first called
     * @param webappDir
     */
    public static void setWebappDir(String webappDir) {
    	if (WEBAPP_DIR == null && webappDir != null){
    		log.info("set webapp directory to "+webappDir);
    		WEBAPP_DIR = webappDir;
    		WEBAPP_DIR_FILE = new File(webappDir);
    	}
	}

	public static File getDataDir(DatasourceBasedResource resource) throws IOException{
		return getDataDir(resource, false);
	}
	public static File getDataDir(DatasourceBasedResource resource, boolean createFile) throws IOException{
    	File dir = new File(WEBAPP_DIR_FILE, String.format("data/%s", resource.getServiceName()));
		if (createFile){
			FileUtils.forceMkdir(dir);			
		}
		return dir;
    }

    public static File getDumpFile(DatasourceBasedResource resource, Extension extension, boolean createFile) throws IOException{    	
		File file = new File(getDataDir(resource, createFile), String.format("%s.txt", extension.getTablename()));
		if (createFile){
			file.createNewFile();
		}
		return file;
	}    
    
    public static File getDumpArchive(DatasourceBasedResource resource, boolean createFile) throws IOException{
		File file = new File(getDataDir(resource, createFile), "data.zip");
		if (createFile){
			file.createNewFile();
		}
		return file;    	
    }
    
}
