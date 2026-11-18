<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.DataPackageTablesAction" -->
<#-- "Split into tables" (from "Map tables"): DwC-DP mappings suggested from one flat source (DataPackageTablesAction).
     Each ticked line becomes an ordinary mapping, opened and changed on the mapping page -->
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<#include "/WEB-INF/pages/inc/header.ftl"/>
<title><@s.text name="manage.tables.title"/>: ${resource.title!resource.shortname}</title>
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/mapping.css">
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl"/>

<form id="tablesForm" action="dataPackageTables.do" method="post">
    <input type="hidden" name="r" value="${resource.shortname}"/>
    <input type="hidden" name="source" value="${source.name}"/>

    <div class="container px-0 ov-alerts">
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
                                <li class="breadcrumb-item"><a href="dataPackageMappingSourceNew.do?r=${resource.shortname}&id=${resource.dataPackageIdentifier?url}&how=SPLIT&from=${source.name?url}"><@s.text name="manage.mapping.new.title"/></a></li>
                                <li class="breadcrumb-item active" aria-current="page"><@s.text name="manage.tables.title"/></li>
                            </ol>
                        </nav>
                    </div>
                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal"><@s.text name="manage.tables.heading"><@s.param>${source.name}</@s.param></@s.text></h1>
                    <#if !unavailable?has_content>
                        <div class="ov-meta"><span><@s.text name="manage.tables.read"/>: ${sampleRows?c} · <@s.text name="manage.overview.source.columns"/>: ${columns?size?c}</span></div>
                    </#if>
                </div>
                <div class="ov-acts">
                    <#if !unavailable?has_content>
                        <button type="submit" class="btn btn-sm btn-gbif-solid top-button" name="save" value="save"><@icon name="add"/> <@s.text name="manage.tables.create"/></button>
                    </#if>
                    <a class="btn btn-sm btn-outline-secondary top-button" href="dataPackageMappingSourceNew.do?r=${resource.shortname}&id=${resource.dataPackageIdentifier?url}&how=SPLIT&from=${source.name?url}"><@s.text name="button.back"/></a>
                </div>
            </div>
        </div>
    </div>

    <div class="container-fluid bg-body">
        <div class="container main-content-container mb-md-4 pt-3">
            <#if unavailable?has_content>
                <div class="ov-banner warn" role="status"><@icon name="warning" class="ov-ic"/><div class="ov-grow"><@s.text name=unavailable/></div></div>
            <#else>
                <p class="text-smaller mb-2"><@s.text name="manage.tables.intro"/></p>
                <#-- how the tables point to each other -->
                <div class="tb-links mb-3">
                    <#list links as table, to>
                        <div class="tb-link"><span class="tb-tname">${table}</span><#if to?has_content> <span class="tb-arrow"><@s.text name="manage.tables.linksTo"/></span> <#list to as t><span class="tb-tname">${t}</span><#sep> </#sep></#list><#else> <span class="tb-arrow"><@s.text name="manage.tables.linksNone"/></span></#if></div>
                    </#list>
                </div>
                <#if unusedColumns?has_content>
                    <div class="ov-banner warn compact mb-3" role="status"><@icon name="warning" class="ov-ic"/><div class="ov-grow"><@s.text name="manage.tables.unused"><@s.param>${unusedColumns?join(", ")}</@s.param></@s.text></div></div>
                </#if>
                <#if suggestions?filter(x -> x.existing)?has_content>
                    <div class="ov-banner info compact mb-3" role="status"><@icon name="info" class="ov-ic"/><div class="ov-grow"><@s.text name="manage.tables.existing"/></div></div>
                </#if>
                <div class="tb-list">
                    <#list suggestions as sg>
                        <#assign m = sg.mapping/>
                        <div class="tb-sugg">
                            <input type="checkbox" class="form-check-input" id="use-${sg.id}" name="use" value="${sg.id}"<#if sg.checked> checked</#if>>
                            <label for="use-${sg.id}" class="tb-name">
                                <span class="tb-table">${sg.table}<#if sg.role?has_content> (${sg.role})</#if></span>
                                <span class="tb-rule">${action.rule(m)}</span>
                            </label>
                            <div class="tb-body">
                                <div class="tb-why">${action.why(sg)}</div>
                                <#-- what the table looks like: its fields, where each value comes from, and the first rows -->
                                <div class="tb-preview">
                                    <table>
                                        <thead>
                                            <tr>
                                                <#list sg.previewFields as f>
                                                    <#assign linkTo = action.linkTable(m, f)/>
                                                    <th>${f}<#if action.isKey(m, f)> <span class="tb-badge key" title="<@s.text name='manage.tables.key.help'/>"><@s.text name="manage.tables.key"/></span><#elseif linkTo?has_content> <span class="tb-badge link" title="<@s.text name='manage.tables.link.help'><@s.param>${linkTo}</@s.param></@s.text>">&rarr; ${linkTo}</span></#if></th>
                                                </#list>
                                            </tr>
                                            <tr class="tb-from">
                                                <#list sg.previewFields as f><td>${action.fieldRule(m, f)}</td></#list>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <#list sg.preview as row>
                                                <tr><#list row as v><td title="${v!}">${v!}</td></#list></tr>
                                            </#list>
                                        </tbody>
                                    </table>
                                </div>
                                <#list sg.filledFromData as f, col>
                                    <div class="tb-note"><@s.text name="manage.tables.filled"><@s.param>${f}</@s.param><@s.param>${(m.getField(f).defaultValue)!}</@s.param><@s.param>${col}</@s.param></@s.text></div>
                                </#list>
                                <#list sg.asks as a>
                                    <div class="tb-ask">
                                        <label for="ask-${sg.id}-${a.field}"><@s.text name="manage.tables.ask"><@s.param><strong>${a.field}</strong></@s.param></@s.text></label>
                                        <input type="text" class="form-control form-control-sm" id="ask-${sg.id}-${a.field}" name="ask_${sg.id}_${a.field}" value="${a.value}">
                                    </div>
                                </#list>
                                <#list sg.warnings as w>
                                    <div class="tb-warn"><@icon name="warning"/><span><@s.text name="manage.tables.varies"><@s.param>${w?keep_before("|")}</@s.param><@s.param>${w?keep_after("|")}</@s.param></@s.text></span></div>
                                </#list>
                            </div>
                            <div class="tb-count"><@s.text name="manage.tables.rows"/>: ${sg.rows?c}</div>
                        </div>
                    </#list>
                </div>
                <p class="text-smaller text-muted mt-3"><@s.text name="manage.tables.after"/></p>
            </#if>
        </div>
    </div>
