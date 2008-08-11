<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ include file="/common/taglibs.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
    <head>
        <%@ include file="/common/meta.jsp" %>
        <title><decorator:title/> | <s:text name="webapp.name"/></title>

        <link rel="stylesheet" type="text/css" media="all" href="<c:url value='/styles/${appConfig["csstheme"]}/theme.css'/>" />
        <link rel="stylesheet" type="text/css" media="print" href="<c:url value='/styles/${appConfig["csstheme"]}/print.css'/>" />

        <script type="text/javascript" src="<c:url value='/scripts/prototype.js'/>"></script>
        <script type="text/javascript" src="<c:url value='/scripts/scriptaculous.js'/>"></script>
        <script type="text/javascript" src="<c:url value='/scripts/global.js'/>"></script>

        <decorator:head/>
    </head>
<body<decorator:getProperty property="body.id" writeEntireProperty="true"/><decorator:getProperty property="body.class" writeEntireProperty="true"/><decorator:getProperty property="body.onload" writeEntireProperty="true"/> >
    <div id="page">
        <div id="header" class="clearfix">
            <jsp:include page="/common/header.jsp"/>
        </div>

        <div id="content" class="clearfix">
            <div id="main">
                <%@ include file="/common/messages.jsp" %>
                
                <h1><decorator:getProperty property="meta.heading"/></h1>

                <decorator:body/>
            </div><!-- end main -->

            <c:set var="currentMenu" scope="request"><decorator:getProperty property="meta.menu"/></c:set>
            <c:set var="currentSubMenu" scope="request"><decorator:getProperty property="meta.submenu"/></c:set>

            <div id="sub">
                <h1 id="resourceName"><decorator:getProperty property="meta.resource"/></h1>
			    <c:choose>
			        <c:when test='${currentSubMenu == "manage"}'>
						<div id="actions">
							<label>Resource Actions</label>
							<ul class="plain">
								<s:url id="newRes" action="addResource">
									<s:param name="resource_id" value="" />
								</s:url>
								<li><s:a href="%{newRes}">New Resource</s:a></li>

								<s:url id="listRes" action="resources" />
								<li><s:a href="%{listRes}">List Resources</s:a></li>
							</ul>
						</div>
			        </c:when>
			        
			        <c:when test='${currentSubMenu == "search"}'>
						<div id="fullsearch">
							<label>Fulltext Search</label>
							<s:form name="search" theme="simple">
								<s:textfield name="q" theme="simple"/>
							</s:form>
						</div>
		
						<div id="taxnav">
							<label>Navigate Taxonomy</label>
							<pre>
Plantae
  Asteraceae
    Hieracium
							</pre>
						</div>
		
						<div id="locnav">
							<label>Navigate Geography</label>
							<pre>
Europe
  Great Britain
    Cornwales
							</pre>
						</div>
					</c:when>
					
			        <c:otherwise>
			            <!-- NO SUBMENU -->
					</c:otherwise>
			    </c:choose>
			    
				<div id="recentlyViewedResources">
					<label>Recent Resources</label>
					<ul class="plain">
						<s:iterator value="#session.recentResources" status="resstatus">
							<s:url id="resLink" action="resource" includeParams="none">
								<s:param name="resource_id" value="value" />
							</s:url>
							<li><s:a href="%{resLink}"><s:property value="label"/></s:a></li>
						</s:iterator>
					</ul>
				</div>

            </div><!-- end sub -->

            <div id="nav">
                <div class="wrapper">
                    <h2 class="accessibility">Navigation</h2>
                    <jsp:include page="/common/menu.jsp"/>
                </div>
                <!-- <hr/> -->
            </div><!-- end nav -->
        </div>

        <div id="footer" class="clearfix">
            <jsp:include page="/common/footer.jsp"/>
        </div>
    </div>
</body>
</html>
