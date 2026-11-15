<#ftl output_format="HTML">
<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.ExtensionsAction" -->
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="admin.home.manageExtensions"/></title>
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/extensions.css">
<script src="${baseURL}/js/extensions.js"></script>

<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/icon.ftl"/>

<#assign dateFormat = "d MMMM yyyy"/>
<#assign coreTypes = extensions?filter(e -> e.core)/>
<#assign nonCoreTypes = extensions?filter(e -> !e.core)/>
<#assign extensionsToUpdate = registryUnreachable?then(0, extensions?filter(e -> !e.latest)?size)/>
<#assign vocabulariesToUpdate = registryUnreachable?then(0, vocabularies?filter(v -> !v.latest)?size)/>

<#macro issued date=""><#if date?is_date_like>${date?date?string[dateFormat]}<#else>-</#if></#macro>

<#-- the version column: the installed version's issued date, and whether a newer one is registered -->
<#macro version issuedDate latest newer="">
    <@issued date=issuedDate/>
    <#if registryUnreachable>
        <span class="ex-small"><@s.text name="admin.extensions.version.notChecked"/></span>
    <#elseif latest>
        <span class="ex-small ex-ok"><@s.text name="admin.extensions.version.latest"/></span>
    <#else>
        <span class="ex-small ex-up"><@icon name="warning"/><@s.text name="admin.extensions.version.newer"><@s.param><@issued date=newer/></@s.param></@s.text></span>
    </#if>
</#macro>

<#-- the core types an extension can be added to; an empty subject means any core type -->
<#macro worksWithLine ext>
    <#if !ext.core>
        <#assign cores = (worksWith[ext.rowType])![]/>
        <div class="ex-works"><@s.text name="admin.extensions.worksWith"/>
            <#if (ext.subject!"")?trim?has_content>
                <#if cores?has_content><#list cores as c><i>${c}</i></#list><#else><i><@s.text name="admin.extensions.worksWith.none"/></i></#if>
            <#else>
                <i><@s.text name="admin.extensions.worksWith.any"/></i>
            </#if>
        </div>
    </#if>
</#macro>

