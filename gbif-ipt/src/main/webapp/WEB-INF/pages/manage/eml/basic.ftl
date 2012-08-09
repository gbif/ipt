 <#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.metadata.basic.title'/></title>
<script type="text/javascript">
	$(document).ready(function(){
		initHelp();	
		 		
		 	var resourceType="${resource.coreTypeTerm!}";	
		 	if(resourceType != ""){
				if(resourceType.indexOf("Occurrence")!=-1){	
					$("#resource\\.coreType").attr('disabled','disabled');
				}else{					
					if (resourceType.indexOf("Taxon")!=-1){	
						$("#resource\\.coreType").attr('disabled','disabled');
					}
				}			
			}
						
			function getList(list){
				var arr=  list.split(",");
				var newlistaOccurrence={};
				for(index in arr ){
					var val=arr[index].replace(/{|}/g,'');
					var arr2=val.split('=');
					var str=arr2[0].replace(/^\s*|\s*$/g,"");
					newlistaOccurrence[str]=arr2[1];
				}
				return newlistaOccurrence;
			}
			
			$("#resource\\.coreType").change(function(){
				var optionType=$("#resource\\.coreType").val();
				$("#resource\\.subtype").attr('selectedIndex', '0');
				$("#resource\\.subtype").css("width", "310px");
				switch(optionType)
		        {
              case 'Occurrence':
                $('#resource\\.subtype >option').remove();
                var list=getList("${occurrenceSubtypesMap}");
                $.each(list, function(key, value) {
                  $('#resource\\.subtype').append('<option value="'+key+'">'+value+'</option>');
                });
              break;
              case 'Checklist':
                $('#resource\\.subtype >option').remove();
                var list=getList("${checklistSubtypesMap}");
                $.each(list, function(key, value) {
                  $('#resource\\.subtype').append('<option value="'+key+'">'+value+'</option>');
                });
              break;
              case 'Other':
                $('#resource\\.subtype >option').remove();
                $('#resource\\.subtype').append('<option value="">No subtype</option>');
              break;
              default:
                $('#resource\\.subtype >option').remove();
                $('#resource\\.subtype').append('<option value=""></option>');
              break;
		        }
			});			
			
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
		$("#copyDetails2").click(function(event) {
			event.preventDefault();
			$("#eml\\.metadataProvider\\.firstName").attr("value", $("#eml\\.contact\\.firstName").attr("value"));
			$("#eml\\.metadataProvider\\.lastName").attr("value", $("#eml\\.contact\\.lastName").attr("value"));
			$("#eml\\.metadataProvider\\.position").attr("value", $("#eml\\.contact\\.position").attr("value"));
			$("#eml\\.metadataProvider\\.organisation").attr("value", $("#eml\\.contact\\.organisation").attr("value"));
			$("#eml\\.metadataProvider\\.address\\.address").attr("value", $("#eml\\.contact\\.address\\.address").attr("value"));
			$("#eml\\.metadataProvider\\.address\\.city").attr("value", $("#eml\\.contact\\.address\\.city").attr("value"));
			$("#eml\\.metadataProvider\\.address\\.province").attr("value", $("#eml\\.contact\\.address\\.province").attr("value"));
			$("#eml\\.metadataProvider\\.address\\.postalCode").attr("value", $("#eml\\.contact\\.address\\.postalCode").attr("value"));
			$("#eml\\.metadataProvider\\.address\\.country").attr("value", $("#eml\\.contact\\.address\\.country").attr("value"));
			$("#eml\\.metadataProvider\\.phone").attr("value", $("#eml\\.contact\\.phone").attr("value"));
			$("#eml\\.metadataProvider\\.email").attr("value", $("#eml\\.contact\\.email").attr("value"));
			$("#eml\\.metadataProvider\\.homepage").attr("value", $("#eml\\.contact\\.homepage").attr("value"));		
		});		
	});
	
</script>
<#assign sideMenuEml=true />
<#assign currentMenu="manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<h1><span class="superscript">Resource Title</span>
    <a class="tooltip" href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}
        <span class="classic">Click the title to go to the <em>resource overview</em> page.<div class="arrow-wrap"><div class="arrow"></div></div></span>
    </a>
