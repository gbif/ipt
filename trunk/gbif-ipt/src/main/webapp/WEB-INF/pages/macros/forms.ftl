<#macro input name i8nkey="null" value="-99999" keyBase="" type="text" size=-1 disabled=false errorField="-99999" help="" helpOptions=[]>
	<#if errorField=="-99999">
	 <#assign efield=name />
	<#else>
     <#assign efield=errorField />
	</#if>
	<#if i8nkey="null" >
	 <#assign i8nkey="${keyBase}${name}" />
	</#if>
	
  <div>
	<@s.fielderror cssClass="fielderror" fieldName="${efield}"/>
	<label for="${name}"><@s.text name="${i8nkey}"/></label>
	<#include "/WEB-INF/pages/macros/help_inc.ftl">
	<input type="${type}" id="${name}" name="${name}" value="<#if value=="-99999"><@s.property value="${name}"/><#else>${value}</#if>" <#if (size>0)>size="${size}"</#if> <#if disabled>readonly="readonly"</#if>/>
  </div>
</#macro> 

<#macro text name keyBase="" size=40 rows=5 disabled=false help="">
  <div>
	<@s.fielderror cssClass="fielderror" fieldName="${name}"/>
	<label for="${name}"><@s.text name="${keyBase}${name}"/></label>
	<#include "/WEB-INF/pages/macros/help_inc.ftl">
	<textarea id="${name}" name="${name}" cols=${size} rows=${rows} <#if disabled>readonly="readonly"</#if>><@s.property value="${name}"/></textarea>
  </div>
</#macro> 

<#macro select name options value="" keyBase="" size=1 disabled=false help="">
  <div>
	<@s.fielderror cssClass="fielderror" fieldName="${name}"/>
	<label for="${name}"><@s.text name="${keyBase}${name}"/></label>
	<#include "/WEB-INF/pages/macros/help_inc.ftl">
    <select name="${name}" id="${name}" size="${size}" <#if disabled>readonly="readonly"</#if>>
    <#list options?keys as val>
      <option value="${val}" <#if (value!"")==val> selected="selected"</#if>><@s.text name="${options[val]}"/></option>
    </#list>
	</select>
  </div>
</#macro>
<#macro selectList name options objValue objTitle value="" keyBase="" size=1 disabled=false help="">
  <div>
	<@s.fielderror cssClass="fielderror" fieldName="${name}"/>
	<label for="${name}"><@s.text name="${keyBase}${name}"/></label>
	<#include "/WEB-INF/pages/macros/help_inc.ftl">
	<@s.select id=name name=name list=options listKey=objValue listValue=objTitle value=value size=size disabled=disabled/>
   </div>
</#macro>

<#macro checkbox name keyBase="" disabled=false value=false help="">
  <div>
	<@s.fielderror cssClass="fielderror" fieldName="${name}"/>
	<label for="${name}"><@s.text name="${keyBase}${name}"/></label>
	<#include "/WEB-INF/pages/macros/help_inc.ftl">
	<@s.checkbox key="${name}" id="${name}" disabled=disabled value=value/>
  </div>
</#macro>  