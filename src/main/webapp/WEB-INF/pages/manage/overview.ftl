<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.OverviewAction" -->
<#escape x as x?html>

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
<#include "/WEB-INF/pages/inc/header-bootstrap.ftl">
<title><@s.text name="manage.overview.title"/>: ${resource.title!resource.shortname}</title>

<script type="text/javascript" src="${baseURL}/js/jconfirmation-bootstrap.jquery.js"></script>

<script type="text/javascript">
    $(document).ready(function(){
        initHelp();
        <#if confirmOverwrite>
        showConfirmOverwrite();
        </#if>
        var $registered = false;

        $('.confirm').jConfirmActionBootstrap({question : "<@s.text name='basic.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>"});
        $('.confirmRegistration').jConfirmActionBootstrap({question : "<@s.text name='manage.overview.visibility.confirm.registration'/> <@s.text name='manage.resource.delete.confirm.registered'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", checkboxText: "<@s.text name='manage.overview.visibility.confirm.agreement'/>"});
        $('.confirmDeletion').jConfirmActionBootstrap({question : "<#if resource.isAlreadyAssignedDoi()><@s.text name='manage.resource.delete.confirm.doi'/></br></br></#if><#if resource.status=='REGISTERED'><@s.text name='manage.resource.delete.confirm.registered'/></br></br></#if><@s.text name='manage.resource.delete.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>"});
        $('.confirmUndeletion').jConfirmActionBootstrap({question : "<@s.text name='manage.resource.undoDelete.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>"});

        $('.confirmReserveDoi').jConfirmActionBootstrap({question : "<@s.text name='manage.overview.publishing.doi.reserve.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>"});
        $('.confirmDeleteDoi').jConfirmActionBootstrap({question : "<@s.text name='manage.overview.publishing.doi.delete.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>"});
        $('.confirmPublishMinorVersion').jConfirmActionBootstrap({question : "<@s.text name='manage.overview.publishing.doi.minorVersion.confirm'/></br></br><@s.text name='manage.overview.publishing.doi.summary'/></br></br><@s.text name='manage.overview.publishing.doi.confirm.end'/>", yesAnswer : "<@s.text name='basic.yes'/>", summary : "<@s.text name='manage.overview.publishing.doi.summary.placeholder'/>", cancelAnswer : "<@s.text name='basic.no'/>"});
        $('.confirmPublishMajorVersion').jConfirmActionBootstrap({question : "<@s.text name='manage.overview.publishing.doi.majorVersion.confirm'/></br></br><@s.text name='manage.overview.publishing.doi.summary'/></br></br><@s.text name='manage.overview.publishing.doi.confirm.end'/>", yesAnswer : "<@s.text name='basic.yes'/>", summary : "<@s.text name='manage.overview.publishing.doi.summary.placeholder'/>", cancelAnswer : "<@s.text name='basic.no'/>", checkboxText: "<@s.text name='manage.overview.publishing.doi.register.agreement'/>"});
        $('.confirmPublishMajorVersionWithoutDOI').jConfirmActionBootstrap({question : "<@s.text name='manage.overview.publishing.withoutDoi.majorVersion.confirm'/></br></br><@s.text name='manage.overview.publishing.doi.summary'/></br></br><@s.text name='manage.overview.publishing.doi.confirm.end'/>", yesAnswer : "<@s.text name='basic.yes'/>", summary : "<@s.text name='manage.overview.publishing.doi.summary.placeholder'/>", cancelAnswer : "<@s.text name='basic.no'/>"});

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
        $("#file").change(function() {
            var usedFileName = $("#file").prop("value");
            if(usedFileName != "") {
                $("#add").attr("value", '<@s.text name="button.add"/>');
            }
        });
        $("#clear").click(function(event) {
            event.preventDefault();
            $("#file").prop("value", "");
            $("#add").attr("value", '<@s.text name="button.connectDB"/>');
        });

        $(function() {
            $('.icon-validate').tooltip({track: true});
        });

        function showConfirmOverwrite() {
            var question='<p><@s.text name="manage.resource.addSource.confirm"/></p>';
            $('#dialog').html(question);
            $("#dialog").dialog({
                'modal'     : true,
                'title'		: '<@s.text name="basic.confirm"/>',
                'buttons'   : {
                    '<@s.text name="basic.yes"/>': function(){
                        $(this).dialog("close");
                        $("#add").click();
                    },
                    '<@s.text name="basic.no"/>' : function(){
                        $(this).dialog("close");
                        $("#cancel").click();
                    }
                }
            });
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
<#include "/WEB-INF/pages/inc/menu-bootstrap.ftl">
<#include "/WEB-INF/pages/macros/forms-bootstrap.ftl"/>
<#include "/WEB-INF/pages/macros/popover-bootstrap.ftl"/>
<#include "/WEB-INF/pages/macros/manage/publish-bootstrap.ftl"/>
<#assign metadataType = "metadata"/>

    <main class="container pt-5">

        <div class="my-3 p-3 bg-body rounded shadow-sm">

            <#include "/WEB-INF/pages/inc/action_alerts-bootstrap.ftl">

            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-success text-center">
                <span data-bs-container="body" data-bs-toggle="popover" data-bs-placement="top" data-bs-html="true" data-bs-content="
                        <#if resource.coreType?has_content && resource.coreType==metadataType>
                            <@s.text name="manage.overview.intro.metadataOnly"><@s.param>${resource.title!resource.shortname}</@s.param></@s.text>
                        <#else>
                            <@s.text name="manage.overview.intro"><@s.param>${resource.title!resource.shortname}</@s.param></@s.text>
                        </#if>
                    ">
                    <i class="bi bi-info-circle"></i>
                </span>

                <span class="resourceOverviewTitle"><@s.text name="manage.overview.title"/>: </span>
                <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
            </h5>

            <p class="text-muted mx-md-4 mx-2">
                <@s.text name="manage.overview.description"><@s.param>${resource.title!resource.shortname}</@s.param></@s.text>
            </p>
        </div>

        <!-- when resource is of type metadata-only, there is no need to show source data and mapping sections -->
        <#if resource.coreType?has_content && resource.coreType==metadataType>
            <#include "/WEB-INF/pages/manage/overview_metadata-bootstrap.ftl"/>
        <#else>
            <#include "/WEB-INF/pages/manage/overview_data-bootstrap.ftl"/>
            <#include "/WEB-INF/pages/manage/overview_metadata-bootstrap.ftl"/>
        </#if>

        <div class="my-3 p-3 bg-body rounded shadow-sm" id="publish">
            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 text-success">
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
                    <p class="text-muted mx-md-4 mx-2">
                        <@s.text name="manage.overview.published.intro"/>
                    </p>

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
                            <table class="table table-sm table-borderless" style="font-size: 0.875em;">
                                <tr>
                                    <th></th><#if resource.lastPublished??><td class="green">${lastPublishedTitle?cap_first}</td></#if><td class="left_padding">${nextPublishedTitle?cap_first}</td>
                                </tr>
                                <tr>
                                    <th>${versionTitle?cap_first}</th><#if resource.lastPublished??>
                                    <td class="separator green">${resource.emlVersion.toPlainString()}&nbsp;<a class="btn btn-sm btn-outline-success ignore-link-color" role="button" href="${baseURL}/resource?r=${resource.shortname}">${viewTitle?cap_first}'</a><@dwcaValidator/></td></#if><td class="left_padding">${resource.getNextVersion().toPlainString()}&nbsp;<a class="btn btn-sm ignore-link-color <#if missingMetadata>btn-outline-secondary disabled<#else>btn-outline-success</#if> " role="button" href="${baseURL}/resource/preview?r=${resource.shortname}">${previewTitle?cap_first}</a></td>
                                </tr>
                                <!-- hide visibility row if 1) a DOI has already been assigned to the resource since any resource with a DOI has to be public, 2) the resource is registered, or 3) the visibility of the currenct version and next version are the same -->
                                <#if !resource.isAlreadyAssignedDoi() && !resource.isRegistered() && (resource.getStatus()?lower_case != resource.getLastPublishedVersionsPublicationStatus()?lower_case) || !resource.lastPublished?? >
                                    <tr>
                                        <th>${visibilityTitle?cap_first}</th><#if resource.lastPublished??><td class="separator green">${resource.getLastPublishedVersionsPublicationStatus()?lower_case?cap_first}</td></#if><td class="left_padding">${resource.status?lower_case?cap_first}</td>
                                    </tr>
                                </#if>
                                <!-- hide DOI row if no organisation with DOI account has been activated yet -->
                                <#if organisationWithPrimaryDoiAccount??>
                                    <tr>
                                        <th>DOI</th><#if resource.lastPublished??><td class="separator green"><#if resource.isAlreadyAssignedDoi()>${resource.versionHistory[0].doi!}<#else>${emptyCell}</#if></td></#if><td class="left_padding"><#if (resource.isAlreadyAssignedDoi() && resource.versionHistory[0].doi != resource.doi!"") || (!resource.isAlreadyAssignedDoi() && resource.doi?has_content)><em>${resource.doi!emptyCell}</em>&nbsp;</#if><@nextDoiButtonTD/></td>
                                    </tr>
                                </#if>
                                <!-- TODO: hide license row if the current version and next version have both been assigned the same license -->
                                <#if (resource.lastPublished?? && !action.isLastPublishedVersionAssignedGBIFSupportedLicense(resource)) || !resource.lastPublished?? || !resource.isAssignedGBIFSupportedLicense()>
                                    <tr>
                                        <th>${licenseTitle?cap_first}</th><#if resource.lastPublished??><td class="separator green"><@shortLicense action.getLastPublishedVersionAssignedLicense(resource)!/></td></#if><td class="left_padding"><@shortLicense resource.getEml().parseLicenseUrl()/></td>
                                    </tr>
                                </#if>
                                <tr>
                                    <th>${releasedTitle?cap_first}</th><#if resource.lastPublished??><td class="separator green">${resource.lastPublished?date?string.medium}</td></#if><td class="left_padding"><#if resource.nextPublished??>${resource.nextPublished?date?string("MMM d, yyyy, HH:mm:ss")}<#else>${emptyCell}</#if></td>
                                </tr>
                                <#if resource.lastPublished??>
                                    <tr>
                                        <th>${pubLogTitle?cap_first}</th><td class="separator"><a class="button" target="_blank" href="${baseURL}/publicationlog.do?r=${resource.shortname}"><input class="button" type="button" value='${downloadTitle?cap_first}'/></a></td><td class="left_padding">${emptyCell}</td>
                                    </tr>
                                </#if>
                                <#if report??>
                                    <tr>
                                        <th>${pubRepTitle?cap_first}</th><td class="separator"><#if report?? && (report.state?contains('cancelled') || report.exception?has_content) ><em>${report.state}</em>&nbsp;</#if><a id="toggleReport" href="#">${showTitle?cap_first}</a></td><td class="left_padding">${emptyCell}</td>
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

                <div class="col-lg-3">
                    <div class="mx-md-4 mx-2">
                        <@publish resource/>
                    </div>
                </div>
            </div>
        </div>

        <div class="my-3 p-3 bg-body rounded shadow-sm" id="autopublish">
            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 text-success">
                <@popoverPropertyInfo "manage.overview.autopublish.description"/>
                <@s.text name="manage.overview.autopublish.title"/>
            </h5>

            <div class="row">
                <div class="col-lg-9 order-lg-last">
                    <div class="mx-md-4 mx-2">
                        <p class="text-muted">
                            <#if resource.usesAutoPublishing()>
                                <@s.text name="manage.overview.autopublish.intro.activated"/>
                            <#else>
                                <@s.text name="manage.overview.autopublish.intro.deactivated"/>
                            </#if>
                        </p>

                        <div class="details">
                            <table class="table table-borderless">
                                <#if resource.usesAutoPublishing()>
                                    <tr>
                                        <th><@s.text name='manage.overview.autopublish.publication.frequency'/></th>
                                        <td><@s.text name="${autoPublishFrequencies.get(resource.updateFrequency.identifier)}"/></td>
                                    </tr>
                                    <tr>
                                        <th><@s.text name='manage.overview.autopublish.publication.next.date'/></th>
                                        <td>${resource.nextPublished?date?string("MMM d, yyyy, HH:mm:ss")}</td>
                                    </tr>
                                </#if>
                            </table>
                        </div>
                    </div>
                </div>

                <div class="col-lg-3">
                    <div class="mx-md-4 mx-2">
                        <form action='auto-publish.do' method='get'>
                            <input name="r" type="hidden" value="${resource.shortname}"/>
                            <#if resource.isDeprecatedAutoPublishingConfiguration()>
                                <@s.submit name="edit" cssClass="btn btn-outline-success" key="button.edit"/>
                                <@popoverPropertyWarning "manage.overview.autopublish.deprecated.warning.button"/>
                            <#else>
                                <@s.submit name="edit" cssClass="btn btn-outline-success" key="button.edit"/>
                            </#if>
                        </form>
                    </div>
                </div>
            </div>
        </div>

        <div class="my-3 p-3 bg-body rounded shadow-sm" id="visibility">
            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 text-success">
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

                            <p class="text-muted">
                                <#if resource.status=="PRIVATE">
                                    <span class="badge rounded-pill bg-danger">
                                        <@s.text name="resource.status.${resource.status?lower_case}"/>
                                    </span>
                                <#else>
                                    <span class="badge rounded-pill bg-success">
                                        <@s.text name="resource.status.${resource.status?lower_case}"/>
                                    </span>
                                </#if>
                                <@s.text name="manage.resource.status.intro.${resource.status?lower_case}"/>
                            </p>

                            <p>
                                <span class="badge rounded-pill bg-warning">
                                    <i class="bi bi-exclamation-triangle" style="color: black;"></i>
                                </span>
                                <em class="text-muted"><@s.text name="manage.overview.published.testmode.warning"/></em>
                            </p>
                            <#if resource.status=="REGISTERED" && resource.key??>
                                <div class="details">
                                    <table>
                                        <tr>
                                            <th>GBIF UUID</th>
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
                                                <td>${resource.organisation.primaryContactName!}, ${resource.organisation.primaryContactEmail!}</td>
                                            </tr>
                                            <tr>
                                                <th><@s.text name="manage.overview.visibility.endorsing.node"/></th>
                                                <td><a href="${cfg.portalUrl}/node/${resource.organisation.nodeKey!"#"}" target="_blank">${resource.organisation.nodeName!}</a></td>
                                            </tr>
                                        </#if>
                                    </table>
                                </div>
                            </#if>
                        </div>
                    </div>
                </div>

                <div class="col-lg-3">
                    <#assign actionMethod>registerResource</#assign>
                    <#if resource.status=="PRIVATE">
                        <#assign actionMethod>makePublic</#assign>
                    </#if>

                    <div class="mx-md-4 mx-2">
                        <form action='resource-${actionMethod}.do' method='post'>
                            <input name="r" type="hidden" value="${resource.shortname}"/>
                            <#if resource.status=="PUBLIC">
                                <#if !currentUser.hasRegistrationRights()>
                                    <!-- Disable register button and show warning: user must have registration rights -->
                                    <@s.submit cssClass="confirmRegistration btn btn-outline-success my-1" name="register" key="button.register" disabled="true"/>
                                    <#assign visibilityConfirmRegistrationWarning>
                                        <@s.text name="manage.resource.status.registration.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/>
                                    </#assign>
                                    <@popoverTextWarning visibilityConfirmRegistrationWarning/>
                                <#elseif missingValidPublishingOrganisation?string == "true">
                                    <!-- Disable register button and show warning: user must assign valid publishing organisation -->
                                    <@s.submit cssClass="confirmRegistration btn btn-outline-secondary my-1" name="register" key="button.register" disabled="true"/>
                                    <@popoverPropertyWarning "manage.overview.visibility.missing.organisation"/>
                                <#elseif missingRegistrationMetadata?string == "true">
                                    <!-- Disable register button and show warning: user must fill in minimum registration metadata -->
                                    <@s.submit cssClass="confirmRegistration btn-outline-secondary my-1" name="register" key="button.register" disabled="true"/>
                                    <@popoverPropertyWarning "manage.overview.visibility.missing.metadata"/>
                                <#elseif !resource.isLastPublishedVersionPublic()>
                                    <!-- Disable register button and show warning: last published version must be publicly available to register -->
                                    <@s.submit cssClass="confirmRegistration btn btn-outline-secondary my-1" name="register" key="button.register" disabled="true"/>
                                    <@popoverPropertyWarning "manage.overview.prevented.resource.registration.notPublic"/>
                                <#elseif !action.isLastPublishedVersionAssignedGBIFSupportedLicense(resource)>
                                    <!-- Disable register button and show warning: resource must be assigned a GBIF-supported license to register if resource has occurrence data -->
                                    <@s.submit cssClass="confirmRegistration btn btn-outline-secondary my-1" name="register" key="button.register" disabled="true"/>
                                    <@popoverPropertyWarning "manage.overview.prevented.resource.registration.noGBIFLicense"/>
                                <#else>
                                    <@s.submit cssClass="confirmRegistration btn btn-outline-success my-1" name="register" key="button.register"/>
                                </#if>
                            <#else>
                                <#if resource.status=="PRIVATE">
                                    <@s.submit name="makePrivate" cssClass="btn btn-outline-success my-1" key="button.public"/>
                                </#if>
                            </#if>
                        </form>

                        <#if resource.status=="PUBLIC" && (resource.identifierStatus=="PUBLIC_PENDING_PUBLICATION" || resource.identifierStatus == "UNRESERVED")>
                            <#assign actionMethod>makePrivate</#assign>
                            <form action='resource-${actionMethod}.do' method='post'>
                                <@s.submit cssClass="confirm btn btn-outline-success my-1" name="unpublish" key="button.private" />
                            </form>
                        </#if>
                    </div>
                </div>
            </div>
        </div>

        <div class="my-3 p-3 bg-body rounded shadow-sm" id="managers">
            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 text-success">
                <@popoverPropertyInfo "manage.overview.resource.managers.description"/>
                <@s.text name="manage.overview.resource.managers"/>
            </h5>

            <div class="row">
                <div class="col-lg-9 order-lg-last">
                    <div class="mx-md-4 mx-2">
                        <p class="text-muted">
                            <@s.text name="manage.overview.resource.managers.intro"><@s.param>${resource.shortname}</@s.param></@s.text>
                        </p>

                        <div class="details">
                            <table class="table table-sm table-borderless" style="font-size: 0.875em;">
                                <tr>
                                    <th><@s.text name="manage.overview.resource.managers.creator"/></th>
                                    <td>${resource.creator.name!}, ${resource.creator.email}</td>
                                </tr>
                                <#if (resource.managers?size>0)>
                                    <#list resource.managers as u>
                                        <tr>
                                            <th><@s.text name="manage.overview.resource.managers.manager"/></th>
                                            <!-- Warning: method name match is case sensitive therefore must be deleteManager -->
                                            <td>${u.name}, ${u.email}&nbsp;
                                                <a class="button" href="resource-deleteManager.do?r=${resource.shortname}&id=${u.email}">
                                                    <input class="btn btn-outline-danger" type="button" value='<@s.text name='button.delete'/>'/>
                                                </a>
                                            </td>
                                        </tr>
                                    </#list>
                                </#if>
                            </table>
                        </div>
                    </div>
                </div>

                <div class="col-lg-3">
                    <#if (potentialManagers?size>0)>
                        <div class="mx-md-4 mx-2">
                            <!-- Warning: method name match is case sensitive therefore must be addManager -->
                            <form action='resource-addManager.do' method='post'>
                                <input name="r" type="hidden" value="${resource.shortname}"/>
                                <select name="id" class="form-select" id="manager" size="1">
                                    <option value=""></option>
                                    <#list potentialManagers?sort_by("name") as u>
                                        <option value="${u.email}">${u.name}</option>
                                    </#list>
                                </select>
                                <@s.submit name="add" cssClass="btn btn-outline-success mt-1" key="button.add"/>
                            </form>
                        </div>
                    </#if>
                </div>
            </div>
        </div>
    </main>

</#escape>
<#include "/WEB-INF/pages/inc/footer-bootstrap.ftl">
