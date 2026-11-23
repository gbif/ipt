<#ftl output_format="HTML">
<#-- @ftlvariable name="" type="org.gbif.ipt.action.AccountAction" -->
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<#include "/WEB-INF/pages/inc/header.ftl">
<link rel="stylesheet" href="${baseURL}/styles/page-heading.css">
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/users.css">
<link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
<title><@s.text name="account.title"/></title>
<#assign currentMenu = "account"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl">

<#assign roleKey = user.role?lower_case/>

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<div class="container-fluid bg-body border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3 ov-head ov-head-left">
            <div class="ov-grow">
                <div class="fs-smaller">
                    <span><@s.text name="menu.account"/></span>
                </div>

                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal">${user.name!user.email}</h1>

                <div class="ov-meta">
                    <span class="fs-smaller-2 text-nowrap dt-content-pill user-${roleKey}"><@s.text name="user.roles.${roleKey}"/></span>
                    <span>${user.email}</span>
                </div>
            </div>

            <div class="ov-acts">
                <#-- Save is for the names only; the password has its own button in its card -->
                <button type="submit" id="save" name="save" value="save" form="profileData" class="btn btn-sm btn-gbif-solid top-button"><@icon name="save"/> <@s.text name="button.save"/></button>
                <a href="${baseURL}/home.do" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="button.back"/></a>
            </div>
        </div>
    </div>
</div>

<main class="container main-content-container mb-5">
    <div class="my-3 us-page">
        <section class="ov-card">
            <div class="ov-ch"><h2><@s.text name="admin.user.section.account"/></h2></div>
            <form id="profileData" class="needs-validation" action="account.do" method="post" novalidate>
                <div class="row g-3 mt-0">
                    <div class="col-md-6">
                        <@input name="user.firstname" value=user.firstname! requiredField=true />
                    </div>
                    <div class="col-md-6">
                        <@input name="user.lastname" value=user.lastname! requiredField=true />
                    </div>
                    <div class="col-md-6">
                        <@input name="user.email" value=user.email! disabled=true />
                    </div>
                    <div class="col-12">
                        <div class="ov-banner info compact">
                            <span class="ov-ic"><@icon name="info_outlined"/></span>
                            <div class="ov-grow"><@s.text name="account.email.note"/></div>
                        </div>
                    </div>
                </div>
            </form>
        </section>

        <section class="ov-card">
            <div class="ov-ch"><h2><@s.text name="user.role"/></h2></div>
            <div class="us-role us-role-static">
                <span>
                    <span class="us-role-name"><@s.text name="user.roles.${roleKey}"/></span>
                    <span class="us-role-desc"><@s.text name="admin.user.role.${roleKey}.desc"/></span>
                </span>
            </div>
            <p class="ov-sub mt-2 mb-0"><@s.text name="account.role.note"/></p>
        </section>

        <section class="ov-card">
            <div class="ov-ch"><h2><@s.text name="user.password"/></h2></div>
            <form id="changePassword" class="needs-validation" action="change-password.do" method="post" novalidate>
                <div class="row g-3 mt-0">
                    <div class="col-md-6">
                        <@input name="currentPassword" i18nkey="user.password.current" type="password" requiredField=true />
                    </div>
                    <div class="w-100 m-0"></div>
                    <div class="col-md-6">
                        <@input name="newPassword" i18nkey="user.password.new" type="password" requiredField=true />
                    </div>
                    <div class="col-md-6">
                        <@input name="password2" i18nkey="user.password2" type="password" requiredField=true />
                    </div>
                    <div class="col-12">
                        <button type="submit" id="change_password" name="change-password" value="change-password" class="btn btn-sm btn-outline-gbif-primary"><@s.text name="button.changePassword"/></button>
                    </div>
                </div>
            </form>
        </section>
    </div>
</main>

<script>
    // a refused password change comes back at the top of the page: show the card with the error
    $(function () {
        var invalid = document.querySelector("#changePassword .is-invalid");
        if (invalid) {
            invalid.closest(".ov-card").scrollIntoView({block: "center"});
            invalid.focus({preventScroll: true});
        }
    });
</script>

<#include "/WEB-INF/pages/inc/footer.ftl">
