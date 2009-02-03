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

import java.util.List;
import java.util.Set;

import org.gbif.provider.model.SourceBase;

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
	
	/** scans a single column of a source and returns the list of distinct values found
	 * @param source
	 * @param column
	 * @return
	 * @throws Exception
	 */
	public Set<String> getDistinctValues(SourceBase source, String column) throws Exception;
}
