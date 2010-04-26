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
    <title><@s.text name="eml.project"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
	<meta name="heading" content="<@s.text name='eml.project'/>"/>
	<script src="http://www.google.com/jsapi?key=ABQIAAAAQmTfPsuZgXDEr012HM6trBT2yXp_ZAY8_ufC3CFXhHIE1NvwkxQTBMMPM0apn-CWBZ8nUq7oUL6nMQ"
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

    var roleSelect=null;
    
    google.load("jquery", "1.4.2");

    function OnLoad() { 
      $('#roleDiv').hide();
      $('#role').attr('value', $('#roleEnum').attr('value'));
    };

    google.setOnLoadCallback(OnLoad);
//]]>
  </script>	    
</head>

<div class="break10"></div>
<@s.form id="emlForm" action="projects" method="post" validate="false">
<fieldset>
	<@s.hidden name="resourceId" value="${resourceId?c}"/>
	<@s.hidden name="nextPage" value="methods"/>

	<@s.textfield key="eml.project.title" cssClass="text xlarge" />
    <div class="leftxhalf">
      <@s.textfield id="firstName" key="eml.project.personnel.firstName" 
        label="%{getText('eml.project.personnel')} %{getText('agent.firstName')}" required="true" 
        cssClass="text xhalf"/>
    </div>
    <div class="leftxhalf">
      <@s.textfield id="lastName" key="eml.project.personnel.lastName" 
        label="%{getText('eml.project.personnel')} %{getText('agent.lastName')}" required="true" 
        cssClass="text xhalf"/>
    </div>
    <div class="leftxhalf">
       <@s.select id="role" key="eml.project.personnel.role" 
         label="%{getText('eml.project.personnel')} %{getText('agent.role')}"
         list="agentRoleMap.entrySet()" value="role.name" listKey="key"
         listValue="value"/>
    </div>
    <div class="newline"></div>
    <div id="roleDiv">
	  <@s.textfield id="roleEnum" key="eml.project.personnel.role"/>
	</div>
    <@s.textarea key="eml.project.funding" cssClass="text xlarge slim" />
	<@s.textarea key="eml.project.studyAreaDescription.descriptorValue" cssClass="text xlarge slim" />
	<@s.textarea key="eml.project.designDescription" cssClass="text xlarge slim" />

</fieldset>

	<div class="breakRightButtons">
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
 	</div>
</@s.form>
