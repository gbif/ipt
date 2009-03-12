<%@ include file="/common/taglibs.jsp"%>
<s:if test="resource.id>0">
<script>
function postPublish(){
	$.post('<@s.url value="/ajax/publish.do"/>', { resource_id:${resource.id}, resourceType:${resourceType} },
	  function(data){
	  	console.debug(data);
	    $('div#publish').html(data);
	});
}
$(document).ready(function(){
	alert("Guten morgen");
	$.get('<@s.url value="/ajax/publish.do"/>', { resource_id:${resource.id}, resourceType:${resourceType} },
	  function(data){
	  	console.debug(data);
	    $('div#publish').html(data);
	    // add onclick for the new button
		$("#btnPublish").click(function(e){
			e.preventDefault(); 
			postPublish();
		});
	});
}
</script>
<div id="publish">
  <div class="cleanContainer">
	<p><s:text name="dirty.clean"/></p>
  </div>
</div>
</s:if>
