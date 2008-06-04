<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="occResourceOverview.title"/></title>
    <meta name="heading" content="<s:property value="occResource.title"/>"/>
</head>

<s:url id="showUrl" action="occResource"><s:param name="id" value="occResource.id"/></s:url>
<div class="actionlinks">
	<s:a href="%{showUrl}">show resource</s:a>
</div>

<form>
	<s:select id="tables" name="tables" list="tables"/>
</form>