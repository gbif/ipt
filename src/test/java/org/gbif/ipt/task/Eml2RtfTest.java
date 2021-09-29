/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.task;

import org.gbif.ipt.mock.MockVocabulariesManager;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.parsers.ParserConfigurationException;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.rtf.RtfWriter2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

public class Eml2RtfTest {

  private VocabulariesManager mockedVocabManager;
  private Eml2Rtf eml2Rtf;

  @BeforeEach
  public void setUp() throws ParserConfigurationException, SAXException {
    eml2Rtf = new Eml2Rtf();
    mockedVocabManager = new MockVocabulariesManager();
    eml2Rtf.setVocabManager(mockedVocabManager);
  }

  @Test
  public void generateRtfFile() {
    try {
      Document doc = new Document(PageSize.LETTER);
      Resource resource = new Resource();
      Eml eml = EmlFactory.build(Eml2RtfTest.class.getResourceAsStream("/data/eml.xml"));
      resource.setEml(eml);
      resource.setShortname("resource");
      User creator = new User();
      creator.setFirstname("Markus");
      creator.setLastname("Doring");
      resource.setCreator(creator);
      File rtfTempFile = File.createTempFile("resource", ".rtf");
      System.out.println("Writing temporary test RTF file to " + rtfTempFile.getAbsolutePath());
      OutputStream out = new FileOutputStream(rtfTempFile);
      RtfWriter2.getInstance(doc, out);
      eml2Rtf.writeEmlIntoRtf(doc, resource);
      out.close();
      // clean-up tmp file
      rtfTempFile.deleteOnExit();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (DocumentException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
  }
}
