<table>	
 <tr>
	<th>GUID</th>
	<td><a href="${cfg.getDetailUrl(rec)}">${rec.guid}</a></td>
 </tr>
 <tr>
	<th>SourceID</th>
	<td><#if rec.link??><a href="${rec.link}">${rec.localId}</a><#else>${rec.localId}</#if></td>
 </tr>
 <tr>
	<th>Data</th>
	<td><a href="${cfg.getDetailUrl(rec,'xml')}">XML</a>, JSON</td>
 </tr>
</table>