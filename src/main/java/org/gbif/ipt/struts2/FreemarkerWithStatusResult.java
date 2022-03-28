package org.gbif.ipt.struts2;

import com.opensymphony.xwork2.ActionInvocation;
import freemarker.template.TemplateException;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.views.freemarker.FreemarkerResult;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A Struts2 Result using Freemarker, but also setting an HTTP status.
 *
 * Compare HttpHeaderResult.
 */
public class FreemarkerWithStatusResult extends FreemarkerResult {

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
