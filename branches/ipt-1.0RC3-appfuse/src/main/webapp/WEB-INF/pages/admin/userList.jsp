<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="userList.title"/></title>
    <meta name="heading" content="<s:text name='userList.heading'/>"/>
    <meta name="decorator" content="fullsize"/>
    <meta name="menu" content="AdminMenu"/>
</head>

<div class="break55"></div>
<display:table name="users" cellspacing="0" cellpadding="0" requestURI="" 
    defaultsort="1" id="users" pagesize="25" class="table" export="true">
    <display:column property="username" escapeXml="true" sortable="true" titleKey="user.username" style="width: 25%"
        url="/admin/editUser.html?from=list" paramId="id" paramProperty="id"/>
    <display:column property="fullName" escapeXml="true" sortable="true" titleKey="activeUsers.fullName" style="width: 34%"/>
    <display:column property="email" sortable="true" titleKey="user.email" style="width: 25%" autolink="true" media="html"/>
    <display:column property="email" titleKey="user.email" media="csv xml excel pdf"/>
    <display:column sortProperty="enabled" sortable="true" titleKey="user.enabled" style="width: 16%; padding-left: 15px" media="html">
        <input type="checkbox" disabled="disabled" <c:if test="${users.enabled}">checked="checked"</c:if>/>
    </display:column>
    <display:column property="enabled" titleKey="user.enabled" media="csv xml excel pdf"/>

    <display:setProperty name="paging.banner.item_name" value="user"/>
    <display:setProperty name="paging.banner.items_name" value="users"/>

    <display:setProperty name="export.excel.filename" value="User List.xls"/>
    <display:setProperty name="export.csv.filename" value="User List.csv"/>
    <display:setProperty name="export.pdf.filename" value="User List.pdf"/>
</display:table>
<div>
	<c:set var="buttons">
	    <input type="button" style="margin-right: 5px"
	        onclick="location.href='<c:url value="/admin/editUser.html?method=Add&from=list"/>'"
	        value="<s:text name="button.add"/>"/>
	    
	    <input type="button" onclick="location.href='<c:url value="/index.html"/>'"
	        value="<s:text name="button.done"/>"/>
	</c:set>
	<c:out value="${buttons}" escapeXml="false" />
</div>	

<script type="text/javascript">
    highlightTableRows("users");
</script>
