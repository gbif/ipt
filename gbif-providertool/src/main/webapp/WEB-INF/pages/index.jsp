<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="home.title"/></title>
    <meta name="menu" content="MainMenu"/>
    <meta name="submenu" content="metadata"/>
    <meta name="decorator" content="fullsize"/>
</head>


<h1><s:text name='home.heading'/> <s:property value='cfg.providerTitle'/></h1>

<div id="about">
	<img src="<s:property value='cfg.providerDescriptionImage'/>" />
	<s:property value='cfg.providerDescription' escape="false"/>
</div>

<br class="clearfix" />

<h3>Hosted resources</h3>

<display:table name="resources" class="table" requestURI="" id="resourceList" export="false" pagesize="5">
    <display:column property="title" sortable="true" titleKey="resource.title" href="resource.html" media="html" paramId="resource_id" paramProperty="id"/>
    <display:column property="modified" sortable="true" titleKey="resource.modified" format="{0,date,${datePattern}}"/>
    <display:column property="recordCount" sortable="true" titleKey="resource.recordCount"/>
    <display:column property="type" sortable="true" titleKey="resource.type"/>

    <display:setProperty name="paging.banner.item_name"><s:text name="resourceList.resource"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><s:text name="resourceList.resources"/></display:setProperty>
</display:table>


<s:url id="metaRepoUrl" action="resources"/>
<p>
You can find additional resource descriptions in the <s:a href="%{metaRepoUrl}">local metadata repository</s:a>.
</p>
