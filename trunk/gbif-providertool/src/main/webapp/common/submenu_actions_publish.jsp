<%@ include file="/common/taglibs.jsp"%>
<s:if test="resource.id>0">
<script>
var params = { resource_id: <s:property value="%{resource.id}"/>, resourceType:'<s:property value="%{resourceType}"/>' };
function postPublish(){
	$.post('<s:url value="/ajax/publish.do"/>', params, function(data){
	    $('div#publish').html(data);
	})
}
$(document).ready(function(){
	$.get('<s:url value="/ajax/publish.do"/>', params, function(data){
	    $('div#publish').html(data);
	    // add onclick for the new button
		$("#btnPublish").click(function(e){
			e.preventDefault(); 
			postPublish();
		});
	});
});
</script>
<div id="publish">
  <div class="cleanContainer">
	<p><s:text name="dirty.clean"/></p>
  </div>
</div>
</s:if>
