<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.MetadataAction" -->
    <#include "/WEB-INF/pages/inc/header.ftl">
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <title><@s.text name='manage.metadata.additionalDescription.title'/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <#include "/WEB-INF/pages/macros/docbook_editor.ftl"/>
    <@docBookEditorAssets/>
    <script>
        $(document).ready(function(){
            // scroll to the error if present
            var invalidElements = $(".is-invalid");

            if (invalidElements !== undefined && invalidElements.length > 0) {
                var invalidElement = invalidElements.first();
                var pos = invalidElement.offset().top - 100;
                // scroll to the element
                $('body, html').animate({scrollTop: pos});
            }

            makeSureResourceParameterIsPresentInURL('${resource.shortname}');
        });
    </script>
    <#assign currentMenu="manage"/>
    <#assign currentMetadataPage = "additionalDescription"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form id="additional-description-form" class="needs-validation track-unsaved" action="metadata-${section}.do" method="post" novalidate>
        <input type="hidden" name="r" value="${resource.shortname}" />

        <div class="container-fluid bg-body border-bottom">
            <div class="container bg-body border rounded-2 mb-4">
                <div class="container my-3 p-3">
                    <div class="text-center fs-smaller">
                        <div class="text-center fs-smaller">
                            <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                                <ol class="breadcrumb justify-content-center mb-0">
                                    <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                                    <li class="breadcrumb-item"><a href="resource?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                                    <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.metadata"/></li>
                                </ol>
                            </nav>
                        </div>
                    </div>

                    <div class="text-center">
                        <h1 class="py-2 mb-0 text-gbif-header fs-2 fw-normal">
                            <@s.text name='manage.metadata.additionalDescription.title'/>
                        </h1>
                    </div>

                    <div class="text-center fs-smaller">
                        <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">
                            <#if resource.title?has_content>
                                ${resource.title}
                            <#else>
                                ${resource.shortname}
                            </#if>
                        </a>
                    </div>

                    <div class="text-center mt-2">
                        <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save"/>
                        <button type="button" class="btn btn-sm btn-outline-secondary top-button" onclick="window.history.back();">
                            <@s.text name="button.back"/>
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <#include "metadata_section_select.ftl"/>

        <div class="container-fluid bg-body">
            <div class="container bd-layout main-content-container">
                <main class="bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "eml_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">
                        <!-- Purpose -->
                        <div class="my-md-3 p-3">
                            <@textinline name="eml.purpose" help="i18n"/>

                            <div>
                                <p class="mb-3 mt-3 text-smaller fst-italic">
                                    <@s.text name='eml.purpose.description'/>
                                </p>
                            </div>

                            <div class="mt-3">
                                <@docBookEditor name="eml.purpose" id="purpose" value=eml.purpose! label=action.getText("eml.purpose")/>
                            </div>
                        </div>

                        <!-- Introduction -->
                        <div class="my-md-3 p-3">
                            <@textinline name="manage.metadata.introduction" help="i18n"/>

                            <div>
                                <p class="mb-3 mt-3 text-smaller fst-italic">
                                    <@s.text name='manage.metadata.introduction.description'/>
                                </p>
                            </div>

                            <div class="mt-3">
                                <@docBookEditor name="eml.introduction" id="introduction" value=eml.introduction! label=action.getText("manage.metadata.introduction")/>
                            </div>
                        </div>

                        <!-- Getting Started -->
                        <div class="my-md-3 p-3">
                            <@textinline name="manage.metadata.gettingStarted" help="i18n"/>

                            <div>
                                <p class="mb-3 mt-3 text-smaller fst-italic">
                                    <@s.text name='manage.metadata.gettingStarted.description'/>
                                </p>
                            </div>

                            <div class="mt-3">
                                <@docBookEditor name="eml.gettingStarted" id="gettingStarted" value=eml.gettingStarted! label=action.getText("manage.metadata.gettingStarted")/>
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </div>
    </form>

    <#include "/WEB-INF/pages/manage/eml/unsaved_changes_modal.ftl">

    <#include "/WEB-INF/pages/inc/footer.ftl">
