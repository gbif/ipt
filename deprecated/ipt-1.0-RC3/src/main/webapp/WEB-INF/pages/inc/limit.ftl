<#function limit x>
  <#if (x?length>40)>
	  <#return x?substring(0, 36)+" ...">
  <#else>
	  <#return x>
  </#if>
</#function>