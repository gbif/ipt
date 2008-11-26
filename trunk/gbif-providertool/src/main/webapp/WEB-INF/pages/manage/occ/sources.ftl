<head>
    <title><@s.text name="occResourceOverview.title"/></title>
    <meta name="resource" content="<@s.property value="resource.title"/>"/>
    <meta name="submenu" content="manage_resource"/>    
</head>


<p>Please upload your data as tab files or define a SQL view to pull it from a database.
If you can, please provide your data in this format. Otherwise you have the option
to do simple manipulations to your data in the next configuration step.</p>

<div>
<legend><@s.text name="fileSources.list"/></legend>
<table class="lefthead">
	<tr>
		<th>Filename</th>
		<th>Uploaded</th>
		<th>Columns</th>
		<th></th>
	</tr>
	<#list fileSources as s>
	<@s.form action="deleteSource" method="post">
	  <@s.hidden key="resource_id"/>
		<tr>
			<td>${s.filename}</td>
			<td>${s.dateUploaded}</td>
			<td>?</td>
			<td><@s.submit cssClass="button" key="button.delete"/></td>
		</tr>
	</@s.form>
	</#list>
</table>
</div>
    

<div>
<legend><@s.text name="sqlSources.list"/></legend>
<@s.form action="editResourceConnection" method="get">
  <@s.hidden name="resource_id" value="${resource.id}"/>
	<#if resource.hasDbConnection()>
		<@s.label key="occResource.hasDbConnection" value="${resource.jdbcUrl}"/>
	<#else>
		<p class="reminder"><@s.text name="occResource.noDbConnection" /></p>
	</#if>
    <@s.submit cssClass="button" key="button.edit"/>
</@s.form>
</div>


<#if resource.hasDbConnection()>
<div>
	<table class="lefthead">
		<tr>
			<th>Name</th>
			<th>SQL</th>
			<th>Columns</th>
			<th></th>
		</tr>
		<#list sqlSources as s>
		<@s.form action="deleteSource" method="post">
		  <@s.hidden key="resource_id"/>
			<tr>
				<td>${s.name}</td>
				<td>${s.sql}</td>
				<td>?</td>
				<td><@s.submit cssClass="button" key="button.delete"/></td>
			</tr>
			<tr>
				<td cols="3"><@s.textarea key="sqlSources[${s_index}].sql" title="sql" cssClass="text xlarge"/></td>
				<td>
					<@s.submit cssClass="button" key="button.save"/>
				</td>
			</tr>
		</@s.form>
		</#list>
	</table>
</div>
</#if>


