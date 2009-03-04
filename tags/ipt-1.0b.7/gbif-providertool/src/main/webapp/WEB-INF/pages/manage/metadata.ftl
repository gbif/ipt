<head>
    <title><@s.text name="dataResource.metadata"/></title>
    <meta name="resource" content="${resource.title!}"/>
    <meta name="menu" content="ManagerMenu"/>
	<meta name="submenu" content="manage_resource"/>    
	<meta name="heading" content="Basic Metadata"/>    
</head>


<p class="explMt">Please describe the dataset you want to publish as a whole</p>

<@s.form id="resourceForm" action="saveResource" enctype="multipart/form-data" method="post">
  <fieldset>
    <@s.hidden name="resource_id" value="${(resource.id)!}"/>
    <@s.hidden name="resourceType" value="${(resourceType)!}"/>
    <@s.hidden name="guid" value="${(resource.guid)!}"/>
    <#if resource_id??>
		<img class="rightf" src="${cfg.getResourceLogoUrl(resource_id)}" />
	</#if>
	<div>
		<div class="leftLarge">
			<@s.textfield key="resource.title" required="true" cssClass="text large"/>
	 	</div>
		<div class="leftMedium">
			<@s.select key="resource.type" list="resourceTypeMap" required="true" cssClass="text medium"/>
	 	</div>
 	</div>
    <div class="newline">
        <div class="leftMedium">
			<@s.textfield key="resource.contactName" required="true" cssClass="text medium"/>
        </div>
        <div class="leftMedium">
			<@s.textfield key="resource.contactEmail" required="true" cssClass="text medium"/>
        </div>
        <div class="leftMedium">
		    <@s.file name="file" key="resource.selectLogoFile" cssClass="text file" required="false" />
        </div>
    </div>
	<@s.textarea key="resource.description" cssClass="text xlarge"/>
	<div class="breakRightButtons">
    	<@s.submit cssClass="button" name="save" key="button.save" theme="simple"/>
	    <@s.submit cssClass="button" method="cancel" key="button.cancel" theme="simple"/>
		<#if resource.id??>
		    <@s.submit cssClass="button" method="delete" key="button.delete" onclick="return confirmDelete('resource')" theme="simple"/>
		</#if>
	</div>
  </fieldset>
	<#if resource.modified??>
	<div class="modifiedDate">
		<@s.text name="dataResource.lastModified"/> ${resource.modified?datetime?string} <#if resource.modifier??>by ${resource.modifier.getFullName()}</#if>
	</div>
	</#if>  
</@s.form>
	
	<#--
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