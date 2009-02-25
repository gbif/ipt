<#escape x as x?xml>
${cfg.getArchiveUrl(resource.guid)}
<archive xmlns="http://rs.tdwg.org/dwc/terms/xsd/archive/">
  <#assign files = fileMap?keys>
  <#list files as f>
  <file rowType="http://rs.tdwg.org/dwc/terms/Sample" location="${f.getName()}">
    <#assign view = fileMap[f]>
    <#list view.getMappedProperties() as p>
    <field index="${p_index}" term="${p.uri}"/>
    </#list>
  </file>
  </#list>
	
  <relationships>
    <relationship>
	  <#list files as f>
      <file location="${f.getName()}" fieldIndex="0"/>
      </#list>
    </relationship>
  </relationships>	
</archive>
</#escape>
