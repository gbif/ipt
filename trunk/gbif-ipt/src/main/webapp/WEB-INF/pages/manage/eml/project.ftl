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
<title><@s.text name='manage.metadata.project.title'/></title>
<#assign sideMenuEml=true />
 
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<h1><@s.text name='manage.metadata.project.title'/>: <em>${ms.resource.title!ms.resource.shortname}</em></h1>
<@s.text name='manage.metadata.project.intro'/>
<form class="topForm" action="metadata-${section}.do" method="post"> 
	<@input name="eml.project.title"/>
    <div class="half">
      <@input name="eml.project.personnel.firstName" />
      <@input name="eml.project.personnel.lastName" />
      <@select name="eml.project.personnel.role" value="${(eml.project.personnel.role)!}" options=roleOptions />
    </div>
    <@text name="eml.project.funding" />
	<@text name="eml.project.studyAreaDescription.descriptorValue" />
	<@text name="eml.project.designDescription" />
<div class="buttons">
  <@s.submit name="save" key="button.save" />
  <@s.submit name="cancel" key="button.cancel" />
</div>
</form>

<#include "/WEB-INF/pages/inc/footer.ftl">
