<#assign display=JspTaglibs["http://displaytag.sf.net"] />
<#include "/WEB-INF/pages/inc/globalVars.ftl">  

<@display.table name="occurrences" uid="occ" class="table" requestURI="" id="occurrenceList" export=false pagesize=50>
    <@display.column property="catalogNumber" sortable=true href="occDetail.html?resourceId=${resourceId?c}" media="html" paramId="guid" paramProperty="guid" titleKey="dwc.catalogNumber"/>
    <@display.column property="collectionCode" sortable=true titleKey="dwc.collectionCode"/>
    <@display.column property="taxon" sortable=true titleKey="dwc.taxon"/>    
    <@display.column property="country" sortable=true maxLength=50 titleKey="dwc.country"/>
    <@display.column property="region" sortable=true maxLength=50 titleKey="dwc.region"/>    
    <@display.column property="eventDate" sortable=true titleKey="dwc.eventDate" />    
    
    <@display.setProperty name="paging.banner.item_name"><@s.text name="dwcList.occurrence"/></@display.setProperty>
    <@display.setProperty name="paging.banner.items_name"><@s.text name="dwcList.occurrences"/></@display.setProperty>
</@display.table>

<script type="text/javascript">
    highlightTableRows("occurrenceList");
</script>