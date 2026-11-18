<#include "/WEB-INF/pages/macros/icon.ftl"/>
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="manage.source.log.title"/>: ${source.name}</title>
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<#-- an Excel sheet has rows rather than lines -->
<#assign excel = source?? && source.sourceType == 'EXCEL_FILE'>
<#assign lineKey = excel?then("manage.source.log.row", "manage.source.log.line")>
<#assign linesKey = excel?then("manage.source.rows", "manage.source.log.linesCount")>
<#-- the column of an example: its name from the header or its letter in an Excel sheet, or its number -->
<#macro column example>
    <#if example.column??><code>${example.column}</code><#elseif example.columnNumber??>#${example.columnNumber?c}</#if>
</#macro>

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
                            <li class="breadcrumb-item ov-crumb-resource"><a href="${baseURL}/manage/resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a></li>
                            <li class="breadcrumb-item"><a href="${baseURL}/manage/source.do?r=${resource.shortname}&id=${source.name?url}">${source.name}</a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="manage.source.log.title"/></li>
                        </ol>
                    </nav>
                </div>

                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal"><@s.text name="manage.source.log.title"/></h1>

                <#-- how the source was read -->
                <#if log??>
                    <div class="ov-meta">
                        <#if log.analysed??><span><@s.text name="manage.source.log.analysed"/>: ${log.analysed?datetime?string.medium_short}</span></#if>
                        <span><@s.text name="manage.source.rows"/>: ${log.rows?string.number} · <@s.text name="manage.source.columns"/>: ${log.columns?string.number}</span>
                        <#if log.encoding??>
                            <span><@s.text name="manage.source.log.encoding"/>: ${log.encoding} · <@s.text name="manage.source.log.delimiterAndQuote"/>: ${log.delimiter!}<#if log.quote??>, <code>${log.quote}</code><#else>, <@s.text name="manage.source.log.notQuoted"/></#if></span>
                        </#if>
                    </div>
                </#if>
            </div>

            <div class="ov-acts">
                <#if log?? || textLog??>
                    <a href="${baseURL}/manage/sourcelog-download.do?r=${resource.shortname}&s=${source.name?url}" class="btn btn-sm btn-outline-gbif-primary top-button"><@icon name="download"/> <@s.text name="button.download"/></a>
                </#if>
                <a href="${baseURL}/manage/source.do?r=${resource.shortname}&id=${source.name?url}" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="button.back"/></a>
            </div>
        </div>
    </div>
</div>

