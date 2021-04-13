[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header-bootstrap.ftl"/]
<title>[@s.text name="manage.404.title"/]</title>
[#assign currentMenu = "manage"/]
[#include "/WEB-INF/pages/inc/menu-bootstrap.ftl"/]

<main class="container">

    <div class="my-3 p-3 bg-body rounded shadow-sm">
        [#include "/WEB-INF/pages/inc/action_alerts-bootstrap.ftl"]

        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-primary text-center">[@s.text name="manage.404.title"/]</h5>
        <p class="text-muted mx-md-4 mx-2 text-center">We are sorry, but the requested resource [#if version?has_content && (version > 0)]with version #${version} [/#if]does not exist</p>
    </div>
</main>

[#include "/WEB-INF/pages/inc/footer-bootstrap.ftl"/]