</form>

<style>
    .tb-links { display: flex; flex-direction: column; gap: 4px; font-size: 0.8rem; padding: 10px 14px; border: 1px solid #dee2e6; border-radius: 8px; background: #f8f9fa; }
    .tb-tname { font-weight: 600; border: 1px solid #dee2e6; background: #fff; border-radius: 6px; padding: 0 6px; }
    .tb-arrow { color: #6c757d; margin: 0 4px; }
    .tb-list { border: 1px solid #dee2e6; border-radius: 8px; padding: 0 14px; }
    .tb-sugg { display: grid; grid-template-columns: 22px minmax(160px, 240px) minmax(0, 1fr) auto; gap: 4px 14px; padding: 12px 0; border-top: 1px solid #f1f3f5; align-items: start; }
    .tb-sugg:first-child { border-top: 0; }
    .tb-sugg .form-check-input { margin-top: 3px; }
    .tb-name { display: flex; flex-direction: column; cursor: pointer; }
    .tb-table { font-weight: 600; }
    .tb-rule { font-size: 0.75rem; color: #6c757d; }
    .tb-why { font-size: 0.82rem; }
    .tb-preview { overflow-x: auto; margin-top: 8px; border: 1px solid #e9ecef; border-radius: 6px; }
    .tb-preview table { border-collapse: collapse; font-size: 0.75rem; width: 100%; }
    .tb-preview th, .tb-preview td { padding: 3px 8px; text-align: left; vertical-align: top; border-top: 1px solid #f1f3f5; }
    .tb-preview th { font-weight: 600; white-space: nowrap; border-top: 0; background: #f8f9fa; }
    .tb-preview tbody td { max-width: 11rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-family: var(--bs-font-monospace); }
    .tb-preview .tb-from td { color: #6c757d; font-style: italic; background: #f8f9fa; min-width: 7rem; }
    .tb-badge { font-size: 0.68rem; font-weight: 500; border-radius: 8px; padding: 0 6px; border: 1px solid #dee2e6; color: #495057; cursor: help; }
    .tb-badge.key { border-color: rgba(var(--color-gbif-primary), .5); color: rgb(55, 115, 55); }
    .tb-note { font-size: 0.75rem; color: #6c757d; margin-top: 4px; }
    .tb-ask { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; margin-top: 6px; font-size: 0.8rem; }
    .tb-ask input { width: 12rem; }
    .tb-warn { display: flex; gap: 6px; font-size: 0.78rem; color: rgb(var(--alert-warning-icon-color)); margin-top: 4px; }
    .tb-count { font-size: 0.8rem; color: #6c757d; white-space: nowrap; }
    @media (max-width: 760px) { .tb-sugg { grid-template-columns: 22px 1fr; } .tb-sugg > :nth-child(n+3) { grid-column: 2; } }
</style>

<#include "/WEB-INF/pages/inc/footer.ftl"/>
