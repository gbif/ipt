<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceOverview.title"/></title>
    <meta name="resource" content="<s:property value="resource.title"/>"/>
    <meta name="submenu" content="manage"/>
	<s:head theme="ajax" debug="false"/>
</head>

<h1>Upload in Progress</h1>
<p class="reminder">This resource is currently being updated and not available. Please wait until the upload is finished or cancel it.</p>

<s:url id="statusUrl" action="resourceStatus" namespace="/ajax">
	<s:param name="resource_id" value="%{#parameters.resource_id}" />
</s:url>

<s:div id="status" theme="ajax" href="%{statusUrl}" updateFreq="3000" showLoadingText="false" 
	startTimerListenTopics="/startTimer" stopTimerListenTopics="/stopTimer" executeScripts="true"
	errorText="Unable to retrieve resource status" >
</s:div>

<br/>

