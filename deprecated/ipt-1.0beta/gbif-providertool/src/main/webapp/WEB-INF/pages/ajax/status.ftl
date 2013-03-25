${status}
<#if busy>
	<@s.form action="cancelUpload" method="post">
		<@s.hidden key="resource_id"/>
		<@s.submit cssClass="button" key="button.cancel"/>
	</@s.form>
<#else>
	<@s.form action="resource" method="get" namespace="/">
		<@s.hidden key="resource_id"/>
		<@s.submit cssClass="button" key="button.done"/>
	</@s.form>
	<script type="text/javascript">    dojo.event.topic.publish("/stopTimer");  </script>
</#if>


