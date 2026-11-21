<script>
$(document).ready(function(){
    var indexOfLastItem = -1;
    var collectionItemsCount = -1;
    var specimenPreservationMethodItemsCount = -1;
    // other data formats on the citations page (#3319)
    var physicalDataItemsCount = -1;
    // alternative identifiers on the citations page (#3319)
    var altIdItemsCount = -1;

    calcIndexOfLastItem();
    calcNumberOfCollectionItems();
    calcNumberOfSpecimenPreservationMethodItems();
    calcNumberOfPhysicalDataItems();
    calcNumberOfAltIdItems();

    function calcIndexOfLastItem() {
        var lastItem = $("#items .item:last-child").attr("id");
        if (lastItem !== undefined)
            indexOfLastItem = parseInt(lastItem.split("-")[1]);
        else
            indexOfLastItem = -1;
    }

    function calcNumberOfCollectionItems() {
        var lastItem = $("#collection-items .item:last-child").attr("id");
        if (lastItem !== undefined)
            collectionItemsCount = parseInt(lastItem.split("-")[2]);
        else
            collectionItemsCount = -1;
    }

    function calcNumberOfSpecimenPreservationMethodItems() {
        var lastItem = $("#specimenPreservationMethod-items .item:last-child").attr("id");
        if (lastItem !== undefined)
            specimenPreservationMethodItemsCount = parseInt(lastItem.split("-")[2]);
        else
            specimenPreservationMethodItemsCount = -1;
    }

    function calcNumberOfAltIdItems() {
        var lastItem = $("#altId-items .item:last-child").attr("id");
        if (lastItem !== undefined)
            altIdItemsCount = parseInt(lastItem.split("-")[2]);
        else
            altIdItemsCount = -1;
    }

    function calcNumberOfPhysicalDataItems() {
        var lastItem = $("#physicalData-items .item:last-child").attr("id");
        if (lastItem !== undefined)
            physicalDataItemsCount = parseInt(lastItem.split("-")[2]);
        else
            physicalDataItemsCount = -1;
    }

    function initializeSortableComponent(componentId) {
        sortable('#' + componentId, {
            forcePlaceholderSize: true,
            placeholderClass: 'border',
            exclude: 'input'
        });
    }

    $("#plus").click(function(event) {
        event.preventDefault();
        addNewItem(true);
        initializeSortableComponent("items");
    });

    $("#plus-collection").click(function (event) {
        event.preventDefault();
        addNewCollectionItem(true);
        initializeSortableComponent("collection-items");
    });

    $("#plus-specimenPreservationMethod").click(function (event) {
        event.preventDefault();
        addNewSpecimenPreservationMethodItem(true);
        initializeSortableComponent("specimenPreservationMethod-items");
    });

    $(".removeLink").click(function(event) {
        removeItem(event);
    });

    $("#plus-physicalData").click(function (event) {
        event.preventDefault();
        addNewPhysicalDataItem(true);
    });

    $(".removePhysicalDataLink").click(function (event) {
        removePhysicalDataItem(event);
    });

    $("#plus-altId").click(function (event) {
        event.preventDefault();
        addNewAltIdItem(true);
    });

    $(".removeAltIdLink").click(function (event) {
        removeAltIdItem(event);
    });

    $(".removeCollectionLink").click(function (event) {
        removeCollectionItem(event);
    });

    $(".removeSpecimenPreservationMethodLink").click(function (event) {
        removeSpecimenPreservationMethodItem(event);
    });
	
    function addNewItem(effects) {
        calcIndexOfLastItem();
        var newItem = $('#baseItem').clone();
        if (effects) newItem.hide();
        newItem.appendTo('#items');

        if (effects) {
            newItem.slideDown('slow');
        }

        setItemIndex(newItem, ++indexOfLastItem);

        initInfoPopovers(newItem[0]);
    }

    function addNewCollectionItem(effects) {
        var newItem = $('#baseItem-collection').clone();
        if (effects) newItem.hide();
        newItem.appendTo('#collection-items');

        if (effects) {
            newItem.slideDown('slow');
        }

        setCollectionItemIndex(newItem, ++collectionItemsCount);

        initInfoPopovers(newItem[0]);
    }

    function addNewSpecimenPreservationMethodItem(effects) {
        var newItem = $('#baseItem-specimenPreservationMethod').clone();
        if (effects) newItem.hide();
        newItem.appendTo('#specimenPreservationMethod-items');

        if (effects) {
            newItem.slideDown('slow');
        }

        setSpecimenPreservationMethodItemIndex(newItem, ++specimenPreservationMethodItemsCount);

        initInfoPopovers(newItem[0]);
    }

    function removeItem(event) {
        event.preventDefault();
        var $target = $(event.target);
        if (!$target.is('a')) {
            $target = $(event.target).closest('a');
        }
        $('#item-' + $target.attr("id").split("-")[1]).slideUp('slow', function () {
            $(this).remove();
            $("#items .item").each(function (index) {
                setItemIndex($(this), index);
            });
            calcIndexOfLastItem();
        });
    }

    function addNewPhysicalDataItem(effects) {
        var newItem = $('#baseItem-physicalData').clone();
        if (effects) newItem.hide();
        newItem.appendTo('#physicalData-items');

        if (effects) {
            newItem.slideDown('slow');
        }

        setPhysicalDataItemIndex(newItem, ++physicalDataItemsCount);

        initInfoPopovers(newItem[0]);
    }

    function removePhysicalDataItem(event) {
        event.preventDefault();
        var $target = $(event.target);
        if (!$target.is('a')) {
            $target = $(event.target).closest('a');
        }
        $('#physicalData-item-' + $target.attr("id").split("-")[2]).slideUp('slow', function () {
            $(this).remove();
            $("#physicalData-items .item").each(function (index) {
                setPhysicalDataItemIndex($(this), index);
            });
            calcNumberOfPhysicalDataItems();
        });
    }

    // fields are named eml.physicalData[index].<field>; the template's are named <field>
    function setPhysicalDataItemIndex(item, index) {
        item.attr("id", "physicalData-item-" + index);
        item.find("[id^='physicalData-removeLink']").attr("id", "physicalData-removeLink-" + index)
            .off("click").click(function (event) {
                removePhysicalDataItem(event);
            });
        var field = function (value) {
            var parts = value.split(".");
            return "eml.physicalData[" + index + "]." + parts[parts.length - 1];
        };
        item.find("input").attr("id", function () { return field($(this).attr("id")); })
            .attr("name", function () { return $(this).attr("id"); });
        item.find("label[for]").attr("for", function () { return field($(this).attr("for")); });
    }

    function addNewAltIdItem(effects) {
        var newItem = $('#baseItem-altId').clone();
        if (effects) newItem.hide();
        newItem.appendTo('#altId-items');

        if (effects) {
            newItem.slideDown('slow');
        }

        setAltIdItemIndex(newItem, ++altIdItemsCount);

        initInfoPopovers(newItem[0]);
    }

    function removeAltIdItem(event) {
        event.preventDefault();
        var $target = $(event.target);
        if (!$target.is('a')) {
            $target = $(event.target).closest('a');
        }
        $('#altId-item-' + $target.attr("id").split("-")[2]).slideUp('slow', function () {
            $(this).remove();
            $("#altId-items .item").each(function (index) {
                setAltIdItemIndex($(this), index);
            });
            calcNumberOfAltIdItems();
        });
    }

    // fields are named eml.alternateIdentifiers[index]; the template's is named alternateIdentifiers
    function setAltIdItemIndex(item, index) {
        item.attr("id", "altId-item-" + index);
        item.find("[id^='altId-removeLink']").attr("id", "altId-removeLink-" + index)
            .off("click").click(function (event) {
                removeAltIdItem(event);
            });
        item.find("input").attr("id", "eml.alternateIdentifiers[" + index + "]").attr("name", "eml.alternateIdentifiers[" + index + "]");
        item.find("label[for]").attr("for", "eml.alternateIdentifiers[" + index + "]");
    }

    function removeCollectionItem(event) {
        event.preventDefault();
        var $target = $(event.target);
        if (!$target.is('a')) {
            $target = $(event.target).closest('a');
        }
        $('#collection-item-' + $target.attr("id").split("-")[2]).slideUp('slow', function () {
            $(this).remove();
            $("#collection-items .item").each(function (index) {
                setCollectionItemIndex($(this), index);
            });
            calcNumberOfCollectionItems();
        });
    }

    function removeSpecimenPreservationMethodItem(event) {
        event.preventDefault();
        var $target = $(event.target);
        if (!$target.is('a')) {
            $target = $(event.target).closest('a');
        }
        $('#specimenPreservationMethod-item-'+$target.attr("id").split("-")[2]).slideUp('slow', function() {
            $(this).remove();
            $("#specimenPreservationMethod-items .item").each(function(index) {
                setPreservationMethodItemIndex($(this), index);
            });
            calcNumberOfSpecimenPreservationMethodItems();
        });
    }

	function setItemIndex(item, index){
		item.attr("id","item-"+index);
        $("#item-" + index + " .removeLink").attr("id", "removeLink-" + index);
        $("#removeLink-" + index).click(function (event) {
            removeItem(event);
        });

	    <#switch "${section}">
            <#case "citations">
                $("#item-"+index+" [id$='citation']").attr("id","eml.bibliographicCitationSet.bibliographicCitations["+index+"].citation");
                $("#item-"+index+" [name$='citation']").attr("name","eml.bibliographicCitationSet.bibliographicCitations["+index+"].citation");
                $("#item-"+index+" [for$='citation']").attr("for","eml.bibliographicCitationSet.bibliographicCitations["+index+"].citation");
                $("#item-"+index+" [id$='identifier']").attr("id","eml.bibliographicCitationSet.bibliographicCitations["+index+"].identifier");
                $("#item-"+index+" [name$='identifier']").attr("name","eml.bibliographicCitationSet.bibliographicCitations["+index+"].identifier");
                $("#item-"+index+" [for$='identifier']").attr("for","eml.bibliographicCitationSet.bibliographicCitations["+index+"].identifier");
            <#break>

            <#case "collections">
                $("#item-" + index + " input").attr("id", function () {
                    var parts = $(this).attr("id").split(".");
                    var n = parseInt(parts.length) - 1;
                    return "eml.jgtiCuratorialUnits[" + index + "]." + parts[n];
                });

                $("#item-" + index + " select").attr("id", "type-" + index).unbind().change(function () {
                    updateSubitem($(this));
                });

                $("#item-" + index + " label").attr("for", function () {
                    var parts = $(this).attr("for").split(".");
                    var n = parseInt(parts.length) - 1;
                    return "eml.jgtiCuratorialUnits[" + index + "]." + parts[n];
                });

                $("#item-" + index + " input").attr("name", function() { return $(this).attr("id"); });
                $("#item-" + index + " select").attr("name", function() { return $(this).attr("id"); });
                $("#item-" + index + " .subitem").attr("id", "subitem-" + index);
                $("#type-" + index).select2({
                    placeholder: '',
                    language: {
                        noResults: function () {
                            return '${selectNoResultsFound}';
                        }
                    },
                    width: "100%",
                    minimumResultsForSearch: 'Infinity',
                    allowClear: false,
                    theme: 'bootstrap4'
                });

                var selectValue = $("#item-" + index + " #type-" + index).val();
                if (selectValue == "COUNT_RANGE") {
                    $("#item-" + index + " [id^='range-']").attr("id", "range-" + index).attr("name", function () {
                        $(this).css("display", "");
                        return $(this).attr("id");
                    });
                } else {
                    $("#item-" + index + " [id^='uncertainty-']").attr("id", "uncertainty-" + index).attr("name", function () {
                        $(this).css("display", "");
                        return $(this).attr("id");
                    });
                }
            <#break>




		<#default>
  	  </#switch>		
	}

    function setCollectionItemIndex(item, index) {
        item.attr("id", "collection-item-" + index);

        $("#collection-item-" + index + " [id^='collection-removeLink']").attr("id", "collection-removeLink-" + index);
        $("#collection-removeLink-" + index).click(function (event) {
            removeCollectionItem(event);
        });

        $("#collection-item-" + index + " [id$='collectionName']").attr("id", "eml.collections[" + index + "].collectionName").attr("name", function () {
            return $(this).attr("id");
        });
        $("#collection-item-" + index + " [for$='collectionName']").attr("for", "eml.collections[" + index + "].collectionName");
        $("#collection-item-" + index + " [id$='collectionId']").attr("id", "eml.collections[" + index + "].collectionId").attr("name", function () {
            return $(this).attr("id");
        });
        $("#collection-item-" + index + " [for$='collectionId']").attr("for", "eml.collections[" + index + "].collectionId");
        $("#collection-item-" + index + " [id$='parentCollectionId']").attr("id", "eml.collections[" + index + "].parentCollectionId").attr("name", function () {
            return $(this).attr("id");
        });
        $("#collection-item-" + index + " [for$='parentCollectionId']").attr("for", "eml.collections[" + index + "].parentCollectionId");
    }

    function setSpecimenPreservationMethodItemIndex(item, index) {
        item.attr("id", "specimenPreservationMethod-item-" + index);

        $("#specimenPreservationMethod-item-" + index + " [id^='specimenPreservationMethod-removeLink']").attr("id", "specimenPreservationMethod-removeLink-" + index);
        $("#specimenPreservationMethod-removeLink-" + index).click(function (event) {
            removeSpecimenPreservationMethodItem(event);
        });

        $("#specimenPreservationMethod-item-" + index + " [id$='specimenPreservationMethods']").attr("id", "eml.specimenPreservationMethods[" + index + "]").attr("name", function () {
            return $(this).attr("id");
        });
        $("#specimenPreservationMethod-item-" + index + " [for$='specimenPreservationMethods']").attr("for", "eml.specimenPreservationMethods[" + index + "]");

        $("#eml\\.specimenPreservationMethods\\[" + index + "\\]").select2({
            placeholder: '${action.getText("eml.preservation.methods.selection")?js_string}',
            language: {
                noResults: function () {
                    return '${selectNoResultsFound}';
                }
            },
            width: "100%",
            minimumResultsForSearch: 15,
            allowClear: true,
            theme: 'bootstrap4'
        });
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
