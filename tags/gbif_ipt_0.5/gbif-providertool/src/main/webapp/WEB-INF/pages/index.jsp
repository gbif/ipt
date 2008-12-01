<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:property value='cfg.title'/></title>
    <meta name="menu" content="MainMenu"/>
    <meta name="submenu" content="metadata"/>
    <meta name="decorator" content="fullsize"/>
</head>


<h1><s:property value='cfg.title'/></h1>

<div id="about">
	<img class="right" src="<s:property value='cfg.getDescriptionImage()'/>" />
	<s:property value='cfg.getDescription()' escape="false"/>
</div>

<h3>Hosted resources</h3>

<%@ include file="/WEB-INF/pages/inc/dataResourceList.jsp"%>

<s:url id="metaRepoUrl" action="resources"/>
<p>
You can find additional resource descriptions in the <s:a href="%{metaRepoUrl}">local metadata repository</s:a>.
</p>
