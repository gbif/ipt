<head>
    <title><@s.text name="occResourceOverview.title"/></title>
    <meta name="resource" content="${view.resource.title}"/>
    <meta name="submenu" content="manage_resource"/>
	<@s.head theme="ajax" debug="false"/>

<script>
var previewLoaded=0;
function sourcePreview(){
	if (previewLoaded<1){
		var url = '<@s.url action="sourcePreview" namespace="/ajax"/>';
		var params = { sid: ${view.source.id} }; 
		var target = 'sourcepreview';	
		var myAjax = new Ajax.Updater(target, url, {method: 'get', parameters: params});
		previewLoaded=1;
	}
};


function confirmTermMapping(idx, tid){
	//alert(idx);	
	//alert( $("mappings_idx").value );
	$("mappings_idx").value = idx;
	//alert( $("mappings_idx").value );
	//alert( $('sourceColumn_'+idx).value );
	if($('sourceColumn_'+idx).value==""){
		alert('You need to select a source column first');
		return false;
	}
	return true;
};
</script>	

</head>

<h2>Mappings for <i>${view.source.name}</i> to ${view.extension.name}</h2>

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
			headerKey="Select local identifier for core record" emptyOption="false" 
			list="columnOptions" />
			
		<#if view.isCore()>
		 	<@s.select key="view.guidColumn" required="false" emptyOption="true" 
				list="columnOptions" />
		 	<@s.select key="view.linkColumn" required="false" emptyOption="true" 
				list="columnOptions" />
	 	</#if>
	 	
	 	<br/>
	 	
	    <li class="buttonBar bottom">
	        <@s.submit cssClass="button" key="button.save" theme="simple"/>
		    <#if (view.id)??>
		        <@s.submit cssClass="button" method="delete" key="button.delete" onclick="return confirmDelete('mapping')" theme="simple"/>
		    </#if>
	        <@s.submit cssClass="button" method="cancel" key="button.done" theme="simple"/>
	    </li>
	    
	 	<br/>
		<ul class="actionmenu">
			<li><a onclick="Element.hide('uploadpreview'); Effect.toggle('sourcepreview', 'blind', { duration: 0.3 }); sourcePreview(); return false;">view source</a></li>
			<li>
				<@s.a targets="uploadpreview" theme="ajax" formid="mappingForm" form="mappingForm" onclick="Element.hide('sourcepreview'); Effect.toggle('uploadpreview', 'blind', { duration: 0.3 }); return false;">preview mapping</@s.a>
			</li>		
		</ul>
	
	<div id="sourcepreview" style="display:none">
		Retrieving source data ...
	</div>	
	<div id="uploadpreview" style="display:none">
		Retrieving mapping preview ...<br/>
		<p class="reminder">Not implemented yet, sorry!</p>
	</div>


	<div class="break"/>
	
	<fieldset>
	<legend>Property Mappings</legend>
	<p>For a single property that you want to map, select a column from your source or enter a fixed value into the text field.
	   If the property has a vocabulary associated you can also select a term from the dropdown
	</p>
    <#list mappings as m> 
	  <div class="minibreak">
		<div>
			<strong>${m.property.name}</strong>
			<#if m.property.link??>
				<a href="${m.property.link}" target="_blank">(about)</a>
			</#if>
		</div>
		<div class="overhang">
			<div class="left">
				<@s.select id="sourceColumn_${m_index}" key="mappings[${m_index}].column" list="sourceColumns"
					required="${m.property.required?string}" headerKey="" emptyOption="true" style="display: inline" theme="simple"/>
			</div>
			<div class="left">
				<#if (m.property.vocabulary??)>
				    <@s.submit cssClass="button" key="button.termMapping" method="termMapping" theme="simple" onclick="return confirmTermMapping('${m_index}')"/>
				    or select a static value:
					<@s.select key="mappings[${m_index}].value"
						list="vocs[${m.property.id}]" 
						style="display: inline" headerKey="" emptyOption="true" theme="simple"/>						
				<#else>
			        <@s.textfield  name="mappings[${m_index}].value" value="${m.value!}" cssClass="large" theme="simple"/>  
				</#if>
			</div>
		</div>
	  </div>
    </#list>
	</fieldset>
 
	<div class="break"/>
    <@s.submit cssClass="button" key="button.save" theme="simple"/>
    <@s.submit cssClass="button" name="cancel" key="button.done" theme="simple"/>
 
</@s.form> 

<@s.url id="termMappingUrl" action="terMappingInit">
    <@s.param name="mappings_idx" value="9"/>
</@s.url>

</#if>
