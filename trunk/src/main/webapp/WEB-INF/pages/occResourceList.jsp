<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="occResourceList.title"/></title>
    <meta name="heading" content="<fmt:message key='occResourceList.heading'/>"/>
    <meta name="menu" content="OccResourceMenu"/>
</head>

<c:set var="buttons">
    <input type="button" style="margin-right: 5px" class="button"
        onclick="location.href='<c:url value="manage/editOccResource.html"/>'"
        value="<fmt:message key="button.add"/>"/>

    <input type="button" class="button" onclick="location.href='<c:url value="/home.html"/>'"
        value="<fmt:message key="button.done"/>"/>
</c:set>

<c:out value="${buttons}" escapeXml="false" />

<display:table name="occResources" class="table" requestURI="" id="occResourceList" export="true" pagesize="25">
    <display:column property="id" sortable="true" href="manage/viewOccResource.html" media="html"
        paramId="id" paramProperty="id" titleKey="occResource.id"/>
    <display:column property="title" sortable="true" titleKey="occResource.title"/>
    <display:column property="serviceName" sortable="true" titleKey="occResource.serviceName"/>
    <display:column sortProperty="modified" sortable="true" titleKey="occResource.modified">
         <fmt:formatDate value="${occResourceList.modified}" pattern="${datePattern}"/>
    </display:column>
    <display:column property="recordCount" sortable="true" titleKey="occResource.recordCount"/>

    <display:setProperty name="paging.banner.item_name"><fmt:message key="occResourceList.occResource"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><fmt:message key="occResourceList.occResources"/></display:setProperty>
</display:table>

<c:out value="${buttons}" escapeXml="false" />

<script type="text/javascript">
    highlightTableRows("occResourceList");
</script>
