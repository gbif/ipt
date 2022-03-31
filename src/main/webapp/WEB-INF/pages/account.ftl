<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="account.title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl">

<div class="container-fluid bg-body border-bottom">
    <div class="container">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container my-3 p-3">
        <div class="text-center text-uppercase fw-bold fs-smaller-2">
            <@s.text name="menu.account"/>
        </div>

        <div class="text-center">
            <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                <@s.text name="account.title"/>
            </h1>

            <div class="mt-2">
                <@s.submit form="profileData" cssClass="btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save"/>
                <@s.submit form="changePassword" cssClass="btn btn-sm btn-outline-gbif-primary top-button" name="change-password" key="button.changePassword"/>
                <@s.submit form="profileData" cssClass="btn btn-sm btn-outline-secondary top-button" name="cancel" key="button.cancel"/>
            </div>
        </div>
    </div>
</div>

<main class="container">
    <div class="my-3 p-3">
        <p>
            <@s.text name="account.intro"/>
        </p>

        <p>
            <@s.text name="account.email.cantChange"/>
        </p>
    </div>

    <div class="my-3 p-3">
        <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fs-5 fw-400">
            <@s.text name="account.profile.title"/>
        </h4>
        <form id="profileData" class="needs-validation" action="account.do" method="post" novalidate>
            <input type="hidden" name="id" value="${email!}" required>

            <div class="row g-3 mt-0 mb-2">
                <div class="col-sm-6">
                    <@input name="user.email" disabled=true />
                </div>

                <#assign val><@s.text name="user.roles.${user.role?lower_case}"/></#assign>

                <div class="col-sm-6">
                    <@readonly name="role" i18nkey="user.role" value=val />
                </div>

                <div class="col-sm-6">
                    <@input name="user.firstname" />
                </div>

                <div class="col-sm-6">
                    <@input name="user.lastname" />
                </div>
            </div>
        </form>
    </div>

    <div class="my-3 p-3">
        <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fs-5 fw-400">
            <@s.text name="account.passwordChange.title"/>
        </h4>
        <form id="changePassword" class="needs-validation" action="change-password.do" method="post" novalidate>
            <input type="hidden" name="id" value="${email!}" required>

            <div class="row g-3 mt-0 mb-2">
                <div class="col-sm-6">
                    <@input name="newPassword" i18nkey="user.password.new" type="password"/>
                </div>

                <div class="col-sm-6">
                    <@input name="password2" i18nkey="user.password2" type="password"/>
                </div>

                <div class="col-sm-6">
                    <@input name="currentPassword" i18nkey="user.password.current" type="password"/>
                </div>
            </div>
        </form>
    </div>

</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
