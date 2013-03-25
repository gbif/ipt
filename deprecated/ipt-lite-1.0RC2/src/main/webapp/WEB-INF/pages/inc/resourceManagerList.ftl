<#assign display=JspTaglibs["http://displaytag.sf.net"] />
<#include "/WEB-INF/pages/inc/globalVars.ftl">  

<@display.table name="resources" class="table" requestURI="" id="res" export=false pagesize=25>
    <@display.column property="title" sortable=true title='${struts.getText("resource.title")}' href="resource.html" media="html" paramId="resource_id" paramProperty="id"/>
    <@display.column property="modified" sortable=true title='${struts.getText("resource.modified")}' format="{0,date,${datePattern}}"/>
    <@display.column property="creator.fullName" sortable=true title='${struts.getText("resource.creator")}'/>
    <@display.column property="type" sortable=true title='${struts.getText("resource.type")}'/>

    <#if (res?? && res.isPublic())>
      <@display.column value='<a href="${cfg.getWebappURL("eml.xml")}?resource_id=${res.id}">EML</a>' title='${struts.getText("resource.metadata")}' />
      <@display.column value='<a href="${cfg.getWebappURL("archive.do")}?resource_id=${res.id}">download</a>' title='${struts.getText("resource.archive")}' />
    <#else>
      <@display.column value="---" title='${struts.getText("resource.metadata")}' />
      <@display.column value="---" title='${struts.getText("resource.archive")}' />
    </#if>

    <@display.setProperty name="paging.banner.item_name"><@s.text name="resourceList.resource"/></@display.setProperty>
    <@display.setProperty name="paging.banner.items_name"><@s.text name="resourceList.resources"/></@display.setProperty>
</@display.table>


<script type="text/javascript">
    highlightTableRows("res");
</script>
