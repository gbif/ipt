package org.gbif.ipt.action;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.util.ServletContextAware;

import com.opensymphony.xwork2.Preparable;

public class FormAction extends BaseAction implements ServletRequestAware, Preparable{

	protected HttpServletRequest req;
	protected boolean delete=false;
	protected String id=null;
	protected boolean notFound=false;
	

	/**
	 * Override this method if you need to load entities based on the id value before the PARAM interceptor is called.
	 * You can also use this method to prepare a new, empty instance in case no id was provided.
	 * If the id parameter alone is not sufficient to load your entities, 
	 * you can access the request object directly like we do here and read any other parameter you need to prepare the action for the param phase.
	 */
	public void prepare() throws Exception {
		// see if an id was provided in the request.
		// we dont use the PARAM - PREPARE - PARAM interceptor stack
		// so we investigate the request object directly BEFORE the param interceptor is called
		// this allows us to load any existing instances that should be modified
		id = StringUtils.trimToNull(req.getParameter("id"));
	}
	
	public String execute(){
		// if notFound was set to true during prepare() the supplied id parameter didnt exist - return a 404!
		if (notFound){
			return NOT_FOUND;
		}
		// if this is a GET request we request the INPUT form
		if (req.getMethod().equalsIgnoreCase("get")){
			return INPUT;
		}else if (req.getMethod().equalsIgnoreCase("post")){
			// if its a POST we either save or delete
			// suplied default methods which be overridden
			String result;
			if (delete){
				result=delete();
			}else{
				result=save();
			}
			// check again if notFound was set
			// this also allows the load() or delete() method to set the flag
			if (notFound){
				return NOT_FOUND;
			}else{
				return result;
			}
		}
		return ERROR;
	}

	/**
	 * Override this method if you need to persist entities after the PARAM interceptor is called
	 * @return
	 */
	public String save(){
		return SUCCESS;
	}
	
	/**
	 * Override this method if you need to delete entities based on the id value after the PARAM interceptor is called
	 * @return
	 */
	public String delete(){
		return SUCCESS;
	}

	public void setServletRequest(HttpServletRequest req) {
		this.req=req;		
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(String delete) {
		this.delete = StringUtils.trimToNull(delete)!=null;
	}

	public String getId() {
		return id;
	}
}
