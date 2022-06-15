<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="account.title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl">

<script>
    $(document).ready(function(){
        $("#edit-profile-radio").change(function () {
            if($('#edit-profile-radio').is(':checked')) {
                $('#change-password-block').hide();
                $('#change_password').hide();
                $('#edit-profile-block').show();
                $('#save').show();
            }
        });

        $("#change-password-radio").change(function () {
            if($('#change-password-radio').is(':checked')) {
                $('#edit-profile-block').hide();
                $('#save').hide();
                $('#change-password-block').show();
                $('#change_password').show();
            }
        });

        // in case of validation error for change-password switch to the proper view
        if (window.location.href.indexOf("change-password") > -1) {
            $('#edit-profile-block').hide();
            $('#save').hide();
            $('#change-password-radio').prop("checked", true);
            $('#change-password-block').show();
            $('#change_password').show();
        }
    });
</script>

<div class="container-fluid bg-body border-bottom">
    <div class="container my-3">
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
                <@s.submit form="profileData" cssClass="btn btn-sm btn-outline-gbif-primary top-button mt-1" name="save" key="button.save"/>
                <@s.submit form="changePassword" cssClass="btn btn-sm btn-outline-gbif-primary top-button mt-1" cssStyle="display: none;" name="change-password" key="button.save"/>
                <@s.submit form="profileData" cssClass="btn btn-sm btn-outline-secondary top-button mt-1" name="cancel" key="button.cancel"/>
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

    <div class="mb-lg-3 p-3">
        <div class="form-check form-check-inline">
            <input class="form-check-input" type="radio" name="metadata-radio" id="edit-profile-radio" value="edit" checked>
            <label class="form-check-label" for="edit-profile-radio"><@s.text name="account.profile.title"/></label>
        </div>
        <div class="form-check form-check-inline">
            <input class="form-check-input" type="radio" name="metadata-radio" id="change-password-radio" value="change-password">
            <label class="form-check-label" for="change-password-radio"><@s.text name="account.passwordChange.title"/></label>
        </div>
    </div>

    <div id="edit-profile-block" class="my-3 p-3">
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

    <div id="change-password-block" class="my-3 p-3" style="display: none;">
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
