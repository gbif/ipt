[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header_setup.ftl"]
[#include "/WEB-INF/pages/macros/forms.ftl"]
[#assign setupStepIndex = 4]

<form action="setupComplete.do" method="post" class="needs-validation" novalidate>
    <div class="container-fluid bg-body border-bottom">
        <div class="container my-3">
            [#if warnings?size == 0 && actionErrors?size == 0]
                [#include "/WEB-INF/pages/inc/action_alerts.ftl"]
            [#else]
                [#include "/WEB-INF/pages/inc/action_alerts_warnings.ftl"]
                [#include "/WEB-INF/pages/inc/action_alerts_errors.ftl"]
            [/#if]
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
                    [@s.submit cssClass="btn btn-sm btn-outline-gbif-primary top-button" name="continue" key="button.continue"/]
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
                    [#if warnings?size == 0 && actionErrors?size == 0]
                        <p>[@s.text name="admin.config.setup3.welcome"/]</p>
                    [#else]
                        <p>[@s.text name="admin.config.setup3.welcomeWithIssues"/]</p>
                    [/#if]
                </div>
            </div>
        </main>
    </div>

</form>
</div>


[#include "/WEB-INF/pages/inc/footer.ftl"]
