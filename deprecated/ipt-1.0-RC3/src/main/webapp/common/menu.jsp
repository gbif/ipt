<%@ include file="/common/taglibs.jsp"%>

<div id="navContainer">
<ul id="primary-nav" class="menuList">
	<li><a title='<s:text name="menu.home"/>'
		<c:if test='${currentMenu == "HomeMenu"}'>class="current"</c:if>
		href='<c:url value="/index.html"/>'><s:text name="menu.home" /></a></li>
	<li><a title='<s:text name="menu.explorer"/>'
		<c:if test='${currentMenu == "ExplorerMenu"}'>class="current"</c:if>
		href='<c:url value="/resources.html"/>'><s:text
		name="menu.explorer" /></a></li>
	<c:choose>
		<c:when test="${empty pageContext.request.remoteUser}">
			<li><a href="<c:url value="/login.jsp"/>"><s:text
				name="login.title" /></a></li>
		</c:when>
		<c:otherwise>

			<s:set name="authorisedAdmin" value="false" />
			<s:set name="authorisedManager" value="false" />
			<s:iterator
				value="#session.SPRING_SECURITY_CONTEXT.authentication.authorities">
				<s:if test='authority in {"ROLE_ADMIN","ROLE_MANAGER"}'>
					<s:set name="authorisedManager" value="true" />
				</s:if>
				<s:if test='authority == "ROLE_ADMIN"'>
					<s:set name="authorisedAdmin" value="true" />
				</s:if>
			</s:iterator>

			<s:if test='#authorisedManager'>
				<li><a title='<s:text name="menu.manager"/>'
					<c:if test='${currentMenu == "ManagerMenu"}'>class="current"</c:if>
					href='<c:url value="/manage/resources.html?resourceType=meta"/>'><s:text
					name="menu.manager" /></a></li>
			</s:if>
			<s:if test='#authorisedAdmin'>
				<li class="menubar"><a title='<s:text name="menu.admin"/>'
					<c:if test='${currentMenu == "AdminMenu"}'>class="current"</c:if>
					href='<c:url value="/admin/config.html"/>'><s:text
					name="menu.admin" /></a>
				<ul>
					<li><a title='<s:text name="menu.admin.config"/>'
						href='<c:url value="/admin/config.html"/>'><s:text
						name="menu.admin.config" /></a></li>
					<li><a title='<s:text name="menu.admin.extensions"/>'
						href='<c:url value="/admin/extensions.html"/>'><s:text
						name="menu.admin.extensions" /></a></li>
					<li><a title='<s:text name="menu.admin.thesaurus"/>'
						href='<c:url value="/admin/vocabularies.html"/>'><s:text
						name="menu.admin.thesaurus" /></a></li>
					<li><a title='<s:text name="menu.admin.users"/>'
						href='<c:url value="/admin/users.html"/>'><s:text
						name="menu.admin.users" /></a></li>
					<!--
			<li>
				<a title='<s:text name="menu.admin.resources"/>' href='<c:url value="/admin/resources.html"/>'><s:text name="menu.admin.resources"/></a>	    
    		</li>
    		-->
					<li class="last"><a
						title='<s:text name="menu.admin.activeUsers"/>'
						href='<c:url value="/admin/activeUsers.html"/>'><s:text
						name="menu.admin.activeUsers" /></a></li>
				</ul>
				</li>
			</s:if>
		</c:otherwise>
	</c:choose>
</ul>

</div>



<div id="fullsearch">
<div class="searchBox"><s:form action="search" namespace="/"
	name="search" theme="simple" method="get">
	<s:hidden name="resourceId" value="%{resourceId}" />
	<s:textfield name="q" theme="simple" cssClass="searchInput" />
</s:form></div>
</div>


