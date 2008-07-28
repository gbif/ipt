package org.gbif.provider.job;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.gbif.provider.service.CoreRecordManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.ResourceManager;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.util.ConfigUtil;
import org.gbif.provider.util.TabFileWriter;
import org.gbif.provider.util.ZipUtil;
import org.gbif.logging.log.I18nDatabaseAppender;
import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.dao.DarwinCoreDao;
import org.gbif.provider.dao.ExtensionRecordDao;
import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.datasource.ImportSourceException;
import org.gbif.provider.datasource.impl.RdbmsImportSource;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.CoreViewMapping;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.ExtensionRecord;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.ViewMapping;
import org.gbif.scheduler.scheduler.Launchable;

public abstract class OccUploadBaseJob implements Job{
	protected static final Log log = LogFactory.getLog(OccDbUploadJob.class);
	protected static I18nLog logdb = I18nLogFactory.getLog(OccDbUploadJob.class);

	public static final String RESOURCE_ID = "resourceId";
	public static final String USER_ID = "userId";
	public static final String MAX_RECORDS = "maxRecords";
	
	protected UploadEventManager uploadEventManager;
	protected CoreRecordManager<DarwinCore> darwinCoreManager;
	protected ExtensionRecordDao extensionRecordDao;
	protected ResourceManager<OccurrenceResource> occResourceManager;
	protected Map<Long, String> status = new HashMap<Long, String>();

	protected OccUploadBaseJob(UploadEventManager uploadEventManager, CoreRecordManager<DarwinCore> darwinCoreManager, 
			ExtensionRecordDao extensionRecordDao, ResourceManager<OccurrenceResource> occResourceManager) {
		super();
		this.uploadEventManager = uploadEventManager;
		this.darwinCoreManager = darwinCoreManager;
		this.extensionRecordDao = extensionRecordDao;
		this.occResourceManager = occResourceManager;
	}
	
	
	public String status(Long resourceId){
		return status.get(resourceId);
	}

	public void launch(Map<String, Object> seed) {
		String webappDir = (String) seed.get(Launchable.WEBAPP_DIR);
		ConfigUtil.setWebappDir(webappDir);
		try {
			log.info("Starting "+this.getClass().getSimpleName() +" with seed "+seed);
			
			Long resourceId = Long.valueOf(seed.get(RESOURCE_ID).toString());
			try{
				Long userId = Long.valueOf(seed.get(USER_ID).toString());
				MDC.put(I18nDatabaseAppender.MDC_USER, userId);
			} catch (NumberFormatException e) {
				String[] params = {RESOURCE_ID, USER_ID, seed.toString()};
				logdb.error("{0} or {1} in seed is no Integer {2}", params, e);
			}
			MDC.put(I18nDatabaseAppender.MDC_GROUP_ID, JobUtils.getJobGroup(resourceId));

			// set sourceId to jobID
			Integer sourceId = null;
			if (seed.get(Launchable.JOB_ID) != null && seed.get(Launchable.JOB_ID) != "null"){
				try{
					sourceId = Integer.valueOf(seed.get(Launchable.JOB_ID).toString());
				} catch (NumberFormatException e) {
					String[] params = {Launchable.JOB_ID, seed.toString()};
					logdb.warn("{0} in seed is no Integer {1}", params, e);
				}
			}
			MDC.put(I18nDatabaseAppender.MDC_SOURCE_ID, (String) seed.get(Launchable.JOB_ID));
			MDC.put(I18nDatabaseAppender.MDC_SOURCE_TYPE, getSourceType());

			Integer maxRecords = null;
			if (seed.get(MAX_RECORDS) != null && seed.get(MAX_RECORDS) != "null"){
				try{
					maxRecords = Integer.valueOf(seed.get(MAX_RECORDS).toString());
				} catch (NumberFormatException e) {
					String[] params = {MAX_RECORDS, seed.toString()};
					logdb.warn("{0} in seed is no Integer {1}", params, e);
				}
			}
			//TODO: set this in the scheduler constructor???
			//MDC.put(I18nDatabaseAppender.MDC_INSTANCE_ID, null);

			
			// get resource
			OccurrenceResource resource = occResourceManager.get(resourceId);

			// track upload in upload event metadata (mainly statistics)
			UploadEvent coreEvent = new UploadEvent();
			coreEvent.setResource(resource);
			
			
			// clear old data/events
			File dataDir = resource.getDataDir();
			FileUtils.deleteDirectory(dataDir);
			log.info("Removed old occurrence data dir "+dataDir.getAbsolutePath());
			FileUtils.forceMkdir(dataDir);
			// clear old log events too?
			log.info("Old log events are kept");
			
			List<File> dumpFiles = new ArrayList<File>();
			
			// try to upload records
			try {
				Map<String, Long> idMap = new HashMap<String, Long>();
				// prepare import source. Source is implementation specific
				ImportSource source = this.getCoreImportSource(seed, resource, maxRecords);
				// run import of core into db & dump file
				dumpFiles.add(uploadCore(source, resource, coreEvent, idMap));
				// upload further extensions one by one
				for (ViewMapping vm : resource.getExtensionMappings().values()){
					Extension ext = vm.getExtension();
					//  prepare import source
					source = this.getImportSource(seed, resource, ext, maxRecords);
					// run import into db & dump file
					dumpFiles.add(uploadExtension(source, idMap, resource, ext));
				}
			} catch (Exception e) {
				logdb.error("Error uploading data", e);
				e.printStackTrace();
			}
			
			// save upload event
			Date now = new Date();
			coreEvent.setExecutionDate(now);
			uploadEventManager.save(coreEvent);
			// update resource properties
			resource.setLastImport(now);
			resource.setLastImportSourceId(sourceId);
			resource.setRecordCount(coreEvent.getRecordsUploaded());
			occResourceManager.save(resource);
			
			// zip all files into single archive
			File archive = resource.getDumpArchiveFile();
			archive.createNewFile();
			ZipUtil.zipFiles(dumpFiles, archive);			
			
		} catch (NumberFormatException e) {
			String[] params = {RESOURCE_ID, seed.toString()};
			logdb.error("{0} in seed is no Integer {1}", params, e);
		} catch (Exception e) {
			logdb.error("Error occurred while running "+this.getClass().getSimpleName(), e);
		}		
	}
	
