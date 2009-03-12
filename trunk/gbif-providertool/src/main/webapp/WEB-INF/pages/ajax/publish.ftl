<#if resource.id??>
  <#if resource.isDirty()>
	<div class="dirtyContainer">
	<@s.form id="publishForm" action="publish" namespace="/ajax" method="post">
	    <@s.hidden name="resource_id" value="${resource.id}"/>
	    <@s.hidden name="resourceType" value="${resourceType}"/>
		<@s.text name="dirty.dirty"/>
		<@s.submit id="btnPublish" cssClass="publishButton" key="button.publish" theme="simple"/>
	</@s.form>
	</div>
  <#else>
  	<div class="cleanContainer">
		<p><@s.text name="dirty.clean"/></p>
	</div>
  </#if>
</#if>
