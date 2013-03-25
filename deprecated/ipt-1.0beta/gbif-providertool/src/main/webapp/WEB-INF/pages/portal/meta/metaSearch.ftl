<head>
    <title><@s.text name="search.title"/></title>
    <meta name="submenu" content="meta"/>
</head>

<h2>Your Search: ${q!keyword!bbox.toStringShort(3)}</h2>  

<#include "/WEB-INF/pages/inc/resourceList.ftl">  