	abstract protected ImportSource getCoreImportSource(Map<String, Object> seed, OccurrenceResource resource, Integer maxRecords) throws ImportSourceException;

	abstract protected ImportSource getImportSource(Map<String, Object> seed, OccurrenceResource resource, Extension extension, Integer maxRecords) throws ImportSourceException;

	protected File uploadCore(ImportSource source, OccurrenceResource resource, UploadEvent event, Map<String, Long> idMap) throws InterruptedException {
				log.info("Uploading occurrence core for resource "+resource.getTitle());
				// use a single date for now (e.g. to set dateLastModified)
				Date now = new Date();
				File out = null;
				try {
					// create new tab file
					TabFileWriter writer = prepareTabFile(resource, resource.getCoreMapping());
					out = writer.getFile();
					
					// flag all previously existing records as deleted before updating/inserting new ones
					darwinCoreManager.flagAsDeleted(resource.getId());

					// keep track of the following statistics for UploadEvent
					int recordsUploaded = 0;
					int recordsDeleted = 0;
					int recordsChanged = 0;
					int recordsAdded = 0;
					
					try{
						
						// go through source records one by one
						for (ImportRecord rec : source){
							// check if thread should shutdown...
							if (Thread.interrupted()) {
							    throw new InterruptedException();
							}			

							// get previous record or null if it didnt exist yet based on localID and resource
							DarwinCore oldRecord = darwinCoreManager.findByLocalId(rec.getLocalId(), resource.getId());
							// get darwincore record based on this core record
							DarwinCore dwc = DarwinCore.newInstance(rec);
							
							// attach to the occurrence resource
							dwc.setResource(resource);
							
							// assign managed properties
							updateManagedProperties(dwc, oldRecord);
							
							// check if new record version is different from old one
							if (oldRecord != null && oldRecord.hashCode() == dwc.hashCode() && oldRecord.equals(dwc)){
								// same record. reset isDeleted flag = false
								dwc.setDeleted(false);
							}else if (oldRecord!=null){
								// modified record
								dwc.setModified(now);
								// remove old + insert new record
								// TODO: could be improved by updating existing record!
								darwinCoreManager.remove(oldRecord.getId());
								recordsChanged++;
							}else{
								// new record that didnt exist before
								dwc.setModified(now);
								recordsAdded++;
							}
							// count all inserted records
							recordsUploaded++;
							// keep track of upload status outside of this method so that we can create services for it
							status.put(resource.getId(), String.valueOf(recordsUploaded));
							if (recordsUploaded % 1000 == 0){
								log.debug(recordsUploaded+" uploaded for resource "+resource.getId());
							}
							// insert/update record
							dwc = darwinCoreManager.save(dwc);
							// write record to tab file
							writer.write(dwc.getDataMap());
							// the new darwin core id used for all other extensions
							Long coreId = dwc.getId();
							idMap.put(rec.getLocalId(), coreId);
						}
					} finally {
						// flush and close writer/file
						writer.close();
						// update resource and upload event statistics
						recordsDeleted = resource.getRecordCount()+recordsAdded-recordsUploaded;
						event.setRecordsAdded(recordsAdded);
						event.setRecordsChanged(recordsChanged);
						event.setRecordsDeleted(recordsDeleted);
						event.setRecordsUploaded(recordsUploaded);		
						resource.setRecordCount(recordsUploaded);
						// reset status
						status.put(resource.getId(), String.format("%s done.", recordsUploaded));
					}

				} catch (IOException e) {
					log.error("Couldnt open tab file. Upload aborted", e);
					throw new InterruptedException();
				}
				
				return out;
			}

