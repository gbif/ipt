<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceList.title"/></title>
    <meta name="heading" content="<s:text name='resourceClass.%{resourceType}'/>"/>
    <meta name="menu" content="MainMenu"/>
    <meta name="submenu" content="manage"/>
</head>

<%@ include file="/WEB-INF/pages/inc/resourceList.jsp"%>

<script type="text/javascript">
    highlightTableRows("resourceList");
</script>