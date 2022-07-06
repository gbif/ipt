<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
    <script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>

    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <script>
        $(document).ready(function(){
            $('.confirm').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>", buttonType: "danger"});

            $('select#organisation\\.key').select2({placeholder: '<@s.text name="admin.organisation.name.select"/>', width:"100%", allowClear: true, theme: 'bootstrap4'});

            $('#organisation\\.key').change(function() {

                var organisationSelected = $('#organisation\\.key :selected');
                var orgName = organisationSelected.text();
                var organisationKey = organisationSelected.val();
                $('#organisation\\.name').val(orgName);
                $('#organisation\\.alias').val(orgName);

                var emailContent = 'Dear sir/madam,%0d%0d';
                emailContent += 'I am installing an Integrated Publishing Toolkit (IPT).%0d';
                emailContent += 'This tool allows me to create resources and assign these resources to organizations. %0d';
                emailContent += 'I would like to link your organization with my IPT, but for this I will need you to provide me with your organization\'s shared token%0d';
                emailContent += 'In case you don\'t know this information, you can open the following link in your browser to receive this information%0d%0d';
                emailContent += 'https://gbrds.gbif.org/registry/organisation/' + organisationKey + '?op=password%0d%0d';
                emailContent += 'Thank you for your attention.';

                if (organisationKey) {
                    var url = '${registryURL}organisation/' + $('#organisation\\.key :selected').val() + ".json";
                    $.getJSON(url, function (data) {

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

            var doiRegistrationAgency = $('organisation\\.doiRegistrationAgency :selected').val();
            if (doiRegistrationAgency) {
                $(".doiAgencyField").css("display", "");
            }

            $('#organisation\\.doiRegistrationAgency').change(function() {
                var doiRegistrationAgency = $('#organisation\\.doiRegistrationAgency :selected').val();

                if (doiRegistrationAgency) {
                    $(".doiAgencyField").css("display", "");
                } else {
                    $("#organisation\\.agencyAccountUsername").val('');
                    $("#organisation\\.agencyAccountPassword").val('');
                    $("#organisation\\.doiPrefix").val('');
                    $('#organisation\\.agencyAccountPrimary').attr('checked', false);
                    $(".doiAgencyField").css("display", "none");
                }
            });
        });
    </script>
    <title><@s.text name="title"/></title>

    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl">
    <#include "/WEB-INF/pages/macros/popover.ftl">

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
                    <#if id?has_content>
                        <@s.text name="admin.organisation.title"/>
                    <#else>
                        <@s.text name="admin.organisation.add.title"/>
                    </#if>
                </h1>

                <div class="mt-2">
                    <@s.submit name="save" form="organisationsForm" key="button.save" cssClass="button btn btn-sm btn-outline-gbif-primary top-button"/>
                    <#if id?has_content>
                        <@s.submit name="delete" form="organisationsForm" key="button.delete" cssClass="button confirm btn btn-sm btn-outline-gbif-danger top-button"/>
                    </#if>
                    <@s.submit name="cancel" form="organisationsForm" key="button.cancel" cssClass="button btn btn-sm btn-outline-secondary top-button"/>
                </div>
            </div>
        </div>
    </div>

    <main class="container">
        <div class="my-3 p-3">
            <form id="organisationsForm" class="needs-validation" action="organisation.do" method="post" novalidate>
                <div class="row g-3">
                    <#if id?has_content>
                        <div class="col-lg-6">
                            <@input name="organisation.name" i18nkey="admin.organisation.name" type="text" disabled=true/>
                        </div>

                        <@s.hidden name="organisation.key" id="organisation.key" required="true" />
                        <@s.hidden name="id" id="id" required="true" />
                        <!-- preserve other fields not edited -->
                        <@s.hidden name="organisation.nodeName" id="organisation.nodeName" />
                        <@s.hidden name="organisation.nodeKey" id="organisation.nodeKey" />
                        <@s.hidden name="organisation.primaryContactPhone" id="organisation.primaryContactPhone" />
                        <@s.hidden name="organisation.primaryContactEmail" id="organisation.primaryContactEmail" />
                        <@s.hidden name="organisation.primaryContactAddress" id="organisation.primaryContactAddress" />
                        <@s.hidden name="organisation.primaryContactDescription" id="organisation.primaryContactDescription" />
                        <@s.hidden name="organisation.primaryContactName" id="organisation.primaryContactName" />
                        <@s.hidden name="organisation.primaryContactType" id="organisation.primaryContactType" />
                        <@s.hidden name="organisation.homepageURL" id="organisation.homepageURL" />
                        <@s.hidden name="organisation.description" id="organisation.description" />
                    <#else>
                        <@s.hidden id="organisation.name" name="organisation.name" required="true" />
                        <!-- preserve other fields not edited -->
                        <@s.hidden name="organisation.nodeName" id="organisation.nodeName" />
                        <@s.hidden name="organisation.nodeKey" id="organisation.nodeKey" />
                        <@s.hidden name="organisation.primaryContactPhone" id="organisation.primaryContactPhone" />
                        <@s.hidden name="organisation.primaryContactEmail" id="organisation.primaryContactEmail" />
                        <@s.hidden name="organisation.primaryContactAddress" id="organisation.primaryContactAddress" />
                        <@s.hidden name="organisation.primaryContactDescription" id="organisation.primaryContactDescription" />
                        <@s.hidden name="organisation.primaryContactName" id="organisation.primaryContactName" />
                        <@s.hidden name="organisation.primaryContactType" id="organisation.primaryContactType" />
                        <@s.hidden name="organisation.homepageURL" id="organisation.homepageURL" />
                        <@s.hidden name="organisation.description" id="organisation.description" />

                        <div class="col-lg-6">
                            <div class="form-group">
                                <#assign selectOrganisationInfo>
                                    <@s.text name="admin.registration.intro"/>&nbsp;<@s.text name="admin.organisation.add.intro2"/>
                                </#assign>
                                <label for="organisation.key" class="form-label">
                                    <@s.text name="admin.organisation.key"/> &#42;
                                </label>
                                <a tabindex="0" role="button"
                                   class="popover-link"
                                   data-bs-toggle="popover"
                                   data-bs-trigger="focus"
                                   data-bs-html="true"
                                   data-bs-content="${selectOrganisationInfo}">
                                    <i class="bi bi-info-circle text-gbif-primary"></i>
                                </a>
                                <@s.select id="organisation.key" cssClass="form-select" name="organisation.key" list="organisations" listKey="key" listValue="name" value="organisation.key" disabled="false"/>
                                <@s.fielderror id="field-error-organisation.key" cssClass="invalid-feedback list-unstyled field-error my-1" fieldName="organisation.key"/>
                            </div>
                            <div id="requestDetails" class="mt-0"></div>
                        </div>
                    </#if>

                    <div class="col-lg-6">
                        <@input name="organisation.password" i18nkey="admin.organisation.password" type="password" requiredField=true />
                    </div>

                    <div class="col-lg-6">
                        <@input name="organisation.alias" i18nkey="admin.organisation.alias" type="text"/>
                    </div>

                    <div class="col-12">
                        <#if id?has_content>
                            <@checkbox name="organisation.canHost" i18nkey="admin.organisation.canPublish" value="organisation.canHost" help="i18n"/>
                        <#else>
                            <@checkbox name="organisation.canHost" i18nkey="admin.organisation.canPublish" value="true" help="i18n"/>
                        </#if>
                    </div>

                    <div class="col-lg-6">
                        <@select name="organisation.doiRegistrationAgency" value="${organisation.doiRegistrationAgency!''}" options=doiRegistrationAgencies help="i18n" i18nkey="admin.organisation.doiRegistrationAgency" includeEmpty=true />
                    </div>

                    <div class="col-lg-6 doiAgencyField" <#if !organisation.doiRegistrationAgency??>style="display: none;"</#if> >
                        <@input name="organisation.agencyAccountUsername" i18nkey="admin.organisation.doiRegistrationAgency.username" help="i18n" type="text"/>
                    </div>

                    <div class="col-lg-6 doiAgencyField" <#if !organisation.doiRegistrationAgency??>style="display: none;"</#if> >
                        <@input name="organisation.agencyAccountPassword" i18nkey="admin.organisation.doiRegistrationAgency.password" help="i18n" type="password"/>
                    </div>

                    <div class="col-lg-6 doiAgencyField" <#if !organisation.doiRegistrationAgency??>style="display: none;"</#if> >
                        <@input name="organisation.doiPrefix" i18nkey="admin.organisation.doiRegistrationAgency.prefix" help="i18n" type="text"/>
                    </div>

                    <div class="col-12 doiAgencyField" <#if !organisation.doiRegistrationAgency??>style="display: none;"</#if> >
                        <@checkbox name="organisation.agencyAccountPrimary" i18nkey="admin.organisation.doiAccount.activated" value="${organisation.agencyAccountPrimary?c}" help="i18n"/>
                    </div>
                </div>
            </form>
        </div>
    </main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
