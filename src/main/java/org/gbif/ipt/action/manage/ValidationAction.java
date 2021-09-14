package org.gbif.ipt.action.manage;

import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.validator.api.Validation;
import org.gbif.validator.api.ValidationSearchRequest;
import org.gbif.validator.ws.client.ValidationWsClient;

import java.util.Collections;

import com.google.inject.Inject;

public class ValidationAction extends ManagerBaseAction {

  private final ValidationWsClient validationWsClient;

  private PagingResponse<Validation> validations;

  @Inject
  public ValidationAction(
    SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    validationWsClient = ValidationWsClient.getInstance(cfg.getGbifApiUrl(),
                                                        registrationManager.getIpt().getKey().toString(),
                                                        registrationManager.getIpt().getWsPassword());
  }

  public PagingResponse<Validation> getValidations() {
    if (resource != null) {
      validations = validationWsClient.list(ValidationSearchRequest.builder()
                                        .installationKey(registrationManager.getIpt().getKey())
                                        .sortByCreated(ValidationSearchRequest.SortOrder.DESC)
                                        .build());
    }  else {
      validations = new PagingResponse<Validation>(0, 0, 0L, Collections.EMPTY_LIST);
    }
    return validations;
  }
}
