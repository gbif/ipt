<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceList.title"/></title>
    <meta name="heading" content="<s:text name='upload.heading'/>"/>
    <meta name="menu" content="MainMenu"/>
</head>

<s:form action="addUploadJob" method="post" validate="true">
    <s:hidden key="resource_id"/>        
    <s:textfield key="upload.repeatInDays" name="repeatInDays" value="0" cssClass="text small"/>
    <s:textfield key="upload.limit" name="limit" cssClass="text small"/>
    <s:submit cssClass="button" key="button.add" theme="simple"/>
</s:form>

<div id="jobs">
	<display:table name="scheduledJobs" class="table" export="false" pagesize="25">
	    <display:column property="nextFireTime" sortProperty="nextFireTime" sortable="true" titleKey="upload.nextFireTime"/>
	    <display:column property="description" sortable="true" titleKey="upload.description"/>
	    <display:column property="repeatInDays" sortable="true" titleKey="upload.repeatInDays"/>
	
	    <display:setProperty name="paging.banner.item_name"><s:text name="upload.job"/></display:setProperty>
	    <display:setProperty name="paging.banner.items_name"><s:text name="upload.jobs"/></display:setProperty>
	</display:table>
</div>

<s:form action="resource" namespace="/manage/occ" method="post">
    <s:hidden key="resource_id"/>        
    <s:submit cssClass="button" key="button.done"/>
</s:form>

<script type="text/javascript">
    highlightTableRows("scheduledJobs");
</script>
