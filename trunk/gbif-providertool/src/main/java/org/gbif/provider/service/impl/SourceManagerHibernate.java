package org.gbif.provider.service.impl;

import java.util.List;

import org.gbif.provider.model.SourceBase;
import org.gbif.provider.model.SourceFile;
import org.gbif.provider.service.SourceManager;

public class SourceManagerHibernate extends	GenericResourceRelatedManagerHibernate<SourceBase> implements SourceManager{

	public SourceManagerHibernate() {
		super(SourceBase.class);
		// TODO Auto-generated constructor stub
	}

	public SourceFile getSourceByFilename(Long resourceId, String filename) {
		return (SourceFile) query("from SourceFile s WHERE s.resource.id = :resourceId and s.filename = :filename")
			        .setLong("resourceId", resourceId)
			        .setString("filename", filename)
	        		.uniqueResult();
	}

}
