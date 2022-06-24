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
        calcNumberOfPersonnelItems();

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

        function calcNumberOfPersonnelItems() {
            var lastItem = $("#personnel-items .item:last-child").attr("id");
            if (lastItem !== undefined)
                personnelItemsCount = parseInt(lastItem.split("-")[2]);
            else
                personnelItemsCount = -1;
        }

        $("#plus-contact").click(function (event) {
            event.preventDefault();
            addNewContactItem(true);
        });

        $("#plus-creator").click(function (event) {
            event.preventDefault();
            addNewCreatorItem(true);
        });

        $("#plus-metadataProvider").click(function (event) {
            event.preventDefault();
            addNewMetadataProviderItem(true);
        });

        $("#plus-associatedParty").click(function (event) {
            event.preventDefault();
            addNewAssociatedPartyItem(true);
        });

        $("#plus-personnel").click(function (event) {
            event.preventDefault();
            addNewPersonnelItem(true);
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

        $("[id^='contact-copyDetails']").click(function(event) {
            event.preventDefault();
            copyDetails(event, "contact-item-");
        });

        $("[id^='creator-copyDetails']").click(function(event) {
            event.preventDefault();
            copyDetails(event, "creator-item-");
        });

        $("[id^='metadataProvider-copyDetails']").click(function(event) {
            event.preventDefault();
            copyDetails(event, "metadataProvider-item-");
        });

        $("[id^='associatedParty-copyDetails']").click(function(event) {
            event.preventDefault();
            copyPrimaryContactDetails(event, "associatedParty-item-");
        });

        $("[id^='personnel-copyDetails']").click(function(event) {
            event.preventDefault();
            copyPrimaryContactDetails(event, "personnel-item-");
        });

        function addNewContactItem(effects) {
            var newItem=$('#baseItem-contact').clone();
            if(effects) newItem.hide();
            newItem.appendTo('#contact-items');

            if(effects) {
                newItem.slideDown('slow');
            }

            setContactItemIndex(newItem, ++contactItems);

            initInfoPopovers(newItem[0]);
        }

        function addNewCreatorItem(effects) {
            var newItem=$('#baseItem-creator').clone();
            if(effects) newItem.hide();
            newItem.appendTo('#creator-items');

            if(effects) {
                newItem.slideDown('slow');
            }

            setCreatorItemIndex(newItem, ++creatorItems);

            initInfoPopovers(newItem[0]);
        }

        function addNewMetadataProviderItem(effects) {
            var newItem=$('#baseItem-metadataProvider').clone();
            if(effects) newItem.hide();
            newItem.appendTo('#metadataProvider-items');

            if(effects) {
                newItem.slideDown('slow');
            }

            setMetadataProviderItemIndex(newItem, ++metadataProviderItems);

            initInfoPopovers(newItem[0]);
        }

        function addNewAssociatedPartyItem(effects){
            var newItem=$('#baseItem-associatedParty').clone();
            if(effects) newItem.hide();
            newItem.appendTo('#associatedParty-items');

            if(effects) {
                newItem.slideDown('slow');
            }

            setAssociatedPartyItemIndex(newItem, ++associatedPartyItemsCount);

            initInfoPopovers(newItem[0]);
        }

        function addNewPersonnelItem(effects){
            var newItem=$('#baseItem-personnel').clone();
            if(effects) newItem.hide();
            newItem.appendTo('#personnel-items');

            if(effects) {
                newItem.slideDown('slow');
            }

            setPersonnelItemIndex(newItem, ++personnelItemsCount);

            initInfoPopovers(newItem[0]);
        }

        function removeContactItem(event) {
            event.preventDefault();
            var $target = $(event.target);
            if (!$target.is('a')) {
                $target = $(event.target).closest('a');
            }
            $('#contact-item-'+$target.attr("id").split("-")[2]).slideUp('slow', function() {
                $(this).remove();
                $("#contact-items .item").each(function(index) {
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

        function removeMetadataProviderItem(event){
            event.preventDefault();
            var $target = $(event.target);
            if (!$target.is('a')) {
                $target = $(event.target).closest('a');
            }
            $('#metadataProvider-item-'+$target.attr("id").split("-")[2]).slideUp('slow', function() {
                $(this).remove();
                $("#metadataProvider-items .item").each(function(index) {
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
            $('#associatedParty-item-'+$target.attr("id").split("-")[2]).slideUp('slow', function() {
                $(this).remove();
                $("#associatedParty-items .item").each(function(index) {
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
            $('#personnel-item-'+$target.attr("id").split("-")[2]).slideUp('slow', function() {
                $(this).remove();
                $("#personnel-items .item").each(function(index) {
                    setPersonnelItemIndex($(this), index);
                });
                calcNumberOfPersonnelItems();
            });
        }

        function copyDetails(event, idPrefix) {
            event.preventDefault();
            var $target = $(event.target);
            if (!$target.is('a')) {
                $target = $(event.target).closest('a');
            }

            var index = $target.attr("id").split("-")[2];
            $("#" + idPrefix + index + " [id$='firstName']").val($("#eml\\.contacts\\[0\\]\\.firstName").val());
            $("#" + idPrefix + index + " [id$='lastName']").val($("#eml\\.contacts\\[0\\]\\.lastName").val());
            $("#" + idPrefix + index + " [id$='position']").val($("#eml\\.contacts\\[0\\]\\.position").val());
            $("#" + idPrefix + index + " [id$='organisation']").val($("#eml\\.contacts\\[0\\]\\.organisation").val());
            $("#" + idPrefix + index + " [id$='address']").val($("#eml\\.contacts\\[0\\]\\.address\\.address").val());
            $("#" + idPrefix + index + " [id$='city']").val($("#eml\\.contacts\\[0\\]\\.address\\.city").val());
            $("#" + idPrefix + index + " [id$='province']").val($("#eml\\.contacts\\[0\\]\\.address\\.province").val());
            $("#" + idPrefix + index + " [id$='postalCode']").val($("#eml\\.contacts\\[0\\]\\.address\\.postalCode").val());
            $("#" + idPrefix + index + " [id$='country']").val($("#eml\\.contacts\\[0\\]\\.address\\.country").val());
            $("#" + idPrefix + index + " [id$='phone']").val($("#eml\\.contacts\\[0\\]\\.phone").val());
            $("#" + idPrefix + index + " [id$='email']").val($("#eml\\.contacts\\[0\\]\\.email").val());
            $("#" + idPrefix + index + " [id$='homepage']").val($("#eml\\.contacts\\[0\\]\\.homepage").val());
            $("#" + idPrefix + index + " [id$='directory']").val($("#eml\\.contacts\\[0\\]\\.userIds\\[0\\]\\.directory").val());
            $("#" + idPrefix + index + " [id$='identifier']").val($("#eml\\.contacts\\[0\\]\\.userIds\\[0\\]\\.identifier").val());
        }

        function copyPrimaryContactDetails(event, idPrefix) {
            event.preventDefault();
            var $target = $(event.target);
            if (!$target.is('a')) {
                $target = $(event.target).closest('a');
            }

            var index = $target.attr("id").split("-")[2];
            // replace " with &quot; to prevent JS from failing
            $("#" + idPrefix + index + " [id$='firstName']").val("${primaryContact.firstName!?replace("\"", "&quot;")}");
            $("#" + idPrefix + index + " [id$='lastName']").val("${primaryContact.lastName!?replace("\"", "&quot;")}");
            $("#" + idPrefix + index + " [id$='position']").val("${primaryContact.position!?replace("\"", "&quot;")}");
            $("#" + idPrefix + index + " [id$='organisation']").val("${primaryContact.organisation!?replace("\"", "&quot;")}");
            $("#" + idPrefix + index + " [id$='address']").val("${primaryContact.address.address!?replace("\"", "&quot;")}");
            $("#" + idPrefix + index + " [id$='city']").val("${primaryContact.address.city!?replace("\"", "&quot;")}");
            $("#" + idPrefix + index + " [id$='province']").val("${primaryContact.address.province!?replace("\"", "&quot;")}");
            $("#" + idPrefix + index + " [id$='postalCode']").val("${primaryContact.address.postalCode!?replace("\"", "&quot;")}");
            $("#" + idPrefix + index + " [id$='country']").val("${primaryContact.address.country!?replace("\"", "&quot;")}");
            $("#" + idPrefix + index + " [id$='phone']").val("${primaryContact.phone!?replace("\"", "&quot;")}");
            $("#" + idPrefix + index + " [id$='email']").val("${primaryContact.email!?replace("\"", "&quot;")}");
            $("#" + idPrefix + index + " [id$='homepage']").val("${primaryContact.homepage!?replace("\"", "&quot;")}");
            $("#" + idPrefix + index + " [id$='directory']").val("${primaryContact.userIds[0].directory!?replace("\"", "&quot;")}");
            $("#" + idPrefix + index + " [id$='identifier']").val("${primaryContact.userIds[0].identifier!?replace("\"", "&quot;")}");
        }

        function setContactItemIndex(item, index) {
            item.attr("id","contact-item-"+index);

            $("#contact-item-"+index+" [id^='contact-removeLink']").attr("id", "contact-removeLink-"+index);
            $("#contact-removeLink-"+index).click(function(event) {
                removeContactItem(event);
            });

            $("#contact-item-"+index+" [id^='contact-copyDetails']").attr("id", "contact-copyDetails-"+index);
            $("#contact-copyDetails-"+index).click(function(event) {
                copyDetails(event, "contact-item-");
            });

            $("#contact-item-"+index+" [id$='firstName']").attr("id", "eml.contacts["+index+"].firstName").attr("name", function() {return $(this).attr("id");});
            $("#contact-item-"+index+" [for$='firstName']").attr("for", "eml.contacts["+index+"].firstName");
            $("#contact-item-"+index+" [id$='lastName']").attr("id", "eml.contacts["+index+"].lastName").attr("name", function() {return $(this).attr("id");});
            $("#contact-item-"+index+" [for$='lastName']").attr("for", "eml.contacts["+index+"].lastName");
            $("#contact-item-"+index+" [id$='position']").attr("id", "eml.contacts["+index+"].position").attr("name", function() {return $(this).attr("id");});
            $("#contact-item-"+index+" [for$='position']").attr("for", "eml.contacts["+index+"].position");
            $("#contact-item-"+index+" [id$='organisation']").attr("id", "eml.contacts["+index+"].organisation").attr("name", function() {return $(this).attr("id");});
            $("#contact-item-"+index+" [for$='organisation']").attr("for", "eml.contacts["+index+"].organisation");
            $("#contact-item-"+index+" [id$='address']").attr("id", "eml.contacts["+index+"].address.address").attr("name", function() {return $(this).attr("id");});
            $("#contact-item-"+index+" [for$='address']").attr("for", "eml.contacts["+index+"].address.address");
            $("#contact-item-"+index+" [id$='postalCode']").attr("id", "eml.contacts["+index+"].address.postalCode").attr("name", function() {return $(this).attr("id");});
            $("#contact-item-"+index+" [for$='postalCode']").attr("for", "eml.contacts["+index+"].address.postalCode");
            $("#contact-item-"+index+" [id$='city']").attr("id", "eml.contacts["+index+"].address.city").attr("name", function() {return $(this).attr("id");});
            $("#contact-item-"+index+" [for$='city']").attr("for", "eml.contacts["+index+"].address.city");
            $("#contact-item-"+index+" [id$='province']").attr("id", "eml.contacts["+index+"].address.province").attr("name", function() {return $(this).attr("id");});
            $("#contact-item-"+index+" [for$='province']").attr("for", "eml.contacts["+index+"].address.province");
            $("#contact-item-"+index+" [id$='country']").attr("id", "eml.contacts["+index+"].address.country").attr("name", function() {return $(this).attr("id");});
            $("#contact-item-"+index+" [for$='country']").attr("for", "eml.contacts["+index+"].address.country");
            $("#contact-item-"+index+" [id$='phone']").attr("id", "eml.contacts["+index+"].phone").attr("name", function() {return $(this).attr("id");});
            $("#contact-item-"+index+" [for$='phone']").attr("for", "eml.contacts["+index+"].phone");
            $("#contact-item-"+index+" [id$='email']").attr("id", "eml.contacts["+index+"].email").attr("name", function() {return $(this).attr("id");});
            $("#contact-item-"+index+" [for$='email']").attr("for", "eml.contacts["+index+"].email");
            $("#contact-item-"+index+" [id$='homepage']").attr("id", "eml.contacts["+index+"].homepage").attr("name", function() {return $(this).attr("id");});
            $("#contact-item-"+index+" [for$='homepage']").attr("for", "eml.contacts["+index+"].homepage");
            $("#contact-item-"+index+" [id$='directory']").attr("id", "eml.contacts["+index+"].userIds[0].directory").attr("name", function() {return $(this).attr("id");});
            $("#contact-item-"+index+" [for$='directory']").attr("for", "eml.contacts["+index+"].userIds[0].directory");
            $("#contact-item-"+index+" [id$='identifier']").attr("id", "eml.contacts["+index+"].userIds[0].identifier").attr("name", function() {return $(this).attr("id");});
            $("#contact-item-"+index+" [for$='identifier']").attr("for", "eml.contacts["+index+"].userIds[0].identifier");
        }

        function setCreatorItemIndex(item, index) {
            item.attr("id","creator-item-"+index);

            $("#creator-item-"+index+" [id^='creator-removeLink']").attr("id", "creator-removeLink-"+index);
            $("#creator-removeLink-"+index).click(function(event) {
                removeCreatorItem(event);
            });

            $("#creator-item-"+index+" [id^='creator-copyDetails']").attr("id", "creator-copyDetails-"+index);
            $("#creator-copyDetails-"+index).click(function(event) {
                copyDetails(event, "creator-item-");
            });

            $("#creator-item-"+index+" [id$='firstName']").attr("id", "eml.creators["+index+"].firstName").attr("name", function() {return $(this).attr("id");});
            $("#creator-item-"+index+" [for$='firstName']").attr("for", "eml.creators["+index+"].firstName");
            $("#creator-item-"+index+" [id$='lastName']").attr("id", "eml.creators["+index+"].lastName").attr("name", function() {return $(this).attr("id");});
            $("#creator-item-"+index+" [for$='lastName']").attr("for", "eml.creators["+index+"].lastName");
            $("#creator-item-"+index+" [id$='position']").attr("id", "eml.creators["+index+"].position").attr("name", function() {return $(this).attr("id");});
            $("#creator-item-"+index+" [for$='position']").attr("for", "eml.creators["+index+"].position");
            $("#creator-item-"+index+" [id$='organisation']").attr("id", "eml.creators["+index+"].organisation").attr("name", function() {return $(this).attr("id");});
            $("#creator-item-"+index+" [for$='organisation']").attr("for", "eml.creators["+index+"].organisation");
            $("#creator-item-"+index+" [id$='address']").attr("id", "eml.creators["+index+"].address.address").attr("name", function() {return $(this).attr("id");});
            $("#creator-item-"+index+" [for$='address']").attr("for", "eml.creators["+index+"].address.address");
            $("#creator-item-"+index+" [id$='postalCode']").attr("id", "eml.creators["+index+"].address.postalCode").attr("name", function() {return $(this).attr("id");});
            $("#creator-item-"+index+" [for$='postalCode']").attr("for", "eml.creators["+index+"].address.postalCode");
            $("#creator-item-"+index+" [id$='city']").attr("id", "eml.creators["+index+"].address.city").attr("name", function() {return $(this).attr("id");});
            $("#creator-item-"+index+" [for$='city']").attr("for", "eml.creators["+index+"].address.city");
            $("#creator-item-"+index+" [id$='province']").attr("id", "eml.creators["+index+"].address.province").attr("name", function() {return $(this).attr("id");});
            $("#creator-item-"+index+" [for$='province']").attr("for", "eml.creators["+index+"].address.province");
            $("#creator-item-"+index+" [id$='country']").attr("id", "eml.creators["+index+"].address.country").attr("name", function() {return $(this).attr("id");});
            $("#creator-item-"+index+" [for$='country']").attr("for", "eml.creators["+index+"].address.country");
            $("#creator-item-"+index+" [id$='phone']").attr("id", "eml.creators["+index+"].phone").attr("name", function() {return $(this).attr("id");});
            $("#creator-item-"+index+" [for$='phone']").attr("for", "eml.creators["+index+"].phone");
            $("#creator-item-"+index+" [id$='email']").attr("id", "eml.creators["+index+"].email").attr("name", function() {return $(this).attr("id");});
            $("#creator-item-"+index+" [for$='email']").attr("for", "eml.creators["+index+"].email");
            $("#creator-item-"+index+" [id$='homepage']").attr("id", "eml.creators["+index+"].homepage").attr("name", function() {return $(this).attr("id");});
            $("#creator-item-"+index+" [for$='homepage']").attr("for", "eml.creators["+index+"].homepage");
            $("#creator-item-"+index+" [id$='directory']").attr("id", "eml.creators["+index+"].userIds[0].directory").attr("name", function() {return $(this).attr("id");});
            $("#creator-item-"+index+" [for$='directory']").attr("for", "eml.creators["+index+"].userIds[0].directory");
            $("#creator-item-"+index+" [id$='identifier']").attr("id", "eml.creators["+index+"].userIds[0].identifier").attr("name", function() {return $(this).attr("id");});
            $("#creator-item-"+index+" [for$='identifier']").attr("for", "eml.creators["+index+"].userIds[0].identifier");
        }

        function setMetadataProviderItemIndex(item, index) {
            item.attr("id","metadataProvider-item-"+index);

            $("#metadataProvider-item-"+index+" [id^='metadataProvider-removeLink']").attr("id", "metadataProvider-removeLink-"+index);
            $("#metadataProvider-removeLink-"+index).click(function(event) {
                removeMetadataProviderItem(event);
            });

            $("#metadataProvider-item-"+index+" [id^='metadataProvider-copyDetails']").attr("id", "metadataProvider-copyDetails-"+index);
            $("#metadataProvider-copyDetails-"+index).click(function(event) {
                copyDetails(event, "metadataProvider-item-");
            });

            $("#metadataProvider-item-"+index+" [id$='firstName']").attr("id", "eml.metadataProviders["+index+"].firstName").attr("name", function() {return $(this).attr("id");});
            $("#metadataProvider-item-"+index+" [for$='firstName']").attr("for", "eml.metadataProviders["+index+"].firstName");
            $("#metadataProvider-item-"+index+" [id$='lastName']").attr("id", "eml.metadataProviders["+index+"].lastName").attr("name", function() {return $(this).attr("id");});
            $("#metadataProvider-item-"+index+" [for$='lastName']").attr("for", "eml.metadataProviders["+index+"].lastName");
            $("#metadataProvider-item-"+index+" [id$='position']").attr("id", "eml.metadataProviders["+index+"].position").attr("name", function() {return $(this).attr("id");});
            $("#metadataProvider-item-"+index+" [for$='position']").attr("for", "eml.metadataProviders["+index+"].position");
            $("#metadataProvider-item-"+index+" [id$='organisation']").attr("id", "eml.metadataProviders["+index+"].organisation").attr("name", function() {return $(this).attr("id");});
            $("#metadataProvider-item-"+index+" [for$='organisation']").attr("for", "eml.metadataProviders["+index+"].organisation");
            $("#metadataProvider-item-"+index+" [id$='address']").attr("id", "eml.metadataProviders["+index+"].address.address").attr("name", function() {return $(this).attr("id");});
            $("#metadataProvider-item-"+index+" [for$='address']").attr("for", "eml.metadataProviders["+index+"].address.address");
            $("#metadataProvider-item-"+index+" [id$='postalCode']").attr("id", "eml.metadataProviders["+index+"].address.postalCode").attr("name", function() {return $(this).attr("id");});
            $("#metadataProvider-item-"+index+" [for$='postalCode']").attr("for", "eml.metadataProviders["+index+"].address.postalCode");
            $("#metadataProvider-item-"+index+" [id$='city']").attr("id", "eml.metadataProviders["+index+"].address.city").attr("name", function() {return $(this).attr("id");});
            $("#metadataProvider-item-"+index+" [for$='city']").attr("for", "eml.metadataProviders["+index+"].address.city");
            $("#metadataProvider-item-"+index+" [id$='province']").attr("id", "eml.metadataProviders["+index+"].address.province").attr("name", function() {return $(this).attr("id");});
            $("#metadataProvider-item-"+index+" [for$='province']").attr("for", "eml.metadataProviders["+index+"].address.province");
            $("#metadataProvider-item-"+index+" [id$='country']").attr("id", "eml.metadataProviders["+index+"].address.country").attr("name", function() {return $(this).attr("id");});
            $("#metadataProvider-item-"+index+" [for$='country']").attr("for", "eml.metadataProviders["+index+"].address.country");
            $("#metadataProvider-item-"+index+" [id$='phone']").attr("id", "eml.metadataProviders["+index+"].phone").attr("name", function() {return $(this).attr("id");});
            $("#metadataProvider-item-"+index+" [for$='phone']").attr("for", "eml.metadataProviders["+index+"].phone");
            $("#metadataProvider-item-"+index+" [id$='email']").attr("id", "eml.metadataProviders["+index+"].email").attr("name", function() {return $(this).attr("id");});
            $("#metadataProvider-item-"+index+" [for$='email']").attr("for", "eml.metadataProviders["+index+"].email");
            $("#metadataProvider-item-"+index+" [id$='homepage']").attr("id", "eml.metadataProviders["+index+"].homepage").attr("name", function() {return $(this).attr("id");});
            $("#metadataProvider-item-"+index+" [for$='homepage']").attr("for", "eml.metadataProviders["+index+"].homepage");
            $("#metadataProvider-item-"+index+" [id$='directory']").attr("id", "eml.metadataProviders["+index+"].userIds[0].directory").attr("name", function() {return $(this).attr("id");});
            $("#metadataProvider-item-"+index+" [for$='directory']").attr("for", "eml.metadataProviders["+index+"].userIds[0].directory");
            $("#metadataProvider-item-"+index+" [id$='identifier']").attr("id", "eml.metadataProviders["+index+"].userIds[0].identifier").attr("name", function() {return $(this).attr("id");});
            $("#metadataProvider-item-"+index+" [for$='identifier']").attr("for", "eml.metadataProviders["+index+"].userIds[0].identifier");
        }

        function setAssociatedPartyItemIndex(item, index) {
            item.attr("id","associatedParty-item-"+index);

            $("#associatedParty-item-"+index+" [id^='associatedParty-removeLink']").attr("id", "associatedParty-removeLink-"+index);
            $("#associatedParty-removeLink-"+index).click(function(event) {
                removeAssociatedPartyItem(event);
            });

            $("#associatedParty-item-"+index+" [id^='associatedParty-copyDetails']").attr("id", "associatedParty-copyDetails-"+index);
            $("#associatedParty-copyDetails-"+index).click(function(event) {
                copyPrimaryContactDetails(event, "associatedParty-item-");
            });

            $("#associatedParty-item-"+index+" [id$='firstName']").attr("id", "eml.associatedParties["+index+"].firstName").attr("name", function() {return $(this).attr("id");});
            $("#associatedParty-item-"+index+" [for$='firstName']").attr("for", "eml.associatedParties["+index+"].firstName");
            $("#associatedParty-item-"+index+" [id$='lastName']").attr("id", "eml.associatedParties["+index+"].lastName").attr("name", function() {return $(this).attr("id");});
            $("#associatedParty-item-"+index+" [for$='lastName']").attr("for", "eml.associatedParties["+index+"].lastName");
            $("#associatedParty-item-"+index+" [id$='position']").attr("id", "eml.associatedParties["+index+"].position").attr("name", function() {return $(this).attr("id");});
            $("#associatedParty-item-"+index+" [for$='position']").attr("for", "eml.associatedParties["+index+"].position");
            $("#associatedParty-item-"+index+" [id$='organisation']").attr("id", "eml.associatedParties["+index+"].organisation").attr("name", function() {return $(this).attr("id");});
            $("#associatedParty-item-"+index+" [for$='organisation']").attr("for", "eml.associatedParties["+index+"].organisation");
            $("#associatedParty-item-"+index+" [id$='address']").attr("id", "eml.associatedParties["+index+"].address.address").attr("name", function() {return $(this).attr("id");});
            $("#associatedParty-item-"+index+" [for$='address']").attr("for", "eml.associatedParties["+index+"].address.address");
            $("#associatedParty-item-"+index+" [id$='postalCode']").attr("id", "eml.associatedParties["+index+"].address.postalCode").attr("name", function() {return $(this).attr("id");});
            $("#associatedParty-item-"+index+" [for$='postalCode']").attr("for", "eml.associatedParties["+index+"].address.postalCode");
            $("#associatedParty-item-"+index+" [id$='city']").attr("id", "eml.associatedParties["+index+"].address.city").attr("name", function() {return $(this).attr("id");});
            $("#associatedParty-item-"+index+" [for$='city']").attr("for", "eml.associatedParties["+index+"].address.city");
            $("#associatedParty-item-"+index+" [id$='province']").attr("id", "eml.associatedParties["+index+"].address.province").attr("name", function() {return $(this).attr("id");});
            $("#associatedParty-item-"+index+" [for$='province']").attr("for", "eml.associatedParties["+index+"].address.province");
            $("#associatedParty-item-"+index+" [id$='country']").attr("id", "eml.associatedParties["+index+"].address.country").attr("name", function() {return $(this).attr("id");});
            $("#associatedParty-item-"+index+" [for$='country']").attr("for", "eml.associatedParties["+index+"].address.country");
            $("#associatedParty-item-"+index+" [id$='phone']").attr("id", "eml.associatedParties["+index+"].phone").attr("name", function() {return $(this).attr("id");});
            $("#associatedParty-item-"+index+" [for$='phone']").attr("for", "eml.associatedParties["+index+"].phone");
            $("#associatedParty-item-"+index+" [id$='email']").attr("id", "eml.associatedParties["+index+"].email").attr("name", function() {return $(this).attr("id");});
            $("#associatedParty-item-"+index+" [for$='email']").attr("for", "eml.associatedParties["+index+"].email");
            $("#associatedParty-item-"+index+" [id$='homepage']").attr("id", "eml.associatedParties["+index+"].homepage").attr("name", function() {return $(this).attr("id");});
            $("#associatedParty-item-"+index+" [for$='homepage']").attr("for", "eml.associatedParties["+index+"].homepage");
            $("#associatedParty-item-"+index+" [id$='role']").attr("id", "eml.associatedParties["+index+"].role").attr("name", function() {return $(this).attr("id");});
            $("#associatedParty-item-"+index+" [for$='role']").attr("for", "eml.associatedParties["+index+"].role");
            $("#associatedParty-item-"+index+" [id$='directory']").attr("id", "eml.associatedParties["+index+"].userIds[0].directory").attr("name", function() {return $(this).attr("id");});
            $("#associatedParty-item-"+index+" [for$='directory']").attr("for", "eml.associatedParties["+index+"].userIds[0].directory");
            $("#associatedParty-item-"+index+" [id$='identifier']").attr("id", "eml.associatedParties["+index+"].userIds[0].identifier").attr("name", function() {return $(this).attr("id");});
            $("#associatedParty-item-"+index+" [for$='identifier']").attr("for", "eml.associatedParties["+index+"].userIds[0].identifier");
        }

        function setPersonnelItemIndex(item, index) {
            item.attr("id","personnel-item-"+index);

            $("#personnel-item-"+index+" [id^='personnel-removeLink']").attr("id", "personnel-removeLink-"+index);
            $("#personnel-removeLink-"+index).click(function(event) {
                removePersonnelItem(event);
            });

            $("#personnel-item-"+index+" [id^='personnel-copyDetails']").attr("id", "personnel-copyDetails-"+index);
            $("#personnel-copyDetails-"+index).click(function(event) {
                copyPrimaryContactDetails(event, "personnel-item-");
            });

            $("#personnel-item-"+index+" [id$='firstName']").attr("id", "eml.project.personnel["+index+"].firstName").attr("name", function() {return $(this).attr("id");});
            $("#personnel-item-"+index+" [for$='firstName']").attr("for", "eml.project.personnel["+index+"].firstName");
            $("#personnel-item-"+index+" [id$='lastName']").attr("id", "eml.project.personnel["+index+"].lastName").attr("name", function() {return $(this).attr("id");});
            $("#personnel-item-"+index+" [for$='lastName']").attr("for", "eml.project.personnel["+index+"].lastName");
            $("#personnel-item-"+index+" [id$='role']").attr("id", "eml.project.personnel["+index+"].role").attr("name", function() {return $(this).attr("id");});
            $("#personnel-item-"+index+" [for$='role']").attr("for", "eml.project.personnel["+index+"].role");
            $("#personnel-item-"+index+" [id$='directory']").attr("id", "eml.project.personnel["+index+"].userIds[0].directory").attr("name", function() {return $(this).attr("id");});
            $("#personnel-item-"+index+" [for$='directory']").attr("for", "eml.project.personnel["+index+"].userIds[0].directory");
            $("#personnel-item-"+index+" [id$='identifier']").attr("id", "eml.project.personnel["+index+"].userIds[0].identifier").attr("name", function() {return $(this).attr("id");});
            $("#personnel-item-"+index+" [for$='identifier']").attr("for", "eml.project.personnel["+index+"].userIds[0].identifier");
        }

});
</script>
