<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <script src="${baseURL}/js/jquery/jquery-3.7.0.min.js"></script>
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>

    <script>
        $(document).ready(function(){
            $('#organisation\\.key').select2({
                placeholder: '${action.getText("admin.organisation.name.select")}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                width: "100%",
                allowClear: true,
                theme: 'bootstrap4'
            });

            function displayChangeTokensView() {
                $('#tokens-block').show();
                $('#registration-block').hide();
                $('#network-block').hide();
                $('#tokens').show();
                $('#update').hide();
                $('#network').hide();
            }

            function displayEditRegistrationView() {
                $('#tokens-block').hide();
                $('#registration-block').show();
                $('#network-block').hide();
                $('#tokens').hide();
                $('#update').show();
                $('#network').hide();
            }

            function displayNetworkView() {
                $('#tokens-block').hide();
                $('#registration-block').hide();
                $('#network-block').show();
                $('#tokens').hide();
                $('#update').hide();
                $('#network').show();
            }

            $('#organisation\\.key').change(function() {

                var organisationSelected = $('#organisation\\.key :selected');
                var organisationKey = organisationSelected.val();
                var orgName = organisationSelected.text();

                $('#organisation\\.name').val(orgName);
                $('#ipt\\.organisationKey').val(organisationKey);

                var emailContent = 'Dear sir/madam,%0d%0d';
                emailContent += 'I am trying to install an Integrated Publishing Toolkit (IPT), which is going to be hosted under your institution/organization.%0d';
                emailContent += 'To continue with the installation, I will need to kindly ask you to provide me with your organization\'s shared token, as this is needed to complete the process%0d';
                emailContent += 'In case you don\'t know this information, you can open the following link in your browser to receive this information%0d%0d';
                emailContent += 'https://gbrds.gbif.org/registry/organisation/' + organisationKey + '?op=password%0d%0d';
                emailContent += 'Thank you for your attention.';

                $('#organisation\\.alias').val(orgName);

                if(organisationKey) {
                    var url = '${registryURL}organisation/' + organisationKey + ".json";

                    $.getJSON(url, function (data) {
                        $('#organisation\\.primaryContactType').val(data.primaryContactType);
                        $('#organisation\\.primaryContactName').val(data.primaryContactName);
                        $('#organisation\\.primaryContactEmail').val(data.primaryContactEmail);
                        $('#organisation\\.nodeKey').val(data.nodeKey);
                        $('#organisation\\.nodeName').val(data.nodeName);

                        //Create a contact link to prefill an email to request a password from an Organisation
                        var contactLink = '<div class="mt-2">';
                        contactLink += '<a href=\"mailto:';
                        contactLink += data.primaryContactEmail;
                        contactLink += '?subject=';
                        contactLink += 'Shared token request for ';
                        contactLink += orgName;
                        contactLink += '&body=';
                        contactLink += emailContent;
                        contactLink += '\">';
                        contactLink += '<svg class="link-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24" data-testid="ContactSupportOutlinedIcon" tabindex="-1" title="ContactSupportOutlined"><path d="M15 4v7H5.17l-.59.59-.58.58V4h11m1-2H3c-.55 0-1 .45-1 1v14l4-4h10c.55 0 1-.45 1-1V3c0-.55-.45-1-1-1zm5 4h-2v9H6v2c0 .55.45 1 1 1h11l4 4V7c0-.55-.45-1-1-1z"></path></svg>';
                        contactLink += ' Click here to contact';
                        contactLink += '</a> ';
                        contactLink += orgName;
                        contactLink += "</div>";
                        $('#requestDetails').html(contactLink);
                    });

                    var installationsUrl = "https://api.gbif.org/v1/organization/" + organisationKey + "/installation";

                    // for test env (UAT)
                    if ("${cfg.registryUrl!}".indexOf("gbif-uat") !== -1) {
                        installationsUrl = "https://api.gbif-uat.org/v1/organization/" + organisationKey + "/installation";
                    }

                    $.getJSON(installationsUrl, function (data) {
                        var numberOfIptInstallations = 0;

                        for (var i in data.results) {
                            if (data.results[i].type === "IPT_INSTALLATION") {
                                numberOfIptInstallations++;
                            }
                        }

                        if (numberOfIptInstallations > 0) {
                            // do not remove, will be substituted in installationsWarningText
                            var organizationPortalLink = "<a href=\"${cfg.portalUrl!}/publisher/" + organisationKey + "\">" + orgName + "</a>";
                            var installationsWarningText = `<@s.text name="admin.registration.duplicate.warning"/>`;
                            var installationsCallout = $("#installations-warning");
                            installationsCallout.html(installationsWarningText);
                            installationsCallout.show();
                        }
                    });
                } else {
                    // remove link
                    $("#requestDetails").empty();
                    // erase installations warning
                    var installationsCallout = $("#installations-warning");
                    installationsCallout.text("");
                    installationsCallout.hide();
                }
            });

            <#if validatedBaseURL>
            $('#registrationFormDiv').show();
            </#if>
            $('#validate').click(function () {
                $("#validation-in-process").show();
                $("#validate").hide();
                var url = "https://tools.gbif.org/ws-validurl/?url=${baseURL}/&callback=?";
                $.getJSON(url, function (data) {
                    $("#validation-in-process").hide();
                    $("#validate").show();
                    if (data.success === true) {
                        $("#validation-success").show();
                        $('#registrationFormDiv').show(500);
                        $('#save').show();
                    } else {
                        <#if cfg.registryType=="DEVELOPMENT">
                        $("#validation-failed-development").show();
                        $('#registrationFormDiv').show(500);
                        $('#save').show();
                        <#else>
                        $("#validation-failed").show();
                        </#if>
                    }
                });

            });

            $("#update").on("click", displayProcessing);

            $(".registration-tab-root").click(function (event) {
                var selectedTab = $(this);
                var selectedTabId = selectedTab[0].id;

                // remove "selected" from all tabs
                $(".registration-tab-root").removeClass("tab-selected");
                // hide all indicators
                $(".tabs-indicator").hide();
                // add "selected" to clicked tab
                selectedTab.addClass("tab-selected");
                // show indicator for this tab
                $("#" + selectedTabId + " .tabs-indicator").show();

                if (selectedTabId === 'tab-tokens') {
                    displayChangeTokensView();
                } else if (selectedTabId === "tab-network") {
                    displayNetworkView();
                } else {
                    displayEditRegistrationView();
                }
            });

            $('select#networkKey').select2({
                placeholder: '${action.getText("admin.ipt.network.selection")?js_string}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                minimumResultsForSearch: 15,
                allowClear: true,
                width: "100%",
                theme: 'bootstrap4'
            });

            $("#network").on("click", displayProcessing);
        });
    </script>
    <title><@s.text name="title"/></title>
    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/macros/forms.ftl">
    <#include "/WEB-INF/pages/inc/menu.ftl">

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container-fluid bg-body border-bottom">
        <div class="container bg-body border rounded-2 mb-4">
            <div class="container my-3 p-3">
                <div class="text-center">
                    <div class="fs-smaller">
                        <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                            <ol class="breadcrumb justify-content-center mb-0">
                                <li class="breadcrumb-item"><a href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                                <li class="breadcrumb-item"><@s.text name="breadcrumb.admin.registration"/></li>
                            </ol>
                        </nav>
                    </div>

                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                        <@s.text name="admin.home.editRegistration"/>
                    </h1>

                    <div class="mt-2">
                        <#if hostingOrganisation?has_content>
                            <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" form="registration" name="update" id="update" key="button.updateRegistration" />
                            <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" form="changeTokens" name="tokens" id="tokens" key="button.updateTokens" cssStyle="display: none;"/>
                            <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" form="networkForm" name="network" id="network" key="button.save" cssStyle="display: none;"/>
                            <a href="${baseURL}/admin/" class="button btn btn-sm btn-outline-secondary top-button">
                                <@s.text name="button.back"/>
                            </a>
                        <#else>
                            <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" cssStyle="display: none;" form="registrationForm" name="save" id="save" key="button.save"/>
                            <a href="${baseURL}/admin/" class="button btn btn-sm btn-outline-secondary top-button">
                                <@s.text name="button.back"/>
                            </a>
                        </#if>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <main class="container main-content-container">
        <div class="my-3 p-3">
            <#-- If the hosting institution already exists, this IP has been registered. Don't present the register form -->
            <#if hostingOrganisation?has_content>
                <p>
                    <@s.text name="admin.registration.registered1"><@s.param><a href="${cfg.portalUrl}/installation/${registeredIpt.key}" target="_blank">IPT</a></@s.param></@s.text>
                    <#-- in prod mode link goes to /publisher (GBIF Portal), in dev mode link goes to /publisher (GBIF UAT Portal) -->
                    <@s.text name="admin.registration.registered2"><@s.param><a href="${cfg.portalUrl}/publisher/${hostingOrganisation.key}" target="_blank">${hostingOrganisation.name!"Organisation"}</a></@s.param></@s.text>
                </p>

                <div class="py-3">
                    <div class="tabs-root">
                        <div class="tabs-scroller tabs-fixed" style="overflow:hidden;margin-bottom:0">
                            <div class="tabs-flexContainer justify-content-start" role="tablist">
                                <button id="tab-registration" class="registration-tab-root tab-selected" type="button" role="tab">
                                    <@s.text name="admin.ipt.registration"/>
                                    <span id="tab-indicator-registration" class="tabs-indicator"></span>
                                </button>
                                <button id="tab-network" class="registration-tab-root" type="button" role="tab">
                                    <@s.text name="admin.ipt.network"/>
                                    <span id="tab-indicator-network" class="tabs-indicator" style="display: none;"></span>
                                </button>
                                <button id="tab-tokens" class="registration-tab-root" type="button" role="tab">
                                    <@s.text name="admin.ipt.tokens.title"/>
                                    <span id="tab-indicator-tokens" class="tabs-indicator" style="display: none;"></span>
                                </button>
                            </div>

                        </div>
                    </div>
                </div>

                <div id="registration-block" class="py-3">
                    <#-- If the hosting institution already exists, this IP has been registered. Don't present the register form -->
                    <form id="registration" class="needs-validation" action="updateRegistration" method="post" novalidate>
                        <div class="row g-3">
                            <div class="col-lg-6">
                                <@input name="registeredIpt.name" i18nkey="admin.ipt.name" type="text" requiredField=true />
                            </div>

                            <div class="col-12">
                                <@text name="registeredIpt.description" i18nkey="admin.ipt.description" requiredField=true />
                            </div>

                            <#-- For future release. Will replace contact name below
                            <@input name="registeredIpt.primaryContactFirstName" i18nkey="admin.ipt.primaryContactFirstName" type="text" />
                            <@input name="registeredIpt.primaryContactLastName" i18nkey="admin.ipt.primaryContactLastName" type="text" />
                            -->

                            <div class="col-lg-6">
                                <@input name="registeredIpt.primaryContactName" i18nkey="admin.ipt.primaryContactName" type="text" requiredField=true />
                            </div>

                            <div class="col-lg-6">
                                <@input name="registeredIpt.primaryContactEmail" i18nkey="admin.ipt.primaryContactEmail" type="text" requiredField=true />
                            </div>
                        </div>
                    </form>
                </div>

                <div id="network-block" style="display: none;" class="py-3">
                    <p class="mb-3 pb-3">
                        <@s.text name="admin.ipt.network.intro"/>
                    </p>

                    <form id="networkForm" class="needs-validation" action="associateWithNetwork.do" method="post" novalidate>
                        <@s.hidden id="associateNetwork" name="associateNetwork" value="true" />

                        <div class="row g-3">
                            <div class="col-lg-6">
                                <@select name="networkKey" options=networks i18nkey="admin.ipt.network" value="${(network.key)!}" />
                            </div>

                            <div class="col-12">
                                <@checkbox name="applyToExistingResources" i18nkey="admin.ipt.network.applyToExisting" value="false"/>
                            </div>
                        </div>
                    </form>
                </div>

                <div id="tokens-block" style="display: none;" class="py-3">
                    <p class="mb-3 pb-3">
                        <@s.text name="admin.ipt.tokens.intro"/>
                    </p>

                    <form id="changeTokens" class="needs-validation" action="changeTokens" method="post" novalidate>
                        <@s.hidden id="tokenChange" name="tokenChange" value="true" />

                        <div class="row g-3">
                            <div class="col-lg-6">
                                <@input name="hostingOrganisationToken" i18nkey="admin.organisation.password" type="password" help="i18n" maxlength=15 size=18 />
                            </div>

                            <div class="col-lg-6">
                                <@input name="registeredIptPassword" i18nkey="admin.ipt.password" type="password" help="i18n" maxlength=15 size=18 />
                            </div>
                        </div>
                    </form>
                </div>
            <#else>
                <#-- BASE URL has not been validated, disable the form -->
                <#if !validatedBaseURL>
                    <p><@s.text name="admin.registration.test1"/></p>

                    <div class="row g-3">
                        <div class="col-md-6">
                            <@input name="registration.baseURL" i18nkey="admin.registration.baseURL" type="text" value="${baseURL}" size=70 disabled=true requiredField=true/>
                        </div>

                        <div class="col-12">
                            <@s.submit cssClass="button btn btn-outline-gbif-primary" name="validate" id="validate" key="admin.registration.validate"/>

                            <button id="validation-in-process" name="validate" class="btn btn-outline-gbif-primary" type="submit" style="display: none" disabled>
                                <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                                <@s.text name="admin.registration.validate"/>
                            </button>
                        </div>

                        <div class="col-12">
                            <div id="validation-success" class="callout callout-info" style="display: none;">
                                <@s.text name="admin.registration.validate.success"/>
                            </div>

                            <div id="validation-failed-development" class="callout callout-danger" style="display: none;">
                                <@s.text name="admin.registration.validate.failed.development"/>
                            </div>

                            <div id="validation-failed" class="callout callout-danger" style="display: none;">
                                <@s.text name="admin.registration.validate.failed"/>
                            </div>
                        </div>
                    </div>
                </#if>

                <div id="registrationFormDiv" class="mt-4" style="display: none;" >

                    <form id="registrationForm" class="needs-validation" action="registration.do" method="post" novalidate>
                        <div class="row g-3">
                            <div class="col-lg-4">
                                <div class="form-group">
                                    <#assign selectOrganisationInfo>
                                        <@s.text name="admin.registration.intro"/>&nbsp;<@s.text name="admin.registration.intro2"/>
                                    </#assign>
                                    <div class="d-flex text-smaller">
                                        <a tabindex="0" role="button"
                                           class="popover-link"
                                           data-bs-toggle="popover"
                                           data-bs-trigger="focus"
                                           data-bs-html="true"
                                           data-bs-content="${selectOrganisationInfo}">
                                            <i class="bi bi-info-circle text-gbif-primary"></i>
                                        </a>&nbsp;
                                        <label for="organisation.key" class="form-label">
                                            <@s.text name="admin.organisation.key"/> <span class="text-gbif-danger">&#42;</span>
                                        </label>
                                    </div>
                                    <@s.select cssClass="form-select" id="organisation.key" name="organisation.key" list="organisations" listKey="key" listValue="name" value="organisation.key" size="15" disabled="false"/>
                                    <@s.fielderror id="field-error-organisation.key" cssClass="invalid-feedback list-unstyled field-error my-1" fieldName="organisation.key"/>
                                </div>
                                <div id="requestDetails" class="mt-0 text-smaller"></div>
                            </div>

                            <div class="col-lg-4">
                                <@input name="organisation.password" i18nkey="admin.organisation.password" type="password" help="i18n" maxlength=15 size=18 requiredField=true />
                            </div>

                            <div class="col-lg-4">
                                <@input name="organisation.alias" i18nkey="admin.organisation.alias" type="text" />
                            </div>

                            <div class="col-12">
                                <div id="installations-warning" class="callout callout-warning" style="display: none;"></div>
                            </div>

                            <div class="col-12">
                                <@checkbox name="organisation.canHost" i18nkey="admin.organisation.canPublish" value="true" help="i18n"/>
                            </div>

                            <@s.hidden id="organisation.primaryContactType" name="organisation.primaryContactType" />
                            <@s.hidden id="organisation.primaryContactName" name="organisation.primaryContactName" />
                            <@s.hidden id="organisation.primaryContactEmail" name="organisation.primaryContactEmail" />
                            <@s.hidden id="organisation.nodeKey" name="organisation.nodeKey" />
                            <@s.hidden id="organisation.nodeName" name="organisation.nodeName" />

                            <div class="col-lg-6">
                                <@input name="ipt.name" i18nkey="admin.ipt.name" type="text" maxlength=255 size=150 requiredField=true />
                            </div>

                            <div class="col-lg-6">
                                <@input name="ipt.wsPassword" i18nkey="admin.ipt.password" type="password" help="i18n" maxlength=15 size=18 requiredField=true />
                            </div>

                            <div class="col-12">
                                <@text name="ipt.description" i18nkey="admin.ipt.description" requiredField=true />
                            </div>

                            <#-- For future release. Will replace contact name below
                            <@input name="ipt.primaryContactFirstName" i18nkey="admin.ipt.primaryContactFirstName" type="text" />
                            <@input name="ipt.primaryContactLastName" i18nkey="admin.ipt.primaryContactLastName" type="text" />
                            -->
                            <div class="col-lg-6">
                                <@input name="ipt.primaryContactName" i18nkey="admin.ipt.primaryContactName" type="text" maxlength=255 requiredField=true />
                            </div>

                            <div class="col-lg-6">
                                <@input name="ipt.primaryContactEmail" i18nkey="admin.ipt.primaryContactEmail" type="text" maxlength=254 requiredField=true />
                            </div>

                            <@s.hidden id="admin.ipt.primaryContactType" name="ipt.primaryContactType" value="technical"/>
                            <@s.hidden id="organisation.name" name="organisation.name" />
                            <@s.hidden id="ipt.organisationKey" name="ipt.organisationKey" />
                        </div>
                    </form>
                </div>
            </#if>
        </div>
    </main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
