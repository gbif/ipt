<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="admin.vocabulary.title"/></title>
<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<div class="container-fluid bg-body border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3">
            <div class="text-center">
                <div class="fs-smaller">
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
</div>

<main class="container main-content-container">
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
          <strong><@s.text name="basic.latest"/></strong>
        </div>
        <div class="col-lg-9">
            <#if vocabulary.latest>
              <i class="bi bi-circle-fill text-gbif-primary"></i>
              <span class="text-gbif-primary"><@s.text name="basic.yes"/></span>
            <#else>
              <i class="bi bi-circle-fill text-gbif-danger"></i>
              <span class="text-gbif-danger"><@s.text name="basic.no"/></span>
            </#if>
        </div>
      </div>

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
                            <ul>
                                <#list c.preferredTerms as t>
                                    <li><em>${t.title} <span class="small">[${t.lang}]</span></em></li>
                                </#list>
                            </ul>
                            <#if c.alternativeTerms?has_content>
                                <p class="mb-0">
                                    <@s.text name="vocabulary.terms.alt"/>:
                                </p>
                                <ul class="fs-smaller-2">
                                    <#list c.alternativeTerms as t>
                                        <li class="text-discreet"><em>${t.title} <span>[${t.lang}]</span></em></li>
                                    </#list>
                                </ul>
                            </#if>
                        </div>
                    </div>
                </div>
            </#list>
        </div>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
