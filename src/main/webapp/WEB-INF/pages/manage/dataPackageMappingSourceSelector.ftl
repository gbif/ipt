<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.DataPackageMappingAction" -->
<#-- "Map tables": the one way into new data package mappings. Asks how to map: one table at a time (source + table
     pairs, dataPackageMappingSourceCreate.do), or, for DwC-DP, split a flat source into tables (dataPackageTables.do).
     ?from=<source> preselects a source, ?how=SPLIT opens the second card -->
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="manage.mapping.new.title"/>: ${resource.title!resource.shortname}</title>
<#assign currentMenu = "manage"/>
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/mapping.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2-ipt.css">
<script src="${baseURL}/js/select2/select2-4.0.13.full.min.js"></script>
<#include "/WEB-INF/pages/inc/menu.ftl">

<#assign isDwcDp = dataPackageSchema.name == "dwc-dp"/>
<#assign from = (Parameters.from)!""/>
<#assign how = (isDwcDp && ((Parameters.how)!"") == "SPLIT")?then("SPLIT", "ONE")/>
<#assign readableSources = resource.sources?filter(x -> x.readable)/>

<#macro sourceOptions selected>
    <#list resource.sources as src>
        <option value="${src.name}"<#if src.name == selected> selected</#if>>${src.name}</option>
    </#list>
</#macro>

<#macro pairRow i selected>
    <div class="mt-pair" data-i="${i}">
        <div>
            <label class="mt-label" for="newSources[${i}]"><@s.text name="manage.mapping.source"/></label>
            <select id="newSources[${i}]" name="newSources[${i}]" class="form-select form-select-sm mt-source">
                <@sourceOptions selected/>
            </select>
        </div>
        <div>
            <label class="mt-label" for="newTableSchemas[${i}]"><@s.text name="manage.mapping.new.table"/></label>
            <select id="newTableSchemas[${i}]" name="newTableSchemas[${i}]" class="form-select form-select-sm mt-table">
                <#list dataPackageSchema.tableSchemas as t>
                    <option value="${t.name}">${t.title!t.name}</option>
                </#list>
            </select>
        </div>
        <button type="button" class="ov-link-btn mt-remove" title="<@s.text name='manage.mapping.delete'/>"<#if i == 0> hidden</#if>><@icon name="close"/></button>
    </div>
</#macro>

<div class="container px-0 ov-alerts">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<div class="container-fluid border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3 ov-head">
            <div class="ov-grow">
                <div class="fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb justify-content-center mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                            <li class="breadcrumb-item"><a href="resource.do?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="manage.mapping.new.title"/></li>
                        </ol>
                    </nav>
                </div>
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-3 fw-normal"><@s.text name="manage.mapping.new.title"/></h1>
                <div class="ov-meta"><span>${resource.title!resource.shortname}</span></div>
            </div>
            <div class="ov-acts">
                <button type="submit" id="mt-continue" form="mt-one" class="btn btn-sm btn-gbif-solid top-button"><@s.text name="manage.mapping.new.continue"/></button>
                <a class="btn btn-sm btn-outline-secondary top-button" href="resource.do?r=${resource.shortname}"><@s.text name="button.back"/></a>
            </div>
        </div>
    </div>
</div>

<div class="container-fluid bg-body">
    <div class="container main-content-container mb-md-4 pt-3">
        <p class="text-smaller mb-2"><@s.text name="manage.mapping.new.how"/></p>
        <div class="mp-cards mt-hows mb-3">
            <label class="mp-choice<#if how == "ONE"> on</#if>">
                <input type="radio" name="how" value="ONE"<#if how == "ONE"> checked</#if>>
                <span class="t"><@s.text name="manage.mapping.new.ONE"/><span class="tick"></span></span>
                <span class="d"><@s.text name="manage.mapping.new.ONE.desc"/></span>
            </label>
            <#if isDwcDp>
                <label class="mp-choice<#if how == "SPLIT"> on</#if><#if !readableSources?has_content> off</#if>">
                    <input type="radio" name="how" value="SPLIT"<#if how == "SPLIT"> checked</#if><#if !readableSources?has_content> disabled</#if>>
                    <span class="t"><@s.text name="manage.mapping.new.SPLIT"/><span class="tick"></span></span>
                    <span class="d"><@s.text name="manage.mapping.new.SPLIT.desc"/></span>
                </label>
            </#if>
        </div>

        <#-- one table at a time: source + table pairs, as many as wanted -->
        <form id="mt-one" action="dataPackageMappingSourceCreate.do" method="post"<#if how != "ONE"> hidden</#if>>
            <input type="hidden" name="r" value="${resource.shortname}"/>
            <input type="hidden" name="id" value="${dataPackageSchema.identifier}"/>
            <input type="hidden" name="schemaName" value="${dataPackageSchema.name}"/>
            <input type="hidden" name="mid" value="${mid!}"/>
            <input type="hidden" name="showAll" value="${Parameters.showAll!"true"}"/>
            <div id="mt-pairs">
                <@pairRow 0 from/>
            </div>
            <button type="button" id="mt-add" class="ov-link-btn mt-2"><@icon name="add"/> <@s.text name="manage.mapping.add"/></button>
            <p class="text-smaller text-muted mt-3 mb-0"><@s.text name="manage.mapping.new.ONE.after"/></p>
        </form>

        <#-- split a flat source: on to the "Split into tables" page -->
        <#if isDwcDp>
            <form id="mt-split" action="dataPackageTables.do" method="get"<#if how != "SPLIT"> hidden</#if>>
                <input type="hidden" name="r" value="${resource.shortname}"/>
                <div class="mt-pair">
                    <div>
                        <label class="mt-label" for="mt-split-source"><@s.text name="manage.mapping.source"/></label>
                        <select id="mt-split-source" name="source" class="form-select form-select-sm mt-source">
                            <#list readableSources as src>
                                <option value="${src.name}"<#if src.name == from> selected</#if>>${src.name}</option>
                            </#list>
                        </select>
                    </div>
                </div>
                <p class="text-smaller text-muted mt-3 mb-0"><@s.text name="manage.mapping.new.SPLIT.after"/></p>
            </form>
        </#if>
    </div>
