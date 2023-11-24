<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.OverviewAction" -->
<#escape x as x?html>

<#macro dwcaValidator>
    <#if (resource.recordsPublished>0)>
        <a href="https://tools.gbif.org/dwca-validator/?archiveUrl=${baseURL}/archive.do?r=${resource.shortname}" title="<@s.text name="manage.overview.publishing.validator"/>" target="_blank" class="icon icon-validate">
            <i class="bi bi-check2 text-gbif-primary"></i>
        </a>
    </#if>
</#macro>

<#macro nextDoiButtonTD>

    <!-- The organisation with DOI account activated must exist,
    the mandatory metadata must have been filled in,
    and the user must have registration rights for any DOI operation made possible -->
    <#if resource.identifierStatus == "UNRESERVED">
        <form action='resource-reserveDoi.do' method='post'>
            <input name="r" type="hidden" value="${resource.shortname}"/>
            <@s.submit cssClass="confirmReserveDoi btn btn-outline-gbif-primary my-3" name="reserveDoi" key="button.reserve" disabled="${missingMetadata?string}"/>
        </form>
    <#elseif resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION">
        <form action='resource-deleteDoi.do' method='post'>
            <input name="r" type="hidden" value="${resource.shortname}"/>
            <@s.submit cssClass="confirmDeleteDoi btn btn-outline-gbif-danger my-3" name="deleteDoi" key="button.delete" disabled="${missingMetadata?string}"/>
        </form>
    <#elseif resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi() >
        <form action='resource-reserveDoi.do' method='post'>
            <input name="r" type="hidden" value="${resource.shortname}"/>
            <@s.submit cssClass="confirmReserveDoi btn btn-outline-gbif-primary my-3" name="reserveDoi" key="button.reserve.new" disabled="${missingMetadata?string}"/>
        </form>
    </#if>
</#macro>

<#macro description text maxLength>
    <#if (text?length>maxLength)>
        ${(text)?substring(0,maxLength)}...
    <#else>
        ${(text)}
    </#if>
</#macro>

<!-- The short form of the license for display in the versions table -->
<#macro shortLicense licenseUrl="">
    <#if licenseUrl == "http://creativecommons.org/publicdomain/zero/1.0/legalcode">
        CC0 1.0
    <#elseif licenseUrl == "http://creativecommons.org/licenses/by/4.0/legalcode">
        CC-BY 4.0
    <#elseif licenseUrl == "http://creativecommons.org/licenses/by-nc/4.0/legalcode">
        CC-BY-NC 4.0
    <#elseif licenseUrl == "http://www.opendatacommons.org/licenses/pddl/1.0">
        ODC PDDL 1.0
    <#elseif licenseUrl == "http://www.opendatacommons.org/licenses/by/1.0">
        ODC-By 1.0
    <#elseif licenseUrl?has_content>
        <@s.text name='manage.overview.noGBIFLicense'/>
    <#else>
        -
    </#if>
</#macro>
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="manage.overview.title"/>: ${resource.title!resource.shortname}</title>

<script src="${baseURL}/js/jconfirmation.jquery.js"></script>
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
<script src="${baseURL}/js/select2/select2-4.0.13.full.min.js"></script>

<#assign currentLocale = .vars["locale"]!"en"/>

