package org.gbif.ipt.struts2;

import org.apache.struts2.dispatcher.Parameter;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.manage.ResourceManager;

import java.math.BigDecimal;
import java.util.Map;

import com.google.inject.Inject;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.apache.commons.lang3.StringUtils;

/**
 * An Interceptor that makes sure a requested resource is either public or the current user has rights to manage the
 * private resource. This prevents private resources, or deleted resource from being made available.
 */
public class PrivateDeletedResourceInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 2340800191217429210L;

  @Inject
  private ResourceManager resourceManager;

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    String requestedResource = RequireManagerInterceptor.getResourceParam(invocation);
    if (requestedResource != null) {
      // does resource exist at all?
      Resource resource = resourceManager.get(requestedResource);
      if (resource == null) {
        return BaseAction.NOT_FOUND;
      }

      // get current user
      Map<String, Object> session = invocation.getInvocationContext().getSession();
      User user = (User) session.get(Constants.SESSION_USER);

      // is the resource version requested private, or has it been deleted?
      String requestedResourceVersion = getResourceVersionParam(invocation);
      if (requestedResourceVersion != null) {
        try {
          BigDecimal version = new BigDecimal(requestedResourceVersion);
          VersionHistory history = resource.findVersionHistory(version);
          if (history != null) {
            if (history.getPublicationStatus() == PublicationStatus.PRIVATE) {
              // user authorised?
              if (user == null || !isAuthorized(user, resource)) {
                return BaseAction.NOT_ALLOWED;
              }
            } else if (history.getPublicationStatus() == PublicationStatus.DELETED) {
              // user authorised?
              if (user == null || !isAuthorized(user, resource)) {
                return BaseAction.NOT_AVAILABLE;
              }
            }
          }
        } catch (NumberFormatException e) {
          // return 404 if version was in incorrect format
          return BaseAction.NOT_FOUND;
        }
      }

      // is the resource currently private, or has it been deleted?
      if (PublicationStatus.PRIVATE == resource.getStatus()) {
        // user authorised?
        if (user == null || !isAuthorized(user, resource)) {
          return BaseAction.NOT_ALLOWED;
        }
      } else if (PublicationStatus.DELETED == resource.getStatus()) {
        // user authorised?
        if (user == null || !isAuthorized(user, resource)) {
          return BaseAction.NOT_AVAILABLE;
        }
      }
    }
    return invocation.invoke();
  }

  private boolean isAuthorized(User user, Resource resource) {
    if (user.hasAdminRights()) {
      return true;
    }
    if (resource != null && resource.getCreator().equals(user)) {
      return true;
    }
    if (user.hasManagerRights()) {
      for (User m : resource.getManagers()) {
        if (user.equals(m)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * @return the value for the version parameter (e.g. v=1.3, this method should return "1.3")
   */
  private static String getResourceVersionParam(ActionInvocation invocation) {
    String version = null;
    Parameter requestedResourceVersion = invocation.getInvocationContext().getParameters().get(Constants.REQ_PARAM_VERSION);
    if (requestedResourceVersion.isDefined()) {
      version = StringUtils.trimToNull(requestedResourceVersion.getValue());
    }

    return version;
  }
}
