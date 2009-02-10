<head>
    <title><@s.text name="search.title"/></title>
    <meta name="submenu" content="meta"/>
</head>

<style>
	.modifiedh1{
		margin-top:-13px;
	}
</style>

<h1 class="modifiedh1">Your Search: ${q!keyword!bbox.toStringShort(3)}</h1>  


<#include "/WEB-INF/pages/inc/resourceList.ftl">  
