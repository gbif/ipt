<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="occResourceOverview.title"/></title>
    <meta name="heading" content="<s:property value="mapping.resource.title"/>"/>
</head>

<h2><s:property value="mapping.extension.name"/> Extension</h2>
<s:form id="viewMappingForm" action="saveMapping" method="post" validate="true">
    <li style="display: none">
        <s:hidden name="mapping_id" value="%{mapping.id}"/>
        <s:hidden name="extension_id" value="%{extension_id}"/>
        <s:hidden name="resource_id" value="%{resource_id}"/>
    </li>
    
    <s:textfield key="mapping.viewSql" required="true" cssClass="text large"/>

    <s:a href="">preview data</s:a>

    <div id="preview">
<table dojoType="dijit.Grid" data="store" style="height: 100px; width: 300px;">
	<thead>
		<tr>
			<th attribute="preview_col_id" dataType="String">ID</th>
			<s:iterator value="columns" status="columnStatus">
				<th field="preview_col_%{#columnStatus.count}" dataType="String">col<s:property value="#columnStatus.count"/></th>
			</s:iterator>
		</tr>
	</thead>
</table>
    </div>
    
    <div id="mappingTable">
    <table>
	<s:iterator value="mapping.extension.properties" status="propertyStatus">
      <tr>
        <th>
			<s:property value="#propertyStatus.count"/>
			<s:a href="%{link}"><s:property value="name" /></s:a>
        </th>
        <td>
			<s:select id="property_%{id}" name="property_%{id}" list="mapping.extension.properties" listKey="id" listValue="name"/>
        </td>
      </tr>
	</s:iterator>
    </table>
	</div>	
		    	
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
