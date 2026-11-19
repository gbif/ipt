<#include "/WEB-INF/pages/macros/icon.ftl"/>
<#include "/WEB-INF/pages/inc/header.ftl">
<title><#if (source.name)?has_content>${source.name}<#else><@s.text name='manage.source.title'/></#if>: ${resource.title!resource.shortname}</title>
<script src="${baseURL}/js/jconfirmation.jquery.js"></script>
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2-ipt.css">
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<script src="${baseURL}/js/select2/select2-4.0.13.full.min.js"></script>
<script>
    $(document).ready(function(){
        $('.confirm').jConfirmAction({
            titleQuestion: "<@s.text name="manage.overview.confirm.source.delete.title"><@s.param>${((source.name)!)?js_string}</@s.param></@s.text>",
            question: "<@s.text name="manage.overview.confirm.source.delete.text"/>",
            yesAnswer: "<@s.text name="button.delete"/>",
            cancelAnswer: "<@s.text name="button.cancel"/>",
            buttonType: "danger"
        });
        $('.sp-analyze').jConfirmAction({
            titleQuestion: "<@s.text name="dialog.source.analyse.title"><@s.param>${((source.name)!)?js_string}</@s.param></@s.text>",
            question: "<@s.text name="dialog.source.analyse.text"/>",
            yesAnswer: "<@s.text name="button.analyze"/>",
            cancelAnswer: "<@s.text name="button.cancel"/>",
            buttonType: "primary",
            processing: true
        });

        // the first rows, read with the saved settings; a SQL source runs its query only when asked
        var $preview = $("#sp-preview");
        function loadPreview() {
            $preview.html('<div class="text-smaller text-muted py-2"><span class="spinner-border spinner-border-sm me-2" role="status"></span><@s.text name="manage.source.preview.loading"/></div>');
            $preview.load("peek.do?r=${resource.shortname}&id=${(id!)?url}", function (response, status, xhr) {
                // the session expired: open the source page, which goes to login and returns there
                if (xhr.status === 401) {
                    window.location.href = "${baseURL}/manage/source.do?r=${resource.shortname}&id=${(id!)?url}";
                    return;
                }
                if (status === "error") {
                    // load() inserts nothing on an error status. 400/404 carry the message from peek.ftl,
                    // other errors have no fragment to show
                    $preview.html(xhr.status === 400 || xhr.status === 404 ? response
                        : $('<div class="alert alert-danger fs-smaller-2" role="alert"></div>')
                            .text('${action.getText("basic.error")?js_string} (' + xhr.status + ')'));
                }
                // long values are cut short in the table: the whole value on hover
                $preview.find(".peek-table td").each(function () {
                    this.title = this.textContent;
                });
                // only the header: say so under it
                var $table = $preview.find(".peek-table");
                if ($table.length && $table.find("tr").not("thead tr").length === 0) {
                    $table.closest(".peek-table-wrapper").after($('<div class="sp-preview-empty"></div>').text($preview.data("empty")));
                }
            });
        }
        // folded by default; the rows are read the first time it opens. Whether a file's preview was left open is
        // remembered in this browser; a SQL source's never is, so its query only runs when asked
        var $toggle = $("#sp-preview-toggle");
        var remember = $preview.data("remember");
        var loaded = false;
        function setPreviewOpen(open) {
            $preview.prop("hidden", !open);
            $toggle.attr("aria-expanded", open).find(".sp-toggle-label").text(open ? $toggle.data("hide") : $toggle.data("show"));
            if (open && !loaded) {
                loaded = true;
                loadPreview();
            }
            if (remember) {
                try { localStorage.setItem("ipt.source.preview.open", open ? "1" : "0"); } catch (e) { /* no storage */ }
            }
        }
        $toggle.on("click", function () {
            setPreviewOpen($preview.prop("hidden"));
        });
        if (remember) {
            var wasOpen = false;
            try { wasOpen = localStorage.getItem("ipt.source.preview.open") === "1"; } catch (e) { /* no storage */ }
            if (wasOpen) {
                setPreviewOpen(true);
            }
        }

        $("#generatedSqlLink").click(function (e) {
            e.preventDefault();
            $("#generatedSqlBlock").slideToggle("fast");
        });

        $("#save").on("click", displayProcessing);

        $(document.body).on('click', '.helpOptionLink', function (e) {
            e.preventDefault();
            // get all link classes
            var classes = $(this).attr('class').split(/\s+/);
            var inputName, inputValue

            for (var i = 0; i < classes.length; i++) {
                // get input name in order to set value
                if (classes[i].startsWith('inputName')) {
                    // get rid of prefix, escape dots
                    inputName = classes[i].replace('inputName-', '').replaceAll('.', '\\.');
                }

                // get value to be set
                if (classes[i].startsWith('inputValue')) {
                    // get rid of prefix
                    inputValue = classes[i].replace('inputValue-', '');
                }
            }

            $('#' + inputName).val(inputValue).trigger('change');
        });

        // changing how a text file or Excel sheet is read analyses it again on save, which reads the whole file
        var readingSettings = $("#source\\.encoding, #source\\.ignoreHeaderLines, #source\\.sheetIdx, "
            + "#fileSource\\.fieldsTerminatedByEscaped, #fileSource\\.fieldsEnclosedByEscaped");
        readingSettings.each(function () {
            $(this).data("saved", $(this).val());
        });
        readingSettings.on("input change", function () {
            var changed = readingSettings.filter(function () {
                return $(this).val() !== $(this).data("saved");
            }).length > 0;
            $("#readingSettingsChanged").prop("hidden", !changed);
        });

        $("#rdbms").select2({
            placeholder: '',
            language: {
                noResults: function () {
                    return '${selectNoResultsFound}';
                }
            },
            width: "100%",
            minimumResultsForSearch: 15,
            theme: 'bootstrap4'
        });

        // DuckDB reads a file on the server: no host, user or password
        var $rdbmsInput = $('#rdbms');
        function toggleServerFields() {
            var duckdb = $rdbmsInput.val() === "duckdb";
            $("#host-block, #username-block, #password-block").prop("hidden", duckdb);
            $("#sqlSource\\.host, #sqlSource\\.username, #sqlSourcePassword").prop('disabled', duckdb);
        }
        if ($rdbmsInput.length) {
            toggleServerFields();
            $rdbmsInput.on('change', toggleServerFields);
        }
    });
