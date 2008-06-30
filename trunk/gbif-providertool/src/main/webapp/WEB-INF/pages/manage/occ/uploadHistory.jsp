<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="occResourceList.title"/></title>
    <meta name="heading" content="<fmt:message key='upload.heading'/>"/>
    <meta name="menu" content="OccResourceMenu"/>
</head>

<c:set var="buttons">
    <input type="button" class="button" onclick="location.href='<c:url value="resource.html"/>'"
        value="<fmt:message key="button.done"/>"/>
</c:set>

<c:out value="${buttons}" escapeXml="false" />

<display:table name="uploadEvents" id="uploadEventList" class="table" requestURI="" export="true" pagesize="25">
    <display:column sortProperty="executionDate" sortable="true" titleKey="uploadEvent.executionDate">
         <fmt:formatDate value="${uploadEventList.executionDate}" pattern="${datePattern}"/>
    </display:column>
    <display:column property="recordsUploaded" sortable="true" titleKey="uploadEvent.recordsUploaded" />
    <display:column property="recordsDeleted" sortable="true" titleKey="uploadEvent.recordsDeleted" />
    <display:column property="recordsChanged" sortable="true" titleKey="uploadEvent.recordsChanged" />
    <display:column property="recordsAdded" sortable="true" titleKey="uploadEvent.recordsAdded" />
    <display:column value="Logs" sortable="false" href="logs.html" media="html"
        paramId="id" paramProperty="id" titleKey="uploadEvent.logs"/>

    <display:setProperty name="paging.banner.item_name"><fmt:message key="upload.event"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><fmt:message key="upload.events"/></display:setProperty>
</display:table>

<c:out value="${buttons}" escapeXml="false" />


<script type="text/javascript">
    highlightTableRows("uploadEvents");
</script>
