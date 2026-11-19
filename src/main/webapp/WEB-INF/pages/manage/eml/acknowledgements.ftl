<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.MetadataAction" -->
    <#include "/WEB-INF/pages/inc/header.ftl">
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <#include "/WEB-INF/pages/macros/docbook_editor.ftl"/>
    <@docBookEditorAssets/>
    <title><@s.text name='manage.metadata.acknowledgements.title'/></title>
    <script>
        $(document).ready(function() {
            makeSureResourceParameterIsPresentInURL('${resource.shortname}');
        });
    </script>
    <#assign currentMenu="manage"/>
    <#assign currentMetadataPage = "acknowledgements"/>
    <#include "/WEB-INF/pages/macros/icon.ftl"/>
    <link rel="stylesheet" href="${baseURL}/styles/overview.css">
    <link rel="stylesheet" href="${baseURL}/styles/eml.css">
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form id="acknowledgements-form" class="needs-validation track-unsaved" action="metadata-${section}.do" method="post" novalidate>
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
                        <div class="my-md-3 p-3">
                            <p class="mb-3">
                                <a tabindex="0" role="button"
                                   class="popover-link"
                                   data-bs-toggle="popover"
                                   data-bs-trigger="focus"
                                   data-bs-html="true"
                                   data-bs-content="<@s.text name='manage.metadata.acknowledgements.info' escapeHtml=true/>">
                                    <i class="bi bi-info-circle text-gbif-primary px-1"></i>
                                </a>
                                <@s.text name='manage.metadata.acknowledgements.intro'/>
                            </p>

                            <p class="mb-3 text-smaller fst-italic">
                                <@s.text name='manage.metadata.acknowledgements.description'/>
                            </p>

                            <@docBookEditor name="eml.acknowledgements" id="acknowledgements" value=eml.acknowledgements! label=action.getText("manage.metadata.acknowledgements")/>
                        </div>
                    </div>
                </main>
            </div>
        </div>

        <#include "eml_savebar.ftl"/>
    </form>

    <#include "/WEB-INF/pages/manage/eml/unsaved_changes_modal.ftl">

    <#include "/WEB-INF/pages/inc/footer.ftl">
