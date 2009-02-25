<%@ include file="/common/taglibs.jsp"%>

<s:if test="%{localeLanguage != null}">
	<s:url id="localeFlag" value="/images/flags/flag_%{localeLanguage}.gif"/>   
	<div class="right" style="padding-right: 23px;">
		<div id="flagContainer">
			<div class="comboFoo">
	  		<a id="locale" href="#"><img src='<s:property value="%{localeFlag}"/>'/></a>
	  	</div>
		</div>
	</div>  		
</s:if> 
<div id="topmenu" class="right">
	<ul class="sepmenu">
	  <c:if test="${pageContext.request.remoteUser != null}">
	  	<li><s:text name="user.status"/> <a href='<s:url value="/editProfile.html"/>'>${pageContext.request.remoteUser}</a></li>
	  	<li><a href='<s:url value="/logout.jsp"/>'><s:text name="user.logout"/></a></li>
	  </c:if>
	</ul>
</div>
<div id="switchLocale" style="display:none" class="right">
  <ul class="plain">  
	<li><a href="<c:url value='/?locale=en'/>"><img src="<c:url value='/images/flags/flag_en.gif'/>"/></a></li>
	<li><a href="<c:url value='/?locale=es'/>"><img src="<c:url value='/images/flags/flag_es.gif'/>"/></a></li>
	<li><a href="<c:url value='/?locale=de'/>"><img src="<c:url value='/images/flags/flag_de.gif'/>"/></a></li>
	<li><a href="<c:url value='/?locale=fr'/>"><img src="<c:url value='/images/flags/flag_fr.gif'/>"/></a></li>
  </ul>
	
</div>

<!-- 
<div id="branding">
	<img src="<c:url value='/images/copyHeader.jpg'/>"/>
</div>
 -->
 
<div id="logo">
	<a href="<c:url value='/index.html'/>"><img src="<c:url value='/images/logo.jpg'/>" /></a>
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