package org.gbif.ipt.task;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.gbif.dwc.terms.ConceptTerm;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveField;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.dwc.text.ArchiveWriter;
import org.gbif.file.CompressionUtil;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.ImportException;
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
			addEmlFile();
			// create meta.xml
			createMetaFile();
			// zip archive and copy to resource folder
			bundleArchive();
			return records;
		} catch (Exception e) {
			throw new GeneratorException(e);
		}
	}
	
	private void bundleArchive() throws IOException {
		// create zip
		File zip = dataDir.tmpFile("dwca",".zip");
		CompressionUtil.zipDir(dwcaFolder, zip);
		// move to data dir
		File target = dataDir.resourceDwcaFile(resource.getShortname());
		if (target.exists()){
			target.delete();
		}
		FileUtils.moveFile(zip, target);
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
	private void addDataFile(ExtensionMapping mapping, boolean isCore) throws IOException, GeneratorException {
		// make sure we have an id column
		if (mapping.getIdColumn()==null){
			throw new GeneratorException("ID column for mapping "+mapping.getExtension().getTitle()+" is missing!");
		}
		// open new file writer
		File dataFile = new File(dwcaFolder, mapping.getExtension().getName().toLowerCase().replaceAll("\\s", "_")+".txt");
		Writer writer = org.gbif.file.FileUtils.startNewUtf8File(dataFile);
		// add concepts to archive
		ArchiveFile af = ArchiveFile.buildTabFile();
		af.setRowType(mapping.getExtension().getRowType());
		af.setEncoding("utf-8");
		af.setDateFormat("YYYY-MM-DD");
		// create new meta.xml via archive
		af.setId(buildField(0));
		List<ArchiveField> newColumns = new ArrayList<ArchiveField>();  
		for (ArchiveField f : mapping.getFields()){
			Integer idx = null;
			if (f.getIndex()!=null){
				newColumns.add(f);
				idx = newColumns.size();
			}
			ArchiveField f2 = buildField(idx);
			f2.setDefaultValue(f.getDefaultValue());
			af.addField(f2);
		}
		// dump file
		Iterator<String[]> iter = mapping.getSource().iterator();
		int rowSize = newColumns.size();
		while (iter.hasNext()){
			String[] in = iter.next();
			String[] row = new String[rowSize];
			row[0]=in[mapping.getIdColumn()];
			int idx=1;
			for (ArchiveField f : newColumns){
				row[idx]=in[f.getIndex()];
			}
			writer.write(tabRow(row));
		}
		// add source file location
		af.addLocation(dataFile.getName());
		// add to archive
		if (isCore){
			archive.setCore(af);
		}else{
			archive.addExtension(af);
		}		
	}
	private ArchiveField buildField(Integer column){
		ArchiveField f = new ArchiveField();
		f.setIndex(column);
		return f;
	}
	private String tabRow(String[] columns){
		//TODO: escape \t \n \r chars !!!
		return StringUtils.join(columns, '\t');
	}
	private void createMetaFile() throws IOException, TemplateException{
		ArchiveWriter writer = new ArchiveWriter();
		writer.writeMetaFile(new File(dwcaFolder,"meta.xml"), archive);
	}
	private void addEmlFile() throws IOException{
		FileUtils.copyFile(dataDir.resourceEmlFile(resource.getShortname(), null), new File(dwcaFolder,"eml.xml"));
		archive.setMetadataLocation("eml.xml");
	}
	public int getRecords() {
		return records;
	}



}
