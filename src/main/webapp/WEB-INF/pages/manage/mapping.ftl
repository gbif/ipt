<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.MappingAction" -->
<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header-bootstrap.ftl"/>
<title><@s.text name="manage.mapping.title"/></title>
    <script type="text/javascript" src="${baseURL}/js/jconfirmation-bootstrap.jquery.js"></script>
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
            }
            var showAllGroups=${Parameters.showAllGroups!"false"};
            if (!showAllGroups){
                hideRedundantGroups();
            }

            $('.confirm').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});

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
                if (index != null && index !== '') {
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
                console.log("filter name changed!")
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
        #filter {
            color: #0080FF;
        }
        #coreID {
            color: #008959;
        }
        div.requiredMapping {
            background-color: #E6F5EB;
        }
        div.mappingFiler {
            background-color: #F0FAFF;
        }
    </style>

<#assign currentMenu = "manage"/>
<#assign auxTopNavbar = true />
<#assign auxTopNavbarPage = "mapping" />
<#assign redundants = action.getRedundantGroups()/>
<#assign nonMapped = action.getNonMappedColumns()/>
<#include "/WEB-INF/pages/inc/menu-bootstrap.ftl"/>
<#include "/WEB-INF/pages/macros/forms-bootstrap.ftl"/>
<#include "/WEB-INF/pages/macros/popover-bootstrap.ftl"/>

<#macro threeButtons>
    <div class="col-12 m-3">
        <@s.submit cssClass="button btn btn-outline-gbif-primary" name="save" key="button.save"/>
        <@s.submit cssClass="confirm btn btn-outline-danger" name="delete" key="button.delete"/>
        <@s.submit cssClass="button btn btn-outline-secondary" name="cancel" key="button.back"/>
    </div>
</#macro>

<#macro sourceSample index fieldsIndex>
    <div id="fSIdx${fieldsIndex}" class="sample mappingText mx-3" style="overflow-x: auto !important;">
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
    <div class="checkbox form-check">
        <#-- use name if value was not supplied -->
        <#if value == "-99999">
            <#assign value><@s.property value="${name}"/></#assign>
        </#if>
        <@s.checkbox key=name id=idAttr value=value cssClass=classAttr/>
        <#include "/WEB-INF/pages/macros/form_checkbox_label-bootstrap.ftl">
        <#include "/WEB-INF/pages/macros/help_icon-bootstrap.ftl">
        <#include "/WEB-INF/pages/macros/form_field_error-bootstrap.ftl">
    </div>
</#macro>

