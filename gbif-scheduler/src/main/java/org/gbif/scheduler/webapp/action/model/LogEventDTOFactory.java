/**
 * 
 */
package org.gbif.scheduler.webapp.action.model;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.googlecode.jsonplugin.JSONException;
import com.googlecode.jsonplugin.JSONUtil;
import com.ibiodiversity.harvest.log.model.CauseDetail;
import com.ibiodiversity.harvest.log.model.ExceptionDetail;
import com.ibiodiversity.harvest.model.LogEvent;
import com.ibiodiversity.harvest.webapp.util.LocalizedTextUtil;

/**
 * Simple factory for log event creation
 * @author timrobertson
 */
public class LogEventDTOFactory {
	// TODO... this is just using the default basenames, so this needs to be Spring wired up in 
	// all uses
	protected static LocalizedTextUtil localizedTextUtil = new LocalizedTextUtil();
	
	/**
	 * Builds the DTO for display using the localizedTextUtil for the locale
	 */
	public static LogEventDTO buildFrom(LogEvent event, Locale locale) {
		LogEventDTO logEventDTO = new LogEventDTO();
		logEventDTO.setMessage(localizedTextUtil.findText(event.getMessage(), event.getMessageParams(), locale, event.getMessage()));
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
