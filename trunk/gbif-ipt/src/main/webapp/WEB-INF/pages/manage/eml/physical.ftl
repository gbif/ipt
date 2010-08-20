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
<title><@s.text name='manage.metadata.physical.title'/></title>
<script type="text/javascript">
$(document).ready(function(){
	var	itemsCount=-1;
	calcNumberOfItems();
	
	function calcNumberOfItems(){
		var lastItem = $("#items .item:last-child").attr("id");
		if(lastItem != undefined)
			itemsCount=parseInt(lastItem.split("-")[1]);
		else
			itemsCount=-1;
	}
	
	$("#plus").click(function(event) {
		event.preventDefault();
		// to add more items, clone the first one and change it's attributes
		var newItem=$('#baseItem').clone();
		newItem.hide();
		newItem.appendTo('#items').slideDown('slow');
		setItemIndex(newItem, ++itemsCount);
	});
		
	$(".removeLink").click(function(event) {
		removeItem(event);
	});
		
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
		<#if "${section}"=="physical">
			$("#item-"+index+" input").attr("id",function() {
				var parts=$(this).attr("id").split(".");var n=parseInt(parts.length)-1;
				return "eml.physicalData["+index+"]."+parts[n]; });
			$("#item-"+index+" select").attr("id",function() {
				var parts=$(this).attr("id").split(".");var n=parseInt(parts.length)-1;
				return "eml.physicalData["+index+"]."+parts[n]; });
			$("#item-"+index+" label").attr("for",function() {
				var parts=$(this).attr("for").split(".");var n=parseInt(parts.length)-1;
				return "eml.physicalData["+index+"]."+parts[n]; });		
			$("#item-"+index+" input").attr("name",function() {return $(this).attr("id"); });
			$("#item-"+index+" select").attr("name",function() {return $(this).attr("id"); });
		</#if>
		
	}
		
});
</script>
<#assign sideMenuEml=true />
 
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<h1><@s.text name='manage.metadata.physical.title'/>: <em>${ms.resource.title!ms.resource.shortname}</em></h1>
<@s.text name='manage.metadata.physical.intro'/>
<form class="topForm" action="metadata-${section}.do" method="post"> 

<div id="items">
<#assign next_agent_index=0 />
<#list eml.physicalData as agent>
	<#assign next_agent_index=agent_index+1>
	<div id="item-${agent_index}" class="item">
	<div class="newline"></div>
	<div class="right">
      <a id="removeLink-${agent_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.physical.item'/> ]</a>
    </div>
    <div class="newline"></div>
    <div class="half">
		<@input name="eml.physicalData[${agent_index}].name" i18nkey="eml.physicalData.name" size=40/>
		<@input name="eml.physicalData[${agent_index}].charset" i18nkey="eml.physicalData.charset" size=40/>
	</div>	
	<@input name="eml.physicalData[${agent_index}].distributionUrl" i18nkey="eml.physicalData.distributionUrl" size=40/>
  	<div class="half">
		<@input name="eml.physicalData[${agent_index}].format" i18nkey="eml.physicalData.format" size=40/>
		<@input name="eml.physicalData[${agent_index}].formatVersion" i18nkey="eml.physicalData.formatVersion" size=40/>
	</div>
  	<div class="newline"></div>
	<div class="horizontal_dotted_line_large_foo" id="separator"></div>
	<div class="newline"></div>
  	</div>
</#list>
</div>
  <a id="plus" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.physical.item'/></a>
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
      <a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.physical.item'/> ]</a>
    </div>
    <div class="newline"></div>
	<div class="half">
		<@input name="name" i18nkey="eml.physicalData.name" size=40/>
		<@input name="charset" i18nkey="eml.physicalData.charset" size=40/>
	</div>
	<@input name="distributionUrl" i18nkey="eml.physicalData.distributionUrl" size=40/>
	<div class="half">
		<@input name="format" i18nkey="eml.physicalData.format" size=40/>
		<@input name="formatVersion" i18nkey="eml.physicalData.formatVersion" size=40/>
	</div>
  	<div class="newline"></div>
	<div class="horizontal_dotted_line_large_foo" id="separator"></div>
	<div class="newline"></div>
</div>
<#include "/WEB-INF/pages/inc/footer.ftl">