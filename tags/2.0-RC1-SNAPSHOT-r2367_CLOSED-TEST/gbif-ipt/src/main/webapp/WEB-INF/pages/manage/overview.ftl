<#include "/WEB-INF/pages/inc/header.ftl">
	<script type="text/javascript" src="${baseURL}/js/jconfirmaction.jquery.js"></script>
	<style>
	img.info{
		position: relative;
		top: 4px;
	}
	.actions select, .actions input[type="file"]{
		width: 200px;
	}
	div.definition div.title{
		width: 30%;
	}
	div.title input[type="submit"], div.title button{
		float: right;
		margin-right: 5px;
	}
	div.definition div.body{
		width: 68%;
	}
	span.rightHead{
		float:right;
		margin-right: 10px;
		font-style: italic;
	}
	</style>
<script type="text/javascript">
$(document).ready(function(){
	$('.confirm').jConfirmAction({question : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});
});
</script>

 <#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<h1>${resource.title!resource.shortname}</h1>
<p>This is the overview page for the <em>${resource.shortname}</em> resource.
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
	    <input name="r" type="hidden" value="${resource.shortname}" />
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
      		${resource.description!"No Description entered yet"}
      	</div>
      	<div class="details">
      		<table>
          		<tr><th>Keywords</th><td>${resource.eml.subject!}</td></tr>
          		<tr><th>Taxon Coverage</th><td><#list resource.eml.taxonomicCoverages as tc><#list tc.taxonKeywords as k>${k.scientificName!k.commonName!}<#if !k_has_next>; </#if></#list></#list></td></tr>
          		<tr><th>Spatial Coverage</th><td><#list resource.eml.geospatialCoverages as geo><#list geo.keywords as k>${k!}<#if !k_has_next>;</#if> </#list></#list></td></tr>
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
	    <input name="r" type="hidden" value="${resource.shortname}" />
	    <input name="validate" type="hidden" value="false" />
	    <@s.file name="file" key="manage.resource.create.file" />
       	<@s.submit name="add" key="button.add"/>
  	  </form>
  	</div>
  </div>
  <div class="body">
      	<div>
			Your data sources for generating a Darwin Core archive. 
			You can upload delimited text files (e.g. csv, tab or using any other delimiter) either directly or compressed (zip or gzip).
			Alternatively you can configure SQL views to databases in your local network.
			To create a new sql source press <@s.text name="button.add"/>, to (re)upload a file please select the local file before hitting <@s.text name="button.add"/>. 
      	</div>
      	<div class="details">
      		<table>
      		  <#list resource.sources as src>
      		  	<tr><th>
      			<#if src.rows?exists>
          		 ${src.name} [file]</th><td>${src.fileSizeFormatted}, ${src.rows} rows, ${src.columns} columns. ${(src.lastModified?datetime?string)!}
          		<#else>
          		 ${src.name} [sql]</th><td>db=${src.database!"..."}, ${src.columns} columns. 
          		</#if>
          		<#if !src.readable><img src="${baseURL}/images/warning.gif" /></#if> 
          		<a href="source.do?r=${resource.shortname}&id=${src.name}"><button>Edit</button></a>
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
  	  <#if (potentialExtensions?size>0)>
	  <form action='mapping.do' method='post'>
	    <input name="r" type="hidden" value="${resource.shortname}" />
	    <select name="id" id="rowType" size="1">
	    <#list potentialExtensions as e>
	      <option value="${e.rowType}">${e.title}</option>
	    </#list>
		</select>
		<input type='submit' name='add' value='Add' />
  	  </form>
  	  <#else>
  	  	<#if !resource.core?exists>
	  	<div class="warn">
			No DwC extensions installed. 
			<br/>Please contact your admin! 
	  	</div>
  	  	</#if>
  	  </#if>
  	</div>
  </div>
  <div class="body">
      	<div>
			Your mapping between the source data and Darwin Core terms.
      	</div>
      	<div class="details">
      		<table>
          		<#if resource.core?exists>
          		<tr><th>${resource.core.extension.title}</th><td>${resource.core.fields?size} terms mapped to source ${resource.core.source.name}
          		<a href="mapping.do?r=${resource.shortname}&id=${resource.core.extension.rowType}"><button>Edit</button></a>
          		</td></tr>
          		</#if>
      		  <#list resource.extensions as m>
          		<tr><th>${m.extension.title}</th><td>${m.fields?size} terms mapped to source ${(m.source.name)!}
          		<a href="mapping.do?r=${resource.shortname}&id=${m.extension.rowType}"><button>Edit</button></a>
          		</td></tr>
          	  </#list>
      		</table>
      	</div>
  </div>
</div>

