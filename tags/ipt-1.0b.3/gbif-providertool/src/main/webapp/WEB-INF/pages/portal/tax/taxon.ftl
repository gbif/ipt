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
			<#if taxon.getParent()??>
			  <th><@s.text name="taxon.parent"/></th>
			  <td><#if taxon.getParent()??><a href="taxDetail.html?resource_id=${resource_id}&id=${taxon.getParent().id?c}">${(taxon.getParent().scientificName)!}</a></#if></td>
			</#if>
			<#if taxon.getAcceptedTaxon()??>
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

<#if (synonyms?size>0)>
<div id="synonymy">
	<fieldset>
		<h2><@s.text name="taxon.synonymy"/></h2>
		<table>
			<tr>
			  <th>Scientific Name</th>
			  <th>Taxonomic Status</th>
			  <th>Nomenclatural Status</th>
			</tr>
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

<#if (commonNames?size>0)>
<div id="commonNames">
	<fieldset>
		<h2><@s.text name="taxon.commonNames"/></h2>
		<table>
			<tr>
			  <th>Common Name</th>
			  <th>Language</th>
			  <th>Region</th>
			</tr>
			<#list commonNames as cn>	
			<tr>
			  <td>${cn.name!}</td>
			  <td>${cn.lang!}</td>
			  <td>${cn.region!}</td>
			</tr>
			</#list>
		</table>
	</fieldset>
</div>
</#if>

<#if (distributions?size>0)>
<div id="distribution">
	<fieldset>
		<h2><@s.text name="taxon.distribution"/></h2>
		<table>
			<tr>
			  <th>Region</th>
			  <th>Status</th>
			</tr>
			<#list distributions as d>	
			<tr>
			  <td>${d.region!}</td>
			  <td>${d.status!}</td>
			</tr>
			</#list>
		</table>
	</fieldset>
</div>
</#if>

<#if (stats?size>0)>
<div id="stats">
	<fieldset>
		<h2><@s.text name="taxon.statistics"/></h2>
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

<#list extensions as ext>
<fieldset>
	<h2>${ext.name}</h2>	
	<table>	
	<#list extWrapper.getExtensionRecords(ext) as eRec>
		<#list eRec.properties as p>
		 <tr>
			<th>${p.name}</th>
			<td>${eRec.getPropertyValue(p)}</td>
		 </tr>
		</#list>
		 <tr>
			<th>&nbsp;</th>
			<td>&nbsp;</td>
		 </tr>
	</#list>
	</table>
</fieldset>
</#list>

</@s.form>
