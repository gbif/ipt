<head>
    <title>Resource Annotations</title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="submenu" content="${resourceType}"/>
	<script type="text/javascript">  
		function showAnnotation(id){
			var url = '<@s.url value="/ajax/annotation.html"/>';
			var params = { id: id }; 
			var target = 'annotation';	
			var myAjax = new Ajax.Updater(target, url, {method: 'get', parameters: params});
		};
	</script>
</head>


<#assign display=JspTaglibs["http://displaytag.sf.net"] />
<#include "/WEB-INF/pages/inc/globalVars.ftl">  

<@display.table name="annotations" uid="anno" class="table" requestURI="" id="annotationList" export=false pagesize=50>
    <@display.column property="id" title='ID' href="Javascript:showAnnotation(${anno.id});return false"/>
    <@display.column property="note" sortable=true maxLength=50 href="annotation.html?resource_id=${resource_id?c}" media="html" paramId="id" paramProperty="id" title='${struts.getText("annotation.note")}'/>
    <@display.column property="type" sortable=true title='${struts.getText("annotation.type")}'/>
	<@display.column property="human" sortable=true title='${struts.getText("annotation.human")}' />    
	<@display.column property="actor" sortable=true title='${struts.getText("annotation.actor")}' />    
	<@display.column property="created" sortable=true title='${struts.getText("annotation.created")}' format="{0,date,${datePattern}}"/>    
    
    <@display.setProperty name="paging.banner.item_name"><@s.text name="annotation.annotation"/></@display.setProperty>
    <@display.setProperty name="paging.banner.items_name"><@s.text name="annotation.annotations"/></@display.setProperty>
</@display.table>

<script type="text/javascript">
    highlightTableRows("annotationList");
</script>



<div id="annotation">

</div>