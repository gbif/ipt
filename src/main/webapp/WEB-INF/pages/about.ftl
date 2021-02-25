<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header-bootstrap.ftl">
<title>[@s.text name="login.title"/]</title>
[#assign currentMenu = "about"/]
<#include "/WEB-INF/pages/inc/menu-bootstrap.ftl">

<main class="container pt-4">
    <div class="my-3 p-3 bg-body rounded shadow-sm" id="summary">

        <@content?interpret />

    </div>
</main>

<#include "/WEB-INF/pages/inc/footer-bootstrap.ftl">
