<#include "/WEB-INF/pages/inc/header.ftl">
<script type="text/javascript">
$(document).ready(function(){
	initHelp();
});   
</script>
<#include "/WEB-INF/pages/macros/metadata.ftl"/>
<title><@s.text name='manage.metadata.taxcoverage.title'/></title>
<#assign sideMenuEml=true />
<#include "/WEB-INF/pages/inc/menu.ftl">
<h1><@s.text name='manage.metadata.taxcoverage.title'/>: <em>${ms.resource.title!ms.resource.shortname}</em></h1>
<p><@s.text name='manage.metadata.taxcoverage.intro'/></p>
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<form class="topForm" action="metadata-${section}.do" method="post">
	<div id="items">
		<!-- Adding the taxonomic coverages that already exists on the file -->	
		<#assign next_agent_index=0 />
		<#list eml.taxonomicCoverages as item>	
			<div id='item-${item_index}' class="item">
				<div class="right">
    				<a id="removeLink-${item_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.taxcoverage.item'/> ]</a>
  				</div>
				<div class="newline"></div>
				<@text  i18nkey="eml.taxonomicCoverage.description" name="eml.taxonomicCoverages[${item_index}].description" help="i18n" />
				<div class="half">
           			<@input i18nkey="eml.taxonomicCoverage.scientificName" name="eml.taxonomicCoverages[${item_index}].taxonKeyword.scientificName" />
           			<@input i18nkey="eml.taxonomicCoverage.commonName" name="eml.taxonomicCoverages[${item_index}].taxonKeyword.commonName" />
        		</div>     	
           		<@select i18nkey="eml.taxonomicCoverage.rank"  name="eml.taxonomicCoverages[${item_index}].taxonKeyword.rank" options=ranks value="${eml.taxonomicCoverages[item_index].taxonKeyword.rank}" help="i18n" />
   	  			<div class="newline"></div>			
				<div class="horizontal_dotted_line_large_foo" id="separator"></div>
				<div class="newline"></div>
				<div class="newline"></div>
			</div>
		</#list>
	</div>	
	<!-- The add link and the buttons should be first. The next div is hidden. -->
	<a id="plus" href="" ><@s.text name='manage.metadata.addnew' /> <@s.text name='manage.metadata.taxcoverage.item' /></a>
	<div class="buttons">
		<@s.submit name="save" key="button.save"/>
		<@s.submit name="cancel" key="button.cancel"/>
	</div>
</form>
<!-- The base form that is going to be cloned every time an user clic in the 'add' link -->
<div id='baseItem' class="item" style="display:none">
	<div class="right">
		<a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.taxcoverage.item'/> ]</a>
	</div>
	<div class="newline"></div>
	<@text  i18nkey="eml.taxonomicCoverage.description" help="i18n" name="description" />
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
<#include "/WEB-INF/pages/inc/footer.ftl">