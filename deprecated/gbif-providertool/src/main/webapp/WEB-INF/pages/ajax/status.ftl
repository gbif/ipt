${status}
<#if busy>
	<@s.form action="cancelUpload" method="post">
		<@s.hidden key="resourceId"/>
		<@s.submit cssClass="button" key="button.cancel"/>
	</@s.form>
<#else>
	<@s.form action="resource" method="get" namespace="/">
		<@s.hidden key="resourceId"/>
		<@s.submit cssClass="button" key="button.done"/>
	</@s.form>
	<script type="text/javascript">    dojo.event.topic.publish("/stopTimer"); // jquery:http://stackoverflow.com/questions/220767/auto-refreshing-div-with-jquery </script>
</#if>


