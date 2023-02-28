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
                <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                    <ol class="breadcrumb justify-content-center mb-0">
                        <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                        <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.vocabulary"/></li>
                    </ol>
                </nav>
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

        <div class="mt-3 text-smaller">
            <#list vocabulary.concepts as c>
                <div class="row py-2 g-2 <#sep>border-bottom</#sep>">
                    <div class="col-lg-3 mt-1">
                        <a href="${c.uri}" style="color:#4e565f !important;" class="fst-italic" target="_blank"><b>${c.identifier}</b></a>
                    </div>

                    <div class="col-lg-9 mt-1">
                        <#if c.description?has_content>
                            <p class="fst-italic">
                                ${c.description}
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
            </#list>
        </div>
    </div>
</main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
