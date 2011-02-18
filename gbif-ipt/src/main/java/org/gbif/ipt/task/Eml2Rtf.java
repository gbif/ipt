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

import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlFactory;
import org.gbif.metadata.eml.KeywordSet;
import org.xml.sax.SAXException;

import com.google.inject.Singleton;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Header;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.rtf.RtfWriter2;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Populates a RTF document with a resources metadata, mainly derived from its EML. TODO: add more eml metadata
 * 
 * @author markus
 * 
 */
@Singleton
public class Eml2Rtf {
	private Font font = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.NORMAL, new Color(0, 0, 0));
	private Font fontItalic = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.ITALIC, new Color(0, 0, 0));
	private Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA, 24, Font.BOLD, new Color(0, 0, 0));
	private Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD, new Color(0, 0, 0));
	
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

		addPara(doc, eml.getTitle(), fontTitle, 2, Element.ALIGN_CENTER);

		addPara(doc, "Description", fontHeader, 10, Element.ALIGN_CENTER);
		addPara(doc, eml.getDescription(), font, 10, Element.ALIGN_JUSTIFIED);
		addAuthors(doc, eml);

		addKeywords(doc, keys);

		doc.close();
	}

	private void addAuthors(Document doc, Eml eml) throws DocumentException {
		int count = 1;
		Agent agent;
		Paragraph p = new Paragraph("", font);
		if ((agent = eml.getContact()) != null) {
			p.add(agent.getFirstName() + " " + agent.getLastName());
			p.add(createSuperScript("" + count));
		}
		count++;
		if ((agent = eml.getResourceCreator()) != null) {
			if (!p.isEmpty())
				p.add(", ");

			p.add(agent.getFirstName() + " " + agent.getLastName());
			p.add(createSuperScript("" + count));
		}

		count++;
		if ((agent = eml.getMetadataProvider()) != null) {
			if (!p.isEmpty())
				p.add(", ");

			p.add(agent.getFirstName() + " " + agent.getLastName());
			p.add(new Chunk("" + count).setTextRise(5f));
		}

		p.setAlignment(Element.ALIGN_CENTER);
		doc.add(p);

	}

	private void addKeywords(Document doc, String keys) throws DocumentException {
		addPara(doc, "Keywords", fontHeader, 10, Element.ALIGN_CENTER);
		addPara(doc, keys, fontItalic, 0, Element.ALIGN_CENTER);
	}
	
}
