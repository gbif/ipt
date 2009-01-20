package org.gbif.provider.service.impl;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

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
import org.gbif.provider.util.ZipUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class DataArchiveManagerImpl extends BaseManager implements DataArchiveManager{
	// H2 supports tab file dumps out of the box
	// Apart from the default CSV, it allows to override delimiters so pure tab files can be created like this:
	// CALL CSVWRITE('/Users/markus/Desktop/test.txt', 'select id, label from taxon order by label', 'utf8', '	', '')
	private static final String CSVWRITE = "CALL CSVWRITE('%s', '%s', 'utf8')";
	@Autowired
	protected AppConfig cfg;
	@Autowired
	protected AnnotationManager annotationManager;
	@Autowired
	private IptNamingStrategy namingStrategy;

	public File createArchive(DataResource resource) throws IOException {
		Set<File> archiveFiles = new HashSet<File>();		
		// individual archive files
		try {
			if (resource instanceof OccurrenceResource){
				archiveFiles.add(dumpOccCore(resource.getCoreMapping()));
			}else if (resource instanceof ChecklistResource){
				archiveFiles.add(dumpTaxCore(resource.getCoreMapping()));
			}else{
				log.error("Unknown resource class "+resource.getClass().getCanonicalName());
			}
		}catch (Exception e) {
			annotationManager.annotateResource(resource, "Could not write data archive file for extension "+resource.getCoreMapping().getExtension().getName() +" of resource "+resource.getTitle());				
		}
		for (ViewExtensionMapping view : resource.getExtensionMappings()){
			try{
				archiveFiles.add(dumpExtension(view));
			}catch (Exception e) {
				annotationManager.annotateResource(resource, "Could not write data archive file for extension "+view.getExtension().getName() +" of resource "+resource.getTitle());				
			}
		}
		// zip archive
		File archive = cfg.getDumpArchiveFile(resource.getId());
		ZipUtil.zipFiles(archiveFiles, archive);
		return archive;		
	}
	
	
	private File dumpOccCore(ViewCoreMapping view) throws IOException, SQLException{
		File file = cfg.getDumpFile(view.getResourceId(), view.getExtension());
		String select = String.format("SELECT dc.id %s FROM Darwin_Core dc where dc.resource_fk=%s order by id", buildPropertySelect(view), view.getResourceId());			
		return dumpFile(file, select);
	}
	private File dumpTaxCore(ViewCoreMapping view) throws IOException, SQLException{
		File file = cfg.getDumpFile(view.getResourceId(), view.getExtension());
		String select = String.format("SELECT id %s FROM taxon where resource_fk=%s order by id", buildPropertySelect(view), view.getResourceId());
		//FIXME: hacking the dump with a hardcoded select. Not too bad, but well...
		select = String.format("select t.ID ,t.NOMENCLATURAL_CODE ,t.LABEL ,t.RANK ,t.LOCAL_ID ,t.GUID ,t.LINK ,t.NOTES ,t.TAXONOMIC_STATUS ,t.NOMENCLATURAL_STATUS ,t.NOMENCLATURAL_REFERENCE , t.accepted_taxon_id, acc.label acceptedTaxon, t.taxonomic_parent_id,  p.label parentTaxon   from taxon t left join taxon acc on t.accepted_taxon_fk = acc.id left join taxon p on t.parent_fk = p.id   where t.resource_fk=%s order by id", view.getResourceId());		 
		return dumpFile(file, select);
	}
	private File dumpExtension(ViewExtensionMapping view) throws IOException, SQLException{
		File file = cfg.getDumpFile(view.getResourceId(), view.getExtension());
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
