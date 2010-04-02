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
package org.gbif.provider.webapp.action.manage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.eml.Role;
import org.gbif.provider.model.eml.TaxonKeyword;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.Vocabulary;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.ThesaurusManager;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

/**
* TODO: Documentation.
*
*/
public class EmlEditorAction extends BaseMetadataResourceAction implements
Preparable {

        protected String next;
        protected String nextPage;

        @Autowired
        private EmlManager emlManager;

        @Autowired
        private ThesaurusManager thesaurusManager;

        private Eml eml;

        @Override
        public String execute() {
                if (resource == null) {
                        return RESOURCE404;
                }
                return SUCCESS;
        }

        public String getCountryVocUri() {
                return Vocabulary.Country.uri;
        }

        public Eml getEml() {
                return eml;
        }

        public String getKeywords() {
                // String keywords = "";
                // for (String k : eml.getKeywords()) {
                // if (k != null) {
                // keywords += k + ", ";
                // }
                // }
                // return keywords.substring(0, keywords.lastIndexOf(","));
                // TODO
                return null;
        }

        public String getLanguageVocUri() {
                return Vocabulary.Language.uri;
        }

        public String getNext() {
                return next;
        }

        public String getNextPage() {
                return nextPage;
        }

        public String getRankVocUri() {
                return Rank.URI;
        }

        public List getRoles() {
                return Arrays.asList(Role.values());
        }

        public String getTaxonomicClassification() {
                String coverage = "";
                // for (TaxonKeyword k : eml.getTaxonomicClassification()) {
                // if (k != null) {
                // coverage += k.getScientificName() + ", ";
                // }
                // }
                return coverage.substring(0, coverage.lastIndexOf(","));
        }

        @Override
        public void prepare() {
                super.prepare();
                if (resource != null) {
                        eml = emlManager.load(resource);
                }
        }

        public String save() {
                if (resource == null) {
                        return RESOURCE404;
                }
                if (cancel != null) {
                        return CANCEL;
                }
                if (next == null) {
                        return INPUT;
                }
                resource.setDirty();
                emlManager.save(eml);
                resourceManager.save(resource);
                return SUCCESS;
        }

        public void setEml(Eml eml) {
                this.eml = eml;
        }

        public void setKeywords(String keywordString) {
                List<String> keywords = new ArrayList<String>();
                for (String k : StringUtils.split(keywordString, ",")) {
                        k = StringUtils.trimToNull(k);
                        if (k != null) {
                                keywords.add(k);
                        }
                }
                // eml.setKeywords(keywords);
        }

        public void setNext(String next) {
                this.next = next;
        }

        public void setNextPage(String nextPage) {
                this.nextPage = nextPage;
        }

        public void setTaxonomicClassification(String taxonomicCoverage) {
                List<TaxonKeyword> keywords = new ArrayList<TaxonKeyword>();
                for (String k : StringUtils.split(taxonomicCoverage, ",")) {
                        k = StringUtils.trimToNull(k);
                        if (k != null) {
                                // keywords.add(TaxonKeyword.create(k, null, null));
                        }
                }
                // eml.setTaxonomicClassification(keywords);
        }
}
