<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.DataPackageMappingAction" -->
<#-- "Map tables": the one way into new data package mappings. Asks how to map: choose the tables yourself (source +
     table pairs, as many as wanted) or match sources to tables (a table suggested per source from its columns), both
     created by dataPackageMappingSourceCreate.do; or, for DwC-DP, split a flat source into tables (dataPackageTables.do).
     ?from=<source> preselects a source and ?how=PICK|MATCH|SPLIT picks the card; otherwise MATCH opens when several
     unmapped sources match a table. Database sources are only read for MATCH: choosing that card reloads with
     ?how=MATCH when there are any -->
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
<#assign readableSources = resource.sources?filter(x -> x.readable)/>
<#assign matches = sourceMatches/>
<#assign notRead = sourcesNotRead/>
<#-- matching lists the unmapped sources a table was found for; the others are only named under the list -->
<#assign toMatch = matches?filter(x -> !x.mappedTo()?has_content && x.match().table()??)/>
<#assign noMatch = matches?filter(x -> !x.mappedTo()?has_content && !x.match().table()?? && !x.flat())/>
<#-- flat DwC-DP files (an occurrenceID, eventID, materialEntityID or catalogNumber, no table's key): split them instead -->
<#assign flatSources = matches?filter(x -> !x.mappedTo()?has_content && !x.match().table()?? && x.flat())/>
<#assign alreadyMapped = matches?filter(x -> x.mappedTo()?has_content)/>
<#assign how = (Parameters.how)!""/>
<#if !(how == "PICK" || how == "MATCH" || (how == "SPLIT" && isDwcDp))>
    <#assign how = (!from?has_content && (toMatch?size > 1))?then("MATCH", "PICK")/>
</#if>

<#macro sourceOptions selected>
    <#list resource.sources as src>
        <option value="${src.name}"<#if src.name == selected> selected</#if>>${src.name}</option>
    </#list>
</#macro>

<#macro pairRow i selected>
    <div class="mt-pair">
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
        <button type="button" class="ov-link-btn mt-remove" title="<@s.text name='manage.mapping.delete'/>" aria-label="<@s.text name='manage.mapping.delete'/>"<#if i == 0> hidden</#if>><@icon name="delete"/></button>
    </div>
</#macro>

<div class="container px-0 ov-alerts">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<div class="container-fluid border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3 ov-head ov-head-left">
            <div class="ov-grow">
                <div class="fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                            <li class="breadcrumb-item ov-crumb-resource"><a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="manage.mapping.new.title"/></li>
                        </ol>
                    </nav>
                </div>
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal"><@s.text name="manage.mapping.new.title"/></h1>
                <#-- every table and field of the schema, with definitions (manage/dataPackage.do), in a new tab -->
                <div class="ov-meta"><a class="mp-terms-link" href="dataPackage.do?id=${dataPackageSchema.identifier?url}" target="_blank" rel="noopener"><@s.text name="manage.mapping.tablesAndFields"/></a></div>
            </div>
            <div class="ov-acts">
                <button type="submit" id="mt-continue" form="mt-pick" class="btn btn-sm btn-gbif-solid top-button"><@s.text name="manage.mapping.new.continue"/></button>
                <a class="btn btn-sm btn-outline-secondary top-button" href="resource.do?r=${resource.shortname}"><@s.text name="button.back"/></a>
            </div>
        </div>
    </div>
</div>

