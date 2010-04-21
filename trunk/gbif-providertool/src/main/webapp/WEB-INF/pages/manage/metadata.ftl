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
  <meta name="heading" content="<@s.text name='metadata.heading'/>"/> 
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

var countriesSelect = null;

function LoadCountriesAsync(callback) {
  if (countriesSelect != null) {
    callback(countriesSelect);
    return;
  }
  var url = '<@s.url value="/ajax/vocSelect.html"/>';
  params = {uri:"${countryVocUri}",alpha:true,empty:true};
  $.get(url, params, function(data) { 
    countriesSelect = $(data);
    callback(countriesSelect);
  });
}

function OnLoad() {  
  LoadCountriesAsync(function(elem) {
    var e = elem.clone();
    var id = 'creatorCountry';
    var idElem = $('#' + id);
    var name = idElem.attr('name');
    var value = idElem.attr('value');
    e.attr('name', 'eml.resourceCreator.address.country');
    e.attr('value', value)
    e.attr('id', id);
    $('#' + id).replaceWith(e);
    
    e = elem.clone();
    id = 'metadataProviderCountry';
    idElem = $('#' + id);
    name = idElem.attr('name');
    value = idElem.attr('value');
    e.attr('name', 'eml.resourceCreator.address.country');
    e.attr('value', value)
    e.attr('id', id);
    $('#' + id).replaceWith(e);
  });
}

google.setOnLoadCallback(OnLoad);

//]]>

</script>  
</head>
    
<p class="explMt"><@s.text name='metadata.describe'/></p>
       
<@s.form id="resourceForm" action="saveResource" enctype="multipart/form-data" 
  method="post">
  
<fieldset>
<@s.hidden name="resourceId" value="${(resource.id)!}"/>
<@s.hidden name="resourceType" value="${(resourceType)!}"/>
<@s.hidden name="guid" value="${(resource.guid)!}"/>
<@s.hidden name="nextPage" value="organisation"/>
<@s.hidden name="method" value="associatedParties"/>

<div class="newline"></div>
<div class="leftxhalf">
  <@s.textfield id="title" key="eml.resource.meta.title" required="true" 
    cssClass="text xhalf"/>
</div>
<div class="leftxhalf">
  <@s.select id="type" key="resource.type" list="resourceTypeMap" 
    required="true" cssClass="text xhalf"/>
</div>
<div class="leftxhalf">
  <@s.file id="logo" key="resource.selectLogoFile" name="file"                    
    cssClass="text xhalf"/>
</div>
<div class="newline">
  <@s.textarea id="abstract" key="eml.resource.meta.description" required="true" 
    cssClass="text xlarge"/>
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
  