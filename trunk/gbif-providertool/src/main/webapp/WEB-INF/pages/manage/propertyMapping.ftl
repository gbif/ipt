<head>
    <title><@s.text name="occResourceOverview.title"/></title>
    <meta name="resource" content="${view.resource.title}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
    <meta name="heading" content="Property Mappings"/>
    
	<script type="text/javascript" src="<@s.url value='/scripts/jquery/ui.core.min.js'/>"></script>
	<script type="text/javascript" src="<@s.url value='/scripts/jquery/ui.accordion.min.js'/>"></script>
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
	
	function addProperty(newPropAnchor){
		var newPropForm = $("#propertyFormTemplate").clone();
		newPropForm.show();
    	var name = newPropAnchor.text();
		$("strong",newPropForm).text(name);
		
    	var id = $(newPropAnchor).attr("id").substring(2);
    	var newPropIds = $("#newProperties").val() +" "+id;
    	$("#newProperties").val(newPropIds);
		$("select",newPropForm).attr("name", "view.propertyMappings["+id+"].column");
		$("input",newPropForm).attr("name", "view.propertyMappings["+id+"].value");
		
    	var link = $("a.propDocLink", newPropAnchor.parent());
    	if(link.length>0){
			$("a",newPropForm).attr("href", link.attr("href"));
    	}else{
			$("a",newPropForm).hide();
    	}
		
		$("#mappings").prepend(newPropForm);
		$("div.propertyForm:first").effect("highlight", {}, 1000);
	}
	
	$(document).ready(function(){
		$("#propertyFormTemplate").hide(0);
	    $("#mappingForm_button_viewsource").click(function (e) {
			e.preventDefault(); 
	      	$("#sourcepreview").slideToggle("normal");
	      	sourcePreview();
	    });
	    $("#properties ul").hide(0);
	    $("#properties div.propGroup").hover(
	      function () {
	      	$("ul", this).slideDown("normal");
	      }, 
	      function () {
	      	$("ul", this).slideUp("normal");
	      }
	    );
	    $("a.propLink").click(function () {
	      	addProperty($(this));
	      	$(this).parent().remove();
	    });

	});
	
	</script>	
  </head>

<#-- 
<content tag="contextmenu">
  <div id="availableProperties">
	<label>Available Properties</label>
	<div id="accordion">
  	<#list availProperties?keys as group>
		<label><a href="#">${group}</a></label>
		<ul>
		<#list availProperties[group] as p>
		  <li>
			<a class="propLink" id="ap${p.id}">${p.name}</a>
			<#if p.link??>
				<a class="propDocLink" href="${p.link}" target="_blank">(about)</a>
			</#if>
		  </li>
		</#list>
	    </ul>
	</#list>
	</div>
  </div>
</content>
-->
<body>


  <div class="sucker"></div>

<#if !columnOptions??>
	<#-- import source doesnt work -->
	<p class="reminder">There is no working import source configured. <br/>
		Please check your <a href="<@s.url action="sources"/>">sources</a>.
	</p>
