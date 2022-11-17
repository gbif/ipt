<#--
resourcesTable macro: Generates a data table that has searching, pagination, and sortable columns.
- shownPublicly: Whether the table will be shown publicly, or only internally to managers
- numResourcesShown: The number of resources shown in the table
- sEmptyTable: The message shown when there are no resource records in the table
- columnToSortOn: The column to sort on by default (index starting at 0)
- sortOrder: The sort order of the columnToSortOn
-->
<#macro resourcesTable resources shownPublicly numResourcesShown sEmptyTable columnToSortOn sortOrder>

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

            var columnIndexName = {
                1: "name",
                2: "organisation",
                3: "type",
                4: "subtype",
                5: "records",
                6: "lastModified",
                7: "lasPublished",
                8: "nextPublished",
                9: "visibility",
                10: "author"
            };
            var columnNameIndex = {
                "name": 1,
                "organisation": 2,
                "type": 3,
                "subtype": 4,
                "records": 5,
                "lastModified": 6,
                "lasPublished": 7,
                "nextPublished": 8,
                "visibility": 9,
                "author": 10
            };

            var urlParams = new URLSearchParams(window.location.search);
            var searchParam = urlParams.get(SEARCH_PARAM) ? urlParams.get(SEARCH_PARAM) : "";
            var sortParam = urlParams.get(SORT_PARAM) ? getSafe(columnNameIndex, urlParams.get(SORT_PARAM), 1) : ${columnToSortOn};
            var orderParam = urlParams.get(ORDER_PARAM) ? urlParams.get(ORDER_PARAM) : "${sortOrder}";

            $('#tableContainer').html('<table  class="display dataTable" id="rtable"></table>');
            var dt = $('#rtable').DataTable({
                ajax: <#if shownPublicly>'/api/resources'<#else>'/manager-api/resources'</#if>,
                "bProcessing": true,
                "bServerSide": true,
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
                    {"sTitle": "<@s.text name="portal.home.logo"/>", "bSearchable": false, "bSortable": false, "bVisible": <#if shownPublicly>true<#else>false</#if>},
                    {"sTitle": "<@s.text name="manage.home.name"/>"},
                    {"sTitle": "<@s.text name="manage.home.organisation"/>"},
                    {"sTitle": "<@s.text name="manage.home.type"/>"},
                    {"sTitle": "<@s.text name="manage.home.subtype"/>"},
                    {"sTitle": "<@s.text name="portal.home.records"/>", "bSearchable": false, "sType": "number"},
                    {"sTitle": "<@s.text name="manage.home.last.modified"/>", "bSearchable": false},
                    {"sTitle": "<@s.text name="manage.home.last.publication" />", "bSearchable": false},
                    {"sTitle": "<@s.text name="manage.home.next.publication" />", "bSearchable": false},
                    {"sTitle": "<@s.text name="manage.home.visible"/>", "bSearchable": false, "bVisible": <#if shownPublicly>false<#else>true</#if>},
                    {"sTitle": "<@s.text name="portal.home.author"/>", "bVisible": <#if shownPublicly>false<#else>true</#if>},
                    {"sTitle": "<@s.text name="resource.shortname"/>", "bVisible": false},
                    {"sTitle": "<@s.text name="portal.resource.summary.keywords"/>", "bVisible": false}
                ],
                "aaSorting": [[sortParam, orderParam]],
                "aoColumnDefs": [
                    {'bSortable': false, 'aTargets': [0]}
                ],
                "oSearch": {"sSearch": searchParam},
                "fnInitComplete": function (oSettings) {
                    /* Do nothing for now, may need in the future */
                }
            });

            // display search and sort parameters in the URL
            dt.on('search.dt', function () {
                if (history.pushState) {
                    var searchValue = dt.search();
                    var sortFieldIndex = dt.order()[0][0];
                    var sortFieldOrder = dt.order()[0][1];
                    var searchParams = new URLSearchParams(window.location.search);

                    searchValue ? searchParams.set(SEARCH_PARAM, searchValue) : searchParams.delete(SEARCH_PARAM);
                    searchParams.set(SORT_PARAM, getSafe(columnIndexName, sortFieldIndex, "name"));
                    searchParams.set(ORDER_PARAM, sortFieldOrder);

                    var newurl = window.location.protocol + "//" + window.location.host + window.location.pathname + '?' + searchParams.toString();
                    window.history.pushState({path: newurl}, '', newurl);
                }
            });

        });
    </script>
</#macro>
