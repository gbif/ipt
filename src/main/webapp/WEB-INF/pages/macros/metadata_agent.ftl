<script>
    $(document).ready(function(){

        var contactItems = -1;
        var creatorItems = -1;
        var metadataProviderItems = -1;
        var associatedPartyItemsCount = -1;

        calcNumberOfContactItems();
        calcNumberOfCreatorItems();
        calcNumberOfMetadataProviderItems();
        calcNumberOfAssociatedPartyItems();

        function calcNumberOfContactItems() {
            var lastItem = $("#contact-items .item:last-child").attr("id");
            if (lastItem !== undefined)
                contactItems = parseInt(lastItem.split("-")[2]);
            else
                contactItems = -1;
        }

        function calcNumberOfCreatorItems() {
            var lastItem = $("#creator-items .item:last-child").attr("id");
            if (lastItem !== undefined)
                creatorItems = parseInt(lastItem.split("-")[2]);
            else
                creatorItems = -1;
        }

        function calcNumberOfMetadataProviderItems() {
            var lastItem = $("#metadataProvider-items .item:last-child").attr("id");
            if (lastItem !== undefined)
                metadataProviderItems = parseInt(lastItem.split("-")[2]);
            else
                metadataProviderItems = -1;
        }

        function calcNumberOfAssociatedPartyItems() {
            var lastItem = $("#associatedParty-items .item:last-child").attr("id");
            if (lastItem !== undefined)
                associatedPartyItemsCount = parseInt(lastItem.split("-")[2]);
            else
                associatedPartyItemsCount = -1;
        }

        function initializeSortableComponent(componentId) {
            sortable('#' + componentId, {
                forcePlaceholderSize: true,
                placeholderClass: 'border',
                exclude: 'input'
            });
        }

        $("#plus-contact").click(function (event) {
            event.preventDefault();
            addNewContactItem(true);
            initializeSortableComponent("contact-items")
        });

        $("#plus-creator").click(function (event) {
            event.preventDefault();
            addNewCreatorItem(true);
            initializeSortableComponent("creator-items")
        });

        $("#plus-metadataProvider").click(function (event) {
            event.preventDefault();
            addNewMetadataProviderItem(true);
            initializeSortableComponent("metadataProvider-items")
        });

        $("#plus-associatedParty").click(function (event) {
            event.preventDefault();
            addNewAssociatedPartyItem(true);
            initializeSortableComponent("associatedParty-items")
        });

        $("#plus-personnel").click(function (event) {
            event.preventDefault();
            addNewPersonnelItem(true);
            initializeSortableComponent("personnel-items");
        });

        $(".add-relatedProject-personnel").click(function (event) {
            event.preventDefault();
            createNewPersonnelForRelatedProject(event);
        });

        $(".removeContactLink").click(function (event) {
            removeContactItem(event);
        });

        $(".removeCreatorLink").click(function (event) {
            removeCreatorItem(event);
        });

        $(".removeMetadataProviderLink").click(function (event) {
            removeMetadataProviderItem(event);
        });

        $(".removeAssociatedPartyLink").click(function (event) {
            removeAssociatedPartyItem(event);
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
            newItem.appendTo('#' + entityName + '-' + entityIndex + '-' + subEntityName + 's');
            newItem.slideDown('slow');

            // set correct indexes, names, ids
            var numberOfSubEntities = $("#" + entityName + "-" + entityIndex + "-" + subEntityName + "s ." + subEntityName + "-item").length;
            var numberOfSubEntitiesInt = parseInt(numberOfSubEntities);
            var subEntityIndex = numberOfSubEntities === 0 ? 0 : numberOfSubEntitiesInt - 1;

            newItem.attr("id", entityName + "-" + entityIndex + "-" + subEntityName + "-" + subEntityIndex);
            var $input = newItem.find("#baseItem-" + subEntityName + "-input");
            var $deleteLink = newItem.find("#baseItem-" + subEntityName + "-remove");
            if (subEntityName === "address") {
                $input.attr("id", "eml." + entityName + "s[" + entityIndex + "].address.address[" + subEntityIndex + "]").attr("name", function () {return $(this).attr("id");});
            } else {
                $input.attr("id", "eml." + entityName + "s[" + entityIndex + "]." + subEntityName + "[" + subEntityIndex + "]").attr("name", function () {return $(this).attr("id");});
            }
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
            if (subEntityName === "address") {
                $input.attr("id", "eml." + entityName + "s[" + entityIndex + "].address.address[" + subEntityIndex + "]").attr("name", function () {return $(this).attr("id");});
            } else {
                $input.attr("id", "eml." + entityName + "s[" + entityIndex + "]." + subEntityName + "[" + subEntityIndex + "]").attr("name", function () {return $(this).attr("id");});
            }
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

            $input.attr("id", "eml." + entityName + "s[" + entityIndex + "].userIds[" + identifierIndex + "].identifier").attr("name", function () {return $(this).attr("id");});
            $select.attr("id", "eml." + entityName + "s[" + entityIndex + "].userIds[" + identifierIndex + "].directory").attr("name", function () {return $(this).attr("id");});
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

            $input.attr("id", "eml." + entityName + "s[" + entityIndex + "].userIds[" + identifierIndex + "].identifier").attr("name", function () {return $(this).attr("id");});
            $select.attr("id", "eml." + entityName + "s[" + entityIndex + "].userIds[" + identifierIndex + "].directory").attr("name", function () {return $(this).attr("id");});
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
            $input.attr("id", "eml." + entityName + "s[" + entityIndex + "]." + subEntityName + "[" + subEntityIndex + "]").attr("name", function () {
                return $(this).attr("id");
            });
            $deleteLink.attr("id", entityName + "-" + subEntityName + "-remove-" + entityIndex + "-" + subEntityIndex);
        }

        function setIdentifiersIndexes(item, entityName, entityIndex, identifierIndex) {
            item.attr("id", entityName + "-" + entityIndex + "-identifier-" + identifierIndex);
            var $input = item.find("input");
            var $select = item.find("select");
            var $deleteLink = item.find("a");
            $input.attr("id", "eml." + entityName + "s[" + entityIndex + "].userIds[" + identifierIndex + "].identifier").attr("name", function () {
                return $(this).attr("id");
            });
            $select.attr("id", "eml." + entityName + "s[" + entityIndex + "].userIds[" + identifierIndex + "].directory").attr("name", function () {
                return $(this).attr("id");
            });
            $deleteLink.attr("id", entityName + "-identifier-remove-" + entityIndex + "-" + identifierIndex);
        }

        function addNewContactItem(effects) {
            var newItem = $('#baseItem-contact').clone();
            if (effects) newItem.hide();
            newItem.appendTo('#contact-items');

            if (effects) {
                newItem.slideDown('slow');
            }

            setContactItemIndex(newItem, ++contactItems);

            initInfoPopovers(newItem[0]);
        }

        function addNewCreatorItem(effects) {
            var newItem = $('#baseItem-creator').clone();
            if (effects) newItem.hide();
            newItem.appendTo('#creator-items');

            if (effects) {
                newItem.slideDown('slow');
            }

            setCreatorItemIndex(newItem, ++creatorItems);

            initInfoPopovers(newItem[0]);
        }

        function addNewMetadataProviderItem(effects) {
            var newItem = $('#baseItem-metadataProvider').clone();
            if (effects) newItem.hide();
            newItem.appendTo('#metadataProvider-items');

            if (effects) {
                newItem.slideDown('slow');
            }

            setMetadataProviderItemIndex(newItem, ++metadataProviderItems);

            initInfoPopovers(newItem[0]);
        }

        function addNewAssociatedPartyItem(effects) {
            var newItem = $('#baseItem-associatedParty').clone();
            if (effects) newItem.hide();
            newItem.appendTo('#associatedParty-items');

            if (effects) {
                newItem.slideDown('slow');
            }

            setAssociatedPartyItemIndex(newItem, ++associatedPartyItemsCount);

            initInfoPopovers(newItem[0]);
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

        function removeContactItem(event) {
            event.preventDefault();
            var $target = $(event.target);
            if (!$target.is('a')) {
                $target = $(event.target).closest('a');
            }
            $('#contact-item-' + $target.attr("id").split("-")[2]).slideUp('slow', function () {
                $(this).remove();
                $("#contact-items .item").each(function (index) {
                    setContactItemIndex($(this), index);
                });
                calcNumberOfContactItems();
            });
        }

        function removeCreatorItem(event) {
            event.preventDefault();
            var $target = $(event.target);
            if (!$target.is('a')) {
                $target = $(event.target).closest('a');
            }
            $('#creator-item-' + $target.attr("id").split("-")[2]).slideUp('slow', function () {
                $(this).remove();
                $("#creator-items .item").each(function (index) {
                    setCreatorItemIndex($(this), index);
                });
                calcNumberOfCreatorItems();
            });
        }

        function removeMetadataProviderItem(event) {
            event.preventDefault();
            var $target = $(event.target);
            if (!$target.is('a')) {
                $target = $(event.target).closest('a');
            }
            $('#metadataProvider-item-' + $target.attr("id").split("-")[2]).slideUp('slow', function () {
                $(this).remove();
                $("#metadataProvider-items .item").each(function (index) {
                    setMetadataProviderItemIndex($(this), index);
                });
                calcNumberOfMetadataProviderItems();
            });
        }

        function removeAssociatedPartyItem(event) {
            event.preventDefault();
            var $target = $(event.target);
            if (!$target.is('a')) {
                $target = $(event.target).closest('a');
            }
            $('#associatedParty-item-' + $target.attr("id").split("-")[2]).slideUp('slow', function () {
                $(this).remove();
                $("#associatedParty-items .item").each(function (index) {
                    setAssociatedPartyItemIndex($(this), index);
                });
                calcNumberOfAssociatedPartyItems();
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

        function setContactItemIndex(item, index) {
            item.attr("id", "contact-item-" + index);

            $("#contact-item-" + index + " [id^='contact-removeLink']").attr("id", "contact-removeLink-" + index);
            $("#contact-removeLink-" + index).click(function (event) {
                removeContactItem(event);
            });

            $("#contact-item-" + index + " [id^='contact-copy']").attr("id", "contact-copy-" + index);
            $("#contact-copy-" + index).click(function (event) {
                event.preventDefault();
                targetItemId = "contact-item-" + index;
                showCopyAgentModal();
            });

            $("#contact-item-" + index + " [id$='firstName']").attr("id", "eml.contacts[" + index + "].firstName").attr("name", function () {return $(this).attr("id");});
            $("#contact-item-" + index + " [for$='firstName']").attr("for", "eml.contacts[" + index + "].firstName");
            $("#contact-item-" + index + " [id$='lastName']").attr("id", "eml.contacts[" + index + "].lastName").attr("name", function () {return $(this).attr("id");});
            $("#contact-item-" + index + " [for$='lastName']").attr("for", "eml.contacts[" + index + "].lastName");
            $("#contact-item-" + index + " [id$='salutation']").attr("id", "eml.contacts[" + index + "].salutation").attr("name", function () {return $(this).attr("id");});
            $("#contact-item-" + index + " [for$='salutation']").attr("for", "eml.contacts[" + index + "].salutation");
            $("#contact-item-" + index + " #contact-positions").attr("id", "contact-" + index + "-positions");
            $("#contact-item-" + index + " #plus-contact-position").attr("id", "plus-contact-position-" + index);
            $("#plus-contact-position-" + index).click(function (event) {
                event.preventDefault();
                createNewSubEntityForAgent(event);
            });
            $("#contact-item-" + index + " [id$='organisation']").attr("id", "eml.contacts[" + index + "].organisation").attr("name", function () {return $(this).attr("id");});
            $("#contact-item-" + index + " [for$='organisation']").attr("for", "eml.contacts[" + index + "].organisation");
            $("#contact-item-" + index + " #contact-addresss").attr("id", "contact-" + index + "-addresss");
            $("#contact-item-" + index + " #plus-contact-address").attr("id", "plus-contact-address-" + index);
            $("#plus-contact-address-" + index).click(function (event) {
                event.preventDefault();
                createNewSubEntityForAgent(event);
            });
            $("#contact-item-" + index + " [id$='postalCode']").attr("id", "eml.contacts[" + index + "].address.postalCode").attr("name", function () {return $(this).attr("id");});
            $("#contact-item-" + index + " [for$='postalCode']").attr("for", "eml.contacts[" + index + "].address.postalCode");
            $("#contact-item-" + index + " [id$='city']").attr("id", "eml.contacts[" + index + "].address.city").attr("name", function () {return $(this).attr("id");});
            $("#contact-item-" + index + " [for$='city']").attr("for", "eml.contacts[" + index + "].address.city");
            $("#contact-item-" + index + " [id$='province']").attr("id", "eml.contacts[" + index + "].address.province").attr("name", function () {return $(this).attr("id");});
            $("#contact-item-" + index + " [for$='province']").attr("for", "eml.contacts[" + index + "].address.province");
            $("#contact-item-" + index + " [id$='country']").attr("id", "eml.contacts[" + index + "].address.country").attr("name", function () {return $(this).attr("id");});
            $("#contact-item-" + index + " [for$='country']").attr("for", "eml.contacts[" + index + "].address.country");
            $("#contact-item-" + index + " [id$='country']").select2({
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

            $("#contact-item-" + index + " #contact-phones").attr("id", "contact-" + index + "-phones");
            $("#contact-item-" + index + " #plus-contact-phone").attr("id", "plus-contact-phone-" + index);
            $("#plus-contact-phone-" + index).click(function (event) {
                event.preventDefault();
                createNewSubEntityForAgent(event);
            });
            $("#contact-item-" + index + " #contact-emails").attr("id", "contact-" + index + "-emails");
            $("#contact-item-" + index + " #plus-contact-email").attr("id", "plus-contact-email-" + index);
            $("#plus-contact-email-" + index).click(function (event) {
                event.preventDefault();
                createNewSubEntityForAgent(event);
            });
            $("#contact-item-" + index + " #contact-homepages").attr("id", "contact-" + index + "-homepages");
            $("#contact-item-" + index + " #plus-contact-homepage").attr("id", "plus-contact-homepage-" + index);
            $("#plus-contact-homepage-" + index).click(function (event) {
                event.preventDefault();
                createNewSubEntityForAgent(event);
            });
            $("#contact-item-" + index + " #contact-identifiers").attr("id", "contact-" + index + "-identifiers");
            $("#contact-item-" + index + " #plus-contact-identifier").attr("id", "plus-contact-identifier-" + index);
            $("#contact-item-" + index + " [id$='directory']").attr("id", "eml.contacts[" + index + "].userIds[0].directory").attr("name", function () {return $(this).attr("id");});
            $("#contact-item-" + index + " [for$='directory']").attr("for", "eml.contacts[" + index + "].userIds[0].directory");
            $("#contact-item-" + index + " [id$='directory']").select2({
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

            $("#contact-item-" + index + " [id$='identifier']").attr("id", "eml.contacts[" + index + "].userIds[0].identifier").attr("name", function () {return $(this).attr("id");});
            $("#contact-item-" + index + " [for$='identifier']").attr("for", "eml.contacts[" + index + "].userIds[0].identifier");
            $("#plus-contact-identifier-" + index).click(function (event) {
                event.preventDefault();
                createNewIdentifierForAgent(event);
            });
        }

        function setCreatorItemIndex(item, index) {
            item.attr("id", "creator-item-" + index);

            $("#creator-item-" + index + " [id^='creator-removeLink']").attr("id", "creator-removeLink-" + index);
            $("#creator-removeLink-" + index).click(function (event) {
                removeCreatorItem(event);
            });

            $("#creator-item-" + index + " [id^='creator-copy']").attr("id", "creator-copy-" + index);
            $("#creator-item-" + index + " [id^='creator-from-contact']").attr("id", "creator-from-contact-" + index);
            $("#creator-copy-" + index).click(function (event) {
                event.preventDefault();
                targetItemId = "creator-item-" + index;
                showCopyAgentModal();
            });
            $("#creator-from-contact-" + index).click(function (event) {
                event.preventDefault();
                copyDetails(event, "creator-item-");
            });

            $("#creator-item-" + index + " [id$='firstName']").attr("id", "eml.creators[" + index + "].firstName").attr("name", function () {return $(this).attr("id");});
            $("#creator-item-" + index + " [for$='firstName']").attr("for", "eml.creators[" + index + "].firstName");
            $("#creator-item-" + index + " [id$='lastName']").attr("id", "eml.creators[" + index + "].lastName").attr("name", function () {return $(this).attr("id");});
            $("#creator-item-" + index + " [for$='lastName']").attr("for", "eml.creators[" + index + "].lastName");
            $("#creator-item-" + index + " [id$='salutation']").attr("id", "eml.creators[" + index + "].salutation").attr("name", function () {return $(this).attr("id");});
            $("#creator-item-" + index + " [for$='salutation']").attr("for", "eml.creators[" + index + "].salutation");
            $("#creator-item-" + index + " #creator-positions").attr("id", "creator-" + index + "-positions");
            $("#creator-item-" + index + " #plus-creator-position").attr("id", "plus-creator-position-" + index);
            $("#plus-creator-position-" + index).click(function (event) {
                event.preventDefault();
                createNewSubEntityForAgent(event);
            });
            $("#creator-item-" + index + " [id$='organisation']").attr("id", "eml.creators[" + index + "].organisation").attr("name", function () {return $(this).attr("id");});
            $("#creator-item-" + index + " [for$='organisation']").attr("for", "eml.creators[" + index + "].organisation");
            $("#creator-item-" + index + " #creator-addresss").attr("id", "creator-" + index + "-addresss");
            $("#creator-item-" + index + " #plus-creator-address").attr("id", "plus-creator-address-" + index);
            $("#plus-creator-address-" + index).click(function (event) {
                event.preventDefault();
                createNewSubEntityForAgent(event);
            });
            $("#creator-item-" + index + " [id$='postalCode']").attr("id", "eml.creators[" + index + "].address.postalCode").attr("name", function () {return $(this).attr("id");});
            $("#creator-item-" + index + " [for$='postalCode']").attr("for", "eml.creators[" + index + "].address.postalCode");
            $("#creator-item-" + index + " [id$='city']").attr("id", "eml.creators[" + index + "].address.city").attr("name", function () {return $(this).attr("id");});
            $("#creator-item-" + index + " [for$='city']").attr("for", "eml.creators[" + index + "].address.city");
            $("#creator-item-" + index + " [id$='province']").attr("id", "eml.creators[" + index + "].address.province").attr("name", function () {return $(this).attr("id");});
            $("#creator-item-" + index + " [for$='province']").attr("for", "eml.creators[" + index + "].address.province");
            $("#creator-item-" + index + " [id$='country']").attr("id", "eml.creators[" + index + "].address.country").attr("name", function () {return $(this).attr("id");});
            $("#creator-item-" + index + " [for$='country']").attr("for", "eml.creators[" + index + "].address.country");
            $("#creator-item-" + index + " [id$='country']").select2({
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

            $("#creator-item-" + index + " #creator-phones").attr("id", "creator-" + index + "-phones");
            $("#creator-item-" + index + " #plus-creator-phone").attr("id", "plus-creator-phone-" + index);
            $("#plus-creator-phone-" + index).click(function (event) {
                event.preventDefault();
                createNewSubEntityForAgent(event);
            });
            $("#creator-item-" + index + " #creator-emails").attr("id", "creator-" + index + "-emails");
            $("#creator-item-" + index + " #plus-creator-email").attr("id", "plus-creator-email-" + index);
            $("#plus-creator-email-" + index).click(function (event) {
                event.preventDefault();
                createNewSubEntityForAgent(event);
            });
            $("#creator-item-" + index + " #creator-homepages").attr("id", "creator-" + index + "-homepages");
            $("#creator-item-" + index + " #plus-creator-homepage").attr("id", "plus-creator-homepage-" + index);
            $("#plus-creator-homepage-" + index).click(function (event) {
                event.preventDefault();
                createNewSubEntityForAgent(event);
            });
            $("#creator-item-" + index + " #creator-identifiers").attr("id", "creator-" + index + "-identifiers");
            $("#creator-item-" + index + " #plus-creator-identifier").attr("id", "plus-creator-identifier-" + index);
            $("#creator-item-" + index + " [id$='directory']").attr("id", "eml.creators[" + index + "].userIds[0].directory").attr("name", function () {return $(this).attr("id");});
            $("#creator-item-" + index + " [for$='directory']").attr("for", "eml.creators[" + index + "].userIds[0].directory");
            $("#creator-item-" + index + " [id$='directory']").select2({
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

            $("#creator-item-" + index + " [id$='identifier']").attr("id", "eml.creators[" + index + "].userIds[0].identifier").attr("name", function () {return $(this).attr("id");});
            $("#creator-item-" + index + " [for$='identifier']").attr("for", "eml.creators[" + index + "].userIds[0].identifier");
            $("#plus-creator-identifier-" + index).click(function (event) {
                event.preventDefault();
                createNewIdentifierForAgent(event);
            });
        }

        function setMetadataProviderItemIndex(item, index) {
            item.attr("id", "metadataProvider-item-" + index);

            $("#metadataProvider-item-" + index + " [id^='metadataProvider-removeLink']").attr("id", "metadataProvider-removeLink-" + index);
            $("#metadataProvider-removeLink-" + index).click(function (event) {
                removeMetadataProviderItem(event);
            });

            $("#metadataProvider-item-" + index + " [id^='metadataProvider-copy']").attr("id", "metadataProvider-copy-" + index);
            $("#metadataProvider-item-" + index + " [id^='metadataProvider-from-contact']").attr("id", "metadataProvider-from-contact-" + index);
            $("#metadataProvider-copy-" + index).click(function (event) {
                event.preventDefault();
                targetItemId = "metadataProvider-item-" + index;
                showCopyAgentModal();
            });
            $("#metadataProvider-from-contact-" + index).click(function (event) {
                event.preventDefault();
                copyDetails(event, "metadataProvider-item-");
            });

            $("#metadataProvider-item-" + index + " [id$='firstName']").attr("id", "eml.metadataProviders[" + index + "].firstName").attr("name", function () {return $(this).attr("id");});
            $("#metadataProvider-item-" + index + " [for$='firstName']").attr("for", "eml.metadataProviders[" + index + "].firstName");
            $("#metadataProvider-item-" + index + " [id$='lastName']").attr("id", "eml.metadataProviders[" + index + "].lastName").attr("name", function () {return $(this).attr("id");});
            $("#metadataProvider-item-" + index + " [for$='lastName']").attr("for", "eml.metadataProviders[" + index + "].lastName");
            $("#metadataProvider-item-" + index + " [id$='salutation']").attr("id", "eml.metadataProviders[" + index + "].salutation").attr("name", function () {return $(this).attr("id");});
            $("#metadataProvider-item-" + index + " [for$='salutation']").attr("for", "eml.metadataProviders[" + index + "].salutation");
            $("#metadataProvider-item-" + index + " #metadataProvider-positions").attr("id", "metadataProvider-" + index + "-positions");
            $("#metadataProvider-item-" + index + " #plus-metadataProvider-position").attr("id", "plus-metadataProvider-position-" + index);
            $("#plus-metadataProvider-position-" + index).click(function (event) {
                event.preventDefault();
                createNewSubEntityForAgent(event);
            });
            $("#metadataProvider-item-" + index + " [id$='organisation']").attr("id", "eml.metadataProviders[" + index + "].organisation").attr("name", function () {return $(this).attr("id");});
            $("#metadataProvider-item-" + index + " [for$='organisation']").attr("for", "eml.metadataProviders[" + index + "].organisation");
            $("#metadataProvider-item-" + index + " #metadataProvider-addresss").attr("id", "metadataProvider-" + index + "-addresss");
            $("#metadataProvider-item-" + index + " #plus-metadataProvider-address").attr("id", "plus-metadataProvider-address-" + index);
            $("#plus-metadataProvider-address-" + index).click(function (event) {
                event.preventDefault();
                createNewSubEntityForAgent(event);
            });
            $("#metadataProvider-item-" + index + " [id$='postalCode']").attr("id", "eml.metadataProviders[" + index + "].address.postalCode").attr("name", function () {return $(this).attr("id");});
            $("#metadataProvider-item-" + index + " [for$='postalCode']").attr("for", "eml.metadataProviders[" + index + "].address.postalCode");
            $("#metadataProvider-item-" + index + " [id$='city']").attr("id", "eml.metadataProviders[" + index + "].address.city").attr("name", function () {return $(this).attr("id");});
            $("#metadataProvider-item-" + index + " [for$='city']").attr("for", "eml.metadataProviders[" + index + "].address.city");
            $("#metadataProvider-item-" + index + " [id$='province']").attr("id", "eml.metadataProviders[" + index + "].address.province").attr("name", function () {return $(this).attr("id");});
            $("#metadataProvider-item-" + index + " [for$='province']").attr("for", "eml.metadataProviders[" + index + "].address.province");
            $("#metadataProvider-item-" + index + " [id$='country']").attr("id", "eml.metadataProviders[" + index + "].address.country").attr("name", function () {return $(this).attr("id");});
            $("#metadataProvider-item-" + index + " [for$='country']").attr("for", "eml.metadataProviders[" + index + "].address.country");
            $("#metadataProvider-item-" + index + " [id$='country']").select2({
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

            $("#metadataProvider-item-" + index + " #metadataProvider-phones").attr("id", "metadataProvider-" + index + "-phones");
            $("#metadataProvider-item-" + index + " #plus-metadataProvider-phone").attr("id", "plus-metadataProvider-phone-" + index);
            $("#plus-metadataProvider-phone-" + index).click(function (event) {
                event.preventDefault();
                createNewSubEntityForAgent(event);
            });
            $("#metadataProvider-item-" + index + " #metadataProvider-emails").attr("id", "metadataProvider-" + index + "-emails");
            $("#metadataProvider-item-" + index + " #plus-metadataProvider-email").attr("id", "plus-metadataProvider-email-" + index);
            $("#plus-metadataProvider-email-" + index).click(function (event) {
                event.preventDefault();
                createNewSubEntityForAgent(event);
            });
            $("#metadataProvider-item-" + index + " #metadataProvider-homepages").attr("id", "metadataProvider-" + index + "-homepages");
            $("#metadataProvider-item-" + index + " #plus-metadataProvider-homepage").attr("id", "plus-metadataProvider-homepage-" + index);
            $("#plus-metadataProvider-homepage-" + index).click(function (event) {
                event.preventDefault();
                createNewSubEntityForAgent(event);
            });
            $("#metadataProvider-item-" + index + " #metadataProvider-identifiers").attr("id", "metadataProvider-" + index + "-identifiers");
            $("#metadataProvider-item-" + index + " #plus-metadataProvider-identifier").attr("id", "plus-metadataProvider-identifier-" + index);
            $("#metadataProvider-item-" + index + " [id$='directory']").attr("id", "eml.metadataProviders[" + index + "].userIds[0].directory").attr("name", function () {return $(this).attr("id");});
            $("#metadataProvider-item-" + index + " [for$='directory']").attr("for", "eml.metadataProviders[" + index + "].userIds[0].directory");
            $("#metadataProvider-item-" + index + " [id$='directory']").select2({
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

            $("#metadataProvider-item-" + index + " [id$='identifier']").attr("id", "eml.metadataProviders[" + index + "].userIds[0].identifier").attr("name", function () {return $(this).attr("id");});
            $("#metadataProvider-item-" + index + " [for$='identifier']").attr("for", "eml.metadataProviders[" + index + "].userIds[0].identifier");
            $("#plus-metadataProvider-identifier-" + index).click(function (event) {
                event.preventDefault();
                createNewIdentifierForAgent(event);
            });
        }

        function setAssociatedPartyItemIndex(item, index) {
            item.attr("id", "associatedParty-item-" + index);

            $("#associatedParty-item-" + index + " [id^='associatedParty-removeLink']").attr("id", "associatedParty-removeLink-" + index);
            $("#associatedParty-removeLink-" + index).click(function (event) {
                removeAssociatedPartyItem(event);
            });

            $("#associatedParty-item-" + index + " [id^='associatedParty-copy']").attr("id", "associatedParty-copy-" + index);
            $("#associatedParty-item-" + index + " [id^='associatedParty-from-contact']").attr("id", "associatedParty-from-contact-" + index);
            $("#associatedParty-copy-" + index).click(function (event) {
                event.preventDefault();
                targetItemId = "associatedParty-item-" + index;
                showCopyAgentModal();
            });
            $("#associatedParty-from-contact-" + index).click(function (event) {
                event.preventDefault();
                copyPrimaryContactDetails(event, "associatedParty-item-" + index);
            });

            $("#associatedParty-item-" + index + " [id$='firstName']").attr("id", "eml.associatedParties[" + index + "].firstName").attr("name", function () {return $(this).attr("id");});
            $("#associatedParty-item-" + index + " [for$='firstName']").attr("for", "eml.associatedParties[" + index + "].firstName");
            $("#associatedParty-item-" + index + " [id$='lastName']").attr("id", "eml.associatedParties[" + index + "].lastName").attr("name", function () {return $(this).attr("id");});
            $("#associatedParty-item-" + index + " [for$='lastName']").attr("for", "eml.associatedParties[" + index + "].lastName");
            $("#associatedParty-item-" + index + " [id$='salutation']").attr("id", "eml.associatedParties[" + index + "].salutation").attr("name", function () {return $(this).attr("id");});
            $("#associatedParty-item-" + index + " [for$='salutation']").attr("for", "eml.associatedParties[" + index + "].salutation");
            $("#associatedParty-item-" + index + " #associatedParty-positions").attr("id", "associatedParty-" + index + "-positions");
            $("#associatedParty-item-" + index + " #plus-associatedParty-position").attr("id", "plus-associatedParty-position-" + index);
            $("#plus-associatedParty-position-" + index).click(function (event) {
                event.preventDefault();
                createNewSubEntityForAgent(event);
            });
            $("#associatedParty-item-" + index + " [id$='organisation']").attr("id", "eml.associatedParties[" + index + "].organisation").attr("name", function () {return $(this).attr("id");});
            $("#associatedParty-item-" + index + " [for$='organisation']").attr("for", "eml.associatedParties[" + index + "].organisation");
            $("#associatedParty-item-" + index + " #associatedParty-addresss").attr("id", "associatedParty-" + index + "-addresss");
            $("#associatedParty-item-" + index + " #plus-associatedParty-address").attr("id", "plus-associatedParty-address-" + index);
            $("#plus-associatedParty-address-" + index).click(function (event) {
                event.preventDefault();
                createNewSubEntityForAgent(event);
            });
            $("#associatedParty-item-" + index + " [id$='postalCode']").attr("id", "eml.associatedParties[" + index + "].address.postalCode").attr("name", function () {return $(this).attr("id");});
            $("#associatedParty-item-" + index + " [for$='postalCode']").attr("for", "eml.associatedParties[" + index + "].address.postalCode");
            $("#associatedParty-item-" + index + " [id$='city']").attr("id", "eml.associatedParties[" + index + "].address.city").attr("name", function () {return $(this).attr("id");});
            $("#associatedParty-item-" + index + " [for$='city']").attr("for", "eml.associatedParties[" + index + "].address.city");
            $("#associatedParty-item-" + index + " [id$='province']").attr("id", "eml.associatedParties[" + index + "].address.province").attr("name", function () {return $(this).attr("id");});
            $("#associatedParty-item-" + index + " [for$='province']").attr("for", "eml.associatedParties[" + index + "].address.province");
            $("#associatedParty-item-" + index + " [id$='country']").attr("id", "eml.associatedParties[" + index + "].address.country").attr("name", function () {return $(this).attr("id");});
            $("#associatedParty-item-" + index + " [for$='country']").attr("for", "eml.associatedParties[" + index + "].address.country");
            $("#associatedParty-item-" + index + " [id$='country']").select2({
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
            $("#associatedParty-item-" + index + " #associatedParty-phones").attr("id", "associatedParty-" + index + "-phones");
            $("#associatedParty-item-" + index + " #plus-associatedParty-phone").attr("id", "plus-associatedParty-phone-" + index);
            $("#plus-associatedParty-phone-" + index).click(function (event) {
                event.preventDefault();
                createNewSubEntityForAgent(event);
            });
            $("#associatedParty-item-" + index + " #associatedParty-emails").attr("id", "associatedParty-" + index + "-emails");
            $("#associatedParty-item-" + index + " #plus-associatedParty-email").attr("id", "plus-associatedParty-email-" + index);
            $("#plus-associatedParty-email-" + index).click(function (event) {
                event.preventDefault();
                createNewSubEntityForAgent(event);
            });
            $("#associatedParty-item-" + index + " #associatedParty-homepages").attr("id", "associatedParty-" + index + "-homepages");
            $("#associatedParty-item-" + index + " #plus-associatedParty-homepage").attr("id", "plus-associatedParty-homepage-" + index);
            $("#plus-associatedParty-homepage-" + index).click(function (event) {
                event.preventDefault();
                createNewSubEntityForAgent(event);
            });
            $("#associatedParty-item-" + index + " [id$='role']").attr("id", "eml.associatedParties[" + index + "].role").attr("name", function () {return $(this).attr("id");});
            $("#associatedParty-item-" + index + " [for$='role']").attr("for", "eml.associatedParties[" + index + "].role");
            $("#associatedParty-item-" + index + " [id$='role']").select2({
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
            $("#associatedParty-item-" + index + " #associatedParty-identifiers").attr("id", "associatedParty-" + index + "-identifiers");
            $("#associatedParty-item-" + index + " #plus-associatedParty-identifier").attr("id", "plus-associatedParty-identifier-" + index);
            $("#associatedParty-item-" + index + " [id$='directory']").attr("id", "eml.associatedParties[" + index + "].userIds[0].directory").attr("name", function () {return $(this).attr("id");});
            $("#associatedParty-item-" + index + " [for$='directory']").attr("for", "eml.associatedParties[" + index + "].userIds[0].directory");
            $("#associatedParty-item-" + index + " [id$='directory']").select2({
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
            $("#associatedParty-item-" + index + " [id$='identifier']").attr("id", "eml.associatedParties[" + index + "].userIds[0].identifier").attr("name", function () {return $(this).attr("id");});
            $("#associatedParty-item-" + index + " [for$='identifier']").attr("for", "eml.associatedParties[" + index + "].userIds[0].identifier");
            $("#plus-associatedParty-identifier-" + index).click(function (event) {
                event.preventDefault();
                createNewIdentifierForAgent(event);
            });

            // show/hide "This contact will appear in the citation"
            $('#associatedParty-item-' + index + " [id$='role']").change(function () {
                var selectedValue = $(this).val();
                var selectId = $(this).attr('id');

                if (selectId.includes("role")) {
                    if (selectedValue === 'originator' || selectedValue === 'metadataProvider') {
                        if (index) $('#associatedParty-item-' + index + ' .contact-citation-banner').show();
                    } else {
                        if (index) $('#associatedParty-item-' + index + ' .contact-citation-banner').hide();
                    }
                }
            });
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
                removeRelateProjectPersonnelItem(event);
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
