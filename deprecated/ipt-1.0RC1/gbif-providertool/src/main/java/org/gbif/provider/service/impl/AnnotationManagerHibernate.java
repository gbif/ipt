package org.gbif.provider.service.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.Annotation;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.dto.ExtensionRecord;
import org.gbif.provider.model.voc.AnnotationType;
import org.gbif.provider.service.AnnotationManager;
import org.hibernate.Session;

public class AnnotationManagerHibernate extends GenericResourceRelatedManagerHibernate<Annotation> implements AnnotationManager{
	public AnnotationManagerHibernate() {
		super(Annotation.class);
	}

	public Annotation annotate(CoreRecord record, AnnotationType type,String note) {
		return annotate(record.getResource(), record.getSourceId(), record.getGuid(), type.toString(), null, true, note);
	}
	public Annotation annotate(CoreRecord record, AnnotationType type, String actor, String note) {
		return annotate(record.getResource(), record.getSourceId(), record.getGuid(), type.toString(), actor, true, note);
	}
	public Annotation annotate(Resource resource, String sourceId, String guid, AnnotationType type, String actor, String note) {
		return annotate(resource, sourceId, guid, type.toString(), actor, true, note);
	}

	
	public Annotation annotate(CoreRecord record, String type, boolean removeDuringImport, String note) {
		return annotate(record.getResource(), record.getSourceId(), record.getGuid(), type, null, removeDuringImport, note);
	}	
	public Annotation annotate(CoreRecord record, String type, String actor, boolean removeDuringImport, String note) {
		return annotate(record.getResource(), record.getSourceId(), record.getGuid(), type, actor, removeDuringImport, note);
	}
	public Annotation annotate(Resource resource, String sourceId, String guid, String type, String actor, boolean removeDuringImport, String note) {
		Annotation anno = new Annotation();
		anno.setResource(resource);
		if (actor==null){
			actor="IPT";
		}
		anno.setRemoveDuringImport(removeDuringImport);
		anno.setCreator(actor);
		anno.setSourceId(sourceId);
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
		return annotate(record.getResource(), record.getSourceId(), record.getGuid(), AnnotationType.WrongDatatype, null, String.format("Couldnt transform value '%s' for property %s into %s value", value, property, dataType));
	}

	public Annotation annotateResource(Resource resource, String note) {
		return annotate(resource, null, null, AnnotationType.Resource, null, note);
	}

	public Annotation badReference(CoreRecord record, String property, String refId, String note) {
		note = StringUtils.trimToEmpty(note);
		if (note.length()>0){
			note = ". "+note;
		}
		return annotate(record.getResource(), record.getSourceId(), record.getGuid(), AnnotationType.BadReference, null, String.format("Couldn't find referenced %s id '%s'%s", property, refId, note));
	}
	public Annotation badCoreRecord(Resource resource, String sourceId, String note) {
		note = StringUtils.trimToEmpty(note);
		if (note.length()>0){
			note = ". "+note;
		}
		if (sourceId==null){
			sourceId="?";
		}
		return annotate(resource, sourceId, null, AnnotationType.BadCoreRecord, null, String.format("Couldn't import core record with local id '%s'%s", sourceId, note));
	}
	public Annotation badExtensionRecord(Resource resource, Extension extension, String sourceId, String note) {
		note = StringUtils.trimToEmpty(note);
		if (note.length()>0){
			note = ". "+note;
		}
		if (sourceId==null){
			sourceId="?";
		}
		return annotate(resource, sourceId, null, AnnotationType.BadExtensionRecord, null, String.format("Couldn't import %s extension record with local id '%s'%s", extension.getName(), sourceId, note));
	}

	
	
	
	
	
	
	
	public List<Annotation> getByActor(Long resourceId, String actor) {
        return query("select a from Annotation a where a.resource.id=:resourceId and a.actor=:actor")
        .setLong("resourceId", resourceId)
        .setString("actor", actor)
		.list();
	}

	public List<Annotation> getByLatest(Long resourceId, Date earliestDate) {
        return query("select a from Annotation a where a.resource.id=:resourceId and a.created>=:date")
        .setLong("resourceId", resourceId)
        .setDate("date", earliestDate)
		.list();
	}

	public List<Annotation> getByRecord(Long resourceId, String guid) {
        return query("select a from Annotation a where a.resource.id=:resourceId and a.guid=:guid")
        .setLong("resourceId", resourceId)
        .setString("guid", guid)
		.list();
	}

	public List<Annotation> getByType(Long resourceId, String type) {
        return query("select a from Annotation a where a.resource.id=:resourceId and a.type=:type")
        .setLong("resourceId", resourceId)
        .setString("type", type)
		.list();
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

	public int updateCoreIds(DataResource resource) {
		String coreTable = "darwin_core";
		if (resource instanceof ChecklistResource){
			coreTable="taxon";
		}
		String sql = String.format("update annotation a set a.guid=(select c.id from %s c where c.resource_fk=a.resource_fk and c.source_id=a.source_id) where a.resource_fk=%s and a.source_id is not null and a.guid is null", coreTable, resource.getId());
		Connection cn = null;
		int count = 0;
		try {
			cn=getConnection();
			Statement st = cn.createStatement();			
			count = st.executeUpdate(sql);
			log.debug(String.format("Updated %s annotations with guids", count));
		} catch (SQLException e) {
			log.error("Couldn't update annotations", e);
		}
		return count;	
	}

	public Annotation annotate(Resource resource, ExtensionRecord record, AnnotationType type, String note) {
		return annotate(resource, record.getSourceId(), null, type, null, note);
	}

}
