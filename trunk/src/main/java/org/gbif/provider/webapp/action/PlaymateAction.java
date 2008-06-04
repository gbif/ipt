package org.gbif.provider.webapp.action;

import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;
import org.appfuse.webapp.action.BaseAction;

import com.opensymphony.xwork2.Preparable;

public class PlaymateAction extends BaseAction implements Preparable, SessionAware {
    private String name;
    private Map session;
    private Integer counter;
    private Long  id;

	public void setSession(Map session) {
		this.session=session;		
	}
	public String getName() {
		return name;
	}
	public Integer getCounter() {
		return counter;
	}


	public void prepare() throws Exception {
    	name="Cindy";
	}

	public String hello(){
		return SUCCESS;
	}
	public String execute(){
		return SUCCESS;
	}

	public String count(){
		if (session.containsKey("counter")){
			counter = (Integer) session.get("counter");
		}else{
			counter = 0;
		}
		counter +=1;
		session.put("counter", counter);
		return SUCCESS;
	}

}
