<#escape x as x?html>
<#macro agentTable agent withRole=false>
<table>
	<#if withRole & agent.role?? ><tr><th><@s.text name='eml.associatedParties.role'/></th><td>${agent.role!}</td></tr></#if>
	<#if agent.firstName?? || agent.lastName??><tr><th><@s.text name='portal.resource.name'/></th><td>${agent.firstName!} ${agent.lastName!}</td></tr></#if>
	<#if agent.position?? ><tr><th><@s.text name='eml.associatedParties.position'/></th><td>${agent.position!}</td></tr></#if>
	<#if agent.organisation?? ><tr><th><@s.text name='eml.contact.organisation'/></th><td>${agent.organisation!}</td></tr></#if>
	<#if !agent.address.isEmpty()><tr><th><@s.text name='eml.contact.address.address'/></th><td>
	<#if agent.address.address??>${agent.address.address}</#if><#if agent.address.city??>, ${agent.address.city}</#if><#if agent.address.province??>, ${agent.address.province}</#if><#if agent.address.country??>, ${agent.address.country}</#if><#if agent.address.postalCode??>, <@s.text name='eml.contact.address.postalCode'/>: ${agent.address.postalCode}</#if>
	</td></tr></#if>
	<#if agent.email?? || agent.phone??><tr><th><@s.text name='portal.resource.contact'/></th><td><a href="mailto:${agent.email!}">${agent.email!}</a> <#if agent.phone??><@s.text name='portal.resource.tel'/>: ${agent.phone!}</#if></td></tr></#if>
	<#if agent.homepage?? ><tr><th><@s.text name='eml.associatedParties.homepage'/></th><td><a href="${agent.homepage!}">${agent.homepage!}</a></td></tr></#if>
</table>
</#macro>
<#include "/WEB-INF/pages/macros/forms.ftl"/>
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
<div id="resourcelogo">
	<#if resource.eml.logoUrl?has_content>
	<img src="${resource.eml.logoUrl}" />
	</#if>
</div>
<#assign no_description><@s.text name='portal.resource.no.description'/></#assign>
<@showMore resource.description!no_description 150 />
<div class="definition" id="metadata">	
  <div class="title">
  	<div class="head">
        <@s.text name='portal.resource.summary'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<#if resource.eml.subject?has_content><tr><th><@s.text name='portal.resource.summary.keywords'/></th><td><@showMore resource.eml.subject!no_description 90/></td></tr></#if>
          		<#assign text><#list eml.taxonomicCoverages as tc><#list tc.taxonKeywords as k>${k.scientificName!}<#if k_has_next>, </#if></#list><#if tc_has_next>; </#if></#list></#assign>
          		<#if eml.taxonomicCoverages?has_content><tr><th><@s.text name='portal.resource.summary.taxcoverage'/></th><td><@showMore text!no_description 90/></td></tr></#if>
          		<#assign text><#list eml.geospatialCoverages as geo>${geo.description!}<#if geo_has_next>; </#if></#list></#assign>
          		<#if eml.geospatialCoverages?has_content><tr><th><@s.text name='portal.resource.summary.geocoverage'/></th><td><@showMore text!no_description 90/></td></tr></#if>

      		   	<tr><th><@s.text name='eml.language'/></th><td>${eml.language!}</td></tr>

          		<tr><th><@s.text name='portal.resource.last.publication'/></th>
			  	<#if resource.lastPublished??>
			  	    <td><@s.text name='portal.resource.version'/> ${resource.eml.emlVersion} <@s.text name='portal.resource.version.from'/> ${resource.lastPublished?date?string.medium}</td></tr>
			  	<#if (resource.recordsPublished>0)>
          		<tr><th><@s.text name='portal.resource.published.archive'/></th><td><a href="${baseURL}/archive.do?r=${resource.shortname}"><@s.text name='portal.resource.download'/></a> (${dwcaFormattedSize}) ${resource.recordsPublished} <@s.text name='portal.resource.records'/></td></tr>
			  	</#if>
          		<tr><th><@s.text name='portal.resource.published.eml'/></th><td><a href="${baseURL}/eml.do?r=${resource.shortname}"><@s.text name='portal.resource.download'/></a> (${emlFormattedSize})</td></tr>
          		
          		<tr><th><@s.text name='portal.resource.published.rtf'/></th><td><a href="${baseURL}/rtf.do?r=${resource.shortname}"><@s.text name='portal.resource.download'/></a> (${rtfFormattedSize})</td></tr>
          		
          		<#else>
          		    <td><@s.text name='portal.resource.published.never'/></td></tr>
			  	</#if>
				
      		   	<#if resource.status=="REGISTERED">
	          		<tr><th><@s.text name='portal.resource.organisation.key'/></th><td><a href="${cfg.registryUrl}/browse/agent?uuid=${resource.key}">${resource.key}</a></td></tr>
	          		<#if resource.organisation?exists>
	          		<tr><th><@s.text name='portal.resource.organisation.name'/></th><td><a href="${cfg.registryUrl}/browse/agent?uuid=${resource.organisation.key}">${resource.organisation.name!}</a> </td></tr>
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

