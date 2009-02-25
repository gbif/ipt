<div class="annotation" id="annotation-${annotation.id}">
	<h3>${annotation.type}</h3>
	<p>${annotation.note}</p>
	<ul class="indexmenu">	
		<li>${annotation.creator}</li>
		<li>${annotation.created?datetime?string}</li>
		<#if annotation.guid??>
		<li><a href="detail.html?resource_id=${resource_id}&guid=${annotation.guid}">record</a></li>
		</#if>
		<li><a href="Javascript:$('#annotation-${annotation.id}').hide();">close</a></li>
	</ul>
</div>