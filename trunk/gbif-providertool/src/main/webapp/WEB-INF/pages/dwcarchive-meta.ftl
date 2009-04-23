<starArchive xmlns="http://rs.tdwg.org/dwc/terms/xsd/archive/">
  <core encoding="UTF-8" fieldsTerminatedBy="," linesTerminatedBy="\n" fieldsEnclosedBy='"' ignoreHeaderLines="1" rowType="${rowType}" location="${coreFilename}">
    <id index="0" term="http://purl.org/dc/terms/identifier"/>
    <field index="1" term="http://purl.org/dc/terms/modified"/>
    <field index="2" term="http://purl.org/dc/terms/source"/>
    <field index="3" term="http://ipt.gbif.org/terms/sourceID"/>
   <#assign idx=4>
   <#if isChecklist>
    <#assign idx=11>
    <field index="4" term="http://rs.tdwg.org/dwc/terms/ScientificName"/>
    <field index="5" term="http://rs.tdwg.org/dwc/terms/AcceptedTaxonID"/>
    <field index="6" term="http://rs.tdwg.org/dwc/terms/AcceptedTaxon"/>
    <field index="7" term="http://rs.tdwg.org/dwc/terms/HigherTaxonID"/>
    <field index="8" term="http://rs.tdwg.org/dwc/terms/HigherTaxon"/>
    <field index="9" term="http://rs.tdwg.org/dwc/terms/BasionymID"/>
    <field index="10" term="http://rs.tdwg.org/dwc/terms/Basionym"/>
   </#if>
    <#list coreProperties as p>
    <field index="${p_index+idx}" term="${p.qualName}"/>
    </#list>
  </core>
 <#assign filenames = fileMap?keys>
 <#list filenames as fn>
  <#assign view = fileMap[fn]>
  <extension encoding="UTF-8" fieldsTerminatedBy="," linesTerminatedBy="\n" fieldsEnclosedBy='"' ignoreHeaderLines="1" rowType="${view.extension.rowType}" location="${fn}">
    <coreid index="0" term="http://purl.org/dc/terms/identifier"/>
    <#list view.getMappedProperties() as p>
    <field index="${p_index+1}" term="${p.qualName}"/>
    </#list>
  </extension>
 </#list>
</starArchive>
<#escape x as x?xml>
</#escape>
