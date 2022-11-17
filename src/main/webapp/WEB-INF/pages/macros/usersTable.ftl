<#--
usersTable macro: Generates a data table that has searching, pagination, and sortable columns.
- numVersionsShown: The number of versions shown in the table
- sEmptyTable: The message shown when there are no resource records in the table
- columnToSortOn: The column to sort on by default (index starting at 0)
- sortOrder: The sort order of the columnToSortOn
-->
<#macro usersTable numUsersShown sEmptyTable columnToSortOn sortOrder>
    <script charset="utf-8">

        /* organisation list */
        var aDataSet = [
            <#list users as u>
            ['<a href="user?id=${u.email?replace("'", "\\'")?replace("\"", '\\"')!}">${u.name?replace("'", "\\'")?replace("\"", '\\"')!}</a>',
                '${u.email?replace("'", "\\'")?replace("\"", '\\"')!}',
                '<@s.text name="user.roles.${u.role?lower_case}" escapeJavaScript="true"/>',
                '${(u.lastLogin?datetime?string("yyyy-MM-dd HH:mm:ss"))!"never"}',
                '<form class="needs-validation" action="user.do" method="post">' +
                '<div class="form-group d-flex justify-content-end">' +
                '<input type="hidden" name="id" value="${u.email?replace("'", "\\'")?replace("\"", '\\"')!}" required="true">' +
                '<a title="<@s.text name="button.edit"/>" class="icon-button icon-button-sm" type="button" href="user?id=${u.email?replace("'", "\\'")?replace("\"", '\\"')!}">' +
                '<svg class="icon-button-svg icon-material-edit" focusable="false" aria-hidden="true" viewBox="0 0 24 24">' +
                '<path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>' +
                '</svg>' +
                '</a>' +
                '<label title="<@s.text name="button.resetPassword"/>" class="confirmPasswordReset icon-button icon-button-sm">' +
                '<input type="submit" value="Reset password" id="resetPassword" name="resetPassword" class="button btn btn-sm btn-outline-gbif-danger top-button" style="display: none;">' +
                '<svg class="icon-button-svg icon-material-reset-password" focusable="false" aria-hidden="true" viewBox="0 0 24 24"><path d="M13 3c-4.97 0-9 4.03-9 9H1l4 4 4-4H6c0-3.86 3.14-7 7-7s7 3.14 7 7-3.14 7-7 7c-1.9 0-3.62-.76-4.88-1.99L6.7 18.42C8.32 20.01 10.55 21 13 21c4.97 0 9-4.03 9-9s-4.03-9-9-9zm2 8v-1c0-1.1-.9-2-2-2s-2 .9-2 2v1c-.55 0-1 .45-1 1v3c0 .55.45 1 1 1h4c.55 0 1-.45 1-1v-3c0-.55-.45-1-1-1zm-1 0h-2v-1c0-.55.45-1 1-1s1 .45 1 1v1z"></path></svg>' +
                '</label>' +
                '<label title="<@s.text name="button.delete"/>" class="userConfirmDeletion icon-button icon-button-sm">' +
                '<input type="submit" value="Delete" id="delete" name="delete" class="button btn btn-sm btn-outline-gbif-danger top-button" style="display: none;">' +
                '<svg class="icon-button-svg icon-material-delete" focusable="false" aria-hidden="true" viewBox="0 0 24 24">' +
                '<path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>' +
                '</svg>' +
                '</label>' +
                '</div>' +
                '</form>'
            ]<#if u_has_next>,</#if>
            </#list>
        ];

        $(document).ready(function() {
            $('#tableContainer').html( '<table class="display dataTable" id="rtable"></table>' );
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
                    { "sTitle": "<@s.text name="admin.users.last.login"/>", "bSearchable": false},
                    { "sTitle": "", "bSearchable": false, "bSortable": false}
                ],
                "aaSorting": [[ ${columnToSortOn}, "${sortOrder}" ]],
                "aoColumnDefs": [
                    { 'bSortable': true, 'aTargets': [ 0,1,2,3] }
                ],
                "columnDefs": [
                    { "width": "100px", "targets": 4 }
                ]
            } );
        } );
    </script>
</#macro>
