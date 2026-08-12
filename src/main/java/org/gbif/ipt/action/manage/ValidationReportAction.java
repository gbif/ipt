/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.action.manage;

import org.gbif.dp.analysis.api.AnalysisExecution;
import org.gbif.dp.analysis.api.DatapackageAnalysisResult;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.File;
import java.io.IOException;
import java.io.Serial;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.inject.Inject;

import lombok.Getter;

/**
 * Displays the result of a Darwin Core Data Package (DwC-DP) structural validation
 * (primary/foreign key integrity, data type checks, per-column completeness) for the
 * current resource.
 */
public class ValidationReportAction extends ManagerBaseAction {

  @Serial
  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LogManager.getLogger(ValidationReportAction.class);

  // Adjust to wherever your validation step actually writes the report.
  private static final String REPORT_FILENAME = "datapackage-validation-report.json";

  private static final ObjectMapper MAPPER = JsonMapper.builder()
      .addModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .build();

  private final DataDir dataDir;

  @Getter
  private String shortname;

  @Getter
  private AnalysisExecution<DatapackageAnalysisResult> validationReport = null;

  @Inject
  public ValidationReportAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ResourceManager resourceManager,
      DataDir dataDir) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.dataDir = dataDir;
  }

  @Override
  public String execute() {
    shortname = getResource().getShortname();
    File reportFile = dataDir.resourceDataPackageValidationReportFile(shortname);

    if (reportFile == null || !reportFile.exists()) {
      addActionError(getText("manage.validation.notFound", new String[] {REPORT_FILENAME}));
      LOG.warn("No validation report found for resource {} at {}", shortname,
          reportFile == null ? "(null path)" : reportFile.getAbsolutePath());
      return SUCCESS;
    }

    try {
      validationReport = MAPPER.readValue(
          reportFile,
          new TypeReference<>() {
          }
      );
    } catch (IOException e) {
      LOG.error("Failed to read validation report for resource {}: {}", shortname,
          e.getMessage(), e);
      addActionError(getText("manage.validation.readError"));
    }

    return SUCCESS;
  }
}
