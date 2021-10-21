<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="account.title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl">

<main class="container">
    <div class="my-3 p-3 bg-body rounded shadow-sm">

        <#include "/WEB-INF/pages/inc/action_alerts.ftl">

        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header text-center">
            <@s.text name="account.title"/>
        </h5>

        <p class="mx-md-4 mx-2">
            <@s.text name="account.intro"/>
        </p>

        <p class="mx-md-4 mx-2">
            <@s.text name="account.email.cantChange"/>
        </p>
    </div>

    <div class="my-3 p-3 bg-body rounded shadow-sm">
        <form class="needs-validation" action="account.do" method="post" novalidate>
            <input type="hidden" name="id" value="${user.email!}" required>

            <div class="row g-3 mx-md-4 mx-2 mt-0 mb-2">
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

                <div class="col-12">
                    <@s.submit cssClass="btn btn-outline-gbif-primary" name="save" key="button.save"/>
                    <@s.submit cssClass="btn btn-outline-secondary" name="cancel" key="button.cancel"/>
                </div>
            </div>
        </form>
    </div>

    <div class="my-3 p-3 bg-body rounded shadow-sm">
        <form class="needs-validation" action="change-password.do" method="post" novalidate>
            <input type="hidden" name="id" value="${user.email!}" required>

            <div class="row g-3 mx-md-4 mx-2 mt-0 mb-2">
                <div class="col-sm-6">
                    <@input name="newPassword" i18nkey="user.password.new" type="password"/>
                </div>

                <div class="col-sm-6">
                    <@input name="password2" i18nkey="user.password2" type="password"/>
                </div>

                <div class="col-sm-6">
                    <@input name="currentPassword" i18nkey="user.password.current" type="password"/>
                </div>

                <div class="col-12">
                    <@s.submit cssClass="btn btn-outline-gbif-primary" name="change-password" key="button.changePassword"/>
                </div>
            </div>
        </form>
    </div>

</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
