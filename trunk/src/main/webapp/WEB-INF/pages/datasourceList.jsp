<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="datasourceList.title"/></title>
    <meta name="heading" content="<fmt:message key='datasourceList.heading'/>"/>
    <meta name="menu" content="DatasourceMenu"/>
</head>

<c:set var="buttons">
    <input type="button" style="margin-right: 5px" class="button"
        onclick="location.href='<c:url value="/editDatasource.html"/>'"
        value="<fmt:message key="button.add"/>"/>

    <input type="button" class="button" onclick="location.href='<c:url value="/mainMenu.html"/>'"
        value="<fmt:message key="button.done"/>"/>
</c:set>

<c:out value="${buttons}" escapeXml="false" />

<display:table name="datasources" class="table" requestURI="" id="datasourceList" export="true" pagesize="25">
    <display:column property="id" sortable="true" href="editDatasource.html" media="html"
        paramId="id" paramProperty="id" titleKey="datasource.id"/>
    <display:column property="id" media="csv excel xml pdf" titleKey="datasource.id"/>
    <display:column sortProperty="modified" sortable="true" titleKey="datasource.modified">
         <fmt:formatDate value="${datasourceList.modified}" pattern="${datePattern}"/>
    </display:column>
    <display:column property="serviceName" sortable="true" titleKey="datasource.serviceName"/>
    <display:column property="sourceJdbcConnection" sortable="true" titleKey="datasource.sourceJdbcConnection"/>

    <display:setProperty name="paging.banner.item_name"><fmt:message key="datasourceList.datasource"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><fmt:message key="datasourceList.datasources"/></display:setProperty>

    <display:setProperty name="export.excel.filename"><fmt:message key="datasourceList.title"/>.xls</display:setProperty>
    <display:setProperty name="export.csv.filename"><fmt:message key="datasourceList.title"/>.csv</display:setProperty>
    <display:setProperty name="export.pdf.filename"><fmt:message key="datasourceList.title"/>.pdf</display:setProperty>
</display:table>

<c:out value="${buttons}" escapeXml="false" />

<script type="text/javascript">
    highlightTableRows("datasourceList");
</script>
