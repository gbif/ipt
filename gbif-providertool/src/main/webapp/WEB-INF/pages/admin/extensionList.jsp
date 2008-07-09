<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceList.title"/></title>
    <meta name="heading" content="<s:text name='extensionList.heading'/>"/>
    <meta name="menu" content="OccResourceMenu"/>
</head>

<display:table name="extensions" uid="ext" class="table" requestURI="" export="true" pagesize="25">
    <display:column property="name" sortable="true" titleKey="extension.name" 
    	href="link" media="html" />
    <display:column property="namespace" sortable="true" titleKey="extension.namespace"/>
    <display:column sortable="true" titleKey="extension.propertyCount"
    	href="extension.html" media="html" paramId="id" paramProperty="id">
    	 ${fn:length(ext.properties)}
    </display:column>
    <display:setProperty name="paging.banner.item_name"><s:text name="extensionList.extension"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><s:text name="extensionList.extensions"/></display:setProperty>
</display:table>

<script type="text/javascript">
    highlightTableRows("extensions");
</script>
