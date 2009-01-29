<?xml version='1.0' encoding='utf-8'?>
<#include "/WEB-INF/pages/inc/globalVars.ftl">  
<#if envelope>
 <#include "/WEB-INF/pages/tapir/header.ftl">  
 <inventory>
<#else>
 <inventory ${nsr.xmlnsDef()}>  
</#if>
<#escape x as x?xml>
 <concepts>
 <#assign properties = inventoryProperties?keys>
 <#assign tags = inventoryProperties?values>
 <#list properties as p>
  <concept id="${p.qualName}"/>
 </#list>
 </concepts>
 <#list values as vl>
  <record count="${vl.count!-1}">
  <#list vl.values as v>
   <${tags[v_index]}<#if v??>>${v}<#else> xsi:nil="true"></#if></${tags[v_index]}>
  </#list>
  </record>
 </#list>
 <summary start="${start?c}" <#if (next>0)> next="${next?c}"</#if> totalReturned="${values?size?c}"<#if totalMatched??> totalMatched="${totalMatched?c}"</#if>/>
</#escape>
</inventory>
<#if envelope>
 <#include "/WEB-INF/pages/tapir/footer.ftl">  
</#if>
