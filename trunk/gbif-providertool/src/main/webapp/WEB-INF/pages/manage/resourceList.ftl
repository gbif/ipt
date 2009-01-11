<#assign display=JspTaglibs["http://displaytag.sf.net"] />
<#include "/WEB-INF/pages/inc/globalVars.ftl">  
<head>
    <title><@s.text name="occResourceList.title"/></title>
    <meta name="heading" content="<@s.text name='resourceClass.${resourceType}'/>"/>
    <meta name="menu" content="MainMenu"/>
    <meta name="submenu" content="manage"/>
</head>

<#include "/WEB-INF/pages/inc/resourceList.ftl">  

<script type="text/javascript">
    highlightTableRows("resourceList");
</script>