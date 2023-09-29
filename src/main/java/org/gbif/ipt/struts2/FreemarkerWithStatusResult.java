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
package org.gbif.ipt.struts2;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.views.freemarker.FreemarkerResult;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import com.opensymphony.xwork2.ActionInvocation;
import freemarker.template.TemplateException;

/**
 * A Struts2 Result using Freemarker, but also setting an HTTP status.
 * <p>
 * Compare HttpHeaderResult.
 */
public class FreemarkerWithStatusResult extends FreemarkerResult {

  private static final long serialVersionUID = -2494293347333549306L;

  private int status = -1;

  public FreemarkerWithStatusResult() {
    super();
  }

  public FreemarkerWithStatusResult(int status) {
    this();
    this.status = status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public int getStatus() {
    return status;
  }

  @Override
  public void doExecute(String locationArg, ActionInvocation invocation) throws IOException, TemplateException {
    HttpServletResponse response = ServletActionContext.getResponse();

    if (status != -1) {
      response.setStatus(status);
    }

    super.doExecute(locationArg, invocation);
  }
}
