<%@ include file="/common/taglibs.jsp"%>

<s:property value="status" />

<s:if test="%{busy}">
	<s:form action="cancelUpload" method="post">
		<s:hidden key="resource_id"/>
		<s:submit cssClass="button" key="button.cancel"/>
	</s:form>
</s:if>
<s:else>
	<s:form action="resource" method="post">
		<s:hidden key="resource_id"/>
		<s:submit cssClass="button" key="button.done"/>
	</s:form>
</s:else>

