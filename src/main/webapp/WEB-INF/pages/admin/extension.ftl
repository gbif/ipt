<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="admin.extension.title"/></title>
<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<main class="container">
    <div class="row g-3">
        <div class="my-3 p-3 bg-body rounded shadow-sm">
            <#include "/WEB-INF/pages/inc/action_alerts.ftl">

            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header text-center">
                <@s.text name="admin.extension.title"/> ${extension.title}
            </h5>

            <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <strong><@s.text name="basic.title"/></strong>
                </div>
                <div class="col-lg-9">
                    ${extension.title}
                </div>
            </div>

            <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <strong><@s.text name="basic.description"/></strong>
                </div>
                <div class="col-lg-9">
                    ${extension.description}
                </div>
            </div>

            <#if extension.link?has_content>
                <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
                    <div class="col-lg-3">
                        <strong><@s.text name="basic.link"/></strong>
                    </div>
                    <div class="col-lg-9">
                        <a href="${extension.link}">${extension.link}</a>
                    </div>
                </div>
            </#if>

            <#if extension.issued??>
                <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
                    <div class="col-lg-3">
                        <strong><@s.text name="basic.issued"/></strong>
                    </div>
                    <div class="col-lg-9">${extension.issued?date?string.medium}</div>
                </div>
            </#if>

            <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <strong><@s.text name="extension.properties"/></strong>
                </div>
                <div class="col-lg-9">${extension.properties?size}</div>
            </div>

            <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <strong><@s.text name="basic.name"/></strong>
                </div>
                <div class="col-lg-9">${extension.name}</div>
            </div>

            <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <strong><@s.text name="basic.namespace"/></strong>
                </div>
                <div class="col-lg-9">${extension.namespace}</div>
            </div>

            <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <strong><@s.text name="extension.rowtype"/></strong>
                </div>
                <div class="col-lg-9">${extension.rowType}</div>
            </div>

            <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <strong><@s.text name="basic.lastModified"/></strong>
                </div>
                <div class="col-lg-9">${extension.modified?datetime?string("yyyy-MM-dd HH:mm:ss")}</div>
            </div>

            <div class="mx-md-4 mx-2 mt-2">
                <a href="extensions.do" class="btn btn-outline-secondary ignore-link-color">
                    <@s.text name="button.back"/>
                </a>
            </div>

        </div>

        <div class="my-3 p-3 bg-body rounded shadow-sm">
            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header">
                <@s.text name="admin.extension.properties"/>
            </h5>

            <#list extension.properties as p>
                <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
                    <div class="col-lg-3">
                        <a name="${p.qualname}"></a>
                        <div class="title">
                            <div class="head">
                                <strong>${p.name}</strong>
                            </div>
                        </div>
                    </div>

                    <div class="col-lg-9">
                        <div class="definition">
                            <div class="body">
                                <p class="overflow-x-auto">
                                    <#if p.description?has_content>${p.description}<br/></#if>
                                    <#if p.link?has_content><@s.text name="basic.seealso"/> <a href="${p.link}">${p.link}</a></#if>
                                </p>
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
                                <div class="details">
                                    <table>
                                        <tr><th><@s.text name="extension.prop.qname"/></th><td>${p.qualname}</td></tr>
                                        <tr><th><@s.text name="basic.namespace"/></th><td>${p.namespace}</td></tr>
                                        <tr><th><@s.text name="extension.prop.group"/></th><td>${p.group!}</td></tr>
                                        <tr><th><@s.text name="extension.prop.type"/></th><td>${p.type}</td></tr>
                                        <tr><th><@s.text name="extension.prop.required"/></th><td>${p.required?string}</td></tr>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </#list>
        </div>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
