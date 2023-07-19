[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header_setup.ftl"]
[#include "/WEB-INF/pages/macros/forms.ftl"]
[#assign setupStepIndex = 2]

<form action="setupDefaultAdministrator.do" method="post" class="needs-validation" novalidate>
    <div class="container-fluid bg-body border-bottom">
        <div class="container my-3">
            [#include "/WEB-INF/pages/inc/action_alerts.ftl"]
        </div>

        <div class="container my-3 p-3">
            <div class="text-center">
                <div class="text-center text-uppercase fw-bold fs-smaller-2">
                    <span>[@s.text name="admin.config.setup.common.setup"/]</span>
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

    <div class="container bd-layout">
        <main class="bd-main">
            <div class="bd-toc py-3">
                [#include "/WEB-INF/pages/inc/setup_sidebar.ftl"]
            </div>

            <div class="bd-content">
                <div class="my-3 p-3">
                    <h5 class="pb-2 mb-2 mt-2 text-gbif-header-2 fw-400">
                        [@s.text name="admin.config.setup2.administrator.title"/]
                    </h5>

                    <p>[@s.text name="admin.config.setup2.welcome"/]</p>

                    <input type="hidden" name="setupDefaultAdministrator" value="true" />

                    <p>
                        [@s.text name="admin.config.setup2.administrator"/]. [@s.text name="admin.config.setup2.administrator.help"/]
                    </p>
                    <div class="row g-3 pb-3 mb-2">
                        <div class="col-md-4">
                            [@input name="user.firstname" requiredField=true /]
                        </div>

                        <div class="col-md-4">
                            [@input name="user.lastname" requiredField=true /]
                        </div>

                        <div class="col-md-4">
                            [@input name="user.email" requiredField=true /]
                        </div>

                        <div class="col-md-6">
                            [@input name="user.password" type="password" requiredField=true /]
                        </div>

                        <div class="col-md-6">
                            [@input name="password2" i18nkey="user.password2" type="password" requiredField=true /]
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>

</form>
</div>


[#include "/WEB-INF/pages/inc/footer.ftl"]
