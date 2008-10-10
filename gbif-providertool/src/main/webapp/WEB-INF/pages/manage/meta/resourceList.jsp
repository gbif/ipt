<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="resourceList.title"/></title>
    <meta name="heading" content="<s:text name='resourceList.heading'/>"/>
    <meta name="menu" content="MainMenu"/>
    <meta name="submenu" content="manage"/>
</head>

<c:set var="buttons">
    <input type="button" style="margin-right: 5px" class="button"
        onclick="location.href='<s:url action="addResource"/>'"
        value="<s:text name="button.add"/>"/>

    <input type="button" class="button" onclick="location.href='<c:url value="/home.html"/>'"
        value="<s:text name="button.done"/>"/>
</c:set>

<c:out value="${buttons}" escapeXml="false" />

<display:table name="resources" class="table" requestURI="" id="resourceList" export="false" pagesize="25">
    <display:column property="title" sortable="true" titleKey="resource.title" href="resource.html" media="html" paramId="resource_id" paramProperty="id"/>
    <display:column property="modified" sortable="true" titleKey="resource.modified" format="{0,date,${datePattern}}"/>
    <display:column property="creator.fullName" sortable="true" titleKey="resource.creator"/>
    <display:column property="type" sortable="true" titleKey="resource.type"/>

    <display:setProperty name="paging.banner.item_name"><s:text name="resourceList.resource"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><s:text name="resourceList.resources"/></display:setProperty>
</display:table>

<c:out value="${buttons}" escapeXml="false" />

<script type="text/javascript">
    highlightTableRows("resourceList");
</script>

<br/>