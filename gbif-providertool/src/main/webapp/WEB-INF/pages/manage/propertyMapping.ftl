<head>
    <title><@s.text name="occResourceOverview.title"/></title>
    <meta name="resource" content="${view.resource.title}"/>
    <meta name="submenu" content="manage_resource"/>
	<script type="text/javascript" src="/scripts/jquery/ui.core.min.js"></script>
	<script type="text/javascript" src="/scripts/jquery/ui.accordion.min.js"></script>
	<script>
	var previewLoaded=0;
	function sourcePreview(){
		if (previewLoaded<1){
			var url = '<@s.url action="sourcePreview" namespace="/ajax"/>';
			var params = { sid: ${view.source.id} }; 
			var target = '#sourcepreview';	
			ajaxHtmlUpdate(url, target, params);
			previewLoaded=1;
		}
	};
	
	
	function confirmTermMapping(idx, tid){
		$("#mappings_idx").val(idx);
		if(isEmpty('#sourceColumn_'+idx)){
			alert('You need to select a source column first');
			return false;
		}
		return true;
	};
	

	$(document).ready(function(){
	    $("#sourceViewLink").click(function () {
	    	$("#uploadpreview").hide();
	      	$("#sourcepreview").slideToggle("normal");
	      	sourcePreview();
	    });
	    $("#previewLink").click(function () {
	      	$("#sourcepreview").hide();
	    	$("#uploadpreview").slideToggle("normal");
	      	sourcePreview();
	    });

		$("#accordion").accordion({
			header: "h3"
		});

	});
	</script>	
</head>


<body>
<h1>Property Mappings</h1>
<div class="horizontal_dotted_line_large_foo"></div>
<h2>for <i>${view.source.name}</i> to ${view.extension.name}</h2>

<#if !columnOptions??>
	<#-- import source doesnt work -->
	<p class="reminder">There is no working import source configured. <br/>
		Please check your <a href="<@s.url action="sources"/>">sources</a>.
	</p>
<#else>

	<@s.form id="mappingForm" action="savePropertyMapping" method="post">
        <@s.hidden key="mid"/>
        <@s.hidden key="sid"/>
        <@s.hidden key="eid"/>
	    <@s.hidden key="resource_id"/>
        <@s.hidden id="mappings_idx" name="mappings_idx" value=""/>

	 	<@s.select key="view.coreIdColumn" required="true"
			headerKey="Select local identifier for core record" emptyOption="false" list="columnOptions" />
			
		<#if view.isCore()>
		 	<@s.select key="view.guidColumn" emptyOption="true" list="columnOptions" />
		 	<div>
		 	<div class="left"/>
		 		<@s.select key="view.linkColumn" emptyOption="true" list="columnOptions"/>
        	</div>  
		 	<div class="left"/>
    	    	<@s.textfield key="view.linkTemplate" cssClass="large"/>
        	</div>  
        	</div>  
	 	</#if>
	 	
		<div class="breakRight">
	        <@s.submit cssClass="button" key="button.save" theme="simple"/>
		    <#if (view.id)??>
		        <@s.submit cssClass="button" method="delete" key="button.delete" onclick="return confirmDelete('mapping')" theme="simple"/>
		    </#if>
	        <@s.submit cssClass="button" method="cancel" key="button.done" theme="simple"/>	        
		</div>	        
	    
	 	<br/>
		<ul class="actionmenu">
			<li id="sourceViewLink"><a>view source</a></li>
			<li id="previewLink"><a>preview mapping</a></li>		
		</ul>

	<div class="break"></div>
	<div id="sourcepreview" style="display:none; clear:both;">
		Retrieving source data ...
	</div>	
	<div id="uploadpreview" style="display:none; clear:both;">
		Retrieving mapping preview ...<br/><br/>
		<p class="reminder">Not implemented yet, sorry!</p>
	</div>


	<div class="break"></div>	
	
	<h2>Property Mappings</h2>
	<p>For a single property that you want to map, select a column from your source or enter a fixed value into the text field.
	   If the property has a vocabulary associated you can also select a term from the dropdown
	</p>
	
	<div id="accordion">
  	<#list mappings?keys as group>
		<h3 class="accordionHeader"><a href="#">${group}</a></h3>
		<div>
		<#list mappings[group] as mp>
		  <div class="minibreak">
			<div>
				<strong>${mp.property.name}</strong>
				<#if mp.property.link??>
					<a href="${mp.property.link}" target="_blank">(about)</a>
				</#if>
			</div>
			<div class="overhang">
				<div class="left">
					<@s.select key="mappings.${group}[${mp_index}].column" list="sourceColumns"
						required="${mp.property.required?string}" headerKey="" emptyOption="true" style="display: inline" theme="simple"/>
				</div>
				<div class="left">
					<#if (mp.property.vocabulary??)>
					    <@s.submit cssClass="button" key="button.termMapping" method="termMapping" theme="simple" onclick="return confirmTermMapping('${mp.id}')"/>
					    or select a static value:
						<@s.select key="mappings.${group}[${mp_index}].value"
							list="vocs[${mp.property.id}]" 
							style="display: inline" headerKey="" emptyOption="true" theme="simple"/>						
					<#else>
				        <@s.textfield  name="mappings.${group}[${mp_index}].value" value="${mp.value!}" cssClass="large" theme="simple"/>  
					</#if>
				</div>
			</div>
		  </div> <#-- minibreak per mapping -->
		</#list>
	    </div> <#-- whole group -->
	</#list>
	</div> <#-- accordion -->
 
	<div class="break"></div>
    <@s.submit cssClass="button" key="button.save" theme="simple"/>
    <@s.submit cssClass="button" name="cancel" key="button.done" theme="simple"/>
 
</@s.form> 

<@s.url id="termMappingUrl" action="terMappingInit">
    <@s.param name="mappings_idx" value="9"/>
</@s.url>

</#if>
</body>