<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.OverviewAction" -->
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<#macro dwcaValidator>
    <#if (resource.recordsPublished>0)>
        <a href="https://tools.gbif.org/dwca-validator/?archiveUrl=${baseURL}/archive.do?r=${resource.shortname}" title="<@s.text name="manage.overview.publishing.validator"/>" target="_blank" class="icon icon-validate">
            <@icon name="check" class="text-gbif-primary"/>
        </a>
    </#if>
</#macro>

<#macro nextDoiButtonTD>

    <!-- The organisation with DOI account activated must exist,
    the mandatory metadata must have been filled in,
    and the user must have registration rights for any DOI operation made possible -->
    <#if resource.identifierStatus == "UNRESERVED">
        <form id="doiForm" action='resource-reserveDoi.do' method='post'>
            <input name="r" type="hidden" value="${resource.shortname}"/>
            <@s.submit cssClass="confirmReserveDoi btn btn-sm btn-outline-gbif-primary" name="reserveDoi" key="button.reserve" disabled="${missingBasicMetadata?string}"/>
            <button id="cancel-button" type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">
                <@s.text name="button.cancel"/>
            </button>
        </form>
    <#elseif resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION">
        <form id="doiForm" action='resource-deleteDoi.do' method='post'>
            <input name="r" type="hidden" value="${resource.shortname}"/>
            <@s.submit cssClass="confirmDeleteDoi btn btn-sm btn-outline-gbif-danger" name="deleteDoi" key="button.delete" disabled="${missingBasicMetadata?string}"/>
            <button id="cancel-button" type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">
                <@s.text name="button.cancel"/>
            </button>
        </form>
    <#elseif resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi() >
        <form id="doiForm" action='resource-reserveDoi.do' method='post'>
            <input name="r" type="hidden" value="${resource.shortname}"/>
            <@s.submit cssClass="confirmReserveDoi btn btn-sm btn-outline-gbif-primary" name="reserveDoi" key="button.reserve.new" disabled="${missingBasicMetadata?string}"/>
            <button id="cancel-button" type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">
                <@s.text name="button.cancel"/>
            </button>
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
    <#if licenseUrl?contains("creativecommons.org/publicdomain/zero/1.0")>
        CC0 1.0
    <#elseif licenseUrl?contains("creativecommons.org/licenses/by/4.0")>
        CC-BY 4.0
    <#elseif licenseUrl?contains("creativecommons.org/licenses/by-nc/4.0")>
        CC-BY-NC 4.0
    <#elseif licenseUrl?contains("http://www.opendatacommons.org/licenses/pddl/1.0")>
        ODC PDDL 1.0
    <#elseif licenseUrl?contains("http://www.opendatacommons.org/licenses/by/1.0")>
        ODC-By 1.0
    <#elseif licenseUrl?has_content>
        <@s.text name='manage.overview.noGBIFLicense'/>
    <#else>
        -
    </#if>
</#macro>
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="manage.overview.title"/>: ${resource.title!resource.shortname}</title>

<style>
    :root {
        /* the shrunk navbar (57px) and the resource bar (52px) stay on top while scrolling;
           sections and the sidebar keep clear of them */
        --ov-bars-height: 109px;
        --navbar-height: calc(var(--ov-bars-height) + 16px);
    }

    .section {
        scroll-margin-top: var(--navbar-height);
    }
</style>

