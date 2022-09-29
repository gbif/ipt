<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name="admin.home.manageSchemas"/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>

    <#macro dataSchemaRow ds currentIndex numberOfSchemas>
        <div class="row py-2 g-2 <#if currentIndex < numberOfSchemas>border-bottom</#if>">
            <div class="col-md-3">
                <div class="title">
                    <div class="head">
                        <a class="fw-bold" href="schema.do?id=${ds.identifier}">${ds.title}</a>
                        <#if !ds.isLatest()>
                            <a tabindex="0" role="button"
                               class="popover-link"
                               data-bs-toggle="popover"
                               data-bs-trigger="focus"
                               data-bs-html="true"
                               data-bs-content="<@s.text name="admin.schemas.version.warning" escapeHtml=true/>">
                                <i class="bi bi-exclamation-triangle-fill text-warning"></i>
                            </a>
                        </#if>
                    </div>
                </div>
            </div>

            <div class="col-md-7">
                <div class="definition">
                    <div class="body">
                        <div>
                            <p class="overflow-x-auto">
                                ${ds.description!}
                            </p>
                        </div>
                        <div class="details table-responsive">
                            <table class="table table-sm table-borderless">
                                <#if ds.issued??>
                                    <tr><th class="col-3"><@s.text name="basic.issued"/></th><td>${ds.issued?date?string.long}</td></tr>
                                </#if>
                                <tr><th class="col-3"><@s.text name="schema.subschemas"/></th><td>${ds.subSchemas?size}</td></tr>
                                <tr><th><@s.text name="basic.name"/></th><td>${ds.name}</td></tr>
                                <tr><th><@s.text name="schema.identifier"/></th><td>${ds.identifier}</td></tr>
                                <tr><th><@s.text name="basic.link"/></th><td><a href="${ds.url}">${ds.url}</a></td></tr>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-2">
                <div class="actions d-flex justify-content-end">
                        <#if !ds.isLatest()>
                            <form action='updateSchema.do' method='post'>
                                <input type='hidden' name='id' value='${ds.identifier}' />
                                <@s.submit cssClass="confirm btn btn-sm btn-outline-gbif-primary mt-1 me-1" name="update" key="button.update"/>
                            </form>
                        </#if>
                        <form action='schema.do' method='post'>
                            <input type='hidden' name='id' value='${ds.identifier}' />
                            <input type='hidden' name='schemaName' value='${ds.name}' />
                            <@s.submit name="delete" cssClass="btn btn-sm btn-outline-gbif-danger mt-1" key="button.remove"/>
                        </form>
                    </div>
            </div>
        </div>
    </#macro>

    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">

    <#assign schemasCount=0>
    <#list schemas as ds>
        <#assign schemasCount=schemasCount+1>
    </#list>

    <div class="container-fluid bg-body border-bottom">
        <div class="container my-3">
            <#include "/WEB-INF/pages/inc/action_alerts.ftl">
        </div>

        <div class="container my-3 p-3">
            <div class="text-center">
                <div class="text-uppercase fw-bold fs-smaller-2">
                    <span><@s.text name="menu.admin"/></span>
                </div>

                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    <@s.text name="admin.schemas.title"/>
                </h1>

                <div class="text-smaller">
                    <#if schemasCount=0>
                        <span class="text-gbif-danger mt-3">
                            <@s.text name="admin.schemas.no.schemas.installed"/>
                        </span>
                    <#else>
                        <#if action.isUpToDate()>
                            <span class="text-gbif-primary"><@s.text name="admin.schemas.upToDate"/></span>
                        <#else>
                            <span class="text-gbif-danger"><@s.text name="admin.schemas.not.upToDate"/></span>
                        </#if>
                    </#if>
                </div>

                <div class="mt-2">
                    <a href="${baseURL}/admin/" class="btn btn-sm btn-outline-secondary mt-1 me-xl-1 top-button">
                        <@s.text name="button.back"/>
                    </a>
                </div>
            </div>
        </div>
    </div>

    <main class="container">
        <div class="my-3 p-3">
            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fs-5 fw-400">
                <@s.text name="admin.schemas.installed"/>
            </h4>

            <#list schemas as ds>
                <#assign schemasCount=schemasCount+1>
                <@dataSchemaRow ds schemasCount schemas?size/>
            </#list>

            <#if schemasCount=0>
                <div class="callout callout-warning">
                    <@s.text name="admin.schemas.no.schemas.installed.debug"><@s.param>${cfg.registryUrl}</@s.param></@s.text>
                </div>
            </#if>
        </div>

        <#if (newSchemas?size > 0)>
            <div class="my-3 p-3">
                <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fs-5 fw-400">
                    <@s.text name="schema.further.title"/>
                </h4>

                <#assign newSchemasCount=0>
                <#list newSchemas as schema>
                    <#assign newSchemasCount=newSchemasCount+1>
                    <div class="row py-2 g-2 <#sep>border-bottom</#sep>">
                        <div class="col-md-3">
                            <div class="title">
                                <div class="head fw-bold">
                                    ${schema.title}
                                </div>
                            </div>
                        </div>

                        <div class="col-md-8">
                            <div class="definition">
                                <div class="body">
                                    <div>
                                        <p class="overflow-x-auto">${schema.description!}</p>
                                    </div>
                                    <div class="details table-responsive">
                                        <table class="table table-sm table-borderless">
                                            <tr><th class="col-3"><@s.text name="schema.identifier"/></th><td>${schema.identifier!}</td></tr>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="col-md-1">
                            <div class="actions d-flex justify-content-end">
                                <form action='schema.do' method='post'>
                                    <input type='hidden' name='id' value='${schema.identifier}' />
                                    <@s.submit name="install" cssClass="btn btn-sm btn-outline-gbif-primary" key="button.install"/>
                                </form>
                            </div>
                        </div>
                    </div>
                </#list>
            </div>
        </#if>
    </main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
