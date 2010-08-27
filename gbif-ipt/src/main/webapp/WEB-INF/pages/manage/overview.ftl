<#include "/WEB-INF/pages/inc/header.ftl">
	<script type="text/javascript" src="${baseURL}/js/jconfirmaction.jquery.js"></script>
	<style>
	.actions select{
		width: 200px;
	}
	div.definition div.title{
		width: 30%;
	}
	div.title input[type="submit"]{
		float: right;
		margin-right: 5px;
	}
	div.definition div.body{
		width: 68%;
	}
	</style>
<script type="text/javascript">
$(document).ready(function(){
	$('.confirm').jConfirmAction({question : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});
});
</script>

<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<h1>${ms.resource.title!ms.resource.shortname}</h1>
<p>This is the overview page for the <em>${ms.resource.shortname}</em> resource.
Please start by filling in at least the mandatory metadata before you can upload and map data in order to generate darwin core archives.
By default a resource is private to the managers. Once published to GBIF you can only remove the resource, but not revert to a private state.
</p>


<div class="definition" id="metadata">	
  <div class="title">
  	<div class="head">
        Metadata
  	</div>
  	<div class="actions">
	  <form action='metadata-basic.do' method='get'>
       	<@s.submit name="edit" key="button.edit"/>
  	  </form>
  	</div>
  	<#if missingMetadata>
  	<div class="warn">
		The resource is missing mandatory metadata! 
  	</div>
  	</#if>
  </div>
  <div class="body">
      	<div>
      		${ms.resource.description!"No Description entered yet"}
      	</div>
      	<div class="details">
      		<table>
          		<tr><th>Keywords</th><td>Taxonomy, Europe, Plants</td></tr>
          		<tr><th>Spatial Coverage</th><td>Europe</td></tr>
          		<tr><th>Taxon Coverage</th><td>Fabaceae, Poaceae, Rosaceae, Fabaceae, Poaceae, Rosaceae</td></tr>
          		<tr><th>EML Download</th><td><a href="${baseURL}/eml.do?resource=${ms.resource.shortname}">${baseURL}/eml.do?resource=${ms.resource.shortname}</a></td></tr>
      		</table>
      	</div>
  </div>
</div>

<div class="definition" id="sources">	
  <div class="title">
  	<div class="head">
        Source Data
  	</div>
  	<div class="actions">
	  <form action='addsource.do' method='post' enctype="multipart/form-data">
	    <input name="validate" type="hidden" value="false" />
	    <@s.file name="file" key="manage.resource.create.file" />
       	<@s.submit name="add" key="button.add"/>
  	  </form>
  	</div>
  </div>
  <div class="body">
      	<div>
			Your data sources for generating a Darwin Core archive. You can upload text files (e.g. csv or tab delimited) or configure SQL views to databases in your local network.
			To create a new sql source press <em><@s.text name="button.add"/></em>, to (re)upload a file please select the local file before hitting <@s.text name="button.add"/>. 
      	</div>
      	<div class="details">
      		<table>
      		  <#list ms.config.sources as src>
      		  	<tr><th>
      			<#if src.rows?exists>
          		 ${src.name} [file]</th><td>${src.fileSizeFormatted}, ${src.rows} rows, ${src.columns} columns. ${(src.lastModified?datetime?string)!}
          		<#else>
          		 ${src.name} [sql]</th><td>db=${src.database!"..."}, ${src.columns} columns. 
          		</#if>
          		<#if !src.readable><img src="${baseURL}/images/warning.gif" /></#if> 
          		<a href="source.do?id=${src.name}"><button class="small">Edit</button></a>
          		</td></tr>
          	  </#list>
      		</table>
      	</div>
  </div>
</div>

<div class="definition" id="mappings">	
  <div class="title">
  	<div class="head">
        DwC Mappings
  	</div>
  	<div class="actions">
	  <form action='mapping.do' method='post'>
	    <select name="id" id="rowType" size="1">
	    <#list potentialExtensions as e>
	      <option value="${e.rowType}">${e.title}</option>
	    </#list>
		</select>
		<input type='submit' name='add' value='Add' />
  	  </form>
  	</div>
  </div>
  <div class="body">
      	<div>
			Your mapping between the source data and Darwin Core terms.
      	</div>
      	<div class="details">
      		<table>
          		<#if ms.config.core?exists>
          		<tr><th>${ms.config.core.extension.title}</th><td>${ms.config.core.fields?size} terms mapped to source ${ms.config.core.source.name}
          		<a href="mapping.do?id=${ms.config.core.extension.rowType}"><button class="small">Edit</button></a>
          		</td></tr>
          		</#if>
      		  <#list ms.config.extensions as m>
          		<tr><th>${m.extension.title}</th><td>${m.fields?size} terms mapped to source ${(m.source.name)!}
          		<a href="mapping.do?id=${m.extension.rowType}"><button class="small">Edit</button></a>
          		</td></tr>
          	  </#list>
      		</table>
      	</div>
  </div>
