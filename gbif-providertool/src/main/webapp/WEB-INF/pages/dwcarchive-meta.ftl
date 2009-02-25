<#escape x as x?xml>
<archive xmlns="http://rs.tdwg.org/dwc/terms/xsd/archive/">
 <#assign filenames = fileMap?keys>
 <#list filenames as fn>
  <#assign view = fileMap[fn]>
  <file encoding="UTF-8" fieldsTerminatedBy="\t" linesTerminatedBy="\n" ignoreHeaderLines="1" rowType="${view.extension.type}" location="${fn}">
    <#list view.getMappedProperties() as p>
    <field index="${p_index}" term="${p.uri}"/>
    </#list>
  </file>
 </#list>
	
  <relationships>
    <relationship>
     <#list filenames as fn>
      <file location="${fn}" fieldIndex="0"/>
     </#list>
    </relationship>
  </relationships>	
</archive>
</#escape>
