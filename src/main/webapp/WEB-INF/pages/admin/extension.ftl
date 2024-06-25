<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.ExtensionsAction" -->
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="admin.extension.title"/></title>
<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/data_bage.ftl">

<script>
    $(document).ready(function(){
        // spy scroll and manage sidebar menu
        $(window).scroll(function () {
            var scrollPosition = $(document).scrollTop();

            $('.bd-toc nav a.sidebar-navigation-link').each(function () {
                var currentLink = $(this);
                var anchor = $(currentLink.attr("href"));
                var sectionId = anchor[0].id.replace("anchor-", "");
                var section = $("#" + sectionId);

                var sectionsContainer = $("#sections");

                if (sectionsContainer.position().top - 100 > scrollPosition) {
                    var removeActiveFromThisLink = $('.bd-toc nav a.active');
                    removeActiveFromThisLink.removeClass('active');
                } else if (section.position().top - 100  <= scrollPosition
                    && section.position().top + section.height() > scrollPosition) {
                    if (!currentLink.hasClass("active")) {
                        var removeFromThisLink = $('.bd-toc nav a.active');
                        removeFromThisLink.removeClass('active');
                        $(this).addClass('active');
                    }
                }
            });
        })
    });
</script>

<#macro processSurroundedWithBackticksAsCode examples>
    ${examples?replace("`(.*?)`", "<code>$1</code>", "r")}
</#macro>

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<div class="container-fluid bg-body border-bottom">
    <div class="container border rounded-2 mb-4">
        <div class="container my-3 p-3">
            <div class="text-center">
                <div class="fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb justify-content-center mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                            <li class="breadcrumb-item"><a href="${baseURL}/admin/extensions.do"><@s.text name="breadcrumb.admin.extensions"/></a></li>
                            <li class="breadcrumb-item"><@s.text name="admin.extension.title"/></li>
                        </ol>
                    </nav>
                </div>

                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    ${extension.title}
                </h1>

                <#if extension.link?has_content>
                    <div class="text-smaller mb-2">
                        <a href="${extension.link}">${extension.link}</a>
                    </div>
                </#if>

                <#if extension.issued??>
                    <div class="text-smaller">
                        <#if extension.isLatest()>
                            <span class="text-gbif-primary">
                                <@s.text name="extension.version.issued.upToDate"><@s.param>${extension.issued?date?string["d MMMM yyyy"]}</@s.param></@s.text>
                            </span>
                        <#else>
                            <span class="text-gbif-danger">
                                <@s.text name="extension.version.issued.outdated"><@s.param>${extension.issued?date?string["d MMMM yyyy"]}</@s.param></@s.text>
                            </span>
                        </#if>
                    </div>
                </#if>

                <div class="mt-2">
                    <a href="extensions.do" class="btn btn-sm btn-outline-secondary top-button">
                        <@s.text name="button.back"/>
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<#assign groups = propertiesByGroup?keys/>

