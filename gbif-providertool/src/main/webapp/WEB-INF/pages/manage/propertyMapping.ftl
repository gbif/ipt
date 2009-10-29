<head>
    <title><@s.text name="propertymapping.title"/></title>
    <meta name="resource" content="${view.resource.title}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
    <meta name="heading" content="<@s.text name='propertymapping.heading'/>"/>
    
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
			alert('<@s.text name='propertyheading.alert'/>');
			return false;
		}
		return true;
	};
	
	function addProperty(newPropAnchor){
		var newPropForm = $("div#propertyFormTemplate").clone();
		newPropForm.show();
    	var name = newPropAnchor.text();
		$("strong",newPropForm).text(name);
		
    	var id = $(newPropAnchor).attr("id").substring(2);
    	var propertyFormId = "propertyForm"+id;
    	$(newPropForm).attr("id", propertyFormId);
    	var newPropIds = $("#newProperties").val() +" "+id;
    	$("#newProperties").val(newPropIds);
		$("select",newPropForm).attr("name", "view.propertyMappings["+id+"].column").attr("id", "sourceColumn_"+id);
    	var link = $("a.propDocLink", newPropAnchor.parent());
    	if(link.length>0){
			$("a",newPropForm).attr("href", link.attr("href"));
    	}else{
			$("a",newPropForm).hide();
    	}
		// add to real list
		$("#mappings").prepend(newPropForm);
		$("div#"+propertyFormId).effect("highlight", {}, 1000);
		// finally load potential vocabulary via ajax
		loadVocabulary(id);
	}
	
	function loadVocabulary(id){
    	var propertyFormId = "propertyForm"+id;
		var url = '<@s.url value="/ajax/propVocabulary.html"/>';
		ajaxHtmlUpdate(url, "div#"+propertyFormId+" div.valueMapping", {id:id});
	}
	
	$(document).ready(function(){
		$("#propertyFormTemplate").hide(0);
	    $("#viewSource").click(function (e) {
			e.preventDefault(); 
	      	$("#sourcepreview").slideToggle("normal");
	      	sourcePreview();
	    });
	    $("#properties ul").hide(0);
	    $("#properties div.propGroup").click(
	      function () {
	      	$("ul", this).slideToggle("fast");
	      	$("div div", this).toggle(0);
	      } 
	    );
	    $("a.propLink").click(function () {
	      	addProperty($(this));
	      	$(this).parent().remove();
	    });
	    
	  	<#list view.getPropertyMappingsSorted() as mp>
		  <#if (mp.property.vocabulary??)>
		    loadVocabulary(${mp.property.id});
		  </#if>
	    </#list>
	});
	
	</script>	
  </head>

<body>
  <div class="sucker"></div>
<#--
-->

<#if !columnOptions??>
	<#-- import source doesnt work -->
	<p class="reminder"><@s.text name='propertymapping.nosource'/> <a href="<@s.url action="sources"/>"><@s.text name='propertymapping.sources'/></a>.
	</p>
<#else>

<@s.form id="mappingForm" action="savePropertyMapping" method="post">
  <div class="block2col">
  	<#-- <img src="<@s.url value="/images/glasses.png"/>" width="20" height="20"/> -->
	<h2>for <a id="viewSource" href="#"><i>${view.source.name}</i></a> to ${view.extension.name}</h2>
        <@s.hidden key="mid"/>
        <@s.hidden key="sid"/>
        <@s.hidden key="eid"/>
	    <@s.hidden key="resourceId"/>
        <@s.hidden id="mappings_idx" name="mappings_idx" value=""/>
        <@s.hidden id="newProperties" name="newProperties" value=""/>
        
	 	<@s.select key="view.coreIdColumn" required="true" headerKey="Select local identifier for core record" emptyOption="false" list="columnOptions" />
		<#if view.isCore()>
		 	<@s.select key="guidProperty.column" emptyOption="true" list="columnOptions" />
	 		<@s.select key="view.linkColumn" emptyOption="true" list="columnOptions"/>
	    	<@s.textfield key="view.linkTemplate" cssClass="block2col"/>
	 	</#if>			
	 	
		<div class="breakLeft">
	        <@s.submit cssClass="button" key="button.save" theme="simple"/>
		    <#if (view.id)??>
		        <@s.submit cssClass="button" method="delete" key="button.delete" onclick="return confirmDelete('mapping')" theme="simple"/>
		    </#if>
	        <@s.submit cssClass="button" method="cancel" key="button.done" theme="simple"/>	        
		</div>	        

  </div>

  <div class="block2col" id="availableProperties">
	<h2><@s.text name='propertymapping.available'/></h2>
	<div id="properties">
  	<#list availProperties?keys as group>
  	  <div class="propGroup">
		<div>
		    <div class="arrow"><img src="<@s.url value='/images/arrow_right.png'/>" /></div>
		    <div class="arrow" style="display:none"><img src="<@s.url value='/images/arrow_down.png'/>" /></div>
		    <label>${group}</label>
	    </div>
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
	<@s.text name='propertymapping.retrieving'/>
  </div>	

  <div class="break"></div>	
	<h2><@s.text name='propertymapping.mappings'/></h2>
	<p><@s.text name='propertymapping.instructions'/>
	</p>
	
	<div id="mappings">
  	<#list view.getPropertyMappingsSorted() as mp>
  	  <#if mp.property.name!=guidPropertyName>
	  <div id="propertyForm${mp.property.id}" class="minibreak propertyForm">
		<div>
			<strong>${mp.property.name}</strong>
			<#if mp.property.link??>
				<a href="${mp.property.link}" target="_blank">(about)</a>
			</#if>
		</div>
		<div class="overhang">
			<div>
				<@s.select id="sourceColumn_${mp.property.id}" key="view.propertyMappings[${mp.property.id}].column" list="sourceColumns"
					required="${mp.property.required?string}" headerKey="" emptyOption="true" style="display: inline" theme="simple"/>
			</div>
		  <#if (mp.property.vocabulary??)>
			<div class="left valueMapping">
				<img src='<@s.url value="/images/ajax-loader.gif"/>'/>
			</div>
		  <#else>
			<div>
		        <@s.textfield  name="view.propertyMappings[${mp.property.id}].value" value="${mp.value!}" cssClass="large" theme="simple"/>  
			</div>
		  </#if>
		</div>
	  </div> <#-- minibreak per mapping -->
	  </#if>
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
<div id="propertyFormTemplate" style="display:none">
  <div class="minibreak propertyForm">
	<div>
		<strong></strong>
		<a href="#" target="_blank">(about)</a>
	</div>
	<div class="overhang">
		<div class="left">
			<@s.select id="" name="" list="sourceColumns" headerKey="" emptyOption="true" style="display: inline" theme="simple"/>
		</div>
		<div class="left valueMapping">
			<img src='<@s.url value="/images/ajax-loader.gif"/>'/>
		</div>
	</div>
  </div>
</div> <#-- property form template -->

</body>