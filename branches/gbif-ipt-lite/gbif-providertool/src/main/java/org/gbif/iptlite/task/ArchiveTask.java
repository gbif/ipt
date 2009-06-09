package org.gbif.iptlite.task;

	import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.gbif.iptlite.util.ArchiveWriter;
import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.datasource.ImportSourceException;
import org.gbif.provider.datasource.ImportSourceFactory;
import org.gbif.provider.model.Annotation;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.dto.ExtensionRecord;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.factory.DarwinCoreFactory;
import org.gbif.provider.model.factory.RegionFactory;
import org.gbif.provider.model.factory.TaxonFactory;
import org.gbif.provider.model.voc.PublicationStatus;
import org.gbif.provider.service.DataArchiveManager;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.task.TaskBase;
import org.hibernate.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

	/**
	 * Tha main task responsible for importing raw data into the cache and doing simple preprocessing while iterating through the ImportSource.
	 * Any further secondary postprocessing is done with the help of a second post processing task that this task will automatically schedule once its done.
	 * @author markus
	 *
	 */
	/**
	 * @author markus
	 *
	 * @param <T>
	 * @param <R>
	 */
	public class ArchiveTask extends TaskBase<UploadEvent, DataResource>{
		// import status
		protected String currentActivity;
		private DataResource resource;
		private Date lastLogDate;
		private UploadEvent event;
		private Integer coreRecordsUploaded;
		private Integer coreRecordsErroneous;
		private AtomicInteger currentProcessed;
		private AtomicInteger currentErroneous;
		// keep track of all core ids for basic integrity checks
		private Set<String> coreIds = new HashSet<String>();
		
		// managers
		@Autowired
		private ImportSourceFactory importSourceFactory;
		@Autowired
		private DataArchiveManager dataArchiveManager;
		@Autowired
		private EmlManager emlManager;
		@Autowired
		private UploadEventManager uploadEventManager;

		
		@Autowired
		public ArchiveTask(@Qualifier("dataResourceManager") GenericResourceManager<DataResource> dataResourceManager) {
			super(dataResourceManager);
		}
		
		private void logElapsedTime(String msg){
			Date now = new Date();
			log.info(String.format(msg+" took %s ms", (now.getTime()-lastLogDate.getTime())));
			lastLogDate = now;
		}
		public final UploadEvent call() throws Exception{
			lastLogDate = new Date();

			//
			// prepare task
			//
			prepare();

			//
			// create data files
			//
			createCoreFile();
			logElapsedTime("Creating core data file");
			// create extension data files
			for (ExtensionMapping vm : resource.getExtensionMappings()){
				createExtensionFile(vm);
				logElapsedTime(String.format("Creating extension data file %s with %s records", vm.getExtension().getName(), vm.getRecTotal()));
			}
			
			//
			// bundle data archive
			//
			currentActivity = "Packaging data archive";
			currentProcessed.set(0);
			currentErroneous.set(0);
			File archive=null;
			try {
				archive = dataArchiveManager.packageArchive(resource);
				log.info("DarwinCore archive created at "+archive.getAbsolutePath());
			} catch (IOException e) {
				log.error("Could not write DarwinCore archive", e);
			} catch (Exception e) {
				log.error("Could not create DarwinCore archive", e);
			}
			
			//
			// cleanup task
			//
			close();
			
			return event;		
		}
		

		private void prepare(){
			// prepare import, removing previous records, calling prepare of helper tasks
			resource= loadResource();
			resource.resetStats();
			
			// clear resource annotations
			annotationManager.removeAll(resource);

			// remove previous archive files
			File archiveZip = cfg.getArchiveFile(resource.getId());
			if (archiveZip.exists()){
				archiveZip.delete();
				log.debug("Removed zipped previous archive");
			}
			File archiveDir = cfg.getArchiveDescriptor(resource.getId()).getParentFile();
			try {
				FileUtils.deleteDirectory(archiveDir);
				archiveDir.mkdir();
			} catch (IOException e) {
				String msg = "Couldn't clear existing resource archive at "+archiveDir.getAbsolutePath();
				log.error(msg, e);
				annotationManager.annotateResource(resource, msg);
			}

			// track import in upload event metadata (mainly statistics)
			event = new UploadEvent();
			event.setJobSourceId(this.hashCode());
			event.setJobSourceType(taskTypeId());
			event.setExecutionDate(new Date());
			event.setResource(resource);

			// prepare this instance
			currentActivity="Preparing import";
			currentProcessed = new AtomicInteger(0);
			currentErroneous = new AtomicInteger(0);
			coreRecordsUploaded = 0;
			coreRecordsErroneous = 0;
			coreIds.clear();
			
		}
		
		private void close(){
			// update upload event summary
			Eml eml = emlManager.load(resource);
			event.setEmlVersion(eml.getEmlVersion());
			event.setRecordsUploaded(coreRecordsUploaded);		
			event.setDuration();
			event = uploadEventManager.save(event);			

			// update resource properties
			resource.setLastUpload(event);
			resource.setRecTotal(coreRecordsUploaded);
			resource.setStatus(PublicationStatus.published);

			resourceManager.save(resource);
			resourceManager.flush();
				
			coreIds.clear();
		}

		
		@Transactional(readOnly=false, noRollbackFor={Exception.class})
		private File createCoreFile() throws InterruptedException {
			log.info("Starting building of core data file for resource "+ getTitle());					
			currentActivity="Building core file "+resource.getCoreMapping().getExtension().getName();
			currentProcessed.set(0);
			currentErroneous.set(0);

			ImportSource source=null;
			File out = null;
			ArchiveWriter writer = null; 
			// make sure in the finally section that source & writer is closed and upload event is created properly.
			try {
				// prepare core import source. Can be a file or database source to iterate over in read-only mode
				source = importSourceFactory.newInstance(resource, resource.getCoreMapping());
				out = cfg.getArchiveFile(resource.getId(), resource.getCoreMapping().getExtension());
				writer = new ArchiveWriter(out, resource.getCoreMapping(), true); 
					
				// get list of all core properties in an ordered, stable way (the same)
				List<ExtensionProperty> props = resource.getCoreMapping().getMappedProperties();
				
				// go through source records one by one
				for (ImportRecord irec : source){
					// keep all annotations for a single record til the end so we can add the correct record GUID to them
					if (irec == null){
						continue;
					}						
					currentProcessed.addAndGet(1);
					
					// keep track of processed source records for integrity checks
					if (coreIds.contains(irec.getSourceId())){
						currentErroneous.addAndGet(1);
						annotationManager.badCoreRecord(resource, irec.getSourceId(), "Core record wit duplicate source ID. Line "+String.valueOf(currentProcessed.get()));
						continue;
					}
					coreIds.add(irec.getSourceId());
					//irec.getLink();
					
					// write core record row to file
					try{
						writer.write(irec);
						// set stats per record. Saving of final resource stats is done in the close() section
						coreRecordsUploaded++;

					} catch (Exception e) {
						currentErroneous.addAndGet(1);
						annotationManager.badCoreRecord(resource, irec.getSourceId(), "Unkown error: "+e.toString());
					}
				}

			} catch (ImportSourceException e) {
				annotationManager.annotateResource(resource, "Couldn't open import source. Import aborted: "+e.toString());
				throw new InterruptedException();
			} catch (IOException e) {
				annotationManager.annotateResource(resource, "Couldn't open archive file for writing. Import aborted: "+e.toString());
				throw new InterruptedException();
			} finally {
				// store final numbers in normal Integer so that the AtomicNumber can be reset by other extension imports
				coreRecordsErroneous = currentErroneous.get();
				if (source!=null){
					source.close();
				}
				if (writer!=null){
					try {
						writer.close();
					} catch (IOException e) {
						log.warn("Couldn't close core data file writer", e);
					}
				}
			}

			return out;
		}


		@Transactional(readOnly=false, noRollbackFor={Exception.class})
		private File createExtensionFile(ExtensionMapping vm) throws InterruptedException {
			String extensionName = vm.getExtension().getName();
			Extension extension = vm.getExtension();
			log.info(String.format("Start building %s extension data file for resource %s", extensionName, getTitle() ));
			
			File out = null;
			ArchiveWriter writer = null;
			ImportSource source=null;
			// keep track of records for each extension and then store the totals in the viewMapping.
			// once extension is imported this counter will be reset by the next extension.
			// used to feed status()
			currentActivity="Building extension data file "+extensionName;
			currentProcessed.set(0);
			currentErroneous.set(0);

			try {
				//  prepare import source
				source = importSourceFactory.newInstance(resource, vm);
				out = cfg.getArchiveFile(resource.getId(), extension);
				writer = new ArchiveWriter(out, vm, false); 

				// Do we need a
				for (ImportRecord rec : source){
					currentProcessed.addAndGet(1);
					if (rec == null || rec.getSourceId()==null){
						currentErroneous.addAndGet(1);
						annotationManager.badExtensionRecord(resource, extension, null, "Seems to be an empty record or missing source ID. Line "+String.valueOf(currentProcessed.get()));
						continue;
					}
					if (!coreIds.contains(rec.getSourceId())){
						currentErroneous.addAndGet(1);
						annotationManager.badExtensionRecord(resource, extension, rec.getSourceId(), "Extension record references a core source ID which does not exist. Line "+String.valueOf(currentProcessed.get()));
						continue;
					}
					// write row to file
					writer.write(rec);
				}

			// outer catch/try
			} catch (ImportSourceException e) {
				annotationManager.annotateResource(resource, String.format("Couldn't open import source for extension %s. Extension skipped", extensionName));
				log.error("Couldn't open import source for extension "+extensionName, e);
			} catch (IOException e) {
				annotationManager.annotateResource(resource, "Couldn't open archive file for writing. Import aborted: "+e.toString());
				throw new InterruptedException();
			} finally {
				// store final numbers in normal Integer so that the AtomicNumber can be reset by other extension imports
				coreRecordsErroneous = currentErroneous.get();
				if (source!=null){
					source.close();
				}
				if (writer!=null){
					try {
						writer.close();
					} catch (IOException e) {
						log.warn("Couldn't close extension data file writer", e);
					}
				}
			}
						
			return out;
		}
		
		
		public final synchronized String status() {
			String coreInfo = "";
			String recordStatus ="";
			if (currentProcessed.get() > 0){
				// core imported already
				recordStatus = String.format(". %s records processed", currentProcessed.get());
			}
			if (coreRecordsUploaded != null){
				// core imported already
				coreInfo = String.format(". %s core records in total", coreRecordsUploaded);
			}
			if (currentProcessed!=null){
				return String.format("%s%s%s", currentActivity, recordStatus, coreInfo);
			}else{
				return "Waiting for archiving to start.";
			}
		}

		public int taskTypeId() {
			return 13;
		}

	}
