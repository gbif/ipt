<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.DataPackageSchemaAction" -->
<#--
  The data package page (admin/dataPackage.do?id=...): the schema's description and details, the resources using it,
  then its tables and fields with search and filters. Same layout and scripts as the extension page (inc/extension_page.ftl).
-->
<#include "/WEB-INF/pages/inc/header.ftl">
<#assign schema = dataPackageSchema/>
<title>${schema.title} - <@s.text name="breadcrumb.admin.dataPackageSchema"/></title>
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/extensions.css">
<style>
    /* the fixed navbar (57px) and a small gap, as on the extension page */
    :root { --navbar-height: 72px; }
</style>
<script src="${baseURL}/js/scrollspy.js"></script>
<script src="${baseURL}/js/extension.js"></script>
<script>
    $(document).ready(function () {
        // the source file, with JSON syntax colours
        var jsonElement = document.getElementById('json-raw-data');
        if (jsonElement) {
            jsonElement.innerHTML = jsonSyntaxHighlight(jsonElement.innerText);
        }

        function jsonSyntaxHighlight(json) {
            json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
            return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
                var cls = 'json-number';
                if (/^"/.test(match)) {
                    cls = /:$/.test(match) ? 'json-key' : 'json-string';
                } else if (/true|false/.test(match)) {
                    cls = 'json-boolean';
                } else if (/null/.test(match)) {
                    cls = 'json-null';
                }
                return '<span class="' + cls + '">' + match + '</span>';
            });
        }
    });
</script>
<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<#include "/WEB-INF/pages/macros/dialog.ftl"/>
<#assign dateFormat = "d MMMM yyyy"/>
<#macro issued date=""><#if date?is_date_like>${date?date?string[dateFormat]}<#else>-</#if></#macro>
<#-- text from a schema: escaped, `backticks` shown as code and [text](http...) as links (only web links: no javascript:) -->
<#macro rich text>${(text?esc?markup_string)
    ?replace("`([^`]*)`", "<code>$1</code>", "r")
    ?replace("\\[([^\\]]+)\\]\\((https?://[^\\)\\s]+)\\)", "<a href=\"$2\" target=\"_blank\" rel=\"noopener\">$1</a>", "r")?no_esc}</#macro>
<#-- an example or a constraint value: a string, a number, a boolean or a list of them -->
<#macro value v><#if v?is_boolean>${v?c}<#elseif v?is_number>${v?c}<#else>${v}</#if></#macro>

<#assign tables = schema.tableSchemas![]/>
<#assign isDwcDp = (schema.name!"") == "dwc-dp"/>
<#-- DwC-DP tables in the groups of the resource overview (DwcDpTableGroup), other schemas in one group in file order -->
<#assign tableGroups = []/>
<#if isDwcDp>
    <#list ["events", "entities", "assertions", "joins"] as key>
        <#assign inGroup = tables?filter(t -> t.dwcDpGroup.key == key)/>
        <#if inGroup?has_content><#assign tableGroups = tableGroups + [{"key": key, "tables": inGroup}]/></#if>
    </#list>
<#else>
    <#assign tableGroups = [{"key": "", "tables": tables}]/>
</#if>
<#assign fieldCount = 0/>
<#assign requiredCount = 0/>
<#assign valuesCount = 0/>
<#assign keyCount = 0/>
<#assign mappedCount = 0/>
<#list tables as t>
    <#list t.fields![] as f>
        <#assign fieldCount++/>
        <#if f.required><#assign requiredCount++/></#if>
        <#if (f.constraints.vocabulary)?has_content><#assign valuesCount++/></#if>
        <#if fieldUsedBy[t.name + "/" + f.name]??><#assign mappedCount++/></#if>
        <#if f.name == (t.primaryKey!"") || (t.foreignKeys![])?filter(fk -> (fk.fields!"") == f.name && fk.reference??)?has_content><#assign keyCount++/></#if>
    </#list>
