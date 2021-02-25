[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header_setup-bootstrap.ftl"]

<main class="container pt-5">

    <form action="setupComplete.do">
        <div class="my-3 p-3 bg-body rounded shadow-sm">
            <h4 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-success text-center">
                [@s.text name="admin.config.setup3.title"/]
            </h4>

            [#if warnings?size>0]
                [#list warnings as w]
                    <div class="alert alert-danger mx-md-4 mx-2" role="alert">
                        ${w!}
                    </div>
                [/#list]
            [/#if]

            <p class="text-muted mx-md-4 mx-2">[@s.text name="admin.config.setup3.welcome"/]</p>

            <div class="row g-3 pb-2">
                <div class="col-12 d-flex justify-content-center">
                    <button class="btn btn-outline-success" type="submit" name="continue">
                        [@s.text name="button.continue"/]
                    </button>
                </div>
            </div>
        </div>
    </form>
</main>
</div>


[#include "/WEB-INF/pages/inc/footer-bootstrap.ftl"]
