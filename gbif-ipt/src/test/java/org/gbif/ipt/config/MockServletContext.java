/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ***************************************************************************/

package org.gbif.ipt.config;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * @author markus
 * 
 */
public class MockServletContext implements ServletContext {

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getAttribute(java.lang.String)
   */
  public Object getAttribute(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getAttributeNames()
   */
  public Enumeration getAttributeNames() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getContext(java.lang.String)
   */
  public ServletContext getContext(String uripath) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getInitParameter(java.lang.String)
   */
  public String getInitParameter(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getInitParameterNames()
   */
  public Enumeration getInitParameterNames() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getMajorVersion()
   */
  public int getMajorVersion() {
    // TODO Auto-generated method stub
    return 0;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getMimeType(java.lang.String)
   */
  public String getMimeType(String file) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getMinorVersion()
   */
  public int getMinorVersion() {
    // TODO Auto-generated method stub
    return 0;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getNamedDispatcher(java.lang.String)
   */
  public RequestDispatcher getNamedDispatcher(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getRealPath(java.lang.String)
   */
  public String getRealPath(String path) {
    // URL res = MockServletContext.class.getResource("MockServletContext.class");
    // System.out.println(res.toExternalForm());
    return new File("target/test-classes/" + path).getAbsolutePath();
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getRequestDispatcher(java.lang.String)
   */
  public RequestDispatcher getRequestDispatcher(String path) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getResource(java.lang.String)
   */
  public URL getResource(String path) throws MalformedURLException {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getResourceAsStream(java.lang.String)
   */
  public InputStream getResourceAsStream(String path) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getResourcePaths(java.lang.String)
   */
  public Set getResourcePaths(String path) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getServerInfo()
   */
  public String getServerInfo() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getServlet(java.lang.String)
   */
  public Servlet getServlet(String name) throws ServletException {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getServletContextName()
   */
  public String getServletContextName() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getServletNames()
   */
  public Enumeration getServletNames() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getServlets()
   */
  public Enumeration getServlets() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#log(java.lang.Exception, java.lang.String)
   */
  public void log(Exception exception, String msg) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#log(java.lang.String)
   */
  public void log(String msg) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#log(java.lang.String, java.lang.Throwable)
   */
  public void log(String message, Throwable throwable) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
   */
  public void removeAttribute(String name) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#setAttribute(java.lang.String, java.lang.Object)
   */
  public void setAttribute(String name, Object object) {
    // TODO Auto-generated method stub

  }

}