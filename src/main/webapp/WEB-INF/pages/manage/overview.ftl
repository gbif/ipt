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

        <a tabindex="0" role="button"
           class="popover-link"
           data-bs-toggle="popover"
           data-bs-trigger="focus"
           data-bs-html="true"
           data-bs-content="<@s.text name="manage.overview.publishing.doi.reserve.prevented.noOrganisation" escapeHtml=true/>">
            <i class="bi bi-exclamation-triangle-fill text-warning"></i>
        </a>

    <#elseif !currentUser.hasRegistrationRights()>

        <a tabindex="0" role="button"
           class="popover-link"
           data-bs-toggle="popover"
           data-bs-trigger="focus"
           data-bs-html="true"
           data-bs-content="<@s.text name="manage.resource.status.doi.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/>">
            <i class="bi bi-exclamation-triangle-fill text-warning"></i>
        </a>

    <#elseif resource.identifierStatus == "UNRESERVED">
        <form action='resource-reserveDoi.do' method='post'>
            <input name="r" type="hidden" value="${resource.shortname}"/>

            <div class="btn-group btn-group-sm" role="group">
                <button type="button" class="btn btn-outline-gbif-primary" data-bs-trigger="focus" data-bs-toggle="popover" data-bs-placement="top" data-bs-html="true" data-bs-content="<@s.text name="manage.overview.publishing.doi.reserve.help" escapeHtml=true/>">
                    <i class="bi bi-info-circle"></i>
                </button>
                <@s.submit cssClass="confirmReserveDoi btn btn-sm btn-outline-gbif-primary" name="reserveDoi" key="button.reserve" disabled="${missingMetadata?string}"/>
            </div>
        </form>

    <#elseif resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION">

        <form action='resource-deleteDoi.do' method='post'>
            <input name="r" type="hidden" value="${resource.shortname}"/>

            <div class="btn-group btn-group-sm" role="group">
                <button type="button" class="btn btn-outline-gbif-danger" data-bs-trigger="focus" data-bs-toggle="popover" data-bs-placement="top" data-bs-html="true" data-bs-content="<@s.text name="manage.overview.publishing.doi.delete.help" escapeHtml=true/>">
                    <i class="bi bi-info-circle"></i>
                </button>
                <@s.submit cssClass="confirmDeleteDoi btn btn-sm btn-outline-gbif-danger" name="deleteDoi" key="button.delete" disabled="${missingMetadata?string}"/>
            </div>
        </form>

    <#elseif resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi() >

        <form action='resource-reserveDoi.do' method='post'>
            <input name="r" type="hidden" value="${resource.shortname}"/>

            <div class="btn-group btn-group-sm" role="group">
                <button type="button" class="btn btn-outline-gbif-primary" data-bs-trigger="focus" data-bs-toggle="popover" data-bs-placement="top" data-bs-html="true" data-bs-content="<@s.text name="manage.overview.publishing.doi.reserve.new.help" escapeHtml=true/>">
                    <i class="bi bi-info-circle"></i>
                </button>
                <@s.submit cssClass="confirmReserveDoi btn btn-sm btn-outline-gbif-primary" name="reserveDoi" key="button.reserve.new" disabled="${missingMetadata?string}"/>
            </div>
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

        var showReport=false;
        $("#toggleReport").click(function() {
            if(showReport){
                showReport=false;
                $("#toggleReport").text("<@s.text name='basic.show'/>");
                $('#dwcaReport').hide();
            }else{
                showReport=true;
                $("#toggleReport").text("<@s.text name='basic.hide'/>");
                $('#dwcaReport').show();
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
            if(usedFileName != "") {
                $("#emlReplace").show();
                $("#emlCancel").show();
            }
        });
        $("#emlCancel").click(function(event) {
            event.preventDefault();
            $("#emlFile").prop("value", "");
            $("#emlReplace").hide();
            $("#emlCancel").hide();
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
                $("#add").attr("value", '<@s.text name="button.connectDB"/>');
                $("#add").show();
            }
        })

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

    <main class="container">

        <div class="my-3 p-3 bg-body rounded shadow-sm">

            <#include "/WEB-INF/pages/inc/action_alerts.ftl">

            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header fw-400 text-center">
                <span class="resourceOverviewTitle"><@s.text name="manage.overview.title"/>: </span>
                <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
            </h5>

            <div class="row g-2 mx-md-4 mx-2">
                <div class="col-lg-10">
                    <p>
                        <#if resource.coreType?has_content && resource.coreType==metadataType>
                            <@s.text name="manage.overview.description.metadataOnly"/>
                        <#else>
                            <@s.text name="manage.overview.description"/>
                        </#if>
                    </p>
                </div>

                <div class="col-lg-2 d-lg-flex justify-content-lg-end">
                    <#if resource.isAlreadyAssignedDoi()?string == "false" && resource.status != "REGISTERED">
                        <#assign disableRegistrationRights="false"/>
                    <#elseif currentUser.hasRegistrationRights()?string == "true">
                        <#assign disableRegistrationRights="false"/>
                    <#else>
                        <#assign disableRegistrationRights="true"/>
                    </#if>

                    <#if resource.status == "DELETED">
                        <form action='resource-undelete.do' method='post'>
                            <input name="r" type="hidden" value="${resource.shortname}" />

                            <@s.submit cssClass="btn btn-sm btn-outline-gbif-primary confirmUndeletion" name="undelete" key="button.undelete" disabled='${disableRegistrationRights?string}' />
                        </form>
                    <#else>
                        <#if !currentUser.hasRegistrationRights() && (resource.isAlreadyAssignedDoi()?string == "true" || resource.status == "REGISTERED")>
                            <div class="btn-group btn-group-sm" role="group">
                                <button id="btnGroupDelete" type="button" class="btn btn-sm btn-outline-gbif-danger dropdown-toggle align-self-start" data-bs-toggle="dropdown" aria-expanded="false" <#if disableRegistrationRights=="true">disabled</#if> >
                                    <@s.text name="button.delete"/>
                                </button>
                                <ul class="dropdown-menu" aria-labelledby="btnGroupDelete">
                                    <li>
                                        <form action="resource-delete.do" method='post'>
                                            <input name="r" type="hidden" value="${resource.shortname}" />
                                            <@s.submit cssClass="btn btn-sm btn-outline-gbif-danger confirmDeletion confirmDeletionFromIptAndGbif w-100" name="delete" key="button.delete.fromIptAndGbif"/>
                                        </form>
                                    </li>
                                    <li>
                                        <form action="resource-deleteFromIpt.do" method='post'>
                                            <input name="r" type="hidden" value="${resource.shortname}" />
                                            <@s.submit cssClass="btn btn-sm btn-outline-gbif-danger confirmDeletion confirmDeletionFromIptOnly w-100" name="delete" key="button.delete.fromIpt"/>
                                        </form>
                                    </li>
                                </ul>
                            </div>
                        <#else>
                            <div class="btn-group btn-group-sm" role="group">
                                <button id="btnGroupDelete" type="button" class="btn btn-sm btn-outline-gbif-danger dropdown-toggle align-self-start" data-bs-toggle="dropdown" aria-expanded="false" <#if disableRegistrationRights=="true">disabled</#if> >
                                    <@s.text name="button.delete"/>
                                </button>
                                <ul class="dropdown-menu" aria-labelledby="btnGroupDelete">
                                    <li>
                                        <form action="resource-delete.do" method='post'>
                                            <input name="r" type="hidden" value="${resource.shortname}" />
                                            <@s.submit cssClass="btn btn-sm btn-outline-gbif-danger confirmDeletion confirmDeletionFromIptAndGbif w-100" name="delete" key="button.delete.fromIptAndGbif"/>
                                        </form>
                                    </li>
                                    <li>
                                        <form action="resource-deleteFromIpt.do" method='post'>
                                            <input name="r" type="hidden" value="${resource.shortname}" />
                                            <@s.submit cssClass="btn btn-sm btn-outline-gbif-danger confirmDeletion confirmDeletionFromIptOnly w-100" name="delete" key="button.delete.fromIpt"/>
                                        </form>
                                    </li>
                                </ul>
                            </div>
                        </#if>
                    </#if>
                </div>
            </div>

            <#if resource.status == "DELETED" && !currentUser.hasRegistrationRights()>
                <div class="mx-md-4 mx-2">
                    <p class="text-gbif-warning fst-italic">
                        <i class="bi bi-exclamation-triangle"></i>
                        <@s.text name="manage.resource.status.undeletion.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/>
                    </p>
                </div>
            </#if>

            <#if currentUser.hasRegistrationRights() && (resource.isAlreadyAssignedDoi()?string == "true" || resource.status == "PRIVATE")>
                <div class="mx-md-4 mx-2">
                    <p class="text-gbif-warning fst-italic">
                        <i class="bi bi-exclamation-triangle"></i>
                        <@s.text name="manage.resource.status.deletion.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/>
                    </p>
                </div>
            </#if>

            <div id="dialog" class="modal fade" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true"></div>
        </div>

        <!-- when resource is of type metadata-only, there is no need to show source data and mapping sections -->
        <#if resource.coreType?has_content && resource.coreType==metadataType>
            <#include "/WEB-INF/pages/manage/overview_metadata.ftl"/>
        <#else>
            <#include "/WEB-INF/pages/manage/overview_data.ftl"/>
            <#include "/WEB-INF/pages/manage/overview_metadata.ftl"/>
        </#if>

        <div class="my-3 p-3 bg-body rounded shadow-sm" id="publish">
            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 text-gbif-header fw-400">
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

            <div class="row">
                <div class="col-lg-9 order-lg-last">
                    <p class="mx-md-4 mx-2">
                        <@s.text name="manage.overview.published.intro"/>
                    </p>

                    <!-- resources cannot be published if the mandatory metadata is missing -->
                    <#if missingMetadata>
                        <p class="mx-md-4 mx-2 text-gbif-warning fst-italic">
                            <i class="bi bi-exclamation-triangle"></i>
                            <@s.text name="manage.overview.published.missing.metadata"/>
                        </p>

                        <!-- resources that are already registered cannot be re-published if they haven't been assigned a GBIF-supported license -->
                    <#elseif resource.isRegistered() && !resource.isAssignedGBIFSupportedLicense()>
                        <p class="mx-md-4 mx-2 text-gbif-warning fst-italic">
                            <i class="bi bi-exclamation-triangle"></i>
                            <@s.text name="manage.overview.prevented.resource.publishing.noGBIFLicense" />
                        </p>

                        <!-- resources with a reserved DOI, existing registered DOI, or registered with GBIF can only be republished by managers with registration rights -->
                    <#elseif (resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION")
                    || (resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi())
                    || resource.status == "REGISTERED">
                        <!-- the user must have registration rights -->
                        <#if !currentUser.hasRegistrationRights()>
                            <p class="mx-md-4 mx-2 text-gbif-warning fst-italic">
                                <i class="bi bi-exclamation-triangle"></i>
                                <@s.text name="manage.resource.status.publication.forbidden"/>
                                &nbsp;<@s.text name="manage.resource.role.change"/>
                            </p>

                            <!-- an organisation with DOI account be activated (if resource has a reserved DOI or existing registered DOI) -->
                        <#elseif ((resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION" && resource.isAlreadyAssignedDoi())
                        || (resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi()))
                        && !organisationWithPrimaryDoiAccount??>

                            <p class="mx-md-4 mx-2 text-gbif-warning fst-italic">
                                <i class="bi bi-exclamation-triangle"></i>
                                <@s.text name="manage.resource.status.publication.forbidden.account.missing" />
                            </p>

                            <!-- when a DOI is reserved.. -->
                        <#elseif resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION">
                            <!-- and the resource has no existing DOI and its status is private..  -->
                            <#if !resource.isAlreadyAssignedDoi() && resource.status == "PRIVATE">
                                <!-- and the resource has never been published before, the first publication is a new major version -->
                                <#if !resource.lastPublished??>
                                    <p class="mx-md-4 mx-2 text-gbif-warning fst-italic">
                                        <i class="bi bi-exclamation-triangle"></i>
                                        <@s.text name="manage.overview.publishing.doi.register.prevented.notPublic"/>
                                    </p>

                                    <!-- and the resource has been published before, the next publication is a new minor version -->
                                <#else>
                                    <p class="mx-md-4 mx-2 text-gbif-primary fst-italic">
                                        <@s.text name="manage.overview.publishing.doi.register.prevented.notPublic" />
                                    </p>
                                </#if>

                                <!-- and its status is public (or registered), its reserved DOI can be registered during next publication  -->
                            <#elseif resource.status == "PUBLIC" || resource.status == "REGISTERED">
                                <p class="mx-md-4 mx-2 text-gbif-primary fst-italic">
                                    <@s.text name="manage.overview.publishing.doi.register.help"/>
                                </p>
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

                        <div class="table-responsive mx-md-4 mx-2">
                            <table class="table table-sm table-borderless text-smaller">
                                <tr>
                                    <th></th>
                                    <#if resource.lastPublished??>
                                        <td class="text-gbif-primary">${lastPublishedTitle?cap_first}</td>
                                        <td class="left_padding">
                                            ${nextPublishedTitle?cap_first}
                                        </td>
                                    <#else>
                                        <td>
                                            ${nextPublishedTitle?cap_first}
                                        </td>
                                    </#if>
                                </tr>
                                <tr>
                                    <th class="col-4">${versionTitle?cap_first}</th>
                                    <#if resource.lastPublished??>
                                        <td class="separator text-gbif-primary">
                                            ${resource.emlVersion.toPlainString()}&nbsp;
                                            <a class="btn btn-sm btn-outline-gbif-primary" role="button" href="${baseURL}/resource?r=${resource.shortname}">${viewTitle?cap_first}</a>
                                            <@dwcaValidator/>
                                        </td>
                                        <td class="left_padding">
                                            ${resource.getNextVersion().toPlainString()}&nbsp;
                                            <a class="btn btn-sm <#if missingMetadata>btn-outline-secondary disabled<#else>btn-outline-gbif-primary</#if> " role="button" href="${baseURL}/resource/preview?r=${resource.shortname}">${previewTitle?cap_first}</a>
                                        </td>
                                    <#else>
                                        <td>
                                            ${resource.getNextVersion().toPlainString()}&nbsp;
                                            <a class="btn btn-sm <#if missingMetadata>btn-outline-secondary disabled<#else>btn-outline-gbif-primary</#if> " role="button" href="${baseURL}/resource/preview?r=${resource.shortname}">${previewTitle?cap_first}</a>
                                        </td>
                                    </#if>
                                </tr>
                                <!-- hide visibility row if 1) a DOI has already been assigned to the resource since any resource with a DOI has to be public, 2) the resource is registered, or 3) the visibility of the currenct version and next version are the same -->
                                <#if !resource.isAlreadyAssignedDoi() && !resource.isRegistered() && (resource.getStatus()?lower_case != resource.getLastPublishedVersionsPublicationStatus()?lower_case) || !resource.lastPublished?? >
                                    <tr>
                                        <th>${visibilityTitle?cap_first}</th>
                                        <#if resource.lastPublished??>
                                            <td class="separator text-gbif-primary">
                                                ${resource.getLastPublishedVersionsPublicationStatus()?lower_case?cap_first}
                                            </td>
                                            <td class="left_padding">
                                                ${resource.status?lower_case?cap_first}
                                            </td>
                                        <#else>
                                            <td>
                                                ${resource.status?lower_case?cap_first}
                                            </td>
                                        </#if>
                                    </tr>
                                </#if>
                                <!-- hide DOI row if no organisation with DOI account has been activated yet -->
                                <#if organisationWithPrimaryDoiAccount??>
                                    <tr>
                                        <th>DOI</th>
                                        <#if resource.lastPublished??>
                                            <td class="separator text-gbif-primary">
                                                <#if resource.isAlreadyAssignedDoi()>
                                                    ${resource.versionHistory[0].doi!}
                                                <#else>
                                                    ${emptyCell}
                                                </#if>
                                            </td>
                                            <td class="left_padding">
                                                <#if (resource.isAlreadyAssignedDoi() && resource.versionHistory[0].doi != resource.doi!"") || (!resource.isAlreadyAssignedDoi() && resource.doi?has_content)>
                                                    <em>${resource.doi!emptyCell}</em>&nbsp;
                                                </#if>
                                                <@nextDoiButtonTD/>
                                            </td>
                                        <#else>
                                            <td>
                                                <#if (resource.isAlreadyAssignedDoi() && resource.versionHistory[0].doi != resource.doi!"") || (!resource.isAlreadyAssignedDoi() && resource.doi?has_content)>
                                                    <em>${resource.doi!emptyCell}</em>&nbsp;
                                                </#if>
                                                <@nextDoiButtonTD/>
                                            </td>
                                        </#if>
                                    </tr>
                                </#if>
                                <!-- TODO: hide license row if the current version and next version have both been assigned the same license -->
                                <#if (resource.lastPublished?? && !action.isLastPublishedVersionAssignedGBIFSupportedLicense(resource)) || !resource.lastPublished?? || !resource.isAssignedGBIFSupportedLicense()>
                                    <tr>
                                        <th>${licenseTitle?cap_first}</th>
                                        <#if resource.lastPublished??>
                                            <td class="separator text-gbif-primary">
                                                <@shortLicense action.getLastPublishedVersionAssignedLicense(resource)!/>
                                            </td>
                                            <td class="left_padding">
                                                <@shortLicense resource.getEml().parseLicenseUrl()/>
                                            </td>
                                        <#else>
                                            <td>
                                                <@shortLicense resource.getEml().parseLicenseUrl()/>
                                            </td>
                                        </#if>
                                    </tr>
                                </#if>
                                <tr>
                                    <th>${releasedTitle?cap_first}</th>
                                    <#if resource.lastPublished??>
                                        <td class="separator text-gbif-primary">
                                            ${resource.lastPublished?datetime?string.long_short}
                                        </td>
                                        <td class="left_padding">
                                            <#if resource.nextPublished??>
                                                ${resource.nextPublished?datetime?string.long_short}
                                            <#else>
                                                ${emptyCell}
                                            </#if>
                                        </td>
                                    <#else>
                                        <td>
                                            <#if resource.nextPublished??>
                                                ${resource.nextPublished?datetime?string.long_short}
                                            <#else>
                                                ${emptyCell}
                                            </#if>
                                        </td>
                                    </#if>
                                </tr>
                                <#if resource.lastPublished??>
                                    <tr>
                                        <th>${pubLogTitle?cap_first}</th>
                                        <td class="separator">
                                            <a class="button" target="_blank" href="${baseURL}/publicationlog.do?r=${resource.shortname}">
                                                <input class="button btn btn-sm btn-outline-gbif-primary" type="button" value='${downloadTitle?cap_first}'/>
                                            </a>
                                        </td>
                                        <td class="left_padding">${emptyCell}</td>
                                    </tr>
                                </#if>
                                <#if report??>
                                    <tr>
                                        <th>${pubRepTitle?cap_first}</th>
                                        <td class="separator">
                                            <#if report?? && (report.state?contains('cancelled') || report.exception?has_content) >
                                                <em>${report.state}</em>&nbsp;
                                            </#if>
                                            <a id="toggleReport" href="#">${showTitle?cap_first}</a>
                                        </td>
                                        <td class="left_padding">${emptyCell}</td>
                                    </tr>
                                </#if>
                            </table>
                        </div>
                        <#if report??>
                            <table>
                                <tr id="dwcaReport" style="display: none;">
                                    <td colspan="2">
                                        <div class="report">
                                            <ul class="simple">
                                                <#list report.messages as msg>
                                                    <li class="${msg.level}">${msg.message} <span class="small">${msg.date?time?string}</span></li>
                                                </#list>
                                            </ul>
                                            <#if cfg.debug() && report.hasException()>
                                                <br/>
                                                <ul class="simple">
                                                    <li><strong>Exception</strong> ${report.exceptionMessage!}</li>
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

                <div class="col-lg-3 border-lg-right">
                    <div class="mx-md-4 mx-2">
                        <@publish resource/>
                    </div>
                </div>
            </div>
        </div>

        <div class="my-3 p-3 bg-body rounded shadow-sm" id="autopublish">
            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 text-gbif-header fw-400">
                <@popoverPropertyInfo "manage.overview.autopublish.description"/>
                <@s.text name="manage.overview.autopublish.title"/>
            </h5>

            <div class="row">
                <div class="col-lg-9 order-lg-last">
                    <div class="mx-md-4 mx-2">
                        <p>
                            <#if resource.usesAutoPublishing()>
                                <@s.text name="manage.overview.autopublish.intro.activated"/>
                            <#else>
                                <@s.text name="manage.overview.autopublish.intro.deactivated"/>
                            </#if>
                        </p>

                        <#if resource.isDeprecatedAutoPublishingConfiguration()>
                            <p class="text-gbif-warning fst-italic">
                                <i class="bi bi-exclamation-triangle"></i>
                                <@s.text name="manage.overview.autopublish.deprecated.warning.button" escapeHtml=true/>
                            </p>
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

                <div class="col-lg-3 border-lg-right">
                    <div class="mx-md-4 mx-2">
                        <form action='auto-publish.do' method='get'>
                            <input name="r" type="hidden" value="${resource.shortname}"/>
                            <@s.submit name="edit" cssClass="btn btn-sm btn-outline-gbif-primary" key="button.edit"/>
                        </form>
                    </div>
                </div>
            </div>
        </div>

        <div class="my-3 p-3 bg-body rounded shadow-sm" id="visibility">
            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 text-gbif-header fw-400">
                <#assign visibilityTitleInfo>
                    <@s.text name='manage.overview.visibility.description'/>
                    <br><br>
                    <@s.text name='manage.resource.status.intro.public.migration'><@s.param><a href="${baseURL}/manage/metadata-additional.do?r=${resource.shortname}&amp;edit=Edit"><@s.text name="submenu.additional"/></a></@s.param></@s.text></br></br><@s.text name='manage.resource.status.intro.public.gbifWarning'/>
                </#assign>

                <@popoverTextInfo visibilityTitleInfo/>
                <@s.text name='manage.overview.visibility'/>
            </h5>

            <div class="row">
                <div class="col-lg-9 order-lg-last">
                    <div class="mx-md-4 mx-2">
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

                            <#if cfg.devMode() && cfg.getRegistryType()!='PRODUCTION'>
                                <p class="text-gbif-danger">
                                    <i class="bi bi-exclamation-triangle"></i>
                                    <em><@s.text name="manage.overview.published.testmode.warning"/></em>
                                </p>
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

                            <#if resource.status=="PUBLIC">
                                <#if !currentUser.hasRegistrationRights()>
                                    <!-- Show warning: user must have registration rights -->
                                    <p class="text-gbif-warning fst-italic">
                                        <i class="bi bi-exclamation-triangle"></i>
                                        <@s.text name="manage.resource.status.registration.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/>
                                    </p>
                                <#elseif missingValidPublishingOrganisation?string == "true">
                                    <!-- Show warning: user must assign valid publishing organisation -->
                                    <p class="text-gbif-warning fst-italic">
                                        <i class="bi bi-exclamation-triangle"></i>
                                        <@s.text name="manage.overview.visibility.missing.organisation"/>
                                    </p>
                                <#elseif missingRegistrationMetadata?string == "true">
                                    <!-- Show warning: user must fill in minimum registration metadata -->
                                    <p class="text-gbif-warning fst-italic">
                                        <i class="bi bi-exclamation-triangle"></i>
                                        <@s.text name="manage.overview.visibility.missing.metadata" />
                                    </p>
                                <#elseif !resource.isLastPublishedVersionPublic()>
                                    <!-- Show warning: last published version must be publicly available to register -->
                                    <p class="text-gbif-warning fst-italic">
                                        <i class="bi bi-exclamation-triangle"></i>
                                        <@s.text name="manage.overview.prevented.resource.registration.notPublic" />
                                    </p>
                                <#elseif !action.isLastPublishedVersionAssignedGBIFSupportedLicense(resource)>
                                    <!-- Show warning: resource must be assigned a GBIF-supported license to register if resource has occurrence data -->
                                    <p class="text-gbif-warning fst-italic">
                                        <i class="bi bi-exclamation-triangle"></i>
                                        <@s.text name="manage.overview.prevented.resource.registration.noGBIFLicense" escapeHtml=true/>
                                    </p>
                                </#if>
                            </#if>
                        </div>
                    </div>
                </div>

                <div class="col-lg-3 border-lg-right">
                    <#assign actionMethod>registerResource</#assign>
                    <#if resource.status=="PRIVATE">
                        <#assign actionMethod>makePublic</#assign>
                    </#if>

                    <div class="mx-md-4 mx-2 d-flex flex-wrap">
                        <form class="me-1" action='resource-${actionMethod}.do' method='post'>
                            <input name="r" type="hidden" value="${resource.shortname}"/>
                            <#if resource.status=="PUBLIC">
                                <#if !currentUser.hasRegistrationRights()>
                                    <!-- Disable register button and show warning: user must have registration rights -->
                                    <#assign visibilityConfirmRegistrationWarning>
                                        <@s.text name="manage.resource.status.registration.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/>
                                    </#assign>

                                    <@s.submit cssClass="confirmRegistration btn btn-sm  btn-outline-gbif-primary" name="register" key="button.register" disabled="true"/>
                                <#elseif missingValidPublishingOrganisation?string == "true">
                                    <!-- Disable register button and show warning: user must assign valid publishing organisation -->
                                    <@s.submit cssClass="confirmRegistration btn btn-sm btn-outline-gbif-primary" name="register" key="button.register" disabled="true"/>
                                <#elseif missingRegistrationMetadata?string == "true">
                                    <!-- Disable register button and show warning: user must fill in minimum registration metadata -->
                                    <@s.submit cssClass="confirmRegistration btn btn-sm btn-outline-gbif-primary" name="register" key="button.register" disabled="true"/>
                                <#elseif !resource.isLastPublishedVersionPublic()>
                                    <!-- Disable register button and show warning: last published version must be publicly available to register -->
                                    <@s.submit cssClass="confirmRegistration btn btn-sm btn-outline-gbif-primary" name="register" key="button.register" disabled="true"/>
                                <#elseif !action.isLastPublishedVersionAssignedGBIFSupportedLicense(resource)>
                                    <!-- Disable register button and show warning: resource must be assigned a GBIF-supported license to register if resource has occurrence data -->
                                    <@s.submit cssClass="confirmRegistration btn btn-sm btn-outline-gbif-primary" name="register" key="button.register" disabled="true"/>
                                <#else>
                                    <@s.submit cssClass="confirmRegistration btn btn-sm btn-outline-gbif-primary" name="register" key="button.register"/>
                                </#if>
                            <#else>
                                <#if resource.status=="PRIVATE">
                                    <@s.submit name="makePrivate" cssClass="btn btn-sm btn-outline-gbif-primary" key="button.public"/>
                                </#if>
                            </#if>
                        </form>

                        <#if resource.status=="PUBLIC" && (resource.identifierStatus=="PUBLIC_PENDING_PUBLICATION" || resource.identifierStatus == "UNRESERVED")>
                            <#assign actionMethod>makePrivate</#assign>
                            <form action='resource-${actionMethod}.do' method='post'>
                                <@s.submit cssClass="confirm btn btn-sm btn-outline-gbif-primary" name="unpublish" key="button.private" />
                            </form>
                        </#if>
                    </div>
                </div>
            </div>
        </div>


        <div class="my-3 p-3 bg-body rounded shadow-sm" id="networks">
            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 text-gbif-header fw-400">
                <@popoverPropertyInfo "manage.overview.networks.description"/>
                <@s.text name="manage.overview.networks.title"/>
            </h5>

            <div class="row">
                <div class="col-lg-9 order-lg-last">
                    <div class="mx-md-4 mx-2">
                        <#if resource.key?has_content>
                            <#if (resourceNetworks?size>0)>
                                <p>
                                    <@s.text name="manage.overview.networks.intro"/>
                                </p>

                                <div class="details table-responsive">
                                    <table class="table table-sm table-borderless text-smaller">
                                        <#list resourceNetworks as n>
                                            <tr>
                                                <th class="col-4">
                                                    ${n.title!""}
                                                </th>
                                                <td>
                                                    <a href="${cfg.portalUrl}/network/${n.key}" target="_blank">${n.key}</a>&nbsp;
                                                </td>
                                                <td class="d-flex justify-content-end">
                                                    <a class="button btn btn-sm btn-outline-gbif-danger" href="resource-deleteNetwork.do?r=${resource.shortname}&id=${n.key}">
                                                        <@s.text name='button.delete'/>
                                                    </a>
                                                </td>
                                            </tr>
                                        </#list>
                                    </table>
                                </div>
                            <#else>
                                <p>
                                    <@s.text name="manage.overview.networks.no.data"/>
                                </p>
                            </#if>
                        <#else>
                            <p class="text-gbif-warning">
                                <i class="bi bi-exclamation-triangle"></i>
                                <em><@s.text name="manage.overview.networks.not.registered"/></em>
                            </p>
                        </#if>
                    </div>
                </div>

                <div class="col-lg-3 border-lg-right">
                    <#if resource.key?has_content>
                        <#if (potentialNetworks?size>0)>
                            <div class="mx-md-4 mx-2">
                                <form action='resource-addNetwork.do' method='post'>
                                    <input name="r" type="hidden" value="${resource.shortname}"/>
                                    <select name="id" class="form-select form-select-sm my-1" id="network" size="1">
                                        <option value=""></option>
                                        <#list potentialNetworks?sort_by("name") as n>
                                            <option value="${n.key}">${n.name}</option>
                                        </#list>
                                    </select>
                                    <@s.submit name="add" cssClass="btn btn-sm btn-outline-gbif-primary my-1" key="button.add"/>
                                </form>
                            </div>
                        </#if>
                    </#if>
                </div>
            </div>
        </div>

        <div class="my-3 p-3 bg-body rounded shadow-sm" id="managers">
            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 text-gbif-header fw-400">
                <@popoverPropertyInfo "manage.overview.resource.managers.description"/>
                <@s.text name="manage.overview.resource.managers"/>
            </h5>

            <div class="row">
                <div class="col-lg-9 order-lg-last">
                    <div class="mx-md-4 mx-2">
                        <p>
                            <@s.text name="manage.overview.resource.managers.intro"><@s.param>${resource.shortname}</@s.param></@s.text>
                        </p>

                        <div class="details table-responsive">
                            <table class="table table-sm table-borderless text-smaller">
                                <tr>
                                    <th class="col-4"><@s.text name="manage.overview.resource.managers.creator"/></th>
                                    <td>${resource.creator.name!}, ${resource.creator.email}</td>
                                </tr>
                                <#if (resource.managers?size>0)>
                                    <#list resource.managers as u>
                                        <tr>
                                            <th><@s.text name="manage.overview.resource.managers.manager"/></th>
                                            <!-- Warning: method name match is case sensitive therefore must be deleteManager -->
                                            <td>
                                                ${u.name}, ${u.email}&nbsp;
                                            </td>
                                            <td class="d-flex justify-content-end">
                                                <a class="button btn btn-sm btn-outline-gbif-danger" href="resource-deleteManager.do?r=${resource.shortname}&id=${u.email}">
                                                    <@s.text name='button.delete'/>
                                                </a>
                                            </td>
                                        </tr>
                                    </#list>
                                </#if>
                            </table>
                        </div>
                    </div>
                </div>

                <div class="col-lg-3 border-lg-right">
                    <#if (potentialManagers?size>0)>
                        <div class="mx-md-4 mx-2">
                            <!-- Warning: method name match is case sensitive therefore must be addManager -->
                            <form action='resource-addManager.do' method='post'>
                                <input name="r" type="hidden" value="${resource.shortname}"/>
                                <select name="id" class="form-select form-select-sm my-1" id="manager" size="1">
                                    <option value=""></option>
                                    <#list potentialManagers?sort_by("name") as u>
                                        <option value="${u.email}">${u.name}</option>
                                    </#list>
                                </select>
                                <@s.submit name="add" cssClass="btn btn-sm btn-outline-gbif-primary my-1" key="button.add"/>
                            </form>
                        </div>
                    </#if>
                </div>
            </div>
        </div>
    </main>

</#escape>
<#include "/WEB-INF/pages/inc/footer.ftl">
