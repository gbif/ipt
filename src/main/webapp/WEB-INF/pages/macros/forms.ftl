<#macro input name value="-99999" i18nkey="" errorfield="" type="text" size=-1 disabled=false help="" helpOptions=[] date=false requiredField=false maxlength=-1>
  <#if date><div class="calendarInfo"><#else><div></#if>
	<#include "/WEB-INF/pages/macros/form_field_common.ftl">
	<input type="${type}" id="${name}" name="${name}" value="<#if value=="-99999"><@s.property value="${name}"/><#else>${value}</#if>" <#if (size>0)>size="${size}"</#if> <#if (maxlength>0)>maxlength="${maxlength}"</#if> <#if disabled>readonly="readonly"</#if>/>
  </div>
</#macro>

<#macro text name value="-99999" i18nkey="" errorfield="" size=40 rows=5 disabled=false help="" requiredField=false maxlength=-1>
  <div>
	<#include "/WEB-INF/pages/macros/form_field_common.ftl">
	<textarea id="${name}" name="${name}" cols="${size}" rows="${rows}" <#if (maxlength>0)>maxlength="${maxlength}"</#if><#if disabled>readonly="readonly"</#if>><#if value=="-99999"><@s.property value="${name}"/><#else>${value}</#if></textarea>
  </div>
</#macro>

<#macro textinline name value="-99999" i18nkey="" errorfield="" help="">
	<div class="textinline">
		<#include "/WEB-INF/pages/macros/help_icon.ftl">
		<h2 class="headerLine"><span><@s.text name="${name}"/></span></h2>
	</div>
</#macro>

<#macro link name value="" href="" class="" i18nkey="" help="" errorfield="">
	<div>
    	<#include "/WEB-INF/pages/macros/help_icon.ftl">
    	<a id="${name}" name="${name}" class="${class}" href="${href}"><@s.text name="${value}"/></a>
    </div>
</#macro>

<#macro select name options value="" i18nkey="" errorfield="" size=1 disabled=false help="" includeEmpty=false javaGetter=true requiredField=false>
  <div>
	<#include "/WEB-INF/pages/macros/form_field_common.ftl">
    <select name="${name}" id="${name}" size="${size}" <#if disabled>readonly="readonly"</#if>>
	<#if includeEmpty>
      <option value="" <#if (value!"")==""> selected="selected"</#if>></option>
	</#if>
    <#list options?keys as val>
      <option value="${val}" <#if (value!"")==""+val> selected="selected"</#if>>
	  <#if javaGetter><@s.text name="${options.get(val)}"/><#else><@s.text name="${options[val]}"/></#if>
	  </option>
    </#list>
	</select>
  </div>
</#macro>

<#macro selectList name options objValue objTitle value="" i18nkey="" errorfield="" size=1 disabled=false help="" includeEmpty=false requiredField=false>
  <div>
	<#include "/WEB-INF/pages/macros/form_field_common.ftl">
	<@s.select id=name name=name list=options listKey=objValue listValue=objTitle value=value size=size disabled=disabled emptyOption=includeEmpty/>
   </div>
</#macro>

<#macro checkbox name i18nkey="" errorfield="" disabled=false value="-99999" help="" requiredField=false>
  <div class="checkbox">
	<div><#include "/WEB-INF/pages/macros/form_field_common.ftl"></div>
	<#if value=="-99999">
	<#assign val><@s.property value="${name}"/></#assign>
	<@s.checkbox key="${name}" id="${name}" disabled=disabled value=val />
	<#else>
	<@s.checkbox key="${name}" id="${name}" disabled=disabled value=value />
	</#if>
  </div>
</#macro>

<#macro readonly name i18nkey value size=-1 help="" errorfield="" requiredField=false>
	<#include "/WEB-INF/pages/macros/form_field_common.ftl">
	<input type="text" value="${value}" <#if (size>0)>size="${size}"</#if> readonly="readonly" />
</#macro>

<#macro label i18nkey help="" requiredField=false>
  <div>
	<label><@s.text name="${i18nkey}"/><#if requiredField>&#42;</#if></label>
	  <img style="visibility:hidden" src="${baseURL}/images/info.gif" />
	  <#nested>
  </div>
</#macro>

<#--
  In the textWithFormattedLink macro, each word in view source will appear on a separate line. This fix this issue
  http://code.google.com/p/gbif-providertoolkit/issues/detail?id=856 the <#t> trim directive is used to ensure that
  the word list gets displayed as a complete line instead.
-->
<#macro textWithFormattedLink text>
  <#-- replace less than and greater than characters -->
  <#assign sanitized = text?replace("<", "&lt;")?replace(">", "&gt;")>
  <#assign words = sanitized?word_list>
  <#list words as x>
    <#assign res = x?matches("(http(s)?|ftp)://(([\\w-]+\\.)?)+[\\w-]+(:\\d+)?+(/[\\w- ./-?%&=]*)?")>
    <#assign flag=false>
    <#list res as m>
      <#if x?contains(m)><a href="${m}">${x}</a> <#t><#assign flag = true><#break></#if>
    </#list>
    <#if flag==false>${x}</#if> <#t>
  </#list>
</#macro>

<#macro showMore text maxLength>
    <#if (text?length>maxLength)>
    	<div id="visibleContent"><@textWithFormattedLink (text)?substring(0,maxLength)/>... <a id="showMore" href=""><@s.text name='basic.showMore'/></a></div>
    	<div id="hiddenContent" style="display: none"><@textWithFormattedLink text/><a id="showLess" href=""><@s.text name='basic.showLess'/></a></div>
    <#else>
    	<@textWithFormattedLink text/>
    </#if>
</#macro>

