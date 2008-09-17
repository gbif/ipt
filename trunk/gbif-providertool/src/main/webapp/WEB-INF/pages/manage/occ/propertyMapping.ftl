<head>
    <title><@s.text name="occResourceOverview.title"/></title>
    <meta name="resource" content="${view.resource.title}"/>
    <meta name="submenu" content="manage"/>
</head>

<h2>Import Mapping for ${view.extension.name}</h2>

<@s.url id="editMappingSourceUrl" action="editMappingSource" includeParams="all">
</@s.url>

<#if columnOptions??>
	<ul class="actionmenu">
		<li><a href="${editMappingSourceUrl}">configure import source</a></li>
		<li><a onclick="Element.hide('preview'); Effect.toggle('sourcepreview', 'blind', { duration: 0.3 }); return false;">sourcedata</a></li>
		<li><a onclick="Element.hide('sourcepreview'); Effect.toggle('preview', 'blind', { duration: 0.3 }); return false;">preview mapping</a></li>
	</ul>
	
	<div id="sourcepreview" style="display:none">
		AJAX CALL TO BE DONE...
	</div>
	
	<div id="preview" style="display:none">
		AJAX CALL TO BE DONE...
	</div>

	<@s.form action="saveMappingProperties" method="post">
	    <li style="display: none">
	        <@s.hidden key="mapping_id"/>
		    <@s.hidden key="resource_id"/>
	    </li>
	
	 	<br/>
	    <li class="buttonBar bottom">
	        <@s.submit cssClass="button" key="button.save" theme="simple"/>
	        <@s.submit cssClass="button" name="cancel" key="button.done" theme="simple"/>
	    </li>
	 	<br/>

	    <#list mappings as m> 
			<@s.select label="${m.property.name}" name="mappings[${m_index}].column.columnName" required="${m.property.required?string}"
				list="mapOptions[${m.property.id?c}]"
				value="mappings[${m_index}].column.columnName"
				headerKey="" emptyOption="true"/>
	
	        <@s.textfield  
	            name="mappings[${m_index}].value"  
	            value="${m.value!}"/>  
			<#if m.property.link??>
		 		<a href="${m.property.link}" target="_blank">help</a>
			</#if>
	        <br/> 
	    </#list> 
	 
	 	<br/>
	
	    <li class="buttonBar bottom">
	        <@s.submit cssClass="button" key="button.save" theme="simple"/>
	        <@s.submit cssClass="button" name="cancel" key="button.done" theme="simple"/>
	    </li>
	 
	</@s.form> 

<#else>
	<#-- import source doesnt work -->
	<p class="reminder">There is no working import source configured. <br/>
		Please <a href="${editMappingSourceUrl}">configure a source</a> first.
	</p>
</#if>

<br />
