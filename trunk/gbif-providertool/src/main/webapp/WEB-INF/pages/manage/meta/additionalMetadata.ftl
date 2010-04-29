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
    <title><@s.text name="metadata.heading.additionalMetadata"/></title>
    <meta name="resource" content="${resource.title!}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
	<meta name="heading" content="<@s.text name='metadata.heading.additionalMetadata'/>"/>      
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

//    google.load("jquery", "1.4.2");

    function OnLoad() { 
//      $('#pubDate').attr("disabled", "disabled");
      $('#role').attr('value', $('#roleEnum').attr('value'));
    };

    google.setOnLoadCallback(OnLoad);
//]]>
  </script>	    
</head>

<div class="break10"></div>
<p class="explMt"><@s.text name='metadata.description.additionalMetadata'/></p>
<@s.form id="emlForm" action="additionalMetadata" enctype="multipart/form-data" method="post"> 

<fieldset>
  <@s.hidden name="resourceId" value="${(resource.id)!}"/>
  <@s.hidden name="resourceType" value="${(resourceType)!}"/>
  <@s.hidden name="guid" value="${(resource.guid)!}"/>
  <!-- NOTE: As the presumed last page in the work flow sequence of EML metadata forms
       this form invokes the action <action name="additionalMetadata"... in struts.xml to go the the Basic Metadata form. 
    -->
  <@s.hidden name="nextPage" value="additionalMetadata"/>
  <@s.hidden name="method" value="additionalMetadata"/>    

    <div class="leftxhalf">
      <@s.textfield key="eml.hierarchyLevel" disabled="true"
        required="true" cssClass="text xhalf"/>
    </div>
    <div class="leftxhalf">
      <@s.textfield id="pubDate" key="eml.pubDate" 
        required="true" cssClass="text xhalf"/>
    </div>
    <div class="newline"></div>
    <div class="leftxhalf">
      <@s.textfield key="eml.distributionUrl"
        required="true" cssClass="text xlarge"/>
    </div>
    <div class="newline"></div>
	<@s.textarea key="eml.purpose" required="false" cssClass="text xlarge slim"/>
	<@s.textarea key="eml.intellectualRights" required="false" cssClass="text xlarge slim"/>
    <div class="newline"></div>
	<@s.textarea key="eml.additionalInfo" required="false" cssClass="text xlarge slim"/>
</fieldset>

	<div class="breakRightButtons">
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
	</div>
</@s.form>
