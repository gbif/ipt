<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="title"/></title>
  <#assign currentMenu = "manage"/>
  <#include "/WEB-INF/pages/inc/menu.ftl">
  <#include "/WEB-INF/pages/macros/forms.ftl"/>
  <#include "/WEB-INF/pages/macros/resourcesTable.ftl"/>
<script type="text/javascript" language="javascript" src="${baseURL}/js/jquery/jquery-3.2.1.min.js"></script>
<script type="text/javascript" language="javascript" src="${baseURL}/js/jquery/jquery.dataTables.js"></script>
<script type="text/javascript">
    $(document).ready(function(){
        initHelp();
    });
</script>
  <@resourcesTable shownPublicly=false numResourcesShown=10 sEmptyTable="manage.home.resources.none" columnToSortOn=6 sortOrder="desc"/>

<h1 class="rtableTitle"><@s.text name="manage.home.title"/></h1>
<div id="tableContainer"></div>

<div id="new-resource" class="grid_18">

  <h2><@s.text name="manage.resource.create.title"/></h2>
  <#include "inc/create_new_resource.ftl"/>

</div>

  <#include "/WEB-INF/pages/inc/footer.ftl">