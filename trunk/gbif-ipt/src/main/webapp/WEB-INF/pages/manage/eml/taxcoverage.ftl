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
			var theNewForm = $("#baseTaxon").clone().attr('id', 'taxonomic-'+count).css('visibility', '');
			$("#taxonomies").append(theNewForm);			
			
			$("#taxonomic-"+count+" #description")
				.attr("id", "eml.taxonomicCoverages["+count+"].description")
				.attr("name", "eml.taxonomicCoverages["+count+"].description");
			
			$("#taxonomic-"+count+" #scientificName")
				.attr("id", "eml.taxonomicCoverages["+count+"].taxonKeyword.scientificName")
				.attr("name", "eml.taxonomicCoverages["+count+"].taxonKeyword.scientificName");
			
			$("#taxonomic-"+count+" #commonName")
				.attr("id", "eml.taxonomicCoverages["+count+"].taxonKeyword.commonName")
				.attr("name", "eml.taxonomicCoverages["+count+"].taxonKeyword.commonName");

			$("#taxonomic-"+count+" #rank")
				.attr("id", "eml.taxonomicCoverages["+count+"].taxonKeyword.rank")
				.attr("name", "eml.taxonomicCoverages["+count+"].taxonKeyword.rank");
			
			$("#taxonomic-"+count+" #removeLink").attr("id", "removeLink"+count).attr("name", "removeLink-"+count);
			$("#removeLink-"+count).click(function(event) { removeTaxonomic(event); });

			$("#taxonomic-"+count).hide().slideDown("slow");						
			count++;
		});
					
		$(".removeLink").click(function(event) {
			removeTaxonomic(event);
		});
		
		function removeTaxonomic(event){
			event.preventDefault();
			var $target = $(event.target);
			var index=$target.attr("id").split("-")[1];
			$('#taxonomic-'+index).slideUp("slow", function() { $(this).remove(); } );
					
  			// TODO reorder parties indexes after remove
			// $('#party0').remove();
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
			<@text  i18nkey="eml.taxonomicCoverage.description" name="eml.taxonomicCoverages[${taxonomicCoverage_index}].description" />
			<div class="half">
           		<@input i18nkey="eml.taxonomicCoverage.scientificName" name="eml.taxonomicCoverages[${taxonomicCoverage_index}].taxonKeyword.scientificName" />
           		<@input i18nkey="eml.taxonomicCoverage.commonName" name="eml.taxonomicCoverages[${taxonomicCoverage_index}].taxonKeyword.commonName" />
        	</div>     	
           	<@select i18nkey="eml.taxonomicCoverage.rank"  name="eml.taxonomicCoverages[${taxonomicCoverage_index}].taxonKeyword.rank" options=ranks value="${eml.taxonomicCoverages[taxonomicCoverage_index].taxonKeyword.rank}" />
   	  		<div class="newline"></div>
         	
			<div class="right">
    			<a id="removeLink-${taxonomicCoverage_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.taxcoverage.item'/> ]</a>
  			</div>
			<div class="newline"></div>
			<div class="horizontal_dotted_line_large_foo" id="separator"></div>
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
      
      <@text  i18nkey="eml.taxonomicCoverage.description" name="description" />  	

      <div class="half">
      	<@input i18nkey="eml.taxonomicCoverage.scientificName" name="scientificName" />
        <@input i18nkey="eml.taxonomicCoverage.commonName" name="commonName" />
      </div>
         
      <@select i18nkey="eml.taxonomicCoverage.rank"  name="rank" options=ranks value="value" />	 
   	  
      <div class="newline"></div>
      <div class="right">
        <a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.taxcoverage.item'/> ]</a>
      </div>
      <div class="newline"></div>
      <div class="horizontal_dotted_line_large_foo" id="separator"></div>
      <div class="newline"></div>
   </div>
	
</form>
<#include "/WEB-INF/pages/inc/footer.ftl">


	
 