<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="admin.vocabulary.title"/></title>
<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<main class="container">
    <div class="row g-3">
        <div class="my-3 p-3 bg-body rounded shadow-sm">
            <#include "/WEB-INF/pages/inc/action_alerts.ftl">

            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header text-center">
                <@s.text name="admin.vocabulary.title"/> ${vocabulary.title}
            </h5>

            <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <strong><@s.text name="basic.title"/></strong>
                </div>
                <div class="col-lg-9">
                    ${vocabulary.title}
                </div>
            </div>

            <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <strong><@s.text name="basic.description"/></strong>
                </div>
                <div class="col-lg-9">
                    ${vocabulary.description}
                </div>
            </div>

            <#if vocabulary.link?has_content>
                <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
                    <div class="col-lg-3">
                        <strong><@s.text name="basic.link"/></strong>
                    </div>
                    <div class="col-lg-9">
                        <a href="${vocabulary.link}">${vocabulary.link}</a>
                    </div>
                </div>
            </#if>

            <#if vocabulary.issued??>
                <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
                    <div class="col-lg-3">
                        <strong><@s.text name="basic.issued"/></strong>
                    </div>
                    <div class="col-lg-9">${vocabulary.issued?date?string.medium}</div>
                </div>
            </#if>

            <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <strong><@s.text name="vocabulary.concepts"/></strong>
                </div>
                <div class="col-lg-9">${vocabulary.concepts?size}</div>
            </div>

            <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <strong><@s.text name="basic.identifier"/></strong>
                </div>
                <div class="col-lg-9">${vocabulary.uriString}</div>
            </div>

            <#if vocabulary.subject??>
                <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
                    <div class="col-lg-3">
                        <strong><@s.text name="basic.keywords"/></strong>
                    </div>
                    <div class="col-lg-9">${vocabulary.subject!}</div>
                </div>
            </#if>

            <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <strong><@s.text name="basic.lastModified"/></strong>
                </div>
                <div class="col-lg-9">${vocabulary.modified?datetime?string("yyyy-MM-dd HH:mm:ss")}</div>
            </div>
        </div>

        <div class="my-3 p-3 bg-body rounded shadow-sm">
            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header">
                <@s.text name="vocabulary.concepts"/>
            </h5>

            <#list vocabulary.concepts as c>
                <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
                    <div class="col-lg-3">
                        <a name="${c.identifier}"></a>
                        <div class="title">
                            <div class="head">
                                <strong>${c.identifier}</strong>
                            </div>
                        </div>
                    </div>

                    <div class="col-lg-9">
                        <div class="body">
                            <#if c.description?has_content || c.link?has_content>
                                <p>
                                    ${c.description!}
                                    <#if c.link?has_content><br/><a href="${c.link}"><@s.text name="basic.seealso"/> ${c.link}</a></#if>
                                </p>
                            </#if>
                            <p>
                                <@s.text name="vocabulary.terms.pref"/>:
                                <em><#list c.preferredTerms as t>${t.title} <span class="small">[${t.lang}]</span>; </#list></em>
                            </p>
                            <p>
                                <@s.text name="vocabulary.terms.alt"/>:
                                <em><#list c.alternativeTerms as t>${t.title} <span class="small">[${t.lang}]</span>; </#list></em>
                            </p>
                            <div class="details">
                                <table>
                                    <tr><th><@s.text name="basic.identifier"/></th><td>${c.identifier}</td></tr>
                                    <tr><th><@s.text name="vocabulary.uri"/></th><td>${c.uri}</td></tr>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </#list>
        </div>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
