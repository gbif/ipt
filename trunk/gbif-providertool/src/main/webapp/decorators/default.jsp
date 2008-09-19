<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/common/taglibs.jsp"%>
<s:url id="resourceLink" action="resource" includeParams="none">
	<s:param name="resource_id" value="resource_id" />
</s:url>
<s:url id="resourcePortal" action="resource" namespace="/" includeParams="none">
	<s:param name="resource_id" value="resource_id" />
</s:url>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
    <head>
        <%@ include file="/common/meta.jsp" %>
        <link rel="stylesheet" type="text/css" media="all" href="<c:url value='/styles/${appConfig["csstheme"]}/layout-navtop-subright.css'/>" />
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

            <div id="sub">
                <s:a href="%{resourcePortal}">
                	<h1 id="resourceName">${currentResourceTitle}</h1>
                </s:a>

			    <c:choose>
			        <c:when test='${currentSubMenu == "manage"}'>
						<div id="actions">
							<label>Resource Actions</label>
							<ul class="plain">
								<s:url id="newRes" action="addResource" includeParams="none">
									<s:param name="resource_id" value="" />
								</s:url>
								<li><s:a href="%{newRes}">New Resource</s:a></li>

								<s:url id="listRes" action="resources" includeParams="none"/>
								<li><s:a href="%{listRes}">List Resources</s:a></li>
							</ul>
						</div>
						
						<div id="recentlyViewedResources">
							<label>Recent Resources</label>
							<ul class="plain">
								<s:iterator value="#session.recentResources" status="resstatus">
									<s:url id="recentLink" action="resource" includeParams="none">
										<s:param name="resource_id" value="value" />
									</s:url>
									<li><s:a href="%{recentLink}"><s:property value="label"/></s:a></li>
								</s:iterator>
							</ul>
						</div>
			        </c:when>
			        
			        <c:when test='${currentSubMenu == "resource"}'>
						<div id="fullsearch">
							<label>Fulltext Search</label>
							<s:form name="search" theme="simple">
								<s:textfield name="q" theme="simple"/>
							</s:form>
						</div>
		
						<div id="taxnav">
							<label>Navigate Taxonomy</label>
               				 <%@ include file="/common/taxontree.jsp" %>
						</div>
		
						<div id="locnav">
							<label>Navigate Geography</label>
               				 <%@ include file="/common/regiontree.jsp" %>
						</div>
					</c:when>
					
			        <c:otherwise>
			            <!-- NO SUBMENU -->
					</c:otherwise>
			    </c:choose>
			    
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
