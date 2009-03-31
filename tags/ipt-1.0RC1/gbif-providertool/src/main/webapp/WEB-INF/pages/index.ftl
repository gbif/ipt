<#assign display=JspTaglibs["http://displaytag.sf.net"] />
<#include "/WEB-INF/pages/inc/globalVars.ftl">  
<head>
    <title>${cfg.ipt.title}</title>
    <meta name="menu" content="HomeMenu"/>
    <meta name="submenu" content="metadata"/>
    <meta name="decorator" content="fullsize"/>
</head>


<h1>${cfg.ipt.title}</h1>

<div id="about">
	<img class="right" src="${cfg.getDescriptionImage()}" />
	${cfg.ipt.description}
</div>

<div class="newline"></div>

<h3>Hosted resources</h3>
<div class="horizontal_dotted_line_xlarge_soft"></div>
<div style="height: 56px; clear: both"></div>
<#include "/WEB-INF/pages/inc/dataResourceList.ftl">  

<@s.url id="metaRepoUrl" action="resources"/>
<p class="tableMoreLink">
You can find additional resource descriptions in the <a href="${metaRepoUrl}">local metadata repository</a>.
</p>
