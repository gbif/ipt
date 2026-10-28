<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="manage.source.log.title"/></title>
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<style>
    .accordion-item {
        border-color: #e3e5e0;
        margin-bottom: .5rem;
        border-radius: .375rem;
        overflow: hidden;
    }

    .accordion-item:not(:first-of-type) {
        border-top: 1px solid rgba(0,0,0,.125) !important;
    }

    .accordion-button {
        color: #4E565F !important;
    }

    .accordion-button:not(.collapsed) {
        background: rgb(var(--alert-warning-background-color));
        color: #2c2c2c;
        box-shadow: none;
    }

    .accordion-button:focus {
        box-shadow: none;
        border-color: #e3e5e0;
    }

    .badge.text-bg-warning {
        background-color: rgb(var(--alert-warning-background-color)) !important;
        color: rgb(var(--alert-warning-text-color)) !important;
        font-weight: 400;
    }

    .source-log-value {
        white-space: pre-wrap;
        word-break: break-all;
    }

    .source-log-value mark {
        background-color: rgb(var(--alert-danger-background-color));
        color: rgb(var(--alert-danger-text-color));
        padding: 0 .1rem;
    }

    /* how the source was read: a list on small screens, a row on wider ones */
    .source-log-fact {
        display: flex;
        justify-content: space-between;
        align-items: baseline;
        gap: 1rem;
        padding: .5rem 0;
    }

    .source-log-fact + .source-log-fact {
        border-top: 1px solid #e3e5e0;
    }

    .source-log-fact > .fw-500 {
        text-align: end;
    }

    @media (min-width: 768px) {
        .source-log-facts {
            display: flex;
        }

        .source-log-fact {
            display: block;
            flex: 1 1 auto;
        }

        .source-log-fact + .source-log-fact {
            border-top: 0;
            border-left: 1px solid #e3e5e0;
            padding-left: 1rem;
        }

        .source-log-fact > .fw-500 {
            text-align: start;
        }
    }

    pre.source-log-text {
        white-space: pre-wrap;
        font-size: .85em;
    }
</style>

<#-- the column of an example: its name from the header, or its number -->
<#macro column example>
    <#if example.column??><code>${example.column}</code><#elseif example.columnNumber??>#${example.columnNumber?c}</#if>
</#macro>

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<div class="container-fluid bg-body border-bottom">
    <div class="container border rounded-2 mb-4">
        <div class="container my-3 p-3">
            <div class="text-center">
                <div class="text-center fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb justify-content-center mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                            <li class="breadcrumb-item"><a href="${baseURL}/manage/resource.do?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                            <li class="breadcrumb-item"><a href="${baseURL}/manage/source.do?r=${resource.shortname}&id=${source.name?url}"><@s.text name="breadcrumb.manage.overview.source"/></a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="manage.source.log.title"/></li>
                        </ol>
                    </nav>
                </div>

                <h5 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-400">
                    <@s.text name="manage.source.log.title"/>: ${source.name}
                </h5>

                <div class="text-smaller">
                    <a href="${baseURL}/manage/resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                </div>

                <div class="mt-2">
                    <a href="${baseURL}/manage/source.do?r=${resource.shortname}&id=${source.name?url}" class="btn btn-sm btn-outline-gbif-primary top-button"><@s.text name="button.back"/></a>
                    <#if log?? || textLog??>
                        <a href="${baseURL}/manage/sourcelog-download.do?r=${resource.shortname}&s=${source.name?url}" class="btn btn-sm btn-outline-gbif-primary top-button"><@s.text name="button.download"/></a>
                    </#if>
                </div>
            </div>
        </div>
    </div>
</div>

