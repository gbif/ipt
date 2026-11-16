<#ftl output_format="HTML">
<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.BulkPublicationAction" -->
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="admin.home.bulkPublication"/></title>
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/publishing.css">
<link rel="stylesheet" href="${baseURL}/styles/bulk-publishing.css">
<script src="${baseURL}/js/bulk-publishing.js"></script>

<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/icon.ftl"/>

<#assign scopes = ["CHANGED", "ALL", "SELECTED", "EXCLUDED"]/>

<#-- the visibility now, and what it becomes on the next publication -->
<#macro statusPill status pending="">
    <span class="text-nowrap fs-smaller-2 status-pill status-${status?lower_case}"<#if pending?has_content> title="${action.getText('manage.overview.changed.publication.status.republication')}"</#if>>
        <@icon name=(status == "PUBLIC" || status == "PRIVATE")?then("radio_button_unchecked", "circle")/><span><@s.text name="manage.home.visible.${status?lower_case}"/></span><#if pending?has_content><@icon name="arrow_forward"/><span><@s.text name="resource.status.${pending?lower_case}"/></span></#if>
    </span>
</#macro>

<#macro formatPill format>
    <span class="bp-format"><#if format == "dwca"><@s.text name="portal.resource.published.dwca"/><#elseif format == "metadata"><@s.text name="manage.overview.publication.metadataOnly"/><#else><@s.text name="portal.resource.type.${format}"/></#if></span>
</#macro>

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<#-- the heading every IPT page has: centred, in a box -->
<div class="container-fluid border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3 ov-head">
            <div class="ov-grow">
                <div class="fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb justify-content-center mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.admin.bulkPublication"/></li>
                        </ol>
                    </nav>
                </div>
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal"><@s.text name="admin.home.bulkPublication"/></h1>
                <div class="ov-meta">
                    <#if run??>
                        <span>
                            <#if running><@s.text name="admin.bulk.meta.running"/><#else><@s.text name="admin.bulk.meta.lastRun"/></#if>:
                            ${run.started?datetime?string["d MMMM yyyy HH:mm"]}<#if run.startedBy?has_content> · ${run.startedBy}</#if>
                        </span>
                    <#else>
                        <span><@s.text name="admin.bulk.meta.noRun"/></span>
                    </#if>
                </div>
            </div>
            <div class="ov-acts">
                <#-- like Publish on the resource overview -->
                <button type="button" id="bp-publish" class="btn btn-sm btn-gbif-solid top-button"<#if run??> hidden</#if>>
                    <@icon name="upload" class="me-1"/><@s.text name="admin.bulk.publish"/>
                </button>
                <#if run?? && !running>
                    <button type="button" id="bp-new" class="btn btn-sm btn-outline-gbif-primary top-button"><@s.text name="admin.bulk.newRun"/></button>
                </#if>
                <a href="${baseURL}/admin/" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="button.back"/></a>
            </div>
        </div>
    </div>
</div>

