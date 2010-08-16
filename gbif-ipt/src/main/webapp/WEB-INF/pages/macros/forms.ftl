<#macro input name value="-99999" i18nkey="" type="text" size=-1 disabled=false help="" helpOptions=[]>
  <div>
	<label for="${name}">
	<#if i18nkey=="">
		<@s.text name="${name}"/></label>
		<@s.fielderror cssClass="fielderror" fieldName="${name}"/>
	<#else>
		<@s.text name="${i18nkey}"/></label>
		<@s.fielderror cssClass="fielderror" fieldName="${i18nkey}"/>
	</#if>
	<#include "/web-inf/pages/macros/help_inc.ftl">
	<input type="${type}" id="${name}" name="${name}" value="<#if value=="-99999"><@s.property value="${name}"/><#else>${value}</#if>" <#if (size>0)>size="${size}"</#if> <#if disabled>readonly="readonly"</#if>/>
  </div>
</#macro> 

<#macro text name i18nkey="" size=40 rows=5 disabled=false help="">
  <div>
	<label for="${name}">
	<#if i18nkey=="">
		<@s.text name="${name}"/></label>
		<@s.fielderror cssClass="fielderror" fieldName="${name}"/>
	<#else>
		<@s.text name="${i18nkey}"/></label>
		<@s.fielderror cssClass="fielderror" fieldName="${i18nkey}"/>
	</#if>
	<#include "/WEB-INF/pages/macros/help_inc.ftl">
	<textarea id="${name}" name="${name}" cols=${size} rows=${rows} <#if disabled>readonly="readonly"</#if>><@s.property value="${name}"/></textarea>
  </div>
</#macro> 

<#macro select name options value="" i18nkey="" size=1 disabled=false help="">
  <div>
	<label for="${name}">
	<#if i18nkey=="">
		<@s.text name="${name}"/></label>
		<@s.fielderror cssClass="fielderror" fieldName="${name}"/>
	<#else>
		<@s.text name="${i18nkey}"/></label>
		<@s.fielderror cssClass="fielderror" fieldName="${i18nkey}"/>
	</#if>
	<#include "/WEB-INF/pages/macros/help_inc.ftl">
    <select name="${name}" id="${name}" size="${size}" <#if disabled>readonly="readonly"</#if>>
    <#list options?keys as val>
      <option value="${val}" <#if (value!"")==val> selected="selected"</#if>><@s.text name="${options[val]}"/></option>
    </#list>
	</select>
  </div>
</#macro>
<#macro selectList name options objValue objTitle value="" i18nkey="" size=1 disabled=false help="">
  <div>
	<label for="${name}">
	<#if i18nkey=="">
		<@s.text name="${name}"/></label>
		<@s.fielderror cssClass="fielderror" fieldName=name/>
	<#else>
		<@s.text name="${i18nkey}"/></label>
		<@s.fielderror cssClass="fielderror" fieldName="${i18nkey}"/>
	</#if>
	<#include "/WEB-INF/pages/macros/help_inc.ftl">
	<@s.select id=name name=name list=options listKey=objValue listValue=objTitle value=value size=size disabled=disabled/>
   </div>
</#macro>

<#macro checkbox name i18nkey="" disabled=false value=false help="">
  <div>
	<label for="${name}">
	<#if i18nkey=="">
		<@s.text name="${name}"/></label>
		<@s.fielderror cssClass="fielderror" fieldName="${name}"/>
	<#else>
		<@s.text name="${i18nkey}"/></label>
		<@s.fielderror cssClass="fielderror" fieldName="${i18nkey}"/>
	</#if>
	<#include "/WEB-INF/pages/macros/help_inc.ftl">
	<@s.checkbox key="${name}" id="${name}" disabled=disabled value=value/>
  </div>
</#macro>  