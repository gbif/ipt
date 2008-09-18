<%@ include file="/common/taglibs.jsp"%>

<s:url id="resUrl" action="occDetail" includeParams="none">
	<s:param name="resource_id" value="resource_id" />
</s:url>

<display:table name="occurrences" uid="occ" class="table" requestURI="" id="occurrenceList" export="false" pagesize="50">
    <display:column property="catalogNumber" sortable="true" href="${resUrl}" media="html" paramId="guid" paramProperty="guid" titleKey="dwc.catalogNumber"/>
    <display:column property="collectionCode" sortable="true" titleKey="dwc.collectionCode"/>
	<display:column property="taxon" sortable="true" titleKey="dwc.taxon"/>    
    <display:column property="country" sortable="true" titleKey="dwc.country"/>
	<display:column property="region" sortable="true" titleKey="dwc.region"/>    
	<display:column property="earliestDateCollected" sortable="true" titleKey="dwc.earliestDateCollected" />    
	<display:column property="collector" sortable="true" titleKey="dwc.collector"/>    
    
    <display:setProperty name="paging.banner.item_name"><s:text name="dwcList.occurrence"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><s:text name="dwcList.occurrences"/></display:setProperty>
</display:table>

<script type="text/javascript">
    highlightTableRows("occurrenceList");
</script>