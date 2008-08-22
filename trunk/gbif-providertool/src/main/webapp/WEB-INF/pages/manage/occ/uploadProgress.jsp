<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceOverview.title"/></title>
    <meta name="resource" content="<s:property value="occResource.title"/>"/>
    <meta name="submenu" content="manage"/>
	<s:head theme="ajax" debug="false"/>
</head>

<h1>Upload in Progress</h1>
<p class="reminder">This resource is currently being updated and not available. Please wait until the upload is finished or cancel it.</p>

<s:url id="recordCountUrl" action="uploadStatus">
	<s:param name="resource_id" value="resource_id" />
	<s:param name="ajax" value="true"/>
</s:url>
<li id="wwgrp_upload_resource_recordCount" class="wwgrp">
	<div id="wwlbl_upload_resource_recordCount" class="wwlbl">
		<label for="upload_resource_recordCount" class="desc"><s:text name="resource.recordCount"/></label>
	</div> 
	<s:div id="recordCount" theme="ajax" href="%{recordCountUrl}" updateFreq="2000">
		<s:property value="occResource.getRecordCount()"/>
	</s:div>
</li>				

<s:form action="upload" method="post">
	<s:hidden key="resource_id"/>
	<s:submit cssClass="button" key="button.cancel"/>
</s:form>

<br/>

