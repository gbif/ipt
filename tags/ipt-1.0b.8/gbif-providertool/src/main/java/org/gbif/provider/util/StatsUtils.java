package org.gbif.provider.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.dto.StatsCount;

public class StatsUtils {
	public static List<StatsCount> getDataMap(List<Object[]> idValueCountRows){
		List<StatsCount> data = new ArrayList<StatsCount>();
        for (Object[] row : idValueCountRows){
        	Long id=null;
        	Object value;
        	Long count;
        	if (row.length==2){
            	value = row[0];
            	count = (Long) row[1];
        	}else{
            	id = (Long) row[0];
            	value = row[1];
            	try{
                	count = (Long) row[2];
            	} catch (ClassCastException e){
            		count = Long.valueOf(row[2].toString());
            	}
        	}
        	String label = null;
        	if (value!=null){
				label = value.toString();
        	}
        	if (StringUtils.trimToNull(label)==null){
        		label = "?";
        	}
        	data.add(new StatsCount(id, label, value, count));
        }
        // sort data
        Collections.sort(data);
        return data;
	}
}
