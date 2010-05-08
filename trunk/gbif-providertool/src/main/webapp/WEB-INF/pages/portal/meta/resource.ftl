<#include "/WEB-INF/pages/inc/tableMacros.ftl">  

<head>
    <title><@s.text name="meta.resource.title"/></title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="menu" content="ExplorerMenu"/>
    <meta name="submenu" content="meta"/>
</head>
	
<h1 class="h1meta">${resource.title}</h1>	
<div class="horizontal_dotted_line_large"></div>


<@s.form>
<div>
<img class="rightImage_nm" src="${cfg.getResourceLogoUrl(resourceId)}" />
<table style="width: 400px;">	
 <tr>
	<th><@s.text name="meta.resource.guid"/></th>
	<td><a href="${cfg.getResourceUrl(resource.guid)}">${resource.guid}</a></td>
 </tr>
 <tr>
	<th>EML</th>
	<td><a href="${cfg.getEmlUrl(resource.guid)}">${cfg.getEmlUrl(resource.guid)}</a></td>
 </tr>
 <tr>
	<th><@s.text name="meta.resource.annotations"/></th>
	<td><a href="annotations.html?resourceId=${resourceId}">annotations.html?resourceId=${resourceId}</a></td>
 </tr>
</table>
</div>

<div id="basics">
	<fieldset>
		<h2><@s.text name="dataResource.metadata"/></h2>
		<table>	
			<@trow label="resource.title" val=resource.title!/>  
			<@trow label="resource.status" val=resource.status!/>  
			<@trow label="resource.contactName" val=resource.contactName!/>  
			<@trow label="resource.description" val=resource.description!/>
		</table>
	</fieldset>
	<div class="horizontal_dotted_line_large_soft"></div>	
</div>

<div>
	<fieldset>
		<h2><@s.text name="metadata.heading.basic.creator"/></h2>
		<table>
			<@trow label="eml.resourceCreator.firstName" val=eml.getResourceCreator().firstName!/>  
			<@trow label="eml.resourceCreator.lastName" val=eml.getResourceCreator().lastName!/>  
			<@trow label="eml.resourceCreator.organisation" val=eml.getResourceCreator().organisation!/>  
			<@trow label="eml.resourceCreator.position" val=eml.getResourceCreator().position!/>  
			<@trow label="eml.resourceCreator.phone" val=eml.getResourceCreator().phone!/>  
			<@trow label="eml.resourceCreator.email" val=eml.getResourceCreator().email!/>  
			<@trow label="agent.homepage" val=eml.getResourceCreator().homepage!/>  
			<@trow label="agent.address.address" val=eml.getResourceCreator().address.address!/>
			<@trow label="agent.address.postalCode" val=eml.getResourceCreator().address.postalCode!/>  
			<@trow label="agent.address.city" val=eml.getResourceCreator().address.city!/>  
			<@trow label="agent.address.province" val=eml.getResourceCreator().address.province!/>  
			<@trow label="agent.address.country" val=eml.getResourceCreator().address.country!/>
		</table>
	</fieldset>
	<div class="horizontal_dotted_line_large_soft"></div>	
</div>

<div>
	<fieldset>
		<h2><@s.text name="metadata.heading.basic.metadataProvider"/></h2>
		<table>
			<@trow label="eml.metadataProvider.firstName" val=eml.getMetadataProvider().firstName!/>  
			<@trow label="eml.metadataProvider.lastName" val=eml.getMetadataProvider().lastName!/>  
			<@trow label="eml.metadataProvider.email" val=eml.getMetadataProvider().email!/>  
			<@trow label="agent.address.address" val=eml.getMetadataProvider().address.address!/>
			<@trow label="agent.address.postalCode" val=eml.getMetadataProvider().address.postalCode!/>  
			<@trow label="agent.address.city" val=eml.getMetadataProvider().address.city!/>  
			<@trow label="agent.address.province" val=eml.getMetadataProvider().address.province!/>  
			<@trow label="agent.address.country" val=eml.getMetadataProvider().address.country!/>
		</table>
	</fieldset>
	<div class="horizontal_dotted_line_large_soft"></div>	
</div>

<!-- taxonomicClassification was here -->

<div>
	<fieldset>
		<h2><@s.text name="metadata.heading.geocoverage"/></h2>
		<table>
			<@trow label="eml.geographicCoverage.boundingCoordinates" val=(eml.getGeographicCoverage().boundingCoordinates.toStringShort(3))!/>  
			<@trow label="eml.geographicCoverage.description" val=(eml.getGeographicCoverage().description)!/>  
		</table>
	</fieldset>
	<div class="horizontal_dotted_line_large_soft"></div>
</div>

<!-- temporalCoverage was here -->

<!-- keywords were here -->
<#if eml.project??>
<div>
	<fieldset>
		<h2><@s.text name="metadata.heading.project"/></h2>
		<table>
			<@trow label="eml.project.title" val=eml.getProject().title!/>  
			<@trow label="eml.project.funding" val=eml.getProject().funding!/>  
			<@trow label="eml.project.studyAreaDescription.descriptorValue" val=eml.getProject().studyAreaDescription.descriptorValue!/>  
			<@trow label="eml.project.designDescription" val=eml.getProject().designDescription!/>  
		</table>
	</fieldset>
	<div class="horizontal_dotted_line_large_soft"></div>
</div>
</#if>
<!-- Sampling Method was here -->

<div>
	<fieldset>
		<h2><@s.text name="metadata.heading.additionalMetadata"/></h2>
		<table>
			<@trow label="eml.pubDate" val=eml.pubDate?datetime?string("yyyy-MM-dd")!/>
			<@trow label="eml.purpose" val=eml.purpose!/>
			<@trow label="eml.hierarchyLevel" val=eml.hierarchyLevel!/>
			<@trow label="eml.distributionUrl" val=eml.distributionUrl!/>  
			<@trow label="eml.additionalInfo" val=eml.additionalInfo!/>  
		</table>
	</fieldset>
</div>
</@s.form>