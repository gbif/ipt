[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header_setup.ftl"]

<form action="setupComplete.do">
    <div class="container-fluid bg-body border-bottom">
        <div class="container">
            [#include "/WEB-INF/pages/inc/action_alerts.ftl"]
        </div>

        <div class="container my-3 p-3">
            <div class="text-center">
                <div class="text-center text-uppercase fw-bold fs-smaller-2">
                    <span>[@s.text name="admin.config.setup.common.setup"/] III</span>
                </div>

                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    [@s.text name="admin.config.setup.common.ipt"/]
                </h1>
                <h1 class="pb-2 mb-0 text-gbif-header fs-5 fw-normal">
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
[#--            <h5 class="pb-2 mb-2 pt-2 text-gbif-header fw-400 text-center">--]
[#--                [@s.text name="admin.config.setup3.title"/]--]
[#--            </h5>--]

            <p class="text-center">[@s.text name="admin.config.setup3.welcome"/]</p>
        </div>
    </main>
</form>
</div>


[#include "/WEB-INF/pages/inc/footer.ftl"]
