<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name='manage.metadata.taxcoverage.title'/></title>
	<#assign sideMenuEml=true />
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name='manage.metadata.taxcoverage.title'/>: <em>${ms.resource.title!ms.resource.shortname}</em></h1>
<p><@s.text name='manage.metadata.taxcoverage.intro'/></p>

<#include "/WEB-INF/pages/macros/forms.ftl"/>

<script type="text/javascript">
	var count = new Number(1);

	$(function() {	
		$("#plus").click(function() {						
			var theNewForm = $("#toAdd").clone().attr('id', 'form'+count).css('visibility', '');
			$("#forms").append(theNewForm);			
			
			//$("#form"+count+" #eml\\.taxonomicCoverages\\[0\\]\\.description").attr("id", "eml.taxonomicCoverages["+count+"].taxonKeyword.scientificName");
			$("#form"+count+" #description")
				.attr("id", "eml.taxonomicCoverages["+count+"].description")
				.attr("name", "eml.taxonomicCoverages["+count+"].description");
			
			
			//$("#form"+count+" #eml\\.taxonomicCoverages\\[\\0\\]\\.taxonKeyword\\.scientificName").attr("id", "eml.taxonomicCoverages["+count+"].taxonKeyword.scientificName");
			$("#form"+count+" #scientificName")
				.attr("id", "eml.taxonomicCoverages["+count+"].taxonKeyword.scientificName")
				.attr("name", "eml.taxonomicCoverages["+count+"].taxonKeyword.scientificName");
			
			$("#form"+count+" #commonName")
				.attr("id", "eml.taxonomicCoverages["+count+"].taxonKeyword.commonName")
				.attr("name", "eml.taxonomicCoverages["+count+"].taxonKeyword.commonName");

			//TODO Falta aplicar la implementacion de la internacionalizacion del label.
			$("#form"+count+" #eml\\.taxonomicCoverages\\[0\\]\\.taxonKeyword\\.rank")
				.attr("id", "eml.taxonomicCoverages["+count+"].taxonKeyword.rank")
				.attr("name", "eml.taxonomicCoverages["+count+"].taxonKeyword.rank");

			$("#form"+count).hide().slideDown("slow");			
			count++;
		});	
	});	
</script>


<form class="topForm" action="metadata-${section}.do" method="post">
<div id="forms">ADD HERE THE NEW FORMS:<br></div>
	<br>
    ---------------------------------------------------------------------
    <br>
	<!-- The add link and the buttons should be first. The next div is hidden. -->
	<a id="plus" href="" onclick="return false;">Add new Taxonomic Coverage</a>
	<div class="buttons">
 			<@s.submit name="save" key="button.save"/>
 			<@s.submit name="cancel" key="button.cancel"/>
   </div>
   
   <div id='toAdd' style="visibility:hidden" >
      <@text  i8nkey="manage.metadata.taxcoverage.description" name="description" />  	

         <div class="half">
            <@input i8nkey="manage.metadata.taxcoverage.scientificName" name="scientificName" />
            <@input i8nkey="manage.metadata.taxcoverage.commonName" name="commonName" />
         </div>
         
      <@select name="eml.taxonomicCoverages[0].taxonKeyword.rank" options=ranks value="value" />	 
   	
	</div>
	
</form>
<#include "/WEB-INF/pages/inc/footer.ftl">