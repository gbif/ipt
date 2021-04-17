[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header.ftl"/]
<title>[@s.text name="error.header.title"/]</title>
[#include "/WEB-INF/pages/inc/menu.ftl"/]

<main class="container">
    <div class="my-3 p-3 bg-body rounded shadow-sm">

        [#include "/WEB-INF/pages/inc/action_alerts.ftl"]

        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header text-center">
            [@s.text name="error.title"/]
        </h5>

        <p class="text-muted mx-md-4 mx-2">
            [@s.text name="error.body"/]
        </p>

        <pre class="mb-0 mx-md-4 mx-2">
            [@s.property value="%{exception.message}"/]
        </pre>

        [#if adminRights]
            <p class="text-muted mx-md-4 mx-2">
                <a href="${baseURL}/admin/logs.do">[@s.text name="error.view.logs"/]</a>
            </p>
        [/#if]

        <p class="text-muted mx-md-4 mx-2">
            [@s.text name="error.report"/]
        </p>

        <p class="text-muted mx-md-4 mx-2">
            [@s.text name="error.thanks"/]
        </p>
    </div>

    <div class="my-3 p-3 bg-body rounded shadow-sm">
        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header text-center">
            [@s.text name="error.details.title"/]
        </h5>

        <pre class="mx-md-4 mx-2">
            [@s.property value="%{exceptionStack}"/]
        </pre>
    </div>
</main>

[#include "/WEB-INF/pages/inc/footer.ftl"/]