<div id="sections" class="container-fluid bg-body">
    <div class="container my-md-4 bd-layout main-content-container">
        <main class="bd-main">
            <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                <nav id="sidebar-content">
                    <ul>
                        <li>
                            <a class="sidebar-navigation-link" href="#anchor-description">
                                <@s.text name="basic.description"/>
                            </a>
                        </li>
                        <#if (groups?size>0)>
                            <#list groups as g>
                                <li>
                                    <a class="sidebar-navigation-link" href="#anchor-group_${g?replace(' ', '_')}">
                                        <#if g?has_content>
                                            <span class="text-capitalize">${g}</span>
                                        <#else>
                                            <@s.text name="manage.mapping.noClass"/>
                                        </#if>
                                    </a>
                                </li>
                            </#list>
                        </#if>
                    </ul>
                </nav>
            </div>

            <div class="bd-content ps-lg-4">
                <span class="anchor anchor-extension-page" id="anchor-description"></span>
                <div class="mt-5 section">
                    <h5 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                        <@s.text name="basic.description"/>
                    </h5>

                    <#if !extension.isLatest()>
                        <div class="callout callout-danger mb-2">
                            <@s.text name="admin.extension.version.warning"/>
                        </div>
                    </#if>

                    <div class="mb-3" id="description">
                        ${extension.description}
                    </div>

                    <div class="row text-smaller">
                        <div class="col-lg-3">
                            <strong><@s.text name="extension.properties"/></strong>
                        </div>
                        <div class="col-lg-9">${extension.properties?size}</div>
                    </div>

                    <div class="row text-smaller">
                        <div class="col-lg-3">
                            <strong><@s.text name="basic.name"/></strong>
                        </div>
                        <div class="col-lg-9">${extension.name}</div>
                    </div>

                    <div class="row text-smaller">
                        <div class="col-lg-3">
                            <strong><@s.text name="basic.namespace"/></strong>
                        </div>
                        <div class="col-lg-9 overflow-x-auto">${extension.namespace}</div>
                    </div>

                    <div class="row text-smaller">
                        <div class="col-lg-3">
                            <strong><@s.text name="extension.rowtype"/></strong>
                        </div>
                        <div class="col-lg-9 overflow-x-auto">${extension.rowType}</div>
                    </div>

                    <div class="row text-smaller">
                        <div class="col-lg-3">
                            <strong><@s.text name="basic.lastModified"/></strong>
                        </div>
                        <div class="col-lg-9">${extension.modified?datetime?string.long_short}</div>
                    </div>
                </div>

                <div class="mt-5">
                    <h5 class="pb-2 pt-2 text-gbif-header-2 fw-400">
                        <@s.text name="admin.extension.properties"/>
                    </h5>
                </div>

                <#list propertiesByGroup as group, groupProperties>
                    <span class="anchor anchor-extension-page" id="anchor-group_${group?replace(' ', '_')}"></span>
                    <div id="group_${group?replace(' ', '_')}" class="mt-5 section">
                        <#if group?has_content>
                            <h6 class="pb-2 mb-2 pt-2 mt-3 text-gbif-header-2 fw-400 text-capitalize" style="font-size: 1.125rem;">
                                ${group}
                            </h6>
                        </#if>
                        <div class="mt-3">
                            <#list groupProperties as p>
                                <div class="row py-2 g-2 <#sep>border-bottom</#sep>">
                                    <div class="col-lg-3 mt-1">
                                        <div class="title">
                                            <div class="head overflow-x-auto text-break text-smaller">
                                                <#if p.link?has_content>
                                                    <a href="${p.link}" style="color:#4e565f !important;" class="fst-italic" target="_blank"><b>${p.name}</b></a>
                                                <#else>
                                                    <span class="fst-italic"><b>${p.name}</b></span>
                                                </#if>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="col-lg-9 mt-1">
                                        <div class="definition text-smaller">
                                            <div class="body">
                                                <#if p.description?has_content>
                                                    <p class="overflow-x-auto fst-italic">
                                                        ${p.description}
                                                    </p>
                                                </#if>
                                                <#if p.link?has_content>
                                                    <p class="overflow-x-auto">
                                                        <@s.text name="basic.seealso"/> <a href="${p.link}">${p.link}</a>
                                                    </p>
                                                </#if>
                                                <#if p.examples?has_content>
                                                    <p class="overflow-x-auto">
                                                        <em><@s.text name="basic.examples"/></em>: <@processSurroundedWithBackticksAsCode p.examples />
                                                    </p>
                                                </#if>
                                                <#if p.vocabulary??>
                                                    <p class="overflow-x-auto">
                                                        <em><@s.text name="extension.vocabulary"/></em>:
                                                        <a href="vocabulary.do?id=${p.vocabulary.uriString}">${p.vocabulary.title}</a>
                                                    </p>
                                                </#if>
                                                <div class="details table-responsive">
                                                    <table>
                                                        <tr><th class="pe-md-4 pe-2"><@s.text name="extension.prop.qname"/></th><td>${p.qualname}</td></tr>
                                                        <tr><th class="pe-md-4 pe-2"><@s.text name="basic.namespace"/></th><td>${p.namespace()}</td></tr>
                                                        <tr><th class="pe-md-4 pe-2"><@s.text name="extension.prop.type"/></th><td>${p.type!}</td></tr>
                                                        <tr><th class="pe-md-4 pe-2"><@s.text name="extension.prop.required"/></th><td><#if p.required><@s.text name="basic.yes"/><#else><@s.text name="basic.no"/></#if></td></tr>
                                                    </table>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </#list>
                        </div>
                    </div>
                </#list>
            </div>
        </main>
    </div>
</div>


<#include "/WEB-INF/pages/inc/footer.ftl">