</script>
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<#include "/WEB-INF/pages/macros/popover.ftl"/>
<#assign currentLocale = .vars["locale"]/>
<#-- field help icons as Material outlined, see macros/help_icon.ftl -->
<#assign materialHelpIcons = true/>

<#-- the mappings that read this source, with a link to each -->
<#assign usedBy = []>
<#if (source.name)?has_content && id?has_content>
    <#if resource.isDataPackage()>
        <#list resource.dataPackageMappings! as m>
            <#if (m.source.name)?? && m.source.name == source.name>
                <#assign schemaId = (m.dataPackageSchema.identifier)!"">
                <#assign usedBy = usedBy + [{"label": (m.dataPackageTableSchemaName.name)!"?", "url": "dataPackageMapping.do?r=${resource.shortname}&id=${schemaId?url}&mid=${m_index}"}]>
            </#if>
        </#list>
    <#else>
        <#list resource.getMappedExtensions() as ext>
            <#list resource.getMappings(ext.rowType) as m>
                <#if (m.source.name)?? && m.source.name == source.name>
                    <#assign usedBy = usedBy + [{"label": ext.title, "url": "mapping.do?r=${resource.shortname}&id=${ext.rowType?url}&mid=${m_index}"}]>
                </#if>
            </#list>
        </#list>
    </#if>
