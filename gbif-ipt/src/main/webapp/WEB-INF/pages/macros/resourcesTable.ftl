<#--
resourcesTable macro: Generates a data table that has searching, pagination, and sortable columns.
- shownPublicly: Whether the table will be shown publicly, or only internally to managers
- numResourcesShown: The number of resources shown in the table
- sEmptyTable: The message shown when there are no resource records in the table
- columnToSortOn: The column to sort on by default (index starting at 0)
- sortOrder: The sort order of the columnToSortOn
-->
<#macro resourcesTable shownPublicly numResourcesShown sEmptyTable columnToSortOn sortOrder>
<script type="text/javascript" charset="utf-8">
    <#assign emptyString="--">
    <#assign dotDot="..">

    /* resources list */
    var aDataSet = [
      <#list resources as r>
          [<#if r.eml.logoUrl?has_content>'<img class="resourceminilogo" src="${r.eml.logoUrl}" />'<#else>'${emptyString}'</#if>,
           "<a href='${baseURL}<#if !shownPublicly>/manage</#if>/resource.do?r=${r.shortname}'><if><#if r.title?has_content>${r.title?replace("\'", "\\'")}<#else>${r.shortname}</#if></a>",
           <#if r.status=='REGISTERED'>'${r.organisation.alias?replace("\'", "\\'")!r.organisation.name?replace("'", "\'")}'<#else>'<@s.text name="manage.home.not.registered"/>'</#if>,
           <#if r.coreType?has_content && types[r.coreType?lower_case]?has_content>'${types[r.coreType?lower_case]?cap_first!}'<#else>'${emptyString}'</#if>,
           <#if r.subtype?has_content && datasetSubtypes[r.subtype?lower_case]?has_content >'${datasetSubtypes[r.subtype?lower_case]?cap_first!}'<#else>'${emptyString}'</#if>,
           '${r.recordsPublished!0}',
           '${r.modified?date}',
           <#if r.published>'${(r.lastPublished?date)!}'<#else>'<@s.text name="portal.home.not.published"/>'</#if>,
           '${(r.nextPublished?date)!'${emptyString}'}',
           <#if r.status=='PRIVATE'>'<@s.text name="manage.home.visible.private"/>'<#else>'<@s.text name="manage.home.visible.public"/>'</#if>,
           <#if r.creator??>'${r.creator.firstname?replace("\'", "\\'")!} ${r.creator.lastname?replace("\'", "\\'")!}'<#else>'${emptyString}'</#if>]<#if r_has_next>,</#if>
      </#list>
    ];

    $(document).ready(function() {
        $('#rtableContainer').html( '<table cellpadding="3" cellspacing="3" border="0" class="display" id="rtable"></table>' );
        $('#rtable').dataTable( {
            "aaData": aDataSet,
            "iDisplayLength": ${numResourcesShown},
            "bLengthChange": false,
            "bAutoWidth": false,
            "oLanguage": {
                "sEmptyTable": "<@s.text name="${sEmptyTable}"/>",
                "sZeroRecords": "<@s.text name="dataTables.sZeroRecords"/>",
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
                { "sTitle": "<@s.text name="portal.home.logo"/>", "bSearchable": false, "bVisible": <#if shownPublicly>true<#else>false</#if> },
                { "sTitle": "<@s.text name="manage.home.name"/>"},
                { "sTitle": "<@s.text name="manage.home.organisation"/>"},
                { "sTitle": "<@s.text name="manage.home.type"/>"},
                { "sTitle": "<@s.text name="manage.home.subtype"/>"},
                { "sTitle": "<@s.text name="portal.home.records"/>", "bSearchable": false},
                { "sTitle": "<@s.text name="manage.home.last.modified"/>", "bSearchable": false},
                { "sTitle": "<@s.text name="manage.home.last.publication" />", "bSearchable": false},
                { "sTitle": "<@s.text name="manage.home.next.publication" />", "bSearchable": false},
                { "sTitle": "<@s.text name="manage.home.visible"/>", "bSearchable": false, "bVisible": <#if shownPublicly>false<#else>true</#if>},
                { "sTitle": "<@s.text name="portal.home.author"/>", "bVisible": <#if shownPublicly>false<#else>true</#if>}
            ],
            "aaSorting": [[ ${columnToSortOn}, "${sortOrder}" ]],
            "aoColumnDefs": [
                { 'bSortable': false, 'aTargets': [ 0 ] }
            ]
        } );
    } );
</script>
</#macro>