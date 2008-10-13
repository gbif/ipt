package org.gbif.provider.webapp.action.manage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.eml.Role;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.webapp.action.BaseResourceAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.Preparable;

public class EmlEditorAction extends BaseResourceAction implements Preparable, SessionAware{
	private Map session;
	@Autowired
	private EmlManager emlManager;
	private Eml eml;
	
	protected String back;
	protected String backPage;
	protected final String BACK = "back";
	protected String next;
	protected String nextPage;
	protected final String NEXT = "next";
	protected Map<String, String> isoLanguageI18nCodeMap;
	
	
	public void prepare() {
		if (resource_id!=null){
			super.prepare();
			eml = emlManager.load(resource);
		}
	}
		
	public String execute(){
		return SUCCESS;
	}
	
	public String save(){
		if (cancel!=null){
			return CANCEL;
		}
		if (back!=null){
			return BACK;
		}
		if (next==null){
			return INPUT;
		}
		emlManager.save(eml);
		return SUCCESS;
	}
	
	
	
	public Eml getEml() {
		return eml;
	}

	public void setEml(Eml eml) {
		this.eml = eml;
	}
	
	
	/**
	 * Back workflow request.
	 * 
	 * @return
	 */
	public String back() {
		return BACK;
	}

	public String getBack() {
		return back;
	}

	public void setBack(String back) {
		this.back = back;
	}

	public String getNext() {
		return next;
	}

	public void setNext(String next) {
		this.next = next;
	}

	public String getBackPage() {
		return backPage;
	}

	public void setBackPage(String backPage) {
		this.backPage = backPage;
	}

	public String getNextPage() {
		return nextPage;
	}

	public void setNextPage(String nextPage) {
		this.nextPage = nextPage;
	}
	public Map<String, String> getIsoLanguageI18nCodeMap() {
		return isoLanguageI18nCodeMap;
	}

	public List getRoles() {
		return  Arrays.asList(Role.values());
	 }

	public void setIsoLanguageI18nCodeMap(Map<String, String> isoLanguageI18nCodeMap) {
		this.isoLanguageI18nCodeMap = isoLanguageI18nCodeMap;
	}
	
}
