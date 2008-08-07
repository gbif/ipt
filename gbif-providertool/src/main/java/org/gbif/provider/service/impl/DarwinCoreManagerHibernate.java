package org.gbif.provider.service.impl;

import java.util.Arrays;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.service.DarwinCoreManager;

public class DarwinCoreManagerHibernate extends CoreRecordManagerHibernate<DarwinCore> implements DarwinCoreManager  {
	public static String[] searchFields = {"scientificName","locality","country","guid"};
	
	public DarwinCoreManagerHibernate() {
		super(DarwinCore.class, searchFields);
	}

}
