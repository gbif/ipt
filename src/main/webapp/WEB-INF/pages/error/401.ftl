[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header.ftl"/]
<title>[@s.text name="401.title"/]</title>
[#include "/WEB-INF/pages/inc/menu.ftl"/]

<div class="container-fluid bg-body border-bottom">
    <div class="container">
        [#include "/WEB-INF/pages/inc/action_alerts.ftl"]
    </div>

    <div class="container my-3 p-3">
        <div class="text-center text-uppercase fw-bold fs-smaller-2">
            [@s.text name="basic.error"/]
        </div>

        <div class="text-center">
            <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                [@s.text name="401.title"/]
            </h1>
        </div>
    </div>
</div>

<main class="container">
    <div class="my-3 p-3">
        <p class="text-center mx-md-4 mx-2">[@s.text name="401.body"][@s.param]${baseURL}/login[/@s.param][/@s.text]</p>
    </div>
</main>

[#include "/WEB-INF/pages/inc/footer.ftl"/]
