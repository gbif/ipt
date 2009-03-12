<%@ include file="/common/taglibs.jsp"%>
<s:if test="resource.id>0">
<script>
var params = { resource_id: <s:property value="%{resource.id}"/>, resourceType:'<s:property value="%{resourceType}"/>' };
function publishResponse(data){
	$("div.loading").hide();
    $('div#publish').html(data);
    // add onclick for the new button
	$("#btnPublish").click(function(e){
		e.preventDefault(); 
		$("div.loading").show();
		$.post('<s:url value="/ajax/publish.do"/>', params, publishResponse);
	});
}
$(document).ready(function(){
	//$("div.loading").hide();
	$.get('<s:url value="/ajax/publish.do"/>', params, publishResponse);
});
</script>
<div class="loading">
  Publishing <img src='<s:url value="/images/ajax-loader.gif"/>'/>
</div>
<div id="publish">
  <div class="cleanContainer">
	<p><s:text name="dirty.clean"/></p>
  </div>
</div>
</s:if>
