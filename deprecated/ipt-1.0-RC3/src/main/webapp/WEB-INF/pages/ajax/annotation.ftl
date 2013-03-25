<div id="annotation-${annotation.id}">
	<h3>${annotation.type}</h3>
	<p>${annotation.note}</p>
	<ul class="indexmenu">	
		<li>${annotation.creator}</li>
		<li>${annotation.created?datetime?string}</li>
		<#if annotation.guid??>
		<li><a href="detail.html?resourceId=${resourceId}&guid=${annotation.guid}">record</a></li>
		</#if>
		<li><a href="#" class="close">close</a></li>
	</ul>
</div>