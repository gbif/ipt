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
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.model.UserImportRow;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.PasswordUtils;
import org.gbif.ipt.validation.UserImportValidator;
import org.gbif.ipt.validation.UserImportValidator.UserImportException;

import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.action.UploadedFilesAware;
import org.apache.struts2.dispatcher.multipart.UploadedFile;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

import lombok.Getter;

/**
 * Creates user accounts in bulk from an uploaded CSV file.
 * The flow is: upload (parse and validate, nothing is created) - preview, where rows can be edited, added or removed
 * and checked again - confirm (rows are validated once more and accounts get created with generated passwords,
 * which are shown once).
 */
public class UserImportAction extends BaseAction implements UploadedFilesAware {

  @Serial
  private static final long serialVersionUID = -3270942357843105468L;

  private static final Logger LOG = LogManager.getLogger(UserImportAction.class);

  private final UserAccountManager userManager;
  private final UserImportValidator validator = new UserImportValidator();

  private List<UploadedFile> uploadedFiles = new ArrayList<>();

  // rows as edited in the preview, parallel arrays
  private String[] lines;
  private String[] firstnames;
  private String[] lastnames;
  private String[] emails;
  private String[] roles;

  @Getter
  private List<UserImportRow> rows = new ArrayList<>();
  @Getter
  private boolean preview;
  @Getter
  private boolean completed;

  @Inject
  public UserImportAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      UserAccountManager userManager) {
    super(textProvider, cfg, registrationManager);
    this.userManager = userManager;
  }

  /**
   * Parses and validates the uploaded file and shows a preview. No account is created at this stage.
   */
  public String upload() {
    if (!isHttpPost()) {
      return SUCCESS;
    }

    UploadedFile file = uploadedFiles.isEmpty() ? null : uploadedFiles.get(0);
    if (file == null || !(file.getContent() instanceof File content)) {
      addActionError(getText("admin.users.import.error.noFile"));
      return SUCCESS;
    }

    try {
      rows = validator.parse(content, userManager);
    } catch (UserImportException e) {
      addActionError(getText(e.getKey(), e.getArgs()));
      return SUCCESS;
    }

    preview = true;
    return SUCCESS;
  }

  /**
   * Validates the rows edited in the preview and shows the preview again.
   */
  public String revalidate() {
    if (!isHttpPost()) {
      return SUCCESS;
    }

    preview = buildRows();
    return SUCCESS;
  }

  /**
   * Validates the rows edited in the preview once more and creates the accounts for all valid rows.
   */
  public String confirm() {
    if (!isHttpPost() || !buildRows()) {
      return SUCCESS;
    }

    User currentUser = getCurrentUser();
    int created = 0;
    for (UserImportRow row : rows) {
      if (!row.isValid()) {
        continue;
      }

      String password = PasswordUtils.generatePassword();
      User user = new User();
      user.setFirstname(row.getFirstname());
      user.setLastname(row.getLastname());
      user.setEmail(row.getEmail());
      user.setRole(row.getRole());
      user.setPassword(password);

      try {
        userManager.create(user);
        row.setPassword(password);
        row.setCreated(true);
        created++;
        LOG.info("User {} has been created by {} (bulk import)", row.getEmail(), currentUser.getEmail());
      } catch (AlreadyExistingException e) {
        // created in the meantime
        row.addError("admin.users.import.error.exists");
      } catch (IOException e) {
        LOG.error("Failed to save users during bulk import: {}", e.getMessage(), e);
        addActionError(getText("admin.user.cantSave", new String[] {e.getMessage()}));
        break;
      }
    }

    completed = true;
    addActionMessage(getText("admin.users.import.created", new String[] {String.valueOf(created)}));
    return SUCCESS;
  }

  /**
   * Builds the rows from the submitted values and validates them.
   *
   * @return false if there are no rows to work with (an action error has been added)
   */
  private boolean buildRows() {
    try {
      rows = validator.build(lines, firstnames, lastnames, emails, roles, userManager);
      return true;
    } catch (UserImportException e) {
      addActionError(getText(e.getKey(), e.getArgs()));
      return false;
    }
  }

  public int getValidCount() {
    return (int) rows.stream().filter(UserImportRow::isValid).count();
  }

  public int getInvalidCount() {
    return rows.size() - getValidCount();
  }

  public int getMaxRows() {
    return UserImportValidator.MAX_ROWS;
  }

  public Role[] getRoleOptions() {
    return Role.values();
  }

  @StrutsParameter
  public void setLines(String[] lines) {
    this.lines = lines;
  }

  @StrutsParameter
  public void setFirstnames(String[] firstnames) {
    this.firstnames = firstnames;
  }

  @StrutsParameter
  public void setLastnames(String[] lastnames) {
    this.lastnames = lastnames;
  }

  @StrutsParameter
  public void setEmails(String[] emails) {
    this.emails = emails;
  }

  @StrutsParameter
  public void setRoles(String[] roles) {
    this.roles = roles;
  }

  @Override
  public void withUploadedFiles(List<UploadedFile> uploadedFiles) {
    this.uploadedFiles = uploadedFiles;
  }
}
