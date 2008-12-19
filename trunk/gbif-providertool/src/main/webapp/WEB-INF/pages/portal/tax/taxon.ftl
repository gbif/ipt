<head>
    <title><@s.text name="taxon.title"/></title>
    <meta name="resource" content="${taxon.resource.title}"/>
    <meta name="submenu" content="tax"/>
    <!-- meta name="heading" content="${taxon.scientificName}"/ -->
</head>
	

<img class="right" src="${cfg.getResourceLogoUrl(resource_id)}" />
<h2>${taxon.scientificName}</h2>	

<@s.form>

<#assign rec=taxon>
<#include "/WEB-INF/pages/inc/coreDetails.ftl">  


<div id="basics">
	<fieldset>
		<table>	
			<tr>
			  <th><@s.text name="taxon.code"/></th>
			  <td>${taxon.nomenclaturalCode!}</td>
			</tr>
			<tr>
			  <th><@s.text name="taxon.rank"/></th>
			  <td>${taxon.rank!}</td>
			</tr>
			<tr>
			  <th><@s.text name="taxon.taxonomicStatus"/></th>
			  <td>${taxon.taxonomicStatus!}</td>
			</tr>
			<tr>
			<#if taxon.accepted>
			  <th><@s.text name="taxon.parent"/></th>
			  <td><#if taxon.getParent()??><a href="taxDetail.html?resource_id=${resource_id}&id=${taxon.getParent().id?c}">${(taxon.getParent().scientificName)!}</a></#if></td>
			<#else>
			  <th><@s.text name="taxon.accepted"/></th>
			  <td><#if taxon.getAcceptedTaxon()??><a href="taxDetail.html?resource_id=${resource_id}&id=${taxon.getAcceptedTaxon().id?c}">${(taxon.getAcceptedTaxon().scientificName)!}</a></#if></td>
			</#if>
			</tr>
			<tr>
			  <th><@s.text name="taxon.nomenclaturalStatus"/></th>
			  <td>${taxon.nomenclaturalStatus!}</td>
			</tr>
			<tr>
			  <th><@s.text name="taxon.basionym"/></th>
			  <td>${(taxon.getBasionym().scientificName)!}</td>
			</tr>
			<tr>
			  <th><@s.text name="taxon.notes"/></th>
			  <td>${taxon.notes!}</td>
			</tr>
		</table>
	</fieldset>
</div>

<#if taxon.accepted>
<div id="synonymy">
	<fieldset>
		<legend><@s.text name="taxon.synonymy"/></legend>
		<table>
			<#list synonyms as s>	
			<tr>
			  <td><a href="taxDetail.html?resource_id=${resource_id}&id=${s.id?c}">${s.scientificName}</a></td>
			  <td>${s.taxonomicStatus!}</td>
			  <td>${s.nomenclaturalStatus!}</td>
			</tr>
			</#list>
		</table>
	</fieldset>
</div>
</#if>

<div id="typification">
	<fieldset>
		<legend><@s.text name="taxon.typification"/></legend>
		<table>
		</table>
	</fieldset>
</div>

<div id="distribution">
	<fieldset>
		<legend><@s.text name="taxon.distribution"/></legend>
		<table>
		</table>
	</fieldset>
</div>


<#if (stats?size>0)>
<div id="stats">
	<fieldset>
		<legend><@s.text name="taxon.statistics"/></legend>
		<p>Number of accepted taxa included:</p>
		<table>
			<#list stats as s>	
			<tr>
			  <th>${s.label}</th>
			  <td><a href="taxListByRank.html?resource_id=${resource_id?c}&category=${s.label}&id=${taxon.id?c}">#${s.count}</a></td>
			</tr>
			</#list>
		</table>
	</fieldset>

</div>
</#if>

</@s.form>
