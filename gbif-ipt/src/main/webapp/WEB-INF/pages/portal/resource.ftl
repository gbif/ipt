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
        <@s.text name='portal.resource.summary'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<tr><th>Keywords</th><td>${resource.eml.subject!}</td></tr>
          		<tr><th>Taxon Coverage</th><td><#list eml.taxonomicCoverages as tc><#list tc.taxonKeywords as k>${k.scientificName}<#if k_has_next>, </#if></#list><#if tc_has_next>; </#if></#list></td></tr>
          		<tr><th>Spatial Coverage</th><td><#list eml.geospatialCoverages as geo>${geo.description!}<#if geo_has_next>; </#if></#list></td></tr>

      		   	<tr><th><@s.text name='eml.language'/></th><td>${eml.language!}</td></tr>

          		<tr><th>Last Publication</th>
			  	<#if resource.lastPublished??>
			  	    <td>Version ${resource.eml.emlVersion} from ${resource.lastPublished?date?string.medium}</td></tr>
			  	<#if (resource.recordsPublished>0)>
          		<tr><th>Archive</th><td><a href="${baseURL}/archive.do?r=${resource.shortname}">download</a>, ${resource.recordsPublished} records </td></tr>
			  	</#if>
          		<tr><th>EML</th><td><a href="${baseURL}/eml.do?r=${resource.shortname}">download</a></td></tr>
          		<#else>
          		    <td>Never</td></tr>
			  	</#if>

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
        <@s.text name='portal.resource.creator'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<tr><th><@s.text name='portal.resource.name'/></th><td>${resource.creator.firstname} ${resource.creator.lastname}</td></tr>
          		<tr><th><@s.text name='resource.creator.email'/></th><td><a href="mailto:${resource.creator.email}">${resource.creator.email}</a></td></tr>
      		</table>
      	</div>
  </div>
</div>

	<#assign address>
		<#if eml.contact.address.address??>${eml.contact.address.address}, </#if>
		<#if eml.contact.address.city??>${eml.contact.address.city}, </#if>
		<#if eml.contact.address.province??>${eml.contact.address.province}, </#if>
		<#if eml.contact.address.country??>${eml.contact.address.country},</#if>
	</#assign>
      
<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='portal.metadata.provider'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<tr><th><@s.text name='portal.resource.name'/></th><td>${eml.contact.firstName!} ${eml.contact.lastName!}</td></tr>
          		<tr><th><@s.text name='eml.contact.organisation'/></th><td>${eml.contact.organisation!} <#if eml.contact.position??>(${eml.contact.position})</#if></td></tr>
          		<tr><th><@s.text name='eml.contact.address.address'/></th><td>${address}</td></tr>
          		<tr><th><@s.text name='portal.resource.contact'/></th><td><a href="mailto:${eml.contact.email!}">${eml.contact.email!}</a> <@s.text name='portal.resource.tel'/>: ${eml.contact.phone!}</td></tr>
      		</table>
      	</div>
  </div>
</div>

