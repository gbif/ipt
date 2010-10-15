package org.gbif.ipt.struts2.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.struts2.util.StrutsTypeConverter;

import com.opensymphony.xwork2.XWorkException;
import com.opensymphony.xwork2.conversion.TypeConversionException;
import com.thoughtworks.xstream.converters.ConversionException;

public class DateFormatConverter extends StrutsTypeConverter {

	public Object convertFromString(Map context, String[] values, Class toClass) {
		if (values != null && values.length > 0) {
			Date date = null;
			try {
				// evaluating 
				SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");
				date = isoFormat.parse(values[0]);
			
				SimpleDateFormat usaFormat = new SimpleDateFormat("dd/MM/yyyy");
				date = usaFormat.parse(values[0]);
			} catch (ParseException e) {
				
			}
			// TODO - There should be a way to send an error to the user telling that the date has a wrong format. 
			// Throwing XWorkException or TypeConversionException doesn't work.
			// 
			return date;
		}
		return null;
	}

	@Override
	public String convertToString(Map context, Object o) {
		if (o instanceof Date) {
			Date d = (Date) o;
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			return format.format(d);
		} 
		return null;
	}

}
