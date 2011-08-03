<#escape x as x?html>
<#setting number_format="#####.##">
<#include "/WEB-INF/pages/inc/header.ftl">
<#include "/WEB-INF/pages/macros/metadata.ftl"/>
<script type="text/javascript">
	$(document).ready(function(){
		initHelp();		
	});   
</script>
<title><@s.text name='manage.metadata.taxcoverage.title'/></title>
<#assign sideMenuEml=true />
 <#assign currentMenu="manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<h1><@s.text name='manage.metadata.taxcoverage.title'/>: <a href="resource.do?r=${resource.shortname}"><em>${resource.title!resource.shortname}</em></a> </h1>
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
  				<@text  i18nkey="eml.taxonomicCoverages.description" help="i18n" name="eml.taxonomicCoverages[${item_index}].description" />
  				<!-- Taxon list-->
				<!-- a id="taxonsLink-${item_index}" class="show-taxonList" href="" ><@s.text name='manage.metadata.addseveral' /> <@s.text name='manage.metadata.taxcoverage.taxon.items' /></a -->
				<@link name="taxonsLink-${item_index}" class="show-taxonList" value="manage.metadata.taxcoverage.addSeveralTaxa" help="i18n" i18nkey="manage.metadata.taxcoverage.addSeveralTaxa"/>	
				<div id="list-${item_index}" class="half" style="display:none">
					<@text i18nkey="eml.taxonomicCoverages.taxonList" help="i18n" name="taxon-list-${item_index}" value="" />
					<div class="buttons">
						<@s.submit cssClass="button" name="add-button-${item_index}" key="button.add"/>
					</div>
				</div>				
				<div id="subItems">
					<#list item.taxonKeywords as subItem>
						<div id="subItem-${subItem_index}" class="sub-item">
							<div class="third">
								<@input i18nkey="eml.taxonomicCoverages.taxonKeyword.scientificName" name="eml.taxonomicCoverages[${item_index}].taxonKeywords[${subItem_index}].scientificName" />
								<@input i18nkey="eml.taxonomicCoverages.taxonKeyword.commonName" name="eml.taxonomicCoverages[${item_index}].taxonKeywords[${subItem_index}].commonName" />
								<@select i18nkey="eml.taxonomicCoverages.taxonKeyword.rank"  name="eml.taxonomicCoverages[${item_index}].taxonKeywords[${subItem_index}].rank" options=ranks value="${eml.taxonomicCoverages[item_index].taxonKeywords[subItem_index].rank!?lower_case}"/>		
								<br><br>
								<img id="trash-${item_index}-${subItem_index}" src="${baseURL}/images/trash-m.png">
							</div>
							<div class="newline"></div>
						</div>						
					</#list>
				</div>
				<br>
				<div class="newline"></div>
				<a id="plus-subItem-${item_index}" href="" ><@s.text name='manage.metadata.addnew' /> <@s.text name='manage.metadata.taxcoverage.taxon.item' /></a> 	  
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
		<@s.submit cssClass="button" name="save" key="button.save"/>
		<@s.submit cssClass="button" name="cancel" key="button.cancel"/>
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
	<@text i18nkey="eml.taxonomicCoverages.description" help="i18n" name="description" />

	<!-- Taxon list-->
	<a id="taxonsLink" class="show-taxonList" href="" ><@s.text name='manage.metadata.taxcoverage.addSeveralTaxa' /></a>	
	<div id="list" class="half" style="display:none">
		<@text i18nkey="eml.taxonomicCoverages.taxonList" help="i18n" name="taxon-list" value="" />
		<div class="buttons">
			<@s.submit cssClass="button" name="add-button" key="button.add"/>
		</div>
	</div>
	<div id="subItems">
	</div>
	<br>
	<div class="newline"></div>
	<a id="plus-subItem" href="" ><@s.text name='manage.metadata.addnew' /> <@s.text name='manage.metadata.taxcoverage.taxon.item' /></a> 	  
	<div class="newline"></div>      
	<div class="horizontal_dotted_line_large_foo" id="separator"></div>
	<div class="newline"></div>
	<div class="newline"></div>
</div>
<div id='subItem-9999' class="sub-item" style="display:none">
	<div class="third">
		<@input i18nkey="eml.taxonomicCoverages.taxonKeyword.scientificName" name="scientificName" />
		<@input i18nkey="eml.taxonomicCoverages.taxonKeyword.commonName" name="commonName" />
		<@select i18nkey="eml.taxonomicCoverages.taxonKeyword.rank"  name="rank" options=ranks />		
		<br><br>
		<img id="trash" src="${baseURL}/images/trash-m.png">
	</div>
	<div class="newline"></div>
</div>
<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>