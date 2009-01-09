package org.gbif.provider.webapp.action.tapir;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.model.dto.ExtendedRecord;
import org.gbif.provider.model.dto.ExtensionRecordsWrapper;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.tapir.Diagnostic;
import org.gbif.provider.tapir.Filter;
import org.gbif.provider.tapir.ParseException;
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
	private static final String MODEL_LOCATION = "http://rs.tdwg.org/tapir/cs/dwc/dwcstar.xml";
	private static final String MODEL_ALIAS = "dwc";
	@Autowired
	private DarwinCoreManager darwinCoreManager;
	@Autowired
	private ExtensionRecordManager extensionRecordManager;
	@Autowired
	private EmlManager emlManager;
    // just in case of fatal errors
    private String error="unknown fatal error";
    // request parameters
    private String op;
    private Boolean count;
    private Integer start;
    private Integer limit;
    private String template;
    private String concept;
    private String tagname;
    private String filter;
    private Boolean envelope=true;
    private String model;
    private String orderby;
    private Boolean descend=false;
    // parsed stuff
    private Filter pFilter;
    // TAPIR envelope data
    private Date now = new Date();
    private List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
    // for all request types with resource
    private NamespaceRegistry nsr = new NamespaceRegistry();
	// CAPABILITIES only
    private Map<String, Set<ExtensionProperty>> conceptSchemas;
	// METADATA only
    private Eml eml;    
    // INVENTORY only
    private List<LinkedHashMap<String, Object>> values;
    // SEARCH only
    private List<ExtendedRecord> records;
    
	public String execute(){
	    if (op!=null && op.startsWith("p")){
	    	return ping();
    	}
	    if (!loadResource()){
			return ERROR;
	    }
    	if (op==null){
    		addInfo("No TAPIR operation requested. Default to metadata");
    		return metadata();
    	}else if (op.startsWith("c")){
    		return capabilities();
    	}else if (op.startsWith("m")){
    		return metadata();
    	}else if (op.startsWith("i")){
    		return inventory();
    	}else if (op.startsWith("s")){
    		return search();
    	}else{
    		addInfo("Unknown TAPIR operation requested. Default to metadata");
    		return metadata();
    	}
    }
	private void addMetaNamespaces(){
		nsr.add("http://purl.org/dc/elements/1.1/");
		nsr.add("http://rs.tdwg.org/dwc/terms/");
		nsr.add("http://purl.org/dc/terms/");
		nsr.add("http://www.w3.org/2003/01/geo/wgs84_pos#");
		nsr.add("http://www.w3.org/2001/vcard-rdf/3.0#");
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
		addMetaNamespaces();
		eml = emlManager.load(resource);
		return METADATA;

	}

	private String search() {
		// check requested model
		if (template!=null){
			addFatal("Templates are not supported");
			return ERROR;
		}else if (model!=null){
			if (model.equalsIgnoreCase(MODEL_LOCATION) || model.equalsIgnoreCase(MODEL_ALIAS)){
				try {
					parseFilter();
					doSearch();
				} catch (ParseException e) {
					addError(e.getTapirMessage());
					return ERROR;
				}
				return SEARCH;
			}
			addFatal("The requested output model is not supported");
		}else{
			addFatal("Illegal request. No template or output model has been specified");
		}
		return ERROR;
	}

	private void parseFilter() throws ParseException{
		pFilter = new Filter(filter);
	}

	private void doSearch() {
		//FIXME: do proper core search
		List<DarwinCore> coreRecords = darwinCoreManager.getLatest(resource_id,0,8);
		records = extensionRecordManager.extendCoreRecords(resource, coreRecords.toArray(new CoreRecord[coreRecords.size()]));
	}
	private void doInventory() {
		//FIXME: do proper inventory search
		values=new ArrayList<LinkedHashMap<String, Object>>();
	}

	private String inventory() {
		try {
			parseFilter();
			doInventory();
		} catch (ParseException e) {
			addError(e.getTapirMessage());
			return ERROR;
		}
		return INVENTORY;
	}


	
	private boolean loadResource() {
		if (resource_id != null) {
			resource = occResourceManager.get(resource_id);
			if (resource != null){
				nsr.addResource(resource);
				return true;
			}
		}
		// resource cant be loaded but is required
		addFatal("Resource unknown");
		return false;
	}
	private void addFatal(String message){
		error = message;
		diagnostics.add(new Diagnostic(Severity.FATAL, new Date(), message));
	}
	private void addError(String message){
		error = message;
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

	public OccurrenceResource getResource() {
		return resource;
	}

	public Date getNow() {
		return now;
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
    public String getModelLocation(){
    	return MODEL_LOCATION;
    }
    public String getModelAlias(){
    	return MODEL_ALIAS;
    }

	public Boolean getCount() {
		return count;
	}

	public void setCount(Boolean count) {
		this.count = count;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getConcept() {
		return concept;
	}

	public void setConcept(String concept) {
		this.concept = concept;
	}

	public String getTagname() {
		return tagname;
	}

	public void setTagname(String tagname) {
		this.tagname = tagname;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public Boolean getEnvelope() {
		return envelope;
	}

	public void setEnvelope(Boolean envelope) {
		this.envelope = envelope;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getOrderby() {
		return orderby;
	}

	public void setOrderby(String orderby) {
		this.orderby = orderby;
	}

	public Boolean getDescend() {
		return descend;
	}

	public void setDescend(Boolean descend) {
		this.descend = descend;
	}
	public List<LinkedHashMap<String, Object>> getValues() {
		return values;
	}
	public List<ExtendedRecord> getRecords() {
		return records;
	}
    
}