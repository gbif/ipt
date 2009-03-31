<%@ include file="/common/taglibs.jsp"%>

<menu:useMenuDisplayer name="Velocity" config="cssHorizontalMenu.vm" permissions="rolesAdapter">
<div id="navContainer">
	<ul id="primary-nav" class="menuList">
	    <menu:displayMenu name="HomeMenu"/>
	    <menu:displayMenu name="ExplorerMenu"/>
	    <menu:displayMenu name="ManagerMenu"/>
	    <menu:displayMenu name="AdminMenu"/>
	    <c:if test="${empty pageContext.request.remoteUser}"><li><a href="<c:url value="/login.jsp"/>"><s:text name="login.title"/></a></li></c:if>
	</ul>
</div>
</menu:useMenuDisplayer>
<div id="fullsearch">
	<div class="searchBox">
		<s:form action="search" namespace="/" name="search" theme="simple" method="get">
			<s:hidden name="resource_id" value="%{resource_id}" />
			<s:textfield name="q" theme="simple" cssClass="searchInput"/>
		</s:form>
	</div>
</div>
