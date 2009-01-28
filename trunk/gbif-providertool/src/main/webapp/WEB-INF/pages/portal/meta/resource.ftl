<head>
    <title><@s.text name="resource.title"/></title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="submenu" content="meta"/>
</head>
	
		
	
<#macro trow label val>
 <tr>
  <th><@s.text name="${label}"/></th>
  <td>${val!}</td>
 </tr>
</#macro>  


<img class="right" src="${cfg.getResourceLogoUrl(resource_id)}" />
<h2>${resource.title}</h2>	

<@s.form>

<table>	
 <tr>
	<th>GUID</th>
	<td><a href="${cfg.getResourceUrl(resource.guid)}">${resource.guid}</a></td>
 </tr>
 <tr>
	<th>EML</th>
	<td><a href="${cfg.getEmlUrl(resource.guid)}">EML</a></td>
 </tr>
</table>

<div id="basics">
	<fieldset>
		<h2>${resource.title!}</h2>
		<table>	
			<@trow label="resource.title" val=resource.title!/>  
			<@trow label="resource.status" val=resource.status!/>  
			<@trow label="resource.contactName" val=resource.contactName!/>  
			<@trow label="resource.description" val=resource.description!/>
		</table>
	</fieldset>
</div>

<div>
	<fieldset>
		<h2>Resource Creator</h2>
		<table>
			<@trow label="eml.resourceCreator.firstName" val=eml.resourceCreator.firstName!/>  
			<@trow label="eml.resourceCreator.lastName" val=eml.resourceCreator.lastName!/>  
			<@trow label="eml.resourceCreator.organisation" val=eml.resourceCreator.organisation!/>  
			<@trow label="eml.resourceCreator.position" val=eml.resourceCreator.position!/>  
			<@trow label="eml.resourceCreator.phone" val=eml.resourceCreator.phone!/>  
			<@trow label="eml.resourceCreator.email" val=eml.resourceCreator.email!/>  
			<@trow label="agent.address.homepage" val=eml.resourceCreator.homepage!/>  
			<@trow label="agent.address.address" val=eml.resourceCreator.address.address!/>  
			<@trow label="agent.address.postalCode" val=eml.resourceCreator.address.postalCode!/>  
			<@trow label="agent.address.city" val=eml.resourceCreator.address.city!/>  
			<@trow label="agent.address.province" val=eml.resourceCreator.address.province!/>  
			<@trow label="agent.address.country" val=eml.resourceCreator.address.country!/>  
		</table>
	</fieldset>
</div>

<div>
	<fieldset>
		<h2>Taxonomic Coverage</h2>
		<table>
			<@trow label="eml.taxonomicCoverageDescription" val=eml.taxonomicCoverageDescription!/>  
			 <tr>
			  <th><@s.text name="eml.taxonomicClassification"/></th>
			  <td><#list eml.taxonomicClassification as tk>${tk.scientificName!tk.commonName!} </#list></td>
			 </tr>
			<@trow label="eml.lowestCommonTaxon.scientificName" val=eml.lowestCommonTaxon.scientificName!/>  
			<@trow label="eml.lowestCommonTaxon.commonName" val=eml.lowestCommonTaxon.commonName!/>  
			<@trow label="eml.lowestCommonTaxon.rank" val=eml.lowestCommonTaxon.rank!/>
		</table>
	</fieldset>
</div>

<div>
	<fieldset>
		<h2>Spatial Coverage</h2>
		<table>
			<@trow label="eml.geographicCoverage.boundingCoordinates" val=eml.geographicCoverage.boundingCoordinates!/>  
			<@trow label="eml.geographicCoverage.description" val=eml.geographicCoverage.description!/>  
		</table>
	</fieldset>
</div>

<div>
	<fieldset>
		<h2>Temporal Coverage</h2>
		<table>
			<@trow label="eml.temporalCoverage.start" val=eml.temporalCoverage.start!/>  
			<@trow label="eml.temporalCoverage.end" val=eml.temporalCoverage.end!/>  
			<@trow label="eml.temporalCoverage.single" val=eml.temporalCoverage.single!/>  
		</table>
	</fieldset>
</div>

<div>
	<fieldset>
		<h2>Keywords</h2>
		<table>
			 <tr>
			  <th><@s.text name="eml.keywords"/></th>
			  <td><#list eml.keywords as k>${k} </#list></td>
			 </tr>
		</table>
	</fieldset>
</div>

<div>
	<fieldset>
		<h2>Project</h2>
		<table>
			<@trow label="eml.researchProject.title" val=eml.researchProject.title!/>  
			<@trow label="eml.personnelOriginator.organisation" val=eml.researchProject.personnelOriginator.organisation!/>  
			<@trow label="eml.researchProject.abstract" val=eml.researchProject.abstract!/>  
			<@trow label="eml.researchProject.funding" val=eml.researchProject.funding!/>  
			<@trow label="eml.researchProject.studyAreaDescription" val=eml.researchProject.studyAreaDescription!/>  
			<@trow label="eml.researchProject.designDescription" val=eml.researchProject.designDescription!/>  
		</table>
	</fieldset>
</div>

<div>
	<fieldset>
		<h2>Methods</h2>
		<table>
			<@trow label="eml.methods" val=eml.methods!/>  
			<@trow label="eml.samplingDescription" val=eml.samplingDescription!/>  
			<@trow label="eml.qualityControl" val=eml.qualityControl!/>  
		</table>
	</fieldset>
</div>

<div>
	<fieldset>
		<h2>Rights</h2>
		<table>
			<@trow label="eml.purpose" val=eml.purpose!/>  
			<@trow label="eml.maintenance" val=eml.maintenance!/>  
			<@trow label="eml.intellectualRights" val=eml.intellectualRights!/>  
		</table>
	</fieldset>
</div>
</@s.form>
