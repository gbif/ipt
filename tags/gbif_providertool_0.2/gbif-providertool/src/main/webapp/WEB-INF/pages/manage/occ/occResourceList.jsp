<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceList.title"/></title>
    <meta name="heading" content="<s:text name='occResourceList.heading'/>"/>
    <meta name="menu" content="MainMenu"/>
    <meta name="submenu" content="manage"/>
</head>

<c:set var="buttons">
    <input type="button" style="margin-right: 5px" class="button"
        onclick="location.href='<s:url action="addResource"/>'"
        value="<s:text name="button.add"/>"/>

    <input type="button" class="button" onclick="location.href='<c:url value="/home.html"/>'"
        value="<s:text name="button.done"/>"/>
</c:set>

<c:out value="${buttons}" escapeXml="false" />

<display:table name="occResources" uid="res" class="table" requestURI="" id="occResourceList" export="false" pagesize="25">
    <display:column property="title" sortable="true" href="resource.html" media="html"
        paramId="resource_id" paramProperty="id" titleKey="resource.title"/>
    <display:column property="serviceName" sortable="true" titleKey="occResource.serviceName"/>
    <display:column property="recordCount" sortable="true" titleKey="resource.recordCount"/>
	<display:column property="lastUpload.executionDate" sortable="true" titleKey="occResource.lastImport" format="{0,date,${datePattern}}"/>    
    
    <display:setProperty name="paging.banner.item_name"><s:text name="resourceList.resource"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><s:text name="resourceList.resources"/></display:setProperty>
</display:table>

<c:out value="${buttons}" escapeXml="false" />

<script type="text/javascript">
    highlightTableRows("occResourceList");
</script>
