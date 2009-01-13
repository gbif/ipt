package org.gbif.provider.service;

import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.voc.ExtensionType;

public interface ExtensionPropertyManager extends GenericManager<ExtensionProperty>{
	public ExtensionProperty getByQualName(String qName, ExtensionType type);

	public ExtensionProperty getByName(String name, ExtensionType type);
}
