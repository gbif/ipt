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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.model.VocabularyConcept;
import org.gbif.ipt.model.VocabularyTerm;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.metadata.eml.EmlFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.xml.sax.SAXException;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.rtf.RtfWriter2;

import static org.mockito.Mockito.*;

/**
 * 
 * @author htobon
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class Eml2RtfTest {
	
	@Mock private VocabulariesManager mockedVocabManager;
	@Mock private Vocabulary mockedVocabulary;
	@Mock private VocabularyConcept mockedVocabConcept;
	
	private VocabularyTerm testVocabTerm;	
	private Eml2Rtf eml2Rtf;
	
	@Before
	public void setUp() {
		// TODO This method should be used to configure some Mock class. If needed.
		testVocabTerm = new VocabularyTerm();
		testVocabTerm.setLang("Country");
		testVocabTerm.setTitle("The Country");
		
		eml2Rtf = new Eml2Rtf();
		eml2Rtf.setVocabManager(mockedVocabManager);
		when(mockedVocabManager.get(anyString())).thenReturn(mockedVocabulary);
		when(mockedVocabulary.findConcept(anyString())).thenReturn(mockedVocabConcept);
		when(mockedVocabConcept.getPreferredTerm(anyString())).thenReturn(testVocabTerm);
		
	}
	
	@Test
	public void generateRtfFile() {
		File rtfTempFile = null;
		try {
			Document doc = new Document();
			Resource resource = new Resource();
			resource.setEml(EmlFactory.build(new FileInputStream("./src/test/resources/data/eml.xml"))); //or eml2.xml
			//resource.setEml(EmlFactory.build(new FileInputStream("./src/test/resources/data/eml-worms_gbif_example-v1.xml"))); //or eml2.xml
			resource.setShortname("resource");
			User creator = new User();
			creator.setFirstname("Markus");
			creator.setLastname("Döring");
			resource.setCreator(creator);
			rtfTempFile = File.createTempFile("resource", ".rtf");
			System.out.println("Writing temporary test RTF file to "+rtfTempFile.getAbsolutePath());
			
			OutputStream out;
			out = new FileOutputStream(rtfTempFile);

			RtfWriter2.getInstance(doc, out);
			eml2Rtf.writeEmlIntoRtf(doc, resource);
			out.close();			
			//Runtime.getRuntime().exec("C:/Program Files/Microsoft Office/Office12/WINWORD.EXE "+rtfTempFile.getAbsolutePath());			
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
}
