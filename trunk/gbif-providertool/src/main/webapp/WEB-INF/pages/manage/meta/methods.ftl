<!--
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
-->

<head>
  <title><@s.text name="metadata.heading.methods"/></title>
  <meta name="resource" content="${eml.title!}"/>
  <meta name="menu" content="ManagerMenu"/>
  <meta name="submenu" content="manage_resource"/>
  <meta name="heading" content="<@s.text name='metadata.heading.methods'/>"/>  

  <script
    src="http://www.google.com/jsapi?key=ABQIAAAAQmTfPsuZgXDEr012HM6trBT2yXp_ZAY8_ufC3CFXhHIE1NvwkxQTBMMPM0apn-CWBZ8nUq7oUL6nMQ"
    type="text/javascript">
  </script>

  <script 
    src="<@s.url value='/scripts/dto.js'/>"
    type="text/javascript">
  </script>

  <script 
    src="<@s.url value='/scripts/widgets.js'/>"
    type="text/javascript">
  </script>

  <script language="Javascript"
    type="text/javascript">

//<![CDATA[  

    google.load("jquery", "1.4.2");

    function HideSamplingMethodClone() {
      $('#removeLink').hide();
      $('#cloneSamplingMethods').hide();
    }
  
    function GetSamplingMethods() {
      var samplingMethods = new Array();
      <#if (eml.samplingMethods ? size > 0)>
        <@s.iterator value="eml.samplingMethods" status="stat">     
          samplingMethods[${stat.index}] = new SamplingMethod()
            .stepDescription("<@s.property value="stepDescription"/>")
            .studyExtent("<@s.property value="studyExtent"/>")
            .sampleDescription("<@s.property value="sampleDescription"/>")
            .qualityControl("<@s.property value="qualityControl"/>");
        </@s.iterator>
      </#if>
      return samplingMethods;
    }

    function OnLoad() {  
      HideSamplingMethodClone();
      var samplingMethodPanel = new SamplingMethodPanel();
      var samplingMethodWidget;
      $('#plus').click(function() {
        samplingMethodPanel.add(new SamplingMethodWidget());
      });
      var samplingMethods = GetSamplingMethods();
      for (samplingMethod in samplingMethods) {
        samplingMethodWidget = new SamplingMethodWidget(samplingMethods[samplingMethod]);
        samplingMethodPanel.add(samplingMethodWidget);
      }
    }

    google.setOnLoadCallback(OnLoad);
    
//]]>

  </script>

</head>

<div class="break10"></div>
<p class="explMt"><@s.text name='metadata.description.methods'/></p>
<@s.form id="emlForm" action="methods" enctype="multipart/form-data" method="post">

<fieldset>
  <@s.hidden name="resourceId" value="${(resource.id)!}"/>
  <@s.hidden name="resourceType" value="${(resourceType)!}"/>
  <@s.hidden name="guid" value="${(resource.guid)!}"/>
  <@s.hidden name="nextPage" value="citations"/>
  <@s.hidden name="method" value="samplingMethods"/>
  <div id="samplingMethodPanel" class="newline">
    <!-- The cloneSamplingMethods DIV is not attached to the DOM. It's used as a template
       for cloning sampling method UI widgets. 
    -->
    <div id="cloneSamplingMethods">
      <div id="separator" class="horizontal_dotted_line_large_foo"></div>
      <div class="newline"></div>
      <div class="right">
        <a id="removeLink" href="" onclick="return false;">[ <@s.text name='metadata.removethis'/> <@s.text name='eml.methods'/> ]</a>
      </div>
      <div class="newline"></div>

      <div id="SamplingMethodDiv">
        <div id="stepDescriptiopnDiv">
          <@s.textarea id="stepDescription" key="" 
          label="%{getText('method.stepDescription')}" required="false" cssClass="text xlarge slim"/>
        </div>
        <div id="studyExtentDiv">
          <@s.textarea id="studyExtent" key="" 
          label="%{getText('method.studyExtent')}" required="false" cssClass="text text xlarge slim"/>
        </div>
        <div id="sampleDescriptionDiv">
          <@s.textarea id="sampleDescription" key="" 
          label="%{getText('method.sampleDescription')}" required="false" cssClass="text text xlarge slim"/>
        </div>
        <div id="qualityControlDiv">
          <@s.textarea id="qualityControl" key="" 
          label="%{getText('method.qualityControl')}" required="false" cssClass="text text xlarge slim"/>
        </div>
      </div>
      <div class="newline"></div>
      <div class="newline"></div>
      <div class="newline"></div>
      <div class="newline"></div>
    </div>
  </div>

  <div class="left">
    <a id="plus" href="" onclick="return false;"><@s.text name='metadata.addnew'/> <@s.text name='eml.methods'/></a>
  </div>
  <div class="newline"></div>
  <div class="newline"></div>
  <div class="newline"></div>
  <div class="newline"></div>
  <div class="newline"></div>
  <div class="breakLeftButtons">
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
  </div>
</fieldset>

</@s.form>