<#include "/WEB-INF/pages/tapir/header.ftl">  
<#assign core=resource.coreMapping>
<#assign declareNamespace=false>
<#escape x as x?xml>
<search>
 <records>
   <#list records as dwc>
	<#include "/WEB-INF/pages/tapir/model/dwc.ftl">  
   </#list>
 </records>
</search>
</#escape>
<#include "/WEB-INF/pages/tapir/footer.ftl">  
