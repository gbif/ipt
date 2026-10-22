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
import org.gbif.doi.metadata.datacite.DataCiteMetadata;
import org.gbif.doi.service.DoiException;
import org.gbif.doi.service.DoiExistsException;
import org.gbif.doi.service.InvalidMetadataException;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceDoiService;
import org.gbif.ipt.utils.DataCiteMetadataBuilder;
import org.gbif.ipt.utils.ResourceUtils;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Objects;

import javax.annotation.Nullable;

import jakarta.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResourceDoiServiceImpl implements ResourceDoiService {

  private static final Logger LOG = LogManager.getLogger(ResourceDoiServiceImpl.class);

  private final AppConfig cfg;
  private final DataDir dataDir;
  private final RegistrationManager registrationManager;

  @Inject
  public ResourceDoiServiceImpl(AppConfig cfg, DataDir dataDir, RegistrationManager registrationManager) {
    this.cfg = cfg;
    this.dataDir = dataDir;
    this.registrationManager = registrationManager;
  }

  @Override
  public void executeDoiWorkflow(Resource resource, BigDecimal version, BigDecimal versionReplaced, BaseAction action)
      throws PublicationException {
    // All DOI operations require resource be publicly available, and resource DOI be PUBLIC/PUBLIC_PENDING_PUBLICATION
    if (resource.getDoi() != null && resource.isPubliclyAvailable() && (
        resource.getIdentifierStatus().equals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION) || resource
            .getIdentifierStatus().equals(IdentifierStatus.PUBLIC))) {
      if (resource.getIdentifierStatus().equals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION)) {
        if (resource.isAlreadyAssignedDoi()) {
          // another new major version that replaces previous version
          replaceDoi(resource, version, versionReplaced);
          String msg = action.getText("manage.overview.publishing.doi.publish.newMajorVersion.replaces",
              new String[]{resource.getDoi().toString()});
          LOG.info(msg);
          action.addActionMessage(msg);
        } else {
          // initial major version
          registerDoi(resource, null);
          String msg = action.getText("manage.overview.publishing.doi.publish.newMajorVersion",
              new String[]{resource.getDoi().toString()});
          LOG.info(msg);
          action.addActionMessage(msg);
        }
      } else {
        // minor version increment
        updateDoi(resource);
        String msg = action.getText("manage.overview.publishing.doi.publish.newMinorVersion",
            new String[]{resource.getDoi().toString()});
        LOG.info(msg);
        action.addActionMessage(msg);
      }
    }
  }

  @Override
  public void updateDoi(Resource resource) {
    Objects.requireNonNull(resource);

    if (resource.getDoi() != null && resource.isPubliclyAvailable()) {
      DOI doi = resource.getDoi();
      try {
        DataCiteMetadata dataCiteMetadata = DataCiteMetadataBuilder.createDataCiteMetadata(doi, resource);
        registrationManager.getDoiService().update(doi, dataCiteMetadata);
      } catch (DoiException e) {
        String errorMsg = "Failed to update " + doi.toString() + " metadata: " + e.getMessage();
        LOG.error(errorMsg);
        throw new PublicationException(PublicationException.TYPE.DOI, errorMsg, e);
      }
    } else {
      throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_DOI_REGISTRATION, "Resource not in required state to update DOI!");
    }
  }

  @Override
  public void replaceDoi(Resource resource, BigDecimal version, BigDecimal replacedVersion) {
    Objects.requireNonNull(resource);

    DOI doiToRegister = resource.getDoi();
    DOI doiToReplace = resource.getAssignedDoi();

    if (doiToRegister != null && resource.isPubliclyAvailable() && doiToReplace != null
        && resource.getMetadataVersion() != null && resource.getMetadataVersion().compareTo(version) == 0
        && replacedVersion != null && resource.findVersionHistory(replacedVersion) != null) {

      // register new DOI first, indicating it replaces former DOI
      registerDoi(resource, doiToReplace);

      // update previously assigned DOI, indicating it has been replaced by new DOI
      try {
        // reconstruct last published version (version being replaced)
        File replacedVersionEmlFile = dataDir.resourceEmlFile(resource.getShortname(), replacedVersion);
        Resource lastPublishedVersion = ResourceUtils
            .reconstructVersion(replacedVersion, resource.getShortname(), resource.getCoreType(), resource.getDataPackageIdentifier(), doiToReplace, resource.getOrganisation(),
                resource.findVersionHistory(replacedVersion), replacedVersionEmlFile, resource.getKey());

        DataCiteMetadata assignedDoiMetadata =
            DataCiteMetadataBuilder.createDataCiteMetadata(doiToReplace, lastPublishedVersion);

        // add isPreviousVersionOf new resource version registered above
        DataCiteMetadataBuilder.addIsPreviousVersionOfDOIRelatedIdentifier(assignedDoiMetadata, doiToRegister);

        // update its URI first
        URI resourceVersionUri = cfg.getResourceVersionUri(resource.getShortname(), replacedVersion);
        registrationManager.getDoiService().update(doiToReplace, resourceVersionUri);
        // then update its metadata
        registrationManager.getDoiService().update(doiToReplace, assignedDoiMetadata);

      } catch (InvalidMetadataException e) {
        String errorMsg = "Failed to update " + doiToReplace + " metadata: " + e.getMessage();
        LOG.error(errorMsg);
        throw new PublicationException(PublicationException.TYPE.DOI, errorMsg, e);
      } catch (DoiException e) {
        String errorMsg = "Failed to update " + doiToReplace + ": " + e.getMessage();
        LOG.error(errorMsg);
        throw new PublicationException(PublicationException.TYPE.DOI, errorMsg, e);
      } catch (IllegalArgumentException e) {
        String errorMsg = "Failed to update " + doiToReplace + ": " + e.getMessage();
        LOG.error(errorMsg, e);
        throw new PublicationException(PublicationException.TYPE.DOI, errorMsg, e);
      }
    } else {
      throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_DOI_REGISTRATION, "Resource not in required state to replace DOI!");
    }
  }

  @Override
  public void registerDoi(Resource resource, @Nullable DOI replaced) {
    Objects.requireNonNull(resource);

    if (resource.getDoi() != null && resource.isPubliclyAvailable()) {
      DataCiteMetadata dataCiteMetadata = null;
      DOI doi = resource.getDoi();
      try {
        // DOI resolves to IPT public resource page
        URI uri = cfg.getResourceUri(resource.getShortname());
        dataCiteMetadata = DataCiteMetadataBuilder.createDataCiteMetadata(doi, resource);

        // if this resource (DOI) replaces a former resource version (DOI) add isNewVersionOf RelatedIdentifier
        if (replaced != null) {
          DataCiteMetadataBuilder.addIsNewVersionOfDOIRelatedIdentifier(dataCiteMetadata, replaced);
        }

        registrationManager.getDoiService().register(doi, uri, dataCiteMetadata);
        resource.setIdentifierStatus(IdentifierStatus.PUBLIC);
        resource.updateAlternateIdentifierForDOI();
        resource.updateCitationIdentifierForDOI(); // set DOI as citation identifier
      } catch (DoiExistsException e) {
        LOG.warn(
            "Received DoiExistsException registering resource meaning this is an existing DOI that should be updated instead",
            e);
        try {
          registrationManager.getDoiService().update(doi, dataCiteMetadata);
          resource.setIdentifierStatus(
              IdentifierStatus.PUBLIC); // must transition reused (registered DOI) from public_pending_publication to public
          resource.updateAlternateIdentifierForDOI();
          resource.updateCitationIdentifierForDOI(); // set DOI as citation identifier
        } catch (DoiException e2) {
          String errorMsg = "Failed to update existing DOI  " + doi.toString() + ": " + e2.getMessage();
          LOG.error(errorMsg, e2);
          throw new PublicationException(PublicationException.TYPE.DOI, errorMsg, e2);
        }
      } catch (InvalidMetadataException e) {
        String errorMsg =
            "Failed to register " + doi.toString() + " because DOI metadata was invalid: " + e.getMessage();
        LOG.error(errorMsg);
        throw new PublicationException(PublicationException.TYPE.DOI, errorMsg, e);
      } catch (DoiException e) {
        String errorMsg = "Failed to register " + doi.toString() + ": " + e.getMessage();
        LOG.error(errorMsg);
        throw new PublicationException(PublicationException.TYPE.DOI, errorMsg, e);
      }
    } else {
      throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_DOI_REGISTRATION,
          "Resource not in required state to register DOI!");
    }
  }
}
