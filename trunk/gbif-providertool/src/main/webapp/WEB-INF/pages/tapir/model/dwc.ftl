<record guid="${rec.core.guid}" <#if declareNamespace==true>${nsr.xmlnsDef()}</#if>>
<#list core.extension.properties as p>
<#if core.hasMappedProperty(p)>
  <${nsr.tagnameQualified(p)}>${(rec.core.getPropertyValue(p)!"")?xml}</${nsr.tagnameQualified(p)}>
</#if>
</#list>
<#-- loop through each extension-->
<#list rec.getExtensions() as ext>
  <extension name="${ext.name}" xmlns:x="${ext.name}">
  <#list rec.getExtensionRecords(ext) as eRec>
	<xrecord>
	  <#list eRec.properties as p>
	    <${nsr.tagnameQualified(p)}>${(eRec.getPropertyValue(p)!"")?xml}</${nsr.tagnameQualified(p)}>
	  </#list>
	</xrecord>
  </#list>
  </extension>
</#list>
</record>