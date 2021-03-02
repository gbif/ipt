[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header_setup-bootstrap.ftl"]

<main class="container pt-5">

    <form action="setup.do" method="post" class="needs-validation" novalidate>
        <div class="my-3 p-3 bg-body rounded shadow-sm">

            [#include "/WEB-INF/pages/inc/action_alerts-bootstrap.ftl"]

            <h4 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-success text-center">
                [@s.text name="admin.config.setup.title"/]
            </h4>

            <h5 class="text-success mx-md-4 mx-2">
                [@s.text name="admin.config.setup.disclaimer.title"/]
            </h5>
            <p class="text-muted mx-md-4 mx-2">[@s.text name="admin.config.setup.disclaimerPart1"/]</p>
            <p class="text-muted mx-md-4 mx-2">[@s.text name="admin.config.setup.disclaimerPart2"/]</p>

            <div class="form-check text-muted mx-md-4 mx-2 pb-2">
                [@s.checkbox name="readDisclaimer" value="readDisclaimer" cssClass="form-check-input" id="readDisclaimer"/]
                <label class="form-check-label" for="readDisclaimer">
                    [@s.text name="admin.config.setup.read"/]
                </label>
                [@s.fielderror id="field-error-readDisclaimer" cssClass="invalid-feedback list-unstyled field-error"]
                    [@s.param value="%{'readDisclaimer'}"/]
                [/@s.fielderror]
            </div>
        </div>

        <div class="my-3 p-3 bg-body rounded shadow-sm">
            <h5 class="text-success pb-2 mx-md-4 mx-2 pt-2 border-bottom">
                [@s.text name="admin.config.server.data.dir"/]
            </h5>
            <p class="text-muted mx-md-4 mx-2">[@s.text name="admin.config.setup.welcome"/]</p>
            <p class="text-muted mx-md-4 mx-2">[@s.text name="admin.config.setup.instructions"/]</p>
            <p class="text-muted mx-md-4 mx-2">[@s.text name="admin.config.setup.examples"/]</p>

            <div class="row mx-md-3 mx-1">
                <div class="input-group mb-3">
                    <input type="text" class="form-control" id="dataDirPath" name="dataDirPath" aria-label="datadir" aria-describedby="submitDatadir" [#if dataDirPath??]value="${dataDirPath}"[/#if] required>
                    [@s.submit cssClass="btn btn-outline-success" name="save" id="submitDatadir" key="button.save"/]
                    <div class="invalid-feedback">
                        [@s.text name="validation.required"]
                            [@s.param][@s.text name="validation.field.required"/][/@s.param]
                        [/@s.text]
                    </div>
                </div>
            </div>

        </div>
    </form>
</main>
</div>


[#include "/WEB-INF/pages/inc/footer-bootstrap.ftl"]
