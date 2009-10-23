<#assign display=JspTaglibs["http://displaytag.sf.net"] />
<#include "/WEB-INF/pages/inc/globalVars.ftl">  

<@display.table name="resources" class="table" requestURI="" id="resourceList" export=false pagesize=25>
    <@display.column property="title" sortable=true title='${struts.getText("resource.title")}' href="resource.html" media="html" paramId="resourceId" paramProperty="id"/>
    <@display.column property="modified" sortable=true title='${struts.getText("resource.modified")}' format="{0,date,${datePattern}}"/>
    <@display.column property="creator.fullName" sortable=true title='${struts.getText("resource.creator")}'/>
    <@display.column property="status" sortable=true title='${struts.getText("resource.status")}'/>
    <@display.column property="type" sortable=true title='${struts.getText("resource.type")}'/>

    <@display.setProperty name="paging.banner.item_name"><@s.text name="resourceList.resource"/></@display.setProperty>
    <@display.setProperty name="paging.banner.items_name"><@s.text name="resourceList.resources"/></@display.setProperty>
</@display.table>


<script type="text/javascript">
    highlightTableRows("resourceList");
</script>
