<#include "/WEB-INF/pages/inc/header.ftl">
	<title>${ms.resource.title!ms.resource.shortname}</title>
	<style>
	.actions select{
		width: 125px;
	}
	</style>
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
	  <form action='metadata.do' method='get'>
		<input type='submit' name='edit' value='Edit' />
  	  </form>
  	</div>
  </div>
  <div class="body">
      	<div>
			<em>Title</em>:
			My full resource title 
      	</div>
      	<div>
			<em>Description</em>:
			My full resource description. A lot of text but probably truncated at some point... 
      	</div>
      	<div class="warn">
			The resource is missing mandatory metadata! Please enter at least the required fields before proceeding. 
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
  	 <#-- 
	  <form action='extension.do' method='post'>
		<input type='submit' name='generate' value='Generate' />
  	  </form>
  	  -->
  	</div>
  </div>
  <div class="body">
      	<div>
			Aaaaaaaaaa bbbb ccc d deee ee f f
      	</div>
      	<div class="details">
      		<table>
          		<tr><th>FILE species.txt</th><td>10MB, 137.889 rows, 7 columns. Apr 20, 2007 12:45:09 PM</td></tr>
          		<tr><th>FILE vernaculars.txt</th><td>4MB, 87.322 rows, 5 columns. Apr 20, 2007 12:52:19 PM</td></tr>
          		<tr><th>SQL taxa</th><td>db=pontaurus, 21 columns</td></tr>
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
  	 <#-- 
	  <form action='extension.do' method='post'>
		<input type='submit' name='generate' value='Generate' />
  	  </form>
  	  -->
  	</div>
  </div>
  <div class="body">
      	<div>
			Your mapping between the source data and Darwin Core terms.
      	</div>
      	<div class="details">
      		<table>
          		<tr><th>Taxon</th><td>7 terms mapped to species.txt</td></tr>
          		<tr><th>Vernacular</th><td>3 terms mapped to vernaculars.txt</td></tr>
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
  	 <#-- 
	  <form action='extension.do' method='post'>
		<input type='submit' name='generate' value='Generate' />
  	  </form>
  	  -->
  	</div>
  </div>
  <div class="body">
      	<div>
			XXXxxxxxxxxxxx yyxyyyyyy
      	</div>
      	<div class="details">
      		<table>
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
  	 <#-- 
  	 	AJAXY ???
  	  -->
	  <form>
	    <#assign options={"ahahn@gbif.org":"Andrea Hahn", "cip@gbif.org":"Cip, the greatest of all Sys Admins"} value="" />
	    <select name="manager" id="manager" size="1">
	      <option value="">Select Manager</option>
	    <#list options?keys as val>
	      <option value="${val}">${options[val]}</option>
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
          		<tr><th>Creator</th><td>${ms.resource.creator.firstname!ms.resource.creator.lastname!"?"}, ${ms.resource.creator.email}</td></tr>
          		<tr><th>Manager</th><td>Tim, trobertson@gbif.org</td></tr>
          		<tr><th>Manager</th><td>Jose, jcuadra@gbif.org</td></tr>
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


<#include "/WEB-INF/pages/inc/footer.ftl">
