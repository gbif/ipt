package org.gbif.provider.service.impl;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.service.DarwinCoreManager;

public class DarwinCoreManagerHibernate extends CoreRecordManagerHibernate<DarwinCore> implements DarwinCoreManager  {
	public DarwinCoreManagerHibernate() {
		super(DarwinCore.class);
	}

}
