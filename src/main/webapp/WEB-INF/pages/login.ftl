<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="login.title"/></title>
<#assign currentMenu = "account"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl">

<main class="container" style="max-width: 400px;">
    <div class="my-3 p-3 bg-body rounded shadow-sm">

        <#include "/WEB-INF/pages/inc/action_alerts_without_warnings.ftl">

        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header text-center">
            <@s.text name="login.title"/>
        </h5>

        <p class="mx-md-4 mx-2">
            <@s.text name="login.intro">
                <@s.param>${adminEmail}</@s.param>
            </@s.text>
        </p>

        <form class="needs-validation" action="${baseURL}/login.do" method="post" novalidate>
            <input type="hidden" name="csrfToken" value="${newCsrfToken!}">

            <div class="row g-3 mx-md-4 mx-2 mt-0 mb-2">
                <div class="col-12">
                    <@input name="email" i18nkey="user.email" value="${email!}"/>
                </div>

                <div class="col-12">
                    <@input name="password" i18nkey="user.password" type="password" value="${password!}"/>
                </div>

                <#if email?has_content>
                    <p>
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
