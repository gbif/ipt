/*
 * Copyright 2010 GBIF.
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
package org.gbif.provider.model.eml;

import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.eml.Method;

/**
 * The EML schema for methods is a sequence of repeating 
 * in the EML, it will always be a repetition of [methodStep, ?sampling, ?qualityControl]* 
 * (sampling and quality control are optional)
 * 
 * This utility container object should be put on the Sax parse stack and populated, and then
 * on </methods> this can be called to get the proper Methods object
 * 
 * @author timrobertson
 */
public class MethodParseUtil {
	// the EML that the methods belong to
	//protected Eml eml;
	protected List<Method> methods = new ArrayList<Method>();
	
	public void collectStep(String desc) {
		methods.add(new Method());
		methods.get(methods.size()-1).setStepDescription(desc);
	}
	
	public void collectExtentDesc(String desc) {
		methods.get(methods.size()-1).setStudyExtent(desc);
	}
	
	public void collectSamplingDesc(String desc) {
		methods.get(methods.size()-1).setSampleDescription(desc);
	}
	
	public void collectQualityDesc(String desc) {
		methods.get(methods.size()-1).setQualityControl(desc);
	}
	
	public void updateEml(Eml eml) {
		eml.setSamplingMethods(methods);
	}
	
	
	// getters / setters follow
	//public Eml getEml() {
	//	return eml;
//	}
	//public void setEml(Eml eml) {
		//this.eml = eml;
//	}
}
