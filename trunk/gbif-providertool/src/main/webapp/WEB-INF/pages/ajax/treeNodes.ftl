<?xml version='1.0' encoding='utf-8'?>
<#escape x as x?xml>
<tree id="${(id!0)?c}">
 <@s.iterator value="nodes">
   <item text="${label}" id="${id?c}" child="${leafNode?string('0','1')}" call="true" select="yes" />
 </@s.iterator>
</tree>
</#escape>