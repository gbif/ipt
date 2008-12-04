<head>
    <title><@s.text name="dataResource.metadata"/></title>
    <meta name="resource" content="${resource.title!}"/>
    <#if resource.isDataResource()>
    	<meta name="submenu" content="manage_resource"/>    
    <#else>
	    <meta name="submenu" content="eml"/>
    </#if>
</head>

<p>Please describe the dataset you want to publish as a whole</p>

<@s.form id="resourceForm" action="saveResource" enctype="multipart/form-data" method="post">
  <fieldset>
    <legend><@s.text name="resource.metadata"/></legend>
    <@s.hidden name="resource_id" value="${(resource.id)!}"/>
    <@s.hidden name="resourceType" value="${(resourceType)!}"/>
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
	<#if resource.title?? && resource.isDataResource()>
	    <span class="right"><a href="/manage/meta/creatorForm.html?resource_id=${resource.id}">More Metadata ...</a></span>
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

<#if resource.modified??>
<p>
	<@s.text name="dataResource.lastModified"/> ${resource.modified} <#if resource.modifier??>by ${resource.modifier.getFullName()}</#if>
</p>
</#if>