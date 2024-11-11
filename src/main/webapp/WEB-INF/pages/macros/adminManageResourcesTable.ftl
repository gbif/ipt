<#--
resourcesTable macro: Generates a data table that has searching, pagination, and sortable columns.
- shownPublicly: Whether the table will be shown publicly, or only internally to managers
- numResourcesShown: The number of resources shown in the table
- sEmptyTable: The message shown when there are no resource records in the table
- columnToSortOn: The column to sort on by default (index starting at 0)
- sortOrder: The sort order of the columnToSortOn
-->
<#macro adminManageResourcesTable resources shownPublicly numResourcesShown sEmptyTable columnToSortOn sortOrder>

    <script charset="utf-8">
        <#assign emptyString="--">
        <#assign dotDot="..">
        <#assign visibilityPrivate><@s.text name="manage.home.visible.private"/></#assign>
        <#assign visibilityPublic><@s.text name="manage.home.visible.public"/></#assign>
        <#assign visibilityDeleted><@s.text name="manage.home.visible.deleted"/></#assign>
        <#assign notRegistered><@s.text name="manage.home.not.registered"/></#assign>
        <#assign unknownOrganisation><@s.text name="manage.home.unknown.organisation"/></#assign>
        <#assign notPublished><@s.text name="portal.home.not.published"/></#assign>

        // parse a date in yyyy-mm-dd format
        function parseDate(input) {
            var parts = input.match(/(\d+)/g);
            return new Date(parts[0], parts[1] - 1, parts[2], parts[3], parts[4], parts[5]); // months are 0-based
        }

        function getSafe(object, key, defaultVal) {
            try {
                return object[key] ? object[key] : defaultVal;
            } catch (e) {
                return defaultVal;
            }
        }

        $(document).ready(function () {
            const SEARCH_PARAM = "search";
            const SORT_PARAM = "sort";
            const ORDER_PARAM = "order";
            const PAGE_PARAM = "page";

            var columnIndexName = {
                0: "name"
            };
            var columnNameIndex = {
                "name": 0
            };

            var urlParams = new URLSearchParams(window.location.search);
            var searchParam = urlParams.get(SEARCH_PARAM) ? urlParams.get(SEARCH_PARAM) : "";
            var sortParam = urlParams.get(SORT_PARAM) ? getSafe(columnNameIndex, urlParams.get(SORT_PARAM), 1) : ${columnToSortOn};
            var orderParam = urlParams.get(ORDER_PARAM) ? urlParams.get(ORDER_PARAM) : "${sortOrder}";
            var rawPageParam = urlParams.get(PAGE_PARAM);
            // converted from page to start param (1, 2, 3 etc. -> 0, 10, 20, 30 etc.)
            var start = rawPageParam && rawPageParam > 0 ? (urlParams.get(PAGE_PARAM) - 1) * 10 : 0;

            $.fn.dataTable.Responsive.breakpoints.push(
                { name: 'desktop',  width: Infinity },
                { name: 'tablet-l', width: 1200 },
                { name: 'tablet-p', width: 992 },
                { name: 'mobile-l', width: 768 },
                { name: 'mobile-p', width: 320 }
            );

            $('#tableContainer').html('<table  class="display dataTable resourcesTable" id="rtable"></table>');
            var dt = $('#rtable').DataTable({
                ajax: {
                    url: '${baseURL}/admin-api/resources',
                    type: 'POST'
                },
                responsive: true,
                "bProcessing": true,
                "bServerSide": true,
                "displayStart": start,
                "iDisplayLength": ${numResourcesShown},
                "bLengthChange": false,
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
                    {"sTitle": "<@s.text name="manage.home.name"/>", "sClass": "all text-break"}
                ],
                // "aaSorting": [[sortParam, orderParam]],
                "aoColumnDefs": [
                    {'bSortable': false, 'aTargets': [0]}
                ],
                "oSearch": {"sSearch": searchParam},
                "fnInitComplete": function (oSettings) {
                    /* Do nothing for now, may need in the future */
                }
            });

            // display page parameter in the URL
            $(document).on("click", "a.paginate_button", function (e) {
                var page = dt.page() + 1;
                var params = new URLSearchParams(window.location.search);
                page !== 0 ? params.set(PAGE_PARAM, page) : params.delete(PAGE_PARAM);
                var newurl = window.location.protocol + "//" + window.location.host + window.location.pathname + '?' + params.toString();
                window.history.pushState({path: newurl}, '', newurl);
            });

            // display search and sort parameters in the URL
            dt.on('search.dt', function () {
                if (history.pushState) {
                    var searchValue = dt.search();
                    var sortFieldIndex = dt.order()[0][0];
                    var sortFieldOrder = dt.order()[0][1];
                    var params = new URLSearchParams(window.location.search);
                    var page = params.get(PAGE_PARAM);

                    // reset page param
                    if (page) {
                        params.set(PAGE_PARAM, 1);
                    }
                    searchValue ? params.set(SEARCH_PARAM, searchValue) : params.delete(SEARCH_PARAM);
                    params.set(SORT_PARAM, getSafe(columnIndexName, sortFieldIndex, "name"));
                    params.set(ORDER_PARAM, sortFieldOrder);

                    var newurl = window.location.protocol + "//" + window.location.host + window.location.pathname + '?' + params.toString();
                    window.history.pushState({path: newurl}, '', newurl);
                }
            });
        });
    </script>
</#macro>
