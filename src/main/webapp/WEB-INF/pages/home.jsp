<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="mainMenu.title"/></title>
    <meta name="heading" content="<fmt:message key='mainMenu.heading'/>"/>
    <meta name="menu" content="MainMenu"/>
</head>

<p><fmt:message key="mainMenu.message"/></p>

<div class="separator"></div>

<ul class="glassList">
    <li>
        5 <a href="<c:url value='/datasources.html'/>"><fmt:message key="menu.occurrence"/></a>
    </li>
    <li>
        2 <a href="<c:url value='/datasources.html'/>"><fmt:message key="menu.checklist"/></a>
    </li>
    <li>
        113 <a href="<c:url value='/datasources.html'/>"><fmt:message key="menu.resource"/></a>
    </li>
</ul>