</div>

<div class="definition" id="publish">	
  <div class="title">
  	<div class="head">
        Publishing State
  	</div>
  	<div class="actions">
	  <form action='resource-publish.do' method='post'>
	    <#if ms.resource.status=="PUBLIC">
		    <select name="id" id="org" size="1">
		    <#list organisations as o>
		      <option value="${o.key}">${o.name}</option>
		    </#list>
			</select>
	       	<@s.submit cssClass="confirm" name="publish" key="button.register" disabled="${missingBasicMetadata?string}"/>
	       	<#if missingBasicMetadata>
	       		<div class="warn">The <a href="${baseURL}/manage/metadata-basic.do">resource's basic metadata</a> should be saved before registering this resource to the GBIF network.</div>
	       	</#if>
		<#else>
		    <#if ms.resource.status=="PRIVATE">
	       	<@s.submit cssClass="confirm" name="publish" key="button.publish"/>
			</#if>
		</#if>
  	  </form>
  	</div>
  </div>
  <div class="body">
      	<div>
			<strong><@s.text name="resource.status.${ms.resource.status?lower_case}"/></strong>
      	</div>
      	<div>
			<@s.text name="manage.resource.status.intro.${ms.resource.status?lower_case}"/>
		    <#if ms.resource.status=="PUBLIC">
		    <a class="confirm" href="resource-unpublish.do"><@s.text name="manage.resource.status.restrict"/></a>
		    </#if>
      	</div>
      	<div class="details">
      		<table>
		      	<#if ms.resource.status=="REGISTERED">
	          		<tr><th>Resource Key</th><td>${ms.resource.key!}</td></tr>
	          		<#if ms.resource.organisation?exists>
	          		<tr><th>Organisation</th><td>${ms.resource.organisation.name!}</td></tr>
	          		<tr><th>Organisation Contact</th><td>${ms.resource.organisation.primaryContactName!}, ${ms.resource.organisation.primaryContactEmail!}</td></tr>
	          		<tr><th>Organisation Key</th><td>${ms.resource.organisation.key!}</td></tr>
	          		<tr><th>Endorsing Node</th><td>${ms.resource.organisation.nodeName!}</td></tr>
	          		</#if>
          		</#if>
      		</table>
      	</div>
  </div>
</div>

<div class="definition" id="archive">	
  <div class="title">
  	<div class="head">
        DwC Archive
  	</div>
  	<div class="actions">
  	 <#-- 
  	  -->
	  <form action='archive.do' method='post'>
		<input type='submit' name='generate' value='Generate' />
  	  </form>
  	</div>
  </div>
  <div class="body">
      	<div>
			A darwin core archive bundles all data sources with mappings and metadata in one archive.
      	</div>
      	<div class="details">
      		<table>
          		<tr><th>Generated</th><td>Apr 20, 2007 12:45:09 PM</td></tr>
          		<tr><th>Download</th><td><a href="${baseURL}/archive.do?resource=${ms.resource.shortname}">${baseURL}/archive.do?resource=${ms.resource.shortname}</a></td></tr>
      		</table>
      	</div>
  </div>
</div>

<div class="definition" id="managers">	
  <div class="title">
  	<div class="head">
        Resource Managers
  	</div>
  	<div class="actions">
	  <form action='resource-addmanager.do' method='post'>
	    <select name="id" id="manager" size="1">
	      <option value=""></option>
	    <#list potentialManagers as u>
	      <option value="${u.email}">${u.name}</option>
	    </#list>
		</select>
		<input type='submit' name='add' value='Add' />
  	  </form>
  	</div>
  </div>
  <div class="body">
      	<div>
			Resources can be managed by several managers. You can grant other managers permission to modify ${ms.resource.shortname} 
      	</div>
      	<div class="details">
      		<table>
          		<tr><th>Creator</th><td>${ms.resource.creator.name}, ${ms.resource.creator.email}</td></tr>
          		<#list ms.resource.managers as u>
          		<tr><th>Manager</th><td>${u.name}, ${u.email} <a class="confirm" href="resource-delmanager.do?id=${u.email}"><button class="small">Delete</button></a></td></tr>
	    		</#list>
      		</table>
      	</div>
  </div>
</div>

<div>
  <form action='resource-delete.do' method='post'>
   	<@s.submit cssClass="confirm" name="delete" key="button.delete"/>
  </form>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
