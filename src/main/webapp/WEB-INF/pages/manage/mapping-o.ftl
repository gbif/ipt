<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.MappingAction" -->
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
  function activateDeactivateAllStaticInputs() {
    $('.fidx').each(function() {
      activateDeactivateStaticInput($(this));
    });
  }
  function activateDeactivateStaticInput(target) {
    var index = target.attr('id').substring(4);
    var input = $("#fVal"+index);
    var checkbox = $("#cVal"+index);
    if (!target.val().trim()) {
      input.prop('disabled', false);
      checkbox.attr('disabled', false);
    } else {
      // deactivate input
      input.val('');
      input.prop('disabled', true);
      // deactivate checkbox
      checkbox.attr('checked', false);
      checkbox.attr('disabled', true);

    }
  }
	function hideFields() {
		showAll=false;
		$("#showAllValue").val("false");
		$("#toggleFields").text("<@s.text name="manage.mapping.showAll" />");
		$('div.mappingRow').not('.required').each(function(index) {
			// always show all mapped and required fields
			if ($(".fidx", this).val()=="" && $(".fval", this).val()=="" && $("#doiUsedForDatasetId", this).is(":checked")==false){
				$(this).hide();
			};
		});
		
		if($('#filterComp option:selected').val()=="") {
		  $('#filterSection').hide();
		}
	}
  function hideRedundantGroups() {
    showAllGroups=false;
    $("#showAllGroupsValue").val("false");
    $("#toggleGroups").text("<@s.text name="manage.mapping.showAllGroups" />");
    $('div.redundant').each(function(index) {
      $(this).hide();
    });
    // hide sidebar links too
    $('li.redundant').each(function(index) {
      $(this).hide();
    });
  }

  initHelp();
  mirrorCoreIdElementMapping();
	showHideIdSuffix();
	showHideFilter();
	showHideFilterName();
  activateDeactivateAllStaticInputs();
	var showAll=${Parameters.showAll!"true"};
	if (!showAll){
		hideFields();
	};
  var showAllGroups=${Parameters.showAllGroups!"false"};
  if (!showAllGroups){
    hideRedundantGroups();
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
			$('div.mappingRow').show();
			$(".groupmenu").show();
		}
		showHideFilter();
	});

  // show only required and mapped fields
  $("#toggleGroups").click(function() {
    if(showAllGroups) {
      hideRedundantGroups();
    } else {
      showAllGroups=true;
      $("#showAllGroupsValue").val("true");
      $("#toggleGroups").text("<@s.text name="manage.mapping.hideGroups"/>");
      // show sidebar links too
      $('li.redundant').each(function(index) {
        $(this).show();
      });
      // show redundant sections
      $('div.redundant').each(function(index) {
        $(this).show();
      });
    }
  });

  /**
   * Ensures coreId term mapping (e.g. eventID) always mirrors id mapping at top of page. Otherwise it
   * would be possible for the user to specify two different mappings for the term.
   * This method also makes the term's select unselectable, disables its constant value input and hides its source
   * examples and translation button.
   */
  function mirrorCoreIdElementMapping() {
    var index = $("#coreIdTermFieldsIndex").val();
    if (index != null && index != '') {
     // value of coreId element mapping
     var coreIdElementValueSelected = $("#idColumn").val();
     // ensure value of coreId term mapping mirrors coreId element mapping
     var coreIdTerm = $("#fIdx"+index);
     coreIdTerm.val(coreIdElementValueSelected);

     // make coreId term mapping select unselectable (warning - do not make disabled or won't get submitted)
     coreIdTerm.css({"pointer-events": "none", "cursor": "default"});
     // disable coreId term constant value input
     $("#fVal"+index).attr('disabled', true);
     // hide coreId term mapping source sample
     $("#fSIdx"+index).hide();
     // hide coreId term mapping translation section
     $("#fTIdx"+index).hide();
     }
  }

	$("#idColumn").change(function() {
		showHideIdSuffix();
    mirrorCoreIdElementMapping()
	});
	
	$("#filterComp").change(function() {
		showHideFilter();
	});
	
	$("#filterName").change(function() {
		showHideFilterName();
	});

  $(".fidx").change(function() {
    activateDeactivateStaticInput($(this));
  });

  $(".sidebar-anchor").click(function(e) {
    $("a").removeClass("sidebar-nav-selected");
    $(this).addClass("sidebar-nav-selected");
  });
	
	//Hack needed for Internet Explorer X.*x
	$('.add').each(function() {
		$(this).click(function() {
			window.location = $(this).parent('a').attr('href');
		});
	});
});   
</script>
<style>
</style>
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<#assign redundants = action.getRedundantGroups()/>
<#assign nonMapped = action.getNonMappedColumns()/>