<main class="container py-4 px-0">
    <div class="p-2">
        <#if log??>
            <#-- how the source was read -->
            <div class="source-log-facts border rounded-2 px-3 mb-3">
                <div class="source-log-fact">
                    <div class="text-smaller text-muted"><@s.text name="manage.source.rows"/></div>
                    <div class="fw-500">${log.rows?string.number}</div>
                </div>
                <div class="source-log-fact">
                    <div class="text-smaller text-muted"><@s.text name="manage.source.columns"/></div>
                    <div class="fw-500">${log.columns?string.number}</div>
                </div>
                <#if log.encoding??>
                    <div class="source-log-fact">
                        <div class="text-smaller text-muted"><@s.text name="manage.source.log.encoding"/></div>
                        <div class="fw-500">${log.encoding}</div>
                    </div>
                    <div class="source-log-fact">
                        <div class="text-smaller text-muted"><@s.text name="manage.source.log.delimiterAndQuote"/></div>
                        <div class="fw-500">${log.delimiter!}<#if log.quote??>, <code>${log.quote}</code><#else>, <@s.text name="manage.source.log.notQuoted"/></#if></div>
                    </div>
                </#if>
                <#if log.analysed??>
                    <div class="source-log-fact">
                        <div class="text-smaller text-muted"><@s.text name="manage.source.log.analysed"/></div>
                        <div class="fw-500">${log.analysed?datetime?string.medium_short}</div>
                    </div>
                </#if>
            </div>

            <#if (log.linesWithEmbeddedBreaksRemoved > 0)>
                <div class="callout callout-info text-smaller">
                    <@s.text name="manage.source.fixed.lineBreaksRemoved"><@s.param>${log.linesWithEmbeddedBreaksRemoved?c}</@s.param></@s.text>
                </div>
            </#if>

            <#if log.problems?has_content>
                <div class="accordion" id="problems">
                    <#list log.problems as problem>
                        <#assign panelId = "problem-" + problem.type.key>
                        <div class="accordion-item">
                            <h2 class="accordion-header" id="heading-${panelId}">
                                <button class="accordion-button" type="button" data-bs-toggle="collapse"
                                        data-bs-target="#${panelId}" aria-expanded="true" aria-controls="${panelId}">
                                    <span class="me-2 fw-500"><@s.text name="manage.source.log.problem.${problem.type.key}"/></span>
                                    <span class="badge rounded-pill text-bg-warning"><@s.text name="manage.source.log.lines"><@s.param>${problem.lines?string.number}</@s.param></@s.text></span>
                                </button>
                            </h2>
                            <div id="${panelId}" class="accordion-collapse collapse show" aria-labelledby="heading-${panelId}">
                                <div class="accordion-body">
                                    <p class="text-smaller">${action.problemMessage(problem)}</p>

                                    <#if problem.type == 'EMPTY_LINES'>
                                        <p class="text-smaller mb-0">
                                            <@s.text name="manage.source.log.line"/>:
                                            <#list problem.examples as example>${example.line?c}<#sep>, </#sep></#list>
                                        </p>
                                    <#else>
                                        <div class="table-responsive">
                                            <table class="table table-sm table-hover align-middle mb-0 text-smaller">
                                                <thead>
                                                <tr>
                                                    <th style="width: 90px;"><@s.text name="manage.source.log.line"/></th>
                                                    <#if problem.type == 'COLUMN_COUNT'>
                                                        <th><@s.text name="manage.source.columns"/></th>
                                                    <#else>
                                                        <th style="width: 20%;"><@s.text name="manage.source.log.column"/></th>
                                                        <th><@s.text name="manage.source.log.value"/></th>
                                                    </#if>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <#list problem.examples as example>
                                                    <tr>
                                                        <td>${example.line?c}</td>
                                                        <#if problem.type == 'COLUMN_COUNT'>
                                                            <td><@s.text name="manage.source.log.lineColumns"><@s.param>${example.lineColumns!0}</@s.param><@s.param>${log.columns}</@s.param></@s.text></td>
                                                        <#else>
                                                            <td><@column example/></td>
                                                            <td>
                                                                <#if example.reason??>
                                                                    <div class="text-muted"><@s.text name="manage.source.log.reason.${example.reason.key}"/></div>
                                                                </#if>
                                                                <code class="source-log-value">${action.markValue(example.value!)?no_esc}</code>
                                                            </td>
                                                        </#if>
                                                    </tr>
                                                </#list>
                                                </tbody>
                                            </table>
                                        </div>
                                    </#if>

                                    <#if (problem.lines > problem.examples?size)>
                                        <p class="text-smaller text-muted mt-2 mb-0"><@s.text name="manage.source.log.more"><@s.param>${problem.examples?size}</@s.param></@s.text></p>
                                    </#if>
                                </div>
                            </div>
                        </div>
                    </#list>
                </div>
            <#else>
                <div class="alert alert-success d-flex" role="alert">
                    <div class="me-3"><i class="bi bi-check2-circle alert-green-2 fs-bigger-2 me-2"></i></div>
                    <div class="overflow-x-hidden pt-1">
                        <span><@s.text name="manage.source.log.noProblems"/></span>
                    </div>
                </div>
            </#if>
        <#elseif textLog??>
            <div class="callout callout-info text-smaller">
                <@s.text name="manage.source.log.notChecked"/>
            </div>
            <pre class="source-log-text border rounded p-3">${textLog}</pre>
        <#else>
            <div class="text-center"><@s.text name="manage.source.log.none"/></div>
        </#if>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
