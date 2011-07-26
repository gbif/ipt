<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name='manage.mapping.title'/></title>
	<script type="text/javascript" src="${baseURL}/js/jconfirmation.jquery.js"></script>
<script type="text/javascript">
$(document).ready(function(){
	function showHideIdSuffix(){
		if($('#idColumn option:selected').val()!="" && $('#idColumn option:selected').val()>=-1){
			$('.idSuffix').show();
		}else{
			$('.idSuffix').hide();
			$('input.idSuffix').val("");
		}
	}
	function showHideFilter(){
		if($('#filterComp option:selected').val()=="Equals" || $('#filterComp option:selected').val()=="NotEquals"){
			$('#filterParam').show();
		}else{
			$('#filterParam').hide();
			$('#filterParam').val("");
		}
	}
	function showHideFilterName() {
		if($("#filterName option:selected").val() == "") {
			$("#filterComp").hide();
			$("#filterComp").val("");
			showHideFilter();
		} else {
			$("#filterComp").show();
		}
	}
	function hideFields() {
		showAll=false;
		$("#showAllValue").val("false");
		$("#toggleFields").text("<@s.text name="manage.mapping.showAll" />");
		$(".groupmenu").hide();
		$('div.definition').not('.required').each(function(index) {
			// always show all mapped and required fields
			if ($(".fidx", this).val()=="" && $(".fval", this).val()==""){
				$(this).hide();
			};
		});
		
		if($('#filterComp option:selected').val()=="") {
		  $('#filterSection').hide();
		}
	}	
	
	initHelp();
	showHideIdSuffix();
	showHideFilter();
	showHideFilterName();
	var showAll=${Parameters.showAll!"true"};
	if (!showAll){
		hideFields();
	};
	
	$('.confirm').jConfirmAction({question : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});
	
	// show only required and mapped fields
	$("#toggleFields").click(function() {
		if(showAll){
			hideFields();
		}else{
			showAll=true;
			$("#showAllValue").val("true");
			$("#toggleFields").text("<@s.text name="manage.mapping.hideEmpty"/>");
			$('div.definition').show();
			$(".groupmenu").show();
		}
		showHideFilter();
	});
	$("#idColumn").change(function() {
		showHideIdSuffix();
	});
	
	$("#filterComp").change(function() {
		showHideFilter();
	});
	
	$("#filterName").change(function() {
		showHideFilterName();
	});
	
	//Hack needed for Internet Explorer X.*x
	$('.add').each(function() {
		$(this).click(function() {
			window.location = $(this).parent('a').attr('href');
		});
	});	

	$(function() {
    if ( $.browser.msie) {
      $('div.definition').css({overflow: "visible"});
    } 
  });
	
});   
</script>
<style>
	div.definition div.title{
		width: 26%;
	}
	div.definition div.body{
		width: 72%;
	}
	div.body select{
		width: 200px;
	}
	div.body input{
		width: 375px;
	}
	div.required div.title{
		color:#bc5e5b;
		font-weight: normal;
	}
	div.infos img.vocabImg {
		top: 2px !important;
		position: relative;
	}
	div.buttons{
		margin-top: 1em !important;
		margin-bottom: 2em !important;
	}
	#filter{
		color: #0080FF;
	}	
</style>
 <#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1>${mapping.extension.title}</h1>
<p>${mapping.extension.description}</p>
<#if mapping.extension.link?has_content>
<p><@s.text name="basic.link"/>: <a href="${mapping.extension.link}">${mapping.extension.link}</a></p>
</#if>

<#include "/WEB-INF/pages/macros/forms.ftl"/>
<form class="topForm" action="mapping.do" method="post">
  	<input type="hidden" name="r" value="${resource.shortname}" />
  	<input type="hidden" name="id" value="${mapping.extension.rowType}" />
  	<input type="hidden" name="mid" value="${mid!}" />
  	<input id="showAllValue" type="hidden" name="showAll" value="${Parameters.showAll!"true"}" />
<a href="resource.do?r=${resource.shortname}"><@s.text name='manage.mapping.backToOverview'/></a>
<h1><@s.text name='manage.mapping.title'/> <span class="small">${mapping.source.name}</span></h1>
<p><@s.text name='manage.mapping.intro'><@s.param name="source">${mapping.source.name}</@s.param></@s.text></p>

