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
    <title><@s.text name="eml.temporalCoverage"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
    <meta name="heading" content="<@s.text name='eml.temporalCoverage'/>"/>        
    
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

  function toggleSingleDate(checkbox){
//    Effect.toggle('endDateDiv', 'appear', { duration: 0.3 });
        if(checkbox.checked){
            // $('startDate').focus();
            $('endDate').value=$('startDate').value;
            $('endDate').hide();
        } else {
            $('endDate').show();
        }
  }

  function HideTemporalCoverageClone() {
    $('#removeLink').hide();
    $('#cloneTemporalCoverage').hide();
  }
  
  function GetTemporalCoverages() {
    var temporalCoverages = new Array();
    <#if (eml.temporalCoverages ? size > 0)>
      <@s.iterator value="eml.temporalCoverages" status="stat">     
        temporalCoverages[${stat.index}] = new TemporalCoverage()
          .startDate("<@s.property value="startDate"/>")
          .endDate("<@s.property value="endDate"/>");
      </@s.iterator>
    </#if>
    return temporalCoverages;
  }

  function OnLoad() {  
    HideTemporalCoverageClone();
    var temporalCoveragePanel = new TemporalCoveragePanel();
    var temporalCoverageWidget;
    $('#plus').click(function() {
      temporalCoveragePanel.add(new TemporalCoverageWidget());
    });
    var temporalCoverages = GetTemporalCoverages();
    for (temporalCoverage in temporalCoverages) {
      temporalCoverageWidget = new TemporalCoverageWidget(temporalCoverages[temporalCoverage]);
      temporalCoveragePanel.add(temporalCoverageWidget);
    }
  }

  google.setOnLoadCallback(OnLoad);

//]]>

</script>

</head>

<div class="break10"></div>
<p class="explMt"><@s.text name='metadata.temporalCoverageDescription'/></p>

<@s.form id="emlForm" action="tempcoverage" enctype="multipart/form-data" method="post">

<fieldset>
  <@s.hidden name="resourceId" value="${(resource.id)!}"/>
  <@s.hidden name="resourceType" value="${(resourceType)!}"/>
  <@s.hidden name="guid" value="${(resource.guid)!}"/>
  <@s.hidden name="nextPage" value="rights"/>
  <@s.hidden name="method" value="temporalCoverages"/>
  <div id="temporalCoveragePanel" class="newline">
    <!-- The cloneTemporalCoverage DIV is not attached to the DOM. It's used as a template
       for cloning temporalCoverage UI widgets. 
    -->
    <div id="cloneTemporalCoverage">
      <div id="separator" class="horizontal_dotted_line_large_foo"></div>
      <div class="newline"></div>
      <div class="right">
        <a id="removeLink" href="" onclick="return false;">[ <@s.text name='metadata.removethis'/> <@s.text name='eml.temporalCoverage'/> ]</a>
      </div>
      <div class="newline"></div>
      <div>
        <div class="leftMedium" id="startDateDiv">
          <@s.textfield id="startDate" key="" 
          label="%{getText('temporalCoverage.startDate')}" required="true" cssClass="text medium"/>
        </div>
        <div class="leftMedium" id="endDateDiv">
          <@s.textfield id="endDate" key="" 
          label="%{getText('temporalCoverage.endDate')}" required="false" cssClass="text medium"/>
        </div>
<!--  Check that this will work -->
<!-- It doesn't. Needs to be a checkbox for every temporalCoverage, controlling the correct dates. -->
<!--
       <div class="left">
          <@s.checkbox key="" value="false" onclick="javascript:toggleSingleDate(this);" />
        </div>
-->
        <div class="left">
          <span><@s.text name='metadata.temporalCoverageExample'/> 1999/07/21</span>
        </div>
      </div>
      <div class="newline"></div>
      <div class="newline"></div>
      <div class="newline"></div>
      <div class="newline"></div>
    </div>
  </div>
  <div class="left">
    <a id="plus" href="" onclick="return false;"><@s.text name='metadata.addnew'/> <@s.text name='eml.temporalCoverage'/></a>
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

<div class="breakRightButtons">
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
</div>

<h1 class="modifiedh1Secondary"><@s.text name="eml.keywords"/></h1>
<div class="horizontal_dotted_line_large_foo"></div>
<fieldset style="padding-top:13px;">
    <p>Keyword:Thesaurus pairs separated by comma (e.g., Animal:Bird, Insect:spider)</p>
    <@s.textarea key="keywords" label="" required="false" cssClass="text xlarge"/>
</fieldset>

    <div class="breakRightButtons">
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
    </div>    
</@s.form>