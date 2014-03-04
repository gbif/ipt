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
</style>
 <#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<h1><span class="superscript"><@s.text name='manage.overview.title.label'/></span>
    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
</h1>
<form class="topForm" action="mapping.do" method="post">
    <div class="grid_17 suffix_7">
        <h3 class="subTitle"><@s.text name='manage.mapping.title'/>: <a href="source.do?r=${resource.shortname}&id=${mapping.source.name}" title="<@s.text name='manage.overview.source.data'/>">${mapping.source.name}</a></h3>
        <p><@s.text name='manage.mapping.intro'><@s.param name="source"><em>${mapping.source.name}</em></@s.param></@s.text></p>

        <p><a id="toggleFields" href="#"><@s.text name='manage.mapping.hideEmpty'/></a></p>
    </div>
    <div class="grid_17 suffix_7">
        <h3 class="subTitle">${mapping.extension.title}</h3>
        <p>${mapping.extension.description}</p>
        <#if mapping.extension.link?has_content>
        <p><@s.text name="basic.link"/>: <a href="${mapping.extension.link}">${mapping.extension.link}</a></p>
        </#if>
      	<input type="hidden" name="r" value="${resource.shortname}" />
      	<input type="hidden" name="id" value="${mapping.extension.rowType}" />
      	<input type="hidden" name="mid" value="${mid!}" />
      	<input id="showAllValue" type="hidden" name="showAll" value="${Parameters.showAll!"true"}" />
    </div>

<div class="conceptItem">
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
            		<#if coreid.description?has_content>${coreid.description}</#if>
            		<#if coreid.link?has_content><@s.text name="basic.seealso"/> <a href="${coreid.link}">${coreid.link}</a></#if>
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
            	  <option value="" <#if !mapping.idColumn??> selected="selected"<#elseif (mapping.idColumn!-99)==-3> selected="selected"</#if>><@s.text name="manage.mapping.noid"/></option>
            	</#if>
                <!-- auto generating identifiers is only available for the Taxon core -->
              <#if mapping.isTaxonCore()>
                <option value="-2" <#if (mapping.idColumn!-99)==-2> selected="selected"</#if>><@s.text name="manage.mapping.uuid"/></option>
                <option value="-1" <#if (mapping.idColumn!-99)==-1> selected="selected"</#if>><@s.text name="manage.mapping.lineNumber"/></option>
              </#if>
            	<#list columns as col>
            	  <option value="${col_index}" <#if (mappingCoreid.index!-1)==col_index> selected="selected"</#if>>${col}</option>		  		  
            	</#list>
            	</select>
                <input type="text" name="mapping.idSuffix" style="width:200px" value="${mapping.idSuffix!}" class="idSuffix" />
            </div>
            <div>
                <p><@s.text name='manage.mapping.idColumn' /></p>
            </div>
        	<#if ((mapping.idColumn!-99)>=0)>
        	<div>
        	    <p>
        		<em><@s.text name='manage.mapping.sourceSample' /></em>:	      		
        		<#assign first=true/>
        		<#list peek as row><#if row??><#if row[mapping.idColumn]?has_content><#if !first> | </#if><#assign first=false/>${row[mapping.idColumn]}</#if></#if></#list>
        		</p>
        	</div>
        	</#if>
        </div>
        </div>
</div>
    <div class="clearfix"></div>
</div>
	
<div class="conceptItem">
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
        	<p><@s.text name='manage.mapping.filter.text' /></p>
        </div>
  </div>
</div>
    <div class="clearfix"></div>
</div>

  <div class="buttons">
 	<@s.submit cssClass="button" name="save" key="button.save"/>
 	<@s.submit cssClass="confirm" name="delete" key="button.delete"/>
 	<@s.submit cssClass="button" name="cancel" key="button.back"/>
  </div>
  <p></p>
<hr />

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
		 	<@s.submit cssClass="button" name="cancel" key="button.back"/>
		  </div>
		  </#if>
		  <#noescape>${groupMenu}</#noescape>
		  <#assign group=p.group/>
		  <a name="${p.group?url}"></a>
      <h3 class="groupTitle">${p.group}</h3>
    </div>
	</#if>

<div class="conceptItem">
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
	      	<a href="vocabulary.do?id=${p.vocabulary.uriString}" target="_blank"><img class="vocabImg" src="${baseURL}/images/vocabulary.png" /></a>
	      	</#if>
				<select id="fIdx${field_index}" class="fidx" name="fields[${field_index}].index">
				  <option value="" <#if !field.index??> selected="selected"</#if>></option>
				<#list columns as col>
				  <option value="${col_index}" <#if (field.index!-1)==col_index> selected="selected"</#if>>${col}</option>
				</#list>
				</select>
		      	<#if p.vocabulary?exists>
		      		<#assign vocab=vocabTerms[p.vocabulary.uriString] />
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
	<div class="clearfix"></div>
</div>
	<#if !field_has_next>
    <div id="unmapped-columns" class="grid_23">
	  <#if (nonMappedColumns.size()>0)>
		<h3 class="subTitle"><@s.text name="manage.mapping.no.mapped.title"/></h3>
		<p><@s.text name="manage.mapping.no.mapped.columns"/>:</p>
		<ul>
			<#list nonMappedColumns as col>
				<li>${col}</li>
			</#list>
		</ul>
	  </#if>
	</div>
    <div>
    <div class="buttons">
	 	<@s.submit cssClass="button" name="save" key="button.save"/>
	 	<@s.submit cssClass="button" name="cancel" key="button.back"/>
	</div>
	</div>
	</#if>
	</#list>

</form>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>