<#else>

  <div class="block2col">
	<h2>for <i>${view.source.name}</i> to ${view.extension.name}</h2>
	<@s.form id="mappingForm" action="savePropertyMapping" method="post">
        <@s.hidden key="mid"/>
        <@s.hidden key="sid"/>
        <@s.hidden key="eid"/>
	    <@s.hidden key="resource_id"/>
        <@s.hidden id="mappings_idx" name="mappings_idx" value=""/>
        <@s.hidden id="newProperties" name="newProperties" value=""/>
        

	 	<@s.select key="view.coreIdColumn" required="true"
			headerKey="Select local identifier for core record" emptyOption="false" list="columnOptions" />
			
		<#if view.isCore()>
		 	<@s.select key="view.guidColumn" emptyOption="true" list="columnOptions" />
	 		<@s.select key="view.linkColumn" emptyOption="true" list="columnOptions"/>
	    	<@s.textfield key="view.linkTemplate" cssClass="block2col"/>
	 	</#if>
	 	
		<div class="breakLeft">
	        <@s.submit cssClass="button" key="button.viewsource" theme="simple"/>	        
	        <@s.submit cssClass="button" key="button.save" theme="simple"/>
		    <#if (view.id)??>
		        <@s.submit cssClass="button" method="delete" key="button.delete" onclick="return confirmDelete('mapping')" theme="simple"/>
		    </#if>
	        <@s.submit cssClass="button" method="cancel" key="button.done" theme="simple"/>	        
		</div>	        
	    
	    <#--
	 	<br/>
		<ul class="actionmenu">
			<li id="sourceViewLink"><a>view source</a></li>
			<li id="previewLink"><a>preview mapping</a></li>		
		</ul>
		-->
	<#--
	<div id="uploadpreview" style="display:none; clear:both;">
		Retrieving mapping preview ...<br/><br/>
		<p class="reminder">Not implemented yet, sorry!</p>
	</div>
    -->
  </div>

  <div class="block2col" id="availableProperties">
	<h2>Available Properties</h2>
	<div id="properties">
  	<#list availProperties?keys as group>
  	  <div class="propGroup">
	    <label>${group}</label>
	    <ul>
		<#list availProperties[group] as p>
		  <li>
			<a class="propLink" id="ap${p.id}">${p.name}</a>
			<#if p.link??>
				<a class="propDocLink" href="${p.link}" target="_blank">(about)</a>
			</#if>
		  </li>
		</#list>
	    </ul>
	  </div>
	</#list>
	</div>
  </div>


  <div class="break"></div>
  <div id="sourcepreview">
	Retrieving source data ...
  </div>	

  <div class="break"></div>	
	<h2>Property Mappings</h2>
	<p>For a single property that you want to map, select a column from your source or enter a fixed value into the text field.
	   If the property has a vocabulary associated you can also select a term from the dropdown.<br/>
	   To add more properties please select them from the available properties just above (hint: the menu opens when you move the mouse above).
	</p>
	
	<div id="mappings">
  	<#list view.getPropertyMappingsSorted() as mp>
	  <div class="minibreak propertyForm">
		<div>
			<strong>${mp.property.name}</strong>
			<#if mp.property.link??>
				<a href="${mp.property.link}" target="_blank">(about)</a>
			</#if>
		</div>
		<div class="overhang">
			<div>
				<@s.select key="view.propertyMappings[${mp.property.id}].column" list="sourceColumns"
					required="${mp.property.required?string}" headerKey="" emptyOption="true" style="display: inline" theme="simple"/>
			</div>
			<div>
				<#if (mp.property.vocabulary??)>
				    <@s.submit cssClass="button" key="button.termMapping" method="termMapping" theme="simple" onclick="return confirmTermMapping('${mp.property.id}')"/>
				    or select a static value:
					<@s.select key="view.propertyMappings[${mp.property.id}].value"
						list="vocs[${mp.property.id}]" 
						style="display: inline" headerKey="" emptyOption="true" theme="simple"/>						
				<#else>
			        <@s.textfield  name="view.propertyMappings[${mp.property.id}].value" value="${mp.value!}" cssClass="large" theme="simple"/>  
				</#if>
			</div>
		</div>
	  </div> <#-- minibreak per mapping -->
	</#list>
	</div>
 
	<div class="break"></div>
    <@s.submit cssClass="button" key="button.save" theme="simple"/>
    <@s.submit cssClass="button" name="cancel" key="button.done" theme="simple"/>
 
  </@s.form> 

  <@s.url id="termMappingUrl" action="terMappingInit">
    <@s.param name="mappings_idx" value="9"/>
  </@s.url>

</#if> <#-- if/else at top testing if source works -->

<#-- property form template -->
<div id="propertyFormTemplate">
<div class="minibreak propertyForm">
<div>
	<strong></strong>
	<a href="#" target="_blank">(about)</a>
</div>
<div class="overhang">
	<div class="left">
		<@s.select name="" list="sourceColumns" headerKey="" emptyOption="true" style="display: inline" theme="simple"/>
	</div>
	<div class="left">
        <@s.textfield  name="" value="" cssClass="large" theme="simple"/>  
	</div>
</div>
</div>
</div> <#-- property form template -->

</body>