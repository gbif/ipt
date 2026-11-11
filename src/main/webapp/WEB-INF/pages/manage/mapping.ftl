<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.MappingAction" -->
<#-- Mapping of a source to a core or an extension (DwC-A). The form posts the same parameters as before; the sections,
     counts, sample checks and the by-column view are built in the browser by js/mapping.js from the markup below -->
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<#include "/WEB-INF/pages/inc/header.ftl"/>
<#assign extensionTitle = mapping.extension.title!mapping.extension.name!/>
<#assign isCoreMapping = action.isCoreMapping()/>
<title><#if isCoreMapping><@s.text name="manage.mapping.title.core"><@s.param>${extensionTitle}</@s.param></@s.text><#else><@s.text name="manage.mapping.title.extension"><@s.param>${extensionTitle}</@s.param></@s.text></#if>: ${resource.title!resource.shortname}</title>
<style>
    :root { --navbar-height: 120px; }
</style>
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/mapping.css">
<script src="${baseURL}/js/scrollspy.js"></script>
<script src="${baseURL}/js/select2/select2-4.0.13.full.min.js"></script>
<script src="${baseURL}/js/mapping.js"></script>

<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl"/>

<#assign redundants = action.getRedundantGroups()/>
<#assign sampleProblems = action.getSampleTypeProblems()/>
<#assign coreRowType = resource.coreRowType!mapping.extension.rowType/>
<#assign coreExtensionTitle = (coreid.extension.title)!""/>
<#assign idTermName = (coreid.name)!"ID"/>
<#assign alsoDeleted = action.getMappingsDeletedWithThis()/>
<#assign coreIdTermFieldsIndex = ""/>
<#if !isCoreMapping && coreid??>
    <#assign coreIdTermFieldsIndex = (action.getFieldsTermIndices().get(coreid.qualname)?c)!""/>
</#if>

<#macro processSurroundedWithBackticksAsCode description>
    ${description?replace("`(.*?)`", "<code>$1</code>", "r")?replace("'", "&#39;")?replace("\\[(.*)\\]\\((.*)\\)", "<a href='$2'>$1</a>", "r")?no_esc}
</#macro>

<#-- dwc:name, dcterms:name, ... -->
<#macro termName p><#compress>
    <#if p.namespace()?starts_with("http://rs.tdwg.org/dwc/terms/")>dwc:${p.name}
    <#elseif p.namespace()?starts_with("http://purl.org/dc/terms")>dcterms:${p.name}
    <#elseif p.namespace()?starts_with("http://purl.org/dc/elements/1.1")>dc:${p.name}
    <#elseif p.namespace()?starts_with("http://rs.tdwg.org/ac/terms/")>ac:${p.name}
    <#else>${p.name}</#if>
</#compress></#macro>

<#macro termDefinition p>
    <#if (p.translations[currentLocale].description)?has_content><p><@processSurroundedWithBackticksAsCode p.translations[currentLocale].description/></p><#elseif p.description?has_content><p>${p.description}</p></#if>
    <#if (p.translations[currentLocale].comments)?has_content><p><@processSurroundedWithBackticksAsCode p.translations[currentLocale].comments/></p><#elseif p.comments?has_content><p>${p.comments}</p></#if>
    <#if (p.translations[currentLocale].examples)?has_content>
        <p><em><@s.text name="basic.examples"/></em>: <@processSurroundedWithBackticksAsCode p.translations[currentLocale].examples/></p>
    <#elseif p.examples?has_content>
        <p><em><@s.text name="basic.examples"/></em>: <@processSurroundedWithBackticksAsCode p.examples/></p>
    </#if>
    <#if p.vocabulary??><p><@s.text name="extension.vocabulary"/> <a href="vocabulary.do?id=${p.vocabulary.uriString}" target="_blank">${p.vocabulary.title!}</a></p></#if>
    <#if datasetId?? && p.qualifiedName()?lower_case == datasetId.qualname?lower_case><p><@s.text name='manage.mapping.datasetIdColumn.help'/></p></#if>
    <p><#if p.link?has_content><@s.text name="basic.seealso"/> <a href="${p.link}" target="_blank">${p.link}</a><#elseif p.qualifiedName()?has_content><a href="${p.qualifiedName()}" target="_blank">${p.qualifiedName()}</a></#if></p>
</#macro>

<#macro columnOptions selected=-99999>
    <option value=""></option>
    <#list columns as col>
        <option value="${col_index}"<#if selected == col_index> selected</#if>>${col}</option>
    </#list>
</#macro>

