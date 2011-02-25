/*
 * Copyright 2009 GBIF. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.gbif.ipt.mock;

import org.apache.http.impl.client.DefaultHttpClient;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.IPTModule;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.model.factory.ExtensionFactory;
import org.gbif.ipt.model.factory.ThesaurusHandlingRule;
import org.gbif.ipt.model.factory.VocabularyFactory;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.admin.impl.VocabulariesManagerImpl;
import org.gbif.ipt.service.admin.impl.VocabulariesManagerImpl.UpdateResult;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.service.registry.impl.RegistryManagerImpl;
import org.gbif.ipt.service.registry.impl.RegistryManagerImplTest;
import org.gbif.ipt.utils.IptMockBaseTest;
import org.xml.sax.SAXException;

import org.mockito.Mock;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/**
 * TODO: Documentation.
 */
public class MockVocabulariesManager extends IptMockBaseTest implements VocabulariesManager {

	private VocabulariesManager vocabManager;
	private ConfigWarnings warnings;
	@Mock private ExtensionManager mockedExtensionManager;

	public MockVocabulariesManager() throws ParserConfigurationException, SAXException {
		SAXParserFactory sax = guice.provideNsAwareSaxParserFactory();
		VocabularyFactory vocabFactory = new VocabularyFactory(buildHttpClient(), sax);
		RegistryManager registryManager = new RegistryManagerImpl(cfg, dataDir, buildHttpUtil(), buildSaxFactory());		
		warnings = new ConfigWarnings();		
		vocabManager = new VocabulariesManagerImpl(cfg, dataDir, vocabFactory, buildHttpUtil(), registryManager, mockedExtensionManager, warnings);
		
	}

	public void delete(String uri) {
	}

	public Vocabulary get(String uri) {
		return new Vocabulary();
	}

	public Vocabulary get(URL url) {
		return new Vocabulary();
	}

	/*
	 * (non-Javadoc)
	 * @see org.gbif.ipt.service.admin.VocabulariesManager#getI18nVocab(java.lang.String, java.lang.String)
	 */
	public Map<String, String> getI18nVocab(String uri, String lang, boolean sort) {
		Map<String, String> vocabMap = new LinkedHashMap<String, String>();
		//TODO 
		return vocabManager.getI18nVocab(uri, lang, sort);
		
		
	}

	public List<Vocabulary> list() {
		return new ArrayList<Vocabulary>();
	}

	public int load() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gbif.ipt.service.admin.VocabulariesManager#updateAll()
	 */
	public UpdateResult updateAll() {
		return null;
	}

}
