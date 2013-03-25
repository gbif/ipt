<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="resourceList.title"/></title>
    <meta name="heading" content="<s:text name='resourceList.heading'/>"/>
    <meta name="menu" content="MainMenu"/>
    <meta name="submenu" content="manage"/>
</head>


<%@ include file="/WEB-INF/pages/inc/resourceList.jsp"%>

<script type="text/javascript">
    highlightTableRows("resourceList");
</script>

<br/>