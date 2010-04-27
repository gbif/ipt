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
    <title><@s.text name="metadata.heading.geocoverages"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
    <meta name="heading" content="<@s.text name='eml.geographicCoverage'/>"/>    
    
<script 
  src="<@s.url value="/scripts/swfobject.js"/>" >
  type="text/javascript" 
</script>

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

/**
 * This function is a callback used by the Map object.
 *
 */
function selectBoundigBox(minx,miny,maxx,maxy){
  $("#maxy").val(maxy);
  $("#miny").val(miny);
  $("#minx").val(minx);
  $("#maxx").val(maxx);
}

function ConfigMap(id) {
  var so = new SWFObject("<@s.url value="/scripts/IptGeoCoverageMap.swf"/>", "swf", "690", "250", "9"); 
  so.addParam("allowFullScreen", "false");
  so.addVariable("swf", "");
  var data = "<#if (eml.geographicCoverage.boundingCoordinates.min.x)??>{'minx':${eml.geographicCoverage.boundingCoordinates.min.x},'maxx':${eml.geographicCoverage.boundingCoordinates.max.x},'miny':${eml.geographicCoverage.boundingCoordinates.min.y},'maxy':${eml.geographicCoverage.boundingCoordinates.max.y}}</#if>";
  so.addVariable("data", data);
  so.addVariable("api_key", "${cfg.getGoogleMapsApiKey()}"); 
  so.write(id);
}

function OnModuleLoaded() {
  ConfigMap("map");
}

google.setOnLoadCallback(OnModuleLoaded);

//]]>
</script>
</head>

<div class="break10"></div>

<p class="explMt"><@s.text name='metadata.description.geocoverage'/></p>
<@s.form id="geoForm" action="geocoverage" method="post" validate="false">
<fieldset>
<@s.hidden name="resourceId" value="${resourceId?c}"/>
<@s.hidden name="nextPage" value="taxcoverage"/>
<div id="clone">
  <@s.hidden id="minx" key="eml.geographicCoverage.boundingCoordinates.min.longitude"/>
  <@s.hidden id="maxx" key="eml.geographicCoverage.boundingCoordinates.max.longitude" />
  <@s.hidden id="miny" key="eml.geographicCoverage.boundingCoordinates.min.latitude" />
  <@s.hidden id="maxy" key="eml.geographicCoverage.boundingCoordinates.max.latitude" />
  <div class="newline"></div>
  <div id="map"></div>
  <@s.textarea key="eml.geographicCoverage.description" required="false" 
    cssClass="text xlarge"/>
</div>
</fieldset>
<div class="break">
  <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
  <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
</div>
</@s.form>