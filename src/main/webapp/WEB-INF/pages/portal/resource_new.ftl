<#-- @ftlvariable name="" type="org.gbif.ipt.action.portal.ResourceAction" -->
<#include "/WEB-INF/pages/inc/header.ftl">
<title>${eml.title!"IPT"}</title>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<#include "/WEB-INF/pages/macros/docbook_editor.ftl"/>
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<#include "/WEB-INF/pages/macros/portal_resource.ftl"/>
<link rel="stylesheet" href="${baseURL}/styles/leaflet/leaflet-1.9.4.css">
<link rel="stylesheet" href="${baseURL}/styles/portal-resource.css">

<#assign download_dwca_url>${baseURL}/archive.do?r=${resource.shortname}<#if version??>&v=${version.toPlainString()}</#if></#assign>
<#assign download_eml_url>${baseURL}/eml.do?r=${resource.shortname}&v=<#if version??>${version.toPlainString()}<#else>${resource.emlVersion.toPlainString()}</#if></#assign>
<#assign download_rtf_url>${baseURL}/rtf.do?r=${resource.shortname}&v=<#if version??>${version.toPlainString()}<#else>${resource.emlVersion.toPlainString()}</#if></#assign>
<#assign isPreviewPage = action.isPreview() />

<style>
    :root {
        --navbar-height: 90px;
    }

    .section {
        scroll-margin-top: var(--navbar-height);
    }
</style>

<@docBookViewerAssets/>
<script src="${baseURL}/js/scrollspy.js"></script>
<script>
    $(document).ready(function() {
        // Render content FIRST, before anything measures the page.
        <#-- JSON from Gson, which escapes <, > and & -->
        const translations = ${action.translationsJson?no_esc};
        if (translations) {
            // the title and description in the interface language if translated, and a note on the other languages
            IptMultilingualFields.translationNote(translations, {
                note: document.getElementById('metadata-translations'),
                title: document.getElementById('resource-title'),
                label: "${action.getText('portal.resource.translations.available')?js_string?no_esc}"
            });
        } else {
            IptDocBookEditor.renderAll();
        }

        // Then initialize scrollspy, now that real content/heights exist.
        IptScrollSpy.init({ tocSelector: '.bd-toc nav a' });
    })
</script>

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<@previewNotice nextVersion=resource.emlVersion.toPlainString()/>

<div class="container-fluid bg-body border-bottom">
    <div class="container border rounded-2 mb-4">
        <div class="container my-3 p-3">
            <div class="text-center fs-smaller">
                <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                    <ol class="breadcrumb justify-content-center mb-0">
                        <li class="breadcrumb-item"><a href="${baseURL}"><@s.text name="breadcrumb.home"/></a></li>
                        <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.resource"/></li>
                    </ol>
                </nav>
            </div>

            <div class="text-center">
                <h1 id="resource-title" property="dc:title" class="rtitle pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    ${eml.title!resource.shortname}
                </h1>

                <div class="text-center">
                    <span class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill type-${resourceTypeLowerCase} me-1"><@s.text name="portal.resource.type.${resourceTypeLowerCase}"/></span>
                    <#if resourceSubtypeLowerCase?has_content>
                        <span class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill type-${resourceSubtypeLowerCase} me-1"><@s.text name="portal.resource.subtype.${resourceSubtypeLowerCase}"/></span>
                    </#if>
                </div>

                <@versionLine latestVersion=resource.emlVersion.toPlainString() pubDate=(eml.pubDate)!""/>

                <#if resource.lastPublished??>
                    <div class="mt-2">
                        <@emlDownloadButtons/>

                        <#if managerRights>
                            <a href="${baseURL}/manage/resource.do?r=${resource.shortname}" class="btn btn-sm btn-outline-secondary mt-1 me-xl-1 top-button">
                                <@s.text name='button.edit'/>
                            </a>
                        </#if>
                    </div>
                </#if>
            </div>
        </div>
    </div>
</div>

<#include "/WEB-INF/pages/inc/resource_eml_sections.ftl">
