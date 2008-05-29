<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="occResourceList.title"/></title>
    <meta name="heading" content="<fmt:message key='occResourceList.heading'/>"/>
    <meta name="menu" content="OccResourceMenu"/>
</head>

<c:set var="buttons">
    <input type="button" style="margin-right: 5px" class="button"
        onclick="location.href='<c:url value="/editOccResource.html"/>'"
        value="<fmt:message key="button.add"/>"/>

    <input type="button" class="button" onclick="location.href='<c:url value="/mainMenu.html"/>'"
        value="<fmt:message key="button.done"/>"/>
</c:set>

<c:out value="${buttons}" escapeXml="false" />

<display:table name="occResources" class="table" requestURI="" id="occResourceList" export="true" pagesize="25">
    <display:column property="id" sortable="true" href="editOccResource.html" media="html"
        paramId="id" paramProperty="id" titleKey="occResource.id"/>
    <display:column property="id" media="csv excel xml pdf" titleKey="occResource.id"/>
    <display:column sortProperty="modified" sortable="true" titleKey="occResource.modified">
         <fmt:formatDate value="${occResourceList.modified}" pattern="${datePattern}"/>
    </display:column>
    <display:column property="serviceName" sortable="true" titleKey="occResource.serviceName"/>
    <display:column property="sourceJdbcConnection" sortable="true" titleKey="occResource.sourceJdbcConnection"/>

    <display:setProperty name="paging.banner.item_name"><fmt:message key="occResourceList.occResource"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><fmt:message key="occResourceList.occResources"/></display:setProperty>

    <display:setProperty name="export.excel.filename"><fmt:message key="occResourceList.title"/>.xls</display:setProperty>
    <display:setProperty name="export.csv.filename"><fmt:message key="occResourceList.title"/>.csv</display:setProperty>
    <display:setProperty name="export.pdf.filename"><fmt:message key="occResourceList.title"/>.pdf</display:setProperty>
</display:table>

<c:out value="${buttons}" escapeXml="false" />

<script type="text/javascript">
    highlightTableRows("occResourceList");
</script>
