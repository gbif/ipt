<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.ValidationReportAction" -->
<#-- The DwC-DP analyser's report from the last publishing: descriptor, EML and per-table checks -->
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<#include "/WEB-INF/pages/macros/duration.ftl"/>
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="manage.validation.title"/>: ${resource.title!resource.shortname}</title>
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/publishing.css">
<link rel="stylesheet" href="${baseURL}/styles/validation-report.css">
<style>
    /* --ov-bars-height: where the sidebar sticks (overview.css), just under the fixed navbar, as there is no resource bar here */
    :root { --navbar-height: 120px; --ov-bars-height: 57px; }
</style>
<script src="${baseURL}/js/scrollspy.js"></script>
<script>
    $(document).ready(function () {
        var spy = window.IptScrollSpy ? IptScrollSpy.init({tocSelector: ".bd-toc nav a"}) : null;

        // "Only tables with problems" hides the clean tables and their sidebar links
        $("#vr-only-problems").on("change", function () {
            $("#vr-data .vr-tables").toggleClass("only-problems", this.checked);
            $(".bd-toc li.vr-toc-table.clean").prop("hidden", this.checked);
            if (spy) spy.refresh();
        });

        // a table picked in the sidebar is opened
        $(".bd-toc").on("click", "a[data-table]", function () {
            var table = document.getElementById("vr-t-" + $(this).data("table"));
            if (table) table.open = true;
        });
    });
</script>
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<#-- a sample value as it is in the file: numbers without grouping -->
<#macro val v><#if !v??><span class="vr-null">null</span><#elseif v?is_number>${v?c}<#elseif v?is_boolean>${v?c}<#else>${v}</#if></#macro>

<#-- "Errors: 2" / "Warnings: 1" / "No problems", with the icon of the worst -->
<#macro status errors warnings>
    <#if errors gt 0>
        <span class="vr-status bad"><@icon name="error"/> <@s.text name="manage.validation.errors"><@s.param>${errors}</@s.param></@s.text></span>
    </#if>
    <#if warnings gt 0>
        <span class="vr-status warn"><@icon name="warning"/> <@s.text name="manage.validation.warnings"><@s.param>${warnings}</@s.param></@s.text></span>
    </#if>
    <#if errors == 0 && warnings == 0>
        <span class="vr-status ok"><@icon name="check_circle"/> <@s.text name="manage.validation.noProblems"/></span>
    </#if>
</#macro>

<#-- descriptor and EML problems -->
<#macro issuesTable issues>
    <table class="ov-table vr-issues">
        <thead>
        <tr>
            <th><@s.text name="manage.validation.problem"/></th>
            <th><@s.text name="manage.validation.location"/></th>
        </tr>
        </thead>
        <tbody>
        <#list issues as issue>
            <#assign sev = issue.severity?string>
            <tr>
                <td>
                    <span class="vr-issue ${(sev == 'ERROR')?then('bad', (sev == 'WARNING')?then('warn', 'info'))}">
                        <@icon name=(sev == 'ERROR')?then('error', (sev == 'WARNING')?then('warning', 'info'))/>
                        <span>
                            <span class="visually-hidden"><@s.text name="manage.validation.severity.${sev}"/>:</span>
                            ${issue.message!}
                            <span class="vr-code">${issue.violationType!}</span>
                        </span>
                    </span>
                </td>
                <td><#if issue.location?has_content><code class="vr-loc">${issue.location}</code></#if></td>
            </tr>
        </#list>
        </tbody>
    </table>
</#macro>

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<div class="container-fluid bg-body border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3 ov-head ov-head-left">
            <div class="ov-grow">
                <div class="fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                            <li class="breadcrumb-item ov-crumb-resource"><a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="manage.validation.title"/></li>
                        </ol>
                    </nav>
                </div>
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal"><@s.text name="manage.validation.title"/></h1>
                <#if checkedAt??>
                    <div class="ov-meta">
                        <span><@s.text name="manage.validation.checked"><@s.param>${checkedAt}</@s.param></@s.text></span>
                        <#if durationMs??><span><@s.text name="manage.validation.took"><@s.param><@duration durationMs/></@s.param></@s.text></span></#if>
                    </div>
                </#if>
            </div>
            <div class="ov-acts">
                <a class="btn btn-sm btn-outline-secondary top-button" href="${baseURL}/manage/resource.do?r=${resource.shortname}"><@s.text name="manage.report.toOverview"/></a>
            </div>
        </div>
    </div>
</div>

