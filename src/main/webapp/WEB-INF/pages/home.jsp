<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="home.title"/></title>
    <meta name="heading" content="<fmt:message key='home.heading'/>"/>
    <meta name="menu" content="MainMenu"/>
</head>

<h3>Occurrence Resources</h3>
<display:table name="occResources" class="table" requestURI="" id="occResourceList" export="true" pagesize="25">
    <display:column property="id" sortable="true" href="editOccResource.html" media="html" paramId="id" paramProperty="id" titleKey="occResource.id"/>
    <display:column property="title" sortable="true" titleKey="occResource.title"/>
    <display:column sortProperty="modified" sortable="true" titleKey="occResource.modified">
         <fmt:formatDate value="${occResourceList.modified}" pattern="${datePattern}"/>
    </display:column>
    <display:column property="recordCount" sortable="true" titleKey="occResource.recordCount"/>

    <display:setProperty name="paging.banner.item_name"><fmt:message key="occResourceList.occResource"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><fmt:message key="occResourceList.occResources"/></display:setProperty>
</display:table>

<h3>Checklist Resources</h3>
<p><s:property value="checklistCount"/> checklists uploaded. <s: a/> </p>

<h3>Generic Resource Descriptions</h3>
<p><s:property value="resourceCount"/> resources listed. <s: a/> </p>

<p></p>
