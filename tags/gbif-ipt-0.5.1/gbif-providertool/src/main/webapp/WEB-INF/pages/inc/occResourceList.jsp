<%@ include file="/common/taglibs.jsp"%>

<display:table name="resources" uid="res" class="table" requestURI="" id="occResourceList" export="false" pagesize="25">
    <display:column property="title" sortable="true" href="resource.html" media="html" paramId="resource_id" paramProperty="id" titleKey="resource.title"/>
    <display:column property="recTotal" sortable="true" titleKey="resource.recordCount"/>
    <display:column property="numTerminalTaxa" sortable="true" titleKey="occResource.numTerminalTaxa"/>
    <display:column property="numRegions" sortable="true" titleKey="occResource.numRegions"/>
	<display:column property="lastUpload.executionDate" sortable="true" titleKey="resource.lastUpload" format="{0,date,${datePattern}}"/>    
    
    <display:setProperty name="paging.banner.item_name"><s:text name="resourceList.resource"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><s:text name="resourceList.resources"/></display:setProperty>
</display:table>

<script type="text/javascript">
    highlightTableRows("occResourceList");
</script>
