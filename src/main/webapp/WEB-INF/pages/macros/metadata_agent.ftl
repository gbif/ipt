<script>
    $(document).ready(function(){

        var contactItems = -1;
        var creatorItems = -1;
        var metadataProviderItems = -1;
        var associatedPartyItemsCount = -1;

        calcNumberOfAgentItems("contact");
        calcNumberOfAgentItems("creator");
        calcNumberOfAgentItems("metadataProvider");
        calcNumberOfAgentItems("associatedParty");

        function calcNumberOfAgentItems(entityName) {
            var lastItem = $("#" + entityName + "-items .item:last-child").attr("id");
            var result = lastItem !== undefined ? parseInt(lastItem.split("-")[2]) : -1;

            if (entityName === "contact") {
                contactItems = result;
            } else if (entityName === "creator") {
                creatorItems = result;
            } else if (entityName === "metadataProvider") {
                metadataProviderItems = result;
            } else if (entityName === "associatedParty") {
                associatedPartyItemsCount = result;
            }
        }

        function initializeSortableComponent(componentId) {
            sortable('#' + componentId, {
                forcePlaceholderSize: true,
                placeholderClass: 'border',
                exclude: 'input'
            });
        }

        $(".plus-agent").click(function (event) {
            event.preventDefault();

            var $target = getTargetLink(event);
            const targetId = $target.attr("id");
            const entityName = targetId.split("-")[1];

            addNewAgentItem(entityName, true);
            initializeSortableComponent(entityName + "-items");
        })

        $("#plus-personnel").click(function (event) {
            event.preventDefault();
            addNewPersonnelItem(true);
            initializeSortableComponent("personnel-items");
        });

        $(".add-relatedProject-personnel").click(function (event) {
            event.preventDefault();
            createNewPersonnelForRelatedProject(event);
        });

        $(".removeAgentLink").click(function (event) {
            removeAgentItem(event);
        });

        $(".removePersonnelLink").click(function (event) {
            removePersonnelItem(event);
        });

        $(".removeRelatedProjectPersonnelLink").click(function (event) {
            removeRelatedProjectPersonnelItem(event);
        });

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

        $(".add-agent-contact-info").click(function (event) {
            event.preventDefault();
            createNewSubEntityForAgent(event);
        });

        $(".add-identifier").click(function (event) {
            event.preventDefault();
            createNewIdentifierForAgent(event);
        });

        // remove phone/email/homepage/position/address from contact/creator/metadataProvider/associatedParty
        $(".removeSubEntity").click(function (event) {
            event.preventDefault();
            removeSubEntityFromAgent(event);
        });

        // remove identifier from contact/creator/metadataProvider
        $(".removeIdentifier").click(function (event) {
            event.preventDefault();
            removeIdentifierFromAgent(event);
        });

        function createNewSubEntityForAgent(event) {
            var $target = getTargetLink(event);
            var id = $target.attr("id");
            var entityName = getEntityNameFromAddNewLinkId(id);
            var subEntityName = getSubEntityNameFromAddNewLinkId(id);
            var entityIndex = getEntityIndexFromRemoveLinkId($target.attr("id"));
            var newItem = $('#baseItem-' + subEntityName).clone();
            newItem.hide();

            var subEntityNamePlural = subEntityName === 'address' ? 'address' : subEntityName + 's';

            newItem.appendTo('#' + entityName + '-' + entityIndex + '-' + subEntityNamePlural);
            newItem.slideDown('slow');

            // set correct indexes, names, ids
            var numberOfSubEntities = $("#" + entityName + "-" + entityIndex + "-" + subEntityNamePlural + " ." + subEntityName + "-item").length;
            var numberOfSubEntitiesInt = parseInt(numberOfSubEntities);
            var subEntityIndex = numberOfSubEntities === 0 ? 0 : numberOfSubEntitiesInt - 1;

            newItem.attr("id", entityName + "-" + entityIndex + "-" + subEntityName + "-" + subEntityIndex);
            var $input = newItem.find("#baseItem-" + subEntityName + "-input");
            var $deleteLink = newItem.find("#baseItem-" + subEntityName + "-remove");

            var entityNameInId = entityName === 'associatedParty' ? 'associatedPartie' : entityName;
            var subEntityNameInId = subEntityName === 'address' ? 'address.address' : subEntityName;

            $input.attr("id", "eml." + entityNameInId + "s[" + entityIndex + "]." + subEntityNameInId + "[" + subEntityIndex + "]").attr("name", function () {return $(this).attr("id");});

            $deleteLink.attr("id", entityName + "-" + subEntityName + "-remove-" + entityIndex + "-" + subEntityIndex);
            $("#" + entityName + "-" + subEntityName + "-remove-" + entityIndex + "-" + subEntityIndex).click(function (event) {
                removeSubEntityFromAgent(event, entityName, subEntityName);
            });
        }

        function createNewSubEntityForAgentDirectly(entityName, subEntityName, entityIndex, value) {
            var newItem = $('#baseItem-' + subEntityName).clone();
            newItem.hide();
            newItem.appendTo('#' + entityName + '-' + entityIndex + '-' + subEntityName + 's');
            newItem.slideDown('slow');

            // set correct indexes, names, ids
            var numberOfSubEntities = $("#" + entityName + "-" + entityIndex + "-" + subEntityName + "s ." + subEntityName + "-item").length;
            var numberOfSubEntitiesInt = parseInt(numberOfSubEntities);
            var subEntityIndex = numberOfSubEntities === 0 ? 0 : numberOfSubEntitiesInt - 1;

            newItem.attr("id", entityName + "-" + entityIndex + "-" + subEntityName + "-" + subEntityIndex);
            var $input = newItem.find("#baseItem-" + subEntityName + "-input");
            var $deleteLink = newItem.find("#baseItem-" + subEntityName + "-remove");

            var entityNameInId = entityName === 'associatedParty' ? 'associatedPartie' : entityName;
            var subEntityNameInId = subEntityName === 'address' ? 'address.address' : subEntityName;

            $input.attr("id", "eml." + entityNameInId + "s[" + entityIndex + "]." + subEntityNameInId + "[" + subEntityIndex + "]").attr("name", function () {return $(this).attr("id");});

            $deleteLink.attr("id", entityName + "-" + subEntityName + "-remove-" + entityIndex + "-" + subEntityIndex);
            $("#" + entityName + "-" + subEntityName + "-remove-" + entityIndex + "-" + subEntityIndex).click(function (event) {
                removeSubEntityFromAgent(event, entityName, subEntityName);
            });

            if (value) {
                $input.val(value)
            }
        }

        function createNewIdentifierForAgent(event) {
            var $target = getTargetLink(event);
            var id = $target.attr("id");
            var entityName = getEntityNameFromAddNewLinkId(id);
            var entityIndex = getEntityIndexFromRemoveLinkId($target.attr("id"));

            var newItem = $('#baseItem-identifier').clone();
            newItem.hide();
            newItem.appendTo('#' + entityName + '-' + entityIndex + '-identifiers');
            newItem.slideDown('slow');

            // set correct indexes, names, ids
            var numberOfSubIdentifiers = $("#" + entityName + "-" + entityIndex + "-identifiers .identifier-item").length;
            var identifierIndex = parseInt(numberOfSubIdentifiers) - 1;

            newItem.attr("id", entityName + "-" + entityIndex + "-identifier-" + identifierIndex);
            var $select = newItem.find("#baseItem-directory-select")
            var $input = newItem.find("#baseItem-identifier-input");
            var $deleteLink = newItem.find("#baseItem-identifier-remove");

            $select.select2({
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

            var entityNameInId = entityName === 'associatedParty' ? 'associatedPartie' : entityName;

            $input.attr("id", "eml." + entityNameInId + "s[" + entityIndex + "].userIds[" + identifierIndex + "].identifier").attr("name", function () {return $(this).attr("id");});
            $select.attr("id", "eml." + entityNameInId + "s[" + entityIndex + "].userIds[" + identifierIndex + "].directory").attr("name", function () {return $(this).attr("id");});
            $deleteLink.attr("id", entityName + "-identifier-remove-" + entityIndex + "-" + identifierIndex);
            $("#" + entityName + "-identifier-remove-" + entityIndex + "-" + identifierIndex).click(function (event) {
                removeIdentifierFromAgent(event);
            });
        }

        function createNewIdentifierForAgentDirectly(entityName, entityIndex, directoryValue, identifierValue) {
            var newItem = $('#baseItem-identifier').clone();
            newItem.hide();
            newItem.appendTo('#' + entityName + '-' + entityIndex + '-identifiers');
            newItem.slideDown('slow');

            // set correct indexes, names, ids
            var numberOfSubIdentifiers = $("#" + entityName + "-" + entityIndex + "-identifiers .identifier-item").length;
            var identifierIndex = parseInt(numberOfSubIdentifiers) - 1;

            newItem.attr("id", entityName + "-" + entityIndex + "-identifier-" + identifierIndex);
            var $select = newItem.find("#baseItem-directory-select")
            var $input = newItem.find("#baseItem-identifier-input");
            var $deleteLink = newItem.find("#baseItem-identifier-remove");

            var entityNameInId = entityName === 'associatedParty' ? 'associatedPartie' : entityName;

            $input.attr("id", "eml." + entityNameInId + "s[" + entityIndex + "].userIds[" + identifierIndex + "].identifier").attr("name", function () {return $(this).attr("id");});
            $select.attr("id", "eml." + entityNameInId + "s[" + entityIndex + "].userIds[" + identifierIndex + "].directory").attr("name", function () {return $(this).attr("id");});
            $deleteLink.attr("id", entityName + "-identifier-remove-" + entityIndex + "-" + identifierIndex);
            $("#" + entityName + "-identifier-remove-" + entityIndex + "-" + identifierIndex).click(function (event) {
                removeIdentifierFromAgent(event);
            });

            if (directoryValue && identifierValue) {
                $select.val(directoryValue);
                $input.val(identifierValue);
            }
        }

        // example ID expected: creator-phone-remove-0-0
        function getEntityNameFromRemoveLinkId(id) {
            return id.split("-")[0];
        }

        // example ID expected: plus-creator-phone-0
        function getEntityNameFromAddNewLinkId(id) {
            return id.split("-")[1];
        }

        // example ID expected: creator-item-0
        function getEntityNameFromItemId(id) {
            return id.split("-")[0];
        }

        // example ID expected: creator-phone-remove-0-0
        function getSubEntityNameFromRemoveLinkId(id) {
            return id.split("-")[1];
        }

        // example ID expected: plus-creator-phone-0
        function getSubEntityNameFromAddNewLinkId(id) {
            return id.split("-")[2];
        }

        // example ID expected: creator-item-0
        function getEntityIndexFromItemId(id) {
            if (id.includes("relatedProject") && id.includes("personnel")) {
                return id.split("-")[3];
            } else {
                return id.split("-")[2];
            }
        }

        // example ID expected: creator-phone-remove-0-0
        function getEntityIndexFromRemoveLinkId(id) {
            return id.split("-")[3];
        }

        // example ID expected: creator-phone-remove-0-0
        function getSubEntityIndexFromRemoveLinkId(id) {
            return id.split("-")[4];
        }

        function getTargetLink(event) {
            var $target = $(event.target);
            if (!$target.is('a')) {
                $target = $(event.target).closest('a');
            }
            return $target;
        }

        function removeSubEntityFromAgent(event) {
            event.preventDefault();
            var $target = getTargetLink(event);
            var id = $target.attr("id");
            var entityName = getEntityNameFromRemoveLinkId(id);
            var subEntityName = getSubEntityNameFromRemoveLinkId(id);
            var entityIndex = getEntityIndexFromRemoveLinkId(id);
            var subEntityIndex = getSubEntityIndexFromRemoveLinkId(id);

            $('#' + entityName + '-' + entityIndex + '-' + subEntityName + '-' + subEntityIndex).slideUp('slow', function () {
                $(this).remove();
                $("#" + entityName + "-" + entityIndex + "-" + subEntityName + "s ." + subEntityName + "-item").each(function (index) {
                    setSubEntityIndexes($(this), entityName, subEntityName, entityIndex, index);
                });
            });
        }

        function removeOrCleanAllSubEntitiesFromAgentDirectly(entityName, subEntityName, entityIndex) {
            $('#' + entityName + '-' + entityIndex + '-' + subEntityName + 's').children('.' + subEntityName + '-item').remove();
        }

        function removeIdentifierFromAgent(event) {
            event.preventDefault();
            var $target = getTargetLink(event);
            var id = $target.attr("id");
            var entityName = getEntityNameFromRemoveLinkId(id);
            var entityIndex = getEntityIndexFromRemoveLinkId(id);
            var identifierIndex = getSubEntityIndexFromRemoveLinkId(id);

            $('#' + entityName + '-' + entityIndex + '-identifier-' + identifierIndex).slideUp('slow', function () {
                $(this).remove();
                $("#" + entityName + "-" + entityIndex + "-identifiers .identifier-item").each(function (index) {
                    setIdentifiersIndexes($(this), entityName, entityIndex, index);
                });
            });
        }

        function setSubEntityIndexes(item, entityName, subEntityName, entityIndex, subEntityIndex) {
            item.attr("id", entityName + "-" + entityIndex + "-" + subEntityName + "-" + subEntityIndex);
            var $input = item.find("input");
            var $deleteLink = item.find("a");

            var entityNameInId = entityName === 'associatedParty' ? 'associatedPartie' : entityName;

            $input
                .attr("id", "eml." + entityNameInId + "s[" + entityIndex + "]." + subEntityName + "[" + subEntityIndex + "]")
                .attr("name", function () {
                    return $(this).attr("id");
                });
            $deleteLink.attr("id", entityName + "-" + subEntityName + "-remove-" + entityIndex + "-" + subEntityIndex);
        }

        function setIdentifiersIndexes(item, entityName, entityIndex, identifierIndex) {
            item.attr("id", entityName + "-" + entityIndex + "-identifier-" + identifierIndex);
            var $input = item.find("input");
            var $select = item.find("select");
            var $deleteLink = item.find("a");

            var entityNameInId = entityName === 'associatedParty' ? 'associatedPartie' : entityName;

            $input
                .attr("id", "eml." + entityNameInId + "s[" + entityIndex + "].userIds[" + identifierIndex + "].identifier")
                .attr("name", function () {
                    return $(this).attr("id");
                });
            $select
                .attr("id", "eml." + entityNameInId + "s[" + entityIndex + "].userIds[" + identifierIndex + "].directory")
                .attr("name", function () {
                    return $(this).attr("id");
                });
            $deleteLink.attr("id", entityName + "-identifier-remove-" + entityIndex + "-" + identifierIndex);
        }

        function addNewAgentItem(entityName, effects) {
            var newItem = $('#baseItem-' + entityName).clone();
            if (effects) newItem.hide();
            newItem.appendTo('#' + entityName + '-items');

            if (effects) {
                newItem.slideDown('slow');
            }

            var index = getEntityIndex(entityName);
            setAgentItemIndex(newItem, index, true);

            initInfoPopovers(newItem[0]);
        }

        function getEntityIndex(entityName) {
            if (entityName === "contact") {
                return ++contactItems;
            } else if (entityName === "creator") {
                return ++creatorItems;
            } else if (entityName === "metadataProvider") {
                return ++metadataProviderItems;
            } else if (entityName === "associatedParty") {
                return ++associatedPartyItemsCount;
            }
        }

        function addNewPersonnelItem(effects) {
            var personnelItemsCount = $('#personnel-items .item').length;
            var newItem = $('#baseItem-personnel').clone();
            if (effects) newItem.hide();
            newItem.appendTo('#personnel-items');

            if (effects) {
                newItem.slideDown('slow');
            }

            setPersonnelItemIndex(newItem, personnelItemsCount);

            initInfoPopovers(newItem[0]);
        }

        function removeAgentItem(event) {
            event.preventDefault();
            var $target = getTargetLink(event);
            const id = $target.attr("id");
            const entityName = id.split("-")[0];
            const index = id.split("-")[2];

            $('#' + entityName + '-item-' + index).slideUp('slow', function () {
                $(this).remove();
                $("#" + entityName + "-items .item").each(function (index) {
                    setAgentItemIndex($(this), index);
                });
                calcNumberOfAgentItems();
            });
        }

        function removePersonnelItem(event) {
            event.preventDefault();
            var $target = $(event.target);
            if (!$target.is('a')) {
                $target = $(event.target).closest('a');
            }
            $('#personnel-item-' + $target.attr("id").split("-")[2]).slideUp('slow', function () {
                $(this).remove();
                $("#personnel-items .item").each(function (index) {
                    setPersonnelItemIndex($(this), index);
                });
            });
        }

        function setAgentItemIndex(item, index, isNew) {
            const entityName = getEntityNameFromId(item.attr("id"));
            const entityNamePlural = getEntityNamePlural(entityName);

            setAgentMainBlockId(item, entityName, index);
            setAgentRemoveLink(entityName, index);
            setAgentCopyLink(entityName, index);
            setAgentRegularInput("firstName", entityName, entityNamePlural, index);
            setAgentRegularInput("lastName", entityName, entityNamePlural, index);
            setAgentRegularInput("salutation", entityName, entityNamePlural, index);
            setAgentRegularInput("organisation", entityName, entityNamePlural, index);
            setAgentRegularDropdown("role", entityName, entityNamePlural, index);
            setAgentRepeatableInput("position", entityName, entityNamePlural, index, isNew);
            setAgentRepeatableInput("address", entityName, entityNamePlural, index, isNew);
            setAgentRegularInput("city", entityName, entityNamePlural, index);
            setAgentRegularInput("province", entityName, entityNamePlural, index);
            setAgentRegularDropdown("country", entityName, entityNamePlural, index);
            setAgentRegularInput("postalCode", entityName, entityNamePlural, index);
            setAgentRepeatableInput("phone", entityName, entityNamePlural, index, isNew);
            setAgentRepeatableInput("email", entityName, entityNamePlural, index, isNew);
            setAgentRepeatableInput("homepage", entityName, entityNamePlural, index, isNew);
            setAgentIdentifier(entityName, entityNamePlural, index, isNew);
        }

        function setAgentMainBlockId(item, entityName, index) {
            item.attr("id", entityName + "-item-" + index);
        }

        function setAgentRemoveLink(entityName, index) {
            $("#" + entityName + "-item-" + index + " [id^='" + entityName + "-removeLink']")
                .attr("id", entityName + "-removeLink-" + index);
            $("#" + entityName + "-removeLink-" + index)
                .click(function (event) {
                    removeAgentItem(event);
                });
        }

        function setAgentCopyLink(entityName, index) {
            $("#" + entityName + "-item-" + index + " [id^='" + entityName + "-copy']")
                .attr("id", entityName + "-copy-" + index);
            $("#" + entityName + "-copy-" + index)
                .click(function (event) {
                    event.preventDefault();
                    targetItemId = entityName + "-item-" + index;
                    showCopyAgentModal();
                });

            if (entityName !== "contact") {
                $("#" + entityName + "-item-" + index + " [id^='" + entityName + "-from-contact']")
                    .attr("id", entityName + "-from-contact-" + index);
                $("#" + entityName + "-from-contact-" + index)
                    .click(function (event) {
                        event.preventDefault();
                        copyDetails(event, entityName + "-item-");
                    });
            }
        }

        function setAgentRegularInput(inputName, entityName, entityNamePlural, index) {
            const inputNameInId = (inputName === "city" || inputName === "province" || inputName === "postalCode") ? "address." + inputName : inputName;

            $("#" + entityName + "-item-" + index + " [id$='" + inputName + "']")
                .attr("id", "eml." + entityNamePlural + "[" + index + "]." + inputNameInId)
                .attr("name", function () {
                    return $(this).attr("id");
                });
            $("#" + entityName + "-item-" + index + " [for$='" + inputName + "']")
                .attr("for", "eml." + entityNamePlural + "[" + index + "]." + inputNameInId);
        }

        function setAgentRegularDropdown(dropdownName, entityName, entityNamePlural, index) {
            const dropdownNameInId = (dropdownName === "country") ? "address.country" : dropdownName;
            var $dropdown = $("#" + entityName + "-item-" + index + " [id$='" + dropdownName + "']");

            $dropdown
                .attr("id", "eml." + entityNamePlural + "[" + index + "]." + dropdownNameInId)
                .attr("name", function () {return $(this).attr("id");});
            $("#" + entityName + "-item-" + index + " [for$='" + dropdownName + "']")
                .attr("for", "eml." + entityNamePlural + "[" + index + "]." + dropdownNameInId);

            if (dropdownName === "role") {
                $dropdown
                    .select2({
                        placeholder: '${action.getText("eml.agent.role.selection")?js_string}',
                        language: {
                            noResults: function () {
                                return '${selectNoResultsFound}';
                            }
                        },
                        width: "100%",
                        allowClear: true,
                        theme: 'bootstrap4'
                    });
            } else if (dropdownName === "country") {
                $dropdown
                    .select2({
                        placeholder: '${action.getText("eml.country.selection")?js_string}',
                        language: {
                            noResults: function () {
                                return '${selectNoResultsFound}';
                            }
                        },
                        width: "100%",
                        allowClear: true,
                        theme: 'bootstrap4'
                    });
            }
        }

        function setAgentRepeatableInput(inputName, entityName, entityNamePlural, index, isNew) {
            const inputNamePlural = (inputName === "address") ? inputName : inputName + 's';

            setAgentRepeatableInputMainId(inputNamePlural, entityName, index);
            setAgentRepeatableInputAddNewLink(inputName, entityName, index, isNew);
            setAgentRepeatableInputItems(inputName, inputNamePlural, entityName, entityNamePlural, index)
        }

        function setAgentRepeatableInputMainId(inputNamePlural, entityName, index) {
            $("#" + entityName + "-item-" + index + " ." + entityName + "-" + inputNamePlural)
                .attr("id", entityName + "-" + index + "-" + inputNamePlural);
        }

        function setAgentRepeatableInputAddNewLink(inputName, entityName, index, isNew) {
            $("#" + entityName + "-item-" + index + " [id^='plus-" + entityName + "-" + inputName + "']")
                .attr("id", "plus-" + entityName + "-" + inputName + "-" + index);
            if (isNew) {
                $("#plus-" + entityName + "-" + inputName + "-" + index).click(function (event) {
                    event.preventDefault();
                    createNewSubEntityForAgent(event);
                });
            }
        }

        function setAgentRepeatableInputItems(inputName, inputNamePlural, entityName, entityNamePlural, index) {
            const inputNameInId = (inputName === "address") ? "address.address" : inputName;

            $("#" + entityName + "-item-" + index + " ." + entityName + "-" + inputNamePlural + " ." + inputName + "-item")
                .each(function (inputIndex) {
                    setAgentRepeatableInputItem($(this), inputName, inputNameInId, inputIndex, entityName, entityNamePlural, index);
                });
        }

        function setAgentRepeatableInputItem($item, inputName, inputNameInId, inputIndex, entityName, entityNamePlural, index) {
            // item's id
            $item.attr("id", entityName + "-" + index + "-" + inputName + "-" + inputIndex);

            // input's id and name
            $("#" + entityName + "-" + index + "-" + inputName + "-" + inputIndex + " input")
                .attr("id", "eml." + entityNamePlural + "[" + index + "]." + inputNameInId + "[" + inputIndex + "]")
                .attr("name", function () {return $(this).attr("id");});

            // remove link
            $("#" + entityName + "-" + index + "-" + inputName + "-" + inputIndex + " .removeSubEntity")
                .attr("id", entityName + "-" + inputName + "-remove-" + index + "-" + inputIndex);
        }

        function setAgentIdentifier(entityName, entityNamePlural, index, isNew) {
            // identifiers block id
            $("#" + entityName + "-item-" + index + " ." + entityName + "-identifiers")
                .attr("id", entityName + "-" + index + "-identifiers");

            // identifier add new link
            $("#" + entityName + "-item-" + index + " [id^='plus-" + entityName + "-identifier']")
                .attr("id", "plus-" + entityName + "-identifier-" + index);

            // identifier item
            $("#" + entityName + "-item-" + index + " ." + entityName + "-identifiers .identifier-item")
                .each(function (identifierIndex) {
                    // identifier item id
                    $(this).attr("id", entityName + "-" + index + "-identifier-" + identifierIndex);

                    // identifier directory dropdown element id/name (and select2 initialization)
                    $(this)
                        .find("select")
                        .attr("id", "eml." + entityNamePlural + "[" + index + "].userIds[" + identifierIndex + "].directory")
                        .attr("name", function () {
                            return $(this).attr("id");
                        })
                        .select2({
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

                    // identifier input element id/name
                    $(this)
                        .find("input")
                        .attr("id", "eml." + entityNamePlural + "[" + index + "].userIds[" + identifierIndex + "].identifier")
                        .attr("name", function () {
                            return $(this).attr("id");
                        });

                    // identifier remove link
                    $("#" + entityName + "-" + index + '-identifier-' + identifierIndex + " .removeIdentifier")
                        .attr("id", entityName + "-identifier-remove-" + index + "-" + identifierIndex);
                });

            // initialize link action if new
            if (isNew) {
                $("#plus-" + entityName + "-identifier-" + index).click(function (event) {
                    event.preventDefault();
                    createNewIdentifierForAgent(event);
                });
            }
        }

        function getEntityNameFromId(id) {
            if (id.includes("baseItem")) {
                return id.split("-")[1];
            } else {
                return id.split("-")[0];
            }
        }

        function getEntityNamePlural(entityName) {
            return entityName === "associatedParty" ? "associatedParties" : entityName + 's';
        }

        function setPersonnelItemIndex(item, index) {
            item.attr("id", "personnel-item-" + index);

            $("#personnel-item-" + index + " [id^='personnel-removeLink']").attr("id", "personnel-removeLink-" + index);
            $("#personnel-removeLink-" + index).click(function (event) {
                removePersonnelItem(event);
            });

            $("#personnel-item-" + index + " [id^='personnel-copy']").attr("id", "personnel-copy-" + index);
            $("#personnel-item-" + index + " [id^='personnel-from-contact']").attr("id", "personnel-from-contact-" + index);
            $("#personnel-copy-" + index).click(function (event) {
                event.preventDefault();
                targetItemId = "personnel-item-" + index;
                showCopyAgentModal();
            });
            $("#personnel-from-contact-" + index).click(function (event) {
                event.preventDefault();
                copyPrimaryContactDetails(event, "personnel-item-" + index);
            });

            $("#personnel-item-" + index + " [id$='firstName']").attr("id", "eml.project.personnel[" + index + "].firstName").attr("name", function () {return $(this).attr("id");});
            $("#personnel-item-" + index + " [for$='firstName']").attr("for", "eml.project.personnel[" + index + "].firstName");
            $("#personnel-item-" + index + " [id$='lastName']").attr("id", "eml.project.personnel[" + index + "].lastName").attr("name", function () {return $(this).attr("id");});
            $("#personnel-item-" + index + " [for$='lastName']").attr("for", "eml.project.personnel[" + index + "].lastName");
            $("#personnel-item-" + index + " [id$='salutation']").attr("id", "eml.project.personnel[" + index + "].salutation").attr("name", function () {return $(this).attr("id");});
            $("#personnel-item-" + index + " [for$='salutation']").attr("for", "eml.project.personnel[" + index + "].salutation");
            $("#personnel-item-" + index + " [id$='role']").attr("id", "eml.project.personnel[" + index + "].role").attr("name", function () {return $(this).attr("id");});
            $("#personnel-item-" + index + " [for$='role']").attr("for", "eml.project.personnel[" + index + "].role");
            $("#personnel-item-" + index + " [id$='role']").select2({
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
            $("#personnel-item-" + index + " [id$='directory']").attr("id", "eml.project.personnel[" + index + "].userIds[0].directory").attr("name", function () {return $(this).attr("id");});
            $("#personnel-item-" + index + " [for$='directory']").attr("for", "eml.project.personnel[" + index + "].userIds[0].directory");
            $("#personnel-item-" + index + " [id$='directory']").select2({
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
            $("#personnel-item-" + index + " [id$='identifier']").attr("id", "eml.project.personnel[" + index + "].userIds[0].identifier").attr("name", function () {return $(this).attr("id");});
            $("#personnel-item-" + index + " [for$='identifier']").attr("for", "eml.project.personnel[" + index + "].userIds[0].identifier");
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

        var url = "${baseURL}/manager-api/suggest-resources";
        $.getJSON(url, function (data) {
            $.each(data, function (i, item) {
                $('#resource').append($('<option>', {
                    value: i,
                    text: item
                }));
            });
        });

        var selectedResource;
        var selectedAgentType;
        var agents;
        var selectedAgent;
        var targetItemId;

        $('#resource').on('change', function() {
            selectedResource = this.value;

            if (selectedResource && selectedAgentType) {
                var suggestAgentsUrl = "${baseURL}/manager-api/suggest-agents?r=" + selectedResource + "&type=" + selectedAgentType;
                $.getJSON(suggestAgentsUrl, function (data) {
                    agents = data;
                    document.getElementById("agent").options.length = 0;
                    $('#agent').append($('<option>', {value: "", text: ""}));
                    $.each(data, function (i, item) {
                        $('#agent').append($('<option>', {
                            value: i,
                            text: i
                        }));
                    });
                });
            }
        });

        $('#agentType').on('change', function() {
            selectedAgentType = this.value;

            if (selectedResource && selectedAgentType) {
                var suggestAgentsUrl = "${baseURL}/manager-api/suggest-agents?r=" + selectedResource + "&type=" + selectedAgentType;
                $.getJSON(suggestAgentsUrl, function (data) {
                    agents = data;
                    document.getElementById("agent").options.length = 0;
                    $('#agent').append($('<option>', {value: "", text: ""}));
                    $.each(data, function (i, item) {
                        $('#agent').append($('<option>', {
                            value: i,
                            text: i
                        }));
                    });
                });
            }
        });

        $('#agent').on('change', function() {
            selectedAgent = agents[this.value];

            if (selectedAgent) {
                $("#copy-agent-button").show();
            } else {
                $("#copy-agent-button").hide();
            }
        });

        function showCopyAgentModal() {
            var dialogWindow = $("#copy-agent-modal");
            dialogWindow.modal('show');
        }

        $("[id^='contact-copy']").on('click', function (e) {
            e.preventDefault();
            targetItemId = this.id.replace('-copy-', '-item-');
            showCopyAgentModal();
        });

        $("[id^='creator-copy']").on('click', function (e) {
            e.preventDefault();
            targetItemId = this.id.replace('-copy-', '-item-');
            showCopyAgentModal();
        });

        $("[id^='metadataProvider-copy']").on('click', function (e) {
            e.preventDefault();
            targetItemId = this.id.replace('-copy-', '-item-');
            showCopyAgentModal();
        });

        $("[id^='associatedParty-copy']").on('click', function (e) {
            e.preventDefault();
            targetItemId = this.id.replace('-copy-', '-item-');
            showCopyAgentModal();
        });

        $("[id^='personnel-copy']").on('click', function (e) {
            e.preventDefault();
            targetItemId = this.id.replace('-copy-', '-item-');
            showCopyAgentModal();
        });

        $(".relatedProject-personnel-copy-personnel-link").on('click', function (e) {
            e.preventDefault();
            targetItemId = this.id.replace('-copy-', '-');
            showCopyAgentModal();
        });

        $("#copy-agent-button").on('click', function (e) {
            $("#" + targetItemId + " input[id$='firstName']").val(selectedAgent['firstName']);
            $("#" + targetItemId + " input[id$='lastName']").val(selectedAgent['lastName']);
            $("#" + targetItemId + " input[id$='salutation']").val(selectedAgent['salutation']);
            copyAllSubEntitiesFromAnother("position");
            $("#" + targetItemId + " input[id$='organisation']").val(selectedAgent['organisation']);

            copyAllSubEntitiesFromAnother("address");
            $("#" + targetItemId + " input[id$='city']").val(selectedAgent['address']['city']);
            $("#" + targetItemId + " input[id$='province']").val(selectedAgent['address']['province']);
            $("#" + targetItemId + " select[id$='country']").val(selectedAgent['address']['country']);
            $("#" + targetItemId + " select[id$='country']").trigger('change');
            $("#" + targetItemId + " input[id$='postalCode']").val(selectedAgent['address']['postalCode']);

            copyAllSubEntitiesFromAnother("phone");
            copyAllSubEntitiesFromAnother("email");
            copyAllSubEntitiesFromAnother("homepage");
            copyAllIdentifiersFromAnother();

            $("#" + targetItemId + " select[id$='role']").val(selectedAgent['role']);

            $('#copy-agent-modal').modal('hide');
        });

        function copyAllSubEntitiesFromAnother(subEntityName) {
            // positions, email, homepages, phones, addresses
            let subEntities = selectedAgent[subEntityName];
            var entityName = getEntityNameFromItemId(targetItemId);
            var entityIndex = getEntityIndexFromItemId(targetItemId);

            // first, remove current info
            removeOrCleanAllSubEntitiesFromAgentDirectly(entityName, subEntityName, entityIndex);

            // multiple objects in the address, choose 'address'
            if (subEntityName === "address") {
                subEntities = subEntities.address;
            }

            for (let i = 0; i < subEntities.length; i++) {
                createNewSubEntityForAgentDirectly(entityName, subEntityName, entityIndex, subEntities[i]);
            }
        }

        function copyAllSubEntitiesFromFirstContact(entityName, subEntityName, entityIndex) {
            // Take first contact
            var $firstContactInputs;
            if (subEntityName === "address") {
                $firstContactInputs = $("[id^=eml\\.contacts\\[0\\]\\.address\\.address]");
            } else {
                $firstContactInputs = $("[id^=eml\\.contacts\\[0\\]\\." + subEntityName + "]");
            }

            // positions, email, homepages, phones, addresses
            for (let i = 0; i < $firstContactInputs.length; i++) {
                createNewSubEntityForAgentDirectly(entityName, subEntityName, entityIndex, $firstContactInputs[i].value)
            }
        }

        function copyAllIdentifiersFromAnother() {
            var selectedAgentUserIds = selectedAgent['userIds'];
            var entityName = getEntityNameFromItemId(targetItemId);
            var entityIndex = getEntityIndexFromItemId(targetItemId);

            removeOrCleanAllSubEntitiesFromAgentDirectly(entityName, "identifier", entityIndex);

            // related project personnel have only one identifier
            if (targetItemId.includes('relatedProject') && targetItemId.includes("personnel")) {
                var firstIndex = targetItemId.split("-")[1];
                var secondIndex = targetItemId.split("-")[3];

                var $directorySelect = $('#eml\\.project\\.relatedProjects\\[' + firstIndex + '\\]\\.personnel\\[' + secondIndex + '\\]\\.userIds\\[0\\]\\.directory');
                var $identifierInput = $('#eml\\.project\\.relatedProjects\\[' + firstIndex + '\\]\\.personnel\\[' + secondIndex + '\\]\\.userIds\\[0\\]\\.identifier');

                $directorySelect.val('');
                $directorySelect.trigger('change');
                $identifierInput.val('');

                if (selectedAgentUserIds[0]) {
                    $directorySelect.val(selectedAgentUserIds[0].directory);
                    $directorySelect.trigger('change');
                    $identifierInput.val(selectedAgentUserIds[0].identifier);
                }
            }

            // project personnel have only one identifier
            if (targetItemId.includes('personnel-item')) {
                var index = targetItemId.split("-")[2];

                var $personnelDirectorySelect = $('#eml\\.project\\.personnel\\[' + index + '\\]\\.userIds\\[0\\]\\.directory');
                var $personnelIdentifierInput = $('#eml\\.project\\.personnel\\[' + index + '\\]\\.userIds\\[0\\]\\.identifier');

                $personnelDirectorySelect.val('');
                $personnelDirectorySelect.trigger('change');
                $personnelIdentifierInput.val('');

                if (selectedAgentUserIds[0]) {
                    $personnelDirectorySelect.val(selectedAgentUserIds[0].directory);
                    $personnelDirectorySelect.trigger('change');
                    $personnelIdentifierInput.val(selectedAgentUserIds[0].identifier);
                }
            }

            for (let i = 0; i < selectedAgentUserIds.length; i++) {
                createNewIdentifierForAgentDirectly(entityName, entityIndex, selectedAgentUserIds[i]["directory"], selectedAgentUserIds[i]["identifier"]);
            }
        }

        function copyAllIdentifiersFromFirstContact(entityName, entityIndex) {
            // Take first contact
            var $firstContactDirectorySelects;
            var $firstContactIdentifierInputs;
            $firstContactDirectorySelects = $("select[id^=eml\\.contacts\\[0\\]\\.userIds]");
            $firstContactIdentifierInputs = $("input[id^=eml\\.contacts\\[0\\]\\.userIds]");

            for (let i = 0; i < $firstContactDirectorySelects.length; i++) {
                createNewIdentifierForAgentDirectly(entityName, entityIndex, $firstContactDirectorySelects[i].value, $firstContactIdentifierInputs[i].value);
            }
        }

        $("[id^='creator-from-contact']").click(function(event) {
            event.preventDefault();
            copyDetails(event, "creator-item-");
        });

        $("[id^='metadataProvider-from-contact']").click(function(event) {
            event.preventDefault();
            copyDetails(event, "metadataProvider-item-");
        });

        $("[id^='associatedParty-from-contact']").click(function(event) {
            event.preventDefault();
            copyPrimaryContactDetails(event, getItemId(event, "item"));
        });

        $("[id^='personnel-from-contact']").click(function(event) {
            event.preventDefault();
            copyPrimaryContactDetails(event, getItemId(event, "item"));
        });

        $(".relatedProject-personnel-copy-from-contact-link").click(function(event) {
            event.preventDefault();
            copyPrimaryContactDetails(event, getItemId(event, "relatedProject-personnel-item"));
        });

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

        function copyDetails(event, idPrefix) {
            event.preventDefault();
            var $target = $(event.target);
            if (!$target.is('a')) {
                $target = $(event.target).closest('a');
            }

            var index = $target.attr("id").split("-")[3];
            var entityName = getEntityNameFromItemId($target.attr("id"));

            $("#" + idPrefix + index + " [id$='firstName']").val($("#eml\\.contacts\\[0\\]\\.firstName").val());
            $("#" + idPrefix + index + " [id$='lastName']").val($("#eml\\.contacts\\[0\\]\\.lastName").val());
            $("#" + idPrefix + index + " [id$='salutation']").val($("#eml\\.contacts\\[0\\]\\.salutation").val());
            copyAllSubEntitiesFromFirstContact(entityName, "position", index);
            $("#" + idPrefix + index + " [id$='organisation']").val($("#eml\\.contacts\\[0\\]\\.organisation").val());
            copyAllSubEntitiesFromFirstContact(entityName, "address", index);
            $("#" + idPrefix + index + " [id$='city']").val($("#eml\\.contacts\\[0\\]\\.address\\.city").val());
            $("#" + idPrefix + index + " [id$='province']").val($("#eml\\.contacts\\[0\\]\\.address\\.province").val());
            $("#" + idPrefix + index + " [id$='postalCode']").val($("#eml\\.contacts\\[0\\]\\.address\\.postalCode").val());
            $("#" + idPrefix + index + " [id$='country']").val($("#eml\\.contacts\\[0\\]\\.address\\.country").val());
            copyAllSubEntitiesFromFirstContact(entityName, "phone", index);
            copyAllSubEntitiesFromFirstContact(entityName, "email", index);
            copyAllSubEntitiesFromFirstContact(entityName, "homepage", index);
            copyAllIdentifiersFromFirstContact(entityName, index);
        }

        function copyPrimaryContactDetails(event, itemId) {
            event.preventDefault();

            // replace " with &quot; to prevent JS from failing
            $("#" + itemId + " [id$='firstName']").val("${primaryContact.firstName!?replace("\"", "&quot;")}");
            $("#" + itemId + " [id$='lastName']").val("${primaryContact.lastName!?replace("\"", "&quot;")}");
            $("#" + itemId + " [id$='salutation']").val("${primaryContact.salutation!?replace("\"", "&quot;")}");
            $("#" + itemId + " [id$='position']").val("${primaryContact.position!?replace("\"", "&quot;")}");
            $("#" + itemId + " [id$='organisation']").val("${primaryContact.organisation!?replace("\"", "&quot;")}");
            $("#" + itemId + " [id$='address']").val("${primaryContact.address.address!?replace("\"", "&quot;")}");
            $("#" + itemId + " [id$='city']").val("${primaryContact.address.city!?replace("\"", "&quot;")}");
            $("#" + itemId + " [id$='province']").val("${primaryContact.address.province!?replace("\"", "&quot;")}");
            $("#" + itemId + " [id$='postalCode']").val("${primaryContact.address.postalCode!?replace("\"", "&quot;")}");
            $("#" + itemId + " [id$='country']").val("${primaryContact.address.country!?replace("\"", "&quot;")}");
            $("#" + itemId + " [id$='phone']").val("${primaryContact.phone!?replace("\"", "&quot;")}");
            $("#" + itemId + " [id$='email']").val("${primaryContact.email!?replace("\"", "&quot;")}");
            $("#" + itemId + " [id$='homepage']").val("${primaryContact.homepage!?replace("\"", "&quot;")}");
            $("#" + itemId + " [id$='directory']").val("${primaryContact.userIds[0].directory!?replace("\"", "&quot;")}");
            $("#" + itemId + " [id$='directory']").trigger('change');
            $("#" + itemId + " [id$='identifier']").val("${primaryContact.userIds[0].identifier!?replace("\"", "&quot;")}");
        }

});
</script>
