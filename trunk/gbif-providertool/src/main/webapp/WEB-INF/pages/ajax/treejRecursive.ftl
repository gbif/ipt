<#include "/WEB-INF/pages/inc/limit.ftl">  
[
<#list nodes as n>
    {
        key: "${n.id?c}",
        isLazy: ${n.isLeafNode()?string('false','true')},
        icon: false,
        title: "${limit(n.label)}",
        tooltip: "${n.label}",
        pia: ${focus?c},
      <#if focus==n.id>
        focus: true,
        activate: true,
      </#if>
      <#if parents?contains(n.id?c)>
        expand: true,
        children:
		<@s.action name="${treeType}Tree" executeResult="true">
			<@s.param name="resource_id" value="${resource_id?c}"/>
			<@s.param name="treeType" value="${treeType}"/>
			<@s.param name="focus" value="${focus?c}"/>
			<@s.param name="parents" value="${parents}"/>
			<@s.param name="id" value="${n.id?c}"/>
		</@s.action>        
      </#if>
    },
</#list>
]