<#macro extensionRow ext>
    <#assign resources = (usedBy[ext.rowType])![]/>
    <tr class="ex-row" data-search="${(ext.title + ' ' + ext.rowType + ' ' + ext.description!'')?lower_case}" data-update="${(!registryUnreachable && !ext.latest)?c}">
        <td class="ex-name">
            <b>${ext.title}</b>
            <span class="ex-rowtype">${ext.rowType}</span>
            <#if ext.description?has_content><div class="ex-desc">${ext.description?truncate(240)}</div></#if>
            <@worksWithLine ext/>
        </td>
        <td class="ex-ver"><@version issuedDate=ext.issued!"" latest=ext.latest newer=newerIssued[ext.rowType]!""/></td>
        <td class="ov-num ex-hide-md">${ext.properties?size}</td>
        <td class="ex-used">
            <#if resources?has_content>
                <button type="button" class="ex-link-btn" aria-expanded="false" aria-controls="ex-used-${ext.rowType?replace("[^A-Za-z0-9]+", "-", "r")}" data-toggle-used>
                    <@s.text name="admin.extensions.usedBy.count"><@s.param>${resources?size}</@s.param></@s.text>
                    <@icon name="expand_more" class="ex-chev"/>
                </button>
                <ul class="ex-uselist" id="ex-used-${ext.rowType?replace("[^A-Za-z0-9]+", "-", "r")}" hidden>
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
                <#if !registryUnreachable && !ext.latest>
                    <button type="button" class="ex-act" data-bs-toggle="modal" data-bs-target="#ex-update-modal"
                            data-id="${ext.rowType}" data-title="${ext.title}"
                            data-from="<@issued date=ext.issued!""/>" data-to="<@issued date=newerIssued[ext.rowType]!""/>"
                            data-resources="<#if resources?has_content>ex-used-${ext.rowType?replace("[^A-Za-z0-9]+", "-", "r")}</#if>">
                        <@icon name="update"/><@s.text name="button.update"/>
                    </button>
                </#if>
                <a href="extension.do?id=${ext.rowType?url}" class="ex-act custom-link" title="<@s.text name='admin.extensions.viewTerms'/>"><@icon name="visibility"/><@s.text name="button.view"/><span class="visually-hidden">: ${ext.title}</span></a>
                <div class="dropdown">
                    <button type="button" class="ex-icon-btn" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='admin.extensions.moreActions'/>" aria-label="<@s.text name='admin.extensions.moreActions'/>: ${ext.title}"><@icon name="more_horiz"/></button>
                    <ul class="dropdown-menu dropdown-menu-end">
                        <li><a class="dropdown-item custom-link" href="extension.do?id=${ext.rowType?url}"><@s.text name="admin.extensions.viewTerms"/></a></li>
                        <#if ext.link??><li><a class="dropdown-item custom-link" href="${ext.link}" target="_blank" rel="noopener"><@s.text name="admin.extensions.documentation"/> <@icon name="open_in_new"/></a></li></#if>
                        <#if ext.url??><li><a class="dropdown-item custom-link" href="${ext.url}" target="_blank" rel="noopener"><@s.text name="admin.extensions.definition"/> <@icon name="open_in_new"/></a></li></#if>
                        <li><hr class="dropdown-divider"></li>
                        <li>
                            <#if resources?has_content>
                                <span class="dropdown-item disabled" aria-disabled="true">
                                    <@s.text name="button.remove"/>
                                    <small><@s.text name="admin.extensions.remove.mapped"/></small>
                                </span>
                            <#else>
                                <button type="button" class="dropdown-item ex-danger" data-bs-toggle="modal" data-bs-target="#ex-remove-modal" data-id="${ext.rowType}" data-title="${ext.title}">
                                    <@s.text name="button.remove"/>…
                                </button>
                            </#if>
                        </li>
                    </ul>
                </div>
            </div>
        </td>
    </tr>
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
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.admin.extensions"/></li>
                        </ol>
                    </nav>
                </div>
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal"><@s.text name="admin.home.manageExtensions"/></h1>
                <div class="ov-meta ex-meta">
                    <#if !extensions?has_content>
                        <span class="ex-state ex-state-bad"><@icon name="error"/><@s.text name="admin.extensions.noneInstalled"/></span>
                    <#elseif registryUnreachable>
                        <span class="ex-state ex-state-bad"><@icon name="error"/><@s.text name="admin.extensions.notChecked"/></span>
                    <#elseif extensionsUpToDate>
                        <span class="ex-state ex-state-ok"><@icon name="check_circle"/><@s.text name="admin.extensions.upToDate"/></span>
                    <#else>
                        <span class="ex-state ex-state-warn"><@icon name="warning"/><@s.text name="admin.extensions.not.upToDate"/></span>
                    </#if>
                    <#if lastSynchronised??>
                        <span><@s.text name="extension.last.synchronised"><@s.param>${lastSynchronised?datetime?string["d MMMM yyyy HH:mm"]}</@s.param></@s.text></span>
                    </#if>
                </div>
            </div>
            <div class="ov-acts">
                <#-- synchronise refreshes the content of everything installed and moves the default vocabularies on; it never changes an extension's version -->
                <form action="extensions.do" method="post" id="ex-synchronise-form">
                    <input type="hidden" name="synchronise" value="true"/>
                    <button type="submit" id="synchronise" class="btn btn-sm btn-outline-gbif-primary top-button" title="<@s.text name='admin.extensions.synchronise.tooltip'/>"<#if registryUnreachable> disabled</#if>>
                        <@icon name="autorenew" class="ex-sync-icon"/> <@s.text name="button.synchronise"/>
                    </button>
                </form>
                <a href="${baseURL}/admin/" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="button.back"/></a>
            </div>
        </div>
    </div>
</div>