</h1>
<div class="grid_17 suffix_1">
<h2 class="subTitle"><@s.text name='manage.metadata.basic.title'/></h2>
    <form class="topForm" action="metadata-${section}.do" method="post">
    <p><@s.text name="manage.metadata.basic.required.message" /></p>
  	<@input name="eml.title" />
  	<@text name="eml.description"/>
  	<div class="halfcolumn">
	  	<@select name="eml.metadataLanguage" help="i18n" options=languages value="${metadataLanguageIso3!'eng'}" />
	</div>
	<div class="halfcolumn">
  		<@select name="eml.language" help="i18n" options=languages value="${languageIso3!'eng'}" />
	</div>	
	<div class="halfcolumn">
		<@select name="resource.coreType" help="i18n" options=types value="${resource.coreType!''}"/>
	</div>
	<div class="halfcolumn" id="selectSubtypeDiv">
    <@select name="resource.subtype" i18nkey="resource.subtype" help="i18n" value="${resource.subtype!''}" options=listSubtypes />
  </div>
  	
  	<!-- Resource Contact -->    	
  	<div class="basicMetadata grid_17 suffix_1">
      	<@textinline name="eml.contact" help="i18n"/>
    	<div class="halfcolumn">
    	  	<@input name="eml.contact.firstName" />
    	</div>
      	<div class="halfcolumn">	  	
    	  	<@input name="eml.contact.lastName" />
      	</div>
      	<div class="halfcolumn">
      		<@input name="eml.contact.position" />
    	</div>
      	<div class="halfcolumn">			
      		<@input name="eml.contact.organisation" />
      	</div>
      	<div class="halfcolumn">
      		<@input name="eml.contact.address.address" />
    	</div>
      	<div class="halfcolumn">			  		
      		<@input name="eml.contact.address.city" />
      	</div>
      	<div class="halfcolumn">
      		<@input name="eml.contact.address.province" />
    	</div>
      	<div class="halfcolumn">		  		
      		<@select name="eml.contact.address.country" help="i18n" options=countries value="${eml.contact.address.country!}"/>
       	</div>
      	<div class="halfcolumn">
      		<@input name="eml.contact.address.postalCode" />
    	</div>
      	<div class="halfcolumn">	  		
      		<@input name="eml.contact.phone" />
      	</div>
    	<div class="halfcolumn">
    	  	<@input name="eml.contact.email" />
    	</div>
      	<div class="halfcolumn">		  	
    	  	<@input name="eml.contact.homepage" />
      	</div> 	
  	</div>  	
	
  	<!-- Resource Creator -->
	<div class="basicMetadata grid_17 suffix_1">
      	<@textinline name="eml.resourceCreator" help="i18n"/>
      	<div class="right">
      		<a id="copyDetails" href="">[ <@s.text name="eml.resourceCreator.copyLink" />  ]</a>
      	</div>  	
      	<div class="halfcolumn">
    		<@input name="eml.resourceCreator.firstName" />
    	</div>
      	<div class="halfcolumn">
    		<@input name="eml.resourceCreator.lastName" />
    	</div>
      	<div class="halfcolumn">
    		<@input name="eml.resourceCreator.position" />
    	</div>
      	<div class="halfcolumn">		
    		<@input name="eml.resourceCreator.organisation" />
    	</div>
      	<div class="halfcolumn">
    		<@input name="eml.resourceCreator.address.address" />
    	</div>
      	<div class="halfcolumn">		
    		<@input name="eml.resourceCreator.address.city" />
    	</div>
      	<div class="halfcolumn">
    		<@input name="eml.resourceCreator.address.province" />
    	</div>
      	<div class="halfcolumn">		
    		<@select name="eml.resourceCreator.address.country" help="i18n" options=countries value="${eml.getResourceCreator().address.country!}"/>
    	</div>		
      	<div class="halfcolumn">
    		<@input name="eml.resourceCreator.address.postalCode" />
    	</div>
      	<div class="halfcolumn">		
    		<@input name="eml.resourceCreator.phone" />
    	</div>
      	<div class="halfcolumn">
    		<@input name="eml.resourceCreator.email" />
    	</div>
      	<div class="halfcolumn">		
    		<@input name="eml.resourceCreator.homepage" />
    	</div>
  	</div>  	

  	<!-- Metadata Provider -->
	<div class="basicMetadata grid_17 suffix_1">
      	<@textinline name="eml.metadataProvider" help="i18n"/>  	
      	<div class="right">
      		<a id="copyDetails2" href="">[ <@s.text name="eml.resourceCreator.copyLink" />  ]</a>
      	</div>
      	<div class="halfcolumn">
    		<@input name="eml.metadataProvider.firstName" />
    	</div>
      	<div class="halfcolumn">				
    		<@input name="eml.metadataProvider.lastName" />
    	</div>
    	<div class="halfcolumn">
    		<@input name="eml.metadataProvider.position" />
    	</div>
      	<div class="halfcolumn">				
    		<@input name="eml.metadataProvider.organisation" />
    	</div>
    	<div class="halfcolumn">
    		<@input name="eml.metadataProvider.address.address" />
    	</div>
      	<div class="halfcolumn">				
    		<@input name="eml.metadataProvider.address.city" />
    	</div>
    	<div class="halfcolumn">
    		<@input name="eml.metadataProvider.address.province" />
    	</div>
      	<div class="halfcolumn">			
    		<@select name="eml.metadataProvider.address.country" help="i18n" options=countries value="${eml.metadataProvider.address.country!}"/>
    	</div>		
    	<div class="halfcolumn">
    		<@input name="eml.metadataProvider.address.postalCode" />
    	</div>
      	<div class="halfcolumn">				
    		<@input name="eml.metadataProvider.phone" />
    	</div>
    	<div class="halfcolumn">
    		<@input name="eml.metadataProvider.email" />
    	</div>
      	<div class="halfcolumn">				
    		<@input name="eml.metadataProvider.homepage" />
    	</div>
    </div>		

	<div class="buttons">
 		<@s.submit cssClass="button" name="save" key="button.save"/>
 		<@s.submit cssClass="button" name="cancel" key="button.cancel"/>
	</div>
	<!-- internal parameter -->
	<input name="r" type="hidden" value="${resource.shortname}" />  
</form>
</div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>