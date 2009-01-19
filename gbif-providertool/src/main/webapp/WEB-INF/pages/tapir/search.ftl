<#if envelope>
<#include "/WEB-INF/pages/tapir/header.ftl">  
</#if>
<#assign core=resource.coreMapping>
<#assign declareNamespace=false>
<#escape x as x?xml>
<search>
 <records>
   <#list records as rec>
	<#include "/WEB-INF/pages/tapir/model/dwc.ftl">  
   </#list>
 </records>
</search>
</#escape>
<#if envelope>
<#include "/WEB-INF/pages/tapir/footer.ftl">  
</#if>
