<script>
$(document).ready(function(){
    var	itemsCount=-1;
    var personnelItemsCount = -1;
    var collectionItemsCount = -1;
    var specimenPreservationMethodItemsCount = -1;

    calcNumberOfItems();
    calcNumberOfCollectionItems();
    calcNumberOfSpecimenPreservationMethodItems();

    function calcNumberOfItems(){
        var lastItem = $("#items .item:last-child").attr("id");
        if(lastItem != undefined)
            itemsCount=parseInt(lastItem.split("-")[1]);
        else
            itemsCount=-1;
    }

    function calcNumberOfCollectionItems() {
        var lastItem = $("#collection-items .item:last-child").attr("id");
        if (lastItem != undefined)
            collectionItemsCount = parseInt(lastItem.split("-")[2]);
        else
            collectionItemsCount = -1;
    }

    function calcNumberOfSpecimenPreservationMethodItems() {
        var lastItem = $("#specimenPreservationMethod-items .item:last-child").attr("id");
        if (lastItem != undefined)
            specimenPreservationMethodItemsCount = parseInt(lastItem.split("-")[2]);
        else
            specimenPreservationMethodItemsCount = -1;
    }

    $("#plus").click(function(event) {
        event.preventDefault();
        addNewItem(true);
    });

    $("#plus-collection").click(function (event) {
        event.preventDefault();
        addNewCollectionItem(true);
    });

    $("#plus-specimenPreservationMethod").click(function (event) {
        event.preventDefault();
        addNewSpecimenPreservationMethodItem(true);
    });

    $(".removeLink").click(function(event) {
        removeItem(event);
    });

    $(".removeCollectionLink").click(function (event) {
        removeCollectionItem(event);
    });

    $(".removeSpecimenPreservationMethodLink").click(function (event) {
        removeSpecimenPreservationMethodItem(event);
    });
	
	$("[id^=plus-subItem]").click(function(event) {
		addNewSubItem(event);
	});
	
	$("[id^=trash]").click(function(event) {
		removeSubItem(event);			
	});
	
	$(".show-taxonList").click(function(event) {
		showList(event);
	});
	
	$("[id^=add-button]").click(function(event) {
		createTaxons(event);
	});
	
	function addNewSubItem(event, text) {
		event.preventDefault();		
		var baseItem = $("#item-"+$(event.target).attr("id").split("-")[2]);
		// calculating the last taxon index.
		var idBaseItem = baseItem.attr("id");		
		var lastIndex = $("#"+idBaseItem+" .sub-item:last-child").attr("id");
		if(lastIndex == undefined) {
			lastIndex = 0;
		} else {
			lastIndex = parseInt(lastIndex.split("-")[1])+1;
		}			
		// cloning the taxonItem and setting the corresponding id.
		var subBaseItem = $("#subItem-9999").clone();
		// setting the ids to the rest of the components of the taxomItem
		$("#"+idBaseItem+" #subItems").append(subBaseItem);						
		// setting the ids to the rest of the components of the taxonItem.
		setSubItemIndex(baseItem, subBaseItem, lastIndex);
		if(text == undefined) {		
			subBaseItem.slideDown('slow');
		} else {
			$("#"+baseItem.attr("id")+" #"+subBaseItem.attr("id")).find("[id$='scientificName']").val(text);
			subBaseItem.show();
		}
	}
		
	function setSubItemIndex(baseItem, subItem, subBaseIndex) {
		<#switch "${section}">
  			<#case "taxcoverage">
				subItem.attr("id", "subItem-"+subBaseIndex);
				$("#"+baseItem.attr("id")+" #"+subItem.attr("id")).find("[id$='scientificName']").attr("id", "eml.taxonomicCoverages["+baseItem.attr("id").split("-")[1]+"].taxonKeywords["+subBaseIndex+"].scientificName").attr("name", function() {return $(this).attr("id");});
				$("#"+baseItem.attr("id")+" #"+subItem.attr("id")).find("[for$='scientificName']").attr("for", "eml.taxonomicCoverages["+baseItem.attr("id").split("-")[1]+"].taxonKeywords["+subBaseIndex+"].scientificName");
				$("#"+baseItem.attr("id")+" #"+subItem.attr("id")).find("[id$='commonName']").attr("id", "eml.taxonomicCoverages["+baseItem.attr("id").split("-")[1]+"].taxonKeywords["+subBaseIndex+"].commonName").attr("name", function() {return $(this).attr("id");});
				$("#"+baseItem.attr("id")+" #"+subItem.attr("id")).find("[for$='commonName']").attr("for", "eml.taxonomicCoverages["+baseItem.attr("id").split("-")[1]+"].taxonKeywords["+subBaseIndex+"].commonName");
				$("#"+baseItem.attr("id")+" #"+subItem.attr("id")).find("[id$='rank']").attr("id", "eml.taxonomicCoverages["+baseItem.attr("id").split("-")[1]+"].taxonKeywords["+subBaseIndex+"].rank").attr("name", function() {return $(this).attr("id");});
				$("#"+baseItem.attr("id")+" #"+subItem.attr("id")).find("[for$='rank']").attr("for", "eml.taxonomicCoverages["+baseItem.attr("id").split("-")[1]+"].taxonKeywords["+subBaseIndex+"].rank");
				$("#"+baseItem.attr("id")+" #"+subItem.attr("id")).find("[id^='trash']").attr("id", "trash-"+baseItem.attr("id").split("-")[1]+"-"+subBaseIndex).attr("name", function() {return $(this).attr("id");});
				$("#trash-"+baseItem.attr("id").split("-")[1]+"-"+subBaseIndex).click(function(event) {
					removeSubItem(event);
				});
				if(subBaseIndex != 0) {
					$("#trash-"+baseItem.attr("id").split("-")[1]+"-0").show();
				} else {
					$("#trash-"+baseItem.attr("id").split("-")[1]+"-"+subBaseIndex).hide();
				}				
				<#break>
			<#default>
		</#switch>		
		}
		
		function removeSubItem(event) {
			event.preventDefault();
			var $target = $(event.target);
			$("#item-"+$target.attr("id").split("-")[1]+" #subItem-"+$target.attr("id").split("-")[2]).slideUp("fast", function() {
				var indexItem = $(this).find("[id^='trash']").attr("id").split("-")[1];
				$(this).remove();
				$("#item-"+indexItem+" .sub-item").each(function(index) {
					var indexItem = $(this).find("[id^='trash']").attr("id").split("-")[1];					
					setSubItemIndex($("#item-"+indexItem), $(this), index);					
				});
			});
		}

    function addNewItem(effects){
        var newItem=$('#baseItem').clone();
        if(effects) newItem.hide();
        newItem.appendTo('#items');

        if(effects) {
            newItem.slideDown('slow');
        }

        setItemIndex(newItem, ++itemsCount);

        initInfoPopovers(newItem[0]);
    }

    function addNewCollectionItem(effects){
        var newItem=$('#baseItem-collection').clone();
        if(effects) newItem.hide();
        newItem.appendTo('#collection-items');

        if(effects) {
            newItem.slideDown('slow');
        }

        setCollectionItemIndex(newItem, ++collectionItemsCount);

        initInfoPopovers(newItem[0]);
    }

    function addNewSpecimenPreservationMethodItem(effects){
        var newItem=$('#baseItem-specimenPreservationMethod').clone();
        if(effects) newItem.hide();
        newItem.appendTo('#specimenPreservationMethod-items');

        if(effects) {
            newItem.slideDown('slow');
        }

        setSpecimenPreservationMethodItemIndex(newItem, ++specimenPreservationMethodItemsCount);

        initInfoPopovers(newItem[0]);
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

    function removeCollectionItem(event) {
        event.preventDefault();
        var $target = $(event.target);
        $('#collection-item-'+$target.attr("id").split("-")[2]).slideUp('slow', function() {
            $(this).remove();
            $("#collection-items .item").each(function(index) {
                setCollectionItemIndex($(this), index);
            });
            calcNumberOfCollectionItems();
        });
    }

    function removeSpecimenPreservationMethodItem(event) {
        event.preventDefault();
        var $target = $(event.target);
        $('#specimenPreservationMethod-item-'+$target.attr("id").split("-")[2]).slideUp('slow', function() {
            $(this).remove();
            $("#specimenPreservationMethod-items .item").each(function(index) {
                setPreservationMethodItemIndex($(this), index);
            });
            calcNumberOfSpecimenPreservationMethodItems();
        });
    }
	
	function showList(event) {
		event.preventDefault();
		var $target = $(event.target);			
		$("#list-"+$target.attr("id").split("-")[1]).slideDown('slow', function(){
			$("#taxonsLink-"+$target.attr("id").split("-")[1]).hide();
			$target.parent().children("img").hide();
			$target.parent().children("span").hide();
		});
	}
	
	function createTaxons(event) {
		event.preventDefault();
		var $target = $(event.target);
		var index = $target.attr("id").split("-")[2];
		var lines = $("#taxon-list-"+index).val().split("\n");
		var line;
		for(var count in lines) {
			line = $.trim(lines[count]);
			if(line != "undefined" && line != "") {
				addNewSubItem(event, line);	
			}
		}
		$("#taxon-list-"+index).val("");
		$("#list-"+index).slideUp('slow', function() {
			$("#taxonsLink-"+index).show();
			$("#taxonsLink-"+index).parent().children("img").show();
		});
	}
	
	function setItemIndex(item, index){
		item.attr("id","item-"+index);
		$("#item-"+index+" .removeLink").attr("id", "removeLink-"+index);
		$("#removeLink-"+index).click(function(event) {
			removeItem(event);
		});

	  <#switch "${section}">
			<#case "basic">
        $("#item-"+index+" textarea").attr("id",function() {
          return "eml.description["+index+"]"; });
        $("#item-"+index+" textarea").attr("name",function() {
          return $(this).attr("id"); });
			<#break>
    	<#case "methods">
			$("#item-"+index+" textarea").attr("id", "eml.methodSteps["+index+"]");	
			$("#item-"+index+" label").attr("for", "eml.methodSteps["+index+"]");		
			$("#item-"+index+" textarea").attr("name", "eml.methodSteps["+index+"]");	
			if($("#removeLink-0") != null) {
			    $("#removeLink-0").hide();
			}
		<#break>
 		<#case "citations">
			$("#item-"+index+" [id$='citation']").attr("id","eml.bibliographicCitationSet.bibliographicCitations["+index+"].citation");
			$("#item-"+index+" [name$='citation']").attr("name","eml.bibliographicCitationSet.bibliographicCitations["+index+"].citation");
			$("#item-"+index+" [for$='citation']").attr("for","eml.bibliographicCitationSet.bibliographicCitations["+index+"].citation");
			$("#item-"+index+" [id$='identifier']").attr("id","eml.bibliographicCitationSet.bibliographicCitations["+index+"].identifier");
			$("#item-"+index+" [name$='identifier']").attr("name","eml.bibliographicCitationSet.bibliographicCitations["+index+"].identifier");
			$("#item-"+index+" [for$='identifier']").attr("for","eml.bibliographicCitationSet.bibliographicCitations["+index+"].identifier");
		<#break>
		<#case "collections">
			$("#item-"+index+" input").attr("id",function() {
				var parts=$(this).attr("id").split(".");var n=parseInt(parts.length)-1;
				return "eml.jgtiCuratorialUnits["+index+"]."+parts[n];
      });

			$("#item-"+index+" select").attr("id","type-"+index).unbind().change(function() {
				updateSubitem($(this));
			});

			$("#item-"+index+" label").attr("for",function() {
				var parts=$(this).attr("for").split(".");var n=parseInt(parts.length)-1;
				return "eml.jgtiCuratorialUnits["+index+"]."+parts[n];
      });

			$("#item-"+index+" input").attr("name",function() {return $(this).attr("id"); });
			$("#item-"+index+" select").attr("name",function() {return $(this).attr("id"); });
			$("#item-"+index+" .subitem").attr("id","subitem-"+index);

			var selectValue = $("#item-"+index+" #type-"+index).val();
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
		<#case "additional">
			$("#item-"+index+" input").attr("id",function() {
				return "eml.alternateIdentifiers["+index+"]"; });
			$("#item-"+index+" label").attr("for",function() {
				return "eml.alternateIdentifiers["+index+"]"; });		
			$("#item-"+index+" input").attr("name",function() {return $(this).attr("id"); });
		<#break>
		<#case "taxcoverage">			
			$("#item-"+index+" [id^='plus-subItem']").attr("id", "plus-subItem-"+index);
			$("#plus-subItem-"+index).unbind();		
			$("#plus-subItem-"+index).click(function(event) {
				event.preventDefault();
				addNewSubItem(event);
			});
			$("#item-"+index+" #subItems").children(".sub-item").each(function(subindex){				
				setSubItemIndex($("#item-"+index), $(this), subindex);
			});
			$("#item-"+index+" [id$='description']").attr("id", "eml.taxonomicCoverages["+index+"].description").attr("name", function() {return $(this).attr("id");});
			$("#item-"+index+" [for$='description']").attr("for", "eml.taxonomicCoverages["+index+"].description");
			
			$("#item-"+index+" [id^='list']").attr("id", "list-"+index).attr("name", function() {return $(this).attr("id");});
			$("#item-"+index+" [id^='taxon-list']").attr("id", "taxon-list-"+index).attr("name", function() {return $(this).attr("id");});
			$("#item-"+index+" [id^='taxonsLink']").attr("id", "taxonsLink-"+index);
			$("#taxonsLink-"+index).click(function(event){
				 showList(event);
			});	
			$("#item-"+index+" [id^='add-button']").attr("id", "add-button-"+index).attr("name", function() {return $(this).attr("id");});
			$("#add-button-"+index).click(function(event){
				createTaxons(event);
			});
			if($("#item-"+index+" #subItems").children().length === 0) {
				$("#plus-subItem-"+index).click();
			};
		<#break>
		<#default>
  	  </#switch>		
	}

    function setCollectionItemIndex(item, index) {
        item.attr("id","collection-item-"+index);

        $("#collection-item-"+index+" [id^='collection-removeLink']").attr("id", "collection-removeLink-"+index);
        $("#collection-removeLink-"+index).click(function(event) {
            removeCollectionItem(event);
        });

        $("#collection-item-"+index+" [id$='collectionName']").attr("id", "eml.collections["+index+"].collectionName").attr("name", function() {return $(this).attr("id");});
        $("#collection-item-"+index+" [for$='collectionName']").attr("for", "eml.collections["+index+"].collectionName");
        $("#collection-item-"+index+" [id$='collectionId']").attr("id", "eml.collections["+index+"].collectionId").attr("name", function() {return $(this).attr("id");});
        $("#collection-item-"+index+" [for$='collectionId']").attr("for", "eml.collections["+index+"].collectionId");
        $("#collection-item-"+index+" [id$='parentCollectionId']").attr("id", "eml.collections["+index+"].parentCollectionId").attr("name", function() {return $(this).attr("id");});
        $("#collection-item-"+index+" [for$='parentCollectionId']").attr("for", "eml.collections["+index+"].parentCollectionId");
    }

    function setSpecimenPreservationMethodItemIndex(item, index) {
        item.attr("id","specimenPreservationMethod-item-"+index);

        $("#specimenPreservationMethod-item-"+index+" [id^='specimenPreservationMethod-removeLink']").attr("id", "specimenPreservationMethod-removeLink-"+index);
        $("#specimenPreservationMethod-removeLink-"+index).click(function(event) {
            removeSpecimenPreservationMethodItem(event);
        });

        $("#specimenPreservationMethod-item-"+index+" [id$='specimenPreservationMethods']").attr("id", "eml.specimenPreservationMethods["+index+"]").attr("name", function() {return $(this).attr("id");});
        $("#specimenPreservationMethod-item-"+index+" [for$='specimenPreservationMethods']").attr("for", "eml.specimenPreservationMethods["+index+"]");
    }
	
	$("[id^='type-']").change(function() {
		updateSubitem($(this));
	});
	
	function updateSubitem(select) {
		<#switch "${section}">
  			<#case "collections">
				var selection = select.val();
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
