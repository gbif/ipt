<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="occResourceOverview.title"/></title>
    <meta name="heading" content="<s:property value="mapping.resource.title"/>"/>
</head>

<h2><s:property value="mapping.extension.name"/> Extension</h2>

<s:form action="saveMappingSource" method="post" validate="true">
    <s:hidden key="mapping_id"/>
    <s:hidden key="extension_id"/>
    
    <s:textarea key="mapping.viewSql" required="true" cssClass="text large"/>
    <br/>
    
    <s:submit cssClass="button" key="button.save" theme="simple"/>
    <c:if test="${not empty mapping.viewSql}">
        <s:submit cssClass="button" method="delete" key="button.delete" onclick="return confirmDelete('mapping')" theme="simple"/>
    </c:if>
    <s:submit cssClass="button" method="cancel" key="button.cancel" theme="simple"/>
		    	
</s:form>

<s:if test="${not empty viewColumnHeaders}">
	<table class="table">
		<thead>
			<tr>
				<s:iterator value="viewColumnHeaders" id="header" status="headStat">
				<th><s:property value="header"/></th>
				</s:iterator>
			</tr>
		</thead>
		<tbody>
		<s:iterator value="preview" status="rowStat">
			<tr>
				<s:iterator value="preview[#rowStat.count]" status="colStat">
					<td><s:property value="preview[#rowStat.count][#colStat.count-1]"/></td>
				</s:iterator>
			</tr>
		</s:iterator>
		</tbody>
	</table>

	<s:form action="editMappingProperties" method="post">
        <s:hidden name="mapping_id" value="%{mapping.id}"/>
        <s:submit cssClass="button" key="button.map" theme="simple"/>
	</s:form>
</s:if>

<br />
