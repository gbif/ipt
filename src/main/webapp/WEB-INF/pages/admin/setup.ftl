[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header_setup-bootstrap.ftl"]
[#include "/WEB-INF/pages/macros/forms-bootstrap.ftl"]

<main class="container">

    <form action="setup.do" method="post" class="needs-validation" novalidate>
        <div class="my-3 p-3 bg-body rounded shadow-sm">

            [#include "/WEB-INF/pages/inc/action_alerts-bootstrap.ftl"]

            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-primary text-center">
                [@s.text name="admin.config.setup.title"/]
            </h5>

            <h5 class="text-gbif-primary mx-md-4 mx-2">
                [@s.text name="admin.config.setup.disclaimer.title"/]
            </h5>
            <p class="text-muted mx-md-4 mx-2">[@s.text name="admin.config.setup.disclaimerPart1"/]</p>
            <p class="text-muted mx-md-4 mx-2">[@s.text name="admin.config.setup.disclaimerPart2"/]</p>

            <div class="mx-md-4 mx-2">
                [@checkbox name="readDisclaimer" value="readDisclaimer" i18nkey="admin.config.setup.read"/]
            </div>
        </div>

        <div class="my-3 p-3 bg-body rounded shadow-sm">
            <h5 class="text-gbif-primary pb-2 mx-md-4 mx-2 pt-2 border-bottom">
                [@s.text name="admin.config.server.data.dir"/]
            </h5>
            <p class="text-muted mx-md-4 mx-2">[@s.text name="admin.config.setup.welcome"/]</p>
            <p class="text-muted mx-md-4 mx-2">[@s.text name="admin.config.setup.instructions"/]</p>
            <p class="text-muted mx-md-4 mx-2">[@s.text name="admin.config.setup.examples"/]</p>

            <div class="row g-3 mx-md-3 mx-1">
                <div class="col-12">
                    [@input name="dataDirPath" type="text" i18nkey="admin.config.setup.datadir" /]
                </div>

                <div class="col-12">
                    [@s.submit cssClass="btn btn-outline-gbif-primary" name="save" id="submitDatadir" key="button.save"/]
                </div>
            </div>

        </div>
    </form>
</main>
</div>


[#include "/WEB-INF/pages/inc/footer-bootstrap.ftl"]
