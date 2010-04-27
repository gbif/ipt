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
  <title><@s.text name="metadata.heading.physicalData"/></title>
  <meta name="resource" content="${eml.title!}"/>
  <meta name="menu" content="ManagerMenu"/>
  <meta name="submenu" content="manage_resource"/>
  <meta name="heading" content="<@s.text name='metadata.heading.physicalData'/>"/>  

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

    function HidePhysicalDataClone() {
      $('#removeLink').hide();
      $('#clonePhysicalData').hide();
    }
  
    function GetPhysicalData() {
      var physicalData = new Array();
      <#if (eml.physicalData ? size > 0)>
        <@s.iterator value="eml.physicalData" status="stat">     
          physicalData[${stat.index}] = new PhysicalData()
            .charset("<@s.property value="charset"/>")
            .distributionUrl("<@s.property value="distributionUrl"/>")
            .format("<@s.property value="format"/>")
            .formatVersion("<@s.property value="formatVersion"/>")
            .name("<@s.property value="name"/>")
            ;
        </@s.iterator>
      </#if>
      return physicalData;
    }

    function OnLoad() {  
      HidePhysicalDataClone();
      var physicalDataPanel = new PhysicalDataPanel();
      var physicalDataWidget;
      $('#plus').click(function() {
        physicalDataPanel.add(new PhysicalDataWidget());
      });
      var physicalData = GetPhysicalData();
      for (physicalDatum in physicalData) {
        physicalDataWidget = new PhysicalDataWidget(physicalData[physicalDatum]);
        physicalDataPanel.add(physicalDataWidget);
      }
    }

    google.setOnLoadCallback(OnLoad);
    
//]]>

  </script>

</head>

<div class="break10"></div>
<p class="explMt"><@s.text name='metadata.description.physicalData'/></p>
<@s.form id="emlForm" action="physicalData" enctype="multipart/form-data" method="post">

<fieldset>
  <@s.hidden name="resourceId" value="${(resource.id)!}"/>
  <@s.hidden name="resourceType" value="${(resourceType)!}"/>
  <@s.hidden name="guid" value="${(resource.guid)!}"/>
  <@s.hidden name="nextPage" value="rights"/>
  <@s.hidden name="method" value="physicalData"/>
  <div id="physicalDataPanel" class="newline">
    <!-- The clonePhysicalData DIV is not attached to the DOM. It's used as a template
       for cloning physicalData UI widgets. 
    -->
    <div id="clonePhysicalData">
      <div id="separator" class="horizontal_dotted_line_large_foo"></div>
      <div class="newline"></div>
      <div class="right">
        <a id="removeLink" href="" onclick="return false;">[ <@s.text name='metadata.removethis'/> <@s.text name='metadata.heading.physicalData'/> ]</a>
      </div>
      <div class="newline"></div>

      <div id="PhysicalDataDiv">
        <div id="nameDiv" class="leftxhalf">
          <@s.textfield id="name" key="" 
          label="%{getText('physicalData.name')}" required="false" cssClass="text text xhalf slim"/>
        </div>
        <div id="charsetDiv" class="leftxhalf">
          <@s.textfield id="charset" key="" 
          label="%{getText('physicalData.charset')}" required="false" cssClass="text xhalf slim"/>
        </div>
        <div class="newline"></div>
        <div id="distributionUrlDiv">
          <@s.textfield id="distributionUrl" key="" 
          label="%{getText('physicalData.distributionUrl')}" required="false" cssClass="text text xlarge slim"/>
        </div>
        <div class="newline"></div>
        <div id="formatDiv" class="leftxhalf">
          <@s.textfield id="format" key="" 
          label="%{getText('physicalData.format')}" required="false" cssClass="text text xhalf slim"/>
        </div>
        <div id="formatVersionDiv" class="leftxhalf">
          <@s.textfield id="formatVersion" key="" 
          label="%{getText('physicalData.formatVersion')}" required="false" cssClass="text text xhalf slim"/>
        </div>
      </div>
      <div class="newline"></div>
      <div class="newline"></div>
      <div class="newline"></div>
      <div class="newline"></div>
    </div>
  </div>

  <div class="left">
    <a id="plus" href="" onclick="return false;"><@s.text name='metadata.addnew'/> <@s.text name='metadata.heading.physicalData'/></a>
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