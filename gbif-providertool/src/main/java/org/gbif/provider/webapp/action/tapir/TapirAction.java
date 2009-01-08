package org.gbif.provider.webapp.action.tapir;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.model.dto.ExtensionRecordsWrapper;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.tapir.Diagnostic;
import org.gbif.provider.tapir.Severity;
import org.gbif.provider.util.NamespaceRegistry;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public class TapirAction extends BaseOccurrenceResourceAction{
	private static final String ERROR = "error";
	private static final String PING = "ping";
	private static final String CAPABILITIES = "capabilities";
	private static final String METADATA = "metadata";
	private static final String SEARCH = "search";
	private static final String INVENTORY = "inventory";
	@Autowired
	private DarwinCoreManager darwinCoreManager;
	@Autowired
	private EmlManager emlManager;
	// request
    private String op;
    // just in case of fatal errors
    private String error="unknown fatal error";
    // TAPIR envelope data
    private Date now = new Date();
    private List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
    // for all request types with resource
    private NamespaceRegistry nsr;
	// CAPABILITIES only
    private Map<String, Set<ExtensionProperty>> conceptSchemas;
	// METADATA only
    private Eml eml;    
    // SEARCH only
    private List<DarwinCore> records;
    private ExtensionRecordsWrapper extWrapper;
    private List<Extension> extensions;
    // INVENTORY only
    
	public String execute(){
	    if (op!=null && op.startsWith("p")){
	    	return ping();
    	}
	    if (!loadResource()){
			return ERROR;
	    }
    	if (op!=null && op.startsWith("c")){
    		return capabilities();
    	}else if (op!=null && op.startsWith("m")){
    		return metadata();
    	}else if (op!=null && op.startsWith("i")){
    		return inventory();
    	}else if (op!=null && op.startsWith("s")){
    		return search();
    	}else{
    		addInfo("No TAPIR operation requested. Default to metadata");
    		return metadata();
    	}
    }

	private String ping() {
		return PING;
	}

	private String capabilities() {
		conceptSchemas = new HashMap<String, Set<ExtensionProperty>>();
		for (String ns : nsr.knownNamespaces()){
			conceptSchemas.put(ns, new HashSet<ExtensionProperty>());
		}
		for (ViewMappingBase vm : resource.getAllMappings()){
			for (ExtensionProperty prop : vm.getMappedProperties()){				
				conceptSchemas.get(prop.getNamespace()).add(prop);
			}
		}
		return CAPABILITIES;
	}

	private String metadata() {
		eml = emlManager.load(resource);
		return METADATA;

	}

	private String search() {
		return SEARCH;
	}

	private String inventory() {
		return INVENTORY;

	}


	
	private boolean loadResource() {
		if (resource_id != null) {
			resource = occResourceManager.get(resource_id);
			if (resource != null){
				nsr = new NamespaceRegistry(resource);
				return true;
			}
		}
		// resource cant be loaded but is required
		addFatal("Resource unknown");
		return false;
	}
	private void addFatal(String message){
		diagnostics.add(new Diagnostic(Severity.FATAL, new Date(), message));
	}
	private void addError(String message){
		diagnostics.add(new Diagnostic(Severity.ERROR, new Date(), message));
	}
	private void addWarning(String message){
		diagnostics.add(new Diagnostic(Severity.WARN, new Date(), message));
	}
	private void addInfo(String message){
		diagnostics.add(new Diagnostic(Severity.INFO, new Date(), message));
	}
	
	
	
	
	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public List<DarwinCore> getRecords() {
		return records;
	}

	public OccurrenceResource getResource() {
		return resource;
	}

	public Date getNow() {
		return now;
	}

	public ExtensionRecordsWrapper getExtWrapper() {
		return extWrapper;
	}

	public List<Extension> getExtensions() {
		return extensions;
	}

	public NamespaceRegistry getNsr() {
		return nsr;
	}

	public List<Diagnostic> getDiagnostics() {
		return diagnostics;
	}

	public String getError() {
		return error;
	}

	public Map<String, Set<ExtensionProperty>> getConceptSchemas() {
		return conceptSchemas;
	}

	public Eml getEml() {
		return eml;
	}
    
}