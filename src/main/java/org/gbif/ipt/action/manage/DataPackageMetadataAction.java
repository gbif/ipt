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
package org.gbif.ipt.action.manage;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.InferredCamtrapGeographicScope;
import org.gbif.ipt.model.InferredCamtrapMetadata;
import org.gbif.ipt.model.InferredCamtrapTaxonomicScope;
import org.gbif.ipt.model.InferredCamtrapTemporalScope;
import org.gbif.ipt.model.InferredMetadata;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapContributor;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapLicense;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CaptureMethod;
import org.gbif.ipt.model.datapackage.metadata.camtrap.Geojson;
import org.gbif.ipt.model.datapackage.metadata.camtrap.ObservationLevel;
import org.gbif.ipt.model.datapackage.metadata.camtrap.Project;
import org.gbif.ipt.model.datapackage.metadata.camtrap.RelatedIdentifier;
import org.gbif.ipt.model.datapackage.metadata.camtrap.Taxonomic;
import org.gbif.ipt.model.voc.CamtrapMetadataSection;
import org.gbif.ipt.model.voc.DataPackageMetadataSection;
import org.gbif.ipt.model.voc.FrictionlessMetadataSection;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourceMetadataInferringService;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.validation.DataPackageMetadataValidator;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import static org.gbif.ipt.config.Constants.CAMTRAP_DP;

public class DataPackageMetadataAction extends ManagerBaseAction {

  private static final Logger LOG = LogManager.getLogger(DataPackageMetadataAction.class);

  private static final long serialVersionUID = -1669636958170716515L;

  private final ResourceMetadataInferringService metadataInferringService;
  private final DataPackageMetadataValidator metadataValidator;
  private final ObjectMapper objectMapper;
  private DataPackageMetadataSection section = FrictionlessMetadataSection.BASIC_SECTION;
  private DataPackageMetadataSection next = FrictionlessMetadataSection.BASIC_SECTION;
  private Map<String, String> organisations = new LinkedHashMap<>();
  private InferredMetadata inferredMetadata;
  private String customGeoJson;

  public final static Map<String, String> CAMTRAP_SUPPORTED_LICENSES_VOCABULARY = new LinkedHashMap<>();

  static {
    CAMTRAP_SUPPORTED_LICENSES_VOCABULARY.put("CC0-1.0", "CC0-1.0");
    CAMTRAP_SUPPORTED_LICENSES_VOCABULARY.put("CC-BY-4.0", "CC-BY-4.0");
    CAMTRAP_SUPPORTED_LICENSES_VOCABULARY.put("CC-BY-NC-4.0", "CC-BY-NC-4.0");
  }

