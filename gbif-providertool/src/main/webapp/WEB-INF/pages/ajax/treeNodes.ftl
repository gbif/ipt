<?xml version='1.0' encoding='utf-8'?>
<#escape x as x?xml>
<tree id="${(id!0)?c}">
 <@s.iterator value="nodes">
   <@s.url id="taxonUrl" action="resource"><@s.param name="resource_id" value="resource_id"/><@s.param name="taxon_id" value="id"/></@s.url>
   <item text="${label}" id="${id?c}" child="${leafNode?string('0','1')}" call="true" select="yes" />
 </@s.iterator>
</tree>
</#escape>