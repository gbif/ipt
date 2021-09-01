[#ftl]
[#include "/WEB-INF/pages/inc/header.ftl"/]
<title>[@s.text name="manage.404.title"/]</title>
[#assign currentMenu = "manage"/]
[#include "/WEB-INF/pages/inc/menu.ftl"/]

<main class="container">

    <div class="my-3 p-3 bg-body rounded shadow-sm">
        [#include "/WEB-INF/pages/inc/action_alerts.ftl"]

        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header text-center">[@s.text name="manage.404.title"/]</h5>
        <p class="mx-md-4 mx-2 text-center">[@s.text name="manage.404.body"/]</p>
    </div>
</main>

[#include "/WEB-INF/pages/inc/footer.ftl"/]
