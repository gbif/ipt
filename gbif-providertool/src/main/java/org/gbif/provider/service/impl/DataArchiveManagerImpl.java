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
		archive.createNewFile();
		ZipUtil.zipFiles(archiveFiles, archive);
		return archive;		
	}
	
	private File dumpOccCore(ViewCoreMapping view) throws IOException, SQLException{
		File file = cfg.getDumpFile(view.getResourceId(), view.getExtension());
		String sql = String.format("CALL CSVWRITE('%s', 'SELECT dc.id %s FROM Darwin_Core dc where dc.resource_fk=%s order by id')", file.getAbsolutePath(), buildPropertySelect(view), view.getResourceId());			
		return dumpFile(file, sql);
	}
	private File dumpTaxCore(ViewCoreMapping view) throws IOException, SQLException{
		File file = cfg.getDumpFile(view.getResourceId(), view.getExtension());
		String sql = String.format("CALL CSVWRITE('%s', 'SELECT id %s FROM taxon where resource_fk=%s order by id')", file.getAbsolutePath(), buildPropertySelect(view), view.getResourceId());			
		return dumpFile(file, sql);
	}
	private File dumpExtension(ViewExtensionMapping view) throws IOException, SQLException{
		File file = cfg.getDumpFile(view.getResourceId(), view.getExtension());
		String sql = String.format("CALL CSVWRITE('%s', 'SELECT coreid %s FROM %s where resource_fk=%s order by coreid')", file.getAbsolutePath(), buildPropertySelect(view), namingStrategy.extensionTableName(view.getExtension()), view.getResourceId());			
		return dumpFile(file, sql);
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
	private File dumpFile(File file, String sql) throws IOException, SQLException{
		if (file.exists()){
			file.delete();
		}
		file.createNewFile();
		log.debug("Created archive file "+file.getAbsolutePath());
		System.out.println(sql);
		// CALL CSVWRITE('test.csv', 'SELECT * FROM TEST');
		getConnection().prepareStatement(sql).execute();
		//annotationManager.annotateResource(resource, "Couldn't open tab file. Import aborted: "+e.toString());
		return file;
	}

}
