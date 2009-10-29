<%@ include file="/common/taglibs.jsp" %>

<title><s:text name="dataaccessfailure.title"/></title>

<head>
    <meta name="heading" content="<s:text name="dataaccessfailure.title"/>"/>
    <meta name="menu" content="AdminMenu"/>
</head>

<p>
    <c:out value="${requestScope.exception.message}"/>
</p>

<!--
<% 
Exception ex = (Exception) request.getAttribute("exception");
ex.printStackTrace(new java.io.PrintWriter(out)); 
%>
-->

<a href="mainMenu.html" onclick="history.back();return false">&#171; Back</a>
