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
	  	<li><a href='<s:url value="/logout.html"/>'><s:text name="user.logout"/></a></li>
	  </c:if>
	</ul>
</div>
<div id="switchLocale" style="display:none" class="right">
  <ul class="plain_m">  
	<li><a href="?<%= request.getQueryString() %>&request_locale=es"><img src="<c:url value='/images/flags/flag_es.gif'/>"/></a></li>
	<li><a href="?<%= request.getQueryString() %>&request_locale=fr"><img src="<c:url value='/images/flags/flag_fr.gif'/>"/></a></li>
	<li><a href="?<%= request.getQueryString() %>&request_locale=de"><img src="<c:url value='/images/flags/flag_de.gif'/>"/></a></li>
	<li><a href="?<%= request.getQueryString() %>&request_locale=en"><img src="<c:url value='/images/flags/flag_en.gif'/>"/></a></li>
  </ul>
	
</div>
 
<div id="logo">
	<a href="<c:url value='/index.html'/>"><img src="<c:url value='/images/logo.jpg'/>" /></a>
</div>

<noscript>
<!-- test for javascript -->
<div id="jsalert">
 <img src="<c:url value='/images/assets/bigWarning.png'/>"/>
 <div class="textColumn">
	 <h2><s:text name="header.nojavascript"/></h2>
	 <p><s:text name="header.jsmessage"/></p>
</div>
</div>
</noscript>

<hr />
