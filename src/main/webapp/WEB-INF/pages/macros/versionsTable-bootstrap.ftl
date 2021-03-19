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
            /* only show released versions. The version must be public unless user has manager rights in which case they can see all released versions */
            <#if v.released?? && ((v.publicationStatus == 'PUBLIC' || v.publicationStatus == 'REGISTERED') || managerRights) >
            [<#if (version?? && v.version == version.toPlainString()) || (!version?? && v.version == resource.emlVersion.toPlainString())>'<img class="latestVersion" src="${baseURL}/images/dataTables/forward_enabled_hover.png"/>${v.version!}'<#else>'<img class="latestVersionHidden" src="${baseURL}/images/dataTables/forward_enabled_hover.png"/><a href="${baseURL}/resource?r=${shortname}&amp;v=${v.version!}">${v.version!}</a>'</#if>,
                '${v.released?date}',
                '${v.recordsPublished}',
                <#if v.changeSummary?has_content>"${v.changeSummary?replace("\'", "\\'")?replace("\"", '\\"')}&nbsp;<#if managerRights><a href='${baseURL}/manage/history.do?r=${resource.shortname}&v=${v.version}'><@s.text name='button.edit'/></a></#if>"<#else>"<@s.text name="publishing.changeSummary.default"/>&nbsp;<#if managerRights><a href='${baseURL}/manage/history.do?r=${resource.shortname}&v=${v.version}'><@s.text name='button.edit'/></a></#if>"</#if>,
                <#if v.doi?? && v.status=="PUBLIC">'${v.doi!}'<#else>''</#if>,
                <#if v.modifiedBy??>'${v.modifiedBy.firstname?replace("\'", "\\'")?replace("\"", '\\"')!} ${v.modifiedBy.lastname?replace("\'", "\\'")?replace("\"", '\\"')!}'<#else>""</#if>]<#if v_has_next>,</#if>
            </#if>
            </#list>
        ];

        $(document).ready(function() {
            $('#vtableContainer').html( '<table class="display table table-sm" id="rtable"></table>' );
            $('#rtable').dataTable( {
                "bFilter": false,
                "aaData": aDataSet,
                "iDisplayLength": ${numVersionsShown},
                "bLengthChange": false,
                "bAutoWidth": false,
                "bSort": false,
                "oLanguage": {
                    "sEmptyTable": "<@s.text name="${sEmptyTable}"/>",
                    "sZeroRecords": "<@s.text name="dataTables.sZeroRecords.versions"/>",
                    "sInfo": "<@s.text name="dataTables.sInfo"/>",
                    "sInfoEmpty": "<@s.text name="dataTables.sInfoEmpty"/>",
                    "sInfoFiltered": "<@s.text name="dataTables.sInfoFiltered"/>",
                    "oPaginate": {
                        "sNext": "<@s.text name="pager.next"/>",
                        "sPrevious": "<@s.text name="pager.previous"/>"

                    }
                },
                "aoColumns": [
                    { "sTitle": "<@s.text name="manage.overview.published.version"/>", "bSearchable": false},
                    { "sTitle": "<@s.text name="manage.overview.published.released"/>", "bSearchable": false},
                    { "sTitle": "<@s.text name="portal.home.records"/>", "bSearchable": false},
                    { "sTitle": "<@s.text name="portal.home.summary"/>", "bSearchable": false},
                    { "sTitle": "<@s.text name="portal.home.doi"/>", "bSearchable": false},
                    { "sTitle": "<@s.text name="portal.home.modifiedBy"/>", "bSearchable": false}
                ],
                "aoColumnDefs": [
                    { 'bSortable': false, 'aTargets': [ 0,1,2,3,4,5] }
                ]
            } );
        } );
    </script>
</#macro>
