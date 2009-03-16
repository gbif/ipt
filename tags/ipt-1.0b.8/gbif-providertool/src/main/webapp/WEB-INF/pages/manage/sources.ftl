<head>
    <title><@s.text name="sources.heading"/></title>
    <meta name="resource" content="<@s.property value="resource.title"/>"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
	<meta name="heading" content="<@s.text name='sources.heading'/>"/>
    <script>
		function confirmDelete() {   
		    var msg = "Are you sure you want to delete this source? \n All associated property mappings will be lost too. If you want to upload a newer version of the file, simply upload a file with the exact same filename again.";
		    ans = confirm(msg);
		    if (ans) {
		        return true;
		    } else {
		        return false;
		    }
		}
    </script>
</head>

<div class="break10"></div>
<p class="explMt">Please upload your data as tab files or define a SQL view to pull it from a database.
You can define as many sources as you like, but there needs to be at least one.
Readily supported formats can be <a href="">found here</a>. 
If your data does not exactly match those formats you have the option to configure simple transformations and adjustments in the next configuration step.
</p>

<h2><@s.text name="sources.filesources"/></h2>
<div class="horizontal_dotted_line_large_soft"></div>
<fieldset class="noBottomMargin">
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
				<@s.submit cssClass="button right" key="button.delete" method="delete" onclick="return confirmDelete()" theme="simple"/>
			</div>
		</div>
	</@s.form>
	</@s.iterator>
	
	<div class="break2">
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


<div class="break"></div>
<p>
	SQL data sources are sql select statements to an external relational database (like views).
	The IPT can pull data from those databases when updating the internal cache.
</p>
<h2><@s.text name="sources.sqlsources"/></h2>
<div class="horizontal_dotted_line_large_soft"></div>
<fieldset class="noBottomMargin">
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
		<div class="right">
			<@s.submit cssClass="button" key="button.edit" theme="simple"/>
		</div>
	  <div>		
	</@s.form>

	<div class="break"></div>
	<#if resource.hasDbConnection()>
		<#if (sqlSources?size>0)>
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
					  <@s.submit cssClass="button" key="button.edit" theme="simple"/>
					</div>
				</@s.form>
				</div>
			</@s.iterator>
			<div class="break"></div>
		<#else>
			<div class="left">
				<p class="reminder">Please configure at least one sql view to start using this datasource</p>
				<#-- <@s.text name="sources.noDbConnection" /> -->
			</div>
		</#if>
		<div class="right">
			<@s.form action="editSource" method="get">
			  <@s.hidden key="resource_id"/>
			  <@s.hidden key="sid" value=""/>
			  <@s.submit cssClass="button" key="button.add" theme="simple"/>
			</@s.form>
		</div>
	</#if>
</fieldset>


<#if (fileSources?size>0) || (sqlSources?size>0)>
<div class="break">
<@s.form action="mappings" method="get">
	<@s.hidden key="resource_id"/>
	<div class="breakRight">
		<@s.submit cssClass="button" key="button.next" theme="simple"/>
	</div>
</@s.form>
</div>
</#if>


<div class="break20"></div>
