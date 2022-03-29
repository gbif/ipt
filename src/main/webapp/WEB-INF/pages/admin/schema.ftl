<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.DataSchemaAction" -->
<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name="admin.home.manageSchemas"/></title>
    <script>
        $(document).ready(function() {
            // spy scroll and manage sidebar menu
            $(window).scroll(function () {
                var scrollPosition = $(document).scrollTop();

                $('.bd-toc nav a').each(function () {
                    var currentLink = $(this);
                    var anchor = $(currentLink.attr("href"));
                    var sectionId = anchor[0].id.replace("anchor-", "");
                    var section = $("#" + sectionId);
                    var sectionsContainer = $("#sections");

                    if (sectionsContainer.position().top - 50 > scrollPosition) {
                        var removeActiveFromThisLink = $('.bd-toc nav a.active');
                        removeActiveFromThisLink.removeClass('active');
                    } else if (section.position().top - 50 <= scrollPosition
                        && section.position().top + section.height() > scrollPosition) {
                        if (!currentLink.hasClass("active")) {
                            var removeFromThisLink = $('.bd-toc nav a.active');
                            removeFromThisLink.removeClass('active');
                            $(this).addClass('active');
                        }
                    }
                });
            })
        })
    </script>

    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">

    <div class="container-fluid bg-body border-bottom">
        <div class="container">
            <#include "/WEB-INF/pages/inc/action_alerts.ftl">
        </div>

        <div class="container my-3 p-3">
            <div class="text-center text-uppercase fw-bold fs-smaller-2">
                <span><@s.text name="admin.schema.title"/></span>
            </div>

            <div class="text-center">
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    ${dataSchema.title}
                </h1>

                <div class="text-smaller text-gbif-primary">
                    <span>
                        <@s.text name='schema.version'/> ${dataSchema.version}
                        <@s.text name='schema.issuedOn'/> ${dataSchema.issued?date?string.long}
                    </span>
                </div>

                <div class="mt-2">
                    <a href="schemas.do" class="btn btn-sm btn-outline-secondary mt-1 me-xl-1 top-button">
                        <@s.text name="button.back"/>
                    </a>
                </div>
            </div>
        </div>
    </div>

    <div id="sections" class="container-fluid bg-body">
        <div class="container my-md-4 bd-layout">

            <main class="bd-main">
                <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                    <nav id="sidebar-content">
                        <ul>
                            <#list dataSchema.subSchemas as subSchema>
                                <li>
                                    <a href="#anchor-${subSchema.name}" class="sidebar-navigation-link">${subSchema.title}</a>
                                </li>
                            </#list>
                        </ul>
                    </nav>
                </div>

                <div class="bd-content ps-lg-4">
                    <#list dataSchema.subSchemas as subSchema>
                        <span class="anchor anchor-home-resource-page" id="anchor-${subSchema.name}"></span>
                        <div id="${subSchema.name}" class="mt-5 section">
                            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fs-5 fw-400">
                                ${subSchema.title}
                            </h4>
                            <div class="mt-3 overflow-x-auto">
                                <div id="tableContainer" class="table-responsive text-smaller pt-2">
                                    <table class="table table-sm dataTable no-footer"  role="grid">
                                        <thead>
                                        <tr role="row">
                                            <th><@s.text name='basic.name'/></th>
                                            <th><@s.text name='basic.description'/></th>
                                            <th><@s.text name='schema.field.type'/></th>
                                            <th><@s.text name='schema.field.format'/></th>
                                            <th><@s.text name='schema.field.constraints'/></th>
                                            <th><@s.text name='schema.field.foreignKeys'/></th>
                                            <th><@s.text name='schema.field.examples'/></th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <#list subSchema.fields as field>
                                            <tr>
                                                <td>
                                                    <span class="fst-italic"><b>${field.name}</b></span>
                                                </td>
                                                <td>
                                                    <#if field.description?has_content>
                                                        <span class="fst-italic">${field.description}</span>
                                                    <#else>
                                                        --
                                                    </#if>
                                                </td>
                                                <td>
                                                    <#if field.type == "string">
                                                        <span class="badge rounded-pill bg-blue">${field.type}</span>
                                                    <#elseif field.type == "number">
                                                        <span class="badge rounded-pill bg-teal">${field.type}</span>
                                                    <#elseif field.type == "integer">
                                                        <span class="badge rounded-pill bg-cyan">${field.type}</span>
                                                    <#elseif field.type == "object">
                                                        <span class="badge rounded-pill bg-purple">${field.type}</span>
                                                    <#elseif field.type == "boolean">
                                                        <span class="badge rounded-pill bg-indigo">${field.type}</span>
                                                    <#elseif field.type == "datetime">
                                                        <span class="badge rounded-pill bg-orange">${field.type}</span>
                                                    <#elseif field.type == "date">
                                                        <span class="badge rounded-pill bg-amber">${field.type}</span>
                                                    <#elseif field.type == "year">
                                                        <span class="badge rounded-pill bg-yellow">${field.type}</span>
                                                    <#else>
                                                        ${field.type}
                                                    </#if>
                                                </td>
                                                <td>
                                                    <#if field.format??>
                                                        ${field.format}
                                                    <#else>
                                                        --
                                                    </#if>
                                                </td>
                                                <td>
                                                    <#if subSchema.primaryKey?? && subSchema.primaryKey == field.name>
                                                        primary key <code>true</code><br>
                                                    </#if>
                                                    <#if field.constraints??>
                                                        <#if field.constraints.required??>
                                                            required <code>${field.constraints.required?string}</code><br>
                                                        </#if>
                                                        <#if field.constraints.unique??>
                                                            unique <code>${field.constraints.unique?string}</code><br>
                                                        </#if>
                                                        <#if field.constraints.maximum??>
                                                            maximum <code>${field.constraints.maximum}</code><br>
                                                        </#if>
                                                        <#if field.constraints.minimum??>
                                                            minimum <code>${field.constraints.minimum}</code><br>
                                                        </#if>
                                                        <#if field.constraints.pattern??>
                                                            pattern <code>${field.constraints.pattern}</code><br>
                                                        </#if>
                                                        <#if field.constraints.vocabulary??>
                                                            enum <code>${field.constraints.vocabulary}</code><br>
                                                        </#if>
                                                    <#else>
                                                        --
                                                    </#if>
                                                </td>
                                                <td>
                                                    <#if subSchema.foreignKeys?has_content>
                                                        <#list subSchema.foreignKeys as foreignKey>
                                                            <#if foreignKey.fields == field.name>
                                                                <a href="#anchor-${foreignKey.reference.resource}">${foreignKey.reference.resource}#${foreignKey.reference.fields}</a>
                                                            </#if>
                                                        </#list>
                                                    <#else>
                                                        --
                                                    </#if>
                                                </td>
                                                <td>
                                                    <#if field.example??>
                                                        <#if field.example?is_collection>
                                                            <#list field.example as ex>
                                                                ${ex}<#sep>, </#sep>
                                                            </#list>
                                                        <#else>
                                                            ${field.example}
                                                        </#if>
                                                    <#else>
                                                        --
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
</#escape>
