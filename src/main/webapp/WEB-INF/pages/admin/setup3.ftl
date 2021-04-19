[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header_setup.ftl"]

<main class="container">

    <form action="setupComplete.do">
        <div class="my-3 p-3 bg-body rounded shadow-sm">

            [#include "/WEB-INF/pages/inc/action_alerts.ftl"]

            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header text-center">
                [@s.text name="admin.config.setup3.title"/]
            </h5>

            <p class="mx-md-4 mx-2 text-center">[@s.text name="admin.config.setup3.welcome"/]</p>

            <div class="row g-3 pb-2">
                <div class="col-12 d-flex justify-content-center">
                    [@s.submit cssClass="btn btn-outline-gbif-primary" name="continue" key="button.continue"/]
                </div>
            </div>
        </div>
    </form>
</main>
</div>


[#include "/WEB-INF/pages/inc/footer.ftl"]
