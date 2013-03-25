<#function limit x>
  <#if (x?length>50)>
	  <#return x?substring(0, 46)+" ...">
  <#else>
	  <#return x>
  </#if>
</#function>