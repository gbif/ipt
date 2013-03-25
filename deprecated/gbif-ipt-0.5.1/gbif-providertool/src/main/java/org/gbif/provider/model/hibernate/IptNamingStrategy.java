package org.gbif.provider.model.hibernate;

import org.gbif.provider.model.Extension;
import org.gbif.provider.model.voc.ExtensionType;
import org.hibernate.cfg.NamingStrategy;

public interface IptNamingStrategy extends NamingStrategy{
	public String extensionTableName(Extension extension);
}