<#assign size=eml.associatedParties?size/>
<#if (size > 0 )>
<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.parties.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
			<#list eml.associatedParties as item>
			<#if "${item_index % 2}"=="0">
				<div class="half">
			</#if>
			<div>
			<#assign itemTitle><@s.text name='manage.metadata.parties.item'/></#assign>
			<div class="head">${itemTitle?upper_case} ${item_index+1}</div>
      		<table>
				<tr><th><@s.text name='portal.resource.name'/></th><td>${eml.associatedParties[item_index].firstName!} ${eml.associatedParties[item_index].lastName!}</td></tr>
				<tr><th><@s.text name='eml.associatedParties.position'/></th><td>${eml.associatedParties[item_index].position!}</td></tr>
				<tr><th><@s.text name='eml.associatedParties.organisation'/></th><td>${eml.associatedParties[item_index].organisation!}</td></tr>
				<tr><th><@s.text name='eml.associatedParties.address.address'/></th><td>${eml.associatedParties[item_index].address.address!}</td></tr>
				<tr><th><@s.text name='eml.associatedParties.address.city'/></th><td>${eml.associatedParties[item_index].address.city!}</td></tr>
				<tr><th><@s.text name='eml.associatedParties.address.province'/></th><td>${eml.associatedParties[item_index].address.province!}</td></tr>
  				<tr><th><@s.text name='eml.associatedParties.address.country'/></th><td>${eml.associatedParties[item_index].address.country!}</td></tr>
  				<tr><th><@s.text name='eml.associatedParties.phone'/></th><td>${eml.associatedParties[item_index].phone!}</td></tr>
  				<tr><th><@s.text name='eml.associatedParties.email'/></th><td><a href="mailto:${eml.associatedParties[item_index].email!}">${eml.associatedParties[item_index].email!}</a></td></tr>
  				<tr><th><@s.text name='eml.associatedParties.homepage'/></th><td><a href="${eml.associatedParties[item_index].homepage!}">${eml.associatedParties[item_index].homepage!}</a></td></tr>
  				<tr><th><@s.text name='eml.associatedParties.role'/></th><td><@s.text name='roleType.${eml.associatedParties[item_index].role!}'/></td></tr>
      		</table>
      		<div class="newline"></div>
			</div>
			<#if "${item_index % 2}"=="1" || "${item_index + 1}"=="${size}">
				</div>
			</#if>
			</#list>
      	</div>
  </div>
</div>
</#if>

<#if eml.geospatialCoverages[0]??>
<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.geocoverage.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<tr><th><@s.text name='eml.geospatialCoverages.description'/></th><td>${eml.geospatialCoverages[0].description}</td></tr>
      			<tr><th><@s.text name='eml.geospatialCoverages.boundingCoordinates.min.longitude'/></th><td>${eml.geospatialCoverages[0].boundingCoordinates.min.longitude}</td></tr>
      			<tr><th><@s.text name='eml.geospatialCoverages.boundingCoordinates.max.longitude'/></th><td>${eml.geospatialCoverages[0].boundingCoordinates.max.longitude}</td></tr>
      			<tr><th><@s.text name='eml.geospatialCoverages.boundingCoordinates.min.latitude'/></th><td>${eml.geospatialCoverages[0].boundingCoordinates.min.latitude}</td></tr>
      			<tr><th><@s.text name='eml.geospatialCoverages.boundingCoordinates.max.latitude'/></th><td>${eml.geospatialCoverages[0].boundingCoordinates.max.latitude}</td></tr>
      		</table>
      	</div>
  </div>
</div>
</#if>

<#assign size=eml.taxonomicCoverages?size/>
<#if (size > 0 )>
<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.taxcoverage.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
			<#list eml.taxonomicCoverages as item>
			<div>
			<#assign itemTitle><@s.text name='manage.metadata.taxcoverage.item'/></#assign>
			<div class="head">${itemTitle?upper_case} ${item_index+1}</div>
			<#assign size=eml.taxonomicCoverages[item_index].taxonKeywords?size/>
      		<table>
				<tr><th><@s.text name='eml.taxonomicCoverages.description'/></th><td>${eml.taxonomicCoverages[item_index].description!}</td></tr>
				<#list eml.taxonomicCoverages[item_index].taxonKeywords as subitem>
				<tr>
				<th>
				<div class="subitem">
					<div class="newline"></div>
					<#assign itemTitle><@s.text name='manage.metadata.taxcoverage.subitem'/></#assign>
					${itemTitle?upper_case} ${subitem_index+1}
					<table>
						<tr><th><@s.text name='eml.taxonomicCoverages.taxonKeywords.scientificName'/></th><td>${eml.taxonomicCoverages[item_index].taxonKeywords[subitem_index].scientificName!}</td></tr>
						<tr><th><@s.text name='eml.taxonomicCoverages.taxonKeywords.commonName'/></th><td>${eml.taxonomicCoverages[item_index].taxonKeywords[subitem_index].commonName!}</td></tr>
						<tr><th><@s.text name='eml.taxonomicCoverages.taxonKeywords.rank'/></th><td>${eml.taxonomicCoverages[item_index].taxonKeywords[subitem_index].rank!}</td></tr>
					</table>
					<div class="newline"></div>
				</div>
				</th>
				</tr>
				</#list>
      		</table>
      		<div class="newline"></div>
			</div>
			</#list>
      	</div>
  </div>
