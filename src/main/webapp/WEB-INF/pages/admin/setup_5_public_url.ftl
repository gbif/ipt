[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header_setup.ftl"]
[#include "/WEB-INF/pages/macros/forms.ftl"]
[#assign setupStepIndex = 4]

<form action="setupPublicUrl.do" method="post" class="needs-validation" novalidate>
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
                    <h5 class="text-gbif-header-2 pb-2 pt-2 fw-400">
                        [@s.text name="admin.config.setup2.publicURL.title"/]
                    </h5>

                    <input type="hidden" name="setupPublicUrl" value="true" />

                    <p>
                        [@s.text name="admin.config.setup2.publicURL.details"/]
                    </p>

                    <div class="row g-2">
                        <div class="col-md-6">
                            [@input name="baseURL" i18nkey="admin.config.baseUrl" requiredField=true /]
                        </div>
                    </div>

                </div>

                <div class="mt-3 p-3">
                    <h5 class="text-gbif-header-2 pb-2 pt-2 fw-400">
                        [@s.text name="admin.config.setup2.forwardProxyURL.title"/]
                    </h5>

                    <input type="hidden" name="setupProxyUrl" value="true" />

                    <p>
                        [@s.text name="admin.config.setup2.forwardProxyURL.details"/]
                    </p>

                    <div class="row g-2">
                        <div class="col-md-6">
                            [@input name="proxy" i18nkey="admin.config.proxy" /]
                        </div>
                    </div>

                </div>
            </div>
        </main>
    </div>

</form>
</div>


[#include "/WEB-INF/pages/inc/footer.ftl"]