<div class="container-fluid bg-body">
    <div class="container main-content-container">
        <main class="pt-2 pb-5">
            <#if registryUnreachable>
                <div class="ex-banner ex-banner-danger" role="status">
                    <@icon name="error" class="ex-banner-icon"/>
                    <div class="ex-banner-body">
                        <h2><@s.text name="admin.extensions.registry.title"/></h2>
                        <div class="ex-banner-text"><@s.text name="admin.extensions.registry.text"><@s.param>${cfg.registryUrl}</@s.param></@s.text></div>
                    </div>
                    <a href="extensions.do" class="btn btn-sm btn-outline-secondary"><@s.text name="admin.extensions.registry.retry"/></a>
                </div>
            </#if>

            <#if !coreTypes?has_content>
                <div class="ex-banner ex-banner-warning" role="status">
                    <@icon name="warning" class="ex-banner-icon"/>
                    <div class="ex-banner-body">
                        <h2><@s.text name="admin.extension.no.coreTypes.installed"/></h2>
                        <div class="ex-banner-text"><@s.text name="admin.extensions.noCoreTypes.text"/></div>
                    </div>
                </div>
            </#if>

            <div class="ex-tools">
                <div class="ex-tabs" role="tablist">
                    <button type="button" role="tab" class="ex-tab" id="ex-tab-installed" data-tab="installed" aria-controls="ex-installed" aria-selected="true">
                        <@s.text name="admin.extensions.tab.installed"/><span class="ex-n">${extensions?size}</span><#if (extensionsToUpdate > 0)><span class="ex-n ex-n-up" title="<@s.text name='admin.extensions.toUpdate'/>"><@icon name="update"/>${extensionsToUpdate}</span></#if>
                    </button>
                    <button type="button" role="tab" class="ex-tab" id="ex-tab-notinstalled" data-tab="notinstalled" aria-controls="ex-notinstalled" aria-selected="false">
                        <@s.text name="admin.extensions.tab.notInstalled"/><span class="ex-n"><#if registryUnreachable>?<#else>${newExtensions?size}</#if></span>
                    </button>
                    <button type="button" role="tab" class="ex-tab" id="ex-tab-vocabularies" data-tab="vocabularies" aria-controls="ex-vocabularies" aria-selected="false">
                        <@s.text name="admin.extension.vocabularies"/><span class="ex-n">${vocabularies?size}</span><#if (vocabulariesToUpdate > 0)><span class="ex-n ex-n-up" title="<@s.text name='admin.extensions.toUpdate'/>"><@icon name="update"/>${vocabulariesToUpdate}</span></#if>
                    </button>
                </div>
                <input type="search" id="ex-q" class="form-control form-control-sm" placeholder="<@s.text name='admin.extensions.search'/>" aria-label="<@s.text name='admin.extensions.search'/>" autocomplete="off">
            </div>

            <#-- installed core types and extensions: one table, core types first -->
            <section id="ex-installed" class="ex-panel" role="tabpanel" aria-labelledby="ex-tab-installed">
                <#if extensionsToUpdate gt 0>
                    <div class="ex-chips" role="group">
                        <button type="button" class="ex-chip on" data-filter="all"><@s.text name="admin.extensions.filter.all"/></button>
                        <button type="button" class="ex-chip" data-filter="update"><@s.text name="admin.extensions.toUpdate"/>: ${extensionsToUpdate}</button>
                    </div>
                </#if>
                <#if extensions?has_content>
                    <table class="ov-table ex-table">
                        <thead>
                        <tr>
                            <th><@s.text name="admin.extensions.col.name"/></th>
                            <th><@s.text name="admin.extensions.col.version"/></th>
                            <th class="ov-num ex-hide-md"><@s.text name="admin.extensions.col.terms"/></th>
                            <th><@s.text name="admin.extensions.col.usedBy"/></th>
                            <th><span class="visually-hidden"><@s.text name="admin.extensions.col.actions"/></span></th>
                        </tr>
                        </thead>
                        <tbody>
                        <#if coreTypes?has_content>
                            <tr class="ov-grp"><td colspan="5"><@s.text name="admin.extension.coreTypes"/></td></tr>
                            <#list coreTypes as ext><@extensionRow ext/></#list>
                        </#if>
                        <#if nonCoreTypes?has_content>
                            <tr class="ov-grp"><td colspan="5"><@s.text name="admin.extension.extensions"/></td></tr>
                            <#list nonCoreTypes as ext><@extensionRow ext/></#list>
                        </#if>
                        </tbody>
                    </table>
                <#else>
                    <div class="ex-empty"><@s.text name="admin.extensions.noneInstalled"/></div>
                </#if>
                <div class="ex-empty" data-no-match hidden><@s.text name="admin.extensions.noMatch"/></div>
            </section>

            <#-- core types and extensions registered with GBIF and not installed yet -->
            <section id="ex-notinstalled" class="ex-panel" role="tabpanel" aria-labelledby="ex-tab-notinstalled" hidden>
                <#if registryUnreachable>
                    <div class="ex-empty"><b><@s.text name="admin.extensions.notInstalled.unreachable"/></b><br><@s.text name="admin.extensions.notInstalled.unreachable.text"/></div>
                <#elseif !newExtensions?has_content>
                    <div class="ex-empty"><@s.text name="extension.already.installed"/></div>
                <#else>
                    <div class="ex-chips" role="group">
                        <button type="button" class="ex-chip on" data-filter="all"><@s.text name="admin.extensions.filter.all"/></button>
                        <button type="button" class="ex-chip" data-filter="core"><@s.text name="admin.extension.coreTypes"/></button>
                        <#list coreNames as c>
                            <button type="button" class="ex-chip" data-filter="for:${c}"><@s.text name="admin.extensions.filter.for"><@s.param>${c}</@s.param></@s.text></button>
                        </#list>
                    </div>
                    <div class="ex-cards">
                        <#list newExtensions as ext>
                            <#assign filters = ext.core?then("core", ((ext.subject!"")?trim?has_content?then((worksWith[ext.rowType])![], coreNames))?map(c -> "for:" + c)?join(" "))/>
                            <div class="ex-card" data-search="${(ext.title + ' ' + (ext.rowType!'') + ' ' + ext.description!'')?lower_case}" data-filters="${filters}">
                                <h3>${ext.title}<#if ext.core> <span class="ov-pill grey"><@s.text name="admin.extensions.core"/></span></#if></h3>
                                <span class="ex-rowtype">${ext.rowType!}</span>
                                <p>${ext.description!?truncate(300)}</p>
                                <@worksWithLine ext/>
                                <div class="ex-card-foot">
                                    <span class="ex-small"><@s.text name="admin.extensions.issued"><@s.param><@issued date=ext.issued!""/></@s.param></@s.text></span>
                                    <form action="extension.do" method="post" class="ex-install-form">
                                        <input type="hidden" name="url" value="${ext.url}"/>
                                        <button type="submit" name="install" value="Install" class="btn btn-sm btn-outline-gbif-primary">
                                            <span class="ex-busy-text" hidden><@icon name="autorenew" class="ex-spin"/> <@s.text name="admin.extensions.installing"/></span>
                                            <span class="ex-idle-text"><@icon name="download"/> <@s.text name="button.install"/></span>
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </#list>
                    </div>
                    <div class="ex-empty" data-no-match hidden><@s.text name="admin.extensions.noMatch"/></div>
                </#if>
            </section>

            <#-- installed vocabularies, with the installed extensions that use them -->
            <section id="ex-vocabularies" class="ex-panel" role="tabpanel" aria-labelledby="ex-tab-vocabularies" hidden>
                <p class="ex-help"><@s.text name="admin.extensions.vocabularies.help"/></p>
                <div class="ex-chips" role="group">
                    <button type="button" class="ex-chip on" data-filter="all"><@s.text name="admin.extensions.filter.all"/></button>
                    <#if (vocabulariesToUpdate > 0)><button type="button" class="ex-chip" data-filter="update"><@s.text name="admin.extensions.toUpdate"/>: ${vocabulariesToUpdate}</button></#if>
                    <button type="button" class="ex-chip" data-filter="default"><@s.text name="admin.extensions.filter.default"/></button>
                    <button type="button" class="ex-chip" data-filter="unused"><@s.text name="admin.extensions.filter.unused"/></button>
                </div>
                <#if vocabularies?has_content>
                    <table class="ov-table ex-table">
                        <thead>
                        <tr>
                            <th><@s.text name="admin.extensions.col.vocabulary"/></th>
                            <th><@s.text name="admin.extensions.col.version"/></th>
                            <th class="ov-num ex-hide-md"><@s.text name="admin.extensions.col.concepts"/></th>
                            <th class="ov-num ex-hide-md"><@s.text name="admin.extensions.col.languages"/></th>
                            <th><@s.text name="admin.extensions.col.usedBy"/></th>
                            <th><span class="visually-hidden"><@s.text name="admin.extensions.col.actions"/></span></th>
                        </tr>
                        </thead>
                        <tbody>
                        <#list vocabularies as v>
                            <#assign isDefault = action.isDefaultVocabulary(v.uriString)/>
                            <#assign usingExtensions = (vocabularyUsedBy[v.uriString])![]/>
                            <#assign filters = [(!registryUnreachable && !v.latest)?then("update", ""), isDefault?then("default", ""), usingExtensions?has_content?then("", "unused")]?filter(f -> f?has_content)?join(" ")/>
                            <tr class="ex-row" data-search="${(v.title + ' ' + v.uriString)?lower_case}" data-filters="${filters}">
                                <td class="ex-name">
                                    <b>${v.title}</b><#if isDefault> <span class="ov-pill grey" title="<@s.text name='admin.extensions.default.help'/>"><@s.text name="admin.extensions.default"/></span></#if>
                                    <span class="ex-rowtype">${v.uriString}</span>
                                </td>
                                <td class="ex-ver">
                                    <@version issuedDate=v.issued!"" latest=v.latest newer=newerVocabularyIssued[v.uriString]!""/>
                                    <#if !registryUnreachable && !v.latest><span class="ex-small"><@s.text name="admin.extensions.vocabulary.updatedBySynchronise"/></span></#if>
                                </td>
                                <td class="ov-num ex-hide-md">${v.concepts?size}</td>
                                <td class="ov-num ex-hide-md">${(vocabularyLanguages[v.uriString])!0}</td>
                                <td class="ex-usedby">
                                    <#if usingExtensions?has_content>
                                        <#list usingExtensions as e>${e.title}<#sep>, </#sep></#list>
                                    <#elseif isDefault>
                                        <span class="ex-muted"><@s.text name="admin.extensions.usedBy.ipt"/></span>
                                    <#else>
                                        <span class="ex-muted"><@s.text name="admin.extensions.usedBy.noExtension"/></span>
                                    </#if>
                                </td>
                                <td>
                                    <div class="ex-acts">
                                        <a href="vocabulary.do?id=${v.uriString?url}" class="ex-act custom-link" title="<@s.text name='admin.extensions.viewConcepts'/>"><@icon name="visibility"/><@s.text name="button.view"/><span class="visually-hidden">: ${v.title}</span></a>
                                    </div>
                                </td>
                            </tr>
                        </#list>
                        </tbody>
                    </table>
                </#if>
                <div class="ex-empty" data-no-match hidden><@s.text name="admin.extensions.noMatch"/></div>
            </section>
        </main>
    </div>