  @Inject
  public DataPackageMetadataAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
                                   ResourceManager resourceManager, DataPackageMetadataValidator metadataValidator,
                                   ResourceMetadataInferringService metadataInferringService) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.metadataValidator = metadataValidator;
    this.objectMapper = new ObjectMapper();
    this.metadataInferringService = metadataInferringService;
  }

  @Override
  public Resource getResource() {
    return resource;
  }

  @Override
  public void prepare() {
    super.prepare();
    if (session.get(Constants.SESSION_USER) == null) {
      return;
    }

    if (CAMTRAP_DP.equals(resource.getCoreType())) {
      prepareCamtrap();
    }
  }

  private void prepareCamtrap() {
    // take the section parameter from the requested url
    section = CamtrapMetadataSection.fromName(StringUtils.substringBetween(req.getRequestURI(), "camtrap-metadata-", "."));
    CamtrapMetadataSection camtrapMetadataSection = (CamtrapMetadataSection) section;
    CamtrapMetadata metadata = (CamtrapMetadata) resource.getDataPackageMetadata();

    if (camtrapMetadataSection == null) {
      return;
    }

    boolean reinferMetadata = Boolean.parseBoolean(StringUtils.trimToNull(req.getParameter(Constants.REQ_PARAM_REINFER_METADATA)));

    // infer metadata if absent or re-infer if requested
    if (reinferMetadata || resource.getInferredMetadata() == null) {
      InferredMetadata inferredMetadataRaw = metadataInferringService.inferMetadata(resource);

      if (inferredMetadataRaw instanceof InferredCamtrapMetadata) {
        inferredMetadata = inferredMetadataRaw;
      } else {
        LOG.error("Wrong type of the inferred metadata class, expected {} got {}",
                InferredCamtrapMetadata.class.getSimpleName(), inferredMetadataRaw.getClass().getSimpleName());
        inferredMetadata = new InferredCamtrapMetadata();
      }
      resource.setInferredMetadata(inferredMetadata);
      resourceManager.saveInferredMetadata(resource);
    } else {
      if (resource.getInferredMetadata() instanceof InferredCamtrapMetadata) {
        inferredMetadata = resource.getInferredMetadata();
      } else {
        LOG.error("Wrong type of the stored inferred metadata class, expected {} got {}",
                InferredCamtrapMetadata.class.getSimpleName(), resource.getInferredMetadata().getClass().getSimpleName());
        inferredMetadata = new InferredCamtrapMetadata();
      }
    }

    switch (camtrapMetadataSection) {
      case BASIC_SECTION:
        if (isHttpPost()) {
          metadata.getContributors().clear();
          metadata.getLicenses().clear();
          metadata.getSources().clear();

          // publishing organisation, if provided must match organisation
          String id = getId();
          Organisation organisation = (id == null) ? null : registrationManager.get(id);
          if (organisation != null) {
            // set organisation: note organisation is locked after 1) DOI assigned, or 2) after registration with GBIF
            if (!resource.isAlreadyAssignedDoi() && !resource.isRegistered()) {
              resource.setOrganisation(organisation);
            }
          }
        }
        break;

      case GEOGRAPHIC_SECTION:
        InferredCamtrapGeographicScope inferredGeographicScope = ((InferredCamtrapMetadata) inferredMetadata).getInferredGeographicScope();

        if (reinferMetadata && inferredGeographicScope != null && !inferredGeographicScope.getErrors().isEmpty()) {
          for (String errorMessage : inferredGeographicScope.getErrors()) {
            addActionError(getText(errorMessage));
          }
        }
        break;

      case TAXONOMIC_SECTION:
        if (isHttpPost()) {
          metadata.getTaxonomic().clear();
        }

        InferredCamtrapTaxonomicScope inferredTaxonomicScope = ((InferredCamtrapMetadata) inferredMetadata).getInferredTaxonomicScope();

        if (reinferMetadata && inferredTaxonomicScope != null && !inferredTaxonomicScope.getErrors().isEmpty()) {
          for (String errorMessage : inferredTaxonomicScope.getErrors()) {
            addActionError(getText(errorMessage));
          }
        }
        break;

      case TEMPORAL_SECTION:
        InferredCamtrapTemporalScope inferredTemporalScope = ((InferredCamtrapMetadata) inferredMetadata).getInferredTemporalScope();

        if (reinferMetadata && inferredTemporalScope != null && !inferredTemporalScope.getErrors().isEmpty()) {
          for (String errorMessage : inferredTemporalScope.getErrors()) {
            addActionError(getText(errorMessage));
          }
        }
        break;

      case KEYWORDS_SECTION:
        if (isHttpPost()) {
          metadata.getKeywords().clear();
        }
        break;

      case PROJECT_SECTION:
        if (isHttpPost()) {
          if (metadata.getProject() != null) {
            metadata.getProject().getCaptureMethod().clear();
          }
        }
        break;

      case OTHER_SECTION:
        if (isHttpPost()) {
          metadata.getReferences().clear();
          metadata.getRelatedIdentifiers().clear();
        }
        break;

      default:
        break;
    }
  }

  @Override
  public String save() throws Exception {
    // pre-process (convert) Camtrap metadata
    if (resource.getDataPackageMetadata() instanceof CamtrapMetadata) {
      if (section == CamtrapMetadataSection.GEOGRAPHIC_SECTION) {
        convertCamtrapGeographicMetadata();
      }
    }

    // before saving, the minimum amount of mandatory metadata must have been provided, and ALL metadata sections must
    // be valid, otherwise an error is displayed
    if (metadataValidator.isSectionValid(this, resource, section)) {
      // Save metadata information (datapackage.json)
      resourceManager.saveDatapackageMetadata(resource);
      // save date metadata was last modified
      resource.setMetadataModified(new Date());
      // Alert user of successful save
      addActionMessage(getText("manage.success", new String[]{getText("submenu.datapackagemetadata." + section.getName())}));
      // Save resource information (resource.xml)
      resourceManager.save(resource);

      if (section instanceof CamtrapMetadataSection) {
        nextSectionCamtrap();
      }

    } else {
      // stay on the same section, since save failed
      next = section;
    }

    return SUCCESS;
  }

  /**
   * 1. When custom GeoJSON is selected: serialize JSON string and set it to the geographic scope metadata.
   * 2. When inferred metadata is selected: set inferred metadata to the geographic scope metadata.
   */
  private void convertCamtrapGeographicMetadata() {
    CamtrapMetadata camtrapMetadata = (CamtrapMetadata) resource.getDataPackageMetadata();

    if (!resource.isInferGeocoverageAutomatically()) {
      if (StringUtils.isNotEmpty(customGeoJson)) {
        try {
          Geojson geojson = objectMapper.readValue(customGeoJson, Geojson.class);
          camtrapMetadata.setSpatial(geojson);
          camtrapMetadata.setCoordinatePrecision(null);
        } catch (JsonProcessingException e) {
          addActionError(getText("datapackagemetadata.geographic.error.customGeojson"));
          LOG.error("Error processing custom GeoJSON object", e);
        }
      }
    } else {
      if (inferredMetadata instanceof InferredCamtrapMetadata) {
        InferredCamtrapMetadata inferredCamtrapMetadata = (InferredCamtrapMetadata) inferredMetadata;
        if (inferredCamtrapMetadata.getInferredGeographicScope().isInferred() &&
            inferredCamtrapMetadata.getInferredGeographicScope().getErrors().isEmpty()) {
          Geojson geojson = new Geojson();
          geojson.setType(Geojson.Type.POLYGON);
          List<Double> coordinates = new ArrayList<>();
          InferredCamtrapGeographicScope inferredGeographicScope = inferredCamtrapMetadata.getInferredGeographicScope();
          coordinates.add(inferredGeographicScope.getMinLongitude());
          coordinates.add(inferredGeographicScope.getMinLatitude());
          coordinates.add(inferredGeographicScope.getMaxLongitude());
          coordinates.add(inferredGeographicScope.getMaxLatitude());
          geojson.setCoordinates(coordinates);

          camtrapMetadata.setSpatial(geojson);
          camtrapMetadata.setCoordinatePrecision(null);
        } else {
          for (String error : inferredCamtrapMetadata.getInferredGeographicScope().getErrors()) {
            addActionError(getText(error));
          }
        }
      }
    }
  }

  private void nextSectionCamtrap() {
    CamtrapMetadataSection camtrapMetadataSection = (CamtrapMetadataSection) section;
    // progress to next section, since save succeeded
    switch (camtrapMetadataSection) {
      case BASIC_SECTION:
        next = CamtrapMetadataSection.GEOGRAPHIC_SECTION;
        break;
      case GEOGRAPHIC_SECTION:
        next = CamtrapMetadataSection.TAXONOMIC_SECTION;
        break;
      case TAXONOMIC_SECTION:
        next = CamtrapMetadataSection.TEMPORAL_SECTION;
        break;
      case TEMPORAL_SECTION:
        next = CamtrapMetadataSection.KEYWORDS_SECTION;
        break;
      case KEYWORDS_SECTION:
        next = CamtrapMetadataSection.PROJECT_SECTION;
        break;
      case PROJECT_SECTION:
        next = CamtrapMetadataSection.OTHER_SECTION;
        break;
      case OTHER_SECTION:
        next = CamtrapMetadataSection.BASIC_SECTION;
        break;
      default:
        break;
    }
  }

  public String getNext() {
    return next.getName();
  }

  public String getSection() {
    return section.getName();
  }

  public DataPackageMetadata getMetadata() {
    return resource.getDataPackageMetadata();
  }

  /**
   * @return map of organisations associated to IPT that can publish resources
   */
  public Map<String, String> getOrganisations() {
    return organisations;
  }

  public Map<String, String> getLicenseScopes() {
    return CamtrapLicense.Scope.VOCABULARY;
  }

  public Map<String, String> getLicenseNames() {
    return CAMTRAP_SUPPORTED_LICENSES_VOCABULARY;
  }

  public Map<String, String> getContributorRoles() {
    return CamtrapContributor.Role.VOCABULARY;
  }

  public Map<String, String> getTaxonRanks() {
    return Taxonomic.TaxonRank.VOCABULARY;
  }

  public Map<String, String> getSamplingDesigns() {
    return Project.SamplingDesign.VOCABULARY;
  }

  public Map<String, String> getCaptureMethods() {
    return CaptureMethod.VOCABULARY;
  }

  public Map<String, String> getObservationLevels() {
    return ObservationLevel.VOCABULARY;
  }

  public Map<String, String> getRelationTypes() {
    return RelatedIdentifier.RelationType.VOCABULARY;
  }

  public Map<String, String> getResourceTypeGenerals() {
    return RelatedIdentifier.ResourceTypeGeneral.VOCABULARY;
  }

  public Map<String, String> getRelatedIdentifierTypes() {
    return RelatedIdentifier.RelatedIdentifierType.VOCABULARY;
  }

  public InferredMetadata getInferredMetadata() {
    return inferredMetadata;
  }

  public String getCustomGeoJson() {
    return customGeoJson;
  }

  public void setCustomGeoJson(String customGeoJson) {
    this.customGeoJson = customGeoJson;
  }
}