<#macro threeButtons>
  <@s.submit cssClass="button" name="save" key="button.save"/>
  <@s.submit cssClass="confirm" name="delete" key="button.delete"/>
  <@s.submit cssClass="button" name="cancel" key="button.back"/>
</#macro>

<#macro sourceSample index fieldsIndex>
  <div id="fSIdx${fieldsIndex}" class="sample mappingText">
    <@s.text name='manage.mapping.sourceSample' />:
      <em>
        <#list peek as row>
          <#if row??>
            <#if row[index]?has_content && row[index]!=" ">
              ${row[index]}
            <#else>
              &nbsp;
            </#if>
            <#if row_has_next> | </#if>
          </#if>
        </#list>
      </em>
  </div>
</#macro>

<#macro datasetDoiCheckbox idAttr name i18nkey classAttr requiredField value="-99999" errorfield="">
  <div class="checkbox">
      <div><#include "/WEB-INF/pages/macros/form_field_common.ftl"></div>
      <#-- use name if value was not supplied -->
      <#if value == "-99999">
        <#assign value><@s.property value="${name}"/></#assign>
      </#if>
      <@s.checkbox key=name id=idAttr value=value cssClass=classAttr/>
  </div>
</#macro>

<#macro showField field index>
  <#assign p=field.term/>
  <#assign fieldsIndex = action.getFieldsTermIndices().get(p.qualifiedName())/>

  <div class="mappingRow<#if p.required> required</#if> ${["odd", "even"][index%2]}">
      <div>
        <img class="infoImg" src="${baseURL}/images/info.gif" />
        <div class="info">
          <#if p.description?has_content>${p.description}<br/><br/></#if>
          <#if datasetId?? && p.qualifiedName()?lower_case == datasetId.qualname?lower_case><@s.text name='manage.mapping.datasetIdColumn.help'/><br/><br/></#if>
          <#if p.link?has_content><@s.text name="basic.seealso"/> <a href="${p.link}">${p.link}</a><br/><br/></#if>
          <#if p.examples?has_content>
              <em><@s.text name="basic.examples"/></em>: ${p.examples}
          </#if>
        </div>
        <div class="title">
          <#if !p.namespace?starts_with("http://purl.org/dc/")>
          ${p.name}
          <#elseif p.namespace?starts_with("http://purl.org/dc/terms")>
              dcterms:${p.name}
          <#elseif p.namespace?starts_with("http://purl.org/dc/elements/1.1")>
              dc:${p.name}
          </#if>
        </div>

        <div class="body">
            <div>
                <select id="fIdx${fieldsIndex}" class="fidx" name="fields[${fieldsIndex}].index">
                    <option value="" <#if !field.index??> selected="selected"</#if>></option>
                  <#list columns as col>
                      <option value="${col_index}" <#if (field.index!-1)==col_index> selected="selected"</#if>>${col}</option>
                  </#list>
                </select>
              <#if p.vocabulary??>
                <#assign vocab=vocabTerms[p.vocabulary.uriString] />
                  <select id="fVal${fieldsIndex}" class="fval" name="fields[${fieldsIndex}].defaultValue">
                      <option value="" <#if !field.defaultValue??> selected="selected"</#if>></option>
                    <#list vocab?keys as code>
                        <option value="${code}" <#if (field.defaultValue!"")==code> selected="selected"</#if>>${vocab.get(code)}</option>
                    </#list>
                  </select>
                  <a href="vocabulary.do?id=${p.vocabulary.uriString}" target="_blank"><img class="vocabImg" src="${baseURL}/images/vocabulary.png" /></a>
              <#else>
                  <input id="fVal${fieldsIndex}" class="fval" name="fields[${fieldsIndex}].defaultValue" value="${field.defaultValue!}"/>
              </#if>
            </div>
        </div>
        <#if datasetId?? && p.qualifiedName()?lower_case == datasetId.qualname?lower_case>
          <div class="sample mappingText">
            <#-- option to use DOI as datasetID -->
            <@datasetDoiCheckbox idAttr="cVal${fieldsIndex}" name="doiUsedForDatasetId" i18nkey="manage.mapping.datasetIdColumn" classAttr="cval datasetDoiCheckbox" requiredField=false value="${doiUsedForDatasetId?string}" errorfield="" />
          </div>
        </#if>
    <#if field.index??>
      <@sourceSample field.index fieldsIndex/>
      <div id="fTIdx${fieldsIndex}" class="sample mappingText">
        <@s.text name='manage.mapping.translation' />:
          <a href="translation.do?r=${resource.shortname}&rowtype=${p.extension.rowType?url}&mid=${mid}&term=${p.qualname?url}">
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
</#macro>

