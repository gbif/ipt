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
 <tr>
	<th>Annotations</th>
	<td>
	  <#if annotations??>
		<a href="Javascript:Effect.toggle('annotations', 'blind', { duration: 0.3 }); return false;">${annotations?size} notes</a> 
	  <#else>
		None 
	  </#if>
	</td>
 </tr> 
</table>


<#if annotations??>
<div id="annotations" style="display:none">
<#list annotations as annotation>
  <div class="annotation" id="annotation-${annotation.id}">
	<h3>${annotation.type}</h3>
	<span>${annotation.creator}, ${annotation.created?datetime?string}</span>
	<p>${annotation.note}</p>
  </div>
</#list>
</div>
</#if>