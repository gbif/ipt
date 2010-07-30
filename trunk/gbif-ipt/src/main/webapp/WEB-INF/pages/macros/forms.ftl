<#macro input name value="-99999" keyBase="" type="text" size=40 disabled=false errorField="-99999">
	<#if errorField=="-99999">
	 <#assign errorField=name/>
	</#if>
  <div>
	<@s.fielderror cssClass="fielderror" fieldName="${errorField}"/>
	<label for="${name}"><@s.text name="${keyBase}${name}"/></label>
	<input type="${type}" id="${name}" name="${name}" value="<#if value=="-99999"><@s.property value="${name}"/><#else>${value}</#if>" size="${size}" <#if disabled>readonly="readonly"</#if>/>
  </div>
</#macro> 

<#macro text name keyBase="" size=40 rows=5 disabled=false>
  <div>
	<@s.fielderror cssClass="fielderror" fieldName="${name}"/>
	<label for="${name}"><@s.text name="${keyBase}${name}"/></label>
	<textarea id="${name}" name="${name}" cols=size rows=rows <#if disabled>readonly="readonly"</#if>><@s.property value="${name}"/></textarea>
  </div>
</#macro> 

<#macro select name options value="" keyBase="" size=1 disabled=false>
  <div>
	<@s.fielderror cssClass="fielderror" fieldName="${name}"/>
	<label for="${name}"><@s.text name="${keyBase}${name}"/></label>
    <select name="${name}" id="${name}" size="${size}" <#if disabled>readonly="readonly"</#if>>
    <#list options?keys as val>
      <option value="${val}" <#if (value!"")==val> selected="selected"</#if>><@s.text name="${options[val]}"/></option>
    </#list>
	</select>
  </div>
</#macro>
<#macro selectList name options objValue objTitle value="" keyBase="" size=1 disabled=false>
  <div>
	<@s.fielderror cssClass="fielderror" fieldName="${name}"/>
	<label for="${name}"><@s.text name="${keyBase}${name}"/></label>
	 <@s.select id=name name=name list=options listKey=objValue listValue=objTitle value=value size=size disabled=disabled/>
   </div>
</#macro>

<#macro checkbox name keyBase="" disabled=false>
  <div>
	<@s.fielderror cssClass="fielderror" fieldName="${name}"/>
	<label for="${name}"><@s.text name="${keyBase}${name}"/></label>
	<@s.checkbox key="${name}" disabled=disabled/>
<#--	<input type="checkbox" id="${name}" name="${name}" value="true" <#if value>checked="checked"</#if>/> -->
  </div>
</#macro>  
