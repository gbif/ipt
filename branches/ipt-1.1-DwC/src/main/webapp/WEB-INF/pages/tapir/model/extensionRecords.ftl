<#if (rec.getExtensions()?size>0)>
<dwr:DarwinExtensions>
<#-- loop through each extension-->
<#list rec.getExtensions() as ext>
  <#-- loop through each record in this extension, creating an extension element for each record -->
  <#list rec.getExtensionRecords(ext) as eRec>
  <${nsr.tagnameQualifiedExtension(ext)}>
    <#-- loop through each property of this record -->
    <#list eRec.properties as p>
	  <${nsr.tagnameQualified(p)}>${(eRec.getPropertyValue(p)!"")?xml}</${nsr.tagnameQualified(p)}>
    </#list>
  </${nsr.tagnameQualifiedExtension(ext)}>
  </#list>
</#list>
</dwr:DarwinExtensions>
</#if>