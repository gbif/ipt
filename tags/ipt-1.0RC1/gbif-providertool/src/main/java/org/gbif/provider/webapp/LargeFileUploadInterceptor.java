/**
 * 
 */
package org.gbif.provider.webapp;

import java.io.File;
import java.util.Locale;

import org.apache.struts2.interceptor.FileUploadInterceptor;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ValidationAware;

/**
 * @author tim
 *
 */
public class LargeFileUploadInterceptor extends FileUploadInterceptor {
@Override
public String intercept(ActionInvocation invocation) throws Exception {
	log.info("Intercept called: " + this.maximumSize);
	return super.intercept(invocation);
}
@Override
protected boolean acceptFile(File file, String contentType,
		String inputName, ValidationAware validation, Locale locale) {
	// TODO Auto-generated method stub
	log.info("Accept called: " + this.maximumSize);
	return super.acceptFile(file, contentType, inputName, validation, locale);
}
}
