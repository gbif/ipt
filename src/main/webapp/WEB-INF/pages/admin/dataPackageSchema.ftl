<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.DataPackageSchemaAction" -->
<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name="admin.home.manageDataPackageSchemas"/></title>
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
            });

            $("#button-display-raw-data").on('click', function () {
                var dialogWindow = $("#raw-data-modal");
                dialogWindow.modal('show');
            });

            var jsonElement = document.getElementById('json-raw-data');
            var jsonString = jsonElement.innerText;
            jsonElement.innerHTML = jsonSyntaxHighlight(jsonString);

            function jsonSyntaxHighlight(json) {
                json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
                    var cls = 'json-number';
                    if (/^"/.test(match)) {
                        if (/:$/.test(match)) {
                            cls = 'json-key';
                        } else {
                            cls = 'json-string';
                        }
                    } else if (/true|false/.test(match)) {
                        cls = 'json-boolean';
                    } else if (/null/.test(match)) {
                        cls = 'json-null';
                    }
                    return '<span class="' + cls + '">' + match + '</span>';
                });
            }
        })
    </script>
    <style>
        body {
            font-family: 'Courier New', monospace;
        }
        .json-key {
            color: brown;
        }
        .json-string {
            color: green;
        }
        .json-number {
            color: blue;
        }
        .json-boolean {
            color: purple;
        }
        .json-null {
            color: gray;
        }
    </style>

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

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container-fluid bg-body border-bottom">
        <div class="container bg-body border rounded-2 mb-4">
            <div class="container my-3 p-3">
                <div class="text-center fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb justify-content-center mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                            <li class="breadcrumb-item"><a href="${baseURL}/admin/dataPackages.do"><@s.text name="breadcrumb.admin.dataPackageSchemas"/></a></li>
                            <li class="breadcrumb-item"><@s.text name="breadcrumb.admin.dataPackageSchema"/></li>
                        </ol>
                    </nav>
                </div>

                <div class="text-center">
                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                        ${dataPackageSchema.title}
                    </h1>

                    <#if dataPackageSchema.url??>
                        <div class="text-center fs-smaller mb-2">
                            <a href="${dataPackageSchema.url}">${dataPackageSchema.url}</a>
                        </div>
                    </#if>

                    <div class="text-smaller">
                        <#if dataPackageSchema.isLatest()>
                            <span class="text-gbif-primary">
                                <@s.text name="schema.version.issued.upToDate"><@s.param>${dataPackageSchema.version}</@s.param><@s.param>${dataPackageSchema.issued?date?string["d MMMM yyyy"]}</@s.param></@s.text>
                            </span>
                        <#else>
                            <span class="text-gbif-danger">
                                <@s.text name="schema.version.issued.outdated"><@s.param>${dataPackageSchema.version}</@s.param><@s.param>${dataPackageSchema.issued?date?string["d MMMM yyyy"]}</@s.param></@s.text>
                            </span>
                        </#if>
                    </div>

                    <div class="mt-2">
                        <div class="btn-group btn-group-sm" role="group">
                            <button id="btnGroup" type="button" class="btn btn-sm btn-outline-gbif-primary dropdown-toggle align-self-start top-button" data-bs-toggle="dropdown" aria-expanded="false">
                                <@s.text name="button.options"/>
                            </button>
                            <ul class="dropdown-menu" aria-labelledby="btnGroup" style="">
                                <li>
                                    <a id="button-display-raw-data" href="#" class="btn btn-sm btn-outline-gbif-primary w-100 dropdown-button">
                                        <@s.text name="schema.view.source"/>
                                    </a>
                                </li>
                            </ul>
                        </div>

                        <a href="dataPackages.do" class="btn btn-sm btn-outline-secondary mt-1 me-xl-1 top-button">
                            <@s.text name="button.back"/>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="sections" class="container-fluid bg-body">
        <div class="container my-md-4 bd-layout main-content-container">

            <main class="bd-main">
                <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                    <nav id="sidebar-content">
                        <ul>
                            <#list dataPackageSchema.tableSchemas as tableSchema>
                                <li>
                                    <a href="#anchor-${tableSchema.name}" class="sidebar-navigation-link">${tableSchema.title}</a>
                                </li>
                            </#list>
                        </ul>
                    </nav>
                </div>

                <div class="bd-content ps-lg-4">
                    <#list dataPackageSchema.tableSchemas as tableSchema>
                        <span class="anchor anchor-schema-page" id="anchor-${tableSchema.name}"></span>
                        <div id="${tableSchema.name}" class="mt-5 section">
                            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fs-5 fw-400">
                                ${tableSchema.title}
                            </h4>
                            <div class="mt-3">
                                <#if tableSchema.description?has_content>
                                    <p class="mb-4">
                                        <@processDescription tableSchema.description />
                                    </p>
                                </#if>

                                <#list tableSchema.fields as field>
                                    <div class="row py-2 g-2 <#sep>border-bottom</#sep>">
                                        <div class="col-lg-3 mt-1">
                                            <div class="title">
                                                <div class="head text-smaller text-truncate">
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
                                                        <em><@s.text name="schema.field.type"/></em> ${field.type!}
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
                                                                    <#if field.example?is_boolean>
                                                                        <code>${ex?string("true", "false")}</code><#sep>, </#sep>
                                                                    <#else>
                                                                        <code>${ex}</code><#sep>, </#sep>
                                                                    </#if>
                                                                </#list>
                                                            <#else>
                                                                <#if field.example?is_boolean>
                                                                    <code>${field.example?string("true", "false")}</code>
                                                                <#else>
                                                                    <code>${field.example}</code>
                                                                </#if>
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

    <div id="raw-data-modal" class="modal fade" tabindex="-1" aria-labelledby="raw-data-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-confirm" style="max-width: none !important; margin: 1.75rem; font-size: 12px;">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <h5 class="modal-title w-100" id="raw-data-modal-title">${dataPackageSchema.title}</h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">×</button>
                </div>
                <div class="modal-body" style="text-align: left !important;">
                    <pre id="json-raw-data" class="fs-smaller-2">${dataPackageSchemaRawData!}</pre>
                </div>
            </div>
        </div>
    </div>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
