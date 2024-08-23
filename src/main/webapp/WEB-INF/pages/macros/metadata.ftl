<script>
$(document).ready(function(){
    var indexOfLastItem = -1;
    var personnelItemsCount = -1;
    var collectionItemsCount = -1;
    var specimenPreservationMethodItemsCount = -1;

    calcIndexOfLastItem();
    calcNumberOfCollectionItems();
    calcNumberOfSpecimenPreservationMethodItems();

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

    if ($("#inferTaxonomicCoverageAutomatically").is(':checked')) {
        $("[id^=item-]").remove();
        $('.intro').hide();
        $('#items').hide();
        $('.addNew').hide();
        $('#preview-inferred-taxonomic').hide();
        $('#static-taxanomic').show();
        $('#dateInferred').show();
    }

    $("#inferTaxonomicCoverageAutomatically").click(function() {
        if ($("#inferTaxonomicCoverageAutomatically").is(':checked')) {
            $("[id^=item-]").remove();
            $('.intro').hide();
            $('#items').hide();
            $('.addNew').hide();
            $('#preview-inferred-taxonomic').hide();
            $('#static-taxanomic').show();
            $('#dateInferred').show();
        } else {
            $('.intro').show();
            $('#items').show();
            $('.addNew').show();
            $('#preview-inferred-taxonomic').show();
            $('#static-taxanomic').hide();
        }
    });

    $("#preview-inferred-taxonomic").click(function(event) {
        event.preventDefault();

        $("#dateInferred").show();

        <#if (inferredMetadata.inferredTaxonomicCoverage)?? && inferredMetadata.inferredTaxonomicCoverage.errors?size gt 0>
        $(".metadata-error-alert").show();
        </#if>

        <#if (inferredMetadata.inferredTaxonomicCoverage.data.taxonKeywords)??>
            // remove all current items
            $("[id^=item-]").remove();

            var subItemIndex = 0;
            indexOfLastItem = -1;

            addNewItem(true);

            <#list inferredMetadata.inferredTaxonomicCoverage.data.taxonKeywords as taxon>
                <#if !taxon?is_first>
                    addNewSubItemByIndex(0, "");
                </#if>
                $('#eml\\.taxonomicCoverages\\[0\\]\\.taxonKeywords\\[' + subItemIndex + '\\]\\.scientificName').val("${taxon.scientificName}");
                $('#eml\\.taxonomicCoverages\\[0\\]\\.taxonKeywords\\[' + subItemIndex + '\\]\\.rank').val("${taxon.rank}");
                subItemIndex++;
            </#list>
        </#if>
    });

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

    $("#plus-award").click(function (event) {
        event.preventDefault();
        addNewAward(true);
    });

    $("#plus-relatedProject").click(function (event) {
        event.preventDefault();
        addRelatedProject(true);
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

    $(".removeAwardlLink").click(function (event) {
        removeAwardItem(event);
    })

    $(".removeRelatedProjectLink").click(function (event) {
        removeRelatedProjectItem(event);
    });

    $(".removeCollectionLink").click(function (event) {
        removeCollectionItem(event);
    });

    $(".removeSpecimenPreservationMethodLink").click(function (event) {
        removeSpecimenPreservationMethodItem(event);
    });
	
	$("[id^=plus-subItem]").click(function(event) {
		addNewSubItem(event);
        var subItemsIndex = event.currentTarget.id.replace("plus-subItem-", "");
        initializeSortableComponent("subItems-" + subItemsIndex);
	});
	
	$("[id^=trash]").click(function(event) {
		removeSubItem(event);
	});
	
	$(".show-taxonList").click(function(event) {
		showList(event);
	});
	
	$("[id^=add-button]").click(function(event) {
		createTaxons(event);
        var subItemsIndex = event.currentTarget.id.replace("add-button-", "");
        initializeSortableComponent("subItems-" + subItemsIndex)
	});

    function addNewSubItem(event, text) {
        event.preventDefault();
        var $target = $(event.target);
        if (!$target.is('a')) {
            $target = $(event.target).closest('a');
        }
        var targetId = $target.attr("id")
        if (!targetId) {
            targetId = $target.prevObject.attr("id");
        }
        addNewSubItemByIndex(targetId.split("-")[2], text);
    }

    function addNewSubItemByIndex(itemIndex, text) {
        var baseItem = $("#item-" + itemIndex);
        // calculating the last taxon index.
        var idBaseItem = baseItem.attr("id");
        var lastIndex = $("#" + idBaseItem + " .sub-item:last-child").attr("id");
        if (lastIndex === undefined) {
            lastIndex = 0;
        } else {
            var splitId = lastIndex.split("-");
            // one or two indexes: subItem-1 or subItem-1-1
            if (splitId.length === 2) {
                lastIndex = parseInt(lastIndex.split("-")[1]) + 1;
            } else {
                lastIndex = parseInt(lastIndex.split("-")[2]) + 1;
            }
        }
        // cloning the taxonItem and setting the corresponding id.
        var subBaseItem = $("#subItem-9999").clone();
        // setting the ids to the rest of the components of the taxomItem
        $("#" + idBaseItem + " #subItems-" + itemIndex).append(subBaseItem);
        // setting the ids to the rest of the components of the taxonItem.
        setSubItemIndex(baseItem, subBaseItem, lastIndex);
        if (text === undefined) {
            subBaseItem.slideDown('slow');
        } else {
            $("#" + baseItem.attr("id") + " #" + subBaseItem.attr("id")).find("[id$='scientificName']").val(text);
            subBaseItem.show();
        }
    }

	function setSubItemIndex(baseItem, subItem, subBaseIndex) {
		<#switch "${section}">
  			<#case "taxcoverage">
                var itemIndex = baseItem[0].id.split("-")[1];
                subItem.attr("id", "subItem-" + itemIndex + "-" + subBaseIndex);
                $("#" + baseItem.attr("id") + " #" + subItem.attr("id")).find("[id$='scientificName']").attr("id", "eml.taxonomicCoverages[" + baseItem.attr("id").split("-")[1] + "].taxonKeywords[" + subBaseIndex + "].scientificName").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#" + baseItem.attr("id") + " #" + subItem.attr("id")).find("[for$='scientificName']").attr("for", "eml.taxonomicCoverages[" + baseItem.attr("id").split("-")[1] + "].taxonKeywords[" + subBaseIndex + "].scientificName");
                $("#" + baseItem.attr("id") + " #" + subItem.attr("id")).find("[id$='commonName']").attr("id", "eml.taxonomicCoverages[" + baseItem.attr("id").split("-")[1] + "].taxonKeywords[" + subBaseIndex + "].commonName").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#" + baseItem.attr("id") + " #" + subItem.attr("id")).find("[for$='commonName']").attr("for", "eml.taxonomicCoverages[" + baseItem.attr("id").split("-")[1] + "].taxonKeywords[" + subBaseIndex + "].commonName");
                $("#" + baseItem.attr("id") + " #" + subItem.attr("id")).find("[id$='rank']").attr("id", "eml.taxonomicCoverages[" + baseItem.attr("id").split("-")[1] + "].taxonKeywords[" + subBaseIndex + "].rank").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#" + baseItem.attr("id") + " #" + subItem.attr("id")).find("[for$='rank']").attr("for", "eml.taxonomicCoverages[" + baseItem.attr("id").split("-")[1] + "].taxonKeywords[" + subBaseIndex + "].rank");
                $("#" + baseItem.attr("id") + " #" + subItem.attr("id")).find("[id^='trash']").attr("id", "trash-" + baseItem.attr("id").split("-")[1] + "-" + subBaseIndex).attr("name", function () {
                    return $(this).attr("id");
                });
                $("#eml\\.taxonomicCoverages\\[" + itemIndex + "\\]\\.taxonKeywords\\[" + subBaseIndex + "\\]\\.rank").select2({
                    placeholder: '${action.getText("eml.rank.selection")?js_string}',
                    language: {
                        noResults: function () {
                            return '${selectNoResultsFound}';
                        }
                    },
                    width: "100%",
                    allowClear: true,
                    theme: 'bootstrap4'
                });
                $("#trash-" + baseItem.attr("id").split("-")[1] + "-" + subBaseIndex).click(function (event) {
                    removeSubItem(event);
                });
                if (subBaseIndex !== 0) {
                    $("#trash-" + baseItem.attr("id").split("-")[1] + "-0").show();
                } else {
                    $("#trash-" + baseItem.attr("id").split("-")[1] + "-" + subBaseIndex).hide();
                }
				<#break>
			<#default>
		</#switch>		
		}

    function removeSubItem(event) {
        event.preventDefault();
        var $target = $(event.target);
        if (!$target.is('a')) {
            $target = $(event.target).closest('a');
        }
        var itemIndex = $target.attr("id").split("-")[1];
        var subItemIndex = $target.attr("id").split("-")[2];
        $("#item-" + itemIndex + " #subItem-" + itemIndex + "-" + subItemIndex).slideUp("fast", function () {
            var indexItem = $(this).find("[id^='trash']").attr("id").split("-")[1];
            $(this).remove();
            $("#item-" + indexItem + " .sub-item").each(function (index) {
                var indexItem = $(this).find("[id^='trash']").attr("id").split("-")[1];
                setSubItemIndex($("#item-" + indexItem), $(this), index);
            });
        });
    }

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

    function addNewAward(effects) {
        var lastItem = $("#award-items .item:last-child").attr("id");
        var lastIndex;
        if (lastItem !== undefined)
            lastIndex = parseInt(lastItem.split("-")[2]);
        else
            lastIndex = -1;

        var newItem = $('#baseItem-award').clone();
        if (effects) newItem.hide();
        newItem.appendTo('#award-items');

        if (effects) {
            newItem.slideDown('slow');
        }

        setAwardIndex(newItem, ++lastIndex);

        initInfoPopovers(newItem[0]);
    }

    function addRelatedProject(effects) {
        var lastItem = $("#relatedProjects-items .item:last-child").attr("id");
        var lastIndex;
        if (lastItem !== undefined)
            lastIndex = parseInt(lastItem.split("-")[2]);
        else
            lastIndex = -1;

        var newItem = $('#baseItem-relatedProject').clone();
        if (effects) newItem.hide();
        newItem.appendTo('#relatedProjects-items');

        if (effects) {
            newItem.slideDown('slow');
        }

        setRelatedProjectIndex(newItem, ++lastIndex);

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

    function removeAwardItem(event) {
        event.preventDefault();
        var $target = $(event.target);
        if (!$target.is('a')) {
            $target = $(event.target).closest('a');
        }
        $('#award-item-' + $target.attr("id").split("-")[2]).slideUp('slow', function () {
            $(this).remove();
            $("#award-items .item").each(function (index) {
                setAwardIndex($(this), index);
            });
        });
    }

    function removeRelatedProjectItem(event) {
        event.preventDefault();
        var $target = $(event.target);
        if (!$target.is('a')) {
            $target = $(event.target).closest('a');
        }
        $('#relatedProject-item-' + $target.attr("id").split("-")[2]).slideUp('slow', function () {
            $(this).remove();
            $("#relatedProjects-items .item").each(function (index) {
                setRelatedProjectIndex($(this), index);
            });
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

    function showList(event) {
        event.preventDefault();
        var $target = $(event.target);
        if (!$target.is('a')) {
            $target = $(event.target).closest('a');
        }
        var targetId = $target.attr("id").split("-")[1];
        $("#list-" + targetId).slideDown('slow', function () {
            $("#taxonsLink-" + targetId).hide();
            $target.parent().children("img").hide();
            $target.parent().children("span").hide();
        });
    }

    function createTaxons(event) {
        event.preventDefault();
        var $target = $(event.target);
        var index = $target.attr("id").split("-")[2];
        var lines = $("#taxon-list-" + index).val().split("\n");
        var line;
        for (var count in lines) {
            line = $.trim(lines[count]);
            if (line !== "undefined" && line !== "") {
                addNewSubItem(event, line);
            }
        }
        $("#taxon-list-" + index).val("");
        $("#list-" + index).slideUp('slow', function () {
            $("#taxonsLink-" + index).show();
            $("#taxonsLink-" + index).parent().children("img").show();
        });
    }

    function getTargetLink(event) {
        var $target = $(event.target);
        if (!$target.is('a')) {
            $target = $(event.target).closest('a');
        }
        return $target;
    }

    function getEntityIndexFromRemoveLinkId(id) {
        return id.split("-")[3];
    }

	function setItemIndex(item, index){
		item.attr("id","item-"+index);
        $("#item-" + index + " .removeLink").attr("id", "removeLink-" + index);
        $("#removeLink-" + index).click(function (event) {
            removeItem(event);
        });

	    <#switch "${section}">
			<#case "basic">
                $("#item-" + index + " textarea").attr("id",function() {
                    return "eml.description[" + index + "]";
                });
                $("#item-" + index + " textarea").attr("name",function() {
                    return $(this).attr("id");
                });
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
                $("#item-" + index + " .subItems").attr("id", "subItems-" + index);
                $("#item-" + index + " [id^='plus-subItem']").attr("id", "plus-subItem-" + index);
                $("#plus-subItem-" + index).unbind();
                $("#plus-subItem-" + index).click(function (event) {
                    event.preventDefault();
                    addNewSubItem(event);
                    initializeSortableComponent("subItems-" + index);
                });
                $("#item-" + index + " #subItems-" + index).children(".sub-item").each(function (subindex) {
                    setSubItemIndex($("#item-" + index), $(this), subindex);
                });
                $("#item-" + index + " [id$='description']").attr("id", "eml.taxonomicCoverages[" + index + "].description").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#item-" + index + " [for$='description']").attr("for", "eml.taxonomicCoverages[" + index + "].description");

                $("#item-" + index + " [id^='list']").attr("id", "list-" + index).attr("name", function () {
                    return $(this).attr("id");
                });
                $("#item-" + index + " [id^='taxon-list']").attr("id", "taxon-list-" + index).attr("name", function () {
                    return $(this).attr("id");
                });
                $("#item-" + index + " [id^='taxonsLink']").attr("id", "taxonsLink-" + index);
                $("#taxonsLink-" + index).click(function (event) {
                    showList(event);
                });
                $("#item-" + index + " [id^='add-button']").attr("id", "add-button-" + index).attr("name", function () {
                    return $(this).attr("id");
                });
                $("#add-button-" + index).click(function (event) {
                    createTaxons(event);
                    initializeSortableComponent("subItems-" + index)

                    // update taxon names
                    // take real parent index from name (if item was dragged)
                    var parentRealIndex = $("div#item-" + index + " input[id^='add-button']").attr("name").replace("add-button-", "");
                    var items = $("#item-" + index + " div.sub-item");

                    items.each(function (subIndex) {
                        $("div#subItem-" + index + "-" + subIndex + " input[id$='scientificName']").attr("name", "eml.taxonomicCoverages[" + parentRealIndex + "].taxonKeywords[" + subIndex + "].scientificName");
                        $("div#subItem-" + index + "-" + subIndex + " input[id$='commonName']").attr("name", "eml.taxonomicCoverages[" + parentRealIndex + "].taxonKeywords[" + subIndex + "].commonName");
                        $("div#subItem-" + index + "-" + subIndex + " select[id$='rank']").attr("name", "eml.taxonomicCoverages[" + parentRealIndex + "].taxonKeywords[" + subIndex + "].rank");
                    });
                });
                if ($("#item-" + index + " #subItems-" + index).children().length === 0) {
                    $("#plus-subItem-" + index).click();
                }
		    <#break>
		<#default>
  	  </#switch>		
	}

    function setAwardIndex(item, index) {
        item.attr("id", "award-item-" + index);

        $("#award-item-" + index + " [id^='award-removeLink']").attr("id", "award-removeLink-" + index);
        $("#award-removeLink-" + index).click(function (event) {
            removeAwardItem(event);
        });

        // title
        $("#award-item-" + index + " [id$='title']").attr("id", "eml.project.awards[" + index + "].title").attr("name", function () {
            return $(this).attr("id");
        });
        $("#award-item-" + index + " [for$='title']").attr("for", "eml.project.awards[" + index + "].title");
        // funder name
        $("#award-item-" + index + " [id$='funderName']").attr("id", "eml.project.awards[" + index + "].funderName").attr("name", function () {
            return $(this).attr("id");
        });
        $("#award-item-" + index + " [for$='funderName']").attr("for", "eml.project.awards[" + index + "].funderName");
        // award number
        $("#award-item-" + index + " [id$='awardNumber']").attr("id", "eml.project.awards[" + index + "].awardNumber").attr("name", function () {
            return $(this).attr("id");
        });
        $("#award-item-" + index + " [for$='awardNumber']").attr("for", "eml.project.awards[" + index + "].awardNumber");
        // award url
        $("#award-item-" + index + " [id$='awardUrl']").attr("id", "eml.project.awards[" + index + "].awardUrl").attr("name", function () {
            return $(this).attr("id");
        });
        $("#award-item-" + index + " [for$='awardUrl']").attr("for", "eml.project.awards[" + index + "].awardUrl");
        // funder identifier
        $("#award-item-" + index + " [id$='funderIdentifiers[0]']").attr("id", "eml.project.awards[" + index + "].funderIdentifiers[0]").attr("name", function () {
            return $(this).attr("id");
        });
        $("#award-item-" + index + " [for$='funderIdentifiers[0]']").attr("for", "eml.project.awards[" + index + "].funderIdentifiers[0]");
    }

    function setRelatedProjectIndex(item, index) {
        item.attr("id", "relatedProject-item-" + index);

        $("#relatedProject-item-" + index + " [id^='relatedProject-removeLink']").attr("id", "relatedProject-removeLink-" + index);
        $("#relatedProject-removeLink-" + index).click(function (event) {
            removeRelatedProjectItem(event);
        });

        // title
        $("#relatedProject-item-" + index + " [id$='title']").attr("id", "eml.project.relatedProjects[" + index + "].title").attr("name", function () {
            return $(this).attr("id");
        });
        $("#relatedProject-item-" + index + " [for$='title']").attr("for", "eml.project.relatedProjects[" + index + "].title");
        // identifier
        $("#relatedProject-item-" + index + " [id$='identifier']").attr("id", "eml.project.relatedProjects[" + index + "].identifier").attr("name", function () {
            return $(this).attr("id");
        });
        $("#relatedProject-item-" + index + " [for$='identifier']").attr("for", "eml.project.relatedProjects[" + index + "].identifier");
        // description
        $("#relatedProject-item-" + index + " [id$='description']").attr("id", "eml.project.relatedProjects[" + index + "].description").attr("name", function () {
            return $(this).attr("id");
        });
        $("#relatedProject-item-" + index + " [for$='description']").attr("for", "eml.project.relatedProjects[" + index + "].description");
        // personnel
        $("#relatedProject-item-" + index + " #relatedProject-personnel").attr("id", "relatedProject-" + index + "-personnel");
        $("#relatedProject-item-" + index + " #plus-relatedProject-personnel").attr("id", "plus-relatedProject-personnel-" + index);
        $("#plus-relatedProject-personnel-" + index).click(function (event) {
            event.preventDefault();
            createNewPersonnelForRelatedProject(event);
        });

        $("#relatedProject-item-" + index + " [id^='relatedProject-'][id$='-personnel'] .relatedProject-personnel-item").each(function (i) {
            var updatedPersonnelItemId = "relatedProject-" + index + "-personnel-" + i;
            $(this).attr("id", updatedPersonnelItemId);

            $("#" + updatedPersonnelItemId + " [id$='firstName']").attr("id", "eml.project.relatedProjects[" + index + "].personnel[" + i + "].firstName").attr("name", function () {
                return $(this).attr("id");
            });
            $("#" + updatedPersonnelItemId + " [id$='lastName']").attr("id", "eml.project.relatedProjects[" + index + "].personnel[" + i + "].lastName").attr("name", function () {
                return $(this).attr("id");
            });
            $("#" + updatedPersonnelItemId + " [id$='salutation']").attr("id", "eml.project.relatedProjects[" + index + "].personnel[" + i + "].salutation").attr("name", function () {
                return $(this).attr("id");
            });
            $("#" + updatedPersonnelItemId + " [id$='directory']").attr("id", "eml.project.relatedProjects[" + index + "].personnel[" + i + "].userIds[0].directory").attr("name", function () {
                return $(this).attr("id");
            });
            $("#" + updatedPersonnelItemId + " [id$='identifier']").attr("id", "eml.project.relatedProjects[" + index + "].personnel[" + i + "].userIds[0].identifier").attr("name", function () {
                return $(this).attr("id");
            });
            $("#" + updatedPersonnelItemId + " [id$='role']").attr("id", "eml.project.relatedProjects[" + index + "].personnel[" + i + "].role").attr("name", function () {
                return $(this).attr("id");
            });
            $("#relatedProject-item-" + index + " [id^='plus-relatedProject-personnel']").attr("id", "plus-relatedProject-personnel-" + index);
            // TODO copy link indexes!
            // $("#plus-relatedProject-personnel-" + index).click(function (event) {
            //     event.preventDefault();
            //     createNewPersonnelForRelatedProject(event);
            // });
        });
        $("#relatedProject-item-" + index + " [id^='relatedProject-'][id$='-personnel']").attr("id", "relatedProject-" + index + "-personnel");
    }

    function createNewPersonnelForRelatedProject(event) {
        var $target = getTargetLink(event);
        var entityIndex = getEntityIndexFromRemoveLinkId($target.attr("id"));

        // set correct indexes, names, ids
        var numberOfSubEntities = $("#relatedProject-" + entityIndex + "-personnel ." + "relatedProject-personnel-item").length;
        var numberOfSubEntitiesInt = parseInt(numberOfSubEntities);
        var subEntityIndex = numberOfSubEntities === 0 ? 0 : numberOfSubEntitiesInt;

        var newItem = $('#baseItem-relatedProject-personnel').clone();
        newItem.hide();
        newItem.appendTo('#relatedProject-' + entityIndex + '-personnel');
        newItem.slideDown('slow');

        newItem.attr("id", "relatedProject-" + entityIndex + "-personnel-" + subEntityIndex);
        var $firstNameInput = newItem.find("#firstName");
        var $lastNameInput = newItem.find("#lastName");
        var $salutationInput = newItem.find("#salutation");
        var $directorySelect = newItem.find("#directory");
        var $identifierInput = newItem.find("#identifier");
        var $roleSelect = newItem.find("#role");
        var $deleteLink = newItem.find("#personnel-removeLink");
        var $copyDropdown = newItem.find("#dropdown-personnel-copy");
        var $copyFromContactLink = newItem.find("#personnel-from-contact");
        var $copyPersonnelLink = newItem.find("#personnel-copy");

        $firstNameInput.attr("id", "eml.project.relatedProjects[" + entityIndex + "].personnel[" + subEntityIndex + "].firstName").attr("name", function () {return $(this).attr("id");});
        $lastNameInput.attr("id", "eml.project.relatedProjects[" + entityIndex + "].personnel[" + subEntityIndex + "].lastName").attr("name", function () {return $(this).attr("id");});
        $salutationInput.attr("id", "eml.project.relatedProjects[" + entityIndex + "].personnel[" + subEntityIndex + "].salutation").attr("name", function () {return $(this).attr("id");});
        $directorySelect.attr("id", "eml.project.relatedProjects[" + entityIndex + "].personnel[" + subEntityIndex + "].userIds[0].directory").attr("name", function () {return $(this).attr("id");});
        $identifierInput.attr("id", "eml.project.relatedProjects[" + entityIndex + "].personnel[" + subEntityIndex + "].userIds[0].identifier").attr("name", function () {return $(this).attr("id");});
        $roleSelect.attr("id", "eml.project.relatedProjects[" + entityIndex + "].personnel[" + subEntityIndex + "].role").attr("name", function () {return $(this).attr("id");});
        $deleteLink.attr("id", "relatedProject-personnel-remove-" + entityIndex + "-" + subEntityIndex);
        $copyDropdown.attr("id", "dropdown-relatedProject-" + entityIndex + "-personnel-copy-" + subEntityIndex);
        $copyFromContactLink.attr("id", "relatedProject-" + entityIndex + "-personnel-from-contact-" + subEntityIndex);
        $copyPersonnelLink.attr("id", "relatedProject-" + entityIndex + "-personnel-copy-" + subEntityIndex);

        $("#relatedProject-personnel-remove-" + entityIndex + "-" + subEntityIndex).click(function (event) {
            removeRelatedProjectPersonnelItem(event);
        });

        $("#relatedProject-" + entityIndex + "-personnel-from-contact-" + subEntityIndex).click(function (event) {
            event.preventDefault();
            copyPrimaryContactDetails(event, getItemId(event, "relatedProject-personnel-item"));
        });

        $("#relatedProject-" + entityIndex + "-personnel-copy-" + subEntityIndex).click(function (event) {
            event.preventDefault();
            targetItemId = this.id.replace('-copy-', '-');
            showCopyAgentModal();
        });
    }

    function copyPrimaryContactDetails(event, itemId) {
        event.preventDefault();

        // replace " with &quot; to prevent JS from failing
        $("#" + itemId + " [id$='firstName']").val("${(primaryContact.firstName)!?replace("\"", "&quot;")}");
        $("#" + itemId + " [id$='lastName']").val("${(primaryContact.lastName)!?replace("\"", "&quot;")}");
        $("#" + itemId + " [id$='salutation']").val("${(primaryContact.salutation)!?replace("\"", "&quot;")}");
        $("#" + itemId + " [id$='position']").val("${(primaryContact.position)!?replace("\"", "&quot;")}");
        $("#" + itemId + " [id$='organisation']").val("${(primaryContact.organisation)!?replace("\"", "&quot;")}");
        $("#" + itemId + " [id$='address']").val("${(primaryContact.address.address)!?replace("\"", "&quot;")}");
        $("#" + itemId + " [id$='city']").val("${(primaryContact.address.city)!?replace("\"", "&quot;")}");
        $("#" + itemId + " [id$='province']").val("${(primaryContact.address.province)!?replace("\"", "&quot;")}");
        $("#" + itemId + " [id$='postalCode']").val("${(primaryContact.address.postalCode)!?replace("\"", "&quot;")}");
        $("#" + itemId + " [id$='country']").val("${(primaryContact.address.country)!?replace("\"", "&quot;")}");
        $("#" + itemId + " [id$='phone']").val("${(primaryContact.phone)!?replace("\"", "&quot;")}");
        $("#" + itemId + " [id$='email']").val("${(primaryContact.email)!?replace("\"", "&quot;")}");
        $("#" + itemId + " [id$='homepage']").val("${(primaryContact.homepage)!?replace("\"", "&quot;")}");
        $("#" + itemId + " [id$='directory']").val("${(primaryContact.userIds[0].directory)!?replace("\"", "&quot;")}");
        $("#" + itemId + " [id$='directory']").trigger('change');
        $("#" + itemId + " [id$='identifier']").val("${(primaryContact.userIds[0].identifier)!?replace("\"", "&quot;")}");
    }

    function removeRelatedProjectPersonnelItem(event) {
        event.preventDefault();
        var $target = $(event.target);
        if (!$target.is('a')) {
            $target = $(event.target).closest('a');
        }
        var relatedProjectId = $target.attr("id").split("-")[3];
        var personnelId = $target.attr("id").split("-")[4];

        $('#relatedProject-' + relatedProjectId + '-personnel-' + personnelId).slideUp('slow', function () {
            $(this).remove();
            $("#relatedProject-" + relatedProjectId + "-personnel .relatedProject-personnel-item").each(function (index) {
                setRelatedProjectPersonnelItemIndex($(this), relatedProjectId, index);
            });
        });
    }

    function showCopyAgentModal() {
        var dialogWindow = $("#copy-agent-modal");
        dialogWindow.modal('show');
    }

    function getItemId(event, itemClass) {
        event.preventDefault();
        var $target = $(event.target);
        if (!$target.is('a')) {
            $target = $(event.target).closest('a');
        }

        var linkId = $target.attr("id");
        var $parentItem = $('#' + linkId).closest('.' + itemClass);
        return $parentItem.attr("id");
    }

    function setRelatedProjectPersonnelItemIndex(item, relatedProjectId, index) {
        var itemId = "relatedProject-" + relatedProjectId + "-personnel-" + index;
        item.attr("id", itemId);

        $("#" + itemId + " [id^='relatedProject-personnel-remove']").attr("id", "relatedProject-personnel-remove-" + relatedProjectId + "-" + index);
        $("#relatedProject-personnel-remove-" + relatedProjectId + "-" + index).click(function (event) {
            removeRelatedProjectPersonnelItem(event);
        });

        $("#" + itemId + " [id^='dropdown-relatedProject-" + relatedProjectId + "-personnel-copy']").attr("id", "dropdown-relatedProject-" + relatedProjectId + "-personnel-copy-" + index);
        $("#" + itemId + " [id^='relatedProject-" + relatedProjectId + "-personnel-from-contact']").attr("id", "relatedProject-" + relatedProjectId + "-personnel-from-contact-" + index);
        $("#" + itemId + " [id^='relatedProject-" + relatedProjectId + "-personnel-copy']").attr("id", "relatedProject-" + relatedProjectId + "-personnel-copy-" + index);
        $("#relatedProject-" + relatedProjectId + "-personnel-copy-" + index).click(function (event) {
            event.preventDefault();
            targetItemId = itemId;
            showCopyAgentModal();
        });
        $("#personnel-" + relatedProjectId + "-personnel-from-contact-" + index).click(function (event) {
            event.preventDefault();
            copyPrimaryContactDetails(event, itemId);
        });

        $("#" + itemId + " [id$='firstName']").attr("id", "eml.project.relatedProjects[" + relatedProjectId + "].personnel[" + index + "].firstName").attr("name", function () {return $(this).attr("id");});
        $("#" + itemId + " [for$='firstName']").attr("for", "eml.project.relatedProjects[" + relatedProjectId + "].personnel[" + index + "].firstName");
        $("#" + itemId + " [id$='lastName']").attr("id", "eml.project.relatedProjects[" + relatedProjectId + "].personnel[" + index + "].lastName").attr("name", function () {return $(this).attr("id");});
        $("#" + itemId + " [for$='lastName']").attr("for", "eml.project.relatedProjects[" + relatedProjectId + "].personnel[" + index + "].lastName");
        $("#" + itemId + " [id$='salutation']").attr("id", "eml.project.relatedProjects[" + relatedProjectId + "].personnel[" + index + "].salutation").attr("name", function () {return $(this).attr("id");});
        $("#" + itemId + " [for$='salutation']").attr("for", "eml.project.relatedProjects[" + relatedProjectId + "].personnel[" + index + "].salutation");
        $("#" + itemId + " [id$='role']").attr("id", "eml.project.relatedProjects[" + relatedProjectId + "].personnel[" + index + "].role").attr("name", function () {return $(this).attr("id");});
        $("#" + itemId + " [for$='role']").attr("for", "eml.project.relatedProjects[" + relatedProjectId + "].personnel[" + index + "].role");
        $("#" + itemId + " [id$='role']").select2({
            placeholder: '${action.getText("eml.agent.role.selection")?js_string}',
            language: {
                noResults: function () {
                    return '${selectNoResultsFound}';
                }
            },
            width: "100%",
            minimumResultsForSearch: 'Infinity',
            allowClear: true,
            theme: 'bootstrap4'
        });
        $("#" + itemId + " [id$='directory']").attr("id", "eml.project.relatedProjects[" + relatedProjectId + "].personnel[" + index + "].userIds[0].directory").attr("name", function () {return $(this).attr("id");});
        $("#" + itemId + " [for$='directory']").attr("for", "eml.project.relatedProjects[" + relatedProjectId + "].personnel[" + index + "].userIds[0].directory");
        $("#" + itemId + " [id$='directory']").select2({
            placeholder: '${action.getText("eml.contact.noDirectory")?js_string}',
            language: {
                noResults: function () {
                    return '${selectNoResultsFound}';
                }
            },
            width: "100%",
            minimumResultsForSearch: 'Infinity',
            allowClear: true,
            theme: 'bootstrap4'
        });
        $("#" + itemId + " [id$='identifier']").attr("id", "eml.project.relatedProjects[" + relatedProjectId + "].personnel[" + index + "].userIds[0].identifier").attr("name", function () {return $(this).attr("id");});
        $("#" + itemId + " [for$='identifier']").attr("for", "eml.project.relatedProjects[" + relatedProjectId + "].personnel[" + index + "].userIds[0].identifier");
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
