<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="launchActionList.title"/></title>
    <meta name="heading" content="<s:text name='launchActionList.heading'/>"/>
    <meta name="menu" content="LaunchActionMenu"/>
</head>

<c:set var="buttons">
    <input type="button" style="margin-right: 5px" class="button"
        onclick="location.href='<c:url value="/editLaunchAction.html"/>'"
        value="<s:text name="button.add"/>"/>

    <input type="button" class="button" onclick="location.href='<c:url value="/mainMenu.html"/>'"
        value="<s:text name="button.done"/>"/>
</c:set>

<c:out value="${buttons}" escapeXml="false" />

<display:table name="launchActions" class="table" requestURI="" id="launchActionList" export="true" pagesize="25">
    <display:column property="id" sortable="true" href="editLaunchAction.html" media="html"
        paramId="id" paramProperty="id" titleKey="launchAction.id"/>
    <display:column property="id" media="csv excel xml pdf" titleKey="launchAction.id"/>
    <display:column property="methodName" sortable="true" titleKey="launchAction.methodName"/>
    <display:column property="i18nKey" sortable="true" titleKey="launchAction.i18nKey"/>
    <display:column property="fullClassName" sortable="true" titleKey="launchAction.fullClassName"/>
    <display:column property="methodParams" sortable="true" titleKey="launchAction.methodParams"/>
    <display:column property="instanceParam" sortable="true" titleKey="launchAction.instanceParam"/>

    <display:setProperty name="paging.banner.item_name"><s:text name="launchActionList.launchAction"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><s:text name="launchActionList.launchActions"/></display:setProperty>

    <display:setProperty name="export.excel.filename"><s:text name="launchActionList.title"/>.xls</display:setProperty>
    <display:setProperty name="export.csv.filename"><s:text name="launchActionList.title"/>.csv</display:setProperty>
    <display:setProperty name="export.pdf.filename"><s:text name="launchActionList.title"/>.pdf</display:setProperty>
</display:table>

<c:out value="${buttons}" escapeXml="false" />

<script type="text/javascript">
    highlightTableRows("launchActionList");
</script>
