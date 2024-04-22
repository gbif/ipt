<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="account.title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl">

<script>
    $(document).ready(function(){
        function displayProfileView() {
            $('#change-password-block').hide();
            $('#change_password').hide();
            $('#edit-profile-block').show();
            $('#save').show();
        }

        function displayPasswordView() {
            $('#edit-profile-block').hide();
            $('#save').hide();
            $('#change-password-block').show();
            $('#change_password').show();
        }

        $(".default-button-tab-root").click(function (event) {
            var selectedTab = $(this);
            var selectedTabId = selectedTab[0].id;

            // remove "selected" from all tabs
            $(".default-button-tab-root").removeClass("tab-selected");
            // hide all indicators
            $(".tabs-indicator").hide();
            // add "selected" to clicked tab
            selectedTab.addClass("tab-selected");
            // show indicator for this tab
            $("#" + selectedTabId + " .tabs-indicator").show();

            if (selectedTabId === 'tab-profile') {
                displayProfileView();
            } else {
                displayPasswordView();
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

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<div class="container-fluid bg-body border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3">
            <div class="text-center fs-smaller">
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
</div>

<main class="container main-content-container">
    <div class="mt-3 p-3">
        <p>
            <@s.text name="account.intro"/>
        </p>

        <p class="mb-0">
            <@s.text name="account.email.cantChange"/>
        </p>
    </div>

    <div class="p-3">
        <div class="tabs-root">
            <div class="tabs-scroller tabs-fixed" style="overflow:hidden;margin-bottom:0">
                <div class="tabs-flexContainer justify-content-start" role="tablist">
                    <button id="tab-profile" class="default-button-tab-root tab-selected" type="button" role="tab">
                        <@s.text name="account.profile.title"/>
                        <span id="tab-indicator-profile" class="tabs-indicator"></span>
                    </button>
                    <button id="tab-password" class="default-button-tab-root" type="button" role="tab">
                        <@s.text name="account.passwordChange.title"/>
                        <span id="tab-indicator-password" class="tabs-indicator" style="display: none;"></span>
                    </button>
                </div>
            </div>
        </div>
    </div>

    <div id="edit-profile-block" class="p-3">
        <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fs-5 fw-400">
            <@s.text name="account.profile.title"/>
        </h4>
        <form id="profileData" class="needs-validation" action="account.do" method="post" novalidate>
            <input type="hidden" name="id" value="${email!}" required>

            <div class="row g-3 mt-0 mb-2">
                <div class="col-sm-6">
                    <@input name="user.email" disabled=true requiredField=true />
                </div>

                <#assign val><@s.text name="user.roles.${user.role?lower_case}"/></#assign>

                <div class="col-sm-6">
                    <@readonly name="role" i18nkey="user.role" value=val requiredField=true />
                </div>

                <div class="col-sm-6">
                    <@input name="user.firstname" requiredField=true />
                </div>

                <div class="col-sm-6">
                    <@input name="user.lastname" requiredField=true />
                </div>
            </div>
        </form>
    </div>

    <div id="change-password-block" class="p-3" style="display: none;">
        <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fs-5 fw-400">
            <@s.text name="account.passwordChange.title"/>
        </h4>
        <form id="changePassword" class="needs-validation" action="change-password.do" method="post" novalidate>
            <input type="hidden" name="id" value="${email!}" required>

            <div class="row g-3 mt-0 mb-2">
                <div class="col-sm-6">
                    <@input name="newPassword" i18nkey="user.password.new" type="password" requiredField=true />
                </div>

                <div class="col-sm-6">
                    <@input name="password2" i18nkey="user.password2" type="password" requiredField=true />
                </div>

                <div class="col-sm-6">
                    <@input name="currentPassword" i18nkey="user.password.current" type="password" requiredField=true />
                </div>
            </div>
        </form>
    </div>

</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
