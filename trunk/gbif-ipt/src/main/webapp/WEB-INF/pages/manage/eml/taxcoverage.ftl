<#include "/WEB-INF/pages/inc/header.ftl">
<!-- #include "/WEB-INF/pages/macros/metadata.ftl"/ -->
<script type="text/javascript">
	$(document).ready(function(){
		initHelp();
		
		var	itemsCount=-1;
		calcNumberOfItems();
		function calcNumberOfItems(){
			var lastItem = $("#items .item:last-child").attr("id");
			if(lastItem != undefined)
				itemsCount=parseInt(lastItem.split("-")[1]);
			else
				itemsCount=-1;
		}
		
		function addNewItem(effects){
			var newItem=$('#baseItem').clone();
			if(effects) newItem.hide();
			newItem.appendTo('#items');
			if(effects) newItem.slideDown('slow');			
			initHelp("#baseItem");			
			setItemIndex(newItem, ++itemsCount);
		}
		
		function addNewTaxon(event, baseItem){
			event.preventDefault();
			// calculating the last taxon index.
			var idBaseItem = baseItem.attr("id");
			var lastIndex = $("#"+idBaseItem+" .sub-item:last-child").attr("id");
			if(lastIndex == undefined) {
				lastIndex = 0;
			} else {
				lastIndex = parseInt(lastIndex.split("-")[1])+1;
			}
			
			// cloning the taxonItem and setting the corresponding id.
			var taxonItem = $("#taxon-9999").clone();
			
			// setting the ids to the rest of the components of the taxonItem.
			setSubItemIndex(baseItem, taxonItem, lastIndex);
			
			$("#"+idBaseItem+" #sub-baseItem").append(taxonItem);
			taxonItem.slideDown('slow');
		}
		
		function setSubItemIndex(baseItem, subItem, subBaseIndex) {
			subItem.attr("id", "taxon-"+subBaseIndex);
		}
		
		function removeItem(event){
			event.preventDefault();
			var $target = $(event.target);
			$('#item-'+$target.attr("id").split("-")[1]).slideUp('slow', function() { 
				$(this).remove();
				$("#items .item").each(function(index) { 
						setItemIndex($(this), index);
					});
				calcNumberOfItems();
				});
		}
		
		function setItemIndex(item, index){
			item.attr("id","item-"+index);
			$("#item-"+index+" .removeLink").attr("id", "removeLink-"+index);
			$("#removeLink-"+index).click(function(event) {
				removeItem(event);
			});
			$("#item-"+index+" #plus-taxon").attr("id", "plus-taxon-"+index);
			$("#plus-taxon-"+index).click(function(event){			
				addNewTaxon(event, item);
			});
			
			$("#item-"+index+" [id$='description']").attr("id", "eml.taxonomicCoverages["+index+"].description").attr("name", function() {return $(this).attr("id");});
			$("#item-"+index+" [for$='description']").attr("for", "eml.taxonomicCoverages["+index+"].description");
			$("#item-"+index+" [id$='scientificName']").attr("id", "eml.taxonomicCoverages["+index+"].taxonKeyword.scientificName").attr("name", function() {return $(this).attr("id");});
			$("#item-"+index+" [for$='scientificName']").attr("for", "eml.taxonomicCoverages["+index+"].taxonKeyword.scientificName");
			$("#item-"+index+" [id$='commonName']").attr("id", "eml.taxonomicCoverages["+index+"].taxonKeyword.commonName").attr("name", function() {return $(this).attr("id");});
			$("#item-"+index+" [for$='commonName']").attr("for", "eml.taxonomicCoverages["+index+"].taxonKeyword.commonName");
			$("#item-"+index+" [id$='rank']").attr("id", "eml.taxonomicCoverages["+index+"].taxonKeyword.rank").attr("name", function() {return $(this).attr("id");});
			$("#item-"+index+" [for$='rank']").attr("for", "eml.taxonomicCoverages["+index+"].taxonKeyword.rank");
			
						
		}
		
		$("#plus").click(function(event) {
			event.preventDefault();
			addNewItem(true);
		});
		
		$("#plus-taxon").click(function(event) {
			addNewTaxon(event);
		});
		
		$(".removeLink").click(function(event) {
			removeItem(event);
		});		
		
		
	});   
</script>
<title><@s.text name='manage.metadata.taxcoverage.title'/></title>
<#assign sideMenuEml=true />
 <#assign currentMenu="manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<h1><@s.text name='manage.metadata.taxcoverage.title'/>: <em>${resource.title!resource.shortname}</em></h1>
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
				<@text  i18nkey="eml.taxonomicCoverages.description" name="eml.taxonomicCoverages[${item_index}].description" help="i18n" />
				<div class="half">
           			<@input i18nkey="eml.taxonomicCoverages.taxonKeyword.scientificName" name="eml.taxonomicCoverages[${item_index}].taxonKeyword.scientificName" />
           			<@input i18nkey="eml.taxonomicCoverages.taxonKeyword.commonName" name="eml.taxonomicCoverages[${item_index}].taxonKeyword.commonName" />
        		</div>     	
           		<@select i18nkey="eml.taxonomicCoverages.taxonKeyword.rank"  name="eml.taxonomicCoverages[${item_index}].taxonKeyword.rank" options=ranks value="${eml.taxonomicCoverages[item_index].taxonKeyword.rank}" />
   	  			<div class="newline"></div>			
				<div class="horizontal_dotted_line_large_foo" id="separator"></div>
				<div class="newline"></div>
				<div class="newline"></div>
			</div>
		</#list>
	</div>	
	<div class="newline"></div>
	<a id="plus" href="" ><@s.text name='manage.metadata.addnew' /> <@s.text name='manage.metadata.taxcoverage.item' /></a>
	<div class="buttons">
		<@s.submit name="save" key="button.save"/>
		<@s.submit name="cancel" key="button.cancel"/>
	</div>
	<!-- internal parameter -->
	<input name="r" type="hidden" value="${resource.shortname}" />
</form>
<!-- The base form that is going to be cloned every time an user clic in the 'add' link -->
<!-- The next divs are hidden. -->
<div id='baseItem' class="item" style="display:none">
	<div class="right">
		<a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.taxcoverage.item'/> ]</a>
	</div>
	<div class="newline"></div>
	<@text  i18nkey="eml.taxonomicCoverages.description" help="i18n" name="description" />
	<div id="sub-baseItem">
	</div>
	<br>
	<div class="newline"></div>
	<a id="plus-taxon" href="" ><@s.text name='manage.metadata.addnew' /> <@s.text name='manage.metadata.taxcoverage.taxon.item' /></a> 	  
	<div class="newline"></div>      
	<div class="horizontal_dotted_line_large_foo" id="separator"></div>
	<div class="newline"></div>
	<div class="newline"></div>
</div>
<div id='taxon-9999' class="sub-item" style="display:none">
	<div class="third">
		<@input i18nkey="eml.taxonomicCoverages.taxonKeyword.scientificName" name="scientificName" />
		<@input i18nkey="eml.taxonomicCoverages.taxonKeyword.commonName" name="commonName" />
		<@select i18nkey="eml.taxonomicCoverages.taxonKeyword.rank"  name="rank" options=ranks />		
		<br><br>
		<img id="trash" src="http://localhost:7001/ipt/images/trash-m.png">
	</div>
	<div class="newline"></div>
</div>
<#include "/WEB-INF/pages/inc/footer.ftl">