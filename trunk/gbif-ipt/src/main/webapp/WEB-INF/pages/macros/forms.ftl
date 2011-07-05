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

<#macro link name value="" href="" class="" i18nkey="" help="" errorfield="">
	<div>   
    	<#include "/WEB-INF/pages/macros/help_icon.ftl">
    	<a id="${name}" name="${name}" class="${class}" href="${href}"><@s.text name="${value}"/></a>
    </div>
</#macro>

<#macro select name options value="" i18nkey="" errorfield="" size=1 disabled=false help="" includeEmpty=false javaGetter=true>
  <div>
	<#include "/WEB-INF/pages/macros/form_field_common.ftl">
    <select name="${name}" id="${name}" size="${size}" <#if disabled>readonly="readonly"</#if>>
	<#if includeEmpty>
      <option value="" <#if (value!"")==""> selected="selected"</#if>></option>
	</#if>
    <#list options?keys as val>
      <option value="${val}" <#if (value!"")==val> selected="selected"</#if>>
	  <#if javaGetter><@s.text name="${options.get(val)}"/><#else><@s.text name="${options[val]}"/></#if>
	  </option>
    </#list>
	</select>
  </div>
</#macro>

<#macro selectList name options objValue objTitle value="" i18nkey="" errorfield="" size=1 disabled=false help="" includeEmpty=false>
  <div>
	<#include "/WEB-INF/pages/macros/form_field_common.ftl">
	<@s.select id=name name=name list=options listKey=objValue listValue=objTitle value=value size=size disabled=disabled emptyOption=includeEmpty/>
   </div>
</#macro>

<#macro checkbox name i18nkey="" errorfield="" disabled=false value="-99999" help="">
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

<#macro readonly name i18nkey value size=-1 help="" errorfield="">
	<#include "/WEB-INF/pages/macros/form_field_common.ftl">
	<input type="text" value="${value}" <#if (size>0)>size="${size}"</#if> readonly="readonly" />
</#macro>

<#macro label i18nkey help="">
  <div>
	<label><@s.text name="${i18nkey}"/></label>
	<img style="visibility:hidden" src="${baseURL}/images/info.gif" />
	<#nested>
  </div>
</#macro>

<#macro textWithFormattedLink text>
        <#assign words = text?word_list>              
        <#assign res = text?matches("(http(s)?|ftp)://(([\\w-]+\\.)?)+[\\w-]+(/[\\w- ./-?%&=]*)?")>
        <#list words as x> 
            <#assign flag=false>
            <#list res as m>
              <#if x?contains(m)><a href="${m}">${x}</a><#assign flag = true><#break></#if>
            </#list>
            <#if flag==false>${x}</#if>
        </#list>          
</#macro>

<#macro showMore text maxLength>
    <#if (text?length>maxLength)>  
    	<div id= "visibleContent"><@textWithFormattedLink (text)?substring(0,maxLength)/>... <a id="showMore" href=""><@s.text name='basic.showMore'/></a></div>
    	<div id="hiddenContent" style="display: none"><@textWithFormattedLink text/><a id="showLess" href=""><@s.text name='basic.showLess'/></a></div>
    <#else>
    	<@textWithFormattedLink text/>
    </#if>
</#macro>

	