<p><a id="toggleFields" href="#"><@s.text name='manage.mapping.hideEmpty'/></a></p>

<div class="definition required">	
  <div class="title">
  	<div class="head">
		${coreid.name!"Record ID"}			
  	</div>
  </div>
  <div class="body">
  <div>
  	<div class="infos">
  	  <#if coreid??>
  		<img class="infoImg" src="${baseURL}/images/info.gif" />
		<div class="info">		
			<#if coreid.description?has_content>${coreid.description}<br/><br/></#if>              	
			<#if coreid.link?has_content><@s.text name="basic.seealso"/> <a href="${coreid.link}">${coreid.link}</a><br/><br/></#if>
			<span class="idSuffix">
				<@s.text name='manage.mapping.info.linenumbers'/>            	
			</span>              	
			<#if coreid.examples?has_content>
			<em><@s.text name="basic.examples"/></em>: ${coreid.examples}
			</#if>
		</div>		
      </#if>  
		<select name="mapping.idColumn" id="idColumn">		
		<#if mapping.isCore()>
		  <option value="" <#if !mapping.idColumn??> selected="selected"</#if>><@s.text name="manage.mapping.noid"/></option>
		  <option value="-2" <#if (mapping.idColumn!-99)==-2> selected="selected"</#if>><@s.text name="manage.mapping.uuid"/></option>
		</#if>
		  <option value="-1" <#if (mapping.idColumn!-99)==-1> selected="selected"</#if>><@s.text name="manage.mapping.lineNumber"/></option>
		<#list columns as col>
		  <option value="${col_index}" <#if (mappingCoreid.index!-1)==col_index> selected="selected"</#if>>${col}</option>		  		  
		</#list>
		</select>
		<input type="text" name="mapping.idSuffix" style="width:200px" value="${mapping.idSuffix!}" class="idSuffix" />
    </div>
    <div>
	    <@s.text name='manage.mapping.idColumn' />
    </div>
  	<#if ((mapping.idColumn!-99)>=0)>
  	<div>
  		<em><@s.text name='manage.mapping.sourceSample' /></em>:	      		
  		<#assign first=true/>
  		<#list peek as row><#if row??><#if row[mapping.idColumn]?has_content><#if !first> | </#if><#assign first=false/>${row[mapping.idColumn]}</#if></#if></#list>
  	</div>
  	</#if>
  </div>
  </div>
</div>
	

<div id="filterSection" class="definition">	
  <div class="title">
  	<div class="head" id="filter">
		<!-- Filter -->
		<@select name="mapping.filter.filterTime" i18nkey="manage.mapping.filter" options=mapping.filter.filterTimes value="${mapping.filter.filterTime!}" />	
  		
  	</div>  	
  </div>
  <div class="body">
  	<div class="infos">
  		<img class="infoImg" src="${baseURL}/images/info.gif" />
		<div class="info">		
			<@s.text name='manage.mapping.info'/>
		</div>	
		<select id="filterName" name="mapping.filter.column">
		  <option value="" <#if !mapping.filter.column??> selected="selected"</#if>></option>
		<#list columns as c>
		  <option value="${c_index}" <#if c_index==mapping.filter.column!-999> selected="selected"</#if>>${c}</option>
		</#list>
		</select>
		
		<select id="filterComp" name="mapping.filter.comparator">
		  <option value="" <#if !mapping.filter.comparator??> selected="selected"</#if>></option>
		<#list comparators as c>
		  <option value="${c}" <#if c==mapping.filter.comparator!""> selected="selected"</#if>>${c}</option>
		</#list>
		</select>
		<input id="filterParam" name="mapping.filter.param" style="width:190px;" value="${mapping.filter.param!}" />
    </div>
    <div>
    	<@s.text name='manage.mapping.filter.text' />
    </div>
  </div>
