<archive xmlns="http://rs.tdwg.org/dwc/terms/xsd/archive/">
  <file encoding="UTF-8" fieldsTerminatedBy="\t" linesTerminatedBy="\n" ignoreHeaderLines="1" rowType="${coreView.extension.rowType}" location="${coreFilename}">
    <field index="0" term="http://purl.org/dc/terms/identifier"/>
    <#list coreView.getMappedProperties() as p>
    <field index="${p_index+1}" term="${p.qualName}"/>
    </#list>
  </file>
 <#assign filenames = fileMap?keys>
 <#list filenames as fn>
  <#assign view = fileMap[fn]>
  <file encoding="UTF-8" fieldsTerminatedBy="\t" linesTerminatedBy="\n" ignoreHeaderLines="1" rowType="${view.extension.rowType}" location="${fn}">
    <field index="0" term="http://purl.org/dc/terms/identifier"/>
    <#list view.getMappedProperties() as p>
    <field index="${p_index+1}" term="${p.qualName}"/>
    </#list>
  </file>
 </#list>
	
  <relationships>
   <#list filenames as fn>
    <relationship>
      <file location="${coreFilename}" fieldIndex="0"/>
      <file location="${fn}" fieldIndex="0"/>
    </relationship>
   </#list>
  </relationships>	
</archive>
<#escape x as x?xml>
</#escape>
