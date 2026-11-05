<#--
organisationsTable macro: Generates a data table that has searching, pagination, and sortable columns.
- numVersionsShown: The number of versions shown in the table
- sEmptyTable: The message shown when there are no resource records in the table
- columnToSortOn: The column to sort on by default (index starting at 0)
- sortOrder: The sort order of the columnToSortOn
-->
<#macro organisationsTable numOrganisationsShown sEmptyTable columnToSortOn sortOrder>
    <script charset="utf-8">
        <#assign emptyString="--">

        /* organisation list */
        var aDataSet = [
            <#list linkedOrganisations as o>
            ['<a id="editLink_${(o.key)!}" class="resource-table-link" href="organisation?id=${(o.key)!}">${(o.name)!?replace("\'", "\\'")?replace("\"", '\\"')}</a>',
                '<#if (o.alias)??>${o.alias?replace("\'", "\\'")?replace("\"", '\\"')}<#else>${emptyString}</#if>',
                '<#if o?? && o.canHost><@s.text name="basic.yes" escapeJavaScript="true"/><#else><@s.text name="basic.no" escapeJavaScript="true"/></#if>',
                '${(o.doiRegistrationAgency)!"${emptyString}"}',
                '<#if o?? && (o.agencyAccountPrimary || o.associatedWithDoiRegistrationAgency)><@s.text name="basic.yes" escapeJavaScript="true"/><#else><@s.text name="basic.no" escapeJavaScript="true"/></#if>',
                '<form class="needs-validation" action="organisation.do" method="post">' +
                '<div class="form-group d-flex justify-content-end">' +
                '<input type="hidden" name="organisation.key" value="${(o.key)!}" required="true">' +
                '<input type="hidden" name="id" value="${(o.key)!}" required="true">' +
                '<a title="<@s.text name="button.edit" escapeJavaScript="true"/>" class="icon-button icon-button-sm" type="button" href="organisation?id=${(o.key)!}">' +
                '<svg class="icon-button-svg icon-material-edit" focusable="false" aria-hidden="true" viewBox="0 0 24 24">' +
                '<path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>' +
                '</svg>' +
                '</a>' +
                '<label title="<@s.text name="button.delete" escapeJavaScript="true"/>" class="organizationConfirmDeletion icon-button icon-button-sm">' +
                '<input type="submit" value="Delete" id="delete" name="deleteFlag" class="button btn btn-sm btn-outline-gbif-danger top-button" style="display: none;">' +
                '<svg class="icon-button-svg icon-material-delete" focusable="false" aria-hidden="true" viewBox="0 0 24 24">' +
                '<path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>' +
                '</svg>' +
                '</label>' +
                '</div>' +
                '</form>'
            ]<#if o_has_next>,</#if>
            </#list>
        ];

        $(document).ready(function() {
            $('#tableContainer').html( '<table class="display dataTable compact" id="rtable"></table>' );
            $('#rtable').dataTable( {
                "data": aDataSet,
                "pageLength": ${numOrganisationsShown},
                "lengthChange": false,
                "autoWidth": false,
                "language": {
                    "emptyTable": "<@s.text name="${sEmptyTable}"/>",
                    "zeroRecords": "<@s.text name="dataTables.sZeroRecords.organisations"/>",
                    "info": "<@s.text name="dataTables.sInfo"/>",
                    "infoEmpty": "<@s.text name="dataTables.sInfoEmpty"/>",
                    "infoFiltered": "<@s.text name="dataTables.sInfoFiltered"/>",
                    "search": "<@s.text name="manage.mapping.filter"/>:",
                    "paginate": {
                        "next": "<@s.text name="pager.next"/>",
                        "previous": "<@s.text name="pager.previous"/>"

                    }
                },
                "columns": [
                    { "title": "<@s.text name="admin.organisation.name"/>", "searchable": true},
                    { "title": "<@s.text name="admin.organisation.alias"/>", "searchable": true},
                    { "title": "<@s.text name="admin.organisation.canPublish"/>", "searchable": false},
                    { "title": "<@s.text name="admin.organisation.doiRegistrationAgency"/>", "searchable": true},
                    { "title": "<@s.text name="admin.organisation.canRegisterDois"/>", "searchable": false},
                    { "title": "", "searchable": false, "orderable": false}
                ],
                "order": [[ ${columnToSortOn}, "${sortOrder}" ]],
                "columnDefs": [
                    { 'orderable': true, 'targets': [ 0,1,2,3] }
                ],
                "columnDefs": [
                    { "width": "100px", "targets": 5 }
                ]
            } );
        } );
    </script>
</#macro>
