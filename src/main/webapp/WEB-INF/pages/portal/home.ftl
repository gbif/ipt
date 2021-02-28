<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header-bootstrap.ftl">
<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu-bootstrap.ftl">
<#include "/WEB-INF/pages/macros/resourcesTable-bootstrap.ftl"/>
<script type="text/javascript" src="https://code.jquery.com/jquery-3.5.1.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/1.10.23/js/jquery.dataTables.min.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/1.10.23/js/dataTables.bootstrap5.min.js"></script>
<@resourcesTableBootstrap shownPublicly=true numResourcesShown=20 sEmptyTable="dataTables.sEmptyTable.resources" columnToSortOn=1 sortOrder="asc" />

<main class="container pt-5">
    <div class="my-3 p-3 bg-body rounded shadow-sm" id="summary">
        <h4 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-success text-center">
            <@s.text name="portal.home.title"/>
        </h4>
        <div id="tableContainer" class="table-responsive mx-md-4 mx-2 pt-2"></div>

        <!-- RSS Feed shown if there are resources -->
        <#if (resources?size>0)>
            <p class="text-muted mx-md-4 mx-2 pt-2">
                <@s.text name="portal.home.feed"><@s.param>${baseURL}/rss.do</@s.param></@s.text>
                <img id="rssImage" src="${baseURL}/images/rss.png"/>.
            </p>
        <#else>
            <p class="text-muted text-center pt-2"><@s.text name="portal.home.no.public"/></p>
        </#if>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer-bootstrap.ftl">
