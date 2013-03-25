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
		    var msg = "<@s.text name='sources.confirmdelete'/>";
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
<p class="explMt">
<@s.text name='sources.instructions.1'/> <a href="http://darwincore.googlecode.com/svn/trunk/terms/index.htm#theterms"><@s.text name='sources.dwcterms'/></a> <@s.text name='sources.instructions.2'/>  
</p>

<h2><@s.text name="sources.filesources"/></h2>
<div class="horizontal_dotted_line_large_soft"></div>
  <#list fileSources as fs>
	<!-- source form -->
	<fieldset class="noBottomMargin">
	<@s.form action="updateSourceFile" method="post">
	  <@s.hidden key="resourceId"/>
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
		<@s.hidden key="resourceId"/>
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
	<@s.text name='sources.explanation'/>
</p>
<h2><@s.text name="sources.sqlsources"/></h2>
<div class="horizontal_dotted_line_large_soft"></div>
<fieldset class="noBottomMargin">
	<@s.form action="editConnection" method="get">
	  <@s.hidden key="resourceId"/>
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
				  <@s.hidden key="resourceId"/>
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
				<p class="reminder"><@s.text name='sources.atleastone'/></p>
				<#-- <@s.text name="sources.noDbConnection" /> -->
			</div>
		</#if>
		<div class="right">
			<@s.form action="editSource" method="get">
			  <@s.hidden key="resourceId"/>
			  <@s.hidden key="sid" value=""/>
			  <@s.submit cssClass="button" key="button.add" theme="simple"/>
			</@s.form>
		</div>
	</#if>
</fieldset>


<div class="break"></div>
<div id="sourcepreview" style="display:none">
	<@s.text name='sources.retrieving'/>
</div>	


<#if (fileSources?size>0) || (sqlSources?size>0)>
<div class="break">
<@s.form action="mappings" method="get">
	<@s.hidden key="resourceId"/>
	<div class="breakRight">
		<@s.submit cssClass="button" key="button.next" theme="simple"/>
	</div>
</@s.form>
</div>
</#if>


<div class="break20"></div>
