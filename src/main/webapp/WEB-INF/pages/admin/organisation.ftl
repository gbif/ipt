<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header-bootstrap.ftl">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/select2@4.0.13/dist/css/select2.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@ttskch/select2-bootstrap4-theme@x.x.x/dist/select2-bootstrap4.min.css">
    <script src="https://cdn.jsdelivr.net/npm/select2@4.0.13/dist/js/select2.min.js" type="text/javascript"></script>

    <script type="text/javascript" src="${baseURL}/js/jconfirmation-bootstrap.jquery.js"></script>
    <script type="text/javascript">
        $(document).ready(function(){
            initHelp();

            $('.confirm').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});

            $('select#organisation\\.key').select2({placeholder: '<@s.text name="admin.organisation.name.select"/>', width:"100%", allowClear: true, theme: 'bootstrap4'});

            $('#organisation\\.key').change(function() {

                var orgName = $('#organisation\\.key :selected').text();
                $('#organisation\\.name').val(orgName);
                $('#organisation\\.alias').val(orgName);

                var emailContent = '<@s.text name="emails.request.organisation.association1"/>';
                emailContent += '<@s.text name="emails.request.organisation.association2"/>';
                emailContent += '<@s.text name="emails.request.organisation.association3"/>';
                emailContent += '<@s.text name="emails.request.organisation.association4"/>';
                emailContent += '<@s.text name="emails.request.organisation.association5"/>';
                emailContent += '<@s.text name="emails.request.organisation.association6"><@s.param>';
                emailContent += $("#organisation\\.key :selected").val();
                emailContent += '</@s.param></@s.text>';
                emailContent += '<@s.text name="emails.request.organisation.association7"/>';

                var url = '${registryURL}organisation/' + $('#organisation\\.key :selected').val() + ".json";
                $.getJSON(url,function(data){

                    $('#organisation\\.primaryContactType').val(data.primaryContactType);
                    $('#organisation\\.primaryContactName').val(data.primaryContactName);
                    $('#organisation\\.primaryContactEmail').val(data.primaryContactEmail);
                    $('#organisation\\.nodeKey').val(data.nodeKey);
                    $('#organisation\\.nodeName').val(data.nodeName);

                    //Create a contact link to prefill an email to request a password from an Organisation
                    var contactLink = '<a href=\"mailto:';
                    contactLink += data.primaryContactEmail;
                    contactLink += '?subject=';
                    contactLink += '<@s.text name="emails.request.organisation.association.subject"><@s.param>';
                    contactLink += orgName;
                    contactLink += '</@s.param></@s.text>';
                    contactLink += '&body=';
                    contactLink += emailContent;
                    contactLink += '\">';
                    contactLink += '<@s.text name="emails.request.organisation.association.footer"/>';
                    contactLink += '</a> ';
                    contactLink += orgName;
                    $('#requestDetails').html(contactLink);
                });
            });
        });
    </script>
    <title><@s.text name="title"/></title>

    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/inc/menu-bootstrap.ftl">
    <#include "/WEB-INF/pages/macros/forms-bootstrap.ftl">
    <#include "/WEB-INF/pages/macros/popover-bootstrap.ftl">

    <main class="container">
        <div class="my-3 p-3 bg-body rounded shadow-sm">
            <#include "/WEB-INF/pages/inc/action_alerts-bootstrap.ftl">

            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-primary text-center">
                <#if id?has_content>
                    <@s.text name="admin.organisation.title"/>
                <#else>
                    <@s.text name="admin.organisation.add.title"/>
                </#if>
            </h5>

            <form id="organisationsForm" class="needs-validation" action="organisation.do" method="post" novalidate>
                <div class="row g-3 mx-md-3 mx-1">
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
                                    <@s.text name="admin.organisation.key"/>
                                    <span data-bs-container="body" data-bs-toggle="popover" data-bs-placement="top" data-bs-html="true" data-bs-content="${selectOrganisationInfo}">
                                        <i class="bi bi-info-circle text-gbif-primary"></i>
                                    </span>
                                </label>
                                <@s.select id="organisation.key" cssClass="form-select" name="organisation.key" list="organisations" listKey="key" listValue="name" value="organisation.key" disabled="false"/>
                                <@s.fielderror id="field-error-organisation.key" cssClass="invalid-feedback list-unstyled field-error my-1" fieldName="organisation.key"/>
                            </div>
                        </div>
                    </#if>

                    <div class="col-lg-6">
                        <@input name="organisation.password" i18nkey="admin.organisation.password" type="password"/>
                    </div>

                    <div id="requestDetails"></div>

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

                    <div class="col-12">
                        <p>
                            <@popoverPropertyInfo "admin.organisation.doiRegistrationAgency.help"/>
                            <@s.text name="admin.organisation.doiRegistrationAgency"/>
                        </p>

                        <#list doiRegistrationAgencies as agency>
                            <div class="form-check form-check-inline">
                                <input class="form-check-input" type="radio" name="organisation.doiRegistrationAgency" id="organisation_doiRegistrationAgency${agency}" <#if agency??>value="${agency}"</#if> <#if organisation.doiRegistrationAgency?? && agency == organisation.doiRegistrationAgency> checked </#if> >
                                <label class="form-check-label" for="organisation_doiRegistrationAgency${agency}" >
                                    ${agency}
                                </label>
                            </div>
                        </#list>
                        <@s.fielderror cssClass="invalid-feedback list-unstyled radio-error radio-name-organisation.doiRegistrationAgency mx-md-4 mx-2 my-1">
                            <@s.param value="%{'organisation.doiRegistrationAgency'}" />
                        </@s.fielderror>
                    </div>

                    <div class="col-lg-6">
                        <@input name="organisation.agencyAccountUsername" i18nkey="admin.organisation.doiRegistrationAgency.username" help="i18n" type="text"/>
                    </div>

                    <div class="col-lg-6">
                        <@input name="organisation.agencyAccountPassword" i18nkey="admin.organisation.doiRegistrationAgency.password" help="i18n" type="password"/>
                    </div>

                    <div class="col-lg-6">
                        <@input name="organisation.doiPrefix" i18nkey="admin.organisation.doiRegistrationAgency.prefix" help="i18n" type="text"/>
                    </div>

                    <div class="col-12">
                        <@checkbox name="organisation.agencyAccountPrimary" i18nkey="admin.organisation.doiAccount.activated" value="${organisation.agencyAccountPrimary?c}" help="i18n"/>
                    </div>

                    <div class="col-12">
                        <@s.submit name="save" key="button.save" cssClass="button btn btn-outline-success"/>
                        <#if id?has_content>
                            <@s.submit name="delete" key="button.delete" cssClass="button confirm btn btn-outline-danger"/>
                        </#if>
                        <@s.submit name="cancel" key="button.cancel" cssClass="button btn btn-outline-secondary"/>
                    </div>
                </div>
            </form>
        </div>
    </main>

    <#include "/WEB-INF/pages/inc/footer-bootstrap.ftl">
</#escape>
