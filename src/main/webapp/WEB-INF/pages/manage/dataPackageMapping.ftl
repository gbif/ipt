<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.DataPackageMappingAction" -->
<#-- Mapping of a source to a table of a data package schema (DwC-DP, Camtrap DP, ColDP). Built like the DwC-A mapping page
     (mapping.ftl) and run by the same script, js/mapping.js, which reads the markup below: no record ID section (a table's
     keys are fields like the others), one list of fields, and the sample values publishing rejects checked on the server -->
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<#include "/WEB-INF/pages/inc/header.ftl"/>
<#assign tableSchema = action.getTableSchema()!{}/>
<#assign tableName = (mapping.dataPackageTableSchemaName.name)!""/>
<#assign tableTitle = (tableSchema.title)!""/>
<#if !tableTitle?has_content><#assign tableTitle = tableName/></#if>
<title><@s.text name="manage.mapping.title.table"><@s.param>${tableTitle}</@s.param></@s.text>: ${resource.title!resource.shortname}</title>
<style>
    /* --ov-bars-height: where the sidebar sticks (overview.css), just under the fixed navbar, as there is no resource bar here */
    :root { --navbar-height: 120px; --ov-bars-height: 57px; }
</style>
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2-ipt.css">
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/mapping.css">
<script src="${baseURL}/js/scrollspy.js"></script>
<script src="${baseURL}/js/select2/select2-4.0.13.full.min.js"></script>
<script src="${baseURL}/js/mapping.js"></script>
<script src="${baseURL}/js/dp-rows.js"></script>

<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl"/>

<#assign primaryKey = action.getPrimaryKey()/>
<#assign rowMode = (mapping.rowMode.name())!"ROW"/>
<#assign splitColumns = mapping.splitColumns![]/>
<#assign mergeKey = mapping.mergeKey!""/>
<#assign foreignKeys = action.getForeignKeys()/>
<#assign expectations = action.getFieldExpectations()/>
<#assign fieldConstraints = action.getFieldConstraints()/>
<#assign badCells = action.getBadSampleCells()/>
<#assign badTranslations = action.getBadTranslations()/>
<#assign badConstants = action.getBadConstants()/>
<#assign unresolved = (mapping.unresolvedFieldMappings)![]/>
<#assign tableMissing = !tableSchema?has_content && tableName?has_content/>
<#assign schemaChanged = (mapping.version)?? && mapping.version != dataPackageSchema.version/>

<#-- schema texts: HTML escaped, then `code` and [text](link) -->
<#macro md text>${(text?esc?markup_string)?replace("`(.*?)`", "<code>$1</code>", "r")?replace("\\[([^\\]]+)\\]\\(([^\\)]+)\\)", "<a href=\"$2\" target=\"_blank\">$1</a>", "r")?no_esc}</#macro>

<#-- an example (Camtrap DP) or examples (DwC-DP): a string, a number, a boolean or a list -->
<#macro examples ex><#compress>
    <#if ex?is_enumerable><#list ex as e><code><#if e?is_string>${e}<#else>${e?c}</#if></code><#sep>, </#sep></#list>
    <#elseif ex?is_string><@md ex/>
    <#else><code>${ex?c}</code></#if>
</#compress></#macro>

<#macro columnOptions selected=-99999>
    <option value=""></option>
    <#list columns as col>
        <option value="${col_index}"<#if selected == col_index> selected</#if>>${col}</option>
    </#list>
</#macro>

