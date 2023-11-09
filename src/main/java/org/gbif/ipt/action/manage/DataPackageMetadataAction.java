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
import org.gbif.ipt.model.datapackage.metadata.camtrap.Temporal;
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
import static org.gbif.ipt.service.manage.impl.ResourceManagerImpl.CAMTRAP_TEMPORAL_METADATA_DATE_FORMAT;

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

  public static final Map<String, String> GBIF_SUPPORTED_LICENSES_VOCABULARY = new LinkedHashMap<>();
  public static final Map<String, String> OPEN_DEFINIITION_LICENSES_VOCABULARY = new LinkedHashMap<>();

  static {
    GBIF_SUPPORTED_LICENSES_VOCABULARY.put("CC0-1.0", "CC0-1.0");
    GBIF_SUPPORTED_LICENSES_VOCABULARY.put("CC-BY-4.0", "CC-BY-4.0");
    GBIF_SUPPORTED_LICENSES_VOCABULARY.put("CC-BY-NC-4.0", "CC-BY-NC-4.0");

    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("AAL", "AAL");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("AFL-3.0", "AFL-3.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("AGPL-3.0", "AGPL-3.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("APL-1.0", "APL-1.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("APSL-2.0", "APSL-2.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("Against-DRM", "Against-DRM");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("Apache-1.1", "Apache-1.1");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("Apache-2.0", "Apache-2.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("Artistic-2.0", "Artistic-2.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("BSD-2-Clause", "BSD-2-Clause");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("BSD-3-Clause", "BSD-3-Clause");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("BSL-1.0", "BSL-1.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("BitTorrent-1.1", "BitTorrent-1.1");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("CATOSL-1.1", "CATOSL-1.1");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("CC-BY-4.0", "CC-BY-4.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("CC-BY-NC-4.0", "CC-BY-NC-4.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("CC-BY-NC-ND-4.0", "CC-BY-NC-ND-4.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("CC-BY-NC-SA-4.0", "CC-BY-NC-SA-4.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("CC-BY-ND-4.0", "CC-BY-ND-4.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("CC-BY-SA-4.0", "CC-BY-SA-4.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("CC0-1.0", "CC0-1.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("CDDL-1.0", "CDDL-1.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("CECILL-2.1", "CECILL-2.1");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("CNRI-Python", "CNRI-Python");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("CPAL-1.0", "CPAL-1.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("CUA-OPL-1.0", "CUA-OPL-1.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("DSL", "DSL");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("ECL-2.0", "ECL-2.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("EFL-2.0", "EFL-2.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("EPL-1.0", "EPL-1.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("EPL-2.0", "EPL-2.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("EUDatagrid", "EUDatagrid");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("EUPL-1.1", "EUPL-1.1");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("Entessa", "Entessa");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("FAL-1.3", "FAL-1.3");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("Fair", "Fair");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("Frameworx-1.0", "Frameworx-1.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("GFDL-1.3-no-cover-texts-no-invariant-sections", "GFDL-1.3-no-cover-texts-no-invariant-sections");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("GPL-2.0", "GPL-2.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("GPL-3.0", "GPL-3.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("HPND", "HPND");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("IPA", "IPA");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("IPL-1.0", "IPL-1.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("ISC", "ISC");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("Intel", "Intel");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("LGPL-2.1", "LGPL-2.1");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("LGPL-3.0", "LGPL-3.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("LO-FR-2.0", "LO-FR-2.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("LPL-1.0", "LPL-1.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("LPL-1.02", "LPL-1.02");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("LPPL-1.3c", "LPPL-1.3c");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("MIT", "MIT");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("MPL-1.0", "MPL-1.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("MPL-1.1", "MPL-1.1");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("MPL-2.0", "MPL-2.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("MS-PL", "MS-PL");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("MS-RL", "MS-RL");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("MirOS", "MirOS");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("Motosoto", "Motosoto");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("Multics", "Multics");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("NASA-1.3", "NASA-1.3");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("NCSA", "NCSA");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("NGPL", "NGPL");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("NPOSL-3.0", "NPOSL-3.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("NTP", "NTP");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("Naumen", "Naumen");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("Nokia", "Nokia");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("OCLC-2.0", "OCLC-2.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("ODC-BY-1.0", "ODC-BY-1.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("ODbL-1.0", "ODbL-1.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("OFL-1.1", "OFL-1.1");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("OGL-Canada-2.0", "OGL-Canada-2.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("OGL-UK-1.0", "OGL-UK-1.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("OGL-UK-2.0", "OGL-UK-2.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("OGL-UK-3.0", "OGL-UK-3.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("OGTSL", "OGTSL");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("OSL-3.0", "OSL-3.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("PDDL-1.0", "PDDL-1.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("PHP-3.0", "PHP-3.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("PostgreSQL", "PostgreSQL");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("Python-2.0", "Python-2.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("QPL-1.0", "QPL-1.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("RPL-1.5", "RPL-1.5");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("RPSL-1.0", "RPSL-1.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("RSCPL", "RSCPL");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("SISSL", "SISSL");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("SPL-1.0", "SPL-1.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("SimPL-2.0", "SimPL-2.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("Sleepycat", "Sleepycat");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("Talis", "Talis");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("Unlicense", "Unlicense");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("VSL-1.0", "VSL-1.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("W3C", "W3C");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("WXwindows", "WXwindows");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("Watcom-1.0", "Watcom-1.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("Xnet", "Xnet");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("ZPL-2.0", "ZPL-2.0");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("Zlib", "Zlib");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("dli-model-use", "dli-model-use");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("geogratis", "geogratis");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("hesa-withrights", "hesa-withrights");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("localauth-withrights", "localauth-withrights");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("met-office-cp", "met-office-cp");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("mitre", "mitre");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("notspecified", "notspecified");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("other-at", "other-at");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("other-closed", "other-closed");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("other-nc", "other-nc");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("other-open", "other-open");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("other-pd", "other-pd");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("ukclickusepsi", "ukclickusepsi");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("ukcrown", "ukcrown");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("ukcrown-withrights", "ukcrown-withrights");
    OPEN_DEFINIITION_LICENSES_VOCABULARY.put("ukpsi", "ukpsi");
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
    preProcessCamtrapMetadata();

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

  private void preProcessCamtrapMetadata() {
    if (resource.getDataPackageMetadata() instanceof CamtrapMetadata) {
      if (section == CamtrapMetadataSection.GEOGRAPHIC_SECTION) {
        convertCamtrapGeographicMetadata();
      } else if (section == CamtrapMetadataSection.TAXONOMIC_SECTION) {
        convertCamtrapTaxonomicMetadata();
      } else if (section == CamtrapMetadataSection.TEMPORAL_SECTION) {
        convertCamtrapTemporalMetadata();
      }
    }
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

        if (inferredCamtrapMetadata.getInferredGeographicScope() != null) {
          if (inferredCamtrapMetadata.getInferredGeographicScope().isInferred()
              && inferredCamtrapMetadata.getInferredGeographicScope().getErrors().isEmpty()) {
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
  }

  private void convertCamtrapTaxonomicMetadata() {
    CamtrapMetadata camtrapMetadata = (CamtrapMetadata) resource.getDataPackageMetadata();

    if (resource.isInferTaxonomicCoverageAutomatically()) {
      if (inferredMetadata instanceof InferredCamtrapMetadata) {
        InferredCamtrapMetadata inferredCamtrapMetadata = (InferredCamtrapMetadata) inferredMetadata;

        if (inferredCamtrapMetadata.getInferredTaxonomicScope() != null
            && inferredCamtrapMetadata.getInferredTaxonomicScope().isInferred()
            && inferredCamtrapMetadata.getInferredTaxonomicScope().getErrors().isEmpty()) {
          InferredCamtrapTaxonomicScope inferredTaxonomicScope = inferredCamtrapMetadata.getInferredTaxonomicScope();
          camtrapMetadata.setTaxonomic(inferredTaxonomicScope.getData());
        }
      }
    }
  }

  private void convertCamtrapTemporalMetadata() {
    CamtrapMetadata camtrapMetadata = (CamtrapMetadata) resource.getDataPackageMetadata();

    if (resource.isInferTemporalCoverageAutomatically()) {
      if (inferredMetadata instanceof InferredCamtrapMetadata) {
        InferredCamtrapMetadata inferredCamtrapMetadata = (InferredCamtrapMetadata) inferredMetadata;

        if (inferredCamtrapMetadata.getInferredTemporalScope() != null
            && inferredCamtrapMetadata.getInferredTemporalScope().isInferred()
            && inferredCamtrapMetadata.getInferredTemporalScope().getErrors().isEmpty()) {
          InferredCamtrapTemporalScope inferredTemporalScope = inferredCamtrapMetadata.getInferredTemporalScope();

          Temporal temporal = new Temporal();
          temporal.setStart(CAMTRAP_TEMPORAL_METADATA_DATE_FORMAT.format(inferredTemporalScope.getStartDate()));
          temporal.setEnd(CAMTRAP_TEMPORAL_METADATA_DATE_FORMAT.format(inferredTemporalScope.getEndDate()));

          camtrapMetadata.setTemporal(temporal);
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

  public Map<String, String> getGbifSupportedLicenseNames() {
    return GBIF_SUPPORTED_LICENSES_VOCABULARY;
  }

  public Map<String, String> getOpenDefinitionLicenseNames() {
    return OPEN_DEFINIITION_LICENSES_VOCABULARY;
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