<#macro fieldRow field g>
    <#local p = field.term/>
    <#local i = action.getFieldsTermIndices().get(p.qualifiedName())/>
    <#local label = (p.translations[currentLocale].label)!p.label!p.name/>
    <#local isDatasetId = datasetId?? && p.qualifiedName()?lower_case == datasetId.qualname?lower_case/>
    <#local isMirrored = coreIdTermFieldsIndex?has_content && coreIdTermFieldsIndex == i?c/>
    <#local type = (p.type?lower_case)!"string"/>
    <#local description><#if (p.translations[currentLocale].description)?has_content>${p.translations[currentLocale].description}<#else>${p.description!}</#if></#local>
    <#local description = description?markup_string/>
    <#local translations = field.translation!{}/>
    <div class="mp-field" id="field-${i?c}" data-i="${i?c}" data-group="${g}" data-label="${label}" data-term="<@termName p/>"
         data-required="${p.required?c}" data-type="${type}"<#if p.vocabulary??> data-vocab="${p.vocabulary.title!}"</#if><#if isMirrored> data-mirrored="true"</#if>
         data-search="${(label + ' ' + p.name + ' ' + description)?lower_case}">
        <div class="mp-name">
            <div class="mp-label">
                <button type="button" class="mp-info" aria-expanded="false" aria-controls="field-def-${i?c}" title="<@s.text name='manage.mapping.field.definition'/>">${label}</button>
            </div>
            <div class="mp-term"><@termName p/>
                <#if type != "string"><span class="mp-badge t">${type}</span></#if>
                <#if p.vocabulary??><span class="mp-badge v" title="<@s.text name='extension.vocabulary'/>">${p.vocabulary.title!}</span></#if>
                <#if p.required><span class="mp-badge r"><@s.text name="manage.mapping.field.required"/></span></#if>
                <#if isMirrored><span class="mp-badge lock"><@icon name="lock"/> <@s.text name="manage.mapping.field.locked"/></span></#if>
            </div>
            <div class="mp-def" id="field-def-${i?c}" hidden><@termDefinition p/></div>
        </div>
        <div>
            <select id="fIdx${i?c}" class="form-select form-select-sm mp-col<#if isMirrored> mp-locked</#if>" name="fields[${i?c}].index" aria-label="${label}: <@s.text name='manage.mapping.fields.head.column'/>"<#if isMirrored> tabindex="-1" aria-disabled="true"</#if>>
                <@columnOptions (field.index)!-99999/>
            </select>
        </div>
        <div>
            <#if p.vocabulary??>
                <#local vocab = vocabTerms[p.vocabulary.uriString]/>
                <select id="fVal${i?c}" class="form-select form-select-sm mp-const" name="fields[${i?c}].defaultValue" aria-label="${label}: <@s.text name='manage.mapping.fields.head.constant'/>">
                    <option value=""></option>
                    <#list vocab?keys as code>
                        <option value="${code}"<#if (field.defaultValue!"") == code> selected</#if>>${vocab.get(code)}</option>
                    </#list>
                </select>
            <#else>
                <input id="fVal${i?c}" class="form-control form-control-sm mp-const" name="fields[${i?c}].defaultValue" value="${field.defaultValue!}" placeholder="<@s.text name='manage.mapping.field.constant.placeholder'/>" aria-label="${label}: <@s.text name='manage.mapping.fields.head.constant'/>"/>
            </#if>
            <#if isDatasetId>
                <div class="form-check mp-doi">
                    <input type="hidden" name="__checkbox_doiUsedForDatasetId" value="true"/>
                    <input class="form-check-input" type="checkbox" id="doiUsedForDatasetId" name="doiUsedForDatasetId" value="true"<#if doiUsedForDatasetId> checked</#if>/>
                    <label class="form-check-label" for="doiUsedForDatasetId"><@s.text name="manage.mapping.datasetIdColumn"/></label>
                </div>
            </#if>
        </div>
        <div class="mp-samples" hidden></div>
        <div class="mp-msgs"></div>
        <#if field.index?? && !isMirrored>
            <a class="mp-translate<#if !p.vocabulary?? && !translations?has_content> quiet</#if>" data-saved-column="${field.index?c}"
               href="translation.do?r=${resource.shortname}&rowtype=${p.extension.rowType?url}&mid=${(mid?c)!}&term=${p.qualname?url}"><#if translations?has_content><@s.text name="manage.mapping.field.translations"><@s.param>${translations?size}</@s.param></@s.text><#else><@s.text name="manage.mapping.field.translate"/></#if></a>
            <#list translations as from, to><span class="mp-tr" hidden data-from="${from}" data-to="${to!}"></span></#list>
        </#if>
    </div>
</#macro>

