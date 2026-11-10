[#ftl]
[#include "/WEB-INF/pages/inc/header.ftl"/]
<title>[@s.text name="manage.400.title"/]</title>
[#assign currentMenu = "manage"/]
[#include "/WEB-INF/pages/inc/menu.ftl"/]

<div class="container px-0">
    [#include "/WEB-INF/pages/inc/action_alerts_warnings.ftl"]
    [#include "/WEB-INF/pages/inc/action_alerts_errors.ftl"]
</div>

<div class="container-fluid bg-body border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3">
            <div class="text-center fs-smaller">
                [@s.text name="basic.error"/]
            </div>

            <div class="text-center">
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    [@s.text name="manage.400.title"/]
                </h1>
            </div>
        </div>
    </div>
</div>

<main class="container main-content-container">
    <div class="my-3 p-3">
        <p class="text-center">[#if badRequestBody?has_content]${badRequestBody}[#else][@s.text name="manage.400.body"/][/#if]</p>
    </div>
</main>

[#include "/WEB-INF/pages/inc/footer.ftl"/]
