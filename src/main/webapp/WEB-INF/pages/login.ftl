<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="login.title"/></title>
<#assign currentMenu = "account"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<#-- the line naming the administrator, or saying there is none, under "Forgot your password?" and at the bottom -->
<#macro adminContact>
    <#if adminEmail?has_content>
        <@s.text name="login.admin.address"><@s.param><a class="login-admin-address" href="mailto:${adminEmail}">${adminEmail}</a></@s.param></@s.text>
    <#else>
        <@s.text name="login.admin.none"/>
    </#if>
</#macro>

<main class="container login-container">
    <div class="login-card my-3 border rounded shadow-sm">
        <div class="login-logo">
            <svg id="login-page-logo" xmlns="http://www.w3.org/2000/svg" viewBox="90 239.1 539.7 523.9" aria-hidden="true">
                <path class="ipt-icon-piece" d="M325.5,495.4c0-89.7,43.8-167.4,174.2-167.4C499.6,417.9,440.5,495.4,325.5,495.4"/>
                <path class="ipt-icon-piece" d="M534.3,731c24.4,0,43.2-3.5,62.4-10.5c0-71-42.4-121.8-117.2-158.4c-57.2-28.7-127.7-43.6-192.1-43.6c28.2-84.6,7.6-189.7-19.7-247.4c-30.3,60.4-49.2,164-20.1,248.3c-57.1,4.2-102.4,29.1-121.6,61.9c-1.4,2.5-4.4,7.8-2.6,8.8c1.4,0.7,3.6-1.5,4.9-2.7c20.6-19.1,47.9-28.4,74.2-28.4c60.7,0,103.4,50.3,133.7,80.5C401.3,704.3,464.8,731.2,534.3,731"/>
            </svg>
            <img id="login-page-logo-custom" src="${baseURL}/appLogo.do" alt="" onerror="handleCustomLogoError()" />
        </div>

        <#-- the page needs a heading for screen readers; on screen the brand and the Log in button say enough -->
        <h1 class="visually-hidden"><@s.text name="login.title"/></h1>
        <div class="login-site-name text-uppercase">Integrated Publishing Toolkit</div>
        <p class="login-sub"><@s.text name="admin.config.setup.common.tagline"/></p>

        <p class="login-intro"><@s.text name="login.intro.email"/></p>

        <#if continueToKey??>
            <p class="login-continue"><@s.text name="login.continueTo"><@s.param><@s.text name=continueToKey/></@s.param></@s.text></p>
        </#if>

        <div class="text-smaller" role="alert">
            <#include "/WEB-INF/pages/inc/action_alerts_messages.ftl">
            <#include "/WEB-INF/pages/inc/action_alerts_errors.ftl">
        </div>

        <div id="login-forgot-info" class="alert alert-info text-smaller" hidden>
            <strong class="d-block"><@s.text name="login.forgot"/></strong>
            <@s.text name="login.forgot.text"/> <@adminContact/>
        </div>

        <form id="login-form" action="${baseURL}/login.do" method="post" novalidate>
            <input type="hidden" name="csrfToken" value="${loginToken}">

            <div class="mb-3">
                <label for="email" class="form-label"><@s.text name="user.email"/></label>
                <#-- type="text", not "email": authenticate() also accepts "admin" when there is a single admin -->
                <input class="form-control" type="text" inputmode="email" id="email" name="email" value="${email!}"
                       autocomplete="username" autocapitalize="off" spellcheck="false" aria-describedby="email-feedback"
                       <#if !email?has_content>autofocus</#if>>
                <div id="email-feedback" class="invalid-feedback"><@s.text name="login.email.required"/></div>
            </div>

            <div class="mb-3">
                <div class="d-flex justify-content-between align-items-baseline gap-2">
                    <label for="password" class="form-label"><@s.text name="user.password"/></label>
                    <a href="#login-forgot-info" id="login-forgot" class="text-smaller" aria-controls="login-forgot-info" aria-expanded="false"><@s.text name="login.forgot"/></a>
                </div>
                <#-- the password is never written back into the page -->
                <div class="input-group has-validation">
                    <input class="form-control" type="password" id="password" name="password"
                           autocomplete="current-password" aria-describedby="password-feedback password-caps"
                           <#if email?has_content>autofocus</#if>>
                    <button type="button" id="login-show-password" class="btn btn-outline-secondary" aria-controls="password" aria-pressed="false"
                            data-show-text="<@s.text name="login.password.show"/>" data-hide-text="<@s.text name="login.password.hide"/>"><@s.text name="login.password.show"/></button>
                    <div id="password-feedback" class="invalid-feedback"><@s.text name="login.password.required"/></div>
                </div>
                <div id="password-caps" class="form-text login-caps" hidden><@s.text name="login.capsLock"/></div>
            </div>

            <button type="submit" class="btn btn-gbif-primary w-100" name="login"><@s.text name="login.submit"/></button>
        </form>

        <p class="login-foot">
            <@s.text name="login.noAccount"/> <@adminContact/>
        </p>
    </div>
</main>

<script>
    (function () {
        var form = document.getElementById("login-form");
        var email = document.getElementById("email");
        var password = document.getElementById("password");

        // empty fields are flagged under the field, and the first one gets the focus
        form.addEventListener("submit", function (e) {
            var emptyEmail = !email.value.trim(), emptyPassword = !password.value;
            email.classList.toggle("is-invalid", emptyEmail);
            password.classList.toggle("is-invalid", emptyPassword);
            if (emptyEmail || emptyPassword) {
                e.preventDefault();
                (emptyEmail ? email : password).focus();
            }
        });
        [email, password].forEach(function (input) {
            input.addEventListener("input", function () { input.classList.remove("is-invalid"); });
        });

        // "Forgot your password?" shows who can set a new one
        var forgot = document.getElementById("login-forgot");
        var forgotInfo = document.getElementById("login-forgot-info");
        forgot.addEventListener("click", function (e) {
            e.preventDefault();
            forgotInfo.hidden = !forgotInfo.hidden;
            forgot.setAttribute("aria-expanded", String(!forgotInfo.hidden));
        });

        // show or hide the password
        var show = document.getElementById("login-show-password");
        show.addEventListener("click", function () {
            var visible = password.type === "password";
            password.type = visible ? "text" : "password";
            show.textContent = visible ? show.dataset.hideText : show.dataset.showText;
            show.setAttribute("aria-pressed", String(visible));
            password.focus();
        });

        // Caps Lock warning while typing the password
        var caps = document.getElementById("password-caps");
        function checkCaps(e) {
            if (e.getModifierState) {
                caps.hidden = !e.getModifierState("CapsLock");
            }
        }
        password.addEventListener("keydown", checkCaps);
        password.addEventListener("keyup", checkCaps);
        password.addEventListener("blur", function () { caps.hidden = true; });
    })();
</script>

<#include "/WEB-INF/pages/inc/footer.ftl">
