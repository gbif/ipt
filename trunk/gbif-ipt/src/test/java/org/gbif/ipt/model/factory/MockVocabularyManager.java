/*
 * Copyright 2009 GBIF.
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
 */
package org.gbif.ipt.model.factory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.admin.VocabulariesManager;

/**
 * TODO: Documentation.
 * 
 */
public class MockVocabularyManager implements VocabulariesManager {

	public void delete(String uri) {
		// TODO Auto-generated method stub
		
	}

	public Vocabulary get(String uri) {
		return new Vocabulary();
	}

	public Vocabulary get(URL url) {
		return new Vocabulary();
	}

	public List<Vocabulary> list() {
		return new ArrayList<Vocabulary>();
	}

	public void update(Vocabulary vocabulary) {
		// TODO Auto-generated method stub		
	}

	public int load() {
		// TODO Auto-generated method stub
		return 0;
	}

  }
