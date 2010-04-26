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
<title><@s.text name="metadata.heading.citations"/></title>
<meta name="resource" content="${resource.title!}"/>
<meta name="menu" content="ManagerMenu"/>
<meta name="submenu" content="manage_resource"/>  
<meta name="heading" content="<@s.text name='metadata.heading.citations'/>"/> 

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

function GetCitations() {
  var citations = new Array();
  <#if (eml.bibliographicCitationSet.bibliographicCitations ? size > 0)>
    <@s.iterator value="eml.bibliographicCitationSet.bibliographicCitations" status="stat">     
      citations[${stat.index}] = "<@s.property value="toString()"/>";
    </@s.iterator>
  </#if>
  return citations;
}

function OnModuleLoaded() {
  $('#cloneCitation').hide();
  var citationPanel = new CitationPanel();
  $('#addCitationLink').click(function() {
    citationPanel.add(new CitationWidget());
  }); 
}

google.setOnLoadCallback(OnModuleLoaded);

//]]>

</script>  
</head>
<p class="explMt"><@s.text name='metadata.description.citations'/></p>
<@s.form id="emlForm" action="citations" enctype="multipart/form-data" 
  method="post">  
<fieldset>
<@s.hidden name="resourceId" value="${(resource.id)!}"/>
<@s.hidden name="resourceType" value="${(resourceType)!}"/>
<@s.hidden name="guid" value="${(resource.guid)!}"/>
<@s.hidden name="nextPage" value="rights"/>
<@s.hidden name="method" value="citations"/>

<div class="newline"></div>
<div>
  <@s.textfield id="citation" key="eml.citation"  cssClass="text xlarge"/>
</div>
<h2 class="explMt"><@s.text name="metadata.heading.citations.bibliography"/></h2>
<div id="citationsPanel" class="newline">
  <div id="cloneCitation">
    <div id="separator" class="horizontal_dotted_line_large_foo"></div>
    <div class="newline"></div>
    <div class="right">
      <a id="removeLink" href="" onclick="return false;">[ Remove this citation ]</a>
    </div>
    <div class="newline"></div>
    <div>
      <@s.textfield id="citation" key="" label="Citation" cssClass="text xlarge"/>
    </div>
    <div class="newline"></div>
    <div class="newline"></div>
    <div class="newline"></div>
    <div class="newline"></div>
  </div>
</div>
<div class="left">
  <a id="addCitationLink" href="" onclick="return false;"><@s.text name='metadata.addnew'/> <@s.text name='metadata.heading.citations'/></a>
</div>
<div class="newline"></div>
<div class="newline"></div>
<div class="newline"></div>    
<div class="breakLeftButtons">
  <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
  <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
</div>

</fieldset>
</@s.form>
  