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
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ViewCoreMapping;
import org.gbif.provider.model.ViewExtensionMapping;
import org.gbif.provider.model.ViewMappingBase;
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

	public File createArchive(DataResource resource) throws IOException {
		Map<File, ViewMappingBase> archiveFiles = new HashMap<File, ViewMappingBase>();		
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
		for (ViewExtensionMapping view : resource.getExtensionMappings()){
			try{
				archiveFiles.put(dumpExtension(view), view);
			}catch (Exception e) {
				annotationManager.annotateResource(resource, "Could not write data archive file for extension "+view.getExtension().getName() +" of resource "+resource.getTitle());				
			}
		}
		
		// meta descriptor file
		File descriptor = writeDescriptor(resource, archiveFiles);
		
		// zip archive
		File archive = cfg.getArchiveFile(resource.getId());
		Set<File> files= new HashSet(archiveFiles.keySet());
		files.add(descriptor);
		files.add(cfg.getEmlFile(resource.getId()));
		ZipUtil.zipFiles(files, archive);
		return archive;		
	}
	
	private File writeDescriptor(DataResource resource, Map<File, ViewMappingBase> archiveFiles){
		Map<String, ViewMappingBase> fileMap = new HashMap<String, ViewMappingBase>();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("cfg", cfg);
		data.put("resource", resource);
		for (File f : archiveFiles.keySet()){
			if (archiveFiles.get(f).isCore()){
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
	private File dumpOccCore(ViewCoreMapping view) throws IOException, SQLException{
		File file = cfg.getArchiveFile(view.getResourceId(), view.getExtension());
		String select = String.format("SELECT dc.id %s FROM Darwin_Core dc where dc.resource_fk=%s order by id", buildPropertySelect(view), view.getResourceId());			
		return dumpFile(file, select);
	}
	private File dumpTaxCore(ViewCoreMapping view) throws IOException, SQLException{
		File file = cfg.getArchiveFile(view.getResourceId(), view.getExtension());
		String select = String.format("SELECT id %s FROM taxon where resource_fk=%s order by id", buildPropertySelect(view), view.getResourceId());
		//FIXME: hacking the dump with a hardcoded select. Not too bad, but well...
		select = String.format("select t.ID ,t.NOMENCLATURAL_CODE ,t.LABEL ,t.RANK ,t.LOCAL_ID ,t.GUID ,t.LINK ,t.NOTES ,t.TAXONOMIC_STATUS ,t.NOMENCLATURAL_STATUS ,t.NOMENCLATURAL_REFERENCE , t.accepted_taxon_id, acc.label acceptedTaxon, t.taxonomic_parent_id,  p.label parentTaxon   from taxon t left join taxon acc on t.accepted_taxon_fk = acc.id left join taxon p on t.parent_fk = p.id   where t.resource_fk=%s order by id", view.getResourceId());		 
		return dumpFile(file, select);
	}
	private File dumpExtension(ViewExtensionMapping view) throws IOException, SQLException{
		File file = cfg.getArchiveFile(view.getResourceId(), view.getExtension());
		String select = String.format("SELECT coreid %s FROM %s where resource_fk=%s order by coreid", buildPropertySelect(view), namingStrategy.extensionTableName(view.getExtension()), view.getResourceId());			
		return dumpFile(file, select);
	}
	private String buildPropertySelect(ViewMappingBase view){
		String select = "";
		for (ExtensionProperty p : view.getMappedProperties()){
			String col = namingStrategy.propertyToColumnName(p.getName());
			// check reserved sql words
			if (col.equalsIgnoreCase("order")){
				col="orderrr as \"ORDER\" ";
			}else if (col.equalsIgnoreCase("class")){
				col="classs as \"CLASS\" ";
			}
			select += ","+col;
		}
		return select;
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
