<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/resourcesTable.ftl"/>
<script src="${baseURL}/js/jquery/jquery-3.5.1.min.js"></script>
<script src="${baseURL}/js/jquery/jquery.dataTables-1.10.23.min.js"></script>
<script src="${baseURL}/js/jquery/dataTables.bootstrap5-1.10.23.min.js"></script>
<@resourcesTable shownPublicly=true numResourcesShown=20 sEmptyTable="dataTables.sEmptyTable.resources" columnToSortOn=1 sortOrder="asc" />

<main class="container">
    <div class="my-3 p-3 bg-body rounded shadow-sm">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">

        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header text-center">
            <@s.text name="portal.home.title"/>
        </h5>
        <div id="tableContainer" class="table-responsive text-smaller mx-md-4 mx-2 pt-2"></div>

        <!-- RSS Feed shown if there are resources -->
        <#if (resources?size>0)>
            <p class="mx-md-4 mx-2 pt-2">
                <i class="bi bi-rss"></i>
                <@s.text name="portal.home.feed"><@s.param>${baseURL}/rss.do</@s.param></@s.text>
            </p>
        <#else>
            <p class="text-center pt-2"><@s.text name="portal.home.no.public"/></p>
        </#if>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
