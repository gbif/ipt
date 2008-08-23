<%@ include file="/common/taglibs.jsp"%>

<s:property value="status" />

<s:if test="%{busy}">
	<s:form action="cancelUpload" method="post">
		<s:hidden key="resource_id"/>
		<s:submit cssClass="button" key="button.cancel"/>
	</s:form>
</s:if>
<s:else>
	<s:form action="resource" method="get">
		<s:hidden key="resource_id"/>
		<s:submit cssClass="button" key="button.done"/>
	</s:form>
	
	<script type="text/javascript">    dojo.event.topic.publish("/stopTimer");  </script>

	<!-- 
	<s:a id="link1" theme="ajax" notifyTopics="/stopTimer">Stop</s:a>
	<s:a id="link1" theme="ajax" notifyTopics="/startTimer">Start</s:a>
 	-->
</s:else>


