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
  <meta name="heading" content="<@s.text name='eml.basic'/>"/> 
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
var langSelect = null;

/**
 * Loads language asynchronously.
 */
function LoadLangAsync(callback) {
  if (langSelect != null) {
    callback(langSelect);
    return;
  }
  var url = '<@s.url value="/ajax/vocSelect.html"/>';  
  params = {uri:"${languageVocUri}",alpha:true,empty:true};
  $.get(url, params, function(data) { 
    langSelect = $(data);
    callback(langSelect);
  });
}

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
      $('#langDiv').hide();
      $('#langSelect').attr('value', $('#langEnum').attr('value'));
  
  LoadLangAsync(function(elem) {
    var e = elem.clone();
    var id = 'langSelect';
    var idElem = $('#' + id);
    var name = idElem.attr('name');
    var value = idElem.attr('value');
    var ename = e.attr('name');
    var eval = e.attr('val');
    e.attr('name', 'eml.resourceCreator.address.country');
//    e.attr('name', 'eml.language');
    e.attr('value', value)
    e.attr('id', id);
    $('#' + id).replaceWith(e);
  });
  
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
    e.attr('name', 'eml.metadataProvider.address.country');
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
  <@s.select id="langSelect" key="eml.language" list="{'${eml.language!}'}" 
    required="true" cssClass="text xhalf"/>
</div>
    <div id="langDiv">
	  <@s.textfield id="langEnum" key="eml.language"/>
	</div>
<!--
<div class="leftxhalf">
  <@s.select id="type" key="resource.type" list="resourceTypeMap" 
    required="true" cssClass="text xhalf"/>
</div>
<div class="leftxhalf">
  <@s.file id="logo" key="resource.selectLogoFile" name="file"                    
    cssClass="text xhalf"/>
</div>
-->
<div class="newline">
  <@s.textarea id="abstract" key="eml.resource.meta.description" required="true" 
    cssClass="text xlarge"/>
</div>
<div class="newline"></div>
<h2 class="explMt">Resource Creator</h2>
<div id="creator">    
  <div class="newline"></div>
  <div class="leftxhalf">
    <@s.textfield id="firstName" key="eml.resourceCreator.firstName" 
      required="true" cssClass="text xhalf"/>
  </div>
  <div class="leftxhalf">
    <@s.textfield id="lastName" key="eml.resourceCreator.lastName" 
      required="true" cssClass="text xhalf"/>
  </div>
  <div class="newline"></div>
  <div class="leftxhalf">
    <@s.textfield id="position" key="eml.resourceCreator.position"  
      cssClass="text xhalf"/>
  </div>       
  <div class="leftxhalf">
    <@s.textfield id="role" key="eml.resourceCreator.role.name" disabled="true"
      cssClass="text xhalf"/>
  </div>
  <div class="newline"></div>
  <div>
    <@s.textfield id="organization" key="eml.resourceCreator.organisation"  
      cssClass="text xlarge"/>
  </div>
  <div class="newline"></div>
  <div class="leftxhalf">
    <@s.textfield id="phone" key="eml.resourceCreator.phone"  
      cssClass="text xhalf"/>
  </div>
  <div class="leftxhalf">
    <@s.textfield id="email" key="eml.resourceCreator.email"  
      required="true" cssClass="text xhalf"/> 
  </div>
  <div class="newline"></div>
  <div>
    <@s.textfield id="homepage" key="eml.resourceCreator.homepage"  
      cssClass="text xlarge"/>
  </div>
  <div class="newline"></div>
  <div>
    <@s.textfield id="address" key="eml.resourceCreator.address.address"  
      cssClass="text xlarge"/>
  </div>
  <div class="newline"></div>
  <div class="leftxhalf">
    <@s.textfield id="postalCode" key="eml.resourceCreator.address.postalCode"  
      cssClass="text xhalf"/>
  </div>
  <div class="leftxhalf">
    <@s.textfield id="city" key="eml.resourceCreator.address.city"  
      cssClass="text xhalf"/>
  </div>
  <div class="newline"></div>
  <div class="leftxhalf">
    <@s.textfield id="province" key="eml.resourceCreator.address.province"  
      cssClass="text xhalf"/>
  </div>
  <div class="leftxhalf">
    <@s.select id="creatorCountry" key="" list="eml.resourceCreator.address.country"
      required="true" cssClass="text xhalf"/>
  </div>    
</div>
<div class="newline"></div>
<div class="newline"></div>
<div class="newline"></div>
<div class="newline"></div>
<h2 class="explMt">Metadata Provider</h2>
<div id="metadataProvider">    
<div class="newline"></div>
<div class="leftxhalf">
  <@s.textfield id="firstName" key="eml.metadataProvider.firstName" 
    required="true" cssClass="text xhalf"/>
</div>
<div class="leftxhalf">
  <@s.textfield id="lastName" key="eml.metadataProvider.lastName" 
    required="true" cssClass="text xhalf"/>
</div>
<div class="newline"></div>
<div class="leftxhalf">
  <@s.textfield id="position" key="eml.metadataProvider.position"  
    cssClass="text xhalf"/>
</div>       
<div class="leftxhalf">
  <@s.textfield id="role" key="eml.metadataProvider.role.name" disabled="true"
    cssClass="text xhalf"/>
</div>       
<div class="newline"></div>
<div>
  <@s.textfield id="organization" key="eml.metadataProvider.organisation"  
    cssClass="text xlarge"/>
</div>
<div class="newline"></div>
<div class="leftxhalf">
  <@s.textfield id="phone" key="eml.metadataProvider.phone"  
    cssClass="text xhalf"/>
</div>
<div class="leftxhalf">
  <@s.textfield id="email" key="eml.metadataProvider.email"  
    required="true" cssClass="text xhalf"/>
</div>
<div class="newline"></div>
<div>
  <@s.textfield id="homepage" key="eml.metadataProvider.homepage"  
    cssClass="text xlarge"/>
</div>
<div class="newline"></div>
<div>
  <@s.textfield id="address" key="eml.metadataProvider.address.address"  
    cssClass="text xlarge"/>
</div>
<div class="newline"></div>
<div class="leftxhalf">
  <@s.textfield id="postalCode" key="eml.metadataProvider.address.postalCode"  
    cssClass="text xhalf"/>
</div>
<div class="leftxhalf">
  <@s.textfield id="city" key="eml.metadataProvider.address.city"  
    cssClass="text xhalf"/>
</div>
<div class="newline"></div>
<div class="leftxhalf">
  <@s.textfield id="province" key="eml.metadataProvider.address.province"  
    cssClass="text xhalf"/>
</div>
<div class="leftxhalf">
  <@s.select id="metadataProviderCountry" key="" list="eml.resourceCreator.address.country"
    required="true" cssClass="text xhalf"/>
</div>    
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
  