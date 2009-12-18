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
 <dwr:DarwinRecordSet>
   <#list records as rec>
	<#include "/WEB-INF/pages/tapir/model/dwc.ftl">  
   </#list>
 </dwr:DarwinRecordSet>
 <summary start="${start?c}" <#if (next>0)> next="${next?c}"</#if> totalReturned="${records?size?c}"<#if totalMatched??> totalMatched="${totalMatched?c}"</#if>/>
</#escape>
</search>
<#if envelope>
 <#include "/WEB-INF/pages/tapir/footer.ftl">  
</#if>
