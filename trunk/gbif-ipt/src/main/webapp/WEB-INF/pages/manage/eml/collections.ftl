<!--
/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
-->

<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.metadata.collections.title'/></title>
<#include "/WEB-INF/pages/macros/metadata.ftl"/>

<#assign sideMenuEml=true />
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<h1><@s.text name='manage.metadata.collections.title'/>: <em>${resource.title!resource.shortname}</em></h1>
<@s.text name='manage.metadata.collections.intro'/>
<form class="topForm" action="metadata-${section}.do" method="post"> 
	<div class="newline"></div>
	<div class="half">
		<@input name="eml.collectionName" />
		<@input name="eml.collectionId" />
	</div>
	<div class="newline"></div>
	<div class="half">
		<@input name="eml.parentCollectionId" />
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
    						<div id="range-${item_index}" class="half">
    							<@input name="eml.jgtiCuratorialUnits[${item_index}].rangeStart" i18nkey="eml.jgtiCuratorialUnits.rangeStart" size=40/>
    							<@input name="eml.jgtiCuratorialUnits[${item_index}].rangeEnd" i18nkey="eml.jgtiCuratorialUnits.rangeEnd" size=40/>
							</div>
    					<#elseif type=="COUNT_WITH_UNCERTAINTY">
    						<div id="uncertainty-${item_index}" class="half">
								<@input name="eml.jgtiCuratorialUnits[${item_index}].rangeMean" i18nkey="eml.jgtiCuratorialUnits.rangeMean" size=40/>
								<@input name="eml.jgtiCuratorialUnits[${item_index}].uncertaintyMeasure" i18nkey="eml.jgtiCuratorialUnits.uncertaintyMeasure" size=40/>
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
  		<@s.submit name="save" key="button.save" />
  		<@s.submit name="cancel" key="button.cancel" />
	</div>
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
    		<div id="range-99999" class="half" style="display:none" >
				<@input name="rangeStart" i18nkey="eml.jgtiCuratorialUnits.rangeStart" size=40/>
   				<@input name="rangeEnd" i18nkey="eml.jgtiCuratorialUnits.rangeEnd" size=40/>
			</div>    		
    	</div>
    	<@input name="unitType" i18nkey="eml.jgtiCuratorialUnits.unitType" size=40/>
    </div>
	<div class="newline"></div>
	<div id="separator" class="horizontal_dotted_line_large_foo"></div>
	<div class="newline"></div>
</div>
<div id="range-99999" class="half" style="display:none" >
	<@input name="rangeStart" i18nkey="eml.jgtiCuratorialUnits.rangeStart" size=40/>
    <@input name="rangeEnd" i18nkey="eml.jgtiCuratorialUnits.rangeEnd" size=40/>
</div>
<div id="uncertainty-99999" class="half" style="display:none" >
	<@input name="rangeMean" i18nkey="eml.jgtiCuratorialUnits.rangeMean" size=40/>
    <@input name="uncertaintyMeasure" i18nkey="eml.jgtiCuratorialUnits.uncertaintyMeasure" size=40/>
</div>
<#include "/WEB-INF/pages/inc/footer.ftl">