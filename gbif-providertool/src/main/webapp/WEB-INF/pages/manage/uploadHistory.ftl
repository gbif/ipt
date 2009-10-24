<#assign display=JspTaglibs["http://displaytag.sf.net"] />
<#include "/WEB-INF/pages/inc/globalVars.ftl">  

<head>
    <title><@s.text name="occResourceList.title"/></title>
    <meta name="heading" content="<@s.text name='upload.heading'/>"/>
    <meta name="resource" content="<@s.property value="resource.title"/>"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage"/>
</head>

<@display.table name="uploadEvents" id="uploadEventList" class="table" requestURI="" export=true pagesize=25>
    <@display.column property="executionDate" sortable=true titleKey="uploadEvent.executionDate" format="{0,date,${datePattern}}"/>
    <@display.column property="recordsUploaded" sortable=true titleKey="uploadEvent.recordsUploaded" />
    <@display.column property="recordsDeleted" sortable=true titleKey="uploadEvent.recordsDeleted" />
    <@display.column property="recordsChanged" sortable=true titleKey="uploadEvent.recordsChanged" />
    <@display.column property="recordsAdded" sortable=true titleKey="uploadEvent.recordsAdded" />

    <@display.setProperty name="paging.banner.item_name"><@s.text name="upload.event"/></@display.setProperty>
    <@display.setProperty name="paging.banner.items_name"><@s.text name="upload.events"/></@display.setProperty>
</@display.table>

<@s.form action="resource" method="get">
  <@s.hidden key="resourceId"/>
  <@s.submit cssClass="button" key="button.done" theme="simple"/>
</@s.form>


<script type="text/javascript">
    highlightTableRows("uploadEvents");
</script>
