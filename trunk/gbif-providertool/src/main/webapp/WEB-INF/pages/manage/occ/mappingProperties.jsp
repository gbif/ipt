<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceOverview.title"/></title>
    <meta name="heading" content="<s:property value="mapping.resource.title"/>"/>
</head>

<h2><s:property value="mapping.extension.name"/> Extension</h2>

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

<c:set var="buttons">
    <li class="buttonBar bottom">
        <s:submit cssClass="button" key="button.save" theme="simple"/>
        <s:submit cssClass="button" method="cancel" key="button.done" theme="simple"/>
    </li>
</c:set>

<s:form action="saveMappingProperties" method="get" validate="true">
    <li style="display: none">
        <s:hidden key="mapping_id"/>
	    <s:hidden key="resource_id"/>
    </li>

	<c:out value="${buttons}" escapeXml="false" /> 
 
 	<s:select key="mapping.coreIdColumnIndex" required="true"
		headerKey="Select identifier for core record" emptyOption="true" 
		list="columnOptions" />
 
    <s:iterator value="mappings" status="stat"> 
		<s:select label="%{property.name}" name="mappings[%{#stat.index}].column" required="%{property.required}"
			value="%{mappings[#stat.index].column}"
			headerKey="" emptyOption="true" 
			list="mapOptions[property]" />
			<!-- headerValue="Select a column or value ..." -->

        <s:textfield  
            name="mappings[%{#stat.index}].value"  
            value="%{mappings[#stat.index].value}"/>  
		<c:if test="${not empty property.link}">
	 		<a href="${property.link}" target="_blank">help</a>
		</c:if>
        <br/> 
    </s:iterator> 
 
 	<br/>

	<c:out value="${buttons}" escapeXml="false" />		    	
 
</s:form> 


<br />
