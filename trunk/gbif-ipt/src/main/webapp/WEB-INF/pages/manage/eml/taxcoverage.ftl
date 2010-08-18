<#include "/WEB-INF/pages/inc/header.ftl">

<script type="text/javascript">
	var count = new Number(0);
	// a function called when adding new taxonomic coverages
	// an element is cloned and the IDs reset etc etc
	$(document).ready($(function() {
		var lastChild = $("#taxonomies .taxonom:last-child").attr("id");
		if(lastChild != undefined) {
			count = parseInt(lastChild.split("-")[1])+1;
		}
		$("#plus").click(function(event) {
			event.preventDefault();
			var idNewForm = 'taxonomic-'+count;		
			var theNewForm = $("#baseTaxon").clone().attr('id', idNewForm).css('visibility', '');
			$("#taxonomies").append(theNewForm);			
			
			updateFields(idNewForm, count);	

			$("#taxonomic-"+count).hide().slideDown("slow");						
			count++;
		});					
		$(".removeLink").click(function(event) {
			event.preventDefault();
			removeTaxonomic(event);			
		});		
		// a function that change the atributes (id, name) of the fields and the labels of the specified form.
		function updateFields(divId, newIndex) {
			$("#"+divId+" [id$='description']").attr("id", "eml.taxonomicCoverages["+newIndex+"].description").attr("name", function() {return $(this).attr("id");});
			$("#"+divId+" [for$='description']").attr("for", "eml.taxonomicCoverages["+newIndex+"].description");
			$("#"+divId+" [id$='scientificName']").attr("id", "eml.taxonomicCoverages["+newIndex+"].taxonKeyword.scientificName").attr("name", function() {return $(this).attr("id");});
			$("#"+divId+" [for$='scientificName']").attr("for", "eml.taxonomicCoverages["+newIndex+"].taxonKeyword.scientificName");
			$("#"+divId+" [id$='commonName']").attr("id", "eml.taxonomicCoverages["+newIndex+"].taxonKeyword.commonName").attr("name", function() {return $(this).attr("id");});
			$("#"+divId+" [for$='commonName']").attr("for", "eml.taxonomicCoverages["+newIndex+"].taxonKeyword.commonName");
			$("#"+divId+" [id$='rank']").attr("id", "eml.taxonomicCoverages["+newIndex+"].taxonKeyword.rank").attr("name", function() {return $(this).attr("id");});
			$("#"+divId+" [for$='rank']").attr("for", "eml.taxonomicCoverages["+newIndex+"].taxonKeyword.rank");
			$("#"+divId+" .removeLink").attr("id", "removeLink-"+newIndex).click(
				function(event){
					event.preventDefault();
					removeTaxonomic(event);	
				}
			);
		}
				
		function removeTaxonomic(event) {
			var $target = $(event.target);
			var index=$target.attr("id").split("-")[1];			
			$('#taxonomic-'+index).attr("id", "toRemove");
			var stopVar = true;
			for(var c = index; stopVar; c++ ) {
				var nextForm = $('#taxonomic-'+(parseInt(c)+1));				
				if(nextForm.attr("id") == undefined) {
					stopVar = false;
				} else {
					var antes = nextForm.attr("id");
					updateFields(nextForm.attr("id"), c);
					nextForm.attr("id", 'taxonomic-'+c);
				}
			}			
			// removing the form in the html.
			$("#toRemove").slideUp("slow", function() { $(this).remove(); } );
			count--;
		}
	})
	);	
</script>


<title><@s.text name='manage.metadata.taxcoverage.title'/></title>
<#assign sideMenuEml=true />
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name='manage.metadata.taxcoverage.title'/>: <em>${ms.resource.title!ms.resource.shortname}</em></h1>
<p><@s.text name='manage.metadata.taxcoverage.intro'/></p>

<#include "/WEB-INF/pages/macros/forms.ftl"/>

<form class="topForm" action="metadata-${section}.do" method="post">
<div id="taxonomies">
	<!-- Adding the taxonomic coverages that already exists on the file -->	
	<#assign next_agent_index=0 />
	<#list eml.taxonomicCoverages as taxonomicCoverage>	
		<div id='taxonomic-${taxonomicCoverage_index}' class="taxonom">
			<div class="right">
    			<a id="removeLink-${taxonomicCoverage_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.taxcoverage.item'/> ]</a>
  			</div>
			<div class="newline"></div>
			<@text  i18nkey="eml.taxonomicCoverage.description" name="eml.taxonomicCoverages[${taxonomicCoverage_index}].description" />
			<div class="half">
           		<@input i18nkey="eml.taxonomicCoverage.scientificName" name="eml.taxonomicCoverages[${taxonomicCoverage_index}].taxonKeyword.scientificName" />
           		<@input i18nkey="eml.taxonomicCoverage.commonName" name="eml.taxonomicCoverages[${taxonomicCoverage_index}].taxonKeyword.commonName" />
        	</div>     	
           	<@select i18nkey="eml.taxonomicCoverage.rank"  name="eml.taxonomicCoverages[${taxonomicCoverage_index}].taxonKeyword.rank" options=ranks value="${eml.taxonomicCoverages[taxonomicCoverage_index].taxonKeyword.rank}" />
   	  		<div class="newline"></div>			
			<div class="horizontal_dotted_line_large_foo" id="separator"></div>
			<div class="newline"></div>
			<div class="newline"></div>
		</div>
	</#list>
	
</div>
	<!-- The add link and the buttons should be first. The next div is hidden. -->
	<a id="plus" href="" >Add new Taxonomic Coverage</a>
	<div class="buttons">
 			<@s.submit name="save" key="button.save"/>
 			<@s.submit name="cancel" key="button.cancel"/>
   </div>
   
   <div id='baseTaxon' class="taxonom" style="visibility:hidden">
      <div class="right">
        <a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.taxcoverage.item'/> ]</a>
      </div>
      <div class="newline"></div>
      <@text  i18nkey="eml.taxonomicCoverage.description" name="description" />
      <div class="half">
      	<@input i18nkey="eml.taxonomicCoverage.scientificName" name="scientificName" />
        <@input i18nkey="eml.taxonomicCoverage.commonName" name="commonName" />
      </div>         
      <@select i18nkey="eml.taxonomicCoverage.rank"  name="rank" options=ranks value="value" />   	  
      <div class="newline"></div>      
      <div class="horizontal_dotted_line_large_foo" id="separator"></div>
      <div class="newline"></div>
      <div class="newline"></div>
   </div>
	
</form>
<#include "/WEB-INF/pages/inc/footer.ftl">


	
 