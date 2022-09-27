<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="admin.vocabulary.title"/></title>
<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<div class="container-fluid bg-body border-bottom">
    <div class="container my-3">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container my-3 p-3">
        <div class="text-center">
            <div class="text-uppercase fw-bold fs-smaller-2">
                <span><@s.text name="admin.vocabulary.title"/></span>
            </div>

            <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                ${vocabulary.title}
            </h1>

            <div class="text-smaller mb-2">
                <a href="${vocabulary.link!vocabulary.uriString}">${vocabulary.link!vocabulary.uriString}</a>
            </div>

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
            <div class="col-lg-9 overflow-x-auto"><a href="${vocabulary.uriString}">${vocabulary.uriString}</a></div>
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

        <div class="mt-3">
            <#list vocabulary.concepts as c>
                <div class="row py-2 g-2 text-smaller <#sep>border-bottom</#sep>">
                    <div class="col-lg-3 mt-1">
                        <div class="title">
                            <div class="head">
                                <a href="${c.uri}" style="color:#4e565f !important;" class="fst-italic" target="_blank"><b>${c.identifier}</b></a>
                            </div>
                        </div>
                    </div>

                    <div class="col-lg-9 mt-1">
                        <div class="body">
                            <#if c.description?has_content>
                                <p class="overflow-x-auto fst-italic">
                                    ${c.description!}
                                </p>
                            </#if>
                            <#if c.link?has_content>
                                <p class="overflow-x-auto">
                                    <@s.text name="basic.seealso"/>: <a href="${c.link}">${c.link}</a>
                                </p>
                            </#if>
                            <div>
                                <@s.text name="vocabulary.terms.pref"/>:
                                <em><#list c.preferredTerms as t>${t.title} <span class="small">[${t.lang}]</span><#sep>;</#sep> </#list></em>
                            </div>
                            <#if c.alternativeTerms?has_content>
                                <div>
                                    <@s.text name="vocabulary.terms.alt"/>:
                                    <em><#list c.alternativeTerms as t>${t.title} <span class="small">[${t.lang}]</span><#sep>;</#sep> </#list></em>
                                </div>
                            </#if>
                        </div>
                    </div>
                </div>
            </#list>
        </div>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
