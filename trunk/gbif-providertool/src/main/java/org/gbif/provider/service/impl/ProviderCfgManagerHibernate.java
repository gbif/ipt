/***************************************************************************
* Copyright (C) 2008 Global Biodiversity Information Facility Secretariat.
* All Rights Reserved.
*
* The contents of this file are subject to the Mozilla Public
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.

***************************************************************************/

package org.gbif.provider.service.impl;

import org.gbif.provider.model.ProviderCfg;
import org.gbif.provider.service.ProviderCfgManager;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly=true)
public class ProviderCfgManagerHibernate implements ProviderCfgManager {
	@Autowired
	private SessionFactory sessionFactory;		

	protected Session getSession(){
		return SessionFactoryUtils.getSession(sessionFactory, false);
	}

	public ProviderCfg load() {
		ProviderCfg cfg =(ProviderCfg) getSession().createQuery("select cfg from ProviderCfg cfg").uniqueResult();
		if (cfg == null){
			// create default config
			cfg = new ProviderCfg();
		}
		return cfg;
	}

	@Transactional(readOnly=false)
	public void save(ProviderCfg cfg) {
		getSession().saveOrUpdate(cfg);
	}
	
}
