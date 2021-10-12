<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="title"/></title>
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<#include "/WEB-INF/pages/macros/popover.ftl">
<#include "/WEB-INF/pages/macros/resourcesTable.ftl"/>
<script src="${baseURL}/js/jquery/jquery-3.5.1.min.js"></script>
<script src="${baseURL}/js/jquery/jquery.dataTables-1.10.23.min.js"></script>
<script src="${baseURL}/js/jquery/dataTables.bootstrap5-1.10.23.min.js"></script>

<@resourcesTable shownPublicly=false numResourcesShown=10 sEmptyTable="manage.home.resources.none" columnToSortOn=6 sortOrder="desc"/>

<main class="container">
    <div class="my-3 p-3 bg-body rounded shadow-sm">
        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header text-center">
            <@s.text name="manage.home.title"/>
        </h5>
        <div id="tableContainer" class="table-responsive text-smaller mx-md-4 mx-2 pt-2"></div>
    </div>

    <div class="my-3 p-3 bg-body rounded shadow-sm">
        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header text-center">
            <@s.text name="manage.resource.create.title"/>
        </h5>

        <#include "inc/create_new_resource.ftl"/>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
