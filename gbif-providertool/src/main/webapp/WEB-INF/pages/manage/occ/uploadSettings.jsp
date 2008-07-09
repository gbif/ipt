<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceList.title"/></title>
    <meta name="heading" content="<s:text name='upload.heading'/>"/>
    <meta name="menu" content="OccResourceMenu"/>
</head>

<s:form action="addUploadJob" method="post" validate="true">
    <s:hidden key="resource_id"/>        
    <s:submit cssClass="button" key="button.upload" theme="simple"/>
    <input type="button" class="button" onclick="location.href='<c:url value="resource.html"/>'"
        value="<s:text name="button.done"/>"/>
</s:form>

<div id="jobs">
	<display:table name="scheduledJobs" class="table" export="false" pagesize="25">
	    <display:column sortProperty="executionDate" sortable="true" titleKey="uploadEvent.executionDate">
	         <fmt:formatDate value="${occResourceList.modified}" pattern="${datePattern}"/>
	    </display:column>
	
	    <display:setProperty name="paging.banner.item_name"><s:text name="upload.job"/></display:setProperty>
	    <display:setProperty name="paging.banner.items_name"><s:text name="upload.jobs"/></display:setProperty>
	</display:table>
</div>


<c:out value="${buttons}" escapeXml="false" />

<script type="text/javascript">
    highlightTableRows("scheduledJobs");
</script>
