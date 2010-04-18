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
<title><@s.text name="dataResource.metadata"/></title>
<meta name="resource" content="${resource.title!}"/>
<meta name="menu" content="ManagerMenu"/>
<meta name="submenu" content="manage_resource"/>  
<meta name="heading" content="<@s.text name='metadata.associatedPartiesHeading'/>"/> 
 
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

function HideAgentClone() {
  $('#removeLink').hide();
  $('#cloneAgent').hide();
}
  
function GetAgents() {
  var agents = new Array();
  <#if (eml.associatedParties ? size > 0)>
    <@s.iterator value="eml.associatedParties" status="stat">      
      agents[${stat.index}] = new Agent()
        .firstName("<@s.property value="firstName"/>")
        .lastName("<@s.property value="lastName"/>")
        .position("<@s.property value="position"/>");
    </@s.iterator>
  </#if>
  return agents;
}

function OnLoad() {
  HideAgentClone();
  var agentPanel = new AgentPanel();
  var agentWidget;
  $('#plus').click(function() {
    agentPanel.add(new AgentWidget());
  });
  var agents = GetAgents();
  for (agent in agents) {
    agentWidget = new AgentWidget(agents[agent]);
    agentPanel.add(agentWidget);
  }
}

google.setOnLoadCallback(OnLoad);

//]]>

</script>
  
<style>
  #btnResend {
    position: relative;
    left: 4px;
  #nodeLoading {
    position: relative;
    bottom: 26px;
    left: 12px;
  }
</style>

</head>
<p class="explMt"><@s.text name='metadata.associatedPartiesDescription'/></p>

<@s.form id="emlForm" action="associatedParties" enctype="multipart/form-data" 
  method="post">
  
<fieldset>
<@s.hidden name="resourceId" value="${(resource.id)!}"/>
<@s.hidden name="resourceType" value="${(resourceType)!}"/>
<@s.hidden name="guid" value="${(resource.guid)!}"/>
<@s.hidden name="nextPage" value="geocoverage"/>
<@s.hidden name="method" value="associatedParties"/>

<div id="agentPanel" class="newline">
  <!-- The cloneAgent DIV is not attached to the DOM. It's used as a template
       for cloning agent UI widgets. 
  -->
  <div id="cloneAgent">
    <div id="separator" class="horizontal_dotted_line_large_foo"></div>
    <div class="newline"></div>
    <div class="right">
      <a id="removeLink" href="" onclick="return false;">[ Remove this person ]</a>
    </div>
    <div class="newline"></div>
    <div class="leftMedium">
      <@s.textfield id="firstName" key="" 
        label="%{getText('agent.firstName')}" required="true" 
        cssClass="text medium"/>
    </div>
    <div class="leftMedium">
      <@s.textfield id="lastName" key="" 
        label="%{getText('agent.lastName')}" required="true" 
        cssClass="text medium"/>
    </div>
    <div class="leftMedium">
      <@s.textfield id="position" key=""  
        label="%{getText('agent.position')}" required="true" cssClass="text medium"/>
    </div>
    <div class="newline"></div>
  </div>
</div>

<div class="left">
  <a id="plus" href="" onclick="return false;">Add new person</a>
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
