<#macro input name value="-99999" i18nkey="" errorfield="" type="text" size=-1 disabled=false help="" helpOptions=[] date=false>
  <#if date><div class="calendarInfo"><#else><div></#if>
	<#include "/WEB-INF/pages/macros/form_field_common.ftl">
	<input type="${type}" id="${name}" name="${name}" value="<#if value=="-99999"><@s.property value="${name}"/><#else>${value}</#if>" <#if (size>0)>size="${size}"</#if> <#if disabled>readonly="readonly"</#if>/>
  </div>
</#macro>

<#macro text name value="-99999" i18nkey="" errorfield="" size=40 rows=5 disabled=false help="">
  <div>
	<#include "/WEB-INF/pages/macros/form_field_common.ftl">
	<textarea id="${name}" name="${name}" cols=${size} rows=${rows} <#if disabled>readonly="readonly"</#if>><#if value=="-99999"><@s.property value="${name}"/><#else>${value}</#if></textarea>
  </div>
</#macro> 

<#macro select name options value="" i18nkey="" errorfield="" size=1 disabled=false help="">
  <div>
	<#include "/WEB-INF/pages/macros/form_field_common.ftl">
    <select name="${name}" id="${name}" size="${size}" <#if disabled>readonly="readonly"</#if>>
    <#list options?keys as val>
      <option value="${val}" <#if (value!"")==val> selected="selected"</#if>><@s.text name="${options[val]}"/></option>
    </#list>
	</select>
  </div>
</#macro>
<#macro selectList name options objValue objTitle value="" i18nkey="" errorfield="" size=1 disabled=false help="">
  <div>
	<#include "/WEB-INF/pages/macros/form_field_common.ftl">
	<@s.select id=name name=name list=options listKey=objValue listValue=objTitle value=value size=size disabled=disabled/>
   </div>
</#macro>

<#macro checkbox name i18nkey="" errorfield="" disabled=false value="-99999" help="">
  <div>
	<#include "/WEB-INF/pages/macros/form_field_common.ftl">
	<#if value=="-99999">
	<#assign val><@s.property value="${name}"/></#assign>
	<@s.checkbox key="${name}" id="${name}" disabled=disabled value=val />
	<#else>
	<@s.checkbox key="${name}" id="${name}" disabled=disabled value=value />
	</#if>
  </div>
</#macro> 
<#macro readonly i18nkey value size=-1 help="">
  <div>
	<label><@s.text name="${i18nkey}"/></label>
	<img style="visibility:hidden" src="${baseURL}/images/info.gif" />
	<input type="text" value="${value}" <#if (size>0)>size="${size}"</#if> readonly="readonly" />
  </div>
</#macro>
<#macro label i18nkey help="">
  <div>
	<label><@s.text name="${i18nkey}"/></label>
	<img style="visibility:hidden" src="${baseURL}/images/info.gif" />
	<#nested>
  </div>
</#macro>