<script>
    $(document).ready(function(){
        try {
            var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
            var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
                return new bootstrap.Tooltip(tooltipTriggerEl)
            })
        } catch (err) {
            console.log("Failed to initialize tooltips")
        }

        $(".source-item-link").click(function (e) {
            e.preventDefault();
            openSourceDetails(e);
        });

        function openSourceDetails(e) {
            var resource = e.currentTarget.attributes["data-ipt-resource"].nodeValue;
            var source = e.currentTarget.attributes["data-ipt-source"].nodeValue;
            location.href = 'source.do?r=' + resource + '&id=' + source;
        }

        $(".mapping-item-link").click(function (e) {
            e.preventDefault();
            displayProcessing();
            openMappingDetails(e);
        });

        function openMappingDetails(e) {
            var resource = e.currentTarget.attributes["data-ipt-resource"].nodeValue;
            var extension = e.currentTarget.attributes["data-ipt-extension"].nodeValue;
            var mapping = e.currentTarget.attributes["data-ipt-mapping"].nodeValue;
            location.href = 'mapping.do?r=' + resource + '&id=' + extension + '&mid=' + mapping;
        }

        $(".schema-mapping-item-link").click(function (e) {
            e.preventDefault();
            displayProcessing();
            openSchemaMappingDetails(e);
        });

        function openSchemaMappingDetails(e) {
            var resource = e.currentTarget.attributes["data-ipt-resource"].nodeValue;
            var extension = e.currentTarget.attributes["data-ipt-extension"].nodeValue;
            var mapping = e.currentTarget.attributes["data-ipt-mapping"].nodeValue;
            location.href = 'schemaMapping.do?r=' + resource + '&id=' + extension + '&mid=' + mapping;
        }

        $(".network-item-link").click(function (e) {
            e.preventDefault();
            openNetworkDetails(e);
        });

        function openNetworkDetails(e) {
            var networkKey = e.currentTarget.attributes["data-ipt-network-key"].nodeValue;
            location.href = '${cfg.portalUrl}/network/' + networkKey;
        }

        $(".registration-item-link").click(function (e) {
            e.preventDefault();
            openRegistrationDetails(e);
        });

        function openRegistrationDetails(e) {
            location.href = '${cfg.portalUrl}/dataset/${resource.key!}';
        }

        $(".published-version-item-link").click(function (e) {
            e.preventDefault();
            displayProcessing();
            openPublishedVersionDetails(e);
        });

        function openPublishedVersionDetails(e) {
            location.href = '${baseURL}/resource?r=${resource.shortname}';
        }

        $(".next-version-item-link").click(function (e) {
            e.preventDefault();
            displayProcessing();
            openNextVersionDetails(e);
        });

        function openNextVersionDetails(e) {
            location.href = '${baseURL}/resource/preview?r=${resource.shortname}';
        }

        <#if resource.status=="PRIVATE">
            <#assign resourceVisibility="private" />
        <#elseif resource.status=="PUBLIC" || resource.status=="REGISTERED" || resource.status=="DELETED">
            <#assign resourceVisibility="public" />
        </#if>

        $('.confirm').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", buttonType: "primary"});
        $('.confirmRegistration').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.visibility.confirm.registration'/> <@s.text name='manage.resource.delete.confirm.registered'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", checkboxText: "<@s.text name='manage.overview.visibility.confirm.agreement'/>", buttonType: "primary", processing: true});
        $('.confirmMakePrivate').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.visibility.confirm.make.private'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", buttonType: "primary"});
        $('.confirmEmlReplace').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.metadata.replace.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", buttonType: "primary"});
        $('.confirmDatapackageMetadataReplace').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.metadata.replace.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", buttonType: "primary"});
        $('.confirmDeletionFromIptAndGbif').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<#if resource.isAlreadyAssignedDoi()><@s.text name='manage.resource.delete.confirm.doi'/></br></br></#if><#if resource.status=='REGISTERED'><@s.text name='manage.resource.delete.fromIptAndGbif.confirm.registered'/></br></br></#if><@s.text name='manage.resource.delete.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>"});
        $('.confirmDeletionFromIptOnly').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<#if resource.isAlreadyAssignedDoi()><@s.text name='manage.resource.delete.confirm.doi'/></br></br></#if><#if resource.status=='REGISTERED'><@s.text name='manage.resource.delete.fromIptOnly.confirm.registered'/></br></br></#if><@s.text name='manage.resource.delete.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>"});
        $('.confirmUndeletion').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.resource.undoDelete.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", buttonType: "primary"});

        $('.confirmReserveDoi').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.publishing.doi.reserve.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", buttonType: "primary"});
        $('.confirmDeleteDoi').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.publishing.doi.delete.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>"});
        $('.confirmPublishMinorVersion').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.publishing.doi.minorVersion.confirm'><@s.param>${resourceVisibility}</@s.param><@s.param>${resourceVisibility}</@s.param></@s.text>", yesAnswer : "<@s.text name='button.publish'/>", summary : "<@s.text name='manage.overview.publishing.doi.summary.placeholder'/>", cancelAnswer : "<@s.text name='button.cancel'/>", buttonType: "primary"});
        $('.confirmPublishMajorVersion').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.publishing.doi.majorVersion.confirm'><@s.param>${resourceVisibility}</@s.param><@s.param>${resourceVisibility}</@s.param></@s.text>", yesAnswer : "<@s.text name='button.publish'/>", summary : "<@s.text name='manage.overview.publishing.doi.summary.placeholder'/>", cancelAnswer : "<@s.text name='button.cancel'/>", checkboxText: "<@s.text name='manage.overview.publishing.doi.register.agreement'/>", buttonType: "primary"});
        $('.confirmPublishMajorVersionWithoutDOI').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.publishing.withoutDoi.majorVersion.confirm'><@s.param>${resourceVisibility}</@s.param><@s.param>${resourceVisibility}</@s.param></@s.text>", yesAnswer : "<@s.text name='button.publish'/>", summary : "<@s.text name='manage.overview.publishing.doi.summary.placeholder'/>", cancelAnswer : "<@s.text name='button.cancel'/>", buttonType: "primary"});

        $('.delete-source').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name="manage.source.confirmation.message"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>", buttonType: "danger"});
        $('.delete-mapping').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name ="manage.mapping.confirmation.message"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>", buttonType: "danger"});

        // spy scroll and manage sidebar menu
        $(window).scroll(function () {
            var scrollPosition = $(document).scrollTop();

            $('.bd-toc nav a').each(function () {
                var currentLink = $(this);
                var anchor = $(currentLink.attr("href"));
                var sectionId = anchor[0].id.replace("anchor-", "");
                var section = $("#" + sectionId);

                var sectionsContainer = $("#sections");

                if (sectionsContainer.position().top - 50 > scrollPosition) {
                    var removeActiveFromThisLink = $('.bd-toc nav a.active');
                    removeActiveFromThisLink.removeClass('active');
                } else if (section.position().top - 50 <= scrollPosition
                    && section.position().top + section.height() > scrollPosition) {
                    if (!currentLink.hasClass("active")) {
                        var removeFromThisLink = $('.bd-toc nav a.active');
                        removeFromThisLink.removeClass('active');
                        $(this).addClass('active');
                    }
                }
            });
        })

        var showReport = false;
        $("#toggleReport").click(function () {
            if (showReport) {
                showReport = false;
                $("#toggleReport-text").text("<@s.text name='manage.overview.published.report.show'/>");
                $('#dwcaReport').fadeOut();
            } else {
                showReport = true;
                $("#toggleReport-text").text("<@s.text name='manage.overview.published.report.hide'/>");
                $('#dwcaReport').fadeIn();
            }
        });
        //Hack needed for Internet Explorer X.*x
        $('.edit').each(function() {
            $(this).click(function() {
                window.location = $(this).parent('a').attr('href');
            });
        });
        $('.submit').each(function() {
            $(this).click(function() {
                $(this).parent('form').submit();
            });
        });

        $("#emlFile").change(function() {
            var usedFileName = $("#emlFile").prop("value");
            if(usedFileName !== "") {
                $("#emlReplace").show();
                $("#emlCancel").show();
                $("#eml-validate").show();
            }
        });

        $("#datapackageMetadataFile").change(function() {
            var usedFileName = $("#datapackageMetadataFile").prop("value");
            if (usedFileName !== "") {
                $("#datapackageMetadataReplace").show();
                $("#datapackageMetadataCancel").show();
                $("#datapackage-metadata-validate").show();
                $("#datapackage-metadata-preserve-scope-metadata").show();
            }
        });

        $("#datapackageMetadataCancel").click(function (e) {
            e.preventDefault();
            $("#datapackageMetadataFile").prop("value", "");
            $("#datapackageMetadataReplace").hide();
            $("#datapackageMetadataCancel").hide();
            $("#datapackage-metadata-validate").hide();
            $("#datapackage-metadata-preserve-scope-metadata").hide();
        });

        $("#emlCancel").click(function(event) {
            event.preventDefault();
            $("#emlFile").prop("value", "");
            $("#emlReplace").hide();
            $("#emlCancel").hide();
            $("#eml-validate").hide();
        });

        var sourceNames = [
            <#list resource.sources as source>
            "${source.name}"<#if source_has_next>,</#if>
            </#list>
        ];
        var confirmedFiles = [];

        // add new source tabs
        $(".sources-tab-root").click(function (event) {
            var selectedTab = $(this);
            var selectedTabId = selectedTab[0].id;

            var fileItems = document.querySelectorAll('.fileItem');

            // inputs
            var sourceNameInput = $("#sourceName");
            var sourceTypeInput = $("#sourceType");
            var urlInput = $("#url");
            var fileInput = $("#fileInput");

            // buttons
            var addButton = $("#add");
            var clearButton = $("#clear");
            var sendButton = $("#sendButton");
            var chooseFilesButton = $("#chooseFilesButton");

            // values
            var sourceNameValue = sourceNameInput.val();

            // remove "selected" from all tabs
            $(".sources-tab-root").removeClass("tab-selected");
            // hide all indicators
            $(".tabs-indicator").hide();
            // add "selected" to clicked tab
            selectedTab.addClass("tab-selected");
            // show indicator for this tab
            $("#" + selectedTabId + " .tabs-indicator").show();

            if (selectedTabId === 'tab-source-file') {
                sourceTypeInput.attr("value", "source-file");
                urlInput.hide();
                sourceNameInput.hide();
                urlInput.prop("value", "");
                chooseFilesButton.show();
                sendButton.hide();
                $(".progress-bar-container").show();
                clearButton.hide();
                addButton.attr("value", '<@s.text name="button.add"/>');
                addButton.hide();
                sourceNameInput.removeClass("is-invalid");
                urlInput.removeClass("is-invalid");
                $("#callout-source-exists").hide();
            } else if (selectedTabId === 'tab-source-url') {
                sourceTypeInput.attr("value", "source-url");
                urlInput.show();
                sourceNameInput.show();
                chooseFilesButton.hide();
                sendButton.hide();
                $(".progress-bar-container").hide();
                fileInput.prop("value", "");
                clearButton.show();
                addButton.attr("value", '<@s.text name="button.add"/>');
                addButton.show();

                fileItems.forEach(function(item) {
                    item.remove();
                });
                selectedFiles = [];
                confirmedFiles = []
                sourceNameInput.removeClass("is-invalid");
                urlInput.removeClass("is-invalid");

                if (sourceNames.includes(sourceNameValue)) {
                    $("#callout-source-exists").show();
                    addButton.hide();
                } else {
                    $("#callout-source-exists").hide();
                    addButton.show();
                }

                // set timeout - otherwise the callout remains. find out why
                setTimeout(function () {
                    $("#callout-not-enough-space").hide();
                }, 100);
            } else {
                sourceTypeInput.attr("value", "source-sql");
                chooseFilesButton.hide();
                $(".progress-bar-container").hide();
                sourceNameInput.show();
                sendButton.hide();
                fileInput.prop("value", "");
                urlInput.hide();
                urlInput.prop("value", "");
                clearButton.hide();
                addButton.attr("value", '<@s.text name="button.connect"/>');
                addButton.show();

                fileItems.forEach(function(item) {
                    item.remove();
                });
                selectedFiles = [];
                confirmedFiles = [];
                sourceNameInput.removeClass("is-invalid");
                urlInput.removeClass("is-invalid");

                if (sourceNames.includes(sourceNameValue)) {
                    $("#callout-source-exists").show();
                    addButton.hide();
                } else {
                    $("#callout-source-exists").hide();
                    addButton.show();
                }
                // set timeout - otherwise the callout remains. find out why
                setTimeout(function () {
                    $("#callout-not-enough-space").hide();
                }, 100);
            }
        });

        $("#btn-confirm-source-overwrite").on("click", function (e) {
            e.preventDefault();
            $("#callout-source-exists").hide();
            $("#add").show();
        });

        $("#url").on("input", function() {
            var urlValue = $(this).val();

            if (urlValue) {
                $(this).removeClass("is-invalid");
            }
        });


        $("#sourceName").on("input", function() {
            var sourceNameValue = $(this).val();

            if (sourceNameValue) {
                $(this).removeClass("is-invalid");
            }

            if (sourceNames.includes(sourceNameValue)) {
                $("#callout-source-exists").show();
                $("#add").hide();
            } else {
                $("#callout-source-exists").hide();
                $("#add").show();
            }
        });

        var sendButton = $("#sendButton");

        $("#manager").change(function (e) {
            var manager = this.options[e.target.selectedIndex].value;

            if (manager) {
                $("#add-manager").show();
            } else {
                $("#add-manager").hide();
            }
        });

        $("#network").change(function (e) {
            var network = this.options[e.target.selectedIndex].value;
            if (network) {
                $("#add-network").show();
                // OBIS network key (show callout)
                if (network === "2b7c7b4f-4d4f-40d3-94de-c28b6fa054a6") {
                    $("#obis-network-validation-notification").show();
                } else {
                    $("#obis-network-validation-notification").hide();
                }
            } else {
                $("#add-network").hide();
                $("#obis-network-validation-notification").hide();
            }
        });

        $("#clear").click(function(event) {
            event.preventDefault();

            var progressBars = document.getElementsByClassName("progress-bar");
            var progressStatuses = document.getElementsByClassName("progress-bar-status");

            for (var i = 0; i < progressBars.length; i++) {
              var progressBar = progressBars[i];
              progressBar.style.width = "0%";
            }

            for (var j = 0; j < progressStatuses.length; j++) {
              var progressStatus = progressStatuses[j];
              progressStatus.innerText = "";
            }

            $("#url").prop("value", "");
            $("#sourceName").prop("value", "");
            $("#callout-source-exists").hide();
            $("#add").show();
        });

        $(function() {
            $('.icon-validate').tooltip({track: true});
        });

        // load a preview of the mapping in the modal window
        $(".peekBtn").click(function(e) {
            e.preventDefault();
            var addressValue = $(this).attr("href");
            displayProcessing();
            $("#modalcontent").load(addressValue, hideProcessing);
            $("#modalbox").show();
        });
        $("#modalbox").click(function(e) {
            e.preventDefault();
            $("#modalbox").hide();
        });

        // change the doi prefix input value, as per the selected organisation
        $( "#doi_select" ).change(function() {
            $("#doi_prefix").prop("value", $( this).val());
        });

        $("#doi_edit").click(function() {
            $('.doiButton').hide();
            $('#doi_edit_block').show();
        });

        $('#doi_edit_cancel').click(function() {
            $('.doiButton').show();
            $('#doi_edit_block').hide();
        });

        $('#makePublic').click(function (e) {
            e.preventDefault();
            showMakePublicModal();
        });

        // make public modal window
        function showMakePublicModal() {
            var dialogWindow = $("#make-public-modal");
            dialogWindow.modal('show');

            var radioValue = $('input[name=makePublicOptions]:checked').val();

            // show input depending on which radio is selected
            if (radioValue === "makePublicImmediately") {
                $("#makePublicDateTime").hide();
                $("#makePublicDateTimeWrapper").hide();
            } else if (radioValue === "makePublicAtDate") {
                $("#makePublicDateTimeWrapper").show();
                $("#makePublicDateTime").show();
            }
        }

        // hide/show input on radio change
        $('input[name=makePublicOptions]').on('change', function() {
            var radioValue = $('input[name=makePublicOptions]:checked').val();

            if (radioValue === "makePublicImmediately") {
                $("#makePublicDateTime").hide();
                $("#makePublicDateTimeWrapper").hide();
            } else if (radioValue === "makePublicAtDate") {
                $("#makePublicDateTimeWrapper").show();
                $("#makePublicDateTime").show();
            }
        });

        // erase makePublicDateTime before submit in case of 'make public immediately'
        $("#make-public-modal-form").one('submit', function (e) {
            e.preventDefault();

            var radioValue = $('input[name=makePublicOptions]:checked').val();
            if (radioValue === "makePublicImmediately") {
                $("#makePublicDateTime").val("");
            }

            $(this).submit();
        });

        // validate add source form (for URL and SQL) and submit data
        $("#add").on("click", function (e) {
            e.preventDefault();

            var sourceType = $("#sourceType").val();
            var sourceNameInput = $("#sourceName");
            var urlInput = $("#url");
            var sourceNameValue = sourceNameInput.val();
            var form = $("#addSourceForm")[0];

            if (sourceType === 'source-sql') {
                if (!sourceNameValue) {
                    sourceNameInput.addClass("is-invalid");
                } else {
                    $('#source-data-modal').modal('hide');
                    displayProcessing();
                    form.submit();
                }
            } else if (sourceType === 'source-url') {
                var urlValue = urlInput.val();
                var invalid = false;

                if (!sourceNameValue) {
                    sourceNameInput.addClass("is-invalid");
                    invalid = true;
                }

                if (!urlValue) {
                    urlInput.addClass("is-invalid");
                    invalid = true;
                }

                if (!invalid) {
                    $('#source-data-modal').modal('hide');
                    displayProcessing();
                    form.submit();
                }
            }
        });

        // Action modals
        function showAddSourceModal() {
            var dialogWindow = $("#source-data-modal");
            dialogWindow.modal('show');
        }

        $("#add-source-button").on('click', function () {
            showAddSourceModal();
        });

        function showAddMappingModal() {
            var dialogWindow = $("#mapping-modal");
            dialogWindow.modal('show');
        }

        $("#add-mapping-button").on('click', function () {
            showAddMappingModal();
        });

        function showMetadataModal() {
            var dialogWindow = $("#metadata-modal");
            dialogWindow.modal('show');
        }

        $("#upload-metadata-button").on('click', function () {
            showMetadataModal();
        });

        // close metadata modal to show confirm override modal instead
        $("#emlReplace").on("click", function () {
            $('#metadata-modal').modal('hide');
        });

        // close DOI modal to show confirmation modal instead
        $("#deleteDoi").on("click", function () {
            $('#reserve-doi-modal').modal('hide');
        });

        // close DOI modal to show confirmation modal instead
        $("#reserveDoi").on("click", function () {
            $('#reserve-doi-modal').modal('hide');
        });

        function showPublicationModal() {
            var dialogWindow = $("#publication-modal");
            dialogWindow.modal('show');
        }

        $("#publish-button-show-warning").on('click', function () {
            showPublicationModal();
        });

        function showVisibilityDisabledModal() {
            var dialogWindow = $("#visibility-disabled-modal");
            dialogWindow.modal('show');
        }

        $("#show-visibility-disabled-modal").on('click', showVisibilityDisabledModal);

        function showRegistrationModal() {
            var dialogWindow = $("#registration-modal");
            dialogWindow.modal('show');
        }

        $("#show-registration-disabled-modal").on('click', showRegistrationModal);

        function showAddNetworkModal() {
            var dialogWindow = $("#networks-modal");
            dialogWindow.modal('show');
        }

        $("#add-network-button").on('click', function () {
            showAddNetworkModal();
        });

        function showAddManagerModal() {
            var dialogWindow = $("#managers-modal");
            dialogWindow.modal('show');
        }

        $("#add-manager-button").on('click', function () {
            showAddManagerModal();
        });

        $("#reserve-doi").on("click", function (e) {
            showReserveDoiModal();
        });

        function showReserveDoiModal() {
            e.preventDefault();
            var dialogWindow = $("#reserve-doi-modal");
            dialogWindow.modal('show');
        }

        var selectedFiles = [];

        document.getElementById("chooseFilesButton").addEventListener("click", function () {
            document.getElementById("fileInput").click();
        });

        document.getElementById("fileInput").addEventListener("change", function () {
            var newlySelectedFiles = Array.from(document.getElementById("fileInput").files);
            selectedFiles = selectedFiles.concat(newlySelectedFiles);
            refreshFileList();

            if (selectedFiles.length > 0) {
                sendButton.show();
            } else {
                sendButton.hide();
            }
        });

        document.getElementById("sendButton").addEventListener("click", async function () {
            var uploadButton = $("#sendButton");
            uploadButton.addClass("clicked")
            uploadButton.hide();

            var fileItems = document.querySelectorAll(".fileItem");
            var promises = [];

            // hide remove buttons - already submitted
            var removeButtons = document.querySelectorAll(".removeButton");
            removeButtons.forEach(function(button) {
                button.style.display = "none";
            });

            for (var i = 0; i < selectedFiles.length; i++) {
                var file = selectedFiles[i];
                var fileItem = fileItems[i];
                var progressBar = fileItem.querySelector(".progressBar-value");
                var fileStatus = fileItem.querySelector(".fileStatus");

                promises.push(uploadFile(file, i, progressBar, fileStatus));
            }

            try {
                await Promise.all(promises);
                closeModal();
                window.location.reload();
            } catch (error) {
                console.log(error)
            }
        });

        function refreshFileList() {
            var fileListContainer = document.getElementById("fileList");
            fileListContainer.innerHTML = "";

            totalFilesSize = 0;

            for (var i = 0; i < selectedFiles.length; i++) {
                var file = selectedFiles[i];
                totalFilesSize += file.size;
                addFileItem(file, i);
            }
        }

        var totalFreeSpace = ${usableSpace?c};
        var totalFilesSize = 0;

        function addFileItem(file, index) {
            // create file item div with index attribute
            var fileItem = document.createElement("div");
            fileItem.className = "fileItem";
            fileItem.setAttribute("data-file-index", index)

            // create file inner div
            var fileItemInner = document.createElement("div");
            fileItemInner.className = "fileItem-inner"
            fileItem.appendChild(fileItemInner);

            // create additional divs: info, meta, name
            var fileInfo = document.createElement("div");
            fileInfo.className = "fileInfo";
            var fileMeta = document.createElement("div");
            fileMeta.className = "fileMeta";
            var fileName = document.createElement("div");
            fileName.className = "fileName";

            // extract file name without extension - check source already exist
            var fileNameWithoutExtension = file.name.substring(0, file.name.lastIndexOf('.'));
            var fileNameWithoutSpaces = fileNameWithoutExtension.replace(/\s/g, "");

            // file error div
            var fileError = document.createElement("div");
            fileError.className = "fileError";
            fileError.setAttribute('data-index', index)

            // file status div
            var fileStatus = document.createElement("div");
            fileStatus.className = "fileStatus";

            // set file name and file size
            fileName.innerText = file.name;
            fileStatus.innerText = formatFileSize(file.size);

            totalFilesSize = totalFilesSize + file.size;

            // make sure source with the name does not exist (case-insensitive check)
            var isSourceAlreadyExist = sourceNames.includes(fileNameWithoutSpaces.toLowerCase());

            if (file.size > 10000000000) {
                fileError.innerText = `<@s.text name='manage.overview.source.file.too.big'/>`;
            } else if (isSourceAlreadyExist && !confirmedFiles.includes(fileNameWithoutSpaces)) {
                fileError.innerText = `<@s.text name='manage.resource.addSource.sameName.confirm'/>`;

                var confirmOverwriteLink = document.createElement("a");
                confirmOverwriteLink.id = 'confirmOverwriteSourceFileLink-' + index;
                confirmOverwriteLink.className = 'confirmOverwriteSourceFileLink custom-link';
                confirmOverwriteLink.href = '#';
                confirmOverwriteLink.textContent = 'Confirm';
                confirmOverwriteLink.setAttribute('data-index', index)
                fileError.appendChild(confirmOverwriteLink);
            }

            // create "done" icon
            var fileDoneIcon = document.createElement("div");
            fileDoneIcon.className = "fileDoneIcon";
            fileDoneIcon.innerHTML = '<i class="bi bi-check2 text-gbif-primary"></i>';

            // create "error/warning" icon
            var fileWarningIcon = document.createElement("div");
            fileWarningIcon.className = "fileWarningIcon";
            fileWarningIcon.innerHTML = '<i class="bi bi-exclamation-circle text-gbif-danger"></i>';

            // link divs
            fileMeta.appendChild(fileName);
            fileMeta.appendChild(fileStatus);
            if (isSourceAlreadyExist && !confirmedFiles.includes(fileNameWithoutSpaces)) {
                fileMeta.appendChild(fileError);
                fileDoneIcon.style.visibility = "hidden";
                fileDoneIcon.style.display = "none";
                fileWarningIcon.style.visibility = "visible";
                fileWarningIcon.style.display = "block";
            }
            fileInfo.appendChild(fileDoneIcon);
            fileInfo.appendChild(fileWarningIcon);
            fileInfo.appendChild(fileMeta);

            // create progress bar divs
            var progressBar = document.createElement("div");
            progressBar.className = "progressBar";
            var progressBarValue = document.createElement("div");
            progressBarValue.className = "progressBar-value";
            progressBar.appendChild(progressBarValue);

            // create remove button and its icon
            var removeButton = document.createElement("button");
            removeButton.className = "removeButton";

            removeButton.addEventListener("click", function () {
                var index = selectedFiles.indexOf(file);
                if (index > -1) {
                    selectedFiles.splice(index, 1);

                    var fileNameWithoutExtension = file.name.substring(0, file.name.lastIndexOf('.')).replace(/\s/g, "");
                    var fileNameIndex = confirmedFiles.indexOf(fileNameWithoutExtension.toLowerCase());
                    if (fileNameIndex > -1) {
                        confirmedFiles.splice(fileNameIndex, 1);
                    }

                    refreshFileList();
                }
            });

            // icon for the remove button
            var xIcon = document.createElementNS("http://www.w3.org/2000/svg", "svg");
            var xIconPath = document.createElementNS("http://www.w3.org/2000/svg", "path");
            xIcon.setAttribute("class", "icon-button-svg");
            xIcon.setAttribute("focusable", "false");
            xIcon.setAttribute("aria-hidden", "true");
            xIcon.setAttribute("viewBox", "0 0 24 24");
            xIconPath.setAttribute("d", "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
            xIcon.appendChild(xIconPath);

            removeButton.appendChild(xIcon);

            removeButton.addEventListener("click", function () {
                fileItem.remove();
            });

            var fileCommands = document.createElement("div");
            fileCommands.className = "fileCommands";
            fileCommands.appendChild(removeButton);

            fileItemInner.appendChild(fileInfo);
            fileItem.appendChild(progressBar);
            fileItemInner.appendChild(fileCommands);

            document.getElementById("fileList").appendChild(fileItem);
        }

        async function uploadFile(file, fileIndex, progressBar, fileStatus) {
            return new Promise(function (resolve, reject) {
                var formData = new FormData();
                formData.append("r", "${resource.shortname}");
                formData.append("sourceType", "source-file");
                formData.append("validate", false);
                formData.append("add", "Add");
                formData.append("file", file);

                var xhr = new XMLHttpRequest();
                xhr.open("POST", "addsource.do", true);

                xhr.onload = function () {
                    var redirectUrl = xhr.getResponseHeader("Location");
                    if (redirectUrl) {
                        window.location.href = redirectUrl;
                    }
                    resolve();
                };

                xhr.onerror = function() {
                    console.error('An error occurred during the request:', xhr.status, xhr.statusText);
                    reject(new Error('An error occurred during the request:' + xhr.status + "" + xhr.statusText));
                };

                xhr.upload.onprogress = function (event) {
                    if (event.lengthComputable) {
                        var percent = Math.round((event.loaded / event.total) * 100);
                        progressBar.style.width = percent + "%";
                        fileStatus.innerText =
                            percent +
                            "% (" +
                            formatFileSize(event.loaded) +
                            " / " +
                            formatFileSize(event.total) +
                            ")";

                        if (percent === 100) {
                            fileStatus.innerText = `<@s.text name='manage.resource.addSource.processingFile'/>`;
                            progressBar.classList.add("progressBar-loader");
                        } else {
                            progressBar.classList.remove("progressBar-loader");
                        }
                    }
                };

                xhr.onloadend = function () {
                    progressBar.style.width = "100%"; // Set progress to 100%

                    // hide progress bar
                    var fileProgressBarWrapper = document.querySelector('.fileItem[data-file-index="' + fileIndex + '"] .progressBar');
                    fileProgressBarWrapper.classList.add("d-none")

                    // and display done icon
                    var fileDoneIcon = document.querySelector('.fileItem[data-file-index="' + fileIndex + '"] .fileDoneIcon');
                    fileDoneIcon.style.visibility = "visible";

                    // "Upload Complete", set to empty
                    fileStatus.innerText = "";

                    resolve(); // Resolve the promise once the request has completed
                };

                xhr.send(formData);
            });
        }

        function formatFileSize(bytes) {
            if (bytes === 0) return "0 Bytes";
            var k = 1000;
            var sizes = ["Bytes", "KB", "MB", "GB", "TB"];
            var i = Math.floor(Math.log(bytes) / Math.log(k));
            var parsed = parseFloat((bytes / Math.pow(k, i)).toFixed(1));
            var locale = '${currentLocale}';
            var formatted;

            try {
                formatted = parsed.toLocaleString(locale.replace("_", "-"), { minimumFractionDigits: 1, maximumFractionDigits: 1 });
            } catch (e) {
                formatted = parsed.toLocaleString("en", { minimumFractionDigits: 1, maximumFractionDigits: 1 });
            }

            return formatted + " " + sizes[i];
        }

        function closeModal() {
            // Get the modal element
            var modal = document.getElementById("source-data-modal");

            // Add the "hide" class to hide the modal
            modal.classList.add("hide");

            // Remove the "show" class to ensure the modal is no longer displayed
            modal.classList.remove("show");

            // Reset the inline style to hide the modal
            modal.style.display = "none";

            // Remove the modal from the DOM to clean up
            modal.parentNode.removeChild(modal);
        }

        // Spy confirm overwrite warnings and enable/disable submit button
        const fileListDiv = document.getElementById('fileList');

        const observer = new MutationObserver(function() {
            var fileErrors = $(".fileError");
            var fileOutOfSpaceCallout = $("#callout-not-enough-space");
            var fileItems = $(".fileItem");

            if (totalFreeSpace - totalFilesSize < 0) {
                fileOutOfSpaceCallout.show();
            } else {
                fileOutOfSpaceCallout.hide();
            }

            var outOfSpaceDisplayed = fileOutOfSpaceCallout.css("display") !== "none";

            if (fileErrors.length > 0 || outOfSpaceDisplayed) {
                sendButton.hide();
            } else if (!sendButton.hasClass("clicked")) {
                sendButton.show();
            }

            if (fileItems.length === 0) {
                sendButton.hide();
            }
        });

        const config = { attributes: true, childList: true, subtree: true };

        observer.observe(fileListDiv, config);

        // remove error classes/icons if confirmed
        $('#fileList').on('click', '.confirmOverwriteSourceFileLink', function() {
            var fileIndex = $(this).data('index');

            var fileItemElement = $('.fileItem[data-file-index="' + fileIndex + '"]');
            fileItemElement.find('.fileError').remove();

            var fileNameWithExtension = fileItemElement.find('.fileName').text();
            var fileNameWithoutExtension = fileNameWithExtension.substring(0, fileNameWithExtension.lastIndexOf('.'));

            confirmedFiles.push(fileNameWithoutExtension);

            var fileWarningIconElement = fileItemElement.find('.fileWarningIcon');
            var fileDoneIconElement = fileItemElement.find('.fileDoneIcon');

            fileWarningIconElement.css({
                'display': 'none',
                'visibility': 'hidden'
            });

            fileDoneIconElement.css({'display': 'block'});

            // Prevent the default link behavior
            return false;
        });

        $("#rowType").select2({
            placeholder: '',
            dropdownParent: $('#mapping-modal'),
            language: {
                noResults: function () {
                    return '${selectNoResultsFound}';
                }
            },
            width: "100%",
            minimumResultsForSearch: 15,
            theme: 'bootstrap4'
        });
        $("#manager").select2({
            placeholder: '${action.getText("manage.overview.resource.managers.select")?js_string}',
            dropdownParent: $('#managers-modal'),
            language: {
                noResults: function () {
                    return '${selectNoResultsFound}';
                }
            },
            width: "100%",
            minimumResultsForSearch: 15,
            theme: 'bootstrap4'
        });
        $("#network").select2({
            placeholder: '${action.getText("manage.overview.networks.select")?js_string}',
            dropdownParent: $('#networks-modal'),
            language: {
                noResults: function () {
                    return '${selectNoResultsFound}';
                }
            },
            width: "100%",
            minimumResultsForSearch: 15,
            theme: 'bootstrap4'
        });

        $("#publishingOrganizationKey").select2({
            placeholder: '',
            language: {
                noResults: function () {
                    return '${selectNoResultsFound}';
                }
            },
            dropdownParent: $('#change-publishing-organization-modal'),
            width: "100%",
            minimumResultsForSearch: 15,
            theme: 'bootstrap4'
        });

        $("#change-publishing-organization").on('click', function (e) {
            e.preventDefault();
            showChangePublishingOrganizationModal();
        });

        function showChangePublishingOrganizationModal() {
            var dialogWindow = $("#change-publishing-organization-modal");
            dialogWindow.modal('show');
        }
    });
