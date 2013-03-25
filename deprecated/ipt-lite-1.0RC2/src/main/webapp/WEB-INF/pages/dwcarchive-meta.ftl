<#escape x as x?xml>
<archive xmlns="http://rs.tdwg.org/dwc/text/">
  <#--<core encoding="UTF-8" fieldsTerminatedBy="," linesTerminatedBy="\n" fieldsEnclosedBy='"' ignoreHeaderLines="1" rowType="${rowType}">-->
  <core encoding="UTF-8" fieldsTerminatedBy="\t" linesTerminatedBy="\n" fieldsEnclosedBy='' ignoreHeaderLines="1" rowType="${rowType}">
    <files>
      <location>${coreFilename}</location>
    </files>
    <id index="0" term="http://rs.tdwg.org/dwc/terms/${guidPropertyName}"/>
    <field index="1" term="http://purl.org/dc/terms/source"/>
    <#list coreProperties as p>
    <field index="${p_index+2}" term="${p.qualName}"/>
    </#list>
  </core>
 <#assign filenames = fileMap?keys>
 <#list filenames as fn>
  <#assign view = fileMap[fn]>
  <extension encoding="UTF-8" fieldsTerminatedBy="\t" linesTerminatedBy="\n" fieldsEnclosedBy='' ignoreHeaderLines="1" rowType="${view.extension.rowType}">
    <files>
      <location>${fn}</location>
    </files>
    <coreid index="0" />
    <#list view.getMappedProperties() as p>
    <field index="${p_index+1}" term="${p.qualName}"/>
    </#list>
  </extension>
 </#list>
</archive>
</#escape>
