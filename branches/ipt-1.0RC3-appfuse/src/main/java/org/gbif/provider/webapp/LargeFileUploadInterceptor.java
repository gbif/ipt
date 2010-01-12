/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.webapp;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ValidationAware;

import org.apache.struts2.interceptor.FileUploadInterceptor;

import java.io.File;
import java.util.Locale;

/**
 * TODO: Documentation.
 * 
 */
public class LargeFileUploadInterceptor extends FileUploadInterceptor {
  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    log.info("Intercept called: " + this.maximumSize);
    return super.intercept(invocation);
  }

  @Override
  protected boolean acceptFile(File file, String contentType, String inputName,
      ValidationAware validation, Locale locale) {
    // TODO Auto-generated method stub
    log.info("Accept called: " + this.maximumSize);
    return super.acceptFile(file, contentType, inputName, validation, locale);
  }
}
