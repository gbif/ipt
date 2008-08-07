<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceList.title"/></title>
    <meta name="heading" content="<s:text name='upload.heading'/>"/>
    <meta name="resource" content="<s:property value="resource.title"/>"/>
    <meta name="submenu" content="manage"/>
</head>

<c:set var="buttons">
	<s:form action="resource" method="get">
	  <s:hidden key="resource_id"/>
	  <s:submit cssClass="button" key="button.done" theme="simple"/>
	</s:form>
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

    <display:setProperty name="paging.banner.item_name"><s:text name="upload.event"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><s:text name="upload.events"/></display:setProperty>
</display:table>

<c:out value="${buttons}" escapeXml="false" />


<script type="text/javascript">
    highlightTableRows("uploadEvents");
</script>
