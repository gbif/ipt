package org.gbif.provider.webapp.action.manage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.SessionAware;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.eml.Role;
import org.gbif.provider.model.eml.TaxonKeyword;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.Vocabulary;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.ThesaurusManager;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;
import org.gbif.provider.webapp.action.BaseResourceAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.Preparable;

public class EmlEditorAction extends BaseMetadataResourceAction implements Preparable{
	@Autowired
	private EmlManager emlManager;
	@Autowired
	private ThesaurusManager thesaurusManager;
	private Eml eml;
	
	protected String next;
	protected String nextPage;
	protected final String NEXT = "next";
	
	protected Map<String, String> isoCountryI18nCodeMap;
	protected Map<String, String> isoLanguageI18nCodeMap;
	protected Map<String, String> ranksI18nCodeMap;
	
	public void prepare() {
		super.prepare();
		if (resource!=null){
			eml = emlManager.load(resource);
			// load term discts according to locale language
			String lang = getLocaleLanguage();
			isoCountryI18nCodeMap = thesaurusManager.getConceptCodeMap(Vocabulary.Country.uri, lang, true);
			isoLanguageI18nCodeMap = thesaurusManager.getConceptCodeMap(Vocabulary.Language.uri, lang, true);
			ranksI18nCodeMap = thesaurusManager.getConceptCodeMap(Rank.URI, lang, false);
		}
	}
		
	public String execute(){
		return SUCCESS;
	}
	
	public String save(){
		if (cancel!=null){
			return CANCEL;
		}
		if (next==null){
			return INPUT;
		}
		resource.setDirty();
		emlManager.save(eml);
		resourceManager.save(resource);
		return SUCCESS;
	}
	
	
	
	public Eml getEml() {
		return eml;
	}

	public void setEml(Eml eml) {
		this.eml = eml;
	}
	
	
	public String getNext() {
		return next;
	}

	public void setNext(String next) {
		this.next = next;
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

	public Map<String, String> getAllRanks() {
		return ranksI18nCodeMap;
	}
	public Map<String, String> getRanksI18nCodeMap() {
		return ranksI18nCodeMap;
	}
	
}
