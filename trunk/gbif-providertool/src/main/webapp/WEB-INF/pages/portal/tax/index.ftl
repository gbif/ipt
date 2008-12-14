<head>
    <title><@s.text name="occResource.overview"/></title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="submenu" content="tax"/>
</head>
	
  
<@s.form>
<fieldset>
	<legend><@s.text name="resource.description"/></legend>
	<div id="metadata">
		<img class="right" src="${cfg.getResourceLogoUrl(resource_id)}" />
		<span><@s.property value="resource.description"/></span>
		
		<@s.label key="dataResource.cache" value="${(resource.lastUpload.recordsUploaded)!0} total records uploaded ${(resource.lastUpload.executionDate)!}"/>
		<ul class="minimenu">
			<li>
				<a onclick="Effect.toggle('services', 'blind', { duration: 0.3 }); return false;">(<@s.text name="dataResource.services"/>)</a>
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
					<td><a href="${cfg.getDumpArchiveUrl(resource_id)}">${cfg.getDumpArchiveUrl(resource_id)}</a></td>
				</tr>
				<tr>
					<th>TCS</th>
					<td><a href="${cfg.getTcsEndpoint(resource_id)}">${cfg.getTcsEndpoint(resource_id)}</a></td>
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
			<td><@s.text name="occResource.numTaxa"/></td>
			<td>${numTaxa}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.numTerminalTaxa"/></td>
			<td>${numTerminalTaxa}</td>
		</tr>
		<tr>
			<td>Accepted Taxa</td>
			<td>${numTaxa-numSynonyms}</td>
		</tr>
		<tr>
			<td>Synonyms</td>
			<td>${numSynonyms}</td>
		</tr>
	</table>
</div>

<div id="tax-stats2" class="stats stat-table">
	<label><@s.text name="stats.taxStats"/></label>
	<table class="lefthead">
		<tr>
			<td>Kingdoms</td>
			<td>Plantae, Animalia</td>
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
	</table>
</div>

<div id="tax-stats3" class="stats stat-table">
	<label><@s.text name="stats.taxStats"/></label>
	<table class="lefthead">
		<tr>
			<td>Common Names</td>
			<td>...</td>
		</tr>
		<tr>
			<td>Common Name Languages</td>
			<td>...</td>
		</tr>
		<tr>
			<td>Distributions</td>
			<td>...</td>
		</tr>
		<tr>
			<td>Distribution Regions</td>
			<td>...</td>
		</tr>
	</table>
</div>

</@s.push>



<br class="clearfix" />


<div id="tax-pie" class="stats chart">
	<label><@s.text name="stats.byTaxon"/></label>
	<@s.form id="rankForm">
		<@s.select id="rank" list="ranks" value="rank" theme="simple"/>
	</@s.form>
	<@s.url id="imgByTaxonUrl" action="taxResourceStatsByTaxon" namespace="/ajax" includeParams="get"/>
	<div id="imgByTaxon">
	</div>
</div>

<script>
function updateByTaxon(){
	var url = '${imgByTaxonUrl}';
	var params = { type: $F("rank") }; 
	var target = 'imgByTaxon';	
	var myAjax = new Ajax.Updater(target, url, {method: 'get', parameters: params});
};
$('rank').observe('change', updateByTaxon);
updateByTaxon();
</script>



<div id="status-pie" class="stats chart stat-right">
	<label><@s.text name="stats.byStatus"/></label>
	<@s.form id="statusClassForm">
		<@s.select id="statusClass" name="status" list="statusClasses" value="1" theme="simple"/>
	</@s.form>
	<@s.url id="imgByStatusUrl" action="taxResourceStatsByStatus" namespace="/ajax" includeParams="get"/>
	<div id="imgByStatus">
	</div>
</div>
<script>
function updateByStatus(){
	var url = '${imgByStatusUrl}';
	var params = { type: $F("statusClass") }; 
	var target = 'imgByStatus';	
	var myAjax = new Ajax.Updater(target, url, {method: 'get', parameters: params});
};
$('statusClass').observe('change', updateByStatus);
updateByStatus();
</script>	

<br class="clearfix" />


<div id="rank-pie" class="stats chart">
	<label><@s.text name="stats.byRank"/></label>
	<@s.url id="imgByRankUrl" action="taxResourceStatsByRank" namespace="/ajax" includeParams="get"/>
	<div id="imgByRank"></div>
</div>

<script>
function updateByRank(){
	var url = '${imgByRankUrl}';
	var target = 'imgByRank';	
	var myAjax = new Ajax.Updater(target, url, {method: 'get'});
};
updateByRank();
</script>


<br class="clearfix" />

