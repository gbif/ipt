<#macro input name keyBase="" type="text" size=40>
  <div>
	<@s.fielderror cssClass="fielderror" fieldName="${name}"/>
	<label for="${name}"><@s.text name="${keyBase}${name}"/></label>
	<input type="${type}" id="${name}" name="${name}" value="<@s.property value="${name}"/>" size="${size}"/>
  </div>
</#macro> 

<#macro select name options value keyBase="" size=1>
  <div>
	<@s.fielderror cssClass="fielderror" fieldName="${name}"/>
	<label for="${name}"><@s.text name="${keyBase}${name}"/></label>
    <select name="${name}" id="${name}" size="${size}">
    <#list options?keys as val>
      <option value="${val}" <#if (value!"")==val> selected="selected"</#if>><@s.text name="${options[val]}"/></option>
    </#list>
	</select>
  </div>
</#macro>
<#macro selectList name options objValue objTitle value keyBase="" size=1>
  <div>
	<@s.fielderror cssClass="fielderror" fieldName="${name}"/>
	<label for="${name}"><@s.text name="${keyBase}${name}"/></label>
	 <@s.select id=name name=name list=options listKey=objValue listValue=objTitle value=value size=size/>
   </div>
</#macro>

<#macro checkbox name keyBase="">
  <div>
	<@s.fielderror cssClass="fielderror" fieldName="${name}"/>
	<label for="${name}"><@s.text name="${keyBase}${name}"/></label>
	<@s.checkbox key="${name}" />
	<#--
	<input type="checkbox" id="${name}" name="${name}" value="true" <#if value>checked="checked"</#if>/>
	-->
  </div>
</#macro>  
