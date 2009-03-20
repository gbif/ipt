package org.gbif.provider.webapp.action.portal;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gbif.provider.geo.MapUtil;
import org.gbif.provider.model.Annotation;
import org.gbif.provider.model.BBox;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.CommonName;
import org.gbif.provider.model.dto.Distribution;
import org.gbif.provider.model.dto.ExtendedRecord;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.StatusType;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.service.TreeNodeManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.NamespaceRegistry;
import org.gbif.provider.webapp.action.BaseDataResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public class TaxonAction extends BaseTreeNodeAction<Taxon, Rank> implements Preparable{
	@Autowired
	private ExtensionRecordManager extensionRecordManager;
	@Autowired
	private AnnotationManager annotationManager;
	private TaxonManager taxonManager;
	// parameters
    private String action;
    private int type;
    private String category;
    private String format;
    private String q;
    // results
    private String title;
    private List<Taxon> taxa;
    private List<Taxon> synonyms;
    private List<CommonName> commonNames;
    private List<Distribution> distributions;
    private List<StatsCount> stats;
    private List<Annotation> annotations;
    // xml/json serialisation only
    private Map<Object, Object> json;
    private NamespaceRegistry nsr;
    private ExtendedRecord rec;
	private List<Extension> extensions = new ArrayList<Extension>();
	
	public TaxonAction(TaxonManager taxonManager) {
		super(taxonManager);
		this.taxonManager=taxonManager;
	}
	private void setRequestedTaxon(){
    	if (id!=null){
    		node=taxonManager.get(id);
    	}else if (guid!=null){
    		node=taxonManager.get(guid);
    		if (node!=null){
        		id=node.getId();
    		}
    	}
    	if(resource==null){
    		resource=node.getResource();
    		updateResourceType();
    	}
	}
	public String execute(){
		setRequestedTaxon();
    	if (node!=null){
			stats = taxonManager.getRankStats(node.getId());
			synonyms = taxonManager.getSynonyms(node.getId());
			rec = extensionRecordManager.extendCoreRecord(node.getResource(), node);
			if (format != null){
	        	if (format.equalsIgnoreCase("xml")){
	        		nsr = new NamespaceRegistry(node.getResource());
	        		return "xml";
	        	}
	        	else if (format.equalsIgnoreCase("rdf")){
	        		return "rdf";
	        	}
	        	else if (format.equalsIgnoreCase("json")){
	        		//TODO: create map to serialise into JSON
	        		json = new HashMap<Object, Object>();
	        		return "json";
	        	}else{
	        		return format;
	        	}
			}else{
	    		for (Extension e:rec.getExtensions()){
	    			if (!e.getId().equals(Constants.COMMON_NAME_EXTENSION_ID) && !e.getId().equals(Constants.DISTRIBUTION_EXTENSION_ID)){
	    				extensions.add(e);
	    			}
	    		}
				commonNames = extensionRecordManager.getCommonNames(node.getCoreId());
				distributions = extensionRecordManager.getDistributions(node.getCoreId());
			}
        	// find annotations
        	annotations = annotationManager.getByRecord(node.getResourceId(), node.getGuid());
        	return SUCCESS;
    	}
		return RECORD404;
    }
	
	@Override
    public String occurrences(){
		String result = super.occurrences();
		if (node!=null){
	    	occurrences = darwinCoreManager.getByTaxon(node.getId(), resource_id, true);
		}
    	return result;
    }

	public String search(){
		super.prepare();
		taxa = taxonManager.search(resource_id, q);
		return SUCCESS;
	}
	
    public String listByRank(){
    	title = category;
    	setRequestedTaxon();
    	if (node != null){
    		title += " below "+node.getScientificName();
    	}
		taxa=taxonManager.getByRank(resource_id, id, category);
		return SUCCESS;
    }
    public String listByStatus(){
    	StatusType st = StatusType.getByInt(type);
    	title = String.format("%s - %s", st.name(), category);
    	setRequestedTaxon();
    	if (node != null){
    		title += " below "+node.getScientificName();
    	}
		taxa=taxonManager.getByStatus(resource_id, id, st, category);
		return SUCCESS;
    }


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Taxon getTaxon() {
		return node;
	}

	public List<DarwinCore> getOccurrences() {
		return occurrences;
	}

	public String getGeoserverMapUrl() {
		return geoserverMapUrl;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Long getTaxon_id() {
		return id;
	}

	public List<Taxon> getTaxa() {
		return taxa;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public List<StatsCount> getStats() {
		return stats;
	}

	public List<Taxon> getSynonyms() {
		return synonyms;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getTitle() {
		return title;
	}
	public Taxon getRecord(){
		return node;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public Map<Object, Object> getJson() {
		return json;
	}
	public NamespaceRegistry getNsr() {
		return nsr;
	}
	public ExtendedRecord getRec() {
		return rec;
	}
	public List<Extension> getExtensions() {
		return extensions;
	}
	public List<CommonName> getCommonNames() {
		return commonNames;
	}
	public List<Distribution> getDistributions() {
		return distributions;
	}
	public void setQ(String q) {
		this.q = q;
	}
	public String getQ() {
		return q;
	}
	public List<Annotation> getAnnotations() {
		return annotations;
	}
	
}