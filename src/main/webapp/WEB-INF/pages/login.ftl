<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="login.title"/></title>
<#assign currentMenu = "account"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl">

<main class="container login-container">
    <div class="my-3 px-4 px-sm-5 pb-3 pb-sm-4 pt-4 pt-sm-36p border rounded shadow-sm">
        <div class="text-smaller">
            <#include "/WEB-INF/pages/inc/action_alerts_messages.ftl">
            <#include "/WEB-INF/pages/inc/action_alerts_errors.ftl">
        </div>

        <div class="row text-center">
            <div class="col-sm-2 p-0">
                <span>
                    <img alt="GBIF" style="height: 50px;" src="${baseURL}/images/gbif-logo.svg"/>
                </span>
            </div>
            <div class="col-sm-10 p-0 pt-1">
                <div class="fs-6 fw-400 text-uppercase site-name">
                    Integrated Publishing Toolkit
                </div>
                <div class="text-smaller" style="opacity: 0.75;">
                    <@s.text name="admin.config.setup.common.tagline"/>
                </div>
            </div>
        </div>

        <p class="text-center text-smaller mt-5">
            <@s.text name="login.intro">
                <@s.param>${adminEmail}</@s.param>
            </@s.text>
        </p>

        <form class="needs-validation" action="${baseURL}/login.do" method="post" novalidate>
            <input type="hidden" name="csrfToken" value="${newCsrfToken!}">
            <#assign userEmail>
                <@s.text name="user.email"/>
            </#assign>
            <#assign userPassword>
                <@s.text name="user.password"/>
            </#assign>

            <div class="row g-3 mt-0 mb-2">
                <div class="col-12">
                    <input class="form-control" type="text" id="email" name="email" value="${email!}" placeholder="${userEmail}">
                </div>

                <div class="col-12">
                    <input class="form-control" type="password" id="password" name="password" value="${password!}" placeholder="${userPassword}">
                </div>

                <#if email?has_content>
                    <p class="text-center text-smaller">
                        <@s.text name="login.forgottenpassword"><@s.param>${adminEmail}</@s.param></@s.text>
                    </p>
                </#if>

                <div class="col-12 pt-3">
                    <@s.submit cssClass="btn btn-outline-gbif-primary w-100 text-capitalize" name="login" key="portal.login"/>
                </div>
            </div>
        </form>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
