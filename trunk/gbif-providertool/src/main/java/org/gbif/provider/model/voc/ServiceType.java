package org.gbif.provider.model.voc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum ServiceType {
    RSS("rss"),
    EML("eml"),
    TAPIR("tapir"),
    WFS("wfs"),  
    WMS("wms"),  
    TCS_RDF("tcs-rdf"),  
    DWC_ARCHIVE("dwc-archive");  

    public String tModel;
    private ServiceType(String tModel){
    	this.tModel=tModel;
    }
    
}