<div class="container-fluid bg-body">
    <div class="container main-content-container mb-md-4 pt-3">
        <p class="text-smaller mb-2"><@s.text name="manage.mapping.new.how"/></p>
        <div class="mp-cards mt-hows mb-3">
            <label class="mp-choice<#if how == "PICK"> on</#if>">
                <input type="radio" name="how" value="PICK"<#if how == "PICK"> checked</#if>>
                <span class="t"><@s.text name="manage.mapping.new.PICK"/><span class="tick"></span></span>
                <span class="d"><@s.text name="manage.mapping.new.PICK.desc"/></span>
            </label>
            <label class="mp-choice<#if how == "MATCH"> on</#if><#if !readableSources?has_content> off</#if>">
                <input type="radio" name="how" value="MATCH"<#if how == "MATCH"> checked</#if><#if !readableSources?has_content> disabled</#if><#if notRead?has_content> data-reload="dataPackageMappingSourceNew.do?r=${resource.shortname}&id=${dataPackageSchema.identifier?url}&how=MATCH"</#if>>
                <span class="t"><@s.text name="manage.mapping.new.MATCH"/><span class="tick"></span></span>
                <span class="d"><@s.text name="manage.mapping.new.MATCH.desc"/></span>
            </label>
            <#if isDwcDp>
                <label class="mp-choice<#if how == "SPLIT"> on</#if><#if !readableSources?has_content> off</#if>">
                    <input type="radio" name="how" value="SPLIT"<#if how == "SPLIT"> checked</#if><#if !readableSources?has_content> disabled</#if>>
                    <span class="t"><@s.text name="manage.mapping.new.SPLIT"/><span class="tick"></span></span>
                    <span class="d"><@s.text name="manage.mapping.new.SPLIT.desc"/></span>
                </label>
            </#if>
        </div>

        <#-- choose the tables yourself: source + table pairs, as many as wanted -->
        <form id="mt-pick" action="dataPackageMappingSourceCreate.do" method="post"<#if how != "PICK"> hidden</#if>>
            <input type="hidden" name="r" value="${resource.shortname}"/>
            <input type="hidden" name="id" value="${dataPackageSchema.identifier}"/>
            <div id="mt-pairs">
                <@pairRow 0 from/>
            </div>
            <button type="button" id="mt-add" class="ov-link-btn mt-2"><@icon name="add"/> <@s.text name="manage.mapping.add"/></button>
            <p class="text-smaller text-muted mt-3 mb-0"><@s.text name="manage.mapping.new.PICK.after"/></p>
        </form>

        <#-- match sources to tables: each unmapped source with the table its columns match best, ticked; only ticked rows
             are sent, numbered on submit. Sources no table matches link to "Choose the tables yourself" -->
        <form id="mt-match" action="dataPackageMappingSourceCreate.do" method="post"<#if how != "MATCH"> hidden</#if>>
            <input type="hidden" name="r" value="${resource.shortname}"/>
            <input type="hidden" name="id" value="${dataPackageSchema.identifier}"/>
            <#if toMatch?has_content>
            <div class="mt-matches">
                <#list toMatch as sm>
                    <#assign mt = sm.match()/>
                    <div class="mt-match" data-columns="${mt.columns()?c}" data-matched="{<#list mt.matched() as t, n>&quot;${t?js_string}&quot;:${n?c}<#sep>,</#sep></#list>}">
                        <input type="checkbox" class="form-check-input mt-use" id="mt-use-${sm?index}" aria-label="${sm.source().name}" checked>
                        <input type="hidden" class="mt-src" value="${sm.source().name}"/>
                        <label class="mt-src-name" for="mt-use-${sm?index}">${sm.source().name}
                            <span class="mt-src-meta"><@s.text name="manage.overview.source.columns"/>: ${mt.columns()?c}</span>
                        </label>
                        <div>
                            <select class="form-select form-select-sm mt-table mt-match-table" aria-label="${sm.source().name}: <@s.text name='manage.mapping.new.table'/>">
                                <option value=""><@s.text name="manage.mapping.new.chooseTable"/></option>
                                <#list dataPackageSchema.tableSchemas as t>
                                    <option value="${t.name}"<#if t.name == (mt.table())!""> selected</#if>>${t.title!t.name}</option>
                                </#list>
                            </select>
                        </div>
                        <span class="mt-count"></span>
                    </div>
                </#list>
            </div>
            <#-- right under the ticked sources it is about, before the sources Continue leaves alone -->
            <p class="text-smaller text-muted mt-2 mb-0"><@s.text name="manage.mapping.new.MATCH.after"/></p>
            <#else>
                <div class="ov-banner info compact" role="status"><@icon name="info" class="ov-ic"/><div class="ov-grow"><@s.text name="manage.mapping.new.nothingToMatch"/></div></div>
            </#if>
            <#if notRead?has_content>
                <p class="mt-rest"><@s.text name="manage.mapping.new.notRead"><@s.param>${notRead?map(x -> x.name)?join(", ")}</@s.param></@s.text>
                    <a href="dataPackageMappingSourceNew.do?r=${resource.shortname}&id=${dataPackageSchema.identifier?url}&how=MATCH"><@s.text name="manage.mapping.new.readNow"/></a></p>
            </#if>
            <#-- the other sources, grouped by why they are not in the list above: one line of explanation per group, then
                 one row per source with what can be done with it -->
            <#if flatSources?has_content || noMatch?has_content || alreadyMapped?has_content>
                <div class="mt-others">
                    <#if flatSources?has_content>
                        <div class="mt-group">
                            <div class="mt-group-h"><@s.text name="manage.mapping.new.flat"/></div>
                            <div class="mt-other-rows"><#list flatSources as sm>
                                <div class="mt-other">
                                    <span class="mt-other-name">${sm.source().name}</span>
                                    <span class="mt-other-act">
                                        <a href="dataPackageTables.do?r=${resource.shortname}&source=${sm.source().name?url}" aria-label="${sm.source().name}: <@s.text name='manage.mapping.new.flat.split'/>"><@s.text name="manage.mapping.new.flat.split"/></a>
                                        <a href="dataPackageMappingSourceNew.do?r=${resource.shortname}&id=${dataPackageSchema.identifier?url}&how=PICK&from=${sm.source().name?url}" aria-label="${sm.source().name}: <@s.text name='manage.mapping.new.flat.pick'/>"><@s.text name="manage.mapping.new.flat.pick"/></a>
                                    </span>
                                </div>
                            </#list></div>
                        </div>
                    </#if>
                    <#if noMatch?has_content>
                        <div class="mt-group">
                            <div class="mt-group-h"><@s.text name="manage.mapping.new.noMatch"/></div>
                            <div class="mt-other-rows"><#list noMatch as sm>
                                <div class="mt-other">
                                    <span class="mt-other-name">${sm.source().name}</span>
                                    <span class="mt-other-act"><a href="dataPackageMappingSourceNew.do?r=${resource.shortname}&id=${dataPackageSchema.identifier?url}&how=PICK&from=${sm.source().name?url}" aria-label="${sm.source().name}: <@s.text name='manage.mapping.new.flat.pick'/>"><@s.text name="manage.mapping.new.flat.pick"/></a></span>
                                </div>
                            </#list></div>
                        </div>
                    </#if>
                    <#if alreadyMapped?has_content>
                        <div class="mt-group">
                            <div class="mt-group-h"><@s.text name="manage.mapping.new.alreadyMapped"/></div>
                            <div class="mt-other-rows"><#list alreadyMapped as sm>
                                <div class="mt-other">
                                    <span class="mt-other-name">${sm.source().name} <span class="mt-other-tables">&rarr; ${sm.mappedTo()?join(", ")}</span></span>
                                </div>
                            </#list></div>
                        </div>
                    </#if>
                </div>
            </#if>
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

