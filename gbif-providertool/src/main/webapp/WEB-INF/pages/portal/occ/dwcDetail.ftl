<head>
    <title><@s.text name="taxon.title"/></title>
    <meta name="resource" content="${dwc.resource.title}"/>
    <meta name="submenu" content="resource"/>
</head>


<img class="right" src="${cfg.getResourceLogoUrl(resource_id)}" />

<h2>${dwc.collectionCode} - ${dwc.catalogNumber}</h2>	
<h3>${dwc.scientificName}</h3>

<@s.form>

<table>	
 <tr>
	<th>GUID</th>
	<td><a href="${cfg.getDetailUrl(dwc)}">${dwc.guid}</a></td>
 </tr>
 <tr>
	<th>SourceID</th>
	<td><a href="${dwc.link}">${dwc.localId}</a></td>
 </tr>
</table>

<#assign core=dwc.resource.coreMapping>
<fieldset>
	<h2>${core.extension.name}</h2>	
	<table>	
	<#list core.extension.properties as p>
	 <#if core.hasMappedProperty(p)>
	  <tr>
		<th>${p.name}</th>
		<td>${dwc.getPropertyValue(p)!"---"}</td>
	  </tr>
	 </#if>
	</#list>
	</table>
</fieldset>

<#list dwc.resource.extensionMappings as view>
<fieldset>
	<h2>${view.extension.name}</h2>	
	<table>	
	<#list view.propertyMappings.values() as pm>
	 <tr>
		<th>${pm.property.name}</th>
		<td>Foo Bar | ${pm.property.columnName}</td>
	 </tr>
	</#list>
	</table>
</fieldset>
</#list>
	
</@s.form>
