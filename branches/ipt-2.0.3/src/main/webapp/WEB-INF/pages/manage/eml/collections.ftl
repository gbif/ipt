<#escape x as x?html>
<#setting number_format="#####.##">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.metadata.collections.title'/></title>
<#include "/WEB-INF/pages/macros/metadata.ftl"/>
<#assign sideMenuEml=true />
 <#assign currentMenu="manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<h1><@s.text name='manage.metadata.collections.title'/>: <a href="resource.do?r=${resource.shortname}"><em>${resource.title!resource.shortname}</em></a> </h1>
<@s.text name='manage.metadata.collections.intro'/>
<form class="topForm" action="metadata-${section}.do" method="post"> 
	<div class="newline"></div>
	<div class="halfcolumn">
		<@input name="eml.collectionName" />
	</div>
	<div class="halfcolumn">
		<@input name="eml.collectionId" />
	</div>
	<div class="newline"></div>
	<div class="halfcolumn">
		<@input name="eml.parentCollectionId" />
	</div>
	<div class="halfcolumn">
		<@select name="eml.specimenPreservationMethod" value="${eml.specimenPreservationMethod!}" options=preservationMethods />
	</div>
	<div class="newline"></div>
	<h2><@s.text name="manage.metadata.collections.curatorialUnits.title"/></h2>
	<p><@s.text name="manage.metadata.collections.curatorialUnits.intro"/></p>
	<div class="newline"></div>
	<div id="separator" class="horizontal_dotted_line_large_foo"></div>
	<div class="newline"></div>
	<div id="items">	
		<#list eml.jgtiCuratorialUnits as item>
			<#assign type="${eml.jgtiCuratorialUnits[item_index].type}"/>
			<div id="item-${item_index}" class="item">
				<div class="newline"></div>
				<div class="right">
     		 		<a id="removeLink-${item_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.collections.curatorialUnits.item'/> ]</a>
   		 		</div>
   				<div class="newline"></div>
    			<@select name="type-${item_index}" i18nkey="eml.jgtiCuratorialUnits.type" value=type options=JGTICuratorialUnitTypeOptions />
    			<div class="newline"></div>
    			<div class="half">
    				<div id="subitem-${item_index}" class="subitem">
    					<#if type=="COUNT_RANGE">
    						<div id="range-${item_index}">
							 <div class="halfcolumn">
    							<@input name="eml.jgtiCuratorialUnits[${item_index}].rangeStart" i18nkey="eml.jgtiCuratorialUnits.rangeStart" size=40/>
							 </div>
							 <div class="halfcolumn">
    							<@input name="eml.jgtiCuratorialUnits[${item_index}].rangeEnd" i18nkey="eml.jgtiCuratorialUnits.rangeEnd" size=40/>
    						 </div>
							</div>
    					<#elseif type=="COUNT_WITH_UNCERTAINTY">
    						<div id="uncertainty-${item_index}">
    						 <div class="halfcolumn">
								<@input name="eml.jgtiCuratorialUnits[${item_index}].rangeMean" i18nkey="eml.jgtiCuratorialUnits.rangeMean" size=40/>
							 </div>
							 <div class="halfcolumn">
								<@input name="eml.jgtiCuratorialUnits[${item_index}].uncertaintyMeasure" i18nkey="eml.jgtiCuratorialUnits.uncertaintyMeasure" size=40/>
							 </div>
    						</div>
    					</#if>
    				</div>
    				<@input name="eml.jgtiCuratorialUnits[${item_index}].unitType" i18nkey="eml.jgtiCuratorialUnits.unitType" size=40/>
    			</div>
				<div class="newline"></div>
				<div id="separator" class="horizontal_dotted_line_large_foo"></div>
				<div class="newline"></div>
			</div>
		</#list>  	
	</div>
	<a id="plus" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.collections.curatorialUnits.item'/></a>
	<div class="newline"></div>
	<div class="newline"></div>    
	<div class="buttons">
  		<@s.submit cssClass="button" name="save" key="button.save" />
  		<@s.submit cssClass="button" name="cancel" key="button.cancel" />
	</div>
	<!-- internal parameter -->
	<input name="r" type="hidden" value="${resource.shortname}" />	
</form>

<div id="baseItem" class="item" style="display:none;">
	<div class="newline"></div>
	<div class="right">
		<a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.collections.curatorialUnits.item'/> ]</a>
	</div>
   	<div class="newline"></div>
    <@select name="type" i18nkey="eml.jgtiCuratorialUnits.type" value="COUNT_RANGE" options=JGTICuratorialUnitTypeOptions />
    <div class="newline"></div>
    <div class="half">
    	<div class="subitem">
    		<!-- The sub-form is here -->
    		<div id="range-99999" style="display:none" >
    			<div class="halfcolumn">
					<@input name="rangeStart" i18nkey="eml.jgtiCuratorialUnits.rangeStart" size=40/>
				</div>
				<div class="halfcolumn">
   					<@input name="rangeEnd" i18nkey="eml.jgtiCuratorialUnits.rangeEnd" size=40/>
				</div>	
			</div>
    	</div>
    	<@input name="unitType" i18nkey="eml.jgtiCuratorialUnits.unitType" size=40/>
    </div>
	<div class="newline"></div>
	<div id="separator" class="horizontal_dotted_line_large_foo"></div>
	<div class="newline"></div>
</div>
<div id="range-99999" style="display:none" >
	<div class="halfcolumn">
		<@input name="rangeStart" i18nkey="eml.jgtiCuratorialUnits.rangeStart" size=40/>
	</div>
	<div class="halfcolumn">
    	<@input name="rangeEnd" i18nkey="eml.jgtiCuratorialUnits.rangeEnd" size=40/>
    </div>
</div>
<div id="uncertainty-99999"  style="display:none" >
	<div class="halfcolumn">
		<@input name="rangeMean" i18nkey="eml.jgtiCuratorialUnits.rangeMean" size=40/>
	</div>
    <div class="halfcolumn">
    	<@input name="uncertaintyMeasure" i18nkey="eml.jgtiCuratorialUnits.uncertaintyMeasure" size=40/>
    </div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>