<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.VocabulariesAction" -->
<#--
  The vocabulary page, shared by admin/vocabulary.ftl and manage/vocabulary.ftl (opened from mappings and translations).
  The including page sets vocabularyArea: "admin" (breadcrumb to the extensions page, Back, links to the extension
  pages, version checked against the Registry) or "manage" (none of these: managers can't open the admin pages).
-->
<#include "/WEB-INF/pages/inc/header.ftl">
<title>${vocabulary.title} - <@s.text name="admin.vocabulary.title"/></title>
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/extensions.css">
<style>
    /* the fixed navbar (57px) and a small gap, as on the extension page */
    :root { --navbar-height: 72px; }
</style>
<script src="${baseURL}/js/scrollspy.js"></script>
<#-- search, show all details, copy and opening at the concept in the URL fragment work as on the extension page -->
<script src="${baseURL}/js/extension.js"></script>
<#assign currentMenu = vocabularyArea/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<#assign isAdmin = vocabularyArea == "admin"/>
<#assign dateFormat = "d MMMM yyyy"/>
<#assign concepts = vocabulary.concepts/>
<#assign isDefault = action.isDefaultVocabulary()/>
<#assign outdated = isAdmin && versionChecked && !vocabulary.latest/>
<#macro issued date=""><#if date?is_date_like>${date?date?string[dateFormat]}<#else>-</#if></#macro>
<#macro copyButton value label><button type="button" class="ext-copy" data-copy="${value}" data-copied="<@s.text name='admin.extension.copied'/>" title="<@s.text name='button.copy'/>" aria-label="<@s.text name='button.copy'/>: ${label}"><@icon name="content_copy"/></button></#macro>

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
                            <#if isAdmin>
                                <li class="breadcrumb-item"><a href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                                <li class="breadcrumb-item"><a href="${baseURL}/admin/extensions.do#vocabularies"><@s.text name="breadcrumb.admin.extensions"/></a></li>
                            <#else>
                                <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                            </#if>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.vocabulary"/></li>
                        </ol>
                    </nav>
                </div>
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal">${vocabulary.title}</h1>
                <#-- issued and, under administration, the version state, on one line -->
                <div class="ov-meta">
                    <span><@s.text name="admin.extensions.issued"><@s.param><@issued date=vocabulary.issued!""/></@s.param></@s.text><#t>
                        <#if isAdmin> · <#t>
                            <#if registryUnreachable><@s.text name="admin.extensions.version.notChecked"/><#t>
                            <#elseif outdated><span class="ex-up ext-version"><@icon name="warning"/><@s.text name="admin.extensions.version.newer"><@s.param><@issued date=newerIssued!""/></@s.param></@s.text></span><#t>
                            <#else><span class="ex-ok"><@s.text name="admin.extensions.version.latest"/></span><#t>
                            </#if><#t>
                        </#if><#t>
                    </span>
                    <#if isDefault><span class="ov-pill grey" title="<@s.text name='admin.extensions.default.help'/>"><@s.text name="admin.extensions.default"/></span></#if>
                </div>
            </div>
            <div class="ov-acts">
                <#if isAdmin>
                    <a href="${baseURL}/admin/extensions.do#vocabularies" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="button.back"/></a>
                    <#if vocabulary.link??>
                        <div class="dropdown">
                            <button type="button" class="btn btn-sm btn-outline-secondary top-button ov-more-btn" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='admin.extensions.moreActions'/>" aria-label="<@s.text name='admin.extensions.moreActions'/>"><@icon name="more_horiz"/></button>
                            <ul class="dropdown-menu dropdown-menu-end">
                                <li><a class="dropdown-item" href="${vocabulary.link}" target="_blank" rel="noopener"><@s.text name="admin.extensions.documentation"/> <@icon name="open_in_new"/></a></li>
                            </ul>
                        </div>
                    </#if>
                <#elseif vocabulary.link??>
                    <#-- opened in a new tab from the mapping and translation pages: no Back -->
                    <a href="${vocabulary.link}" target="_blank" rel="noopener" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="admin.extensions.documentation"/> <@icon name="open_in_new"/></a>
                </#if>
            </div>
        </div>
    </div>
</div>

<div class="container-fluid bg-body">
    <div class="container my-md-4 bd-layout main-content-container">
        <main class="bd-main">
            <div class="bd-toc ext-side mt-4 ps-3 mb-lg-5 text-muted">
                <div class="fw-semibold text-gbif-header-2 mb-2 text-smaller"><@s.text name="manage.overview.onThisPage"/></div>
                <nav id="sidebar-content" aria-label="<@s.text name='manage.overview.onThisPage'/>">
                    <ul class="ext-toc">
                        <li><a class="sidebar-navigation-link" href="#anchor-ext-description"><@s.text name="basic.description"/></a></li>
                        <li><a class="sidebar-navigation-link" href="#anchor-ext-usedby"><@s.text name="admin.extensions.col.usedBy"/> <small>${usedBy?size}</small></a></li>
                        <li><a class="sidebar-navigation-link" href="#anchor-ext-terms"><@s.text name="admin.extensions.col.concepts"/> <small data-toc-count>${concepts?size}</small></a></li>
                    </ul>
                </nav>
            </div>

            <div class="bd-content ps-lg-4">
                <#if registryUnreachable>
                    <div class="ex-banner ex-banner-danger mt-4" role="status">
                        <@icon name="error" class="ex-banner-icon"/>
                        <div class="ex-banner-body">
                            <h2><@s.text name="admin.extensions.registry.title"/></h2>
                            <div class="ex-banner-text"><@s.text name="admin.extensions.registry.text"><@s.param>${cfg.registryUrl}</@s.param></@s.text></div>
                        </div>
                    </div>
                <#elseif outdated>
                    <div class="ex-banner ex-banner-warning mt-4" role="status">
                        <@icon name="warning" class="ex-banner-icon"/>
                        <div class="ex-banner-body">
                            <h2><@s.text name="admin.extensions.version.newer"><@s.param><@issued date=newerIssued!""/></@s.param></@s.text></h2>
                            <div class="ex-banner-text"><@s.text name="admin.extensions.vocabulary.updatedBySynchronise"/></div>
                        </div>
                    </div>
                </#if>

                <span class="ext-anchor" id="anchor-ext-description"></span>
                <section id="ext-description" class="ext-section">
                    <div class="ext-sec-head"><h2><@s.text name="basic.description"/></h2></div>
                    <#if vocabulary.description?has_content><p class="ext-desc">${vocabulary.description}</p></#if>

                    <div class="ext-boxes">
                        <div class="ext-box">
                            <h3><@s.text name="admin.extension.details"/></h3>
                            <dl class="ext-kv ext-kv-2">
                                <dt><@s.text name="basic.identifier"/></dt>
                                <dd><span class="ext-mono">${vocabulary.uriString}</span><@copyButton value=vocabulary.uriString label=action.getText("basic.identifier")/></dd>
                                <dt><@s.text name="admin.extensions.col.concepts"/></dt><dd>${concepts?size}</dd>
                                <#-- languages with labels, by name; how many concepts each covers in the tooltip -->
                                <dt><@s.text name="admin.extensions.col.languages"/></dt>
                                <dd><#if labelLanguages?has_content><#list labelLanguages as l, n><span<#if !cfg.isSupportedLanguage(l)> class="ex-muted"</#if> title="${n} / ${concepts?size}<#if !cfg.isSupportedLanguage(l)> · <@s.text name='admin.extension.translations.notInterface'/></#if>">${action.languageName(l)}</span><#sep>, </#sep></#list><#else><span class="ex-muted">-</span></#if></dd>
                                <dt><@s.text name="basic.lastModified"/></dt><dd><@issued date=vocabulary.modified!""/></dd>
                                <#if vocabulary.subject?has_content><dt><@s.text name="basic.keywords"/></dt><dd>${vocabulary.subject}</dd></#if>
                            </dl>
                        </div>
                    </div>
                </section>

                <#-- the extension terms whose values come from this vocabulary -->
                <span class="ext-anchor" id="anchor-ext-usedby"></span>
                <section id="ext-usedby" class="ext-section">
                    <div class="ext-sec-head">
                        <h2><@s.text name="admin.extensions.col.usedBy"/></h2>
                        <span><@s.text name="admin.vocabulary.usedBy.desc"/></span>
                    </div>
                    <#if usedBy?has_content>
                        <table class="ov-table ext-usage">
                            <thead>
                            <tr>
                                <th><@s.text name="admin.extension.title"/></th>
                                <th><@s.text name="admin.extensions.col.terms"/></th>
                            </tr>
                            </thead>
                            <tbody>
                            <#list usedBy as u>
                                <tr>
                                    <td>
                                        <#if isAdmin><a class="resource-table-link" href="${baseURL}/admin/extension.do?id=${u.extension.rowType?url}">${u.extension.title}</a><#else>${u.extension.title}</#if>
                                        <div class="ext-shortname">${u.extension.rowType}</div>
                                    </td>
                                    <td><#list u.terms as p><#if isAdmin><a href="${baseURL}/admin/extension.do?id=${u.extension.rowType?url}#${p.qualname?url}">${p.name}</a><#else>${p.name}</#if><#sep>, </#sep></#list></td>
                                </tr>
                            </#list>
                            </tbody>
                        </table>
                    <#else>
                        <p class="ex-muted text-smaller mb-0"><#if isDefault><@s.text name="admin.extensions.usedBy.ipt"/><#else><@s.text name="admin.extensions.usedBy.noExtension"/></#if></p>
                    </#if>
                </section>

                <#-- concepts: label in the interface language, the value to write in the data, then the rest under Details -->
                <span class="ext-anchor" id="anchor-ext-terms"></span>
                <section id="ext-terms" class="ext-section">
                    <div class="ext-sec-head">
                        <h2><@s.text name="admin.extensions.col.concepts"/></h2>
                        <span><@s.text name="admin.vocabulary.concepts.desc"/></span>
                    </div>
                    <div class="ext-toolbar">
                        <input type="search" id="ext-q" class="form-control form-control-sm" placeholder="<@s.text name='admin.vocabulary.search'/>" aria-label="<@s.text name='admin.vocabulary.search'/>" autocomplete="off">
                        <span class="flex-grow-1"></span>
                        <button type="button" class="ex-link-btn" id="ext-all-details" data-show="<@s.text name='admin.extension.details.showAll'/>" data-hide="<@s.text name='admin.extension.details.hideAll'/>"><@s.text name="admin.extension.details.showAll"/></button>
                    </div>

                    <#list concepts as c>
                        <#assign label = action.labelOf(c)!""/>
                        <#assign alternatives = action.alternativesOf(c)/>
                        <div class="ext-term" id="concept-${c.identifier?replace(" ", "_")}"
                             data-search="${action.searchTextOf(c)}">
                            <div class="ext-term-name">
                                <b><#if label?has_content>${label}<#else>${c.identifier}</#if></b>
                                <span class="ext-mono">${c.identifier}<@copyButton value=c.identifier label=action.getText("admin.vocabulary.value")/></span>
                                <#if action.isEnglishFallback(c)>
                                    <div class="ext-tags"><span class="ext-tag ext-tag-fallback" title="<@s.text name='admin.extension.term.fallback.help'/>"><@s.text name="admin.extension.term.fallback"/></span></div>
                                </#if>
                            </div>
                            <div class="ext-term-body">
                                <#if c.description?has_content><p>${c.description}</p></#if>
                                <#if alternatives?has_content><p class="ext-comments"><@s.text name="admin.vocabulary.alternatives"/>: ${alternatives?join(", ")}</p></#if>
                                <details class="ext-details">
                                    <summary>
                                        <#if c.uri?has_content><span class="ext-mono">${c.uri}</span></#if>
                                        <span class="ext-details-toggle"><@s.text name="admin.extension.details"/><@icon name="expand_more"/></span>
                                    </summary>
                                    <dl class="ext-kv">
                                        <dt><@s.text name="admin.vocabulary.value"/></dt><dd class="ext-mono">${c.identifier}</dd>
                                        <#if c.uri?has_content><dt><@s.text name="vocabulary.uri"/></dt><dd class="ext-mono"><#if c.uri?starts_with("http")><a href="${c.uri}" target="_blank" rel="noopener">${c.uri}</a><#else>${c.uri}</#if></dd></#if>
                                        <#if c.link?has_content><dt><@s.text name="basic.seealso"/></dt><dd><a href="${c.link}" target="_blank" rel="noopener">${c.link}</a></dd></#if>
                                        <#if c.preferredTerms?has_content>
                                            <dt><@s.text name="admin.vocabulary.labels"/></dt>
                                            <dd><#list action.byLanguage(c.preferredTerms) as t><span class="ext-chipv"<#if t.lang?has_content> lang="${t.lang}"</#if>><#if t.lang?has_content>${t.lang} · </#if>${t.title}</span></#list></dd>
                                        </#if>
                                        <#if c.alternativeTerms?has_content>
                                            <dt><@s.text name="admin.vocabulary.alternatives"/></dt>
                                            <dd><#list action.byLanguage(c.alternativeTerms) as t><span class="ext-chipv"<#if t.lang?has_content> lang="${t.lang}"</#if>><#if t.lang?has_content>${t.lang} · </#if>${t.title}</span></#list></dd>
                                        </#if>
                                    </dl>
                                </details>
                            </div>
                        </div>
                    </#list>
                    <div class="ex-empty" id="ext-no-match" hidden><@s.text name="admin.extensions.noMatch"/></div>
                </section>
            </div>
        </main>
    </div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
