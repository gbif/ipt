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

<div class="container-fluid bg-body border-bottom">
    <div class="container">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container my-3 p-3">
        <div class="text-center text-uppercase fw-bold fs-smaller-2">
            <@s.text name="menu.manage.short"/>
        </div>

        <div class="text-center">
            <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                <@s.text name="manage.home.title"/>
            </h1>

            <#if (resources?size>0)>
                <div class="text-smaller text-gbif-primary">
                    <@s.text name="portal.home.resources.available"><@s.param>${resources?size}</@s.param></@s.text>
                </div>
            <#else>
                <div class="text-smaller text-gbif-danger">
                    <@s.text name="manage.home.no.resources"/>
                </div>
            </#if>

            <#if (organisations?size==0)>
                <div class="text-smaller text-gbif-danger">
                    <@s.text name="manage.resource.create.forbidden"/>
                </div>
            </#if>

            <#if (organisations?size>0)>
                <div class="mt-2">
                    <a href="${baseURL}/manage/create.do" class="btn btn-sm btn-outline-gbif-primary top-button"><@s.text name="button.create.new"/></a>
                </div>
            </#if>
        </div>
    </div>
</div>

<main class="container">
    <div class="my-3 p-3">
        <div id="tableContainer" class="table-responsive text-smaller mx-md-4 mx-2 pt-2"></div>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