</#list>
<#assign resourceCount = schemaUsage?size/>
<#assign outdated = !registryUnreachable && !schema.latest/>
<#assign newerVersion><#if latestRegistered??><#if latestRegistered.version != schema.version>${latestRegistered.version}<#else><@issued date=latestRegistered.issued!""/></#if></#if></#assign>

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
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.admin.dataPackageSchema"/></li>
                        </ol>
                    </nav>
                </div>
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal">${schema.title}</h1>
                <#-- version, issued and the version state, on one line -->
                <div class="ov-meta">
                    <span><@s.text name="admin.dataPackage.version"><@s.param>${schema.version!}</@s.param></@s.text> · <@s.text name="admin.extensions.issued"><@s.param><@issued date=schema.issued!""/></@s.param></@s.text><#t>
                        <#if registryUnreachable> · <@s.text name="admin.extensions.version.notChecked"/><#t>
                        <#elseif schema.latest> · <span class="ex-ok"><@s.text name="admin.extensions.version.latest"/></span><#t>
                        <#else> · <span class="ex-up ext-version"><@icon name="warning"/><@s.text name="admin.extensions.version.newer"><@s.param>${newerVersion}</@s.param></@s.text></span><#t>
                        </#if><#t>
                    </span>
                </div>
            </div>
            <div class="ov-acts">
                <#if outdated && schema.updatable>
                    <#-- the update page shows what changes for each resource before anything is updated -->
                    <a href="${baseURL}/admin/dataPackageUpdateImpact.do?id=${schema.identifier?url}" class="btn btn-sm btn-gbif-solid top-button">
                        <@icon name="update" class="me-1"/><@s.text name="button.update"/>
                    </a>
                </#if>
                <a href="${baseURL}/admin/dataPackages.do" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="button.back"/></a>
                <#-- more actions last, as on the resource overview -->
                <div class="dropdown">
                    <button type="button" class="btn btn-sm btn-outline-secondary top-button ov-more-btn" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='admin.extensions.moreActions'/>" aria-label="<@s.text name='admin.extensions.moreActions'/>"><@icon name="more_horiz"/></button>
                    <ul class="dropdown-menu dropdown-menu-end">
                        <li><button type="button" class="dropdown-item" data-bs-toggle="modal" data-bs-target="#raw-data-modal"><@s.text name="schema.view.source"/></button></li>
                        <#if schema.url??><li><a class="dropdown-item" href="${schema.url}" target="_blank" rel="noopener"><@s.text name="admin.dataPackage.definition"/> <@icon name="open_in_new"/></a></li></#if>
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
                        <li><a class="sidebar-navigation-link" href="#anchor-ext-terms"><@s.text name="admin.dataPackage.tables"/> <small>${tables?size}</small></a></li>
                        <#list tableGroups as g>
                            <#if g.key?has_content><li class="dp-toc-group"><@s.text name="manage.overview.data.group.${g.key}"/></li></#if>
                            <#list g.tables as t>
                                <li><a class="sidebar-navigation-link ext-toc-sub" href="#anchor-ext-group-${t.name}"><span>${t.title!t.name}</span> <small data-toc-count="${t.name}">${(t.fields![])?size}</small></a></li>
                            </#list>
                        </#list>
                    </ul>
                </nav>
            </div>

            <div class="bd-content ps-lg-4">
                <#if registryUnreachable>
                    <div class="ex-banner ex-banner-danger mt-4" role="status">
                        <@icon name="error" class="ex-banner-icon"/>
                        <div class="ex-banner-body">
                            <h2><@s.text name="admin.extensions.registry.title"/></h2>
                            <div class="ex-banner-text"><@s.text name="admin.dataPackage.registry.text"><@s.param>${cfg.registryUrl}</@s.param></@s.text></div>
                        </div>
                    </div>
                <#elseif outdated>
                    <div class="ex-banner ex-banner-warning mt-4" role="status">
                        <@icon name="warning" class="ex-banner-icon"/>
                        <div class="ex-banner-body">
                            <h2><@s.text name="admin.extensions.version.newer"><@s.param>${newerVersion}</@s.param></@s.text></h2>
                            <div class="ex-banner-text">
                                <#if schema.updatable><@s.text name="admin.dataPackage.version.warning"/><#else><@s.text name="admin.dataPackage.version.iptUpdate"/></#if>
                            </div>
                        </div>
                    </div>
                </#if>

                <span class="ext-anchor" id="anchor-ext-description"></span>
                <section id="ext-description" class="ext-section">
                    <div class="ext-sec-head"><h2><@s.text name="basic.description"/></h2></div>
                    <#if schema.description?has_content><p class="ext-desc"><@rich schema.description/></p></#if>

                    <div class="ext-boxes">
                        <div class="ext-box">
                            <h3><@s.text name="admin.extension.details"/></h3>
                            <dl class="ext-kv ext-kv-2">
                                <dt><@s.text name="schema.identifier"/></dt>
                                <dd><span class="ext-mono">${schema.identifier}</span><button type="button" class="ext-copy" data-copy="${schema.identifier}" data-copied="<@s.text name='admin.extension.copied'/>" title="<@s.text name='button.copy'/>" aria-label="<@s.text name='button.copy'/>: <@s.text name='schema.identifier'/>"><@icon name="content_copy"/></button></dd>
                                <dt><@s.text name="basic.name"/></dt><dd class="ext-mono">${schema.name!}</dd>
                                <#if schema.profile?has_content>
                                    <dt><@s.text name="admin.dataPackage.profile"/></dt><dd><a class="ext-mono" href="${schema.profile}" target="_blank" rel="noopener">${schema.profile}</a></dd>
                                </#if>
                                <dt><@s.text name="admin.dataPackage.tables"/></dt><dd>${tables?size}</dd>
                                <dt><@s.text name="admin.dataPackage.fields"/></dt><dd>${fieldCount}</dd>
                                <dt><@s.text name="admin.dataPackage.requiredFields"/></dt><dd>${requiredCount}</dd>
                            </dl>
                        </div>
                        <#if tableRules?has_content>
                            <div class="ext-box">
                                <h3><@s.text name="admin.dataPackage.tableRules"/></h3>
                                <ul class="dp-rules">
                                    <#list tableRules as rule><li><@rich rule/></li></#list>
                                </ul>
                            </div>
                        </#if>
                    </div>
                </section>

                <#-- the resources using it: a table, the first 5 shown, as there can be many -->
                <span class="ext-anchor" id="anchor-ext-usedby"></span>
                <section id="ext-usedby" class="ext-section">
                    <div class="ext-sec-head">
                        <h2><@s.text name="admin.extensions.col.usedBy"/></h2>
                        <span><#if resourceCount gt 0><@s.text name="admin.extensions.usedBy.count"><@s.param>${resourceCount}</@s.param></@s.text> · </#if><@s.text name="admin.dataPackage.usedBy.desc"/></span>
                    </div>
                    <#if schemaUsage?has_content>
                        <table class="ov-table ext-usage">
                            <thead>
                            <tr>
                                <th><@s.text name="admin.extension.col.resource"/></th>
                                <th><@s.text name="admin.dataPackage.col.tablesMapped"/></th>
                                <th class="text-end"><@s.text name="admin.dataPackage.col.fieldsMapped"/></th>
                            </tr>
                            </thead>
                            <tbody>
                            <#list schemaUsage as u>
                                <tr<#if u?index gte 5> class="ext-more-row" hidden</#if>>
                                    <td><a class="resource-table-link" href="${baseURL}/manage/resource.do?r=${u.resource.shortname?url}">${action.titleOf(u.resource)}</a><div class="ext-shortname">${u.resource.shortname}</div></td>
                                    <td class="dp-usage-tables"><#if u.tables?has_content><#list u.tables as t><a href="#anchor-ext-group-${t.name}">${t.title!t.name}</a><#sep>, </#sep></#list><#else><span class="ex-muted"><@s.text name="admin.extensions.usedBy.none"/></span></#if></td>
                                    <td class="ext-n">${u.fieldsMapped}</td>
                                </tr>
                            </#list>
                            </tbody>
                        </table>
                        <#if resourceCount gt 5>
                            <button type="button" class="ex-link-btn ext-usage-all" id="ext-usage-all"><@s.text name="admin.extension.usedBy.showAll"/>: ${resourceCount}<@icon name="expand_more" class="ex-chev"/></button>
                        </#if>
                    <#else>
                        <p class="ex-muted text-smaller mb-0"><@s.text name="admin.extensions.usedBy.none"/></p>
                    </#if>
                </section>

                <span class="ext-anchor" id="anchor-ext-terms"></span>
                <section id="ext-terms" class="ext-section">
                    <div class="ext-sec-head">
                        <h2><@s.text name="admin.dataPackage.tables"/></h2>
                        <span><#if isDwcDp><@s.text name="admin.dataPackage.tables.desc.dwcDp"/><#else><@s.text name="admin.dataPackage.tables.desc"/></#if></span>
                    </div>
                    <div class="ext-toolbar">
                        <div class="ex-chips" role="group" id="ext-chips">
                            <button type="button" class="ex-chip on" data-filter="all"><@s.text name="admin.extensions.filter.all"/>: <span data-chip-count>${fieldCount}</span></button>
                            <#if requiredCount gt 0><button type="button" class="ex-chip" data-filter="req"><@s.text name="extension.prop.required"/>: <span data-chip-count>${requiredCount}</span></button></#if>
                            <#if keyCount gt 0><button type="button" class="ex-chip" data-filter="key"><@s.text name="admin.dataPackage.filter.keys"/>: <span data-chip-count>${keyCount}</span></button></#if>
                            <#if valuesCount gt 0><button type="button" class="ex-chip" data-filter="voc"><@s.text name="admin.dataPackage.filter.values"/>: <span data-chip-count>${valuesCount}</span></button></#if>
                            <#if resourceCount gt 0>
                                <button type="button" class="ex-chip" data-filter="used"><@s.text name="admin.extension.filter.used"/>: <span data-chip-count>${mappedCount}</span></button>
                                <button type="button" class="ex-chip" data-filter="unused"><@s.text name="admin.extensions.usedBy.none"/>: <span data-chip-count>${fieldCount - mappedCount}</span></button>
                            </#if>
                        </div>
                        <input type="search" id="ext-q" class="form-control form-control-sm" placeholder="<@s.text name='admin.dataPackage.search'/>" aria-label="<@s.text name='admin.dataPackage.search'/>" autocomplete="off">
                        <span class="flex-grow-1"></span>
                        <button type="button" class="ex-link-btn" id="ext-all-details" data-show="<@s.text name='admin.extension.details.showAll'/>" data-hide="<@s.text name='admin.extension.details.hideAll'/>"><@s.text name="admin.extension.details.showAll"/></button>
                    </div>

                    <#list tableGroups as g>
                        <div class="dp-tgroup" data-hide-empty>
                        <#if g.key?has_content><div class="dp-tgroup-title"><@s.text name="manage.overview.data.group.${g.key}"/></div></#if>
                        <#list g.tables as t>
                            <#assign users = tableUsedBy[t.name]![]/>
                            <span class="ext-anchor" id="anchor-ext-group-${t.name}"></span>
                            <section class="ext-group dp-table" id="ext-group-${t.name}" data-group="${t.name}">
                                <h3 class="ext-group-title">${t.title!t.name} <small data-group-count>${(t.fields![])?size}</small></h3>
                                <div class="dp-table-head">
                                    <div class="dp-table-meta">
                                        <span class="ext-mono">${t.name}</span>
                                        <#if t.primaryKey?has_content><span><@s.text name="admin.dataPackage.tag.primaryKey"/>: <span class="ext-mono">${t.primaryKey}</span></span></#if>
                                        <#if resourceCount gt 0><span><#if users?has_content><@s.text name="admin.extension.term.mappedIn"><@s.param>${users?size}</@s.param><@s.param>${resourceCount}</@s.param></@s.text><#else><@s.text name="admin.extensions.usedBy.none"/></#if></span></#if>
                                    </div>
                                    <#if t.description?has_content><p><@rich t.description/></p></#if>
                                    <#if t.comments?has_content><p class="ext-comments"><@rich t.comments/></p></#if>
                                    <#if t.examples?has_content><p class="ext-examples"><em><@s.text name="basic.examples"/>:</em> <@rich t.examples/></p></#if>
                                </div>
                                <#list t.fields![] as f>
                                    <#assign c = f.constraints!{}/>
                                    <#assign fieldId = t.name + "/" + f.name/>
                                    <#assign fieldUsers = fieldUsedBy[fieldId]![]/>
                                    <#assign isPk = f.name == (t.primaryKey!"")/>
                                    <#assign fks = (t.foreignKeys![])?filter(fk -> (fk.fields!"") == f.name && fk.reference??)/>
                                    <#assign values = (c.vocabulary)![]/>
                                    <#assign filters = [f.required?then("req", ""), values?has_content?then("voc", ""), (isPk || fks?has_content)?then("key", ""), fieldUsers?has_content?then("used", "unused")]?filter(x -> x?has_content)?join(" ")/>
                                    <div class="ext-term" id="${fieldId}" data-filters="${filters}"
                                         data-search="${[f.name, f.title!"", f.description!"", t.name, t.title!""]?join(" ")?lower_case}">
                                        <div class="ext-term-name">
                                            <b><#if f.title?has_content>${f.title}<#else>${f.name}</#if></b>
                                            <#-- the name too when the label differs from it (ColDP labels repeat the name) -->
                                            <#if f.title?has_content && f.title != f.name><span class="ext-mono">${f.name}</span></#if>
                                            <div class="ext-tags">
                                                <#if f.required><span class="ext-tag ext-tag-req"><@s.text name="extension.prop.required"/></span></#if>
                                                <#if isPk><span class="ext-tag"><@s.text name="admin.dataPackage.tag.primaryKey"/></span>
                                                <#elseif (c.unique)!false><span class="ext-tag"><@s.text name="admin.dataPackage.tag.unique"/></span></#if>
                                                <#list fks as fk>
                                                    <#assign refTable = fk.reference.resource?has_content?then(fk.reference.resource, t.name)/>
                                                    <a class="ext-tag ext-tag-voc" href="#${refTable}/${fk.reference.fields!}" title="<@s.text name='admin.dataPackage.field.references'/>: ${refTable} / ${fk.reference.fields!}"><@s.text name="admin.dataPackage.tag.foreignKey"/> → ${refTable}</a>
                                                </#list>
                                                <#if f.type?has_content && f.type != "string"><span class="ext-tag">${f.type}</span></#if>
                                                <#if values?has_content><span class="ext-tag"><@s.text name="admin.dataPackage.filter.values"/>: ${values?size}</span></#if>
                                            </div>
                                        </div>
                                        <div class="ext-term-body">
                                            <#if f.description?has_content><p><@rich f.description/></p></#if>
                                            <#if f.comments?has_content><p class="ext-comments"><@rich f.comments/></p></#if>
                                            <#if f.example?has_content>
                                                <p class="ext-examples"><em><@s.text name="basic.examples"/>:</em>
                                                    <#if f.example?is_sequence><#list f.example as ex><code><@value ex/></code><#sep>, </#sep></#list>
                                                    <#elseif f.example?is_string && f.example?contains("`")><@rich f.example/>
                                                    <#else><code><@value f.example/></code></#if>
                                                </p>
                                            </#if>
                                            <details class="ext-details">
                                                <summary>
                                                    <span class="ext-used"><#if resourceCount gt 0><#if fieldUsers?has_content><@s.text name="admin.extension.term.mappedIn"><@s.param>${fieldUsers?size}</@s.param><@s.param>${resourceCount}</@s.param></@s.text><#else><@s.text name="admin.extensions.usedBy.none"/></#if></#if></span>
                                                    <span class="ext-details-toggle"><@s.text name="admin.extension.details"/><@icon name="expand_more"/></span>
                                                </summary>
                                                <dl class="ext-kv">
                                                    <dt><@s.text name="basic.name"/></dt>
                                                    <dd class="ext-mono">${f.name} <button type="button" class="ext-copy" data-copy="${f.name}" data-copied="<@s.text name='admin.extension.copied'/>" title="<@s.text name='button.copy'/>" aria-label="<@s.text name='button.copy'/>: ${f.name}"><@icon name="content_copy"/></button></dd>
                                                    <dt><@s.text name="schema.field.type"/></dt><dd>${f.type!"string"}</dd>
                                                    <#if f.format?has_content && f.format != "default"><dt><@s.text name="schema.field.format"/></dt><dd><code>${f.format}</code></dd></#if>
                                                    <dt><@s.text name="extension.prop.required"/></dt><dd><#if f.required><@s.text name="basic.yes"/><#else><@s.text name="basic.no"/></#if></dd>
                                                    <#if (c.unique)??><dt><@s.text name="admin.dataPackage.tag.unique"/></dt><dd><#if c.unique><@s.text name="basic.yes"/><#else><@s.text name="basic.no"/></#if></dd></#if>
                                                    <#if (c.minimum)??><dt><@s.text name="admin.dataPackage.field.minimum"/></dt><dd><code>${c.minimum?c}</code></dd></#if>
                                                    <#if (c.maximum)??><dt><@s.text name="admin.dataPackage.field.maximum"/></dt><dd><code>${c.maximum?c}</code></dd></#if>
                                                    <#if (c.pattern)?has_content><dt><@s.text name="admin.dataPackage.field.pattern"/></dt><dd><code>${c.pattern}</code></dd></#if>
                                                    <#if values?has_content><dt><@s.text name="admin.dataPackage.filter.values"/></dt><dd><#list values as v><span class="ext-chipv">${v}</span></#list></dd></#if>
                                                    <#list fks as fk>
                                                        <#assign refTable = fk.reference.resource?has_content?then(fk.reference.resource, t.name)/>
                                                        <dt><@s.text name="admin.dataPackage.field.references"/></dt><dd><a class="ext-mono" href="#${refTable}/${fk.reference.fields!}">${refTable} / ${fk.reference.fields!}</a></dd>
                                                    </#list>
                                                    <#if fieldUsers?has_content>
                                                        <dt><@s.text name="admin.extensions.col.usedBy"/></dt>
                                                        <dd><ul class="ext-list"><#list fieldUsers as r><li><a href="${baseURL}/manage/resource.do?r=${r.shortname?url}">${action.titleOf(r)}</a></li></#list></ul></dd>
                                                    </#if>
                                                </dl>
                                            </details>
                                        </div>
                                    </div>
                                </#list>
                            </section>
                        </#list>
                        </div>
                    </#list>
                    <div class="ex-empty" id="ext-no-match" hidden><@s.text name="admin.extensions.noMatch"/></div>
                </section>
            </div>
        </main>
    </div>
</div>

<#assign dlgTitle>${schema.title}</#assign>
<@dialog id="raw-data-modal" title=dlgTitle size="wide">
    <div class="modal-body">
        <pre id="json-raw-data" class="fs-smaller-2 m-0 p-3">${dataPackageSchemaRawData!}</pre>
    </div>
    <div class="modal-footer">
        <@dialogCancel key="button.close"/>
    </div>
</@dialog>

<#include "/WEB-INF/pages/inc/footer.ftl">
