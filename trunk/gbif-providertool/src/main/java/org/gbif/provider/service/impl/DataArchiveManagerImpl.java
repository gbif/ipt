package org.gbif.provider.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ViewCoreMapping;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.DataArchiveManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.TabFileWriter;
import org.gbif.provider.util.ZipUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class DataArchiveManagerImpl implements DataArchiveManager{
	@Autowired
	protected AppConfig cfg;
	@Autowired
	protected AnnotationManager annotationManager;

	public File createArchive(DataResource resource) throws IOException {
		Set<File> archiveFiles = new HashSet<File>();		
		// individual archive files
		File coreF = cfg.getDumpFile(resource.getId(), resource.getCoreMapping().getExtension());
		archiveFiles.add(coreF);
		
		// zip archive
		File archive = cfg.getDumpArchiveFile(resource.getId());
//		archive.createNewFile();
//		ZipUtil.zipFiles(archiveFiles, archive);
		return archive;		
	}
	
	private File dumpFile(Resource resource){
	// catch (IOException e) {
		//annotationManager.annotateResource(resource, "Couldn't open tab file. Import aborted: "+e.toString());
		return null;
	}
	
	private TabFileWriter getTabFileWriter(ViewMappingBase view) throws IOException{
		// create new file, overwriting existing one
		File file = cfg.getDumpFile(1l, view.getExtension());
		// remove previously existing file
		if (file.exists()){
			file.delete();
		}
		file.createNewFile();
		
		if (view instanceof ViewCoreMapping){
			return new TabFileWriter(file, (ViewCoreMapping) view);
		}else{
			return new TabFileWriter(file, view);				
		}
	}
}