<form id="mappingForm" action="mapping.do" method="post">
    <input type="hidden" name="r" value="${resource.shortname}"/>
    <input type="hidden" name="id" value="${mapping.extension.rowType}"/>
    <input type="hidden" name="mid" value="${(mid?c)!}"/>
    <input type="hidden" name="mapping.idColumn" id="idColumn" value="${(mapping.idColumn?c)!}"/>

    <#-- messages from the last action: above the heading, as wide as its box -->
    <div class="container px-0 ov-alerts">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <#-- the heading every IPT page has: centred, in a box; what it is, then what you can do with it -->
    <div class="container-fluid border-bottom">
        <div class="container bg-body border rounded-2 mb-4">
            <div class="container my-3 p-3 ov-head">
                <div class="ov-grow">
                    <div class="fs-smaller">
                        <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                            <ol class="breadcrumb justify-content-center mb-0">
                                <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                                <li class="breadcrumb-item"><a href="resource.do?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                                <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.mapping"/></li>
                            </ol>
                        </nav>
                    </div>
                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal"><#if isCoreMapping><@s.text name="manage.mapping.title.core"><@s.param>${extensionTitle}</@s.param></@s.text><#else><@s.text name="manage.mapping.title.extension"><@s.param>${extensionTitle}</@s.param></@s.text></#if></h1>
                    <div class="ov-meta">
                        <span>${resource.title!resource.shortname}</span>
                    </div>
                </div>

                <div class="ov-acts">
                    <button type="submit" class="btn btn-sm btn-gbif-solid" name="save" value="save"><@s.text name="button.save"/></button>
                    <button type="submit" class="btn btn-sm btn-outline-secondary" name="cancel" value="cancel"><@s.text name="button.back"/></button>
                    <#-- confirmed in #mp-delete-modal, which lists the mappings deleted with it -->
                    <button type="button" class="btn btn-sm btn-outline-gbif-danger" data-bs-toggle="modal" data-bs-target="#mp-delete-modal"><@s.text name="button.delete"/></button>
                </div>
            </div>
        </div>
    </div>

    <#-- texts for js/mapping.js -->
    <div id="mp-i18n" hidden
         data-fields-mapped="<@s.text name='manage.mapping.fact.fieldsValue'/>"
         data-required-missing="<@s.text name='manage.mapping.fact.requiredMissing'/>"
         data-none="<@s.text name='manage.mapping.fact.none'/>"
         data-not-set="<@s.text name='manage.mapping.fact.notSet'/>"
         data-no-id="<@s.text name='manage.mapping.id.none'/>"
         data-line-number="<@s.text name='manage.mapping.id.line'/>"
         data-uuid="<@s.text name='manage.mapping.id.uuid'/>"
         data-translations-value="<@s.text name='manage.mapping.fact.translationsValue'/>"
         data-filter-on="<@s.text name='manage.mapping.filter.on'/>"
         data-msg-required="<@s.text name='manage.mapping.msg.required'/>"
         data-msg-vocab="<@s.text name='manage.mapping.msg.vocab'/>"
         data-msg-integer="<@s.text name='manage.mapping.msg.type.integer'/>"
         data-msg-decimal="<@s.text name='manage.mapping.msg.type.decimal'/>"
         data-msg-date="<@s.text name='manage.mapping.msg.type.date'/>"
         data-msg-uri="<@s.text name='manage.mapping.msg.type.uri'/>"
         data-sample="<@s.text name='manage.mapping.field.sample'/>"
         data-empty="<@s.text name='manage.mapping.field.empty'/>"
         data-id-ok="<@s.text name='manage.mapping.id.sample.ok'/>"
         data-id-filled="<@s.text name='manage.mapping.id.sample.filled'/>"
         data-suffix-integer="<@s.text name='validation.mapping.coreid.linenumber.integer'/>"
         data-suffix-same="<@s.text name='validation.mapping.coreid.linenumber.samesufix'/>"
         data-suffix-no-core-line="<@s.text name='validation.mapping.coreid.linenumber.nocoresuffix'/>"
         data-suffix-no-core="<@s.text name='manage.mapping.id.suffix.noCore'/>"
         data-id-dup="<@s.text name='manage.mapping.id.sample.dup'/>"
         data-id-empty="<@s.text name='manage.mapping.id.sample.empty'/>"
         data-filter-kept="<@s.text name='manage.mapping.filter.kept'/>"
         data-filter-skipped="<@s.text name='manage.mapping.filter.skipped'/>"
         data-filter-preview="<@s.text name='manage.mapping.filter.preview'/>"
         data-group-more="<@s.text name='manage.mapping.group.more'/>"
         data-group-mapped="<@s.text name='manage.mapping.group.mapped'/>"
         data-group-problems="<@s.text name='manage.mapping.group.problems'/>"
         data-expand-all="<@s.text name='manage.mapping.fields.expandAll'/>"
         data-collapse-all="<@s.text name='manage.mapping.fields.collapseAll'/>"
         data-search-field="<@s.text name='manage.mapping.fields.search'/>"
         data-search-column="<@s.text name='manage.mapping.fields.searchColumn'/>"
         data-column-n="<@s.text name='manage.mapping.bycol.columnN'/>"
         data-not-used="<@s.text name='manage.mapping.bycol.notUsed'/>"
         data-map-to="<@s.text name='manage.mapping.bycol.mapTo'/>"
         data-remove="<@s.text name='manage.mapping.bycol.remove'/>"
         data-record-id="<#if isCoreMapping><@s.text name='manage.mapping.bycol.id'><@s.param>${idTermName}</@s.param></@s.text><#else><@s.text name='manage.mapping.bycol.link'><@s.param>${idTermName}</@s.param></@s.text></#if>"
         data-translated="<@s.text name='manage.mapping.bycol.translated'/>"
         data-no-results="${selectNoResultsFound}"
         data-unsaved="<@s.text name='manage.mapping.savebar.leave'/>"></div>

    <#-- the 5 sample rows the IPT read, with the data types each value fails (MappingAction.getSampleTypeProblems) -->
    <table id="mp-peek" hidden>
        <#list peek! as row>
            <#if row??>
                <tr><#list columns as col><td data-bad="${(sampleProblems[row?index][col?index])!}">${(row[col?index])!}</td></#list></tr>
            </#if>
        </#list>
    </table>
    <ul id="mp-columns" hidden><#list columns as col><li>${col}</li></#list></ul>

    <div id="sections" class="container-fluid bg-body">
        <div class="container main-content-container mb-md-4 bd-layout">
            <main class="bd-main">
                <div class="bd-toc bd-toc-overview mt-4 pt-3 ps-3 mb-lg-5 text-muted">
                    <div class="fw-semibold text-gbif-header-2 mb-2 text-smaller"><@s.text name="manage.overview.onThisPage"/></div>
                    <nav id="sidebar-content" aria-label="<@s.text name='manage.overview.onThisPage'/>">
                        <ul>
                            <li><a href="#anchor-mp-overview" class="sidebar-navigation-link"><span class="ov-dot" id="mp-dot-overview"></span><@s.text name="manage.mapping.overview.title"/></a></li>
                            <li><a href="#anchor-mp-id" class="sidebar-navigation-link"><span class="ov-dot" id="mp-dot-id"></span><#if isCoreMapping><@s.text name="manage.mapping.id.title"/><#else><@s.text name="manage.mapping.id.title.extension"/></#if></a></li>
                            <li><a href="#anchor-mp-filter" class="sidebar-navigation-link"><span class="ov-dot off" id="mp-dot-filter"></span><@s.text name="manage.mapping.filter.title"/></a></li>
                            <li><a href="#anchor-mp-fields" class="sidebar-navigation-link"><span class="ov-dot" id="mp-dot-fields"></span><@s.text name="manage.mapping.fields.title"/></a></li>
                            <#list fieldsByGroup?keys as g>
                                <#if fieldsByGroup.get(g)?has_content>
                                    <li class="mp-toc-group<#if redundants?seq_contains(g)> redundant</#if>"><a href="#anchor-mp-group-${g?index}" class="sidebar-navigation-link" data-group-link="${g?index}"><span><#if g?has_content>${g}<#else><@s.text name="manage.mapping.noClass"/></#if></span><span class="mp-count" data-group-count="${g?index}"></span></a></li>
                                </#if>
                            </#list>
                        </ul>
                    </nav>
                </div>

                <div class="bd-content ps-lg-4 pt-4">

                    <#-- OVERVIEW -->
                    <span class="mp-anchor" id="anchor-mp-overview"></span>
                    <section id="mp-overview" class="mp-section">
                        <div class="mp-sec-head"><h2><@s.text name="manage.mapping.overview.title"/></h2><span><@s.text name="manage.mapping.overview.desc"/></span></div>
                        <div class="mp-summary">
                            <div>
                                <div class="mp-flow">
                                    <div class="mp-node">
                                        <div class="k"><@s.text name="manage.mapping.overview.source"/></div>
                                        <div class="v"><a href="source.do?r=${resource.shortname}&id=${mapping.source.name?url}">${mapping.source.name}</a></div>
                                        <div class="m">
                                            <#if !mapping.source.readable><span class="ov-missing"><@s.text name="manage.source.notReadable"/></span>
                                            <#elseif mapping.source.isSqlSource()>${columns?size} <@s.text name="manage.overview.source.columns"/>
                                            <#else><#attempt>${mapping.source.rows?string.number} <@s.text name="manage.overview.source.rows"/> · <#recover></#attempt>${columns?size} <@s.text name="manage.overview.source.columns"/></#if>
                                        </div>
                                    </div>
                                    <span class="mp-arrow"><@icon name="arrow_forward"/></span>
                                    <div class="mp-node">
                                        <div class="k"><#if isCoreMapping><@s.text name="manage.mapping.overview.core"/><#elseif coreExtensionTitle?has_content><@s.text name="manage.mapping.overview.extensionOf"><@s.param>${coreExtensionTitle}</@s.param></@s.text><#else><@s.text name="manage.mapping.overview.extension"/></#if></div>
                                        <div class="v"><a href="${baseURL}/admin/extension.do?id=${mapping.extension.rowType?url}" target="_blank">${extensionTitle}</a></div>
                                        <div class="m"><#if mapping.extension.issued??>${mapping.extension.issued?date?string("yyyy-MM-dd")} · </#if><#if mapping.extension.latest><@s.text name="manage.mapping.overview.latest"/><#else><span class="mp-warn-text"><@s.text name="manage.mapping.overview.outdated"/></span></#if></div>
                                    </div>
                                </div>
                            </div>
                            <div class="mp-facts">
                                <div><span><@s.text name="manage.mapping.fact.fields"/></span><span id="mp-fact-fields"></span></div>
                                <div><span><@s.text name="manage.mapping.overview.columns"/></span><span id="mp-fact-columns"></span></div>
                                <div><span><@s.text name="manage.mapping.fact.required"/></span><span id="mp-fact-required"></span></div>
                                <div><span><@s.text name="manage.mapping.fact.problems"/></span><span id="mp-fact-problems"></span></div>
                                <div id="mp-fact-translations-row" hidden><span><@s.text name="manage.mapping.fact.translations"/></span><span id="mp-fact-translations"></span></div>
                                <div><span><#if isCoreMapping><@s.text name="manage.mapping.fact.recordId"/><#else><@s.text name="manage.mapping.fact.linkedBy"/></#if></span><span id="mp-fact-id"></span></div>
                                <div><span><@s.text name="manage.mapping.fact.filter"/></span><span id="mp-fact-filter"></span></div>
                            </div>
                        </div>
                    </section>

                    <#-- RECORD ID / LINK TO THE CORE -->
                    <#assign idColumn = mapping.idColumn!-99999/>
                    <#if !mapping.idColumn?? || idColumn == -3><#assign idMode = isCoreMapping?then("none", "column")/>
                    <#elseif idColumn == -1><#assign idMode = "line"/>
                    <#elseif idColumn == -2><#assign idMode = "uuid"/>
                    <#else><#assign idMode = "column"/></#if>
                    <#-- the other core mappings: an extension links to their IDs, a core mapping mustn't repeat their line numbers -->
                    <#assign coreSetups = action.getCoreIdSetups()/>
                    <#assign coreUsesLines = coreSetups?filter(c -> c.mode == "line")?has_content/>
                    <#if !isCoreMapping>
                        <#-- an extension can't generate UUIDs: they'd point at no core record (the validator says so on save) -->
                        <#if idMode == "uuid"><#assign idMode = "column"/></#if>
                        <#-- line numbers only make sense when the core uses them too; otherwise the column is the only way -->
                        <#assign showIdCards = coreUsesLines || idMode == "line"/>
                    <#else>
                        <#assign showIdCards = true/>
                    </#if>
                    <span class="mp-anchor" id="anchor-mp-id"></span>
                    <section id="mp-id" class="mp-section" data-core="${isCoreMapping?c}" data-has-extensions="${resource.mappings?filter(m -> !m.isCore())?has_content?c}">
                        <div class="mp-sec-head">
                            <h2><#if isCoreMapping><@s.text name="manage.mapping.id.title"/><#else><@s.text name="manage.mapping.id.title.extension"/></#if></h2>
                            <span><#if isCoreMapping><@s.text name="manage.mapping.id.desc"><@s.param>${idTermName}</@s.param></@s.text><#else><@s.text name="manage.mapping.id.desc.extension"><@s.param>${idTermName}</@s.param><@s.param>${coreExtensionTitle}</@s.param></@s.text></#if></span>
                        </div>
                        <ul id="mp-core-setups" hidden><#list coreSetups as c><li data-mode="${c.mode}" data-suffix="${c.suffix}"></li></#list></ul>
                        <#-- the cards and the column select aren't submitted (no form "mp-ui"): js/mapping.js writes the choice into mapping.idColumn -->
                        <#if showIdCards>
                        <div class="mp-cards" role="radiogroup" aria-label="<#if isCoreMapping><@s.text name='manage.mapping.id.title'/><#else><@s.text name='manage.mapping.id.title.extension'/></#if>">
                            <label class="mp-choice">
                                <input type="radio" name="mp-idmode" value="column" form="mp-ui"<#if idMode == "column"> checked</#if>>
                                <span class="t"><@s.text name="manage.mapping.id.column"/><span class="tick"></span></span>
                                <span class="d"><#if isCoreMapping><@s.text name="manage.mapping.id.column.desc"><@s.param>${idTermName}</@s.param></@s.text><#else><@s.text name="manage.mapping.id.column.desc.extension"><@s.param>${idTermName}</@s.param></@s.text></#if></span>
                            </label>
                            <#if isCoreMapping>
                                <label class="mp-choice">
                                    <input type="radio" name="mp-idmode" value="none" form="mp-ui"<#if idMode == "none"> checked</#if>>
                                    <span class="t"><@s.text name="manage.mapping.id.none"/><span class="tick"></span></span>
                                    <span class="d"><@s.text name="manage.mapping.id.none.desc"/></span>
                                </label>
                                <label class="mp-choice<#if !mapping.isTaxonCore()> off</#if>">
                                    <input type="radio" name="mp-idmode" value="line" form="mp-ui"<#if idMode == "line"> checked</#if><#if !mapping.isTaxonCore()> disabled</#if>>
                                    <span class="t"><@s.text name="manage.mapping.id.line"/><span class="tick"></span></span>
                                    <span class="d"><#if mapping.isTaxonCore()><@s.text name="manage.mapping.id.line.desc"/><#else><@s.text name="manage.mapping.id.off.taxon"/></#if></span>
                                </label>
                                <label class="mp-choice<#if !mapping.isTaxonCore()> off</#if>">
                                    <input type="radio" name="mp-idmode" value="uuid" form="mp-ui"<#if idMode == "uuid"> checked</#if><#if !mapping.isTaxonCore()> disabled</#if>>
                                    <span class="t"><@s.text name="manage.mapping.id.uuid"/><span class="tick"></span></span>
                                    <span class="d"><#if mapping.isTaxonCore()><@s.text name="manage.mapping.id.uuid.desc"/><#else><@s.text name="manage.mapping.id.off.taxon"/></#if></span>
                                </label>
                            <#else>
                                <label class="mp-choice">
                                    <input type="radio" name="mp-idmode" value="line" form="mp-ui"<#if idMode == "line"> checked</#if>>
                                    <span class="t"><@s.text name="manage.mapping.id.line.extension"/><span class="tick"></span></span>
                                    <span class="d"><@s.text name="manage.mapping.id.line.desc.extension"/></span>
                                </label>
                            </#if>
                        </div>
                        </#if>

                        <div class="mp-idrow">
                            <div class="mp-id-column">
                                <label class="mp-fl" for="mp-idcol"><@s.text name="manage.mapping.id.columnLabel"><@s.param>${idTermName}</@s.param></@s.text> <span class="text-gbif-danger">*</span></label>
                                <select id="mp-idcol" class="form-select form-select-sm" form="mp-ui">
                                    <@columnOptions (idColumn >= 0)?then(idColumn, -99999)/>
                                </select>
                                <div class="mp-msg bad" id="mp-idcol-missing" hidden><#if isCoreMapping><@s.text name="manage.mapping.id.chooseColumn"/><#else><@s.text name="validation.mapping.coreid.missing"/></#if></div>
                            </div>
                            <div class="mp-id-suffix">
                                <label class="mp-fl" for="mp-idsuffix"><@s.text name="manage.mapping.id.suffix"/> <span class="mp-opt"><@s.text name="manage.mapping.id.suffix.optional"/></span></label>
                                <input id="mp-idsuffix" type="text" name="mapping.idSuffix" value="${mapping.idSuffix!}" class="form-control form-control-sm"/>
                                <#if isCoreMapping>
                                    <div class="mp-msg mu mp-suffix-column"><@s.text name="manage.mapping.id.suffix.help"/></div>
                                    <div class="mp-msg mu mp-suffix-line"><@s.text name="manage.mapping.id.suffix.help.line"/></div>
                                <#else>
                                    <div class="mp-msg mu"><@s.text name="manage.mapping.id.suffix.help.extension"/></div>
                                </#if>
                                <#-- filled in by js/mapping.js: the suffix checks of ExtensionMappingValidator, before saving -->
                                <div class="mp-msg" id="mp-suffix-problem" hidden></div>
                            </div>
                        </div>
                        <div class="mp-id-samples" id="mp-id-samples"></div>
                        <div class="mp-msg w mp-id-none" hidden><@s.text name="manage.mapping.id.none.warn"/></div>
                        <div class="mp-msg bad" id="mp-id-uuid-ext" hidden><@s.text name="validation.mapping.coreid.uuid.extensions.exist"/></div>

                        <#-- what the core does, so the two can be compared -->
                        <#if !isCoreMapping>
                            <div class="mp-core-setups">
                                <#list coreSetups as c>
                                    <div class="mp-msg <#if c.mode == "none" || c.mode == "uuid">bad<#else>mu</#if>">
                                        <#if c.mode == "column">
                                            <#if c.suffix?has_content><@s.text name="manage.mapping.id.core.columnSuffix"><@s.param>${c.source}</@s.param><@s.param>${c.column}</@s.param><@s.param>${c.suffix}</@s.param></@s.text><#else><@s.text name="manage.mapping.id.core.column"><@s.param>${c.source}</@s.param><@s.param>${c.column}</@s.param></@s.text></#if>
                                        <#elseif c.mode == "line">
                                            <#if c.suffix?has_content><@s.text name="manage.mapping.id.core.lineSuffix"><@s.param>${c.source}</@s.param><@s.param>${c.suffix}</@s.param></@s.text><#else><@s.text name="manage.mapping.id.core.line"><@s.param>${c.source}</@s.param></@s.text></#if>
                                        <#elseif c.mode == "uuid">
                                            <@s.text name="manage.mapping.id.core.uuid"><@s.param>${c.source}</@s.param></@s.text>
                                        <#else>
                                            <@s.text name="manage.mapping.id.core.none"><@s.param>${c.source}</@s.param></@s.text>
                                        </#if>
                                    </div>
                                </#list>
                            </div>
                        </#if>
                        <#if !isCoreMapping && coreIdTermFieldsIndex?has_content>
                            <div class="mp-msg mu mp-id-mirror"><@icon name="lock"/> <@s.text name="manage.mapping.id.mirrored"><@s.param>${idTermName}</@s.param></@s.text></div>
                        </#if>
                        <#if idProblem?has_content>
                            <div class="mp-msg bad" id="mp-id-server-problem"><@icon name="error"/> ${idProblem}</div>
                        </#if>
                    </section>

                    <#-- FILTER -->
                    <#assign filterOn = mapping.filter.column?? && mapping.filter.comparator??/>
                    <span class="mp-anchor" id="anchor-mp-filter"></span>
                    <section id="mp-filter" class="mp-section">
                        <div class="mp-sec-head"><h2><@s.text name="manage.mapping.filter.title"/></h2><span><@s.text name="manage.mapping.filter.desc"/></span></div>
                        <div class="mp-sentence">
                            <label class="mp-filter-on"><input type="checkbox" class="form-check-input" id="mp-filter-on" form="mp-ui"<#if filterOn> checked</#if>> <@s.text name="manage.mapping.filter.sentence"/></label>
                            <select id="filterName" name="mapping.filter.column" class="form-select form-select-sm" aria-label="<@s.text name='manage.mapping.fields.head.column'/>">
                                <@columnOptions (mapping.filter.column)!-99999/>
                            </select>
                            <select id="filterComp" name="mapping.filter.comparator" class="form-select form-select-sm" aria-label="<@s.text name='manage.mapping.filter.comparator'/>">
                                <option value=""></option>
                                <#list comparators as c>
                                    <option value="${c}"<#if c == mapping.filter.comparator!""> selected</#if>><@s.text name="manage.mapping.filter.${c}"/></option>
                                </#list>
                            </select>
                            <input id="filterParam" name="mapping.filter.param" class="form-control form-control-sm" value="${mapping.filter.param!}" placeholder="<@s.text name='manage.mapping.filter.value'/>" aria-label="<@s.text name='manage.mapping.filter.value'/>"/>
                            <span class="mp-filter-when"><@s.text name="manage.mapping.filter.checked"/></span>
                            <select id="filterTime" name="mapping.filter.filterTime" class="form-select form-select-sm" aria-label="<@s.text name='manage.mapping.filter.time'/>">
                                <#list ["BeforeTranslation", "AfterTranslation"] as t>
                                    <option value="${t}"<#if t == (mapping.filter.filterTime!"")?string> selected</#if>><#if t == "BeforeTranslation"><@s.text name="manage.mapping.filter.time.before"/><#else><@s.text name="manage.mapping.filter.time.after"/></#if></option>
                                </#list>
                            </select>
                        </div>
                        <div class="mp-msg mu" id="mp-filter-off"><@s.text name="manage.mapping.filter.off"/></div>
                        <div id="mp-filter-preview"></div>
                    </section>

                    <#-- FIELDS -->
                    <#assign groupCount = 0/>
                    <#list fieldsByGroup?keys as g><#if fieldsByGroup.get(g)?has_content><#assign groupCount = groupCount + 1/></#if></#list>
                    <span class="mp-anchor" id="anchor-mp-fields"></span>
                    <section id="mp-fields" class="mp-section">
                        <div class="mp-sec-head"><h2><@s.text name="manage.mapping.fields.title"/></h2><span><@s.text name="manage.mapping.fields.desc"><@s.param>${extensionTitle}</@s.param><@s.param>${fields?size}</@s.param><@s.param>${groupCount}</@s.param></@s.text></span></div>
                        <div class="mp-toolbar">
                            <div class="mp-seg" id="mp-view" role="group">
                                <button type="button" data-view="field" class="on"><@s.text name="manage.mapping.fields.byField"/></button>
                                <button type="button" data-view="column"><@s.text name="manage.mapping.fields.byColumn"/></button>
                            </div>
                            <div class="mp-seg" id="mp-show" role="group">
                                <button type="button" data-show="default" class="on"><@s.text name="manage.mapping.show.default"/><span class="mp-count"></span></button>
                                <button type="button" data-show="problems"><@s.text name="manage.mapping.show.problems"/><span class="mp-count"></span></button>
                                <button type="button" data-show="unmapped"><@s.text name="manage.mapping.show.unmapped"/><span class="mp-count"></span></button>
                                <button type="button" data-show="all"><@s.text name="manage.mapping.show.all"/><span class="mp-count"></span></button>
                            </div>
                            <input type="search" id="mp-q" class="form-control form-control-sm" form="mp-ui" placeholder="<@s.text name='manage.mapping.fields.search'/>" aria-label="<@s.text name='manage.mapping.fields.search'/>">
                            <span class="ov-sp"></span>
                            <button type="button" class="ov-link-btn" id="mp-expand-all"><@s.text name="manage.mapping.fields.expandAll"/></button>
                        </div>

                        <div id="mp-byfield">
                            <div class="mp-fhead"><span><@s.text name="manage.mapping.fields.head.field"/></span><span><@s.text name="manage.mapping.fields.head.column"/></span><span><@s.text name="manage.mapping.fields.head.constant"/></span></div>
                            <#list fieldsByGroup?keys as g>
                                <#assign groupFields = fieldsByGroup.get(g)/>
                                <#if groupFields?has_content>
                                    <#assign isRedundant = redundants?seq_contains(g)/>
                                    <span class="mp-anchor" id="anchor-mp-group-${g?index}"></span>
                                    <div class="mp-group<#if isRedundant> redundant</#if>" id="mp-group-${g?index}" data-g="${g?index}">
                                        <button type="button" class="mp-group-h" aria-expanded="false" aria-controls="mp-group-b-${g?index}">
                                            <h3><#if g?has_content>${g}<#else><@s.text name="manage.mapping.noClass"/></#if></h3>
                                            <#if isRedundant><span class="mp-redundant"><@s.text name="manage.mapping.group.redundant"/></span></#if>
                                            <span class="ov-sp"></span>
                                            <span class="mp-count" data-group-problems hidden></span>
                                            <span class="mp-count" data-group-mapped></span>
                                            <@icon name="expand_more" class="mp-chev"/>
                                        </button>
                                        <div class="mp-group-b" id="mp-group-b-${g?index}">
                                            <#list groupFields as f>
                                                <@fieldRow f g?index?c/>
                                            </#list>
                                            <div class="mp-group-more" hidden><span></span> · <button type="button" class="ov-link-btn" data-show-group><@s.text name="manage.mapping.group.showThem"/></button></div>
                                        </div>
                                    </div>
                                </#if>
                            </#list>
                            <div class="ov-empty" id="mp-nomatch" hidden><b><@s.text name="manage.mapping.fields.noMatch"/></b><button type="button" class="ov-link-btn" id="mp-clear-q"><@s.text name="manage.mapping.fields.clearSearch"/></button></div>
                        </div>

                        <div id="mp-bycolumn" hidden>
                            <table class="ov-table mp-cols">
                                <thead><tr><th><@s.text name="manage.mapping.bycol.column"/></th><th><@s.text name="manage.mapping.bycol.samples"/></th><th><@s.text name="manage.mapping.bycol.publishedAs"/></th></tr></thead>
                                <tbody></tbody>
                            </table>
                        </div>
                    </section>
                </div>
            </main>
        </div>
    </div>

    <div class="mp-savebar" id="mp-savebar" aria-hidden="true">
        <div class="container">
            <span class="ov-grow"><@s.text name="manage.mapping.savebar.unsaved"/></span>
            <a class="btn btn-sm btn-outline-secondary" href="mapping.do?r=${resource.shortname}&id=${mapping.extension.rowType?url}&mid=${(mid?c)!}" id="mp-discard"><@s.text name="manage.mapping.savebar.discard"/></a>
            <button type="submit" class="btn btn-sm btn-gbif-solid" name="save" value="save"><@s.text name="button.save"/></button>
        </div>
    </div>
