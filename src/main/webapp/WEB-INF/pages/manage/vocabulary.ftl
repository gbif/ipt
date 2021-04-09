<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header-bootstrap.ftl">
    <title><@s.text name="admin.vocabulary.title"/></title>
    <#assign currentMenu = "manage"/>
    <#include "/WEB-INF/pages/inc/menu-bootstrap.ftl">

<main class="container">
    <div class="my-3 p-3 bg-body rounded shadow-sm">

        <#include "/WEB-INF/pages/inc/action_alerts-bootstrap.ftl">

        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-success text-center">
            ${vocabulary.title}
        </h5>

        <p class="text-muted mx-md-4 mx-2">${vocabulary.description!}</p>

        <#if vocabulary.subject?has_content>
            <p><@s.text name="basic.keywords"/>: ${vocabulary.subject}</p>
        </#if>
        <#if vocabulary.link?has_content>
            <p><@s.text name="basic.link"/>: <a href="${vocabulary.link}">${vocabulary.link}</a></p>
        </#if>
    </div>

    <div class="my-3 p-3 bg-body rounded shadow-sm">
        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-success">
            <@s.text name="vocabulary.concepts"/>
        </h5>

        <#list vocabulary.concepts as c>
            <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
                <div class="col-lg-3">
                    <strong class="text-muted">${c.identifier}</strong>
                </div>

                <div class="col-lg-9">
                    <#if c.description?has_content>
                        <div>
                            ${c.description!}
                        </div>
                    </#if>
                    <#if c.link?has_content>
                        <div>
                            <@s.text name="basic.seealso"/>: <a href="${c.link}">${c.link}</a>
                        </div>
                    </#if>
                    <div>
                        <@s.text name="vocabulary.terms.pref"/>:
                        <em><#list c.preferredTerms as t>${t.title} <span class="small">[${t.lang}]</span>; </#list></em>
                    </div>
                    <div>
                        <@s.text name="vocabulary.terms.alt"/>:
                        <em><#list c.alternativeTerms as t>${t.title} <span class="small">[${t.lang}]</span>; </#list></em>
                    </div>
                </div>
            </div>
        </#list>
    </div>
</main>

    <#include "/WEB-INF/pages/inc/footer-bootstrap.ftl">
</#escape>