<script src="${baseURL}/js/jconfirmation.jquery.js"></script>
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<script src="${baseURL}/js/select2/select2-4.0.13.full.min.js"></script>
<script src="${baseURL}/js/scrollspy.js"></script>

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

        $(".source-item").click(function (e) {
            // Ignore clicks on actions or anything inside them
            if ($(e.target).closest(".source-item-actions, .delete-source").length) {
                return;
            }

            e.preventDefault();
            displayProcessing();
            openSourceDetails(e);
        });

        function openSourceDetails(e) {
            const $item = $(e.currentTarget).find(".source-item-link");
            const resource = $item.data("ipt-resource");
            const source = $item.data("ipt-source");
            location.href = 'source.do?r=' + resource + '&id=' + source;
        }

        $(".mapping-item").click(function (e) {
            // Ignore clicks on actions or anything inside them
            if ($(e.target).closest(".mapping-item-actions, .delete-mapping").length) {
                return;
            }

            e.preventDefault();
            displayProcessing();
            openMappingDetails(e);
        });

        function openMappingDetails(e) {
            const $row = $(e.currentTarget);

            // Try schema mapping link first, fall back to regular mapping link
            let $item = $row.find(".schema-mapping-item-link").first();
            const isSchemaMapping = $item.length > 0;
            if (!isSchemaMapping) {
                $item = $row.find(".mapping-item-link").first();
            }

            const resource = $item.data("ipt-resource");
            const extension = $item.data("ipt-extension");
            const mapping = $item.data("ipt-mapping");

            const target = isSchemaMapping ? "dataPackageMapping.do" : "mapping.do";
            location.href = target + "?r=" + resource + "&id=" + extension + "&mid=" + mapping;
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

        <#if action.isNextVisibilityPrivate()>
            <#assign resourceVisibility="private" />
        <#elseif action.isNextVisibilityPublic() || resource.status=="DELETED">
            <#assign resourceVisibility="public" />
        </#if>

        $('.confirm').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", buttonType: "primary", baseUrl: "${baseURL}", logo: "success"});
        $('.confirmRegistration').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.visibility.confirm.registration'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", checkboxText: "<@s.text name='manage.overview.visibility.confirm.agreement'/>",<#if resource.isDwcDp()> select: {label: "<@s.text name='manage.overview.registration.datasetType'/>", placeholder: "<@s.text name='manage.overview.registration.datasetType.select'/>", target: "#dwcDpDatasetType", options: [<#list action.getDwcDpRegistrationDatasetTypes() as datasetType>{value: "${datasetType.name()}", label: "<@s.text name='portal.resource.type.${datasetType.name()?lower_case?replace("_", "")}'/>"}<#sep>, </#sep></#list>]},</#if> buttonType: "primary", processing: true, baseUrl: "${baseURL}"});
        $('.confirmEmlReplace').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.metadata.replace.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", buttonType: "primary", baseUrl: "${baseURL}", logo: "success"});
        $('.confirmDatapackageMetadataReplace').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.metadata.replace.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", buttonType: "primary", baseUrl: "${baseURL}"});
        $('.confirmDeletionFromIptAndGbif').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<#if resource.isAlreadyAssignedDoi()><@s.text name='manage.resource.delete.confirm.doi'/></br></br></#if><#if resource.status=='REGISTERED'><@s.text name='manage.resource.delete.fromIptAndGbif.confirm.registered'/></br></br></#if><@s.text name='manage.resource.delete.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", baseUrl: "${baseURL}"});
        $('.confirmDeletionFromIptOnly').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<#if resource.isAlreadyAssignedDoi()><@s.text name='manage.resource.delete.confirm.doi'/></br></br></#if><#if resource.status=='REGISTERED'><@s.text name='manage.resource.delete.fromIptOnly.confirm.registered'/></br></br></#if><@s.text name='manage.resource.delete.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", baseUrl: "${baseURL}"});
        $('.confirmUndeletion').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.resource.undoDelete.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", buttonType: "primary", baseUrl: "${baseURL}", logo: "success"});

        $('.confirmReserveDoi').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.publishing.doi.reserve.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", buttonType: "primary", baseUrl: "${baseURL}", logo: "success"});
        $('.confirmDeleteDoi').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.publishing.doi.delete.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", baseUrl: "${baseURL}"});
        <#if !outdatedExtensions>
        $('.confirmPublish').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.publishing.confirm'><@s.param>${resourceVisibility}</@s.param><@s.param>${resourceVisibility}</@s.param></@s.text>", yesAnswer : "<@s.text name='button.publish'/>", summary : "<@s.text name='manage.overview.publishing.doi.summary.placeholder'/>", cancelAnswer : "<@s.text name='button.cancel'/>", buttonType: "primary", baseUrl: "${baseURL}", logo: "success"});
        $('.confirmPublishMinorVersion').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.publishing.doi.minorVersion.confirm'><@s.param>${resourceVisibility}</@s.param><@s.param>${resourceVisibility}</@s.param></@s.text>", yesAnswer : "<@s.text name='button.publish'/>", summary : "<@s.text name='manage.overview.publishing.doi.summary.placeholder'/>", cancelAnswer : "<@s.text name='button.cancel'/>", buttonType: "primary", baseUrl: "${baseURL}", logo: "success"});
        $('.confirmPublishMajorVersion').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.publishing.doi.majorVersion.confirm'><@s.param>${resourceVisibility}</@s.param><@s.param>${resourceVisibility}</@s.param></@s.text>", yesAnswer : "<@s.text name='button.publish'/>", summary : "<@s.text name='manage.overview.publishing.doi.summary.placeholder'/>", cancelAnswer : "<@s.text name='button.cancel'/>", checkboxText: "<@s.text name='manage.overview.publishing.doi.register.agreement'/>", buttonType: "primary", baseUrl: "${baseURL}", logo: "success"});
        $('.confirmPublishMajorVersionWithoutDOI').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.publishing.withoutDoi.majorVersion.confirm'><@s.param>${resourceVisibility}</@s.param><@s.param>${resourceVisibility}</@s.param></@s.text>", yesAnswer : "<@s.text name='button.publish'/>", summary : "<@s.text name='manage.overview.publishing.doi.summary.placeholder'/>", cancelAnswer : "<@s.text name='button.cancel'/>", buttonType: "primary", baseUrl: "${baseURL}", logo: "success"});
        <#else>
        $('.confirmPublish').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.publishing.outdatedExtensions'/><br><br><@s.text name='manage.overview.publishing.confirm'><@s.param>${resourceVisibility}</@s.param><@s.param>${resourceVisibility}</@s.param></@s.text>", yesAnswer : "<@s.text name='button.publish'/>", summary : "<@s.text name='manage.overview.publishing.doi.summary.placeholder'/>", cancelAnswer : "<@s.text name='button.cancel'/>", buttonType: "primary", baseUrl: "${baseURL}", logo: "success"});
        $('.confirmPublishMinorVersion').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.publishing.outdatedExtensions'/><br><br><@s.text name='manage.overview.publishing.doi.minorVersion.confirm'><@s.param>${resourceVisibility}</@s.param><@s.param>${resourceVisibility}</@s.param></@s.text>", yesAnswer : "<@s.text name='button.publish'/>", summary : "<@s.text name='manage.overview.publishing.doi.summary.placeholder'/>", cancelAnswer : "<@s.text name='button.cancel'/>", buttonType: "primary", baseUrl: "${baseURL}", logo: "success"});
        $('.confirmPublishMajorVersion').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.publishing.outdatedExtensions'/><br><br><@s.text name='manage.overview.publishing.doi.majorVersion.confirm'><@s.param>${resourceVisibility}</@s.param><@s.param>${resourceVisibility}</@s.param></@s.text>", yesAnswer : "<@s.text name='button.publish'/>", summary : "<@s.text name='manage.overview.publishing.doi.summary.placeholder'/>", cancelAnswer : "<@s.text name='button.cancel'/>", checkboxText: "<@s.text name='manage.overview.publishing.doi.register.agreement'/>", buttonType: "primary", baseUrl: "${baseURL}", logo: "success"});
        $('.confirmPublishMajorVersionWithoutDOI').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.publishing.outdatedExtensions'/><br><br><@s.text name='manage.overview.publishing.withoutDoi.majorVersion.confirm'><@s.param>${resourceVisibility}</@s.param><@s.param>${resourceVisibility}</@s.param></@s.text>", yesAnswer : "<@s.text name='button.publish'/>", summary : "<@s.text name='manage.overview.publishing.doi.summary.placeholder'/>", cancelAnswer : "<@s.text name='button.cancel'/>", buttonType: "primary", baseUrl: "${baseURL}", logo: "success"});
        </#if>

        $('.delete-source').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name="manage.source.confirmation.message"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>", buttonType: "danger", baseUrl: "${baseURL}"});
        $('.delete-mapping').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name ="manage.mapping.confirmation.message"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>", buttonType: "danger", baseUrl: "${baseURL}"});
        $('.delete-core-mapping').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name ="manage.mapping.confirmation.message"/><br><br><@s.text name ="manage.mapping.confirmation.message.core"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>", buttonType: "danger", baseUrl: "${baseURL}"});

        // Initialize scrollspy
        IptScrollSpy.init({tocSelector: '.bd-toc nav a'});

        // room under the last section, so every section in "On this page" can scroll up under the bars, but no more than that
        (function () {
            var content = document.querySelector("#sections .bd-content");
            var last = document.getElementById("managers");
            if (!content || !last) return;
            function fitLastSection() {
                var padding = parseFloat(content.style.paddingBottom) || 0;
                var below = document.documentElement.scrollHeight - padding - (last.getBoundingClientRect().top + window.scrollY);
                var needed = window.innerHeight - (parseFloat(getComputedStyle(last).scrollMarginTop) || 0) - below;
                content.style.paddingBottom = needed > 0 ? Math.ceil(needed) + "px" : "";
            }
            // content-box changes only (cards opening, tables filtering), so setting the padding doesn't fire it again
            new ResizeObserver(fitLastSection).observe(content);
            window.addEventListener("resize", fitLastSection);
        })();

        // the publication report: toggled from the Publication card (menu, failure line), shown from the page header menu
        function setReportVisible(show) {
            $(".ov-toggle-report-text").text(show ? "<@s.text name='manage.overview.published.report.hide'/>" : "<@s.text name='manage.overview.published.report.show'/>");
            if (show) {
                $('#dwcaReport').fadeIn();
            } else {
                $('#dwcaReport').fadeOut();
            }
        }
        $(".ov-toggle-report").on('click', function (e) {
            e.preventDefault();
            setReportVisible(!$('#dwcaReport').is(':visible'));
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

        $('#metadata-modal').on('show.bs.modal', function() {
            $("#emlReplace").hide();
            $("#eml-validate").hide();
            $("#emlFile").prop("value", "");
        });

        $("#emlFile").change(function() {
            var usedFileName = $("#emlFile").prop("value");
            if(usedFileName !== "") {
                $("#emlReplace").show();
                $("#eml-validate").show();
            }
        });

        $("#datapackageMetadataFile").change(function() {
            var usedFileName = $("#datapackageMetadataFile").prop("value");
            if (usedFileName !== "") {
                $("#datapackageMetadataReplace").show();
                $("#datapackage-metadata-validate").show();
                $("#datapackage-metadata-preserve-scope-metadata").show();
            }
        });

        $("#datapackageMetadataCancel").click(function (e) {
            e.preventDefault();
            $("#datapackageMetadataFile").prop("value", "");
            $("#datapackageMetadataReplace").hide();
            $("#datapackage-metadata-validate").hide();
            $("#datapackage-metadata-preserve-scope-metadata").hide();
        });

        $("#emlCancel").click(function(event) {
            event.preventDefault();
            $("#emlFile").prop("value", "");
            $("#emlReplace").hide();
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

            // "check url" elements
            var urlSourceInfoBlock = $("#url-source-info-block");
            var urlSourceCheckLinkWrapper = $("#url-source-check-link-wrapper");
            var urlSourceCheckLink = $("#check-url-source-link");

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
                addButton.attr("value", '<@s.text name="button.add" escapeJavaScript="true"/>');
                addButton.hide();
                urlSourceInfoBlock.hide();

                sourceNameInput.removeClass("is-invalid");
                urlInput.removeClass("is-invalid");
                $("#callout-source-exists").hide();
                $("#callout-file-upload-info").show();
            } else if (selectedTabId === 'tab-source-url') {
                sourceTypeInput.attr("value", "source-url");
                urlInput.show();
                sourceNameInput.show();
                chooseFilesButton.hide();
                sendButton.hide();
                $(".progress-bar-container").hide();
                fileInput.prop("value", "");
                clearButton.show();
                addButton.attr("value", '<@s.text name="button.add" escapeJavaScript="true"/>');
                addButton.show();

                // display "Check URL"
                urlInput.on('input', function () {
                    urlSourceInfoBlock.show();
                    const value = $(this).val().trim();

                    $('#url-source-size-wrapper').hide();
                    $('#url-source-content-type-wrapper').hide();
                    $('#url-source-check-status-wrapper').hide();
                    $('#url-source-error-message-wrapper').hide();

                    if (value) {
                        urlSourceCheckLinkWrapper.show();
                    } else {
                        urlSourceCheckLinkWrapper.hide();
                    }
                });

                // "Check URL" button
                urlSourceCheckLink.on('click', async function (e) {
                    e.preventDefault();
                    const url = urlInput.val().trim();
                    const urlMetadata = await checkUrlMetadata(url);

                    const status = urlMetadata.status;
                    const contentLength = urlMetadata.contentLength;
                    const contentType = urlMetadata.contentType;

                    if (status === 200) {
                        $('#url-source-check-status-wrapper').show();
                        $('#url-source-check-status-success').show();
                        $('#url-source-check-status-fail').hide();
                        $('#url-source-size-wrapper').show();
                        $('#url-source-content-type-wrapper').show();
                        $("#url-source-size").text(Number.isFinite(contentLength) && contentLength >= 0 ? formatFileSize(contentLength) : "?");
                        $("#url-source-content-type").text(contentType ? contentType.split(";")[0].trim() : "?");
                    } else {
                        $('#url-source-check-status-wrapper').show();
                        $('#url-source-check-status-fail').show();
                        $('#url-source-check-status-success').hide();
                        $('#url-source-error-message-wrapper').show();
                    }
                });

                fileItems.forEach(function(item) {
                    item.remove();
                });
                selectedFiles = [];
                confirmedFiles = []
                sourceNameInput.removeClass("is-invalid");
                urlInput.removeClass("is-invalid");

                $("#callout-file-upload-info").hide();

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
                urlSourceInfoBlock.hide();
                urlInput.hide();
                urlInput.prop("value", "");
                clearButton.hide();
                addButton.attr("value", '<@s.text name="button.connect" escapeJavaScript="true"/>');
                addButton.show();

                fileItems.forEach(function(item) {
                    item.remove();
                });
                selectedFiles = [];
                confirmedFiles = [];
                sourceNameInput.removeClass("is-invalid");
                urlInput.removeClass("is-invalid");

                $("#callout-file-upload-info").hide();

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
            $("#url-source-info-block").hide();
            $("#sourceName").prop("value", "");
            $("#callout-source-exists").hide();
            $("#add").show();
        });

        $(function() {
            $('.icon-validate').tooltip();
        });

        // load a preview of the mapping in the modal window
        $(".peekBtn").click(function(e) {
            e.preventDefault();
            var addressValue = $(this).attr("href");
            displayProcessing();
            $("#modalcontent").load(addressValue, function (response, status, xhr) {
                // the session expired: open the overview, which goes to login and returns there
                if (xhr.status === 401) {
                    window.location.href = "${baseURL}/manage/resource.do?r=${resource.shortname}";
                    return;
                }
                if (status === "error") {
                    // load() inserts nothing on an error status. 400/404 carry the message from peek.ftl,
                    // other errors have no fragment to show
                    $("#modalcontent").html(xhr.status === 400 || xhr.status === 404 ? response
                        : $('<div class="alert alert-danger fs-smaller-2" role="alert"></div>')
                            .text('${action.getText("basic.error")?js_string} (' + xhr.status + ')'));
                }
                hideProcessing();
            });
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

        $('#makePrivate').click(function (e) {
            e.preventDefault();
            showMakePrivateModal();
        });

        $('#cancelVisibilityChange').click(function (e) {
            e.preventDefault();
            showCancelVisibilityChangeModal();
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

        // make public modal window
        function showMakePrivateModal() {
            var dialogWindow = $("#make-private-modal");
            dialogWindow.modal('show');
        }

        function showCancelVisibilityChangeModal() {
            var dialogWindow = $("#cancel-visibility-change-modal");
            dialogWindow.modal('show');
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

        $("#make-private-modal-form").one('submit', function (e) {
            e.preventDefault();
            $(this).submit();
        });

        async function checkUrlMetadata(url) {
            const res = await fetch('/manage/urlMetadata?url=' + encodeURIComponent(url));
            return await res.json();
        }

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

        $(".change-dwc-dp-dataset-type-button").on('click', function (e) {
            e.preventDefault();
            $("#dwc-dp-dataset-type-modal").modal('show');
        });

        $("#registeredDwcDpDatasetType").change(function () {
            if (this.value) {
                $("#update-dwc-dp-dataset-type").show();
            } else {
                $("#update-dwc-dp-dataset-type").hide();
            }
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
            var dialogWindow = $("#reserve-doi-modal");
            dialogWindow.modal('show');
        }

        const uploadStatus = sessionStorage.getItem("uploadStatus");

        if (uploadStatus === "success") {
            showSourceCreatedSuccessfullyInfoWindow();
            showCarriedOverUploadWarnings();
            sessionStorage.removeItem("uploadStatus");
        } else if (uploadStatus === "fail") {
            console.log("upload failed");
            sessionStorage.removeItem("uploadStatus");
        }

        // Warnings picked up from the final response document of an XHR-driven source upload (see
        // uploadFile() below) don't otherwise survive the widget's own window.location.reload(), since
        // by the time that reload's request reaches the server, RedirectMessageInterceptor has already
        // handed them off (and cleared them from the session) to the page the XHR silently followed.
        function showCarriedOverUploadWarnings() {
            var raw = sessionStorage.getItem("uploadWarnings");
            sessionStorage.removeItem("uploadWarnings");
            if (!raw) {
                return;
            }

            var warnings;
            try {
                warnings = JSON.parse(raw);
            } catch (e) {
                return;
            }

            var container = document.getElementById("action-alerts");
            if (!container || !warnings || !warnings.length) {
                return;
            }

            // one alert, the other warnings collapsed under the first, like action_alerts_list.ftl
            var alertDiv = document.createElement("div");
            alertDiv.className = "alert alert-warning alert-dismissible fade show d-flex";
            alertDiv.setAttribute("role", "alert");
            alertDiv.innerHTML =
                '<div class="me-3"><@icon name="warning" class="alert-orange-2 fs-bigger-2 me-2"/></div>' +
                '<div class="overflow-x-hidden pt-1"><span></span></div>' +
                '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>';
            alertDiv.querySelector("span").textContent = warnings[0];
            if (warnings.length > 1) {
                var more = document.createElement("details");
                more.className = "alert-more mt-1";
                more.innerHTML =
                    '<summary class="fs-smaller"></summary>' +
                    '<ul class="mt-1 mb-0 ps-3"></ul>';
                more.querySelector("summary").append(`<@s.text name="alerts.more.warnings"/>`.replace("{0}", warnings.length - 1));
                warnings.slice(1).forEach(function (text) {
                    var item = document.createElement("li");
                    item.textContent = text;
                    more.querySelector("ul").appendChild(item);
                });
                alertDiv.querySelector(".overflow-x-hidden").appendChild(more);
            }
            container.appendChild(alertDiv);

            window.scrollTo({ top: 0, behavior: "smooth" });
        }

        function showSourceCreatedSuccessfullyInfoWindow() {
            const templateOneSource = `<@s.text name="manage.overview.source.created"/>`;
            const templateMultipleSources = `<@s.text name="manage.overview.sources.created"/>`;
            const files = sessionStorage.getItem("uploadedFiles");
            sessionStorage.removeItem("uploadedFiles");

            var message;
            if (files.includes(",")) {
                message = templateMultipleSources.replace("{0}", files);
            } else {
                message = templateOneSource.replace("{0}", files);
            }

            $("#sources-created-successfully-info .message").html(message);

            $("#sources-created-successfully-info").fadeIn(200, function () {
                window.scrollTo({ top: 0, behavior: "smooth" });
            });
        }

        var selectedFiles = [];

        $("#chooseFilesButton").on("click", function (e) {
            e.preventDefault();
            $("#fileInput").click();
        });

        $("#fileInput").on("change", function () {
            var newlySelectedFiles = Array.from(this.files);
            selectedFiles = selectedFiles.concat(newlySelectedFiles);
            refreshFileList();

            if (selectedFiles.length > 0) {
                sendButton.show();
            } else {
                sendButton.hide();
            }
        });

        $("#sendButton").on("click", async function () {
            $(this).addClass("clicked");
            $(this).hide();

            var fileItems = document.querySelectorAll(".fileItem");
            var promises = [];
            var fileNamesConcatenated = "";

            // hide remove buttons - already submitted
            var removeButtons = document.querySelectorAll(".removeButton");
            removeButtons.forEach(function(button) {
                button.style.display = "none";
            });

            for (var i = 0; i < selectedFiles.length; i++) {
                var file = selectedFiles[i];
                var fileItem = fileItems[i];

                if (fileNamesConcatenated) {
                    fileNamesConcatenated += ", ";
                }
                fileNamesConcatenated += file.name;

                promises.push(uploadFile(file, i, fileItem));
            }

            try {
                var results = await Promise.all(promises);
                var allWarnings = results.flat().filter(Boolean);

                sessionStorage.setItem("uploadStatus", "success");
                sessionStorage.setItem("uploadedFiles", fileNamesConcatenated);
                if (allWarnings.length > 0) {
                    sessionStorage.setItem("uploadWarnings", JSON.stringify(allWarnings));
                }
                closeModal();
                window.location.reload();
            } catch (error) {
                sessionStorage.setItem("uploadStatus", "fail");
                console.log(error);
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

            const ACCEPTED_FILE_NAMES = /^[\w.\-\s()]+$/;

            function isValidString(input) {
                return ACCEPTED_FILE_NAMES.test(input);
            }

            var isFileNameInvalid = !isValidString(file.name);
            var isFileSizeTooBig = file.size > 10000000000;
            var isSourceAlreadyExistOrRepeated = isSourceAlreadyExist && !confirmedFiles.includes(fileNameWithoutSpaces);
            var displayWarning = isFileNameInvalid || isFileSizeTooBig || isSourceAlreadyExistOrRepeated;

            if (isFileNameInvalid) {
                fileError.innerText = `<@s.text name='manage.overview.source.file.name.invalid'/>`;
            } else if (isFileSizeTooBig) {
                fileError.innerText = `<@s.text name='manage.overview.source.file.too.big'/>`;
            } else if (isSourceAlreadyExistOrRepeated) {
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
            fileDoneIcon.innerHTML = '<@icon name="check" class="text-gbif-primary"/>';

            // create "error/warning" icon
            var fileWarningIcon = document.createElement("div");
            fileWarningIcon.className = "fileWarningIcon";
            fileWarningIcon.innerHTML = '<@icon name="error" class="text-gbif-danger"/>';

            // link divs
            fileMeta.appendChild(fileName);
            fileMeta.appendChild(fileStatus);
            if (displayWarning) {
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

        async function uploadFile(file, fileIndex, fileItem) {
            return new Promise(function (resolve, reject) {
                var fileMeta = fileItem.querySelector(".fileMeta");
                var progressBar = fileItem.querySelector(".progressBar-value");
                var fileDoneIcon = fileItem.querySelector(".fileDoneIcon");
                var fileWarningIcon = fileItem.querySelector(".fileWarningIcon");
                var fileStatus = fileItem.querySelector(".fileStatus");

                var formData = new FormData();
                formData.append("r", "${resource.shortname}");
                formData.append("sourceType", "source-file");
                formData.append("validate", false);
                formData.append("add", "Add");
                formData.append("file", file);

                var xhr = new XMLHttpRequest();
                xhr.open("POST", "addsource.do", true);
                xhr.setRequestHeader(IPT_CSRF.header, IPT_CSRF.token());
                // an expired session then gets a 401 instead of the login page, which would look like success
                xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest");

                // Prevent automatic redirection by setting responseType to 'document'
                xhr.responseType = 'document';

                xhr.onload = function () {
                    if (xhr.status >= 300 && xhr.status < 400) {
                        // Redirection detected, handle it manually
                        var redirectUrl = xhr.getResponseHeader("Location");
                        if (redirectUrl) {
                            // Optional: Navigate to the redirect URL manually
                            window.location.href = redirectUrl;
                        }
                    } else {
                        // the session expired: open the overview, which goes to login and returns there
                        if (xhr.status === 401) {
                            window.location.href = "${baseURL}/manage/resource.do?r=${resource.shortname}";
                            reject(new Error("Session expired"));
                            return;
                        }

                        // Process the error response: the message of a rejected file, or any other failed request
                        // (e.g. 403 for a missing CSRF token), which has no message of its own
                        var errorMessage = xhr.getResponseHeader("X-Error-Message");
                        if (!errorMessage && xhr.status !== 200) {
                            errorMessage = '${action.getText("basic.error")?js_string} (' + xhr.status + ')';
                        }

                        if (errorMessage) {
                            // file error div
                            var fileError = document.createElement("div");
                            fileError.className = "fileError";
                            fileError.setAttribute('data-index', fileIndex);
                            fileError.innerText = errorMessage;

                            fileMeta.appendChild(fileError)

                            fileDoneIcon.style.visibility = "hidden";
                            fileDoneIcon.style.display = "none";
                            fileWarningIcon.style.visibility = "visible";
                            fileWarningIcon.style.display = "block";

                            reject(new Error(errorMessage)); // Reject with the error message
                            return;
                        }

                        // The XHR silently followed the addsource -> ... -> resource.do redirect chain,
                        // so xhr.response is the final page's parsed document. Any action warnings that
                        // survived that chain (e.g. "embedded line breaks removed") are rendered into it
                        // by action_alerts.ftl - grab them now, because they'll be gone from the session
                        // by the time our own window.location.reload() below asks for the page again.
                        var fileWarnings = [];
                        try {
                            var finalDoc = xhr.response;
                            if (finalDoc && typeof finalDoc.querySelectorAll === "function") {
                                // each warning: the first of the alert, and the others collapsed under it
                                finalDoc.querySelectorAll("#action-alerts .alert-warning .overflow-x-hidden > span, "
                                    + "#action-alerts .alert-warning .alert-more li").forEach(function (el) {
                                    var text = el.textContent.trim();
                                    if (text) {
                                        fileWarnings.push(text);
                                    }
                                });
                            }
                        } catch (e) {
                            console.warn("Could not read warnings from upload response:", e);
                        }

                        resolve(fileWarnings); // Resolve on success, carrying any warnings found
                    }
                };

                xhr.onerror = function () {
                    console.error("An error occurred during the request:", xhr.status, xhr.statusText);
                    reject(new Error("An error occurred during the request: " + xhr.status + " " + xhr.statusText));
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

                    // Hide progress bar
                    var fileProgressBarWrapper = document.querySelector('.fileItem[data-file-index="' + fileIndex + '"] .progressBar');
                    if (fileProgressBarWrapper) {
                        fileProgressBarWrapper.classList.add("d-none");
                    }

                    // Display done icon
                    var fileDoneIcon = document.querySelector('.fileItem[data-file-index="' + fileIndex + '"] .fileDoneIcon');
                    if (fileDoneIcon) {
                        fileDoneIcon.style.visibility = "visible";
                    }

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

        if (fileListDiv) {
            const observer = new MutationObserver(function () {
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

            const config = {attributes: true, childList: true, subtree: true};

            observer.observe(fileListDiv, config);

            // remove error classes/icons if confirmed
            $('#fileList').on('click', '.confirmOverwriteSourceFileLink', function () {
                var fileIndex = $(this).data('index');

                var fileItemElement = $('.fileItem[data-file-index="' + fileIndex + '"]');
                fileItemElement.find('.fileError').remove();

                var fileNameWithExtension = fileItemElement.find('.fileName').text();
                var fileNameWithoutExtension = fileNameWithExtension.substring(0, fileNameWithExtension.lastIndexOf('.')).replace(/\s/g, "");

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
        }

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


        $(".proxy-button-delete-from-ipt").on('click', function () {
            $("#delete-resource-modal").modal('hide');
            $(".confirmDeletionFromIptOnly").click();
        });

        $(".proxy-button-delete-from-gbif-and-ipt").on('click', function () {
            $("#delete-resource-modal").modal('hide');
            $(".confirmDeletionFromIptAndGbif").click();
        });

        $(".proxy-button-view-resource").on('click', function () {
            $("#top-button-view-resource")[0].click();
        });

        $(".proxy-button-cancel").on('click', function () {
            $("#top-button-cancel")[0].click();
        });

        $(".proxy-button-undelete-resource").on('click', function () {
            $(".confirmUndeletion").click();
        });

        $(".button-show-delete-resource-modal").on('click', function () {
            var dialogWindow = $("#delete-resource-modal");
            dialogWindow.modal('show');
        });

        $(".button-show-delete-resource-disabled-modal").on('click', function () {
            var dialogWindow = $("#delete-resource-disabled-modal");
            dialogWindow.modal('show');
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

        // the resource bar replaces the heading once its actions have scrolled under the navbar (which shrinks on scroll)
        var resourceNav = document.getElementById("resource-nav");
        var mainNav = document.querySelector(".main-nav");
        var headActions = document.querySelector(".ov-head .ov-acts");
        function displayHideSecondNavOnScroll() {
            var navBottom = mainNav.getBoundingClientRect().bottom;
            resourceNav.style.top = navBottom + "px";
            resourceNav.hidden = headActions.getBoundingClientRect().bottom > navBottom;
        }
        window.addEventListener("scroll", displayHideSecondNavOnScroll, {passive: true});
        window.addEventListener("resize", displayHideSecondNavOnScroll);
        displayHideSecondNavOnScroll();

        $(".proxy-button-publish").on('click', function () {
            var target = document.querySelector(".ov-head #publishButton, .ov-head #publish-button-show-warning, .ov-head #publish-button-blocked");
            if (target) target.click();
        });

        $("#view-metadata-button").on('click', function () {
            var dialogWindow = $("#datapackage-metadata-modal");
            dialogWindow.modal('show');
        });

        $('.show-metadata-validation-result').on('click', function (e) {
            e.preventDefault();
            var dialogWindow = $("#metadata-validation-result-modal");
            dialogWindow.modal('show');
        });

        $('.go-to-publication-settings').on('click', function (e) {
            e.preventDefault();
            window.location.href = "publication-settings.do?r=${resource.shortname}";
        });

        // ---- redesigned overview: actions that hand over to the existing buttons and modals ----
        // when registering is blocked, the greyed-out Register link opens the list of what's missing
        $('#register-blocked').on('click', function () {
            var checks = document.getElementById('registration-checks');
            checks.open = true;
            checks.scrollIntoView({behavior: 'smooth', block: 'center'});
        });
        // when publishing is blocked the header button points at the reasons in the banner
        $('#publish-button-blocked, .ov-see-blockers').on('click', function (e) {
            e.preventDefault();
            document.getElementById('overview-status').scrollIntoView({behavior: 'smooth', block: 'center'});
        });
        $('.ov-open-add-source').on('click', function (e) {
            e.preventDefault();
            $('#add-source-button').trigger('click');
        });
        $('.ov-open-add-mapping').on('click', function (e) {
            e.preventDefault();
            $('#add-mapping-button').trigger('click');
        });
        $('.ov-show-report').on('click', function () {
            if (!$('#dwcaReport').is(':visible')) {
                setReportVisible(true);
            }
        });

        // ---- sources and mappings: filter, search and show all, over the mappings and the unused sources ----
        (function () {
            var $data = $('#ov-data');
            if (!$data.length) return;

            var $more = $('#ov-data-more');
            var limit = parseInt($data.attr('data-ov-limit'), 10) || 8;
            var state = {filter: 'all', query: '', expanded: false};

            function matches($row) {
                if (state.query && String($row.attr('data-ov-text') || '').indexOf(state.query) === -1) return false;
                if (state.filter === 'changed') return $row.attr('data-ov-changed') === 'true' || $row.attr('data-ov-mapping-changed') === 'true';
                if (state.filter === 'unmapped') return $row.attr('data-ov-unmapped') === 'true';
                if (state.filter === 'problems') return $row.attr('data-ov-problem') === 'true';
                return true;
            }

            function render() {
                $('[data-ov-filter]').each(function () {
                    $(this).toggleClass('on', $(this).attr('data-ov-filter') === state.filter);
                });

                var $rows = $data.find('.ov-row');
                var shown = 0, total = 0;
                $rows.each(function () {
                    var $row = $(this);
                    var ok = matches($row);
                    $row.toggleClass('ov-filtered', !ok);
                    if (ok) {
                        total++;
                        $row.toggleClass('ov-hidden', !state.expanded && total > limit);
                        if (state.expanded || total <= limit) shown++;
                    } else {
                        $row.removeClass('ov-hidden');
                    }
                });
                function isVisible() { return !$(this).hasClass('ov-filtered') && !$(this).hasClass('ov-hidden'); }
                // a group heading, the table and the unused list show only when a row under them does
                $data.find('tr.ov-grp').each(function () {
                    $(this).toggle($(this).nextUntil('tr.ov-grp').filter('.ov-row').filter(isVisible).length > 0);
                });
                $data.find('table.ov-table').toggle($data.find('tr.ov-row').filter(isVisible).length > 0);
                $data.find('.ov-unused').toggle($data.find('.ov-unused-row').filter(isVisible).length > 0);
                $data.find('.ov-nomatch').toggle($rows.length > 0 && total === 0);

                if (total > limit) {
                    $('#ov-data-showing').text($more.attr('data-showing').replace('{0}', shown).replace('{1}', total));
                    $('#ov-data-expand').text(state.expanded ? $more.attr('data-show-fewer') : $more.attr('data-show-all').replace('{0}', total));
                    $more.show();
                } else {
                    $more.hide();
                }
            }

            $('[data-ov-filter]').on('click', function () {
                state.filter = $(this).attr('data-ov-filter');
                state.expanded = false;
                render();
            });
            $('#ov-data-filter').on('input', function () {
                state.query = $(this).val().trim().toLowerCase();
                state.expanded = false;
                render();
            });
            $('#ov-data-expand').on('click', function () {
                state.expanded = !state.expanded;
                render();
            });
            render();
        })();
    });
</script>

<#assign currentMenu = "manage"/>
<#assign currentPage = "overview"/>
<#if resource.isAlreadyAssignedDoi()?string == "false" && resource.status != "REGISTERED">
    <#assign disableRegistrationRights="false"/>
<#elseif currentUser.hasRegistrationRights()?string == "true">
    <#assign disableRegistrationRights="false"/>
<#else>
    <#assign disableRegistrationRights="true"/>
</#if>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<#include "/WEB-INF/pages/macros/popover.ftl"/>
<#include "/WEB-INF/pages/macros/manage/publish.ftl"/>
<#include "/WEB-INF/pages/macros/dwc_dp_types.ftl"/>
<#assign metadataType = "metadata"/>
<#assign isDataPackage = resource.isDataPackage()/>
<#assign isColDp = resource.coreType?has_content && resource.coreType == "coldp"/>

<#include "/WEB-INF/pages/manage/overview_status.ftl"/>
<#assign publishLabel><@s.text name="manage.overview.publishVersion"><@s.param>${nextVersion}</@s.param></@s.text></#assign>
<#assign publishLabel = publishLabel?markup_string>

    <div class="container-fluid border-bottom">
        <div class="container">
            <div class="ov-head">
                <div class="ov-grow">
                    <nav class="ov-crumb" aria-label="breadcrumb">
                        <a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a> › <span aria-current="page"><@s.text name="breadcrumb.manage.overview"/></span>
                    </nav>
                    <h1 property="dc:title" class="rtitle text-gbif-header">${resource.title!resource.shortname}</h1>
                    <div class="ov-meta">
                        <span class="fs-smaller-2 text-nowrap type-pill dt-content-link dt-content-pill type-${resourceTypeLowerCase}"<#if installedSchemaVersion?has_content> title="<@s.text name="manage.overview.schemaVersion"/>"</#if>><@s.text name="portal.resource.type.${resourceTypeLowerCase}"/><#if installedSchemaVersion?has_content> &middot; ${installedSchemaVersion}</#if></span>
                        <#if resourceSubtypeLowerCase?has_content>
                            <span class="fs-smaller-2 text-nowrap subtype-pill dt-content-link dt-content-pill type-${resourceSubtypeLowerCase}"><@s.text name="portal.resource.subtype.${resourceSubtypeLowerCase}"/></span>
                        </#if>
                        <span class="text-nowrap text-discreet fs-smaller-2 status-pill main-status-pill status-${resource.status!?lower_case}">
                            <#if resource.status == "PUBLIC" || resource.status == "PRIVATE">
                                <@icon name="radio_button_unchecked" class="fs-smaller-2"/>
                            <#else>
                                <@icon name="circle" class="fs-smaller-2"/>
                            </#if>
                            <span><@s.text name="manage.home.visible.${resource.status!?lower_case}"/></span>
                        </span>
                        <#if isStatusPending>
                            <span class="ov-pill warn" title="<@s.text name='manage.overview.changed.publication.status.republication'/>">→ <@s.text name="manage.overview.pendingOnPublication"><@s.param><@s.text name="resource.status.${resource.pendingStatus?lower_case}"/></@s.param></@s.text></span>
                        </#if>
                        <span><@s.text name="basic.createdByOn"><@s.param>${(resource.creator.name)!}</@s.param><@s.param>${resource.created?date?string("MMM d, yyyy")}</@s.param></@s.text></span>
                        <span><@s.text name="manage.overview.shortname"/> <code>${resource.shortname}</code></span>
                    </div>
                </div>

                <div class="ov-acts">
                    <#if isPublished && !isDeleted>
                        <a href="${baseURL}/resource?r=${resource.shortname}" id="top-button-view-resource" class="btn btn-sm btn-outline-gbif-primary">
                            <#if resource.status == "PRIVATE"><@s.text name="manage.overview.viewPage.private"/><#else><@s.text name="manage.overview.viewPage"/></#if>
                        </a>
                    </#if>

                    <#if isDeleted>
                        <#if disableRegistrationRights == "false">
                            <form action='resource-undelete.do' method='post'>
                                <input name="r" type="hidden" value="${resource.shortname}" />
                                <@s.submit cssClass="btn btn-sm btn-gbif-solid confirmUndeletion" name="undelete" key="button.undelete"/>
                            </form>
                        <#else>
                            <button class="btn btn-sm btn-gbif-solid ov-blocked button-show-delete-resource-disabled-modal" name="undelete"><@s.text name="button.undelete"/></button>
                        </#if>
                    <#elseif canPublish>
                        <@publish resource=resource label=publishLabel/>
                    <#else>
                        <a href="#overview-status" id="publish-button-blocked" class="btn btn-sm btn-gbif-solid ov-blocked" title="<@s.text name='manage.overview.publishBlocked'/>">
                            <@icon name="upload" class="me-1"/>${publishLabel}
                        </a>
                    </#if>

                    <#-- DOI and delete; publication links are in the Publication card. Nothing to offer once deleted -->
                    <#if !isDeleted>
                    <div class="dropdown">
                        <button class="btn btn-sm btn-outline-secondary" type="button" id="overview-more-actions" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='manage.overview.moreActions'/>" aria-label="<@s.text name='manage.overview.moreActions'/>">
                            <@icon name="more_horiz"/>
                        </button>
                        <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="overview-more-actions">
                            <#if displayDoiFunctionality && !isDeleted>
                                <li>
                                    <a class="dropdown-item" id="reserve-doi" href="#">
                                        <#if resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION"><@s.text name="button.delete"/> DOI<#elseif resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi()><@s.text name="button.reserve.new"/> DOI<#else><@s.text name="button.reserve"/> DOI</#if>
                                        <small><@s.text name="manage.overview.menu.doi.help"><@s.param>${organisationWithPrimaryDoiAccount.doiRegistrationAgency}</@s.param></@s.text></small>
                                    </a>
                                </li>
                            </#if>
                            <#if !isDeleted>
                                <#if displayDoiFunctionality><li><hr class="dropdown-divider"></li></#if>
                                <li>
                                    <#if disableRegistrationRights == "false">
                                        <#if resource.key?? && resource.status == "REGISTERED">
                                            <button class="dropdown-item text-gbif-danger button-show-delete-resource-modal" type="button" name="deleteFlag">
                                        <#else>
                                            <button class="dropdown-item text-gbif-danger proxy-button-delete-from-ipt" type="button" name="deleteFlag">
                                        </#if>
                                    <#else>
                                        <button class="dropdown-item text-gbif-danger button-show-delete-resource-disabled-modal" type="button" name="deleteFlag">
                                    </#if>
                                        <@s.text name="manage.overview.menu.delete"/>
                                        <small><#if resource.status == "REGISTERED"><@s.text name="manage.overview.menu.delete.registered"/><#else><@s.text name="manage.overview.menu.delete.help"/></#if></small>
                                    </button>
                                </li>
                            </#if>
                        </ul>
                    </div>
                    </#if>

                    <#-- the forms the delete actions submit, and the target of the "Cancel" button in the sticky resource bar -->
                    <div class="d-none">
                        <#if !isDeleted && disableRegistrationRights == "false">
                            <#if resource.key?? && resource.status == "REGISTERED">
                                <form action="resource-delete.do" method='post'>
                                    <input name="r" type="hidden" value="${resource.shortname}" />
                                    <@s.submit cssClass="confirmDeletion confirmDeletionFromIptAndGbif" name="deleteFlag" key="button.delete.fromIptAndGbif"/>
                                </form>
                            </#if>
                            <form action="resource-deleteFromIpt.do" method='post'>
                                <input name="r" type="hidden" value="${resource.shortname}" />
                                <@s.submit cssClass="confirmDeletion confirmDeletionFromIptOnly" name="deleteFlag" key="button.delete.fromIpt"/>
                            </form>
                        </#if>
                        <a href="${baseURL}/manage/" id="top-button-cancel"><@s.text name="button.cancel"/></a>
                    </div>
                </div>
            </div>
            <div id="dialog" class="modal fade" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true"></div>
        </div>
    </div>

    <#-- shown once the heading has scrolled under the navbar: the title and the same actions as the heading -->
    <nav id="resource-nav" class="resource-nav ov-bar border-bottom" aria-label="${(resource.title!resource.shortname)}" hidden>
        <div class="container">
            <div class="ov-bar-in">
                <div class="ov-bar-title">
                    <span class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill type-${resourceTypeLowerCase}"><@s.text name="portal.resource.type.${resourceTypeLowerCase}"/></span>
                    <span class="ov-bar-name">${resource.title!resource.shortname}</span>
                </div>
                <div class="ov-acts">
                    <#if isPublished && !isDeleted>
                        <button type="button" class="btn btn-sm btn-outline-gbif-primary proxy-button-view-resource">
                            <#if resource.status == "PRIVATE"><@s.text name="manage.overview.viewPage.private"/><#else><@s.text name="manage.overview.viewPage"/></#if>
                        </button>
                    </#if>
                    <#if isDeleted>
                        <#if disableRegistrationRights == "false">
                            <button type="button" class="btn btn-sm btn-gbif-solid proxy-button-undelete-resource"><@s.text name="button.undelete"/></button>
                        <#else>
                            <button type="button" class="btn btn-sm btn-gbif-solid ov-blocked button-show-delete-resource-disabled-modal"><@s.text name="button.undelete"/></button>
                        </#if>
                    <#else>
                        <button type="button" class="btn btn-sm btn-gbif-solid proxy-button-publish<#if !canPublish> ov-blocked</#if>"<#if !canPublish> title="<@s.text name='manage.overview.publishBlocked'/>"</#if>>
                            <@icon name="upload" class="me-1"/>${publishLabel}
                        </button>
                    </#if>
                </div>
            </div>
        </div>
    </nav>

    <div id="sections" class="container-fluid bg-body">
        <div class="container main-content-container mb-md-4 bd-layout">

            <main class="bd-main">
                <div class="bd-toc bd-toc-overview mt-4 pt-3 ps-3 mb-lg-5 text-muted">
                    <div class="fw-semibold text-gbif-header-2 mb-2 text-smaller"><@s.text name="manage.overview.onThisPage"/></div>
                    <nav id="sidebar-content" aria-label="<@s.text name='manage.overview.onThisPage'/>">
                        <ul>
                            <#if !isMetadataOnly>
                                <li><a href="#anchor-data" class="sidebar-navigation-link"><span class="ov-dot ${dataStatus}"></span><#if isDataPackage><@s.text name="manage.overview.data.title.dp"/><#else><@s.text name="manage.overview.data.title"/></#if></a></li>
                            </#if>
                            <li><a href="#anchor-metadata" class="sidebar-navigation-link"><span class="ov-dot ${metadataStatus}"></span><@s.text name="manage.overview.metadata"/></a></li>
                            <li><a href="#anchor-visibility" class="sidebar-navigation-link"><span class="ov-dot ${sharingStatus}"></span><@s.text name="manage.overview.sharing.title"/></a></li>
                            <li><a href="#anchor-publish" class="sidebar-navigation-link"><span class="ov-dot ${publicationStatus}"></span><@s.text name="manage.overview.published"/></a></li>
                            <li><a href="#anchor-managers" class="sidebar-navigation-link"><span class="ov-dot ok"></span><@s.text name="manage.overview.resource.managers"/></a></li>
                        </ul>
                    </nav>

                    <div class="ov-ready">
                        <#if attention?has_content>
                            <b><@s.text name="manage.overview.attention.title"><@s.param>${attention?size}</@s.param></@s.text></b>
                            <ul>
                                <#list attention as a>
                                    <li><span class="ov-dot ${a[0]}"></span><span>${a[1]}</span></li>
                                </#list>
                            </ul>
                        <#elseif isDeleted>
                            <b><@s.text name="manage.overview.attention.readiness"/></b>
                            <ul><li><span class="ov-dot off"></span><span><@s.text name="manage.overview.banner.deleted.title"/></span></li></ul>
                        <#else>
                            <b><@s.text name="manage.overview.attention.readiness"/></b>
                            <ul><li><span class="ov-dot ok"></span><span><@s.text name="manage.overview.attention.none"/></span></li></ul>
                        </#if>
                    </div>
                </div>

                <div class="bd-content ps-lg-4 pt-4">
                    <#-- messages from the last action and loading problems, in the content column next to the cards they are about -->
                    <div class="ov-alerts">
                    <#include "/WEB-INF/pages/inc/action_alerts.ftl">

                    <#-- problems the IPT worked around when loading the resource, e.g. unreadable metadata: they stay until fixed -->
                    <#list resource.loadNotices?filter(n -> n.needsAttention) as n>
                        <div class="alert alert-warning d-flex" role="status">
                            <div class="me-3">
                                <@icon name="warning" class="alert-orange-2 fs-bigger-2 me-2"/>
                            </div>
                            <div class="overflow-x-hidden pt-1">
                                <@s.text name=n.textKey><@s.param>${n.detail}</@s.param></@s.text>
                            </div>
                        </div>
                    </#list>

                    <div id="sources-created-successfully-info" class="alert alert-success alert-dismissible fade show d-flex" role="alert" style="display: none !important;">
                        <div class="me-3">
                            <@icon name="check_circle" class="alert-green-2 fs-bigger-2 me-2"/>
                        </div>
                        <div class="overflow-x-hidden pt-1 message">
                        </div>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                    </div>

                    <#-- a banner only when something stops publishing; versions and changes are in the Publication card -->
                    <#if isDeleted>
                        <div id="overview-status" class="ov-banner del" role="status">
                            <@icon name="cancel" class="ov-ic text-discreet"/>
                            <div class="ov-grow">
                                <h2><@s.text name="manage.overview.banner.deleted.title"/></h2>
                                <div class="ov-text"><#if resource.isAlreadyAssignedDoi()><@s.text name="manage.resource.status.intro.deleted"/><#else><@s.text name="manage.overview.banner.deleted.text"/></#if></div>
                            </div>
                            <#if disableRegistrationRights == "false">
                                <button type="button" class="btn btn-sm btn-gbif-solid proxy-button-undelete-resource"><@s.text name="button.undelete"/></button>
                            </#if>
                        </div>
                    <#elseif publishBlockers?has_content>
                        <div id="overview-status" class="ov-banner bad" role="status">
                            <@icon name="error" class="ov-ic alert-red-2"/>
                            <div class="ov-grow">
                                <h2><#if isPublished><@s.text name="manage.overview.banner.blocked.title"><@s.param>${nextVersion}</@s.param></@s.text><#else><@s.text name="manage.overview.banner.blocked.titleFirst"/></#if></h2>
                                <ul class="ov-text">
                                    <#list publishBlockers as b>
                                        <li>${b.text}${b.detail}<#if b.href?has_content> <a href="${b.href}" class="${b.linkClass}">${b.linkText} →</a></#if></li>
                                    </#list>
                                </ul>
                            </div>
                        </div>
                    <#elseif usesAutoPublishing && autoPublicationSuspendedForThisResource>
                        <div id="overview-status" class="ov-banner bad" role="status">
                            <@icon name="error" class="ov-ic alert-red-2"/>
                            <div class="ov-grow">
                                <h2><@s.text name="manage.overview.banner.suspended.title"/></h2>
                                <div class="ov-text"><@s.text name="manage.overview.banner.suspended.text"/></div>
                            </div>
                            <#if report??><a href="#anchor-publish" class="btn btn-sm btn-outline-secondary ov-show-report"><@s.text name="manage.overview.published.report.show"/></a></#if>
                        </div>
                    </#if>

                    <#if !isMetadataOnly>
                        <#include "/WEB-INF/pages/manage/overview_data.ftl"/>
                    </#if>
                    <#include "/WEB-INF/pages/manage/overview_metadata.ftl"/>
                    <#include "/WEB-INF/pages/manage/overview_sharing.ftl"/>
                    <#include "/WEB-INF/pages/manage/overview_publication.ftl"/>
                </div>
            </main>
        </div>
    </div>

    <div id="make-public-modal" class="modal fade" tabindex="-1" aria-labelledby="make-public-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <img src="${baseURL}/images/logo-modal-success.png" alt="Success" class="modal-image" />
                </div>
                <div class="modal-body">
                    <h5 class="modal-title w-100" id="make-public-modal-title"><@s.text name="manage.overview.visibility.change.public"/></h5>

                    <div class="border rounded px-3 py-1 mt-3">
                        <div class="simpleCallout">
                            <div class="simpleCallout-inner">
                                <div class="simpleCalloutInfo simpleCalloutInfo-message">
                                    <div class="simpleCalloutIcon">
                                        <@icon name="info" class="text-gbif-primary"/>
                                    </div>
                                    <div class="simpleCalloutMeta">
                                        <div class="simpleCalloutMessage">
                                            <@s.text name="manage.overview.visibility.change.republish"/>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="pt-3">
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="makePublicOptions" id="makePublicImmediately" value="makePublicImmediately" <#if !resource.makePublicDate?has_content>checked</#if> >
                            <label class="form-check-label" for="inlineRadio1"><@s.text name="manage.overview.visibility.change.public.immediately"/></label>
                        </div>
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="makePublicOptions" id="makePublicAtDate" value="makePublicAtDate" <#if resource.makePublicDate?has_content>checked</#if> >
                            <label class="form-check-label" for="inlineRadio2"><@s.text name="manage.overview.visibility.change.public.date"/></label>
                        </div>
                    </div>

                    <div id="makePublicDateTimeWrapper" class="pt-2" style="display: none !important;">
                        <div class="d-flex justify-content-center">
                            <form id="make-public-modal-form" action="resource-makePublic.do" method="post">
                                <input name="r" type="hidden" value="${resource.shortname}"/>
                                <#if resource.makePublicDate?has_content>
                                    <input id="makePublicDateTime" name="makePublicDateTime" class="form-control form-control-sm" type="datetime-local" value="${resource.makePublicDate?datetime?string["yyyy-MM-dd'T'HH:mm"]}" />
                                <#else>
                                    <input id="makePublicDateTime" name="makePublicDateTime" class="form-control form-control-sm" type="datetime-local" value="${makePublicDateTime!}" />
                                </#if>
                            </form>
                            <form id="cancel-make-public" action="resource-cancelVisibilityChange.do" method="post">
                                <input name="r" type="hidden" value="${resource.shortname}"/>
                            </form>
                        </div>
                    </div>
                </div>
                <div class="modal-footer justify-content-center">
                    <button id="cancel-button" type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal"><@s.text name="button.cancel"/></button>
                    <button id="changeStateSubmit" type="submit" form="make-public-modal-form" class="btn btn-sm btn-outline-gbif-primary"><@s.text name="button.submit"/></button>
                    <#if resource.makePublicDate?has_content>
                        <button id="cancelMakePublic" type="submit" form="cancel-make-public" class="btn btn-sm btn-outline-gbif-danger"><@s.text name="button.reset"/></button>
                    </#if>
                </div>
            </div>
        </div>
    </div>

    <div id="make-private-modal" class="modal fade" tabindex="-1" aria-labelledby="make-private-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <img src="${baseURL}/images/logo-modal-warning.png" alt="Warning" class="modal-image" />
                </div>
                <div class="modal-body">
                    <h5 class="modal-title w-100" id="make-private-modal-title"><@s.text name="manage.overview.visibility.change.private"/></h5>

                    <div class="border rounded px-3 py-1 mt-3">
                        <div class="simpleCallout">
                            <div class="simpleCallout-inner">
                                <div class="simpleCalloutInfo simpleCalloutInfo-warning">
                                    <div class="simpleCalloutIcon">
                                        <@icon name="info" class="text-gbif-danger"/>
                                    </div>
                                    <div class="simpleCalloutMeta">
                                        <div class="simpleCalloutMessage">
                                            <@s.text name="manage.overview.visibility.change.republish"/>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="mt-3">
                        <form id="make-private-modal-form" action="resource-makePrivate.do" method="post">
                            <input name="r" type="hidden" value="${resource.shortname}"/>
                            <input name="unpublish" type="hidden" value="Change"/>
                        </form>
                        <@s.text name="manage.overview.visibility.confirm.make.private"/>
                    </div>
                </div>

                <div class="modal-footer justify-content-center">
                    <button id="cancel-button" type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal"><@s.text name="button.cancel"/></button>
                    <button id="changeStateSubmit" type="submit" form="make-private-modal-form" class="btn btn-sm btn-outline-gbif-danger"><@s.text name="button.submit"/></button>
                </div>
            </div>
        </div>
    </div>

    <div id="cancel-visibility-change-modal" class="modal fade" tabindex="-1" aria-labelledby="cancel-visibility-change-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <img src="${baseURL}/images/logo-modal-warning.png" alt="Warning" class="modal-image" />
                </div>
                <div class="modal-body">
                    <h5 class="modal-title w-100" id="cancel-visibility-change-modal-title"><@s.text name="manage.overview.visibility.change.cancel"/></h5>

                    <div class="mt-3">
                        <form id="cancel-visibility-change-modal-form" action="resource-cancelVisibilityChange.do" method="post">
                            <input name="r" type="hidden" value="${resource.shortname}"/>
                        </form>
                        <@s.text name="manage.overview.visibility.confirm.cancel.change"><@s.param>${resource.status!}</@s.param><@s.param>${resource.pendingStatus!}</@s.param></@s.text>
                    </div>
                </div>

                <div class="modal-footer justify-content-center">
                    <button id="cancel-button" type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal"><@s.text name="button.cancel"/></button>
                    <button id="changeStateSubmit" type="submit" form="cancel-visibility-change-modal-form" class="btn btn-sm btn-outline-gbif-danger"><@s.text name="button.submit"/></button>
                </div>
            </div>
        </div>
    </div>

    <div id="visibility-disabled-modal" class="modal fade" tabindex="-1" aria-labelledby="make-public-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <img src="${baseURL}/images/logo-modal-warning.png" alt="Warning" class="modal-image" />
                </div>

                <div class="modal-body">
                    <h5 class="modal-title w-100" id="make-public-modal-title"><@s.text name="manage.overview.visibility"/></h5>
                    <#if resource.status == "DELETED">
                        <@s.text name="manage.overview.visibility.warning.deleted"/>
                    <#elseif resource.status == "REGISTERED">
                        <@s.text name="manage.overview.visibility.warning.registered"/>
                    <#elseif resource.identifierStatus?has_content && (resource.identifierStatus == "PUBLIC" || resource.identifierStatus == "UNAVAILABLE")>
                        <@s.text name="manage.overview.visibility.warning.identifier"/>
                    <#else>
                        <@s.text name="manage.overview.visibility.warning.general"/>
                    </#if>
                </div>

                <div class="modal-footer justify-content-center">
                    <button id="cancel-button" type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">
                        <@s.text name="button.cancel"/>
                    </button>
                </div>
            </div>
        </div>
    </div>

    <div id="source-data-modal" class="modal fade" tabindex="-1" aria-labelledby="source-data-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <img src="${baseURL}/images/logo-modal-success.png" alt="Success" class="modal-image" />
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
                                    <div id="callout-file-upload-info" class="border rounded px-3 py-1 mt-3">
                                        <div class="simpleCallout">
                                            <div class="simpleCallout-inner">
                                                <div class="simpleCalloutInfo simpleCalloutInfo-message">
                                                    <div class="simpleCalloutIcon">
                                                        <@icon name="info" class="text-gbif-primary"/>
                                                    </div>
                                                    <div class="simpleCalloutMeta">
                                                        <div class="simpleCalloutMessage">
                                                            <@s.text name="manage.source.file.multipleUpload"/>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <a id="chooseFilesButton" href="#" class="btn btn-sm btn-outline-gbif-primary mt-3"><@s.text name="button.chooseFiles"/></a>

                                    <div id="callout-source-exists" class="border rounded px-3 py-1 mt-3" style="display: none;">
                                        <div class="simpleCallout" >
                                            <div class="simpleCallout-inner">
                                                <div class="simpleCalloutInfo simpleCalloutInfo-warning">
                                                    <div class="simpleCalloutIcon">
                                                        <@icon name="error" class="text-gbif-danger"/>
                                                    </div>
                                                    <div class="simpleCalloutMeta">
                                                        <div class="simpleCalloutError">
                                                            <@s.text name="manage.resource.addSource.sameName.confirm"/>
                                                            <a id="btn-confirm-source-overwrite" class="confirmOverwriteSourceLink custom-link" href="#">
                                                                <@s.text name="button.confirm"/>
                                                            </a>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <div id="callout-not-enough-space" class="callout callout-danger text-smaller" style="display: none;">
                                        <@s.text name="manage.resource.addSource.notEnoughSpace"><@s.param>${freeDiscSpaceReadable}</@s.param></@s.text>
                                    </div>

                                    <input id="fileInput" type="file" multiple style="display: none;" />
                                    <div id="fileList"></div>

                                    <ul id="field-error-file" class="invalid-feedback list-unstyled field-error my-1">
                                        <li class="text-start">
                                            <span><@s.text name="manage.overview.source.file.too.big"/></span>
                                        </li>
                                    </ul>

                                    <div>
                                        <input type="text" id="sourceName" name="sourceName" class="form-control form-control-sm my-1" placeholder="<@s.text name='source.name'/>" style="display: none">
                                        <ul id="field-error-sourceName" class="invalid-feedback list-unstyled field-error my-1">
                                            <li class="text-start">
                                                <span><@s.text name="manage.source.name.empty"/></span>
                                            </li>
                                        </ul>
                                    </div>

                                    <div>
                                        <input type="url" id="url" name="url" class="form-control form-control-sm my-1" placeholder="URL" style="display: none">
                                        <ul id="field-error-url" class="invalid-feedback list-unstyled field-error my-1">
                                            <li class="text-start">
                                                <@s.text name="validation.required"><@s.param><@s.text name="manage.source.url"/></@s.param></@s.text>
                                            </li>
                                        </ul>
                                    </div>

                                    <div id="url-source-info-block" style="display: none;">
                                        <div id="url-source-info-block-inner" class="d-flex justify-content-start fs-smaller-2">
                                            <div id="url-source-check-link-wrapper" class="me-3" style="display: none;">
                                                <a id="check-url-source-link" class="smaller-action-link-button action-link-button-primary" href="#"><@s.text name="manage.source.url.checkUrl"/></a>
                                            </div>
                                            <div id="url-source-check-status-wrapper" class="me-3" style="display: none;">
                                                <@icon name="cancel" class="text-gbif-danger" id="url-source-check-status-fail" style="display: none;"/>
                                                <@icon name="check_circle" class="text-gbif-primary" id="url-source-check-status-success" style="display: none;"/>
                                            </div>
                                            <div id="url-source-size-wrapper" class="me-3" style="display: none;">
                                                <strong><@s.text name="manage.source.url.checkUrl.size"/>:</strong> <span id="url-source-size">?</span>
                                            </div>
                                            <div id="url-source-content-type-wrapper" style="display: none;">
                                                <strong><@s.text name="manage.source.url.checkUrl.type"/>:</strong> <span id="url-source-content-type">?</span>
                                            </div>
                                            <div id="url-source-error-message-wrapper" class="text-gbif-danger" style="display: none;">
                                                <@s.text name="manage.source.url.checkUrl.failed"/>
                                            </div>
                                        </div>
                                    </div>

                                    <input id="sourceType" type="hidden" name="sourceType" value="source-sql"/>
                                </div>
                            </div>
                        </form>
                        <form id="cancelOverwriteForm" action='canceloverwrite.do' method='post'>
                            <input name="r" type="hidden" value="${resource.shortname}"/>
                            <input name="validate" type="hidden" value="false"/>
                        </form>
                    </div>
                </div>
                <div class="modal-footer justify-content-center">
                    <a id="sendButton" href="#" class="btn btn-sm btn-outline-gbif-primary" style="display: none;"><@s.text name="button.upload"/></a>
                    <input type="submit" form="addSourceForm" value="Add" id="add" name="add" class="btn btn-sm btn-outline-gbif-primary" style="display: none;">
                    <input type="submit" form="addSourceForm" value="Clear" id="clear" name="clear" class="btn btn-sm btn-outline-secondary" style="display: none;">
                    <input type="submit" form="cancelOverwriteForm" value="Cancel" id="clear" name="clear" class="btn btn-sm btn-outline-secondary" style="display: none;">
                    <button id="cancel-button" type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal"><@s.text name="button.cancel"/></button>
                </div>
            </div>
        </div>
    </div>

    <div id="mapping-modal" class="modal fade" tabindex="-1" aria-labelledby="mapping-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <#assign numberOfPotentialCores=potentialCores?size />

                <div class="modal-header flex-column">
                    <#if numberOfPotentialCores==0>
                        <img src="${baseURL}/images/logo-modal-warning.png" alt="Warning" class="modal-image" />
                    <#else>
                        <img src="${baseURL}/images/logo-modal-success.png" alt="Success" class="modal-image" />
                    </#if>
                </div>

                <div class="modal-body">
                    <h5 class="modal-title w-100" id="mapping-modal-title"><@s.text name="manage.mapping.title"/></h5>
                    <div>
                        <#if (numberOfPotentialCores>0)>
                            <form id="addMappingForm" action='mapping.do' method='post'>
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
                            </form>
                        <#else>
                            <#if isDataPackage>
                                <@s.text name="manage.overview.mappings.cantdo"/>
                            <#else>
                                <@s.text name="manage.overview.DwC.Mappings.cantdo"/>
                            </#if>
                        </#if>
                    </div>
                </div>

                <div class="modal-footer justify-content-center">
                    <input type="submit" value="Add" id="add" name="add" form="addMappingForm" class="btn btn-sm btn-outline-gbif-primary">
                    <button id="cancel-button" type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">
                        <@s.text name="button.cancel"/>
                    </button>
                </div>
            </div>
        </div>
    </div>

    <div id="metadata-modal" class="modal fade" tabindex="-1" aria-labelledby="metadata-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <img src="${baseURL}/images/logo-modal-success.png" alt="Success" class="modal-image" />
                </div>
                <div class="modal-body">
                    <h5 class="modal-title w-100" id="metadata-modal-title"><@s.text name="manage.overview.metadata"/></h5>
                    <div>
                        <#if isDataPackage && !isDwcDp>
                            <form id="upload-metadata-form" action='replace-datapackage-metadata.do' method='post' enctype="multipart/form-data">
                                <input name="r" type="hidden" value="${resource.shortname}"/>
                                <div class="row">
                                    <#if resource.citationAutoGenerated>
                                        <div class="fs-smaller">
                                            <div id="callout-metadata-citation-override-info" class="border rounded px-3 py-1 mb-2">
                                                <div class="simpleCallout">
                                                    <div class="simpleCallout-inner">
                                                        <div class="simpleCalloutInfo simpleCalloutInfo-warning">
                                                            <div class="simpleCalloutIcon">
                                                                <@icon name="error" class="text-gbif-danger"/>
                                                            </div>
                                                            <div class="simpleCalloutMeta">
                                                                <div class="simpleCalloutMessage">
                                                                    <@s.text name="manage.overview.metadata.citation.overridden"/>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </#if>

                                    <div class="col-12">
                                        <@s.file name="datapackageMetadataFile" cssClass="form-control form-control-sm my-1"/>
                                    </div>

                                    <div id="datapackage-metadata-validate" class="col-12 text-smaller" style="display: none;">
                                        <@checkbox name="validateDatapackageMetadata" i18nkey="button.validate" />
                                    </div>
                                </div>
                            </form>
                        <#else>
                            <form id="upload-metadata-form" action='replace-eml.do' method='post' enctype="multipart/form-data">
                                <input name="r" type="hidden" value="${resource.shortname}"/>
                                <div class="row">
                                    <div class="col-12">
                                        <@s.file name="emlFile" cssClass="form-control form-control-sm my-1"/>
                                    </div>
                                    <div id="eml-validate" class="col-12" style="display: none;">
                                        <@checkbox name="validateEml" i18nkey="button.validate" />
                                    </div>
                                </div>
                            </form>
                        </#if>
                    </div>
                </div>
                <div class="modal-footer justify-content-center">
                    <#if isDataPackage && !isDwcDp>
                        <input type="submit" form="upload-metadata-form" value="Replace" id="datapackageMetadataReplace" name="datapackageMetadataReplace" class="btn btn-sm btn-outline-gbif-primary confirmDatapackageMetadataReplace" style="">
                        <button id="datapackageMetadataCancel" type="button" class="btn btn-sm btn-outline-secondary " data-bs-dismiss="modal"><@s.text name="button.cancel"/></button>
                    <#else>
                        <input type="submit" form="upload-metadata-form" value="Replace" id="emlReplace" name="emlReplace" class="btn btn-sm btn-outline-gbif-primary confirmEmlReplace" style="display:none;">
                        <button id="emlCancel" type="button" class="btn btn-sm btn-outline-secondary " data-bs-dismiss="modal"><@s.text name="button.cancel"/></button>
                    </#if>
                </div>
            </div>
        </div>
    </div>

    <div id="publication-modal" class="modal fade" tabindex="-1" aria-labelledby="publication-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <img src="${baseURL}/images/logo-modal-warning.png" alt="Warning" class="modal-image" />
                </div>

                <div class="modal-body">
                    <h5 class="modal-title w-100" id="publication-modal-title"><@s.text name="manage.overview.published"/></h5>
                    <!-- resources cannot be published if it's deleted -->
                    <#if resource.status == "DELETED">
                        <p class="mb-0">
                            <@s.text name="manage.overview.published.deleted"/>
                        </p>

                        <!-- resources cannot be published if the mandatory metadata is missing -->
                    <#elseif missingBasicMetadata>
                        <p>
                            <@s.text name="manage.overview.published.missing.metadata"/>
                        </p>

                        <#if !isDataPackage>
                            <p class="mb-0">
                                <@s.text name="manage.overview.published.metadata.validation.report"/>
                            </p>
                        </#if>

                        <!-- resources cannot be published if the metadata is invalid -->
                    <#elseif !validMetadata>
                        <p>
                            <@s.text name="manage.overview.published.metadata.invalid"/>
                        </p>

                        <#if !isDataPackage || isColDp>
                        <p class="mb-0">
                            <@s.text name="manage.overview.published.metadata.validation.report"/>
                        </p>
                        </#if>

                        <!-- DwC-A resources cannot be published if the publishing organization is missing -->
                    <#elseif !isDataPackage && !resource.organisation?has_content>
                        <p class="mb-0">
                            <@s.text name="manage.overview.published.missing.organisation"/>
                        </p>

                        <p class="mb-0">
                            <@s.text name="manage.overview.published.missing.organisation.link"/>
                        </p>

                      <!-- resources cannot be published if mappings are missing (for DPs) -->
                    <#elseif isDataPackage && dataPackageMappingsMissing>
                        <p class="mb-0">
                            <@s.text name="manage.overview.published.missing.mappings"/>
                        </p>

                    <#elseif isDataPackage && !allRequiredFieldsMappedDataPackage>
                        <p>
                            <@s.text name="manage.overview.published.missing.requiredFields"/>
                        </p>

                        <p class="mb-0">
                            <#list missingRequiredFieldsDataPackage as key, value>
                                <strong>${key}</strong>:
                                <#list value as field>
                                    <code>${field.name}</code><#sep>, </#sep>
                                </#list><br>
                            </#list>
                        </p>

                        <!-- resources cannot be published if mappings refer to tables not in the installed schema (for DPs) -->
                    <#elseif isDataPackage && dataPackageMappingsWithMissingTable?has_content>
                        <p>
                            <@s.text name="manage.overview.published.missing.tables"/>
                        </p>

                        <p class="mb-0">
                            <#list dataPackageMappingsWithMissingTable as mappingLabel>
                                <code>${mappingLabel}</code><#sep><br></#sep>
                            </#list>
                        </p>

                        <!-- resources that are already registered cannot be re-published if they haven't been assigned a GBIF-supported license -->
                    <#elseif resource.isRegistered() && !resource.isAssignedGBIFSupportedLicense()>
                        <p class="mb-0">
                            <@s.text name="manage.overview.prevented.resource.publishing.noGBIFLicense" />
                        </p>

                        <!-- resources with a reserved DOI, existing registered DOI, or registered with GBIF can only be republished by managers with registration rights -->
                    <#elseif (resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION")
                    || (resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi())
                    || resource.status == "REGISTERED">
                        <!-- the user must have registration rights -->
                        <#if !currentUser.hasRegistrationRights()>
                            <p class="mb-0">
                                <@s.text name="manage.resource.status.publication.forbidden"/>
                                &nbsp;<@s.text name="manage.resource.role.change"/>
                            </p>

                            <!-- an organisation with DOI account be activated (if resource has a reserved DOI or existing registered DOI) -->
                        <#elseif ((resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION" && resource.isAlreadyAssignedDoi())
                        || (resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi()))
                        && !organisationWithPrimaryDoiAccount??>
                            <p class="mb-0">
                                <@s.text name="manage.resource.status.publication.forbidden.account.missing" />
                            </p>

                            <!-- when a DOI is reserved.. -->
                        <#elseif resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION">
                            <!-- and the resource has no existing DOI and its status is private..  -->
                            <#if !resource.isAlreadyAssignedDoi() && resource.status == "PRIVATE">
                                <!-- and the resource has never been published before, the first publication is a new major version -->
                                <#if !resource.lastPublished??>
                                    <p class="mb-0">
                                        <@s.text name="manage.overview.publishing.doi.register.prevented.notPublic"/>
                                    </p>

                                    <!-- and the resource has been published before, the next publication is a new minor version -->
                                <#else>
                                    <p class="mb-0">
                                        <@s.text name="manage.overview.publishing.doi.register.prevented.notPublic" />
                                    </p>
                                </#if>

                                <!-- and its status is public (or registered), its reserved DOI can be registered during next publication  -->
                            <#elseif resource.status == "PUBLIC" || resource.status == "REGISTERED">
                                <div id="callout-doi-register-info" class="border rounded px-3 py-1 mt-3">
                                    <div class="simpleCallout">
                                        <div class="simpleCallout-inner">
                                            <div class="simpleCalloutInfo simpleCalloutInfo-message">
                                                <div class="simpleCalloutIcon">
                                                    <@icon name="info" class="text-gbif-primary"/>
                                                </div>
                                                <div class="simpleCalloutMeta">
                                                    <div class="simpleCalloutMessage">
                                                        <@s.text name="manage.overview.publishing.doi.register.help"/>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                            </#if>
                        </#if>
                    </#if>
                </div>

                <div class="modal-footer justify-content-center">
                    <button id="cancel-button" type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">
                        <@s.text name="button.cancel"/>
                    </button>
                </div>
            </div>
        </div>
    </div>

    <div id="networks-modal" class="modal fade" tabindex="-1" aria-labelledby="networks-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <#if !networksAvailable || resource.status == "DELETED" || (potentialNetworks?size==0) || !resource.key?has_content>
                        <img src="${baseURL}/images/logo-modal-warning.png" alt="Warning" class="modal-image" />
                    <#else>
                        <img src="${baseURL}/images/logo-modal-success.png" alt="Success" class="modal-image" />
                    </#if>
                </div>

                <div class="modal-body">
                    <h5 class="modal-title w-100" id="networks-modal-title"><@s.text name="manage.overview.networks.title"/></h5>
                    <#if resource.status == "DELETED">
                        <p class="mb-0">
                            <@s.text name="manage.overview.networks.deleted"/>
                        </p>
                    <#elseif !networksAvailable>
                        <p class="mb-0">
                            <@s.text name="manage.overview.networks.registryAccess"/>
                        </p>
                    <#elseif (potentialNetworks?size==0)>
                        <p class="mb-0">
                            <@s.text name="manage.overview.networks.select.empty"/>
                        </p>
                    <#elseif resource.key?has_content>
                        <div>
                            <div id="obis-network-validation-notification" class="callout callout-info text-smaller" style="display: none;">
                                <@s.text name="manage.overview.networks.obis.notification"/>
                            </div>
                            <form id="addNetworkForm" action='resource-addNetwork.do' method='post'>
                                <input name="r" type="hidden" value="${resource.shortname}"/>
                                <select name="id" class="form-select my-1" id="network" size="1">
                                    <option value="" disabled selected><@s.text name='manage.overview.networks.select'/></option>
                                    <#list potentialNetworks?sort_by("name") as n>
                                        <option value="${n.key}">${n.name}</option>
                                    </#list>
                                </select>
                            </form>
                        </div>
                    <#else>
                        <p class="mb-0">
                            <@s.text name="manage.overview.networks.not.registered"/>
                        </p>
                    </#if>
                </div>

                <div class="modal-footer justify-content-center">
                    <input type="submit" form="addNetworkForm" value="Add" id="add-network" name="add" class="btn btn-sm btn-outline-gbif-primary" style="display: none;">
                    <button id="cancel-button" type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">
                        <@s.text name="button.cancel"/>
                    </button>
                </div>
            </div>
        </div>
    </div>

    <#if resource.isDwcDp() && resource.status=="REGISTERED" && currentUser.hasRegistrationRights()>
        <div id="dwc-dp-dataset-type-modal" class="modal fade" tabindex="-1" aria-labelledby="dwc-dp-dataset-type-modal-title" aria-hidden="true">
            <div class="modal-dialog modal-confirm modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header flex-column">
                        <img src="${baseURL}/images/logo-modal-success.png" alt="Success" class="modal-image" />
                    </div>

                    <div class="modal-body">
                        <h5 class="modal-title w-100" id="dwc-dp-dataset-type-modal-title"><@s.text name="manage.overview.registration.datasetType.change"/></h5>
                        <p class="mb-2">
                            <@s.text name="manage.overview.registration.datasetType.change.confirm"/>
                        </p>
                        <form id="dwcDpDatasetTypeForm" action="resource-updateDwcDpDatasetType.do" method="post">
                            <input name="r" type="hidden" value="${resource.shortname}"/>
                            <select name="dwcDpDatasetType" class="form-select my-1" id="registeredDwcDpDatasetType" size="1">
                                <option value="" disabled <#if !resource.dwcDpDatasetType??>selected</#if>><@s.text name="manage.overview.registration.datasetType.select"/></option>
                                <#list action.getDwcDpRegistrationDatasetTypes() as datasetType>
                                    <option value="${datasetType.name()}" <#if resource.dwcDpDatasetType?? && resource.dwcDpDatasetType == datasetType>selected</#if>><@s.text name="portal.resource.type.${datasetType.name()?lower_case?replace('_', '')}"/></option>
                                </#list>
                            </select>
                        </form>
                    </div>

                    <div class="modal-footer justify-content-center">
                        <button id="cancel-button" type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">
                            <@s.text name="button.cancel"/>
                        </button>
                        <input type="submit" form="dwcDpDatasetTypeForm" value="<@s.text name="button.update"/>" id="update-dwc-dp-dataset-type" class="btn btn-sm btn-outline-gbif-primary" <#if !resource.dwcDpDatasetType??>style="display: none;"</#if>>
                    </div>
                </div>
            </div>
        </div>
    </#if>

    <div id="registration-modal" class="modal fade" tabindex="-1" aria-labelledby="registration-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <img src="${baseURL}/images/logo-modal-warning.png" alt="Warning" class="modal-image" />
                </div>

                <#assign hasToBePublishedFirst=false/>

                <div class="modal-body">
                    <h5 class="modal-title w-100" id="registration-modal-title"><@s.text name="manage.overview.registration"/></h5>

<#--                    <#if cfg.devMode() || cfg.getRegistryType()!='PRODUCTION'>-->
<#--                        <p class="fst-italic">-->
<#--                            <@s.text name="manage.overview.published.testmode.warning"/>-->
<#--                        </p>-->
<#--                    </#if>-->

                    <#if resource.status=="DELETED">
                        <!-- Show warning: resource must not be deleted -->
                        <p class="mb-0">
                            <@s.text name="manage.overview.registration.deleted" />
                        </p>
                    <#elseif resource.status=="REGISTERED">
                        <!-- Show warning: resource already registered -->
                        <p class="mb-0">
                            <@s.text name="manage.overview.registration.registered" />
                        </p>
                    <#elseif resource.status=="PRIVATE">
                        <!-- Show warning: resource must be public -->
                        <p class="mb-0">
                            <@s.text name="manage.overview.registration.private" />
                        </p>
                    <#elseif resource.status=="PUBLIC">
                        <#if !currentUser.hasRegistrationRights()>
                            <!-- Show warning: user must have registration rights -->
                            <p class="mb-0">
                                <@s.text name="manage.resource.status.registration.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/>
                            </p>
                        <#elseif resource.dataPackage && resource.coreType != "camtrap-dp" && resource.coreType != "coldp" && resource.coreType != "dwc-dp">
                            <!-- Show warning: registration is not available now -->
                            <p class="mb-0">
                                <@s.text name="manage.resource.status.registration.forbiddenTypes"/>
                            </p>
                        <#elseif missingValidPublishingOrganisation?string == "true">
                            <!-- Show warning: user must assign valid publishing organisation -->
                            <p class="mb-0">
                                <@s.text name="manage.overview.visibility.missing.organisation"/>
                            </p>
                        <#elseif missingRegistrationMetadata?string == "true">
                            <!-- Show warning: user must fill in minimum registration metadata -->
                            <p class="mb-0">
                                <@s.text name="manage.overview.visibility.missing.metadata" />
                            </p>
                        <#elseif !resource.isLastPublishedVersionPublic()>
                            <!-- Show warning: last published version must be publicly available to register -->
                            <p class="mb-0">
                                <@s.text name="manage.overview.prevented.resource.registration.notPublic" />
                            </p>
                            <#assign hasToBePublishedFirst=true/>
                        <#elseif !action.isLastPublishedVersionAssignedGBIFSupportedLicense(resource)>
                            <!-- Show warning: resource must be assigned a GBIF-supported license to register if resource has occurrence data -->
                            <p class="mb-0">
                                <@s.text name="manage.overview.prevented.resource.registration.noGBIFLicense" escapeHtml=true/>
                            </p>
                        </#if>
                    </#if>
                </div>

                <div class="modal-footer justify-content-center">
                    <#if hasToBePublishedFirst>
                        <button id="publish-button" type="button" class="btn btn-sm btn-outline-gbif-primary ${buttonClass!"confirmPublishMinorVersion"}" data-bs-dismiss="modal">
                            <@s.text name="button.publish"/>
                        </button>
                    </#if>
                    <button id="cancel-button" type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">
                        <@s.text name="button.cancel"/>
                    </button>
                </div>
            </div>
        </div>
    </div>

    <div id="managers-modal" class="modal fade" tabindex="-1" aria-labelledby="managers-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <#assign numberOfPotentialManagers=potentialManagers?size />

                <div class="modal-header flex-column">
                    <#if (numberOfPotentialManagers==0)>
                        <img src="${baseURL}/images/logo-modal-warning.png" alt="Warning" class="modal-image" />
                    <#else>
                        <img src="${baseURL}/images/logo-modal-success.png" alt="Success" class="modal-image" />
                    </#if>
                </div>

                <div class="modal-body">
                    <h5 class="modal-title w-100" id="managers-modal-title"><@s.text name="manage.overview.resource.managers"/></h5>
                    <#if (numberOfPotentialManagers>0)>
                        <div>
                            <form id="addManagerForm" action='resource-addManager.do' method='post'>
                                <input name="r" type="hidden" value="${resource.shortname}"/>
                                <select name="id" class="form-select my-1" id="manager" size="1">
                                    <option value="" disabled selected><@s.text name='manage.overview.resource.managers.select'/></option>
                                    <#list potentialManagers?sort_by("name") as u>
                                        <option value="${u.email}">${u.name}</option>
                                    </#list>
                                </select>
                            </form>
                        </div>
                    <#else>
                        <p class="mb-0">
                            <@s.text name="manage.overview.resource.managers.select.empty"/>
                        </p>
                    </#if>
                </div>

                <div class="modal-footer justify-content-center">
                    <#if (numberOfPotentialManagers>0)>
                    <input type="submit" form="addManagerForm" value="Add" id="add-manager" name="add" class="btn btn-sm btn-outline-gbif-primary" style="display: none;">
                    </#if>
                    <button id="cancel-button" type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">
                        <@s.text name="button.cancel"/>
                    </button>
                </div>
            </div>
        </div>
    </div>

    <div id="reserve-doi-modal" class="modal fade" tabindex="-1" aria-labelledby="reserve-doi-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <#if !organisationWithPrimaryDoiAccount?? || !currentUser.hasRegistrationRights() || resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION">
                        <img src="${baseURL}/images/logo-modal-warning.png" alt="Warning" class="modal-image" />
                    <#else>
                        <img src="${baseURL}/images/logo-modal-success.png" alt="Success" class="modal-image" />
                    </#if>
                </div>

                <div class="modal-body">
                    <h5 class="modal-title w-100" id="reserve-doi-modal-title">DOI</h5>
                    <#if !organisationWithPrimaryDoiAccount??>
                        <@s.text name="manage.overview.publishing.doi.reserve.prevented.noOrganisation" escapeHtml=true/>
                    <#elseif !currentUser.hasRegistrationRights()>
                        <@s.text name="manage.resource.status.doi.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/>
                    <#elseif resource.identifierStatus == "UNRESERVED">
                        <@s.text name="manage.overview.publishing.doi.reserve.help" escapeHtml=true/>
                    <#elseif resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION">
                        <@s.text name="manage.overview.publishing.doi.delete.help" escapeHtml=true/>
                    <#elseif resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi() >
                        <@s.text name="manage.overview.publishing.doi.reserve.new.help" escapeHtml=true/>
                    </#if>
                </div>

                <div class="modal-footer justify-content-center">
                    <#if organisationWithPrimaryDoiAccount?? && currentUser.hasRegistrationRights()>
                        <@nextDoiButtonTD/>
                    <#else>
                        <button id="cancel-button" type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">
                            <@s.text name="button.cancel"/>
                        </button>
                    </#if>
                </div>
            </div>
        </div>
    </div>

    <div id="delete-resource-modal" class="modal fade" tabindex="-1" aria-labelledby="delete-resource-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <img src="${baseURL}/images/logo-modal-warning.png" alt="Warning" class="modal-image" />
                </div>

                <div class="modal-body">
                    <h5 class="modal-title w-100" id="delete-resource-modal-title"><@s.text name="manage.overview.resource.delete"/></h5>
                    <@s.text name="manage.overview.resource.delete.description"/>
                </div>

                <div class="modal-footer justify-content-center">
                    <button class="btn btn-sm btn-outline-gbif-danger proxy-button-delete-from-ipt" name="delete"><@s.text name="button.delete.fromIpt"/></button>
                    <button class="btn btn-sm btn-outline-gbif-danger proxy-button-delete-from-gbif-and-ipt" name="delete"><@s.text name="button.delete.fromIptAndGbif"/></button>
                </div>
            </div>
        </div>
    </div>

    <div id="delete-resource-disabled-modal" class="modal fade" tabindex="-1" aria-labelledby="delete-resource-disabled-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <img src="${baseURL}/images/logo-modal-warning.png" alt="Warning" class="modal-image" />
                </div>

                <div class="modal-body">
                    <h5 class="modal-title w-100" id="delete-resource-disabled-modal-title"><@s.text name="manage.overview.resource.delete"/></h5>
                    <#if !currentUser.hasRegistrationRights()>
                        <#if resource.status == "DELETED">
                            <@s.text name="manage.resource.status.undeletion.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/>
                        <#elseif resource.isAlreadyAssignedDoi()?string == "true" || resource.status == "REGISTERED">
                            <@s.text name="manage.resource.status.deletion.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/>
                        </#if>
                    </#if>
                </div>

                <div class="modal-footer justify-content-center">
                    <button id="cancel-button" type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">
                        <@s.text name="button.cancel"/>
                    </button>
                </div>
            </div>
        </div>
    </div>

    <div id="datapackage-metadata-modal" class="modal fade" tabindex="-1" aria-labelledby="datapackage-metadata-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm" style="max-width: none !important; margin: 1.75rem; font-size: 12px;">
            <div class="modal-content">
                <div class="modal-header flex-column">
                </div>
                <div class="modal-body" style="text-align: left !important;">
                    <h5 class="modal-title w-100" id="datapackage-metadata-modal-title">Metadata</h5>
                    <pre id="json-raw-data" class="fs-smaller-2">${datapackageMetadataRaw!}</pre>
                </div>
                <div class="modal-footer justify-content-center">
                    <button id="cancel-button" type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">
                        <@s.text name="button.cancel"/>
                    </button>
                </div>
            </div>
        </div>
    </div>

    <#if !isDataPackage || isDwcDp || isColDp>
    <div id="metadata-validation-result-modal" class="modal fade" tabindex="-1" aria-labelledby="metadata-validation-result-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <#if isColDp>
                    <#assign isMetadataValid=!colMetadataErrors?has_content />
                <#else>
                    <#assign isMetadataValid=!errorCollector.hasErrors() />
                </#if>

                <div class="modal-header flex-column">
                    <#if isMetadataValid>
                        <img src="${baseURL}/images/logo-modal-success.png" alt="Success" class="modal-image" />
                    <#else>
                        <img src="${baseURL}/images/logo-modal-warning.png" alt="Warning" class="modal-image" />
                    </#if>
                </div>
                <div class="modal-body" style="text-align: left !important;">
                    <h5 class="modal-title w-100 mb-0" id="metadata-validation-result-modal">
                        <@s.text name="manage.overview.metadata.modal.result"/>:
                        <#if isMetadataValid>
                            <span class="text-gbif-primary"><@s.text name="manage.overview.metadata.modal.valid"/></span>
                        <#else>
                            <span class="text-gbif-danger"><@s.text name="manage.overview.metadata.modal.invalid"/></span>
                        </#if>
                    </h5>

                    <#assign metadataSections = {
                    "BASIC_SECTION": "basic",
                    "CONTACTS_SECTION": "contacts",
                    "ACKNOWLEDGEMENTS_SECTION": "acknowledgements",
                    "GEOGRAPHIC_COVERAGE_SECTION": "geocoverage",
                    "TAXANOMIC_COVERAGE_SECTION": "taxcoverage",
                    "TEMPORAL_COVERAGE_SECTION": "tempcoverage",
                    "ADDITIONAL_DESCRIPTION_SECTION": "additionalDescription",
                    "PROJECT_SECTION": "project",
                    "METHODS_SECTION": "methods",
                    "CITATIONS_SECTION": "citations",
                    "COLLECTIONS_SECTION": "collections",
                    "PHYSICAL_SECTION": "physical",
                    "KEYWORDS_SECTION": "keywords",
                    "ADDITIONAL_SECTION": "additional"
                    }/>


                    <#if !isMetadataValid && isColDp>
                        <ul class="mt-2">
                            <#list colMetadataErrors as error>
                                <li>${error}</li>
                            </#list>
                        </ul>
                    <#elseif !isMetadataValid>
                        <div class="mt-2">
                            <#list errorCollector.result?keys as key>
                                <#if errorCollector.result[key].hasErrors()>
                                    <div>
                                        <div>
                                            <@icon name="close" class="text-gbif-danger"/>
                                            <span class="text-gbif-danger me-2">
                                                <b><@s.text name="submenu.${metadataSections[key]!key}"/></b>
                                            </span>
                                            <a class="metadata-action-link custom-link" type="button" href="${baseURL}/manage/metadata-${metadataSections[key]!key}.do?r=${resource.shortname}">
                                                <span>
                                                    <svg class="link-icon link-icon-primary" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                        <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                                                    </svg>
                                                </span>
                                                <span>
                                                    <@s.text name="button.edit"/>
                                                </span>
                                            </a>
                                        </div>

                                        <ul>
                                            <#list (errorCollector.result[key].fieldErrors)! as fe>
                                                <li>${fe}</li>
                                            </#list>
                                            <#list (errorCollector.result[key].actionErrors)! as ae>
                                                <li>${ae}</li>
                                            </#list>
                                        </ul>
                                    </div>
                                </#if>
                            </#list>
                        </div>
                    </#if>
                </div>
                <div class="modal-footer justify-content-center">
                    <button id="cancel-button" type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">
                        <@s.text name="button.cancel"/>
                    </button>
                </div>
            </div>
        </div>
    </div>
    </#if>

<#include "/WEB-INF/pages/inc/footer.ftl">
