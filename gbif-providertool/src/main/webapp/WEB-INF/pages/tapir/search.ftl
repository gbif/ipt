<?xml version='1.0' encoding='utf-8'?>
<#include "/WEB-INF/pages/inc/globalVars.ftl">  
<#assign core=resource.coreMapping>
<#assign declareNamespace=false>
<#if envelope>
 <#include "/WEB-INF/pages/tapir/header.ftl">
<search>
<#else>
<search ${nsr.xmlnsDef()}>  
</#if>
<#escape x as x?xml>
 <records>
   <#list records as rec>
	<#include "/WEB-INF/pages/tapir/model/dwc.ftl">  
   </#list>
 </records>
 <summary start="${start}" <#if (limit=records?size)> next="${start+limit+1}"</#if> totalReturned="${records?size}"<#if totalMatched??> totalMatched="${totalMatched}"</#if>/>
</#escape>
</search>
<#if envelope>
 <#include "/WEB-INF/pages/tapir/footer.ftl">  
</#if>