</div>

<#-- update: says which resources get their mappings moved, filled in by js/extensions.js from the button -->
<div class="modal fade" id="ex-update-modal" tabindex="-1" aria-labelledby="ex-update-modal-title" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <form class="modal-content" action="updateExtension.do" method="post">
            <input type="hidden" name="id" value="">
            <div class="modal-header">
                <h5 class="modal-title" id="ex-update-modal-title" data-template="<@s.text name='admin.extensions.update.title'/>"></h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="<@s.text name='button.cancel'/>"></button>
            </div>
            <div class="modal-body text-smaller">
                <p data-versions data-template="<@s.text name='admin.extensions.update.versions'/>"></p>
                <div data-with-resources>
                    <p class="mb-1"><@s.text name="admin.extensions.update.resources"/></p>
                    <ul class="ex-modal-list"></ul>
                    <p class="mb-0"><@s.text name="admin.extensions.update.resources.after"/></p>
                </div>
                <p class="mb-0" data-without-resources><@s.text name="admin.extensions.update.noResources"/></p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal"><@s.text name="button.cancel"/></button>
                <button type="submit" class="btn btn-sm btn-gbif-solid" name="update" value="Update"><@s.text name="button.update"/></button>
            </div>
        </form>
    </div>
</div>

<#-- remove: only offered when no resource maps the extension -->
<div class="modal fade" id="ex-remove-modal" tabindex="-1" aria-labelledby="ex-remove-modal-title" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <form class="modal-content" action="extension.do" method="post">
            <input type="hidden" name="id" value="">
            <div class="modal-header">
                <h5 class="modal-title" id="ex-remove-modal-title" data-template="<@s.text name='admin.extensions.remove.title'/>"></h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="<@s.text name='button.cancel'/>"></button>
            </div>
            <div class="modal-body text-smaller">
                <p class="mb-0"><@s.text name="admin.extensions.remove.text"/></p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal"><@s.text name="button.cancel"/></button>
                <button type="submit" class="btn btn-sm btn-danger" name="deleteFlag" value="Delete"><@s.text name="button.remove"/></button>
            </div>
        </form>
    </div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
