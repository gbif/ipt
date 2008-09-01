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

package org.gbif.provider.webapp.action.manage;

import java.util.List;

import org.gbif.provider.datasource.DatasourceRegistry;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;

public class DatasourceRegistryAction extends BaseOccurrenceResourceAction {
    private DatasourceRegistry datasourceRegistry;
    private List<DatasourceBasedResource> datasources;

	public void setDatasourceRegistry(DatasourceRegistry datasourceRegistry) {
		this.datasourceRegistry = datasourceRegistry;
	}

	public List getDatasources() {
        return datasources;
    }	
	
	public String list() {
        datasources = datasourceRegistry.getAll();
        return SUCCESS;
    }

}
