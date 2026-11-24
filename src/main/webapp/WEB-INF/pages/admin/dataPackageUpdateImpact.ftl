<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.DataPackageSchemaAction" -->
<#--
  Before updating a data package (admin/dataPackageUpdateImpact.do?id=...): what the new version changes, and what that
  means for each resource using it. Nothing changes until Update. Same look as the data package pages.
-->
<#include "/WEB-INF/pages/inc/header.ftl">
<#assign impact = updateImpact/>
<#assign schemaTitle = impact.installed.title!impact.installed.name/>
<title><@s.text name="admin.dataPackages.update.title"><@s.param>${schemaTitle}</@s.param></@s.text></title>
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/extensions.css">
<script>
    $(document).ready(function () {
        // updating migrates the mappings of every resource using the data package
        $("#update").on("click", displayProcessing);
    });
    // the page may be restored from the browser cache with the processing overlay, e.g. going back
    window.addEventListener("pageshow", function (event) {
        if (event.persisted) {
            hideProcessing();
        }
    });
</script>

<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<#assign dateFormat = "d MMMM yyyy"/>
<#macro issued date=""><#if date?is_date_like>${date?date?string[dateFormat]}<#else>-</#if></#macro>
<#-- a count as "Label: N", only when there is one -->
<#macro stat key n><#if (n > 0)><span class="dpu-stat"><@s.text name=key/>: <b>${n}</b></span></#if></#macro>

<#assign severity = impact.severity.name()/>
<#assign affected = impact.affectedResources/>
<#assign unaffected = impact.unaffectedResources/>
<#assign sameVersion = (impact.installed.version!"") == (impact.update.version!"")/>

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
                            <li class="breadcrumb-item"><a href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                            <li class="breadcrumb-item"><a href="${baseURL}/admin/dataPackages.do"><@s.text name="breadcrumb.admin.dataPackageSchemas"/></a></li>
                            <#-- the short title (e.g. ColDP): breadcrumbs don't wrap -->
                            <li class="breadcrumb-item"><a href="${baseURL}/admin/dataPackage.do?id=${impact.installed.identifier?url}" title="${schemaTitle}">${impact.installed.shortTitle!schemaTitle}</a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="button.update"/></li>
                        </ol>
                    </nav>
                </div>
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal"><@s.text name="admin.dataPackages.update.title"><@s.param>${schemaTitle}</@s.param></@s.text></h1>
                <#-- from and to: the version, and the issued dates as the version may stay the same -->
                <div class="ov-meta">
                    <span><@s.text name="admin.dataPackage.version"><@s.param>${impact.installed.version!}</@s.param></@s.text><#if sameVersion> · <@issued date=impact.installed.issued!""/></#if></span>
                    <@icon name="arrow_forward" class="dpu-arrow"/>
                    <span><@s.text name="admin.dataPackage.version"><@s.param>${impact.update.version!}</@s.param></@s.text> · <@issued date=impact.update.issued!""/></span>
                </div>
            </div>
            <div class="ov-acts">
                <form action="updateDataPackage.do" method="post" class="d-inline">
                    <input type="hidden" name="id" value="${impact.installed.identifier}"/>
                    <button id="update" type="submit" name="update" class="btn btn-sm btn-gbif-solid top-button">
                        <#-- "Update to v1.2" only when the version changes: a re-issued version keeps its number -->
                        <@icon name="update" class="me-1"/><#if sameVersion><@s.text name="button.update"/><#else><@s.text name="admin.dataPackages.update.button"><@s.param>${impact.update.version!}</@s.param></@s.text></#if>
                    </button>
                </form>
                <a href="${baseURL}/admin/dataPackage.do?id=${impact.installed.identifier?url}" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="button.back"/></a>
            </div>
        </div>
    </div>
</div>

