<%@ include file="/common/taglibs.jsp"%>

<page:applyDecorator name="default">

<head>
    <title><s:text name="403.title"/></title>
    <meta name="heading" content="<s:text name='403.title'/>"/>
</head>

<p>
    <s:text name="403.message">
        <fmt:param><c:url value="/"/></fmt:param>
    </s:text>
</p>
<p style="text-align: center; margin-top: 20px">
    <a href="http://community.webshots.com/photo/56793801/56801692jkyHaR"
        title="Hawaii, click to Zoom In">
    <img src="<c:url value="/images/403.jpg"/>" alt="Hawaii" /></a>
</p>
</page:applyDecorator>