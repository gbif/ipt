<#if (prop.vocabulary??)>
    <@s.submit cssClass="button" key="button.termMapping" method="termMapping" theme="simple" onclick="return confirmTermMapping('${prop.id}')"/>
    or select a static value:
	<@s.select key="view.propertyMappings[${prop.id}].value" list="voc" 
		style="display: inline" headerKey="" emptyOption="true" theme="simple"/>						
<#else>
    <@s.textfield  name="view.propertyMappings[${prop.id}].value" value="" cssClass="large" theme="simple"/>  
</#if>