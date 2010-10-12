<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.metadata.basic.title'/></title>
<script type="text/javascript">
	$(document).ready(function(){
		initHelp();	
		$("#copyDetails").click(function(event) {
			event.preventDefault();
			$("#eml\\.resourceCreator\\.firstName").attr("value", $("#eml\\.contact\\.firstName").attr("value"));
			$("#eml\\.resourceCreator\\.lastName").attr("value", $("#eml\\.contact\\.lastName").attr("value"));
			$("#eml\\.resourceCreator\\.position").attr("value", $("#eml\\.contact\\.position").attr("value"));
			$("#eml\\.resourceCreator\\.organisation").attr("value", $("#eml\\.contact\\.organisation").attr("value"));
			$("#eml\\.resourceCreator\\.address\\.address").attr("value", $("#eml\\.contact\\.address\\.address").attr("value"));
			$("#eml\\.resourceCreator\\.address\\.city").attr("value", $("#eml\\.contact\\.address\\.city").attr("value"));
			$("#eml\\.resourceCreator\\.address\\.province").attr("value", $("#eml\\.contact\\.address\\.province").attr("value"));
			$("#eml\\.resourceCreator\\.address\\.postalCode").attr("value", $("#eml\\.contact\\.address\\.postalCode").attr("value"));
			$("#eml\\.resourceCreator\\.address\\.country").attr("value", $("#eml\\.contact\\.address\\.country").attr("value"));
			$("#eml\\.resourceCreator\\.phone").attr("value", $("#eml\\.contact\\.phone").attr("value"));
			$("#eml\\.resourceCreator\\.email").attr("value", $("#eml\\.contact\\.email").attr("value"));
			$("#eml\\.resourceCreator\\.homepage").attr("value", $("#eml\\.contact\\.homepage").attr("value"));		
		});
	});
	
</script>
<#assign sideMenuEml=true />
 <#assign currentMenu="manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<h1><@s.text name='manage.metadata.basic.title'/>: <em>${resource.title!resource.shortname}</em></h1>
<p><@s.text name='manage.metadata.basic.intro'/></p>
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<form class="topForm" action="metadata-${section}.do" method="post">
  	<@input name="eml.title" />
  	<@text name="eml.description" />
  	<div class="half">
	  	<@select name="resource.subtype" options=resourceTypes value="${resource.subtype!}" />
	  	<@select name="eml.language" help="i18n" options=languages value="${eml.language!}" />
  	</div>
  	<div class="newline"></div>
  	<div class="horizontal_dotted_line_large_foo" id="separator"></div>
  	
  	<!-- Resource Contact -->   	
  	<h2><@s.text name="eml.contact"/></h2>
	<div class="half">
	  	<@input name="eml.contact.firstName" />
	  	<@input name="eml.contact.lastName" />
  	</div>
  	<div class="half">
  		<@input name="eml.contact.position" />
  		<@input name="eml.contact.organisation" />
  	</div>
  	<div class="half">
  		<@input name="eml.contact.address.address" />
  		<@input name="eml.contact.address.city" />
  	</div>
  	<div class="half">
  		<@input name="eml.contact.address.province" />
  		<@select name="eml.contact.address.country" options=countries value="${eml.contact.address.country!}"/>
   	</div>
  	<div class="half">
  		<@input name="eml.contact.address.postalCode" />
  		<@input name="eml.contact.phone" />
  	</div>
	<div class="half">
	  	<@input name="eml.contact.email" />
	  	<@input name="eml.contact.homepage" />
  	</div> 	
	<div class="newline"></div>
  	<div class="horizontal_dotted_line_large_foo" id="separator"></div>  	

  	<!-- Resource Creator -->
  	<div class="right">
  		<a id="copyDetails" href="">[ <@s.text name="eml.resourceCreator.copyLink" />  ]</a>
  	</div>
  	<h2><@s.text name="eml.resourceCreator"/></h2>
  	<div class="half">
		<@input name="eml.resourceCreator.firstName" />
		<@input name="eml.resourceCreator.lastName" />
	</div>
	<div class="half">
		<@input name="eml.resourceCreator.position" />
		<@input name="eml.resourceCreator.organisation" />
	</div>
	<div class="half">
		<@input name="eml.resourceCreator.address.address" />
		<@input name="eml.resourceCreator.address.city" />
	</div>
	<div class="half">
		<@input name="eml.resourceCreator.address.province" />
		<@select name="eml.resourceCreator.address.country" options=countries value="${eml.getResourceCreator().address.country!}"/>
	</div>		
	<div class="half">
		<@input name="eml.resourceCreator.address.postalCode" />
		<@input name="eml.resourceCreator.phone" />
	</div>
	<div class="half">
		<@input name="eml.resourceCreator.email" />
		<@input name="eml.resourceCreator.homepage" />
	</div>
	<div class="newline"></div>
  	<div class="horizontal_dotted_line_large_foo" id="separator"></div>  	
  	
  	<!-- Metadata Provider -->
	<h2><@s.text name="eml.metadataProvider"/></h2>
	
  	<div class="half">
		<@input name="eml.metadataProvider.firstName" />
		<@input name="eml.metadataProvider.lastName" />
	</div>
	<div class="half">
		<@input name="eml.metadataProvider.position" />
		<@input name="eml.metadataProvider.organisation" />
	</div>
	<div class="half">
		<@input name="eml.metadataProvider.address.address" />
		<@input name="eml.metadataProvider.address.city" />
	</div>
	<div class="half">
		<@input name="eml.metadataProvider.address.province" />
		<@select name="eml.metadataProvider.address.country" options=countries value="${eml.metadataProvider.address.country!}"/>
	</div>		
	<div class="half">
		<@input name="eml.metadataProvider.address.postalCode" />
		<@input name="eml.metadataProvider.phone" />
	</div>
	<div class="half">
		<@input name="eml.metadataProvider.email" />
		<@input name="eml.metadataProvider.homepage" />
	</div>
	
	<div class="buttons">
 		<@s.submit name="save" key="button.save"/>
 		<@s.submit name="cancel" key="button.cancel"/>
	</div>
	<!-- internal parameter -->
	<input name="r" type="hidden" value="${resource.shortname}" />  
</form>
<#include "/WEB-INF/pages/inc/footer.ftl">
