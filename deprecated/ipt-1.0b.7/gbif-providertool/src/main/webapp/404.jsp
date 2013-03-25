<%@ include file="/common/taglibs.jsp"%>

<page:applyDecorator name="default">

<head>
    <title><s:text name="404.title"/></title>
    <meta name="heading" content="<s:text name='404.title'/>"/>
</head>

<p>
    <s:text name="404.message">
        <s:param><s:url action="/index"/></s:param>
    </s:text>
</p>
<p style="text-align: center; margin-top: 20px">
    <a href="http://community.webshots.com/photo/87848122/87848260vtOXvy"
        title="Emerald Lake - Western Canada, click to Zoom In">
    <img  src="<c:url value="/images/404.jpg"/>" alt="Emerald Lake - Western Canada" /></a>
</p>
</page:applyDecorator>