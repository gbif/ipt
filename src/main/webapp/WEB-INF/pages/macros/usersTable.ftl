<#--
usersTable macro: Generates a data table that has searching, pagination, and sortable columns.
- numVersionsShown: The number of versions shown in the table
- sEmptyTable: The message shown when there are no resource records in the table
- columnToSortOn: The column to sort on by default (index starting at 0)
- sortOrder: The sort order of the columnToSortOn
-->
<#macro usersTable numUsersShown sEmptyTable columnToSortOn sortOrder>
    <script type="text/javascript" charset="utf-8">

        /* organisation list */
        var aDataSet = [
            <#list users as u>
            ['<a href="user?id=${u.email?replace("'", "\\'")?replace("\"", '\\"')!}">${u.name?replace("'", "\\'")?replace("\"", '\\"')!}</a>',
                '${u.email?replace("'", "\\'")?replace("\"", '\\"')!}',
                '<@s.text name="user.roles.${u.role?lower_case}" escapeJavaScript="true"/>',
                '${(u.lastLogin?datetime?string("yyyy-MM-dd HH:mm:ss"))!"never"}']<#if u_has_next>,</#if>
            </#list>
        ];

        $(document).ready(function() {
            $('#tableContainer').html( '<table class="table table-sm text-muted" id="rtable"></table>' );
            $('#rtable').dataTable( {
                "aaData": aDataSet,
                "iDisplayLength": ${numUsersShown},
                "bLengthChange": false,
                "bAutoWidth": false,
                "oLanguage": {
                    "sEmptyTable": "<@s.text name="${sEmptyTable}"/>",
                    "sZeroRecords": "<@s.text name="dataTables.sZeroRecords.users"/>",
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
                    { "sTitle": "<@s.text name="admin.users.name"/>", "bSearchable": true},
                    { "sTitle": "<@s.text name="admin.users.email"/>", "bSearchable": true},
                    { "sTitle": "<@s.text name="admin.users.role"/>", "bSearchable": false},
                    { "sTitle": "<@s.text name="admin.users.last.login"/>", "bSearchable": false}
                ],
                "aaSorting": [[ ${columnToSortOn}, "${sortOrder}" ]],
                "aoColumnDefs": [
                    { 'bSortable': true, 'aTargets': [ 0,1,2,3] }
                ]
            } );
        } );
    </script>
</#macro>
