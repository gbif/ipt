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
<#assign sideMenuEml=true />
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<h1><@s.text name='manage.metadata.methods.title'/>: <em>${resource.title!resource.shortname}</em></h1>
<@s.text name='manage.metadata.methods.intro'/>
<form class="topForm" action="metadata-${section}.do" method="post">
	<div id="sampling" >
		<@text name="eml.studyExtent"  i18nkey="eml.studyExtent"/>
		<@text name="eml.sampleDescription" i18nkey="eml.sampleDescription"/>
	</div>
	<div id="qualitycontrol" >
		<@text name="eml.qualityControl" i18nkey="eml.qualityControl"/>
		<div class="newline"></div>
		<div class="horizontal_dotted_line_large_foo" id="separator"></div>
		<div class="newline"></div>
	</div>
	<div id="items">
		<#if eml.methodSteps??>
			<#list eml.methodSteps as item>
				<div id="item-${item_index}" class="item">
					<div class="newline"></div>
					<div class="right">
				      <a id="removeLink-${item_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.methods.item'/> ]</a>
				    </div>
				    <div class="newline"></div>
					<@text name="eml.methodSteps[${item_index}]" i18nkey="eml.methodSteps"/>
				  	<div class="newline"></div>
					<div class="horizontal_dotted_line_large_foo" id="separator"></div>
					<div class="newline"></div>
				</div>
			</#list>
		</#if>
	</div>
	<a id="plus" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.methods.item'/></a></br></br>
	    <div class="newline"></div>
	    <div class="newline"></div>
	<div class="buttons">
		<@s.submit name="save" key="button.save" />
		<@s.submit name="cancel" key="button.cancel" />
	</div>
	<!-- internal parameter -->
	<input name="r" type="hidden" value="${resource.shortname}" />
<form>
<div id="baseItem" class="item" style="display:none">
	<div class="newline"></div>
	<div class="right">
		<a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.methods.item'/> ]</a>
	</div>
	<div class="newline"></div>
	<@text name="" i18nkey="eml.methodSteps"/>
	<div class="newline"></div>
	<div class="horizontal_dotted_line_large_foo" id="separator"></div>
	<div class="newline"></div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">