	private void updateManagedProperties(DarwinCore dwc, DarwinCore oldRecord) {
		// assign new GUID if none exists
		if (dwc.getGuid() == null){
			// if old version exists already reuse the previously assigned GUID
			if (oldRecord != null){
				dwc.setGuid(oldRecord.getGuid());
			}else{
				dwc.setGuid(UUID.randomUUID().toString());					
			}
		}			
		// assign link to detailed record if not existing
		if (dwc.getLink() == null){
			dwc.setLink(String.format("%s/%s", dwc.getResource().getRecordResolverEndpoint(), dwc.getGuid()));
		}
	}

	protected File uploadExtension(ImportSource source, Map<String, Long> idMap, OccurrenceResource resource, Extension extension) throws InterruptedException {
		File out = null;
		try {
			// create new tab file
			TabFileWriter writer = prepareTabFile(resource, extension);

			try{
				out = writer.getFile();
				for (ImportRecord rec : source){
					// check if thread should shutdown...
					if (Thread.interrupted()) {
					    throw new InterruptedException();
					}
					Long coreId = idMap.get(rec.getLocalId());
					if (coreId == null){
						String[] paras = {rec.getLocalId(), extension.getName(), resource.getId().toString()};
						//FIXME: use i18n job logging ???
						log.warn("uploadManager.unknownLocalId" +paras.toString());
					}else{
						// TODO: check if record has changed
						ExtensionRecord extRec = ExtensionRecord.newInstance(rec);
						extensionRecordDao.insertExtensionRecord(extRec);
					}
				}
			} finally {
				writer.close();
			}
		} catch (IOException e) {
			log.error("Couldnt open tab file. Upload aborted", e);
			throw new InterruptedException();
		}
		
		return out;
	}
	
	protected static TabFileWriter prepareTabFile(OccurrenceResource resource, CoreViewMapping coreViewMapping) throws IOException{
		List<String> additionalHeader = new ArrayList<String>();
		return prepareTabFile(resource, coreViewMapping.getExtension(), additionalHeader);
	}
	protected static TabFileWriter prepareTabFile(OccurrenceResource resource, Extension extension) throws IOException{
		return prepareTabFile(resource, extension, null);
	}
	protected static TabFileWriter prepareTabFile(OccurrenceResource resource, Extension extension, List<String> additionalHeader) throws IOException{
		// create new file, overwriting existing one
		File file = resource.getDumpFile(extension);
		file.createNewFile();

		List<String> header = new ArrayList<String>();
		// add core record id first as first column
		header.add(CoreRecord.ID_COLUMN_NAME);
		header.add(CoreRecord.MODIFIED_COLUMN_NAME);
		// add only existing mapped concepts
		ViewMapping mapping = resource.getExtensionMapping(extension);
		if (mapping == null){
			throw new IllegalArgumentException(String.format("Resource %s does not have the extension %s mapped",resource.getTitle(), extension.getName()));
		}
		for (ExtensionProperty prop : mapping.getMappedProperties()){
			header.add(prop.getName());
		}
		header.addAll(additionalHeader);
		TabFileWriter writer = new TabFileWriter(file, header);
		return writer;
	}

}
