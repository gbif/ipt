<%@ include file="/common/taglibs.jsp"%>
<s:if test="resource.id>0">
<script>
var data = { 
	resource_id: <s:property value="%{resource.id}"/>, 
	resourceType:'<s:property value="%{resourceType}"/>', 
};
var params = {
	url: '<s:url value="/ajax/publish.do"/>',
	timeout: 5000,
	data: data,
	dataType: 'html',
	error: function (XMLHttpRequest, textStatus, errorThrown) {
		$("div.loading").hide();
		alert("There has been an publishing error.\nPlease try again later.\n\nReason:"+textStatus);
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
		$("div.loading").show();
		params['type']='POST';	
		//console.debug("Publish POST");
		//console.debug(params);
		$.ajax(params);
	});
    $("#statusContainer div.arrow").click(
      function () {
      	$("div.arrow", this).toggle(0);
      	$("div#registryDetails").slideToggle("normal");
      } 
    );
}
$(document).ready(function(){
	params['type']='GET';	
	//console.debug("Publish GET");
	//console.debug(params);
	$.ajax(params);
});
</script>
<div class="loading" style="display:none">
  Publishing <img src='<s:url value="/images/ajax-loader.gif"/>'/>
</div>
<div id="publish">
 <p>loading status...</p>
</div>
</s:if>
