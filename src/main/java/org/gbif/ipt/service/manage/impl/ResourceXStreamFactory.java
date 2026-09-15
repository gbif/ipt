/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.service.manage.impl;

import org.gbif.api.model.common.DOI;
import org.gbif.dwc.terms.Term;
import org.gbif.ipt.action.portal.OrganizedTaxonomicKeywords;
import org.gbif.ipt.model.DataPackageField;
import org.gbif.ipt.model.DataPackageFieldConstraints;
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageFieldReference;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageTableSchema;
import org.gbif.ipt.model.DataPackageTableSchemaForeignKey;
import org.gbif.ipt.model.ExcelFileSource;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.InferredCamtrapGeographicScope;
import org.gbif.ipt.model.InferredCamtrapMetadata;
import org.gbif.ipt.model.InferredCamtrapTaxonomicScope;
import org.gbif.ipt.model.InferredCamtrapTemporalScope;
import org.gbif.ipt.model.InferredEmlGeographicCoverage;
import org.gbif.ipt.model.InferredEmlMetadata;
import org.gbif.ipt.model.InferredEmlTaxonomicCoverage;
import org.gbif.ipt.model.InferredEmlTemporalCoverage;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.SqlSource;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.model.UrlSource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.converter.PasswordEncrypter;
import org.gbif.ipt.model.converter.SafeTreeMapConverter;
import org.gbif.ipt.model.converter.SafeTreeSetConverter;
import org.gbif.metadata.eml.ipt.model.TaxonKeyword;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;

public class ResourceXStreamFactory {

  public static XStream create(ResourceConvertersManager convertersManager, PasswordEncrypter passwordEncrypter) {
    XStream xstream = new XStream();

    xstream.addPermission(AnyTypePermission.ANY);
    xstream.ignoreUnknownElements();
    xstream.alias("resource", Resource.class);
    xstream.alias("user", User.class);

    // aliases for inferred metadata
    xstream.alias("inferredMetadata", InferredEmlMetadata.class);
    xstream.alias("inferredMetadataCamtrap", InferredCamtrapMetadata.class);
    xstream.alias("inferredGeographicCoverage", InferredEmlGeographicCoverage.class);
    xstream.alias("inferredGeographicScope", InferredCamtrapGeographicScope.class);
    xstream.alias("inferredTaxonomicCoverage", InferredEmlTaxonomicCoverage.class);
    xstream.alias("inferredTaxonomicScope", InferredCamtrapTaxonomicScope.class);
    xstream.alias("inferredTemporalCoverage", InferredEmlTemporalCoverage.class);
    xstream.alias("inferredTemporalScope", InferredCamtrapTemporalScope.class);
    xstream.alias("taxonKeyword", TaxonKeyword.class);
    xstream.alias("organizedTaxonomicKeywords", OrganizedTaxonomicKeywords.class);

    xstream.alias("filesource", TextFileSource.class);
    xstream.alias("excelsource", ExcelFileSource.class);
    xstream.alias("sqlsource", SqlSource.class);
    xstream.alias("urlsource", UrlSource.class);
    xstream.alias("mapping", ExtensionMapping.class);
    xstream.alias("field", PropertyMapping.class);
    xstream.alias("dataPackageMapping", DataPackageMapping.class);
    xstream.alias("dataPackageFieldMapping", DataPackageFieldMapping.class);
    xstream.alias("tableSchema", DataPackageTableSchema.class);
    xstream.alias("dataPackageField", DataPackageField.class);
    xstream.alias("dataPackageForeignKey", DataPackageTableSchemaForeignKey.class);
    xstream.alias("dataPackageFieldReference", DataPackageFieldReference.class);
    xstream.alias("constraints", DataPackageFieldConstraints.class);
    xstream.alias("versionhistory", VersionHistory.class);
    xstream.alias("doi", DOI.class);

    // transient properties
    xstream.omitField(Resource.class, "shortname");
    xstream.omitField(Resource.class, "eml");
    xstream.omitField(Resource.class, "dataPackageMetadata");
    xstream.omitField(Resource.class, "type");
    // inferred metadata in the separate file
    xstream.omitField(Resource.class, "inferredMetadata");
    // make files transient to allow moving the datadir
    xstream.omitField(TextFileSource.class, "file");

    // Read legacy TreeMap/TreeSet without triggering XStream's TreeMapConverter (Struts 7/Java 17 issues).
    xstream.registerConverter(new SafeTreeMapConverter(), 10000);
    xstream.registerConverter(new SafeTreeSetConverter(), 10000);
    // persist only emails for users
    xstream.registerConverter(convertersManager.getUserConverter());
    // custom converter for ExtensionMapping
    xstream.registerConverter(convertersManager.getExtensionMappingConverter());
    // persist only rowtype
    xstream.registerConverter(convertersManager.getExtensionConverter());
    // persist only qualified concept name
    xstream.registerConverter(convertersManager.getConceptTermConverter());
    // persist only the schema identifier, table schema name and field name
    xstream.registerConverter(convertersManager.getDataSchemaConverter());
    xstream.registerConverter(convertersManager.getTableSchemaNameConverter());
    xstream.registerConverter(convertersManager.getDataPackageFieldConverter());
    // encrypt passwords
    xstream.registerConverter(passwordEncrypter);

    xstream.addDefaultImplementation(ExtensionProperty.class, Term.class);
    xstream.registerConverter(convertersManager.getOrgConverter());
    xstream.registerConverter(convertersManager.getJdbcInfoConverter());

    return xstream;
  }
}
