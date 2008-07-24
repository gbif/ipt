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
import org.gbif.provider.service.CoreRecordManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.util.TabFileWriter;
import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.dao.DarwinCoreDao;
import org.gbif.provider.dao.ExtensionRecordDao;
import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.datasource.ImportSource;
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

public abstract class UploadBaseJob implements Launchable{
	protected static final Log log = LogFactory.getLog(RdbmsUploadJob.class);
	protected static I18nLog logdb = I18nLogFactory.getLog(RdbmsUploadJob.class);

	public static final String RESOURCE_ID = "resourceId";
	public static final String USER_ID = "userId";
	public static final String MAX_RECORDS = "maxRecords";
	
	protected UploadEventManager uploadEventManager;
	protected CoreRecordManager<DarwinCore> darwinCoreManager;
	protected ExtensionRecordDao extensionRecordDao;
	protected Map<Long, String> status = new HashMap<Long, String>();

	public String status(Long resourceId){
		return status.get(resourceId);
	}

	protected UploadBaseJob(UploadEventManager uploadEventManager, CoreRecordManager<DarwinCore> darwinCoreManager, ExtensionRecordDao extensionRecordDao) {
		super();
		this.uploadEventManager = uploadEventManager;
		this.darwinCoreManager = darwinCoreManager;
		this.extensionRecordDao = extensionRecordDao;
	}

	protected Map<String, Long> uploadCore(ImportSource source, OccurrenceResource resource, UploadEvent event) throws InterruptedException {
				log.info("Uploading occurrence core for resource "+resource.getTitle());
				Map<String, Long> idMap = new HashMap<String, Long>();
				// use a single date for now (e.g. to set dateLastModified)
				Date now = new Date();
				
				try {
					// create new tab file
					TabFileWriter writer = prepareTabFile(resource, resource.getCoreMapping());
					
					// flag all previously existing records as deleted before updating/inserting new ones
					darwinCoreManager.flagAsDeleted(resource.getId());

					// keep track of the following statistics for UploadEvent
					int recordsUploaded = 0;
					int recordsDeleted = 0;
					int recordsChanged = 0;
					int recordsAdded = 0;
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
							log.info(recordsUploaded+" uploaded for resource "+resource.getId());
						}
						if (recordsUploaded % 100 == 0){
							logdb.info(recordsUploaded+" uploaded for resource "+resource.getId());
						}
						// insert/update record
						dwc = darwinCoreManager.save(dwc);
						// write record to tab file
						writer.write(dwc.getDataMap());
						// the new darwin core id used for all other extensions
						Long coreId = dwc.getId();
						idMap.put(rec.getLocalId(), coreId);
					}
					
					// update resource and upload event statistics
					recordsDeleted = resource.getRecordCount()+recordsAdded-recordsUploaded;
					event.setRecordsAdded(recordsAdded);
					event.setRecordsChanged(recordsChanged);
					event.setRecordsDeleted(recordsDeleted);
					event.setRecordsUploaded(recordsUploaded);		
					resource.setRecordCount(recordsUploaded);
					// reset status
					status.put(resource.getId(), String.format("%s done.", recordsUploaded));
				} catch (IOException e) {
					logdb.error("Couldnt write tab file. Upload aborted", e);
					e.printStackTrace();
				}
				return idMap;
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
			//FIXME: assign real URL to webapp
			dwc.setLink("http://localhost/providertool/record/"+dwc.getGuid());
		}
	}

	protected void uploadExtension(ImportSource source, Map<String, Long> idMap, OccurrenceResource resource, Extension extension) throws InterruptedException {
		try {
			TabFileWriter writer = prepareTabFile(resource, extension);
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected TabFileWriter prepareTabFile(DatasourceBasedResource resource, CoreViewMapping coreViewMapping) throws IOException{
		List<String> additionalHeader = new ArrayList<String>();
		return prepareTabFile(resource, coreViewMapping.getExtension(), additionalHeader);
	}
	protected TabFileWriter prepareTabFile(DatasourceBasedResource resource, Extension extension) throws IOException{
		return prepareTabFile(resource, extension, null);
	}
	protected TabFileWriter prepareTabFile(DatasourceBasedResource resource, Extension extension, List<String> additionalHeader) throws IOException{
		String dirname = String.format("/Users/markus/Desktop/data/%s", resource.getServiceName());
		String filename = String.format("%s/%s.txt", dirname, extension.getTablename());
		// create directories and file
		File dir = new File(dirname);
		FileUtils.forceMkdir(dir);
		File file = new File(filename);
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
