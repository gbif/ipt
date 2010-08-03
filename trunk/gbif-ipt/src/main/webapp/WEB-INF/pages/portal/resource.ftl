<#include "/WEB-INF/pages/inc/header.ftl">
	<title>${resource.title!resource.shortname!}</title>
	<style>
	div.definition div.title{
		width: 20%;
	}
	div.definition div.body{
		width: 78%;
	}
	</style>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1>${resource.title!resource.shortname}</h1>
<p>${resource.description!"No Description available"}</p>

<div class="definition" id="metadata">	
  <div class="title">
  	<div class="head">
        Summary
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<tr><th>Keywords</th><td>Taxonomy, Europe, Plants</td></tr>
          		<tr><th>EML</th><td><a href="${baseURL}/eml.do?id=${resource.shortname}">EML</a></td></tr>
          		<tr><th>DwC archive</th><td><a href="${baseURL}/archive.do?id=${resource.shortname}">DwC-A</a> from Apr 20, 2007 12:45:09 PM</td></tr>
      		   	<#if resource.status=="REGISTERED">
	          		<tr><th>GBIF Registration</th><td><a href="http://gbrdsdev.gbif.org/browse/agent?uuid=${resource.key}">${resource.key}</a></td></tr>
	          		<#if resource.organisation?exists>
	          		<tr><th>Organisation</th><td><a href="http://gbrdsdev.gbif.org/browse/agent?uuid=${resource.organisation.key}">${resource.organisation.name!}</a> </td></tr>
	          		<tr><th>Endorsing Node</th><td>${resource.organisation.nodeName!}</td></tr>
	          		</#if>
          		</#if>
      		</table>
      	</div>
  </div>
</div>

<div class="definition">	
  <div class="title">
  	<div class="head">
        Metadata II
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<tr><th>Spatial Coverage</th><td>Europe</td></tr>
          		<tr><th>Taxon Coverage</th><td>Fabaceae, Poaceae, Rosaceae, Fabaceae, Poaceae, Rosaceae</td></tr>
          		<tr><th>Spatial Coverage</th><td>Europe</td></tr>
          		<tr><th>Taxon Coverage</th><td>Fabaceae, Poaceae, Rosaceae, Fabaceae, Poaceae, Rosaceae</td></tr>
          		<tr><th>Spatial Coverage</th><td>Europe</td></tr>
          		<tr><th>Taxon Coverage</th><td>Fabaceae, Poaceae, Rosaceae, Fabaceae, Poaceae, Rosaceae</td></tr>
      		</table>
      	</div>
  </div>
</div>

<div class="definition">	
  <div class="title">
  	<div class="head">
        Metadata III
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<tr><th>Spatial Coverage</th><td>Europe</td></tr>
          		<tr><th>Taxon Coverage</th><td>Fabaceae, Poaceae, Rosaceae, Fabaceae, Poaceae, Rosaceae</td></tr>
          		<tr><th>Spatial Coverage</th><td>Europe</td></tr>
          		<tr><th>Taxon Coverage</th><td>Fabaceae, Poaceae, Rosaceae, Fabaceae, Poaceae, Rosaceae</td></tr>
          		<tr><th>Spatial Coverage</th><td>Europe</td></tr>
          		<tr><th>Taxon Coverage</th><td>Fabaceae, Poaceae, Rosaceae, Fabaceae, Poaceae, Rosaceae</td></tr>
      		</table>
      	</div>
  </div>
</div>




<#include "/WEB-INF/pages/inc/footer.ftl">
