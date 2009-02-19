<head>
    <title><@s.text name="occResource.overview"/></title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="menu" content="ExplorerMenu"/>
    <meta name="submenu" content="tax"/>    
	<script>
	function updateByTaxon(){
		var url = '<@s.url value="/ajax/taxResourceStatsByTaxon.html"/>';
		var params = { resource_id: ${resource_id}, type: $("#rank").val() }; 
		var target = '#imgByTaxon';	
		ajaxHtmlUpdate(url, target, params);
	};
	function updateByStatus(){
		var url = '<@s.url value="/ajax/taxResourceStatsByStatus.html"/>';
		var params = { resource_id: ${resource_id}, type: $("#statusClass").val() }; 
		var target = '#imgByStatus';	
		ajaxHtmlUpdate(url, target, params);
	};
	function updateByRank(){
		var url = '<@s.url value="/ajax/taxResourceStatsByRank.html"/>';
		var params = { resource_id: ${resource_id} }; 
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
<h1 class="h1tax"><@s.text name="resource.description"/></h1>
<div class="horizontal_dotted_line_large"></div>
<div class="rounded" style="margin-bottom:30px">
<fieldset>
	<legend><!--<@s.text name="resource.description"/>--></legend>
	<div id="metadata">
		<img class="right" src="${cfg.getResourceLogoUrl(resource_id)}" />
		<span><@s.property value="resource.description"/></span>
		
		<@s.label key="dataResource.cache" value="${(resource.lastUpload.recordsUploaded)!0} total records uploaded ${(resource.lastUpload.executionDate)!}"/>
		<ul class="minimenu">
			<li class="last">
				<a id="showWebservices"><@s.text name="dataResource.services"/></a>
			</li>
			<li>
				<a href="metaResource.html?resource_id=${resource_id}">Full Metadata</a>
			</li>
			<li>
				<a href="annotations.html?resource_id=${resource_id}">Annotations</a>
			</li>
		</ul>
		<div id="services" style="display:none">
			<table class="lefthead">
				<tr>
					<th>Contact</th>
					<td>${resource.contactName} <#if resource.contactEmail??>&lt;${resource.contactEmail}&gt;</#if></td>
				</tr>
				<tr>
					<th>Homepage</th>
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
</div>
</@s.form>

<@s.push value="resource">

<div id="tax-stats1" class="stats stat-table">
	<label><@s.text name="stats.taxStats"/></label>
	<table class="lefthead">
		<tr>
			<td>Distinct Names</td>
			<td>${numTaxa}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.numTerminalTaxa"/></td>
			<td>${numTerminalTaxa}</td>
		</tr>
		<tr>
			<td>Accepted Taxa</td>
			<td>${numAccepted}</td>
		</tr>
		<tr>
			<td>Other Names</td>
			<td>${numSynonyms}</td>
		</tr>
	</table>
</div>

<div id="tax-stats2" class="stats stat-table">
	<label><@s.text name="stats.taxStats"/></label>
	<table class="lefthead">
		<tr>
			<td>Kingdoms</td>
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
	<label><@s.text name="stats.taxStats"/></label>
	<table class="lefthead">
		<tr>
			<td>Common Names</td>
			<td>${numCommonNames}</td>
		</tr>
		<tr>
			<td>Common Name Languages</td>
			<td>${numCommonNameLanguages}</td>
		</tr>
		<tr>
			<td>Distributions</td>
			<td>${numDistributions}</td>
		</tr>
		<tr>
			<td>Distribution Regions</td>
			<td>${numDistributionRegions}</td>
		</tr>
	</table>
</div>

</@s.push>

<br class="clearfix" />

<div id="tax-pie" class="stats chart">
	<label><@s.text name="stats.byName"/></label>
	<div class="horizontal_dotted_line_graph"></div>	
	<@s.form id="rankForm">
		<@s.select id="rank" list="ranks" value="rank" theme="simple"/>
	</@s.form>
	<div id="imgByTaxon">
	</div>
</div>


<div id="status-pie" class="stats chart stat-right">
	<label><@s.text name="stats.byStatus"/></label>
	<div class="horizontal_dotted_line_graph"></div>	
	<@s.form id="statusClassForm">
		<@s.select id="statusClass" name="status" list="statusClasses" value="1" theme="simple"/>
	</@s.form>
	<div id="imgByStatus">
	</div>
</div>


<br class="clearfix" />


<div id="rank-pie" class="stats chart">
	<label><@s.text name="stats.byRank"/></label>
	<div class="horizontal_dotted_line_graph"></div>	
	<div id="imgByRank"></div>
</div>


<br class="clearfix" />

<!--<script type="text/javascript">
	Rounded('rounded', 6, 6);
</script> -->
