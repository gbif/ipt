<#escape x as x?xml>
<archive xmlns="http://rs.tdwg.org/dwc/text/">
  <core encoding="UTF-8" fieldsTerminatedBy="," linesTerminatedBy="\n" fieldsEnclosedBy='"' ignoreHeaderLines="1" rowType="${rowType}">
    <files>
      <location>${coreFilename}</location>
    </files>
    <id index="0" />
    <field index="1" term="http://purl.org/dc/terms/modified"/>
    <field index="2" term="http://purl.org/dc/terms/source"/>
    <field index="3" term="http://ipt.gbif.org/terms/sourceID"/>
   <#assign idx=4>
   <#if isChecklist>
    <#assign idx=11>
    <field index="4" term="http://rs.tdwg.org/dwc/terms/scientificName"/>
    <field index="5" term="http://rs.tdwg.org/dwc/terms/acceptedNameUsageID"/>
    <field index="6" term="http://rs.tdwg.org/dwc/terms/acceptedNameUsage"/>
    <field index="7" term="http://rs.tdwg.org/dwc/terms/parentNameUsageID"/>
    <field index="8" term="http://rs.tdwg.org/dwc/terms/parentNameUsage"/>
    <field index="9" term="http://rs.tdwg.org/dwc/terms/originalNameUsageID"/>
    <field index="10" term="http://rs.tdwg.org/dwc/terms/originalNameUsage"/>
   </#if>
    <#list coreProperties as p>
    <field index="${p_index+idx}" term="${p.qualName}"/>
    </#list>
  </core>
 <#assign filenames = fileMap?keys>
 <#list filenames as fn>
  <#assign view = fileMap[fn]>
  <extension encoding="UTF-8" fieldsTerminatedBy="," linesTerminatedBy="\n" fieldsEnclosedBy='"' ignoreHeaderLines="1" rowType="${view.extension.rowType}">
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