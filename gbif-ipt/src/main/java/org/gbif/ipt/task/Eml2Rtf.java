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
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.KeywordSet;

import com.google.inject.Singleton;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Header;
import com.lowagie.text.Paragraph;

import java.awt.Color;

/**
 * Populates a RTF document with a resources metadata, mainly derived from its EML.
 * TODO: add more eml metadata
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

  private void addPara(Document doc, String text, Font font, int spacing, boolean alignCenter) throws DocumentException {
    Paragraph p = new Paragraph(text, font);
    if (spacing != 0) {
      p.setSpacingBefore(spacing);
    }
    if (alignCenter) {
      p.setAlignment(Element.ALIGN_CENTER);
    } else {
      p.setAlignment(Element.ALIGN_LEFT);
    }
    doc.add(p);
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

    addPara(doc, eml.getTitle(), fontTitle, 2, true);

    addPara(doc, "Description", fontHeader, 10, true);
    addPara(doc, eml.getDescription(), font, 0, false);

    addPara(doc, "Keywords", fontHeader, 10, true);
    addPara(doc, keys, fontItalic, 0, false);

    doc.close();
  }
}
