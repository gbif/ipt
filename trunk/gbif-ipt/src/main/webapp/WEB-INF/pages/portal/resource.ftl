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
      	<table>
			<#list eml.associatedParties as item>
			<#if "${item_index % 2}"=="0">
				<tr>
				<th>
				<div class="half">
			</#if>
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
			<#if "${item_index % 2}"=="1">
				</div>
				</th>
			 	</tr>
			</#if>
			</#list>
      	</table>
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
      	<table>
			<#list eml.taxonomicCoverages as item>
			<#if "${item_index % 2}"=="0">
				<tr>
				<th>
				<div class="half">
			</#if>
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
			<#if "${item_index % 2}"=="1">
				</div>
				</th>
			 	</tr>
			</#if>
			</#list>
      	</table>
      	</div>
  </div>
</div>

<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.tempcoverage.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      	<table>
			<#list eml.temporalCoverages as item>
			<#if "${item_index % 2}"=="0">
				<tr>
				<th>
				<div class="half">
			</#if>
			<div>
			<@s.text name='manage.metadata.tempcoverage.item'/> ${item_index+1}
      		<table>
				<#if "${item.type}" == "DATE_RANGE" >
					<tr><th><@s.text name='eml.temporalCoverage.startDate'/></th><td>${eml.temporalCoverages[item_index].startDate?date}</td></tr>
					<tr><th><@s.text name='eml.temporalCoverage.endDate'/></th><td>${eml.temporalCoverages[item_index].endDate?date}</td></tr>
				<#elseif "${item.type}" == "SINGLE_DATE" >
					<tr><th><@s.text name='eml.temporalCoverage.startDate'/></th><td>${eml.temporalCoverages[item_index].startDate?date}</td></tr>
				<#elseif "${item.type}" == "FORMATION_PERIOD" >
					<tr><th><@s.text name='eml.temporalCoverage.formationPeriod'/></th><td>${eml.temporalCoverages[item_index].formationPeriod}</td></tr>
				<#else> <!-- LIVING_TIME_PERIOD -->
					<tr><th><@s.text name='eml.temporalCoverage.livingTimePeriod'/></th><td>${eml.temporalCoverages[item_index].livingTimePeriod!}</td></tr>
				</#if>
			</table>
			<div class="newline"></div>
			</div>
			<#if "${item_index % 2}"=="1">
				</div>
				</th>
			 	</tr>
			</#if>
			</#list>
      	</table>
      	</div>
  </div>
</div>

<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.project.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<tr><th><@s.text name='eml.project.title'/></th><td>${eml.project.title!}</td></tr>
          		<tr><th><@s.text name='eml.project.personnel.firstName'/></th><td>${eml.project.personnel.firstName!}</td></tr>
          		<tr><th><@s.text name='eml.project.personnel.lastName'/></th><td>${eml.project.personnel.lastName!}</td></tr>
          		<tr><th><@s.text name='eml.project.personnel.role'/></th><td>${eml.project.personnel.role!}</td></tr>
          		<tr><th><@s.text name='eml.project.funding'/></th><td>${eml.project.funding!}</td></tr>
          		<tr><th><@s.text name='eml.project.studyAreaDescription.descriptorValue'/></th><td>${eml.project.studyAreaDescription.descriptorValue!}</td></tr>
          		<tr><th><@s.text name='eml.project.designDescription'/></th><td>${eml.project.designDescription!}</td></tr>
      		</table>
      	</div>
  </div>
</div>

<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.methods.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<tr><th><@s.text name='eml.studyExtent'/></th><td>${eml.studyExtent!}</td></tr>
          		<tr><th><@s.text name='eml.sampleDescription'/></th><td>${eml.sampleDescription!}</td></tr>
          		<tr><th><@s.text name='eml.qualityControl'/></th><td>${eml.qualityControl!}</td></tr>
          		<#list eml.methodSteps as item>
          			<tr><th><@s.text name='eml.methodSteps'/> ${item_index+1}</th><td>${eml.methodSteps[item_index]!}</td></tr>
          		</#list>
      		</table>
      	</div>
  </div>
</div>

<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.citations.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<tr><th><@s.text name='eml.citation'/></th><td>${eml.citation!}</td></tr>
          		<#list eml.bibliographicCitationSet.bibliographicCitations as item>
          			<tr><th><@s.text name='eml.bibliographicCitationSet.bibliographicCitations'/> ${item_index+1}</th><td>${eml.bibliographicCitationSet.bibliographicCitations[item_index]!}</td></tr>
          		</#list>
      		</table>
      	</div>
  </div>
