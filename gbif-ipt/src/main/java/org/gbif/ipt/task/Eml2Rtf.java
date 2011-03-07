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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.CoordinateUtils;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.BBox;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.GeospatialCoverage;
import org.gbif.metadata.eml.JGTICuratorialUnit;
import org.gbif.metadata.eml.JGTICuratorialUnitType;
import org.gbif.metadata.eml.KeywordSet;
import org.gbif.metadata.eml.PhysicalData;
import org.gbif.metadata.eml.TaxonKeyword;
import org.gbif.metadata.eml.TaxonomicCoverage;
import org.gbif.metadata.eml.TemporalCoverage;
import org.gbif.metadata.eml.TemporalCoverageType;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lowagie.text.Anchor;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Header;
import com.lowagie.text.List;
import com.lowagie.text.ListItem;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;

/**
 * Populates a RTF document with a resources metadata, mainly derived from its EML. TODO: add more eml metadata TODO: implement internationalisation.
 * 
 * @author markus
 * @author htobon
 * 
 */
@Singleton
public class Eml2Rtf {
	private Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, Font.NORMAL, Color.BLACK);
	private Font fontToComplete = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, Font.NORMAL, Color.RED);
	private Font fontItalic = FontFactory.getFont(FontFactory.TIMES_ITALIC, 12, Font.ITALIC, Color.BLACK);
	private Font fontTitle = FontFactory.getFont(FontFactory.TIMES_BOLD, 12, Font.BOLD, Color.BLACK);
	private Font fontHeader = FontFactory.getFont(FontFactory.TIMES_BOLD, 14, Font.BOLD, Color.BLACK);

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
		p.clear();
	}

	private Chunk createSuperScript(String text) {
		return new Chunk(text).setTextRise(5f);
	}

	public void writeEmlIntoRtf(Document doc, Resource resource) throws DocumentException {
		Eml eml = resource.getEml();
		// configure page
		doc.setMargins(72, 72, 72, 72);

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
		// Authors, affiliations and corresponging authors
		addAuthors(doc, eml);
		// Received, Revised, Accepted, and ‘published’ dates
		/* These are to be manually inserted by the Publisher of the Data Paper 
		 * to indicate the dates of original manuscript submission, revised manuscript 
		 * submission, acceptance of manuscript and publishing of the manuscript 
		 * as Data Paper in the journal. */
		addDates(doc, eml);
		addCitations(doc, eml);
		addAbstract(doc, eml);
		addKeywords(doc, keys);
		addTaxonomicCoverages(doc, eml);
		addSpatialCoverage(doc, eml);
		addTemporalCoverages(doc, eml);
		addProjectData(doc, eml);
		addNaturalCollections(doc, eml);
		addMethods(doc, eml);
		addDatasetDescriptions(doc, eml);
		doc.close();
	}

	private void addDatasetDescriptions(Document doc, Eml eml) throws DocumentException {
		Paragraph p = new Paragraph();
		p.setAlignment(Element.ALIGN_JUSTIFIED);
		p.setFont(font);
		for (PhysicalData data : eml.getPhysicalData()) {
			p.add(new Phrase("Dataset descriptions", fontTitle));
			p.add(Chunk.NEWLINE);
			p.add(new Phrase("Object name: ", fontTitle));
			p.add(data.getName());
			p.add(Chunk.NEWLINE);
			p.add(new Phrase("Character encoding: ", fontTitle));
			p.add(data.getCharset());
			p.add(Chunk.NEWLINE);
			p.add(new Phrase("Format name: ", fontTitle));
			p.add(data.getFormat());
			p.add(Chunk.NEWLINE);
			p.add(new Phrase("Format version: ", fontTitle));
			p.add(data.getFormatVersion());
			p.add(Chunk.NEWLINE);
			p.add(new Phrase("Distribution: ", fontTitle));
			Anchor distributionLink = new Anchor(data.getDistributionUrl(), font);
			distributionLink.setReference(data.getDistributionUrl());
			p.add(distributionLink);
			p.add(Chunk.NEWLINE);
			p.add(Chunk.NEWLINE);
		}
		p.add(new Phrase("Publication date: ", fontTitle));
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-DD");
		p.add(f.format(eml.getPubDate()));
		p.add(Chunk.NEWLINE);
		p.add(new Phrase("Language: ", fontTitle));
		p.add(vocabManager.get(Constants.VOCAB_URI_LANGUAGE).findConcept(eml.getLanguage()).getPreferredTerm("en").getTitle());
		p.add(Chunk.NEWLINE);
		p.add(new Phrase("Intellectual rights: ", fontTitle));
		p.add(eml.getIntellectualRights());
		p.add(Chunk.NEWLINE);
		p.add(Chunk.NEWLINE);
		doc.add(p);
		p.clear();
	}

	private void addMethods(Document doc, Eml eml) throws DocumentException {
		Paragraph p = new Paragraph();
		p.setAlignment(Element.ALIGN_JUSTIFIED);
		p.setFont(font);
		p.add(new Phrase("Methods", fontTitle));
		p.add(Chunk.NEWLINE);
		if (eml.getMethodSteps().size() > 0 && eml.getMethodSteps().size() == 1) {
			p.add(new Phrase("Method step description: ", fontTitle));
			p.add(eml.getMethodSteps().get(0));
			p.add(Chunk.NEWLINE);
		} else if (eml.getMethodSteps().size() > 1) {
			p.add(new Phrase("Method step description: ", fontTitle));
			p.add(Chunk.NEWLINE);
			List list = new List(List.UNORDERED, 0);
			list.setIndentationLeft(20);
			for (String method : eml.getMethodSteps()) {
				list.add(new ListItem(method, font));
			}
			p.add(list);
		}
		if (eml.getStudyExtent() != null && !eml.getStudyExtent().equals("")) {
			p.add(new Phrase("Study extent description: ", fontTitle));
			p.add(eml.getStudyExtent());
			p.add(Chunk.NEWLINE);
		}
		if (eml.getStudyExtent() != null && !eml.getSampleDescription().equals("")) {
			p.add(new Phrase("Sampling description: ", fontTitle));
			p.add(eml.getSampleDescription());
			p.add(Chunk.NEWLINE);
		}
		if (eml.getStudyExtent() != null && !eml.getQualityControl().equals("")) {
			p.add(new Phrase("Quality control description: ", fontTitle));
			p.add(eml.getQualityControl());
			p.add(Chunk.NEWLINE);
		}
		doc.add(p);
		p.clear();
	}

	private void addNaturalCollections(Document doc, Eml eml) throws DocumentException {
		Paragraph p = new Paragraph();
		p.setAlignment(Element.ALIGN_JUSTIFIED);
		p.setFont(font);
		p.add(new Phrase("Natural Collections Description", fontTitle));
		p.add(Chunk.NEWLINE);
		p.add(new Phrase("Parent collection identifier: ", fontTitle));
		p.add(eml.getParentCollectionId());
		p.add(Chunk.NEWLINE);
		p.add(new Phrase("Collection name: ", fontTitle));
		p.add(eml.getCollectionName());
		for (TemporalCoverage coverage : eml.getTemporalCoverages()) {
			if (coverage.getType().equals(TemporalCoverageType.FORMATION_PERIOD)) {
				p.add(Chunk.NEWLINE);
				p.add(new Phrase("Formation period: ", fontTitle));
				p.add(coverage.getFormationPeriod());
			}
		}
		for (TemporalCoverage coverage : eml.getTemporalCoverages()) {
			if (coverage.getType().equals(TemporalCoverageType.LIVING_TIME_PERIOD)) {
				p.add(Chunk.NEWLINE);
				p.add(new Phrase("Living time period: ", fontTitle));
				p.add(coverage.getLivingTimePeriod());
			}
		}
		p.add(Chunk.NEWLINE);
		p.add(new Phrase("Specimen preservation method: ", fontTitle));
		p.add(eml.getSpecimenPreservationMethod());
		for (JGTICuratorialUnit unit : eml.getJgtiCuratorialUnits()) {
			p.add(Chunk.NEWLINE);
			p.add(new Phrase("Curatorial unit: ", fontTitle));
			if (unit.getType().equals(JGTICuratorialUnitType.COUNT_RANGE)) {
				p.add("Between " + unit.getRangeStart() + " and " + unit.getRangeEnd());
			}
			if (unit.getType().equals(JGTICuratorialUnitType.COUNT_WITH_UNCERTAINTY)) {
				p.add(unit.getRangeMean() + " with an uncertainty of " + unit.getUncertaintyMeasure());
			}
			p.add(" (" + unit.getUnitType() + ")");
		}
		p.add(Chunk.NEWLINE);
		doc.add(p);
		p.clear();
	}

	private void addProjectData(Document doc, Eml eml) throws DocumentException {
		Paragraph p = new Paragraph();
		p.setAlignment(Element.ALIGN_JUSTIFIED);
		p.setFont(font);
		p.add(new Phrase("Project description", fontTitle));
		p.add(Chunk.NEWLINE);
		p.add(new Phrase("Project title: ", fontTitle));
		p.add(eml.getProject().getTitle());
		p.add(Chunk.NEWLINE);
		p.add(new Phrase("Personnel: ", fontTitle));
		p.add(eml.getProject().getPersonnel().getFirstName() + " " + eml.getProject().getPersonnel().getLastName());
		p.add(Chunk.NEWLINE);
		p.add(new Phrase("Funding: ", fontTitle));
		p.add(eml.getProject().getFunding());
		p.add(Chunk.NEWLINE);
		p.add(new Phrase("Study Area Descriptions/descriptor: ", fontTitle));
		p.add(eml.getProject().getStudyAreaDescription().getDescriptorValue());
		p.add(Chunk.NEWLINE);
		p.add(new Phrase("Design Description: ", fontTitle));
		p.add(eml.getProject().getDesignDescription());
		p.add(Chunk.NEWLINE);
		doc.add(p);
		p.clear();
	}

	private void addTemporalCoverages(Document doc, Eml eml) throws DocumentException {
		Paragraph p = new Paragraph();
		p.setAlignment(Element.ALIGN_JUSTIFIED);
		p.setFont(font);
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
		SimpleDateFormat timeFormat = new SimpleDateFormat("SSS");
		SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
		boolean firstCoverage = true;
		for (TemporalCoverage coverage : eml.getTemporalCoverages()) {
			if (coverage.getType().equals(TemporalCoverageType.SINGLE_DATE)) {
				if (!firstCoverage) {
					p.add(Chunk.NEWLINE);
				} else {
					firstCoverage = false;
				}
				p.add(new Phrase("Temporal Coverage: ", fontTitle));
				if (timeFormat.format(coverage.getStartDate()).equals("001")) {
					p.add(yearFormat.format(coverage.getStartDate()));
				} else {
					p.add(dateFormat.format(coverage.getStartDate()));
				}
				p.add(Chunk.NEWLINE);
			} else if (coverage.getType().equals(TemporalCoverageType.DATE_RANGE)) {
				if (!firstCoverage) {
					p.add(Chunk.NEWLINE);
				} else {
					firstCoverage = false;
				}
				p.add(new Phrase("Temporal Coverage: ", fontTitle));
				if (timeFormat.format(coverage.getStartDate()).equals("001")) {
					p.add(yearFormat.format(coverage.getStartDate()));
				} else {
					p.add(dateFormat.format(coverage.getStartDate()));
				}
				p.add(" - ");
				if (timeFormat.format(coverage.getEndDate()).equals("001")) {
					p.add(yearFormat.format(coverage.getEndDate()));
				} else {
					p.add(dateFormat.format(coverage.getEndDate()));
				}
				p.add(Chunk.NEWLINE);
			}
		}
		doc.add(p);
		p.clear();
	}

	private void addSpatialCoverage(Document doc, Eml eml) throws DocumentException {
		Paragraph p = new Paragraph();
		p.setAlignment(Element.ALIGN_JUSTIFIED);
		p.setFont(font);
		boolean firstCoverage = true;
		for (GeospatialCoverage coverage : eml.getGeospatialCoverages()) {
			if (!firstCoverage) {
				p.add(Chunk.NEWLINE);
			} else {
				firstCoverage = false;
			}
			p.add(new Phrase("Spatial Coverage", fontTitle));
			p.add(Chunk.NEWLINE);
			p.add(Chunk.NEWLINE);
			p.add(new Phrase("General Spatial Coverage: ", fontTitle));
			p.add(coverage.getDescription());
			p.add(Chunk.NEWLINE);
			p.add(new Phrase("Coordinates: ", fontTitle));
			BBox coordinates = coverage.getBoundingCoordinates();
			p.add(CoordinateUtils.decToDms(coordinates.getMin().getLatitude(), CoordinateUtils.LATITUDE));
			p.add(" and ");
			p.add(CoordinateUtils.decToDms(coordinates.getMax().getLatitude(), CoordinateUtils.LATITUDE));
			p.add(" Latitude; ");
			p.add(CoordinateUtils.decToDms(coordinates.getMin().getLongitude(), CoordinateUtils.LONGITUDE));
			p.add(" and ");
			p.add(CoordinateUtils.decToDms(coordinates.getMax().getLongitude(), CoordinateUtils.LONGITUDE));
			p.add(" Longitude");
			p.add(Chunk.NEWLINE);
		}
		doc.add(p);
		p.clear();

	}

	private void addTaxonomicCoverages(Document doc, Eml eml) throws DocumentException {
		Paragraph p = new Paragraph();
		p.setAlignment(Element.ALIGN_JUSTIFIED);
		p.setFont(font);
		boolean firstTaxon = true;
		for (TaxonomicCoverage taxcoverage : eml.getTaxonomicCoverages()) {
			if (!firstTaxon) {
				p.add(Chunk.NEWLINE);
			}
			firstTaxon = false;
			p.add(new Phrase("Taxonomic Coverage", fontTitle));
			p.add(Chunk.NEWLINE);
			p.add(Chunk.NEWLINE);
			p.add(new Phrase("General Taxonomic Coverage Description: ", fontTitle));
			p.add(taxcoverage.getDescription());
			p.add(Chunk.NEWLINE);
			Map<String, String> ranks = vocabManager.getI18nVocab(Constants.VOCAB_URI_RANKS, Locale.getDefault().toString(), false);
			boolean firstRank = true;
			for (String rank : ranks.keySet()) {
				boolean wroteRank = false;
				for (TaxonKeyword keyword : taxcoverage.getTaxonKeywords()) {
					if (keyword.getRank() != null && keyword.equals(rank)) {
						if (!wroteRank) {
							if (!firstRank) {
								p.add(", ");
							} else {
								p.add(new Phrase("Taxonomic Ranks: ", fontTitle));
								p.add(Chunk.NEWLINE);
							}
							p.add(rank + ": ");
							p.add(keyword.getScientificName());
							wroteRank = true;
							firstRank = false;
						} else {
							p.add(", " + keyword.getScientificName());
						}
					}
				}
			}
			boolean isFirst = true;
			for (TaxonKeyword keyword : taxcoverage.getTaxonKeywords()) {
				if (keyword.getCommonName() != null && !keyword.getCommonName().equals("")) {
					if (!isFirst) {
						p.add(", ");
					} else {
						p.add(new Phrase("Common Name: ", fontTitle));
					}
					isFirst = false;
					p.add(keyword.getCommonName());
				}
			}
		}
		p.add(Chunk.NEWLINE);
		doc.add(p);
		p.clear();
	}

	private void addAbstract(Document doc, Eml eml) throws DocumentException {
		if (eml.getDescription() != null) {
			addPara(doc, "Abstract", fontTitle, 0, Element.ALIGN_LEFT);
			addPara(doc, eml.getDescription(), font, 0, Element.ALIGN_JUSTIFIED);
		}
	}

	private void addCitations(Document doc, Eml eml) throws DocumentException {
		Paragraph p = new Paragraph();
		p.setAlignment(Element.ALIGN_JUSTIFIED);
		p.setFont(fontToComplete);
		p.add(new Phrase("Citation: ", fontTitle));
		p.add("Combination of Authors, year of data paper publication (in paranthesis), Title, Journal Name, Volume, Issue number (in paranthesis), and doi of the data paper.");
		p.add(Chunk.NEWLINE);
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
		p.add(Chunk.NEWLINE);
		doc.add(p);
		p.clear();
	}

	private void addAuthors(Document doc, Eml eml) throws DocumentException {
		// <AUTHORS>
		// Creating set of authors with different names. (first names + last names).
		LinkedHashSet<Agent> tempAgents = new LinkedHashSet<Agent>();

		/*TreeSet<Agent> tempAgents2 = new TreeSet<Agent>(new Comparator<Agent>() {
			public int compare(Agent a, Agent b) {
				return (a.getFirstName() + a.getLastName()).compareTo(b.getFirstName() + b.getLastName());
			}
		});*/
		if (eml.getResourceCreator() != null) {
			tempAgents.add(eml.getResourceCreator());
		}
		if (eml.getMetadataProvider() != null) {
			tempAgents.add(eml.getMetadataProvider());
		}
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
			if (agents[c].getFirstName() != null) {
				p.add(agents[c].getFirstName() + " ");
			}
			p.add(agents[c].getLastName());
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
		p.clear();
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
			if (affiliations.get(c).getOrganisation() != null) {
				p.add(affiliations.get(c).getOrganisation() + ", ");
			}
			if (affiliations.get(c).getAddress().getAddress() != null) {
				p.add(affiliations.get(c).getAddress().getAddress() + ", ");
			}
			if (affiliations.get(c).getAddress().getPostalCode() != null) {
				p.add(affiliations.get(c).getAddress().getPostalCode() + ", ");
			}
			if (affiliations.get(c).getAddress().getCity() != null) {
				p.add(affiliations.get(c).getAddress().getCity());
			}
			if (affiliations.get(c).getAddress().getCountry() != null) {
				p.add(", " + vocabManager.get(Constants.VOCAB_URI_COUNTRY).findConcept(affiliations.get(c).getAddress().getCountry()).getPreferredTerm("en").getTitle());
			}
		}
		doc.add(p);
		p.clear();
		doc.add(Chunk.NEWLINE);
		// <Corresponding Authors>
		p = new Paragraph();
		p.setAlignment(Element.ALIGN_JUSTIFIED);
		p.add(new Phrase("Corresponding authors: ", fontTitle));
		p.setFont(font);
		boolean isFirst = true;
		if (eml.getResourceCreator() != null) {
			if (eml.getResourceCreator().getFirstName() != null) {
				p.add(eml.getResourceCreator().getFirstName() + " ");
			}
			p.add(eml.getResourceCreator().getLastName());
			if (eml.getResourceCreator().getEmail() != null) {
				p.add(" (" + eml.getResourceCreator().getEmail() + ")");
			}
			isFirst = false;
		}
		if (eml.getMetadataProvider() != null) {
			if (!isFirst) {
				p.add(", ");
			}
			if (eml.getMetadataProvider().getFirstName() != null) {
				p.add(eml.getMetadataProvider().getFirstName() + " ");
			}
			p.add(eml.getMetadataProvider().getLastName());
			if (eml.getMetadataProvider().getEmail() != null) {
				p.add(" (" + eml.getMetadataProvider().getEmail() + ")");
			}
		}
		p.add(Chunk.NEWLINE);
		doc.add(p);
		p.clear();
	}

	private void addKeywords(Document doc, String keys) throws DocumentException {
		if (keys != null && !keys.equals("")) {
			addPara(doc, "Keywords", fontTitle, 10, Element.ALIGN_LEFT);
			addPara(doc, keys, font, 0, Element.ALIGN_LEFT);
			doc.add(Chunk.NEWLINE);
		}
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