<#-- return struts param: an HTML anchor to the extension link, or the extension title if no link exists -->
<#macro linkOrNameParam ext>
  <#if ext.link?has_content>
    <@s.param><a href="${ext.link}">${ext.title!}</a></@s.param>
  <#else>
    <@s.param>${ext.title!}</@s.param>
  </#if>
</#macro>

<h1><span class="superscript"><@s.text name='manage.overview.title.label'/></span>
  <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
</h1>

<form id="mappingForm" action="mapping.do" method="post">

  <!-- Sidebar -->
  <div id="sidebar-wrapper">
      <ul class="sidebar-nav">
        <li class="title"><@s.text name='manage.mapping.index'/></li>
        <#assign groups = fieldsByGroup?keys/>
        <#if (groups?size>0)>
          <#list groups as g>
            <li <#if redundants?seq_contains(g)>class="redundant"</#if>><a class="sidebar-anchor" href="#group_${g}">${g}</a></li>
          </#list>
        </#if>
        <#if (nonMapped?size>0)>
          <li><a class="sidebar-anchor" href="#nonmapped"><@s.text name='manage.mapping.no.mapped.title'/></a></li>
        </#if>
        <#if (redundants?size>0)>
            <li><a class="sidebar-anchor" href="#redundant"><@s.text name='manage.mapping.redundant'/></a></li>
        </#if>
          <li class="title"><@s.text name='manage.mapping.filters'/></li>
          <li><a id="toggleFields" href="#"><@s.text name='manage.mapping.hideEmpty'/></a></li>
        <#if (redundants?size>0)>
            <li><a id="toggleGroups" href="#"><@s.text name='manage.mapping.hideGroups'/></a></li>
        </#if>
          <li>
              <div>
                <@threeButtons/>
              </div>
          </li>
      </ul>

  </div>
  <!-- /#sidebar-wrapper -->

