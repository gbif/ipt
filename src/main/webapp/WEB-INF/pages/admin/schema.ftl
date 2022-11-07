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
    <#include "/WEB-INF/pages/macros/data_bage.ftl">

<#--    1. Interpret backticked text as code-->
<#--    2. Interpret text like []() as a link-->
    <#macro processDescription description>
        <#noescape>
            ${description?replace("`(.*?)`", "<code>$1</code>", "r")?replace("\\[(.*)\\]\\((.*)\\)", "<a href='$2'>$1</a>", "r")}
        </#noescape>
    </#macro>

    <div class="container-fluid bg-body border-bottom">
        <div class="container my-3">
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

                <#if dataSchema.url??>
                    <div class="text-center fs-smaller">
                        <a href="${dataSchema.url}">${dataSchema.url}</a>
                    </div>
                </#if>

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
                                <#if subSchema.description?has_content>
                                    <p class="mb-4">
                                        <@processDescription subSchema.description />
                                    </p>
                                </#if>

                                <#list subSchema.fields as field>
                                    <div class="row py-2 g-2 <#sep>border-bottom</#sep>">
                                        <div class="col-lg-3 mt-1">
                                            <div class="title">
                                                <div class="head overflow-x-auto text-smaller">
                                                    <span class="fst-italic">
                                                        <b>
                                                            ${field.name}
                                                            <#if (field.constraints.required)?? && field.constraints.required?string == "true">
                                                                <span class="text-gbif-danger">&#42;</span>
                                                            </#if>
                                                        </b>
                                                    </span>
                                                </div>
                                            </div>
                                        </div>

                                        <div class="col-lg-9 mt-1">
                                            <div class="definition text-smaller">
                                                <div class="body">
                                                    <#if field.description?has_content>
                                                        <p class="overflow-x-auto fst-italic">
                                                            <span class="fst-italic">
                                                                <@processDescription field.description/>
                                                            </span>
                                                        </p>
                                                    </#if>
                                                    <#if field.constraints?? && (field.constraints.unique?? || field.constraints.maximum?? || field.constraints.minimum?? || field.constraints.pattern??)>
                                                        <em><@s.text name="schema.field.constraints"/>:</em>
                                                        <ul>
                                                            <#if field.constraints.unique??>
                                                                <li>unique <code>${field.constraints.unique?string}</code></li>
                                                            </#if>
                                                            <#if field.constraints.maximum??>
                                                                <li>maximum <code>${field.constraints.maximum}</code></li>
                                                            </#if>
                                                            <#if field.constraints.minimum??>
                                                                <li>minimum <code>${field.constraints.minimum}</code></li>
                                                            </#if>
                                                            <#if field.constraints.pattern??>
                                                                <li>pattern <code>${field.constraints.pattern}</code></li>
                                                            </#if>
                                                        </ul>
                                                    </#if>
                                                    <p class="overflow-x-auto">
                                                        <em><@s.text name="schema.field.type"/></em> <@dataBage field.type/>
                                                    </p>
                                                    <#if field.format?? && field.format != 'default'>
                                                        <p class="overflow-x-auto">
                                                            <em><@s.text name="schema.field.format"/></em> <code>${field.format}</code>
                                                        </p>
                                                    </#if>
                                                    <#if field.example?has_content>
                                                        <p class="overflow-x-auto">
                                                            <em><@s.text name="basic.examples"/></em>:
                                                            <#if field.example?is_collection>
                                                                <#list field.example as ex>
                                                                    <code>${ex}</code><#sep>, </#sep>
                                                                </#list>
                                                            <#else>
                                                                <code>${field.example}</code>
                                                            </#if>
                                                        </p>
                                                    </#if>
                                                    <#if field.constraints?? && field.constraints.vocabulary??>
                                                        <p class="overflow-x-auto">
                                                            <em><@s.text name="extension.vocabulary"/></em>:
                                                            <#list field.constraints.vocabulary as item>
                                                                ${item}<#sep>,</#sep>
                                                            </#list>
                                                        </p>
                                                    </#if>
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
</#escape>
