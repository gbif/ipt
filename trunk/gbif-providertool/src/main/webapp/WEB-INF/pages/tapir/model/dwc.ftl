<record guid="${dwc.guid}" <#if declareNamespace==true>${nsr.xmlnsDef()}</#if>>
<#list core.extension.properties as p>
<#if core.hasMappedProperty(p)>
  <${nsr.tagnameQualified(p)}>${(dwc.getPropertyValue(p)!"")?xml}</${nsr.tagnameQualified(p)}>
</#if>
</#list>
<#-- loop through each extension-->
<#list extWrapper.getExtensions() as ext>
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