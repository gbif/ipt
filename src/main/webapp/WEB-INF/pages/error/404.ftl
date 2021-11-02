[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header.ftl"/]
<title>[@s.text name="404.title"/]</title>
[#include "/WEB-INF/pages/inc/menu.ftl"/]

<main class="container">

    [#include "/WEB-INF/pages/inc/action_alerts.ftl"]

    <div class="my-3 p-3 bg-body rounded shadow-sm">
        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header fw-400 text-center">[@s.text name="404.title"/]</h5>
        <p class="text-center mx-md-4 mx-2">[@s.text name="404.body"/]</p>
    </div>
</main>

[#include "/WEB-INF/pages/inc/footer.ftl"/]
