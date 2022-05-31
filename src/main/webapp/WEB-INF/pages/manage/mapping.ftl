<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.MappingAction" -->
<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl"/>
<title><@s.text name="manage.mapping.title"/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <script>
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
                    if ($(".fidx", this).val() === "" && $(".fval", this).val() === "" && $("#doiUsedForDatasetId", this).is(":checked") === false){
                        $(this).hide();
                    }
                });

                if($('#filterComp option:selected').val() === "") {
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

            $('.confirm').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>", buttonType: "danger"});

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
                showHideFilterName();
            });

            $(".fidx").change(function() {
                activateDeactivateStaticInput($(this));
            });

            //Hack needed for Internet Explorer X.*x
            $('.add').each(function() {
                $(this).click(function() {
                    window.location = $(this).parent('a').attr('href');
                });
            });

            // spy scroll and manage sidebar menu
            $(window).scroll(function () {
                var scrollPosition = $(document).scrollTop();

                $('.bd-toc nav a.sidebar-navigation-link').each(function () {
                    var currentLink = $(this);
                    var anchor = $(currentLink.attr("href"));
                    var sectionId = anchor[0].id.replace("anchor-", "");
                    var section = $("#" + sectionId);

                    var sectionsContainer = $("#sections");

                    if (sectionsContainer.position().top - 100 > scrollPosition) {
                        var removeActiveFromThisLink = $('.bd-toc nav a.active');
                        removeActiveFromThisLink.removeClass('active');
                    } else if (section.position().top - 100  <= scrollPosition
                        && section.position().top + section.height() > scrollPosition) {
                        if (!currentLink.hasClass("active")) {
                            var removeFromThisLink = $('.bd-toc nav a.active');
                            removeFromThisLink.removeClass('active');
                            $(this).addClass('active');
                        }
                    }
                });
            })
        });
    </script>

<#assign currentMenu = "manage"/>
<#assign redundants = action.getRedundantGroups()/>
<#assign nonMapped = action.getNonMappedColumns()/>
<#include "/WEB-INF/pages/inc/menu.ftl"/>
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<#include "/WEB-INF/pages/macros/popover.ftl"/>

<#macro threeButtons>
    <div class="col-12 my-3">
        <@s.submit cssClass="button btn btn-outline-gbif-primary" name="save" key="button.save"/>
        <@s.submit cssClass="confirm btn btn-outline-gbif-danger" name="delete" key="button.delete"/>
        <@s.submit cssClass="button btn btn-outline-secondary" name="cancel" key="button.back"/>
    </div>
</#macro>

<#macro sourceSample index fieldsIndex>
    <div id="fSIdx${fieldsIndex}" class="sample mappingText mx-3 overflow-x-auto">
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
        <#include "/WEB-INF/pages/macros/form_checkbox_label.ftl">
        <#include "/WEB-INF/pages/macros/help_icon.ftl">
        <#include "/WEB-INF/pages/macros/form_field_error.ftl">
    </div>
</#macro>

