<#include "/WEB-INF/pages/inc/limit.ftl">  
[
<#list nodes as n>
    {
        key: "${n.id?c}",
        isLazy: ${n.isLeafNode()?string('false','true')},
		icon: false,
        title: "${limit(n.label)}",
        tooltip: "${n.label}"
    },
</#list>
]