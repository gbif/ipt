<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceList.title"/></title>
    <meta name="heading" content="<s:text name='occResourceList.heading'/>"/>
    <meta name="menu" content="MainMenu"/>
</head>

<c:set var="buttons">
    <input type="button" style="margin-right: 5px" class="button"
        onclick="location.href='<s:url action="editResourceMetadata"><s:param name="neu" value="true"/></s:url>'"
        value="<s:text name="button.add"/>"/>

    <input type="button" class="button" onclick="location.href='<c:url value="/home.html"/>'"
        value="<s:text name="button.done"/>"/>
</c:set>

<c:out value="${buttons}" escapeXml="false" />

<display:table name="occResources" class="table" requestURI="" id="occResourceList" export="false" pagesize="25">
    <display:column property="title" sortable="true" href="resource.html" media="html"
        paramId="resource_id" paramProperty="id" titleKey="resource.title"/>
    <display:column property="serviceName" sortable="true" titleKey="occResource.serviceName"/>
    <display:column sortProperty="lastImport" sortable="true" titleKey="occResource.lastImport">
         <fmt:formatDate value="${occResourceList.lastImport}" pattern="${datePattern}"/>
    </display:column>
    <display:column property="recordCount" sortable="true" titleKey="resource.recordCount"/>

    <display:setProperty name="paging.banner.item_name"><s:text name="resourceList.resource"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><s:text name="resourceList.resources"/></display:setProperty>
</display:table>

<c:out value="${buttons}" escapeXml="false" />

<script type="text/javascript">
    highlightTableRows("occResourceList");
</script>
