<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name="admin.home.manageSchemas"/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>

    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">

    <main class="container">
        <div class="my-3 p-3 border rounded shadow-sm">
            <#include "/WEB-INF/pages/inc/action_alerts.ftl">

            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header fw-400 text-center">
                <@s.text name="admin.schemas.title"/>: ${dataSchema.title}
            </h5>

            <div class="row mx-md-4 mx-2 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <strong><@s.text name="basic.title"/></strong>
                </div>
                <div class="col-lg-9 overflow-x-auto">
                    ${dataSchema.title}
                </div>
            </div>

            <div class="row mx-md-4 mx-2 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <strong><@s.text name="basic.description"/></strong>
                </div>
                <div class="col-lg-9 overflow-x-auto">
                    ${dataSchema.description}
                </div>
            </div>

            <div class="row mx-md-4 mx-2 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <strong><@s.text name="basic.link"/></strong>
                </div>
                <div class="col-lg-9 overflow-x-auto">
                    <a href="${dataSchema.url}">${dataSchema.url}</a>
                </div>
            </div>

            <#if dataSchema.issued??>
                <div class="row mx-md-4 mx-2 p-2 pb-2 g-2 border-bottom">
                    <div class="col-lg-3">
                        <strong><@s.text name="basic.issued"/></strong>
                    </div>
                    <div class="col-lg-9">${dataSchema.issued?date?string.long}</div>
                </div>
            </#if>

            <div class="row mx-md-4 mx-2 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <strong><@s.text name="basic.name"/></strong>
                </div>
                <div class="col-lg-9">${dataSchema.name}</div>
            </div>

            <div class="row mx-md-4 mx-2 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <strong><@s.text name="schema.identifier"/></strong>
                </div>
                <div class="col-lg-9 overflow-x-auto">${dataSchema.identifier}</div>
            </div>

            <div class="row mx-md-4 mx-2 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <strong><@s.text name="schema.version"/></strong>
                </div>
                <div class="col-lg-9 overflow-x-auto">${dataSchema.version}</div>
            </div>

            <div class="mx-md-4 mx-2 mt-2">
                <a href="schemas.do" class="btn btn-outline-secondary">
                    <@s.text name="button.back"/>
                </a>
            </div>
        </div>

        <#list dataSchema.subSchemas as subSchema>
            <div class="my-3 p-3 border rounded shadow-sm">
                <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header fw-400 text-center">
                    <@s.text name="schema.subschemas"/>: ${subSchema.title}
                </h5>

                <#list subSchema.fields as field>
                    <div class="row mx-md-4 mx-2 p-2 pb-2 g-2 <#sep>border-bottom</#sep>">
                        <div class="col-lg-3">
                            <div class="title">
                                <div class="head overflow-x-auto">
                                    <strong>${field.name}</strong>
                                </div>
                            </div>
                        </div>

                        <div class="col-lg-9">
                            <div class="definition">
                                <div class="body">
                                    <#if field.description?has_content>
                                        <p class="overflow-x-auto">
                                            ${field.description}
                                        </p>
                                    </#if>

                                    <div class="details table-responsive">
                                        <table>
                                            <tr>
                                                <th class="pe-md-4 pe-2"><@s.text name="schema.field.type"/></th>
                                                <td>${field.type}</td>
                                            </tr>
                                            <tr>
                                                <th class="pe-md-4 pe-2"><@s.text name="schema.field.format"/></th>
                                                <td>
                                                    <#if field.format??>
                                                        ${field.format}
                                                    <#else>
                                                        -
                                                    </#if>
                                                </td>
                                            </tr>
                                            <tr>
                                                <th class="pe-md-4 pe-2"><@s.text name="schema.field.constraints"/></th>
                                                <td>
                                                    <#if field.constraints??>
                                                        <#list field.constraints as constraintKey, constraintValue>
                                                            ${constraintKey} : <code>${constraintValue?string}</code><br>
                                                        </#list>
                                                    <#else>
                                                        -
                                                    </#if>
                                                </td>
                                            </tr>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </#list>
            </div>
        </#list>

    </main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
