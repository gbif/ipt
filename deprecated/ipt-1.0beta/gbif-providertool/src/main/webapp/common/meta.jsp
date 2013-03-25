<%@ include file="/common/taglibs.jsp"%>
<c:set var="currentMenu" scope="request"><decorator:getProperty property="meta.menu"/></c:set>
<c:set var="currentSubMenu" scope="request"><decorator:getProperty property="meta.submenu"/></c:set>
<c:set var="currentResourceTitle" scope="request"><decorator:getProperty property="meta.resource"/></c:set>
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
        <link rel="stylesheet" type="text/css" media="all" href="<c:url value='/styles/${appConfig["csstheme"]}/theme.css'/>" />
        <link rel="stylesheet" type="text/css" media="print" href="<c:url value='/styles/${appConfig["csstheme"]}/print.css'/>" />
	    <c:choose>
	      <c:when test='${currentSubMenu == "meta"}'>
			<link href="/data/atom.xml" rel="alternate" type="application/atom+xml" title="RSS Atom Feed for all resources" />
			<link href="/data/rss.xml" rel="alternate" type="application/rss+xml" title="RSS 2.0 Feed for all resources" />
          </c:when>        
          <c:when test='${currentSubMenu == "occ" || currentSubMenu == "tax"}'>
	        <c:if test='${currentSubMenu == "occ"}'>
				<c:set var="taxTreeAction">occTaxon</c:set>
			</c:if>			
			<link rel="STYLESHEET" type="text/css" href="<c:url value='/scripts/dhtmlxtree/dhtmlxtree.css'/>">
			<script  src="<c:url value='/scripts/dhtmlxtree/dhtmlxcommon.js'/>"></script>
			<script  src="<c:url value='/scripts/dhtmlxtree/dhtmlxtree.js'/>"></script>
          </c:when>        
	      <c:when test='${currentSubMenu == "tax"}'>
          </c:when>        
	      <c:when test='${currentSubMenu == "manage" || currentSubMenu == "manage_resource"}'>
          </c:when>        
	    </c:choose>