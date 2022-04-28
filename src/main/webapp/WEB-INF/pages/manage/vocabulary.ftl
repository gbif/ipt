<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name="admin.vocabulary.title"/></title>
    <#assign currentMenu = "manage"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">

<div class="container-fluid bg-body border-bottom">
    <div class="container my-3">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container my-3 p-3">
        <div class="text-center">
            <div class="text-uppercase fw-bold fs-smaller-2">
                <span><@s.text name="extension.vocabulary"/></span>
            </div>

            <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                ${vocabulary.title}
            </h1>

            <#if vocabulary.link?has_content>
                <div class="text-smaller mb-2">
                    <a href="${vocabulary.link}">${vocabulary.link}</a>
                </div>
            </#if>

            <#if vocabulary.issued??>
                <div class="text-smaller text-gbif-primary">
                <span>
                    <@s.text name='schema.version'/> <@s.text name='schema.issuedOn'/> ${vocabulary.issued?date?string.long}
                </span>
                </div>
            </#if>
        </div>
    </div>
</div>

<main class="container">
    <div class="my-3 p-3">
        <h5 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
            <@s.text name="basic.description"/>
        </h5>

        <#if vocabulary.description??>
            <div class="mb-3">
                ${vocabulary.description}
            </div>
        </#if>

        <div class="row pb-2 text-smaller">
            <div class="col-lg-3">
                <strong><@s.text name="vocabulary.concepts"/></strong>
            </div>
            <div class="col-lg-9">${vocabulary.concepts?size}</div>
        </div>

        <div class="row pb-2 text-smaller">
            <div class="col-lg-3">
                <strong><@s.text name="basic.identifier"/></strong>
            </div>
            <div class="col-lg-9 overflow-x-auto">${vocabulary.uriString}</div>
        </div>

        <#if vocabulary.subject??>
            <div class="row pb-2 text-smaller">
                <div class="col-lg-3">
                    <strong><@s.text name="basic.keywords"/></strong>
                </div>
                <div class="col-lg-9">${vocabulary.subject!}</div>
            </div>
        </#if>

        <div class="row pb-2 text-smaller">
            <div class="col-lg-3">
                <strong><@s.text name="basic.lastModified"/></strong>
            </div>
            <div class="col-lg-9">${vocabulary.modified?datetime?string.long_short}</div>
        </div>
    </div>

    <div class="my-3 p-3">
        <h5 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
            <@s.text name="vocabulary.concepts"/>
        </h5>

        <div class="mt-3 overflow-x-auto">
            <div id="tableContainer" class="table-responsive text-smaller pt-2">
                <table class="table table-sm dataTable no-footer"  role="grid">
                    <thead>
                    <tr role="row">
                        <th><@s.text name='basic.name'/></th>
                        <th><@s.text name='basic.description'/></th>
                        <th><@s.text name="vocabulary.terms.pref"/></th>
                        <th><@s.text name="vocabulary.terms.alt"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <#list vocabulary.concepts as c>
                        <tr>
                            <td>
                                <a href="${c.uri}" style="color:#4e565f !important;" class="fst-italic" target="_blank"><b>${c.identifier}</b></a>
                            </td>
                            <td>
                                <#if c.description?has_content>
                                    <span class="fst-italic">${c.description}</span>
                                <#else>
                                    --
                                </#if>
                            </td>
                            <td><#list c.preferredTerms as t>${t.title} <span class="small">[${t.lang}]</span><#sep>;</#sep> </#list></td>
                            <td>
                                <#if c.alternativeTerms?has_content>
                                    <em><#list c.alternativeTerms as t>${t.title} <span class="small">[${t.lang}]</span><#sep>;</#sep> </#list></em>
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
</main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
