<head>
    <title><@s.text name="resourceOverview.title"/></title>
    <meta name="resource" content="<@s.property value="resource.title"/>"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
	<@s.head theme="ajax" debug="false"/>
</head>

<h1>Upload in Progress</h1>
<p class="reminder">This resource is currently being updated and not available. Please wait until the upload is finished or cancel it.</p>

<@s.url id="statusUrl" value="/ajax/resourceStatus.html?resource_id=${Parameters.resource_id}"/>
<@s.div id="status" theme="ajax" href="%{statusUrl}" updateFreq="3000" showLoadingText="false" 
	startTimerListenTopics="/startTimer" stopTimerListenTopics="/stopTimer" executeScripts="true"
	errorText="Unable to retrieve resource status" >
</@s.div>

<br/>