<#macro showField field index>
    <#assign p=field.term/>
    <#assign fieldsIndex = action.getFieldsTermIndices().get(p.qualifiedName())/>

    <div class="row mx-md-3 mx-1 p-2 pb-3 g-2 mappingRow<#if p.required> text-danger</#if> border-bottom" style="border-color: #dee2e6 !important;">
            <div class="col-md-4 pt-1">
                <#assign fieldPopoverInfo>
                    <#if p.description?has_content>${p.description}<br/><br/></#if>
                    <#if datasetId?? && p.qualifiedName()?lower_case == datasetId.qualname?lower_case><@s.text name='manage.mapping.datasetIdColumn.help'/><br/><br/></#if>
                    <#if p.link?has_content><@s.text name="basic.seealso"/> <a href="${p.link}">${p.link}</a><br/><br/></#if>
                    <#if p.examples?has_content>
                        <em><@s.text name="basic.examples"/></em>: ${p.examples}
                    </#if>
                </#assign>
                <@popoverTextInfo fieldPopoverInfo />

                <strong class="<#if p.required> text-danger<#else>text-muted</#if>">
                    <#if !p.namespace?starts_with("http://purl.org/dc/")>
                        ${p.name}
                    <#elseif p.namespace?starts_with("http://purl.org/dc/terms")>
                        dcterms:${p.name}
                    <#elseif p.namespace?starts_with("http://purl.org/dc/elements/1.1")>
                        dc:${p.name}
                    </#if>
                </strong>
            </div>

            <div class="col-md-4">
                <select id="fIdx${fieldsIndex}" class="fidx form-select" name="fields[${fieldsIndex}].index">
                    <option value="" <#if !field.index??> selected="selected"</#if>></option>
                    <#list columns as col>
                        <option value="${col_index}" <#if (field.index!-1)==col_index> selected="selected"</#if>>${col}</option>
                    </#list>
                </select>
            </div>

            <div class="col-md-4">
                <#if p.vocabulary??>
                    <#assign vocab=vocabTerms[p.vocabulary.uriString] />

                    <div class="input-group">
                        <label class="input-group-text" for="fVal${fieldsIndex}">
                            <a href="vocabulary.do?id=${p.vocabulary.uriString}" target="_blank">
                                <i class="bi bi-book"></i>
                            </a>
                        </label>
                        <select id="fVal${fieldsIndex}" class="fval form-select" name="fields[${fieldsIndex}].defaultValue">
                            <option value="" <#if !field.defaultValue??> selected="selected"</#if>></option>
                            <#list vocab?keys as code>
                                <option value="${code}" <#if (field.defaultValue!"")==code> selected="selected"</#if>>${vocab.get(code)}</option>
                            </#list>
                        </select>
                    </div>
                <#else>
                    <input id="fVal${fieldsIndex}" class="fval form-control" name="fields[${fieldsIndex}].defaultValue" value="${field.defaultValue!}"/>
                </#if>
            </div>

            <#if field.index??>
                <small class="text-truncate"><@sourceSample field.index fieldsIndex/></small>
                <div id="fTIdx${fieldsIndex}" class="sample mappingText">
                    <small class="mx-3"><@s.text name='manage.mapping.translation' />:</small>
                    <a href="translation.do?r=${resource.shortname}&rowtype=${p.extension.rowType?url}&mid=${mid}&term=${p.qualname?url}">
                        <#if (((field.translation?size)!0)>0)>
                            ${(field.translation?size)!0} terms
                        <#else>
                            <button type="button" class="add btn btn-sm btn-outline-gbif-primary" onclick="window.location.href"><@s.text name="button.add"/></button>
                        </#if>
                    </a>
                </div>
            </#if>

            <#if datasetId?? && p.qualifiedName()?lower_case == datasetId.qualname?lower_case>
                <div class="sample mappingText">
                    <#-- option to use DOI as datasetID -->
                    <@datasetDoiCheckbox idAttr="cVal${fieldsIndex}" name="doiUsedForDatasetId" i18nkey="manage.mapping.datasetIdColumn" classAttr="cval datasetDoiCheckbox form-check-input" requiredField=false value="${doiUsedForDatasetId?string}" errorfield="" />
                </div>
            </#if>
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

