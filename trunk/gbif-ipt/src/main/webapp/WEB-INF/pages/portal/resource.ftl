<#macro agentTable agent>
<table>
	<#if agent.role?? ><tr><th><@s.text name='eml.associatedParties.role'/></th><td>${agent.role!}</td></tr></#if>
	<#if agent.firstName?? ><tr><th><@s.text name='portal.resource.name'/></th><td>${agent.firstName!} ${agent.lastName!}</td></tr></#if>
	<#if agent.position?? ><tr><th><@s.text name='eml.associatedParties.position'/></th><td>${agent.position!}</td></tr></#if>
	<#if agent.organisation?? ><tr><th><@s.text name='eml.contact.organisation'/></th><td>${agent.organisation!}</td></tr></#if>
	<#if !agent.address.isEmpty()><tr><th><@s.text name='eml.contact.address.address'/></th><td>
		<#if agent.address.address??>${agent.address.address}, </#if>
		<#if agent.address.city??>${agent.address.city}, </#if>
		<#if agent.address.province??>${agent.address.province}, </#if>
		<#if agent.address.country??>${agent.address.country},</#if>
	</td></tr></#if>
	<#if agent.email?? || agent.phone??><tr><th><@s.text name='portal.resource.contact'/></th><td><a href="mailto:${agent.email!}">${agent.email!}</a> <#if agent.phone??><@s.text name='portal.resource.tel'/>: ${agent.phone!}</#if></td></tr></#if>
	<#if agent.homepage?? ><tr><th><@s.text name='eml.associatedParties.homepage'/></th><td><a href="${agent.homepage!}">${agent.homepage!}</a></td></tr></#if>
</table>
</#macro>

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
<#assign no_description><@s.text name='portal.resource.no.description'/></#assign>
<p>${resource.description!no_description}</p>
<div class="definition" id="metadata">	
  <div class="title">
  	<div class="head">
        <@s.text name='portal.resource.summary'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<#if resource.eml.subject?has_content><tr><th><@s.text name='portal.resource.summary.keywords'/></th><td>${resource.eml.subject!}</td></tr></#if>
          		<#if eml.taxonomicCoverages?has_content><tr><th><@s.text name='portal.resource.summary.taxcoverage'/></th><td><#list eml.taxonomicCoverages as tc><#list tc.taxonKeywords as k>${k.scientificName}<#if k_has_next>, </#if></#list><#if tc_has_next>; </#if></#list></td></tr></#if>
          		<#if eml.geospatialCoverages?has_content><tr><th><@s.text name='portal.resource.summary.geocoverage'/></th><td><#list eml.geospatialCoverages as geo>${geo.description!}<#if geo_has_next>; </#if></#list></td></tr></#if>

      		   	<tr><th><@s.text name='eml.language'/></th><td>${eml.language!}</td></tr>

          		<tr><th><@s.text name='portal.resource.last.publication'/></th>
			  	<#if resource.lastPublished??>
			  	    <td><@s.text name='portal.resource.version'/> ${resource.eml.emlVersion} <@s.text name='portal.resource.version.from'/> ${resource.lastPublished?date?string.medium}</td></tr>
			  	<#if (resource.recordsPublished>0)>
          		<tr><th><@s.text name='portal.resource.published.archive'/></th><td><a href="${baseURL}/archive.do?r=${resource.shortname}"><@s.text name='portal.resource.download'/></a>, ${resource.recordsPublished} <@s.text name='portal.resource.records'/> </td></tr>
			  	</#if>
          		<tr><th><@s.text name='portal.resource.published.eml'/></th><td><a href="${baseURL}/eml.do?r=${resource.shortname}"><@s.text name='portal.resource.download'/></a></td></tr>
          		<#else>
          		    <td><@s.text name='portal.resource.published.never'/></td></tr>
			  	</#if>

      		   	<#if resource.status=="REGISTERED">
	          		<tr><th><@s.text name='portal.resource.organisation.key'/></th><td><a href="http://gbrdsdev.gbif.org/browse/agent?uuid=${resource.key}">${resource.key}</a></td></tr>
	          		<#if resource.organisation?exists>
	          		<tr><th><@s.text name='portal.resource.organisation.name'/></th><td><a href="http://gbrdsdev.gbif.org/browse/agent?uuid=${resource.organisation.key}">${resource.organisation.name!}</a> </td></tr>
	          		<tr><th><@s.text name='portal.resource.organisation.node'/></th><td>${resource.organisation.nodeName!}</td></tr>
	          		</#if>
          		</#if>
      		</table>
      	</div>
  </div>
</div>

<#if eml.distributionUrl?? || eml.physicalData?has_content >
<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.physical.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<#if eml.distributionUrl??><tr><th><@s.text name='eml.distributionUrl'/></th><td><a href="${eml.distributionUrl!}">${eml.distributionUrl!}</a></td></tr></#if>
		<#if (eml.physicalData?size > 0 )>
			<#list eml.physicalData as item>
				<#assign link=eml.physicalData[item_index]/>
				<tr><th>${link.name!}</th><td><a href="${link.distributionUrl}">${link.distributionUrl!"?"}</a>
				<#if link.charset?? || link.format?? || link.formatVersion??> 
				${link.charset!} ${link.format!} ${link.formatVersion!}
				</#if>
				</td></tr>
			</#list>
		</#if>
      		</table>
      	</div>
  </div>
</div>
</#if>

<#if eml.contact.organisation?has_content || eml.contact.lastName?has_content || eml.contact.position?has_content>
<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='eml.contact'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">      		
      		<@agentTable eml.contact />
      	</div>
  </div>
</div>
</#if>

<#if eml.getResourceCreator().organisation?has_content || eml.getResourceCreator().lastName?has_content || eml.getResourceCreator().position?has_content>
<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='portal.resource.creator'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">      		
      		<@agentTable eml.getResourceCreator() />
      	</div>
  </div>
