package org.gbif.provider.model.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.Taxon;

public enum ResourceType {
	Specimen("Sample","http://rs.tdwg.org/dwc/terms/Sample"),
	PreservedSpecimen("Sample","http://rs.tdwg.org/dwc/terms/Sample"),
	LivingSpecimen("Sample","http://rs.tdwg.org/dwc/terms/Sample"),
	FossilSpecimen("Sample","http://rs.tdwg.org/dwc/terms/Sample"),
	Observation("Sample","http://rs.tdwg.org/dwc/terms/Sample"),
	MachineObservation("Sample","http://rs.tdwg.org/dwc/terms/Sample"),
	HumanObservation("Sample","http://rs.tdwg.org/dwc/terms/Sample"),
	MultimediaImage("Sample","http://rs.tdwg.org/dwc/terms/Sample"),
	MultimediaMovie("Sample","http://rs.tdwg.org/dwc/terms/Sample"),
	MultimediaSound("Sample","http://rs.tdwg.org/dwc/terms/Sample"),
	Checklist("Taxon","http://rs.tdwg.org/dwc/terms/Taxon"),
	RegionalChecklist("Taxon","http://rs.tdwg.org/dwc/terms/Taxon"),
	NomenclatureChecklist("Taxon","http://rs.tdwg.org/dwc/terms/Taxon"),
	TaxonomicChecklist("Taxon","http://rs.tdwg.org/dwc/terms/Taxon"),
	LegislativeChecklist("Taxon","http://rs.tdwg.org/dwc/terms/Taxon"),
	DescriptionChecklist("Taxon","http://rs.tdwg.org/dwc/terms/Taxon"),
	DistributionChecklist("Taxon","http://rs.tdwg.org/dwc/terms/Taxon");

	public static ResourceType byName(String name){
		for (ResourceType rt : ResourceType.values()){
			if (rt.name().equalsIgnoreCase(name)){
				return rt;
			}
		}
		return null;
	}
	
    public static final Map<String, String> htmlSelectMap;
	static
	{
		Map<String, String> map = new HashMap<String, String>();
		for (ResourceType rt : ResourceType.values()){
			map.put(rt.name(), "resourceType."+rt.name());
		}
		htmlSelectMap = Collections.unmodifiableMap(map);
	}
	
	public String group;	
	public String rowType;	
	
	private ResourceType (String group, String rowType){
		this.group=group;
		this.rowType=rowType;
	}
}
