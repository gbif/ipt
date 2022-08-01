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
                <div class="text-center text-uppercase fw-bold fs-smaller-2">
                    <@s.text name="manage.overview.metadata"/>
                </div>

                <div class="text-center">
                    <h1 class="py-2 mb-0 text-gbif-header fs-2 fw-normal">
                        <@s.text name='manage.metadata.project.title'/>
                    </h1>
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

                <main class="bd-main bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "eml_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">
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
                                <@textinline name="eml.project.personnel" help="i18n"/>

                                <div id="personnel-items">
                                    <#list eml.project.personnel as item>
                                        <div id="personnel-item-${item_index}" class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                            <div class="columnLinks mt-2 d-flex justify-content-between">
                                                <div>
                                                    <a id="personnel-copyDetails-${item_index}" href="" class="text-smaller">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                                <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${copyLink?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <div>
                                                    <a id="personnel-removeLink-${item_index}" class="removePersonnelLink text-smaller" href="">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${removeLink?lower_case?cap_first}</span>
                                                    </a>
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

                                <div class="addNew col-12 mt-2">
                                    <a id="plus-personnel" href="" class="text-smaller">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span>${addLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                            </div>

                            <!-- internal parameter -->
                            <input name="r" type="hidden" value="${resource.shortname}" />

                            <div id="baseItem-personnel" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display:none;">
                                <div class="columnLinks mt-2 d-flex justify-content-between">
                                    <div>
                                        <a id="personnel-copyDetails" href="" class="text-smaller">
                                            <span>
                                                <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                    <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"></path>
                                                </svg>
                                            </span>
                                            <span>${copyLink?lower_case?cap_first}</span>
                                        </a>
                                    </div>
                                    <div class="text-end">
                                        <a id="personnel-removeLink" class="removePersonnelLink text-smaller" href="">
                                            <span>
                                                <svg viewBox="0 0 24 24" class="link-icon">
                                                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                </svg>
                                            </span>
                                            <span>${removeLink?lower_case?cap_first}</span>
                                        </a>
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
