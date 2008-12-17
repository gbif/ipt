package org.gbif.provider.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Date;

import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.CoreRecordFactory;
import org.gbif.provider.util.Constants;

public class CoreRecordFactoryImpl implements CoreRecordFactory {
	private static I18nLog logdb = I18nLogFactory.getLog(CoreRecordFactoryImpl.class);
	private static Log log = LogFactory.getLog(CoreRecordFactoryImpl.class);

	public CoreRecord build(DataResource resource, ImportRecord rec) {
		if (resource instanceof OccurrenceResource){
			return build((OccurrenceResource) resource, rec);
		}
		if (resource instanceof ChecklistResource){
			return build((ChecklistResource) resource, rec);
		}
		return null;
	}

	public CoreRecord copyPersistentProperties(CoreRecord target, CoreRecord source) {
			Class recClass = target.getClass();
			for (Method getter : recClass.getMethods()){
				try {
					String methodName = getter.getName();
					if ( (methodName.startsWith("get") || methodName.startsWith("is")) && !methodName.equals("getClass")  && !getter.isAnnotationPresent(Transient.class)){
						String setterName;
						if (methodName.startsWith("get")){
							setterName = "set"+methodName.substring(3);
						}else{
							setterName = "set"+methodName.substring(2);
						}
						Class returnType = getter.getReturnType();
						try{
							Method setter = recClass.getMethod(setterName, returnType);
							setter.invoke(target, getter.invoke(source));
						} catch (NoSuchMethodException e){
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} catch (SecurityException e) {
					e.printStackTrace();
				}
				
			}
		
		return target;
	}

	public DarwinCore build(OccurrenceResource resource, ImportRecord rec) {
		if (rec==null){
			return null;
		}
		DarwinCore dwc = DarwinCore.newInstance(resource);
		dwc.setGuid(rec.getGuid());
		dwc.setLink(rec.getLink());
		dwc.setLocalId(rec.getLocalId());
		dwc.setDeleted(false);
		for (ExtensionProperty prop : rec.getProperties().keySet()){
			String val = StringUtils.trimToNull(rec.getPropertyValue(prop));
			if (val.length() > prop.getColumnLength() && prop.getColumnLength() > 0){
				val = val.substring(0, prop.getColumnLength());
				logdb.warn(String.format("Exceeding data for property %s [%s] cut off", prop.getName(), prop.getColumnLength()));
			}
			String propName = prop.getName();
			// first try the properties which we try to convert to other data types
			if(propName.equals("MinimumElevationInMeters")){
				dwc.setMinimumElevationInMeters(val);
				Integer typedVal = null;
				if (val !=null){
					try {
						typedVal = Integer.valueOf(val);
						dwc.setMinimumElevationInMetersAsInteger(typedVal);
					} catch (NumberFormatException e) {
						logdb.warn("log.transform", new String[]{val, "MinimumElevationInMeters", "Integer"});
					}
				}
			}else if(propName.equals("MaximumElevationInMeters")){
				dwc.setMaximumElevationInMeters(val);
				Integer typedVal = null;
				if (val !=null){
					try {
						typedVal = Integer.valueOf(val);
						dwc.setMaximumElevationInMetersAsInteger(typedVal);
					} catch (NumberFormatException e) {
						logdb.warn("log.transform", new String[]{val, "MaximumElevationInMeters", "Integer"});
					}
				}
			}else if(propName.equals("MinimumDepthInMeters")){
				dwc.setMinimumDepthInMeters(val);
				Integer typedVal = null;
				if (val !=null){
					try {
						typedVal = Integer.valueOf(val);
						dwc.setMinimumDepthInMetersAsInteger(typedVal);
					} catch (NumberFormatException e) {
						logdb.warn("log.transform", new String[]{val, "MinimumDepthInMeters", "Integer"});
					}
				}
			}else if(propName.equals("MaximumDepthInMeters")){
				dwc.setMaximumDepthInMeters(val);
				Integer typedVal = null;
				if (val !=null){
					try {
						typedVal = Integer.valueOf(val);
						dwc.setMaximumDepthInMetersAsInteger(typedVal);
					} catch (NumberFormatException e) {
						logdb.warn("log.transform", new String[]{val, "MaximumDepthInMeters", "Integer"});
					}
				}
			}else if(propName.equals("EarliestDateCollected")){
				dwc.setEarliestDateCollected(val);
				Date typedVal;
				if (val !=null){
					try {						
						typedVal = Constants.DATE_ISO_FORMAT().parse(val);
						dwc.setDateCollected(typedVal);
					} catch (ParseException e) {
						logdb.warn("log.transform", new String[]{val, "EarliestDateCollected", "Date"});
					}
				}				
			}else if(propName.equals("Class")){
				// stupid case. property is called Classs because Class is a reserved word in java...
				dwc.setClasss(val);
			}else{
				// use reflection to find property
				if (!dwc.setPropertyValue(prop, val)){
					log.warn("Can't set unknown property DarwinCore."+propName);
				}
			}
		}
		return dwc;
	}

	public Taxon build(ChecklistResource resource, ImportRecord rec) {
		if (rec==null){
			return null;
		}
		Taxon tax = Taxon.newInstance(resource);
		tax.setGuid(rec.getGuid());
		tax.setLink(rec.getLink());
		tax.setLocalId(rec.getLocalId());
		tax.setDeleted(false);
		for (ExtensionProperty prop : rec.getProperties().keySet()){
			// set properties via reflection
			String val = StringUtils.trimToNull(rec.getPropertyValue(prop));
			if (val.length() > prop.getColumnLength() && prop.getColumnLength() > 0){
				val = val.substring(0, prop.getColumnLength());
				logdb.warn(String.format("Exceeding data for property %s [%s] cut off", prop.getName(), prop.getColumnLength()));
			}
			if (!tax.setPropertyValue(prop, val)){
				String propName = prop.getName();
				log.warn("Can't set unknown property Taxon."+propName);
			}
		}
		return tax;
	}

}
