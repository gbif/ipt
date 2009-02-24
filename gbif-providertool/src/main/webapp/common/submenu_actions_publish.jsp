<%@ include file="/common/taglibs.jsp"%>

<div id="publish">
  <s:if test="resource.isDirty()">
	<s:form id="publishForm" action="saveResource" enctype="multipart/form-data" method="post">
	    <s:hidden name="resource_id" value="%{resource.id}"/>
	    <s:hidden name="resourceType" value="%{resourceType}"/>
		<s:submit cssClass="button" name="publish" method="publish" key="button.publish" theme="simple"/>
	</s:form>
  </s:if>
  <s:else>
	<p><s:property value="resource.status"/></p>
  </s:else>
</div>
