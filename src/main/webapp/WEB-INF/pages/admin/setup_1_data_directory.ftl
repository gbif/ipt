[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header_setup.ftl"]
[#include "/WEB-INF/pages/macros/forms.ftl"]
[#assign setupStepIndex = 0]

<div class="container px-0">
    [#include "/WEB-INF/pages/inc/action_alerts.ftl"]
</div>

<form action="setupDataDirectory.do" method="post" class="needs-validation" novalidate>
    <div class="container-fluid bg-body border-bottom">
        <div class="container bg-body border rounded-2 mb-4">
            <div class="container my-3 p-3">
                <div class="text-center">
                    <div class="text-center fs-smaller">
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
    </div>

    <div class="container bd-layout main-content-container">
        <main class="bd-main">
            <div class="bd-toc py-3">
                [#include "/WEB-INF/pages/inc/setup_sidebar.ftl"]
            </div>

            <div class="bd-content">
                <div class="my-3 p-3">
                    <h5 class="pb-2 pt-2 text-gbif-header-2 fw-400">
                        [@s.text name="admin.config.server.data.dir"/]
                    </h5>
                    <p>[@s.text name="admin.config.setup.welcome"/]</p>
                    <p>[@s.text name="admin.config.setup.instructions"/]</p>
                    <p>[@s.text name="admin.config.setup.examples"/]</p>

                    <div class="row g-3">
                        <div class="col-12">
                            [@input name="dataDirPath" type="text" i18nkey="admin.config.setup.datadir" requiredField=true /]
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>

</form>
</div>


[#include "/WEB-INF/pages/inc/footer.ftl"]
