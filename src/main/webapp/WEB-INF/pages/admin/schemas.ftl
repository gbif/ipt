<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name="admin.home.manageSchemas"/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>

    <#macro dataSchemaRow ds currentIndex numberOfSchemas>
        <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 <#if currentIndex < numberOfSchemas>border-bottom</#if>">
            <div class="col-md-3">
                <div class="title">
                    <div class="head">
                        <a href="schema.do?id=${ds.identifier}">${ds.title}</a>
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
                    <div class="actions d-flex">
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

            <div class="col-md-9">
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
        </div>
    </#macro>

    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">

    <main class="container">
        <div class="my-3 p-3 border rounded shadow-sm">
            <#include "/WEB-INF/pages/inc/action_alerts.ftl">

            <h5 class="border-bottom pb-2 mb-0 mx-md-4 mx-2 pt-2 text-gbif-header fw-400 text-center">
                <@s.text name="admin.schemas.title"/>
            </h5>

            <#assign count=0>

            <#list schemas as ds>
                <#assign count=count+1>
                <@dataSchemaRow ds count schemas?size/>
            </#list>

            <#if count=0>
                <p class="text-gbif-danger mx-md-4 mx-2 mb-0">
                    <@s.text name="admin.schemas.no.schemas.installed"/>
                </p>
                <p class="mx-md-4 mx-2">
                    <span class="text-gbif-warning">
                        <i class="bi bi-exclamation-triangle"></i>
                        <@s.text name="admin.schemas.no.schemas.installed.debug"><@s.param>${cfg.registryUrl}</@s.param></@s.text>
                    </span>
                </p>
            </#if>
        </div>

        <#if (newSchemas?size > 0)>
            <div class="my-3 p-3 border rounded shadow-sm">
                <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header fw-400 text-center">
                    <@s.text name="schema.further.title"/>
                </h5>

                <#assign count=0>
                <#list newSchemas as schema>
                    <#assign count=count+1>
                    <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 <#sep>border-bottom</#sep>">
                        <div class="col-md-3">
                            <div class="title">
                                <div class="head">
                                    ${schema.title}
                                </div>
                                <div class="actions">
                                    <form action='schema.do' method='post'>
                                        <input type='hidden' name='id' value='${schema.identifier}' />
                                        <@s.submit name="install" cssClass="btn btn-sm btn-outline-gbif-primary" key="button.install"/>
                                    </form>
                                </div>
                            </div>
                        </div>

                        <div class="col-md-9">
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
                    </div>
                </#list>
            </div>
        </#if>
    </main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
