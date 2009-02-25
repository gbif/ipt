<%@ include file="/common/taglibs.jsp"%>

<s:if test="resource.id>0">
<div id="publish">
  <s:if test="resource.isDirty()">
	<div class="dirtyContainer">
	<s:form id="publishForm" action="saveResource" enctype="multipart/form-data" method="post">
	    <s:hidden name="resource_id" value="%{resource.id}"/>
	    <s:hidden name="resourceType" value="%{resourceType}"/>
		<s:text name="dirty.dirty"/>
		<s:submit cssClass="publishButton" name="publish" method="publish" key="button.publish" theme="simple"/>
	</s:form>
	</div>
  </s:if>
  <s:else>
  	<div class="cleanContainer">
		<p><s:text name="dirty.clean"/></p>
	</div>
  </s:else>
</div>
</s:if>
