[
	{
		"id": "36",
		"text": "Abies alba L.",
		"expanded": true,
		"classes": "important",
		"hasChildren": true
	},
	{
		"text": "2. Lunch  (60 min)"
	},
	{
		"text": "3. After Lunch  (120+ min)",
		"children":
		[
			{
				"text": "3.1 jQuery Calendar Success Story (20 min)"
			},
		 	{
				"text": "3.2 jQuery and Ruby Web Frameworks (20 min)"
			},
		 	{
				"text": "3.3 Hey, I Can Do That! (20 min)"
			},
		 	{
				"text": "3.4 Taconite and Form (20 min)"
			},
		 	{
				"text": "3.5 Server-side JavaScript with jQuery and AOLserver (20 min)"
			},
		 	{
				"text": "3.6 The Onion: How to add features without adding features (20 min)",
				"id": "36",
				"hasChildren": true
			},
		 	{
				"text": "3.7 Visualizations with JavaScript and Canvas (20 min)"
			},
		 	{
				"text": "3.8 ActiveDOM (20 min)"
			},
		 	{
				"text": "3.8 Growing jQuery (20 min)"
			}
		]
	}
]
<#include "/WEB-INF/pages/inc/limit.ftl">  
<#escape x as x?xml>
<tree id="${(id!0)?c}">
 <#list nodes as n>
  <#if parents?contains(n.id?c)>
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
</tree>
</#escape>