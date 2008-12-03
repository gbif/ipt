<head>
    <title><@s.text name="occResourceOverview.title"/></title>
    <meta name="resource" content="<@s.property value="resource.title"/>"/>
    <meta name="submenu" content="manage_resource"/>    
</head>


<p>Please upload your data as tab files or define a SQL view to pull it from a database.
You can define as many sources as you like, but there needs to be at least one.
Readily supported formats can be <a href="">found here</a>. 
If your data does not exactly match those formats you have the option to configure simple transformations and adjustments in the next configuration step.
</p>


<div class="break">
	<p>
	File data sources are tab delimited files with a maximum of 2MB currently (limit will be removed in final version).
	To upload a new file version of an existing source, simple upload a file with the same name as the existing one.
	</p>
</div>
<fieldset>
<legend><@s.text name="sources.filesources"/></legend>
	<@s.iterator value="fileSources" status="stat">
	<@s.form action="saveSource" method="post">
	  <@s.hidden key="resource_id"/>
	  <@s.hidden key="sid" value="${id}"/>
		<div class="newline">
			<div class="left">
				<strong>${stat.index+1}) ${filename}</strong>				
				<span>
					<#if (fileSize<0)>
						FILE MISSING!
					<#else>
						[${fileSizeInKB}kB<#if dateUploaded??>, ${dateUploaded?datetime}</#if>]
					</#if>
				</span>
			</div>
			<div class="right">
				<@s.submit cssClass="button right" key="button.delete" method="delete" onclick="return confirmDelete('file source')" theme="simple"/>
			</div>
		</div>
	</@s.form>
	</@s.iterator>
	
	<div class="break">
	  <@s.form action="uploadSource" enctype="multipart/form-data" method="post">
		<@s.hidden key="resource_id"/>
		<div class="left">
			<@s.file name="file" key="sources.selectSourceFile" cssClass="file tablefile" required="false"/>
		</div>
		<div class="right">
			<li class="wwgrp">
				<div class="wwlbl">&nbsp;</div>
				<@s.submit cssClass="button" key="button.upload" />
			</li>
		</div>
	  </@s.form>
	</div>
</fieldset>


<div class="break">
<p>
	SQL data sources are sql select statements to an external relational database (like views).
	The IPT can pull data from those databases when updating the internal cache.
</p>
</div>
<fieldset>
<legend><@s.text name="sources.sqlsources"/></legend>
	<div>
		<@s.form action="editConnection" method="get">
		  <@s.hidden key="resource_id"/>
		  	<div>
		  	<div class="left">
			<#if resource.hasDbConnection()>
				<@s.label key="resource.jdbcUrl" />
			<#else>
				<p class="reminder">
					<@s.text name="sources.noDbConnection" />
				</p>
			</#if>
			</div>
			<div>		
		  <@s.submit cssClass="button" key="button.edit" />
		  </div>
		  </div>
		</@s.form>
	</div>

	<div class="break">
		<#if resource.hasDbConnection() && (sqlSources?size>0)>
			<@s.iterator value="sqlSources" status="stat">
				<div class="newline">
				<@s.form action="editSource" method="get">
				  <@s.hidden key="resource_id"/>
				  <@s.hidden key="sid" value="${id}"/>
					<div class="left">
						<strong>${stat.index+1}) ${name!"???"}</strong> : 
						<span class="citation"><#if (sql?length>50)>${sql?substring(0, 50)}<#else>${sql}</#if></span>
					</div>
					<div class="right">
					  <@s.submit cssClass="button" key="button.edit"/>
					</div>
				</@s.form>
				</div>
			</@s.iterator>	
		</#if>
	</div>
	<div class="break">
		<#if resource.hasDbConnection()>
			<@s.form action="editSource" method="get">
			  <@s.hidden key="resource_id"/>
			  <@s.hidden key="sid" value=""/>
			  <@s.submit cssClass="button" key="button.add" />
			</@s.form>
		</#if>
	</div>
</fieldset>


<div class="break">
<@s.form action="transformations" method="get">
  <@s.hidden key="resource_id"/>
  <@s.submit cssClass="button" key="button.next" theme="simple"/>
</@s.form>
</div>
