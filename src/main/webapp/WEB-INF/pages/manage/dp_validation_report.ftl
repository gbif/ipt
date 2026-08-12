<#setting url_escaping_charset="UTF-8">

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

    .accordion-button {
        color: #4E565F !important;
    }

    .accordion-button:not(.collapsed) {
        background: rgb(var(--alert-success-background-color));
        color: #2c2c2c;
        box-shadow: none;
    }

    .accordion-item:has(.alert-danger) .accordion-button:not(.collapsed) {
        background: rgb(var(--alert-danger-background-color)) !important;
    }

    .accordion-item:has(.text-bg-warning) .accordion-button:not(.collapsed) {
        background: rgb(var(--alert-warning-background-color)) !important;
    }

    .accordion-item:has(.text-bg-danger) .accordion-button:not(.collapsed) {
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

    .badge.text-bg-warning {
        background-color: rgb(var(--alert-warning-background-color)) !important;
        color: rgb(var(--alert-warning-text-color)) !important;
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
                    <@s.text name="manage.validation.title"/>
                </h1>
            </div>
        </div>
    </div>
</div>

<div class="container py-4">
    <#if (validationReport.result)?has_content>

        <#-- helper macro: severity badge-->
        <#macro severityBadge severity>
            <span class="badge rounded-pill <#if severity == 'ERROR'>text-bg-danger<#elseif severity == 'WARNING'>text-bg-warning<#else>text-bg-secondary</#if>">${severity}</span>
        </#macro>

        <#-- helper macro: issues table used by descriptor + EML sections-->
        <#macro issuesTable issues>
            <div class="table-responsive">
                <table class="table table-sm table-hover align-middle mb-0">
                    <thead>
                    <tr>
                        <th style="width: 100px;"><@s.text name="manage.validation.severity"/></th>
                        <th style="width: 220px;"><@s.text name="manage.validation.type"/></th>
                        <th><@s.text name="manage.validation.message"/></th>
                        <th><@s.text name="manage.validation.location"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <#list issues as issue>
                        <tr>
                            <td><@severityBadge issue.severity/></td>
                            <td class="small text-muted">${issue.violationType}</td>
                            <td class="small">${issue.message}</td>
                            <td class="small"><#if issue.location?has_content><code>${issue.location!}</code></#if></td>
                        </tr>
                    </#list>
                    </tbody>
                </table>
            </div>
        </#macro>

        <#-- Metadata (descriptor) validation-->
        <#if validationReport.result.descriptorValidation??>
            <#assign dv = validationReport.result.descriptorValidation>
            <#assign dvIssueCount = dv.issues?size>
            <#assign dvErrorCount = 0>
            <#list dv.issues as issue><#if issue.severity == 'ERROR'><#assign dvErrorCount = dvErrorCount + 1></#if></#list>

            <h5 class="py-2 mb-3 text-gbif-header fw-400"><@s.text name="manage.validation.descriptor.title"/></h5>

            <#if dvIssueCount == 0>
                <div class="alert alert-success alert-dismissible fade show d-flex mb-4" role="alert">
                    <div class="me-3"><i class="bi bi-check2-circle alert-green-2 fs-bigger-2 me-2"></i></div>
                    <div class="overflow-x-hidden pt-1">
                        <span><@s.text name="manage.validation.descriptor.allClean"/></span>
                    </div>
                </div>
            <#else>
                <div class="alert <#if dvErrorCount gt 0>alert-danger<#else>alert-warning</#if> alert-dismissible fade show d-flex mb-3" role="alert">
                    <div class="me-3"><i class="bi bi-exclamation-circle <#if dvErrorCount gt 0>alert-red-2</#if> fs-bigger-2 me-2"></i></div>
                    <div class="overflow-x-hidden pt-1">
                        <span><@s.text name="manage.validation.descriptor.someIssues"><@s.param>${dvIssueCount}</@s.param></@s.text></span>
                        <#if !dv.canProceedToDataAnalysis>
                            <br/><strong><@s.text name="manage.validation.descriptor.blocking"/></strong>
                        </#if>
                    </div>
                </div>

                <div class="accordion mb-4" id="descriptorAccordion">
                    <div class="accordion-item">
                        <h2 class="accordion-header" id="heading-descriptor-issues">
                            <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
                                    data-bs-target="#descriptor-issues" aria-expanded="false" aria-controls="descriptor-issues">
                                <span class="me-2 fw-500"><@s.text name="manage.validation.descriptor.issuesHeading"/></span>
                                <span class="badge rounded-pill <#if dvErrorCount gt 0>text-bg-danger<#else>text-bg-warning</#if>"><@s.text name="manage.validation.issuesLower"><@s.param>${dvIssueCount}</@s.param></@s.text></span>
                            </button>
                        </h2>
                        <div id="descriptor-issues" class="accordion-collapse collapse" aria-labelledby="heading-descriptor-issues" data-bs-parent="#descriptorAccordion">
                            <div class="accordion-body">
                                <@issuesTable dv.issues/>
                            </div>
                        </div>
                    </div>
                </div>
            </#if>
        </#if>

        <#-- EML validation-->
        <#if validationReport.result.emlValidation??>
            <#assign ev = validationReport.result.emlValidation>
            <#assign evIssueCount = ev.issues?size>
            <#assign evErrorCount = 0>
            <#list ev.issues as issue><#if issue.severity == 'ERROR'><#assign evErrorCount = evErrorCount + 1></#if></#list>

            <h5 class="py-2 mb-3 text-gbif-header fw-400"><@s.text name="manage.validation.eml.title"/></h5>

            <#if !ev.emlPresent>
                <div class="alert alert-secondary alert-dismissible fade show d-flex mb-4" role="alert">
                    <div class="me-3"><i class="bi bi-info-circle fs-bigger-2 me-2"></i></div>
                    <div class="overflow-x-hidden pt-1">
                        <span><@s.text name="manage.validation.eml.notPresent"/></span>
                    </div>
                </div>
            <#elseif evIssueCount == 0>
                <div class="alert alert-success alert-dismissible fade show d-flex mb-4" role="alert">
                    <div class="me-3"><i class="bi bi-check2-circle alert-green-2 fs-bigger-2 me-2"></i></div>
                    <div class="overflow-x-hidden pt-1">
                        <span><@s.text name="manage.validation.eml.allClean"/></span>
                    </div>
                </div>
            <#else>
                <div class="alert <#if evErrorCount gt 0>alert-danger<#else>alert-warning</#if> alert-dismissible fade show d-flex mb-3" role="alert">
                    <div class="me-3"><i class="bi bi-exclamation-circle <#if evErrorCount gt 0>alert-red-2</#if> fs-bigger-2 me-2"></i></div>
                    <div class="overflow-x-hidden pt-1">
                        <span><@s.text name="manage.validation.eml.someIssues"><@s.param>${evIssueCount}</@s.param></@s.text></span>
                    </div>
                </div>

                <div class="accordion mb-4" id="emlAccordion">
                    <div class="accordion-item">
                        <h2 class="accordion-header" id="heading-eml-issues">
                            <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
                                    data-bs-target="#eml-issues" aria-expanded="false" aria-controls="eml-issues">
                                <span class="me-2 fw-500"><@s.text name="manage.validation.eml.issuesHeading"/></span>
                                <span class="badge rounded-pill <#if evErrorCount gt 0>text-bg-danger<#else>text-bg-warning</#if>"><@s.text name="manage.validation.issuesLower"><@s.param>${evIssueCount}</@s.param></@s.text></span>
                            </button>
                        </h2>
                        <div id="eml-issues" class="accordion-collapse collapse" aria-labelledby="heading-eml-issues" data-bs-parent="#emlAccordion">
                            <div class="accordion-body p-0">
                                <@issuesTable ev.issues/>
                            </div>
                        </div>
                    </div>
                </div>
            </#if>
        </#if>

        <#if validationReport.result.resourceAnalysisResults?has_content>
            <h5 class="py-2 mb-3 text-gbif-header fw-400"><@s.text name="manage.validation.data.title"/></h5>
            <#assign totalTables = validationReport.result.resourceAnalysisResults?size>
            <#assign totalRowsAllTables = 0>
            <#assign totalIssues = 0>
            <#assign tablesWithIssues = 0>
            <#list validationReport.result.resourceAnalysisResults as t>
                <#assign totalRowsAllTables = totalRowsAllTables + t.totalRows>
                <#assign tableIssueCount = t.foreignKeyViolations?size + t.dataTypeViolations?size + (t.primaryKeyViolation??)?then(1, 0)>
                <#assign totalIssues = totalIssues + tableIssueCount>
                <#if tableIssueCount gt 0><#assign tablesWithIssues = tablesWithIssues + 1></#if>
            </#list>

            <#-- Summary cards-->
            <div class="row row-cols-2 row-cols-md-4 g-3 mb-4">
                <div class="col">
                    <div class="card h-100">
                        <div class="card-body">
                            <div class="small text-muted text-uppercase"><@s.text name="manage.validation.tables"/></div>
                            <div class="fs-3 fw-500">${totalTables}</div>
                        </div>
                    </div>
                </div>
                <div class="col">
                    <div class="card h-100">
                        <div class="card-body">
                            <div class="small text-muted text-uppercase"><@s.text name="manage.validation.rows"/></div>
                            <div class="fs-3 fw-500">${totalRowsAllTables?string.number}</div>
                        </div>
                    </div>
                </div>
                <div class="col">
                    <div class="card h-100">
                        <div class="card-body">
                            <div class="small text-muted text-uppercase"><@s.text name="manage.validation.tablesClean"/></div>
                            <div class="fs-3 fw-500 <#if tablesWithIssues == 0>text-gbif-primary<#else>text-gbif-danger</#if>">
                                ${totalTables - tablesWithIssues} / ${totalTables}
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col">
                    <div class="card h-100">
                        <div class="card-body">
                            <div class="small text-muted text-uppercase"><@s.text name="manage.validation.issues"/></div>
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

            <#-- per table accordion-->
            <div class="accordion" id="validationAccordion">
                <#list validationReport.result.resourceAnalysisResults as t>
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
                                    <#assign pk = t.primaryKeyViolation>
                                    <div class="mb-3">
                                        <div class="alert alert-danger d-flex" role="alert">
                                            <div class="me-3">
                                                <i class="bi bi-exclamation-circle alert-red-2 fs-bigger-2 me-2"></i>
                                            </div>
                                            <div class="overflow-x-hidden pt-1">
                                                <span><@s.text name="manage.validation.pkViolation"/></span>
                                            </div>
                                        </div>

                                        <div class="table-responsive">
                                            <table class="table table-sm table-hover align-middle mb-0">
                                                <thead>
                                                <tr>
                                                    <th><@s.text name="manage.validation.pk.fields">Field(s)</@s.text></th>
                                                    <th class="text-end" style="width: 90px;"><@s.text name="manage.validation.pk.count">Violations</@s.text></th>
                                                    <th><@s.text name="manage.validation.pk.sample">Sample values</@s.text></th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <tr>
                                                    <td><code><#list pk.fields as f>${f}<#sep>, </#sep></#list></code></td>
                                                    <td class="text-end">
                                                        <span class="badge rounded-pill text-bg-danger">${pk.violationCount?string.number}</span>
                                                    </td>
                                                    <td class="small">
                                                        <#list pk.sampleRows as row>
                                                            <div><code><#list row?keys as k>${k}=${row[k]}<#sep>, </#sep></#list></code></div>
                                                        </#list>
                                                    </td>
                                                </tr>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </#if>
                                <#if t.foreignKeyViolations?has_content>
                                    <div class="mb-3">
                                        <div class="alert alert-danger d-flex" role="alert">
                                            <div class="me-3">
                                                <i class="bi bi-exclamation-circle alert-red-2 fs-bigger-2 me-2"></i>
                                            </div>
                                            <div class="overflow-x-hidden pt-1">
                                                <span><@s.text name="manage.validation.fkViolations"/></span>
                                            </div>
                                        </div>

                                        <div class="table-responsive">
                                            <table class="table table-sm table-hover align-middle mb-0">
                                                <thead>
                                                <tr>
                                                    <th><@s.text name="manage.validation.fk.fields"/></th>
                                                    <th><@s.text name="manage.validation.fk.reference"/></th>
                                                    <th class="text-end" style="width: 90px;"><@s.text name="manage.validation.fk.count"/></th>
                                                    <th><@s.text name="manage.validation.fk.sample"/></th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <#list t.foreignKeyViolations as fk>
                                                    <tr>
                                                        <td><code><#list fk.fields as f>${f}<#sep>, </#sep></#list></code></td>
                                                        <td>
                                                            <span class="text-muted">${fk.referenceResource}</span>.<code><#list fk.referenceFields as rf>${rf}<#sep>, </#sep></#list></code>
                                                        </td>
                                                        <td class="text-end">
                                                            <span class="badge rounded-pill text-bg-danger">${fk.violationCount?string.number}</span>
                                                        </td>
                                                        <td class="small">
                                                            <#list fk.sampleRows as row>
                                                                <div><code><#list row?keys as k>${k}=${row[k]}<#sep>, </#sep></#list></code></div>
                                                            </#list>
                                                        </td>
                                                    </tr>
                                                </#list>
                                                </tbody>
                                            </table>
                                        </div>
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
        </#if>
    <#else>
        <div class="text-center">No validation report found</div>
    </#if>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">