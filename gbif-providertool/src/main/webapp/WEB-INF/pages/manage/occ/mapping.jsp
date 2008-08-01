<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceOverview.title"/></title>
    <meta name="heading" content="<s:property value="mapping.resource.title"/>"/>
    
</head>

<s:url id="editResourceConnectionUrl" action="editResourceConnection"/>
<%-- See if file or sql was used before --%>
<s:if test="%{mapping.sourceFileLocation}">
	<s:set name="sourceDivId" value="%{'filesource'}"/>
</s:if>
<s:else>
	<s:set name="sourceDivId" value="%{'dbsource'}"/>
</s:else>

			
<div id="dbsource" style="display:none">
	<h2>Configure DB Import Source</h2>
	<s:if test="%{resource.hasDbConnection()}">
	  <s:form action="saveMappingSource" method="post">
	    <s:hidden key="resource_id"/>
	    <s:hidden key="mapping_id"/>
	    <s:hidden key="extension_id"/>
	    
	    <!-- cssClass="text large" -->    
	    <s:textarea key="mapping.sourceSql" required="true" cssClass="text large"/>
	    <br/>
	    
	    <s:submit cssClass="button" key="button.save" theme="simple"/>
	    <s:if test="%{mapping.id}">
	        <s:submit cssClass="button" method="delete" key="button.delete" onclick="return confirmDelete('mapping')" theme="simple"/>
	    </s:if>
	    <s:submit cssClass="button" method="cancel" key="button.done" theme="simple"/>
	
	  </s:form>
	</s:if>
	<s:else>
		<p class="reminder">There is no working database connection configured. <br/>
			If you want to upload data from a database, please <s:a href="%{editResourceConnectionUrl}">configure a connection</s:a> first.
		</p>
	</s:else>
	<p>
		Want to upload a <a onclick="Element.hide('dbsource'); Effect.toggle('filesource', 'blind', { duration: 0.3 }); return false;">file</a> instead?
	</p>
</div>

<div id="filesource" style="display:none">
	<h2>Configure File Import Source</h2>
	<s:form action="uploadMappingSource" enctype="multipart/form-data" method="post" validate="true" id="uploadForm">
	    <s:hidden key="resource_id"/>
	    <s:hidden key="mapping_id"/>
	    <li class="info">
	        Please upload a tab delimited file to base the mapping on.
	    </li>
	    <s:file name="file" key="mapping.selectUploadFile" cssClass="text file" required="true"/>
	    <li class="buttonBar bottom">
	        <s:submit key="button.upload" name="upload" cssClass="button"/>
		    <s:submit method="cancel" key="button.cancel" theme="simple" cssClass="button" />
	    </li>
	</s:form>
	<p>
		Want to upload from a <a onclick="Element.hide('filesource'); Effect.toggle('dbsource', 'blind', { duration: 0.3 }); return false;">database</a> instead?
	</p>
</div>




<s:if test="%{viewColumnHeaders.isEmpty}">
	<!-- import source not yet configured -->
	<s:if test="%{not mapping.sourceFileLocation and not mapping.sourceSql}">
		<%-- no source ever configured before --%>
		<p id="initialSourceQuestion">Do you want to map a <a onclick="Element.hide('initialSourceQuestion'); Effect.toggle('dbsource', 'blind', { duration: 0.3 }); return false;">database</a> 
		or a <a onclick="Element.hide('initialSourceQuestion'); Effect.toggle('filesource', 'blind', { duration: 0.3 }); return false;">file</a> upload?
		</p>
	</s:if>
	<s:else>
		<script>
			Element.show('<s:property value="sourceDivId"/>');
		</script>		
	</s:else>	
</s:if>


<s:if test="%{not viewColumnHeaders.isEmpty}">
	<!-- import source is configured -->
	<h2>Mapping Import Source to <s:property value="mapping.extension.name"/> </h2>
	<ul class="actionmenu">
		<li><a onclick="Effect.toggle('<s:property value="sourceDivId"/>', 'blind', { duration: 0.3 }); return false;">configure import source</a></li>
		<li><a onclick="Effect.toggle('sourcepreview', 'blind', { duration: 0.3 }); return false;">sourcedata</a></li>
		<li><a onclick="Effect.toggle('preview', 'blind', { duration: 0.3 }); return false;">preview mapping</a></li>
	</ul>
	<div id="sourcepreview">
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
	
	<div id="preview" style="display:none">
		AJAX CALL TO BE DONE...
	</div>

	<c:set var="buttons">
	    <li class="buttonBar bottom">
	        <s:submit cssClass="button" key="button.save" theme="simple"/>
	        <s:submit cssClass="button" method="cancel" key="button.done" theme="simple"/>
	    </li>
	</c:set>
	
	<s:form action="saveMappingProperties" method="post" validate="true">
	    <li style="display: none">
	        <s:hidden key="mapping_id"/>
		    <s:hidden key="resource_id"/>
	    </li>
	
	 	<s:select key="mapping.coreIdColumn.columnName" required="true"
			headerKey="Select identifier for core record" emptyOption="true" 
			list="columnOptions" />
	 
	 	<br/>
	 	<br/>
		<c:out value="${buttons}" escapeXml="false" /> 
	 	<br/>
	 	<br/>

	    <s:iterator value="mappings" status="stat"> 
		 	<br/>
			<s:select label="%{property.name}" name="mappings[%{#stat.index}].column.columnName" required="%{property.required}"
				value="%{mappings[#stat.index].column.columnName}"
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

<!-- import source works END -->
</s:if>

<br />
