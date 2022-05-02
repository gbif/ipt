<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="menu.about"/></title>
<#assign currentMenu = "about"/>

<#include "/WEB-INF/pages/inc/menu.ftl">

<div class="container-fluid bg-body border-bottom">
    <div class="container my-3 p-3">
        <div class="text-center">
            <div class="text-uppercase fw-bold fs-smaller-2">
                <span><@s.text name="menu.about"/></span>
            </div>

            <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                ${title}
            </h1>

            <#assign aDateTime = .now>
            <div class="text-smaller text-gbif-primary mb-2">
                ${aDateTime?date?string.long}
            </div>
        </div>
    </div>
</div>

<main class="container">
    <div class="my-3 p-3">
        <div>
            <@content?interpret />
        </div>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
