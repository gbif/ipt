<head>
    <title><@s.text name="occResourceOverview.title"/></title>
    <meta name="resource" content="${view.resource.title}"/>
    <meta name="submenu" content="manage"/>
    <script>
    	function copySQL(sql){
    		$('mappingSourceSql').value=sql;
    	}
    </script>
</head>

<h2>Import Source for ${view.extension.name}</h2>

<@s.url id="editResourceConnectionUrl" action="editResourceConnection" includeParams="all">
</@s.url>

<#-- See if file or sql was used before -->
<#if resource.hasDbConnection() && !view.isMappedToFile()>
	<#assign sourceDivId="dbsource" dbsourceStyle="" filesourceStyle="display:none">
<#else>
	<#assign sourceDivId="filesource" dbsourceStyle="display:none" filesourceStyle="">
</#if>
			
<div id="dbsource" style="${dbsourceStyle}">
	<#if resource.hasDbConnection()>
	  <@s.form action="saveMappingSource" method="post">
	    <@s.hidden key="resource_id"/>
	    <@s.hidden key="mapping_id"/>
	    <@s.hidden key="extension_id"/>
	    	    
	    <@s.textarea id="mappingSourceSql" key="view.sourceSql" required="true" cssClass="text xlarge slim"/>
	    <#if existingDbMappings??>
		    <span>
		    Copy already existing sources
			<ul class="actionmenu">
			  <#list existingDbViews as v>
		    	<li><a href="JavaScript:copySQL('${v.sourceSql}');return false;">${v.extension.name}</a></li>
		      </#list>
		    </ul>
		    </span>
		</#if>
	    <br/>
	    
	    <@s.submit cssClass="button" key="button.save" theme="simple"/>
	    <#if (view.id)??>
	        <@s.submit cssClass="button" name="delete" key="button.delete" onclick="return confirmDelete('import source')" theme="simple"/>
		</#if>
	    <@s.submit cssClass="button" name="cancel" key="button.done" theme="simple"/>
	
	  </@s.form>
	<#else>
		<p class="reminder">There is no working database connection configured. <br/>
			If you want to upload data from a database, please <a href="${editResourceConnectionUrl}">configure a connection</a> first.
		</p>
	</#if>
	<p>
		Want to upload a <a onclick="Element.hide('dbsource'); Effect.toggle('filesource', 'blind', { duration: 0.3 }); return false;">file</a> instead?
	</p>
</div>


<div id="filesource" style="${filesourceStyle}">
	<#if view.isMappedToFile()>
		<@s.form><@s.label key="view.sourceFileLocation"/></@s.form>
	</#if>
	<@s.form action="uploadMappingSource" enctype="multipart/form-data" method="post" validate="true" id="uploadForm">
	    <@s.hidden key="resource_id"/>
	    <@s.hidden key="extension_id"/>
	    <@s.hidden key="mapping_id"/>
	    <li class="info">
	        Please upload a tab delimited file to base the mapping on.
	    </li>
	    <@s.file name="file" key="view.selectUploadFile" cssClass="text file" required="true"/>
	    <li class="buttonBar bottom">
	        <@s.submit key="button.upload" name="upload" cssClass="button"/>
		    <#if (view.id)??>
		        <@s.submit cssClass="button" name="delete" key="button.delete" onclick="return confirmDelete('import source')" theme="simple"/>
			</#if>
		    <@s.submit name="cancel" key="button.cancel" theme="simple" cssClass="button" />
	    </li>
	</@s.form>
	<p>
		Want to upload from a <a onclick="Element.hide('filesource'); Effect.toggle('dbsource', 'blind', { duration: 0.3 }); return false;">database</a> instead?
	</p>
</div>


<div id="viewBasics">
<#if columnOptions??>
	<#-- import source is configured -->
	<h2>Assign Basic Record Properties</h2>
	<@s.form action="saveMappingSource" method="post" validate="true">
	    <li style="display: none">
	        <@s.hidden key="mapping_id"/>
		    <@s.hidden key="resource_id"/>
	    </li>
	
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

	</@s.form> 
</#if>
</div>

<br />
