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
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.metadata.methods.title'/></title>
<#assign sideMenuEml=true />
 
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<h1><@s.text name='manage.metadata.methods.title'/>: <em>${ms.resource.title!ms.resource.shortname}</em></h1>
<@s.text name='manage.metadata.methods.intro'/>
<form class="topForm" action="metadata-${section}.do" method="post"> 
    <div id="samplingMethodPanel" class="newline">
      <!-- The cloneSamplingMethods DIV is not attached to the DOM. It's used as a template
           for cloning sampling method UI widgets. 
      -->
      <div id="cloneSamplingMethods">
        <div id="separator" class="horizontal_dotted_line_large_foo"></div>
        <div class="newline"></div>
        <div class="right">
          <a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.methods.item'/> ]</a>
    	</div>
        <div class="newline"></div>

        <div class="leftxhalf" id="typeDiv">
          <@s.select id="type" key="" 
            label="%{getText('method.type')}"
            list="methodTypeMap.entrySet()" 
            value="methodType.name()" listKey="key"
            listValue="value" required="true"/>
        </div>
        <div class="newline"></div>

        <div id="SamplingMethodDiv">
          <div id="stepDescriptionDiv">
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

<a id="plus" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.methods.item'/></a></br></br>

    <div class="newline"></div>
    <div class="newline"></div>
    <div class="newline"></div>
    <div class="newline"></div>
    <div class="newline"></div>
<div class="buttons">
  <@s.submit name="save" key="button.save" />
  <@s.submit name="cancel" key="button.cancel" />
</div>
<form>

<#include "/WEB-INF/pages/inc/footer.ftl">