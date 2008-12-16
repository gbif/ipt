<head>
    <title><@s.text name="taxon.title"/></title>
    <meta name="resource" content="${taxon.resource.title}"/>
    <meta name="submenu" content="tax"/>
    <meta name="heading" content="taxon.scientificName"/>
</head>
	


<div id="basics">
	<fieldset>
		<h2>${taxon.scientificName}</h2>
		<table>	
			<tr>
			  <th><@s.text name="taxon.code"/></th>
			  <td>${taxon.code}</td>
			</tr>
			<tr>
			  <th><@s.text name="taxon.basionym"/></th>
			  <td>${taxon.basionym.scientificName}</td>
			</tr>
			<tr>
			  <th><@s.text name="taxon.rank"/></th>
			  <td>${taxon.rank}</td>
			</tr>
			<tr>
			  <th><@s.text name="taxon.parent"/></th>
			  <td>${taxon.parent}</td>
			</tr>
			<tr>
			  <th><@s.text name="taxon.notes"/></th>
			  <td>${taxon.notes}</td>
			</tr>
		</table>
	</fieldset>
</div>

<#if !taxon.accepted>
<div id="synonymy">
	<fieldset>
		<h2>Synonymy</h2>
		<table>
			<#list synonyms as s>	
			<tr>
			  <th>&nbsp;</th>
			  <td>${s.scientificName}</td>
			</tr>
			</#list>
		</table>
	</fieldset>
</div>
</#if>

<div id="typification">
	<fieldset>
		<h2>Typification</h2>
		<table>
			<#list synonyms as s>	
			<tr>
			  <th>&nbsp;</th>
			  <td>${s.scientificName}</td>
			</tr>
			</#list>
		</table>
	</fieldset>
</div>

<div id="distribution">
	<fieldset>
		<h2>Distribution</h2>
		<table>
			<#list synonyms as s>	
			<tr>
			  <th>&nbsp;</th>
			  <td>${s.scientificName}</td>
			</tr>
			</#list>
		</table>
	</fieldset>
</div>

<div id="stats">
	<fieldset>
		<h2>Statistics</h2>
		<table>
			<#list stats as s>	
			<tr>
			  <th>${s.label}</th>
			  <td>${s.count}</td>
			</tr>
			</#list>
		</table>
	</fieldset>

</div>

