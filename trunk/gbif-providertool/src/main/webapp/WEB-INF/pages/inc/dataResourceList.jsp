<%@ include file="/common/taglibs.jsp"%>

<display:table name="resources" class="table" requestURI="" id="resourceList" export="false" pagesize="5">
    <display:column property="title" sortable="true" titleKey="resource.title" href="resource.html" media="html" paramId="resource_id" paramProperty="id"/>
    <display:column property="modified" sortable="true" titleKey="resource.modified" format="{0,date,${datePattern}}"/>
    <display:column property="creator.fullName" sortable="true" titleKey="resource.creator"/>
    <display:column property="recTotal" sortable="true" titleKey="resource.recordCount"/>
    <display:column property="type" sortable="true" titleKey="resource.type"/>

    <display:setProperty name="paging.banner.item_name"><s:text name="resourceList.resource"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><s:text name="resourceList.resources"/></display:setProperty>
</display:table>

<script type="text/javascript">
    highlightTableRows("resourceList");
</script>
