<#--
organisationsTable macro: Generates a data table that has searching, pagination, and sortable columns.
- numVersionsShown: The number of versions shown in the table
- sEmptyTable: The message shown when there are no resource records in the table
- columnToSortOn: The column to sort on by default (index starting at 0)
- sortOrder: The sort order of the columnToSortOn
-->
<#macro organisationsTable numOrganisationsShown sEmptyTable columnToSortOn sortOrder>
<script type="text/javascript" charset="utf-8">

    /* organisation list */
    var aDataSet = [
      <#list linkedOrganisations as o>
          ['<a id="editLink_${o.key}" href="organisation?id=${o.key}">${o.name!"???"}</a>',
           '${o.alias!}',
           '<#if o.canHost><@s.text name="basic.yes"/><#else><@s.text name="basic.no"/></#if>',
           'DataCite']<#if o_has_next>,</#if>
      </#list>
    ];

    $(document).ready(function() {
        $('#tableContainer').html( '<table cellpadding="3" cellspacing="3" border="0" class="display" id="rtable"></table>' );
        $('#rtable').dataTable( {
            "aaData": aDataSet,
            "iDisplayLength": ${numOrganisationsShown},
            "bLengthChange": false,
            "bAutoWidth": false,
            "oLanguage": {
                "sEmptyTable": "<@s.text name="${sEmptyTable}"/>",
                "sZeroRecords": "<@s.text name="dataTables.sZeroRecords"/>",
                "sInfo": "<@s.text name="dataTables.sInfo.versions"/>",
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
                { "sTitle": "<@s.text name="admin.organisation.doiRegistrationAgency"/>", "bSearchable": true}
            ],
            "aaSorting": [[ ${columnToSortOn}, "${sortOrder}" ]],
            "aoColumnDefs": [
                { 'bSortable': true, 'aTargets': [ 0,1,2,3] }
            ]
        } );
    } );
</script>
</#macro>