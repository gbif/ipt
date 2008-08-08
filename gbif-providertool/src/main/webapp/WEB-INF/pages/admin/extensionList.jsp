<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceList.title"/></title>
    <meta name="heading" content="<s:text name='extensionList.heading'/>"/>
    <meta name="menu" content="MainMenu"/>
</head>

<display:table name="extensions" uid="ext" class="table" requestURI="" export="true" pagesize="25">
    <display:column property="name" sortable="true" titleKey="extension.name" 
    	href="extension.html" media="html" paramId="id" paramProperty="id"/>
    <display:column sortable="true" titleKey="extension.propertyCount">
    	 ${fn:length(ext.properties)}
    </display:column>
    <display:column property="link" sortable="true" titleKey="extension.link" autolink="true"  media="html" />
    
    <display:setProperty name="paging.banner.item_name"><s:text name="extensionList.extension"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><s:text name="extensionList.extensions"/></display:setProperty>
</display:table>

<script type="text/javascript">
    highlightTableRows("extensions");
</script>