<#macro showField field index>
    <#assign p=field.term/>
    <#assign fieldsIndex = action.getFieldsTermIndices().get(p.qualifiedName())/>

    <div class="row py-1 g-2 mappingRow border-bottom text-smaller">
            <div class="col-lg-4 pt-1">
                <#assign fieldPopoverInfo>
                    <#if p.description?has_content>${p.description}<br/><br/></#if>
                    <#if datasetId?? && p.qualifiedName()?lower_case == datasetId.qualname?lower_case><@s.text name='manage.mapping.datasetIdColumn.help'/><br/><br/></#if>
                    <#if p.link?has_content><@s.text name="basic.seealso"/> <a href="${p.link}" target="_blank">${p.link}</a><br/><br/></#if>
                    <#if p.examples?has_content>
                        <em><@s.text name="basic.examples"/></em>: <code>${p.examples}</code>
                    </#if>
                </#assign>
                <@popoverTextInfo fieldPopoverInfo />

                <strong class="<#if p.required>text-gbif-danger</#if>" >
                    <#if !p.namespace()?starts_with("http://purl.org/dc/")>
                        ${p.name}
                    <#elseif p.namespace()?starts_with("http://purl.org/dc/terms")>
                        dcterms:${p.name}
                    <#elseif p.namespace()?starts_with("http://purl.org/dc/elements/1.1")>
                        dc:${p.name}
                    </#if>
                </strong>
            </div>

            <div class="col-lg-4">
                <select id="fIdx${fieldsIndex}" class="fidx form-select form-select-sm" name="fields[${fieldsIndex}].index">
                    <option value="" <#if !field.index??> selected="selected"</#if>></option>
                    <#list columns as col>
                        <option value="${col_index}" <#if (field.index!-1)==col_index> selected="selected"</#if>>${col}</option>
                    </#list>
                </select>
            </div>

            <div class="col-lg-4">
                <#if p.vocabulary??>
                    <#assign vocab=vocabTerms[p.vocabulary.uriString] />

                    <div class="input-group input-group-sm">
                        <label class="input-group-text" for="fVal${fieldsIndex}">
                            <a href="vocabulary.do?id=${p.vocabulary.uriString}" class="no-text-decoration" target="_blank">
                                <i class="bi bi-book"></i>
                            </a>
                        </label>
                        <select id="fVal${fieldsIndex}" class="fval form-select form-select-sm" name="fields[${fieldsIndex}].defaultValue">
                            <option value="" <#if !field.defaultValue??> selected="selected"</#if>></option>
                            <#list vocab?keys as code>
                                <option value="${code}" <#if (field.defaultValue!"")==code> selected="selected"</#if>>${vocab.get(code)}</option>
                            </#list>
                        </select>
                    </div>
                <#else>
                    <input id="fVal${fieldsIndex}" class="fval form-control form-control-sm" name="fields[${fieldsIndex}].defaultValue" value="${field.defaultValue!}"/>
                </#if>
            </div>

            <#if field.index??>
                <small class="text-truncate"><@sourceSample field.index fieldsIndex/></small>
                <div id="fTIdx${fieldsIndex}" class="sample mappingText">
                    <small class="mx-3"><@s.text name='manage.mapping.translation' />:</small>
                    <small>
                        <a href="translation.do?r=${resource.shortname}&rowtype=${p.extension.rowType?url}&mid=${mid}&term=${p.qualname?url}">
                            <#if (((field.translation?size)!0)>0)>
                                ${(field.translation?size)!0} terms
                            <#else>
                                <button type="button" class="add btn btn-sm btn-outline-gbif-primary" onclick="window.location.href"><@s.text name="button.add"/></button>
                            </#if>
                        </a>
                    </small>
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

<form id="mappingForm" class="needs-validation" action="mapping.do" method="post">
<div class="container-fluid bg-body border-bottom">

    <div class="container my-3">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container p-3">

        <div class="text-center">
            <div class="text-center text-uppercase fw-bold fs-smaller-2">
                <@s.text name="basic.resource"/>
            </div>

            <h5 property="dc:title" class="rtitle pt-2 text-gbif-header fs-2 fw-400 text-center">
                <@popoverPropertyInfo "manage.mapping.intro"/>
                <@s.text name='manage.mapping.title'/>
            </h5>

            <div class="text-center fs-smaller">
                <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
            </div>

            <#if action.isCoreMapping()>
                <#assign extensionType><@s.text name='extension.core'/></#assign>
            <#else>
                <#assign extensionType><@s.text name='extension'/></#assign>
            </#if>

            <div class="mt-2">
                <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save"/>
                <@s.submit cssClass="confirm btn btn-sm btn-outline-gbif-danger top-button" name="delete" key="button.delete"/>
                <@s.submit cssClass="button btn btn-sm btn-outline-secondary top-button" name="cancel" key="button.back"/>
            </div>

            <p class="mt-3 text-smaller fst-italic">
                <@s.text name='manage.mapping.intro1'>
                    <@s.param>
                        <a href="source.do?r=${resource.shortname}&id=${mapping.source.name}" title="<@s.text name='manage.overview.source.data'/>">
                            ${mapping.source.name}
                        </a>
                    </@s.param>
                    <@s.param>${extensionType?lower_case}:</@s.param>
                    <@s.param><a href="${baseURL}/admin/extension.do?id=${mapping.extension.rowType!}" target="_blank">${mapping.extension.title!}</a></@s.param>
                </@s.text>
            </p>
        </div>
    </div>
</div>

