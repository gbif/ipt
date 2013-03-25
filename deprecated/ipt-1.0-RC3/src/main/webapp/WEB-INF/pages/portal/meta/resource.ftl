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
	<td><a href="${cfg.getEmlUrl(resource.guid)}">EML</a></td>
 </tr>
 <tr>
	<th><@s.text name="meta.resource.annotatations"/></th>
	<td><a href="annotations.html?resourceId=${resourceId}"><@s.text name="meta.resource.annotations"/></a></td>
 </tr>
</table>
</div>

<div id="basics">
	<fieldset>
		<h2><@s.text name="meta.resource.basics"/></h2>
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
		<h2><@s.text name="meta.resource.creator"/></h2>
		<table>
			<@trow label="eml.resourceCreator.firstName" val=eml.resourceCreator().firstName!/>  
			<@trow label="eml.resourceCreator.lastName" val=eml.resourceCreator().lastName!/>  
			<@trow label="eml.resourceCreator.organisation" val=eml.resourceCreator().organisation!/>  
			<@trow label="eml.resourceCreator.position" val=eml.resourceCreator().position!/>  
			<@trow label="eml.resourceCreator.phone" val=eml.resourceCreator().phone!/>  
			<@trow label="eml.resourceCreator.email" val=eml.resourceCreator().email!/>  
			<@trow label="agent.homepage" val=eml.resourceCreator().homepage!/>  
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
		<h2><@s.text name="meta.resource.taxoncoverage"/></h2>
		<table>
			<@trow label="eml.taxonomicCoverageDescription" val=eml.taxonomicCoverageDescription!/>  
			 <tr>
			  <th><@s.text name="eml.taxonomicClassification"/></th>
			  <td><#list eml.taxonomicClassification as tk>${tk.scientificName!tk.commonName!} </#list></td>
			 </tr>
			<@trow label="eml.lowestCommonTaxon.scientificName" val=eml.getLowestCommonTaxon().scientificName!/>  
			<@trow label="eml.lowestCommonTaxon.commonName" val=eml.getLowestCommonTaxon().commonName!/>  
			<@trow label="eml.lowestCommonTaxon.rank" val=eml.getLowestCommonTaxon().rank!/>
		</table>
	</fieldset>
	<div class="horizontal_dotted_line_large_soft"></div>
</div>

<div>
	<fieldset>
		<h2><@s.text name="meta.resource.spatialcoverage"/></h2>
		<table>
			<@trow label="eml.geographicCoverage.boundingCoordinates" val=(eml.getGeographicCoverage().boundingCoordinates.toStringShort(3))!/>  
			<@trow label="eml.geographicCoverage.description" val=eml.getGeographicCoverage().description!/>  
		</table>
	</fieldset>
	<div class="horizontal_dotted_line_large_soft"></div>
</div>

<div>
	<fieldset>
		<h2><@s.text name="meta.resource.temporalcoverage"/></h2>
		<table>
			<@trow label="eml.temporalCoverage.start" val=(eml.getTemporalCoverage().start?date?string)!/>  
			<@trow label="eml.temporalCoverage.end" val=(eml.getTemporalCoverage().end?date?string)!/>
			<#if eml.getTemporalCoverage().single??>  
			<@trow label="eml.temporalCoverage.single" val=eml.getTemporalCoverage().single?date?string!/>
			</#if>  
		</table>
	</fieldset>
	<div class="horizontal_dotted_line_large_soft"></div>
</div>

<div>
	<fieldset>
		<h2><@s.text name="meta.resource.keywords"/></h2>
		<table>
			 <tr>
			  <th><@s.text name="eml.keywords"/></th>
			  <td><#list eml.keywords as k>${k} </#list></td>
			 </tr>
		</table>
	</fieldset>
	<div class="horizontal_dotted_line_large_soft"></div>
</div>

<div>
	<fieldset>
		<h2>Project</h2>
		<table>
			<@trow label="eml.researchProject.title" val=eml.getResearchProject().title!/>  
			<@trow label="eml.researchProject.personnelOriginator.organisation" val=eml.getResearchProject().personnelOriginator.organisation!/>  
			<@trow label="eml.researchProject.abstract" val=eml.getResearchProject().abstract!/>  
			<@trow label="eml.researchProject.funding" val=eml.getResearchProject().funding!/>  
			<@trow label="eml.researchProject.studyAreaDescription" val=eml.getResearchProject().studyAreaDescription!/>  
			<@trow label="eml.researchProject.designDescription" val=eml.getResearchProject().designDescription!/>  
		</table>
	</fieldset>
	<div class="horizontal_dotted_line_large_soft"></div>
</div>

<div>
	<fieldset>
		<h2><@s.text name="meta.resource.methods"/></h2>
		<table>
			<@trow label="eml.methods" val=eml.methods!/>  
			<@trow label="eml.samplingDescription" val=eml.samplingDescription!/>  
			<@trow label="eml.qualityControl" val=eml.qualityControl!/>  
		</table>
	</fieldset>
	<div class="horizontal_dotted_line_large_soft"></div>
</div>

<div>
	<fieldset>
		<h2><@s.text name="meta.resource.rights"/></h2>
		<table>
			<@trow label="eml.purpose" val=eml.purpose!/>  
			<@trow label="eml.maintenance" val=eml.maintenance!/>  
			<@trow label="eml.intellectualRights" val=eml.intellectualRights!/>  
		</table>
	</fieldset>
</div>
</@s.form>