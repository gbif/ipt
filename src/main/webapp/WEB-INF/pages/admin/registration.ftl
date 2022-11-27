<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <script src="${baseURL}/js/jquery/jquery-3.5.1.min.js"></script>
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
    <script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>

    <script>
        $(document).ready(function() {
            $('#organisation\\.key').select2({placeholder: '<@s.text name="admin.organisation.name.select"/>', width: "100%", allowClear: true, theme: 'bootstrap4'});
        });
    </script>
    <script>
        $(document).ready(function(){
            $("#edit-registration-radio").change(function () {
                if ($('#edit-registration-radio').is(':checked')) {
                    displayEditRegistrationView();
                }
            });

            $("#change-tokens-radio").change(function () {
                if ($('#change-tokens-radio').is(':checked')) {
                    displayChangeTokensView();
                }
            });

            function displayChangeTokensView() {
                $('#tokens-block').show();
                $('#registration-block').hide();
                $('#tokens').show();
                $('#update').hide();
            }

            function displayEditRegistrationView() {
                $('#tokens-block').hide();
                $('#registration-block').show();
                $('#tokens').hide();
                $('#update').show();
            }

            if (window.location.href.indexOf("changeTokens") > -1) {
                displayChangeTokensView();
                $('#change-tokens-radio').prop("checked", true);
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

                    $.getJSON(url,function(data){
                        $('#organisation\\.primaryContactType').val(data.primaryContactType);
                        $('#organisation\\.primaryContactName').val(data.primaryContactName);
                        $('#organisation\\.primaryContactEmail').val(data.primaryContactEmail);
                        $('#organisation\\.nodeKey').val(data.nodeKey);
                        $('#organisation\\.nodeName').val(data.nodeName);

                        //Create a contact link to prefill an email to request a password from an Organisation
                        var contactLink = '<div class="mt-2"><a href=\"mailto:';
                        contactLink += data.primaryContactEmail;
                        contactLink += '?subject=';
                        contactLink += 'Shared token request for ';
                        contactLink += orgName;
                        contactLink += '&body=';
                        contactLink += emailContent;
                        contactLink += '\">';
                        contactLink += 'Click here to contact';
                        contactLink += '</a> ';
                        contactLink += orgName;
                        contactLink += "</div>";
                        $('#requestDetails').html(contactLink);
                    });
                } else {
                    // remove link
                    $("#requestDetails").empty();
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

            function displayProcessing() {
                var processingDiv = $(".dataTables_processing");
                processingDiv.show();
                processingDiv.css("z-index", "1001");
                var div= document.createElement("div");
                div.className += "overlay";
                document.body.appendChild(div);
            }

            $("#update").on("click", function () {
                displayProcessing();
            });
        });
    </script>
    <title><@s.text name="title"/></title>
    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/macros/forms.ftl">
    <#include "/WEB-INF/pages/inc/menu.ftl">

    <div class="container-fluid bg-body border-bottom">
        <div class="container my-3">
            <#include "/WEB-INF/pages/inc/action_alerts.ftl">
        </div>

        <div class="container my-3 p-3">
            <div class="text-center">
                <div class="text-uppercase fw-bold fs-smaller-2">
                    <span><@s.text name="menu.admin"/></span>
                </div>

                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    <@s.text name="admin.home.editRegistration"/>
                </h1>

                <div class="mt-2">
                    <#if hostingOrganisation?has_content>
                        <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" form="registration" name="update" id="update" key="button.updateRegistration" />
                        <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" form="changeTokens" name="tokens" id="tokens" key="button.updateTokens" cssStyle="display: none;"/>
                        <@s.submit cssClass="button btn btn-sm btn-outline-secondary top-button" form="registration" name="cancel" key="button.cancel"/>
                    <#else>
                        <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" cssStyle="display: none;" form="registrationForm" name="save" id="save" key="button.save"/>
                        <a href="${baseURL}" class="btn btn-sm btn-outline-secondary me-xl-1 top-button">
                            <@s.text name="button.cancel"/>
                        </a>
                    </#if>
                </div>
            </div>
        </div>
    </div>

    <main class="container">
        <div class="my-3 p-3">
            <#-- If the hosting institution already exists, this IP has been registered. Don't present the register form -->
            <#if hostingOrganisation?has_content>
                <p>
                    <@s.text name="admin.registration.registered1"><@s.param><a href="${cfg.portalUrl}/installation/${registeredIpt.key}" target="_blank">IPT</a></@s.param></@s.text>
                    <#-- in prod mode link goes to /publisher (GBIF Portal), in dev mode link goes to /publisher (GBIF UAT Portal) -->
                    <@s.text name="admin.registration.registered2"><@s.param><a href="${cfg.portalUrl}/publisher/${hostingOrganisation.key}" target="_blank">${hostingOrganisation.name!"Organisation"}</a></@s.param></@s.text>
                </p>

                <div class="py-3">
                    <div class="form-check form-check-inline">
                        <input class="form-check-input registration-options-radio" type="radio" name="registration-options-radio" id="edit-registration-radio" value="edit-registration" checked>
                        <label class="form-check-label" for="edit-profile-radio"><@s.text name="admin.ipt.registration"/></label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input class="form-check-input registration-options-radio" type="radio" name="registration-options-radio" id="change-tokens-radio" value="change-tokens">
                        <label class="form-check-label" for="change-password-radio"><@s.text name="admin.ipt.tokens.title"/></label>
                    </div>
                </div>

                <div id="registration-block" class="py-3">
                    <h4 class="pb-2 mb-3 pt-2 text-gbif-header-2 fs-5 fw-400">
                        <@s.text name="admin.ipt.registration"/>
                    </h4>
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

                <div id="tokens-block" style="display: none;" class="py-3">
                    <h4 class="pb-2 mb-3 pt-2 text-gbif-header-2 fs-5 fw-400">
                        <@s.text name="admin.ipt.tokens.title"/>
                    </h4>

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
                        <div class="col-12">
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
                            <div class="col-lg-6">
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
                                <div id="requestDetails" class="mt-0"></div>
                            </div>

                            <div class="col-lg-6">
                                <@input name="organisation.password" i18nkey="admin.organisation.password" type="password" help="i18n" maxlength=15 size=18 requiredField=true />
                            </div>

                            <div class="col-lg-6">
                                <@input name="organisation.alias" i18nkey="admin.organisation.alias" type="text" />
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

                            <div class="col-lg-6">
                                <@input name="ipt.wsPassword" i18nkey="admin.ipt.password" type="password" help="i18n" maxlength=15 size=18 requiredField=true />
                            </div>

                            <@s.hidden id="organisation.name" name="organisation.name" />
                            <@s.hidden id="ipt.organisationKey" name="ipt.organisationKey" />
                        </div>
                    </form>
                </div>
            </#if>
        </div>
    </main>

    <div class="dataTables_processing" style="display: none;">
        <div><div></div><div></div><div></div><div></div></div>
    </div>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
