<#include "/WEB-INF/pages/tapir/header.ftl">  
<#escape x as x?xml>
<inventory>
 <concepts>
 <#list properties as p>
  <concept id="${p.qualName}"/>
 </#list>
 </concepts>
 <#list values as vl>
  <record count="${vl.count!-1}">
  <#list vl.values as v>
   <value>${v}</value>
  </#list>
  </record>
 </#list>
 <summary start="${start}" <#if (limit=values?size)> next="${start+limit+1}"</#if> totalReturned="${values?size}" totalMatched="${totalMatched!-1}"/>
</inventory>
</#escape>
<#include "/WEB-INF/pages/tapir/footer.ftl">  
