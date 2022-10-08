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
            <div class="col-12 p-0">
                <span>
                    <svg id="login-page-logo" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
                         viewBox="90 239.1 539.7 523.9" width="16px" height="16px" style="enable-background:new 90 239.1 539.7 523.9;" xml:space="preserve">
                        <path class="ipt-icon-piece" d="M325.5,495.4c0-89.7,43.8-167.4,174.2-167.4C499.6,417.9,440.5,495.4,325.5,495.4"/>
                        <path class="ipt-icon-piece" d="M534.3,731c24.4,0,43.2-3.5,62.4-10.5c0-71-42.4-121.8-117.2-158.4c-57.2-28.7-127.7-43.6-192.1-43.6c28.2-84.6,7.6-189.7-19.7-247.4c-30.3,60.4-49.2,164-20.1,248.3c-57.1,4.2-102.4,29.1-121.6,61.9c-1.4,2.5-4.4,7.8-2.6,8.8c1.4,0.7,3.6-1.5,4.9-2.7c20.6-19.1,47.9-28.4,74.2-28.4c60.7,0,103.4,50.3,133.7,80.5C401.3,704.3,464.8,731.2,534.3,731"/>
                    </svg>
                    <img id="login-page-logo-custom" src="${baseURL}/appLogo.do" onerror="handleCustomLogoError()" />
                </span>
            </div>
            <div class="col-12 p-0 pt-1">
                <div class="fs-6 fw-400 text-uppercase site-name">
                    Integrated Publishing Toolkit
                </div>
                <div class="text-smaller" style="opacity: 0.75;">
                    <@s.text name="admin.config.setup.common.tagline"/>
                </div>
            </div>
        </div>

        <p class="text-center text-smaller mt-5 mb-4">
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
                    <p class="text-center text-smaller pt-3">
                        <@s.text name="login.forgottenpassword"><@s.param>${adminEmail}</@s.param></@s.text>
                    </p>
                </#if>

                <div class="col-12">
                    <@s.submit cssClass="btn btn-outline-gbif-primary w-100 text-capitalize" name="login" key="portal.login"/>
                </div>
            </div>
        </form>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