<div class="container-fluid bg-body">
    <div class="container main-content-container">
        <main class="pt-2 pb-5" id="bp-main" data-prev="${action.getText('pager.previous')}" data-next="${action.getText('pager.next')}">
            <#-- the running or last run, polled while it runs -->
            <#if run??>
                <section id="bp-run" data-running="${running?c}" data-url="${baseURL}/admin/bulkReport.do" data-reload="${baseURL}/admin/bulk-publication.do">
                    <div id="bp-report"><#include "/WEB-INF/pages/admin/bulk_report.ftl"></div>
                </section>
            </#if>

            <#-- what to publish -->
            <section id="bp-setup"<#if run??> hidden</#if>>
                <h2 class="bp-h2"><@s.text name="admin.bulk.scope"/></h2>
                <div class="bp-scopes" role="radiogroup" aria-label="${action.getText('admin.bulk.scope')}">
                    <#list scopes as scope>
                        <label class="bp-scope<#if scope == "CHANGED"> on</#if>">
                            <input type="radio" name="bp-scope" value="${scope}"<#if scope == "CHANGED"> checked</#if>>
                            <span>
                                <b><@s.text name="admin.bulk.scope.${scope}"/></b>
                                <small><@s.text name="admin.bulk.scope.${scope}.help"/></small>
                                <#-- the same counts as the chips and the confirmation: what will be published, and what will be skipped -->
                                <small class="bp-scope-n"><@s.text name="admin.bulk.chip.included"/>: <span data-scope-count="${scope}">-</span><span data-scope-skip="${scope}"> · <@s.text name="admin.bulk.chip.notReady"/>: <span data-scope-skip-n="${scope}">-</span></span></small>
                            </span>
                        </label>
                    </#list>
                </div>

                <h2 class="bp-h2"><@s.text name="admin.bulk.list"/>
                    <small id="bp-list-hint" data-fixed="${action.getText('admin.bulk.list.hint.fixed')}" data-selected="${action.getText('admin.bulk.list.hint.selected')}" data-excluded="${action.getText('admin.bulk.list.hint.excluded')}"></small>
                </h2>

                <#if rows?has_content>
                    <div class="bp-tools">
                        <div class="ex-chips bp-chips" role="group" aria-label="${action.getText('admin.bulk.show')}">
                            <#list ["all", "included", "notReady", "registered", "auto"] as chip>
                                <button type="button" class="ex-chip<#if chip == "included"> on</#if>" data-chip="${chip}"><@s.text name="admin.bulk.chip.${chip}"/>: <span data-chip-count="${chip}">-</span></button>
                            </#list>
                        </div>
                        <span class="bp-tick-acts" hidden>
                            <button type="button" class="btn btn-sm btn-outline-secondary" id="bp-tick-all" data-selected="${action.getText('admin.bulk.tickAll')}" data-excluded="${action.getText('admin.bulk.untickAll')}"></button>
                            <button type="button" class="btn btn-sm btn-outline-secondary" id="bp-tick-reset"><@s.text name="button.reset"/></button>
                        </span>
                        <input type="search" id="bp-q" class="form-control form-control-sm" placeholder="${action.getText('admin.bulk.search')}" aria-label="${action.getText('admin.bulk.search')}" autocomplete="off">
                    </div>

                    <table class="ov-table bp-table">
                        <thead>
                        <tr>
                            <th class="bp-ck"><span class="visually-hidden"><@s.text name="admin.bulk.col.include"/></span></th>
                            <th><@s.text name="admin.bulk.col.resource"/></th>
                            <th class="bp-hide-md"><@s.text name="admin.bulk.col.visibility"/></th>
                            <th><@s.text name="admin.bulk.col.changes"/></th>
                            <th><@s.text name="admin.bulk.col.version"/></th>
                            <th><@s.text name="admin.bulk.col.ready"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <#-- compressed and flags only when true: IPTs can have hundreds of resources -->
                        <#compress>
                        <#list rows as row>
                            <tr class="bp-row" data-sn="${row.shortname}"<#if row.changed> data-changed</#if><#if !row.notReady??> data-ready</#if><#if row.status == "REGISTERED"> data-registered</#if><#if row.autoPublishing?has_content> data-auto</#if><#if row.liveSources> data-live</#if>>
                                <td class="bp-ck"><input type="checkbox" class="form-check-input" data-ck aria-labelledby="bp-t${row?index}"></td>
                                <td class="bp-name">
                                    <a class="resource-table-link" id="bp-t${row?index}" href="${baseURL}/manage/resource.do?r=${row.shortname?url('UTF-8')}" target="_blank">${row.title}</a>
                                    <@formatPill row.format/>
                                    <#if row.autoPublishing?has_content><span class="bp-auto" title="${action.getText('manage.home.autoPublishing')}"><@icon name="autorenew"/> ${row.autoPublishing?lower_case}</span></#if>
                                    <span class="bp-sn">${row.shortname}</span>
                                </td>
                                <td class="bp-hide-md"><@statusPill status=row.status?string pending=(row.pendingStatus!"")?string/></td>
                                <td class="bp-chg">
                                    <#if row.neverPublished>
                                        <span class="bp-dot yes"></span><@s.text name="admin.bulk.changes.never"/>
                                    <#elseif row.pendingStatus??>
                                        <span class="bp-dot yes"></span><@s.text name="admin.bulk.changes.visibility"><@s.param><@s.text name="resource.status.${row.pendingStatus?lower_case}"/></@s.param></@s.text>
                                    <#elseif row.changes?has_content>
                                        <span class="bp-dot yes"></span><@s.text name="admin.bulk.changes.changed"/>: <#list row.changes as c><@s.text name="admin.bulk.changes.${c}"/><#sep>, </#sep></#list>
                                    <#elseif row.liveSources>
                                        <span class="bp-dot live"></span><@s.text name="admin.bulk.changes.live"/>
                                        <small class="bp-sub"><@s.text name="admin.bulk.changes.live.help"/></small>
                                    <#else>
                                        <span class="bp-dot"></span><@s.text name="admin.bulk.changes.none"><@s.param>${row.lastPublished?date?string["d MMM yyyy"]}</@s.param></@s.text>
                                    </#if>
                                </td>
                                <td class="bp-ver">
                                    <#if row.lastVersion?has_content>${row.lastVersion}<#else>–</#if><span class="bp-next"> <@icon name="arrow_forward"/> <b>${row.nextVersion}</b></span>
                                </td>
                                <td class="bp-ready">
                                    <#if row.notReady??>
                                        <span class="bp-bad"><@icon name=(row.notReady == "LOCKED")?then("lock", "error")/> <@s.text name="admin.bulk.notReady.${row.notReady}"/></span>
                                        <#if row.notReady != "DELETED">
                                            <small class="bp-sub"><a href="${baseURL}/manage/resource.do?r=${row.shortname?url('UTF-8')}" target="_blank"><@s.text name="admin.bulk.openResource"/></a></small>
                                        </#if>
                                    <#else>
                                        <span class="bp-ok"><@icon name="check"/> <@s.text name="admin.bulk.ready"/></span>
                                    </#if>
                                </td>
                            </tr>
                        </#list>
                        </#compress>
                        </tbody>
                    </table>
                    <nav class="bp-pager" id="bp-setup-pager" aria-label="${action.getText('portal.home.pages')}"></nav>
                    <p class="bp-empty" id="bp-nomatch" hidden><@s.text name="admin.bulk.noMatch"/></p>
                <#else>
                    <p class="bp-empty"><@s.text name="admin.config.updateMetadata.none"/></p>
                </#if>

            </section>
        </main>
    </div>
