package org.gbif.provider.model.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.Extension;

public class ExtensionRecordsWrapper {
	private Long coreid;
	private HashMap<Extension, List<ExtensionRecord>> extensionRecords = new HashMap<Extension, List<ExtensionRecord>>();

	public ExtensionRecordsWrapper(Long coreid) {
		super();
		this.coreid = coreid;
	}

	public Long getCoreid() {
		return coreid;
	}


	public void clear() {
		extensionRecords.clear();
	}
	public boolean hasExtension(Extension extension) {
		return extensionRecords.containsKey(extension);
	}
	public List<ExtensionRecord> getExtensionRecords(Extension extension) {
		return extensionRecords.get(extension);
	}
	public boolean isEmpty() {
		return extensionRecords.isEmpty();
	}
	public List<Extension> getExtensions() {
		return new ArrayList<Extension>(extensionRecords.keySet());
	}
	public int size() {
		int size=0;
		for (Extension ext : extensionRecords.keySet()){
			size += extensionRecords.get(ext).size();
		}
		return size;
	}

	public void addExtensionRecords(List<ExtensionRecord> extensionRecords) {
		for (ExtensionRecord eRec : extensionRecords){
			List<ExtensionRecord> eRecList;
			if (!this.extensionRecords.containsKey(eRec.getExtension())){
				eRecList = new ArrayList<ExtensionRecord>(); 
				this.extensionRecords.put(eRec.getExtension(), eRecList);
			}else{
				eRecList = this.extensionRecords.get(eRec.getExtension());
			}
			eRecList.add(eRec);
		}
		
	}
	
}
