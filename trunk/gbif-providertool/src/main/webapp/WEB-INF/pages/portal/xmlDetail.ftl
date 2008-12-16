<?xml version='1.0' encoding='utf-8'?>
<record guid="${record.guid}" xmlns="http://ipt.gbif.org" ${nsr.xmlnsDef()}>
<#assign core=taxon.resource.coreMapping>
<#list core.extension.properties as p>
<#if core.hasMappedProperty(p)>
  <${nsr.tagnameQualified(p)}>${(record.getPropertyValue(p)!"")?xml}</${nsr.tagnameQualified(p)}>
</#if>
</#list>
<#-- loop through each extension-->
<#list extensions as ext>
  <extension name="${ext.name}" xmlns:x="${ext.name}">
  <#list extWrapper.getExtensionRecords(ext) as eRec>
	<xrecord>
	  <#list eRec.properties as p>
	    <${nsr.tagnameQualified(p)}>${(eRec.getPropertyValue(p)!"")?xml}</${nsr.tagnameQualified(p)}>
	  </#list>
	</xrecord>
  </#list>
  </extension>
</#list>
</record>
