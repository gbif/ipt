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

import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;
import org.gbif.ipt.service.manage.MetadataReader;
import org.gbif.ipt.service.manage.ResourceMetadataLoader;
import org.gbif.ipt.utils.EmlUtils;
import org.gbif.metadata.eml.ipt.model.Eml;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.gbif.ipt.config.Constants.CAMTRAP_DP;
import static org.gbif.ipt.config.Constants.COL_DP;
import static org.gbif.ipt.utils.MetadataUtils.metadataClassForType;

public class ResourceMetadataLoaderImpl implements ResourceMetadataLoader {

  private static final Logger LOG = LogManager.getLogger(ResourceMetadataLoaderImpl.class);

  private final DataDir dataDir;
  private final MetadataReader metadataReader;

  public ResourceMetadataLoaderImpl(DataDir dataDir, MetadataReader metadataReader) {
    this.dataDir = dataDir;
    this.metadataReader = metadataReader;
  }

  /**
   * Loads a resource's metadata from its eml.xml file located inside its resource directory. If no eml.xml file was
   * found, the resource is loaded with an empty EML instance.
   *
   * @param resource resource
   */
  @Override
  public void loadEml(Resource resource) {
    File emlFile = dataDir.resourceEmlFile(resource.getShortname());
    // loads resource metadata, use US Locale to interpret it because uses '.' for decimal separator
    Eml eml = EmlUtils.loadWithLocale(emlFile, Locale.US);
    resource.setEml(eml);
  }

  /**
   * Loads a resource's metadata from its datapackage.json (for frictionless) or metadata.yaml (for ColDP) file located
   * inside its resource directory.
   * If no file was found, the resource is loaded with an empty metadata class instance.
   *
   * @param resource resource
   */
  @Override
  public void loadDatapackageMetadata(Resource resource) {
    DataPackageMetadata metadata;

    if (CAMTRAP_DP.equals(resource.getCoreType())) {
      metadata = new CamtrapMetadata();
    } else if (COL_DP.equals(resource.getCoreType())) {
      metadata = new ColMetadata();
    } else {
      metadata = new FrictionlessMetadata<>();
    }

    File metadataFile = dataDir.resourceDatapackageMetadataFile(resource.getShortname(), resource.getCoreType());
    if (metadataFile.exists() && !metadataFile.isDirectory()) {
      try {
        metadata = metadataReader.readValue(metadataFile, metadataClassForType(resource.getCoreType()));
      } catch (IOException e) {
        LOG.error("Failed to read resource metadata {}", resource.getShortname());
        LOG.error(e);
        throw new RuntimeException(e);
      }
    } else {
      if (metadata instanceof FrictionlessMetadata) {
        ((FrictionlessMetadata<?, ?, ?>) metadata).setName(resource.getShortname());
      }
    }

    resource.setDataPackageMetadata(metadata);
  }

  @Override
  public void loadMetadata(Resource resource) {
    if (resource.isDataPackage() && resource.isDwcDp()) {
      loadDatapackageMetadata(resource);
      loadEml(resource);
    } else if (resource.isDataPackage()) {
      loadDatapackageMetadata(resource);
    } else {
      loadEml(resource);
    }
  }
}
