<#assign page=JspTaglibs["http://www.opensymphony.com/sitemesh/page"]>
<@page.applyDecorator name="tapir-envelope" title="ping">
<@page.param name="tapir.content">
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
</capabilities>
</@page.param>
</@page.applyDecorator>