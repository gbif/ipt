<#assign display=JspTaglibs["http://displaytag.sf.net"] />
<#include "/WEB-INF/pages/inc/globalVars.ftl">  
<head>
    <title>${cfg.title}</title>
    <meta name="menu" content="MainMenu"/>
    <meta name="submenu" content="metadata"/>
    <meta name="decorator" content="fullsize"/>
</head>


<h1>${cfg.title}</h1>

<div id="about">
	<img class="right" src="${cfg.getDescriptionImage()}" />
	${cfg.getDescription()}
</div>

<h3>Hosted resources</h3>


<#include "/WEB-INF/pages/inc/dataResourceList.ftl">  

<@s.url id="metaRepoUrl" action="resources"/>
<p>
You can find additional resource descriptions in the <a href="${metaRepoUrl}">local metadata repository</a>.
</p>
