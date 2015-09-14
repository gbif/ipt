package org.gbif.ipt.struts2;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;

/**
 * Request filter that forces application to handle all requests and responses as UTF-8 encoded.
 */
@Singleton
public class CharacterEncodingFilter implements Filter {
  private static final String UTF8 = "UTF-8";

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
    if (request instanceof HttpServletRequest) {
      HttpServletRequest httpReq = (HttpServletRequest) request;
      httpReq.setCharacterEncoding(UTF8);
    }

    if (response instanceof HttpServletResponse) {
      HttpServletResponse httpRes = ((HttpServletResponse) response);
      httpRes.setCharacterEncoding(UTF8);
    }

    chain.doFilter(request, response);
  }
}