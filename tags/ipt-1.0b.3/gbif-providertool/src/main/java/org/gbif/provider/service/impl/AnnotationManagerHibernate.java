package org.gbif.provider.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.Annotation;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.voc.AnnotationType;
import org.gbif.provider.service.AnnotationManager;
import org.hibernate.Session;

public class AnnotationManagerHibernate extends GenericResourceRelatedManagerHibernate<Annotation> implements AnnotationManager{
	public AnnotationManagerHibernate() {
		super(Annotation.class);
	}

	public Annotation annotate(CoreRecord record, AnnotationType type,String note) {
		return annotate(record.getResource(), record.getGuid(), type.toString(), null, true, note);
	}
	public Annotation annotate(CoreRecord record, AnnotationType type, String actor, String note) {
		return annotate(record.getResource(), record.getGuid(), type.toString(), actor, true, note);
	}
	public Annotation annotate(Resource resource, String guid, AnnotationType type, String actor, String note) {
		return annotate(resource, guid, type.toString(), actor, true, note);
	}

	
	public Annotation annotate(CoreRecord record, String type, boolean removeDuringImport, String note) {
		return annotate(record.getResource(), record.getGuid(), type, null, removeDuringImport, note);
	}	
	public Annotation annotate(CoreRecord record, String type, String actor, boolean removeDuringImport, String note) {
		return annotate(record.getResource(), record.getGuid(), type, actor, removeDuringImport, note);
	}
	public Annotation annotate(Resource resource, String guid, String type, String actor, boolean removeDuringImport, String note) {
		Annotation anno = new Annotation();
		anno.setResource(resource);
		if (actor==null){
			actor="IPT";
		}
		anno.setRemoveDuringImport(removeDuringImport);
		anno.setCreator(actor);
		anno.setGuid(guid);
		anno.setNote(note);
		anno.setType(type);
		save(anno);
		if (log.isDebugEnabled()){
			log.debug(anno);
		}
		return anno;
	}

	public Annotation badDataType(CoreRecord record, String property, String dataType, String value){
		return annotate(record.getResource(), record.getGuid(), AnnotationType.WrongDatatype, null, String.format("Couldnt transform value '%s' for property %s into %s value", value, property, dataType));
	}

	public Annotation annotateResource(Resource resource, String note) {
		return annotate(resource, null, AnnotationType.Resource, null, note);
	}

	public Annotation badReference(CoreRecord record, String property, String refId, String note) {
		note = StringUtils.trimToEmpty(note);
		if (note.length()>0){
			note = ". "+note;
		}
		return annotate(record.getResource(), record.getGuid(), AnnotationType.BadReference, null, String.format("Couldn't find referenced %s id '%s'%s", property, refId, note));
	}
	public Annotation badCoreRecord(Resource resource, String localId, String note) {
		note = StringUtils.trimToEmpty(note);
		if (note.length()>0){
			note = ". "+note;
		}
		if (localId==null){
			localId="?";
		}
		return annotate(resource, null, AnnotationType.BadCoreRecord, null, String.format("Couldn't import core record with local id '%s'%s", localId, note));
	}
	public Annotation badExtensionRecord(Resource resource, Extension extension, String localId, String note) {
		note = StringUtils.trimToEmpty(note);
		if (note.length()>0){
			note = ". "+note;
		}
		if (localId==null){
			localId="?";
		}
		return annotate(resource, null, AnnotationType.BadExtensionRecord, null, String.format("Couldn't import %s extension record with local id '%s'%s", extension.getName(), localId, note));
	}

	
	
	
	
	
	
	
	public List<Annotation> getByActor(Long resourceId, String actor) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Annotation> getByLatest(Long resourceId, Date earliestDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Annotation> getByRecord(Long resourceId, String guid) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Annotation> getByType(Long resourceId, String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int removeAll(Resource resource) {
		Session session = getSession();
		// now delete resource related entities
		int count = session.createQuery("delete Annotation a WHERE a.resource = :resource and removeDuringImport=true")
		        .setEntity("resource", resource)
		        .executeUpdate();
		log.info(String.format("Removed %s annotations bound to resource %s", count, resource.getTitle()));
		return count;
	}

}