<div class="definition" id="visibility">	
  <div class="title">
  	<div class="head">
        Visibility
        <em class="<#if resource.status=="PRIVATE">RED<#else>green</#if>"><@s.text name="resource.status.${resource.status?lower_case}"/></em>
  	</div>
  	<div class="actions">
	  <form action='resource-visibility.do' method='post'>
	    <input name="r" type="hidden" value="${resource.shortname}" />
	    <#if resource.status=="PUBLIC">
	    	<#if currentUser.hasRegistrationRights() && (organisations?size>0)>
		    <select name="id" id="org" size="1">
		    <#list organisations as o>
		      <option value="${o.key}">${o.alias!o.name}</option>
		    </#list>
			</select>
	       	<@s.submit cssClass="confirm" name="publish" key="button.register" disabled="${missingRegistrationMetadata?string}"/>
	       	<#if missingRegistrationMetadata>
	       		<div class="warn">The <a href="${baseURL}/manage/metadata-basic.do?r=${resource.shortname}">resource's basic metadata</a> should be saved and the EML & Archive files need to be generated prior to registering to the GBIF Network</div>
	       	</#if>
	       	</#if>
	       	<@s.submit cssClass="confirm" name="unpublish" key="button.private" />
		<#else>
		    <#if resource.status=="PRIVATE">
	       	<@s.submit name="publish" key="button.public"/>
			</#if>
		    <#if resource.status=="REGISTERED">
	       	<@s.submit name="update" key="button.update"/>
			</#if>			
		</#if>
  	  </form>
  	</div>
  </div>
  <div class="body">
      	<div>
			<@s.text name="manage.resource.status.intro.${resource.status?lower_case}"/> 
		    <#if resource.status=="PUBLIC">
			  <#if currentUser.hasRegistrationRights()>
				<@s.text name="manage.resource.status.registration.intro"/>
			  <#else>
			    <div>
		          	<img class="info" src="${baseURL}/images/info.gif" /> 
					<em><@s.text name="manage.resource.status.registration.forbidden"/></em>
	      		</div>
			  </#if>
		      <#if missingRegistrationMetadata>
			    <div>
		          	<img class="info" src="${baseURL}/images/info.gif" /> 
					<em>In order to register this resource with GBIF you must provide more metadata (ask Jose what is required)!</em>
	      		</div>
		      </#if>
		      <#if organisations?size==0>
			    <div>
		          	<img class="info" src="${baseURL}/images/info.gif" /> 
					<em>In order to register this resource with GBIF your IPT admin must first associate organisations with this IPT!</em>
	      		</div>
			  </#if>
		    </#if>
      	</div>
      	<div class="details">
      		<table>
		      	<#if resource.status=="REGISTERED" && resource.key??>
	          		<tr><th>Resource Key</th><td>${resource.key} <a href="${cfg.registryUrl}/browse/agent?uuid=${resource.key}">GBRDS</a></td></tr>
	          		<#if resource.organisation?exists>
	          		<tr><th>Organisation</th><td>${resource.organisation.name!}</td></tr>
	          		<tr><th>Organisation Contact</th><td>${resource.organisation.primaryContactName!}, ${resource.organisation.primaryContactEmail!}</td></tr>
	          		<tr><th>Organisation Key</th><td>${resource.organisation.key!}</td></tr>
	          		<tr><th>Endorsing Node</th><td>${resource.organisation.nodeName!}</td></tr>
	          		</#if>
          		</#if>
      		</table>
      	</div>
  </div>
</div>

<div class="definition" id="publish">	
  <div class="title">
  	<div class="head">
        Published Release
  	</div>
   	<div class="actions">
   	  <#if !missingMetadata>
	  <form action='publish.do' method='post'>
	    <input name="r" type="hidden" value="${resource.shortname}" />
	    <@s.submit name="publish" key="button.publish" />
  	  </form>
  	  </#if>
  	</div>
  </div>
  <div class="body">
      	<div>
			When publishing a new release a new EML version and a darwin core archive (DWCA) will be created. <br/>
			A DWCA bundles all data sources with mappings and metadata in one zipped archive.
      	</div>
   	  <#if missingMetadata>
      	<div>
          	<img class="info"src="${baseURL}/images/info.gif" /> 
			<em>Before you can publish a resource you have to provide the basic metadata first!</em>
      	</div>
  	  </#if>
      	<div class="details">
      		<table>
			  	<#if resource.lastPublished??>
          		 <tr><th>Last Publication</th><td>Version ${resource.eml.emlVersion} from ${resource.lastPublished?date?string.medium}</td></tr>
			  	 <#if (resource.recordsPublished>0)>
          		  <tr><th>Archive</th><td><a href="${baseURL}/archive.do?r=${resource.shortname}">download</a>, ${resource.recordsPublished} records </td></tr>
			  	 </#if>
          		 <tr><th>EML</th><td><a href="${baseURL}/eml.do?r=${resource.shortname}">download</a> <a href="${baseURL}/resource.do?r=${resource.shortname}">view</a></td></tr>
			  	</#if>
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
  	  <#if (potentialManagers?size>0)>
	  <form action='resource-addmanager.do' method='post'>
	    <input name="r" type="hidden" value="${resource.shortname}" />
	    <select name="id" id="manager" size="1">
	      <option value=""></option>
	    <#list potentialManagers as u>
	      <option value="${u.email}">${u.name}</option>
	    </#list>
		</select>
		<input type='submit' name='add' value='Add' />
  	  </form>
  	  </#if>
  	</div>
  </div>
  <div class="body">
      	<div>
			Resources can be managed by several managers. You can grant other managers permission to modify ${resource.shortname} 
      	</div>
      	<div class="details">
      		<table>
          		<tr><th>Creator</th><td>${resource.creator.name}, ${resource.creator.email}</td></tr>
          		<#list resource.managers as u>
          		<tr><th>Manager</th><td>${u.name}, ${u.email} <a class="confirm" href="resource-delmanager.do?r=${resource.shortname}&id=${u.email}"><button>Delete</button></a></td></tr>
	    		</#list>
      		</table>
      	</div>
  </div>
</div>

<div>
  <form action='resource-delete.do' method='post'>
    <input name="r" type="hidden" value="${resource.shortname}" />
   	<@s.submit cssClass="confirm" name="delete" key="button.delete"/>
  </form>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