<div class="container-fluid bg-body">
    <div class="container main-content-container">
        <main class="pt-2 pb-5">
            <#-- what the update means overall: blocked publishing, mappings to review, or nothing -->
            <#if severity == "BLOCKS_PUBLICATION">
                <div class="ov-banner bad mt-3" role="status">
                    <@icon name="error" class="ov-ic text-gbif-danger"/>
                    <div class="ov-grow">
                        <h2><@s.text name="admin.dataPackages.update.banner.blocked.title"/></h2>
                        <div class="ov-text"><@s.text name="admin.dataPackages.update.banner.blocked.text"/></div>
                    </div>
                </div>
            <#elseif severity == "NEEDS_REVIEW">
                <div class="ov-banner warn mt-3" role="status">
                    <@icon name="warning" class="ov-ic"/>
                    <div class="ov-grow">
                        <h2><@s.text name="admin.dataPackages.update.banner.review.title"/></h2>
                        <div class="ov-text"><@s.text name="admin.dataPackages.update.banner.review.text"/></div>
                    </div>
                </div>
            <#else>
                <div class="ov-banner info mt-3" role="status">
                    <@icon name="info" class="ov-ic"/>
                    <div class="ov-grow">
                        <h2><@s.text name="admin.dataPackages.update.summary.none"/></h2>
                        <div class="ov-text"><@s.text name="admin.dataPackages.update.banner.none.text"/></div>
                    </div>
                </div>
            </#if>

            <section class="ext-section">
                <div class="ext-sec-head">
                    <h2><@s.text name="admin.dataPackages.update.changes"/></h2>
                    <#if impact.schemaChanged>
                        <span class="dpu-stats">
                            <@stat key="admin.dataPackages.update.stat.tablesAdded" n=impact.addedTables?size/>
                            <@stat key="admin.dataPackages.update.stat.tablesRemoved" n=impact.removedTables?size/>
                            <@stat key="admin.dataPackages.update.stat.fieldsAdded" n=impact.addedFieldsCount/>
                            <@stat key="admin.dataPackages.update.stat.fieldsRemoved" n=impact.removedFieldsCount/>
                            <@stat key="admin.dataPackages.update.stat.newlyRequired" n=impact.newlyRequiredFieldsCount/>
                            <@stat key="admin.dataPackages.update.stat.possiblyRenamed" n=impact.possiblyRenamedFieldsCount/>
                        </span>
                    </#if>
                </div>
                <#if !impact.schemaChanged>
                    <p class="ex-muted text-smaller mb-0"><@s.text name="admin.dataPackages.update.noChanges"/></p>
                <#else>
                    <table class="ov-table dpu-table">
                        <thead>
                        <tr>
                            <th><@s.text name="manage.overview.data.col.table"/></th>
                            <th><@s.text name="admin.dataPackages.update.col.changes"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <#list impact.addedTables as t>
                            <tr><td class="ext-mono">${t}</td><td><span class="dpu-chip add label"><@s.text name="admin.dataPackages.update.tableAdded"/></span></td></tr>
                        </#list>
                        <#list impact.removedTables as t>
                            <tr><td class="ext-mono">${t}</td><td><span class="dpu-chip del label"><@s.text name="admin.dataPackages.update.mapping.tableRemoved"/></span></td></tr>
                        </#list>
                        <#list impact.tableChanges as change>
                            <#assign requiredExisting = change.newlyRequiredFields?filter(f -> !change.addedFields?seq_contains(f))/>
                            <tr>
                                <td class="ext-mono">${change.name}</td>
                                <td>
                                    <div class="dpu-chips">
                                        <#list change.addedFields as f><span class="dpu-chip add<#if change.newlyRequiredFields?seq_contains(f)> req</#if>"<#if change.newlyRequiredFields?seq_contains(f)> title="<@s.text name='admin.dataPackages.update.fields.requiredMark'/>"</#if>>+ ${f}<#if change.newlyRequiredFields?seq_contains(f)> *</#if></span></#list>
                                        <#list change.removedFields as f><span class="dpu-chip del">&minus; ${f}</span></#list>
                                    </div>
                                    <#if requiredExisting?has_content>
                                        <div class="dpu-note"><@s.text name="admin.dataPackages.update.fields.nowRequired"><@s.param>${requiredExisting?join(", ")}</@s.param></@s.text></div>
                                    </#if>
                                    <#if change.possiblyRenamedFields?has_content>
                                        <div class="dpu-note"><@s.text name="admin.dataPackages.update.possiblyRenamed"><@s.param><#list change.possiblyRenamedFields as removedField, addedField>${removedField} &rarr; ${addedField}<#sep>, </#sep></#list></@s.param></@s.text></div>
                                    </#if>
                                </td>
                            </tr>
                        </#list>
                        </tbody>
                    </table>
                    <#if impact.tableChanges?filter(c -> c.addedFields?filter(f -> c.newlyRequiredFields?seq_contains(f))?has_content)?has_content>
                        <p class="ex-small mt-2 mb-0">* <@s.text name="admin.dataPackages.update.fields.requiredMark"/></p>
                    </#if>
                </#if>
            </section>

            <section class="ext-section">
                <div class="ext-sec-head">
                    <h2><@s.text name="admin.dataPackages.update.resources"/></h2>
                    <span><@s.text name="admin.dataPackages.update.resources.desc"><@s.param>${affected?size}</@s.param><@s.param>${unaffected?size}</@s.param></@s.text></span>
                </div>
                <#if !impact.resources?has_content>
                    <p class="ex-muted text-smaller mb-0"><@s.text name="admin.extensions.usedBy.none"/></p>
                <#elseif !affected?has_content>
                    <p class="ex-muted text-smaller mb-2"><@s.text name="admin.dataPackages.update.resources.none"/></p>
                </#if>

                <#list affected as r>
                    <#assign blocked = r.severity.name() == "BLOCKS_PUBLICATION"/>
                    <div class="dpu-res">
                        <div class="dpu-res-head">
                            <div class="ov-grow">
                                <a class="resource-table-link" href="${baseURL}/manage/resource.do?r=${r.resource.shortname?url}">${action.titleOf(r.resource)}</a>
                                <div class="ext-shortname">${r.resource.shortname}</div>
                            </div>
                            <span class="ov-pill ${blocked?then('bad', 'warn')}"><@s.text name="admin.dataPackages.update.severity.${r.severity.name()}"/></span>
                        </div>
                        <div class="dpu-stats">
                            <@stat key="admin.dataPackages.update.stat.mappings" n=r.mappings?size/>
                            <@stat key="admin.dataPackages.update.stat.tablesRemoved" n=r.removedTablesCount/>
                            <@stat key="admin.dataPackages.update.stat.missingRequired" n=r.missingRequiredFieldsCount/>
                            <@stat key="admin.dataPackages.update.stat.fieldsRemoved" n=r.removedFieldsCount/>
                            <@stat key="admin.dataPackages.update.stat.possiblyRenamed" n=r.possiblyRenamedFieldsCount/>
                        </div>
                        <#if blocked && r.autoPublishing>
                            <div class="dpu-alert warn"><@icon name="warning"/><@s.text name="admin.dataPackages.update.autoPublishing"/></div>
                        </#if>
                        <#if r.tableRequirementsProblem??>
                            <div class="dpu-alert bad"><@icon name="error"/><@s.text name="admin.dataPackages.update.tableRequirements"><@s.param>${r.tableRequirementsProblem}</@s.param></@s.text></div>
                        </#if>
                        <#if r.mappings?has_content>
                            <table class="ov-table dpu-table dpu-mappings">
                                <tbody>
                                <#list r.mappings as m>
                                    <tr>
                                        <td class="ext-mono">${m.label}</td>
                                        <td>
                                            <#if m.tableRemoved><div class="dpu-bad"><@s.text name="admin.dataPackages.update.mapping.tableRemoved"/></div></#if>
                                            <#if m.missingRequiredFields?has_content><div class="dpu-bad"><@s.text name="admin.dataPackages.update.mapping.missingRequiredFields"><@s.param>${m.missingRequiredFields?join(", ")}</@s.param></@s.text></div></#if>
                                            <#if m.possiblyRenamedFields?has_content>
                                                <div><@s.text name="admin.dataPackages.update.mapping.possiblyRenamed"/>
                                                    <#list m.possiblyRenamedFields as removedField, addedField>${removedField} &rarr; <#if m.requiredRenamedFields?seq_contains(addedField)><span class="dpu-bad">${addedField} *</span><#else>${addedField}</#if><#sep>, </#sep></#list>
                                                </div>
                                            </#if>
                                            <#if m.removedFields?has_content><div><@s.text name="admin.dataPackages.update.mapping.removedFields"><@s.param>${m.removedFields?join(", ")}</@s.param></@s.text></div></#if>
                                        </td>
                                    </tr>
                                </#list>
                                </tbody>
                            </table>
                        </#if>
                    </div>
                </#list>
                <#-- the legend explains the * and the possibly renamed fields: only when a mapping has some -->
                <#if affected?filter(r -> r.mappings?filter(m -> m.possiblyRenamedFields?has_content)?has_content)?has_content>
                    <p class="ex-small mb-3"><@s.text name="admin.dataPackages.update.mapping.legend"/></p>
                </#if>

                <#if unaffected?has_content>
                    <button type="button" class="ex-link-btn dpu-unaffected" aria-expanded="false" aria-controls="dpu-unaffected-list" data-toggle-used>
                        <@s.text name="admin.dataPackages.update.notAffected"/>: ${unaffected?size}<@icon name="expand_more" class="ex-chev"/>
                    </button>
                    <ul class="ex-uselist" id="dpu-unaffected-list" hidden>
                        <#list unaffected as r>
                            <li><a href="${baseURL}/manage/resource.do?r=${r.resource.shortname?url}">${action.titleOf(r.resource)}</a></li>
                        </#list>
                    </ul>
                </#if>
            </section>
        </main>
    </div>
</div>

<script src="${baseURL}/js/extensions.js"></script>
<#include "/WEB-INF/pages/inc/footer.ftl">
