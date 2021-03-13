<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header-bootstrap.ftl">
    <script type="text/javascript" src="${baseURL}/js/jquery/jquery-3.2.1.min.js"></script>
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-3.5.1.css">
    <script src="${baseURL}/js/select2/select2-3.5.1.min.js" type="text/javascript"></script>
    <script type="text/javascript">
        $(document).ready(function() {
            $('#organisation\\.key').select2({placeholder: '<@s.text name="admin.organisation.name.select"/>', width:"375px", allowClear: true});
        });
    </script>
    <script type="text/javascript">
        $(document).ready(function(){
            initHelp();

            $('#organisation\\.key').change(function() {

                var orgName = $('#organisation\\.key :selected').text();
                $('#organisation\\.name').val(orgName);
                $('#ipt\\.organisationKey').val($('#organisation\\.key :selected').val());

                var emailContent = '<@s.text name="emails.request.organisation.association1"/>';
                emailContent += '<@s.text name="emails.request.organisation.association2"/>';
                emailContent += '<@s.text name="emails.request.organisation.association3"/>';
                emailContent += '<@s.text name="emails.request.organisation.association4"/>';
                emailContent += '<@s.text name="emails.request.organisation.association5"/>';
                emailContent += '<@s.text name="emails.request.organisation.association6"><@s.param>';
                emailContent += $("#organisation\\.key :selected").val();
                emailContent += '</@s.param></@s.text>';
                emailContent += '<@s.text name="emails.request.organisation.association7"/>';

                $('#organisation\\.alias').val(orgName);
                var url = "<@s.url value='${registryURL}organisation/'/>" + $('#organisation\\.key :selected').val() + ".json";
                $.getJSON(url+"?callback=?",function(data){

                    $('#organisation\\.primaryContactType').val(data.primaryContactType);
                    $('#organisation\\.primaryContactName').val(data.primaryContactName);
                    $('#organisation\\.primaryContactEmail').val(data.primaryContactEmail);
                    $('#organisation\\.nodeKey').val(data.nodeKey);
                    $('#organisation\\.nodeName').val(data.nodeName);

                    //Create a contact link to prefill an email to request a password from an Organisation
                    var contactLink = '<a href=\"mailto:';
                    contactLink += data.primaryContactEmail;
                    contactLink += '?subject=';
                    contactLink += '<@s.text name="emails.request.ipt.registration.subject"><@s.param>';
                    contactLink += orgName;
                    contactLink += '</@s.param></@s.text>';
                    contactLink += '&body=';
                    contactLink += emailContent;
                    contactLink += '\">';
                    contactLink += '<@s.text name="emails.request.ipt.registration.footer"/>';
                    contactLink += '</a> ';
                    contactLink += orgName;
                    $('#requestDetails').html(contactLink);
                });
            });

            <#if validatedBaseURL>
            $('#registrationForm').show();
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
                        $('#registrationForm').show(500);
                    } else {
                        <#if cfg.registryType=="DEVELOPMENT">
                        $("#validation-failed-development").show();
                        $('#registrationForm').show(500);
                        <#else>
                        $("#validation-failed").show();
                        </#if>
                    }
                });

            });
        });
    </script>
    <title><@s.text name="title"/></title>
    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/macros/forms-bootstrap.ftl">
    <#include "/WEB-INF/pages/inc/menu-bootstrap.ftl">

