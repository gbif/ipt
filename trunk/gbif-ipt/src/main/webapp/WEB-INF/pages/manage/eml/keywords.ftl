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
<title><@s.text name='manage.metadata.keywords.title'/></title>
<#include "/WEB-INF/pages/macros/metadata.ftl"/>
<#assign sideMenuEml=true /> 
<script type="text/javascript">
$(document).ready(function(){
	initHelp();
});   
</script>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<h1><@s.text name='manage.metadata.keywords.title'/>: <em>${ms.resource.title!ms.resource.shortname}</em></h1>
<@s.text name='manage.metadata.keywords.intro'/>
<form class="topForm" action="metadata-${section}.do" method="post"> 

<div id="items">
<#list eml.keywords as item>
	<div id="item-${item_index}" class="item">
	<div class="newline"></div>
	<div class="right">
      <a id="removeLink-${item_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.keywords.item'/> ]</a>
    </div>
    <div class="newline"></div>
	<@input name="eml.keywords[${item_index}].keywordThesaurus" i18nkey="eml.keywords.keywordThesaurus" help="i18n"/>
	<@text name="eml.keywords[${item_index}].keywordsString" i18nkey="eml.keywords.keywordsString" help="i18n"/>
  	<div class="newline"></div>
	<div class="horizontal_dotted_line_large_foo" id="separator"></div>
	<div class="newline"></div>
  	</div>
</#list>
</div>
  <a id="plus" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.keywords.item'/></a>
  <div class="newline"></div>
  <div class="newline"></div>
<div class="buttons">
  <@s.submit name="save" key="button.save" />
  <@s.submit name="cancel" key="button.cancel" />
</div>
</form>
<div id="baseItem" class="item" style="display:none;">
	<div class="newline"></div>
	<div class="right">
      <a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.keywords.item'/> ]</a>
    </div>
	<@input name="keywordThesaurus" i18nkey="eml.keywords.keywordThesaurus" help="i18n"/>
	<@text name="keywordsString" i18nkey="eml.keywords.keywordsString" help="i18n"/>
  	<div class="newline"></div>
	<div class="horizontal_dotted_line_large_foo" id="separator"></div>
	<div class="newline"></div>
</div>
<#include "/WEB-INF/pages/inc/footer.ftl">