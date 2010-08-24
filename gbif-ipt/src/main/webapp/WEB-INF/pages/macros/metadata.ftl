<script type="text/javascript">
$(document).ready(function(){
	var	itemsCount=-1;
	calcNumberOfItems();
	
	function calcNumberOfItems(){
		var lastItem = $("#items .item:last-child").attr("id");
		if(lastItem != undefined)
			itemsCount=parseInt(lastItem.split("-")[1]);
		else
			itemsCount=-1;
	}
	
	$("#plus").click(function(event) {
		event.preventDefault();
		var newItem=$('#baseItem').clone();
		newItem.hide();
		newItem.appendTo('#items').slideDown('slow');
		setItemIndex(newItem, ++itemsCount);
	});
		
	$(".removeLink").click(function(event) {
		removeItem(event);
	});
		
	function removeItem(event){
		event.preventDefault();
		var $target = $(event.target);
		$('#item-'+$target.attr("id").split("-")[1]).slideUp('slow', function() { 
			$(this).remove();
			$("#items .item").each(function(index) { 
					setItemIndex($(this), index);
				});
			calcNumberOfItems();
			});
	}
	
	function setItemIndex(item, index){
		item.attr("id","item-"+index);
		$("#item-"+index+" .removeLink").attr("id", "removeLink-"+index);
		$("#removeLink-"+index).click(function(event) {
			removeItem(event);
		});	
		
	  <#switch "${section}">
  		<#case "parties">
			$("#item-"+index+" input").attr("id",function() {
				var parts=$(this).attr("id").split(".");var n=parseInt(parts.length)-1;
				return "eml.associatedParties["+index+"]."+parts[n]; });
			$("#item-"+index+" select").attr("id",function() {
				var parts=$(this).attr("id").split(".");var n=parseInt(parts.length)-1;
				return "eml.associatedParties["+index+"]."+parts[n]; });
			$("#item-"+index+" label").attr("for",function() {
				var parts=$(this).attr("for").split(".");var n=parseInt(parts.length)-1;
				return "eml.associatedParties["+index+"]."+parts[n]; });		
			$("#item-"+index+" input").attr("name",function() {return $(this).attr("id"); });
			$("#item-"+index+" select").attr("name",function() {return $(this).attr("id"); });
    	<#break>
 		<#case "citations">
			$("#item-"+index+" input").attr("id","eml.bibliographicCitationSet.bibliographicCitations["+index+"]");
			$("#item-"+index+" input").attr("name","eml.bibliographicCitationSet.bibliographicCitations["+index+"]");
			$("#item-"+index+" label").attr("for","eml.bibliographicCitationSet.bibliographicCitations["+index+"]");
		<#break>
		<#case "collections">
			$("#item-"+index+" input").attr("id",function() {
				var parts=$(this).attr("id").split(".");var n=parseInt(parts.length)-1;
				return "eml.jgtiCuratorialUnits["+index+"]."+parts[n]; });
			$("#item-"+index+" select").attr("id",function() {
				var parts=$(this).attr("id").split(".");var n=parseInt(parts.length)-1;
				return "eml.jgtiCuratorialUnits["+index+"]."+parts[n]; });
			$("#item-"+index+" label").attr("for",function() {
				var parts=$(this).attr("for").split(".");var n=parseInt(parts.length)-1;
				return "eml.jgtiCuratorialUnits["+index+"]."+parts[n]; });		
			$("#item-"+index+" input").attr("name",function() {return $(this).attr("id"); });
			$("#item-"+index+" select").attr("name",function() {return $(this).attr("id"); });
			$("#item-"+index+" .subitem").attr("id","subitem-"+index);
		<#break>
		<#case "physical">
			$("#item-"+index+" input").attr("id",function() {
				var parts=$(this).attr("id").split(".");var n=parseInt(parts.length)-1;
				return "eml.physicalData["+index+"]."+parts[n]; });
			$("#item-"+index+" select").attr("id",function() {
				var parts=$(this).attr("id").split(".");var n=parseInt(parts.length)-1;
				return "eml.physicalData["+index+"]."+parts[n]; });
			$("#item-"+index+" label").attr("for",function() {
				var parts=$(this).attr("for").split(".");var n=parseInt(parts.length)-1;
				return "eml.physicalData["+index+"]."+parts[n]; });		
			$("#item-"+index+" input").attr("name",function() {return $(this).attr("id"); });
			$("#item-"+index+" select").attr("name",function() {return $(this).attr("id"); });
		<#break>
		<#case "keywords">
			$("#item-"+index+" input").attr("id",function() {
				var parts=$(this).attr("id").split(".");var n=parseInt(parts.length)-1;
				return "eml.keywords["+index+"]."+parts[n]; });
			$("#item-"+index+" textarea").attr("id",function() {
				var parts=$(this).attr("id").split(".");var n=parseInt(parts.length)-1;
				return "eml.keywords["+index+"]."+parts[n]; });	
			$("#item-"+index+" select").attr("id",function() {
				var parts=$(this).attr("id").split(".");var n=parseInt(parts.length)-1;
				return "eml.keywords["+index+"]."+parts[n]; });
			$("#item-"+index+" label").attr("for",function() {
				var parts=$(this).attr("for").split(".");var n=parseInt(parts.length)-1;
				return "eml.keywords["+index+"]."+parts[n]; });		
			$("#item-"+index+" input").attr("name",function() {return $(this).attr("id"); });
			$("#item-"+index+" textarea").attr("name",function() {return $(this).attr("id"); });
			$("#item-"+index+" select").attr("name",function() {return $(this).attr("id"); });
		<#break>
		<#case "taxcoverage">
			$("#item-"+index+" [id$='description']").attr("id", "eml.taxonomicCoverages["+index+"].description").attr("name", function() {return $(this).attr("id");});
			$("#item-"+index+" [for$='description']").attr("for", "eml.taxonomicCoverages["+index+"].description");
			$("#item-"+index+" [id$='scientificName']").attr("id", "eml.taxonomicCoverages["+index+"].taxonKeyword.scientificName").attr("name", function() {return $(this).attr("id");});
			$("#item-"+index+" [for$='scientificName']").attr("for", "eml.taxonomicCoverages["+index+"].taxonKeyword.scientificName");
			$("#item-"+index+" [id$='commonName']").attr("id", "eml.taxonomicCoverages["+index+"].taxonKeyword.commonName").attr("name", function() {return $(this).attr("id");});
			$("#item-"+index+" [for$='commonName']").attr("for", "eml.taxonomicCoverages["+index+"].taxonKeyword.commonName");
			$("#item-"+index+" [id$='rank']").attr("id", "eml.taxonomicCoverages["+index+"].taxonKeyword.rank").attr("name", function() {return $(this).attr("id");});
			$("#item-"+index+" [for$='rank']").attr("for", "eml.taxonomicCoverages["+index+"].taxonKeyword.rank");
		<#break>
		<#default>
  	  </#switch>
		
	}
		
});
</script>
