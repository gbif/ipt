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

<%@ include file="/WEB-INF/pages/inc/resourceList.jsp"%>

<c:out value="${buttons}" escapeXml="false" />

<script type="text/javascript">
    highlightTableRows("resourceList");
</script>

<br/>