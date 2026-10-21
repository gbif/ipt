<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.DataPackageSchemaAction" -->
<#include "/WEB-INF/pages/inc/header.ftl">
<#assign impact = updateImpact/>
<#assign schemaTitle = impact.installed.title!impact.installed.name/>
<title><@s.text name="admin.dataPackages.update.title"><@s.param>${schemaTitle}</@s.param></@s.text></title>

<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<div class="container-fluid bg-body border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3">
            <div class="text-center">
                <div class="fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb justify-content-center mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                            <li class="breadcrumb-item"><a href="${baseURL}/admin/dataPackages.do"><@s.text name="breadcrumb.admin.dataPackageSchemas"/></a></li>
                            <li class="breadcrumb-item"><@s.text name="button.update"/></li>
                        </ol>
                    </nav>
                </div>

                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    <@s.text name="admin.dataPackages.update.title"><@s.param>${schemaTitle}</@s.param></@s.text>
                </h1>

                <#-- green if no resource is affected, yellow if some need review, red if some can't be published -->
                <#assign severity = impact.severity.name()/>
                <#assign severityClass = (severity == "BLOCKS_PUBLICATION")?then("schema-status-blocked", (severity == "NEEDS_REVIEW")?then("schema-status-update", "schema-status-installed"))/>
                <div class="fs-smaller">
                    <span class="schema-status ${severityClass}">v${impact.installed.version!}</span>
                    <i class="bi bi-arrow-right"></i>
                    <span class="schema-status ${severityClass}">v${impact.update.version!}</span>
                </div>
                <div class="fs-smaller mt-1">
                    <#if severity == "NONE">
                        <@s.text name="admin.dataPackages.update.summary.none"/>
                    <#else>
                        <#if (impact.blockedResourcesCount > 0)>
                            <span class="text-gbif-danger"><@s.text name="admin.dataPackages.update.summary.blocked"><@s.param>${impact.blockedResourcesCount}</@s.param></@s.text></span><#if (impact.resourcesNeedingReviewCount > 0)>,</#if>
                        </#if>
                        <#if (impact.resourcesNeedingReviewCount > 0)>
                            <span class="text-gbif-warning"><@s.text name="admin.dataPackages.update.summary.review"><@s.param>${impact.resourcesNeedingReviewCount}</@s.param></@s.text></span>
                        </#if>
                    </#if>
                </div>

                <p class="text-smaller mt-2 mb-0"><@s.text name="admin.dataPackages.update.intro"/></p>

                <div class="mt-2">
                    <form action="updateDataPackage.do" method="post" class="d-inline">
                        <input type="hidden" name="id" value="${impact.installed.identifier}"/>
                        <button type="submit" name="update" class="btn btn-sm btn-outline-gbif-primary mt-1 me-xl-1 top-button">
                            <@s.text name="admin.dataPackages.update.button"><@s.param>${impact.update.version!}</@s.param></@s.text>
                        </button>
                    </form>
                    <a href="${baseURL}/admin/dataPackages.do" class="btn btn-sm btn-outline-secondary mt-1 me-xl-1 top-button">
                        <@s.text name="button.cancel"/>
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<main class="container main-content-container">
    <div class="my-3 p-3">
        <h5 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
            <@s.text name="admin.dataPackages.update.changes"/>
        </h5>

        <#if !impact.schemaChanged>
            <p class="text-smaller"><@s.text name="admin.dataPackages.update.noChanges"/></p>
        <#else>
            <div class="text-smaller">
                <#if impact.addedTables?has_content || impact.removedTables?has_content>
                    <p class="mb-1">
                        <strong><@s.text name="admin.dataPackages.update.tables"/></strong>
                        <#list impact.addedTables as t><span>+&nbsp;${t}</span><#sep>, </#sep></#list><#if impact.addedTables?has_content && impact.removedTables?has_content>, </#if>
                        <#list impact.removedTables as t><span class="text-gbif-danger">&minus;&nbsp;${t}</span><#sep>, </#sep></#list>
                    </p>
                </#if>
                <#if impact.tableChanges?has_content>
                    <p class="mb-1">
                        <strong><@s.text name="admin.dataPackages.update.fields"/></strong>
                        <@s.text name="admin.dataPackages.update.fields.added"><@s.param>${impact.addedFieldsCount}</@s.param></@s.text>,
                        <@s.text name="admin.dataPackages.update.count.removed"><@s.param>${impact.removedFieldsCount}</@s.param></@s.text>,
                        <@s.text name="admin.dataPackages.update.fields.newlyRequired"><@s.param>${impact.newlyRequiredFieldsCount}</@s.param></@s.text><#if (impact.possiblyRenamedFieldsCount > 0)>,
                        <@s.text name="admin.dataPackages.update.count.possiblyRenamed"><@s.param>${impact.possiblyRenamedFieldsCount}</@s.param></@s.text></#if>
                    </p>

                    <details class="mt-2">
                        <summary><@s.text name="admin.dataPackages.update.fields.details"/></summary>
                        <table class="table table-sm mt-2">
                            <tbody>
                            <#list impact.tableChanges as change>
                                <tr>
                                    <th class="fw-400">${change.name}</th>
                                    <td>
                                        <#list change.addedFields as f><span>+&nbsp;${f}</span><#if change.newlyRequiredFields?seq_contains(f)>*</#if><#sep>, </#sep></#list><#if change.addedFields?has_content && change.removedFields?has_content>, </#if>
                                        <#list change.removedFields as f><span class="text-gbif-danger">&minus;&nbsp;${f}</span><#sep>, </#sep></#list>
                                        <#assign requiredExisting = change.newlyRequiredFields?filter(f -> !change.addedFields?seq_contains(f))/>
                                        <#if requiredExisting?has_content>
                                            <br><@s.text name="admin.dataPackages.update.fields.nowRequired"><@s.param>${requiredExisting?join(", ")}</@s.param></@s.text>
                                        </#if>
                                        <#if change.possiblyRenamedFields?has_content>
                                            <br><span class="text-muted"><@s.text name="admin.dataPackages.update.possiblyRenamed"><@s.param><#list change.possiblyRenamedFields as removedField, addedField>${removedField} &rarr; ${addedField}<#sep>, </#sep></#list></@s.param></@s.text></span>
                                        </#if>
                                    </td>
                                </tr>
                            </#list>
                            </tbody>
                        </table>
                        <#if impact.tableChanges?filter(c -> c.addedFields?filter(f -> c.newlyRequiredFields?seq_contains(f))?has_content)?has_content>
                            <p class="text-smaller">* <@s.text name="admin.dataPackages.update.fields.requiredMark"/></p>
                        </#if>
                    </details>
                </#if>
            </div>
        </#if>

        <h5 class="pb-2 mb-2 pt-4 text-gbif-header-2 fw-400">
            <@s.text name="admin.dataPackages.update.resources"/>
        </h5>

        <#assign affected = impact.affectedResources/>
        <#assign unaffected = impact.unaffectedResources/>
        <#if !affected?has_content>
            <p class="text-smaller"><@s.text name="admin.dataPackages.update.resources.none"/></p>
        <#else>
            <p class="text-smaller">
                <@s.text name="admin.dataPackages.update.resources.count">
                    <@s.param>${affected?size}</@s.param>
                    <@s.param>${impact.resources?size}</@s.param>
                </@s.text>
            </p>

            <#list affected as r>
                <details class="border rounded-2 p-3 mb-2 text-smaller">
                    <summary class="d-flex justify-content-between">
                        <span>
                            <a href="${baseURL}/manage/resource.do?r=${r.resource.shortname}"><strong>${r.resource.title!r.resource.shortname}</strong></a>
                            <br>
                            <span class="text-muted">
                                <@s.text name="admin.dataPackages.update.count.mappings"><@s.param>${r.mappings?size}</@s.param></@s.text><#if (r.removedTablesCount > 0)>
                                · <@s.text name="admin.dataPackages.update.count.tablesRemoved"><@s.param>${r.removedTablesCount}</@s.param></@s.text></#if><#if (r.possiblyRenamedFieldsCount > 0)>
                                · <@s.text name="admin.dataPackages.update.count.possiblyRenamed"><@s.param>${r.possiblyRenamedFieldsCount}</@s.param></@s.text></#if><#if (r.missingRequiredFieldsCount > 0)>
                                · <@s.text name="admin.dataPackages.update.count.newRequired"><@s.param>${r.missingRequiredFieldsCount}</@s.param></@s.text></#if><#if (r.removedFieldsCount > 0)>
                                · <@s.text name="admin.dataPackages.update.count.removed"><@s.param>${r.removedFieldsCount}</@s.param></@s.text></#if>
                            </span>
                        </span>
                        <span class="text-nowrap ms-2">
                            <#if r.severity.name() == "BLOCKS_PUBLICATION">
                                <span class="schema-status schema-status-blocked"><@s.text name="admin.dataPackages.update.severity.BLOCKS_PUBLICATION"/></span>
                            <#else>
                                <span class="schema-status schema-status-update"><@s.text name="admin.dataPackages.update.severity.NEEDS_REVIEW"/></span>
                            </#if>
                        </span>
                    </summary>

                    <#if r.severity.name() == "BLOCKS_PUBLICATION" && r.autoPublishing>
                        <div class="mt-2 text-gbif-warning">
                            <i class="bi bi-exclamation-triangle-fill"></i>
                            <@s.text name="admin.dataPackages.update.autoPublishing"/>
                        </div>
                    </#if>
                    <#if r.tableRequirementsProblem??>
                        <div class="mt-2 text-gbif-danger">
                            <@s.text name="admin.dataPackages.update.tableRequirements"><@s.param>${r.tableRequirementsProblem}</@s.param></@s.text>
                        </div>
                    </#if>
                    <#list r.mappings as m>
                        <div class="row mt-2">
                            <div class="col-lg-4"><code>${m.label}</code></div>
                            <div class="col-lg-8">
                                <#if m.tableRemoved>
                                    <span class="text-gbif-danger"><@s.text name="admin.dataPackages.update.mapping.tableRemoved"/></span>
                                </#if>
                                <#if m.possiblyRenamedFields?has_content>
                                    <div>
                                        <@s.text name="admin.dataPackages.update.mapping.possiblyRenamed"/>
                                        <#list m.possiblyRenamedFields as removedField, addedField>${removedField} &rarr; <#if m.requiredRenamedFields?seq_contains(addedField)><span class="text-gbif-danger">${addedField}*</span><#else>${addedField}</#if><#sep>, </#sep></#list>
                                    </div>
                                </#if>
                                <#if m.missingRequiredFields?has_content>
                                    <div class="text-gbif-danger"><@s.text name="admin.dataPackages.update.mapping.missingRequiredFields"><@s.param>${m.missingRequiredFields?join(", ")}</@s.param></@s.text></div>
                                </#if>
                                <#if m.removedFields?has_content>
                                    <div><@s.text name="admin.dataPackages.update.mapping.removedFields"><@s.param>${m.removedFields?join(", ")}</@s.param></@s.text></div>
                                </#if>
                            </div>
                        </div>
                    </#list>
                    <#if r.mappings?has_content>
                        <p class="mt-2 mb-0 text-muted"><@s.text name="admin.dataPackages.update.mapping.legend"/></p>
                    </#if>
                </details>
            </#list>
        </#if>

        <#if unaffected?has_content>
            <details class="text-smaller mt-2">
                <summary><@s.text name="admin.dataPackages.update.unaffected"><@s.param>${unaffected?size}</@s.param></@s.text></summary>
                <ul class="mt-2">
                    <#list unaffected as r>
                        <li><a href="${baseURL}/manage/resource.do?r=${r.resource.shortname}">${r.resource.title!r.resource.shortname}</a></li>
                    </#list>
                </ul>
            </details>
        </#if>

        <p class="text-smaller mt-3"><@s.text name="admin.dataPackages.update.note"/></p>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
