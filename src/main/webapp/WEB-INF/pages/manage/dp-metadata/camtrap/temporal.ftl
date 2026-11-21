<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.CamtrapMetadataAction" -->
    <#include "/WEB-INF/pages/macros/icon.ftl"/>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.datapackagemetadata.camtrap.temporal.title'/></title>
    <link rel="stylesheet" href="${baseURL}/styles/overview.css">
    <link rel="stylesheet" href="${baseURL}/styles/eml.css">
    <script src="${baseURL}/js/dp-metadata.js"></script>
    <#assign currentMenu="manage"/>
    <#assign currentMetadataPage = "temporal"/>
    <#assign savebarAction = "camtrap-metadata-temporal"/>
    <#assign materialHelpIcons = true/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>
    <#include "scope_macros.ftl"/>

    <#assign inferredScope = (inferredMetadata.inferredTemporalScope)!{}/>
    <#assign inferredOk = (inferredScope.inferred)!false && !(inferredScope.errors)?has_content
        && (inferredScope.startDate)?? && (inferredScope.endDate)??/>
    <#assign storedOk = (metadata.temporal.start)?has_content || (metadata.temporal.end)?has_content/>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form id="temporal-scope-form" class="needs-validation track-unsaved" action="camtrap-metadata-${section}.do" method="post" novalidate>
        <input type="hidden" name="r" value="${resource.shortname}" />

        <#include "metadata_heading.ftl"/>

        <div class="container-fluid bg-body">
            <div class="container bd-layout main-content-container">
                <main class="bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "metadata_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">
                        <div class="my-md-3 p-3">
                            <@scopeModes field="resource.inferTemporalCoverageAutomatically" page="temporal" infer=resource.inferTemporalCoverageAutomatically/>

                            <div id="scope-inferred" class="dp-scope" hidden>
                                <@inferBar scope=inferredScope page="temporal"/>
                                <div class="mt-2">
                                    <#if inferredOk>
                                        <dl class="dp-values mb-0">
                                            <dt><@s.text name="datapackagemetadata.temporal.start"/></dt><dd>${inferredScope.startDate?date}</dd>
                                            <dt><@s.text name="datapackagemetadata.temporal.end"/></dt><dd>${inferredScope.endDate?date}</dd>
                                        </dl>
                                    <#else>
                                        <div class="dp-values-empty"><@s.text name="datapackagemetadata.noData"/></div>
                                    </#if>
                                </div>
                            </div>

                            <div id="scope-stored" class="dp-scope" hidden>
                                <#if storedOk>
                                    <dl class="dp-values mb-0">
                                        <dt><@s.text name="datapackagemetadata.temporal.start"/></dt><dd>${(metadata.temporal.start)!"-"}</dd>
                                        <dt><@s.text name="datapackagemetadata.temporal.end"/></dt><dd>${(metadata.temporal.end)!"-"}</dd>
                                    </dl>
                                <#else>
                                    <div class="dp-values-empty"><@s.text name="datapackagemetadata.noData"/></div>
                                </#if>
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </div>

        <#include "/WEB-INF/pages/manage/eml/eml_savebar.ftl"/>
    </form>

    <#include "/WEB-INF/pages/manage/eml/unsaved_changes_modal.ftl">

    <#include "/WEB-INF/pages/inc/footer.ftl">

    <script>
        $(document).ready(function () {
            dpScope();
            $("#re-infer-link").on('click', displayProcessing);
            makeSureResourceParameterIsPresentInURL('${resource.shortname}');
        });
    </script>
