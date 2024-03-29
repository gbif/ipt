<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/resourcesTable.ftl"/>
<script src="${baseURL}/js/jquery/jquery-3.7.0.min.js"></script>
<script src="${baseURL}/js/jquery/jquery.dataTables-1.13.6.min.js"></script>
<script src="${baseURL}/js/jquery/dataTables.responsive-2.5.0.min.js"></script>

<@resourcesTable resources=resources shownPublicly=true numResourcesShown=10 sEmptyTable="dataTables.sEmptyTable.resources" columnToSortOn=1 sortOrder="asc" />

<div class="container">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<div class="container-fluid border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3">
            <div class="text-center fs-smaller">
                <@s.text name="menu.home"/>
            </div>

            <div class="text-center">
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    <@s.text name="portal.home.title"/>
                </h1>

                <#if (resources?size>0)>
                    <div class="text-smaller text-gbif-primary mb-2">
                        <@s.text name="portal.home.resources.available"><@s.param>${resourcesSize}</@s.param></@s.text>
                    </div>
                <#else>
                    <div class="text-smaller text-gbif-danger mb-2">
                        <@s.text name="portal.home.no.public"/>
                    </div>
                </#if>
            </div>
        </div>
    </div>
</div>

<main class="container main-content-container border rounded my-4">
    <div class="py-md-4 py-3 px-md-5 px-3">
        <div id="tableContainer" class="resource-table text-smaller pt-2"></div>

        <!-- RSS Feed shown if there are resources -->
        <#if (resources?size>0)>
            <p class="mb-0 pt-2 text-center">
                <i class="bi bi-rss"></i>
                <@s.text name="portal.home.feed"><@s.param>${baseURL}/rss.do</@s.param></@s.text>
            </p>
        </#if>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
