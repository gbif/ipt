<#setting url_escaping_charset="UTF-8">

<#assign totalTables = validationReport?size>
<#assign totalRowsAllTables = 0>
<#assign totalIssues = 0>
<#assign tablesWithIssues = 0>
<#list validationReport as t>
    <#assign totalRowsAllTables = totalRowsAllTables + t.totalRows>
    <#assign tableIssueCount = t.foreignKeyViolations?size + t.dataTypeViolations?size + (t.primaryKeyViolation??)?then(1, 0)>
    <#assign totalIssues = totalIssues + tableIssueCount>
    <#if tableIssueCount gt 0><#assign tablesWithIssues = tablesWithIssues + 1></#if>
</#list>

<#include "/WEB-INF/pages/inc/header.ftl">
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<style>
    :root {
        --gbif-primary: #509E2F;
        --gbif-primary-dark: #3d7a24;
        --gbif-danger: #C0392B;
    }

    .card {
        border: 1px solid #e3e5e0;
        box-shadow: 0 1px 2px rgba(0, 0, 0, .03);
    }

    .accordion-button:not(.collapsed) {
        background: rgb(var(--alert-success-background-color));
        color: #2c2c2c;
        box-shadow: none;
    }

    .accordion-item:has(.alert-danger) .accordion-button:not(.collapsed) {
        background: rgb(var(--alert-danger-background-color)) !important;
    }

    .accordion-button:focus {
        box-shadow: none;
        border-color: #e3e5e0;
    }

    .accordion-item {
        border-color: #e3e5e0;
        margin-bottom: .5rem;
        border-radius: .375rem;
        overflow: hidden;
    }

    .accordion-item:not(:first-of-type) {
        border-top: 1px solid rgba(0,0,0,.125) !important;
    }

    .badge.text-bg-success {
        background-color: rgb(var(--alert-success-background-color)) !important;
        color: rgb(var(--alert-success-text-color)) !important;
        font-weight: 400;
    }

    .badge.text-bg-danger {
        background-color: rgb(var(--alert-danger-background-color)) !important;
        color: rgb(var(--alert-danger-text-color)) !important;
        font-weight: 400;
    }

    code {
        color: #5a5a5a;
        background: #f1f1ef;
        padding: .1rem .35rem;
        border-radius: .25rem;
        font-size: .8em;
    }

    .progress {
        background-color: #eceeea;
    }

    .bg-info {
        background-color: #7fb0d8 !important;
    }
</style>

<div class="container-fluid border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3">
            <div class="text-center fs-smaller">
                <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                    <ol class="breadcrumb justify-content-center mb-0">
                        <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                        <li class="breadcrumb-item"><a href="${baseURL}/manage/resource?r=${shortname!}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                        <li class="breadcrumb-item active" aria-current="page">Validation report</li>
                    </ol>
                </nav>
            </div>

            <div class="text-center mt-1">
                <h1 class="rtitle pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    Data Package Validation Report
                </h1>
            </div>
        </div>
    </div>
</div>

