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
  <title><@s.text name="metadata.heading.collections"/></title>
  <meta name="resource" content="${resource.title!}"/>
  <meta name="menu" content="ManagerMenu"/>
  <meta name="submenu" content="manage_resource"/>  
  <meta name="heading" content="<@s.text name='metadata.heading.collections'/>"/> 

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

  <script
    language="Javascript"
    type="text/javascript">

//<![CDATA[  

  google.load("jquery", "1.4.2");

  function HideCuratorialUnitClone() {
    $('#removeLink').hide();
    $('#cloneCuratorialUnit').hide();
  }
  
  function GetCuratorialUnits() {
    var curatorialUnits = new Array();
    <#if (eml.jgtiCuratorialUnits ? size > 0)>
      <@s.iterator value="eml.jgtiCuratorialUnits" status="stat">     
         curatorialUnits[${stat.index}] = new CuratorialUnit()
          .rangeStart("<@s.property value="rangeStart"/>")
          .rangeEnd("<@s.property value="rangeEnd"/>")
          .rangeMean("<@s.property value="rangeMean"/>")
          .unitType("<@s.property value="unitType"/>")
          .uncertaintyMeasure("<@s.property value="uncertaintyMeasure"/>")
          .type("<@s.property value="type"/>");
      </@s.iterator>
    </#if>
    return curatorialUnits;
  }

  function OnLoad() {  
    HideCuratorialUnitClone();
    var curatorialUnitPanel = new CuratorialUnitPanel();
    var size = curatorialUnitPanel.size();
    var curatorialUnitWidget;
    $('#plus').click(function() {
      curatorialUnitPanel.add(new CuratorialUnitWidget());
    });
    var curatorialUnits = GetCuratorialUnits();
    for (curatorialUnit in curatorialUnits) {
      curatorialUnitWidget = new CuratorialUnitWidget(curatorialUnits[curatorialUnit]);
      curatorialUnitPanel.add(curatorialUnitWidget);
    }
  }

  google.setOnLoadCallback(OnLoad);

//]]>

  </script>  
</head>

<div class="break10"></div>
<p class="explMt"><@s.text name='metadata.description.collections'/></p>
<@s.form id="emlForm" action="collections" enctype="multipart/form-data" method="post">
  <fieldset>
    <@s.hidden name="resourceId" value="${(resource.id)!}"/>
    <@s.hidden name="resourceType" value="${(resourceType)!}"/>
    <@s.hidden name="guid" value="${(resource.guid)!}"/>
    <@s.hidden name="nextPage" value="PhysicalData"/>
    <@s.hidden name="method" value="collections"/>

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
    <div class="breakLeftButtons">
      <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
      <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
    </div>
  </fieldset>
</@s.form>