<div id="wrapper">
    <!-- Page Content -->
    <div id="page-content-wrapper">
        <div class="container-fluid">

            <h2 class="subTitle">
                <img class="infoImg" src="${baseURL}/images/info.gif" />
                <div class="info autop">
                  <@s.text name='manage.mapping.intro'/>
                </div>
              <@s.text name='manage.mapping.title'/>
            </h2>
            <!-- Is this extension mapped as a core? -->
            <#if action.isCoreMapping()>
              <#assign extensionType><@s.text name='extension.core'/></#assign>
            <#else>
              <#assign extensionType><@s.text name='extension'/></#assign>
            </#if>
            <p>
              <@s.text name='manage.mapping.intro1'><@s.param><a href="source.do?r=${resource.shortname}&id=${mapping.source.name}" title="<@s.text name='manage.overview.source.data'/>">${mapping.source.name}</a></@s.param><@s.param>${extensionType?lower_case}:</@s.param><@linkOrNameParam mapping.extension/></@s.text>
            </p>

                <div>
                    <input type="hidden" name="r" value="${resource.shortname}" />
                    <input type="hidden" name="id" value="${mapping.extension.rowType}" />
                    <input type="hidden" name="mid" value="${mid!}" />
                    <input id="showAllValue" type="hidden" name="showAll" value="${Parameters.showAll!"true"}" />
                    <input id="showAllGroupsValue" type="hidden" name="showAllGroups" value="${Parameters.showAllGroups!"true"}" />
                </div>


                    <div class="mappingRow requiredMapping">
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

                      <div class="title" id="coreID">
                        ${coreid.name!"Record ID"}
                      </div>

                      <div class="body">
                          <select name="mapping.idColumn" id="idColumn">
                              <#if action.isCoreMapping()>
                                  <option value="" <#if !mapping.idColumn??> selected="selected"<#elseif (mapping.idColumn!-99)==-3> selected="selected"</#if>><@s.text name="manage.mapping.noid"/></option>
                              </#if>
                                <!-- auto generating identifiers is only available for the Taxon core -->
                              <#if mapping.isTaxonCore()>
                                  <option value="-2" <#if (mapping.idColumn!-99)==-2> selected="selected"</#if>><@s.text name="manage.mapping.uuid"/></option>
                                  <option value="-1" <#if (mapping.idColumn!-99)==-1> selected="selected"</#if>><@s.text name="manage.mapping.lineNumber"/></option>
                              </#if>
                              <#list columns as col>
                                  <option value="${col_index}" <#if (mapping.idColumn!-99)==col_index> selected="selected"</#if>>${col}</option>
                              </#list>
                            </select>
                            <input type="text" name="mapping.idSuffix" value="${mapping.idSuffix!}" class="idSuffix" />
                        </div>

                      <#if ((mapping.idColumn!-99)>=0)>
                        <@sourceSample mapping.idColumn "idColumn"/>
                      </#if>
                  </div>



                    <div id="filterSection" class="mappingRow mappingFiler">

                            <img class="infoImg" src="${baseURL}/images/info.gif" />
                            <div class="info">
                              <@s.text name='manage.mapping.info'/>
                            </div>

                            <div class="title" id="filter">
                              <@s.text name='manage.mapping.filter'/>
                              <select name="mapping.filter.filterTime" id="mapping.filter.filterTime" size="1">
                                <#list mapping.filter.filterTimes?keys as filterTime>
                                    <option value="${filterTime}" <#if (mapping.filter.filterTime!"")==filterTime> selected="selected"</#if>>${filterTime}</option>
                                </#list>
                              </select>
                            </div>

                            <div class="body">
                                <div>
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
                                    <input id="filterParam" style="width:100px" name="mapping.filter.param" style="width:190px;" value="${mapping.filter.param!}" />
                                </div>
                            </div>
                    </div>

            <#-- Display fields either by group, or as single list of fields-->
            <#if (fieldsByGroup?keys?size>0)>
              <#list fieldsByGroup?keys as g>
                <#assign groupsFields = fieldsByGroup.get(g)/>
                  <#if (groupsFields?size>0)>
                    <div id="group_${g}" <#if redundants?seq_contains(g)>class="redundant"</#if> >
                      <h3 class="twenty_top">${g}</h3>
                      <#list groupsFields as field>
                        <@showField field field_index/>
                      </#list>
                      <div class="twenty_top">
                        <@threeButtons/>
                      </div>
                    </div>
                </#if>
              </#list>
            <#else>
                <h3 class="twenty_top"><@s.text name="manage.mapping.fields"/></h3>
              <#list fields as field>
                <@showField field field_index/>
              </#list>
              <div class="twenty_top">
                <@threeButtons/>
              </div>
            </#if>

          <#-- store coreId term mapping field index, used to mirror coreId element mapping -->
          <#if !action.isCoreMapping() && coreid??>
            <#assign coreIdTermFieldsIndex = action.getFieldsTermIndices().get(coreid.qualname)!/>
            <#if coreIdTermFieldsIndex?has_content>
              <input id="coreIdTermFieldsIndex" type="hidden" value="${coreIdTermFieldsIndex}" />
            </#if>
          </#if>

          <#if (nonMapped?size>0)>
            <div>
              <h3 id="nonmapped" class="twenty_top"><@s.text name="manage.mapping.no.mapped.title"/></h3>
              <p><@s.text name="manage.mapping.no.mapped.columns"/>:</p>
              <ul>
                <#list nonMapped as col>
                  <li>${col}</li>
                </#list>
              </ul>
            </div>
          </#if>

          <#if (action.getRedundantGroups()?size>0)>
            <div>
              <h3 id="redundant" class="twenty_top"><@s.text name="manage.mapping.redundant.classes.title"/></h3>
              <p><@s.text name="manage.mapping.redundant.classes.intro"/>:</p>
              <ul>
                <#list action.getRedundantGroups() as gr>
                    <li>${gr}</li>
                </#list>
              </ul>
          </div>
        </#if>
      </div>
    </div>
    <!-- /#page-content-wrapper -->
</div>
<!-- /#wrapper -->
</form>

  <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>