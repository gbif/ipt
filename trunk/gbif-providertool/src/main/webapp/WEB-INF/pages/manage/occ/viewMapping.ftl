<head>
    <title><@s.text name="occResourceOverview.title"/></title>
    <meta name="resource" content="${view.resource.title}"/>
    <meta name="submenu" content="manage_resource"/>
	<@s.head theme="ajax" debug="false"/>
</head>

<h2>Import Mapping for ${view.extension.name}</h2>

<@s.url id="editMappingSourceUrl" action="editMappingSource" includeParams="all">
</@s.url>

<#if columnOptions??>
	<ul class="actionmenu">
		<li><a href="${editMappingSourceUrl}">configure import source</a></li>
		<li><a onclick="Element.hide('uploadpreview'); Effect.toggle('sourcepreview', 'blind', { duration: 0.3 }); sourcePreview(); return false;">view source</a></li>
		<li>
			<@s.a targets="uploadpreview" theme="ajax" formid="mappingForm" form="mappingForm" onclick="Element.hide('sourcepreview'); Effect.toggle('uploadpreview', 'blind', { duration: 0.3 }); return false;">preview mapping</@s.a>
		</li>
		
	</ul>
	
	<div id="sourcepreview" style="display:none">
		Retrieving source data ...
	</div>
	
	<div id="uploadpreview" style="display:none">
		Retrieving mapping preview ...<br/>
		<p class="reminder">Not implemented yet, sorry!</p>
	</div>
	
<script>
var previewLoaded=0;
function sourcePreview(){
	if (previewLoaded<1){
		var url = '<@s.url action="sourcePreview.html" namespace="/ajax"/>';
		var params = { mapping_id: ${mapping_id}, resource_id: ${resource_id} }; 
		var target = 'sourcepreview';	
		var myAjax = new Ajax.Updater(target, url, {method: 'get', parameters: params});
		previewLoaded=1;
	}
};
</script>	

	<@s.form id="mappingForm" action="saveMappingProperties" method="post">
        <@s.hidden key="mapping_id"/>
	    <@s.hidden key="resource_id"/>
	    
		<div class="break"/>
        <@s.submit cssClass="button" key="button.save" theme="simple"/>
        <@s.submit cssClass="button" name="cancel" key="button.done" theme="simple"/>

		<h2>Configure Basic Properties</h2>
	 	<@s.select key="view.coreIdColumn.columnName" required="true"
			headerKey="Select local identifier for core record" emptyOption="false" 
			list="columnOptions" />
			
		<#if view.isCore()>
		 	<@s.select key="view.guidColumn.columnName" required="false" emptyOption="true" 
				list="columnOptions" />
		 	<@s.select key="view.linkColumn.columnName" required="false" emptyOption="true" 
				list="columnOptions" />
	 	</#if>
	 	
	 	<br/>
	 	
	    <li class="buttonBar bottom">
	        <@s.submit cssClass="button" key="button.save" theme="simple"/>
		    <#if (view.id)??>
		        <@s.submit cssClass="button" name="delete" key="button.delete" onclick="return confirmDelete('import source')" theme="simple"/>
		    </#if>
	        <@s.submit cssClass="button" name="cancel" key="button.done" theme="simple"/>
	    </li>

		<div class="break"/>
	    <#list mappings as m> 
		  <div class="mapping">
			<fieldset>
			<legend>${m.property.name}<#if m.property.link??> <a href="${m.property.link}" target="_blank">(about)</a> </#if></legend>
					<div class="left">
						<@s.select style="display: inline" name="mappings[${m_index}].column.columnName" required="${m.property.required?string}"
							list="mapOptions[${m.property.id?c}]" value="mappings[${m_index}].column.columnName"
							headerKey="" emptyOption="true"/>
					</div>
					<div>
				        <@s.textfield  name="mappings[${m_index}].value" value="${m.value!}" cssClass="large"/>  
					</div>
			</fieldset>
		  </div>
	    </#list>
	 
		<div class="break"/>
        <@s.submit cssClass="button" key="button.save" theme="simple"/>
        <@s.submit cssClass="button" name="cancel" key="button.done" theme="simple"/>
	 
	</@s.form> 

<#else>
	<#-- import source doesnt work -->
	<p class="reminder">There is no working import source configured. <br/>
		Please <a href="${editMappingSourceUrl}">configure a source</a> first.
	</p>
</#if>
