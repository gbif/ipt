package org.gbif.provider.webapp.action.portal;


import java.util.List;

import org.gbif.provider.geo.MapUtil;
import org.gbif.provider.model.BBox;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.TreeNodeManager;
import org.gbif.provider.webapp.action.BaseDataResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseTreeNodeAction<T extends org.gbif.provider.model.TreeNodeBase<T, E>, E extends Enum> extends BaseDataResourceAction {
	@Autowired
	protected MapUtil mapUtil;
	@Autowired
	protected DarwinCoreManager darwinCoreManager;
	protected TreeNodeManager<T, E> treeNodeManager;
	protected Long id;
	protected T node;
	protected List<DarwinCore> occurrences;
	protected String geoserverMapUrl;
	protected String geoserverMapBBox;
	
	public int width = OccResourceStatsAction.DEFAULT_WIDTH;
	public int height = OccResourceStatsAction.DEFAULT_HEIGHT;
	 
    public BaseTreeNodeAction(TreeNodeManager<T, E> treeNodeManager) {
		super();
		this.treeNodeManager = treeNodeManager;
	}

	public String execute(){
    	if (id!=null){
    		node=treeNodeManager.get(id);
			// geoserver map link
    		if (node!=null){
    			geoserverMapUrl = getGeoserverUrl(node);
    			BBox bbox = BBox.NewWorldInstance();
    			if (node.getBbox()!=null && node.getBbox().isValid()){
    				bbox = node.getBbox();
    			}else if (resource!=null && resource.getGeoCoverage()!=null && resource.getGeoCoverage().isValid()){
    				bbox = resource.getGeoCoverage();
    			}
				geoserverMapBBox = bbox.toStringWMS();
    		}else{
        		return RECORD404;
    		}
    	}
		return SUCCESS;
    }
    
    public String occurrences(){
    	if (resource_id!=null && id!=null){
    		node=treeNodeManager.get(id);
    		if (node!=null){
    			geoserverMapUrl = getGeoserverUrl(node);
    			BBox bbox = BBox.NewWorldInstance();
    			if (node.getBbox()!=null && node.getBbox().isValid()){
    				bbox = node.getBbox();
    			}else if (resource!=null && resource.getGeoCoverage()!=null && resource.getGeoCoverage().isValid()){
    				bbox = resource.getGeoCoverage();
    			}
				geoserverMapBBox = bbox.toStringWMS();
    		}else{
        		return RECORD404;
    		}
    	}
		return SUCCESS;
    }
    private String getGeoserverUrl(T node){
    	if (Region.class.isAssignableFrom(node.getClass())){
    		return mapUtil.getWMSGoogleMapUrl(resource_id, null, (Region) node); 
    	}
    	else if (Taxon.class.isAssignableFrom(node.getClass())){
    		return mapUtil.getWMSGoogleMapUrl(resource_id, (Taxon) node, null); 
    	}
    	return "";
    }
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<DarwinCore> getOccurrences() {
		return occurrences;
	}

	public T getNode() {
		return node;
	}

	public String getGeoserverMapUrl() {
		return geoserverMapUrl;
	}

	public String getGeoserverMapBBox() {
		return geoserverMapBBox;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Long getRegion_id() {
		return id;
	}

}