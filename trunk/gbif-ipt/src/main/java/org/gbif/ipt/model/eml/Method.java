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
package org.gbif.ipt.model.eml;

import java.io.Serializable;

/**
 * Encapsulates the description of the sampling methods employed
 */
public class Method implements Serializable {
  /**
   * Generated
   */
  private static final long serialVersionUID = 8272714768039859733L;

  /**
   * "The methodStep field allows for repeated sets of elements that document a
   * series of procedures followed to produce a data object. These include text
   * descriptions of the procedures, relevant literature, software,
   * instrumentation, source data and any quality control measures taken." This
   * implementation allows only the declaration of the step description
   * 
   * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-methods.html#
   *      methodStep
   */
  private String stepDescription;

  /**
   * "The coverage field allows for a textual description of the specific
   * sampling area, the sampling frequency (temporal boundaries, frequency of
   * occurrence), and groups of living organisms sampled (taxonomic coverage)."
   * This implementation allows only the declaration of the extent description
   * 
   * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-methods.html#
   *      studyExtent
   */
  private String studyExtent;

  /**
   * The samplingDescription field allows for a text-based/human readable
   * description of the sampling procedures used in the research project. The
   * content of this element would be similar to a description of sampling
   * procedures found in the methods section of a journal article.
   * 
   * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-methods.html#
   *      samplingDescription
   */
  private String sampleDescription;

  /**
   * The qualityControl field provides a location for the description of actions
   * taken to either control or assess the quality of data resulting from the
   * associated method step.
   * 
   * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-methods.html#
   *      qualityControl
   */
  private String qualityControl;

  /**
   * Default constructor required by Struts2
   */
  public Method() {
  }

  /**
   * @return the qualityControl
   */
  public String getQualityControl() {
    if(qualityControl == null || qualityControl.length() == 0) return null;
    return qualityControl;
  }

  /**
   * @return the sampleDescription
   */
  public String getSampleDescription() {
    if(sampleDescription == null || sampleDescription.length() == 0) return null;
    return sampleDescription;
  }

  /**
   * @return the stepDescription
   */
  public String getStepDescription() {
    if(stepDescription == null || stepDescription.length() == 0) return null;
    return stepDescription;
  }

  /**
   * @return the studyExtent
   */
  public String getStudyExtent() {
    if(studyExtent == null || studyExtent.length() == 0) return null;
    return studyExtent;
  }

  public MethodType getType() {
    if(this.stepDescription != null && this.stepDescription.length()>0){
      return MethodType.METHOD_STEP;
    } else if(this.qualityControl != null && this.qualityControl.length()>0) {
      return MethodType.QUALITY_CONTROL;
    }
    return MethodType.SAMPLING;
  }

  /**
   * @param qualityControl the qualityControl to set
   */
  public void setQualityControl(String qualityControl) {
    if(qualityControl != null && qualityControl.length() == 0 )
      this.qualityControl=null;
    else {
      this.qualityControl = qualityControl;
    }
  }

  /**
   * @param sampleDescription the sampleDescription to set
   */
  public void setSampleDescription(String sampleDescription) {
    if(sampleDescription != null && sampleDescription.length() == 0 )
      this.sampleDescription=null;
    else {
      this.sampleDescription = sampleDescription;
    }
  }

  /**
   * @param stepDescription the stepDescription to set
   */
  public void setStepDescription(String stepDescription) {
    if(stepDescription != null && stepDescription.length() == 0 )
      this.stepDescription=null;
    else {
      this.stepDescription = stepDescription;
    }
  }

  /**
   * @param studyExtent the studyExtent to set
   */
  public void setStudyExtent(String studyExtent) {
    if(studyExtent != null && studyExtent.length() == 0 )
      this.studyExtent=null;
    else {
      this.studyExtent = studyExtent;
    }
  }
}