<main class="container">

        <div class="my-3 p-3 bg-body rounded shadow-sm">

            <#include "/WEB-INF/pages/inc/action_alerts-bootstrap.ftl">

            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-primary text-center">
                <@popoverPropertyInfo "manage.mapping.intro"/>
                <@s.text name='manage.mapping.title'/>:
                <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
            </h5>

            <#if action.isCoreMapping()>
                <#assign extensionType><@s.text name='extension.core'/></#assign>
            <#else>
                <#assign extensionType><@s.text name='extension'/></#assign>
            </#if>

            <p class="text-muted mx-md-4 mx-2">
                <@s.text name='manage.mapping.intro1'><@s.param><a href="source.do?r=${resource.shortname}&id=${mapping.source.name}" title="<@s.text name='manage.overview.source.data'/>">${mapping.source.name}</a></@s.param><@s.param>${extensionType?lower_case}:</@s.param><@linkOrNameParam mapping.extension/></@s.text>
            </p>

            <div>
                <input type="hidden" name="r" value="${resource.shortname}" />
                <input type="hidden" name="id" value="${mapping.extension.rowType}" />
                <input type="hidden" name="mid" value="${mid!}" />
                <input id="showAllValue" type="hidden" name="showAll" value="${Parameters.showAll!"true"}" />
                <input id="showAllGroupsValue" type="hidden" name="showAllGroups" value="${Parameters.showAllGroups!"true"}" />
            </div>

            <div class="row mx-md-3 mx-1 p-2 pb-3 g-2 requiredMapping">
                <div class="col-md-4 pt-1" id="coreID">
                        <#if coreid??>
                            <#assign text1>
                                <#if coreid.description?has_content>${coreid.description}</#if>
                                <#if coreid.link?has_content><@s.text name="basic.seealso"/> <a href="${coreid.link}">${coreid.link}</a></#if>
                                <span class="idSuffix">
                                    <@s.text name='manage.mapping.info.linenumbers'/>
                                </span>
                                <#if coreid.examples?has_content>
                                    <em><@s.text name="basic.examples"/></em>: ${coreid.examples}
                                </#if>
                            </#assign>
                            <@popoverTextInfo text1/>
                        </#if>
                        <strong>${coreid.name!"Record ID"}</strong>
                </div>

                <div class="col-md-4">
                    <select name="mapping.idColumn" id="idColumn" class="form-select">
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
                </div>

                <div class="col-md-4">
                    <input type="text" name="mapping.idSuffix" value="${mapping.idSuffix!}" class="form-control" />
                </div>

                <#if ((mapping.idColumn!-99)>=0)>
                    <small><@sourceSample mapping.idColumn "idColumn"/></small>
                </#if>
            </div>

            <div id="filterSection" class="mappingRow">

                <div class="row mx-md-3 mx-1 p-2 pb-3 g-2 mappingFiler">
                    <div class="col-lg-1 pt-1" id="filter">
                        <@popoverPropertyInfo "manage.mapping.info" />
                        <strong><@s.text name='manage.mapping.filter'/></strong>
                    </div>

                    <div class="col-lg-3">
                        <select name="mapping.filter.filterTime" id="mapping.filter.filterTime" class="form-select">
                            <#list mapping.filter.filterTimes?keys as filterTime>
                                <option value="${filterTime}" <#if (mapping.filter.filterTime!"")==filterTime> selected="selected"</#if>>${filterTime}</option>
                            </#list>
                        </select>
                    </div>

                    <div class="col-lg-4">
                        <select id="filterName" name="mapping.filter.column" class="form-select">
                            <option value="" <#if !mapping.filter.column??> selected="selected"</#if>></option>
                            <#list columns as c>
                                <option value="${c_index}" <#if c_index==mapping.filter.column!-999> selected="selected"</#if>>${c}</option>
                            </#list>
                        </select>
                    </div>

                    <div class="col-lg-2">
                        <select id="filterComp" name="mapping.filter.comparator" class="form-select">
                            <option value="" <#if !mapping.filter.comparator??> selected="selected"</#if>></option>
                            <#list comparators as c>
                                <option value="${c}" <#if c==mapping.filter.comparator!""> selected="selected"</#if>>${c}</option>
                            </#list>
                        </select>
                    </div>

                    <div class="col-lg-2">
                        <input id="filterParam" name="mapping.filter.param" class="form-control" value="${mapping.filter.param!}" />
                    </div>
                </div>

            </div>
        </div>

        <#-- Display fields either by group, or as single list of fields-->
        <#if (fieldsByGroup?keys?size>0)>
            <#list fieldsByGroup?keys as g>
                <#assign groupsFields = fieldsByGroup.get(g)/>
                <#if (groupsFields?size>0)>
                    <div class="my-3 p-3 bg-body rounded shadow-sm">
                        <div id="group_${g}" <#if redundants?seq_contains(g)>class="redundant"</#if> >
                            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-primary">${g}</h5>
                            <#list groupsFields as field>
                                <@showField field field_index/>
                            </#list>
                            <div>
                                <@threeButtons/>
                            </div>
                        </div>
                    </div>
                </#if>
            </#list>
        <#else>
            <div class="my-3 p-3 bg-body rounded shadow-sm">
                <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-primary">
                    <@s.text name="manage.mapping.fields"/>
                </h5>
                <#list fields as field>
                    <@showField field field_index/>
                </#list>
                <div>
                    <@threeButtons/>
                </div>
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
            <div class="my-3 p-3 bg-body rounded shadow-sm">
                <h5 id="nonmapped" class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-primary">
                    <@s.text name="manage.mapping.no.mapped.title"/>
                </h5>
                <p class="text-muted mx-md-4 mx-2"><@s.text name="manage.mapping.no.mapped.columns"/>:</p>
                <ul class="text-muted mx-md-4 mx-2">
                    <#list nonMapped as col>
                        <li>${col}</li>
                    </#list>
                </ul>

            </div>
        </#if>

        <#if (action.getRedundantGroups()?size>0)>
            <div class="my-3 p-3 bg-body rounded shadow-sm">
                <h5 id="redundant" class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-primary">
                    <@s.text name="manage.mapping.redundant.classes.title"/>
                </h5>
                <p class="text-muted mx-md-4 mx-2"><@s.text name="manage.mapping.redundant.classes.intro"/>:</p>
                <ul class="text-muted mx-md-4 mx-2">
                    <#list action.getRedundantGroups() as gr>
                        <li>${gr}</li>
                    </#list>
                </ul>
            </div>
        </#if>

</main>
</form>

<#include "/WEB-INF/pages/inc/footer-bootstrap.ftl"/>
</#escape>
