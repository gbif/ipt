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
<title><@s.text name='manage.metadata.collections.title'/></title>
<#assign sideMenuEml=true />
 
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<h1><@s.text name='manage.metadata.collections.title'/>: <em>${ms.resource.title!ms.resource.shortname}</em></h1>
<@s.text name='manage.metadata.collections.intro'/>
<form class="topForm" action="metadata-${section}.do" method="post"> 
    <div class="newline"></div>
    <div>
      <div id="collectionNameDiv" class="leftxhalf">
        <@s.textfield id="collectionName" key="eml.collectionName" 
          required="false" cssClass="text xhalf slim"/>
      </div>
    <div id="collectionIdDiv" class="leftxhalf">
      <@s.textfield id="collectionId" key="eml.collectionId" 
          required="false" cssClass="text xhalf slim"/>
    </div>
    <div class="newline"></div>
      <div id="parentCollectionIdDiv" class="leftxhalf">
      <@s.textfield id="parentCollectionId" key="eml.parentCollectionId" 
          required="false" cssClass="text xhalf slim"/>
      </div>
    </div>

    <div class="newline"></div>
    <h2 class="explMt"><@s.text name="metadata.heading.curatorialUnits"/></h2>
    <p class="explMt"><@s.text name='metadata.description.curatorialUnits'/></p>

    <div id="curatorialUnitPanel" class="newline">
      <!-- The cloneCuratorialUnit DIV is not attached to the DOM. It's used as a template
         for cloning CuratorialUnit UI widgets. 
      -->
      <div id="cloneCuratorialUnit">
        <div id="separator" class="horizontal_dotted_line_large_foo"></div>
        <div class="newline"></div>
        <div class="right">
          <a id="removeLink" href="" onclick="return false;">[ <@s.text name='metadata.removethis'/> <@s.text name='metadata.heading.curatorialUnit'/> ]</a>
        </div>
        <div class="newline"></div>

        <div class="leftxhalf" id="typeDiv">
          <@s.select id="type" key="" 
            label="%{getText('method.type')}"
            list="curatorialUnitTypeMap.entrySet()" 
            value="curatorialUnitType.name()" listKey="key"
            listValue="value" required="true"/>
        </div>
        <div class="newline"></div>

        <div>
          <div class="leftMedium" id="rangeStartDiv">
            <@s.textfield id="rangeStart" key="" 
              label="%{getText('jgtiCuratorialUnit.rangeStart')}"
              required="false" cssClass="text medium"/>
          </div>
          <div class="leftMedium" id="rangeEndDiv">
            <@s.textfield id="rangeEnd" key="" 
              label="%{getText('jgtiCuratorialUnit.rangeEnd')}" 
              required="false" cssClass="text medium"/>
          </div>
          <div class="leftMedium" id="rangeMeanDiv">
            <@s.textfield id="rangeMean" key="" 
              label="%{getText('jgtiCuratorialUnit.rangeMean')}"
              required="false" cssClass="text medium"/>
          </div>
          <div class="leftMedium" id="uncertaintyMeasureDiv">
            <@s.textfield id="uncertaintyMeasure" key="" 
              label="%{getText('jgtiCuratorialUnit.uncertaintyMeasure')}" 
              required="false" cssClass="text medium"/>
          </div>
          <div class="leftMedium" id="unitTypeDiv">
            <@s.textfield id="unitType" key="" 
              label="%{getText('jgtiCuratorialUnit.unitType')}" 
              required="false" cssClass="text medium"/>
          </div>
        </div>
        <div class="newline"></div>
        <div class="newline"></div>
        <div class="newline"></div>
        <div class="newline"></div>
      </div>
    </div>
    <div class="left">
      <a id="plus" href="" onclick="return false;"><@s.text name='metadata.addnew'/> <@s.text name='metadata.heading.curatorialUnit'/></a>
    </div>
    <div class="newline"></div>
    <div class="newline"></div>
    <div class="newline"></div>
    <div class="newline"></div>
    <div class="newline"></div>    
<div class="buttons">
  <@s.submit name="save" key="button.save" />
  <@s.submit name="cancel" key="button.cancel" />
</div>
</form>
<#include "/WEB-INF/pages/inc/footer.ftl">