<div class="container-fluid bg-body">
    <div class="container bd-layout">

        <main class="bd-main">

            <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                <#assign groups = fieldsByGroup?keys/>
                <nav id="sidebar-content">
                    <ul>
                        <#if (groups?size>0)>
                            <#list groups as g>
                                <li <#if redundants?seq_contains(g)> class="redundant" </#if> >
                                    <a class="sidebar-navigation-link" href="#anchor-group_${g?replace(' ', '_')}">
                                        <#if g?has_content>
                                            ${g}
                                        <#else>
                                            <@s.text name="manage.mapping.noClass"/>
                                        </#if>
                                    </a>
                                </li>
                            </#list>
                        </#if>

                        <#if (nonMapped?size>0)>
                            <li><a class="sidebar-navigation-link" href="#anchor-nonmapped"><@s.text name='manage.mapping.no.mapped.title'/></a></li>
                        </#if>

                        <#if (redundants?size>0)>
                            <li><a class="sidebar-navigation-link" href="#anchor-redundant"><@s.text name='manage.mapping.redundant'/></a></li>
                        </#if>
                    </ul>

                    <ul>
                        <li><a id="toggleFields" class="sidebar-link"><@s.text name='manage.mapping.hideEmpty'/></a></li>
                        <#if (redundants?size>0)>
                            <li><a id="toggleGroups" class="sidebar-link"><@s.text name='manage.mapping.hideGroups'/></a></li>
                        </#if>
                    </ul>

                    <div class="d-flex align-content-between">
                        <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary me-1" name="save" key="button.save"/>
                        <@s.submit cssClass="confirm btn btn-sm btn-outline-gbif-danger me-1" name="delete" key="button.delete"/>
                        <@s.submit cssClass="button btn btn-sm btn-outline-secondary" name="cancel" key="button.back"/>
                    </div>
                </nav>

                <nav id="sidebar-content">
                    <ul class="dropdown-menu dropdown-menu-light text-light" aria-labelledby="filtersDropdown">
                        <li><a id="toggleFields" class="dropdown-item menu-link" href="#"><@s.text name='manage.mapping.hideEmpty'/></a></li>

                        <#if (redundants?size>0)>
                            <li><a id="toggleGroups" class="dropdown-item menu-link" href="#"><@s.text name='manage.mapping.hideGroups'/></a></li>
                        </#if>
                    </ul>
                </nav>

            </div>

            <div class="bd-content ps-lg-4">

                <div>
                    <input type="hidden" name="r" value="${resource.shortname}" />
                    <input type="hidden" name="id" value="${mapping.extension.rowType}" />
                    <input type="hidden" name="mid" value="${mid!}" />
                    <input id="showAllValue" type="hidden" name="showAll" value="${Parameters.showAll!"true"}" />
                    <input id="showAllGroupsValue" type="hidden" name="showAllGroups" value="${Parameters.showAllGroups!"true"}" />
                </div>

                <#-- Filter and required mapping -->
                <div class="border-bottom mb-2 text-smaller">
                    <div class="row pt-3 pb-2 g-2 requiredMapping">
                        <div class="col-lg-4 pt-1" id="coreID">
                            <#if coreid??>
                                <#assign text1>
                                    <#if coreid.description?has_content>${coreid.description}</#if>
                                    <#if coreid.link?has_content><@s.text name="basic.seealso"/> <a href="${coreid.link}" target="_blank">${coreid.link}</a></#if>
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

                        <div class="col-lg-4">
                            <select name="mapping.idColumn" id="idColumn" class="form-select form-select-sm">
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

                        <div class="col-lg-4">
                            <input type="text" name="mapping.idSuffix" value="${mapping.idSuffix!}" class="form-control form-control-sm" />
                        </div>

                        <#if ((mapping.idColumn!-99)>=0)>
                            <small><@sourceSample mapping.idColumn "idColumn"/></small>
                        </#if>
                    </div>

                    <div id="filterSection" class="mappingRow">

                        <div class="row pt-2 pb-3 g-2 mappingFiler">
                            <div class="col-lg-1 pt-1" id="filter">
                                <@popoverPropertyInfo "manage.mapping.info" />
                                <strong><@s.text name='manage.mapping.filter'/></strong>
                            </div>

                            <div class="col-lg-3">
                                <select name="mapping.filter.filterTime" id="mapping.filter.filterTime" class="form-select form-select-sm">
                                    <#list mapping.filter.filterTimes?keys as filterTime>
                                        <option value="${filterTime}" <#if (mapping.filter.filterTime!"")==filterTime> selected="selected"</#if>>${filterTime}</option>
                                    </#list>
                                </select>
                            </div>

                            <div class="col-lg-4">
                                <select id="filterName" name="mapping.filter.column" class="form-select form-select-sm">
                                    <option value="" <#if !mapping.filter.column??> selected="selected"</#if>></option>
                                    <#list columns as c>
                                        <option value="${c_index}" <#if c_index==mapping.filter.column!-999> selected="selected"</#if>>${c}</option>
                                    </#list>
                                </select>
                            </div>

                            <div class="col-lg-2">
                                <select id="filterComp" name="mapping.filter.comparator" class="form-select form-select-sm">
                                    <option value="" <#if !mapping.filter.comparator??> selected="selected"</#if>></option>
                                    <#list comparators as c>
                                        <option value="${c}" <#if c==mapping.filter.comparator!""> selected="selected"</#if>>${c}</option>
                                    </#list>
                                </select>
                            </div>

                            <div class="col-lg-2">
                                <input id="filterParam" name="mapping.filter.param" class="form-control form-control-sm" value="${mapping.filter.param!}" />
                            </div>
                        </div>

                    </div>
                </div>

                <div id="sections">
                    <#-- Display fields either by group, or as single list of fields-->
                    <#if (fieldsByGroup?keys?size>0)>
                        <#list fieldsByGroup?keys as g>
                            <#assign groupsFields = fieldsByGroup.get(g)/>
                            <#if (groupsFields?size>0)>
                                <span class="anchor anchor-base" id="anchor-group_${g?replace(' ', '_')}"></span>
                                <div class="mt-5 <#if redundants?seq_contains(g)>redundant</#if>">
                                    <div id="group_${g?replace(' ', '_')}" <#if redundants?seq_contains(g)>class="redundant"</#if> >
                                        <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fs-5 fw-400">
                                            <#if g?has_content>
                                                ${g}
                                            <#else>
                                                <@s.text name="manage.mapping.noClass"/>
                                            </#if>
                                        </h4>
                                        <#list groupsFields as field>
                                            <@showField field field_index/>
                                        </#list>
                                    </div>
                                </div>
                            </#if>
                        </#list>
                    <#else>
                        <div class="mt-5">
                            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fs-5 fw-400">
                                <@s.text name="manage.mapping.fields"/>
                            </h4>
                            <#list fields as field>
                                <@showField field field_index/>
                            </#list>
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
                        <span class="anchor anchor-base" id="anchor-nonmapped"></span>
                        <div class="mt-5" <#if (action.getRedundantGroups()?size==0)>style="height: 100vh; min-height: 200px;"</#if> >
                            <h4 id="nonmapped" class="pb-2 mb-2 pt-2 text-gbif-header-2 fs-5 fw-400">
                                <@s.text name="manage.mapping.no.mapped.title"/>
                            </h4>
                            <p><@s.text name="manage.mapping.no.mapped.columns"/>:</p>

                            <div class="text-smaller">
                                <#list nonMapped as col>
                                    <code>${col}<#sep>;</#sep></code>
                                </#list>
                            </div>
                        </div>
                    </#if>

                    <#if (redundants?size>0)>
                        <span class="anchor anchor-base" id="anchor-redundant"></span>
                        <div class="mt-5" style="height: 100vh; min-height: 200px;">
                            <h4 id="redundant" class="pb-2 mb-2 pt-2 text-gbif-header-2 fs-5 fw-400">
                                <@s.text name="manage.mapping.redundant.classes.title"/>
                            </h4>
                            <p><@s.text name="manage.mapping.redundant.classes.intro"/>:</p>

                            <div class="text-smaller">
                                <#list redundants as gr>
                                    <code>${gr}<#sep>;</#sep></code>
                                </#list>
                            </div>
                        </div>
                    </#if>
                </div>

            </div>
        </main>
    </div>
</div>

</form>

<#include "/WEB-INF/pages/inc/footer.ftl"/>
</#escape>
