<record guid="${rec.core.guid}"<#if declareNamespace==true> ${nsr.xmlnsDef()}</#if>>
<#list core.extension.properties as p>
<#if core.hasMappedProperty(p)>
  <${nsr.tagnameQualified(p)}>${(rec.core.getPropertyValue(p)!"")?xml}</${nsr.tagnameQualified(p)}>
</#if>
</#list>
<#-- loop through each extension-->
<#include "/WEB-INF/pages/tapir/model/extensionRecords.ftl">
</record>