<#if eml.getMetadataProvider().organisation?has_content || eml.getMetadataProvider().lastName?has_content || eml.getMetadataProvider().position?has_content>
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
</#if>

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
		      		<@agentTable item true />
				</div>
			<#if (item_index % 2) == 1 || eml.associatedParties?size=item_index+1>
			</div>
      		<div class="newline"> <br/><br/> </div>
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
          		<tr><th><@s.text name='eml.geospatialCoverages.description'/></th><td><@showMore eml.geospatialCoverages[0].description!no_description 100/></td></tr>
      			<tr><th><@s.text name='eml.geospatialCoverages.boundingCoordinates'/></th><td>${eml.geospatialCoverages[0].boundingCoordinates.min.latitude}, ${eml.geospatialCoverages[0].boundingCoordinates.max.latitude} / ${eml.geospatialCoverages[0].boundingCoordinates.min.longitude}, ${eml.geospatialCoverages[0].boundingCoordinates.max.longitude} <@s.text name='eml.geospatialCoverages.boundingCoordinates.indicator'/></td></tr>
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
			<#assign size=eml.taxonomicCoverages[item_index].taxonKeywords?size/>
			<table>
			<tr><th><@s.text name='eml.taxonomicCoverages.description'/></th><td><@showMore eml.taxonomicCoverages[item_index].description!no_description 80/></td></tr>
      		<table>
				<#list eml.taxonomicCoverages[item_index].taxonKeywords as subitem>
				<tr>
				<td>
				<div class="subitem">
					<#if subitem.scientificName?has_content><i>${subitem.scientificName!}</i>,</#if> <#if subitem.commonName?has_content>${subitem.commonName!}</#if> <#if subitem.rank?has_content>[${subitem.rank!}]</#if>
					<div class="newline"></div>
				</div>
				</td>
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
			<td>
				<div>
				<table>
					<#if "${item.type}" == "DATE_RANGE" >
						<tr><th><@s.text name='eml.temporalCoverages.startDate'/> / <@s.text name='eml.temporalCoverages.endDate'/></th><td>${eml.temporalCoverages[item_index].startDate?date} / ${eml.temporalCoverages[item_index].endDate?date}</td></tr>
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
          		<#if eml.project.funding?has_content><tr><th><@s.text name='eml.project.funding'/></th><td><@showMore eml.project.funding 100/></td></tr></#if>
          		<#if eml.project.studyAreaDescription.descriptorValue?has_content><tr><th><@s.text name='eml.project.studyAreaDescription.descriptorValue'/></th><td><@showMore eml.project.studyAreaDescription.descriptorValue 100/></td></tr></#if>
          		<#if eml.project.designDescription?has_content><tr><th><@s.text name='eml.project.designDescription'/></th><td><@showMore eml.project.designDescription 100/></td></tr></#if>
      		</table>
      	</div>
  </div>
</div>
</#if>

<#if eml.studyExtent?has_content || eml.sampleDescription?has_content || eml.qualityControl?has_content || eml.methodSteps?has_content>
<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.methods.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<#if eml.studyExtent?has_content><tr><th><@s.text name='eml.studyExtent'/></th><td><@showMore eml.studyExtent 100 /></td></tr></#if>
          		<#if eml.sampleDescription?has_content><tr><th><@s.text name='eml.sampleDescription'/></th><td><@showMore eml.sampleDescription 100 /></td></tr></#if>
          		<#if eml.qualityControl?has_content><tr><th><@s.text name='eml.qualityControl'/></th><td><@showMore eml.qualityControl 100 /></td></tr></#if>
          		<#list eml.methodSteps as item>
          			<tr><th><@s.text name='eml.methodSteps'/> ${item_index+1}</th><td><@showMore eml.methodSteps[item_index] 100 /></td></tr>
          		</#list>
      		</table>
      	</div>
  </div>
</div>
</#if>