<#macro fieldRow fm i>
    <#local f = fm.field/>
    <#local label = (f.title?has_content)?then(f.title, f.name)/>
    <#local type = (f.type?has_content)?then(f.type?lower_case, "string")/>
    <#local format = (f.format?has_content && f.format != "default")?then(f.format, "")/>
    <#local c = f.constraints!{}/>
    <#local vocabulary = (c.vocabulary)![]/>
    <#local isPk = primaryKey?seq_contains(f.name)/>
    <#local fk = foreignKeys[f.name]!""/>
    <#local translations = fm.translation!{}/>
    <#local badTr = badTranslations[f.name]![]/>
    <#local badConst = badConstants[f.name]!""/>
    <#local constraints = fieldConstraints[f.name]![]/>
    <div class="mp-field" id="field-${i?c}" data-i="${i?c}" data-group="0" data-label="${label}" data-term="${f.name}"
         data-required="${f.required?c}" data-type="${type}" data-unique="${((c.unique)!false)?c}"
         data-enum="${vocabulary?has_content?c}" data-expect="${expectations[f.name]!}" data-bad="${badCells[f.name]!}"<#if badConst?has_content> data-bad-const="${badConst}"</#if>
         data-search="${(label + ' ' + f.name + ' ' + (f.description!''))?lower_case}">
        <div class="mp-name">
            <div class="mp-label">
                <button type="button" class="mp-info" aria-expanded="false" aria-controls="field-def-${i?c}" title="<@s.text name='manage.mapping.field.definition'/>">${label}</button>
            </div>
            <#-- the name is the label already when the schema has no titles (Camtrap DP) -->
            <div class="mp-term"><#if label != f.name>${f.name}</#if>
                <#if type != "string" && type != "any"><span class="mp-badge t">${type}</span><#elseif format?has_content><span class="mp-badge t">${format}</span></#if>
                <#if isPk><span class="mp-badge k" title="<@s.text name='manage.tables.key.help'/>"><@s.text name="manage.mapping.field.pk"/></span></#if>
                <#if fk?has_content><span class="mp-badge k" title="<@s.text name='manage.tables.link.help'><@s.param>${fk?keep_before_last(".")}</@s.param></@s.text>">→ ${fk?keep_before_last(".")}</span></#if>
                <#if f.required><span class="mp-badge r"><@s.text name="manage.mapping.field.required"/></span></#if>
            </div>
            <div class="mp-def" id="field-def-${i?c}" hidden>
                <#if f.description?has_content><p><@md f.description/></p><#else><p><@s.text name="basic.no.description"/></p></#if>
                <#if f.comments?has_content><p><@md f.comments/></p></#if>
                <#if (f.example)?has_content><p><em><@s.text name="basic.examples"/></em>: <@examples f.example/></p></#if>
                <p><em><@s.text name="schema.field.type"/></em>: <code>${type}</code><#if format?has_content>, <em><@s.text name="schema.field.format"/></em>: <code>${format}</code></#if><#if constraints?has_content>, <em><@s.text name="schema.field.constraints"/></em>: <#list constraints as x><code>${x}</code><#sep>, </#sep></#list></#if></p>
                <#if vocabulary?has_content><p><em><@s.text name="manage.mapping.field.values"/></em>: <#list vocabulary as v><code>${v}</code><#sep>, </#sep></#list></p></#if>
                <#if fk?has_content><p><em><@s.text name="manage.mapping.field.fk"/></em>: <code>${fk}</code></p></#if>
            </div>
        </div>
        <div>
            <#local gen = (fm.valueKind.name())!""/>
            <#-- open when set, and on keys, which are often generated; other fields behind "Generate a value" -->
            <#local showGen = gen?has_content || isPk || fk?has_content/>
            <div class="mp-colcell<#if gen?has_content && gen != "ID"> mp-generated</#if>">
                <select id="fIdx${i?c}" class="form-select form-select-sm mp-col" name="fields[${i?c}].index" aria-label="${label}: <@s.text name='manage.mapping.fields.head.column'/>">
                    <@columnOptions (fm.index)!-99999/>
                </select>
            </div>
            <div class="mp-gen"<#if !showGen> hidden</#if>>
                <select id="fGen${i?c}" class="form-select form-select-sm mp-gen-kind" name="valueKind_${i?c}" aria-label="${label}: <@s.text name='manage.mapping.gen.label'/>">
                    <option value=""><@s.text name="manage.mapping.gen.none"/></option>
                    <option value="ITEM"<#if gen == "ITEM"> selected</#if>><@s.text name="manage.mapping.gen.ITEM"/></option>
                    <option value="POSITION"<#if gen == "POSITION"> selected</#if>><@s.text name="manage.mapping.gen.POSITION"/></option>
                    <option value="ID"<#if gen == "ID"> selected</#if>><@s.text name="manage.mapping.gen.ID"/></option>
                    <#if fk?has_content><option value="LINK"<#if gen == "LINK"> selected</#if>><@s.text name="manage.mapping.gen.LINK"><@s.param>${fk?keep_before_last(".")}</@s.param></@s.text></option></#if>
                    <option value="TEMPLATE"<#if gen == "TEMPLATE"> selected</#if>><@s.text name="manage.mapping.gen.TEMPLATE"/></option>
                    <option value="PAIRED"<#if gen == "PAIRED"> selected</#if>><@s.text name="manage.mapping.gen.PAIRED"/></option>
                </select>
                <input id="fTpl${i?c}" class="form-control form-control-sm mp-gen-tpl" name="template_${i?c}" value="${fm.template!}" placeholder="{occurrenceID}-id" aria-label="${label}: <@s.text name='manage.mapping.gen.TEMPLATE'/>"<#if gen != "TEMPLATE"> hidden</#if>/>
                <#-- PAIRED: for each list column of the saved Rows, the list whose item at the same place is taken -->
                <div class="mp-gen-help mp-gen-pairs" data-kind-help="PAIRED"<#if gen != "PAIRED"> hidden</#if>>
                    <@s.text name="manage.mapping.gen.PAIRED.help"/>
                    <#if splitColumns?has_content>
                        <#local pairs = fm.pairColumns![]/>
                        <#list splitColumns as sc>
                            <#local chosen = (pairs[sc?index])!-1/>
                            <label class="mp-pair"><span><@s.text name="manage.mapping.gen.PAIRED.for"><@s.param>${columns[sc]!sc}</@s.param></@s.text></span>
                                <select class="form-select form-select-sm" name="pair_${i?c}_${sc?index}">
                                    <option value=""></option>
                                    <#list columns as col><option value="${col_index}"<#if col_index == chosen> selected</#if>>${col}</option></#list>
                                </select>
                            </label>
                        </#list>
                    <#else>
                        <div><@s.text name="manage.mapping.gen.PAIRED.noLists"/></div>
                    </#if>
                </div>
                <div class="mp-gen-help" data-kind-help="ITEM"<#if gen != "ITEM"> hidden</#if>><@s.text name="manage.mapping.gen.ITEM.help"/></div>
                <div class="mp-gen-help" data-kind-help="POSITION"<#if gen != "POSITION"> hidden</#if>><@s.text name="manage.mapping.gen.POSITION.help"/></div>
                <#if fk?has_content><div class="mp-gen-help" data-kind-help="LINK"<#if gen != "LINK"> hidden</#if>><@s.text name="manage.mapping.gen.LINK.help"><@s.param>${fk?keep_before_last(".")}</@s.param></@s.text></div></#if>
                <div class="mp-gen-help" data-kind-help="ID"<#if gen != "ID"> hidden</#if>><@s.text name="manage.mapping.gen.ID.help"/></div>
                <div class="mp-gen-help" data-kind-help="TEMPLATE"<#if gen != "TEMPLATE"> hidden</#if>><@s.text name="manage.mapping.gen.TEMPLATE.help"><@s.param>{occurrenceID}-id, {item}, {position}</@s.param></@s.text></div>
            </div>
            <button type="button" class="ov-link-btn mp-gen-open"<#if showGen> hidden</#if>><@s.text name="manage.mapping.gen.open"/></button>
        </div>
        <div>
            <#if vocabulary?has_content || type == "boolean">
                <#local options = vocabulary?has_content?then(vocabulary, ["true", "false"])/>
                <select id="fVal${i?c}" class="form-select form-select-sm mp-const" name="fields[${i?c}].defaultValue" aria-label="${label}: <@s.text name='manage.mapping.fields.head.constant'/>">
                    <option value=""></option>
                    <#list options as code>
                        <option value="${code}"<#if (fm.defaultValue!"") == code> selected</#if>>${code}</option>
                    </#list>
                </select>
            <#else>
                <input id="fVal${i?c}" class="form-control form-control-sm mp-const" name="fields[${i?c}].defaultValue" value="${fm.defaultValue!}" placeholder="<@s.text name='manage.mapping.field.constant.placeholder'/>" aria-label="${label}: <@s.text name='manage.mapping.fields.head.constant'/>"/>
            </#if>
        </div>
        <div class="mp-samples" hidden></div>
        <div class="mp-msgs"></div>
        <#if fm.index??>
            <#-- translations belong to the saved column: the translation page edits the saved mapping -->
            <a class="mp-translate<#if !vocabulary?has_content && !translations?has_content> quiet</#if>" data-saved-column="${fm.index?c}"
               href="dataPackageFieldTranslation.do?r=${resource.shortname}&mid=${(mid?c)!}&field=${f.name?url}"><#if translations?has_content><@s.text name="manage.mapping.field.translations"><@s.param>${translations?size}</@s.param></@s.text><#else><@s.text name="manage.mapping.field.translate"/></#if></a>
            <#list translations as from, to><span class="mp-tr" hidden data-from="${from}" data-to="${to!}"<#if badTr?seq_contains(to!"")> data-bad="true"</#if>></span></#list>
        </#if>
    </div>
