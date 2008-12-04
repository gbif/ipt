<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/common/taglibs.jsp"%>

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
			    <c:choose>
			        <c:when test='${currentSubMenu == "manage"}'>
			        	<%@ include file="/common/submenu_actions_new.jsp" %>
			        	<%@ include file="/common/submenu_recent.jsp" %>						
			        </c:when>

			        <c:when test='${currentSubMenu == "manage_resource"}'>
		                <s:a href="%{resourcePortal}">
		                	<h1 id="resourceName">${currentResourceTitle}</h1>
		                </s:a>
			        	<%@ include file="/common/submenu_actions_manage.jsp" %>
			        	<%@ include file="/common/submenu_recent.jsp" %>						
			        </c:when>
			        
			        <c:when test='${currentSubMenu == "eml"}'>
		                <s:a href="%{resourcePortal}">
		                	<h1 id="resourceName">${currentResourceTitle}</h1>
		                </s:a>
			        	<%@ include file="/common/submenu_actions_eml.jsp" %>
					</c:when>
								        
			        <c:when test='${currentSubMenu == "occ"}'>
		                <s:a href="%{resourcePortal}">
		                	<h1 id="resourceName">${currentResourceTitle}</h1>
		                </s:a>
			        	<%@ include file="/common/submenu_search.jsp" %>
						<div id="taxnav">
							<label>Navigate Taxonomy</label>
               				 <%@ include file="/common/taxontree.jsp" %>
						</div>
						<div id="locnav">
							<label>Navigate Geography</label>
               				 <%@ include file="/common/regiontree.jsp" %>
						</div>
					</c:when>

			        <c:when test='${currentSubMenu == "tax"}'>
		                <s:a href="%{resourcePortal}">
		                	<h1 id="resourceName">${currentResourceTitle}</h1>
		                </s:a>
			        	<%@ include file="/common/submenu_search.jsp" %>
						<div id="taxnav">
							<label>Navigate Taxonomy</label>
               				 <%@ include file="/common/taxontree.jsp" %>
						</div>
					</c:when>
			        
			        <c:when test='${currentSubMenu == "meta"}'>
	                	<h1>Metadata Repo</h1>
			        	<%@ include file="/common/submenu_search.jsp" %>
			        	<%@ include file="/common/submenu_recent.jsp" %>
						<div id="tagcloud">
							<label>Keyword Cloud</label>
							<div class="tagCloud">
						      <a rel="tag" class="tag cloud2" href="">river</a>
						      <a rel="tag" class="tag cloud5" href="">mountain</a>
						      <a rel="tag" class="tag cloud3" href="">tree</a>
						      <a rel="tag" class="tag cloud9" href="">monitoring</a>
						      <a rel="tag" class="tag cloud2" href="">berlin</a>
						      <a rel="tag" class="tag cloud0" href="">seaweed</a>
						      <a rel="tag" class="tag cloud6" href="">birds</a>
						      <a rel="tag" class="tag cloud1" href="">longterm monitoring</a>
						      <a rel="tag" class="tag cloud0" href="">monsune</a>
						      <a rel="tag" class="tag cloud4" href="">impact study</a>
						      <a rel="tag" class="tag cloud1" href="">turkish</a>
							</div>
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
