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
		String select = String.format("SELECT dc.id %s FROM Darwin_Core dc where dc.resource_fk=%s order by id", buildPropertySelect(view), view.getResourceId());			
		return dumpFile(file, select);
	}
	private File dumpTaxCore(ExtensionMapping view) throws IOException, SQLException{
		File file = cfg.getArchiveFile(view.getResourceId(), view.getExtension());
		String select = String.format("SELECT id %s FROM taxon where resource_fk=%s order by id", buildPropertySelect(view), view.getResourceId());
		//FIXME: hacking the dump with a hardcoded select. Not too bad, but well...
		select = String.format("select t.ID ,t.NOMENCLATURAL_CODE ,t.LABEL ,t.RANK ,t.LOCAL_ID ,t.GUID ,t.LINK ,t.NOTES ,t.TAXONOMIC_STATUS ,t.NOMENCLATURAL_STATUS ,t.NOMENCLATURAL_REFERENCE , t.accepted_taxon_id, acc.label acceptedTaxon, t.taxonomic_parent_id,  p.label parentTaxon   from taxon t left join taxon acc on t.accepted_taxon_fk = acc.id left join taxon p on t.parent_fk = p.id   where t.resource_fk=%s order by id", view.getResourceId());		 
		return dumpFile(file, select);
	}
	private File dumpExtension(ExtensionMapping view) throws IOException, SQLException{
		File file = cfg.getArchiveFile(view.getResourceId(), view.getExtension());
		String select = String.format("SELECT coreid %s FROM %s where resource_fk=%s order by coreid", buildPropertySelect(view), namingStrategy.extensionTableName(view.getExtension()), view.getResourceId());			
		return dumpFile(file, select);
	}
	private File dumpTaxa(Long resourceId) throws IOException, SQLException{
		File file = new File(cfg.getArchiveFile(resourceId).getParentFile(), "archive-extra-taxa.txt");
		String select = String.format("select dc.LOCAL_ID as DwcLocalId, dc.guid as DwcGuid, t.GUID ,t.LABEL as ScientificName, t.RANK as DwcRank, acc.guid as acceptedTaxonGuid, acc.label acceptedTaxon, p.guid as parentTaxonGuid,  p.label parentTaxon,      dc.binomial, dc.higher_taxon_iD, dc.higher_taxon, dc.kingdom, dc.phylum, dc.classs, dc.orderrr, dc.family, dc.genus, dc.subgenus, dc.specific_epithet, dc.taxon_rank, dc.infraspecific_epithet, dc.scientific_name_authorship, dc.nomenclatural_code, dc.taxon_according_to, dc.name_published_in, dc.taxonomic_status, dc.nomenclatural_status, dc.accepted_taxon_iD, dc.accepted_taxon, dc.basionym_iD, dc.basionym     FROM taxon t left join taxon acc on t.accepted_taxon_fk = acc.id left join taxon p on t.parent_fk = p.id  left join darwin_core dc on dc.taxon_fk = t.id   where t.resource_fk=%s order by t.id", resourceId);		 
		return dumpFile(file, select);
	}
	private String buildPropertySelect(ExtensionMapping view){
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
