package org.gbif.ipt.struts2;

/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

import org.gbif.ipt.action.BaseAction;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;

import org.apache.struts2.dispatcher.ServletActionRedirectResult;
import org.apache.struts2.dispatcher.ServletRedirectResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * An Interceptor to preserve an actions ValidationAware messages across a redirect result. It makes the assumption that
 * you always want to preserve messages across a redirect and restore them to the next action if they exist. The way
 * this works is it looks at the result type after a action has executed and if the result was a redirect
 * (ServletRedirectResult) or a redirectAction (ServletActionRedirectResult) and there were any errors, messages, or
 * fieldErrors they are stored in the session. Before the next action executes it will check if there are any messages
 * stored in the session and add them to the next action.
 * 
 * @See http://glindholm.wordpress.com/2008/07/02/preserving-messages-across-a-redirect-in-struts-2/
 */
public class RedirectMessageInterceptor extends MethodFilterInterceptor {
  private static final long serialVersionUID = -1847557437429753540L;

  public static final String FIELD_ERRORS_KEY = "RedirectMessageInterceptor_FieldErrors";
  public static final String ACTION_ERRORS_KEY = "RedirectMessageInterceptor_ActionErrors";
  public static final String ACTION_MESSAGES_KEY = "RedirectMessageInterceptor_ActionMessages";
  public static final String ACTION_WARNINGS_KEY = "RedirectMessageInterceptor_ActionWarnings";

  public RedirectMessageInterceptor() {
  }

  /**
   * If the result is a redirect then store error and messages in the session.
   */
  protected void after(ActionInvocation invocation, BaseAction action) throws Exception {
    Result result = invocation.getResult();

    if (result != null && (result instanceof ServletRedirectResult || result instanceof ServletActionRedirectResult)) {
      Map<String, Object> session = invocation.getInvocationContext().getSession();

      Collection<String> actionWarnings = action.getWarnings();
      if (actionWarnings != null && actionWarnings.size() > 0) {
        session.put(ACTION_WARNINGS_KEY, actionWarnings);
      }

      Collection<String> actionErrors = action.getActionErrors();
      if (actionErrors != null && actionErrors.size() > 0) {
        session.put(ACTION_ERRORS_KEY, actionErrors);
      }

      Collection<String> actionMessages = action.getActionMessages();
      if (actionMessages != null && actionMessages.size() > 0) {
        session.put(ACTION_MESSAGES_KEY, actionMessages);
      }

      Map<String, List<String>> fieldErrors = action.getFieldErrors();
      if (fieldErrors != null && fieldErrors.size() > 0) {
        session.put(FIELD_ERRORS_KEY, fieldErrors);
      }
    }
  }

  /**
   * Retrieve the errors and messages from the session and add them to the action.
   */
  protected void before(ActionInvocation invocation, BaseAction action) throws Exception {
    @SuppressWarnings("unchecked")
    Map<String, ?> session = invocation.getInvocationContext().getSession();

    @SuppressWarnings("unchecked")
    Collection<String> actionErrors = (Collection) session.remove(ACTION_ERRORS_KEY);
    if (actionErrors != null && actionErrors.size() > 0) {
      for (String error : actionErrors) {
        action.addActionError(error);
      }
    }

    @SuppressWarnings("unchecked")
    Collection<String> actionWarnings = (Collection) session.remove(ACTION_WARNINGS_KEY);
    if (actionWarnings != null && actionWarnings.size() > 0) {
      for (String error : actionWarnings) {
        action.addActionWarning(error);
      }
    }

    @SuppressWarnings("unchecked")
    Collection<String> actionMessages = (Collection) session.remove(ACTION_MESSAGES_KEY);
    // if (actionMessages!=null){
    // System.out.println("Found "+actionMessages.size()+" actionMessages in session");
    // }
    if (actionMessages != null && actionMessages.size() > 0) {
      for (String message : actionMessages) {
        action.addActionMessage(message);
      }
    }

    @SuppressWarnings("unchecked")
    Map<String, List<String>> fieldErrors = (Map) session.remove(FIELD_ERRORS_KEY);
    if (fieldErrors != null && fieldErrors.size() > 0) {
      for (Map.Entry<String, List<String>> fieldError : fieldErrors.entrySet()) {
        for (String message : fieldError.getValue()) {
          action.addFieldError(fieldError.getKey(), message);
        }
      }
    }
  }

  @Override
  public String doIntercept(ActionInvocation invocation) throws Exception {
    Object action = invocation.getAction();
    if (action instanceof BaseAction) {
      before(invocation, (BaseAction) action);
    }

    String result = invocation.invoke();

    if (action instanceof BaseAction) {
      after(invocation, (BaseAction) action);
    }
    return result;
  }
}