<div class="container py-4">
    <#--    Summary cards-->
    <div class="row row-cols-2 row-cols-md-4 g-3 mb-4">
        <div class="col">
            <div class="card h-100">
                <div class="card-body">
                    <div class="small text-muted text-uppercase"><@s.text name="manage.validation.tables">Tables</@s.text></div>
                    <div class="fs-3 fw-500">${totalTables}</div>
                </div>
            </div>
        </div>
        <div class="col">
            <div class="card h-100">
                <div class="card-body">
                    <div class="small text-muted text-uppercase"><@s.text name="manage.validation.rows">Total rows</@s.text></div>
                    <div class="fs-3 fw-500">${totalRowsAllTables?string.number}</div>
                </div>
            </div>
        </div>
        <div class="col">
            <div class="card h-100">
                <div class="card-body">
                    <div class="small text-muted text-uppercase"><@s.text name="manage.validation.tablesClean">Tables with no issues</@s.text></div>
                    <div class="fs-3 fw-500 <#if tablesWithIssues == 0>text-gbif-primary<#else>text-gbif-danger</#if>">
                        ${totalTables - tablesWithIssues} / ${totalTables}
                    </div>
                </div>
            </div>
        </div>
        <div class="col">
            <div class="card h-100">
                <div class="card-body">
                    <div class="small text-muted text-uppercase"><@s.text name="manage.validation.issues">Integrity issues</@s.text></div>
                    <div class="fs-3 fw-500 <#if totalIssues == 0>text-gbif-primary<#else>text-gbif-danger</#if>">
                        ${totalIssues}
                    </div>
                </div>
            </div>
        </div>
    </div>

    <#if totalIssues == 0>
        <div class="alert alert-success alert-dismissible fade show d-flex" role="alert">
            <div class="me-3">
                <i class="bi bi-check2-circle alert-green-2 fs-bigger-2 me-2"></i>
            </div>
            <div class="overflow-x-hidden pt-1">
                <span><@s.text name="manage.validation.allClean"/></span>
            </div>
        </div>
    <#else>
        <div class="alert alert-danger alert-dismissible fade show d-flex" role="alert">
            <div class="me-3">
                <i class="bi bi-exclamation-circle alert-red-2 fs-bigger-2 me-2"></i>
            </div>
            <div class="overflow-x-hidden pt-1">
                <span><@s.text name="manage.validation.someIssues"><@s.param>${totalIssues}</@s.param><@s.param>${tablesWithIssues}</@s.param></@s.text></span>
            </div>
        </div>
    </#if>

    <#--    per table accordion-->
    <div class="accordion" id="validationAccordion">
        <#list validationReport as t>
            <#assign tableIssueCount = t.foreignKeyViolations?size + t.dataTypeViolations?size + (t.primaryKeyViolation??)?then(1, 0)>
            <#assign panelId = "table-" + t.name?replace("[^a-zA-Z0-9]", "-", "r")>
            <div class="accordion-item">
                <h2 class="accordion-header" id="heading-${panelId}">
                    <button class="accordion-button <#if tableIssueCount == 0>collapsed</#if>" type="button"
                            data-bs-toggle="collapse" data-bs-target="#${panelId}"
                            aria-expanded="<#if tableIssueCount gt 0>true<#else>false</#if>" aria-controls="${panelId}">
                        <span class="me-2 fw-500">${t.name}</span>
                        <span class="small text-muted me-3"><@s.text name="manage.validation.rowsLower"><@s.param>${t.totalRows?string.number}</@s.param></@s.text></span>
                        <#if tableIssueCount == 0>
                            <span class="badge rounded-pill text-bg-success"><i class="bi bi-check2-circle"></i> <@s.text name="manage.validation.ok"/></span>
                        <#else>
                            <span class="badge rounded-pill text-bg-danger"><@s.text name="manage.validation.issuesLower"><@s.param>${tableIssueCount}</@s.param></@s.text></span>
                        </#if>
                    </button>
                </h2>
                <div id="${panelId}" class="accordion-collapse collapse <#if tableIssueCount gt 0>show</#if>"
                     aria-labelledby="heading-${panelId}" data-bs-parent="#validationAccordion">
                    <div class="accordion-body">

                        <#-- integrity violations -->
                        <#if t.primaryKeyViolation??>
                            <div class="alert alert-danger py-2 mb-2">
                                <strong><@s.text name="manage.validation.pkViolation"/></strong> ${t.primaryKeyViolation}
                            </div>
                        </#if>
                        <#if t.foreignKeyViolations?has_content>
                            <div class="alert alert-danger py-2 mb-2">
                                <strong><@s.text name="manage.validation.fkViolations"/>:</strong>
                                <ul class="mb-0">
                                    <#list t.foreignKeyViolations as fk>
                                        <li>${fk}</li>
                                    </#list>
                                </ul>
                            </div>
                        </#if>
                        <#if t.dataTypeViolations?has_content>
                            <div class="alert alert-danger py-2 mb-2">
                                <strong><@s.text name="manage.validation.typeViolations"/>:</strong>
                                <ul class="mb-0">
                                    <#list t.dataTypeViolations as dt>
                                        <li>${dt}</li>
                                    </#list>
                                </ul>
                            </div>
                        </#if>

                        <#-- column analysis -->
                        <div class="table-responsive">
                            <table class="table table-sm table-hover align-middle mb-0">
                                <thead>
                                    <tr>
                                        <th><@s.text name="manage.validation.column"/></th>
                                        <th style="width: 30%;"><@s.text name="manage.validation.completeness"/></th>
                                        <th class="text-end"><@s.text name="manage.validation.unique"/></th>
                                        <th class="text-end"><@s.text name="manage.validation.note"/></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <#list t.columnAnalyses as col>
                                        <#assign pct = (t.totalRows gt 0)?then((col.populatedValues / t.totalRows * 100)?round, 0)>
                                        <tr>
                                            <td><code>${col.name}</code></td>
                                            <td>
                                                <div class="d-flex align-items-center gap-2">
                                                    <div class="progress flex-grow-1" style="height: 6px;">
                                                        <div class="progress-bar <#if pct == 100>bg-success<#elseif pct == 0>bg-secondary<#else>bg-info</#if>"
                                                             role="progressbar" style="width: ${pct}%;"
                                                             aria-valuenow="${pct}" aria-valuemin="0" aria-valuemax="100"></div>
                                                    </div>
                                                    <span class="small text-muted" style="min-width: 3.5em; text-align: right;">${pct}%</span>
                                                </div>
                                            </td>
                                            <td class="text-end">${col.uniqueValues?string.number}</td>
                                            <td class="text-end small text-muted">
                                            </td>
                                        </tr>
                                    </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </#list>
    </div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">