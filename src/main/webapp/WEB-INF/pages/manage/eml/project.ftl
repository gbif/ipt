<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.metadata.project.title'/></title>
    <script>
        $(document).ready(function () {
            $('#metadata-section').change(function () {
                var metadataSection = $('#metadata-section').find(':selected').val()
                $(location).attr('href', 'metadata-' + metadataSection + '.do?r=${resource.shortname!r!}');
            });
        });
    </script>
    <#assign currentMetadataPage = "project"/>
    <#assign currentMenu="manage"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>
    <#include "/WEB-INF/pages/macros/metadata_agent.ftl"/>

    <form class="needs-validation" action="metadata-${section}.do" method="post" novalidate>
        <div class="container-fluid bg-body border-bottom">
            <div class="container pt-2">
                <#include "/WEB-INF/pages/inc/action_alerts.ftl">
            </div>

            <div class="container my-3 p-3">

                <div class="text-center">
                    <h5 class="pt-2 text-gbif-header fs-4 fw-400 text-center">
                        <@s.text name='manage.metadata.project.title'/>
                    </h5>
                </div>

                <div class="text-center fs-smaller">
                    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                </div>

                <div class="text-center mt-2">
                    <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save" />
                    <@s.submit cssClass="button btn btn-sm btn-outline-secondary top-button" name="cancel" key="button.back" />
                </div>
            </div>
        </div>

        <#include "metadata_section_select.ftl"/>

        <div class="container-fluid bg-body">
            <div class="container bd-layout">

                <main class="bd-main bd-main-right">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "eml_sidebar.ftl"/>
                    </div>

                    <div class="bd-content ps-lg-4">
                        <div class="my-md-3 p-3">
                            <p class="mb-0">
                                <@s.text name='manage.metadata.project.intro'/>
                            </p>

                            <!-- retrieve some link names one time -->
                            <#assign copyLink><@s.text name="eml.resourceCreator.copyLink"/></#assign>
                            <#assign removeLink><@s.text name='manage.metadata.removethis'/> <@s.text name='rtf.project.personnel'/></#assign>
                            <#assign addLink><@s.text name='manage.metadata.addnew'/> <@s.text name='rtf.project.personnel'/></#assign>

                            <div class="row g-3 mt-1 mb-2">
                                <@input name="eml.project.title" requiredField=true/>
                                <@input name="eml.project.identifier" help="i18n"/>
                                <@text name="eml.project.description" help="i18n"/>
                                <@text name="eml.project.funding" help="i18n"/>
                                <@text name="eml.project.studyAreaDescription.descriptorValue" help="i18n" />
                                <@text name="eml.project.designDescription" help="i18n" />
                            </div>

                        </div>

                        <div class="my-md-3 p-3">
                            <!-- List of personnel -->
                            <div class="listBlock">
                                <@textinline name="eml.project.personnel" help="i18n" requiredField=true/>

                                <div id="personnel-items">
                                    <#list eml.project.personnel as item>
                                        <div id="personnel-item-${item_index}" class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                            <div class="columnLinks mt-3 d-flex justify-content-between">
                                                <div>
                                                    <a id="personnel-copyDetails-${item_index}" href="">${copyLink?lower_case?cap_first}</a>
                                                </div>
                                                <div>
                                                    <a id="personnel-removeLink-${item_index}" class="removePersonnelLink" href="">${removeLink?lower_case?cap_first}</a>
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

                                <div class="addNew col-12 mt-1">
                                    <a id="plus-personnel" href="">${addLink?lower_case?cap_first}</a>
                                </div>
                            </div>

                            <!-- internal parameter -->
                            <input name="r" type="hidden" value="${resource.shortname}" />


                            <div id="baseItem-personnel" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display:none;">
                                <div class="columnLinks mt-3 d-flex justify-content-between">
                                    <div>
                                        <a id="personnel-copyDetails" href="">${copyLink}</a>
                                    </div>
                                    <div>
                                        <a id="personnel-removeLink" class="removePersonnelLink" href="">${removeLink?lower_case?cap_first}</a>
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
            </div>
        </div>
    </form>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
