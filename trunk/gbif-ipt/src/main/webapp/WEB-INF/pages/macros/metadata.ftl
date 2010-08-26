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
		addNewItem(true);
	});
		
	$(".removeLink").click(function(event) {
		removeItem(event);
	});
	
	$("#save").click(function() {
		<#switch "${section}">
  			<#case "methods">
      		if(itemsCount<0){
				addNewItem(false);
				$("#item-0 textarea").attr("value"," ");
			}
			$("#sampling textarea").attr("id",function() {
				var parts=$(this).attr("id").split(".");var n=parseInt(parts.length)-1;
				return "eml.samplingMethods["+(itemsCount+2)+"]."+parts[n]; });
		    $("#qualitycontrol textarea").attr("id",function() {
				var parts=$(this).attr("id").split(".");var n=parseInt(parts.length)-1;
				return "eml.samplingMethods["+(itemsCount+1)+"]."+parts[n]; });
			$("#sampling textarea").attr("name",function() {return $(this).attr("id"); });
			$("#qualitycontrol textarea").attr("name",function() {return $(this).attr("id"); });
			$("#qualitycontrol textarea").each(function() {
				if($(this).attr("value")==""){
					$(this).attr("value"," ");}
			});				
			$("#sampling label").attr("for",function() {
				var parts=$(this).attr("for").split(".");var n=parseInt(parts.length)-1;
				return "eml.samplingMethods["+(itemsCount+2)+"]."+parts[n]; });	
			$("#qualitycontrol label").attr("for",function() {
				var parts=$(this).attr("for").split(".");var n=parseInt(parts.length)-1;
				return "eml.samplingMethods["+(itemsCount+1)+"]."+parts[n]; });
			<#break>
		<#default>
  	  </#switch>	
	  });
	
	function addNewItem(effects){
		var newItem=$('#baseItem').clone();
		if(effects){
			newItem.hide();}
		newItem.appendTo('#items');
		if(effects){
			newItem.slideDown('slow');}
		setItemIndex(newItem, ++itemsCount);
	}
		
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
    	<#case "methods">
			$("#item-"+index+" textarea").attr("id",function() {
				var parts=$(this).attr("id").split(".");var n=parseInt(parts.length)-1;
				return "eml.samplingMethods["+index+"]."+parts[n]; });	
			$("#item-"+index+" label").attr("for",function() {
				var parts=$(this).attr("for").split(".");var n=parseInt(parts.length)-1;
				return "eml.samplingMethods["+index+"]."+parts[n]; });		
			$("#item-"+index+" textarea").attr("name",function() {return $(this).attr("id"); });
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
			$("#item-"+index+" select").attr("id","type-"+index).unbind().change(function() {
				updateSubitem($(this));
			});
			$("#item-"+index+" label").attr("for",function() {
				var parts=$(this).attr("for").split(".");var n=parseInt(parts.length)-1;
				return "eml.jgtiCuratorialUnits["+index+"]."+parts[n]; });
			$("#item-"+index+" input").attr("name",function() {return $(this).attr("id"); });
			$("#item-"+index+" select").attr("name",function() {return $(this).attr("id"); });
			$("#item-"+index+" .subitem").attr("id","subitem-"+index);			
			var selectValue = $("#item-"+index+" #type-"+index).attr("value");
			if(selectValue == "COUNT_RANGE") {
				$("#item-"+index+" [id^='range-']").attr("id", "range-"+index).attr("name", function() {
						$(this).css("display", "");
						return $(this).attr("id"); 
				});
			} else {
				$("#item-"+index+" [id^='uncertainty-']").attr("id", "uncertainty-"+index).attr("name", function() {
						$(this).css("display", "");
						return $(this).attr("id");
				});
			}
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
	
	$("[id^='type-']").change(function() {
		updateSubitem($(this));
	});
	
	function updateSubitem(select) {		
		<#switch "${section}">
  			<#case "collections">
				var selection = select.attr("value");
				var index = select.attr("id").split("-")[1];
				if(selection == "COUNT_RANGE") {
					$("#subitem-"+index+" [id^='uncertainty-']").fadeOut(function() {
						$(this).remove();
						var newItem = $("#range-99999").clone().css("display", "").attr("id", "range-"+index).attr("name", function() {$(this).attr("id")});
						$("#subitem-"+index).append(newItem).hide().fadeIn(function() {
							setItemIndex($("#item-"+index), index);
						});
					});		
				} else {			
					$("#subitem-"+index+" [id^='range-']").fadeOut(function() {
						$(this).remove();
						var newItem = $("#uncertainty-99999").clone().css("display", "").attr("id", "uncertainty-"+index).attr("name", function() {$(this).attr("id")});
						$("#subitem-"+index).append(newItem).hide().fadeIn(function() {
							setItemIndex($("#item-"+index), index);
						});
					});
				}			
    		<#break>
    		<#default>
    	</#switch>
		
	}
		
});
</script>
