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

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.mock.MockAppConfig;
import org.gbif.ipt.mock.MockVocabulariesManager;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.IptMockBaseTest;
import org.gbif.metadata.eml.EmlFactory;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.rtf.RtfWriter2;
import com.opensymphony.xwork2.LocaleProvider;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;

/**
 * @author htobon
 */
@RunWith(MockitoJUnitRunner.class)
public class Eml2RtfTest extends IptMockBaseTest {

  private VocabulariesManager mockedVocabManager;
  private Eml2Rtf eml2Rtf;
  private BaseAction action;

  @Test
  public void generateRtfFile() {
    File rtfTempFile = null;
    try {
      Document doc = new Document(PageSize.LETTER);
      Resource resource = new Resource();
      resource.setEml(EmlFactory.build(new FileInputStream("./src/test/resources/data/eml.xml"))); // or eml2.xml
      resource.setShortname("resource");
      User creator = new User();
      creator.setFirstname("Markus");
      creator.setLastname("Doring");
      resource.setCreator(creator);
      rtfTempFile = File.createTempFile("resource", ".rtf");
      System.out.println("Writing temporary test RTF file to " + rtfTempFile.getAbsolutePath());
      OutputStream out = new FileOutputStream(rtfTempFile);
      RtfWriter2.getInstance(doc, out);
      eml2Rtf.writeEmlIntoRtf(doc, resource, action);
      out.close();
      /*
       * FOR WINDOWS XP TEST ONLY
       * Runtime.getRuntime().exec(
       * "C:/Program Files/Microsoft Office/Office12/WINWORD.EXE " + rtfTempFile.getAbsolutePath());
       */

      // Do not comment the following line if you are going to commit this code.
      rtfTempFile.deleteOnExit();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (DocumentException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    }

  }

  /**
   * This method should be used to configure some Mock class if needed.
   * 
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  @Before
  public void setUp() throws ParserConfigurationException, SAXException {
    eml2Rtf = new Eml2Rtf();
    mockedVocabManager = new MockVocabulariesManager();
    eml2Rtf.setVocabManager(mockedVocabManager);

    // Building mocks.
    AppConfig cfg = MockAppConfig.buildMock();
    SimpleTextProvider textProvider = mock(SimpleTextProvider.class);

    // Stubbing TextProvider
    when(textProvider.getText(any(LocaleProvider.class), anyString(), anyString(), any(Object[].class))).thenReturn(
        "i18n-word");

    action = new BaseAction(textProvider, cfg);
  }
}
