<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="occResourceList.title"/></title>
    <meta name="heading" content="<fmt:message key='occResourceList.heading'/>"/>
    <meta name="menu" content="OccResourceMenu"/>
</head>

<display:table name="activeDatasources" class="table" requestURI="" id="activeDatasources" export="false" pagesize="25">
    <display:column property="id" sortable="true" href="/manage/occ/resource.html" media="html"
        paramId="resource_id" paramProperty="id" titleKey="resource.id"/>
    <display:column property="title" sortable="true" titleKey="resource.title"/>
    <display:column property="validConnection" sortable="true" titleKey="occResource.isValidConnection"/>

    <display:setProperty name="paging.banner.item_name"><fmt:message key="occResourceList.resource"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><fmt:message key="occResourceList.resources"/></display:setProperty>
</display:table>

<script type="text/javascript">
    highlightTableRows("activeDatasources");
</script>