<main class="container pt-5">
    <div class="row g-3">
        <div class="my-3 p-3 bg-body rounded shadow-sm">
            <#include "/WEB-INF/pages/inc/action_alerts-bootstrap.ftl">

            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-success text-center">
                <@s.text name="admin.home.editRegistration"/>
            </h5>

            <#-- If the hosting institution already exists, this IP has been registered. Don't present the register form -->
            <#if hostingOrganisation?has_content>
                <p>
                    <@s.text name="admin.registration.registered1"/><br />
                    <@s.text name="admin.registration.registered2"><@s.param>${hostingOrganisation.name!"???"}</@s.param></@s.text>
                </p>

                <form id="registration" class="topForm half" action="updateRegistration" method="post">
                    <h3 class="subTitle"><@s.text name="admin.registration.links"/></h3>

                    <ul>
                        <li><a href="${cfg.portalUrl}/installation/${registeredIpt.key}" target="_blank">${registeredIpt.name!"IPT"}</a></li>
                        <#-- in prod mode link goes to /publisher (GBIF Portal), in dev mode link goes to /publisher (GBIF UAT Portal) -->
                        <li><a href="${cfg.portalUrl}/publisher/${hostingOrganisation.key}" target="_blank">${hostingOrganisation.name!"Organisation"}</a></li>
                    </ul>

                    <@input name="registeredIpt.name" i18nkey="admin.ipt.name" type="text" requiredField=true />
                    <@text name="registeredIpt.description" i18nkey="admin.ipt.description" requiredField=true />

                    <#-- For future release. Will replace contact name below
                    <@input name="registeredIpt.primaryContactFirstName" i18nkey="admin.ipt.primaryContactFirstName" type="text" />
                    <@input name="registeredIpt.primaryContactLastName" i18nkey="admin.ipt.primaryContactLastName" type="text" />
                    -->

                    <@input name="registeredIpt.primaryContactName" i18nkey="admin.ipt.primaryContactName" type="text" requiredField=true />
                    <@input name="registeredIpt.primaryContactEmail" i18nkey="admin.ipt.primaryContactEmail" type="text" requiredField=true />
                    <div class="buttons">
                        <@s.submit cssClass="button" name="update" id="update" key="button.updateRegistration" />
                        <@s.submit cssClass="button" name="cancel" id="cancel" key="button.cancel"/>
                    </div>
                </form>
            <#else>

            <#-- BASE URL has not been validated, disable the form -->
                <#if !validatedBaseURL>

                    <p class="text-muted mx-md-4 mx-2"><@s.text name="admin.registration.test1"/></p>

                    <div class="row g-3 mx-md-3 mx-1">
                        <div class="col-12">
                            <@input name="registration.baseURL" i18nkey="admin.registration.baseURL" type="text" value="${baseURL}" size=70 disabled=true requiredField=true/>
                        </div>

                        <div class="col-12">
                            <@s.submit cssClass="button btn btn-outline-success" name="validate" id="validate" key="admin.registration.validate"/>

                            <button id="validation-in-process" name="validate" class="btn btn-outline-success" type="submit" style="display: none" disabled>
                                <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                                <@s.text name="admin.registration.validate"/>
                            </button>
                        </div>

                        <div class="col-12">
                            <div id="validation-success" class="text-muted" style="display: none">
                                <@s.text name="admin.registration.validate.success"/>
                            </div>

                            <div id="validation-failed-development" class="text-muted" style="display: none">
                                <@s.text name="admin.registration.validate.failed.development"/>
                            </div>

                            <div id="validation-failed" class="text-muted" style="display: none">
                                <@s.text name="admin.registration.validate.failed"/>
                            </div>
                        </div>
                    </div>
                </#if>

                <div id="registrationForm" class="mx-md-4 mx-2 mt-4" style="display: none;" >

                    <@s.form cssClass="topForm half" action="registration" method="post" id="registrationForm" namespace="" includeContext="false">
                        <div class="row g-3">
                            <div class="col-lg-6">
                                <@s.fielderror>
                                    <@s.param value="%{'organisation.key'}" />
                                </@s.fielderror>
                                <img class="infoImg" src="${baseURL}/images/info.gif">
                                <div class="info" style="display: none;"><@s.text name="admin.registration.intro"/>&nbsp;<@s.text name="admin.registration.intro2"/></div>
                                <@s.select cssClass="form-select" id="organisation.key" name="organisation.key" list="organisations" listKey="key" listValue="name" value="organisation.key" size="15" disabled="false"/>
                            </div>

                            <div class="col-lg-6">
                                <@input name="organisation.password" i18nkey="admin.organisation.password" type="password" help="i18n" maxlength=15 size=18 requiredField=true />
                            </div>

                            <div id="requestDetails"></div>

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

                            <div class="buttons col-12">
                                <@s.submit cssClass="button btn btn-outline-success" name="save" id="save" key="button.save"/>
                                <@s.submit cssClass="button btn btn-outline-secondary" name="cancel" id="cancel" key="button.cancel"/>
                            </div>
                        </div>
                    </@s.form>
                </div>
            </#if>
        </div>
    </div>
</main>

    <#include "/WEB-INF/pages/inc/footer-bootstrap.ftl">
</#escape>
