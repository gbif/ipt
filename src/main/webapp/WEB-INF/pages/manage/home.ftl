<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header-bootstrap.ftl">
<title><@s.text name="title"/></title>
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu-bootstrap.ftl">
<#include "/WEB-INF/pages/macros/forms-bootstrap.ftl"/>
<#include "/WEB-INF/pages/macros/resourcesTable-bootstrap.ftl"/>
<script type="text/javascript" src="https://code.jquery.com/jquery-3.5.1.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/1.10.23/js/jquery.dataTables.min.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/1.10.23/js/dataTables.bootstrap5.min.js"></script>
<script type="text/javascript">
    $(document).ready(function(){
        initHelp();
    });
</script>
<@resourcesTableBootstrap shownPublicly=false numResourcesShown=10 sEmptyTable="manage.home.resources.none" columnToSortOn=6 sortOrder="desc"/>

<main class="container">
    <div class="my-3 p-3 bg-body rounded shadow-sm">
        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-success text-center">
            <@s.text name="manage.home.title"/>
        </h5>
        <div id="tableContainer" class="table-responsive mx-md-4 mx-2 pt-2" style='font-size: 0.875rem !important;'></div>
    </div>

    <div class="my-3 p-3 bg-body rounded shadow-sm">
        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-success text-center">
            <@s.text name="manage.resource.create.title"/>
        </h5>

        <#include "inc/create_new_resource-bootstrap.ftl"/>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer-bootstrap.ftl">
