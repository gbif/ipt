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
package org.gbif.provider.dao.hibernate;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;

public class DaoUtils {
	
	public static Log logger = LogFactory.getLog(DaoUtils.class);

	public static void addQueryParam(StringBuffer queryBuffer, Map<String, Object> params,
			String hqlParamName, Object param, String hqlOperator, boolean fuzzy, boolean anyPlace, boolean andParams){
		
		if(param!=null){
			
			Object searchParam = param;
			if(param instanceof String){
				logger.debug("String param");
				String stringSearchParam = (String) param;
				if(StringUtils.isEmpty(stringSearchParam)){
					return;
				}
				
				if(fuzzy){
					logger.debug("fuzzy");
					stringSearchParam = stringSearchParam.replace('*', '%');
					if(!stringSearchParam.endsWith("%")){
						stringSearchParam = stringSearchParam +'%';
					}
				}
				if(anyPlace){
					stringSearchParam = '%' + stringSearchParam;
				}
				searchParam = stringSearchParam;
			}
			queryBuffer.append(" ");
			if(params.size()>0){
				if(andParams)
					queryBuffer.append("AND ");
				else
					queryBuffer.append("OR ");
			} else {
				queryBuffer.append(" WHERE ");
			}
			
			queryBuffer.append(hqlParamName);
			queryBuffer.append(" ");
			queryBuffer.append(hqlOperator);
			queryBuffer.append(" :");
			String namedParam = hqlParamName.replace(".", "_") + params.size();
			queryBuffer.append(namedParam);
			logger.debug("namedParam: "+namedParam+", searchParam:"+searchParam);
			params.put(namedParam, searchParam);
		}
	}

	public static void setParams(Query query, Map<String, Object> params) {
		Set<String> keys = params.keySet();
		for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
	    String paramName = (String) iterator.next();
	    logger.debug("setting: "+paramName+", value: "+params.get(paramName));
	    query.setParameter(paramName, params.get(paramName));
	    
    }
  }
}