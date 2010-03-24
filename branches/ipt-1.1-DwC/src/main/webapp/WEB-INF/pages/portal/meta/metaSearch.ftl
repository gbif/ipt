<head>
    <title><@s.text name="search.title"/></title>
    <meta name="menu" content="ExplorerMenu"/>
    <meta name="submenu" content="meta"/>
    
</head>

<h1><@s.text name="search.yoursearch"/> ${q!keyword!bbox.toStringShort(3)}</h1>  
<div class="horizontal_dotted_line_large_foo"></div>
<div class="break79"></div>
<#include "/WEB-INF/pages/inc/resourceList.ftl">