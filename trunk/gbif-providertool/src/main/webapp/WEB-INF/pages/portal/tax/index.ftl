<head>
    <title><@s.text name="occResource.overview"/></title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="menu" content="ExplorerMenu"/>
    <meta name="submenu" content="tax"/>    
	<script>
	function updateByTaxon(){
		var url = '<@s.url value="/ajax/taxResourceStatsByTaxon.html"/>';
		var params = { resourceId: ${resourceId}, type: $("#rank").val() }; 
		var target = '#imgByTaxon';	
		ajaxHtmlUpdate(url, target, params);
	};
	function updateByStatus(){
		var url = '<@s.url value="/ajax/taxResourceStatsByStatus.html"/>';
		var params = { resourceId: ${resourceId}, type: $("#statusClass").val() }; 
		var target = '#imgByStatus';	
		ajaxHtmlUpdate(url, target, params);
	};
	function updateByRank(){
		var url = '<@s.url value="/ajax/taxResourceStatsByRank.html"/>';
		var params = { resourceId: ${resourceId} }; 
		var target = '#imgByRank';	
		ajaxHtmlUpdate(url, target, params);
	};
	$(document).ready(function(){
		updateByTaxon();
		updateByStatus();
		updateByRank();
		listenToChange("#rank", updateByTaxon);
		listenToChange("#statusClass", updateByStatus);
		
	    $("#showWebservices").click(function () {
	      $("#services").slideToggle("normal");
	    });
	});
	</script>
	
</head>
	
<@s.form>
<h1 style="margin-bottom: 13px;"><@s.text name="resource.description"/></h1>
<div class="horizontal_dotted_line_large"></div>
<fieldset>
	<div id="metadata">
		<img class="right" src="${cfg.getResourceLogoUrl(resourceId)}" />
		<span><@s.property value="resource.description"/></span>
		<p style="margin-left: 0px;">
		<label><@s.text name="dataResource.cache"/></label>
		<label>${(resource.lastUpload.recordsUploaded)!0} <@s.text name='occ.index.recordsloaded'/> ${(resource.lastUpload.executionDate)!}</label>
		</p>
		
		<ul class="minimenu">
			<li class="last">
				<a id="showWebservices"><@s.text name="dataResource.services"/></a>
			</li>
			<li>
				<a href="metaResource.html?resourceId=${resourceId}"><@s.text name='occ.index.fullmetadata'/></a>
			</li>
			<li>
				<a href="annotations.html?resourceId=${resourceId}"><@s.text name='occ.index.annotations'/></a>
			</li>
		</ul>
		<div class="break35"></div>
		<div id="services" style="display:none">
			<table class="lefthead">
				<tr>
					<th><@s.text name='occ.index.contact'/></th>
					<td>${resource.contactName} <#if resource.contactEmail??>&lt;${resource.contactEmail}&gt;</#if></td>
				</tr>
				<tr>
					<th><@s.text name='occ.index.homepage'/></th>
					<td><#if resource.link??><a href="${resource.link}">${resource.link}</a></#if></td>
				</tr>
				<tr>
					<th>EML</th>
					<td><a href="${cfg.getEmlUrl(resource.guid)}">${cfg.getEmlUrl(resource.guid)}</a></td>
				</tr>
				<tr>
					<th><@s.text name="dataResource.archive"/></th>
					<td><a href="${cfg.getArchiveUrl(resource.guid)}">${cfg.getArchiveUrl(resource.guid)}</a></td>
				</tr>
				<tr>
					<th>TCS</th>
					<td><a href="${cfg.getArchiveTcsUrl(resource.guid)}">${cfg.getArchiveTcsUrl(resource.guid)}</a></td>
				</tr>
			</table>
		</div>
	</div>
</@s.form>

<@s.push value="resource">

<div id="tax-stats1" class="stats stat-table">
	<label><@s.text name="stats.taxStats"/></label>
	<table class="lefthead">
		<tr>
			<td><@s.text name='occ.index.distinctnames'/></td>
			<td>${numTaxa}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.numTerminalTaxa"/></td>
			<td>${numTerminalTaxa}</td>
		</tr>
		<tr>
			<td><@s.text name='occ.index.acceptedtaxa'/></td>
			<td>${numAccepted}</td>
		</tr>
		<tr>
			<td><@s.text name='occ.index.othernames'/></td>
			<td>${numSynonyms}</td>
		</tr>
	</table>
</div>

<div id="tax-stats2" class="stats stat-table">
	<label><@s.text name="stats.taxStats"/></label>
	<table class="lefthead">
		<tr>
			<td><@s.text name='occ.index.kingdoms'/></td>
			<td>${numKingdoms}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.numOrders"/></td>
			<td>${numOrders}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.numFamilies"/></td>
			<td>${numFamilies}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.numGenera"/></td>
			<td>${numGenera}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.numSpecies"/></td>
			<td>${numSpecies}</td>
		</tr>
	</table>
</div>

<div id="tax-stats3" class="stats stat-table">
	<label><@s.text name="stats.extensions"/></label>
	<table class="lefthead">
 	  	<#list resource.getExtensionMappings() as e>
		<tr>
			<td>${e.extension.name}</td>
			<td>${e.recTotal}</td>
		</tr>
		</#list>
	</table>
</div>

</@s.push>

<br class="clearfix" />

<div id="tax-pie" class="stats chart">
	<label><@s.text name="stats.byName"/></label>
	<@s.form id="rankForm">
		<@s.select id="rank" list="ranks" value="rank" theme="simple"/>
	</@s.form>
	<div id="imgByTaxon">
	</div>
</div>


<div id="status-pie" class="stats chart stat-right">
	<label><@s.text name="stats.byStatus"/></label>
	<@s.form id="statusClassForm">
		<@s.select id="statusClass" name="status" list="statusClasses" value="1" theme="simple"/>
	</@s.form>
	<div id="imgByStatus">
	</div>
</div>


<br class="clearfix" />


<div id="rank-pie" class="stats chart">
	<label><@s.text name="stats.byRank"/></label>
	<div id="imgByRank"></div>
</div>


<br class="clearfix" />