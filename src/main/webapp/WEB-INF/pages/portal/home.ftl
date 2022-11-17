<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/resourcesTable.ftl"/>
<script src="${baseURL}/js/jquery/jquery-3.5.1.min.js"></script>
<script src="${baseURL}/js/jquery/jquery.dataTables-1.13.1.min.js"></script>

<@resourcesTable shownPublicly=true numResourcesShown=20 sEmptyTable="dataTables.sEmptyTable.resources" columnToSortOn=1 sortOrder="asc" />

<div class="container-fluid bg-body border-bottom">
    <div class="container my-3">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container my-3 p-3">
        <div class="text-center text-uppercase fw-bold fs-smaller-2">
            <@s.text name="menu.home"/>
        </div>

        <div class="text-center">
            <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                <@s.text name="portal.home.title"/>
            </h1>

            <#if (resources?size>0)>
                <div class="text-smaller text-gbif-primary mb-2">
                    <@s.text name="portal.home.resources.available"><@s.param>${resources?size}</@s.param></@s.text>
                </div>
            <#else>
                <div class="text-smaller text-gbif-danger mb-2">
                    <@s.text name="portal.home.no.public"/>
                </div>
            </#if>
        </div>
    </div>
</div>

<main class="container">
    <div class="my-3 p-3">
        <div id="tableContainer" class="table-responsive text-smaller pt-2"></div>

        <!-- RSS Feed shown if there are resources -->
        <#if (resources?size>0)>
            <p class="pt-2 text-center">
                <i class="bi bi-rss"></i>
                <@s.text name="portal.home.feed"><@s.param>${baseURL}/rss.do</@s.param></@s.text>
            </p>
        </#if>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
