<head>
    <title><@s.text name="search.title"/></title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="menu" content="ExplorerMenu"/>
    <meta name="submenu" content="occ"/>
</head>
	

<h1><@s.text name='occsearch.yoursearch'/> ${q}</h1>  
<div class="horizontal_dotted_line_large_foo"></div>

<div class="break55"></div>

<#include "/WEB-INF/pages/inc/occurrenceList.ftl">