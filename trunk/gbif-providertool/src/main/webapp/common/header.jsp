<%@ include file="/common/taglibs.jsp"%>

<c:if test="${pageContext.request.locale.language != 'en'}">
    <div id="switchLocale"><a href="<c:url value='/?locale=en'/>"><fmt:message key="webapp.name"/> in English</a></div>
</c:if>

<div id="branding">
    <h1><a href="<c:url value='/'/>"><fmt:message key="webapp.name"/></a></h1>
    <p><s:text name="webapp.tagline"/></p>
</div>

<div id="logo" class="right">
	<a href="http://data.gbif.org/"><img src="<c:url value='/images/logo.gif'/>" /></a>
</div>

<hr />

<%-- Put constants into request scope --%>
<appfuse:constants scope="request"/>