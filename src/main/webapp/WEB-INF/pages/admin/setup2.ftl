[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header_setup.ftl"]
[#include "/WEB-INF/pages/macros/forms.ftl"]

<form action="setup2.do" method="post" class="needs-validation">

    <div class="container-fluid bg-body border-bottom">
        <div class="container my-3">
            [#include "/WEB-INF/pages/inc/action_alerts.ftl"]
        </div>

        <div class="container my-3 p-3">
            <div class="text-center">
                <div class="text-center text-uppercase fw-bold fs-smaller-2">
                    <span>[@s.text name="admin.config.setup.common.setup"/] II</span>
                </div>

                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    [@s.text name="admin.config.setup.common.ipt"/]
                </h1>
                <h1 class="pb-2 mb-0 text-gbif-header-light fs-5 fw-normal">
                    [@s.text name="admin.config.setup.common.tagline"/]
                </h1>

                <div class="mt-2">
                    [@s.submit cssClass="btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save"/]
                </div>
            </div>
        </div>
    </div>

    <main class="container">
        <div class="my-3 p-3">
            <h5 class="pb-2 mb-2 mt-2 text-gbif-header-2 fw-400">
                [@s.text name="admin.config.setup2.title"/]
            </h5>

            <p>[@s.text name="admin.config.setup2.welcome"/]</p>

            <input type="hidden" name="setup2" value="true" />
            <input type="hidden" name="ignoreUserValidation" value="${ignoreUserValidation}" />

            <p>
                [@s.text name="admin.config.setup2.administrator"/]. [@s.text name="admin.config.setup2.administrator.help"/]
            </p>
            <div class="row g-3 pb-3 mb-2">
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

        <div class="my-3 p-3">
            <h5 class="text-gbif-header pb-2 mt-2 fw-400">
                [@s.text name="admin.config.setup2.mode.title"/]
            </h5>

            <p>[@s.text name="admin.config.setup2.mode.help"/]</p>
            <p>[@s.text name="admin.config.setup2.mode.test"/]</p>
            <p>[@s.text name="admin.config.setup2.mode.production"/]</p>
            <p>[@s.text name="admin.config.setup2.mode"/]</p>

            [#list modes as mode]
                <div class="form-check form-check-inline pb-2">
                    <input class="form-check-input" type="radio" name="modeSelected" id="mode${mode}" aria-describedby="field-error-mode" [#if mode??]value="${mode}"[/#if] [#if modeSelected?? && mode == modeSelected] checked [/#if]>
                    <label class="form-check-label" for="mode${mode}">
                        ${mode}
                    </label>
                </div>
            [/#list]
            [@s.fielderror cssClass="invalid-feedback list-unstyled radio-error radio-name-modeSelected my-0" fieldName="modeSelected"/]

        </div>

        <div class="my-3 p-3">
            <h5 class="text-gbif-header-2 pb-2 pt-2 fw-400">
                [@s.text name="admin.config.setup2.publicURL.title"/]
            </h5>

            <p>
                [@s.text name="admin.config.setup2.publicURL.details"/]
            </p>

            <div class="row g-2">
                <div class="col-md-6">
                    [@input name="baseURL" i18nkey="admin.config.baseUrl"/]
                </div>
            </div>

        </div>

        <div class="my-3 p-3">
            <h5 class="text-gbif-header-2 pb-2 pt-2 fw-400">
                [@s.text name="admin.config.setup2.forwardProxyURL.title"/]
            </h5>

            <p>
                [@s.text name="admin.config.setup2.forwardProxyURL.details"/]
            </p>

            <div class="row g-2">
                <div class="col-md-6">
                    [@input name="proxy" i18nkey="admin.config.proxy" /]
                </div>
            </div>

        </div>
    </main>

</form>
</div>


[#include "/WEB-INF/pages/inc/footer.ftl"]
