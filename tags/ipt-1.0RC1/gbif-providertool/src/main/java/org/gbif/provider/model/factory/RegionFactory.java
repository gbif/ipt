package org.gbif.provider.model.factory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Date;
import java.util.Set;

import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.model.Annotation;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.Point;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.voc.AnnotationType;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;

public class RegionFactory extends ModelBaseFactory<Region>{

	public Region build(DarwinCore dwc) {
		return build(dwc, RegionType.Locality);
		
	}
	public Region build(DarwinCore dwc, RegionType regionType) {
		if (dwc==null){
			return null;
		}
		Region region = Region.newInstance(dwc.getResource());
		region.setMpath(dwc.getGeographyPath(regionType));
		region.setLabel(dwc.getHigherGeographyName(regionType));
		region.setType(regionType);
		// currently regions dont have a GUID. Otherwise use dwc.getSamplingLocationID() for localities
		return region;
	}
}
