<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="title"/></title>
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<#include "/WEB-INF/pages/macros/popover.ftl">
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<script src="${baseURL}/js/dataTables/dataTables-3.1.5.min.js"></script>
<script src="${baseURL}/js/dataTables/dataTables.responsive-4.1.1.min.js"></script>
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
<script src="${baseURL}/js/select2/select2-4.0.13.full.min.js"></script>
<link rel="stylesheet" href="${baseURL}/styles/manage-home.css">

<script>
    <#-- the table: the server-side DataTable of /manager-api/resources, with an organisation filter next to the search.
         Columns are in the order ResourceDataTableViewManagerImpl#toDatatableResourceManageView writes them -->
    $(document).ready(function () {
        const SEARCH_PARAM = "search", SORT_PARAM = "sort", ORDER_PARAM = "order", PAGE_PARAM = "page", ORG_PARAM = "organisation";
        var columnIndexName = {0: "name", 1: "status", 2: "organisation", 3: "type", 4: "records", 5: "lastPublished", 6: "nextPublished", 7: "lastModified"};
        var columnNameIndex = {};
        Object.keys(columnIndexName).forEach(function (i) { columnNameIndex[columnIndexName[i]] = Number(i); });

        var urlParams = new URLSearchParams(window.location.search);
        var searchParam = urlParams.get(SEARCH_PARAM) || "";
        var sortParam = columnNameIndex[urlParams.get(SORT_PARAM)] !== undefined ? columnNameIndex[urlParams.get(SORT_PARAM)] : 7;
        var orderParam = urlParams.get(ORDER_PARAM) === "asc" ? "asc" : "desc";
        var rawPageParam = urlParams.get(PAGE_PARAM);
        var start = rawPageParam && rawPageParam > 0 ? (rawPageParam - 1) * 10 : 0;
        var organisation = $("#mh-organisation").length ? (urlParams.get(ORG_PARAM) || "") : "";
        $("#mh-organisation").val(organisation);
        if ($("#mh-organisation").val() === null) { organisation = ""; $("#mh-organisation").val(""); }

        function updateUrl(changes) {
            if (!history.pushState) return;
            var params = new URLSearchParams(window.location.search);
            Object.keys(changes).forEach(function (k) { changes[k] ? params.set(k, changes[k]) : params.delete(k); });
            var query = params.toString();
            history.replaceState(null, "", window.location.pathname + (query ? "?" + query : ""));
        }

        $.fn.dataTable.Responsive.breakpoints.push(
            {name: 'desktop', width: Infinity},
            {name: 'tablet-l', width: 1200},
            {name: 'tablet-p', width: 992},
            {name: 'mobile-l', width: 768},
            {name: 'mobile-p', width: 320}
        );

        var dt = $('#rtable').DataTable({
            ajax: {
                url: '${baseURL}/manager-api/resources',
                type: 'POST',
                // DataTables sends its own XMLHttpRequest, not $.ajax, so csrf.js doesn't add the CSRF header
                headers: window.IPT_CSRF && IPT_CSRF.token() ? {[IPT_CSRF.header]: IPT_CSRF.token()} : {},
                data: function (d) {
                    d.organisation = organisation;
                }
            },
            responsive: true,
            processing: true,
            serverSide: true,
            displayStart: start,
            pageLength: 10,
            lengthChange: false,
            autoWidth: false,
            layout: {
                topStart: null,
                topEnd: document.getElementById("mh-organisation") ? [document.getElementById("mh-toolbar"), "search"] : "search",
                bottomStart: "info",
                bottomEnd: "paging"
            },
            language: {
                emptyTable: "<@s.text name="manage.home.resources.none"/><div class=\"mt-2\"><a href=\"${baseURL}/manage/create.do\"><@s.text name="manage.home.createFirst"/></a></div>",
                zeroRecords: "<@s.text name="dataTables.sZeroRecords.resources"/>",
                info: "<@s.text name="dataTables.sInfo"/>",
                infoEmpty: "<@s.text name="dataTables.sInfoEmpty"/>",
                infoFiltered: "<@s.text name="dataTables.sInfoFiltered"/>",
                search: "",
                searchPlaceholder: "<@s.text name="manage.mapping.filter"/>",
                paginate: {
                    next: "<@s.text name="pager.next"/>",
                    previous: "<@s.text name="pager.previous"/>"
                }
            },
            columns: [
                {title: "<@s.text name="manage.home.resource"/>", className: "all text-break mh-name"},
                {title: "<@s.text name="manage.home.status"/>", className: "all mh-status"},
                {title: "<@s.text name="manage.home.organisation"/>", className: "desktop", visible: ${showOrganisationColumn?c}},
                {title: "<@s.text name="manage.home.type"/>", className: "desktop tablet-l"},
                {title: "<@s.text name="portal.home.records"/>", searchable: false, className: "dt-right desktop tablet-l", orderSequence: ["desc", "asc"]},
                {title: "<@s.text name="manage.home.last.publication"/>", searchable: false, className: "desktop tablet-l tablet-p", orderSequence: ["desc", "asc"]},
                {title: "<@s.text name="manage.home.next.publication"/>", searchable: false, className: "desktop mh-next"},
                {title: "<@s.text name="manage.home.last.modified"/>", searchable: false, visible: false}
            ],
            order: [[sortParam, orderParam]],
            search: {search: searchParam}
        });

        // as the other organisation selects (e.g. the overview's publishing organisation): a search from 15 options
        $("#mh-organisation").select2({
            language: {
                noResults: function () {
                    return '${selectNoResultsFound}';
                }
            },
            width: "resolve",
            dropdownAutoWidth: true,
            minimumResultsForSearch: 15,
            theme: 'bootstrap4'
        });

        $("#mh-organisation").on("change", function () {
            organisation = $(this).val();
            updateUrl({[ORG_PARAM]: organisation, [PAGE_PARAM]: null});
            dt.page(0).ajax.reload();
        });

        // page, search and sort in the URL
        dt.on('page.dt', function () {
            updateUrl({[PAGE_PARAM]: dt.page() > 0 ? dt.page() + 1 : null});
        });
        dt.on('search.dt order.dt', function () {
            var params = new URLSearchParams(window.location.search);
            updateUrl({
                [SEARCH_PARAM]: dt.search(),
                [SORT_PARAM]: columnIndexName[dt.order()[0][0]] || "name",
                [ORDER_PARAM]: dt.order()[0][1],
                [PAGE_PARAM]: params.get(PAGE_PARAM) ? 1 : null
            });
        });
    });
