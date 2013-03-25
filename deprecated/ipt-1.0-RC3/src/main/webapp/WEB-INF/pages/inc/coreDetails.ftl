<table>	
 <tr>
	<th><@s.text name='coredetails.guid'/></th>
	<td><a href="${cfg.getDetailUrl(rec)}">${rec.guid}</a></td>
 </tr>
 <#if rec.sourceId??>
	 <tr>
		<th><@s.text name='coredetails.sourceid'/></th>
		<td><#if rec.link??><a href="${rec.link}">${rec.sourceId}</a><#else>${rec.sourceId}</#if></td>
	 </tr>
 </#if>
 <tr>
	<th><@s.text name='coredetails.data'/></th>
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
	<th><@s.text name='coredetails.annotations'/></th>
	<td>
	  <#if annotations??>
		<a id="annotationToggle" href="#">${annotations?size} <@s.text name='coredetails.notes'/></a> 
	  <#else>
		<@s.text name='coredetails.none'/> 
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