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

<s:if test="resourceType!=null">
<div id="currentResourceClass">
	<s:text name="resourceClass.%{resourceType}"/><s:if test="resource.isDirty()">*</s:if>
</div>
</s:if>



<hr />

<%-- Put constants into request scope --%>
<appfuse:constants scope="request"/>


<!-- test for javascript -->
<noscript>
 <p class="reminder">You seem to have JavaScript disabled<br/>
 This site is making extensive use of JavaScript so it is advised that you enable JavaScript. 
 If you're running the NoScript Mozilla extenstion you need to add ipt.gbif.org in the list of trusted domains.
 </p>
</noscript>	    