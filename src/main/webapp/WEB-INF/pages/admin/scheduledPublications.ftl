<#assign autoPublishingDisabled=!cfg.autoPublishingEnabled/>

<#macro scheduledPublicationsTable numResourcesShown sEmptyTable columnToSortOn sortOrder>
    <script>

        var aDataSet = [
            <#list scheduledResources as r>
            [
                '<a href="${baseURL}/manage/resource.do?r=${r.shortname?url}" class="resource-table-link">${(r.title!r.shortname)?js_string}</a>' +
                '<div class="fs-smaller-2 text-gbif-primary">${r.shortname?js_string}</div>',
                '${(r.managers!"")?js_string}',
                '${(r.frequency!"")?js_string}',
                '${(r.lastPublished?datetime?string("yyyy-MM-dd HH:mm"))!""}',
                '<#if autoPublishingDisabled><del>${(r.nextPublished?datetime?string("yyyy-MM-dd HH:mm"))!""}</del><#else>${(r.nextPublished?datetime?string("yyyy-MM-dd HH:mm"))!""}</#if>',
                ${r.recordsPublished?c}
            ]<#if r_has_next>,</#if>
            </#list>
        ];

        $(document).ready(function() {
            $('#tableContainer').html( '<table class="display dataTable compact" id="rtable"></table>' );
            $('#rtable').dataTable( {
                "aaData": aDataSet,
                "iDisplayLength": ${numResourcesShown},
                "bLengthChange": false,
                "bAutoWidth": false,
                "oLanguage": {
                    "sEmptyTable": "<@s.text name="${sEmptyTable}"/>",
                    "sZeroRecords": "<@s.text name="dataTables.sZeroRecords.resources"/>",
                    "sInfo": "<@s.text name="dataTables.sInfo"/>",
                    "sInfoEmpty": "<@s.text name="dataTables.sInfoEmpty"/>",
                    "sInfoFiltered": "<@s.text name="dataTables.sInfoFiltered"/>",
                    "sSearch": "<@s.text name="manage.mapping.filter"/>:",
                    "oPaginate": {
                        "sNext": "<@s.text name="pager.next"/>",
                        "sPrevious": "<@s.text name="pager.previous"/>"
                    }
                },
                "aoColumns": [
                    { "sTitle": "<@s.text name="admin.scheduledPublications.resource"/>", "bSearchable": true},
                    { "sTitle": "<@s.text name="admin.scheduledPublications.managers"/>", "bSearchable": true},
                    { "sTitle": "<@s.text name="admin.scheduledPublications.frequency"/>", "bSearchable": false},
                    { "sTitle": "<@s.text name="admin.scheduledPublications.lastPublished"/>", "bSearchable": false},
                    { "sTitle": "<@s.text name="admin.scheduledPublications.nextPublished"/>", "bSearchable": false},
                    { "sTitle": "<@s.text name="admin.scheduledPublications.records"/>", "bSearchable": false},
                    <#--{ "sTitle": "<@s.text name="admin.scheduledPublications.status"/>", "bSearchable": false},-->
                    <#--{ "sTitle": "<@s.text name="admin.scheduledPublications.failures"/>", "bSearchable": false}-->
                ],
                "aaSorting": [[ ${columnToSortOn}, "${sortOrder}" ]],
                "aoColumnDefs": [
                    { 'bSortable': true, 'aTargets': [ 0,1,2,3,4,5 ] }
                ]
            } );
        } );
    </script>
</#macro>

<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="admin.scheduledPublications.title"/></title>
<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<script src="${baseURL}/js/jquery/jquery.dataTables-1.13.6.min.js"></script>

<@scheduledPublicationsTable numResourcesShown=25 sEmptyTable="admin.scheduledPublications.empty" columnToSortOn=4 sortOrder="asc"/>

<#if autoPublishingDisabled>
    <div class="container px-0">
        <div class="alert alert-warning">
            <@s.text name="admin.scheduledPublications.suppressed"/>
            <a href="${baseURL}/admin/autoPublishingSettings.do"><@s.text name="admin.home.autoPublicationSettings"/></a>
        </div>
    </div>
</#if>

<div class="container-fluid bg-body border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3">
            <div class="text-center">
                <div class="fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb justify-content-center mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                            <li class="breadcrumb-item"><@s.text name="breadcrumb.admin.autopublication"/></li>
                        </ol>
                    </nav>
                </div>

                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    <@s.text name="admin.scheduledPublications.title"/>
                </h1>

                <div class="mt-2">
                    <a href="${baseURL}/admin/" class="btn btn-sm btn-outline-secondary top-button">
                        <@s.text name="button.cancel"/>
                    </a>
                </div>

                <p class="mt-3 mb-0 text-smaller fst-italic">
                    <@s.text name="admin.scheduledPublications.intro"/>
                </p>
            </div>
        </div>
    </div>
</div>

<main class="container main-content-container border rounded-2 mt-4 mb-5">
    <div class="mb-3 py-4 px-5">
        <div id="tableContainer" class="table-responsive text-smaller pt-2"></div>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
