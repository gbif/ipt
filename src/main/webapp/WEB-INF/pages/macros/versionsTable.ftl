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
    <script charset="utf-8">

      <#assign isDataPackage = resource.isDataPackage() />

        /* version history list */
        var aDataSet = [
            <#list resource.getVersionHistory() as v>
            /* only show released versions. The version must be public unless user has manager rights in which case they can see all released versions */
            <#if v.released?? && ((v.publicationStatus == 'PUBLIC' || v.publicationStatus == 'REGISTERED') || managerRights) >
            [<#if (version?? && v.version == version.toPlainString()) || (!version?? && v.version == resource.emlVersion.toPlainString())>'<img class="latestVersion" src="${baseURL}/images/dataTables/forward_enabled_hover.png"/>${v.version!}'<#else>'<img class="latestVersionHidden" src="${baseURL}/images/dataTables/forward_enabled_hover.png"/><a href="${baseURL}/resource?r=${shortname}&amp;v=${v.version!}">${v.version!}</a>'</#if>,
                '${(v.released?date?string("yyyy-MM-dd HH:mm:ss"))!""}',
                '${v.recordsPublished}',
                <#if v.changeSummary?has_content>"${v.changeSummary?replace("\'", "\\'")?replace("\"", '\\"')?replace("[\r\n]+", "<br>",'r')}&nbsp;<#if managerRights && !isPreviewPage><a href='${baseURL}/manage/history.do?r=${resource.shortname}&v=${v.version}'><@s.text name='button.edit'/></a></#if>"<#else>"<@s.text name="publishing.changeSummary.default"/>&nbsp;<#if managerRights && !isPreviewPage><a href='${baseURL}/manage/history.do?r=${resource.shortname}&v=${v.version}'><@s.text name='button.edit'/></a></#if>"</#if>,
                <#if v.doi?? && v.status=="PUBLIC">'${v.doi!}'<#else>''</#if>,
                <#if v.modifiedBy??>'${v.modifiedBy.firstname?replace("\'", "\\'")?replace("\"", '\\"')!} ${v.modifiedBy.lastname?replace("\'", "\\'")?replace("\"", '\\"')!}'<#else>""</#if>]<#if v_has_next>,</#if>
            </#if>
            </#list>
        ];

        $(document).ready(function() {
            $('#vtableContainer').html( '<table class="hover row-border dataTable compact" id="rtable"></table>' );
            $('#rtable').dataTable( {
                "searching": false,
                "data": aDataSet,
                "pageLength": ${numVersionsShown},
                "lengthChange": false,
                "autoWidth": false,
                "ordering": false,
                "language": {
                    "emptyTable": "<@s.text name="${sEmptyTable}"/>",
                    "zeroRecords": "<@s.text name="dataTables.sZeroRecords.versions"/>",
                    "info": "<@s.text name="dataTables.sInfo"/>",
                    "infoEmpty": "<@s.text name="dataTables.sInfoEmpty"/>",
                    "infoFiltered": "<@s.text name="dataTables.sInfoFiltered"/>",
                    "paginate": {
                        "next": "<@s.text name="pager.next"/>",
                        "previous": "<@s.text name="pager.previous"/>"

                    }
                },
                "columns": [
                    { "title": "<@s.text name="manage.overview.published.version"/>", "searchable": false},
                    { "title": "<@s.text name="manage.overview.published.released"/>", "searchable": false},
                    { "title": "<@s.text name="portal.home.records"/>", "searchable": false},
                    { "title": "<@s.text name="portal.home.summary"/>", "searchable": false},
                    { "title": "<@s.text name="portal.home.doi"/>", "searchable": false},
                    { "title": "<@s.text name="portal.home.modifiedBy"/>", "searchable": false}
                ],
                "columnDefs": [
                    { 'orderable': false, 'targets': [ 0,1,2,3,4,5] }
                ]
            } );
        } );
    </script>
</#macro>
