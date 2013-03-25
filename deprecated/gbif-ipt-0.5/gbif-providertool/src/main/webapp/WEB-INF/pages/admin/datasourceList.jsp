<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="datasourceList.title"/></title>
    <meta name="heading" content="<s:text name='datasourceList.heading'/>"/>
    <meta name="menu" content="MainMenu"/>
</head>

<display:table name="activeDatasources" class="table" requestURI="" id="activeDatasources" export="false" pagesize="25">
    <display:column property="id" sortable="true" href="/manage/occ/resource.html" media="html"
        paramId="resource_id" paramProperty="id" titleKey="resource.id"/>
    <display:column property="title" sortable="true" titleKey="resource.title"/>
    <display:column property="hasDbConnection" sortable="true" titleKey="occResource.hasDbConnection"/>

    <display:setProperty name="paging.banner.item_name"><s:text name="datasourceList.datasource"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><s:text name="datasourceList.datasources"/></display:setProperty>
</display:table>

<script type="text/javascript">
    highlightTableRows("activeDatasources");
</script>
