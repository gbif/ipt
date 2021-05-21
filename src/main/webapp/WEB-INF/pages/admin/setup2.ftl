[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header_setup.ftl"]
[#include "/WEB-INF/pages/macros/forms.ftl"]

<main class="container">

    <form action="setup2.do" method="post" class="needs-validation">
        <div class="my-3 p-3 bg-body rounded shadow-sm">

            [#include "/WEB-INF/pages/inc/action_alerts.ftl"]

            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 mt-2 text-gbif-header text-center">
                [@s.text name="admin.config.setup2.title"/]
            </h5>

            <p class="mx-md-4 mx-2">[@s.text name="admin.config.setup2.welcome"/]</p>

            <input type="hidden" name="setup2" value="true" />
            <input type="hidden" name="ignoreUserValidation" value="${ignoreUserValidation}" />

            <h5 class="text-gbif-header mx-md-4 mx-2">
                [@s.text name="admin.config.setup2.administrator"/]
            </h5>
            <p class="mx-md-4 mx-2">
                [@s.text name="admin.config.setup2.administrator.help"/]
            </p>
            <div class="row g-3 mx-md-4 mx-2 pb-3 mb-2">
                <div class="col-12">
                    [@input name="user.email" disabled=(ignoreUserValidation==1) /]
                </div>

                <div class="col-md-6">
                    [@input name="user.firstname" disabled=(ignoreUserValidation==1) /]
                </div>

                <div class="col-md-6">
                    [@input name="user.lastname" disabled=(ignoreUserValidation==1) /]
                </div>

                <div class="col-md-6">
                    [@input name="user.password" type="password" disabled=(ignoreUserValidation==1) /]
                </div>

                <div class="col-md-6">
                    [@input name="password2" i18nkey="user.password2" type="password" disabled=(ignoreUserValidation==1) /]
                </div>
            </div>
        </div>

        <div class="my-3 p-3 bg-body rounded shadow-sm">
            <h5 class="text-gbif-header pb-2 mt-2 border-bottom mx-md-4 mx-2">
                [@s.text name="admin.config.setup2.mode.title"/]
            </h5>

            <p class="mx-md-4 mx-2">[@s.text name="admin.config.setup2.mode.help"/]</p>
            <p class="mx-md-4 mx-2">[@s.text name="admin.config.setup2.mode.test"/]</p>
            <p class="mx-md-4 mx-2">[@s.text name="admin.config.setup2.mode.production"/]</p>
            <p class="mx-md-4 mx-2">[@s.text name="admin.config.setup2.mode"/]</p>

            [#list modes as mode]
                <div class="form-check form-check-inline mx-md-4 mx-2 pb-2">
                    <input class="form-check-input" type="radio" name="modeSelected" id="mode${mode}" aria-describedby="field-error-mode" [#if mode??]value="${mode}"[/#if] [#if modeSelected?? && mode == modeSelected] checked [/#if]>
                    <label class="form-check-label" for="mode${mode}">
                        ${mode}
                    </label>
                </div>
            [/#list]
            [@s.fielderror cssClass="invalid-feedback list-unstyled radio-error radio-name-modeSelected mx-md-4 mx-2 my-0" fieldName="modeSelected"/]

        </div>

        <div class="my-3 p-3 bg-body rounded shadow-sm">
            <h5 class="text-gbif-header pb-2 mx-md-4 mx-2 pt-2 border-bottom">
                [@s.text name="admin.config.setup2.publicURL.title"/]
            </h5>

            <p class="mx-md-4 mx-2">
                [@s.text name="admin.config.setup2.publicURL.details"/]
            </p>

            <div class="row g-2 mx-md-3 mx-1">
                <div class="col-md-6">
                    [@input name="baseURL" help="i18n" i18nkey="admin.config.baseUrl"/]
                </div>
            </div>

        </div>

        <div class="my-3 p-3 bg-body rounded shadow-sm">
            <h5 class="text-gbif-header pb-2 mx-md-4 mx-2 pt-2 border-bottom">
                [@s.text name="admin.config.setup2.forwardProxyURL.title"/]
            </h5>

            <p class="mx-md-4 mx-2">
                [@s.text name="admin.config.setup2.forwardProxyURL.details"/]
            </p>

            <div class="row g-2 mx-md-3 mx-1">
                <div class="col-md-6">
                    [@input name="proxy" help="i18n" i18nkey="admin.config.proxy" /]
                </div>

                <div class="col-12">
                    [@s.submit cssClass="btn btn-outline-gbif-primary" name="save" key="button.save"/]
                </div>
            </div>

        </div>
    </form>
</main>
</div>


[#include "/WEB-INF/pages/inc/footer.ftl"]
