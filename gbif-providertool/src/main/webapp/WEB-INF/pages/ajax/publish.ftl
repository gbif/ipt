<#if resource.id??>
  	<div id="statusContainer" class="${resource.status}">
	  <#if resource.isPublic()>
		<@s.form id="publishForm" action="unpublish" namespace="/ajax">
			<@s.submit id="btnPublish" cssClass="publishButton" key="button.unpublish" theme="simple"/>
		</@s.form>
	  <#else>
		<@s.form id="publishForm" action="publish" namespace="/ajax">
			<@s.submit id="btnPublish" cssClass="publishButton" key="button.publish" theme="simple"/>
		</@s.form>
	  </#if>
	</div>
</#if>
