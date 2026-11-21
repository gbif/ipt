<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.CamtrapMetadataAction" -->
    <#include "/WEB-INF/pages/macros/icon.ftl"/>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.datapackagemetadata.camtrap.taxonomic.title'/></title>
    <link rel="stylesheet" href="${baseURL}/styles/overview.css">
    <link rel="stylesheet" href="${baseURL}/styles/eml.css">
    <script src="${baseURL}/js/dp-metadata.js"></script>
    <#assign currentMenu="manage"/>
    <#assign currentMetadataPage = "taxonomic"/>
    <#assign savebarAction = "camtrap-metadata-taxonomic"/>
    <#assign materialHelpIcons = true/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>
    <#include "scope_macros.ftl"/>

    <#-- the taxa as a table: scientific name, and rank and taxon ID when any taxon has them -->
    <#macro taxa list>
        <#if list?has_content>
            <#local withRank = false/>
            <#local withId = false/>
            <#list list as tx>
                <#if (tx.taxonRank)??><#local withRank = true/></#if>
                <#if (tx.taxonID)?has_content><#local withId = true/></#if>
            </#list>
            <div class="dp-taxa">
                <table class="tax-table">
                    <thead>
                        <tr>
                            <th scope="col"><@s.text name="datapackagemetadata.taxonomic.scientificName"/></th>
                            <#if withRank><th scope="col"><@s.text name="datapackagemetadata.taxonomic.taxonRank"/></th></#if>
                            <#if withId><th scope="col"><@s.text name="datapackagemetadata.taxonomic.taxonId"/></th></#if>
                        </tr>
                    </thead>
                    <tbody>
                        <#list list as tx>
                            <tr>
                                <td class="tax-sci">${tx.scientificName!}</td>
                                <#if withRank><td>${(tx.taxonRank)!}</td></#if>
                                <#if withId><td>${tx.taxonID!}</td></#if>
                            </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
            <p class="dp-count"><@s.text name="manage.datapackagemetadata.camtrap.taxonomic.count"><@s.param>${list?size}</@s.param></@s.text></p>
        <#else>
            <div class="dp-values-empty"><@s.text name="datapackagemetadata.noData"/></div>
        </#if>
    </#macro>

    <#assign inferredScope = (inferredMetadata.inferredTaxonomicScope)!{}/>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form id="taxonomic-metadata-form" class="needs-validation track-unsaved" action="camtrap-metadata-${section}.do" method="post" novalidate>
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
                            <@scopeModes field="resource.inferTaxonomicCoverageAutomatically" page="taxonomic" infer=resource.inferTaxonomicCoverageAutomatically/>

                            <div id="scope-inferred" class="dp-scope" hidden>
                                <@inferBar scope=inferredScope page="taxonomic"/>
                                <div class="mt-2">
                                    <#if !(inferredScope.errors)?has_content>
                                        <@taxa list=(inferredScope.data)![]/>
                                    </#if>
                                </div>
                            </div>

                            <div id="scope-stored" class="dp-scope" hidden>
                                <@taxa list=(metadata.taxonomic)![]/>
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
