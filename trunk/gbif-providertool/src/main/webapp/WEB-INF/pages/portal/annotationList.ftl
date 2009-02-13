<#assign display=JspTaglibs["http://displaytag.sf.net"] />
<#include "/WEB-INF/pages/inc/globalVars.ftl">  
<head>
    <title>Resource Annotations</title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="submenu" content="${resourceType}"/>
    <meta name="heading" content="Resource Annotations"/>
	<script type="text/javascript">  
		function showAnnotation(id){
			var url = '<@s.url value="/ajax/annotation.html"/>';
			var params = { resource_id:${resource_id}, id: id }; 
			var target = '#annotation';	
			ajaxHtmlUpdate(url, target, params);
			return false;
		};

		$(document).ready(function(){
			listenToChange("#annotationType", $('#annotationTypeForm').submit);
		});
	</script>
</head>


<div class="right">
<@s.form id="annotationTypeForm" method="get">
	<@s.hidden name="resource_id" value="${resource_id}"/>
	<@s.select id="annotationType" name="annotationType" value="annotationType" list="annotationTypes" emptyOption="true" style="display: inline" theme="simple"/>
</@s.form>
</div>

<br class="break"/>

<@display.table name="annotations" id="anno" class="table" export=false pagesize=25>
    <@display.column property="type" sortable=true title='${struts.getText("annotation.type")}'/>
    <#if anno??>
    <@display.column value="<a href='Javascript:showAnnotation(${anno.id})'>${anno.note?substring(0.50)}</a>" sortable=true title='${struts.getText("annotation.note")}'/>
    </#if>
	<@display.column property="creator" sortable=true title='${struts.getText("annotation.creator")}' />    
	<@display.column property="created" sortable=true title='${struts.getText("annotation.created")}' format="{0,date,${datePattern}}"/>    
    
    <@display.setProperty name="paging.banner.item_name"><@s.text name="annotation.annotation"/></@display.setProperty>
    <@display.setProperty name="paging.banner.items_name"><@s.text name="annotation.annotations"/></@display.setProperty>
</@display.table>

<div id="annotation">
	<!-- empty for AJAX call -->
</div>

<script type="text/javascript">
    highlightTableRows("anno");
</script>

