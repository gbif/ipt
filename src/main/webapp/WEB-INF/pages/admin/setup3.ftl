[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header_setup.ftl"]

<form action="setupComplete.do">
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
                    <span>[@s.text name="admin.config.setup.common.setup"/] III</span>
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

    <main class="container">
        <div class="my-3 p-3">
            [#if warnings?size == 0 && actionErrors?size == 0]
                <p class="text-center">[@s.text name="admin.config.setup3.welcome"/]</p>
            [#else]
                <p class="text-center">[@s.text name="admin.config.setup3.welcomeWithIssues"/]</p>
            [/#if]
        </div>
    </main>
</form>
</div>


[#include "/WEB-INF/pages/inc/footer.ftl"]
