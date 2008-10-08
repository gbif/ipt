package org.gbif.logging.webapp.action;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.googlecode.jsonplugin.annotations.SMDMethod;

import org.appfuse.webapp.action.BaseAction;
import org.gbif.logging.model.LogEvent;
import org.gbif.logging.service.LogEventManager;
import org.gbif.logging.webapp.action.model.LogEventDTO;
import org.gbif.logging.webapp.action.model.LogEventDTOFactory;
import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.TextProviderFactory;

public class LogEventAction extends MRBBaseAction implements Preparable {
    private LogEventManager logEventManager;
    private LogEventDTOFactory logEventDTOFactory;
    private List logEvents;
    private LogEvent logEvent;
    private Long  id;

    public List getLogEvents() {
        return logEvents;
    }

    /**
     * Grab the entity from the database before populating with request parameters
     */
    public void prepare() {
        if (getRequest().getMethod().equalsIgnoreCase("post")) {
            // prevent failures on new
            String logEventId = getRequest().getParameter("logEvent.id");
            if (logEventId != null && !logEventId.equals("")) {
                logEvent = logEventManager.get(new Long(logEventId));
            }
        }
		TextProvider textProvider = new TextProviderFactory().createInstance(getClass(), this);
        logEventDTOFactory = new LogEventDTOFactory(textProvider);
    }

    public String list() {
        logEvents = logEventManager.getAll();
        return SUCCESS;
    }
    
    @SMDMethod
    public List<LogEventDTO> getAllLatest(long minId, int minLevel) throws IOException {
    	List<LogEvent> events = logEventManager.findByIdGreaterThan(minId, minLevel);
    	List<LogEventDTO> eventDTOs = new LinkedList<LogEventDTO>();
    	for (LogEvent event : events) {
    		eventDTOs.add(LogEventDTOFactory.buildFrom(event));
    	}   	
    	return eventDTOs;
    }     

    @SMDMethod
    public List<LogEventDTO> getLatestForSource(int sourceId, int sourceType, long minId, int minLevel) throws IOException {
    	List<LogEvent> events = logEventManager.findBySourceAndIdGreaterThan(sourceId, sourceType, minId, minLevel);
    	List<LogEventDTO> eventDTOs = new LinkedList<LogEventDTO>();
    	for (LogEvent event : events) {
    		eventDTOs.add(LogEventDTOFactory.buildFrom(event));
    	}
    	return eventDTOs;
    }     

    @SMDMethod
    public List<LogEventDTO> getLatestForGroup(int groupId, long minId, int minLevel) throws IOException {
    	List<LogEvent> events = logEventManager.findByGroupAndIdGreaterThan(groupId, minId, minLevel);
    	List<LogEventDTO> eventDTOs = new LinkedList<LogEventDTO>();
    	for (LogEvent event : events) {
    		eventDTOs.add(LogEventDTOFactory.buildFrom(event));
    	}
    	return eventDTOs;
    }   
    
    @SMDMethod
    public List<LogEventDTO> getLatestForUser(long userId, long minId, int minLevel) throws IOException {
    	List<LogEvent> events = logEventManager.findByUserAndIdGreaterThan(userId, minId, minLevel);
    	List<LogEventDTO> eventDTOs = new LinkedList<LogEventDTO>();
    	for (LogEvent event : events) {
    		eventDTOs.add(LogEventDTOFactory.buildFrom(event));
    	}
    	return eventDTOs;
    }   
    
    public void setId(Long  id) {
        this. id =  id;
    }

    public LogEvent getLogEvent() {
        return logEvent;
    }

    public void setLogEvent(LogEvent logEvent) {
        this.logEvent = logEvent;
    }

    public String delete() {
        logEventManager.remove(logEvent.getId());
        saveMessage(getText("logEvent.deleted"));
        return SUCCESS;
    }

    public String edit() {
        if (id != null) {
            logEvent = logEventManager.get(id);
        } else {
            logEvent = new LogEvent();
        }

        return SUCCESS;
    }

    public String save() throws Exception {
        if (cancel != null) {
            return "cancel";
        }

        if (delete != null) {
            return delete();
        }

        boolean isNew = (logEvent.getId() == null);

        logEventManager.save(logEvent);

        String key = (isNew) ? "logEvent.added" : "logEvent.updated";
        saveMessage(getText(key));

        if (!isNew) {
            return INPUT;
        } else {
            return SUCCESS;
        }
    }

    public void setLogEventManager(LogEventManager logEventManager) {
        this.logEventManager = logEventManager;
    }
   
}