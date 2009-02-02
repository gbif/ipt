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