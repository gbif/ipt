<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="admin.extension.title"/></title>
<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/data_bage.ftl">

<div class="container-fluid bg-body border-bottom">
    <div class="container">
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

<main class="container">
    <div class="my-3 p-3">
        <h5 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
            <@s.text name="basic.description"/>
        </h5>

        <div class="mb-3">
            ${extension.description}
        </div>

        <div class="row pb-2 text-smaller">
            <div class="col-lg-3">
                <strong><@s.text name="extension.properties"/></strong>
            </div>
            <div class="col-lg-9">${extension.properties?size}</div>
        </div>

        <div class="row pb-2 text-smaller">
            <div class="col-lg-3">
                <strong><@s.text name="basic.name"/></strong>
            </div>
            <div class="col-lg-9">${extension.name}</div>
        </div>

        <div class="row pb-2 text-smaller">
            <div class="col-lg-3">
                <strong><@s.text name="basic.namespace"/></strong>
            </div>
            <div class="col-lg-9 overflow-x-auto">${extension.namespace}</div>
        </div>

        <div class="row pb-2 text-smaller">
            <div class="col-lg-3">
                <strong><@s.text name="extension.rowtype"/></strong>
            </div>
            <div class="col-lg-9 overflow-x-auto">${extension.rowType}</div>
        </div>

        <div class="row pb-2 text-smaller">
            <div class="col-lg-3">
                <strong><@s.text name="basic.lastModified"/></strong>
            </div>
            <div class="col-lg-9">${extension.modified?datetime?string.long_short}</div>
        </div>
    </div>

    <div class="my-3 p-3">
        <h5 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
            <@s.text name="admin.extension.properties"/>
        </h5>

        <div class="mt-3 overflow-x-auto">
            <div id="tableContainer" class="table-responsive text-smaller pt-2">
                <table class="table table-sm dataTable no-footer"  role="grid">
                    <thead>
                    <tr role="row">
                        <th><@s.text name='basic.name'/></th>
                        <th><@s.text name='basic.description'/></th>
                        <th><@s.text name='extension.prop.group'/></th>
                        <th><@s.text name='extension.prop.type'/></th>
                        <th><@s.text name='extension.prop.required'/></th>
                        <th><@s.text name='extension.vocabulary'/></th>
                        <th><@s.text name='basic.examples'/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <#list extension.properties as p>
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
                            <td>${p.group!"--"}</td>
                            <td><@dataBage p.type/></td>
                            <td>${p.required?string}</td>
                            <td>
                                <#if p.vocabulary??>
                                    <a href="vocabulary.do?id=${p.vocabulary.uriString}" target="_blank">${p.vocabulary.title}</a>
                                <#else>
                                    --
                                </#if>
                            </td>
                            <td>
                                <#if p.examples?has_content>
                                    ${p.examples}
                                </#if>
                            </td>
                        </tr>
                    </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