</#macro>

<form id="mappingForm" action="dataPackageMapping.do" method="post">
    <input type="hidden" name="r" value="${resource.shortname}"/>
    <input type="hidden" name="id" value="${mapping.dataPackageSchema.identifier}"/>
    <input type="hidden" name="mid" value="${(mid?c)!}"/>

    <#-- messages from the last action: above the heading, as wide as its box -->
    <div class="container px-0 ov-alerts">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

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
                    <#-- the table's name; its schema is the pill below -->
                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">${tableTitle}</h1>
                    <div class="ov-meta">
                        <span>${resource.title!resource.shortname}</span>
                    </div>
                    <#-- the schema and the source; "Columns not used" (js/mapping.js) opens the by-column view -->
                    <div class="ov-meta mp-source-line">
                        <#-- short schema name: the title's abbreviation in brackets, DwC-DP by identifier, else the title.
                             Not shortTitle: DwC-DP's is "dwc-dp" -->
                        <#assign schemaTitle = dataPackageSchema.title!dataPackageSchema.name!""/>
                        <#assign schemaAbbr = schemaTitle?matches(".*\\(([^()]+)\\)\\s*$")/>
                        <#if schemaAbbr><#assign schemaLabel = schemaAbbr?groups[1]/>
                        <#elseif dataPackageSchema.identifier == "http://rs.tdwg.org/dwc/dwc-dp"><#assign schemaLabel = "DwC-DP"/>
                        <#else><#assign schemaLabel = schemaTitle/></#if>
                        <span class="mp-kind-pill">${schemaLabel}</span>
                        <span><@s.text name="manage.mapping.overview.source"/>: <a href="source.do?r=${resource.shortname}&id=${mapping.source.name?url}">${mapping.source.name}</a> · <#t>
                            <#if !mapping.source.readable><span class="ov-missing"><@s.text name="manage.source.notReadable"/></span><#t>
                            <#elseif mapping.source.isSqlSource()>${columns?size} <@s.text name="manage.overview.source.columns"/><#t>
                            <#else><#attempt>${mapping.source.rows?string.number} <@s.text name="manage.overview.source.rows"/> · <#recover></#attempt>${columns?size} <@s.text name="manage.overview.source.columns"/></#if><#t>
                            <span id="mp-unused" hidden> · <button type="button" class="ov-link-btn mu" data-goto-view="column"></button></span><#t>
                        </span>
                    </div>
                    <#-- what still needs fixing (js/mapping.js), red / orange; empty and hidden otherwise. A newer schema in
                         the registry is listed there too: administrators get the schema page, where it's updated. Only
                         on small screens, which have no sidebar: the Readiness card lists the same -->
                    <div class="ov-meta mp-status mp-status-sm" id="mp-status" aria-live="polite"<#if !dataPackageSchema.latest> data-newer="<@s.text name='manage.mapping.extension.newer'/>"<#if adminRights> data-newer-url="${baseURL}/admin/dataPackage.do?id=${dataPackageSchema.identifier?url}"</#if></#if>></div>
                    <ul id="mp-status-extra" hidden>
                        <#if tableMissing><li data-cls="bad" data-goto="mp-table-missing"><@s.text name="manage.overview.mappings.tableMissing"/></li></#if>
                        <#if unresolved?has_content && !tableMissing><li data-cls="w" data-goto="mp-unresolved"><@s.text name="manage.mapping.status.unresolved"><@s.param>${unresolved?size}</@s.param></@s.text></li></#if>
                        <#-- the Result's red checks (rows with the same key but different values, broken links...): they stop publishing too -->
                        <#assign pvTop = action.getPreview()!/>
                        <#if pvTop?has_content && pvTop.columns?has_content>
                            <#assign resultProblems = pvTop.problemCount/>
                            <#if resultProblems gt 0><li data-cls="bad" data-goto="mp-result"><@s.text name="manage.mapping.status.result"><@s.param>${resultProblems?c}</@s.param></@s.text></li></#if>
                        </#if>
                        <#if schemaChanged && !tableMissing><li data-cls="w" data-goto="mp-fields"><@s.text name="manage.mapping.status.schemaChanged"><@s.param>${mapping.version}</@s.param><@s.param>${dataPackageSchema.version}</@s.param></@s.text></li></#if>
                    </ul>
                </div>

                <div class="ov-acts">
                    <button type="submit" class="btn btn-sm btn-gbif-solid top-button" name="save" value="save"><@icon name="save"/> <@s.text name="button.save"/></button>
                    <button type="submit" class="btn btn-sm btn-outline-secondary top-button" name="cancel" value="cancel"><@s.text name="button.back"/></button>
                    <#-- more actions last, as on the resource overview -->
                    <div class="dropdown">
                        <button type="button" class="btn btn-sm btn-outline-secondary top-button ov-more-btn" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='manage.overview.moreActions'/>" aria-label="<@s.text name='manage.overview.moreActions'/>"><@icon name="more_horiz"/></button>
                        <ul class="dropdown-menu dropdown-menu-end">
                            <li><button type="button" class="dropdown-item text-gbif-danger" data-bs-toggle="modal" data-bs-target="#mp-delete-modal"><@s.text name="button.delete"/></button></li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <#-- texts for js/mapping.js -->
    <div id="mp-i18n" hidden
         data-automapped="${automappedFields?c}"
         data-sprite="${baseURL}/images/icons/material-icons.svg"
         data-status-required="<@s.text name='manage.mapping.status.required'/>"
         data-status-problems="<@s.text name='manage.mapping.status.problems'/>"
         data-status-unused="<@s.text name='manage.mapping.status.unused'/>"
         data-ready="<@s.text name='manage.overview.attention.readiness'/>"
         data-ready-title="<@s.text name='manage.overview.attention.title'/>"
         data-ready-none="<@s.text name='manage.mapping.ready.none'/>"
         data-msg-required="<@s.text name='manage.mapping.msg.required'/>"
         data-msg-required-empty="<@s.text name='manage.mapping.msg.requiredEmpty'/>"
         data-msg-enum="<@s.text name='manage.mapping.msg.enum'/>"
         data-msg-invalid="<@s.text name='manage.mapping.msg.invalid'/>"
         data-id-dup="<@s.text name='manage.mapping.id.sample.dup'/>"
         data-sample="<@s.text name='manage.mapping.field.sample'/>"
         data-empty="<@s.text name='manage.mapping.field.empty'/>"
         data-all-empty="<@s.text name='manage.mapping.field.allEmpty'/>"
         data-const-all="<@s.text name='manage.mapping.field.constant.placeholder'/>"
         data-const-fallback="<@s.text name='manage.mapping.field.constant.fallback'/>"
         data-filter-kept="<@s.text name='manage.mapping.filter.kept'/>"
         data-filter-skipped="<@s.text name='manage.mapping.filter.skipped'/>"
         data-filter-preview="<@s.text name='manage.mapping.filter.preview'/>"
         data-group-more="<@s.text name='manage.mapping.group.more'/>"
         data-group-mapped="<@s.text name='manage.mapping.group.mapped'/>"
         data-group-problems="<@s.text name='manage.mapping.group.problems'/>"
         data-hide-samples="<@s.text name='manage.mapping.fields.hideSamples'/>"
         data-show-samples="<@s.text name='manage.mapping.fields.showSamples'/>"
         data-search-field="<@s.text name='manage.mapping.fields.search.table'/>"
         data-search-column="<@s.text name='manage.mapping.fields.searchColumn'/>"
         data-column-n="<@s.text name='manage.mapping.bycol.columnN'/>"
         data-not-used="<@s.text name='manage.mapping.bycol.notUsed'/>"
         data-map-to="<@s.text name='manage.mapping.bycol.mapTo'/>"
         data-remove="<@s.text name='manage.mapping.bycol.remove'/>"
         data-translated="<@s.text name='manage.mapping.bycol.translated'/>"
         data-no-results="${selectNoResultsFound}"
         data-unsaved="<@s.text name='manage.mapping.savebar.leave'/>"></div>

    <#-- the 5 sample rows the IPT read; what each field rejects is on the field (data-bad). A row it couldn't read
         stays as an empty row: data-bad numbers the rows as read -->
    <table id="mp-peek" hidden>
        <#list peek! as row>
            <tr><#if row??><#list columns as col><td>${(row[col?index])!}</td></#list></#if></tr>
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
                            <#if tableMissing>
                                <li><a href="#anchor-mp-table-missing" class="sidebar-navigation-link"><span class="ov-dot bad"></span><@s.text name="manage.overview.mappings.tableMissing"/></a></li>
                            <#else>
                                <#if unresolved?has_content>
                                    <li><a href="#anchor-mp-unresolved" class="sidebar-navigation-link"><span class="ov-dot warn"></span><@s.text name="manage.mapping.unresolved.title"/></a></li>
                                </#if>
                                <li><a href="#anchor-mp-rows" class="sidebar-navigation-link"><span class="ov-dot<#if rowMode == "ROW"> off</#if>"></span><@s.text name="manage.mapping.rows.title"/></a></li>
                                <li><a href="#anchor-mp-filter" class="sidebar-navigation-link"><span class="ov-dot off" id="mp-dot-filter"></span><@s.text name="manage.mapping.filter.title"/></a></li>
                                <li><a href="#anchor-mp-fields" class="sidebar-navigation-link"><span class="ov-dot" id="mp-dot-fields"></span><span><@s.text name="manage.mapping.fields.title"/></span><span class="mp-count" data-group-count="0"></span></a></li>
                                <#if (mid)??><li><a href="#anchor-mp-result" class="sidebar-navigation-link"><span class="ov-dot <#if !(resultProblems??)>off<#elseif resultProblems gt 0>bad<#else>ok</#if>"></span><@s.text name="manage.mapping.result.title"/></a></li></#if>
                            </#if>
                        </ul>
                    </nav>

                    <#-- what needs fixing, as on the resource overview; filled by js/mapping.js -->
                    <div class="ov-ready" id="mp-ready" aria-live="polite"><b></b><ul></ul></div>
                </div>

                <div class="bd-content ps-lg-4 pt-4">

                    <#if tableMissing>
                        <#-- TABLE NOT IN THE INSTALLED SCHEMA: nothing to map -->
                        <span class="mp-anchor" id="anchor-mp-table-missing"></span>
                        <section id="mp-table-missing" class="mp-section">
                            <div class="mp-sec-head"><h2><@s.text name="manage.overview.mappings.tableMissing"/></h2></div>
                            <div class="mp-msg bad"><@icon name="error"/>
                                <span><@s.text name="manage.mapping.tableMissing.info">
                                    <@s.param><strong>${tableName}</strong></@s.param>
                                    <@s.param>${dataPackageSchema.name}</@s.param>
                                    <@s.param>${dataPackageSchema.version}</@s.param>
                                </@s.text></span>
                            </div>
                        </section>
                    <#else>
                        <#if unresolved?has_content>
                            <#-- FIELDS NOT IN THE INSTALLED SCHEMA: what to do with each, on save -->
                            <span class="mp-anchor" id="anchor-mp-unresolved"></span>
                            <section id="mp-unresolved" class="mp-section">
                                <div class="mp-sec-head"><h2><@s.text name="manage.mapping.unresolved.title"/></h2></div>
                                <div class="mp-msg w mb-3"><@icon name="warning"/>
                                    <span><@s.text name="manage.mapping.unresolved.info">
                                        <@s.param>${dataPackageSchema.name}</@s.param>
                                        <@s.param>${dataPackageSchema.version}</@s.param>
                                    </@s.text></span>
                                </div>
                                <div class="mp-unres-list">
                                    <#list unresolved as u>
                                        <div class="mp-unres">
                                            <div>
                                                <div class="mp-label">${u.field.name}</div>
                                                <div class="mp-term">
                                                    <#if u.index??><@s.text name="manage.mapping.unresolved.column"><@s.param>${columns[u.index]!("#" + u.index)}</@s.param></@s.text></#if>
                                                    <#if u.defaultValue?has_content><@s.text name="manage.mapping.unresolved.default"><@s.param>${u.defaultValue}</@s.param></@s.text></#if>
                                                </div>
                                            </div>
                                            <select class="form-select form-select-sm" name="unresolvedFieldActions['${u.field.name}']" aria-label="${u.field.name}">
                                                <option value=""><@s.text name="manage.mapping.unresolved.keep"/></option>
                                                <option value="discard"><@s.text name="manage.mapping.unresolved.discard"/></option>
                                                <#list fields as fm>
                                                    <#if !fm.index?? && !fm.defaultValue?has_content>
                                                        <option value="field:${fm.field.name}"><@s.text name="manage.mapping.unresolved.moveTo"><@s.param>${fm.field.name}</@s.param></@s.text></option>
                                                    </#if>
                                                </#list>
                                            </select>
                                        </div>
                                    </#list>
                                </div>
                            </section>
                        </#if>

                        <#-- ROWS: how the rows of the table are made from the source rows -->
                        <span class="mp-anchor" id="anchor-mp-rows"></span>
                        <section id="mp-rows" class="mp-section">
                            <div class="mp-sec-head"><h2><@s.text name="manage.mapping.rows.title"/></h2><span><@s.text name="manage.mapping.rows.desc"/></span></div>
                            <div class="mp-cards mp-rowmodes">
                                <#list ["ROW", "DISTINCT", "ITEMS"] as m>
                                    <label class="mp-choice<#if rowMode == m> on</#if>">
                                        <input type="radio" name="rowMode" value="${m}"<#if rowMode == m> checked</#if>>
                                        <span class="t"><@s.text name="manage.mapping.rows.${m}"/></span>
                                        <span class="d"><@s.text name="manage.mapping.rows.${m}.desc"/></span>
                                    </label>
                                </#list>
                            </div>
                            <div class="mp-rows-opts">
                                <div class="mp-sentence" data-for="ITEMS"<#if rowMode != "ITEMS"> hidden</#if>>
                                    <span><@s.text name="manage.mapping.rows.lists"/></span>
                                    <select id="mp-split" name="splitColumns" multiple class="form-select form-select-sm" aria-label="<@s.text name='manage.mapping.rows.lists'/>">
                                        <#list columns as col>
                                            <option value="${col_index}"<#if splitColumns?seq_contains(col_index)> selected</#if>>${col}</option>
                                        </#list>
                                    </select>
                                    <span><@s.text name="manage.mapping.rows.separator"/></span>
                                    <input id="mp-separator" name="separator" class="form-control form-control-sm mp-sep" value="${mapping.separator!"|"}" aria-label="<@s.text name='manage.mapping.rows.separator'/>"/>
                                </div>
                                <div class="mp-sentence" data-for="DISTINCT ITEMS"<#if rowMode == "ROW"> hidden</#if>>
                                    <label class="mp-merge-on"><input type="checkbox" class="form-check-input" id="mp-merge" name="merge" value="true"<#if mergeKey?has_content || rowMode == "DISTINCT"> checked</#if><#if rowMode == "DISTINCT"> disabled</#if>> <@s.text name="manage.mapping.rows.merge"/></label>
                                    <select id="mp-mergekey" name="mergeKey" class="form-select form-select-sm" aria-label="<@s.text name='manage.mapping.rows.merge'/>">
                                        <#assign defaultKey = mergeKey?has_content?then(mergeKey, (primaryKey[0])!"")/>
                                        <#-- tables without a key (roles): identical rows once -->
                                        <option value="*"<#if defaultKey == "*"> selected</#if>><@s.text name="manage.mapping.rows.merge.wholeRow"/></option>
                                        <#list fields as fm>
                                            <option value="${fm.field.name}"<#if fm.field.name == defaultKey> selected</#if>>${fm.field.name}<#if primaryKey?seq_contains(fm.field.name)> (<@s.text name="manage.mapping.field.pk"/>)</#if></option>
                                        </#list>
                                    </select>
                                </div>
                                <div id="mp-merge-info" class="ov-banner info compact" role="note"<#if rowMode == "ROW" || (rowMode == "ITEMS" && !mergeKey?has_content)> hidden</#if>><@icon name="info" class="ov-ic"/><span class="ov-grow"><@s.text name="manage.mapping.rows.merge.info"/></span></div>
                            </div>
                        </section>

                        <#-- FILTER -->
                        <#assign filterOn = mapping.filter.column?? && mapping.filter.comparator??/>
                        <span class="mp-anchor" id="anchor-mp-filter"></span>
                        <section id="mp-filter" class="mp-section">
                            <div class="mp-sec-head"><h2><@s.text name="manage.mapping.filter.title"/></h2><span><@s.text name="manage.mapping.filter.desc"/></span></div>
                            <div class="mp-sentence">
                                <label class="mp-filter-on"><input type="checkbox" class="form-check-input" id="mp-filter-on" form="mp-ui"<#if filterOn> checked</#if>> <@s.text name="manage.mapping.filter.sentence"/></label>
                                <select id="filterName" name="filter.column" class="form-select form-select-sm" aria-label="<@s.text name='manage.mapping.fields.head.column'/>">
                                    <@columnOptions (mapping.filter.column)!-99999/>
                                </select>
                                <select id="filterComp" name="filter.comparator" class="form-select form-select-sm" aria-label="<@s.text name='manage.mapping.filter.comparator'/>">
                                    <option value=""></option>
                                    <#list comparators as cmp>
                                        <option value="${cmp}"<#if cmp == mapping.filter.comparator!""> selected</#if>><@s.text name="manage.mapping.filter.${cmp}"/></option>
                                    </#list>
                                </select>
                                <input id="filterParam" name="filter.param" class="form-control form-control-sm" value="${mapping.filter.param!}" placeholder="<@s.text name='manage.mapping.filter.value'/>" aria-label="<@s.text name='manage.mapping.filter.value'/>"/>
                                <span class="mp-filter-when"><@s.text name="manage.mapping.filter.checked"/></span>
                                <select id="filterTime" name="filter.filterTime" class="form-select form-select-sm" aria-label="<@s.text name='manage.mapping.filter.time'/>">
                                    <#list ["BeforeTranslation", "AfterTranslation"] as t>
                                        <option value="${t}"<#if t == (mapping.filter.filterTime!"")?string> selected</#if>><#if t == "BeforeTranslation"><@s.text name="manage.mapping.filter.time.before"/><#else><@s.text name="manage.mapping.filter.time.after"/></#if></option>
                                    </#list>
                                </select>
                            </div>
                            <div class="mp-msg mu" id="mp-filter-off"><@s.text name="manage.mapping.filter.off.table"/></div>
                            <div id="mp-filter-preview"></div>
                        </section>

                        <#-- FIELDS: one list, in the table schema's order -->
                        <span class="mp-anchor" id="anchor-mp-fields"></span>
                        <section id="mp-fields" class="mp-section">
                            <div class="mp-sec-head"><h2><@s.text name="manage.mapping.fields.title"/></h2><span><@s.text name="manage.mapping.fields.desc.table"><@s.param>${tableTitle}</@s.param><@s.param>${fields?size}</@s.param></@s.text></span>
                                <div class="mp-seg" id="mp-view" role="group">
                                    <button type="button" data-view="field" class="on"><@s.text name="manage.mapping.fields.byField"/></button>
                                    <button type="button" data-view="column"><@s.text name="manage.mapping.fields.byColumn"/></button>
                                </div>
                            </div>
                            <#-- sticky: what narrows the list -->
                            <div class="mp-toolbar">
                                <div class="mp-seg" id="mp-show" role="group">
                                    <button type="button" data-show="default" class="on"><@s.text name="manage.mapping.show.default"/><span class="mp-count"></span></button>
                                    <button type="button" data-show="problems"><@s.text name="manage.mapping.show.problems"/><span class="mp-count"></span></button>
                                    <button type="button" data-show="unmapped"><@s.text name="manage.mapping.show.unmapped"/><span class="mp-count"></span></button>
                                    <button type="button" data-show="all"><@s.text name="manage.mapping.show.all"/><span class="mp-count"></span></button>
                                </div>
                                <input type="search" id="mp-q" class="form-control form-control-sm" form="mp-ui" placeholder="<@s.text name='manage.mapping.fields.search.table'/>" aria-label="<@s.text name='manage.mapping.fields.search.table'/>">
                            </div>

                            <div id="mp-byfield">
                                <div class="mp-fhead"><span><@s.text name="manage.mapping.fields.head.field"/></span><span><@s.text name="manage.mapping.fields.head.column"/></span><span><span class="mp-fhead-l"><@s.text name="manage.mapping.fields.head.constant"/></span><#-- the samples choice is kept in the browser (js/mapping.js) -->
                                    <span class="mp-fhead-acts"><button type="button" class="ov-link-btn" id="mp-samples-toggle"><@s.text name="manage.mapping.fields.hideSamples"/></button></span></span></div>
                                <#-- no classes in a table schema: one group, always open, without a header -->
                                <div class="mp-group single" id="mp-group-0" data-g="0">
                                    <div class="mp-group-b">
                                        <#list fields as fm>
                                            <@fieldRow fm fm?index/>
                                        </#list>
                                        <div class="mp-group-more" hidden><span></span> · <button type="button" class="ov-link-btn" data-show-group><@s.text name="manage.mapping.group.showThem"/></button></div>
                                    </div>
                                </div>
                                <div class="ov-empty" id="mp-nomatch" hidden><b><@s.text name="manage.mapping.fields.noMatch"/></b><button type="button" class="ov-link-btn" id="mp-clear-q"><@s.text name="manage.mapping.fields.clearSearch"/></button></div>
                            </div>

                            <div id="mp-bycolumn" hidden>
                                <table class="ov-table mp-cols">
                                    <thead><tr><th><@s.text name="manage.mapping.bycol.column"/></th><th><@s.text name="manage.mapping.bycol.samples"/></th><th><@s.text name="manage.mapping.bycol.publishedAs"/></th></tr></thead>
                                    <tbody></tbody>
                                </table>
                            </div>
                        </section>

                        <#-- RESULT: the rows the saved mapping makes from the first source rows -->
                        <#assign pv = action.getPreview()!/>
                        <#if pv?has_content>
                            <span class="mp-anchor" id="anchor-mp-result"></span>
                            <section id="mp-result" class="mp-section">
                                <div class="mp-sec-head"><h2><@s.text name="manage.mapping.result.title"/></h2><span><@s.text name="manage.mapping.result.desc"/></span></div>
                                <#if !pv.columns?has_content>
                                    <div class="mp-msg mu"><@icon name="info"/><span><@s.text name="manage.mapping.result.nothing"/></span></div>
                                <#else>
                                    <div class="mp-result-counts">
                                        <span><@s.text name="manage.mapping.result.sourceRows"/>: <b>${pv.sourceRows?c}</b></span>
                                        <#-- after merging: the rows the table gets -->
                                        <span><@s.text name="manage.mapping.result.rowsMade"/>: <b>${(pv.rowsMade - pv.merged)?c}</b></span>
                                        <#if pv.merged gt 0><span><@s.text name="manage.mapping.result.merged"/>: <b>${pv.merged?c}</b></span></#if>
                                    </div>
                                    <ul class="mp-checks">
                                        <#list pv.problems as p><li class="bad"><@icon name="error"/><span>${action.problemText(p)}</span></li></#list>
                                        <#list pv.conflicts as c>
                                            <li class="bad"><@icon name="error"/><span><@s.text name="manage.mapping.result.conflict"><@s.param>${mapping.mergeKey!}</@s.param><@s.param>${c.key()}</@s.param><@s.param>${c.field()}</@s.param><@s.param>${c.first()!}</@s.param><@s.param>${c.firstLine()?c}</@s.param><@s.param>${c.other()!}</@s.param><@s.param>${c.otherLine()?c}</@s.param></@s.text></span></li>
                                        </#list>
                                        <#list pv.emptyRequired as f, n><li class="bad"><@icon name="error"/><span><@s.text name="manage.mapping.result.emptyRequired"><@s.param>${f}</@s.param><@s.param>${n?c}</@s.param></@s.text></span></li></#list>
                                        <#list pv.linkChecks as l>
                                            <#if l.missing()?has_content>
                                                <li class="bad"><@icon name="error"/><span><@s.text name="manage.mapping.result.linkMissing"><@s.param>${l.field()}</@s.param><@s.param>${l.target()}</@s.param><@s.param>${l.missing()?size?c}</@s.param><@s.param><#list l.missing() as v><#if v?index lt 3>${v}<#sep>, </#sep></#if></#list></@s.param></@s.text></span></li>
                                            <#else>
                                                <li class="ok"><@icon name="check_circle"/><span><@s.text name="manage.mapping.result.linkOk"><@s.param>${l.field()}</@s.param><@s.param>${l.target()}</@s.param></@s.text></span></li>
                                            </#if>
                                        </#list>
                                    </ul>
                                    <div class="mp-result-table">
                                        <table class="ov-table">
                                            <thead><tr><th class="mp-rn"><@s.text name="manage.mapping.result.line"/></th><#list pv.columns as c><th>${c}</th></#list></tr></thead>
                                            <tbody>
                                                <#list pv.rows as r>
                                                    <tr><td class="mp-rn">${r.line()?c}</td><#list r.values() as v><td>${v!}</td></#list></tr>
                                                </#list>
                                            </tbody>
                                        </table>
                                    </div>
                                    <#if pv.rows?size lt pv.rowsMade - pv.merged><div class="mp-msg mu"><@s.text name="manage.mapping.result.shown"/>: ${pv.rows?size?c}</div></#if>
                                </#if>
                            </section>
                        </#if>
                    </#if>
                </div>
            </main>
        </div>
    </div>

    <div class="mp-savebar" id="mp-savebar" aria-hidden="true">
        <div class="container">
            <span class="ov-grow"><@s.text name="manage.mapping.savebar.unsaved"/></span>
            <a class="btn btn-sm btn-outline-secondary" href="dataPackageMapping.do?r=${resource.shortname}&id=${mapping.dataPackageSchema.identifier?url}&mid=${(mid?c)!}" id="mp-discard"><@s.text name="manage.mapping.savebar.discard"/></a>
            <button type="submit" class="btn btn-sm btn-gbif-solid" name="save" value="save"><@icon name="save"/> <@s.text name="button.save"/></button>
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
                <p class="mb-0"><@s.text name="manage.mapping.delete.modal.text"><@s.param>${tableTitle}</@s.param><@s.param>${mapping.source.name}</@s.param></@s.text></p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal"><@s.text name="button.cancel"/></button>
                <button type="submit" form="mappingForm" class="btn btn-sm btn-danger" name="deleteFlag" value="delete"><@s.text name="button.delete"/></button>
            </div>
        </div>
    </div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl"/>
