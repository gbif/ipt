<#ftl output_format="HTML">
<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.ExtensionsAction" -->
<#include "/WEB-INF/pages/inc/header.ftl">
<title>${extension.title} - <@s.text name="admin.extension.title"/></title>
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/extensions.css">
<style>
    /* the fixed navbar (57px) and a small gap: where sections stop when jumped to (js/scrollspy.js, scroll-margin-top)
       and where the sidebar sticks, so both line up just under the navbar */
    :root { --navbar-height: 72px; }
</style>
<script src="${baseURL}/js/extensions.js"></script>
<script src="${baseURL}/js/scrollspy.js"></script>
<script src="${baseURL}/js/extension.js"></script>
<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<#assign dateFormat = "d MMMM yyyy"/>
<#assign lang = action.localeLanguage!"en"/>
<#assign groups = propertiesByGroup?keys/>
<#assign properties = extension.properties/>
<#assign requiredCount = properties?filter(p -> p.required)?size/>
<#assign vocabularyCount = properties?filter(p -> p.vocabulary??)?size/>
<#assign mappedCount = properties?filter(p -> termUsedBy[p.qualname]??)?size/>
<#assign resourceCount = extensionUsage?size/>
<#assign outdated = !registryUnreachable && !extension.latest/>
<#-- text from an extension definition: escaped, with `backticks` shown as code -->
<#macro rich text>${(text?esc?markup_string)?replace("`([^`]*)`", "<code>$1</code>", "r")?no_esc}</#macro>
<#-- a term's text in the interface language, else the definition's own (English) -->
<#function pick tr key fallback><#return (tr[key])?has_content?then(tr[key], fallback)></#function>
<#macro issued date=""><#if date?is_date_like>${date?date?string[dateFormat]}<#else>-</#if></#macro>

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
                            <li class="breadcrumb-item"><a href="${baseURL}/admin/extensions.do"><@s.text name="breadcrumb.admin.extensions"/></a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="admin.extension.title"/></li>
                        </ol>
                    </nav>
                </div>
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">${extension.title}</h1>
                <#-- issued and the version state, on one line -->
                <div class="ov-meta">
                    <span><@s.text name="admin.extensions.issued"><@s.param><@issued date=extension.issued!""/></@s.param></@s.text> · <#t>
                        <#if registryUnreachable><@s.text name="admin.extensions.version.notChecked"/><#t>
                        <#elseif extension.latest><span class="ex-ok"><@s.text name="admin.extensions.version.latest"/></span><#t>
                        <#else><span class="ex-up ext-version"><@icon name="warning"/><@s.text name="admin.extensions.version.newer"><@s.param><@issued date=newerIssued[extension.rowType]!""/></@s.param></@s.text></span><#t>
                        </#if><#t>
                    </span>
                </div>
            </div>
            <div class="ov-acts">
                <#if outdated>
                    <button type="button" class="btn btn-sm btn-gbif-solid top-button" data-bs-toggle="modal" data-bs-target="#ex-update-modal"
                            data-id="${extension.rowType}" data-title="${extension.title}"
                            data-from="<@issued date=extension.issued!""/>" data-to="<@issued date=newerIssued[extension.rowType]!""/>"
                            data-resources="<#if resourceCount gt 0>ext-resource-names</#if>">
                        <@icon name="update" class="me-1"/><@s.text name="button.update"/>
                    </button>
                </#if>
                <a href="${baseURL}/admin/extensions.do" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="button.back"/></a>
                <#-- more actions last, as on the resource overview -->
                <div class="dropdown">
                    <button type="button" class="btn btn-sm btn-outline-secondary top-button ov-more-btn" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='admin.extensions.moreActions'/>" aria-label="<@s.text name='admin.extensions.moreActions'/>"><@icon name="more_horiz"/></button>
                    <ul class="dropdown-menu dropdown-menu-end">
                        <#if extension.link??><li><a class="dropdown-item" href="${extension.link}" target="_blank" rel="noopener"><@s.text name="admin.extensions.documentation"/> <@icon name="open_in_new"/></a></li></#if>
                        <#-- the divider only when Documentation is above Remove -->
                        <#if extension.link??><li><hr class="dropdown-divider"></li></#if>
                        <li>
                            <#if resourceCount gt 0>
                                <span class="dropdown-item text-gbif-danger disabled" aria-disabled="true">
                                    <@s.text name="button.remove"/>
                                    <small><@s.text name="admin.extensions.remove.mapped"/></small>
                                </span>
                            <#else>
                                <button type="button" class="dropdown-item text-gbif-danger" data-bs-toggle="modal" data-bs-target="#ex-remove-modal" data-id="${extension.rowType}" data-title="${extension.title}">
                                    <@s.text name="button.remove"/>
                                </button>
                            </#if>
                        </li>
                    </ul>
                </div>
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
                        <li><a class="sidebar-navigation-link" href="#anchor-ext-usedby"><@s.text name="admin.extensions.col.usedBy"/> <small>${resourceCount}</small></a></li>
                        <li><a class="sidebar-navigation-link" href="#anchor-ext-terms"><@s.text name="admin.extensions.col.terms"/> <small data-toc-count>${properties?size}</small></a></li>
                        <#if (groups?size > 1)>
                            <#list groups as g>
                                <li><a class="sidebar-navigation-link ext-toc-sub" href="#anchor-ext-group-${g?index}"><span><#if g?has_content>${g}<#else><@s.text name="manage.mapping.noClass"/></#if></span> <small data-toc-count="${g?index}">${propertiesByGroup[g]?size}</small></a></li>
                            </#list>
                        </#if>
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
                            <h2><@s.text name="admin.extensions.version.newer"><@s.param><@issued date=newerIssued[extension.rowType]!""/></@s.param></@s.text></h2>
                            <div class="ex-banner-text"><@s.text name="admin.extension.version.warning"/></div>
                        </div>
                    </div>
                </#if>

                <span class="ext-anchor" id="anchor-ext-description"></span>
                <section id="ext-description" class="ext-section">
                    <div class="ext-sec-head"><h2><@s.text name="basic.description"/></h2></div>
                    <#if extension.description?has_content><p class="ext-desc">${extension.description}</p></#if>

                    <div class="ext-boxes">
                        <div class="ext-box">
                            <h3><@s.text name="admin.extension.details"/></h3>
                            <dl class="ext-kv ext-kv-2">
                                <dt><@s.text name="admin.extension.type"/></dt><dd><#if extension.core><@s.text name="admin.extensions.core"/><#else><@s.text name="admin.extension.title"/></#if></dd>
                                <dt><@s.text name="extension.rowtype"/></dt>
                                <dd><span class="ext-mono">${extension.rowType}</span><button type="button" class="ext-copy" data-copy="${extension.rowType}" data-copied="<@s.text name='admin.extension.copied'/>" title="<@s.text name='button.copy'/>" aria-label="<@s.text name='button.copy'/>: <@s.text name='extension.rowtype'/>"><@icon name="content_copy"/></button></dd>
                                <dt><@s.text name="basic.name"/></dt><dd class="ext-mono">${extension.name}</dd>
                                <dt><@s.text name="basic.namespace"/></dt><dd class="ext-mono">${extension.namespace}</dd>
                                <#if !extension.core>
                                    <dt><@s.text name="admin.extensions.worksWith"/></dt>
                                    <dd class="ex-works">
                                        <#if (extension.subject!"")?trim?has_content>
                                            <#if (worksWith[extension.rowType]![])?has_content><#list worksWith[extension.rowType] as c><i>${c}</i></#list><#else><i><@s.text name="admin.extensions.worksWith.none"/></i></#if>
                                        <#else>
                                            <i><@s.text name="admin.extensions.worksWith.any"/></i>
                                        </#if>
                                    </dd>
                                </#if>
                                <dt><@s.text name="admin.extensions.col.terms"/></dt><dd>${properties?size}</dd>
                                <dt><@s.text name="extension.prop.required"/></dt><dd>${requiredCount}</dd>
                                <dt><@s.text name="admin.extension.filter.vocabulary"/></dt><dd>${vocabularyCount}</dd>
                                <#-- languages with term labels, by name; how many terms each covers in the tooltip -->
                                <dt><@s.text name="admin.extension.translations"/></dt>
                                <dd><#if labelTranslations?has_content><#list labelTranslations as l, n><span<#if !cfg.isSupportedLanguage(l)> class="ex-muted"</#if> title="${n} / ${properties?size}<#if !cfg.isSupportedLanguage(l)> · <@s.text name='admin.extension.translations.notInterface'/></#if>">${action.languageName(l)}</span><#sep>, </#sep></#list><#else><span class="ex-muted"><@s.text name="admin.extension.translations.none"/></span></#if></dd>
                            </dl>
                        </div>
                    </div>
                </section>

                <#-- the resources mapping it: a table, the first 5 shown, as there are often many -->
                <span class="ext-anchor" id="anchor-ext-usedby"></span>
                <section id="ext-usedby" class="ext-section">
                    <div class="ext-sec-head">
                        <h2><@s.text name="admin.extensions.col.usedBy"/></h2>
                        <span><#if resourceCount gt 0><@s.text name="admin.extensions.usedBy.count"><@s.param>${resourceCount}</@s.param></@s.text> · </#if><@s.text name="admin.extension.usedBy.desc"/></span>
                    </div>
                    <#if extensionUsage?has_content>
                        <table class="ov-table ext-usage">
                            <thead>
                            <tr>
                                <th><@s.text name="admin.extension.col.resource"/></th>
                                <th><@s.text name="admin.extension.col.termsMapped"/></th>
                            </tr>
                            </thead>
                            <tbody>
                            <#list extensionUsage as u>
                                <tr<#if u?index gte 5> class="ext-more-row" hidden</#if>>
                                    <td><a class="resource-table-link" href="${baseURL}/manage/resource.do?r=${u.resource.shortname?url}">${action.titleOf(u.resource)}</a><div class="ext-shortname">${u.resource.shortname}</div></td>
                                    <td class="ext-mapped">
                                        <span class="ext-bar"><i style="width:${(properties?size > 0)?then(u.termsMapped * 100 / properties?size, 0)?c}%"></i></span>
                                        <span class="ext-n">${u.termsMapped} / ${properties?size}</span>
                                    </td>
                                </tr>
                            </#list>
                            </tbody>
                        </table>
                        <#if resourceCount gt 5>
                            <button type="button" class="ex-link-btn ext-usage-all" id="ext-usage-all"><@s.text name="admin.extension.usedBy.showAll"/>: ${resourceCount}<@icon name="expand_more" class="ex-chev"/></button>
                        </#if>
                        <#-- names for the update dialog -->
                        <ul id="ext-resource-names" hidden><#list extensionUsage as u><li>${action.titleOf(u.resource)}</li></#list></ul>
                    <#else>
                        <p class="ex-muted text-smaller mb-0"><@s.text name="admin.extensions.usedBy.none"/></p>
                    </#if>
                </section>

                <span class="ext-anchor" id="anchor-ext-terms"></span>
                <section id="ext-terms" class="ext-section">
                    <div class="ext-sec-head">
                        <h2><@s.text name="admin.extensions.col.terms"/></h2>
                        <span><@s.text name="admin.extension.terms.desc"><@s.param>${groups?size}</@s.param></@s.text></span>
                    </div>
                    <div class="ext-toolbar">
                        <div class="ex-chips" role="group" id="ext-chips">
                            <button type="button" class="ex-chip on" data-filter="all"><@s.text name="admin.extensions.filter.all"/>: <span data-chip-count>${properties?size}</span></button>
                            <#if requiredCount gt 0><button type="button" class="ex-chip" data-filter="req"><@s.text name="extension.prop.required"/>: <span data-chip-count>${requiredCount}</span></button></#if>
                            <#if vocabularyCount gt 0><button type="button" class="ex-chip" data-filter="voc"><@s.text name="admin.extension.filter.vocabulary"/>: <span data-chip-count>${vocabularyCount}</span></button></#if>
                            <#if resourceCount gt 0>
                                <button type="button" class="ex-chip" data-filter="used"><@s.text name="admin.extension.filter.used"/>: <span data-chip-count>${mappedCount}</span></button>
                                <button type="button" class="ex-chip" data-filter="unused"><@s.text name="admin.extensions.usedBy.none"/>: <span data-chip-count>${properties?size - mappedCount}</span></button>
                            </#if>
                        </div>
                        <input type="search" id="ext-q" class="form-control form-control-sm" placeholder="<@s.text name='admin.extension.search'/>" aria-label="<@s.text name='admin.extension.search'/>" autocomplete="off">
                        <span class="flex-grow-1"></span>
                        <button type="button" class="ex-link-btn" id="ext-all-details" data-show="<@s.text name='admin.extension.details.showAll'/>" data-hide="<@s.text name='admin.extension.details.hideAll'/>"><@s.text name="admin.extension.details.showAll"/></button>
                    </div>

                    <#list propertiesByGroup as group, groupProperties>
                        <span class="ext-anchor" id="anchor-ext-group-${group?index}"></span>
                        <section class="ext-group" id="ext-group-${group?index}" data-group="${group?index}">
                            <#if (groups?size > 1) || group?has_content>
                                <h3 class="ext-group-title"><#if group?has_content>${group}<#else><@s.text name="manage.mapping.noClass"/></#if> <small data-group-count>${groupProperties?size}</small></h3>
                            </#if>
                            <#list groupProperties as p>
                                <#assign tr = (p.translations[lang])!{}/>
                                <#assign label = pick(tr, "label", p.label!"")/>
                                <#assign description = pick(tr, "description", p.description!"")/>
                                <#assign comments = pick(tr, "comments", p.comments!"")/>
                                <#assign examples = pick(tr, "examples", p.examples!"")/>
                                <#assign englishFallback = lang != "en" && !(tr["label"])?has_content && (p.label!"")?has_content/>
                                <#assign users = (termUsedBy[p.qualname])![]/>
                                <#assign filters = [p.required?then("req", ""), (p.vocabulary??)?then("voc", ""), users?has_content?then("used", "unused")]?filter(f -> f?has_content)?join(" ")/>
                                <#assign qualnameDiffers = p.qualname != (p.namespace()!"") + p.name/>
                                <div class="ext-term" id="${p.qualname}" data-filters="${filters}"
                                     data-search="${[p.name, p.label!"", label, p.description!"", description, comments, p.qualname]?join(" ")?lower_case}">
                                    <div class="ext-term-name">
                                        <b><#if label?has_content>${label}<#else>${p.name}</#if></b>
                                        <span class="ext-mono">${p.name}</span>
                                        <div class="ext-tags">
                                            <#if p.required><span class="ext-tag ext-tag-req"><@s.text name="extension.prop.required"/></span></#if>
                                            <#if p.type?? && "${p.type}" != "string"><span class="ext-tag">${p.type}</span></#if>
                                            <#if p.vocabulary??><a class="ext-tag ext-tag-voc" href="vocabulary.do?id=${p.vocabulary.uriString?url}" title="${p.vocabulary.title!}"><@s.text name="extension.vocabulary"/></a></#if>
                                            <#if englishFallback><span class="ext-tag ext-tag-fallback" title="<@s.text name='admin.extension.term.fallback.help'/>"><@s.text name="admin.extension.term.fallback"/></span></#if>
                                        </div>
                                    </div>
                                    <div class="ext-term-body">
                                        <#if description?has_content><p><@rich description/></p></#if>
                                        <#if comments?has_content><p class="ext-comments"><@rich comments/></p></#if>
                                        <#if examples?has_content><p class="ext-examples"><em><@s.text name="basic.examples"/>:</em> <@rich examples/></p></#if>
                                        <details class="ext-details">
                                            <summary>
                                                <span class="ext-used"><#if resourceCount gt 0><#if users?has_content><@s.text name="admin.extension.term.mappedIn"><@s.param>${users?size}</@s.param><@s.param>${resourceCount}</@s.param></@s.text><#else><@s.text name="admin.extensions.usedBy.none"/></#if></#if></span>
                                                <#if p.vocabulary??><span><@s.text name="extension.vocabulary"/>: <a href="vocabulary.do?id=${p.vocabulary.uriString?url}">${p.vocabulary.title!p.vocabulary.uriString}</a></span></#if>
                                                <#if qualnameDiffers><span class="ext-mono">${p.qualname}</span></#if>
                                                <span class="ext-details-toggle"><@s.text name="admin.extension.details"/><@icon name="expand_more"/></span>
                                            </summary>
                                            <dl class="ext-kv">
                                                <dt><@s.text name="extension.prop.qname"/></dt>
                                                <dd class="ext-mono">${p.qualname} <button type="button" class="ext-copy" data-copy="${p.qualname}" data-copied="<@s.text name='admin.extension.copied'/>" title="<@s.text name='button.copy'/>" aria-label="<@s.text name='button.copy'/>: <@s.text name='extension.prop.qname'/>"><@icon name="content_copy"/></button></dd>
                                                <dt><@s.text name="basic.namespace"/></dt><dd class="ext-mono">${p.namespace()}</dd>
                                                <dt><@s.text name="extension.prop.type"/></dt><dd>${p.type!}</dd>
                                                <dt><@s.text name="extension.prop.required"/></dt><dd><#if p.required><@s.text name="basic.yes"/><#else><@s.text name="basic.no"/></#if></dd>
                                                <#if p.link?has_content><dt><@s.text name="basic.seealso"/></dt><dd><a href="${p.link}" target="_blank" rel="noopener">${p.link}</a></dd></#if>
                                                <#if users?has_content>
                                                    <dt><@s.text name="admin.extensions.col.usedBy"/></dt>
                                                    <dd><ul class="ext-list"><#list users as r><li><a href="${baseURL}/manage/resource.do?r=${r.shortname?url}">${action.titleOf(r)}</a></li></#list></ul></dd>
                                                </#if>
                                                <#if p.translations?has_content>
                                                    <dt><@s.text name="admin.extension.translations"/></dt>
                                                    <dd><#list p.translations as l, t><#if (t.label)?has_content><span class="ext-chipv" lang="${l}">${l} · ${t.label}</span></#if></#list></dd>
                                                </#if>
                                            </dl>
                                        </details>
                                    </div>
                                </div>
                            </#list>
                        </section>
                    </#list>
                    <div class="ex-empty" id="ext-no-match" hidden><@s.text name="admin.extensions.noMatch"/></div>
                </section>
            </div>
        </main>
    </div>
</div>

<#-- update: names the resources whose mappings move; filled in by js/extensions.js from the button -->
<div class="modal fade" id="ex-update-modal" tabindex="-1" role="alertdialog" aria-labelledby="ex-update-modal-title" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered dlg-alert">
        <form class="modal-content" action="updateExtension.do" method="post">
            <input type="hidden" name="id" value="">
            <div class="modal-header">
                <span class="dlg-tone info" aria-hidden="true"><@icon name="update"/></span>
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
<div class="modal fade" id="ex-remove-modal" tabindex="-1" role="alertdialog" aria-labelledby="ex-remove-modal-title" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered dlg-alert">
        <form class="modal-content" action="extension.do" method="post">
            <input type="hidden" name="id" value="">
            <div class="modal-header">
                <span class="dlg-tone danger" aria-hidden="true"><@icon name="delete"/></span>
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