<#if (validationReport.result)??>
        <#assign result = validationReport.result>
        <#assign tables = result.resourceAnalysisResults![]>

        <#-- counts. Every table problem is an error; descriptor and EML issues have their own severity.
             Publishing stops at any error (GenerateDarwinCoreDataPackage), warnings don't stop it -->
        <#assign descErrors = 0><#assign descWarnings = 0>
        <#if result.descriptorValidation??>
            <#list result.descriptorValidation.issues as i>
                <#if i.severity?string == "ERROR"><#assign descErrors++><#elseif i.severity?string == "WARNING"><#assign descWarnings++></#if>
            </#list>
        </#if>
        <#assign emlErrors = 0><#assign emlWarnings = 0>
        <#if result.emlValidation??>
            <#list result.emlValidation.issues as i>
                <#if i.severity?string == "ERROR"><#assign emlErrors++><#elseif i.severity?string == "WARNING"><#assign emlWarnings++></#if>
            </#list>
        </#if>
        <#assign totalRows = 0><#assign tableErrors = 0><#assign tablesWithProblems = 0>
        <#list tables as t>
            <#assign totalRows = totalRows + t.totalRows>
            <#assign n = t.foreignKeyViolations?size + t.dataTypeViolations?size + (t.primaryKeyViolation??)?then(1, 0)>
            <#assign tableErrors = tableErrors + n>
            <#if n gt 0><#assign tablesWithProblems++></#if>
        </#list>
        <#assign errors = descErrors + emlErrors + tableErrors>
        <#assign warnings = descWarnings + emlWarnings>
        <#assign blocked = result.descriptorValidation?? && !result.descriptorValidation.hasDataAnalysis()>

        <#-- a dot per section: red for errors, orange for warnings, grey when not checked -->
        <#function dot errors warnings><#return (errors gt 0)?then('bad', (warnings gt 0)?then('warn', 'ok'))></#function>

    <div id="sections" class="container-fluid bg-body vr-page">
        <div class="container main-content-container mb-md-4 bd-layout">
            <main class="bd-main">
                <div class="bd-toc bd-toc-overview mt-4 pt-3 ps-3 mb-lg-5 text-muted">
                    <div class="fw-semibold text-gbif-header-2 mb-2 text-smaller"><@s.text name="manage.overview.onThisPage"/></div>
                    <nav id="sidebar-content" aria-label="<@s.text name='manage.overview.onThisPage'/>">
                        <ul>
                            <#if result.descriptorValidation??>
                                <li><a href="#anchor-vr-descriptor" class="sidebar-navigation-link"><span class="ov-dot ${dot(descErrors, descWarnings)}"></span><@s.text name="manage.validation.descriptor.title"/></a></li>
                            </#if>
                            <#if result.emlValidation??>
                                <li><a href="#anchor-vr-eml" class="sidebar-navigation-link"><span class="ov-dot ${result.emlValidation.isPresent()?then(dot(emlErrors, emlWarnings), 'off')}"></span><@s.text name="manage.validation.eml.title"/></a></li>
                            </#if>
                            <li><a href="#anchor-vr-data" class="sidebar-navigation-link"><span class="ov-dot ${blocked?then('off', dot(tableErrors, 0))}"></span><@s.text name="manage.validation.data.title"/></a></li>
                            <#list tables as t>
                                <#assign n = t.foreignKeyViolations?size + t.dataTypeViolations?size + (t.primaryKeyViolation??)?then(1, 0)>
                                <li class="vr-toc-table ${(n gt 0)?then('has-problems', 'clean')}"><a href="#anchor-vr-t-${t?index}" class="sidebar-navigation-link" data-table="${t?index}"><span class="ov-dot ${dot(n, 0)}"></span><span class="vr-toc-name">${t.name}</span></a></li>
                            </#list>
                        </ul>
                    </nav>
                </div>

                <div class="bd-content ps-lg-4 pt-4">

        <#-- outcome -->
        <div class="pub-head">
            <h2 class="${(errors == 0)?then('ok', 'bad')}">
                <#if errors == 0>
                    <@icon name="check_circle"/> <@s.text name="manage.validation.passed"/>
                <#else>
                    <@icon name="error"/> <@s.text name="manage.validation.failed"/>
                </#if>
            </h2>
            <span class="pub-meta">
                <@s.text name="manage.validation.errors"><@s.param>${errors}</@s.param></@s.text>
                · <@s.text name="manage.validation.warnings"><@s.param>${warnings}</@s.param></@s.text>
            </span>
        </div>
        <p class="vr-lead">
            <#if errors gt 0><@s.text name="manage.validation.failed.text"/><#elseif warnings gt 0><@s.text name="manage.validation.passed.warnings"/><#else><@s.text name="manage.validation.passed.text"/></#if>
        </p>

        <#-- the package descriptor: datapackage.json -->
        <#if result.descriptorValidation??>
            <#assign dv = result.descriptorValidation>
            <span class="vr-anchor" id="anchor-vr-descriptor"></span>
            <section class="ov-card" id="vr-descriptor">
                <div class="ov-ch">
                    <h2><@s.text name="manage.validation.descriptor.title"/></h2>
                    <span class="vr-file">datapackage.json</span>
                    <span class="ov-sp"></span>
                    <@status descErrors descWarnings/>
                </div>
                <#if blocked>
                    <div class="ov-banner bad compact mt-3">
                        <@icon name="error" class="ov-ic"/>
                        <div class="ov-grow"><@s.text name="manage.validation.descriptor.blocking"/></div>
                    </div>
                </#if>
                <#if dv.issues?has_content>
                    <div class="vr-body"><@issuesTable dv.issues/></div>
                </#if>
            </section>
        </#if>

        <#-- the metadata: eml.xml -->
        <#if result.emlValidation??>
            <#assign ev = result.emlValidation>
            <span class="vr-anchor" id="anchor-vr-eml"></span>
            <section class="ov-card" id="vr-eml">
                <div class="ov-ch">
                    <h2><@s.text name="manage.validation.eml.title"/></h2>
                    <span class="vr-file">eml.xml</span>
                    <span class="ov-sp"></span>
                    <#if ev.isPresent()>
                        <@status emlErrors emlWarnings/>
                    <#else>
                        <span class="vr-status neutral"><@icon name="info"/> <@s.text name="manage.validation.eml.notPresent"/></span>
                    </#if>
                </div>
                <#if ev.issues?has_content>
                    <div class="vr-body"><@issuesTable ev.issues/></div>
                </#if>
            </section>
        </#if>

        <#-- the data: one line per table, opened when it has problems -->
        <span class="vr-anchor" id="anchor-vr-data"></span>
        <section class="ov-card" id="vr-data">
            <div class="ov-ch">
                <h2><@s.text name="manage.validation.data.title"/></h2>
                <span class="ov-sp"></span>
                <#if blocked>
                    <span class="vr-status neutral"><@icon name="info"/> <@s.text name="manage.validation.data.notChecked"/></span>
                <#else>
                    <@status tableErrors 0/>
                </#if>
            </div>
            <#if tables?has_content>
                <div class="vr-sub">
                    <dl class="pub-facts">
                        <div><dt><@s.text name="manage.validation.tables"/></dt><dd>${tables?size}</dd></div>
                        <div><dt><@s.text name="manage.validation.rows"/></dt><dd>${totalRows?string.number}</dd></div>
                        <div><dt><@s.text name="manage.validation.tablesWithProblems"/></dt><dd>${tablesWithProblems}</dd></div>
                    </dl>
                    <#if tablesWithProblems gt 0 && tablesWithProblems lt tables?size>
                        <label class="pub-filter vr-filter fs-smaller-2">
                            <input type="checkbox" class="form-check-input mt-0" id="vr-only-problems">
                            <@s.text name="manage.validation.onlyProblems"/>
                        </label>
                    </#if>
                </div>

                <div class="vr-tables">
                    <#list tables as t>
                        <#assign n = t.foreignKeyViolations?size + t.dataTypeViolations?size + (t.primaryKeyViolation??)?then(1, 0)>
                        <span class="vr-anchor" id="anchor-vr-t-${t?index}"></span>
                        <details class="vr-table ${(n gt 0)?then('has-problems', 'clean')}" id="vr-t-${t?index}"<#if n gt 0> open</#if>>
                            <summary>
                                <@icon name="expand_more" class="vr-chev"/>
                                <span class="vr-name">${t.name}</span>
                                <span class="vr-rows"><@s.text name="manage.validation.rowsCount"><@s.param>${t.totalRows?string.number}</@s.param></@s.text></span>
                                <span class="ov-sp"></span>
                                <@status n 0/>
                            </summary>
                            <div class="vr-table-body">
                                <#if n gt 0>
                                    <ul class="vr-problems">
                                        <#if t.primaryKeyViolation??>
                                            <#assign pk = t.primaryKeyViolation>
                                            <li>
                                                <div class="vr-issue bad">
                                                    <@icon name="error"/>
                                                    <span>
                                                        <@s.text name="manage.validation.pkViolation"><@s.param><code><#list pk.fields as f>${f}<#sep>, </#sep></#list></code></@s.param></@s.text>
                                                        <span class="vr-count"><@s.text name="manage.validation.violations"><@s.param>${pk.violationCount?string.number}</@s.param></@s.text></span>
                                                    </span>
                                                </div>
                                                <#if pk.sampleRows?has_content>
                                                    <div class="vr-samples"><span class="vr-samples-label"><@s.text name="manage.validation.examples"/></span>
                                                        <#assign seen = []>
                                                        <#list pk.sampleRows as row>
                                                            <#assign key><#list row?keys as k><#if row?keys?size gt 1>${k}=</#if><@val row[k]/><#sep>, </#sep></#list></#assign>
                                                            <#assign key = key?markup_string><#if !seen?seq_contains(key)><#assign seen = seen + [key]><code>${key?no_esc}</code></#if>
                                                        </#list>
                                                    </div>
                                                </#if>
                                            </li>
                                        </#if>
                                        <#list t.foreignKeyViolations as fk>
                                            <li>
                                                <div class="vr-issue bad">
                                                    <@icon name="error"/>
                                                    <span>
                                                        <@s.text name="manage.validation.fkViolation">
                                                            <@s.param><code><#list fk.fields as f>${f}<#sep>, </#sep></#list></code></@s.param>
                                                            <@s.param><code>${fk.referenceResource}.<#list fk.referenceFields as f>${f}<#sep>, </#sep></#list></code></@s.param>
                                                        </@s.text>
                                                        <span class="vr-count"><@s.text name="manage.validation.violations"><@s.param>${fk.violationCount?string.number}</@s.param></@s.text></span>
                                                    </span>
                                                </div>
                                                <#if fk.sampleRows?has_content>
                                                    <div class="vr-samples"><span class="vr-samples-label"><@s.text name="manage.validation.examples"/></span>
                                                        <#assign seen = []>
                                                        <#list fk.sampleRows as row>
                                                            <#assign key><#list row?keys as k><#if row?keys?size gt 1>${k}=</#if><@val row[k]/><#sep>, </#sep></#list></#assign>
                                                            <#assign key = key?markup_string><#if !seen?seq_contains(key)><#assign seen = seen + [key]><code>${key?no_esc}</code></#if>
                                                        </#list>
                                                    </div>
                                                </#if>
                                            </li>
                                        </#list>
                                        <#list t.dataTypeViolations as dt>
                                            <li>
                                                <div class="vr-issue bad">
                                                    <@icon name="error"/>
                                                    <span>
                                                        <@s.text name="manage.validation.typeViolation">
                                                            <@s.param><code>${dt.field}</code></@s.param>
                                                            <@s.param><code>${dt.declaredType}</code></@s.param>
                                                        </@s.text>
                                                        <span class="vr-count"><@s.text name="manage.validation.violations"><@s.param>${dt.violationCount?string.number}</@s.param></@s.text></span>
                                                    </span>
                                                </div>
                                                <#if dt.sampleValues?has_content>
                                                    <div class="vr-samples"><span class="vr-samples-label"><@s.text name="manage.validation.examples"/></span>
                                                        <#assign seen = []>
                                                        <#list dt.sampleValues as v>
                                                            <#assign key><@val v/></#assign>
                                                            <#assign key = key?markup_string><#if !seen?seq_contains(key)><#assign seen = seen + [key]><code>${key?no_esc}</code></#if>
                                                        </#list>
                                                    </div>
                                                </#if>
                                            </li>
                                        </#list>
                                    </ul>
                                </#if>

                                <#-- how full each column is -->
                                <#if t.columnStatistics?has_content>
                                    <table class="ov-table vr-columns">
                                        <thead>
                                        <tr>
                                            <th><@s.text name="manage.validation.column"/></th>
                                            <th class="vr-fill-col"><@s.text name="manage.validation.completeness"/></th>
                                            <th class="ov-num"><@s.text name="manage.validation.unique"/></th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <#list t.columnStatistics as col>
                                            <#assign pct = (t.totalRows gt 0)?then((col.populatedValues / t.totalRows * 100)?floor, 0)>
                                            <#-- under 1% but not empty still shows a sliver -->
                                            <#assign shown = (col.populatedValues gt 0 && pct lt 1)?then(1, pct)>
                                            <tr>
                                                <td><code class="vr-col">${col.name}</code></td>
                                                <td>
                                                    <div class="vr-fill">
                                                        <div class="vr-bar" role="presentation"><span class="${(pct == 100)?then('full', '')}" style="width: ${shown?c}%"></span></div>
                                                        <span class="vr-pct">${pct?c}%</span>
                                                    </div>
                                                </td>
                                                <td class="ov-num">${col.uniqueValues?string.number}</td>
                                            </tr>
                                        </#list>
                                        </tbody>
                                    </table>
                                </#if>
                            </div>
                        </details>
                    </#list>
                </div>
            <#elseif !blocked>
                <p class="vr-sub mb-0"><@s.text name="manage.validation.data.none"/></p>
            </#if>
        </section>

                </div>
            </main>
        </div>
    </div>
<#else>
    <main class="container main-content-container">
        <#-- the action error above says why; the heading leads back to the overview -->
        <div class="mb-4"></div>
    </main>
</#if>

<#include "/WEB-INF/pages/inc/footer.ftl">
