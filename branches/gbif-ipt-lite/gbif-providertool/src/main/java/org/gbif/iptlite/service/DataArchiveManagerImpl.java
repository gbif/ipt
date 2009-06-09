package org.gbif.iptlite.service;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.FileUtils;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.hibernate.IptNamingStrategy;
import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.DataArchiveManager;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.impl.BaseManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.TabFileWriter;
import org.gbif.provider.util.XmlFileUtils;
import org.gbif.provider.util.ZipUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

public class DataArchiveManagerImpl extends BaseManager implements DataArchiveManager{
	private static final String DESCRIPTOR_TEMPLATE = "/WEB-INF/pages/dwcarchive-meta.ftl";
	
	@Autowired
	protected AppConfig cfg;
	@Autowired
	private Configuration freemarker;
	@Autowired
	private EmlManager emlManager;
	

	public File packageArchive(DataResource resource) throws IOException, IllegalStateException {
		if (resource.getCoreMapping()==null){
			throw new IllegalStateException("Resource needs at least a core mapping to create a data archive");
		}
		Map<File, ExtensionMapping> extensionFiles = new HashMap<File, ExtensionMapping>();		
		Set<File> files= new HashSet<File>();
		// core file
		File coreFile = cfg.getArchiveFile(resource.getCoreMapping().getResourceId(), resource.getCoreMapping().getExtension());
		// archive extension files
		for (ExtensionMapping view : resource.getExtensionMappings()){
			File extFile = cfg.getArchiveFile(resource.getId(), view.getExtension());
			extensionFiles.put(extFile, view);
		}
		files.addAll(extensionFiles.keySet());
		files.add(coreFile);
		
		// eml metadata
		Eml eml = emlManager.publishNewEmlVersion(resource);
		File emlFile = cfg.getEmlFile(resource.getId());
		if (emlFile.exists()){
			files.add(emlFile);
		}else{
			log.warn("No EML file existing to include in archive");
		}
		
		// meta descriptor file
		File descriptor = writeDescriptor(resource, extensionFiles, coreFile);
		if (descriptor.exists()){
			files.add(descriptor);
		}else{
			log.error("Archive descriptor could not be generated");
		}
		
		// zip archive
		File archive = cfg.getArchiveFile(resource.getId());
		ZipUtil.zipFiles(files, archive);
		
		return archive;		
	}
	
	private File writeDescriptor(DataResource resource, Map<File, ExtensionMapping> archiveFiles, File coreFile){
		Map<String, ExtensionMapping> fileMap = new HashMap<String, ExtensionMapping>();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("cfg", cfg);
		data.put("resource", resource);
		data.put("rowType", resource.getCoreMapping().getExtension().getRowType());
		// get all properties but the GUID one (SampleID or TaxonID)
		String guidPropertyName = resource.getDwcGuidPropertyName();
		data.put("guidPropertyName", guidPropertyName);
		List<ExtensionProperty> props = resource.getCoreMapping().getMappedProperties();
		for (ExtensionProperty p : props){
			if (p.getName().equalsIgnoreCase(guidPropertyName)){
				props.remove(p);
				break;
			}
		}
		data.put("coreProperties", props);
		data.put("coreFilename", coreFile.getName());
		
		for (File f : archiveFiles.keySet()){
			fileMap.put(f.getName(), archiveFiles.get(f));
		}
		data.put("fileMap", fileMap);
		File descriptor = cfg.getArchiveDescriptor(resource.getId());
		try {
			String page = FreeMarkerTemplateUtils.processTemplateIntoString(freemarker.getTemplate(DESCRIPTOR_TEMPLATE), data);
			Writer out = XmlFileUtils.startNewUtf8XmlFile(descriptor);
	        out.write(page);
	        out.close();
			log.info("Created DarwinCore archive descriptor with "+fileMap.size()+" files for resource "+resource.getTitle());
		} catch (TemplateException e) {
			log.error("Freemarker template exception", e);
		} catch (IOException e) {
			log.error("IO Error when writing dwc archive descriptor", e);
		}
		return descriptor;
	}
}
