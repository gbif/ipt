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
import org.gbif.provider.model.voc.StatusType;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.NamespaceRegistry;
import org.gbif.provider.webapp.action.BaseDataResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public class TaxonAction extends BaseDataResourceAction implements Preparable{
	@Autowired
	private MapUtil mapUtil;
	@Autowired
	private TaxonManager taxonManager;
	@Autowired
	private DarwinCoreManager darwinCoreManager;
	@Autowired
	private ExtensionRecordManager extensionRecordManager;
	@Autowired
	private AnnotationManager annotationManager;
	// parameters
    private Long id;
    private String action;
    private int type;
    private String category;
    private String format;
    private String q;
    // results
    private String title;
    private Taxon taxon;
    private List<Taxon> taxa;
    private List<Taxon> synonyms;
    private List<CommonName> commonNames;
    private List<Distribution> distributions;
    private List<StatsCount> stats;
    private List<Annotation> annotations;
    // occurrences only
    private List<DarwinCore> occurrences;
	public String geoserverMapUrl;
	public String geoserverMapBBox;
	public int width = OccResourceStatsAction.DEFAULT_WIDTH;
	public int height = OccResourceStatsAction.DEFAULT_HEIGHT;
    // xml/json serialisation only
    private Map<Object, Object> json;
    private NamespaceRegistry nsr;
    private ExtendedRecord rec;
	private List<Extension> extensions = new ArrayList<Extension>();
	
	private void setRequestedTaxon(){
    	if (id!=null){
    		taxon=taxonManager.get(id);
    	}else if (guid!=null){
    		taxon=taxonManager.get(guid);
    		if (taxon!=null){
        		id=taxon.getId();
    		}
    	}
    	if(resource==null){
    		resource=taxon.getResource();
    		updateResourceType();
    	}
	}
	public String execute(){
		setRequestedTaxon();
    	if (taxon!=null){
			stats = taxonManager.getRankStats(taxon.getId());
			synonyms = taxonManager.getSynonyms(taxon.getId());
			rec = extensionRecordManager.extendCoreRecord(taxon.getResource(), taxon);
			if (format != null){
	        	if (format.equalsIgnoreCase("xml")){
	        		nsr = new NamespaceRegistry(taxon.getResource());
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
				commonNames = extensionRecordManager.getCommonNames(taxon.getCoreId());
				distributions = extensionRecordManager.getDistributions(taxon.getCoreId());
			}
        	// find annotations
        	annotations = annotationManager.getByRecord(taxon.getResourceId(), taxon.getGuid());
        	return SUCCESS;
    	}
		return RECORD404;
    }
    
	public String search(){
		super.prepare();
		taxa = taxonManager.search(resource_id, q);
		return SUCCESS;
	}
	
    public String listByRank(){
    	title = category;
    	setRequestedTaxon();
    	if (taxon != null){
    		title += " below "+taxon.getScientificName();
    	}
		taxa=taxonManager.getByRank(resource_id, id, category);
		return SUCCESS;
    }
    public String listByStatus(){
    	StatusType st = StatusType.getByInt(type);
    	title = String.format("%s - %s", st.name(), category);
    	setRequestedTaxon();
    	if (taxon != null){
    		title += " below "+taxon.getScientificName();
    	}
		taxa=taxonManager.getByStatus(resource_id, id, st, category);
		return SUCCESS;
    }
    
    public String occurrences(){
    	if (id!=null && resource_id!=null){
    		taxon=taxonManager.get(id);
    		occurrences = darwinCoreManager.getByTaxon(id, resource_id, true);
    		if (taxon!=null){
    			geoserverMapUrl = mapUtil.getWMSGoogleMapUrl(resource_id, taxon, null);
    			if (taxon.getBbox()!=null && taxon.getBbox().isValid()){
    				geoserverMapBBox = taxon.getBbox().toStringWMS();
    			}else{
    				geoserverMapBBox = resource.getGeoCoverage().toStringWMS();
    			}
    		}
    	}
		return SUCCESS;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Taxon getTaxon() {
		return taxon;
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
		return taxon;
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