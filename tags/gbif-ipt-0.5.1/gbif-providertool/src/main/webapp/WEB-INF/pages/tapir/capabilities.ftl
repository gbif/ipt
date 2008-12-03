<#assign page=JspTaglibs["http://www.opensymphony.com/sitemesh/page"]>
<@page.applyDecorator name="tapir" title="ping">
<@page.param name="tapir.content">
<#escape x as x?xml>
<capabilities>
<operations>
  <ping/>
  <metadata/>
  <capabilities/>
  <inventory>
    <anyConcepts/>
  </inventory>
  <search>
    <templates>
      <template location="http://example.net/tmpl/search_by_taxonomy.xml"/>
      <template location="http://example.net/tmpl/search_by_geography.xml" alias="geo"/>
    </templates>
  </search>
</operations>
<concepts>
 <schema>
  <#list getAllMappings() map>
   <mappedConcept id="${map.property.qualName}">
  <#/list>
 </schema>
</concepts>
</capabilities>
</#escape>
</@page.param>
</@page.applyDecorator>