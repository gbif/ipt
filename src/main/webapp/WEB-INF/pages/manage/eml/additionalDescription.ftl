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
    <#include "/WEB-INF/pages/macros/icon.ftl"/>
    <link rel="stylesheet" href="${baseURL}/styles/overview.css">
    <link rel="stylesheet" href="${baseURL}/styles/eml.css">
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form id="additional-description-form" class="needs-validation track-unsaved" action="metadata-${section}.do" method="post" novalidate>
        <input type="hidden" name="r" value="${resource.shortname}" />

        <#include "eml_heading.ftl"/>

        <#include "metadata_section_select.ftl"/>

        <div class="container-fluid bg-body">
            <div class="container bd-layout main-content-container">
                <main class="bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "eml_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">
                        <!-- Purpose -->
                        <div class="my-md-3 p-3" id="s-purpose" data-toc="${action.getText('eml.purpose')}">
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
                        <div class="my-md-3 p-3" id="s-introduction" data-toc="${action.getText('manage.metadata.introduction')}">
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
                        <div class="my-md-3 p-3" id="s-getting-started" data-toc="${action.getText('manage.metadata.gettingStarted')}">
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

        <#include "eml_savebar.ftl"/>
    </form>

    <#include "/WEB-INF/pages/manage/eml/unsaved_changes_modal.ftl">

    <#include "/WEB-INF/pages/inc/footer.ftl">
