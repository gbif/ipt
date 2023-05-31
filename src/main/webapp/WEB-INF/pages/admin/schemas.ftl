<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name="admin.home.manageSchemas"/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>

    <#macro dataSchemaRow ds currentIndex numberOfSchemas>
        <div class="row py-2 g-2 <#if currentIndex < numberOfSchemas>border-bottom</#if>">
            <div class="col-lg-3 mt-3">
                <div class="d-flex justify-content-between">
                    <div class="title">
                        <div class="head">
                            <a class="fw-bold" href="schema.do?id=${ds.identifier}">${ds.title}</a>
                        </div>
                    </div>

                    <div class="d-flex justify-content-end d-lg-none">
                        <#if !ds.isLatest()>
                          <form action='updateSchema.do' method='post'>
                            <input type='hidden' name='id' value='${ds.identifier}' />

                            <button type="submit" value="Update" id="update" name="update" class="confirm extension-action-button extension-action-button-primary me-1">
                              <svg class="extension-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                <path d="M12 4V1L8 5l4 4V6c3.31 0 6 2.69 6 6 0 1.01-.25 1.97-.7 2.8l1.46 1.46C19.54 15.03 20 13.57 20 12c0-4.42-3.58-8-8-8zm0 14c-3.31 0-6-2.69-6-6 0-1.01.25-1.97.7-2.8L5.24 7.74C4.46 8.97 4 10.43 4 12c0 4.42 3.58 8 8 8v3l4-4-4-4v3z"></path>
                              </svg>
                                <@s.text name="button.update"/>
                            </button>
                          </form>
                        </#if>
                        <form action='schema.do' method='post'>
                            <input type='hidden' name='id' value='${ds.identifier}' />
                            <input type='hidden' name='schemaName' value='${ds.name}' />

                            <button type="submit" value="Delete" id="delete" name="delete" class="extension-action-button extension-action-button-danger">
                                <svg class="extension-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                </svg>
                                <@s.text name="button.remove"/>
                            </button>
                        </form>
                    </div>
                </div>
            </div>

            <div class="col-lg-7 text-smaller mt-3">
                <div class="definition">
                    <div class="body">
                        <div>
                            <p class="overflow-x-auto">
                                ${ds.description!}
                            </p>

                            <#if !ds.isLatest()>
                                <p class="fst-italic text-gbif-danger">
                                    <@s.text name="admin.schemas.version.warning" escapeHtml=true/>
                                </p>
                            </#if>
                        </div>
                        <div class="details table-responsive">
                            <table class="table table-sm table-borderless">
                                <#if ds.issued??>
                                    <tr><th class="col-3 py-0"><@s.text name="basic.issued"/></th><td class="py-0">${ds.issued?date?string.long}</td></tr>
                                </#if>
                                <tr><th class="col-3 py-0"><@s.text name="schema.subschemas"/></th><td class="py-0">${ds.subSchemas?size}</td></tr>
                                <tr><th class="py-0"><@s.text name="basic.name"/></th><td class="py-0">${ds.name}</td></tr>
                                <tr><th class="py-0"><@s.text name="schema.identifier"/></th><td class="py-0">${ds.identifier}</td></tr>
                                <tr><th class="py-0"><@s.text name="basic.link"/></th><td class="py-0"><a href="${ds.url}">${ds.url}</a></td></tr>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-3 col-lg-2 text-smaller d-lg-max-none mt-3">
                <div class="actions d-flex justify-content-end">
                    <#if !ds.isLatest()>
                        <form action='updateSchema.do' method='post'>
                            <input type='hidden' name='id' value='${ds.identifier}' />

                            <button type="submit" value="Update" id="update" name="update" class="confirm extension-action-button extension-action-button-primary me-1">
                                <svg class="extension-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                    <path d="M12 4V1L8 5l4 4V6c3.31 0 6 2.69 6 6 0 1.01-.25 1.97-.7 2.8l1.46 1.46C19.54 15.03 20 13.57 20 12c0-4.42-3.58-8-8-8zm0 14c-3.31 0-6-2.69-6-6 0-1.01.25-1.97.7-2.8L5.24 7.74C4.46 8.97 4 10.43 4 12c0 4.42 3.58 8 8 8v3l4-4-4-4v3z"></path>
                                </svg>
                                <@s.text name="button.update"/>
                            </button>
                        </form>
                    </#if>
                    <form action='schema.do' method='post'>
                        <input type='hidden' name='id' value='${ds.identifier}' />
                        <input type='hidden' name='schemaName' value='${ds.name}' />

                        <button type="submit" value="Delete" id="delete" name="delete" class="extension-action-button extension-action-button-danger">
                            <svg class="extension-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                            </svg>
                            <@s.text name="button.remove"/>
                        </button>
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
                        <div class="col-lg-3 mt-3">
                            <div class="d-flex justify-content-between">
                                <div class="d-flex title">
                                    <div class="head fw-bold">
                                        ${schema.title}
                                    </div>
                                </div>

                                <div class="d-flex justify-content-end d-lg-none">
                                    <form action='schema.do' method='post'>
                                        <input type='hidden' name='id' value='${schema.identifier}' />

                                        <button type="submit" value="Install" id="install" name="install" class="extension-action-button extension-action-button-primary">
                                            <svg class="extension-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                <path d="M20 17H4V5h8V3H4c-1.11 0-2 .89-2 2v12c0 1.1.89 2 2 2h4v2h8v-2h4c1.1 0 2-.9 2-2v-3h-2v3z"></path><path d="m17 14 5-5-1.41-1.41L18 10.17V3h-2v7.17l-2.59-2.58L12 9z"></path>
                                            </svg>
                                            <@s.text name="button.install"/>
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </div>

                        <div class="col-lg-8 text-smaller mt-3">
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

                        <div class="col-lg-1 text-smaller mt-3 d-lg-max-none">
                            <div class="actions d-flex justify-content-end">
                                <form action='schema.do' method='post'>
                                    <input type='hidden' name='id' value='${schema.identifier}' />

                                    <button type="submit" value="Install" id="install" name="install" class="extension-action-button extension-action-button-primary">
                                        <svg class="extension-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                            <path d="M20 17H4V5h8V3H4c-1.11 0-2 .89-2 2v12c0 1.1.89 2 2 2h4v2h8v-2h4c1.1 0 2-.9 2-2v-3h-2v3z"></path><path d="m17 14 5-5-1.41-1.41L18 10.17V3h-2v7.17l-2.59-2.58L12 9z"></path>
                                        </svg>
                                        <@s.text name="button.install"/>
                                    </button>
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
