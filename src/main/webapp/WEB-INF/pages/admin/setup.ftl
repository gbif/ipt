[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header_setup.ftl"]
[#include "/WEB-INF/pages/macros/forms.ftl"]

<form action="setup.do" method="post" class="needs-validation" novalidate>
    <div class="container-fluid bg-body border-bottom">
        <div class="container my-3">
            [#include "/WEB-INF/pages/inc/action_alerts.ftl"]
        </div>

        <div class="container my-3 p-3">
            <div class="text-center">
                <div class="text-center text-uppercase fw-bold fs-smaller-2">
                    <span>[@s.text name="admin.config.setup.common.setup"/] I</span>
                </div>

                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    [@s.text name="admin.config.setup.common.ipt"/]
                </h1>
                <h1 class="pb-2 mb-0 text-gbif-header-light fs-5 fw-normal">
                    [@s.text name="admin.config.setup.common.tagline"/]
                </h1>

                <div class="mt-2">
                    [@s.submit cssClass="btn btn-sm btn-outline-gbif-primary top-button" name="save" id="submitDatadir" key="button.save"/]
                </div>
            </div>
        </div>
    </div>

    <main class="container">
        <div class="my-3 p-3">
            <h5 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                [@s.text name="admin.config.setup.title"/]
            </h5>

            <p>[@s.text name="admin.config.setup.disclaimerPart1"/]</p>
            <p>[@s.text name="admin.config.setup.disclaimerPart2"/]</p>

            <div>
                [@checkbox name="readDisclaimer" value="readDisclaimer" i18nkey="admin.config.setup.read"/]
            </div>
        </div>

        <div class="my-3 p-3">
            <h5 class="pb-2 pt-2 text-gbif-header-2 fw-400">
                [@s.text name="admin.config.server.data.dir"/]
            </h5>
            <p>[@s.text name="admin.config.setup.welcome"/]</p>
            <p>[@s.text name="admin.config.setup.instructions"/]</p>
            <p>[@s.text name="admin.config.setup.examples"/]</p>

            <div class="row g-3">
                <div class="col-12">
                    [@input name="dataDirPath" type="text" i18nkey="admin.config.setup.datadir" /]
                </div>
            </div>
        </div>
    </main>
</form>
</div>


[#include "/WEB-INF/pages/inc/footer.ftl"]