</script>

<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<#include "/WEB-INF/pages/macros/popover.ftl"/>
<#include "/WEB-INF/pages/macros/manage/publish.ftl"/>
<#assign metadataType = "metadata"/>
<#assign isDataPackage = resource.isDataPackage()/>

<#if resource.isAlreadyAssignedDoi()?string == "false" && resource.status != "REGISTERED">
    <#assign disableRegistrationRights="false"/>
<#elseif currentUser.hasRegistrationRights()?string == "true">
    <#assign disableRegistrationRights="false"/>
<#else>
    <#assign disableRegistrationRights="true"/>
</#if>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">

        <#if !currentUser.hasRegistrationRights()>
            <#if resource.status == "DELETED">
                <div class="alert alert-warning alert-dismissible fade show d-flex" role="alert">
                    <div class="me-3">
                        <i class="bi bi-exclamation-triangle alert-orange-2 fs-bigger-2 me-2"></i>
                    </div>
                    <div class="overflow-x-hidden pt-1">
                        <span><@s.text name="manage.resource.status.undeletion.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/></span>
                    </div>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            <#elseif resource.isAlreadyAssignedDoi()?string == "true" || resource.status == "REGISTERED">
                <div class="alert alert-warning alert-dismissible fade show d-flex" role="alert">
                    <div class="me-3">
                        <i class="bi bi-exclamation-triangle alert-orange-2 fs-bigger-2 me-2"></i>
                    </div>
                    <div class="overflow-x-hidden pt-1">
                        <span><@s.text name="manage.resource.status.deletion.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/></span>
                    </div>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </#if>
        </#if>
    </div>

    <div class="container-fluid border-bottom">
        <div class="container bg-body border rounded-2 mb-4">
            <div class="container my-3 p-3">
                <div class="text-center fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb justify-content-center mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview"/></li>
                        </ol>
                    </nav>
                </div>

                <div class="text-center mt-1">
                    <h1 property="dc:title" class="rtitle pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                        <#if resource.title?has_content>
                            ${resource.title}
                        <#else>
                            ${resource.shortname}
                        </#if>
                    </h1>

                    <div>
                        <div class="my-auto me-3">
                            <#if typesVocabulary[resourceTypeLowerCase]??><span class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill type-${resourceTypeLowerCase} me-1">${typesVocabulary[resourceTypeLowerCase]}</span></#if>
                            <span class="fs-smaller-2 text-discreet"><@s.text name="basic.createdByOn"><@s.param>${(resource.creator.email)!}</@s.param><@s.param>${resource.created?date?string["d MMMM yyyy"]}</@s.param></@s.text></span>
                        </div>
                    </div>

                    <div class="mt-2">
                        <a href="${baseURL}/resource?r=${resource.shortname}" class="btn btn-sm btn-outline-gbif-primary top-button"><@s.text name="button.view"/></a>

                        <#if resource.status == "DELETED">
                            <div style="display: inline-block;">
                                <#if disableRegistrationRights == "false">
                                    <form action='resource-undelete.do' method='post'>
                                        <input name="r" type="hidden" value="${resource.shortname}" />
                                        <@s.submit cssClass="btn btn-sm btn-outline-gbif-primary confirmUndeletion top-button" name="undelete" key="button.undelete"/>
                                    </form>
                                <#else>
                                    <button class="btn btn-sm btn-outline-gbif-primary top-button" name="undelete" disabled><@s.text name="button.undelete"/></button>
                                </#if>
                            </div>
                        <#else>
                            <#if disableRegistrationRights == "false">
                                <div class="btn-group btn-group-sm" role="group">
                                    <button id="btnGroupDelete" type="button" class="btn btn-sm btn-outline-gbif-danger dropdown-toggle align-self-start top-button" data-bs-toggle="dropdown" aria-expanded="false">
                                        <@s.text name="button.delete"/>
                                    </button>
                                    <ul class="dropdown-menu" aria-labelledby="btnGroupDelete">
                                        <#if resource.key?? && resource.status == "REGISTERED">
                                            <li>
                                                <form action="resource-delete.do" method='post'>
                                                    <input name="r" type="hidden" value="${resource.shortname}" />
                                                    <@s.submit cssClass="btn btn-sm btn-outline-gbif-danger confirmDeletion confirmDeletionFromIptAndGbif w-100 dropdown-button" cssStyle="text-transform: unset !important" name="delete" key="button.delete.fromIptAndGbif"/>
                                                </form>
                                            </li>
                                        </#if>
                                        <li>
                                            <form action="resource-deleteFromIpt.do" method='post'>
                                                <input name="r" type="hidden" value="${resource.shortname}" />
                                                <@s.submit cssClass="btn btn-sm btn-outline-gbif-danger confirmDeletion confirmDeletionFromIptOnly w-100 dropdown-button" cssStyle="text-transform: unset !important" name="delete" key="button.delete.fromIpt"/>
                                            </form>
                                        </li>
                                    </ul>
                                </div>
                            <#else>
                                <button class="btn btn-sm btn-outline-gbif-danger top-button" name="delete" disabled><@s.text name="button.delete"/></button>
                            </#if>
                        </#if>

                        <a href="${baseURL}/manage/" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="button.cancel"/></a>
                    </div>

                    <p class="mt-3 mb-0 text-smaller fst-italic">
                        <#if dataPackageResource>
                            <@s.text name="manage.overview.dataSchema.description"/>
                        <#elseif resource.coreType?has_content && resource.coreType==metadataType>
                            <@s.text name="manage.overview.description.metadataOnly"/>
                        <#else>
                            <@s.text name="manage.overview.description"/>
                        </#if>
                    </p>

                    <div id="dialog" class="modal fade" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true"></div>
                </div>
            </div>
        </div>
    </div>

    <div id="sections" class="container-fluid bg-body">
        <div class="container main-content-container my-md-4 bd-layout">

            <main class="bd-main">
                <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                    <nav id="sidebar-content">
                        <ul>
                            <#if resource.coreType?has_content && resource.coreType==metadataType>
                                <li><a href="#anchor-metadata" class="sidebar-navigation-link"><@s.text name='manage.overview.metadata'/></a></li>
                            <#else>
                                <li><a href="#anchor-sources" class="sidebar-navigation-link"><@s.text name='manage.overview.source.data'/></a></li>
                                <li><a href="#anchor-mappings" class="sidebar-navigation-link"><#if dataPackageResource><@s.text name='manage.overview.mappings'/><#else><@s.text name='manage.overview.DwC.Mappings'/></#if></a></li>
                                <li><a href="#anchor-metadata" class="sidebar-navigation-link"><@s.text name='manage.overview.metadata'/></a></li>
                            </#if>
                            <li><a href="#anchor-visibility" class="sidebar-navigation-link"><@s.text name='manage.overview.visibility'/></a></li>
                            <li><a href="#anchor-publish" class="sidebar-navigation-link"><@s.text name='manage.overview.published'/></a></li>
                            <li><a href="#anchor-registration" class="sidebar-navigation-link"><@s.text name='manage.overview.registration'/></a></li>
                            <li><a href="#anchor-autopublish" class="sidebar-navigation-link"><@s.text name='manage.overview.autopublish.title'/></a></li>
                            <li><a href="#anchor-networks" class="sidebar-navigation-link"><@s.text name='manage.overview.networks.title'/></a></li>
                            <li><a href="#anchor-managers" class="sidebar-navigation-link"><@s.text name='manage.overview.resource.managers'/></a></li>
                        </ul>
                    </nav>
                </div>

                <div class="bd-content ps-lg-4">
                    <!-- when resource is of type metadata-only, there is no need to show source data and mapping sections -->
                    <#if resource.coreType?has_content && resource.coreType==metadataType>
                        <#include "/WEB-INF/pages/manage/overview_metadata.ftl"/>
                    <#else>
                        <#include "/WEB-INF/pages/manage/overview_data.ftl"/>
                        <#include "/WEB-INF/pages/manage/overview_metadata.ftl"/>
                    </#if>

                    <span class="anchor anchor-home-resource-page" id="anchor-visibility"></span>
                    <div class="py-5 border-bottom section" id="visibility">
                        <div class="d-flex justify-content-between">
                            <div class="d-flex">
                                <h5 class="my-auto text-gbif-header-2 fw-400">
                                    <#assign visibilityTitleInfo>
                                        <@s.text name='manage.overview.visibility.description'/>
                                    </#assign>

                                    <@popoverTextInfo visibilityTitleInfo/>
                                    <@s.text name='manage.overview.visibility'/>
                                </h5>
                            </div>

                            <div class="d-flex justify-content-end">
                                <#if resource.status=="PRIVATE">
                                    <#assign actionMethod>makePublic</#assign>
                                    <form action='resource-${actionMethod}.do' method='post'>
                                        <input name="r" type="hidden" value="${resource.shortname}"/>
                                        <button id="makePublic" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button" type="submit">
                                            <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                                <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                                            </svg>
                                            <@s.text name='button.change'/>
                                        </button>
                                    </form>
                                <#elseif resource.status=="PUBLIC" && (resource.identifierStatus=="PUBLIC_PENDING_PUBLICATION" || resource.identifierStatus == "UNRESERVED")>
                                    <#assign actionMethod>makePrivate</#assign>
                                    <form action='resource-${actionMethod}.do' method='post'>
                                        <input name="r" type="hidden" value="${resource.shortname}"/>
                                        <input name="unpublish" type="hidden" value="Change"/>
                                        <button class="confirmMakePrivate text-gbif-header-2 icon-button icon-material-actions overview-action-button" type="submit">
                                            <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                                <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                                            </svg>
                                            <@s.text name='button.change'/>
                                        </button>
                                    </form>
                                <#else>
                                    <button id="show-visibility-disabled-modal" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button" type="button">
                                        <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                            <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                                        </svg>
                                        <@s.text name='button.change'/>
                                    </button>
                                </#if>
                            </div>
                        </div>

                        <div class="mt-4">
                            <p class="mb-0">
                                <#if resource.status=="PRIVATE">
                                    <#if resource.makePublicDate?has_content>
                                        <@s.text name="manage.resource.status.intro.private.public.scheduled">
                                            <@s.param>${resource.makePublicDate?datetime?string.long_short}</@s.param>
                                        </@s.text>
                                    <#else>
                                        <@s.text name="manage.resource.status.intro.private"/>
                                    </#if>
                                <#elseif resource.status=="PUBLIC">
                                    <@s.text name="manage.resource.status.intro.public"/>
                                <#elseif resource.status=="REGISTERED">
                                    <@s.text name="manage.resource.status.intro.registered"/>
                                <#elseif resource.status=="DELETED">
                                    <@s.text name="manage.resource.status.intro.deleted"/>
                                </#if>
                            </p>
                        </div>
                    </div>

                    <span class="anchor anchor-home-resource-page" id="anchor-publish"></span>
                    <div class="py-5 border-bottom section" id="publish">
                        <div class="d-flex justify-content-between">
                            <div class="d-flex">
                                <h5 class="my-auto text-gbif-header-2 fw-400">
                                    <#assign overviewTitleInfo>
                                        <#if resource.coreType?has_content && resource.coreType==metadataType>
                                            <@s.text name="manage.overview.published.description.metadataOnly"/>
                                        <#elseif resource.dataPackage??>
                                            <@s.text name="manage.overview.published.description.dp"/>
                                        <#else>
                                            <@s.text name="manage.overview.published.description"/>
                                        </#if>
                                        <br/><br/>
                                        <#if organisationWithPrimaryDoiAccount??>
                                            <@s.text name='manage.overview.published.description.doiAccount'><@s.param>${organisationWithPrimaryDoiAccount.doiRegistrationAgency}</@s.param><@s.param>${organisationWithPrimaryDoiAccount.name}</@s.param><@s.param>${organisationWithPrimaryDoiAccount.doiPrefix}</@s.param></@s.text>
                                        <#else>
                                            <@s.text name="manage.overview.published.description.noDoiAccount"/>
                                        </#if>
                                    </#assign>
                                    <@popoverTextInfo overviewTitleInfo/>

                                    <@s.text name='manage.overview.published'/>
                                </h5>
                            </div>

                            <div class="col-4 d-flex justify-content-end">
                                <@publish resource/>
                            </div>
                        </div>

                        <div class="mt-4">
                            <div class="mb-2">
                                <@s.text name="eml.publishingOrganisation"/>: <i>${(resource.organisation.name)!""}</i>
                                <a id="change-publishing-organization" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button" type="button" href="#">
                                    <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                      <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                                    </svg>
                                    <@s.text name="button.change"/>
                                </a>
                            </div>

                            <p class="mb-0">
                                <@s.text name="manage.overview.published.intro"/>
                            </p>

                            <div class="details mt-3">
                                <#assign lastPublishedTitle><@s.text name="manage.overview.published.last.publication.intro"/></#assign>
                                <#assign nextPublishedTitle><@s.text name="manage.overview.published.next.publication.intro"/></#assign>
                                <#assign versionTitle><@s.text name="manage.overview.published.version"/></#assign>
                                <#assign releasedTitle><@s.text name="manage.overview.published.released"/></#assign>
                                <#assign pubLogTitle><@s.text name="portal.publication.log"/></#assign>
                                <#assign pubRepTitle><@s.text name="manage.overview.published.report.show"/></#assign>
                                <#assign downloadTitle><@s.text name='manage.overview.published.download'/></#assign>
                                <#assign showTitle><@s.text name="basic.show"/></#assign>
                                <#assign viewTitle><@s.text name='button.view'/></#assign>
                                <#assign previewTitle><@s.text name='button.preview'/></#assign>
                                <#assign emptyCell="-"/>
                                <#assign visibilityTitle><@s.text name='manage.overview.visibility'/></#assign>
                                <#assign licenseTitle><@s.text name='eml.intellectualRights.license'/></#assign>

                                <div class="row g-2">
                                    <#if resource.lastPublished??>
                                        <#assign lastPublishedVersionStatus>${resource.getLastPublishedVersionsPublicationStatus()?lower_case}</#assign>

                                        <div class="col-xl-6" style="height: 100%">
                                            <div class="d-flex justify-content-between border rounded-2 mx-1 p-1 py-2 version-item text-smaller">
                                                <div class="ps-2 published-version-item-link">
                                                    <span>
                                                        <#if lastPublishedVersionStatus == "registered">
                                                            <i class="bi bi-circle-fill status-registered" title="<@s.text name="resource.status.registered"/>"></i>
                                                        <#elseif lastPublishedVersionStatus == "public">
                                                            <i class="bi bi-circle status-public" title="<@s.text name="resource.status.public"/>"></i>
                                                        <#elseif lastPublishedVersionStatus == "private">
                                                            <i class="bi bi-circle status-private" title="<@s.text name="resource.status.private"/>"></i>
                                                        <#elseif lastPublishedVersionStatus == "deleted">
                                                            <i class="bi bi-circle-fill status-deleted" title="<@s.text name="resource.status.deleted"/>"></i>
                                                        </#if>
                                                    </span>
                                                    <span class="me-2 overview-version-title">
                                                        <#if isDataPackage>
                                                            <strong><@s.text name="footer.version"/> ${resource.metadataVersion.toPlainString()}</strong>
                                                        <#else>
                                                            <strong><@s.text name="footer.version"/> ${resource.emlVersion.toPlainString()}</strong>
                                                        </#if>
                                                    </span><br>
                                                    <span class="fs-smaller-2">
                                                        <small>
                                                            ${releasedTitle?cap_first} ${resource.lastPublished?datetime?string.medium}
                                                        </small>
                                                    </span><br>
                                                    <span class="fs-smaller-2 text-nowrap version-pill version-current mt-2 mb-1">${lastPublishedTitle?upper_case}</span>
                                                    <#if resource.isAlreadyAssignedDoi()>
                                                        <span title="DOI" class="fs-smaller-2 text-nowrap doi-pill doi-pill-current mt-2 mb-1"><strong>DOI</strong> ${resource.versionHistory[0].doi!}</span>
                                                    </#if>
                                                    <#if !resource.isDataPackage()>
                                                        <span title="${licenseTitle?cap_first}" class="fs-smaller-2 text-nowrap license-pill license-pill-current mt-2 mb-1"><@shortLicense action.getLastPublishedVersionAssignedLicense(resource)!/></span><br>
                                                    <#else>
                                                        <#if !(resource.dataPackageMetadata.licenses)?has_content && !(resource.dataPackageMetadata.license)?has_content>
                                                            <span title="${licenseTitle?cap_first}" class="fs-smaller-2 text-nowrap license-pill license-pill-current mt-2 mb-1"><@s.text name="manage.overview.published.licenseNotSet"/></span><br>
                                                        <#elseif resource.coreType?? && resource.coreType == "camtrap-dp">
                                                            <#list resource.dataPackageMetadata.licenses as license>
                                                                <#if license.scope?? && license.scope == "data">
                                                                    <span title="${licenseTitle?cap_first}" class="fs-smaller-2 text-nowrap license-pill license-pill-current mt-2 mb-1">${(license.name)!}</span><br>
                                                                </#if>
                                                            </#list>
                                                        <#elseif resource.coreType?? && resource.coreType == "coldp">
                                                            <span title="${licenseTitle?cap_first}" class="fs-smaller-2 text-nowrap license-pill license-pill-current mt-2 mb-1">${(resource.dataPackageMetadata.license)!}</span><br>
                                                        </#if>
                                                    </#if>
                                                </div>

                                                <div class="d-flex justify-content-end my-auto version-item-actions">
                                                    <a title="<@s.text name="button.view"/>" class="icon-button icon-material-actions version-item-action fs-smaller-2 d-sm-max-none" type="button" href="${baseURL}/resource?r=${resource.shortname}">
                                                        <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                            <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                            <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                        </svg>
                                                    </a>

                                                    <#if (resource.coreType)! != "metadata">
                                                        <a title="${pubLogTitle?cap_first}" class="icon-button icon-material-actions version-item-action fs-smaller-2 d-sm-max-none" type="button" href="${baseURL}/publicationlog.do?r=${resource.shortname}">
                                                            <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                <path d="M19 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.89-2-2-2zm0 16H5V7h14v12zm-2-7H7v-2h10v2zm-4 4H7v-2h6v2z"></path>
                                                            </svg>
                                                        </a>

                                                        <#if report??>
                                                            <a title="${pubRepTitle?cap_first}" id="toggleReport" class="icon-button icon-material-actions version-item-action fs-smaller-2 d-sm-max-none" type="button" href="#anchor-publish">
                                                                <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                    <path d="M19 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.89-2-2-2zm0 16H5V7h14v12zm-5.5-6c0 .83-.67 1.5-1.5 1.5s-1.5-.67-1.5-1.5.67-1.5 1.5-1.5 1.5.67 1.5 1.5zM12 9c-2.73 0-5.06 1.66-6 4 .94 2.34 3.27 4 6 4s5.06-1.66 6-4c-.94-2.34-3.27-4-6-4zm0 6.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"></path>
                                                                </svg>
                                                            </a>
                                                        </#if>
                                                    </#if>

                                                    <div class="dropdown d-sm-none">
                                                        <a class="icon-button icon-material-actions version-item-action" type="button" href="#" id="dropdown-version-item-actions-current" data-bs-toggle="dropdown" aria-expanded="false">
                                                            <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                <path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"></path>
                                                            </svg>
                                                        </a>

                                                        <ul class="dropdown-menu" aria-labelledby="dropdown-version-item-actions-current">
                                                            <li>
                                                                <a class="dropdown-item action-link" type="button" href="${baseURL}/resource?r=${resource.shortname}">
                                                                    <svg class="overview-item-action-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                        <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                                        <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                                    </svg>
                                                                    <@s.text name="button.view"/>
                                                                </a>
                                                            </li>

                                                            <#if (resource.coreType)! != "metadata">
                                                                <li>
                                                                    <a class="dropdown-item action-link" type="button" target="_blank" href="${baseURL}/publicationlog.do?r=${resource.shortname}">
                                                                        <svg class="overview-item-action-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                            <path d="M19 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.89-2-2-2zm0 16H5V7h14v12zm-2-7H7v-2h10v2zm-4 4H7v-2h6v2z"></path>
                                                                        </svg>
                                                                        ${pubLogTitle?cap_first}
                                                                    </a>
                                                                </li>


                                                                <#if report??>
                                                                    <li>
                                                                        <a id="toggleReport" class="dropdown-item action-link" type="button" href="#anchor-publish">
                                                                            <svg class="overview-item-action-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                                <path d="M19 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.89-2-2-2zm0 16H5V7h14v12zm-5.5-6c0 .83-.67 1.5-1.5 1.5s-1.5-.67-1.5-1.5.67-1.5 1.5-1.5 1.5.67 1.5 1.5zM12 9c-2.73 0-5.06 1.66-6 4 .94 2.34 3.27 4 6 4s5.06-1.66 6-4c-.94-2.34-3.27-4-6-4zm0 6.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"></path>
                                                                            </svg>
                                                                            <span id="toggleReport-text">
                                                                                ${pubRepTitle?cap_first}
                                                                            </span>
                                                                        </a>
                                                                    </li>
                                                                </#if>
                                                            </#if>
                                                        </ul>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </#if>

                                    <div class="col-xl-6" style="height: 100%">
                                        <#assign nextVersionStatus>${resource.status?lower_case}</#assign>

                                        <div class="d-flex justify-content-between border rounded-2 mx-1 p-1 py-2 version-item text-smaller">
                                            <div class="ps-2 next-version-item-link">
                                                <span>
                                                    <#if nextVersionStatus == "registered">
                                                        <i class="bi bi-circle-fill status-registered" title="<@s.text name="resource.status.registered"/>"></i>
                                                    <#elseif nextVersionStatus == "public">
                                                        <i class="bi bi-circle status-public" title="<@s.text name="resource.status.public"/>"></i>
                                                    <#elseif nextVersionStatus == "private">
                                                        <i class="bi bi-circle status-private" title="<@s.text name="resource.status.private"/>"></i>
                                                    <#elseif nextVersionStatus == "deleted">
                                                        <i class="bi bi-circle-fill status-deleted" title="<@s.text name="resource.status.deleted"/>"></i>
                                                    </#if>
                                                </span>
                                                <span class="me-2 overview-version-title">
                                                    <strong><@s.text name="footer.version"/> ${resource.nextVersionPlainString}</strong>
                                                </span><br>
                                                <span class="fs-smaller-2">
                                                    <small>
                                                        <#if resource.nextPublished??>
                                                            ${releasedTitle?cap_first} ${resource.nextPublished?datetime?string.medium}
                                                        <#else>
                                                            <@s.text name="manage.overview.published.date.not.set"/>
                                                        </#if>
                                                    </small>
                                                </span><br>
                                                <span class="fs-smaller-2 text-nowrap version-pill version-next mt-2 mb-1">
                                                    ${nextPublishedTitle?upper_case}
                                                </span>
                                                <#if resource.doi??>
                                                    <span title="DOI" class="fs-smaller-2 text-nowrap doi-pill doi-pill-next mt-2 mb-1"><strong>DOI</strong> ${resource.versionHistory[0].doi!}</span>
                                                </#if>
                                                <#if (resource.eml)?has_content && !resource.isDataPackage()>
                                                    <#if resource.getEml().parseLicenseUrl()?has_content>
                                                        <span title="${licenseTitle?cap_first}" class="fs-smaller-2 text-nowrap license-pill license-pill-next mt-2 mb-1"><@shortLicense resource.getEml().parseLicenseUrl()/></span><br>
                                                    <#else>
                                                        <span title="${licenseTitle?cap_first}" class="fs-smaller-2 text-nowrap license-pill license-pill-next mt-2 mb-1"><@s.text name="manage.overview.published.licenseNotSet"/></span><br>
                                                    </#if>
                                                <#elseif resource.isDataPackage()>
                                                    <#if !(resource.dataPackageMetadata.licenses)?has_content && !(resource.dataPackageMetadata.license)?has_content>
                                                        <span title="${licenseTitle?cap_first}" class="fs-smaller-2 text-nowrap license-pill license-pill-next mt-2 mb-1"><@s.text name="manage.overview.published.licenseNotSet"/></span><br>
                                                    <#elseif resource.coreType?? && resource.coreType == "camtrap-dp">
                                                        <#list resource.dataPackageMetadata.licenses as license>
                                                            <#if license.scope?? && license.scope == "data">
                                                                <span title="${licenseTitle?cap_first}" class="fs-smaller-2 text-nowrap license-pill license-pill-next mt-2 mb-1">${(license.name)!}</span><br>
                                                            </#if>
                                                        </#list>
                                                    <#elseif resource.coreType?? && resource.coreType == "coldp">
                                                        <span title="${licenseTitle?cap_first}" class="fs-smaller-2 text-nowrap license-pill license-pill-next mt-2 mb-1">${(resource.dataPackageMetadata.license)!}</span><br>
                                                    </#if>
                                                </#if>
                                            </div>

                                            <div class="d-flex justify-content-end my-auto version-item-actions">
                                                <#assign doiActionName>
                                                    <#if !organisationWithPrimaryDoiAccount?? || !currentUser.hasRegistrationRights() ||  resource.identifierStatus == "UNRESERVED"><@s.text name="button.reserve"/> DOI<#t>
                                                    <#elseif resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION"><@s.text name="button.delete"/> DOI<#t>
                                                    <#elseif resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi()><@s.text name="button.reserve.new"/> DOI<#t>
                                                    <#else><@s.text name="button.reserve"/> DOI<#t>
                                                    </#if>
                                                </#assign>

                                                <#if !missingMetadata>
                                                    <a title="<@s.text name="button.preview"/>" class="icon-button icon-material-actions version-item-action fs-smaller-2 d-sm-max-none" type="button" href="${baseURL}/resource/preview?r=${resource.shortname}">
                                                        <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                            <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                            <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                        </svg>
                                                    </a>
                                                </#if>

                                                <#if organisationWithPrimaryDoiAccount??>
                                                    <a title="${doiActionName}" id="reserve-doi" class="icon-button icon-material-actions version-item-action fs-smaller-2 d-sm-max-none" type="button" href="#">
                                                        <svg class="icon-button-svg" viewBox="0 0 24 24">
                                                            <path d="M4 10h3v7H4zm6.5 0h3v7h-3zM2 19h20v3H2zm15-9h3v7h-3zm-5-9L2 6v2h20V6z"></path>
                                                        </svg>
                                                    </a>
                                                </#if>

                                                <div class="dropdown d-sm-none">
                                                    <a class="icon-button icon-material-actions version-item-action" type="button" href="#" id="dropdown-version-item-actions-pending" data-bs-toggle="dropdown" aria-expanded="false">
                                                        <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                            <path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"></path>
                                                        </svg>
                                                    </a>

                                                    <ul class="dropdown-menu" aria-labelledby="dropdown-version-item-actions-pending">
                                                        <#if !missingMetadata>
                                                            <li>
                                                                <a class="dropdown-item action-link" type="button" href="${baseURL}/resource/preview?r=${resource.shortname}">
                                                                    <svg class="overview-item-action-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                        <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                                        <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                                    </svg>
                                                                    <@s.text name="button.preview"/>
                                                                </a>
                                                            </li>
                                                        </#if>
                                                        <li>
                                                            <a id="reserve-doi" class="dropdown-item action-link" type="button" href="#">
                                                                <svg class="overview-item-action-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                    <path d="M4 10h3v7H4zm6.5 0h3v7h-3zM2 19h20v3H2zm15-9h3v7h-3zm-5-9L2 6v2h20V6z"></path>
                                                                </svg>
                                                                ${doiActionName}
                                                            </a>
                                                        </li>
                                                    </ul>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div class="mt-2">
                                    <#if report??>
                                        <table>
                                            <tr id="dwcaReport" style="display: none;">
                                                <td colspan="2">
                                                    <div class="report text-smaller">
                                                        <ul class="simple list-unstyled">
                                                            <#list report.messages as msg>
                                                                <li class="${msg.level}"><span
                                                                            class="small">${msg.date?time?string}</span> ${msg.message}
                                                                </li>
                                                            </#list>
                                                        </ul>
                                                        <#if cfg.debug() && report.hasException()>
                                                            <br/>
                                                            <ul class="simple">
                                                                <li>
                                                                    <strong>Exception</strong> ${report.exceptionMessage!}
                                                                </li>
                                                                <#list report.exceptionStacktrace as msg>
                                                                    <li>${msg}</li>
                                                                </#list>
                                                            </ul>
                                                        </#if>
                                                    </div>
                                                </td>
                                            </tr>
                                        </table>
                                    </#if>
                                </div>
                            </div>
                        </div>
                    </div>

                    <span class="anchor anchor-home-resource-page" id="anchor-registration"></span>
                    <div class="py-5 border-bottom section" id="registration">

                        <div class="d-flex justify-content-between">
                            <div class="d-flex">
                                <h5 class="my-auto text-gbif-header-2 fw-400">
                                    <#assign registrationTitleInfo>
                                        <@s.text name="manage.resource.status.intro.registration"/>
                                        <#if !resource.dataPackage??><br><br><@s.text name='manage.resource.status.intro.public.migration'><@s.param><a href="${baseURL}/manage/metadata-additional.do?r=${resource.shortname}&amp;edit=Edit"><@s.text name="submenu.additional"/></a></@s.param></@s.text></#if>
                                        <br><br>
                                        <@s.text name='manage.resource.status.intro.public.gbifWarning'/>
                                    </#assign>

                                    <@popoverTextInfo registrationTitleInfo/>
                                    <@s.text name='manage.overview.registration'/>
                                </h5>
                            </div>

                            <div class="d-flex justify-content-end">
                                <#if resource.status!="REGISTERED">
                                    <#if !currentUser.hasRegistrationRights()>
                                        <!-- Hide register button and show warning: user must have registration rights -->
                                        <#assign visibilityConfirmRegistrationWarning>
                                            <@s.text name="manage.resource.status.registration.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/>
                                        </#assign>
                                        <button id="show-registration-disabled-modal" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button" type="button">
                                            <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                                <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                                            </svg>
                                            <@s.text name='button.register'/>
                                        </button>
                                    <#elseif missingValidPublishingOrganisation?string == "true">
                                        <!-- Hide register button and show warning: user must assign valid publishing organisation -->
                                        <button id="show-registration-disabled-modal" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button" type="button">
                                            <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                                <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                                            </svg>
                                            <@s.text name='button.register'/>
                                        </button>
                                    <#elseif missingRegistrationMetadata?string == "true">
                                        <!-- Hide register button and show warning: user must fill in minimum registration metadata -->
                                        <button id="show-registration-disabled-modal" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button" type="button">
                                            <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                                <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                                            </svg>
                                            <@s.text name='button.register'/>
                                        </button>
                                    <#elseif !resource.isLastPublishedVersionPublic()>
                                        <!-- Hide register button and show warning: last published version must be publicly available to register -->
                                        <button id="show-registration-disabled-modal" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button" type="button">
                                            <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                                <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                                            </svg>
                                            <@s.text name='button.register'/>
                                        </button>
                                    <#elseif !action.isLastPublishedVersionAssignedGBIFSupportedLicense(resource)>
                                        <!-- Hide register button and show warning: resource must be assigned a GBIF-supported license to register if resource has occurrence data -->
                                        <button id="show-registration-disabled-modal" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button" type="button">
                                            <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                                <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                                            </svg>
                                            <@s.text name='button.register'/>
                                        </button>
                                    <#else>
                                        <form action="resource-registerResource.do" method="post">
                                            <input name="r" type="hidden" value="${resource.shortname}"/>
                                            <button id="register-resource-button" class="confirmRegistration text-gbif-header-2 icon-button icon-material-actions overview-action-button" type="submit">
                                                <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                                    <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                                                </svg>
                                                <@s.text name='button.register'/>
                                            </button>
                                        </form>
                                    </#if>
                                <#else>
                                    <button id="show-registration-disabled-modal" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button" type="button">
                                        <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                            <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                                        </svg>
                                        <@s.text name='button.register'/>
                                    </button>
                                </#if>
                            </div>
                        </div>

                        <div class="row mt-4">
                            <#if resource.status=="REGISTERED" && resource.key??>
                                <p class="mb-0">
                                    <@s.text name="manage.overview.registration.intro"/>
                                </p>

                                <div class="details mt-3">
                                    <div class="row g-2">
                                        <div class="col-xl-6">
                                            <div class="d-flex registration-item border rounded-2 mx-1 p-1 py-2 text-smaller">
                                                <div class="my-auto ps-2 d-flex">
                                                    <svg class="gbif-logo-icon" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" viewBox="0 0 539.7 523.9" style="display: inline-block;" xml:space="preserve">
                                                        <path class="ipt-icon-piece" d="M230.7,255.5c0-102.2,49.9-190.7,198.4-190.7C429.1,167.2,361.7,255.5,230.7,255.5"></path>
                                                        <path class="ipt-icon-piece" d="M468.6,523.9c27.8,0,49.2-4,71.1-12c0-80.9-48.3-138.7-133.5-180.4c-65.2-32.7-145.5-49.7-218.8-49.7C219.5,185.4,196.1,65.7,165,0c-34.5,68.8-56,186.8-22.9,282.8C77,287.6,25.4,315.9,3.6,353.3c-1.6,2.8-5,8.9-3,10c1.6,0.8,4.1-1.7,5.6-3.1c23.5-21.8,54.6-32.4,84.5-32.4c69.1,0,117.8,57.3,152.3,91.7C317.1,493.5,389.4,524.1,468.6,523.9"></path>
                                                    </svg>
                                                </div>

                                                <div class="my-auto ps-2 text-truncate registration-item-link fs-smaller-2 me-auto">
                                                    <strong class="overview-registered-title fs-smaller">${resource.title!resource.shortname}</strong>
                                                    <br>
                                                    <small>${resource.key}</small>
                                                    <#if resource.organisation??>
                                                        |
                                                        <small>${resource.organisation.name!"Organisation"}</small>
                                                    </#if>
                                                </div>

                                                <div class="d-flex justify-content-end my-auto registration-item-actions">
                                                    <a title="<@s.text name="manage.overview.registration.view"/>" class="icon-button icon-material-actions network-item-action fs-smaller-2 d-sm-max-none" type="button" href="${cfg.portalUrl}/dataset/${resource.key!}">
                                                        <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                            <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                        </svg>
                                                    </a>

                                                    <div class="dropdown d-sm-none">
                                                        <a class="icon-button icon-material-actions registration-item-action" type="button" href="#" id="dropdown-registration-item-actions-current" data-bs-toggle="dropdown" aria-expanded="false">
                                                            <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                <path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"></path>
                                                            </svg>
                                                        </a>

                                                        <ul class="dropdown-menu" aria-labelledby="dropdown-registration-item-actions-current">
                                                            <li>
                                                                <a class="dropdown-item action-link" type="button" href="${cfg.portalUrl}/dataset/${resource.key!}">
                                                                    <svg class="overview-item-action-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                        <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                                    </svg>
                                                                    <@s.text name="manage.overview.registration.view"/>
                                                                </a>
                                                            </li>
                                                        </ul>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            <#else>
                                <p class="mb-0">
                                    <@s.text name="manage.overview.registration.notRegistered"/>
                                </p>
                            </#if>
                        </div>
                    </div>

                    <span class="anchor anchor-home-resource-page" id="anchor-autopublish"></span>
                    <div class="py-5 border-bottom section" id="autopublish">
                        <div class="d-flex justify-content-between">
                            <div class="d-flex">
                                <h5 class="my-auto text-gbif-header-2 fw-400">
                                    <@popoverPropertyInfo "manage.overview.autopublish.description"/>
                                    <@s.text name="manage.overview.autopublish.title"/>
                                </h5>
                            </div>

                            <div class="d-flex justify-content-end">
                                <a id="edit-autopublish-button" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button" type="button" href="auto-publish.do?r=${resource.shortname}">
                                    <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                        <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                                    </svg>
                                    <@s.text name="button.edit"/>
                                </a>
                            </div>
                        </div>

                        <div class="mt-4">
                            <p class="mb-0">
                                <#if resource.usesAutoPublishing()>
                                    <span class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill autopublish-enabled">
                                        <@s.text name="manage.overview.autopublish.enabled"/>: ${autoPublishFrequencies.get(resource.updateFrequency.identifier)}
                                    </span>
                                    <@s.text name="manage.overview.autopublish.intro.activated"/>
                                <#else>
                                    <span class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill autopublish-disabled">
                                        <@s.text name="manage.overview.autopublish.disabled"/>
                                    </span>
                                    <@s.text name="manage.overview.autopublish.intro.deactivated"/>
                                </#if>
                            </p>

                            <#if resource.isDeprecatedAutoPublishingConfiguration()>
                                <div class="callout callout-warning text-smaller">
                                    <@s.text name="manage.overview.autopublish.deprecated.warning.button" escapeHtml=true/>
                                </div>
                            </#if>
                        </div>
                    </div>

                    <span class="anchor anchor-home-resource-page" id="anchor-networks"></span>
                    <div class="py-5 border-bottom section" id="networks">
                        <div class="d-flex justify-content-between">
                            <div class="d-flex">
                                <h5 class="my-auto text-gbif-header-2 fw-400">
                                    <@popoverPropertyInfo "manage.overview.networks.description"/>
                                    <@s.text name="manage.overview.networks.title"/>
                                </h5>
                            </div>

                            <div class="d-flex justify-content-end">
                                <a id="add-network-button" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button" type="button" href="#">
                                    <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                    </svg>
                                    <@s.text name="button.add"/>
                                </a>
                            </div>
                        </div>

                        <div class="mt-4">
                            <#if resource.key?has_content && (resourceNetworks?size>0)>
                                <p class="mb-0">
                                    <@s.text name="manage.overview.networks.intro"/>
                                </p>
                                <div class="details mt-3">
                                    <div class="row g-2">
                                        <#list resourceNetworks as n>
                                            <div class="col-xl-6">
                                                <div class="d-flex border rounded-2 mx-1 p-1 py-2 network-item" data-ipt-network-key="${(n.key)!}">
                                                    <div class="my-auto ps-2 d-flex">
                                                        <i class="bi bi-globe2 me-1 text-gbif-primary"></i>
                                                    </div>

                                                    <div class="my-auto ps-2 text-truncate network-item-link fs-smaller-2 me-auto">
                                                        <strong class="fs-smaller">${n.title!""}</strong><br>
                                                        <small>${(n.key)!}</small>
                                                    </div>

                                                    <div class="d-flex justify-content-end my-auto network-item-actions">
                                                        <a title="<@s.text name="manage.overview.networks.view.gbif"/>" class="icon-button icon-material-actions network-item-action fs-smaller-2 d-sm-max-none" type="button" href="${cfg.portalUrl}/network/${n.key!}">
                                                            <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                            </svg>
                                                        </a>

                                                        <a title="<@s.text name="button.delete"/>" class="icon-button icon-material-actions network-item-action fs-smaller-2 d-sm-max-none" type="button" href="resource-deleteNetwork.do?r=${resource.shortname}&id=${n.key!}">
                                                            <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                            </svg>
                                                        </a>

                                                        <div class="dropdown d-sm-none">
                                                            <a class="icon-button icon-material-actions network-item-action" type="button" href="#" id="dropdown-network-item-actions-current" data-bs-toggle="dropdown" aria-expanded="false">
                                                                <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                    <path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"></path>
                                                                </svg>
                                                            </a>

                                                            <ul class="dropdown-menu" aria-labelledby="dropdown-network-item-actions-current">
                                                                <li>
                                                                    <a class="dropdown-item action-link" type="button" href="${cfg.portalUrl}/network/${n.key!}">
                                                                        <svg class="overview-item-action-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                            <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                                        </svg>
                                                                        <@s.text name="manage.overview.networks.view.gbif"/>
                                                                    </a>
                                                                </li>
                                                                <li>
                                                                    <a class="dropdown-item action-link" type="button" href="resource-deleteNetwork.do?r=${resource.shortname}&id=${n.key!}">
                                                                        <svg class="overview-item-action-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                                        </svg>
                                                                        <@s.text name="button.delete"/>
                                                                    </a>
                                                                </li>
                                                            </ul>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </#list>
                                    </div>
                                </div>
                            <#else>
                                <p class="mb-0">
                                    <@s.text name="manage.overview.networks.noNetworks"/>
                                </p>
                            </#if>
                        </div>
                    </div>

                    <span class="anchor anchor-home-resource-page" id="anchor-managers"></span>
                    <div class="py-5" id="managers">
                        <div class="d-flex justify-content-between">
                            <div class="d-flex">
                                <h5 class="my-auto text-gbif-header-2 fw-400">
                                    <@popoverPropertyInfo "manage.overview.resource.managers.description"/>
                                    <@s.text name="manage.overview.resource.managers"/>
                                </h5>
                            </div>

                            <div class="d-flex justify-content-end">
                                <a id="add-manager-button" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button" type="button" href="#">
                                    <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                    </svg>
                                    <@s.text name="button.add"/>
                                </a>
                            </div>
                        </div>

                        <div class="mt-4">
                            <p class="mb-0">
                                <@s.text name="manage.overview.resource.managers.intro"><@s.param>${resource.shortname}</@s.param></@s.text>
                            </p>

                            <div class="details mt-3">
                                <div class="row g-2">
                                    <div class="col-xl-6">
                                        <div class="d-flex border rounded-2 mx-1 p-1 py-2 manager-item">
                                            <div class="my-auto ps-2 d-flex">
                                                <i class="bi bi-person me-1 text-gbif-primary"></i>
                                            </div>
                                            <div class="my-auto text-truncate ps-2 fs-smaller-2">
                                                <strong class="fs-smaller">${resource.creator.name!}</strong><br>
                                                <small><@s.text name="manage.overview.resource.managers.creator"/>
                                                    | ${resource.creator.email}</small>
                                            </div>
                                            <div class="d-flex justify-content-end my-auto manager-item-actions"></div>
                                        </div>
                                    </div>

                                    <#if (resource.managers?size>0)>
                                        <#list resource.managers as u>
                                            <div class="col-xl-6">
                                                <div class="d-flex border rounded-2 mx-1 p-1 py-2 manager-item">
                                                    <div class="my-auto ps-2 d-flex">
                                                        <i class="bi bi-person me-1 text-gbif-primary"></i>
                                                    </div>
                                                    <div class="my-auto text-truncate ps-2 fs-smaller-2 me-auto">
                                                        <strong class="fs-smaller">${u.name}</strong><br>
                                                        <small><@s.text name="manage.overview.resource.managers.manager"/>
                                                            | ${u.email}</small>
                                                    </div>
                                                    <div class="d-flex justify-content-end my-auto manager-item-actions">
                                                        <a title="<@s.text name="button.delete"/>" class="icon-button icon-material-actions manager-item-action fs-smaller-2 d-sm-max-none" type="button" href="resource-deleteManager.do?r=${resource.shortname}&id=${u.email}">
                                                            <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                            </svg>
                                                        </a>

                                                        <div class="dropdown d-sm-none">
                                                            <a class="icon-button icon-material-actions manager-item-action" type="button" href="#" id="dropdown-manager-item-actions-current" data-bs-toggle="dropdown" aria-expanded="false">
                                                                <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                    <path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"></path>
                                                                </svg>
                                                            </a>

                                                            <ul class="dropdown-menu" aria-labelledby="dropdown-manager-item-actions-current">
                                                                <li>
                                                                    <a class="dropdown-item action-link" type="button" href="resource-deleteManager.do?r=${resource.shortname}&id=${u.email}">
                                                                        <svg class="overview-item-action-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                                        </svg>
                                                                        <@s.text name="button.delete"/>
                                                                    </a>
                                                                </li>
                                                            </ul>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </#list>
                                    </#if>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

            </main>
        </div>
            </main>
        </div>
    </div>

    <div id="make-public-modal" class="modal fade" tabindex="-1" aria-labelledby="make-public-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <h5 class="modal-title w-100" id="make-public-modal-title"><@s.text name="manage.overview.visibility.change.public"/></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">×</button>
                </div>
                <div class="modal-body">
                    <div>
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="makePublicOptions" id="makePublicImmediately" value="makePublicImmediately" <#if !resource.makePublicDate?has_content>checked</#if> >
                            <label class="form-check-label" for="inlineRadio1"><@s.text name="manage.overview.visibility.change.public.immediately"/></label>
                        </div>
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="makePublicOptions" id="makePublicAtDate" value="makePublicAtDate" <#if resource.makePublicDate?has_content>checked</#if> >
                            <label class="form-check-label" for="inlineRadio2"><@s.text name="manage.overview.visibility.change.public.date"/></label>
                        </div>
                    </div>

                    <div id="makePublicDateTimeWrapper" class="mt-3" style="display: none !important;">
                        <div class="d-flex justify-content-center">
                            <form id="make-public-modal-form" action="resource-makePublic.do" method="post">
                                <input name="r" type="hidden" value="${resource.shortname}"/>
                                <#if resource.makePublicDate?has_content>
                                    <input id="makePublicDateTime" name="makePublicDateTime" class="form-control" type="datetime-local" value="${resource.makePublicDate?datetime?string["yyyy-MM-dd'T'HH:mm"]}" />
                                <#else>
                                    <input id="makePublicDateTime" name="makePublicDateTime" class="form-control" type="datetime-local" value="${makePublicDateTime!}" />
                                </#if>
                            </form>
                            <form id="cancel-make-public" action="resource-cancelMakePublic.do" method="post">
                                <input name="r" type="hidden" value="${resource.shortname}"/>
                            </form>
                        </div>
                    </div>
                </div>
                <div class="modal-footer justify-content-center">
                    <button id="changeStateSubmit" type="submit" form="make-public-modal-form" class="btn btn-outline-gbif-primary"><@s.text name="button.submit"/></button>
                    <#if resource.makePublicDate?has_content>
                        <button id="cancelMakePublic" type="submit" form="cancel-make-public" class="btn btn-outline-gbif-danger"><@s.text name="button.reset"/></button>
                    </#if>
                </div>
            </div>
        </div>
    </div>

    <div id="visibility-disabled-modal" class="modal fade" tabindex="-1" aria-labelledby="make-public-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <h5 class="modal-title w-100" id="make-public-modal-title"><@s.text name="manage.overview.visibility"/></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">×</button>
                </div>
                <div class="modal-body">
                    <#if resource.status == "DELETED">
                        <div class="callout callout-warning text-smaller">
                            <@s.text name="manage.overview.visibility.warning.deleted"/>
                        </div>
                    </#if>

                    <#if resource.status == "REGISTERED">
                        <div class="callout callout-warning text-smaller">
                            <@s.text name="manage.overview.visibility.warning.registered"/>
                        </div>
                    </#if>
                </div>
            </div>
        </div>
    </div>

    <div id="source-data-modal" class="modal fade" tabindex="-1" aria-labelledby="source-data-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <h5 class="modal-title w-100" id="source-data-modal-title"><@s.text name="manage.source.title"/></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">×</button>
                </div>
                <div class="modal-body">
                    <div>
                        <form id="addSourceForm" action='addsource.do' method='post' enctype="multipart/form-data">
                            <input name="r" type="hidden" value="${resource.shortname}"/>
                            <input name="validate" type="hidden" value="false"/>

                            <div class="row">
                                <div class="col-12">
                                    <div class="tabs-root">
                                        <div class="tabs-scroller tabs-fixed" style="overflow:hidden;margin-bottom:0">
                                            <div class="tabs-flexContainer tabs-centered" role="tablist">
                                                <button id="tab-source-file" class="sources-tab-root tab-selected" type="button" role="tab">
                                                    <@s.text name="manage.source.file"/>
                                                    <span id="tab-indicator-source-file" class="tabs-indicator"></span>
                                                </button>
                                                <button id="tab-source-url" class="sources-tab-root" type="button" role="tab">
                                                    URL
                                                    <span id="tab-indicator-source-url" class="tabs-indicator" style="display: none;"></span>
                                                </button>
                                                <button id="tab-source-sql" class="sources-tab-root" type="button" role="tab">
                                                    SQL
                                                    <span id="tab-indicator-source-sql" class="tabs-indicator" style="display: none;"></span>
                                                </button>
                                            </div>

                                        </div>
                                    </div>
                                </div>
                                <div class="col-12">
                                    <a id="chooseFilesButton" href="#" class="btn btn-outline-gbif-primary mt-3"><@s.text name="button.chooseFiles"/></a>

                                    <div id="callout-source-exists" class="callout callout-danger text-smaller" style="display: none;">
                                        <@s.text name="manage.resource.addSource.sameName.confirm"/>
                                        <br>
                                        <button id="btn-confirm-source-overwrite" class="btn btn-sm btn-outline-gbif-danger mt-3"><@s.text name="button.confirm"/></button>
                                    </div>

                                    <div id="callout-not-enough-space" class="callout callout-danger text-smaller" style="display: none;">
                                        <@s.text name="manage.resource.addSource.notEnoughSpace"><@s.param>${freeDiscSpaceReadable}</@s.param></@s.text>
                                    </div>

                                    <input id="fileInput" type="file" multiple style="display: none;" />
                                    <div id="fileList"></div>
                                    <a id="sendButton" href="#" class="btn btn-outline-gbif-primary" style="display: none;"><@s.text name="button.upload"/></a>

                                    <ul id="field-error-file" class="invalid-feedback list-unstyled field-error my-1">
                                        <li class="text-start">
                                            <span><@s.text name="manage.overview.source.file.too.big"/></span>
                                        </li>
                                    </ul>

                                    <div>
                                        <input type="text" id="sourceName" name="sourceName" class="form-control my-1" placeholder="<@s.text name='source.name'/>" style="display: none">
                                        <ul id="field-error-sourceName" class="invalid-feedback list-unstyled field-error my-1">
                                            <li class="text-start">
                                                <span><@s.text name="manage.source.name.empty"/></span>
                                            </li>
                                        </ul>
                                    </div>

                                    <div>
                                        <input type="url" id="url" name="url" class="form-control my-1" placeholder="URL" style="display: none">
                                        <ul id="field-error-url" class="invalid-feedback list-unstyled field-error my-1">
                                            <li class="text-start">
                                                <@s.text name="validation.required"><@s.param><@s.text name="manage.source.url"/></@s.param></@s.text>
                                            </li>
                                        </ul>
                                    </div>

                                    <input id="sourceType" type="hidden" name="sourceType" value="source-sql"/>
                                </div>
                                <div class="col-12">
                                    <@s.submit name="add" cssClass="btn btn-outline-gbif-primary my-1" cssStyle="display: none" key="button.connect"/>
                                    <@s.submit name="clear" cssClass="btn btn-outline-secondary my-1" cssStyle="display: none" key="button.clear"/>
                                </div>
                            </div>
                        </form>
                        <form action='canceloverwrite.do' method='post'>
                            <input name="r" type="hidden" value="${resource.shortname}"/>
                            <input name="validate" type="hidden" value="false"/>
                            <@s.submit name="canceloverwrite" key="button.cancel" cssStyle="display: none;" cssClass="btn btn-outline-secondary my-1"/>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="mapping-modal" class="modal fade" tabindex="-1" aria-labelledby="mapping-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <h5 class="modal-title w-100" id="mapping-modal-title"><@s.text name="manage.mapping.title"/></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">×</button>
                </div>
                <div class="modal-body">
                    <div>
                        <#if (potentialCores?size>0)>
                            <form action='mapping.do' method='post'>
                                <input name="r" type="hidden" value="${resource.shortname}"/>
                                <select name="id" class="form-select my-1" id="rowType" size="1">
                                    <optgroup label="<@s.text name='manage.overview.DwC.Mappings.cores.select'/>">
                                        <#list potentialCores as c>
                                            <#if c?has_content>
                                                <option value="${c.rowType}">${c.title}</option>
                                            </#if>
                                        </#list>
                                    </optgroup>
                                    <#if (potentialExtensions?size>0)>
                                        <optgroup label="<@s.text name='manage.overview.DwC.Mappings.extensions.select'/>">
                                            <#list potentialExtensions as e>
                                                <#if e?has_content>
                                                    <option value="${e.rowType}">${e.title}</option>
                                                </#if>
                                            </#list>
                                        </optgroup>
                                    </#if>
                                </select>
                                <@s.submit name="add" cssClass="btn btn-outline-gbif-primary my-3" key="button.add"/>
                            </form>
                        <#else>
                            <#if dataPackageResource>
                                <div class="callout callout-warning text-smaller">
                                    <@s.text name="manage.overview.mappings.cantdo"/>
                                </div>
                            <#else>
                                <div class="callout callout-warning text-smaller">
                                    <@s.text name="manage.overview.DwC.Mappings.cantdo"/>
                                </div>
                            </#if>
                        </#if>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="metadata-modal" class="modal fade" tabindex="-1" aria-labelledby="metadata-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <h5 class="modal-title w-100" id="metadata-modal-title"><@s.text name="manage.overview.metadata"/></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">×</button>
                </div>
                <div class="modal-body">
                    <div>
                        <#if dataPackageResource>
                            <form id="upload-metadata-form" action='replace-datapackage-metadata.do' method='post' enctype="multipart/form-data">
                                <input name="r" type="hidden" value="${resource.shortname}"/>
                                <div class="row">
                                    <div class="col-12">
                                        <@s.file name="datapackageMetadataFile" cssClass="form-control my-1"/>
                                    </div>
                                    <div id="datapackage-metadata-validate" class="col-12 text-smaller" style="display: none;">
                                        <@checkbox name="validateDatapackageMetadata" i18nkey="button.validate" value="${validateDatapackageMetadata?c}"/>
                                    </div>
                                    <div class="col-12">
                                        <@s.submit name="datapackageMetadataReplace" cssClass="btn btn-sm btn-outline-gbif-primary my-1 confirmDatapackageMetadataReplace" cssStyle="display: none" key="button.replace"/>
                                        <@s.submit name="datapackageMetadataCancel" cssClass="btn btn-sm btn-outline-secondary my-1" cssStyle="display: none" key="button.cancel"/>
                                    </div>
                                </div>
                            </form>
                        <#else>
                            <form id="upload-metadata-form" action='replace-eml.do' method='post' enctype="multipart/form-data">
                                <input name="r" type="hidden" value="${resource.shortname}"/>
                                <div class="row">
                                    <div class="col-12">
                                        <@s.file name="emlFile" cssClass="form-control my-1"/>
                                    </div>
                                    <div id="eml-validate" class="col-12" style="display: none;">
                                        <@checkbox name="validateEml" i18nkey="button.validate" value="${validateEml?c}"/>
                                    </div>
                                    <div class="col-12">
                                        <@s.submit name="emlReplace" cssClass="btn btn-outline-gbif-primary my-1 confirmEmlReplace" cssStyle="display: none" key="button.replace"/>
                                        <@s.submit name="emlCancel" cssClass="btn btn-outline-secondary my-1" cssStyle="display: none" key="button.cancel"/>
                                    </div>
                                </div>
                            </form>
                        </#if>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="publication-modal" class="modal fade" tabindex="-1" aria-labelledby="publication-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <h5 class="modal-title w-100" id="publication-modal-title"><@s.text name="manage.overview.published"/></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">×</button>
                </div>

                <div class="modal-body">
                    <!-- resources cannot be published if it's deleted -->
                    <#if resource.status == "DELETED">
                        <div class="callout callout-warning text-smaller">
                            <@s.text name="manage.overview.published.deleted"/>
                        </div>

                        <!-- resources cannot be published if the mandatory metadata is missing -->
                    <#elseif missingMetadata>
                        <div class="callout callout-warning text-smaller">
                            <@s.text name="manage.overview.published.missing.metadata"/>
                        </div>

                      <!-- resources cannot be published if mappings are missing (for DPs) -->
                    <#elseif dataPackageResource && dataPackageMappingsMissing>
                        <div class="callout callout-warning text-smaller">
                            <@s.text name="manage.overview.published.missing.mappings"/>
                        </div>

                        <!-- resources that are already registered cannot be re-published if they haven't been assigned a GBIF-supported license -->
                    <#elseif resource.isRegistered() && !resource.isAssignedGBIFSupportedLicense()>
                        <div class="callout callout-warning text-smaller">
                            <@s.text name="manage.overview.prevented.resource.publishing.noGBIFLicense" />
                        </div>

                        <!-- resources with a reserved DOI, existing registered DOI, or registered with GBIF can only be republished by managers with registration rights -->
                    <#elseif (resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION")
                    || (resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi())
                    || resource.status == "REGISTERED">
                        <!-- the user must have registration rights -->
                        <#if !currentUser.hasRegistrationRights()>
                            <div class="callout callout-warning text-smaller">
                                <@s.text name="manage.resource.status.publication.forbidden"/>
                                &nbsp;<@s.text name="manage.resource.role.change"/>
                            </div>

                            <!-- an organisation with DOI account be activated (if resource has a reserved DOI or existing registered DOI) -->
                        <#elseif ((resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION" && resource.isAlreadyAssignedDoi())
                        || (resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi()))
                        && !organisationWithPrimaryDoiAccount??>
                            <div class="callout callout-warning text-smaller">
                                <@s.text name="manage.resource.status.publication.forbidden.account.missing" />
                            </div>

                            <!-- when a DOI is reserved.. -->
                        <#elseif resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION">
                            <!-- and the resource has no existing DOI and its status is private..  -->
                            <#if !resource.isAlreadyAssignedDoi() && resource.status == "PRIVATE">
                                <!-- and the resource has never been published before, the first publication is a new major version -->
                                <#if !resource.lastPublished??>
                                    <div class="callout callout-warning text-smaller">
                                        <@s.text name="manage.overview.publishing.doi.register.prevented.notPublic"/>
                                    </div>

                                    <!-- and the resource has been published before, the next publication is a new minor version -->
                                <#else>
                                    <div class="callout callout-warning text-smaller">
                                        <@s.text name="manage.overview.publishing.doi.register.prevented.notPublic" />
                                    </div>
                                </#if>

                                <!-- and its status is public (or registered), its reserved DOI can be registered during next publication  -->
                            <#elseif resource.status == "PUBLIC" || resource.status == "REGISTERED">
                                <div class="callout callout-info text-smaller">
                                    <@s.text name="manage.overview.publishing.doi.register.help"/>
                                </div>

                            </#if>
                        </#if>
                    </#if>
                </div>
            </div>
        </div>
    </div>

    <div id="networks-modal" class="modal fade" tabindex="-1" aria-labelledby="networks-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <h5 class="modal-title w-100" id="networks-modal-title"><@s.text name="manage.overview.networks.title"/></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">×</button>
                </div>
                <div class="modal-body">
                    <#if resource.status == "DELETED">
                        <div class="callout callout-warning text-smaller">
                            <@s.text name="manage.overview.networks.deleted"/>
                        </div>
                    <#elseif (potentialNetworks?size==0)>
                        <div class="callout callout-warning text-smaller">
                            <@s.text name="manage.overview.networks.select.empty"/>
                        </div>
                    <#elseif resource.key?has_content>
                        <div>
                            <div id="obis-network-validation-notification" class="callout callout-info text-smaller" style="display: none;">
                                <@s.text name="manage.overview.networks.obis.notification"/>
                            </div>
                            <form action='resource-addNetwork.do' method='post'>
                                <input name="r" type="hidden" value="${resource.shortname}"/>
                                <select name="id" class="form-select my-1" id="network" size="1">
                                    <option value="" disabled selected><@s.text name='manage.overview.networks.select'/></option>
                                    <#list potentialNetworks?sort_by("name") as n>
                                        <option value="${n.key}">${n.name}</option>
                                    </#list>
                                </select>
                                <@s.submit id="add-network" name="add" cssClass="btn btn-outline-gbif-primary my-3" key="button.add" cssStyle="display: none"/>
                            </form>
                        </div>
                    <#else>
                        <div class="callout callout-warning text-smaller">
                            <@s.text name="manage.overview.networks.not.registered"/>
                        </div>
                    </#if>
                </div>
            </div>
        </div>
    </div>

    <div id="registration-modal" class="modal fade" tabindex="-1" aria-labelledby="registration-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <h5 class="modal-title w-100" id="registration-modal-title"><@s.text name="manage.overview.registration"/></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">×</button>
                </div>
                <div class="modal-body">
                    <#if cfg.devMode() && cfg.getRegistryType()!='PRODUCTION'>
                        <div class="callout callout-warning text-smaller">
                            <@s.text name="manage.overview.published.testmode.warning"/>
                        </div>
                    </#if>

                    <#if resource.status=="DELETED">
                        <!-- Show warning: resource must not be deleted -->
                        <div class="callout callout-warning text-smaller">
                            <@s.text name="manage.overview.registration.deleted" />
                        </div>
                    <#elseif resource.status=="REGISTERED">
                        <!-- Show warning: resource already registered -->
                        <div class="callout callout-warning text-smaller">
                            <@s.text name="manage.overview.registration.registered" />
                        </div>
                    <#elseif resource.status=="PRIVATE">
                        <!-- Show warning: resource must be public -->
                        <div class="callout callout-warning text-smaller">
                            <@s.text name="manage.overview.registration.private" />
                        </div>
                    <#elseif resource.status=="PUBLIC">
                        <#if !currentUser.hasRegistrationRights()>
                            <!-- Show warning: user must have registration rights -->
                            <div class="callout callout-warning text-smaller">
                                <@s.text name="manage.resource.status.registration.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/>
                            </div>
                        <#elseif missingValidPublishingOrganisation?string == "true">
                            <!-- Show warning: user must assign valid publishing organisation -->
                            <div class="callout callout-warning text-smaller">
                                <@s.text name="manage.overview.visibility.missing.organisation"/>
                            </div>
                        <#elseif missingRegistrationMetadata?string == "true">
                            <!-- Show warning: user must fill in minimum registration metadata -->
                            <div class="callout callout-warning text-smaller">
                                <@s.text name="manage.overview.visibility.missing.metadata" />
                            </div>
                        <#elseif !resource.isLastPublishedVersionPublic()>
                            <!-- Show warning: last published version must be publicly available to register -->
                            <div class="callout callout-warning text-smaller">
                                <@s.text name="manage.overview.prevented.resource.registration.notPublic" />
                            </div>
                        <#elseif !action.isLastPublishedVersionAssignedGBIFSupportedLicense(resource)>
                            <!-- Show warning: resource must be assigned a GBIF-supported license to register if resource has occurrence data -->
                            <div class="callout callout-warning text-smaller">
                                <@s.text name="manage.overview.prevented.resource.registration.noGBIFLicense" escapeHtml=true/>
                            </div>
                        </#if>
                    </#if>
                </div>
            </div>
        </div>
    </div>

    <div id="managers-modal" class="modal fade" tabindex="-1" aria-labelledby="managers-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <h5 class="modal-title w-100" id="managers-modal-title"><@s.text name="manage.overview.resource.managers"/></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">×</button>
                </div>
                <div class="modal-body">
                    <#if (potentialManagers?size>0)>
                        <div>
                            <form action='resource-addManager.do' method='post'>
                                <input name="r" type="hidden" value="${resource.shortname}"/>
                                <select name="id" class="form-select my-1" id="manager" size="1">
                                    <option value="" disabled selected><@s.text name='manage.overview.resource.managers.select'/></option>
                                    <#list potentialManagers?sort_by("name") as u>
                                        <option value="${u.email}">${u.name}</option>
                                    </#list>
                                </select>
                                <@s.submit id="add-manager" name="add" cssClass="btn btn-outline-gbif-primary my-3" key="button.add" cssStyle="display: none"/>
                            </form>
                        </div>
                    <#else>
                        <div class="callout callout-warning text-smaller">
                            <@s.text name="manage.overview.resource.managers.select.empty"/>
                        </div>
                    </#if>
                </div>
            </div>
        </div>
    </div>

    <div id="reserve-doi-modal" class="modal fade" tabindex="-1" aria-labelledby="reserve-doi-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <h5 class="modal-title w-100" id="reserve-doi-modal-title">DOI</h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">×</button>
                </div>
                <div class="modal-body">
                    <#if !organisationWithPrimaryDoiAccount??>
                        <div class="callout callout-warning text-smaller">
                            <@s.text name="manage.overview.publishing.doi.reserve.prevented.noOrganisation" escapeHtml=true/>
                        </div>
                    <#elseif !currentUser.hasRegistrationRights()>
                        <div class="callout callout-warning text-smaller">
                            <@s.text name="manage.resource.status.doi.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/>
                        </div>
                    <#elseif resource.identifierStatus == "UNRESERVED">
                        <div class="callout callout-info text-smaller">
                            <@s.text name="manage.overview.publishing.doi.reserve.help" escapeHtml=true/>
                        </div>
                    <#elseif resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION">
                        <div class="callout callout-danger text-smaller">
                            <@s.text name="manage.overview.publishing.doi.delete.help" escapeHtml=true/>
                        </div>
                    <#elseif resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi() >
                        <div class="callout callout-info text-smaller">
                            <@s.text name="manage.overview.publishing.doi.reserve.new.help" escapeHtml=true/>
                        </div>
                    </#if>
                    <#if organisationWithPrimaryDoiAccount?? && currentUser.hasRegistrationRights()>
                        <@nextDoiButtonTD/>
                    </#if>
                </div>
            </div>
        </div>
    </div>

    <div id="change-publishing-organization-modal" class="modal fade" tabindex="-1" aria-labelledby="change-publishing-organization-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <h5 class="modal-title w-100" id="change-publishing-organization-modal-title"><@s.text name="eml.publishingOrganisation"/></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">×</button>
                </div>
                <div class="modal-body">
                    <form action='resource-changePublishingOrganization.do' method='post'>
                        <input name="r" type="hidden" value="${resource.shortname}"/>
                        <@selectList name="publishingOrganizationKey" options=organisations objValue="key" objTitle="name" withLabel=false />
                        <@s.submit id="changePublishingOrganization-submit" name="change" cssClass="btn btn-outline-gbif-primary my-3" key="button.change"/>
                    </form>
                </div>
            </div>
        </div>
    </div>

</#escape>
<#include "/WEB-INF/pages/inc/footer.ftl">