<#if (eml.citation?? && (eml.citation.citation?has_content || eml.citation.identifier?has_content)) || eml.bibliographicCitationSet.bibliographicCitations?has_content>
<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.citations.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<#if eml.citation.citation?has_content>
          			<#if eml.citation.identifier?has_content><tr><th><@s.text name='eml.citation.identifier'/></th><td>${eml.citation.identifier!}</td></tr></#if>
          			<tr><th><@s.text name='eml.citation.citation'/></th><td><@showMore eml.citation.citation 100 /></td></tr>
          		</#if>
          	</table>
          	<div class="newline"></div>
   			<#assign itemTitle><@s.text name='eml.bibliographicCitationSet.bibliographicCitations.citation'/></#assign>
          	<table>
       		<#list eml.bibliographicCitationSet.bibliographicCitations as item>
       			<tr>				
	          	<table>
          			<tr><th><@s.text name='eml.bibliographicCitationSet.bibliographicCitations.identifier'/></th><td>${item.identifier!}</td></tr>
          			<tr><th><@s.text name='eml.bibliographicCitationSet.bibliographicCitations.citation'/></th><td><@showMore item.citation 100 /></td></tr>
	      		</table>
	      		<div class="newline"></div>
	      		<div class="newline"></div>
				</tr>
       		</#list>
       		</table>
      	</div>
  </div>
</div>
</#if>

<#if eml.collectionName?has_content || eml.collectionId?has_content || eml.parentCollectionId?has_content || eml.specimenPreservationMethod?has_content || eml.jgtiCuratorialUnits?has_content >
<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.collections.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<#if eml.collectionName?has_content><tr><th><@s.text name='eml.collectionName'/></th><td>${eml.collectionName!}</td></tr></#if>
          		<#if eml.collectionId?has_content><tr><th><@s.text name='eml.collectionId'/></th><td>${eml.collectionId!}</td></tr></#if>
          		<#if eml.parentCollectionId?has_content><tr><th><@s.text name='eml.parentCollectionId'/></th><td>${eml.parentCollectionId!}</td></tr></#if>
          		<#if eml.specimenPreservationMethod?has_content><tr><th><@s.text name='eml.specimenPreservationMethod'/></th><td>${eml.specimenPreservationMethod!}</td></tr></#if>
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
    						<tr><th><@s.text name='eml.jgtiCuratorialUnits.unitType'/></th><td>${eml.jgtiCuratorialUnits[item_index].unitType}</td></tr>
    					<#else>
    						<tr><th><@s.text name='eml.jgtiCuratorialUnits.rangeMean'/></th><td>${eml.jgtiCuratorialUnits[item_index].rangeMean}</td></tr>
    						<tr><th><@s.text name='eml.jgtiCuratorialUnits.uncertaintyMeasure'/></th><td>${eml.jgtiCuratorialUnits[item_index].uncertaintyMeasure}</td></tr>
    						<tr><th><@s.text name='eml.jgtiCuratorialUnits.unitType'/></th><td>${eml.jgtiCuratorialUnits[item_index].unitType}</td></tr>
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

<#--
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
				<tr><th><@s.text name='eml.keywords.keywordThesaurus'/></th><td>${eml.keywords[item_index].keywordThesaurus!}</td></tr>
				<tr><th><@s.text name='eml.keywords.keywordsString'/></th><td>${eml.keywords[item_index].keywordsString!}</td></tr>
			</table>
      		<div class="newline"></div>
			</div>
		</#list>
      	</div>
  </div>
</div>
</#if>
-->

<#if (eml.alternateIdentifiers?size > 0 )>
<div class="definition">	
  <div class="title">
  	<div class="head">
        <@s.text name='manage.metadata.alternateIdentifiers.title'/>
  	</div>
  </div>
  <div class="body">
      	<div class="details">
      		<table>
          		<#list eml.alternateIdentifiers as item>
          			<tr><td>${eml.alternateIdentifiers[item_index]!}</td></tr>
          		</#list>
      		</table>
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
          		<#if eml.hierarchyLevel?has_content><tr><th><@s.text name='eml.hierarchyLevel'/></th><td>${eml.hierarchyLevel!}</td></tr></#if>
          		<#if eml.pubDate?has_content><tr><th><@s.text name='eml.pubDate'/></th><td>${eml.pubDate?date!}</td></tr></#if>
          		<#if eml.purpose?has_content><tr><th><@s.text name='eml.purpose'/></th><td><@showMore eml.purpose 100 /></td></tr></#if>
          		<#if eml.intellectualRights?has_content><tr><th><@s.text name='eml.intellectualRights'/></th><td><@showMore eml.intellectualRights!no_description 100/></td></tr></#if>
          		<#if eml.additionalInfo?has_content><tr><th><@s.text name='eml.additionalInfo'/></th><td><@showMore eml.additionalInfo 100 /></td></tr></#if>
      		</table>
      	</div>
  </div>
</div>
<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>