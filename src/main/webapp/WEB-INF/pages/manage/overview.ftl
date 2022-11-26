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
    <#if !organisationWithPrimaryDoiAccount??>
        -
    <#elseif !currentUser.hasRegistrationRights()>
        -
    <#elseif resource.identifierStatus == "UNRESERVED">
        <form action='resource-reserveDoi.do' method='post'>
            <input name="r" type="hidden" value="${resource.shortname}"/>
            <@s.submit cssClass="confirmReserveDoi button-link text-gbif-primary" name="reserveDoi" key="button.reserve" disabled="${missingMetadata?string}"/>
        </form>
    <#elseif resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION">
        <form action='resource-deleteDoi.do' method='post'>
            <input name="r" type="hidden" value="${resource.shortname}"/>
            <@s.submit cssClass="confirmDeleteDoi button-link text-gbif-danger" name="deleteDoi" key="button.delete" disabled="${missingMetadata?string}"/>
        </form>
    <#elseif resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi() >
        <form action='resource-reserveDoi.do' method='post'>
            <input name="r" type="hidden" value="${resource.shortname}"/>
            <@s.submit cssClass="confirmReserveDoi button-link text-gbif-primary" name="reserveDoi" key="button.reserve.new" disabled="${missingMetadata?string}"/>
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
            openMappingDetails(e);
        });

        function openMappingDetails(e) {
            var resource = e.currentTarget.attributes["data-ipt-resource"].nodeValue;
            var extension = e.currentTarget.attributes["data-ipt-extension"].nodeValue;
            var mapping = e.currentTarget.attributes["data-ipt-mapping"].nodeValue;
            location.href = 'mapping.do?r=' + resource + '&id=' + extension + '&mid=' + mapping;
        }

        $('.confirm').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", buttonType: "primary"});
        $('.confirmRegistration').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='manage.overview.visibility.confirm.registration'/> <@s.text name='manage.resource.delete.confirm.registered'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", checkboxText: "<@s.text name='manage.overview.visibility.confirm.agreement'/>", buttonType: "primary"});
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

        $("#edit-metadata-radio").change(function () {
            if($('#edit-metadata-radio').is(':checked')) {
                $('#upload-metadata-form').hide();
                $('#edit-metadata-form').show();
            }
        });

        $("#upload-metadata-radio").change(function () {
            if($('#upload-metadata-radio').is(':checked')) {
                $('#edit-metadata-form').hide();
                $('#upload-metadata-form').show();
            }
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
            if (usedFileName !== "") {
                var addButton = $('#add');
                addButton.attr("value", '<@s.text name="button.add"/>');
                addButton.show();
            }
        });

        $("#clear").click(function(event) {
            event.preventDefault();
            $("#file").prop("value", "");
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
            $("#modalcontent").load(addressValue);
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
                        <h5 class="pb-2 text-gbif-header-2 fw-400">
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

                        <div class="row mt-4">
                            <div class="col-lg-9 order-lg-last ps-lg-5">
                                <p>
                                    <@s.text name="manage.overview.published.intro"/>
                                </p>

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

                                <div class="details">
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

                                    <div class="row g-3">
                                        <#if resource.lastPublished??>
                                            <div class="col-xl-6" style="height: 100%">
                                                <div class="card">
                                                    <div class="card-header d-flex justify-content-between">
                                                        <div class="my-auto text-smaller">
                                                            <strong>v${resource.emlVersion.toPlainString()}</strong>
                                                            (${lastPublishedTitle})
                                                        </div>
                                                        <div>
                                                            <a class="icon-button icon-button-sm" type="button"
                                                               href="${baseURL}/resource?r=${resource.shortname}">
                                                                <svg class="icon-button-svg icon-material-eye mb-1"
                                                                     focusable="false" aria-hidden="true"
                                                                     viewBox="0 0 24 24">
                                                                    <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                                    <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                                </svg>
                                                            </a>
                                                        </div>
                                                    </div>
                                                    <div class="card-body">
                                                        <div class="table-responsive text-smaller">
                                                            <table class="table table-sm table-borderless mb-0">
                                                                <tbody>
                                                                <tr>
                                                                    <td class="col-6"><strong>${visibilityTitle?cap_first}</strong>
                                                                    </td>
                                                                    <td class="text-end">${resource.getLastPublishedVersionsPublicationStatus()?lower_case?cap_first}</td>
                                                                </tr>
                                                                <tr>
                                                                    <td class="col-6"><strong>${licenseTitle?cap_first}</strong></td>
                                                                    <td class="text-end">
                                                                        <@shortLicense action.getLastPublishedVersionAssignedLicense(resource)!/>
                                                                    </td>
                                                                </tr>
                                                                <tr>
                                                                    <td class="col-6"><strong>DOI</strong></td>
                                                                    <td class="text-end">
                                                                        <#if resource.isAlreadyAssignedDoi()>
                                                                            ${resource.versionHistory[0].doi!}
                                                                        <#else>
                                                                            ${emptyCell}
                                                                        </#if>
                                                                    </td>
                                                                </tr>
                                                                <tr>
                                                                    <td class="col-6"><strong>${releasedTitle?cap_first}</strong></td>
                                                                    <td class="text-end">
                                                                        ${resource.lastPublished?datetime?string["dd.MM.yyyy HH:mm"]}
                                                                    </td>
                                                                </tr>
                                                                <tr>
                                                                    <td class="col-6"><strong>${pubLogTitle?cap_first}</strong></td>
                                                                    <td class="text-end">
                                                                        <a target="_blank"
                                                                           href="${baseURL}/publicationlog.do?r=${resource.shortname}">
                                                                            ${downloadTitle?cap_first}
                                                                        </a>
                                                                    </td>
                                                                </tr>
                                                                <tr>
                                                                    <td class="col-6"><strong>${pubRepTitle?cap_first}</strong></td>
                                                                    <td class="text-end">
                                                                        <#if report??>
                                                                            <a id="toggleReport" href="#anchor-publish">${showTitle?cap_first}</a>
                                                                        <#else>
                                                                            ${emptyCell}
                                                                        </#if>
                                                                    </td>
                                                                </tr>
                                                                </tbody>
                                                            </table>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </#if>

                                        <div class="col-xl-6" style="height: 100%">
                                            <div class="card">
                                                <div class="card-header d-flex justify-content-between">
                                                    <div class="my-auto text-smaller">
                                                        <span>
                                                            <strong>v${resource.getNextVersion().toPlainString()}</strong> (${nextPublishedTitle})
                                                        </span>
                                                    </div>

                                                    <#if !missingMetadata>
                                                        <div>
                                                            <a class="ms-auto icon-button icon-button-sm" type="button"
                                                               href="${baseURL}/resource/preview?r=${resource.shortname}">
                                                                <svg class="icon-button-svg icon-material-eye mb-1"
                                                                     focusable="false" aria-hidden="true"
                                                                     viewBox="0 0 24 24">
                                                                    <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                                </svg>
                                                            </a>
                                                        </div>
                                                    </#if>
                                                </div>
                                                <div class="card-body">
                                                    <div class="table-responsive text-smaller">
                                                        <table class="table table-sm table-borderless mb-0">
                                                            <tbody>
                                                            <tr>
                                                                <td class="col-6"><strong>${visibilityTitle?cap_first}</strong></td>
                                                                <td class="text-end">${resource.status?lower_case?cap_first}</td>
                                                            </tr>
                                                            <tr>
                                                                <td class="col-6"><strong>${licenseTitle?cap_first}</strong></td>
                                                                <td class="text-end">
                                                                    <@shortLicense resource.getEml().parseLicenseUrl()/>
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td class="col-6">
                                                                    <strong>DOI</strong>
                                                                    <#if !organisationWithPrimaryDoiAccount??>
                                                                        <a tabindex="0" role="button"
                                                                           class="popover-link"
                                                                           data-bs-toggle="popover"
                                                                           data-bs-trigger="focus"
                                                                           data-bs-html="true"
                                                                           data-bs-content="<@s.text name="manage.overview.publishing.doi.reserve.prevented.noOrganisation" escapeHtml=true/>">
                                                                            <i class="bi bi-info-circle text-warning"></i>
                                                                        </a>
                                                                    <#elseif !currentUser.hasRegistrationRights()>
                                                                        <a tabindex="0" role="button"
                                                                           class="popover-link"
                                                                           data-bs-toggle="popover"
                                                                           data-bs-trigger="focus"
                                                                           data-bs-html="true"
                                                                           data-bs-content="<@s.text name="manage.resource.status.doi.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/>">
                                                                            <i class="bi bi-info-circle text-warning"></i>
                                                                        </a>
                                                                    <#elseif resource.identifierStatus == "UNRESERVED">
                                                                        <a tabindex="0" role="button"
                                                                           class="popover-link"
                                                                           data-bs-trigger="focus"
                                                                           data-bs-toggle="popover"
                                                                           data-bs-placement="top"
                                                                           data-bs-html="true"
                                                                           data-bs-content="<@s.text name="manage.overview.publishing.doi.reserve.help" escapeHtml=true/>">
                                                                            <i class="bi bi-info-circle text-gbif-primary"></i>
                                                                        </a>
                                                                    <#elseif resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION">
                                                                        <a tabindex="0" role="button"
                                                                           class="popover-link"
                                                                           data-bs-trigger="focus"
                                                                           data-bs-toggle="popover"
                                                                           data-bs-placement="top"
                                                                           data-bs-html="true"
                                                                           data-bs-content="<@s.text name="manage.overview.publishing.doi.delete.help" escapeHtml=true/>">
                                                                            <i class="bi bi-info-circle text-gbif-danger"></i>
                                                                        </a>
                                                                    <#elseif resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi() >
                                                                        <a tabindex="0" role="button"
                                                                           class="popover-link"
                                                                           data-bs-trigger="focus"
                                                                           data-bs-toggle="popover"
                                                                           data-bs-placement="top"
                                                                           data-bs-html="true"
                                                                           data-bs-content="<@s.text name="manage.overview.publishing.doi.reserve.new.help" escapeHtml=true/>">
                                                                            <i class="bi bi-info-circle text-gbif-primary"></i>
                                                                        </a>
                                                                    </#if>
                                                                </td>
                                                                <td class="text-end">
                                                                    <#if (resource.isAlreadyAssignedDoi() && resource.versionHistory[0].doi != resource.doi!"") || (!resource.isAlreadyAssignedDoi() && resource.doi?has_content)>
                                                                        <em>${resource.doi!emptyCell}</em>&nbsp;
                                                                    </#if>
                                                                    <@nextDoiButtonTD/>
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td class="col-6"><strong>${releasedTitle?cap_first}</strong></td>
                                                                <td class="text-end">
                                                                    <#if resource.nextPublished??>
                                                                        ${resource.nextPublished?datetime?string["dd.MM.yyyy HH:mm"]}
                                                                    <#else>
                                                                        ${emptyCell}
                                                                    </#if>
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td class="col-6"><strong>${pubLogTitle?cap_first}</strong></td>
                                                                <td class="text-end">${emptyCell}</td>
                                                            </tr>
                                                            <tr>
                                                                <td class="col-6"><strong>${pubRepTitle?cap_first}</strong></td>
                                                                <td class="text-end">${emptyCell}</td>
                                                            </tr>
                                                            </tbody>
                                                        </table>
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

                            <div class="col-lg-3 border-lg-right pe-lg-5">
                                <div class="mt-2">
                                    <@publish resource/>
                                </div>
                            </div>
                        </div>
                    </div>

                    <span class="anchor anchor-home-resource-page" id="anchor-autopublish"></span>
                    <div class="py-5 border-bottom section" id="autopublish">
                        <h5 class="pb-2 text-gbif-header-2 fw-400">
                            <@popoverPropertyInfo "manage.overview.autopublish.description"/>
                            <@s.text name="manage.overview.autopublish.title"/>
                        </h5>

                        <div class="row mt-4">
                            <div class="col-lg-9 order-lg-last ps-lg-5">
                                <div>
                                    <p>
                                        <#if resource.usesAutoPublishing()>
                                            <@s.text name="manage.overview.autopublish.intro.activated"/>
                                        <#else>
                                            <@s.text name="manage.overview.autopublish.intro.deactivated"/>
                                        </#if>
                                    </p>

                                    <#if resource.isDeprecatedAutoPublishingConfiguration()>
                                        <div class="callout callout-warning text-smaller">
                                            <@s.text name="manage.overview.autopublish.deprecated.warning.button" escapeHtml=true/>
                                        </div>
                                    </#if>

                                    <#if resource.usesAutoPublishing()>
                                        <div class="details table-responsive mt-3">
                                            <table class="table table-sm table-borderless text-smaller">
                                                <tr>
                                                    <th class="col-4"><@s.text name='manage.overview.autopublish.publication.frequency'/></th>
                                                    <td><@s.text name="${autoPublishFrequencies.get(resource.updateFrequency.identifier)}"/></td>
                                                </tr>
                                                <tr>
                                                    <th><@s.text name='manage.overview.autopublish.publication.next.date'/></th>
                                                    <td>${resource.nextPublished?datetime?string.long_short}</td>
                                                </tr>
                                            </table>
                                        </div>
                                    </#if>
                                </div>
                            </div>

                            <div class="col-lg-3 border-lg-right pe-lg-5">
                                <div>
                                    <form action='auto-publish.do' method='get'>
                                        <input name="r" type="hidden" value="${resource.shortname}"/>
                                        <@s.submit name="edit" cssClass="btn btn-sm btn-outline-gbif-primary" key="button.edit"/>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>

                    <span class="anchor anchor-home-resource-page" id="anchor-visibility"></span>
                    <div class="py-5 border-bottom section" id="visibility">
                        <h5 class="pb-2 text-gbif-header-2 fw-400">
                            <#assign visibilityTitleInfo>
                                <@s.text name='manage.overview.visibility.description'/>
                            </#assign>

                            <@popoverTextInfo visibilityTitleInfo/>
                            <@s.text name='manage.overview.visibility'/>
                        </h5>

                        <div class="row mt-4">
                            <div class="col-lg-9 order-lg-last ps-lg-5">
                                <div>
                                    <div class="bodyOverview">
                                        <p>
                                            <#if resource.status=="PRIVATE">
                                                <span class="badge rounded-pill bg-gbif-danger">
                                                    <@s.text name="resource.status.${resource.status?lower_case}"/>
                                                </span>
                                            <#else>
                                                <span class="badge rounded-pill bg-gbif-primary">
                                                    <@s.text name="resource.status.${resource.status?lower_case}"/>
                                                </span>
                                            </#if>
                                            <@s.text name="manage.resource.status.intro.${resource.status?lower_case}"/>
                                        </p>
                                    </div>
                                </div>
                            </div>

                            <div class="col-lg-3 border-lg-right pe-lg-5">
                                <div class="d-flex flex-wrap">
                                    <#if resource.status=="PRIVATE">
                                        <#assign actionMethod>makePublic</#assign>
                                        <form class="me-1 pb-1" action='resource-${actionMethod}.do' method='post'>
                                            <input name="r" type="hidden" value="${resource.shortname}"/>
                                            <@s.submit name="makePublic" cssClass="btn btn-sm btn-outline-gbif-primary" key="button.public"/>
                                        </form>
                                    </#if>

                                    <#if resource.status=="PUBLIC" && (resource.identifierStatus=="PUBLIC_PENDING_PUBLICATION" || resource.identifierStatus == "UNRESERVED")>
                                        <#assign actionMethod>makePrivate</#assign>
                                        <form action='resource-${actionMethod}.do' method='post'>
                                            <input name="r" type="hidden" value="${resource.shortname}"/>
                                            <@s.submit cssClass="confirm btn btn-sm btn-outline-gbif-primary" name="unpublish" key="button.private" />
                                        </form>
                                    </#if>
                                </div>
                            </div>
                        </div>
                    </div>

                    <span class="anchor anchor-home-resource-page" id="anchor-registration"></span>
                    <div class="py-5 border-bottom section" id="registration">
                        <h5 class="pb-2 text-gbif-header-2 fw-400">
                            <#assign registrationTitleInfo>
                                Register resource with GBIF to make it globally discoverable
                                <br><br>
                                <@s.text name='manage.resource.status.intro.public.migration'><@s.param><a href="${baseURL}/manage/metadata-additional.do?r=${resource.shortname}&amp;edit=Edit"><@s.text name="submenu.additional"/></a></@s.param></@s.text>
                                <br><br>
                                <@s.text name='manage.resource.status.intro.public.gbifWarning'/>
                            </#assign>

                            <@popoverTextInfo registrationTitleInfo/>
                            <@s.text name='manage.overview.registration'/>
                        </h5>

                        <div class="row mt-4">
                            <div class="col-lg-9 order-lg-last ps-lg-5">
                                <div>
                                    <div class="bodyOverview">
                                        <p>
                                            <@s.text name="manage.overview.registration.intro"/>
                                        </p>

                                        <#if cfg.devMode() && cfg.getRegistryType()!='PRODUCTION'>
                                            <div class="callout callout-warning text-smaller">
                                                <@s.text name="manage.overview.published.testmode.warning"/>
                                            </div>
                                        </#if>

                                        <#if resource.status=="REGISTERED" && resource.key??>
                                            <div class="details table-responsive">
                                                <table class="table table-sm table-borderless text-smaller">
                                                    <tr>
                                                        <th class="col-4">GBIF UUID</th>
                                                        <td><a href="${cfg.portalUrl}/dataset/${resource.key}" target="_blank">${resource.key}</a>
                                                        </td>
                                                    </tr>
                                                    <#if resource.organisation??>
                                                    <#-- in prod mode link goes to /publisher (GBIF Portal), in dev mode link goes to /publisher (GBIF UAT Portal) -->
                                                        <tr>
                                                            <th><@s.text name="manage.overview.visibility.organisation"/></th>
                                                            <td><a href="${cfg.portalUrl}/publisher/${resource.organisation.key}" target="_blank">${resource.organisation.name!"Organisation"}</a></td>
                                                        </tr>
                                                        <tr>
                                                            <th><@s.text name="manage.overview.visibility.organisation.contact"/></th>
                                                            <td>
                                                                <#-- Check if name or email missing -->
                                                                <#if resource.organisation.primaryContactName?? && resource.organisation.primaryContactEmail??>
                                                                    ${resource.organisation.primaryContactName!}, ${resource.organisation.primaryContactEmail!}
                                                                <#elseif resource.organisation.primaryContactName??>
                                                                    ${resource.organisation.primaryContactName!}
                                                                <#elseif resource.organisation.primaryContactEmail??>
                                                                    ${resource.organisation.primaryContactEmail!}
                                                                <#else>
                                                                    -
                                                                </#if>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <th><@s.text name="manage.overview.visibility.endorsing.node"/></th>
                                                            <td><a href="${cfg.portalUrl}/node/${resource.organisation.nodeKey!"#"}" target="_blank">${resource.organisation.nodeName!}</a></td>
                                                        </tr>
                                                    </#if>
                                                </table>
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

                            <div class="col-lg-3 border-lg-right pe-lg-5">
                                <#assign actionMethod>registerResource</#assign>
                                <#if resource.status=="PRIVATE">
                                    <#assign actionMethod>makePublic</#assign>
                                </#if>

                                <div class="d-flex flex-wrap">
                                    <form class="me-1 pb-1" action='resource-${actionMethod}.do' method='post'>
                                        <input name="r" type="hidden" value="${resource.shortname}"/>
                                        <#if resource.status=="PUBLIC">
                                            <#if !currentUser.hasRegistrationRights()>
                                                <!-- Disable register button and show warning: user must have registration rights -->
                                                <#assign visibilityConfirmRegistrationWarning>
                                                    <@s.text name="manage.resource.status.registration.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/>
                                                </#assign>

                                                <button class="btn btn-sm btn-outline-gbif-primary" name="register" disabled><@s.text name="button.register"/></button>
                                            <#elseif missingValidPublishingOrganisation?string == "true">
                                                <!-- Disable register button and show warning: user must assign valid publishing organisation -->
                                                <button class="btn btn-sm btn-outline-gbif-primary" name="register" disabled><@s.text name="button.register"/></button>
                                            <#elseif missingRegistrationMetadata?string == "true">
                                                <!-- Disable register button and show warning: user must fill in minimum registration metadata -->
                                                <button class="btn btn-sm btn-outline-gbif-primary" name="register" disabled><@s.text name="button.register"/></button>
                                            <#elseif !resource.isLastPublishedVersionPublic()>
                                                <!-- Disable register button and show warning: last published version must be publicly available to register -->
                                                <button class="btn btn-sm btn-outline-gbif-primary" name="register" disabled><@s.text name="button.register"/></button>
                                            <#elseif !action.isLastPublishedVersionAssignedGBIFSupportedLicense(resource)>
                                                <!-- Disable register button and show warning: resource must be assigned a GBIF-supported license to register if resource has occurrence data -->
                                                <button class="btn btn-sm btn-outline-gbif-primary" name="register" disabled><@s.text name="button.register"/></button>
                                            <#else>
                                                <@s.submit cssClass="confirmRegistration btn btn-sm btn-outline-gbif-primary" name="register" key="button.register"/>
                                            </#if>
                                        </#if>
                                    </form>

                                </div>
                            </div>
                        </div>
                    </div>

                    <span class="anchor anchor-home-resource-page" id="anchor-networks"></span>
                    <div class="py-5 border-bottom section" id="networks">
                        <h5 class="pb-2 text-gbif-header-2 fw-400">
                            <@popoverPropertyInfo "manage.overview.networks.description"/>
                            <@s.text name="manage.overview.networks.title"/>
                        </h5>

                        <div class="row mt-4">
                            <div class="col-lg-3 border-lg-right <#if resource.key?has_content && (potentialNetworks?size>0)> border-lg-max py-lg-max-2 mb-4</#if> pe-lg-5 rounded">
                                <#if resource.key?has_content && (potentialNetworks?size>0)>
                                    <div>
                                        <form action='resource-addNetwork.do' method='post'>
                                            <input name="r" type="hidden" value="${resource.shortname}"/>
                                            <select name="id" class="form-select form-select-sm my-1" id="network" size="1">
                                                <option value="" disabled selected><@s.text name='manage.overview.networks.select'/></option>
                                                <#list potentialNetworks?sort_by("name") as n>
                                                    <option value="${n.key}">${n.name}</option>
                                                </#list>
                                            </select>
                                            <@s.submit id="add-network" name="add" cssClass="btn btn-sm btn-outline-gbif-primary my-1" key="button.add" cssStyle="display: none"/>
                                        </form>
                                    </div>
                                </#if>
                            </div>

                            <div class="col-lg-9 ps-lg-5">
                                <div>
                                    <p>
                                        <@s.text name="manage.overview.networks.intro"/>
                                    </p>

                                    <#if resource.key?has_content>
                                        <#if (resourceNetworks?size>0)>
                                            <#list resourceNetworks as n>
                                                <div class="row border rounded-2 mx-1 my-2 p-1 py-2 network-item text-smaller">
                                                    <div class="col-10 my-auto">
                                                        <strong>${n.title!""}</strong><br>
                                                        <small>${(n.key)!}</small>
                                                    </div>
                                                    <div class="col-2 my-auto d-flex justify-content-end py-0">
                                                        <a class="icon-button icon-button-sm icon-material-eye"
                                                           type="button" target="_blank"
                                                           href="https://www.gbif-uat.org/network/${n.key!}">
                                                            <svg class="icon-button-svg" focusable="false"
                                                                 aria-hidden="true" viewBox="0 0 24 24">
                                                                <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                            </svg>
                                                        </a>
                                                        <a class="icon-button icon-button-sm icon-material-delete"
                                                           type="button"
                                                           href="resource-deleteNetwork.do?r=${resource.shortname}&id=${n.key!}">
                                                            <svg class="icon-button-svg" focusable="false"
                                                                 aria-hidden="true" viewBox="0 0 24 24">
                                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                            </svg>
                                                        </a>
                                                    </div>
                                                </div>
                                            </#list>
                                        <#else>
                                            <p>
                                                <@s.text name="manage.overview.networks.no.data"/>
                                            </p>
                                        </#if>
                                    <#else>
                                        <div class="callout callout-warning text-smaller">
                                            <@s.text name="manage.overview.networks.not.registered"/>
                                        </div>
                                    </#if>
                                </div>
                            </div>
                        </div>
                    </div>

                    <span class="anchor anchor-home-resource-page" id="anchor-managers"></span>
                    <div class="py-5" id="managers">
                        <h5 class="pb-2 text-gbif-header-2 fw-400">
                            <@popoverPropertyInfo "manage.overview.resource.managers.description"/>
                            <@s.text name="manage.overview.resource.managers"/>
                        </h5>

                        <div class="row mt-4">
                            <div class="col-lg-3 border-lg-right <#if (potentialManagers?size>0)>border-lg-max py-lg-max-2 mb-4</#if> pe-lg-5 rounded">
                                <#if (potentialManagers?size>0)>
                                    <div>
                                        <!-- Warning: method name match is case sensitive therefore must be addManager -->
                                        <form action='resource-addManager.do' method='post'>
                                            <input name="r" type="hidden" value="${resource.shortname}"/>
                                            <select name="id" class="form-select form-select-sm my-1" id="manager" size="1">
                                                <option value="" disabled selected><@s.text name='manage.overview.resource.managers.select'/></option>
                                                <#list potentialManagers?sort_by("name") as u>
                                                    <option value="${u.email}">${u.name}</option>
                                                </#list>
                                            </select>
                                            <@s.submit id="add-manager" name="add" cssClass="btn btn-sm btn-outline-gbif-primary my-1" key="button.add" cssStyle="display: none"/>
                                        </form>
                                    </div>
                                </#if>
                            </div>

                            <div class="col-lg-9 ps-lg-5">
                                <div>
                                    <p>
                                        <@s.text name="manage.overview.resource.managers.intro"><@s.param>${resource.shortname}</@s.param></@s.text>
                                    </p>

                                    <div class="row border rounded-2 mx-1 my-2 p-1 py-2 manager-item text-smaller">
                                        <div class="col-10 my-auto">
                                            <strong>${resource.creator.name!}</strong><br>
                                            <small><@s.text name="manage.overview.resource.managers.creator"/>
                                                | ${resource.creator.email}</small>
                                        </div>
                                        <div class="col-2 my-auto"></div>
                                    </div>
                                    <#if (resource.managers?size>0)>
                                        <#list resource.managers as u>
                                            <div class="row border rounded-2 mx-1 my-2 p-1 py-2 manager-item text-smaller">
                                                <div class="col-10 my-auto">
                                                    <strong>${u.name}</strong><br>
                                                    <small><@s.text name="manage.overview.resource.managers.manager"/>
                                                        | ${u.email}</small>
                                                </div>
                                                <div class="col-2 my-auto d-flex justify-content-end p-0">
                                                    <a class="icon-button icon-button-sm icon-material-delete"
                                                       type="button"
                                                       href="resource-deleteManager.do?r=${resource.shortname}&id=${u.email}">
                                                        <svg class="icon-button-svg" focusable="false"
                                                             aria-hidden="true" viewBox="0 0 24 24">
                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                        </svg>
                                                    </a>
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

</#escape>
<#include "/WEB-INF/pages/inc/footer.ftl">
