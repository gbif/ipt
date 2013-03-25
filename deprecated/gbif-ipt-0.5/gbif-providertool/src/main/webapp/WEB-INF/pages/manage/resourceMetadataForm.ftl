
<@s.form id="resourceForm" action="saveResource" enctype="multipart/form-data" method="post">
  <fieldset>
    <legend><@s.text name="resource.metadata"/></legend>
    <@s.hidden name="resource_id" value="${(resource.id)!}"/>
    <@s.hidden name="guid" value="${(resource.guid)!}"/>
    <#if resource_id??>
		<img class="rightf" src="${cfg.getResourceLogoUrl(resource_id)}" />
	</#if>
	<div>
		<div class="left">
			<@s.textfield key="resource.title" required="true" cssClass="text large"/>
	 	</div>
		<div class="left">
		 	<@s.select key="resource.type" required="true" emptyOption="false" list="resourceTypes" cssClass="text medium"/>
	 	</div>
 	</div>
    <div class="newline">
        <div class="left">
			<@s.textfield key="resource.contactName" required="true" cssClass="text medium"/>
        </div>
        <div class="left">
			<@s.textfield key="resource.contactEmail" required="true" cssClass="text medium"/>
        </div>
        <div>
		    <@s.file name="file" key="resource.selectLogoFile" cssClass="text file" required="false"/>
        </div>
    </div>
	<@s.textfield key="resource.link" required="false" cssClass="text xlarge"/>
	<@s.textarea key="resource.description" cssClass="text xlarge"/>
    <@s.submit cssClass="button" name="save" key="button.save" theme="simple"/>
    <@s.submit cssClass="button" method="cancel" key="button.cancel" theme="simple"/>
	<#if resource.id??>
	    <@s.submit cssClass="button" method="delete" key="button.delete" onclick="return confirmDelete('resource')" theme="simple"/>
	</#if>
	<#if resource.title??>
	    <@s.submit cssClass="button" key="button.details" action="eml" theme="simple"/>
	</#if>
  </fieldset>
</@s.form>
	
	<!--
	<#if !resource.title??>
		<hr/>
		<@s.form id="resourceFormEml" action="importEml" enctype="multipart/form-data" method="post">
	    	<@s.file name="file" key="resource.selectEmlFile" cssClass="text file" required="false"/>
		    <@s.submit cssClass="button" key="button.import" theme="simple"/>
		</@s.form>
	</#if>
	-->
	
  

<script type="text/javascript">
    Form.focusFirstElement($("resourceForm"));
</script>