</div>

<#-- confirmation, with the numbers; it posts the scope and the ticked or unticked resources -->
<div class="modal fade" id="bp-confirm" tabindex="-1" aria-labelledby="bp-confirm-title" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable">
        <form class="modal-content" action="${baseURL}/admin/publishAll.do" method="post" id="bp-form">
            <input type="hidden" name="publishMode" value="CHANGED">
            <div class="modal-header">
                <h5 class="modal-title" id="bp-confirm-title"><@s.text name="admin.bulk.confirm.title"/></h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="${action.getText('button.cancel')}"></button>
            </div>
            <div class="modal-body">
                <dl class="bp-figures">
                    <div><dt><@s.text name="admin.bulk.summary.publish"/></dt><dd data-sum="publish">0</dd></div>
                    <div class="bp-bad"><dt><@s.text name="admin.bulk.summary.skip"/></dt><dd data-sum="skip">0</dd></div>
                </dl>
                <div id="bp-confirm-live" hidden>
                    <h6 class="bp-h6"><@s.text name="admin.bulk.confirm.live"/></h6>
                    <p><span data-list="live"></span><br><@s.text name="admin.bulk.confirm.live.help"/></p>
                </div>
                <div id="bp-confirm-skip" hidden>
                    <h6 class="bp-h6"><@s.text name="admin.bulk.confirm.skip"/></h6>
                    <p data-list="skip"></p>
                </div>
                <p class="bp-sub mb-0"><@s.text name="admin.bulk.confirm.leave"/></p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal"><@s.text name="button.cancel"/></button>
                <button type="submit" class="btn btn-sm btn-gbif-solid" id="bp-start"><@s.text name="admin.bulk.publish"/></button>
            </div>
        </form>
    </div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
