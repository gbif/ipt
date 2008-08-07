<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="home.title"/></title>
    <meta name="menu" content="MainMenu"/>
</head>


<h1><s:text name='home.heading'/></h1>

<h3>Occurrence Resources</h3>

<display:table name="occResources" class="table" requestURI="" id="occResourceList" export="false" pagesize="5">
    <display:column property="title" sortable="true" titleKey="resource.title" href="occResource.html" media="html" paramId="resource_id" paramProperty="id"/>
    <display:column property="modified" sortable="true" titleKey="resource.modified" format="{0,date,${datePattern}}"/>
    <display:column property="recordCount" sortable="true" titleKey="resource.recordCount"/>

    <display:setProperty name="paging.banner.item_name"><s:text name="resourceList.resource"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><s:text name="resourceList.resources"/></display:setProperty>
</display:table>


<h3>Checklist Resources</h3>

<display:table name="checklistResources" class="table" requestURI="" id="checklistResourceList" export="false" pagesize="5">
    <display:column property="title" sortable="true" titleKey="resource.title" href="taxResource.html" media="html" paramId="resource_id" paramProperty="id"/>
    <display:column property="modified" sortable="true" titleKey="resource.modified" format="{0,date,${datePattern}}"/>
    <display:column property="recordCount" sortable="true" titleKey="resource.recordCount"/>

    <display:setProperty name="paging.banner.item_name"><s:text name="resourceList.resource"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><s:text name="resourceList.resources"/></display:setProperty>
</display:table>



<h3>Generic Resource Descriptions</h3>

<display:table name="resources" class="table" requestURI="" id="resourceList" export="false" pagesize="5">
    <display:column property="title" sortable="true" titleKey="resource.title" href="resource.html" media="html" paramId="resource_id" paramProperty="id"/>
    <display:column property="modified" sortable="true" titleKey="resource.modified" format="{0,date,${datePattern}}"/>
    <display:column property="recordCount" sortable="true" titleKey="resource.recordCount"/>

    <display:setProperty name="paging.banner.item_name"><s:text name="resourceList.resource"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><s:text name="resourceList.resources"/></display:setProperty>
</display:table>


<p>
</p>
