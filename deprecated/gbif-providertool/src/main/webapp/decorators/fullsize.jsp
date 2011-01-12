<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ include file="/common/taglibs.jsp"%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
<%@ include file="/common/meta.jsp"%>
<!-- the 1 column layout -->
<link rel="stylesheet" type="text/css" media="all"
	href="<c:url value='/styles/gbifn/layout-1col.css'/>" />
<decorator:head />
</head>
<body
	<decorator:getProperty property="body.id" writeEntireProperty="true"/>
	<decorator:getProperty property="body.class" writeEntireProperty="true"/>
	<decorator:getProperty property="body.onload" writeEntireProperty="true"/>>
<div id="page">
<div id="header" class="clearfix"><jsp:include
	page="/common/header.jsp" /></div>
<div style="height: 65px; clear: both"><%-- spacer --%></div>

<div id="content" class="clearfix">
<div id="mainfull"><%@ include file="/common/messages.jsp"%>

<c:if test='${currentHeading != "" }'>
	<h1><decorator:getProperty property="meta.heading" /></h1>
	<div class="horizontal_dotted_line_xlarge"></div>
</c:if> <decorator:body /></div>
<!-- end mainfull -->

<div id="nav"><jsp:include page="/common/menu.jsp" /></div>
<!-- end nav --></div>

<div id="footer" class="clearfix"><jsp:include
	page="/common/footer.jsp" /></div>
</div>
</body>
</html>