</div>

<#-- copied for "Add another" -->
<template id="mt-pair-template"><@pairRow 999 ""/></template>

<style>
    .mt-hows { max-width: 760px; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); }
    .mt-pair { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1.4fr) 24px; gap: 12px; align-items: end; max-width: 760px; margin-bottom: 10px; }
    #mt-split .mt-pair { grid-template-columns: minmax(0, 1fr); max-width: 360px; }
    .mt-label { font-size: 0.8rem; color: #495057; margin-bottom: 3px; }
    .mt-remove { padding-bottom: 6px; color: #6c757d; }
    .mt-t-name { font-weight: 600; }
    .mt-t-desc { font-size: 0.75rem; color: #6c757d; }
    @media (max-width: 600px) { .mt-pair { grid-template-columns: 1fr 24px; } .mt-pair > div:nth-child(2) { grid-column: 1; } }
</style>
<script>
    $(function () {
        'use strict';
        // table descriptions, for the searchable table dropdown
        var tables = {
            <#list dataPackageSchema.tableSchemas as t>
            "${t.name?js_string}": {name: "${t.name?js_string}", description: "${(t.description!"")?js_string}", fields: ${t.fields?size?c}}<#sep>,</#sep>
            </#list>
        };
        <#assign fieldsLabel><@s.text name="manage.mapping.new.fields"/></#assign>
        var fieldsLabel = "${fieldsLabel?js_string}";
        var noResults = {noResults: function () { return '${selectNoResultsFound?js_string}'; }};

        function tableOption(o) {
            if (!o.id || !tables[o.id]) return o.text;
            var t = tables[o.id];
            var $r = $('<div><div class="mt-t-name"></div><div class="mt-t-desc"></div></div>');
            $r.find('.mt-t-name').text(o.text + (o.text !== t.name ? ' (' + t.name + ')' : ''));
            $r.find('.mt-t-desc').text(t.description + ' ' + fieldsLabel + ': ' + t.fields);
            return $r;
        }

        function init($scope) {
            if (!$.fn.select2) return;
            $scope.find('.mt-source').select2({theme: 'bootstrap4', width: '100%', minimumResultsForSearch: 10, language: noResults});
            $scope.find('.mt-table').select2({theme: 'bootstrap4', width: '100%', templateResult: tableOption, language: noResults});
        }
        init($(document));

        // the chosen card shows its form, and Continue submits it
        $('.mt-hows').on('change', 'input[name=how]', function () {
            var how = $('.mt-hows input[name=how]:checked').val();
            $('.mt-hows .mp-choice').each(function () { $(this).toggleClass('on', $(this).find('input').is(':checked')); });
            $('#mt-one').prop('hidden', how !== 'ONE');
            $('#mt-split').prop('hidden', how !== 'SPLIT');
            $('#mt-continue').attr('form', how === 'SPLIT' ? 'mt-split' : 'mt-one');
        });
        $('.mt-hows input[name=how]:checked').trigger('change');

        function renumber() {
            $('#mt-pairs .mt-pair').each(function (i) {
                $(this).attr('data-i', i);
                $(this).find('.mt-source').attr({id: 'newSources[' + i + ']', name: 'newSources[' + i + ']'})
                    .closest('div').find('label').attr('for', 'newSources[' + i + ']');
                $(this).find('.mt-table').attr({id: 'newTableSchemas[' + i + ']', name: 'newTableSchemas[' + i + ']'})
                    .closest('div').find('label').attr('for', 'newTableSchemas[' + i + ']');
                $(this).find('.mt-remove').prop('hidden', i === 0);
            });
        }
        $('#mt-add').on('click', function () {
            var $row = $($('#mt-pair-template').html());
            $('#mt-pairs').append($row);
            renumber();
            init($row);
        });
        $('#mt-pairs').on('click', '.mt-remove', function () {
            $(this).closest('.mt-pair').remove();
            renumber();
        });
    });
</script>

<#include "/WEB-INF/pages/inc/footer.ftl">
