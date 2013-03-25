package org.gbif.provider.service;

import java.util.Date;
import java.util.List;

import org.gbif.provider.model.Annotation;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.voc.AnnotationType;

public interface AnnotationManager extends GenericResourceRelatedManager<Annotation>{
	public List<Annotation> getByRecord(Long resourceId, String guid);
	public List<Annotation> getByType(Long resourceId, String type);
	public List<Annotation> getByActor(Long resourceId, String actor);
	public List<Annotation> getByLatest(Long resourceId, Date earliestDate);

	public Annotation annotate(Resource resource, String guid, String type, String actor, boolean removeDuringImport, String note);
	public Annotation annotate(CoreRecord record, String type, String actor, boolean removeDuringImport, String note);
	public Annotation annotate(CoreRecord record, String type, boolean removeDuringImport, String note);

	public Annotation annotate(Resource resource, String guid, AnnotationType type, String actor, String note);
	public Annotation annotate(CoreRecord record, AnnotationType type, String actor, String note);
	public Annotation annotate(CoreRecord record, AnnotationType type, String note);
	
	public Annotation badDataType(CoreRecord record, String property, String dataType, String value);
	public Annotation badReference(CoreRecord record, String property, String id, String note);
	
	// annotations linked only to resource with guid=null
	public Annotation badCoreRecord(Resource resource, String id, String note);
	public Annotation badExtensionRecord(Resource resource, Extension extension, String localId, String note);

	// annotations for the whole resource will result in guid=null
	public Annotation annotateResource(Resource resource, String note);
}
