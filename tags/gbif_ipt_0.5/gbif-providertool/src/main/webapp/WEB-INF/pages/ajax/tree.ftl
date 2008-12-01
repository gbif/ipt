<?xml version='1.0' encoding='utf-8'?>
<#escape x as x?xml>
<tree id="${(id!0)?c}">
 <#list nodes as n>
  <#if parents?contains(n.id?c)>
   <item text="${n.label}" id="${n.id?c}" child="${n.isLeafNode()?string('0','1')}" call="true" select="yes">
	<@s.action name="${treeType}TreeItems" executeResult="true">
		<@s.param name="resource_id" value="${resource_id?c}"/>
		<@s.param name="treeType" value="${treeType}"/>
		<@s.param name="parents" value="${parents}"/>
		<@s.param name="id" value="${n.id?c}"/>
	</@s.action>
   </item>
  <#else>
   <item text="${n.label}" id="${n.id?c}" child="${n.isLeafNode()?string('0','1')}" call="true" select="yes" />
  </#if>
 </#list>
</tree>
</#escape>