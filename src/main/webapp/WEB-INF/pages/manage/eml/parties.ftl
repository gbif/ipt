<#escape x as x?html>
    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header-bootstrap.ftl">
    <title><@s.text name='manage.metadata.parties.title'/></title>
    <#include "/WEB-INF/pages/macros/metadata_agent.ftl"/>
    <script type="text/javascript">
        $(document).ready(function(){
            initHelp();
        });
    </script>
    <#assign auxTopNavbar=true />
    <#assign auxTopNavbarPage = "metadata" />
    <#assign currentMenu="manage"/>
    <#include "/WEB-INF/pages/inc/menu-bootstrap.ftl">
    <#include "/WEB-INF/pages/macros/forms-bootstrap.ftl"/>

<main class="container">

    <form class="needs-validation" action="metadata-${section}.do" method="post" novalidate>
        <div class="row g-3">
            <div class="p-3 bg-body rounded shadow-sm">

                <#include "/WEB-INF/pages/inc/action_alerts-bootstrap.ftl">

                <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-success text-center">
                    <@s.text name='manage.metadata.parties.title'/>:
                    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                </h5>

                <p class="text-muted mx-md-4 mx-2 mb-0">
                    <@s.text name='manage.metadata.parties.intro'/>
                </p>

                <!-- retrieve some link names one time -->
                <#assign copyLink><@s.text name="eml.resourceCreator.copyLink"/></#assign>
                <#assign removeLink><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.parties.item'/></#assign>
                <#assign addLink><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.parties.item'/></#assign>

                <div id="associatedParty-items">
                    <#list eml.associatedParties as item>
                        <div id="associatedParty-item-${item_index}" class="item clearfix row g-3 mx-md-3 mx-1 border-bottom pb-3 mt-1">
                            <div class="columnLinks mt-3 d-flex justify-content-between">
                                <div>
                                    <a id="associatedParty-copyDetails-${item_index}" href="">[ ${copyLink?lower_case?cap_first} ]</a>
                                </div>
                                <div>
                                    <a id="associatedParty-removeLink-${item_index}" class="removeAssociatedPartyLink" href="">[ ${removeLink?lower_case?cap_first} ]</a>
                                </div>
                            </div>
                            <div class="col-lg-6">
                                <@input name="eml.associatedParties[${item_index}].firstName" i18nkey="eml.associatedParties.firstName"/>
                            </div>
                            <div class="col-lg-6">
                                <@input name="eml.associatedParties[${item_index}].lastName" i18nkey="eml.associatedParties.lastName" requiredField=true/>
                            </div>
                            <div class="col-lg-6">
                                <@input name="eml.associatedParties[${item_index}].position" i18nkey="eml.associatedParties.position" requiredField=true />
                            </div>
                            <div class="col-lg-6">
                                <@input name="eml.associatedParties[${item_index}].organisation" i18nkey="eml.associatedParties.organisation" requiredField=true />
                            </div>
                            <div class="col-lg-6">
                                <@input name="eml.associatedParties[${item_index}].address.address" i18nkey="eml.associatedParties.address.address" />
                            </div>
                            <div class="col-lg-6">
                                <@input name="eml.associatedParties[${item_index}].address.city" i18nkey="eml.associatedParties.address.city" />
                            </div>
                            <div class="col-lg-6">
                                <@input name="eml.associatedParties[${item_index}].address.province" i18nkey="eml.associatedParties.address.province" />
                            </div>
                            <div class="col-lg-6 countryList">
                                <@select name="eml.associatedParties[${item_index}].address.country" help="i18n" options=countries i18nkey="eml.associatedParties.address.country" value="${eml.associatedParties[item_index].address.country!}"/>
                            </div>
                            <div class="col-lg-6">
                                <@input name="eml.associatedParties[${item_index}].address.postalCode" i18nkey="eml.associatedParties.address.postalCode" />
                            </div>
                            <div class="col-lg-6">
                                <@input name="eml.associatedParties[${item_index}].phone" i18nkey="eml.associatedParties.phone" />
                            </div>
                            <div class="col-lg-6">
                                <@input name="eml.associatedParties[${item_index}].email" i18nkey="eml.associatedParties.email" />
                            </div>
                            <div class="col-lg-6">
                                <@input name="eml.associatedParties[${item_index}].homepage" i18nkey="eml.associatedParties.homepage" type="url" />
                            </div>
                            <div class="col-lg-6">
                                <#if eml.associatedParties[item_index].userIds[0]??>
                                    <@select name="eml.associatedParties[${item_index}].userIds[0].directory" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" value="${eml.associatedParties[item_index].userIds[0].directory!}"/>
                                <#else>
                                    <@select name="eml.associatedParties[${item_index}].userIds[0].directory" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" value=""/>
                                </#if>
                            </div>
                            <div class="col-lg-6">
                                <#if eml.associatedParties[item_index].userIds[0]??>
                                    <@input name="eml.associatedParties[${item_index}].userIds[0].identifier" help="i18n" i18nkey="eml.contact.identifier" value="${eml.associatedParties[item_index].userIds[0].identifier}"/>
                                <#else>
                                    <@input name="eml.associatedParties[${item_index}].userIds[0].identifier" help="i18n" i18nkey="eml.contact.identifier" value=""/>
                                </#if>
                            </div>
                            <div class="col-lg-6">
                                <@select name="eml.associatedParties[${item_index}].role" i18nkey="eml.associatedParties.role" help="i18n" value="${eml.associatedParties[item_index].role!}" options=roles />
                            </div>
                        </div>
                    </#list>
                </div>

                <div class="addNew col-12 mx-md-4 mx-2 mt-1">
                    <a id="plus-associatedParty" href="">${addLink?lower_case?cap_first}</a>
                </div>

                <div id='buttons' class="buttons col-12 mx-md-4 mx-2 mt-3">
                    <@s.submit cssClass="button btn btn-outline-success" name="save" key="button.save"/>
                    <@s.submit cssClass="button btn btn-outline-secondary" name="cancel" key="button.cancel"/>
                </div>

                <!-- internal parameter -->
                <input name="r" type="hidden" value="${resource.shortname}" />


                <div id="baseItem-associatedParty" class="item clearfix row g-3 mx-md-3 mx-1 border-bottom pb-3 mt-1" style="display:none;">
                    <div class="columnLinks mt-3 d-flex justify-content-between">
                        <div>
                            <a id="associatedParty-copyDetails" href="">[ ${copyLink}  ]</a>
                        </div>
                        <div>
                            <a id="associatedParty-removeLink" class="removeAssociatedPartyLink" href="">[ ${removeLink?lower_case?cap_first} ]</a>
                        </div>
                    </div>
                    <div class="col-lg-6">
                        <@input name="firstName" i18nkey="eml.associatedParties.firstName" />
                    </div>
                    <div class="col-lg-6">
                        <@input name="lastName" i18nkey="eml.associatedParties.lastName" requiredField=true />
                    </div>
                    <div class="col-lg-6">
                        <@input name="position" i18nkey="eml.associatedParties.position" requiredField=true />
                    </div>
                    <div class="col-lg-6">
                        <@input name="organisation" i18nkey="eml.associatedParties.organisation" requiredField=true />
                    </div>
                    <div class="col-lg-6">
                        <@input name="address" i18nkey="eml.associatedParties.address.address" />
                    </div>
                    <div class="col-lg-6">
                        <@input name="city" i18nkey="eml.associatedParties.address.city" />
                    </div>
                    <div class="col-lg-6">
                        <@input name="province" i18nkey="eml.associatedParties.address.province" />
                    </div>
                    <div class="col-lg-6 countryList">
                        <@select name="country" options=countries help="i18n" i18nkey="eml.associatedParties.address.country" />
                    </div>
                    <div class="col-lg-6">
                        <@input name="postalCode" i18nkey="eml.associatedParties.address.postalCode" />
                    </div>
                    <div class="col-lg-6">
                        <@input name="phone" i18nkey="eml.associatedParties.phone" />
                    </div>
                    <div class="col-lg-6">
                        <@input name="email" i18nkey="eml.associatedParties.email" />
                    </div>
                    <div class="col-lg-6">
                        <@input name="homepage" i18nkey="eml.associatedParties.homepage" />
                    </div>
                    <div class="col-lg-6">
                        <@select name="directory" options=userIdDirectories help="i18n" i18nkey="eml.contact.directory" />
                    </div>
                    <div class="col-lg-6">
                        <@input name="identifier" help="i18n" i18nkey="eml.contact.identifier" />
                    </div>
                    <div class="col-lg-6">
                        <@select name="role" i18nkey="eml.associatedParties.role" help="i18n" options=roles />
                    </div>
                </div>

            </div>
        </div>
    </form>
</main>


    <#include "/WEB-INF/pages/inc/footer-bootstrap.ftl">
</#escape>
