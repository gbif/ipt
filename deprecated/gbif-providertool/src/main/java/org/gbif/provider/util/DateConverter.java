package org.gbif.provider.util;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import org.apache.struts2.util.StrutsTypeConverter;

import com.opensymphony.xwork2.util.TypeConversionException;

public class DateConverter extends StrutsTypeConverter {

    public Object convertFromString(Map ctx, String[] value, Class arg2) {
        if (value[0] == null || value[0].trim().equals("")) {
            return null;
        }

        try {
            return DateUtil.convertStringToDate(value[0]);
        } catch (ParseException pe) {
            pe.printStackTrace();
            throw new TypeConversionException(pe.getMessage());
        }
    }

    public String convertToString(Map ctx, Object data) {
        return DateUtil.convertDateToString((Date) data);
    }
} 