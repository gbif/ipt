<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="occResourceOverview.title"/></title>
    <meta name="heading" content="<s:property value="mapping.resource.title"/>"/>
    <script type="text/javascript" src="<c:url value='/scripts/jquery.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/scripts/flexigrid.js'/>"></script>
</head>

<h2><s:property value="mapping.extension.name"/> Extension</h2>
<s:form id="viewMappingForm" action="saveMapping" method="post" validate="true">
    <li style="display: none">
        <s:hidden name="mapping_id" value="%{mapping.id}"/>
        <s:hidden name="extension_id" value="%{extension_id}"/>
    </li>
    
    <s:textarea key="mapping.viewSql" value="%{mapping.viewSql}" required="true" cssClass="text large"/>
		    	
    <li class="buttonBar bottom">
        <s:submit cssClass="button" method="save" key="button.save" theme="simple"/>
        <c:if test="${not empty mapping.id}">
            <s:submit cssClass="button" method="delete" key="button.delete"
                onclick="return confirmDelete('ViewMapping')" theme="simple"/>
        </c:if>
        <s:submit cssClass="button" method="cancel" key="button.cancel" theme="simple"/>
    </li>
</s:form>

<div id="preview">
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
</div>


<s:form action="saveMapping" method="post" > 
     <li style="display: none">
        <s:hidden name="mapping_id" value="%{mapping.id}"/>
        <s:hidden name="extension_id" value="%{extension_id}"/>
    </li>
 
    <s:iterator value="mappings" status="stat"> 
		<s:select label="%{property.name}" name="mappings[%{#stat.index}].column" required="false"
			value="%{mappings[#stat.index].column}"
			headerKey="" emptyOption="true" 
			list="mapOptions[property]" />
			<!-- headerValue="Select a column or value ..." -->
        <s:textfield  
            name="mappings[%{#stat.index}].value"  
            value="%{mappings[#stat.index].value}"/>  
		<c:if test="${not empty property.link}">
	 		<s:a href="%{property.link}">help</s:a>
		</c:if>
        <br/> 
    </s:iterator> 
 
 	<br/>
    <s:submit value="button.save" theme="simple"/> 
 
</s:form> 


<br />
