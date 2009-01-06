<table>	
 <tr>
	<th>GUID</th>
	<td><a href="${cfg.getDetailUrl(rec)}">${rec.guid}</a></td>
 </tr>
 <#if localId??>
	 <tr>
		<th>SourceID</th>
		<td><#if rec.link??><a href="${rec.link}">${rec.localId}</a><#else>${rec.localId}</#if></td>
	 </tr>
 </#if>
 <tr>
	<th>Data</th>
	<td><a href="${cfg.getDetailUrl(rec,'xml')}">XML</a></td>
 </tr>
 <!--
 <#if resourceType=="occ">
	 <tr>
		<th>Related</th>
		<td><a href="/occTaxon.html?resource_id=${resource_id?c}&id=${rec.id?c}">Taxon Occurrences</a></td>
	 </tr>
 </#if>
  -->
</table>