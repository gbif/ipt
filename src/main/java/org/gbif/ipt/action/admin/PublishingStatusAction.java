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
import org.gbif.ipt.model.BulkPublication;
import org.gbif.ipt.service.admin.BulkPublicationManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.inject.Inject;

import java.io.Serial;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.struts2.interceptor.parameter.StrutsParameter;

import lombok.Getter;

/**
 * The progress of the running or last bulk publication, as a page fragment the bulk publishing page polls.
 */
public class PublishingStatusAction extends BaseAction {

  @Serial
  private static final long serialVersionUID = -3984381818825518246L;

  private final BulkPublicationManager bulkPublicationManager;

  @Getter
  private BulkPublication run;
  // the resources whose log is open on the page
  @Getter
  private Set<String> openLogs = Collections.emptySet();
  @Getter
  private BulkRunPage runPage;
  private String filter;
  private int page = 1;
  private boolean all;

  @Inject
  public PublishingStatusAction(SimpleTextProvider textProvider,
                                AppConfig cfg,
                                RegistrationManager registrationManager,
                                BulkPublicationManager bulkPublicationManager) {
    super(textProvider, cfg, registrationManager);
    this.bulkPublicationManager = bulkPublicationManager;
  }

  @Override
  public String execute() {
    run = bulkPublicationManager.getCurrent();
    runPage = BulkRunPage.of(run, filter, page, all);
    return SUCCESS;
  }

  public boolean isRunning() {
    return run != null && !runPage.isFinished();
  }

  @StrutsParameter
  public void setFilter(String filter) {
    this.filter = filter;
  }

  @StrutsParameter
  public void setPage(int page) {
    this.page = page;
  }

  @StrutsParameter
  public void setAll(boolean all) {
    this.all = all;
  }

  @StrutsParameter
  public void setOpen(String open) {
    openLogs = open == null ? Collections.emptySet()
        : Arrays.stream(open.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
  }
}
