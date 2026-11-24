<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.TranslationAction" -->
<#-- Value translation of one mapped DwC term: what each source value is published as. Values list, save bar and delete
     dialog: macros/translation.ftl; behaviour: js/translation.js -->
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<#include "/WEB-INF/pages/inc/header.ftl">
<#include "/WEB-INF/pages/macros/translation.ftl"/>
<#assign currentLocale = .vars["locale"]!"en"/>
<#assign fieldLabel = (property.translations[currentLocale].label)!property.label!property.name/>
<#assign mappingUrl = "mapping.do?r=${resource.shortname}&id=${property.extension.rowType?url}&mid=${mid}"/>
<#assign pageUrl = "translation.do?r=${resource.shortname}&rowtype=${property.extension.rowType?url}&mid=${mid}&term=${property.qualname?url}"/>
<title><@s.text name="manage.translation.title"/>: ${fieldLabel}</title>
<style>
    /* where the sticky toolbar sits: just under the fixed navbar */
    :root { --navbar-height: 120px; --ov-bars-height: 57px; }
</style>
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2-ipt.css">
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/mapping.css">
<link rel="stylesheet" href="${baseURL}/styles/translation.css">
<script src="${baseURL}/js/select2/select2-4.0.13.full.min.js"></script>
<script src="${baseURL}/js/translation.js"></script>
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<#macro backticksAsCode description>
    ${description?replace("`(.*?)`", "<code>$1</code>", "r")?replace("'", "&#39;")?replace("\\[(.*)\\]\\((.*)\\)", "<a href='$2'>$1</a>", "r")?no_esc}
</#macro>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container-fluid border-bottom">
        <div class="container bg-body border rounded-2 mb-4">
            <div class="container my-3 p-3 ov-head ov-head-left">
                <div class="ov-grow">
                    <div class="fs-smaller">
                        <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                            <ol class="breadcrumb mb-0">
                                <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                                <li class="breadcrumb-item ov-crumb-resource"><a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a></li>
                                <li class="breadcrumb-item"><a href="${mappingUrl}">${property.extension.title!property.extension.name!}</a></li>
                                <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.mapping.translation"/></li>
                            </ol>
                        </nav>
                    </div>
                    <#-- the field; "Value translation" is the pill below -->
                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal">${fieldLabel}</h1>
                    <div class="ov-meta mp-source-line">
                        <span class="mp-kind-pill"><@s.text name="manage.translation.title"/></span>
                        <span>${property.qualname}</span>
                        <#if property.vocabulary??>
                            <span><@s.text name="manage.translation.vocabulary.label"/>: <a href="vocabulary.do?id=${property.vocabulary.uriString?url}" target="_blank" rel="noopener">${property.vocabulary.title!property.vocabulary.uriString}</a></span>
                        </#if>
                    </div>
                </div>

                <div class="ov-acts">
                    <button type="submit" id="save" name="save" value="save" form="translation-form" class="btn btn-sm btn-gbif-solid top-button"><@icon name="save"/> <@s.text name="button.save"/></button>
                    <a class="btn btn-sm btn-outline-secondary top-button" role="button" href="${mappingUrl}"><@s.text name="button.back"/></a>
                    <#-- more actions last, as on the resource overview -->
                    <div class="dropdown">
                        <button type="button" class="btn btn-sm btn-outline-secondary top-button ov-more-btn" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='manage.overview.moreActions'/>" aria-label="<@s.text name='manage.overview.moreActions'/>"><@icon name="more_horiz"/></button>
                        <ul class="dropdown-menu dropdown-menu-end">
                            <li><a class="dropdown-item" href="translationReload.do?r=${resource.shortname}&mapping=${property.extension.rowType?url}&term=${property.qualname?url}&mid=${mid}&rowtype=${property.extension.rowType?url}"><@s.text name="manage.translation.reload"/></a></li>
                            <li><button type="button" class="dropdown-item text-gbif-danger" data-bs-toggle="modal" data-bs-target="#tr-delete-modal"><@s.text name="button.delete"/></button></li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <@i18n/>

    <main class="container main-content-container pt-4 pb-5">
        <#-- the field's definition, to know what the values should become -->
        <section class="mp-section" id="tr-field">
            <div class="mp-sec-head"><h2><@s.text name="manage.translation.field.title"/></h2></div>
            <div class="tr-def">
                <#if (property.translations[currentLocale].description)?has_content><p><@backticksAsCode property.translations[currentLocale].description/></p><#elseif property.description?has_content><p>${property.description}</p></#if>
                <#if (property.translations[currentLocale].comments)?has_content><p><@backticksAsCode property.translations[currentLocale].comments/></p><#elseif property.comments?has_content><p>${property.comments}</p></#if>
                <dl class="tr-facts">
                    <#if (property.translations[currentLocale].examples)?has_content>
                        <dt><@s.text name="basic.examples"/></dt><dd><@backticksAsCode property.translations[currentLocale].examples/></dd>
                    <#elseif property.examples?has_content>
                        <dt><@s.text name="basic.examples"/></dt><dd><@backticksAsCode property.examples/></dd>
                    </#if>
                    <#if property.vocabulary??>
                        <dt><@s.text name="manage.translation.vocabulary.label"/></dt>
                        <dd><a href="vocabulary.do?id=${property.vocabulary.uriString?url}" target="_blank" rel="noopener">${property.vocabulary.title!property.vocabulary.uriString}</a></dd>
                    </#if>
                </dl>
            </div>
        </section>

        <@valuesSection formAction="translation.do" automapUrl=(property.vocabulary??)?then("translationAutomap.do?r=${resource.shortname}&mapping=${property.extension.rowType?url}&rowtype=${property.extension.rowType?url}&term=${property.qualname?url}&mid=${mid}", "")>
            <input type="hidden" name="r" value="${resource.shortname}"/>
            <input type="hidden" name="rowtype" value="${property.extension.rowType}"/>
            <input type="hidden" name="mid" value="${mid}"/>
            <input type="hidden" name="term" value="${property.qualname}"/>
        </@valuesSection>
    </main>

    <@saveBar discardUrl=pageUrl/>
    <@deleteModal fieldName=fieldLabel/>

<#include "/WEB-INF/pages/inc/footer.ftl">
