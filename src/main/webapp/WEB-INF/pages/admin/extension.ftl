<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="admin.extension.title"/></title>
<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<main class="container">
    <div class="my-3 p-3 border rounded shadow-sm">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">

        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header fw-400 text-center">
            <@s.text name="admin.extension.title"/> ${extension.title}
        </h5>

        <div class="row mx-md-4 mx-2 p-2 pb-2 g-2 border-bottom">
            <div class="col-lg-3">
                <strong><@s.text name="basic.title"/></strong>
            </div>
            <div class="col-lg-9 overflow-x-auto">
                ${extension.title}
            </div>
        </div>

        <div class="row mx-md-4 mx-2 p-2 pb-2 g-2 border-bottom">
            <div class="col-lg-3">
                <strong><@s.text name="basic.description"/></strong>
            </div>
            <div class="col-lg-9 overflow-x-auto">
                ${extension.description}
            </div>
        </div>

        <#if extension.link?has_content>
            <div class="row mx-md-4 mx-2 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <strong><@s.text name="basic.link"/></strong>
                </div>
                <div class="col-lg-9 overflow-x-auto">
                    <a href="${extension.link}">${extension.link}</a>
                </div>
            </div>
        </#if>

        <#if extension.issued??>
            <div class="row mx-md-4 mx-2 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <strong><@s.text name="basic.issued"/></strong>
                </div>
                <div class="col-lg-9">${extension.issued?date?string.long}</div>
            </div>
        </#if>

        <div class="row mx-md-4 mx-2 p-2 pb-2 g-2 border-bottom">
            <div class="col-lg-3">
                <strong><@s.text name="extension.properties"/></strong>
            </div>
            <div class="col-lg-9">${extension.properties?size}</div>
        </div>

        <div class="row mx-md-4 mx-2 p-2 pb-2 g-2 border-bottom">
            <div class="col-lg-3">
                <strong><@s.text name="basic.name"/></strong>
            </div>
            <div class="col-lg-9">${extension.name}</div>
        </div>

        <div class="row mx-md-4 mx-2 p-2 pb-2 g-2 border-bottom">
            <div class="col-lg-3">
                <strong><@s.text name="basic.namespace"/></strong>
            </div>
            <div class="col-lg-9 overflow-x-auto">${extension.namespace}</div>
        </div>

        <div class="row mx-md-4 mx-2 p-2 pb-2 g-2 border-bottom">
            <div class="col-lg-3">
                <strong><@s.text name="extension.rowtype"/></strong>
            </div>
            <div class="col-lg-9 overflow-x-auto">${extension.rowType}</div>
        </div>

        <div class="row mx-md-4 mx-2 p-2 pb-2 g-2 border-bottom">
            <div class="col-lg-3">
                <strong><@s.text name="basic.lastModified"/></strong>
            </div>
            <div class="col-lg-9">${extension.modified?datetime?string.long_short}</div>
        </div>

        <div class="mx-md-4 mx-2 mt-2">
            <a href="extensions.do" class="btn btn-outline-secondary">
                <@s.text name="button.back"/>
            </a>
        </div>

    </div>

    <div class="my-3 p-3 border rounded shadow-sm">
        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header fw-400">
            <@s.text name="admin.extension.properties"/>
        </h5>

        <#list extension.properties as p>
            <div class="row mx-md-4 mx-2 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <a name="${p.qualname}"></a>
                    <div class="title">
                        <div class="head overflow-x-auto">
                            <strong>${p.name}</strong>
                        </div>
                    </div>
                </div>

                <div class="col-lg-9">
                    <div class="definition">
                        <div class="body">
                            <#if p.description?has_content>
                                <p class="overflow-x-auto">
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
                                    <em><@s.text name="basic.examples"/></em>: ${p.examples}
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
                                    <tr><th class="pe-md-4 pe-2"><@s.text name="extension.prop.group"/></th><td>${p.group!}</td></tr>
                                    <tr><th class="pe-md-4 pe-2"><@s.text name="extension.prop.type"/></th><td>${p.type}</td></tr>
                                    <tr><th class="pe-md-4 pe-2"><@s.text name="extension.prop.required"/></th><td>${p.required?string}</td></tr>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </#list>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