</#if>
<#assign isFileLike = source?? && (source.sourceType == 'TEXT_FILE' || source.sourceType == 'EXCEL_FILE' || source.sourceType == 'URL')>
<#-- a file read without trouble that has no rows (only its header, or nothing): mappings to it publish nothing -->
<#assign isEmpty = isFileLike && id?has_content && !source.processing && source.readable && source.rows == 0>

    <form class="topForm needs-validation" action="source.do" method="post" novalidate>
        <input type="hidden" name="r" value="${resource.shortname}" />
        <input type="hidden" name="id" value="${id!}" />

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
                                    <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.source"/></li>
                                </ol>
                            </nav>
                        </div>

                        <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal">
                            <#if (source.name)?has_content>${source.name}<#else><@s.text name='manage.source.title'/></#if>
                        </h1>

                        <#if id?has_content && source??>
                            <#-- what kind of source, and what's in it -->
                            <div class="ov-meta">
                                <span class="sp-kind-pill"><#t>
                                    <#if source.sourceType == 'EXCEL_FILE'><@s.text name="manage.overview.source.excel"/><#t>
                                    <#elseif source.sourceType == 'TEXT_FILE'><@s.text name="manage.overview.source.file"/><#t>
                                    <#elseif source.sourceType == 'URL'><@s.text name="manage.overview.source.url"/><#t>
                                    <#else><@s.text name="manage.overview.source.sql"/></#if><#t>
                                </span>
                                <#if source.processing>
                                    <span><span class="spinner-border spinner-border-sm me-1" role="status"></span><@s.text name="manage.source.processing.info"/></span>
                                <#elseif isFileLike && (source.sourceType != 'URL' || source.readable)>
                                    <span><@s.text name="manage.source.rows"/>: ${source.rows?string.number} · <#t>
                                        <@s.text name="manage.source.columns"/>: ${source.getColumns()?string.number} · <#t>
                                        <@s.text name="manage.source.size"/>: <#attempt><@source.formattedFileSize(currentLocale)?interpret /><#recover>-</#attempt> · <#t>
                                        <@s.text name="manage.source.modified"/>: ${(source.lastModified?datetime?string.medium_short)!"-"}<#t>
                                    </span>
                                <#elseif source.isSqlSource() && source.readable>
                                    <span><@s.text name="manage.source.columns"/>: ${source.getColumns()?string.number}</span>
                                </#if>
                            </div>
                            <#if source.sourceType == 'URL' && (source.url)??>
                                <div class="ov-meta sp-url"><span><@s.text name="manage.source.url"/>: <a href="${source.url}" target="_blank" rel="noopener">${source.url}</a></span></div>
                            </#if>
                            <div class="ov-meta">
                                <span><@s.text name="manage.source.usedBy"/>: <#t>
                                    <#if usedBy?has_content><#list usedBy as u><a href="${u.url}">${u.label}</a><#sep>, </#sep></#list><#t>
                                    <#else><@s.text name="manage.source.usedBy.none"/></#if><#t>
                                </span>
                            </div>
                        </#if>
                    </div>

                    <div class="ov-acts">
                        <#if source??>
                            <button type="submit" id="save" class="btn btn-sm btn-gbif-solid top-button" name="save" value="save"><@icon name="save"/> <@s.text name="button.save"/></button>
                        </#if>
                        <button type="submit" class="btn btn-sm btn-outline-secondary top-button" name="cancel" value="cancel"><@s.text name="button.back"/></button>
                        <#if source?? && id?has_content>
                            <#-- more actions last, as on the mapping page -->
                            <div class="dropdown">
                                <button type="button" class="btn btn-sm btn-outline-secondary top-button ov-more-btn" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='manage.overview.moreActions'/>" aria-label="<@s.text name='manage.overview.moreActions'/>"><@icon name="more_horiz"/></button>
                                <ul class="dropdown-menu dropdown-menu-end">
                                    <li><button type="submit" class="dropdown-item sp-analyze" name="analyze" value="analyze"><@s.text name="button.analyze"/></button></li>
                                    <#if logExists>
                                        <li><a class="dropdown-item" href="${baseURL}/manage/sourcelog.do?r=${resource.shortname}&s=${source.name?url}"><@s.text name="manage.source.source.log"/></a></li>
                                    </#if>
                                    <li><hr class="dropdown-divider"></li>
                                    <li><button type="submit" class="dropdown-item text-gbif-danger confirm" name="deleteFlag" value="delete"><@s.text name="button.delete"/></button></li>
                                </ul>
                            </div>
                        </#if>
                    </div>
                </div>
            </div>
        </div>

        <div class="container-fluid bg-body">
            <main class="container main-content-container mb-md-4 pt-4">
                <#if source??>
                    <#-- problems found when the source was last analysed -->
                    <#if problem??>
                        <div class="ov-banner bad" role="status"><@icon name="error" class="ov-ic text-gbif-danger"/><div class="ov-grow">${problem}</div></div>
                    <#elseif id?has_content && source.sourceType == 'URL' && !source.processing && !source.readable>
                        <div class="ov-banner bad" role="status"><@icon name="error" class="ov-ic text-gbif-danger"/><div class="ov-grow"><@s.text name="manage.source.url.notDownloaded"/></div></div>
                    <#elseif id?has_content && (source.sourceType == 'TEXT_FILE' || source.sourceType == 'EXCEL_FILE' || (source.sourceType == 'URL' && !source.processing))>
                        <#if !problemMessages??>
                            <div class="ov-banner info" role="status">
                                <@icon name="info" class="ov-ic"/>
                                <div class="ov-grow"><@s.text name="manage.source.problems.notChecked"/></div>
                                <button type="submit" class="btn btn-sm btn-outline-secondary sp-analyze" name="analyze" value="analyze"><@s.text name="button.analyze"/></button>
                            </div>
                        <#elseif problemMessages?has_content>
                            <div class="ov-banner warn" role="status">
                                <@icon name="warning" class="ov-ic"/>
                                <div class="ov-grow">
                                    <h2><@s.text name="manage.source.problems.found.title"/></h2>
                                    <ul>
                                        <#list problemMessages as message>
                                            <li>${message}</li>
                                        </#list>
                                    </ul>
                                    <#if (source.linesWithEmbeddedBreaksRemoved!0) gt 0>
                                        <div class="ov-text"><@s.text name="manage.source.fixed.lineBreaksRemoved"><@s.param>${source.linesWithEmbeddedBreaksRemoved?c}</@s.param></@s.text></div>
                                    </#if>
                                    <#if emptyLinesMessage??>
                                        <div class="ov-text">${emptyLinesMessage}</div>
                                    </#if>
                                    <div class="ov-text"><a href="${baseURL}/manage/sourcelog.do?r=${resource.shortname}&s=${source.name?url}"><@s.text name="manage.source.problems.seeLog"/></a></div>
                                </div>
                            </div>
                        <#elseif !isEmpty>
                            <div class="sp-ok">
                                <@icon name="check_circle" class="sp-ok-ic text-gbif-success"/>
                                <span>
                                    <@s.text name="manage.source.problems.none"/>
                                    <#if (source.linesWithEmbeddedBreaksRemoved!0) gt 0>
                                        <@s.text name="manage.source.fixed.lineBreaksRemoved"><@s.param>${source.linesWithEmbeddedBreaksRemoved?c}</@s.param></@s.text>
                                    </#if>
                                    ${emptyLinesMessage!}
                                </span>
                            </div>
                        </#if>
                    </#if>
                    <#if isEmpty>
                        <div class="ov-banner warn" role="status"><@icon name="warning" class="ov-ic"/><div class="ov-grow"><@s.text name="manage.source.empty"/></div></div>
                    </#if>

                    <#-- the first rows, as the IPT reads them: above the settings, but under the query that makes them for SQL -->
                    <#macro preview>
                    <#if id?has_content && !source.processing && (source.readable || source.isSqlSource())>
                        <section class="sp-section">
                            <div class="sp-sec-head">
                                <h2><@s.text name="manage.source.preview.title"/></h2>
                                <span><@s.text name="manage.source.preview.desc"/></span>
                                <button type="button" id="sp-preview-toggle" class="ov-link-btn sp-toggle" aria-expanded="false" aria-controls="sp-preview"
                                        data-show="<@s.text name='basic.show'/>" data-hide="<@s.text name='basic.hide'/>"><span class="sp-toggle-label"><@s.text name="basic.show"/></span><@icon name="expand_more"/></button>
                            </div>
                            <div id="sp-preview" class="sp-preview" data-remember="${(!source.isSqlSource())?c}" data-empty="<@s.text name='manage.source.preview.empty'/>" hidden></div>
                        </section>
                    </#if>

                    </#macro>
                    <#if !source.isSqlSource()><@preview/></#if>

                    <#if !id?has_content>
                        <section class="sp-section">
                            <div class="row g-3">
                                <div class="col-lg-6">
                                    <@input name="source.name" value=source.name! help="i18n"/>
                                </div>
                            </div>
                        </section>
                    </#if>

                    <#-- inputs used by multiple source types -->
                    <#macro multivalue>
                        <@input name="source.multiValueFieldsDelimitedBy" help="i18n" value=(source.multiValueFieldsDelimitedBy)! helpOptions={"|":"[ | ] Pipe",";":"[ ; ] Semicolon",",":"[ , ] Comma"}/>
                    </#macro>
                    <#macro encoding>
                        <@input name="source.encoding" help="i18n" value=(source.encoding)! helpOptions={"UTF-8":"UTF-8","Latin1":"Latin 1","Cp1252":"Windows1252"}/>
                    </#macro>
                    <#macro headerLines>
                        <@input name="source.ignoreHeaderLines" value=(source.ignoreHeaderLines)! help="i18n" helpOptions={"0":"None","1":"Single Header row"}/>
                    </#macro>
                    <#-- shown when a setting that decides how the source is read changes -->
                    <#macro readingSettingsChanged>
                        <#if id?has_content>
                            <div class="col-12" id="readingSettingsChanged" hidden>
                                <div class="ov-banner info compact" role="status"><@icon name="info" class="ov-ic"/><div class="ov-grow"><@s.text name="manage.source.readingSettings.help"/></div></div>
                            </div>
                        </#if>
                    </#macro>

                    <#-- only for sql sources -->
                    <#if source.isSqlSource()>
                        <#if (source.rdbms.name)?? && source.rdbms.name=="duckdb">
                            <#assign hideServer = true/>
                        <#else>
                            <#assign hideServer = false/>
                        </#if>
                        <section class="sp-section">
                            <div class="sp-sec-head"><h2><@s.text name="manage.source.database"/></h2></div>
                            <div class="row g-3">
                                <div class="col-lg-6">
                                    <@select name="rdbms" options=jdbcOptions value="${source.rdbms.name!}" i18nkey="sqlSource.rdbms" />
                                </div>
                                <div class="col-lg-6" id="host-block"<#if hideServer> hidden</#if>>
                                    <@input name="sqlSource.host" value=sqlSource.host! help="i18n"/>
                                </div>
                                <div class="col-lg-6">
                                    <@input name="sqlSource.database" value=sqlSource.database! help="i18n"/>
                                </div>
                                <div class="col-lg-6" id="username-block"<#if hideServer> hidden</#if>>
                                    <@input name="sqlSource.username" value=sqlSource.username! />
                                </div>
                                <div class="col-lg-6" id="password-block"<#if hideServer> hidden</#if>>
                                    <@input name="sqlSourcePassword" value=sqlSourcePassword! i18nkey="sqlSource.password" type="password" />
                                </div>
                            </div>
                        </section>
                        <section class="sp-section">
                            <div class="sp-sec-head"><h2><@s.text name="manage.source.query.title"/></h2></div>
                            <@text name="sqlSource.sql" value=sqlSource.sql! help="i18n"/>
                            <#if sqlSource.sql?has_content>
                                <div class="mt-2 text-smaller">
                                    <a id="generatedSqlLink" href="#"><@s.text name="sqlSource.sqlLimited"/></a>
                                </div>
                                <div id="generatedSqlBlock" class="mt-2 text-smaller" style="display: none;">
                                    <pre class="sp-sql">${sqlSource.getSqlLimited(10)!}</pre>
                                </div>
                            </#if>
                        </section>
                        <@preview/>
                    </#if>

                    <section class="sp-section">
                        <div class="sp-sec-head">
                            <h2><@s.text name="manage.source.reading.title"/></h2>
                            <#if isFileLike><span><@s.text name="manage.source.reading.desc"/></span></#if>
                        </div>
                        <div class="row g-3">
                            <#if source.isSqlSource()>
                                <div class="col-lg-6"><@encoding/></div>
                                <div class="col-lg-6"><@multivalue/></div>

                            <#-- excel source -->
                            <#elseif source.isExcelSource()>
                                <div class="col-lg-6">
                                    <@select name="source.sheetIdx" options=source.sheets() value="${source.sheetIdx}" i18nkey="excelSource.sheets" />
                                </div>
                                <div class="col-lg-6"><@headerLines/></div>
                                <@readingSettingsChanged/>
                                <div class="col-lg-6"><@multivalue/></div>

                            <#-- file source -->
                            <#elseif source.isFileSource()>
                                <div class="col-lg-6"><@encoding/></div>
                                <div class="col-lg-6"><@headerLines/></div>
                                <div class="col-lg-6">
                                    <@input name="fileSource.fieldsTerminatedByEscaped" value=fileSource.fieldsTerminatedByEscaped! help="i18n" helpOptions={"\\t":"[ \\t ] Tab",",":"[ , ] Comma",";":"[ ; ] Semicolon","|":"[ | ] Pipe"}/>
                                </div>
                                <div class="col-lg-6">
                                    <@input name="fileSource.fieldsEnclosedByEscaped" value=fileSource.fieldsEnclosedByEscaped! help="i18n" helpOptions={"":"None","&quot;":"Double Quote","&apos;":"Single Quote"}/>
                                </div>
                                <@readingSettingsChanged/>
                                <div class="col-lg-6"><@multivalue/></div>

                            <#-- URL source -->
                            <#else>
                                <div class="col-lg-6"><@encoding/></div>
                                <div class="col-lg-6"><@headerLines/></div>
                                <div class="col-lg-6">
                                    <@input name="source.fieldsTerminatedByEscaped" value=source.fieldsTerminatedByEscaped! i18nkey="fileSource.fieldsTerminatedByEscaped" help="i18n" helpOptions={"\\t":"[ \\t ] Tab",",":"[ , ] Comma",";":"[ ; ] Semicolon","|":"[ | ] Pipe"}/>
                                </div>
                                <div class="col-lg-6">
                                    <@input name="source.fieldsEnclosedByEscaped" value=source.fieldsEnclosedByEscaped! i18nkey="fileSource.fieldsEnclosedByEscaped" help="i18n" helpOptions={"":"None","&quot;":"Double Quote","&apos;":"Single Quote"}/>
                                </div>
                                <div class="col-lg-6"><@multivalue/></div>
                            </#if>
                        </div>
                    </section>
                </#if>
            </main>
        </div>
    </form>

