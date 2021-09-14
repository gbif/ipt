<#--
usersTable macro: Generates a data table that has searching, pagination, and sortable columns.
- numVersionsShown: The number of versions shown in the table
- sEmptyTable: The message shown when there are no resource records in the table
- columnToSortOn: The column to sort on by default (index starting at 0)
- sortOrder: The sort order of the columnToSortOn
-->
<#macro validationsTable numValidationsShown sEmptyTable columnToSortOn sortOrder>
    <script charset="utf-8">

        /* validations list */
        var aDataSet = [
            <#list validations.results as v>
            ['${v.key}',
             '${v.sourceId!}',
             '${v.created?datetime?string("yyyy-MM-dd HH:mm:ss")}',
             '${v.status}']<#if v_has_next>,</#if>
            </#list>
        ];

        $(document).ready(function() {
            $('#tableContainer').html( '<table class="table table-sm" id="rtable"></table>' );
            $('#rtable').dataTable( {
                "aaData": aDataSet,
                "iDisplayLength": ${numValidationsShown},
                "bLengthChange": false,
                "bAutoWidth": false,
                "oLanguage": {
                    "sEmptyTable": "<@s.text name="${sEmptyTable}"/>",
                    "sZeroRecords": "<@s.text name="dataTables.sZeroRecords.validations"/>",
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
                    { "sTitle": "<@s.text name="admin.validation.key"/>", "bSearchable": false},
                    { "sTitle": "<@s.text name="admin.validation.resource"/>", "bSearchable": false},
                    { "sTitle": "<@s.text name="admin.validation.created"/>", "bSearchable": false},
                    { "sTitle": "<@s.text name="admin.validation.status"/>", "bSearchable": false}
                ],
                "aaSorting": [[ ${columnToSortOn}, "${sortOrder}" ]],
                "aoColumnDefs": [
                    { 'bSortable': true, 'aTargets': [ 0,1,2,3] }
                ]
            } );
        } );
    </script>
</#macro>
