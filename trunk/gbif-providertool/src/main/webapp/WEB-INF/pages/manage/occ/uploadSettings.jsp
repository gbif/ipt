<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="occResourceList.title"/></title>
    <meta name="heading" content="<fmt:message key='upload.heading'/>"/>
    <meta name="menu" content="OccResourceMenu"/>
</head>

<s:form action="addUploadJob" method="post" validate="true">
    <s:hidden key="resource_id"/>        
    <s:submit cssClass="button" key="button.upload" theme="simple"/>
    <input type="button" class="button" onclick="location.href='<c:url value="resource.html"/>'"
        value="<fmt:message key="button.done"/>"/>
</s:form>

<div id="jobs">
	<display:table name="scheduledJobs" class="table" export="false" pagesize="25">
	    <display:column sortProperty="executionDate" sortable="true" titleKey="uploadEvent.executionDate">
	         <fmt:formatDate value="${occResourceList.modified}" pattern="${datePattern}"/>
	    </display:column>
	
	    <display:setProperty name="paging.banner.item_name"><fmt:message key="upload.job"/></display:setProperty>
	    <display:setProperty name="paging.banner.items_name"><fmt:message key="upload.jobs"/></display:setProperty>
	</display:table>
</div>


<c:out value="${buttons}" escapeXml="false" />

<script type="text/javascript">
    highlightTableRows("scheduledJobs");
</script>
