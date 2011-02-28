/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.task;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeSet;

import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.model.VocabularyConcept;
import org.gbif.ipt.model.VocabularyTerm;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.KeywordSet;
import org.gbif.metadata.eml.TaxonKeyword;
import org.gbif.metadata.eml.TaxonomicCoverage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Header;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;

/**
 * Populates a RTF document with a resources metadata, mainly derived from its EML. 
 * TODO: add more eml metadata
 * TODO: implement internationalisation.
 * 
 * @author markus
 * @author htobon
 * 
 */
@Singleton
public class Eml2Rtf {
	private Font font = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, Color.BLACK);
	private Font fontToComplete = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, Color.RED);
	private Font fontItalic = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12, Font.ITALIC, Color.BLACK);
	private Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.BOLD, Color.BLACK);
	private Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD, Color.BLACK);
	
	@Inject
	private SimpleTextProvider textProvider;	
	@Inject
	private VocabulariesManager vocabManager;

	private void addPara(Document doc, String text, Font font, int spacing, int alignType) throws DocumentException {
		Paragraph p = new Paragraph(text, font);
		if (spacing != 0) {
			p.setSpacingBefore(spacing);
		}
		if (alignType != 0) {
			p.setAlignment(alignType);
		}
		doc.add(p);
	}

	private Chunk createSuperScript(String text) {
		return new Chunk(text).setTextRise(5f);
	}

	public void writeEmlIntoRtf(Document doc, Resource resource) throws DocumentException {
		Eml eml = resource.getEml();
		// write metadata
		doc.addAuthor(resource.getCreator().getName());
		doc.addCreationDate();
		doc.addTitle(eml.getTitle());
		String keys = "";
		for (KeywordSet kw : eml.getKeywords()) {
			if (keys.length() == 0) {
				keys = kw.getKeywordsString(", ");
			} else {
				keys += ", " + kw.getKeywordsString(", ");
			}
		}
		doc.addKeywords(keys);
		doc.add(new Header("inspired by", "William Shakespeare"));
		// write proper doc
		doc.open();
		// title
		addPara(doc, eml.getTitle(), fontHeader, 0, Element.ALIGN_CENTER);
		doc.add(Chunk.NEWLINE);
		// Authors, affiliations and corresponging authors
		addAuthors(doc, eml);
		doc.add(Chunk.NEWLINE);
		// Received, Revised, Accepted, and ‘published’ dates
		/* These are to be manually inserted by the Publisher of the Data Paper 
		 * to indicate the dates of original manuscript submission, revised manuscript 
		 * submission, acceptance of manuscript and publishing of the manuscript 
		 * as Data Paper in the journal. */
		addDates(doc, eml);
		doc.add(Chunk.NEWLINE);
		addCitations(doc, eml);
		doc.add(Chunk.NEWLINE);		
		addAbstract(doc, eml);
		doc.add(Chunk.NEWLINE);		
		addKeywords(doc, keys);
		doc.add(Chunk.NEWLINE);
		addTaxonomicCoverages(doc, eml);
		doc.add(Chunk.NEWLINE);

		doc.close();
	}

	private void addTaxonomicCoverages(Document doc, Eml eml) throws DocumentException {
		Paragraph p = new Paragraph();
		p.setAlignment(Element.ALIGN_JUSTIFIED);
		p.setFont(font);
		
		p.add(new Phrase("Taxonomic Coverage", fontTitle));
		p.add(Chunk.NEWLINE);
		System.out.println(eml.getTaxonomicCoverages().size());
		for(TaxonomicCoverage taxcoverage : eml.getTaxonomicCoverages()) {
			p.add(new Phrase("General Taxonomic Coverage Description: ", fontTitle));
			p.add(taxcoverage.getDescription());
			p.add(Chunk.NEWLINE);
			Map<String, String> ranks = vocabManager.getI18nVocab(Constants.VOCAB_URI_RANKS, Locale.getDefault().toString(), false);
			
			//TODO working on! here
			
			p.add(Chunk.NEWLINE);
			p.add(new Phrase("Common Name: ", fontTitle));
			for(int c = 0 ; c < taxcoverage.getTaxonKeywords().size(); c++) {
				if(c != 0) p.add(", ");
				// TODO Common Names should not be repeated.
				p.add(((TaxonKeyword) taxcoverage.getTaxonKeywords().get(c)).getCommonName());
			}
			
			
			p.add(Chunk.NEWLINE);
		}
		
		
		doc.add(p);
		p.clear();
		
	}

	private void addAbstract(Document doc, Eml eml) throws DocumentException {
		addPara(doc, "Abstract", fontTitle, 0, Element.ALIGN_LEFT);
		addPara(doc, eml.getDescription(), font, 0, Element.ALIGN_JUSTIFIED);		
	}

	private void addCitations(Document doc, Eml eml) throws DocumentException {
		Paragraph p = new Paragraph();
		p.setAlignment(Element.ALIGN_JUSTIFIED);
		p.setFont(fontToComplete);
		p.add(new Phrase("Citation: ", fontTitle));
		p.add("Combination of Authors, year of data paper publication (in paranthesis), Title, Journal Name, Volume, Issue number (in paranthesis), and doi of the data paper.");
		doc.add(p);
		p.clear();
	}

	private void addDates(Document doc, Eml eml) throws DocumentException {
		Paragraph p = new Paragraph();
		Phrase phrase = new Phrase("{date}", fontToComplete);
		p.setFont(font);
		p.add("Received ");
		p.add(phrase);
		p.add("; Revised ");
		p.add(phrase);
		p.add("; Accepted ");
		p.add(phrase);
		p.add("; Published ");
		p.add(phrase);

		doc.add(p);
		p.clear();
	}

	private void addAuthors(Document doc, Eml eml) throws DocumentException {

		// <AUTHORS>

		// Creating set of authors with different names. (first names + last names).
		TreeSet<Agent> tempAgents = new TreeSet<Agent>(new Comparator<Agent>() {
			public int compare(Agent a, Agent b) {
				return (a.getFirstName() + a.getLastName()).compareTo(b.getFirstName() + b.getLastName());
			}
		});
		tempAgents.add(eml.getResourceCreator());
		tempAgents.add(eml.getMetadataProvider());
		tempAgents.addAll(eml.getAssociatedParties());
		Agent[] agents = new Agent[tempAgents.size()];
		tempAgents.toArray(agents);
		// Adding authors
		Paragraph p = new Paragraph();
		p.setFont(font);
		p.setAlignment(Element.ALIGN_CENTER);
		ArrayList<Agent> affiliations = new ArrayList<Agent>();
		for (int c = 0; c < agents.length; c++) {
			if (c != 0)
				p.add(", ");
			// First Name and Last Name
			p.add(agents[c].getFirstName() + " " + agents[c].getLastName());

			// Looking for addresses of other authors (superscripts should not be repeated).
			int index = 0;
			while (index < c) {
				if (agents[c].getAddress().equals(agents[index].getAddress())) {
					p.add(createSuperScript("" + (index + 1)));
					break;
				}
				index++;
			}
			if (index == c) {
				p.add(createSuperScript("" + (index + 1)));
				affiliations.add(agents[c]);
			}
		}
		doc.add(p);

		doc.add(Chunk.NEWLINE);

		tempAgents.clear();

		// <AFFILIATIONS>

		p = new Paragraph();
		p.setFont(font);
		p.setAlignment(Element.ALIGN_JUSTIFIED);
		for (int c = 0; c < affiliations.size(); c++) {
			if (c != 0)
				p.add("; ");
			p.add((c + 1) + " ");
			p.add(affiliations.get(c).getOrganisation() + ", ");
			p.add(affiliations.get(c).getAddress().getAddress() + ", ");
			p.add(affiliations.get(c).getAddress().getPostalCode() + ", ");
			p.add(affiliations.get(c).getAddress().getCity());			
			if (affiliations.get(c).getAddress().getCountry() != null) {
				p.add(", " + vocabManager.get(Constants.VOCAB_URI_COUNTRY).findConcept(affiliations.get(c).getAddress().getCountry()).getPreferredTerm("en").getTitle());
			}
		}
		doc.add(p);

		doc.add(Chunk.NEWLINE);

		// <Corresponding Authors>

		p = new Paragraph();
		p.setAlignment(Element.ALIGN_JUSTIFIED);
		p.add(new Phrase("Corresponding authors: ", fontTitle));
		p.setFont(font);
		for (int c = 0; c < agents.length; c++) {
			if (c != 0)
				p.add(", ");
			// TODO - Validation for authors with blank email needed.
			p.add(agents[c].getFirstName() + " " + agents[c].getLastName() + " (" + agents[c].getEmail() + ")");
		}

		doc.add(p);
		p.clear();
	}

	private void addKeywords(Document doc, String keys) throws DocumentException {
		addPara(doc, "Keywords", fontHeader, 10, Element.ALIGN_LEFT);
		addPara(doc, keys, fontItalic, 0, Element.ALIGN_LEFT);
	}

	public void setVocabManager(VocabulariesManager vocabManager) {
		this.vocabManager = vocabManager;
	}
	public void setTextProvider(SimpleTextProvider textProvider) {
		this.textProvider = textProvider;
	}
	public String getText(String key) {		
		ResourceBundle res = textProvider.getTexts(Locale.getDefault());
		return textProvider.findText(res, key, "default.message", new String[0]);
		
	}

}
