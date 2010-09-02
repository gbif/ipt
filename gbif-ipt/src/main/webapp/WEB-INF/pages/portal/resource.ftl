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
        <@s.text name='portal.resource.metadata'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<tr><th><@s.text name='eml.title'/></th><td>${eml.title!}</td></tr>
          		<tr><th><@s.text name='eml.language'/></th><td>${eml.language!}</td></tr>
          		<tr><th><@s.text name='eml.description'/></th><td>${eml.description!}</td></tr>
      		</table>
      	</div>
  </div>
</div>

<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='portal.resource.creator'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<tr><th><@s.text name='resource.creator.firstname'/></th><td>${resource.creator.firstname}</td></tr>
          		<tr><th><@s.text name='resource.creator.lastname'/></th><td>${resource.creator.lastname}</td></tr>
          		<tr><th><@s.text name='resource.creator.email'/></th><td>${resource.creator.email}</td></tr>
      		</table>
      	</div>
  </div>
</div>

<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='portal.metadata.provider'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<tr><th><@s.text name='eml.contact.firstName'/></th><td>${eml.contact.firstName!}</td></tr>
          		<tr><th><@s.text name='eml.contact.lastName'/></th><td>${eml.contact.lastName!}</td></tr>
          		<tr><th><@s.text name='eml.contact.organisation'/></th><td>${eml.contact.organisation!}</td></tr>
          		<tr><th><@s.text name='eml.contact.position'/></th><td>${eml.contact.position!}</td></tr>
          		<tr><th><@s.text name='eml.contact.phone'/></th><td>${eml.contact.phone!}</td></tr>
          		<tr><th><@s.text name='eml.contact.email'/></th><td>${eml.contact.email!}</td></tr>
          		<tr><th><@s.text name='eml.contact.address.address'/></th><td>${eml.contact.address.address!}</td></tr>
          		<tr><th><@s.text name='eml.contact.address.city'/></th><td>${eml.contact.address.city!}</td></tr>
          		<tr><th><@s.text name='eml.contact.address.province'/></th><td>${eml.contact.address.province!}</td></tr>
          		<tr><th><@s.text name='eml.contact.address.country'/></th><td>${eml.contact.address.country!}</td></tr>
      		</table>
      	</div>
  </div>
</div>

<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.parties.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      	<div class="half">
			<#list eml.associatedParties as item>
			<div>
			<@s.text name='manage.metadata.parties.item'/> ${item_index+1}
      		<table>
				<tr><th><@s.text name='eml.associatedParties.firstName'/></th><td>${eml.associatedParties[item_index].firstName!}</td></tr>
				<tr><th><@s.text name='eml.associatedParties.lastName'/></th><td>${eml.associatedParties[item_index].lastName!}</td></tr>
				<tr><th><@s.text name='eml.associatedParties.position'/></th><td>${eml.associatedParties[item_index].position!}</td></tr>
				<tr><th><@s.text name='eml.associatedParties.organisation'/></th><td>${eml.associatedParties[item_index].organisation!}</td></tr>
				<tr><th><@s.text name='eml.associatedParties.address.address'/></th><td>${eml.associatedParties[item_index].address.address!}</td></tr>
				<tr><th><@s.text name='eml.associatedParties.address.city'/></th><td>${eml.associatedParties[item_index].address.city!}</td></tr>
				<tr><th><@s.text name='eml.associatedParties.address.province'/></th><td>${eml.associatedParties[item_index].address.province!}</td></tr>
  				<tr><th><@s.text name='eml.associatedParties.address.country'/></th><td>${eml.associatedParties[item_index].address.country!}</td></tr>
  				<tr><th><@s.text name='eml.associatedParties.phone'/></th><td>${eml.associatedParties[item_index].phone!}</td></tr>
  				<tr><th><@s.text name='eml.associatedParties.email'/></th><td>${eml.associatedParties[item_index].email!}</td></tr>
  				<tr><th><@s.text name='eml.associatedParties.homepage'/></th><td>${eml.associatedParties[item_index].homepage!}</td></tr>
  				<tr><th><@s.text name='eml.associatedParties.role'/></th><td>${eml.associatedParties[item_index].role!}</td></tr>
      		</table>
      		<div class="newline"></div>
			</div>
			</#list>
      	</div>
      	</div>
  </div>
</div>

<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.geocoverage.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<tr><th><@s.text name='eml.geospatialCoverages.description'/></th><td><#if eml.geospatialCoverages[0]??>${eml.geospatialCoverages[0].description}</#if></td></tr>
      			<tr><th><@s.text name='eml.geospatialCoverages.boundingCoordinates.min.longitude'/></th><td><#if eml.geospatialCoverages[0]??>${eml.geospatialCoverages[0].boundingCoordinates.min.longitude}</#if></td></tr>
      			<tr><th><@s.text name='eml.geospatialCoverages.boundingCoordinates.max.longitude'/></th><td><#if eml.geospatialCoverages[0]??>${eml.geospatialCoverages[0].boundingCoordinates.max.longitude}</#if></td></tr>
      			<tr><th><@s.text name='eml.geospatialCoverages.boundingCoordinates.min.latitude'/></th><td><#if eml.geospatialCoverages[0]??>${eml.geospatialCoverages[0].boundingCoordinates.min.latitude}</#if></td></tr>
      			<tr><th><@s.text name='eml.geospatialCoverages.boundingCoordinates.max.latitude'/></th><td><#if eml.geospatialCoverages[0]??>${eml.geospatialCoverages[0].boundingCoordinates.max.latitude}</#if></td></tr>
      		</table>
      	</div>
  </div>
</div>

<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.taxcoverage.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      	<div class="half">
			<#list eml.taxonomicCoverages as item>
			<div>
			<@s.text name='manage.metadata.taxcoverage.item'/> ${item_index+1}
      		<table>
				<tr><th><@s.text name='eml.taxonomicCoverages.taxonKeyword.scientificName'/></th><td>${eml.taxonomicCoverages[item_index].taxonKeyword.scientificName!}</td></tr>
				<tr><th><@s.text name='eml.taxonomicCoverages.taxonKeyword.commonName'/></th><td>${eml.taxonomicCoverages[item_index].taxonKeyword.commonName!}</td></tr>
				<tr><th><@s.text name='eml.taxonomicCoverages.taxonKeyword.rank'/></th><td>${eml.taxonomicCoverages[item_index].taxonKeyword.rank!}</td></tr>
				<tr><th><@s.text name='eml.taxonomicCoverages.description'/></th><td>${eml.taxonomicCoverages[item_index].description!}</td></tr>
			</table>
      		<div class="newline"></div>
			</div>
			</#list>
      	</div>
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
