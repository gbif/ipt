<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name='manage.metadata.taxcoverage.title'/></title>
	<#assign sideMenuEml=true />
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name='manage.metadata.taxcoverage.title'/>: <em>${ms.resource.title!ms.resource.shortname}</em></h1>
<p><@s.text name='manage.metadata.taxcoverage.intro'/></p>

<#include "/WEB-INF/pages/macros/forms.ftl"/>

<script type="text/javascript">
	var count = new Number(1);

	// a function called when adding new taxonomic coverages
	// an element is cloned and the IDs reset etc etc
	$(function() {	
		$("#plus").click(function() {						
			var theNewForm = $("#toAdd").clone().attr('id', 'form'+count).css('visibility', '');
			$("#forms").append(theNewForm);			
			
			$("#form"+count+" #description")
				.attr("id", "eml.taxonomicCoverages["+count+"].description")
				.attr("name", "eml.taxonomicCoverages["+count+"].description");
			
			
			$("#form"+count+" #scientificName")
				.attr("id", "eml.taxonomicCoverages["+count+"].taxonKeyword.scientificName")
				.attr("name", "eml.taxonomicCoverages["+count+"].taxonKeyword.scientificName");
			
			$("#form"+count+" #commonName")
				.attr("id", "eml.taxonomicCoverages["+count+"].taxonKeyword.commonName")
				.attr("name", "eml.taxonomicCoverages["+count+"].taxonKeyword.commonName");

			$("#form"+count+" #rank")
				.attr("id", "eml.taxonomicCoverages["+count+"].taxonKeyword.rank")
				.attr("name", "eml.taxonomicCoverages["+count+"].taxonKeyword.rank");
			
			$("#form"+count+" #removeLink").attr("id", "removeLink"+count).attr("name", "removeLink"+count);
			$("#removeLink"+count).click(function(event) { removeTaxonomic(event); });

			$("#form"+count).hide().slideDown("slow");						
			count++;
		});
					
		$(".removeLink").click(function(event) {
			removeTaxonomic(event);
		});
		
		function removeTaxonomic(event){
			var $target = $(event.target);
			var index=$target.attr("id").split("removeLink")[1];
			$('#form'+index).slideUp("slow", function() { $(this).remove(); } );
					
  			// TODO reorder parties indexes after remove
			// $('#party0').remove();
		}
	});	
</script>


<form class="topForm" action="metadata-${section}.do" method="post">
<div id="forms"></div>
	
	<!-- The add link and the buttons should be first. The next div is hidden. -->
	<a id="plus" href="" onclick="return false;">Add new Taxonomic Coverage</a>
	<div class="buttons">
 			<@s.submit name="save" key="button.save"/>
 			<@s.submit name="cancel" key="button.cancel"/>
   </div>
   
   <div id='toAdd' style="visibility:hidden" >
      
      <@text  i18nkey="manage.metadata.taxcoverage.description" name="description" />  	

         <div class="half">
            <@input i18nkey="manage.metadata.taxcoverage.scientificName" name="scientificName" />
            <@input i18nkey="manage.metadata.taxcoverage.commonName" name="commonName" />
         </div>
         
      <@select i18nkey="manage.metadata.taxcoverage.rank"  name="rank" options=ranks value="value" />	 
   	  
      <div class="newline"></div>
      <div class="right">
        <a id="removeLink" class="removeLink" href="" onclick="return false;">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.taxcoverage.item'/> ]</a>
      </div>
      <div class="newline"></div>
      <div class="horizontal_dotted_line_large_foo" id="separator"></div>
      <div class="newline"></div>
	</div>
	
</form>
<#include "/WEB-INF/pages/inc/footer.ftl">