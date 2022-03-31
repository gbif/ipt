[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header.ftl"/]
<title>[@s.text name="error.header.title"/]</title>
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
                [@s.text name="error.title"/]
            </h1>
        </div>
    </div>
</div>

<main class="container">
    <div class="my-3 p-3">
        <p class="mx-md-4 mx-2">
            [@s.text name="error.body"/]
        </p>

        <pre class="mb-0 mx-md-4 mx-2">
            [@s.property value="%{exception.message}"/]
        </pre>

        [#if adminRights]
            <p class="mx-md-4 mx-2">
                <a href="${baseURL}/admin/logs.do">[@s.text name="error.view.logs"/]</a>
            </p>
        [/#if]

        <p class="mx-md-4 mx-2">
            [@s.text name="error.report"/]
        </p>

        <p class="mx-md-4 mx-2">
            [@s.text name="error.thanks"/]
        </p>
    </div>

    <div class="my-3 p-3">
        <h5 class="pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header-2 fw-400">
            [@s.text name="error.details.title"/]
        </h5>

        <pre class="mx-md-4 mx-2">
            [@s.property value="%{exceptionStack}"/]
        </pre>
    </div>
</main>

[#include "/WEB-INF/pages/inc/footer.ftl"/]