<style>
    .help-icon-m { width: 16px; height: 16px; margin: 0 4px 0 2px; vertical-align: -3px; }
    .sp-kind-pill { font-size: 0.72rem; font-weight: 500; color: #495057; background: #f1f3f5; border-radius: 10px; padding: 1px 8px; line-height: 18px; }
    .ov-head-left .ov-meta + .ov-meta { margin-top: 4px; }
    .sp-url a { overflow-wrap: anywhere; }
    .sp-ok-ic { width: 18px; height: 18px; flex: none; }
    .sp-ok { display: flex; gap: 6px; align-items: center; font-size: 0.82rem; color: #495057; margin: 0 0 32px; }
    .sp-preview-empty { font-size: 0.82rem; color: #6c757d; margin-top: 8px; }
    /* single characters are easier to tell apart in a monospace font */
    #fileSource\.fieldsTerminatedByEscaped, #fileSource\.fieldsEnclosedByEscaped, #source\.fieldsTerminatedByEscaped,
    #source\.fieldsEnclosedByEscaped, #source\.multiValueFieldsDelimitedBy { font-family: var(--bs-font-monospace); }
    .sp-section { margin-bottom: 32px; }
    .sp-sec-head { display: flex; align-items: baseline; gap: 10px; flex-wrap: wrap; border-bottom: 1px solid #dee2e6; padding-bottom: 6px; margin-bottom: 14px; }
    .sp-sec-head h2 { font-size: 1.1rem; font-weight: 500; color: #4e565f; margin: 0; }
    .sp-sec-head > span { color: #6c757d; font-size: 0.78rem; flex: 1; }
    .sp-toggle { display: inline-flex; align-items: center; gap: 2px; font-size: 0.82rem; align-self: center; }
    .sp-toggle .material-icon { width: 18px; height: 18px; transition: transform .15s; }
    .sp-toggle[aria-expanded="true"] .material-icon { transform: rotate(180deg); }
    .sp-preview .peek-table-wrapper { border: 1px solid #dee2e6; border-radius: 8px; max-height: 420px; overflow: auto; }
    .sp-preview .peek-table { margin: 0; font-size: 0.78rem; }
    .sp-preview .peek-table th { position: sticky; top: 0; background: #f8f9fa; font-weight: 600; color: #495057; white-space: nowrap; border-bottom: 1px solid #dee2e6; padding: 6px 10px; }
    .sp-preview .peek-table td { max-width: 260px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; padding: 4px 10px; border-top: 1px solid #f1f3f5; }
    .sp-preview .peek-table tr.odd td { background: #fcfcfd; }
    .sp-preview .peek-table td em { color: #adb5bd; }
    .sp-sql { white-space: pre-wrap; background: #f8f9fa; border: 1px solid #dee2e6; border-radius: 6px; padding: 8px 10px; }
</style>

<#include "/WEB-INF/pages/inc/footer.ftl">