</div>

  <div class="buttons">
 	<@s.submit cssClass="button" name="save" key="button.save"/>
 	<@s.submit cssClass="confirm" name="delete" key="button.delete"/>
 	<@s.submit cssClass="button" name="cancel" key="button.cancel"/>
  </div>


	<#assign group=""/>
	<#assign groupMenu>
	 <ul class="horizontal">
	 <#list mapping.extension.properties as p>
	 <#if (p.group!"")!="" && (p.group!"")!=group>
		<#assign group=p.group/>
		<li class="horizontal"><a href="#${p.group?url}">${p.group}</a></li>
	 </#if>
	 </#list>
	 </ul>
	</#assign>

	<#assign group=""/>
	<#--list mapping.extension.properties as p-->
	<#list fields as field>
	<#assign p=field.term/>
	
	<#if p.group?exists && p.group!=group>
	  <div class="groupmenu">
		  <#if group!="">
		  <div class="buttons">
		 	<@s.submit cssClass="button" name="save" key="button.save"/>
		 	<@s.submit cssClass="button" name="cancel" key="button.cancel"/>
		  </div>
		  </#if>
		  <#noescape>${groupMenu}</#noescape>
		  <#assign group=p.group/>
		  <a name="${p.group?url}"></a>
		  <h2>${p.group}</h2>
	  </div>
	</#if>
	<div class="definition<#if p.required> required</#if>">	
	  <div class="title">
	  	<div class="head">
			${p.name}			
	  	</div>
	  </div>
	  <div class="body">
	  <div>
	  	<div class="infos">
	  		<img class="infoImg" src="${baseURL}/images/info.gif" />
			<div class="info">
				<#if p.description?has_content>${p.description}<br/><br/></#if>              	
				<#if p.link?has_content><@s.text name="basic.seealso"/> <a href="${p.link}">${p.link}</a><br/><br/></#if>
				<#if p.examples?has_content>
				<em><@s.text name="basic.examples"/></em>: ${p.examples}
				</#if>              	
			</div>
	      	<#if p.vocabulary?exists>	  		
	      	<a href="vocabulary.do?id=${p.vocabulary.uri}" target="_blank"><img class="vocabImg" src="${baseURL}/images/vocabulary.png" /></a>
	      	</#if>
				<select id="fIdx${field_index}" class="fidx" name="fields[${field_index}].index">
				  <option value="" <#if !field.index??> selected="selected"</#if>></option>
				<#list columns as col>
				  <option value="${col_index}" <#if (field.index!-1)==col_index> selected="selected"</#if>>${col}</option>
				</#list>
				</select>
		      	<#if p.vocabulary?exists>
		      		<#assign vocab=vocabTerms[p.vocabulary.uri] />
					<select id="fVal${field_index}" class="fval" name="fields[${field_index}].defaultValue">
					  <option value="" <#if !field.defaultValue??> selected="selected"</#if>></option>
					<#list vocab?keys as code>
					  <option value="${code}" <#if (field.defaultValue!"")==code> selected="selected"</#if>>${vocab.get(code)}</option>
					</#list>
					</select>
		      	<#else>
  					<input id="fVal${field_index}" class="fval" name="fields[${field_index}].defaultValue" value="${field.defaultValue!}"/>  
		      	</#if>		
	      	</div>
	      	<#if field.index?exists>
	      	<div>
	      		<em><@s.text name='manage.mapping.sourceSample' /></em>:	      		
	      		<#assign first=true/>
	      		<#list peek as row><#if row??><#if row[field.index]?has_content><#if !first> | </#if><#assign first=false/>${row[field.index]}</#if></#if></#list>
	      	</div>
	      	<div>
	      		<em><@s.text name='manage.mapping.translation' /></em>:
	      		<a href="translation.do?r=${resource.shortname}&rowtype=${p.extension.rowType}&mid=${mid}&term=${p.qualname}">
	      		<#if (((field.translation?size)!0)>0)>
	      		${(field.translation?size)!0} terms
	      		<#else>
	      		<button type="button" class="add" onclick="window.location.href"><@s.text name="button.add"/></button>
	      		</#if>
	      		</a>
	      	</div>
	      	</#if>
	      	</div>
	  </div>
	</div>

	<#if !field_has_next>
	  <#if (nonMappedColumns.size()>0)>
		<br/><b><@s.text name="manage.mapping.no.mapped.title"/></b><br/><br/>
		<@s.text name="manage.mapping.no.mapped.columns"/>:<br/>
		<em>
		<ul>
			<#list nonMappedColumns as col>
				<li>${col}</li>
			</#list>
		</ul>
		</em>
	  </#if>
	
	  <div class="buttons">
	 	<@s.submit cssClass="button" name="save" key="button.save"/>
	 	<@s.submit cssClass="button" name="cancel" key="button.cancel"/>
	  </div>
	</#if>

	</#list>

</form>
<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>