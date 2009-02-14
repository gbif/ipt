<head>
    <title><@s.text name="search.title"/></title>
    <meta name="submenu" content="meta"/>
</head>

<h1>Your Search: ${q!keyword!bbox.toStringShort(3)}</h1>  
<div class="horizontal_dotted_line_large_foo"></div>

<#include "/WEB-INF/pages/inc/resourceList.ftl">  
