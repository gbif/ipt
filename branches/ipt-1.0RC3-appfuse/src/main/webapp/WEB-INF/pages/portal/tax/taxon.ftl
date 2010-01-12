<head>
    <title><@s.text name="taxon.title"/></title>
    <meta name="resource" content="${taxon.resource.title}"/>
    <meta name="menu" content="ExplorerMenu"/>
    <meta name="submenu" content="tax"/>
    <meta name="heading" content="${taxon.scientificName}"/>
    <script>
	$(document).ready(function(){
		$("a#annotationToggle").click(function(e){
			e.preventDefault();
			$('#annotations').slideToggle('normal');
		});    
	});
    </script>
    <style>
	    th{padding-right:15px;}
    </style>
</head>

<div class="break20"></div>
<img class="taxDetailImg" src="${cfg.getResourceLogoUrl(resourceId)}" />

<@s.form>

<#assign extRec=rec>
<#assign rec=taxon>
<#assign resourceId=taxon.resource.id>
<#include "/WEB-INF/pages/inc/coreDetails.ftl">  
<#assign rec=extRec>

<div class="horizontal_dotted_line_large_soft"></div>	
<div id="basics">
	<fieldset>
		<h2>Basics</h2>
		<table>	
		
			<tr>
			  <th><@s.text name="taxon.taxonAccordingTo"/></th>
			  <td>${taxon.taxonAccordingTo!}</td>
			</tr>
			<tr>
			  <th><@s.text name="taxon.namePublishedIn"/></th>
			  <td>${taxon.namePublishedIn!}</td>
			</tr>
			<tr>
			  <th><@s.text name="taxon.code"/></th>
			  <td>${taxon.nomenclaturalCode!}</td>
			</tr>
			<tr>
			  <th><@s.text name="taxon.taxonRank"/></th>
			  <td>${taxon.taxonRank!}</td>
			</tr>
			<tr>
			  <th><@s.text name="taxon.nomenclaturalStatus"/></th>
			  <td>${taxon.nomenclaturalStatus!}</td>
			</tr>
			<tr>
			  <th><@s.text name="taxon.taxonomicStatus"/></th>
			  <td>${taxon.taxonomicStatus!}</td>
			</tr>
			<tr>
			<#if taxon.getParent()??>
			  <th><@s.text name="taxon.parent"/></th>
			  <td><#if taxon.getParent()??><a href='<@s.url value="/taxDetail.html?resourceId=${resourceId}&guid=${taxon.getHigherTaxonID()}"/>'>${taxon.getHigherTaxon()!}</a></#if></td>
			</#if>
			<#if taxon.getAcceptedTaxon()??>
			  <th><@s.text name="taxon.accepted"/></th>
			  <td><#if taxon.getAcceptedTaxon()??><a href="<@s.url value='/taxDetail.html?resourceId=${resourceId}&guid=${taxon.getAcceptedTaxonID()}'/>">${taxon.getAcceptedTaxon()!}</a></#if></td>
			</#if>
			<#if taxon.getBasionym()??>
			  <th><@s.text name="taxon.basionym"/></th>
			  <td><#if taxon.getBasionym()??><a href="<@s.url value='/taxDetail.html?resourceId=${resourceId}&guid=${taxon.getBasionymID()}'/>">${taxon.getBasionym()!}</a></#if></td>
			</#if>
			</tr>
			<tr>
			  <th><@s.text name="taxon.binomial"/></th>
			  <td>${taxon.binomial!}</td>
			</tr>
			<tr>
			  <th><@s.text name="taxon.specificEpithet"/></th>
			  <td>${taxon.specificEpithet!}</td>
			</tr>
			<tr>
			  <th><@s.text name="taxon.infraspecificEpithet"/></th>
			  <td>${taxon.infraspecificEpithet!}</td>
			</tr>
			<tr>
			  <th><@s.text name="taxon.scientificNameAuthorship"/></th>
			  <td>${taxon.scientificNameAuthorship!}</td>
			</tr>
			<tr>
			  <th><@s.text name="taxon.notes"/></th>
			  <td>${taxon.notes!}</td>
			</tr>
		</table>
	</fieldset>
	<div class="horizontal_dotted_line_large_soft"></div>		
</div>

<#if (synonyms?size>0)>
<div id="synonymy">
	<fieldset>
		<h2><@s.text name="taxon.synonymy"/></h2>
		<table>
			<tr>
			  <th><@s.text name="taxon.scientificName"/></th>
			  <th><@s.text name="taxon.taxonomicstatus"/></th>
			  <th><@s.text name="taxon.nomenclaturalstatus"/></th>
			</tr>
			<#list synonyms as syn>	
			<tr>
			  <td><a href='<@s.url value="/taxDetail.html?resourceId=${resourceId}&id=${syn.id?c}"/>'>${syn.scientificName}</a></td>
			  <td>${syn.taxonomicStatus!}</td>
			  <td>${syn.nomenclaturalStatus!}</td>
			</tr>
			</#list>
		</table>
	</fieldset>
	<div class="horizontal_dotted_line_large_soft"></div>		
</div>
</#if>

<#if (stats?size>0)>
<div id="stats">
	<fieldset>
		<h2><@s.text name="taxon.included"/></h2>
		<table>
			<#list stats as st>	
			<tr>
			  <th>${st.label}&nbsp;</th>
			  <td><a href="<@s.url value='/taxListByRank.html?resourceId=${resourceId?c}&category=${st.label}&id=${taxon.id?c}'/>">${st.count} taxa</a></td>
			</tr>
			</#list>
		</table>
	</fieldset>
	<div class="horizontal_dotted_line_large_soft"></div>	
</div>
</#if>

<#list extensions as ext>
<fieldset>
	<h2>${ext.name}</h2>	
	<table>	
	<#list rec.getExtensionRecords(ext) as eRec>
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