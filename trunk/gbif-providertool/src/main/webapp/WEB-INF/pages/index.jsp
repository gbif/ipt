<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="home.title"/></title>
    <meta name="heading" content="<s:text name='home.heading'/>"/>
    <meta name="menu" content="MainMenu"/>
</head>


<h3>Occurrence Resources</h3>

<display:table name="occResources" class="table" requestURI="" id="occResourceList" export="false" pagesize="5">
    <display:column property="title" sortable="true" titleKey="resource.title" href="resource.html" media="html" paramId="id" paramProperty="id"/>
    <display:column property="modified" sortable="true" titleKey="resource.modified" format="{0,date,${datePattern}}"/>
    <display:column property="recordCount" sortable="true" titleKey="resource.recordCount"/>

    <display:setProperty name="paging.banner.item_name"><s:text name="resourceList.resource"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><s:text name="resourceList.resources"/></display:setProperty>
</display:table>


<h3>Checklist Resources</h3>

<display:table name="checklistResources" class="table" requestURI="" id="checklistResourceList" export="false" pagesize="5">
    <display:column property="title" sortable="true" titleKey="resource.title" href="resource.html" media="html" paramId="id" paramProperty="id"/>
    <display:column property="modified" sortable="true" titleKey="resource.modified" format="{0,date,${datePattern}}"/>
    <display:column property="recordCount" sortable="true" titleKey="resource.recordCount"/>

    <display:setProperty name="paging.banner.item_name"><s:text name="resourceList.resource"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><s:text name="resourceList.resources"/></display:setProperty>
</display:table>



<h3>Generic Resource Descriptions</h3>

<display:table name="resources" class="table" requestURI="" id="resourceList" export="false" pagesize="5">
    <display:column property="title" sortable="true" titleKey="resource.title" href="resource.html" media="html" paramId="id" paramProperty="id"/>
    <display:column property="modified" sortable="true" titleKey="resource.modified" format="{0,date,${datePattern}}"/>
    <display:column property="recordCount" sortable="true" titleKey="resource.recordCount"/>

    <display:setProperty name="paging.banner.item_name"><s:text name="resourceList.resource"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><s:text name="resourceList.resources"/></display:setProperty>
</display:table>


<p>
</p>
