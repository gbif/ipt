<#if envelope>
<#include "/WEB-INF/pages/tapir/header.ftl">  
</#if>
<#escape x as x?xml>
<inventory>
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
   <${tags[v_index]}>${v}</${tags[v_index]}>
  </#list>
  </record>
 </#list>
 <summary start="${start}" <#if (limit=values?size)> next="${start+limit+1}"</#if> totalReturned="${values?size}" totalMatched="${totalMatched!-1}"/>
</inventory>
</#escape>
<#if envelope>
<#include "/WEB-INF/pages/tapir/footer.ftl">  
</#if>