</div>

<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.collections.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<tr><th><@s.text name='eml.collectionName'/></th><td>${eml.collectionName!}</td></tr>
          		<tr><th><@s.text name='eml.collectionId'/></th><td>${eml.collectionId!}</td></tr>
          		<tr><th><@s.text name='eml.parentCollectionId'/></th><td>${eml.parentCollectionId!}</td></tr>
          	</table>
          	<div class="newline"></div>
        	<table>
        	<#list eml.jgtiCuratorialUnits as item>
				<#if "${item_index % 2}"=="0">
					<tr>
					<th>
					<div >
				</#if>
				<div>
				<@s.text name='manage.metadata.collections.curatorialUnits.item'/> ${item_index+1}
		       		<table>	
         		   		<#if item.type=="COUNT_RANGE">
          					<tr><th><@s.text name='eml.jgtiCuratorialUnits.rangeStart'/></th><td>${eml.jgtiCuratorialUnits[item_index].rangeStart}</td></tr>
    						<tr><th><@s.text name='eml.jgtiCuratorialUnits.rangeEnd'/></th><td>${eml.jgtiCuratorialUnits[item_index].rangeEnd}</td></tr>
    					<#else>
    						<tr><th><@s.text name='eml.jgtiCuratorialUnits.rangeMean'/></th><td>${eml.jgtiCuratorialUnits[item_index].rangeMean}</td></tr>
    						<tr><th><@s.text name='eml.jgtiCuratorialUnits.uncertaintyMeasure'/></th><td>${eml.jgtiCuratorialUnits[item_index].uncertaintyMeasure}</td></tr>
   						</#if>
    	  			</table>
				<div class="newline"></div>
				</div>
				<#if "${item_index % 2}"=="1">
					</div>
					</th>
				 	</tr>
				</#if>
	   		</#list>
	   		</table>
      	</div>
  </div>
</div>

<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.physical.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      	<table>
			<#list eml.physicalData as item>
			<tr>
			<th>
			<div>
			<@s.text name='manage.metadata.physical.item'/> ${item_index+1}
      		<table>
				<tr><th><@s.text name='eml.physicalData.name'/></th><td>${eml.physicalData[item_index].name!}</td></tr>
				<tr><th><@s.text name='eml.physicalData.charset'/></th><td>${eml.physicalData[item_index].charset!}</td></tr>
				<tr><th><@s.text name='eml.physicalData.format'/></th><td>${eml.physicalData[item_index].format!}</td></tr>
				<tr><th><@s.text name='eml.physicalData.formatVersion'/></th><td>${eml.physicalData[item_index].formatVersion!}</td></tr>
				<tr><th><@s.text name='eml.physicalData.distributionUrl'/></th><td><a href="${eml.physicalData[item_index].distributionUrl}">${eml.physicalData[item_index].distributionUrl!}</a></td></tr>				
      		</table>
      		<div class="newline"></div>
			</div>
			</th>
		 	</tr>
			</#list>
      	</table>
      	</div>
  </div>
</div>

<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.keywords.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      	<table>
			<#list eml.physicalData as item>
			<tr>
			<th>
			<div>
			<@s.text name='manage.metadata.keywords.item'/> ${item_index+1}
      		<table>
				<tr><th><@s.text name='eml.keywords.keywordThesaurus'/></th><td>${eml.keywords[item_index].name!}</td></tr>
				<tr><th><@s.text name='eml.keywords.keywordsString'/></th><td>${eml.keywords[item_index].keywordsString!}</td></tr>
			</table>
      		<div class="newline"></div>
			</div>
			</th>
			</tr>
			</#list>
      	</table>
      	</div>
  </div>
</div>

<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.project.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<tr><th><@s.text name='eml.hierarchyLevel'/></th><td>${eml.hierarchyLevel!}</td></tr>
          		<tr><th><@s.text name='eml.pubDate'/></th><td>${eml.pubDate?date!}</td></tr>
          		<tr><th><@s.text name='eml.distributionUrl'/></th><td><a href="${eml.distributionUrl}">${eml.distributionUrl!}</a></td></tr>
          		<tr><th><@s.text name='eml.purpose'/></th><td>${eml.purpose!}</td></tr>
          		<tr><th><@s.text name='eml.intellectualRights'/></th><td>${eml.intellectualRights!}</td></tr>
          		<tr><th><@s.text name='eml.additionalInfo'/></th><td>${eml.additionalInfo!}</td></tr>
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
