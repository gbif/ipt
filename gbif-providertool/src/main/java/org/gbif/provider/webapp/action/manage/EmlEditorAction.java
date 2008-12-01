package org.gbif.provider.webapp.action.manage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.SessionAware;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.eml.Role;
import org.gbif.provider.model.eml.TaxonKeyword;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;
import org.gbif.provider.webapp.action.BaseResourceAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.Preparable;

public class EmlEditorAction extends BaseMetadataResourceAction implements Preparable{
	@Autowired
	private EmlManager emlManager;
	private Eml eml;
	
	protected String back;
	protected String backPage;
	protected final String BACK = "back";
	protected String next;
	protected String nextPage;
	protected final String NEXT = "next";
	
	protected Map<String, String> isoCountryI18nCodeMap;
	protected Map<String, String> isoLanguageI18nCodeMap;
	protected Map<String, String> majorTaxonRanks;
	protected Map<String, String> otherTaxonRanks;
	
	public void prepare() {
		super.prepare();
		if (resource!=null){
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

	public String getTaxonomicClassification() {
		String coverage="";
		for (TaxonKeyword k : eml.getTaxonomicClassification()){
			if (k!=null){
				coverage += k.getScientificName()+", ";
			}
		}
		return coverage.substring(0, coverage.lastIndexOf(","));
	}

	public void setTaxonomicClassification(String taxonomicCoverage) {
		List<TaxonKeyword> keywords = new ArrayList<TaxonKeyword>();
		for (String k : StringUtils.split(taxonomicCoverage, ",")){
			k=StringUtils.trimToNull(k);
			if (k!=null){
				keywords.add(new TaxonKeyword(k,null,null));
			}
		}
		eml.setTaxonomicClassification(keywords);
	}

	public String getKeywords() {
		String keywords="";
		for (String k : eml.getKeywords()){
			if (k!=null){
				keywords += k+", ";
			}
		}
		return keywords.substring(0, keywords.lastIndexOf(","));
	}

	public void setKeywords(String keywordString) {
		List<String> keywords = new ArrayList<String>();
		for (String k : StringUtils.split(keywordString, ",")){
			k=StringUtils.trimToNull(k);
			if (k!=null){
				keywords.add(k);
			}
		}
		eml.setKeywords(keywords);
	}
	
	public Map<String, String> getIsoCountryI18nCodeMap() {
		return isoCountryI18nCodeMap;
	}

	public void setIsoCountryI18nCodeMap(Map<String, String> isoCountryI18nCodeMap) {
		this.isoCountryI18nCodeMap = translateI18nMap(isoCountryI18nCodeMap);
	}

	public Map<String, String> getMajorTaxonRanks() {
		return majorTaxonRanks;
	}

	public void setMajorTaxonRanks(Map<String, String> majorTaxonRanks) {
		this.majorTaxonRanks = translateI18nMap(majorTaxonRanks);
	}

	public Map<String, String> getOtherTaxonRanks() {
		return otherTaxonRanks;
	}

	public void setOtherTaxonRanks(Map<String, String> otherTaxonRanks) {
		this.otherTaxonRanks = translateI18nMap(otherTaxonRanks);
	}

	public Map<String, String> getAllRanks() {
		Map<String, String> ranks = new HashMap<String, String>(majorTaxonRanks);
		ranks.putAll(otherTaxonRanks);
		return ranks;
	}
	
}
