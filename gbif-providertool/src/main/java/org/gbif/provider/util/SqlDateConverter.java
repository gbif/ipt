package org.gbif.provider.util;

import java.text.ParseException;
import java.util.Map;

import org.apache.struts2.util.StrutsTypeConverter;
import org.appfuse.util.DateUtil;

import com.opensymphony.xwork2.util.TypeConversionException;
/**
 * Supports the conversion of java.sql.Date - required by JIBX
 * 
 * @author davemartin
 */
public class SqlDateConverter extends StrutsTypeConverter {
	
	/**
	 * @see org.apache.struts2.util.StrutsTypeConverter#convertFromString(java.util.Map, java.lang.String[], java.lang.Class)
	 */
	public Object convertFromString(Map ctx, String[] value, Class arg2) {
		if (value[0] == null || value[0].trim().equals("")) {
			return null;
		}
        try {
            java.util.Date myDate = DateUtil.convertStringToDate(value[0]);
            return new java.sql.Date(myDate.getTime());
        } catch (ParseException pe) {
            pe.printStackTrace();
            throw new TypeConversionException(pe.getMessage());
        }
    }

	/**
	 * @see org.apache.struts2.util.StrutsTypeConverter#convertToString(java.util.Map, java.lang.Object)
	 */
    public String convertToString(Map ctx, Object data) {
        return org.appfuse.util.DateUtil.convertDateToString((java.sql.Date) data);
    }
} 