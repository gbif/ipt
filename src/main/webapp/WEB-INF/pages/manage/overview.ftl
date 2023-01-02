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

<script>
    $(document).ready(function(){
        <#if confirmOverwrite>
        showConfirmOverwrite();
        </#if>
        var $registered = false;

        var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
        var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl)
        })

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

        $('.confirm').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", buttonType: "primary"});
        $('.confirmRegistration').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.visibility.confirm.registration'/> <@s.text name='manage.resource.delete.confirm.registered'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", checkboxText: "<@s.text name='manage.overview.visibility.confirm.agreement'/>", buttonType: "primary", processing: true});
        $('.confirmMakePrivate').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.visibility.confirm.make.private'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", buttonType: "primary"});
        $('.confirmEmlReplace').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.metadata.replace.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", buttonType: "primary"});
        $('.confirmDeletionFromIptAndGbif').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<#if resource.isAlreadyAssignedDoi()><@s.text name='manage.resource.delete.confirm.doi'/></br></br></#if><#if resource.status=='REGISTERED'><@s.text name='manage.resource.delete.fromIptAndGbif.confirm.registered'/></br></br></#if><@s.text name='manage.resource.delete.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>"});
        $('.confirmDeletionFromIptOnly').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<#if resource.isAlreadyAssignedDoi()><@s.text name='manage.resource.delete.confirm.doi'/></br></br></#if><#if resource.status=='REGISTERED'><@s.text name='manage.resource.delete.fromIptOnly.confirm.registered'/></br></br></#if><@s.text name='manage.resource.delete.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>"});
        $('.confirmUndeletion').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.resource.undoDelete.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", buttonType: "primary"});

        $('.confirmReserveDoi').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.publishing.doi.reserve.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", buttonType: "primary"});
        $('.confirmDeleteDoi').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.publishing.doi.delete.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>"});
        $('.confirmPublishMinorVersion').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.publishing.doi.minorVersion.confirm'/></br></br><@s.text name='manage.overview.publishing.doi.summary'/></br></br><@s.text name='manage.overview.publishing.doi.confirm.end'/>", yesAnswer : "<@s.text name='basic.yes'/>", summary : "<@s.text name='manage.overview.publishing.doi.summary.placeholder'/>", cancelAnswer : "<@s.text name='basic.no'/>", buttonType: "primary"});
        $('.confirmPublishMajorVersion').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.publishing.doi.majorVersion.confirm'/></br></br><@s.text name='manage.overview.publishing.doi.summary'/></br></br><@s.text name='manage.overview.publishing.doi.confirm.end'/>", yesAnswer : "<@s.text name='basic.yes'/>", summary : "<@s.text name='manage.overview.publishing.doi.summary.placeholder'/>", cancelAnswer : "<@s.text name='basic.no'/>", checkboxText: "<@s.text name='manage.overview.publishing.doi.register.agreement'/>", buttonType: "primary"});
        $('.confirmPublishMajorVersionWithoutDOI').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.publishing.withoutDoi.majorVersion.confirm'/></br></br><@s.text name='manage.overview.publishing.doi.summary'/></br></br><@s.text name='manage.overview.publishing.doi.confirm.end'/>", yesAnswer : "<@s.text name='basic.yes'/>", summary : "<@s.text name='manage.overview.publishing.doi.summary.placeholder'/>", cancelAnswer : "<@s.text name='basic.no'/>", buttonType: "primary"});

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
                $("#toggleReport").text("<@s.text name='basic.show'/>");
                $('#dwcaReport').fadeOut();
            } else {
                showReport = true;
                $("#toggleReport").text("<@s.text name='basic.hide'/>");
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

        $("#emlCancel").click(function(event) {
            event.preventDefault();
            $("#emlFile").prop("value", "");
            $("#emlReplace").hide();
            $("#emlCancel").hide();
            $("#eml-validate").hide();
        });

        $("#sourceType").change(function (e) {
            var sourceType = this.options[e.target.selectedIndex].value;

            if (sourceType === 'source-file') {
                $("#url").hide();
                $("#sourceName").hide();
                $("#url").prop("value", "");
                $("#file").show();
                $("#clear").show();
                $("#add").attr("value", '<@s.text name="button.add"/>');
                $("#add").hide();
            } else if (sourceType === 'source-url') {
                $("#url").show();
                $("#sourceName").show();
                $("#file").hide();
                $("#file").prop("value", "");
                $("#clear").show();
                $("#add").attr("value", '<@s.text name="button.add"/>');
                $("#add").show();
            } else {
                $("#file").hide();
                $("#sourceName").hide();
                $("#file").prop("value", "");
                $("#url").hide();
                $("#url").prop("value", "");
                $("#clear").hide();
                $("#add").attr("value", '<@s.text name="button.connect"/>');
                $("#add").show();
            }
        })

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
            } else {
                $("#add-network").hide();
            }
        });

        $("#file").change(function() {
            var usedFileName = $("#file").prop("value");
            var currentFileSize = this.files[0].size;

            // validate size
            if (currentFileSize > 209715200) {
                $("#file").addClass("is-invalid");
                return;
            }

            if (usedFileName !== "") {
                var addButton = $('#add');
                addButton.attr("value", '<@s.text name="button.add"/>');
                addButton.show();
            }
        });

        $("#clear").click(function(event) {
            event.preventDefault();
            $("#file").prop("value", "");
            $("#file").removeClass("is-invalid");
            $("#url").prop("value", "");
            if ($("#file").is(":visible")) {
                $("#add").hide();
            }
        });

        $(function() {
            $('.icon-validate').tooltip({track: true});
        });

        function showConfirmOverwrite() {
            var dialogWindow = $("#dialog");
            var titleQuestion = '<@s.text name="basic.confirm"/>';

            var question = <#if overwriteMessage?has_content>"${overwriteMessage}"<#else>"<@s.text name="manage.resource.addSource.sameName.confirm"/>"</#if>

            var yesButtonText = '<@s.text name="basic.yes"/>';
            var cancelButtonText = '<@s.text name="basic.no"/>';

            // prepare html content for modal window
            var content = '<div class="modal-dialog modal-confirm modal-dialog-centered">';
            content += '<div class="modal-content">';

            // header
            content += '<div class="modal-header flex-column">';
            content += '<div class="icon-box"><i class="confirm-danger-icon">!</i></div>'
            content += '<h5 class="modal-title w-100" id="staticBackdropLabel">' + titleQuestion + '</h5>';
            content += '<button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">×</button>'
            content += '</div>';

            // body
            content += '<div class="modal-body">';
            content += '<p>' + question + '</p>';
            content += '</div>'

            // footer
            content += '<div class="modal-footer justify-content-center">'
            content += '<button id="cancel-button" type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">' + cancelButtonText + '</button>';
            content += '<button id="yes-button" type="button" class="btn btn-outline-gbif-primary">' + yesButtonText + '</button>';
            content += '</div>';

            content += '</div>';
            content += '</div>';

            // add content to window
            dialogWindow.html(content);

            $("#yes-button").on("click", function () {
                $("#add").click()
            });

            $("#cancel-button").on("click", function () {
                $("#canceloverwrite").click();
            });

            dialogWindow.modal('show');
        }

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
            } else if (radioValue === "makePublicAtDate") {
                $("#makePublicDateTime").show();
            }
        }

        // hide/show input on radio change
        $('input[name=makePublicOptions]').on('change', function() {
            var radioValue = $('input[name=makePublicOptions]:checked').val();

            if (radioValue === "makePublicImmediately") {
                $("#makePublicDateTime").hide();
            } else if (radioValue === "makePublicAtDate") {
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

        // show spinner file upload
        $("#add").on("click", function () {
            $('#source-data-modal').modal('hide');
            displayProcessing();
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

        function showPublicationModal() {
            var dialogWindow = $("#publication-modal");
            dialogWindow.modal('show');
        }

        $("#publish-button-show-warning").on('click', function () {
            showPublicationModal();
        });

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
            showReserveDoiModal(e);
        });

        function showReserveDoiModal(e) {
            e.preventDefault();
            var dialogWindow = $("#reserve-doi-modal");
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

<#if resource.isAlreadyAssignedDoi()?string == "false" && resource.status != "REGISTERED">
    <#assign disableRegistrationRights="false"/>
<#elseif currentUser.hasRegistrationRights()?string == "true">
    <#assign disableRegistrationRights="false"/>
<#else>
    <#assign disableRegistrationRights="true"/>
</#if>

    <div class="container-fluid bg-body border-bottom">
        <div class="container my-3">
            <#include "/WEB-INF/pages/inc/action_alerts.ftl">

            <#if !currentUser.hasRegistrationRights()>
                <#if resource.status == "DELETED">
                    <div class="alert alert-warning mt-2 alert-dismissible fade show d-flex" role="alert">
                        <div class="me-3">
                            <i class="bi bi-exclamation-triangle alert-orange-2 fs-bigger-2 me-2"></i>
                        </div>
                        <div class="overflow-x-hidden pt-1">
                            <span><@s.text name="manage.resource.status.undeletion.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/></span>
                        </div>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                <#elseif resource.isAlreadyAssignedDoi()?string == "true" || resource.status == "REGISTERED">
                    <div class="alert alert-warning mt-2 alert-dismissible fade show d-flex" role="alert">
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

        <div class="container my-3 p-3">
            <div class="text-center text-uppercase fw-bold fs-smaller-2">
                <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                    <ol class="breadcrumb justify-content-center mb-0">
                        <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                        <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview"/></li>
                    </ol>
                </nav>
            </div>

            <div class="text-center mt-1">
                <h1 property="dc:title" class="rtitle pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    ${resource.title!resource.shortname}
                </h1>

                <div class="mt-2">
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
                                    <li>
                                        <form action="resource-delete.do" method='post'>
                                            <input name="r" type="hidden" value="${resource.shortname}" />
                                            <@s.submit cssClass="btn btn-sm btn-outline-gbif-danger confirmDeletion confirmDeletionFromIptAndGbif w-100 top-button" cssStyle="text-transform: unset !important" name="delete" key="button.delete.fromIptAndGbif"/>
                                        </form>
                                    </li>
                                    <li>
                                        <form action="resource-deleteFromIpt.do" method='post'>
                                            <input name="r" type="hidden" value="${resource.shortname}" />
                                            <@s.submit cssClass="btn btn-sm btn-outline-gbif-danger confirmDeletion confirmDeletionFromIptOnly w-100 top-button" cssStyle="text-transform: unset !important" name="delete" key="button.delete.fromIpt"/>
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
                    <#if resource.coreType?has_content && resource.coreType==metadataType>
                        <@s.text name="manage.overview.description.metadataOnly"/>
                    <#else>
                        <@s.text name="manage.overview.description"/>
                    </#if>
                </p>

                <div id="dialog" class="modal fade" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true"></div>
            </div>
        </div>
    </div>

    <div id="sections" class="container-fluid bg-body">
        <div class="container my-md-4 bd-layout">

            <main class="bd-main">
                <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                    <nav id="sidebar-content">
                        <ul>
                            <#if resource.coreType?has_content && resource.coreType==metadataType>
                                <li><a href="#anchor-metadata" class="sidebar-navigation-link"><@s.text name='manage.overview.metadata'/></a></li>
                            <#else>
                                <li><a href="#anchor-sources" class="sidebar-navigation-link"><@s.text name='manage.overview.source.data'/></a></li>
                                <li><a href="#anchor-mappings" class="sidebar-navigation-link"><@s.text name='manage.overview.DwC.Mappings'/></a></li>
                                <li><a href="#anchor-metadata" class="sidebar-navigation-link"><@s.text name='manage.overview.metadata'/></a></li>
                            </#if>
                            <li><a href="#anchor-publish" class="sidebar-navigation-link"><@s.text name='manage.overview.published'/></a></li>
                            <li><a href="#anchor-autopublish" class="sidebar-navigation-link"><@s.text name='manage.overview.autopublish.title'/></a></li>
                            <li><a href="#anchor-visibility" class="sidebar-navigation-link"><@s.text name='manage.overview.visibility'/></a></li>
                            <li><a href="#anchor-registration" class="sidebar-navigation-link"><@s.text name='manage.overview.registration'/></a></li>
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

                    <span class="anchor anchor-home-resource-page" id="anchor-publish"></span>
                    <div class="py-5 border-bottom section" id="publish">
                        <div class="d-flex justify-content-between">
                            <div class="d-flex">
                                <h5 class="my-auto text-gbif-header-2 fw-400">
                                    <#assign overviewTitleInfo>
                                        <#if resource.coreType?has_content && resource.coreType==metadataType>
                                            <@s.text name="manage.overview.published.description.metadataOnly"/>
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
                            <p class="mb-0">
                                <@s.text name="manage.overview.published.intro"/>
                            </p>

                            <div class="details mt-3">
                                <#assign lastPublishedTitle><@s.text name="manage.overview.published.last.publication.intro"/></#assign>
                                <#assign nextPublishedTitle><@s.text name="manage.overview.published.next.publication.intro"/></#assign>
                                <#assign versionTitle><@s.text name="manage.overview.published.version"/></#assign>
                                <#assign releasedTitle><@s.text name="manage.overview.published.released"/></#assign>
                                <#assign pubLogTitle><@s.text name="portal.publication.log"/></#assign>
                                <#assign pubRepTitle><@s.text name="manage.overview.published.report"/></#assign>
                                <#assign downloadTitle><@s.text name='manage.overview.published.download'/></#assign>
                                <#assign showTitle><@s.text name="basic.show"/></#assign>
                                <#assign viewTitle><@s.text name='button.view'/></#assign>
                                <#assign previewTitle><@s.text name='button.preview'/></#assign>
                                <#assign emptyCell="-"/>
                                <#assign visibilityTitle><@s.text name='manage.overview.visibility'/></#assign>
                                <#assign licenseTitle><@s.text name='eml.intellectualRights.license'/></#assign>

                                <div class="row g-2">
                                    <#if resource.lastPublished??>
                                        <div class="col-xl-6" style="height: 100%">
                                            <#assign lastPublishedVersionStatus>${resource.getLastPublishedVersionsPublicationStatus()?lower_case}</#assign>

                                            <div class="d-flex justify-content-between border rounded-2 mx-1 p-1 py-2 version-item text-smaller">
                                                <div class="ps-2">
                                                    <span class="me-2 overview-version-title"><strong><@s.text name="footer.version"/> ${resource.emlVersion.toPlainString()}</strong></span><br>
                                                    <span class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill version-current">${lastPublishedTitle?cap_first}</span>
                                                    <span title="${licenseTitle?cap_first}" class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill"><@shortLicense action.getLastPublishedVersionAssignedLicense(resource)!/></span>
                                                    <#if resource.isAlreadyAssignedDoi()>
                                                        <span title="DOI" class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill doi-pill">${resource.versionHistory[0].doi!}</span>
                                                    </#if>
                                                    <span title="${visibilityTitle?cap_first}" class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill status-${lastPublishedVersionStatus}">${lastPublishedVersionStatus?cap_first}</span><br>
                                                    <small>
                                                        ${releasedTitle?cap_first} ${resource.lastPublished?datetime?string.medium}
                                                    </small>
                                                </div>

                                                <div class="d-flex justify-content-end my-auto version-item-actions">
                                                    <div class="dropdown">
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
                                                                        <a id="toggleReport" class="delete-source source-item-action dropdown-item action-link" type="button" href="#anchor-publish">
                                                                            <svg class="overview-item-action-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                                <path d="M19 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.89-2-2-2zm0 16H5V7h14v12zm-5.5-6c0 .83-.67 1.5-1.5 1.5s-1.5-.67-1.5-1.5.67-1.5 1.5-1.5 1.5.67 1.5 1.5zM12 9c-2.73 0-5.06 1.66-6 4 .94 2.34 3.27 4 6 4s5.06-1.66 6-4c-.94-2.34-3.27-4-6-4zm0 6.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"></path>
                                                                            </svg>
                                                                            ${pubRepTitle?cap_first}
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
                                        <div class="d-flex justify-content-between border rounded-2 mx-1 p-1 py-2 version-item text-smaller">
                                            <div class="ps-2">
                                                <span class="me-2 overview-version-title"><strong><@s.text name="footer.version"/> ${resource.getNextVersion().toPlainString()}</strong></span><br>
                                                <span class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill version-pending">${nextPublishedTitle?cap_first}</span>
                                                <span title="${licenseTitle?cap_first}" class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill"><@shortLicense resource.getEml().parseLicenseUrl()/></span>
                                                <#if resource.isAlreadyAssignedDoi()>
                                                    <span title="DOI" class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill doi-pill">${resource.versionHistory[0].doi!}</span>
                                                </#if>
                                                <span title="${visibilityTitle?cap_first}" class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill status-${resource.status?lower_case}">${resource.status?lower_case?cap_first}</span><br>
                                                <small>
                                                    <#if resource.nextPublished??>
                                                        ${releasedTitle?cap_first} ${resource.nextPublished?datetime?string.medium}
                                                    <#else>
                                                        <@s.text name="manage.overview.published.date.not.set"/>
                                                    </#if>
                                                </small>
                                            </div>

                                            <div class="d-flex justify-content-end my-auto version-item-actions">
                                                <div class="dropdown">
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
                                                                    <path d="M8 11h8v2H8zm12.1 1H22c0-2.76-2.24-5-5-5h-4v1.9h4c1.71 0 3.1 1.39 3.1 3.1zM3.9 12c0-1.71 1.39-3.1 3.1-3.1h4V7H7c-2.76 0-5 2.24-5 5s2.24 5 5 5h4v-1.9H7c-1.71 0-3.1-1.39-3.1-3.1zM19 12h-2v3h-3v2h3v3h2v-3h3v-2h-3z"></path>
                                                                </svg>
                                                                <#if !organisationWithPrimaryDoiAccount?? || !currentUser.hasRegistrationRights() ||  resource.identifierStatus == "UNRESERVED">
                                                                    <@s.text name="button.reserve"/> DOI
                                                                <#elseif resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION">
                                                                    <@s.text name="button.delete"/> DOI
                                                                <#elseif resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi() >
                                                                    <@s.text name="button.reserve.new"/> DOI
                                                                <#else>
                                                                    <@s.text name="button.reserve"/> DOI
                                                                </#if>
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
                                <div class="dropdown">
                                    <a class="icon-button icon-material-actions overview-action-button autopublish-action" type="button" href="#" id="dropdown-autopublish-actions" data-bs-toggle="dropdown" aria-expanded="false">
                                        <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                            <path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"></path>
                                        </svg>
                                    </a>

                                    <ul class="dropdown-menu" aria-labelledby="dropdown-autopublish-actions">
                                        <li>
                                            <a id="edit-autopublish-button" class="dropdown-item action-link" type="button" href="auto-publish.do?r=${resource.shortname}">
                                                <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                    <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                                                </svg>
                                                <@s.text name="button.edit"/>
                                            </a>
                                        </li>
                                    </ul>
                                </div>
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
                                <div class="dropdown">
                                    <a class="icon-button icon-material-actions overview-action-button visibility-action" type="button" href="#" id="dropdown-visibility-actions" data-bs-toggle="dropdown" aria-expanded="false">
                                        <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                            <path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"></path>
                                        </svg>
                                    </a>

                                    <ul class="dropdown-menu" aria-labelledby="dropdown-visibility-actions">
                                        <#if resource.status=="PRIVATE">
                                            <#assign actionMethod>makePublic</#assign>
                                            <li>
                                                <form action='resource-${actionMethod}.do' method='post'>
                                                    <input name="r" type="hidden" value="${resource.shortname}"/>
                                                    <button id="makePublic" class="dropdown-item action-link" type="submit">
                                                        <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                                            <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                                                        </svg>
                                                        <@s.text name='button.change'/>
                                                    </button>
                                                </form>
                                            </li>
                                        </#if>

                                        <#if resource.status=="PUBLIC" && (resource.identifierStatus=="PUBLIC_PENDING_PUBLICATION" || resource.identifierStatus == "UNRESERVED")>
                                            <#assign actionMethod>makePrivate</#assign>
                                            <li>
                                                <form action='resource-${actionMethod}.do' method='post'>
                                                    <input name="r" type="hidden" value="${resource.shortname}"/>
                                                    <input name="unpublish" type="hidden" value="Change"/>
                                                    <button class="confirmMakePrivate dropdown-item action-link" type="submit">
                                                        <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                                            <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                                                        </svg>
                                                        <@s.text name='button.change'/>
                                                    </button>
                                                </form>
                                            </li>
                                        </#if>
                                    </ul>
                                </div>
                            </div>
                        </div>

                        <div class="mt-4">
                            <p class="mb-0">
                                <#if resource.status=="PRIVATE">
                                    <span class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill status-private">
                                        <@s.text name="resource.status.private"/>
                                    </span>
                                    <#if resource.makePublicDate?has_content>
                                        <@s.text name="manage.resource.status.intro.private.public.scheduled">
                                            <@s.param>${resource.makePublicDate?datetime?string.long_short}</@s.param>
                                        </@s.text>
                                    <#else>
                                        <@s.text name="manage.resource.status.intro.private"/>
                                    </#if>
                                <#elseif resource.status=="PUBLIC">
                                    <span class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill status-public">
                                        <@s.text name="resource.status.public"/>
                                    </span>
                                    <@s.text name="manage.resource.status.intro.public"/>
                                <#elseif resource.status=="REGISTERED">
                                    <span class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill status-registered">
                                        <@s.text name="resource.status.registered"/>
                                    </span>
                                    <@s.text name="manage.resource.status.intro.registered"/>
                                <#elseif resource.status=="DELETED">
                                    <span class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill status-deleted">
                                        <@s.text name="resource.status.deleted"/>
                                    </span>
                                    <@s.text name="manage.resource.status.intro.deleted"/>
                                </#if>
                            </p>
                        </div>
                    </div>

                    <span class="anchor anchor-home-resource-page" id="anchor-registration"></span>
                    <div class="py-5 border-bottom section" id="registration">

                        <div class="d-flex justify-content-between">
                            <div class="d-flex">
                                <h5 class="my-auto text-gbif-header-2 fw-400">
                                    <#assign registrationTitleInfo>
                                        <@s.text name="manage.resource.status.intro.registration"/>
                                        <br><br>
                                        <@s.text name='manage.resource.status.intro.public.migration'><@s.param><a href="${baseURL}/manage/metadata-additional.do?r=${resource.shortname}&amp;edit=Edit"><@s.text name="submenu.additional"/></a></@s.param></@s.text>
                                        <br><br>
                                        <@s.text name='manage.resource.status.intro.public.gbifWarning'/>
                                    </#assign>

                                    <@popoverTextInfo registrationTitleInfo/>
                                    <@s.text name='manage.overview.registration'/>
                                </h5>
                            </div>

                            <div class="d-flex justify-content-end">
                                <div class="dropdown">
                                    <a class="icon-button icon-material-actions overview-action-button registration-action" type="button" href="#" id="dropdown-registration-actions" data-bs-toggle="dropdown" aria-expanded="false">
                                        <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                            <path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"></path>
                                        </svg>
                                    </a>

                                    <ul class="dropdown-menu" aria-labelledby="dropdown-registration-actions">
                                        <#if resource.status=="PUBLIC">
                                            <#if !currentUser.hasRegistrationRights()>
                                                <!-- Hide register button and show warning: user must have registration rights -->
                                                <#assign visibilityConfirmRegistrationWarning>
                                                    <@s.text name="manage.resource.status.registration.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/>
                                                </#assign>
                                                <button id="show-registration-disabled-modal" class="dropdown-item action-link" type="button">
                                                    <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                                        <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                                                    </svg>
                                                    <@s.text name='button.register'/>
                                                </button>
                                            <#elseif missingValidPublishingOrganisation?string == "true">
                                                <!-- Hide register button and show warning: user must assign valid publishing organisation -->
                                                <button id="show-registration-disabled-modal" class="dropdown-item action-link" type="button">
                                                    <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                                        <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                                                    </svg>
                                                    <@s.text name='button.register'/>
                                                </button>
                                            <#elseif missingRegistrationMetadata?string == "true">
                                                <!-- Hide register button and show warning: user must fill in minimum registration metadata -->
                                                <button id="show-registration-disabled-modal" class="dropdown-item action-link" type="button">
                                                    <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                                        <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                                                    </svg>
                                                    <@s.text name='button.register'/>
                                                </button>
                                            <#elseif !resource.isLastPublishedVersionPublic()>
                                                <!-- Hide register button and show warning: last published version must be publicly available to register -->
                                                <button id="show-registration-disabled-modal" class="dropdown-item action-link" type="button">
                                                    <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                                        <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                                                    </svg>
                                                    <@s.text name='button.register'/>
                                                </button>
                                            <#elseif !action.isLastPublishedVersionAssignedGBIFSupportedLicense(resource)>
                                                <!-- Hide register button and show warning: resource must be assigned a GBIF-supported license to register if resource has occurrence data -->
                                                <button id="show-registration-disabled-modal" class="dropdown-item action-link" type="button">
                                                    <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                                        <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                                                    </svg>
                                                    <@s.text name='button.register'/>
                                                </button>
                                            <#else>
                                                <li>
                                                    <form action="resource-registerResource.do" method="post">
                                                        <input name="r" type="hidden" value="${resource.shortname}"/>
                                                        <button id="register-resource-button" class="confirmRegistration dropdown-item action-link" type="submit">
                                                            <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                                                <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                                                            </svg>
                                                            <@s.text name='button.register'/>
                                                        </button>
                                                    </form>
                                                </li>
                                            </#if>
                                        </#if>
                                    </ul>
                                </div>
                            </div>
                        </div>

                        <div class="row mt-4">
                            <p class="mb-0">
                                <@s.text name="manage.overview.registration.intro"/>
                            </p>

                            <#if resource.status=="REGISTERED" && resource.key??>
                                <div class="details mt-3">
                                    <div class="row g-2">
                                        <div class="col-12">
                                            <div class="registration-item border rounded-2 mx-1 p-1 py-2 version-item text-smaller">
                                                <div class="ps-2">
                                                    <strong class="overview-registered-title">${resource.title!resource.shortname}</strong>
                                                    <br>
                                                    <small>${resource.key}</small>
                                                    <#if resource.organisation??>
                                                        |
                                                        <small>${resource.organisation.name!"Organisation"}</small>
                                                    </#if>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </#if>
                        </div>
                    </div>

                    <span class="anchor anchor-home-resource-page" id="anchor-networks"></span>
                    <div class="py-5 border-bottom section" id="networks">
                        <div class="row">
                            <div class="col-9">
                                <h5 class="mb-0 text-gbif-header-2 fw-400">
                                    <@popoverPropertyInfo "manage.overview.networks.description"/>
                                    <@s.text name="manage.overview.networks.title"/>
                                </h5>
                            </div>

                            <div class="col-3 d-flex justify-content-end">
                                <#if resource.key?has_content && (potentialNetworks?size>0)>
                                <button id="add-network-button" class="btn btn-sm overview-action-button">
                                    <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                    </svg>
                                    <@s.text name='button.add'/>
                                </button>
                                </#if>
                            </div>
                        </div>

                        <div class="mt-4">
                            <p class="mb-0">
                                <@s.text name="manage.overview.networks.intro"/>
                            </p>

                            <#if resource.key?has_content>
                                <div class="details mt-3">
                                    <#if (resourceNetworks?size>0)>
                                        <div class="row g-2">
                                            <#list resourceNetworks as n>
                                                <div class="col-xl-6">
                                                    <div class="d-flex justify-content-between border rounded-2 mx-1 p-1 py-2 network-item text-smaller">
                                                        <div class="my-auto">
                                                            <strong>${n.title!""}</strong><br>
                                                            <small>${(n.key)!}</small>
                                                        </div>

                                                        <div class="d-flex justify-content-end my-auto network-item-actions">
                                                            <div class="dropdown">
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
                                    <#else>
                                        <p class="mb-0">
                                            <@s.text name="manage.overview.networks.no.data"/>
                                        </p>
                                    </#if>
                                </div>
                            <#else>
                                <div class="callout callout-warning text-smaller">
                                    <@s.text name="manage.overview.networks.not.registered"/>
                                </div>
                            </#if>
                        </div>
                    </div>

                    <span class="anchor anchor-home-resource-page" id="anchor-managers"></span>
                    <div class="py-5" id="managers">
                        <div class="row">
                            <div class="col-9">
                                <h5 class="mb-0 text-gbif-header-2 fw-400">
                                    <@popoverPropertyInfo "manage.overview.resource.managers.description"/>
                                    <@s.text name="manage.overview.resource.managers"/>
                                </h5>
                            </div>

                            <div class="col-3 d-flex justify-content-end">
                                <#if (potentialManagers?size>0)>
                                <button id="add-manager-button" class="btn btn-sm overview-action-button">
                                    <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                    </svg>
                                    <@s.text name='button.add'/>
                                </button>
                                </#if>
                            </div>
                        </div>

                        <div class="mt-4">
                            <p class="mb-0">
                                <@s.text name="manage.overview.resource.managers.intro"><@s.param>${resource.shortname}</@s.param></@s.text>
                            </p>

                            <div class="details mt-3">
                                <div class="row g-2">
                                    <div class="col-xl-6">
                                        <div class="d-flex justify-content-between border rounded-2 mx-1 p-1 py-2 manager-item text-smaller">
                                            <div class="my-auto ps-2">
                                                <strong>${resource.creator.name!}</strong><br>
                                                <small><@s.text name="manage.overview.resource.managers.creator"/>
                                                    | ${resource.creator.email}</small>
                                            </div>
                                            <div class="d-flex justify-content-end my-auto manager-item-actions"></div>
                                        </div>
                                    </div>

                                    <#if (resource.managers?size>0)>
                                        <#list resource.managers as u>
                                            <div class="col-xl-6">
                                                <div class="d-flex justify-content-between border rounded-2 mx-1 p-1 py-2 manager-item text-smaller">
                                                    <div class="my-auto ps-2">
                                                        <strong>${u.name}</strong><br>
                                                        <small><@s.text name="manage.overview.resource.managers.manager"/>
                                                            | ${u.email}</small>
                                                    </div>
                                                    <div class="d-flex justify-content-end my-auto manager-item-actions">
                                                        <div class="dropdown">
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

                    <div class="d-flex justify-content-center mt-3">
                        <div>
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

    <div id="source-data-modal" class="modal fade" tabindex="-1" aria-labelledby="source-data-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <h5 class="modal-title w-100" id="source-data-modal-title"><@s.text name="manage.source.title"/></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">×</button>
                </div>
                <div class="modal-body">
                    <div>
                        <form action='addsource.do' method='post' enctype="multipart/form-data">
                            <input name="r" type="hidden" value="${resource.shortname}"/>
                            <input name="validate" type="hidden" value="false"/>

                            <div class="row">
                                <div class="col-12">
                                    <select id="sourceType" name="sourceType" class="form-select my-1">
                                        <option value="" disabled selected><@s.text name='manage.source.select.type'/></option>
                                        <option value="source-sql"><@s.text name='manage.source.database'/></option>
                                        <option value="source-file"><@s.text name='manage.source.file'/></option>
                                        <option value="source-url"><@s.text name='manage.source.url'/></option>
                                    </select>
                                </div>
                                <div class="col-12">
                                    <@s.file name="file" cssClass="form-control my-1" cssStyle="display: none;" key="manage.resource.create.file"/>
                                    <ul id="field-error-file" class="invalid-feedback list-unstyled field-error my-1">
                                        <li>
                                            <span><@s.text name="manage.overview.source.file.too.big"/></span>
                                        </li>
                                    </ul>
                                    <input type="text" id="sourceName" name="sourceName" class="form-control my-1" placeholder="<@s.text name='source.name'/>" style="display: none">
                                    <input type="url" id="url" name="url" class="form-control my-1" placeholder="URL" style="display: none">
                                </div>
                                <div class="col-12 mt-3">
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
                            <div class="callout callout-warning text-smaller">
                                <@s.text name="manage.overview.DwC.Mappings.cantdo"/>
                            </div>
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
                    <!-- resources cannot be published if the mandatory metadata is missing -->
                    <#if missingMetadata>
                        <div class="callout callout-warning text-smaller">
                            <@s.text name="manage.overview.published.missing.metadata"/>
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

                    <form action='publish.do' method='post'>
                        <input name="r" type="hidden" value="${resource.shortname}"/>
                        <input name="publish" type="hidden" value="Publish"/>

                        <textarea id="summary" name="summary" cols="40" rows="5" style="display: none"></textarea>

                        <#if missingMetadata>
                            <!-- resources cannot be published if the mandatory metadata is missing -->
                        <#elseif resource.isRegistered() && !resource.isAssignedGBIFSupportedLicense()>
                            <!-- resources that are already registered cannot be re-published if they haven't been assigned a GBIF-supported license -->

                            <!-- previously published resources without a DOI, or that haven't been registered yet can be republished whenever by any manager -->
                        <#elseif resource.lastPublished?? && resource.identifierStatus == "UNRESERVED" && resource.status != "REGISTERED">

                            <button class="confirmPublishMinorVersion btn btn-sm overview-action-button" id="publishButton" name="publish" type="submit">
                                <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                    <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                                </svg>
                                <@s.text name="button.publish"/>
                            </button>

                            <!-- resources with a reserved DOI, existing registered DOI, or registered with GBIF can only be republished by managers with registration rights -->
                        <#elseif (resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION")
                        || (resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi())
                        || resource.status == "REGISTERED">

                            <#if !currentUser.hasRegistrationRights()>
                                <!-- the user must have registration rights -->

                                <!-- an organisation with DOI account be activated (if resource has a reserved DOI or existing registered DOI) -->
                            <#elseif ((resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION" && resource.isAlreadyAssignedDoi())
                            || (resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi()))
                            && !organisationWithPrimaryDoiAccount??>

                                <!-- when a DOI is reserved -->
                            <#elseif resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION">
                                <!-- and the resource has no existing DOI and its status is private..  -->
                                <#if !resource.isAlreadyAssignedDoi() && resource.status == "PRIVATE">
                                    <!-- and the resource has never been published before, the first publication is a new major version -->
                                    <#if !resource.lastPublished??>
                                        <button class="confirmPublishMajorVersionWithoutDOI btn btn-sm overview-action-button" id="publishButton" name="publish" type="submit">
                                            <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                                <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                                            </svg>
                                            <@s.text name="button.publish"/>
                                        </button>
                                        <!-- and the resource has been published before, the next publication is a new minor version -->
                                    <#else>
                                        <button class="confirmPublishMinorVersion btn btn-sm overview-action-button" id="publishButton" name="publish" type="submit">
                                            <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                                <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                                            </svg>
                                            <@s.text name="button.publish"/>
                                        </button>
                                    </#if>

                                    <!-- and its status is public (or registered), its reserved DOI can be registered during next publication  -->
                                <#elseif resource.status == "PUBLIC" || resource.status == "REGISTERED">
                                    <@s.submit cssClass="confirmPublishMajorVersion btn btn-sm overview-action-button" id="publishButton" name="publish" key="button.publish"/>
                                </#if>

                                <!-- publishing a new minor version -->
                            <#elseif resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi()>
                                <button class="confirmPublishMinorVersion btn btn-sm overview-action-button" id="publishButton" name="publish" type="submit">
                                    <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                        <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                                    </svg>
                                    <@s.text name="button.publish"/>
                                </button>

                                <!-- publishing a new version registered with GBIF -->
                            <#elseif resource.status == "REGISTERED">
                                <button class="confirmPublishMinorVersion btn btn-sm overview-action-button" id="publishButton" name="publish" type="submit">
                                    <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                        <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                                    </svg>
                                    <@s.text name="button.publish"/>
                                </button>
                            </#if>

                            <!-- first time any resource not assigned a DOI is published is always new major version -->
                        <#elseif !resource.lastPublished?? && resource.identifierStatus == "UNRESERVED">
                            <button class="confirmPublishMajorVersionWithoutDOI btn btn-sm overview-action-button" id="publishButton" name="publish" type="submit">
                                <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                    <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                                </svg>
                                <@s.text name="button.publish"/>
                            </button>
                        <#else>
                            <!-- otherwise prevent publication from happening just to be safe -->
                        </#if>
                    </form>
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
                    <#if resource.key?has_content && (potentialNetworks?size>0)>
                        <div>
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

                    <#if resource.status=="PRIVATE">
                        <!-- Show warning: resource must be public -->
                        <div class="callout callout-warning text-smaller">
                            <@s.text name="manage.overview.registration.private" />
                        </div>
                    </#if>

                    <#if resource.status=="PUBLIC">
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
                        <div class="callout callout-primary text-smaller">
                            <@s.text name="manage.overview.publishing.doi.reserve.help" escapeHtml=true/>
                        </div>
                    <#elseif resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION">
                        <div class="callout callout-danger text-smaller">
                            <@s.text name="manage.overview.publishing.doi.delete.help" escapeHtml=true/>
                        </div>
                        <@s.text name="manage.overview.publishing.doi.delete.help" escapeHtml=true/>
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

</#escape>
<#include "/WEB-INF/pages/inc/footer.ftl">
