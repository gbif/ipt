<head>
    <title><@s.text name="sources.heading"/></title>
    <meta name="resource" content="<@s.property value="resource.title"/>"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
	<meta name="heading" content="<@s.text name='sources.heading'/>"/>
	<style>
		.noBottomMargin{
			padding:0px;
			margin:0px;
		}
	</style>
	<script>
		function sourcePreview(sid){
			var url = '<@s.url action="sourcePreview" namespace="/ajax"/>';
			var params = { sid: sid }; 
			var target = '#sourcepreview';	
			ajaxHtmlUpdate(url, target, params);
		};
		function confirmDelete() {   
		    var msg = "Are you sure you want to delete this source? \n All associated property mappings will be lost too. If you want to upload a newer version of the file, simply upload a file with the exact same filename again.";
		    ans = confirm(msg);
		    if (ans) {
		        return true;
		    } else {
		        return false;
		    }
		}
		$(document).ready(function(){
		    $(".previewGlass").click(function (e) {
				e.preventDefault(); 
		      	$("#sourcepreview").slideDown("normal");
		    	var id = $(this).attr("id").substring(2);
		      	sourcePreview(id);
		    });
		    $("#sourcepreview").click(function () {
		      	$("#sourcepreview").slideUp("normal");
		    });
		});
		
    </script>
</head>

<div class="break10"></div>
<p class="explMt">Please upload your data as tab delimited text files or define a SQL view to pull it from a database.
You will need to map the main, core records to the <a href="http://darwincore.googlecode.com/svn/trunk/terms/index.htm#theterms">Darwin Core terms</a> in the next step, 
so if you can, please adjust your source data accordingly.
For the main "core" records, please also make sure they have a unique identifier per row.
<br/>You can define as many sources as you like and you will be able to upload additional information to complement a core darwin core record.
For this to work, your additional sources need to refer to your core identifiers, i.e. have a foreign key. They dont need their own identifier though.
<br/>Files currently cannot be larger than 100MB, but you can compress 1 or more files using zip. They will be unpacked on the server automatically.  
</p>

<h2><@s.text name="sources.filesources"/></h2>
<div class="horizontal_dotted_line_large_soft"></div>
  <#list fileSources as fs>
	<!-- source form -->
	<fieldset class="noBottomMargin">
	<@s.form action="updateSourceFile" method="post">
	  <@s.hidden key="resource_id"/>
	  <@s.hidden key="sid" value="${fs.id}"/>
		<div class="newline">
			<div class="left">
				<img id="sp${fs.id}" class="previewGlass" src="<@s.url value="/images/glasses.png"/>" width="20" height="20"/>
				<strong>${fs.filename}</strong>				
				<span style="padding-left:10px">
					<#if (fs.fileSize<0)>
						FILE MISSING!
					<#else>
						[${fs.fileSizeInKB}kB<#if fs.dateUploaded??>, ${fs.dateUploaded?datetime}</#if>]
					</#if>
				</span>
		    	<span style="padding-left:20px"></span>
			 	<@s.checkbox key="sourceFile.headers" value="${fs.headers?string}" theme="simple"/>
			 	<span><@s.text name="source.headers"/></span>
			</div>
			
			<div class="right">
				<@s.submit cssClass="button right" key="button.delete" method="delete" onclick="return confirmDelete()" theme="simple"/>
				<@s.submit cssClass="button right" key="button.update" theme="simple"/>
			</div>
		</div>
	</@s.form>
	</fieldset>
  </#list>
	
<!-- source upload -->
<fieldset class="noBottomMargin">
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
						<img id="sp${id}" class="previewGlass" src="<@s.url value="/images/glasses.png"/>" width="20" height="20"/>
						<strong>${name!"???"}</strong> : 
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


<div class="break"></div>
<div id="sourcepreview" style="display:none">
	Retrieving source data ...
</div>	


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