</script>

<div class="container">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<div class="container-fluid border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3">
            <div class="text-center fs-smaller">
                <@s.text name="menu.manage.short"/>
            </div>

            <div class="text-center">
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    <@s.text name="manage.home.title"/>
                </h1>

                <#if (resources?size>0)>
                    <div class="text-smaller text-gbif-primary">
                        <@s.text name="portal.home.resources.available"><@s.param>${resourcesSize}</@s.param></@s.text>
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
                        <a href="${baseURL}/manage/create.do" class="btn btn-sm btn-outline-gbif-primary top-button"><@icon name="add" class="me-1"/><@s.text name="button.create.new"/></a>
                    </div>
                </#if>
            </div>
        </div>
    </div>
</div>

<main class="container main-content-container border rounded my-4">
    <div class="py-md-4 py-3 px-md-5 px-3">
        <#-- moved next to the search by DataTables -->
        <div id="mh-toolbar" class="mh-toolbar">
            <#if (resourceOrganisations?size > 1)>
                <select id="mh-organisation" class="form-select form-select-sm mh-organisation-filter" aria-label="<@s.text name="manage.home.organisation"/>">
                    <option value=""><@s.text name="manage.home.organisation.all"/></option>
                    <#list resourceOrganisations as key, name>
                        <option value="${key}">${name}</option>
                    </#list>
                </select>
            </#if>
        </div>
        <div class="resource-table text-smaller pt-2">
            <table class="display dataTable resourcesTable mh-table" id="rtable"></table>
        </div>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
