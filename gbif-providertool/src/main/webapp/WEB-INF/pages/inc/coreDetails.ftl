<table>	
 <tr>
	<th>GUID</th>
	<td><a href="${cfg.getDetailUrl(rec)}">${rec.guid}</a></td>
 </tr>
 <#if rec.localId??>
	 <tr>
		<th>SourceID</th>
		<td><#if rec.link??><a href="${rec.link}">${rec.localId}</a><#else>${rec.localId}</#if></td>
	 </tr>
 </#if>
 <tr>
	<th>Data</th>
	<td><a href="${cfg.getDetailUrl(rec,'xml')}">XML</a> 
		<#if resourceType="tax"><a href="${cfg.getDetailUrl(rec,'rdf')}">RDF</a></#if>
	</td>
 </tr>
 <#if rec.occTotal??>
  <tr>
	<th><@s.text name="region.occTotal"/></th>
	<td>${rec.occTotal}</td>
 </tr>
 </#if>
 
</table>