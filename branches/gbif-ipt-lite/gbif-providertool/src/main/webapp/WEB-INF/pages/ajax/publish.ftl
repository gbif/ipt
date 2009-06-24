<#if resource.id??>
  	<div id="statusContainer" class="${resource.status}">
  	 <p>
  	 	<@s.text name="publish.status.${resource.status}"/>, 
     </p>
	  <#if resource.isPublic()>
		<@s.form id="publishForm" action="unpublish" namespace="/ajax" method="post">
		    <@s.hidden name="resource_id" value="${resource.id}"/>
		    <@s.hidden name="resourceType" value="${resourceType}"/>
			<@s.submit id="btnPublish" cssClass="publishButton" key="button.unpublish" theme="simple"/>
		</@s.form>
	  <#else>
		<@s.form id="publishForm" action="publish" namespace="/ajax" method="post">
		    <@s.hidden name="resource_id" value="${resource.id}"/>
		    <@s.hidden name="resourceType" value="${resourceType}"/>
			<@s.submit id="btnPublish" cssClass="publishButton" key="button.publish" theme="simple"/>
		</@s.form>
	  </#if>
	</div>
</#if>