</form>

<div class="modal fade" id="mp-delete-modal" tabindex="-1" aria-labelledby="mp-delete-modal-title" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="mp-delete-modal-title"><@s.text name="manage.mapping.delete.modal.title"/></h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="<@s.text name='button.cancel'/>"></button>
            </div>
            <div class="modal-body text-smaller">
                <#if alsoDeleted?has_content>
                    <p><@s.text name="manage.mapping.delete.modal.core"/></p>
                    <ul>
                        <#list alsoDeleted as m>
                            <li>${(m.extension.title)!m.extension.rowType} ← ${(m.source.name)!""}</li>
                        </#list>
                    </ul>
                    <p class="mb-0"><@s.text name="manage.mapping.delete.modal.kept.core"/></p>
                <#else>
                    <p class="mb-0"><@s.text name="manage.mapping.delete.modal.text"><@s.param>${extensionTitle}</@s.param><@s.param>${mapping.source.name}</@s.param></@s.text></p>
                </#if>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal"><@s.text name="button.cancel"/></button>
                <button type="submit" form="mappingForm" class="btn btn-sm btn-danger" name="deleteFlag" value="delete"><#if alsoDeleted?has_content><@s.text name="manage.mapping.delete.modal.button.n"><@s.param>${alsoDeleted?size + 1}</@s.param></@s.text><#else><@s.text name="button.delete"/></#if></button>
            </div>
        </div>
    </div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl"/>
