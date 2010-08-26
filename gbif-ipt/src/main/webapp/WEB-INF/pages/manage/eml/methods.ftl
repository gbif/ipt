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
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.metadata.methods.title'/></title>
<#include "/WEB-INF/pages/macros/metadata.ftl"/>
<script type="text/javascript">
    $(document).ready(function(){
      
      $("#save").click(function() {
			$("#sampling textarea").attr("id",function() {
				var parts=$(this).attr("id").split(".");var n=parseInt(parts.length)-1;
				return "eml.samplingMethods["+(itemsCount+2)+"]."+parts[n]; });
		    $("#qualitycontrol textarea").attr("id",function() {
				var parts=$(this).attr("id").split(".");var n=parseInt(parts.length)-1;
				return "eml.samplingMethods["+(itemsCount+1)+"]."+parts[n]; });
			$("#sampling textarea").attr("name",function() {return $(this).attr("id"); });
			$("#qualitycontrol textarea").attr("name",function() {return $(this).attr("id"); });
			$("#sampling label").attr("for",function() {
				var parts=$(this).attr("for").split(".");var n=parseInt(parts.length)-1;
				return "eml.samplingMethods["+(itemsCount+2)+"]."+parts[n]; });	
			$("#qualitycontrol label").attr("for",function() {
				var parts=$(this).attr("for").split(".");var n=parseInt(parts.length)-1;
				return "eml.samplingMethods["+(itemsCount+1)+"]."+parts[n]; });	
	  });
     
});
</script>
<#assign sideMenuEml=true /> 
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<h1><@s.text name='manage.metadata.methods.title'/>: <em>${ms.resource.title!ms.resource.shortname}</em></h1>
<@s.text name='manage.metadata.methods.intro'/>
<form class="topForm" action="metadata-${section}.do" method="post"> 
<#assign last=("${eml.samplingMethods.size()}"?number)-1/>
<div id="sampling" class="item">
<@text name="studyExtent" value="${((eml.samplingMethods[last].studyExtent)!)}"  i18nkey="eml.samplingMethods.studyExtent"/>
<@text name="sampleDescription" value="${((eml.samplingMethods[last].sampleDescription)!)}" i18nkey="eml.samplingMethods.sampleDescription"/>
</div>
<div id="qualitycontrol" class="item">
<#if eml.samplingMethods[last].qualityControl?? >
<@text name="qualityControl" value="${(eml.samplingMethods[last].qualityControl)!}" i18nkey="eml.samplingMethods.qualityControl"/>
<#else>
<@text name="qualityControl" value="${(eml.samplingMethods[last-1].qualityControl)!}" i18nkey="eml.samplingMethods.qualityControl"/>
</#if>
<div class="newline"></div>
<div class="horizontal_dotted_line_large_foo" id="separator"></div>
<div class="newline"></div>
</div>
<div id="items">
<#list eml.samplingMethods as item>
<#if eml.samplingMethods[item_index].stepDescription?exists >
<div id="item-${item_index}" class="item">
	<div class="newline"></div>
	<div class="right">
      <a id="removeLink-${item_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.methods.item'/> ]</a>
    </div>
    <div class="newline"></div>
	<@text name="eml.samplingMethods[${item_index}].stepDescription" i18nkey="eml.samplingMethods.stepDescription"/>
  	<div class="newline"></div>
	<div class="horizontal_dotted_line_large_foo" id="separator"></div>
	<div class="newline"></div>
</div>
</#if>
</#list>
</div>
<a id="plus" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.methods.item'/></a></br></br>
    <div class="newline"></div>
    <div class="newline"></div>
<div class="buttons">
  <@s.submit name="save" key="button.save" />
  <@s.submit name="cancel" key="button.cancel" />
</div>
<form>
<div id="baseItem" class="item" style="display:none">
	<div class="newline"></div>
	<div class="right">
      <a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.methods.item'/> ]</a>
    </div>
    <div class="newline"></div>
	<@text name="stepDescription" i18nkey="eml.samplingMethods.stepDescription"/>
  	<div class="newline"></div>
	<div class="horizontal_dotted_line_large_foo" id="separator"></div>
	<div class="newline"></div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">