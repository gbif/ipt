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
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.SourceFileProblem;
import org.gbif.ipt.model.SourceLog;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.EncodingDamage;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.inject.Inject;

import lombok.Getter;

/**
 * Shows what was found when a source was last analysed: how it was read, and the problems found with examples.
 * Sources last analysed before the log was kept as JSON show their text log instead.
 */
public class SourceLogAction extends ManagerBaseAction {

  @Serial
  private static final long serialVersionUID = 6730198457201395482L;

  private static final Logger LOG = LogManager.getLogger(SourceLogAction.class);

  private final SourceManager sourceManager;
  private final DataDir dataDir;

  @Getter
  private Source source;
  @Getter
  private SourceLog log;
  // the text log of a source not analysed since the log was kept as JSON
  @Getter
  private String textLog;

  @Inject
  public SourceLogAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
                         ResourceManager resourceManager, SourceManager sourceManager, DataDir dataDir) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.sourceManager = sourceManager;
    this.dataDir = dataDir;
  }

  @Override
  public void prepare() {
    super.prepare();
    String name = StringUtils.trimToNull(req.getParameter(Constants.REQ_PARAM_SOURCE));
    if (resource != null && name != null) {
      source = resource.getSource(name);
    }
    if (source == null) {
      notFound = true;
    }
  }

  @Override
  public String execute() {
    if (source == null) {
      return NOT_FOUND;
    }
    log = sourceManager.readLog(source);
    if (log == null) {
      File textFile = dataDir.sourceLogFile(resource.getShortname(), source.getName());
      if (textFile.exists()) {
        try {
          textLog = new String(Files.readAllBytes(textFile.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
          LOG.warn("Can't read the log of source {}", source.getName(), e);
        }
      }
    }
    return SUCCESS;
  }

  /**
   * @return the problem's summary, as on the source page
   */
  public String problemMessage(SourceLog.Problem problem) {
    return getText(SourceFileProblem.messageKey(problem.getType(), source), new String[] {
      String.valueOf(problem.getLines()), String.valueOf(problem.getFirstLine()), StringUtils.defaultString(
        log.getEncoding())});
  }

  /**
   * @return the value escaped for HTML, with the characters that look damaged marked if the problem is with the
   *         encoding. Other problems' values aren't marked, as an Excel value like Ã© isn't damage
   */
  public String markValue(SourceLog.Problem problem, String value) {
    if (problem.getType() == SourceFileProblem.Type.INVALID_CHARACTERS
        || problem.getType() == SourceFileProblem.Type.DAMAGED_VALUES) {
      return EncodingDamage.markHtml(value);
    }
    return StringEscapeUtils.escapeHtml4(StringUtils.defaultString(value));
  }
}
