<%@ include file="/common/taglibs.jsp"%>
<s:if test="resource.id>0">
<script>
var ajaxData = { 
	resource_id: <s:property value="%{resource.id}"/>, 
	resourceType:'<s:property value="%{resourceType}"/>', 
};
var params = {
		url: '<s:url value="/ajax/publish.do"/>',
		timeout: 5000,
		data: ajaxData,
		dataType: 'html',
		type: 'GET',	
		error: function (XMLHttpRequest, textStatus, errorThrown) {
			$("div.loading").hide();
			alert("There has been an error receiving the publishing status.\n\nReason:"+textStatus);
			//console.debug(errorThrown);
			//console.debug(XMLHttpRequest);
			},
		success: publishResponse
};
function publishResponse(data, textStatus){
	$("div.loading").hide();
    $('div#publish').html(data);
    // add onclick for the new button
	$("#btnPublish").click(function(e){
		e.preventDefault(); 
		//$("div.loading").show();
		params['type']='POST';
		params['url']=$("#publishForm").attr("action");
		params['data']=ajaxData;
		//window.console.log(params);
		$.ajax(params);
	});
}
$(document).ready(function(){
	$.ajax(params);
});
</script>
<div class="loading" style="display:none">
 <img src='<s:url value="/images/ajax-loader.gif"/>'/>
</div>
<div id="publish">
 <p>loading status <img src='<s:url value="/images/ajax-loader.gif"/>'/></p>
</div>
</s:if>
