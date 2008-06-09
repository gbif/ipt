<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="occResourceOverview.title"/></title>
    <meta name="heading" content="<s:property value="mapping.resource.title"/>"/>
</head>

<s:form id="viewMappingForm" action="saveMapping" method="post" validate="true">
    <li style="display: none">
        <s:hidden name="mapping_id" value="%{mapping.id}"/>
        <s:hidden name="extension_id" value="%{extension_id}"/>
        <s:hidden name="resource_id" value="%{resource_id}"/>
    </li>

	<ul>
	<s:iterator value="mapping.extension.properties" status="propertyStatus">
	<li>
		<s:property value="name" />
	</li>
	</s:iterator>
	</ul>
	    	
    <li class="buttonBar bottom">
        <s:submit cssClass="button" method="save" key="button.save" theme="simple"/>
        <c:if test="${not empty mapping.id}">
            <s:submit cssClass="button" method="delete" key="button.delete"
                onclick="return confirmDelete('ViewMapping')" theme="simple"/>
        </c:if>
        <s:submit cssClass="button" method="cancel" key="button.cancel" theme="simple"/>
    </li>
</s:form>

<br />
