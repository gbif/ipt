<head>
    <title><@s.text name="uploadstatus.title"/></title>
    <meta name="resource" content="<@s.property value="resource.title"/>"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
    <@s.head theme="ajax" debug="false"/>
</head>

<h1><@s.text name="uploadstatus.inprogress"/></h1>
<p class="reminder"><@s.text name="uploadstatus.reminder"/></p>


<@s.url id="statusUrl" value="/ajax/resourceStatus.html?resourceId=${Parameters.resourceId}"/>
<@s.div id="status" theme="ajax" href="%{statusUrl}" updateFreq="3000" showLoadingText="false" 
    startTimerListenTopics="/startTimer" stopTimerListenTopics="/stopTimer" executeScripts="true"
    errorText="Can't load status." >
</@s.div>

<br/>