<%@ include file="/common/taglibs.jsp"%>
<c:set var="currentMenu" scope="request"><decorator:getProperty property="meta.menu"/></c:set>
<c:set var="currentSubMenu" scope="request"><decorator:getProperty property="meta.submenu"/></c:set>
<c:set var="currentResourceTitle" scope="request"><decorator:getProperty property="meta.resource"/></c:set>
<c:set var="currentHeading" scope="request"><decorator:getProperty property="meta.heading"/></c:set>
<c:set var="currentHeading2" scope="request"><decorator:getProperty property="meta.heading2"/></c:set>
<c:set var="taxTreeAction">taxDetail</c:set>
        <title><decorator:title/> | <s:text name="webapp.name"/></title>
        <!-- HTTP 1.1 -->
        <meta http-equiv="Cache-Control" content="no-store"/>
        <!-- HTTP 1.0 -->
        <meta http-equiv="Pragma" content="no-cache"/>
        <!-- Prevents caching at the Proxy Server -->
        <meta http-equiv="Expires" content="0"/>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <link rel="icon" href="<c:url value="/images/favicon.ico"/>"/>
		<link href="<s:url value='/atom.xml'/>" rel="alternate" type="application/atom+xml" title="RSS Atom Feed for all resources" />
		<link rel="stylesheet" type="text/css" media="all" href="<c:url value='/styles/${appConfig["csstheme"]}/theme.css'/>" />
		<script type="text/javascript" src="<c:url value='/scripts/jquery/jquery-1.3.min.js'/>"></script>
		<script type="text/javascript" src="<c:url value='/scripts/jquery/effects.core.min.js'/>"></script>
		<script type="text/javascript" src="<c:url value='/scripts/jquery/effects.highlight.min.js'/>"></script>
		<script type="text/javascript" src="<c:url value='/scripts/global.js'/>"></script>
		
		<s:test value='%{cfg.isGbifAnalytics()}'>
			<script type="text/javascript">
			var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
			document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
			</script>
			<script type="text/javascript">
			try {
			var pageTracker = _gat._getTracker("<s:property value='%{cfg.getGbifAnalyticsKey()}'/>");
			pageTracker._trackPageview();
			} catch(err) {}
			</script>
		</s:test>
		<!-- custom user header START -->
		<s:property value='%{cfg.getHeaderHtml()}'/>
		<!-- custom user header END -->
		
	    <c:choose>
	      <c:when test='${currentSubMenu == "meta"}'>
          </c:when>        
          <c:when test='${currentSubMenu == "occ" || currentSubMenu == "tax"}'>
	        <c:if test='${currentSubMenu == "occ"}'>
				<c:set var="taxTreeAction">occTaxon</c:set>
			</c:if>			
			<link rel="STYLESHEET" type="text/css" href="<c:url value='/scripts/dynatree/skin/ui.dynatree.css'/>">
			<script  src="<c:url value='/scripts/jquery/ui.core.min.js'/>"></script>
			<script  src="<c:url value='/scripts/jquery.cookie.js'/>"></script>
			<script  src="<c:url value='/scripts/dynatree/jquery.dynatree.min.js'/>"></script>
          </c:when>        
	      <c:when test='${currentSubMenu == "tax"}'>
          </c:when>        
	      <c:when test='${currentSubMenu == "manage" || currentSubMenu == "manage_resource"}'>
          </c:when>        
	    </c:choose>