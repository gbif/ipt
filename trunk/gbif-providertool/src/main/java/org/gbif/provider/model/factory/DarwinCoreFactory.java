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
import org.gbif.provider.model.voc.AnnotationType;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;

public class DarwinCoreFactory extends ModelBaseFactory<DarwinCore>{

	public DarwinCore build(DataResource resource, ImportRecord rec, Set<Annotation> annotations) {
		if (rec==null){
			return null;
		}
		DarwinCore dwc = DarwinCore.newInstance(resource);
		dwc.setGuid(rec.getGuid());
		dwc.setLink(rec.getLink());
		dwc.setSourceId(rec.getSourceId());
		dwc.setDeleted(false);
		Point loc = new Point();
		for (ExtensionProperty prop : rec.getProperties().keySet()){
			String val = StringUtils.trimToNull(rec.getPropertyValue(prop));
			if (val!=null && val.length() > prop.getColumnLength() && prop.getColumnLength() > 0){
				val = val.substring(0, prop.getColumnLength());
				annotations.add(annotationManager.annotate(dwc, AnnotationType.TrimmedData, String.format("Exceeding data for property %s [%s] cut off", prop.getName(), prop.getColumnLength())));
			}
			String propName = prop.getName();
			// first try the properties which we try to persist converted as other data types
			if(propName.equalsIgnoreCase("MinimumElevationInMeters")){
				dwc.setMinimumElevationInMeters(val);
				if (val !=null){
					try {
						Double typedVal = Double.valueOf(val);
						dwc.setElevation(typedVal);
					} catch (NumberFormatException e) {
						annotations.add(annotationManager.badDataType(dwc, "MinimumElevationInMeters", "Double", val));
					}
				}
			}else if(propName.equalsIgnoreCase("MinimumDepthInMeters")){
				dwc.setMinimumDepthInMeters(val);
				if (val !=null){
					try {
						Double typedVal = Double.valueOf(val);
						dwc.setDepth(typedVal);
					} catch (NumberFormatException e) {
						annotations.add(annotationManager.badDataType(dwc, "MinimumDepthInMeters", "Double", val));
					}
				}
			}else if(propName.equalsIgnoreCase("EarliestDateCollected")){
				dwc.setEarliestDateCollected(val);
				if (val !=null){
					try {						
						Date typedVal = Constants.DATE_ISO_FORMAT().parse(val);
						dwc.setCollected(typedVal);
					} catch (ParseException e) {
						annotations.add(annotationManager.badDataType(dwc, "EarliestDateCollected", "Integer", val));
					}
				}				
			}else if(propName.equalsIgnoreCase("DecimalLatitude")){
				dwc.setDecimalLatitude(val);
				if (val !=null){
					try {
						loc.setLatitude(Double.valueOf(val));
					} catch (NumberFormatException e) {
						annotationManager.badDataType(dwc, "DecimalLatitude", "Double", val);
					} catch (IllegalArgumentException e) {
						annotationManager.annotate(dwc, AnnotationType.WrongDatatype, String.format("Latitude value '%s' is out of allowed range", val));
					}
				}
			}else if(propName.equalsIgnoreCase("DecimalLongitude")){
				dwc.setDecimalLongitude(val);
				if (val !=null){
					try {
						loc.setLongitude(Double.valueOf(val));
					} catch (NumberFormatException e) {
						annotationManager.badDataType(dwc, "DecimalLongitude", "Double", val);
					} catch (IllegalArgumentException e) {
						annotationManager.annotate(dwc, AnnotationType.WrongDatatype, String.format("Longitude value '%s' is out of allowed range", val));
					}
				}
			}else if(propName.equalsIgnoreCase("Class")){
				// stupid case. property is called Classs because Class is a reserved word in java...
				dwc.setClasss(val);
			}else{
				// use reflection to find property
				if (!dwc.setPropertyValue(prop, val)){
					log.warn("Can't set unknown property DarwinCore."+propName);
				}
			}
			
			
			// now just check types and potentially annotate
			if (val !=null){
				for (String p : DarwinCore.INTEGER_PROPERTIES){
					if(propName.equalsIgnoreCase(p)){
						Integer typedVal = null;
						try {
							typedVal = Integer.valueOf(val);
						} catch (NumberFormatException e) {
							annotations.add(annotationManager.badDataType(dwc, p, "Integer", val));
						}
					}
				}
				for (String p : DarwinCore.DOUBLE_PROPERTIES){
					if(propName.equalsIgnoreCase(p)){
						Double typedVal = null;
						try {
							typedVal = Double.valueOf(val);
						} catch (NumberFormatException e) {
							annotations.add(annotationManager.badDataType(dwc, p, "Double", val));
						}
					}
				}
				for (String p : DarwinCore.DATE_PROPERTIES){
					if(propName.equalsIgnoreCase(p)){
						try {
							Date typedVal = Constants.DATE_ISO_FORMAT().parse(val);
						} catch (ParseException e) {
							annotations.add(annotationManager.badDataType(dwc, p, "ISO Date", val));
						}
					}
				}
				for (String p : DarwinCore.TOKEN_PROPERTIES){
					if(propName.equalsIgnoreCase(p) && StringUtils.trimToEmpty(val).contains(" ")){
						annotations.add(annotationManager.badDataType(dwc, p, "Monominal", val));
					}
				}
			}
		}
		// persist only valid localities
		if (loc.isValid()){
			dwc.setLocation(loc);
		}
		return dwc;
	}

}
