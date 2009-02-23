<%@ include file="/common/taglibs.jsp"%>

<%-- <c:if test="${pageContext.request.locale.language != 'en'}"> --%>
    <div id="switchLocale"><a href="<c:url value='/?locale=en'/>"><fmt:message key="webapp.name"/> in English</a></div>
<%-- </c:if> --%>

<div id="branding">
<%--
    <h1><a href="<c:url value='/'/>"><fmt:message key="webapp.name"/></a></h1>
    <p><s:text name="webapp.tagline"/></p>
--%>
	<img src="<c:url value='/images/copyHeader.jpg'/>"/>
</div>

<div id="logo">
	<a href="http://data.gbif.org/"><img src="<c:url value='/images/logo.jpg'/>" /></a>
</div>

<s:if test="resourceType!=null">
<%--
<div id="currentResourceClass">
	<s:text name="resourceClass.%{resourceType}"/><s:if test="resource.isDirty()">*</s:if>
</div>
--%>
</s:if>

<noscript>
<!-- test for javascript -->
<div id="jsalert">
 <h2>You seem to have JavaScript disabled</h2>
 <p>This site is making extensive use of JavaScript so it is advised that you enable JavaScript. 
 If you're running the NoScript Mozilla extenstion you need to add ipt.gbif.org in the list of trusted domains.</p>
</div>
</noscript>

<hr />

<%-- Put constants into request scope --%>
<appfuse:constants scope="request"/> 