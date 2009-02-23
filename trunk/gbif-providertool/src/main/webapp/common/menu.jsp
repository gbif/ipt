<%@ include file="/common/taglibs.jsp"%>

<menu:useMenuDisplayer name="Velocity" config="cssHorizontalMenu.vm" permissions="rolesAdapter">
<div id="navContainer">
	<ul id="primary-nav" class="menuList">
<%-- 	    <li class="pad">&nbsp;</li> --%>
	    <c:if test="${empty pageContext.request.remoteUser}"><li><a href="<c:url value="/login.jsp"/>"><s:text name="login.title"/></a></li></c:if>
	    <menu:displayMenu name="ExplorerMenu"/>
	    <menu:displayMenu name="ManagerMenu"/>
	    <menu:displayMenu name="AdminMenu"/>
	    <menu:displayMenu name="UserMenu"/>
	    <menu:displayMenu name="AboutMenu"/>
	</ul>
</div>
</menu:useMenuDisplayer>