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
import org.gbif.ipt.model.ResourceLoadNotice;
import org.gbif.ipt.model.ResourceLoadNotice.Type;
import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;
import org.gbif.ipt.service.manage.MetadataReader;
import org.gbif.ipt.service.manage.ResourceMetadataLoader;
import org.gbif.ipt.utils.EmlUtils;
import org.gbif.metadata.eml.ipt.model.Eml;

import jakarta.inject.Inject;
import java.io.File;
import java.math.BigDecimal;
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

  @Inject
  public ResourceMetadataLoaderImpl(
      DataDir dataDir,
      MetadataReader metadataReader) {
    this.dataDir = dataDir;
    this.metadataReader = metadataReader;
  }

  /**
   * Loads a resource's metadata from its eml.xml file located inside its resource directory. If no eml.xml file was
   * found, the resource is loaded with an empty EML instance.
   * If eml.xml can't be read, the last published EML is used, or an empty EML if there is none, and the resource gets
   * a load notice. eml.xml isn't changed: saving the metadata replaces it.
   *
   * @param resource resource
   */
  @Override
  public void loadEml(Resource resource) {
    File emlFile = dataDir.resourceEmlFile(resource.getShortname());
    if (emlFile == null || !emlFile.exists()) {
      resource.setEml(new Eml());
      return;
    }

    try {
      // loads resource metadata, use US Locale to interpret it because uses '.' for decimal separator
      resource.setEml(EmlUtils.readWithLocale(emlFile, Locale.US));
      resource.removeEmlLoadNotices();
      return;
    } catch (Exception e) {
      LOG.error("Can't read EML of resource {}", resource.getShortname(), e);
    }

    BigDecimal version = resource.getLastPublishedVersionsVersion();
    if (version != null) {
      File publishedEmlFile = dataDir.resourceEmlFile(resource.getShortname(), version);
      try {
        resource.setEml(EmlUtils.readWithLocale(publishedEmlFile, Locale.US));
        resource.addLoadNotice(new ResourceLoadNotice(Type.EML_FROM_PUBLISHED, version.toPlainString()));
        LOG.warn("Using the published EML version {} of resource {}", version.toPlainString(), resource.getShortname());
        return;
      } catch (Exception e) {
        LOG.error("Can't read published EML version {} of resource {} either",
            version.toPlainString(), resource.getShortname(), e);
      }
    }

    resource.setEml(new Eml());
    resource.addLoadNotice(new ResourceLoadNotice(Type.EML_UNREADABLE, null));
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
    if (metadataFile != null && metadataFile.exists() && !metadataFile.isDirectory()) {
      try {
        resource.setDataPackageMetadata(
            metadataReader.readValue(metadataFile, metadataClassForType(resource.getCoreType())));
        resource.removeDataPackageMetadataLoadNotices();
        return;
      } catch (Exception e) {
        LOG.error("Can't read metadata of resource {}", resource.getShortname(), e);
      }

      // fall back to the last published metadata, the file isn't changed: saving the metadata replaces it
      BigDecimal version = resource.getLastPublishedVersionsVersion();
      if (version != null) {
        File publishedFile =
            dataDir.resourceDatapackageMetadataFile(resource.getShortname(), resource.getCoreType(), version);
        try {
          resource.setDataPackageMetadata(
              metadataReader.readValue(publishedFile, metadataClassForType(resource.getCoreType())));
          resource.addLoadNotice(new ResourceLoadNotice(Type.METADATA_FROM_PUBLISHED, version.toPlainString()));
          LOG.warn("Using the published metadata version {} of resource {}",
              version.toPlainString(), resource.getShortname());
          return;
        } catch (Exception e) {
          LOG.error("Can't read published metadata version {} of resource {} either",
              version.toPlainString(), resource.getShortname(), e);
        }
      }
      resource.addLoadNotice(new ResourceLoadNotice(Type.METADATA_UNREADABLE, null));
    }

    if (metadata instanceof FrictionlessMetadata) {
      ((FrictionlessMetadata<?, ?, ?>) metadata).setName(resource.getShortname());
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
