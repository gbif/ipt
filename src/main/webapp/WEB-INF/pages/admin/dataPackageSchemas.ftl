<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.DataPackageSchemaAction" -->
<#--
  The data packages page (admin/dataPackages.do): the installed data package schemas with their version state and the
  resources using them, then the ones registered with GBIF and not installed. Same layout as the extensions page.
-->
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="admin.home.manageDataPackageSchemas"/></title>
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/extensions.css">
<script src="${baseURL}/js/extensions.js"></script>
<script>
    // the update page works out what changes for each resource, which takes a moment
    $(document).ready(function () {
        $(".update-schema").on("click", displayProcessing);
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
<#assign installed = schemas?sort_by("title")/>
<#assign toUpdate = registryUnreachable?then(0, installed?filter(ds -> !ds.latest)?size)/>
<#macro issued date=""><#if date?is_date_like>${date?date?string[dateFormat]}<#else>-</#if></#macro>
<#-- a newer version: its number, or its issued date when only that changed -->
<#macro newer ds><#assign l = action.latestOf(ds)!""/><#if l?has_content><#if l.version != ds.version>${l.version}<#else><@issued date=l.issued!""/></#if></#if></#macro>

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
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.admin.dataPackageSchemas"/></li>
                        </ol>
                    </nav>
                </div>
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal"><@s.text name="admin.dataPackageSchemas.title"/></h1>
                <div class="ov-meta ex-meta">
                    <#if !installed?has_content>
                        <span class="ex-state ex-state-bad"><@icon name="error"/><@s.text name="admin.dataPackages.no.schemas.installed"/></span>
                    <#elseif registryUnreachable>
                        <span class="ex-state ex-state-bad"><@icon name="error"/><@s.text name="admin.extensions.notChecked"/></span>
                    <#elseif upToDate>
                        <span class="ex-state ex-state-ok"><@icon name="check_circle"/><@s.text name="admin.dataPackages.upToDate"/></span>
                    <#elseif iptReinstallationRequired>
                        <span class="ex-state ex-state-warn"><@icon name="warning"/><@s.text name="admin.dataPackages.iptReinstallationRequired"/></span>
                    <#else>
                        <span class="ex-state ex-state-warn"><@icon name="warning"/><@s.text name="admin.dataPackages.not.upToDate"/></span>
                    </#if>
                </div>
            </div>
            <div class="ov-acts">
                <a href="${baseURL}/admin/" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="button.back"/></a>
            </div>
        </div>
    </div>
</div>

<div class="container-fluid bg-body">
    <div class="container main-content-container">
        <main class="pt-2 pb-5">
            <#if registryUnreachable>
                <div class="ex-banner ex-banner-danger mt-3" role="status">
                    <@icon name="error" class="ex-banner-icon"/>
                    <div class="ex-banner-body">
                        <h2><@s.text name="admin.extensions.registry.title"/></h2>
                        <div class="ex-banner-text"><@s.text name="admin.dataPackage.registry.text"><@s.param>${cfg.registryUrl}</@s.param></@s.text></div>
                    </div>
                    <a href="dataPackages.do" class="btn btn-sm btn-outline-secondary"><@s.text name="admin.extensions.registry.retry"/></a>
                </div>
            </#if>

            <section id="dp-installed" class="ex-panel mt-3">
                <div class="ext-sec-head">
                    <h2><@s.text name="admin.dataPackages.installed"/></h2>
                    <span><@s.text name="admin.extensions.filter.all"/>: ${installed?size}<#if toUpdate gt 0> · <@s.text name="admin.extensions.toUpdate"/>: ${toUpdate}</#if></span>
                </div>
                <#if installed?has_content>
                    <table class="ov-table ex-table">
                        <thead>
                        <tr>
                            <th><@s.text name="admin.extensions.col.name"/></th>
                            <th><@s.text name="admin.extensions.col.version"/></th>
                            <th class="ov-num ex-hide-md"><@s.text name="admin.dataPackage.tables"/></th>
                            <th><@s.text name="admin.extensions.col.usedBy"/></th>
                            <th><span class="visually-hidden"><@s.text name="admin.extensions.col.actions"/></span></th>
                        </tr>
                        </thead>
                        <tbody>
                        <#list installed as ds>
                            <#assign resources = usedBy[ds.identifier]![]/>
                            <#assign listId = "ex-used-" + ds?index/>
                            <tr class="ex-row">
                                <td class="ex-name">
                                    <a class="resource-table-link" href="dataPackage.do?id=${ds.identifier?url}">${ds.title}</a>
                                    <span class="ex-rowtype">${ds.identifier}</span>
                                    <#if ds.description?has_content><div class="ex-desc">${ds.description?truncate(240)}</div></#if>
                                </td>
                                <td class="ex-ver">
                                    ${ds.version!} · <@issued date=ds.issued!""/>
                                    <#if registryUnreachable>
                                        <span class="ex-small"><@s.text name="admin.extensions.version.notChecked"/></span>
                                    <#elseif ds.latest>
                                        <span class="ex-small ex-ok"><@s.text name="admin.extensions.version.latest"/></span>
                                    <#else>
                                        <span class="ex-small ex-up"><@icon name="warning"/><@s.text name="admin.extensions.version.newer"><@s.param><@newer ds/></@s.param></@s.text></span>
                                        <#if !ds.updatable><span class="ex-small"><@s.text name="admin.dataPackage.version.iptUpdate"/></span></#if>
                                    </#if>
                                </td>
                                <td class="ov-num ex-hide-md">${(ds.tableSchemas![])?size}</td>
                                <td class="ex-used">
                                    <#if resources?has_content>
                                        <button type="button" class="ex-link-btn" aria-expanded="false" aria-controls="${listId}" data-toggle-used>
                                            <@s.text name="admin.extensions.usedBy.count"><@s.param>${resources?size}</@s.param></@s.text>
                                            <@icon name="expand_more" class="ex-chev"/>
                                        </button>
                                        <ul class="ex-uselist" id="${listId}" hidden>
                                            <#list resources as r>
                                                <li><a href="${baseURL}/manage/resource.do?r=${r.shortname?url}">${action.titleOf(r)}</a></li>
                                            </#list>
                                        </ul>
                                    <#else>
                                        <span class="ex-muted"><@s.text name="admin.extensions.usedBy.none"/></span>
                                    </#if>
                                </td>
                                <td>
                                    <div class="ex-acts">
                                        <#if !registryUnreachable && !ds.latest && ds.updatable>
                                            <#-- the update page shows what changes for each resource before anything is updated -->
                                            <a class="ex-act ex-act-primary update-schema" href="dataPackageUpdateImpact.do?id=${ds.identifier?url}">
                                                <@icon name="update"/><@s.text name="button.update"/>
                                            </a>
                                        </#if>
                                        <#if ds.url??>
                                            <div class="dropdown">
                                                <button type="button" class="ex-icon-btn" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='admin.extensions.moreActions'/>" aria-label="<@s.text name='admin.extensions.moreActions'/>: ${ds.title}"><@icon name="more_horiz"/></button>
                                                <ul class="dropdown-menu dropdown-menu-end">
                                                    <li><a class="dropdown-item custom-link" href="${ds.url}" target="_blank" rel="noopener"><@s.text name="admin.dataPackage.definition"/> <@icon name="open_in_new"/></a></li>
                                                </ul>
                                            </div>
                                        </#if>
                                    </div>
                                </td>
                            </tr>
                        </#list>
                        </tbody>
                    </table>
                <#else>
                    <div class="ex-empty"><@s.text name="admin.dataPackages.no.schemas.installed"/></div>
                </#if>
            </section>

            <#-- registered with GBIF and not installed: only shown when there are some -->
            <#if !registryUnreachable && newSchemas?has_content>
                <section id="dp-notinstalled" class="ex-panel mt-4">
                    <div class="ext-sec-head">
                        <h2><@s.text name="admin.extensions.tab.notInstalled"/></h2>
                        <span><@s.text name="admin.extensions.filter.all"/>: ${newSchemas?size}</span>
                    </div>
                    <div class="ex-cards">
                        <#list newSchemas as ds>
                            <div class="ex-card">
                                <h3>${ds.title}</h3>
                                <span class="ex-rowtype">${ds.identifier}</span>
                                <p>${ds.description!?truncate(300)}</p>
                                <div class="ex-card-foot">
                                    <span class="ex-small"><@s.text name="admin.dataPackage.version"><@s.param>${ds.version!}</@s.param></@s.text> · <@s.text name="admin.extensions.issued"><@s.param><@issued date=ds.issued!""/></@s.param></@s.text></span>
                                    <form action="dataPackage.do" method="post" class="ex-install-form">
                                        <input type="hidden" name="id" value="${ds.identifier}"/>
                                        <button type="submit" name="install" value="Install" class="btn btn-sm btn-outline-gbif-primary">
                                            <span class="ex-busy-text" hidden><@icon name="autorenew" class="ex-spin"/> <@s.text name="admin.extensions.installing"/></span>
                                            <span class="ex-idle-text"><@icon name="download"/> <@s.text name="button.install"/></span>
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </#list>
                    </div>
                </section>
            </#if>
        </main>
    </div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
