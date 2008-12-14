<%@ include file="/common/taglibs.jsp"%>

<s:url id="resUrl" action="taxDetail" includeParams="none">
	<s:param name="resource_id" value="resource_id" />
</s:url>

<display:table name="taxa" uid="tax" class="table" requestURI="" id="taxonList" export="false" pagesize="50">
    <display:column property="scientificName" sortable="true" href="${resUrl}" media="html" paramId="guid" paramProperty="guid" titleKey="taxon.scientificName"/>
    <display:column property="rank" sortable="true" titleKey="taxon.rank"/>
    <display:column property="dwcRank" sortable="true" titleKey="taxon.rank"/>
	<display:column property="taxonomicStatus" sortable="true" titleKey="taxon.taxStatus"/>    
    <display:column property="nomenclaturalStatus" sortable="true" titleKey="taxon.nomStatus"/>
    
    <display:setProperty name="paging.banner.item_name"><s:text name="taxonList.taxon"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><s:text name="taxonList.taxa"/></display:setProperty>
</display:table>

<script type="text/javascript">
    highlightTableRows("taxonList");
</script>