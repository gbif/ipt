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

<div class="container-fluid bg-body border-bottom">
    <div class="container my-3">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container my-3 p-3">
        <div class="text-center">
            <div class="text-uppercase fw-bold fs-smaller-2">
                <span><@s.text name="admin.extension.title"/></span>
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
                <div class="text-smaller text-gbif-primary">
                    <span>
                        <@s.text name='schema.version'/> <@s.text name='schema.issuedOn'/> ${extension.issued?date?string.long}
                    </span>
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

<#assign groups = propertiesByGroup?keys/>

<div id="sections" class="container-fluid bg-body">
    <div class="container my-md-4 bd-layout">
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
                <span class="anchor anchor-home-resource-page" id="anchor-description"></span>
                <div class="mt-5 section">
                    <h5 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                        <@s.text name="basic.description"/>
                    </h5>

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
                    <span class="anchor anchor-home-resource-page" id="anchor-group_${group?replace(' ', '_')}"></span>
                    <div id="group_${group?replace(' ', '_')}" class="mt-5 section">
                        <#if group?has_content>
                            <h6 class="pb-2 mb-2 pt-2 mt-3 text-gbif-header-2 fw-400 text-capitalize" style="font-size: 1.125rem;">
                                ${group}
                            </h6>
                        </#if>
                        <div class="mt-3 overflow-x-auto">
                            <div class="table-responsive text-smaller pt-2">
                            <table class="table table-sm dataTable no-footer"  role="grid">
                                <thead>
                                <tr role="row">
                                    <th><@s.text name='basic.name'/></th>
                                    <th><@s.text name='basic.description'/></th>
                                    <th><@s.text name='extension.prop.type'/></th>
                                    <th><@s.text name='extension.prop.required'/></th>
                                    <th><@s.text name='extension.vocabulary'/></th>
                                    <th><@s.text name='basic.examples'/></th>
                                </tr>
                                </thead>
                                <tbody>
                                <#list groupProperties as p>
                                    <tr>
                                        <td>
                                            <#if p.link?has_content>
                                                <a href="${p.link}" style="color:#4e565f !important;" class="fst-italic" target="_blank"><b>${p.name}</b></a>
                                            <#else>
                                                <span class="fst-italic"><b>${p.name}</b></span>
                                            </#if>
                                        </td>
                                        <td>
                                            <#if p.description?has_content>
                                                <span class="fst-italic">${p.description}</span>
                                            <#else>
                                                --
                                            </#if>
                                        </td>
                                        <td><@dataBage p.type/></td>
                                        <td><#if p.required><@s.text name="basic.yes"/><#else><@s.text name="basic.no"/></#if></td>
                                        <td>
                                            <#if p.vocabulary??>
                                                <a href="vocabulary.do?id=${p.vocabulary.uriString}" target="_blank">${p.vocabulary.title}</a>
                                            <#else>
                                                --
                                            </#if>
                                        </td>
                                        <td>
                                            <#if p.examples?has_content>
                                                <@processSurroundedWithBackticksAsCode p.examples />
                                            </#if>
                                        </td>
                                    </tr>
                                </#list>
                                </tbody>
                            </table>
                        </div>
                        </div>
                    </div>
                </#list>
            </div>
        </main>
    </div>
</div>


<#include "/WEB-INF/pages/inc/footer.ftl">
