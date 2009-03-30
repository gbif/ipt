package org.gbif.provider.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.gbif.provider.model.hibernate.IptNamingStrategy;
import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.DataArchiveManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.TabFileWriter;
import org.gbif.provider.util.XmlFileUtils;
import org.gbif.provider.util.ZipUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

public class DataArchiveManagerImpl extends BaseManager implements DataArchiveManager{
	// H2 supports tab file dumps out of the box
	// Apart from the default CSV, it allows to override delimiters so pure tab files can be created like this:
	// CALL CSVWRITE('/Users/markus/Desktop/test.txt', 'select id, label from taxon order by label', 'utf8', '	', '')
	private static final String CSVWRITE = "CALL CSVWRITE('%s', '%s', 'utf8')";
	private static final String DESCRIPTOR_TEMPLATE = "/WEB-INF/pages/dwcarchive-meta.ftl";
	
	@Autowired
	protected AppConfig cfg;
	@Autowired
	protected AnnotationManager annotationManager;
	@Autowired
	private IptNamingStrategy namingStrategy;
	@Autowired
	private Configuration freemarker;

	public File createArchive(DataResource resource) throws IOException, IllegalStateException {
		if (resource.getCoreMapping()==null){
			throw new IllegalStateException("Resource needs at least a core mapping to create a data archive");
		}
		Map<File, ExtensionMapping> archiveFiles = new HashMap<File, ExtensionMapping>();		
		// individual archive files
		try {
			if (resource instanceof OccurrenceResource){
				archiveFiles.put(dumpOccCore(resource.getCoreMapping()), resource.getCoreMapping());
			}else if (resource instanceof ChecklistResource){
				archiveFiles.put(dumpTaxCore(resource.getCoreMapping()), resource.getCoreMapping());
			}else{
				log.error("Unknown resource class "+resource.getClass().getCanonicalName());
			}
		}catch (Exception e) {
			annotationManager.annotateResource(resource, "Could not write data archive file for extension "+resource.getCoreMapping().getExtension().getName() +" of resource "+resource.getTitle());				
		}
		for (ExtensionMapping view : resource.getExtensionMappings()){
			try{
				archiveFiles.put(dumpExtension(view), view);
			}catch (Exception e) {
				annotationManager.annotateResource(resource, "Could not write data archive file for extension "+view.getExtension().getName() +" of resource "+resource.getTitle());				
			}
		}
		
		// meta descriptor file
		File descriptor = writeDescriptor(resource, archiveFiles);
		if (!descriptor.exists()){
			throw new IOException("Archive descriptor could not be generated");
		}
		// zip archive
		File archive = cfg.getArchiveFile(resource.getId());
		Set<File> files= new HashSet(archiveFiles.keySet());
		files.add(descriptor);
		File eml = cfg.getEmlFile(resource.getId());
		if (eml.exists()){
			files.add(eml);
		}
		ZipUtil.zipFiles(files, archive);
		
		// also dump all taxa in a separate file
//		try {
//			File taxDump = dumpTaxa(resource.getId());
//			files= new HashSet();
//			files.add(taxDump);
//			archive = new File(archive.getParentFile(), "archive-taxon.zip");
//			ZipUtil.zipFiles(files, archive);
//		} catch (SQLException e) {
//			log.warn("Couldnt dump taxa for resource "+resource.getId(), e);
//		}
		return archive;		
	}
	
	private File writeDescriptor(DataResource resource, Map<File, ExtensionMapping> archiveFiles){
		Map<String, ExtensionMapping> fileMap = new HashMap<String, ExtensionMapping>();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("cfg", cfg);
		data.put("resource", resource);
		for (File f : archiveFiles.keySet()){
			if (archiveFiles.get(f).isCore()){
				if (resource instanceof ChecklistResource){
					data.put("isChecklist", true);
				}else {
					data.put("isChecklist", false);
				}
				data.put("coreView", archiveFiles.get(f));
				data.put("coreFilename", f.getName());
			}else{
				fileMap.put(f.getName(), archiveFiles.get(f));
			}
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
	private File dumpOccCore(ExtensionMapping view) throws IOException, SQLException{
		File file = cfg.getArchiveFile(view.getResourceId(), view.getExtension());
		String select = String.format("SELECT guid, modified, link, source_id %s %s FROM Darwin_Core where resource_fk=%s", buildPropertySelect(view.getResource().getAdditionalIdentifiers()), buildPropertySelect(view), view.getResourceId());			
		return dumpFile(file, select);
	}
	private File dumpTaxCore(ExtensionMapping view) throws IOException, SQLException{
		File file = cfg.getArchiveFile(view.getResourceId(), view.getExtension());
		String select = String.format("SELECT guid, modified, link, source_id %s %s FROM taxon where resource_fk=%s", buildPropertySelect(view.getResource().getAdditionalIdentifiers()), buildPropertySelect(view), view.getResourceId());
		return dumpFile(file, select);
	}
	private File dumpExtension(ExtensionMapping view) throws IOException, SQLException{
		File file = cfg.getArchiveFile(view.getResourceId(), view.getExtension());
		String select = String.format("SELECT guid %s FROM %s where resource_fk=%s order by guid", buildPropertySelect(view), namingStrategy.extensionTableName(view.getExtension()), view.getResourceId());			
		return dumpFile(file, select);
	}
	private String buildPropertySelect(List<String> propertyNames){
		String select = "";
		for (String pn : propertyNames){
			select += ","+getColumnName(pn);
		}
		return select;
	}
	private String buildPropertySelect(ExtensionMapping view){
		String select = "";
		for (ExtensionProperty p : view.getMappedProperties()){
			select += ","+getColumnName(p.getName());
		}
		return select;
	}
	private String getColumnName(String propName){
		String col = namingStrategy.propertyToColumnName(propName);
		// check reserved sql words
		if (col.equalsIgnoreCase("order")){
			col="orderrr as \"ORDER\" ";
		}else if (col.equalsIgnoreCase("class")){
			col="classs as \"CLASS\" ";
		}
		return col;
	}
	private File dumpFile(File file, String select) throws IOException, SQLException{
		if (file.exists()){
			file.delete();
		}
		file.createNewFile();
		log.debug("Created archive file "+file.getAbsolutePath());
		String sql = String.format(CSVWRITE, file.getAbsolutePath(), select);
		log.debug(sql);
		getConnection().prepareStatement(sql).execute();
		return file;
	}

}
