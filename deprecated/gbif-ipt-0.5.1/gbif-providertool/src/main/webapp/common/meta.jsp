<%@ include file="/common/taglibs.jsp"%>
<c:set var="currentMenu" scope="request"><decorator:getProperty property="meta.menu"/></c:set>
<c:set var="currentSubMenu" scope="request"><decorator:getProperty property="meta.submenu"/></c:set>
<c:set var="currentResourceTitle" scope="request"><decorator:getProperty property="meta.resource"/></c:set>
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
			<link href="/data/rss.xml" rel="alternate" type="application/rss+xml" title="RSS Feed for all resources" />
          </c:when>        
	      <c:when test='${currentSubMenu == "manage"}'>
          </c:when>        
          <c:when test='${currentSubMenu == "occ"}'>
	        <c:if test='${resource_id != null}'>
				<link href="/data/${resource_id}/rss.xml" rel="alternate" type="application/rss+xml" title="${currentResourceTitle} RSS Feed" />
			</c:if>
			<link rel="STYLESHEET" type="text/css" href="<c:url value='/scripts/dhtmlxtree/dhtmlxtree.css'/>">
			<script  src="<c:url value='/scripts/dhtmlxtree/dhtmlxcommon.js'/>"></script>
			<script  src="<c:url value='/scripts/dhtmlxtree/dhtmlxtree.js'/>"></script>
          </c:when>        
	    </c:choose>        