</div>
</#if>

<#assign size=eml.temporalCoverages?size/>
<#if (size > 0 )>
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
			<tr>
			<#assign itemTitle><@s.text name='manage.metadata.tempcoverage.item'/></#assign>
			<th>${itemTitle?upper_case} ${item_index+1}</th>
			<td>
				<div>
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
			</td>
			</tr>
			</#list>
		</table>
      	</div>
  </div>
</div>
</#if>

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
          		<tr><th><@s.text name='portal.resource.name'/></th><td>${eml.project.personnel.firstName!} ${eml.project.personnel.lastName!}</td></tr>
	          	<#if eml.project.personnel.role??><tr><th><@s.text name='eml.project.personnel.role'/></th><td><@s.text name='roleType.${eml.project.personnel.role!}'/></td></tr></#if>
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
				<tr>
				<div>
					<#assign itemTitle><@s.text name='manage.metadata.collections.curatorialUnits.item'/></#assign>
					<th>${itemTitle?upper_case} ${item_index+1}</th>
					<td>
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
				</td>
				</tr>
	   		</#list>
	   		</table>
      	</div>
  </div>
</div>

<#if (eml.physicalData?size > 0 )>
<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.physical.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
		<#list eml.physicalData as item>
			<div>
			<#assign itemTitle><@s.text name='manage.metadata.physical.item'/></#assign>
			<div class="head">${itemTitle?upper_case} ${item_index+1}</div>
			<table>
				<tr><th><@s.text name='eml.physicalData.name'/></th><td>${eml.physicalData[item_index].name!}</td></tr>
				<tr><th><@s.text name='eml.physicalData.charset'/></th><td>${eml.physicalData[item_index].charset!}</td></tr>
				<tr><th><@s.text name='eml.physicalData.format'/></th><td>${eml.physicalData[item_index].format!}</td></tr>
				<tr><th><@s.text name='eml.physicalData.formatVersion'/></th><td>${eml.physicalData[item_index].formatVersion!}</td></tr>
				<tr><th><@s.text name='eml.physicalData.distributionUrl'/></th><td><a href="${eml.physicalData[item_index].distributionUrl}">${eml.physicalData[item_index].distributionUrl!}</a></td></tr>				
      		</table>
      		<div class="newline"></div>
			</div>
		</#list>
      	</div>
  </div>
</div>
</#if>

<#if (eml.physicalData?size > 0 )>
<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.keywords.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
		<#list eml.physicalData as item>
			<div>
			<#assign itemTitle><@s.text name='manage.metadata.keywords.item'/></#assign>
			<div class="head">${itemTitle?upper_case} ${item_index+1}</div>
      		<table>
				<tr><th><@s.text name='eml.keywords.keywordThesaurus'/></th><td>${eml.keywords[item_index].name!}</td></tr>
				<tr><th><@s.text name='eml.keywords.keywordsString'/></th><td>${eml.keywords[item_index].keywordsString!}</td></tr>
			</table>
      		<div class="newline"></div>
			</div>
		</#list>
      	</div>
  </div>
</div>
</#if>

<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.additional.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<tr><th><@s.text name='eml.hierarchyLevel'/></th><td>${eml.hierarchyLevel!}</td></tr>
          		<tr><th><@s.text name='eml.pubDate'/></th><td>${eml.pubDate?date!}</td></tr>
          		<tr><th><@s.text name='eml.distributionUrl'/></th><td><a href="${eml.distributionUrl!}">${eml.distributionUrl!}</a></td></tr>
          		<tr><th><@s.text name='eml.purpose'/></th><td>${eml.purpose!}</td></tr>
          		<tr><th><@s.text name='eml.intellectualRights'/></th><td>${eml.intellectualRights!}</td></tr>
          		<tr><th><@s.text name='eml.additionalInfo'/></th><td>${eml.additionalInfo!}</td></tr>
      		</table>
      	</div>
  </div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
