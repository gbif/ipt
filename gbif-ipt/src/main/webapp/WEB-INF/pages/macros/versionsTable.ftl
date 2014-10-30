<#--
versionsTable macro: Generates a data table that has pagination.
- numVersionsShown: The number of versions shown in the table
- sEmptyTable: The message shown when there are no resource records in the table
- columnToSortOn: The column to sort on by default (index starting at 0)
- sortOrder: The sort order of the columnToSortOn
- baseURL: IPT baseURL
- shortname: The shortname of the resource
-->
<#macro versionsTable numVersionsShown sEmptyTable baseURL shortname>
<script type="text/javascript" charset="utf-8">

    /* version history list */
    var aDataSet = [
      <#list resource.getVersionHistory() as v>
          [<#if (version?? && v.version == version) || (!version?? && v.version == resource.emlVersion)>'<img class="latestVersion" src="../images/dataTables/forward_enabled_hover.png"/>${v.version!}'<#else>'<img class="latestVersionHidden" src="../images/dataTables/forward_enabled_hover.png"/>${v.version!}'</#if>,
           '${v.released?date}',
           '${v.recordsPublished}',
           '${v.changeSummary!}',
           '${v.doi!}',
           <#if v.modifiedBy??>'${v.modifiedBy.firstname?replace("\'", "\\'")?replace("\"", '\\"')!} ${v.modifiedBy.lastname?replace("\'", "\\'")?replace("\"", '\\"')!}'<#else>""</#if>,
           "<a href='${baseURL}/resource.do?r=${shortname}&amp;v=${v.version!}'>view</a>"]<#if v_has_next>,</#if>
      </#list>
    ];

    $(document).ready(function() {
        $('#vtableContainer').html( '<table cellpadding="3" cellspacing="3" border="0" class="display" id="rtable"></table>' );
        $('#rtable').dataTable( {
            "bFilter": false,
            "aaData": aDataSet,
            "iDisplayLength": ${numVersionsShown},
            "bLengthChange": false,
            "bAutoWidth": false,
            "bSort": false,
            "oLanguage": {
                "sEmptyTable": "<@s.text name="${sEmptyTable}"/>",
                "sZeroRecords": "<@s.text name="dataTables.sZeroRecords"/>",
                "sInfo": "<@s.text name="dataTables.sInfo.versions"/>",
                "sInfoEmpty": "<@s.text name="dataTables.sInfoEmpty"/>",
                "sInfoFiltered": "<@s.text name="dataTables.sInfoFiltered"/>",
                "oPaginate": {
                    "sNext": "<@s.text name="pager.next"/>",
                    "sPrevious": "<@s.text name="pager.previous"/>"

                }
            },
            "aoColumns": [
                { "sTitle": "Version", "bSearchable": false},
                { "sTitle": "Released", "bSearchable": false},
                { "sTitle": "Records", "bSearchable": false},
                { "sTitle": "Change Summary", "bSearchable": false},
                { "sTitle": "DOI Handle", "bSearchable": false},
                { "sTitle": "Published By", "bSearchable": false},
                { "sTitle": "", "bSearchable": false}
            ],
            "aoColumnDefs": [
                { 'bSortable': false, 'aTargets': [ 0,1,2,3,4,5,6] }
            ]
        } );
    } );
</script>
</#macro>