<#include "/WEB-INF/pages/inc/limit.ftl">  
<#escape x as x?xml>
 <#list nodes as n>
  <#if parents?contains(n.id?c)&& !n.isLeafNode()>
   <item text="${limit(n.label)}" id="${n.id?c}" child="${n.isLeafNode()?string('0','1')}" call="true" select="yes">
	<@s.action name="${treeType}TreeItems" executeResult="true">
		<@s.param name="resource_id" value="${resource_id?c}"/>
		<@s.param name="treeType" value="${treeType}"/>
		<@s.param name="parents" value="${parents}"/>
		<@s.param name="id" value="${n.id?c}"/>
	</@s.action>
   </item>
  <#else>
   <item text="${limit(n.label)}" id="${n.id?c}" child="${n.isLeafNode()?string('0','1')}" call="true" select="yes" />
  </#if>
 </#list>
</#escape>