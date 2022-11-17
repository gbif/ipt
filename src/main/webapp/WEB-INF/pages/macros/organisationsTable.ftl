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
            ['<a id="editLink_${o.key}" href="organisation?id=${o.key}">${o.name?replace("\'", "\\'")?replace("\"", '\\"')}</a>',
                '<#if o.alias??>${o.alias?replace("\'", "\\'")?replace("\"", '\\"')}<#else>${emptyString}</#if>',
                '<#if o.canHost><@s.text name="basic.yes"/><#else><@s.text name="basic.no"/></#if>',
                '<#if o.doiRegistrationAgency??>${o.doiRegistrationAgency}<#else>${emptyString}</#if>',
                '<#if o.agencyAccountPrimary><@s.text name="basic.yes"/><#else><@s.text name="basic.no"/></#if>',
                '<form class="needs-validation" action="organisation.do" method="post">' +
                '<div class="form-group d-flex justify-content-end">' +
                '<input type="hidden" name="organisation.key" value="${o.key!}" required="true">' +
                '<input type="hidden" name="id" value="${o.key!}" required="true">' +
                '<a title="<@s.text name="button.edit"/>" class="icon-button icon-button-sm" type="button" href="organisation?id=${o.key}">' +
                '<svg class="icon-button-svg icon-material-edit" focusable="false" aria-hidden="true" viewBox="0 0 24 24">' +
                '<path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>' +
                '</svg>' +
                '</a>' +
                '<label title="<@s.text name="button.delete"/>" class="organizationConfirmDeletion icon-button icon-button-sm">' +
                '<input type="submit" value="Delete" id="delete" name="delete" class="button btn btn-sm btn-outline-gbif-danger top-button" style="display: none;">' +
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
            $('#tableContainer').html( '<table class="display dataTable" id="rtable"></table>' );
            $('#rtable').dataTable( {
                "aaData": aDataSet,
                "iDisplayLength": ${numOrganisationsShown},
                "bLengthChange": false,
                "bAutoWidth": false,
                "oLanguage": {
                    "sEmptyTable": "<@s.text name="${sEmptyTable}"/>",
                    "sZeroRecords": "<@s.text name="dataTables.sZeroRecords.organisations"/>",
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
                    { "sTitle": "<@s.text name="admin.organisation.name"/>", "bSearchable": true},
                    { "sTitle": "<@s.text name="admin.organisation.alias"/>", "bSearchable": true},
                    { "sTitle": "<@s.text name="admin.organisation.canPublish"/>", "bSearchable": false},
                    { "sTitle": "<@s.text name="admin.organisation.doiRegistrationAgency"/>", "bSearchable": true},
                    { "sTitle": "<@s.text name="admin.organisation.canRegisterDois"/>", "bSearchable": false},
                    { "sTitle": "", "bSearchable": false, "bSortable": false}
                ],
                "aaSorting": [[ ${columnToSortOn}, "${sortOrder}" ]],
                "aoColumnDefs": [
                    { 'bSortable': true, 'aTargets': [ 0,1,2,3] }
                ],
                "columnDefs": [
                    { "width": "100px", "targets": 5 }
                ]
            } );
        } );
    </script>
</#macro>
