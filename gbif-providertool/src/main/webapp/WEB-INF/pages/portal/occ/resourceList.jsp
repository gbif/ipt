<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceList.title"/></title>
    <meta name="heading" content="<s:text name='occResourceList.heading'/>"/>
</head>

<display:table name="resources" uid="res" class="table" requestURI="" id="occResourceList" export="false" pagesize="25">
    <display:column property="title" sortable="true" href="resource.html" media="html" paramId="resource_id" paramProperty="id" titleKey="resource.title"/>
	<display:column property="lastUploadDate" sortable="true" titleKey="resource.lastUpload" format="{0,date,${datePattern}}"/>    
    <display:column property="recordCount" sortable="true" titleKey="resource.recordCount"/>
    
    <display:setProperty name="paging.banner.item_name"><s:text name="resourceList.resource"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><s:text name="resourceList.resources"/></display:setProperty>
</display:table>

<script type="text/javascript">
    highlightTableRows("occResourceList");
</script>