</div>
</#if>

<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='portal.metadata.provider'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<@agentTable eml.getMetadataProvider() />
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
			<#if (item_index % 2) == 0>
			<div>
			</#if>
				<div class="halfcolumn">
					<#assign itemTitle><@s.text name='manage.metadata.parties.item'/></#assign>
					<div class="head">${itemTitle?upper_case} ${item_index+1}</div>
		      		<@agentTable item />
				</div>
			<#if (item_index % 2) == 1 || eml.associatedParties?size=item_index+1>
			</div>
      		<div class="newline"> <br/> </div>
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
          		<tr><th><@s.text name='eml.geospatialCoverages.description'/></th><td>${eml.geospatialCoverages[0].description!}</td></tr>
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
						<tr><th><@s.text name='eml.taxonomicCoverages.taxonKeyword.scientificName'/></th><td>${eml.taxonomicCoverages[item_index].taxonKeywords[subitem_index].scientificName!}</td></tr>
						<tr><th><@s.text name='eml.taxonomicCoverages.taxonKeyword.commonName'/></th><td>${eml.taxonomicCoverages[item_index].taxonKeywords[subitem_index].commonName!}</td></tr>
						<tr><th><@s.text name='eml.taxonomicCoverages.taxonKeyword.rank'/></th><td>${eml.taxonomicCoverages[item_index].taxonKeywords[subitem_index].rank!}</td></tr>
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
			<b><th class="title">${itemTitle?upper_case} ${item_index+1}</th></b>
			<td>
				<div>
				<table>
					<#if "${item.type}" == "DATE_RANGE" >
						<tr><th><@s.text name='eml.temporalCoverages.startDate'/></th><td>${eml.temporalCoverages[item_index].startDate?date}</td></tr>
						<tr><th><@s.text name='eml.temporalCoverages.endDate'/></th><td>${eml.temporalCoverages[item_index].endDate?date}</td></tr>
					<#elseif "${item.type}" == "SINGLE_DATE" >
						<tr><th><@s.text name='eml.temporalCoverages.startDate'/></th><td>${eml.temporalCoverages[item_index].startDate?date}</td></tr>
					<#elseif "${item.type}" == "FORMATION_PERIOD" >
						<tr><th><@s.text name='eml.temporalCoverages.formationPeriod'/></th><td>${eml.temporalCoverages[item_index].formationPeriod}</td></tr>
					<#else> <!-- LIVING_TIME_PERIOD -->
						<tr><th><@s.text name='eml.temporalCoverages.livingTimePeriod'/></th><td>${eml.temporalCoverages[item_index].livingTimePeriod!}</td></tr>
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

<#if eml.project.personnel.lastName??>
<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.project.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<#if eml.project.title?has_content><tr><th><@s.text name='eml.project.title'/></th><td>${eml.project.title!}</td></tr></#if>
          		<#if eml.project.personnel.lastName?has_content><tr><th><@s.text name='portal.resource.name'/></th><td>${eml.project.personnel.firstName!} ${eml.project.personnel.lastName!}</td></tr></#if>
	          	<#if eml.project.personnel.role??><tr><th><@s.text name='eml.project.personnel.role'/></th><td>${eml.project.personnel.role!}</td></tr></#if>
          		<#if eml.project.funding?has_content><tr><th><@s.text name='eml.project.funding'/></th><td>${eml.project.funding!}</td></tr></#if>
          		<#if eml.project.studyAreaDescription.descriptorValue?has_content><tr><th><@s.text name='eml.project.studyAreaDescription.descriptorValue'/></th><td>${eml.project.studyAreaDescription.descriptorValue!}</td></tr></#if>
          		<#if eml.project.designDescription?has_content><tr><th><@s.text name='eml.project.designDescription'/></th><td>${eml.project.designDescription!}</td></tr></#if>
      		</table>
      	</div>
  </div>
</div>
</#if>

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
          		<tr><th><@s.text name='eml.citation.citation'/></th><td>${eml.citation!}</td></tr>
          		<#list eml.bibliographicCitationSet.bibliographicCitations as item>
          			<tr><th><@s.text name='eml.bibliographicCitationSet.bibliographicCitations.citation'/> ${item_index+1}</th><td>${eml.bibliographicCitationSet.bibliographicCitations[item_index]!}</td></tr>
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
          		<tr><th><@s.text name='eml.specimenPreservationMethod'/></th><td>${eml.specimenPreservationMethod!}</td></tr>
          	</table>
          	<div class="newline"></div>
        	<table>
        	<#list eml.jgtiCuratorialUnits as item>
				<tr>
				<div>
					<#assign itemTitle><@s.text name='manage.metadata.collections.curatorialUnits.item'/></#assign>
					<th class="title">${itemTitle?upper_case} ${item_index+1}</th>
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

<#if (eml.keywords?size > 0 )>
<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.keywords.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
		<#list eml.keywords as item>
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
          		<tr><th><@s.text name='eml.purpose'/></th><td>${eml.purpose!}</td></tr>
          		<tr><th><@s.text name='eml.intellectualRights'/></th><td>${eml.intellectualRights!}</td></tr>
          		<tr><th><@s.text name='eml.additionalInfo'/></th><td>${eml.additionalInfo!}</td></tr>
      		</table>
      		<table>
          		<#list eml.alternateIdentifiers as item>
          		<#assign itemTitle><@s.text name='manage.metadata.alternateIdentifiers.item'/></#assign>
          			<tr><th>${itemTitle?upper_case} ${item_index+1}</th><td>${eml.alternateIdentifiers[item_index]!}</td></tr>
          		</#list>
      		</table>
      	</div>
  </div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
