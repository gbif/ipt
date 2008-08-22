<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceOverview.title"/></title>
    <meta name="resource" content="<s:property value="occResource.title"/>"/>
    <meta name="submenu" content="manage"/>
	<s:head theme="ajax" debug="false"/>
</head>

<h1>Upload in Progress</h1>
<p class="reminder">This resource is currently being updated and not available. Please wait until the upload is finished or cancel it.</p>

<s:url id="statusUrl" action="status">
	<s:param name="resource_id" value="resource_id" />
	<s:param name="ajax" value="true"/>
</s:url>

<s:label value="Status"/>
<s:div id="status" theme="ajax" href="%{statusUrl}" updateFreq="2000">
	...
</s:div>


<br/>

