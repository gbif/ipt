<#assign display=JspTaglibs["http://displaytag.sf.net"] />
<#include "/WEB-INF/pages/inc/globalVars.ftl">  
<head>
    <title><@s.text name='annotationslist.title'/></title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="menu" content="ExplorerMenu"/>
    <meta name="submenu" content="${resourceType}"/>
    <meta name="heading" content="<@s.text name='annotationslist.heading'/>"/>
	<script type="text/javascript">  
		function showAnnotation(anchor){
			var url = '<@s.url value="/ajax/annotation.html"/>';
			var params = { resourceId:${resourceId}, id: anchor.name }; 
			$.get(url, params, function(data) { 
				$(anchor).parent().parent().after("<tr class='odd anno'><td colspan='4'>"+data+"</td></tr>");
				$("a.close").click(function (e) {
					e.preventDefault();
					$(this).parent().parent().parent().parent().parent().empty();
			    });
			});
		};
		
		function updateAnnotations(){
			$('#annotationTypeForm').submit();
		}

		$(document).ready(function(){
			listenToChange("#annotationType", updateAnnotations);

			$("a.annotationLink").click(function (e) {
				e.preventDefault();
				showAnnotation(this);
		    });
		});
	</script>
<style>
	span.pagebanner{
		margin-top: 0px;
	}
	.annotation{
	    padding: 10px;
	}
</style>
</head>


<div class="break10"></div>
<div class="annotationRight">
<@s.form id="annotationTypeForm" method="get">
	<@s.hidden name="resourceId" value="${resourceId}"/>
	<@s.select id="annotationType" name="annotationType" value="annotationType" list="annotationTypes" emptyOption="true" style="display: inline" theme="simple"/>
</@s.form>
</div>

<br class="break"/>

<@display.table name="annotations" id="anno" class="table" export=false pagesize=25>
    <@display.column property="type" sortable=true title='${struts.getText("annotation.type")}'/>
    <#if anno??>
    <@display.column value="<a class='annotationLink' name='${anno.id}'>${anno.note?substring(0.50)}</a>" sortable=true title='${struts.getText("annotation.note")}'/>
    </#if>
	<@display.column property="creator" sortable=true title='${struts.getText("annotation.creator")}' />    
	<@display.column property="created" sortable=true title='${struts.getText("annotation.created")}' format="{0,date,${datePattern}}"/>    
    
    <@display.setProperty name="paging.banner.item_name"><@s.text name="annotation.annotation"/></@display.setProperty>
    <@display.setProperty name="paging.banner.items_name"><@s.text name="annotation.annotations"/></@display.setProperty>
</@display.table>