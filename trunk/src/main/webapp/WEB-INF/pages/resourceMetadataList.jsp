<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="resourceMetadataList.title"/></title>
    <meta name="heading" content="<fmt:message key='resourceMetadataList.heading'/>"/>
    <meta name="menu" content="ResourceMetadataMenu"/>
</head>

<c:set var="buttons">
    <input type="button" style="margin-right: 5px" class="button"
        onclick="location.href='<c:url value="/editResourceMetadata.html"/>'"
        value="<fmt:message key="button.add"/>"/>

    <input type="button" class="button" onclick="location.href='<c:url value="/mainMenu.html"/>'"
        value="<fmt:message key="button.done"/>"/>
</c:set>

<c:out value="${buttons}" escapeXml="false" />

<display:table name="resourceMetadatas" class="table" requestURI="" id="resourceMetadataList" export="true" pagesize="25">
    <display:column property="id" sortable="true" href="editResourceMetadata.html" media="html"
        paramId="id" paramProperty="id" titleKey="resourceMetadata.id"/>
    <display:column property="id" media="csv excel xml pdf" titleKey="resourceMetadata.id"/>
    <display:column sortProperty="modified" sortable="true" titleKey="resourceMetadata.modified">
         <fmt:formatDate value="${resourceMetadataList.modified}" pattern="${datePattern}"/>
    </display:column>
    <display:column property="uri" sortable="true" titleKey="resourceMetadata.uri"/>
    <display:column property="uuid" sortable="true" titleKey="resourceMetadata.uuid"/>
    <display:column property="description" sortable="true" titleKey="resourceMetadata.description"/>
    <display:column property="title" sortable="true" titleKey="resourceMetadata.title"/>

    <display:setProperty name="paging.banner.item_name"><fmt:message key="resourceMetadataList.resourceMetadata"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><fmt:message key="resourceMetadataList.resourceMetadatas"/></display:setProperty>

    <display:setProperty name="export.excel.filename"><fmt:message key="resourceMetadataList.title"/>.xls</display:setProperty>
    <display:setProperty name="export.csv.filename"><fmt:message key="resourceMetadataList.title"/>.csv</display:setProperty>
    <display:setProperty name="export.pdf.filename"><fmt:message key="resourceMetadataList.title"/>.pdf</display:setProperty>
</display:table>

<c:out value="${buttons}" escapeXml="false" />

<script type="text/javascript">
    highlightTableRows("resourceMetadataList");
</script>
