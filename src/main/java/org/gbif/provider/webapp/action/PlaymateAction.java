package org.gbif.provider.webapp.action;

import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;
import org.appfuse.webapp.action.BaseAction;

import com.opensymphony.xwork2.Preparable;

public class PlaymateAction extends BaseAction implements Preparable {
    private String name;
    private Integer counter;
    private Long  id;
    
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
		counter = (Integer) getSession().getAttribute("counter");
		counter +=1;
		getSession().setAttribute("coutner", counter);
		return SUCCESS;
	}
}