<#-- copied for "Add mapping" -->
<template id="mt-pair-template"><@pairRow 999 ""/></template>

<style>
    /* the ways to map side by side, as wide as the page: three for DwC-DP, two for other data packages */
    .mt-hows { grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); }
    .mt-pair { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1.4fr) 24px; gap: 12px; align-items: end; margin-bottom: 10px; }
    #mt-split .mt-pair { grid-template-columns: minmax(0, 1fr) minmax(0, 1.4fr) 24px; }
    .mt-label { font-size: 0.8rem; color: #495057; margin-bottom: 3px; }
    .mt-matches { border: 1px solid #dee2e6; border-radius: 8px; padding: 0 14px; }
    .mt-match { display: grid; grid-template-columns: 22px minmax(140px, 1fr) minmax(0, 1.4fr) minmax(150px, auto); gap: 12px; align-items: center; padding: 10px 0; border-top: 1px solid #f1f3f5; }
    .mt-match:first-child { border-top: 0; }
    .mt-src-name { font-weight: 600; font-size: 0.875rem; margin: 0; cursor: pointer; }
    .mt-src-meta { display: block; font-weight: 400; font-size: 0.75rem; color: #6c757d; }
    .mt-rest { font-size: 0.8rem; color: #6c757d; margin: 10px 0 0; }
    .mt-count { font-size: 0.78rem; color: #6c757d; }
    .mt-count.none { color: rgb(var(--alert-warning-icon-color)); }
    /* sources not in the match list, grouped by reason */
    .mt-others { margin-top: 14px; }
    .mt-group + .mt-group { margin-top: 14px; }
    .mt-group-h { font-size: 0.8rem; color: #6c757d; margin-bottom: 4px; }
    .mt-other-rows { border: 1px solid #dee2e6; border-radius: 8px; padding: 0 14px; }
    .mt-other { display: flex; flex-wrap: wrap; align-items: baseline; justify-content: space-between; column-gap: 16px; row-gap: 2px; padding: 8px 0; border-top: 1px solid #f1f3f5; font-size: 0.875rem; }
    .mt-other:first-child { border-top: 0; }
    .mt-other-name { font-weight: 600; min-width: 0; overflow-wrap: anywhere; }
    .mt-other-act { display: flex; gap: 16px; white-space: nowrap; font-size: 0.8rem; }
    .mt-other-tables { font-weight: 400; color: #6c757d; margin-left: 4px; }
    @media (max-width: 760px) { .mt-match { grid-template-columns: 22px 1fr; } .mt-match > :nth-child(n+4) { grid-column: 2; } }
    /* as tall as the dropdowns beside it (--s2-height in select2-ipt.css, set on the select2 container only), the bin
       centred in it */
    .mt-remove { height: calc(1.5rem + .75rem + 2px); padding: 0; display: flex; align-items: center; justify-content: center; color: #6c757d; font-size: 1.25rem; line-height: 1; }
    .mt-remove:hover { color: rgb(var(--color-gbif-danger)); }
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
            "${t.name?js_string?no_esc}": {name: "${t.name?js_string?no_esc}", description: "${(t.description!"")?js_string?no_esc}", fields: ${t.fields?size?c}}<#sep>,</#sep>
            </#list>
        };
        var fieldsLabel = "${action.getText('manage.mapping.new.fields')?js_string?no_esc}";
        var matchedText = "${action.getText('manage.mapping.new.matched', ['%N%', '%C%'])?js_string?no_esc}";
        var noneMatchedText = "${action.getText('manage.mapping.new.noneMatched')?js_string?no_esc}";
        var noResults = {noResults: function () { return "${action.getText('select.noResults')?js_string?no_esc}"; }};

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
        $('.mt-hows').on('change', 'input[name=how]', function (e, initial) {
            var how = $('.mt-hows input[name=how]:checked').val();
            // matching database sources needs their columns, read only when the card is chosen: reload with ?how=MATCH.
            // Not when the page opens on the card by itself: then the line under the list offers it
            var reload = $(this).filter(':checked').attr('data-reload');
            if (how === 'MATCH' && reload && !initial) {
                window.location.href = reload;
                return;
            }
            $('.mt-hows .mp-choice').each(function () { $(this).toggleClass('on', $(this).find('input').is(':checked')); });
            $('#mt-pick').prop('hidden', how !== 'PICK');
            $('#mt-split').prop('hidden', how !== 'SPLIT');
            $('#mt-match').prop('hidden', how !== 'MATCH');
            $('#mt-continue').attr('form', how === 'SPLIT' ? 'mt-split' : how === 'MATCH' ? 'mt-match' : 'mt-pick');
            renderMatchContinue();
        });
        // several sources: how many columns the chosen table maps, and Continue only with something to create
        function renderMatch($row) {
            var table = $row.find('.mt-match-table').val();
            var n = table ? ($row.data('matched')[table] || 0) : 0;
            var $count = $row.find('.mt-count');
            if (!table) $count.text('').removeClass('none');
            else if (n === 0) $count.text(noneMatchedText).addClass('none');
            else $count.text(matchedText.replace('%N%', n).replace('%C%', $row.data('columns'))).removeClass('none');
        }
        function renderMatchContinue() {
            if ($('#mt-match').prop('hidden')) { $('#mt-continue').prop('disabled', false); return; }
            var any = $('#mt-match .mt-match').filter(function () {
                return $(this).find('.mt-use').is(':checked') && $(this).find('.mt-match-table').val();
            }).length > 0;
            $('#mt-continue').prop('disabled', !any);
        }
        $('#mt-match').on('change', '.mt-match-table', function () {
            var $row = $(this).closest('.mt-match');
            // choosing a table ticks the source
            if ($(this).val()) $row.find('.mt-use').prop('checked', true);
            renderMatch($row);
            renderMatchContinue();
        });
        $('#mt-match').on('change', '.mt-use', renderMatchContinue);
        $('#mt-match .mt-match').each(function () { renderMatch($(this)); });
        $('#mt-match').on('submit', function () {
            var i = 0;
            $(this).find('.mt-match').each(function () {
                var $src = $(this).find('.mt-src'), $table = $(this).find('.mt-match-table');
                $src.removeAttr('name');
                $table.removeAttr('name');
                if ($(this).find('.mt-use').is(':checked') && $table.val()) {
                    $src.attr('name', 'newSources[' + i + ']');
                    $table.attr('name', 'newTableSchemas[' + i + ']');
                    i++;
                }
            });
        });
        $('.mt-hows input[name=how]:checked').trigger('change', [true]);

        function renumber() {
            $('#mt-pairs .mt-pair').each(function (i) {
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
