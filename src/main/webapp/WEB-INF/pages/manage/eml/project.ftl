<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header-bootstrap.ftl">
    <title><@s.text name='manage.metadata.project.title'/></title>
    <#assign auxTopNavbar=true />
    <#assign auxTopNavbarPage = "metadata" />
    <#assign currentMenu="manage"/>
    <script type="text/javascript">
        $(document).ready(function(){
            initHelp();
        });
    </script>
    <#include "/WEB-INF/pages/inc/menu-bootstrap.ftl">
    <#include "/WEB-INF/pages/macros/forms-bootstrap.ftl"/>
    <#include "/WEB-INF/pages/macros/metadata_agent.ftl"/>

    <main class="container">
        <div class="row g-3">
            <div class="p-3 bg-body rounded shadow-sm">

                <#include "/WEB-INF/pages/inc/action_alerts-bootstrap.ftl">

                <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header text-center">
                    <@s.text name='manage.metadata.project.title'/>:
                    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                </h5>

                <p class="text-muted mx-md-4 mx-2 mb-0">
                    <@s.text name='manage.metadata.project.intro'/>
                </p>

                <!-- retrieve some link names one time -->
                <#assign copyLink><@s.text name="eml.resourceCreator.copyLink"/></#assign>
                <#assign removeLink><@s.text name='manage.metadata.removethis'/> <@s.text name='rtf.project.personnel'/></#assign>
                <#assign addLink><@s.text name='manage.metadata.addnew'/> <@s.text name='rtf.project.personnel'/></#assign>

                <div class="row g-3 mx-md-4 mx-2 mt-1 mb-2">
                    <@input name="eml.project.title" requiredField=true/>
                    <@input name="eml.project.identifier" help="i18n"/>
                    <@text name="eml.project.description" help="i18n"/>
                    <@text name="eml.project.funding" help="i18n"/>
                    <@text name="eml.project.studyAreaDescription.descriptorValue" help="i18n" />
                    <@text name="eml.project.designDescription" help="i18n" />
                </div>

            </div>
        </div>

        <div class="row g-3 mt-1">
            <div class="col-lg-12 p-3 bg-body rounded shadow-sm">
                <!-- List of personnel -->
                <div class="listBlock">
                    <@textinline name="eml.project.personnel" help="i18n" requiredField=true/>

                    <div id="personnel-items">
                        <#list eml.project.personnel as item>
                            <div id="personnel-item-${item_index}" class="item clearfix row g-3 mx-md-3 mx-1 border-bottom pb-3 mt-1">
                                <div class="columnLinks mt-3 d-flex justify-content-between">
                                    <div>
                                        <a id="personnel-copyDetails-${item_index}" href="">[ ${copyLink?lower_case?cap_first} ]</a>
                                    </div>
                                    <div>
                                        <a id="personnel-removeLink-${item_index}" class="removePersonnelLink" href="">[ ${removeLink?lower_case?cap_first} ]</a>
                                    </div>
                                </div>
                                <div class="col-lg-6">
                                    <@input name="eml.project.personnel[${item_index}].firstName" i18nkey="eml.project.personnel.firstName"/>
                                </div>
                                <div class="col-lg-6">
                                    <@input name="eml.project.personnel[${item_index}].lastName" i18nkey="eml.project.personnel.lastName" requiredField=true/>
                                </div>
                                <div class="col-lg-6">
                                    <#if eml.project.personnel[item_index]?? && eml.project.personnel[item_index].userIds[0]??>
                                        <@select name="eml.project.personnel[${item_index}].userIds[0].directory" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" value="${eml.project.personnel[item_index].userIds[0].directory!}"/>
                                    <#else>
                                        <@select name="eml.project.personnel[${item_index}].userIds[0].directory" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" value=""/>
                                    </#if>
                                </div>
                                <div class="col-lg-6">
                                    <@input name="eml.project.personnel[${item_index}].userIds[0].identifier" help="i18n" i18nkey="eml.contact.identifier" />
                                </div>
                                <div class="col-lg-6">
                                    <@select name="eml.project.personnel[${item_index}].role" i18nkey="eml.associatedParties.role" help="i18n" value="${eml.project.personnel[item_index].role!}" options=roles />
                                </div>
                            </div>
                        </#list>
                    </div>

                    <div class="addNew col-12 mx-md-4 mx-2 mt-1">
                        <a id="plus-personnel" href="">${addLink?lower_case?cap_first}</a>
                    </div>
                </div>

                <div class="buttons col-12 mx-md-4 mx-2 mt-3">
                    <@s.submit cssClass="button btn btn-outline-gbif-primary" name="save" key="button.save" />
                    <@s.submit cssClass="button btn btn-outline-secondary" name="cancel" key="button.cancel" />
                </div>

                <!-- internal parameter -->
                <input name="r" type="hidden" value="${resource.shortname}" />


                <div id="baseItem-personnel" class="item clearfix row g-3 mx-md-3 mx-1 border-bottom pb-3 mt-1" style="display:none;">
                    <div class="columnLinks mt-3 d-flex justify-content-between">
                        <div>
                            <a id="personnel-copyDetails" href="">[ ${copyLink}  ]</a>
                        </div>
                        <div>
                            <a id="personnel-removeLink" class="removePersonnelLink" href="">[ ${removeLink?lower_case?cap_first} ]</a>
                        </div>
                    </div>
                    <div class="col-lg-6">
                        <@input name="firstName" i18nkey="eml.project.personnel.firstName" />
                    </div>
                    <div class="col-lg-6">
                        <@input name="lastName" i18nkey="eml.project.personnel.lastName" requiredField=true />
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
    </main>
    </form>

    <#include "/WEB-INF/pages/inc/footer-bootstrap.ftl">
</#escape>
