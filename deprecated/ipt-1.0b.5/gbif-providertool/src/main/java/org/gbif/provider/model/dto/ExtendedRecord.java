package org.gbif.provider.model.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;

public class ExtendedRecord {
	private CoreRecord core;
	private Map<Extension, List<ExtensionRecord>> extensionRecords = new HashMap<Extension, List<ExtensionRecord>>();

	public ExtendedRecord(CoreRecord core) {
		super();
		this.core = core;
	}

	public CoreRecord getCore() {
		return core;
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
			addExtensionRecord(eRec);
		}	
	}
	public void addExtensionRecord(ExtensionRecord extensionRecord) {
		List<ExtensionRecord> eRecList;
		if (!this.extensionRecords.containsKey(extensionRecord.getExtension())){
			eRecList = new ArrayList<ExtensionRecord>(); 
			this.extensionRecords.put(extensionRecord.getExtension(), eRecList);
		}else{
			eRecList = this.extensionRecords.get(extensionRecord.getExtension());
		}
		eRecList.add(extensionRecord);
	}
}
