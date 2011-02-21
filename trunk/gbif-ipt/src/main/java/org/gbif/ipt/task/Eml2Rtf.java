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
import java.util.HashSet;

import org.gbif.ipt.model.Resource;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.KeywordSet;

import com.google.inject.Singleton;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Header;
import com.lowagie.text.Paragraph;

/**
 * Populates a RTF document with a resources metadata, mainly derived from its EML. TODO: add more eml metadata
 * 
 * @author markus
 * @author htobon
 * 
 */
@Singleton
public class Eml2Rtf {
	private Font font = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, new Color(0, 0, 0));
	private Font fontItalic = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12, Font.ITALIC, new Color(0, 0, 0));
	private Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.BOLD, new Color(0, 0, 0));
	private Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD, new Color(0, 0, 0));
	
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
		//addKeywords(doc, keys);

		addPara(doc, "Abstract", fontTitle, 0, Element.ALIGN_LEFT);
		addPara(doc, eml.getDescription(), fontItalic, 0, Element.ALIGN_JUSTIFIED);
		doc.close();
	}

	private void addAuthors(Document doc, Eml eml) throws DocumentException {
		
		ArrayList<Agent> agents = new ArrayList<Agent>();
		agents.add(eml.getResourceCreator());
		agents.add(eml.getMetadataProvider());
		agents.addAll(eml.getAssociatedParties());
		
		// Adding authors
		Paragraph p = new Paragraph();
		p.setFont(font);
		p.setAlignment(Element.ALIGN_CENTER);
		for(int c = 0; c < agents.size(); c++) {
			if(c!=0) p.add(", ");
			// First Name and Last Name
			p.add(agents.get(c).getFirstName()+" "+agents.get(c).getLastName());			
			
			// Looking for addresses of other authors (superscripts should not be repeated).
			int index = 0;
			while(index < c) {				
				if(agents.get(c).getAddress().equals(agents.get(index).getAddress())) {
					p.add(createSuperScript(""+(index+1)));
					break;
				}
				index++;
			}
			if(index == c) {
				p.add(createSuperScript(""+(index+1)));
			}
		}
		doc.add(p);

	}

	private void addKeywords(Document doc, String keys) throws DocumentException {
		addPara(doc, "Keywords", fontHeader, 10, Element.ALIGN_LEFT);
		addPara(doc, keys, fontItalic, 0, Element.ALIGN_LEFT);
	}
	
}
