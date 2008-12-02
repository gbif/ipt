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

package org.gbif.provider.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.gbif.provider.model.SourceBase;
import org.gbif.provider.model.ViewMappingBase;

public interface SourceInspectionManager {
	/**
	 * @param data source either to file or SQL statement
	 * @return a list of PREVIEW_SIZE (~5) rows plus a first header row of strings that contains the column names as TABLE.COLUMNNAME 
	 * @throws Exception
	 */
	public List<List<? extends Object>> getPreview(SourceBase source) throws Exception;
	/**
	 * @param data source either to file or SQL statement
	 * @return list of column headers only (same first row in getPreview())
	 * @throws Exception
	 */
	public List<String> getHeader(SourceBase source) throws Exception;
	
}
