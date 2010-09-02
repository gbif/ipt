package org.gbif.ipt.task;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveField;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.dwc.text.ArchiveWriter;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.manage.SourceManager;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import freemarker.template.TemplateException;

public class GenerateDwca implements ReportingTask<Integer>{
	private final Resource resource;
	private final DataDir dataDir;
	private final SourceManager sourceManager;
	private int records=0;
	private Archive archive;
	private File dwcaFolder;
	
	@Inject
	public GenerateDwca(@Assisted Resource resource, DataDir dataDir, SourceManager sourceManager) {
		super();
		this.resource = resource;
		this.dataDir=dataDir;
		this.sourceManager=sourceManager;
	}

	public String state() {
		return "Processing record "+records;
	}
	
	public Integer call() throws Exception {
		try {
			// create a temp dir to copy all dwca files to
			dwcaFolder = dataDir.tmpDir();
			archive = new Archive();
			// create data files
			createDataFiles();
			// copy eml file
			copyEmlFile();
			// create meta.xml
			createMetaFile();
			
			return records;
		} catch (Exception e) {
			throw new GeneratorException(e);
		}
	}
	
	private void createDataFiles() throws IOException, GeneratorException{
		if (resource.getCore()==null || resource.getCore().getSource()==null){
			throw new GeneratorException("Core is not mapped");
		}
		addDataFile(resource.getCore(), true);
		for (ExtensionMapping mapping : resource.getExtensions()){
			addDataFile(mapping, false);
		}
	}
	private void addDataFile(ExtensionMapping mapping, boolean isCore) throws IOException {
		// open new file writer
		File dataFile = new File(dwcaFolder, mapping.getExtension().getName().toLowerCase().replaceAll("\\s", "_")+".txt");
		Writer writer = org.gbif.file.FileUtils.startNewUtf8File(dataFile);
		// add concepts to archive
		ArchiveFile af = new ArchiveFile();
		af.setRowType(mapping.getExtension().getRowType());
		af.setId(af.getId());
		for (ArchiveField f : af.getFields().values()){
			af.addField(f);
		}
		if (isCore){
			archive.setCore(af);
		}else{
			archive.addExtension(af);
		}
		// dump file
		Iterator<String[]> iter = mapping.getSource().iterator();
		while (iter.hasNext()){
			String[] row = iter.next();
			writer.write(StringUtils.join(row)+"\n");
		}
	}
	private ArchiveField buildField(Integer column){
		ArchiveField f = new ArchiveField();
		f.setIndex(column);
		return f;
	}
	private void createMetaFile() throws IOException, TemplateException{
		ArchiveWriter writer = new ArchiveWriter();
		writer.writeMetaFile(new File(dwcaFolder,"meta.xml"), archive);
	}
	private void copyEmlFile() throws IOException{
		FileUtils.copyFile(dataDir.resourceEmlFile(resource.getShortname(), null), new File(dwcaFolder,"eml.xml"));
	}
	public int getRecords() {
		return records;
	}



}
