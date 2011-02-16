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
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getAttributeNames()
   */
  public Enumeration getAttributeNames() {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getContext(java.lang.String)
   */
  public ServletContext getContext(String uripath) {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getInitParameter(java.lang.String)
   */
  public String getInitParameter(String name) {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getInitParameterNames()
   */
  public Enumeration getInitParameterNames() {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getMajorVersion()
   */
  public int getMajorVersion() {
    return 0;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getMimeType(java.lang.String)
   */
  public String getMimeType(String file) {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getMinorVersion()
   */
  public int getMinorVersion() {
    return 0;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getNamedDispatcher(java.lang.String)
   */
  public RequestDispatcher getNamedDispatcher(String name) {
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
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getResource(java.lang.String)
   */
  public URL getResource(String path) throws MalformedURLException {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getResourceAsStream(java.lang.String)
   */
  public InputStream getResourceAsStream(String path) {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getResourcePaths(java.lang.String)
   */
  public Set getResourcePaths(String path) {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getServerInfo()
   */
  public String getServerInfo() {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getServlet(java.lang.String)
   */
  public Servlet getServlet(String name) throws ServletException {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getServletContextName()
   */
  public String getServletContextName() {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getServletNames()
   */
  public Enumeration getServletNames() {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#getServlets()
   */
  public Enumeration getServlets() {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#log(java.lang.Exception, java.lang.String)
   */
  public void log(Exception exception, String msg) {

  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#log(java.lang.String)
   */
  public void log(String msg) {

  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#log(java.lang.String, java.lang.Throwable)
   */
  public void log(String message, Throwable throwable) {

  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
   */
  public void removeAttribute(String name) {

  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContext#setAttribute(java.lang.String, java.lang.Object)
   */
  public void setAttribute(String name, Object object) {

  }

}