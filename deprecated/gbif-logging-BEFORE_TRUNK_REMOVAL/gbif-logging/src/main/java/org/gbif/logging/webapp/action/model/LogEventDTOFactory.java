/**
 * 
 */
package org.gbif.logging.webapp.action.model;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.googlecode.jsonplugin.JSONException;
import com.googlecode.jsonplugin.JSONUtil;
import com.opensymphony.xwork2.LocaleProvider;
import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.TextProviderFactory;

import org.gbif.logging.model.LogEvent;
import org.gbif.logging.log.model.CauseDetail;
import org.gbif.logging.log.model.ExceptionDetail;

/**
 * Simple factory for log event creation
 * @author timrobertson
 */
public class LogEventDTOFactory {
	private static TextProvider textProvider;

	public LogEventDTOFactory(TextProvider textProvider){
		super();
		this.textProvider = textProvider;
	}

	/**
	 * Builds the DTO for display using an already localized message
	 */
	//TODO: use a spring provided text-provider that works with struts2
	public static LogEventDTO buildFrom(LogEvent event) {
		LogEventDTO logEventDTO = new LogEventDTO();
		logEventDTO.setMessage(textProvider.getText(event.getMessage(), event.getMessageParams()));
		logEventDTO.setTimestamp(event.getTimestamp());
		logEventDTO.setLevel(event.getLevel());
		logEventDTO.setId(event.getId());
		
		if (StringUtils.isNotBlank(event.getInfoAsJSON())) {
			try {
				ExceptionDetail ed = deserialize(event.getInfoAsJSON());
				logEventDTO.setExceptionDetail(ed);
			} catch (JSONException e) {
				// hmmm.. we are deserializing a log message, which should not be corrupt!
				// lets juyst print to system.err
				e.printStackTrace();
			}
		}
		
		return logEventDTO;
	}
	
	/**
	 * The MAP is keyed on the property names of the object that the JSON was serialzied from
	 * @throws JSONException But should not unless something is really wrong...
	 */
	@SuppressWarnings("unchecked")
	protected static ExceptionDetail deserialize(String json) throws JSONException {
		Map<String, Object> edAsMap = (Map<String, Object>) JSONUtil.deserialize(json);
		ExceptionDetail ed = new ExceptionDetail();
		ed.setMessage((String) edAsMap.get("message"));
		
		List<Map<String,Object>> detailsAsListMap = (List<Map<String, Object>>)edAsMap.get("causeDetails");
		for (Map<String, Object> detailsAsMap : detailsAsListMap) {
			CauseDetail cd = new CauseDetail();
			cd.setClassName((String)detailsAsMap.get("className"));
			// would be a pretty damn long class if we needed a long ;o)
			cd.setLineNumber(((Long)detailsAsMap.get("lineNumber")).intValue());
			cd.setMethodName((String)detailsAsMap.get("methodName"));
			cd.setFileName((String)detailsAsMap.get("fileName"));
			ed.getCauseDetails().add(cd);
		}
		
		return ed;
	}
}