<div class="container-fluid bg-body">
    <main class="container main-content-container mb-md-4 pt-4">
        <#if log??>
            <#if (log.linesWithEmbeddedBreaksRemoved > 0)>
                <div class="ov-banner info" role="status"><@icon name="info" class="ov-ic"/><div class="ov-grow"><@s.text name="manage.source.fixed.lineBreaksRemoved"><@s.param>${log.linesWithEmbeddedBreaksRemoved?c}</@s.param></@s.text></div></div>
            </#if>

            <#-- read without trouble but no rows: as on the source page -->
            <#if log.rows == 0>
                <div class="ov-banner warn" role="status"><@icon name="warning" class="ov-ic"/><div class="ov-grow"><@s.text name="manage.source.empty"/></div></div>
            </#if>

            <#if log.problems?has_content>
                <#list log.problems as problem>
                    <section class="sl-problem" id="problem-${problem.type.key}">
                        <div class="sl-head">
                            <@icon name="warning" class="sl-ic"/>
                            <h2><@s.text name="manage.source.log.problem.${(excel && problem.type == 'EMPTY_LINES')?then('excel.', '')}${problem.type.key}"/></h2>
                            <span class="sl-count"><@s.text name="${linesKey}"/>: ${problem.lines?string.number}</span>
                        </div>
                        <p class="sl-desc">${action.problemMessage(problem)}</p>

                        <#if problem.type == 'EMPTY_LINES'>
                            <p class="sl-desc mb-0"><@s.text name="${linesKey}"/>: <#list problem.examples as example>${example.line?c}<#sep>, </#sep></#list></p>
                        <#else>
                            <div class="sl-table">
                                <table>
                                    <thead>
                                        <tr>
                                            <th class="sl-line"><@s.text name="${lineKey}"/></th>
                                            <#if problem.type == 'COLUMN_COUNT'>
                                                <th><@s.text name="manage.source.columns"/></th>
                                            <#else>
                                                <th class="sl-col"><@s.text name="manage.source.log.column"/></th>
                                                <th><@s.text name="manage.source.log.value"/></th>
                                            </#if>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <#list problem.examples as example>
                                            <tr>
                                                <td class="sl-line">${example.line?c}</td>
                                                <#if problem.type == 'COLUMN_COUNT'>
                                                    <td><@s.text name="manage.source.log.lineColumns"><@s.param>${example.lineColumns!0}</@s.param><@s.param>${log.columns}</@s.param></@s.text></td>
                                                <#else>
                                                    <td class="sl-col"><@column example/></td>
                                                    <td>
                                                        <#if example.reason??>
                                                            <div class="sl-reason"><@s.text name="manage.source.log.reason.${example.reason.key}"/></div>
                                                        </#if>
                                                        <code class="sl-value">${action.markValue(problem, example.value!)?no_esc}</code>
                                                    </td>
                                                </#if>
                                            </tr>
                                        </#list>
                                    </tbody>
                                </table>
                            </div>
                        </#if>

                        <#if (problem.lines > problem.examples?size)>
                            <p class="sl-more"><@s.text name="manage.source.log.more"><@s.param>${problem.examples?size}</@s.param></@s.text></p>
                        </#if>
                    </section>
                </#list>
            <#elseif log.rows != 0>
                <div class="sl-ok"><@icon name="check_circle" class="sp-ok-ic text-gbif-success"/><span><@s.text name="manage.source.log.noProblems"/></span></div>
            </#if>
        <#elseif textLog??>
            <div class="ov-banner info" role="status"><@icon name="info" class="ov-ic"/><div class="ov-grow"><@s.text name="manage.source.log.notChecked"/></div></div>
            <pre class="sl-text">${textLog}</pre>
        <#else>
            <div class="ov-banner info" role="status"><@icon name="info" class="ov-ic"/><div class="ov-grow"><@s.text name="manage.source.log.none"/></div></div>
        </#if>
    </main>
</div>

<style>
    .sl-problem { margin-bottom: 32px; }
    .sl-head { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; border-bottom: 1px solid #dee2e6; padding-bottom: 6px; margin-bottom: 10px; }
    .sl-head h2 { font-size: 1.1rem; font-weight: 500; color: #4e565f; margin: 0; }
    .sl-ic { color: rgb(var(--alert-warning-icon-color)); font-size: 1.15rem; flex: none; }
    .sl-count { color: #6c757d; font-size: 0.78rem; }
    .sl-desc { font-size: 0.875rem; color: #212529; margin-bottom: 12px; }
    .sl-table { border: 1px solid #dee2e6; border-radius: 8px; overflow-x: auto; }
    .sl-table table { width: 100%; border-collapse: collapse; font-size: 0.8rem; }
    .sl-table th { background: #f8f9fa; font-weight: 600; color: #495057; text-align: left; padding: 6px 12px; border-bottom: 1px solid #dee2e6; white-space: nowrap; }
    .sl-table td { padding: 6px 12px; border-top: 1px solid #f1f3f5; vertical-align: top; }
    .sl-table tbody tr:first-child td { border-top: 0; }
    .sl-table .sl-line { width: 90px; font-variant-numeric: tabular-nums; }
    .sl-table .sl-col { width: 22%; }
    .sl-table code, .ov-meta code { color: #212529; background: none; padding: 0; font-size: 0.8rem; }
    .sl-reason { color: #6c757d; margin-bottom: 2px; }
    .sl-value { white-space: pre-wrap; word-break: break-all; }
    .sl-value mark { background-color: rgb(var(--alert-danger-background-color)); color: rgb(var(--alert-danger-text-color)); padding: 0 .1rem; border-radius: 2px; }
    .sl-more { font-size: 0.78rem; color: #6c757d; margin: 8px 0 0; }
    .sp-ok-ic { width: 18px; height: 18px; flex: none; }
    .sl-ok { display: flex; gap: 6px; align-items: center; font-size: 0.875rem; color: #495057; }
    .sl-text { white-space: pre-wrap; font-size: 0.8rem; border: 1px solid #dee2e6; border-radius: 8px; padding: 12px 14px; background: #f8f9fa; }
</style>

<#include "/WEB-INF/pages/inc/footer.ftl">
