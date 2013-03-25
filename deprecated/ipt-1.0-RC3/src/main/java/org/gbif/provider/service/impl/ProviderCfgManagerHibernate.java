/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.service.impl;

import org.gbif.provider.model.ProviderCfg;
import org.gbif.provider.service.ProviderCfgManager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * TODO: Documentation.
 * 
 */
@Transactional(readOnly = true)
public class ProviderCfgManagerHibernate implements ProviderCfgManager {
  @Autowired
  private SessionFactory sessionFactory;

  public ProviderCfg load() {
    ProviderCfg cfg = (ProviderCfg) getSession().createQuery(
        "select cfg from ProviderCfg cfg").uniqueResult();
    if (cfg == null) {
      // create default config
      cfg = new ProviderCfg();
    }
    return cfg;
  }

  @Transactional(readOnly = false)
  public void save(ProviderCfg cfg) {
    getSession().saveOrUpdate(cfg);
  }

  protected Session getSession() {
    return SessionFactoryUtils.getSession(sessionFactory, false);
  }

}
