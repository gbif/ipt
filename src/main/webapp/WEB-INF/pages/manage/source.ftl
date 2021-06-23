<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.source.title'/></title>
<script src="${baseURL}/js/jconfirmation.jquery.js"></script>
<script>
    $(document).ready(function(){
        initHelp();
        $('.confirm').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name="manage.source.confirmation.message"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});
        $("#peekBtn").click(function(e) {
            e.preventDefault();
            $("#modalcontent").load("peek.do?r=${resource.shortname}&id=${id!}");
            $("#modalbox").show();
        });
        $("#modalbox").click(function(e) {
            e.preventDefault();
            $("#modalbox").hide();
        });
    });
</script>
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

    <main class="container">

        <div class="my-3 p-3 bg-body rounded shadow-sm">

            <#include "/WEB-INF/pages/inc/action_alerts.ftl">

            <h5 class="border-bottom pb-2 mb-2 mx-lg-4 mx-2 pt-2 text-gbif-header text-center">
                <@s.text name='manage.source.title'/>
                <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
            </h5>

            <p class="mx-lg-4 mx-2">
                <@s.text name='manage.source.intro'/>
            </p>

            <form class="topForm" action="source.do" method="post">
                <div class="row g-3 mx-lg-4 mx-2">
                    <input type="hidden" name="r" value="${resource.shortname}" />
                    <input type="hidden" name="id" value="${id!}" />

                    <#if source??>
                        <div class="col-lg-6">
                            <@input name="source.name" help="i18n" disabled=id?has_content/>
                        </div>

                        <#-- inputs used by multiple source types -->
                        <#macro multivalue>
                            <@input name="source.multiValueFieldsDelimitedBy" help="i18n" helpOptions={"|":"[ | ] Pipe",";":"[ ; ] Semicolon",",":"[ , ] Comma"}/>
                        </#macro>
                        <#macro dateFormat>
                            <@input name="source.dateFormat" help="i18n" helpOptions={"YYYY-MM-DD":"ISO format: YYYY-MM-DD","MM/DD/YYYY":"US dates: MM/DD/YYYY","DD.MM.YYYY":"DD.MM.YYYY"}/>
                        </#macro>
                        <#macro encoding>
                            <@input name="source.encoding" help="i18n" helpOptions={"UTF-8":"UTF-8","Latin1":"Latin 1","Cp1252":"Windows1252"}/>
                        </#macro>
                        <#macro headerLines>
                            <@input name="source.ignoreHeaderLines" help="i18n" helpOptions={"0":"None","1":"Single Header row"}/>
                        </#macro>

                        <div class="col-lg-6">
                            <div class="table-responsive">
                                <table id="source-properties" class="table table-sm table-borderless text-smaller">
                                    <tr><th><@s.text name='manage.source.readable'/></th><td><#if source.readable> <i class="bi bi-check-circle text-gbif-primary"></i><#else><i class="bi bi-exclamation-circle text-gbif-danger"></i> ${problem!}</#if></td></tr>
                                    <tr><th><@s.text name='manage.source.columns'/></th><td>${source.getColumns()}</td></tr>
                                    <#if source.fieldsTerminatedBy?has_content>
                                        <tr><th><@s.text name='manage.source.file'/></th><td>${(source.file.getAbsolutePath())!}</td></tr>
                                        <tr><th><@s.text name='manage.source.size'/></th><td>${source.fileSizeFormatted!"???"}</td></tr>
                                        <tr><th><@s.text name='manage.source.rows'/></th><td>${source.rows!"???"}</td></tr>
                                        <tr><th><@s.text name='manage.source.modified'/></th><td>${(source.lastModified?datetime?string.long_medium)!}</td></tr>
                                        <#if (logExists)>
                                            <tr><th><@s.text name='manage.source.source.log'/></th><td><a href="${baseURL}/sourcelog.do?r=${resource.shortname}&s=${source.name}"><@s.text name='manage.source.download'/></a></td></tr>
                                        </#if>
                                    <#else>
                                    </#if>
                                </table>
                            </div>
                            <table class="bottomButtons table table-borderless">
                                <tr>
                                    <th>
                                        <@s.submit cssClass="btn btn-sm btn-outline-gbif-primary" name="analyze" key="button.analyze"/>
                                        <a href="#" id="peekBtn" class="btn btn-sm btn-outline-secondary my-1">
                                            <i class="bi bi-eye"></i>
                                        </a>
                                    </th>
                                </tr>
                            </table>
                        </div>

                        <#-- only for sql sources -->
                        <#if source.isSqlSource()>
                            <div class="col-12">
                                <@select name="rdbms" options=jdbcOptions value="${source.rdbms.name!}" i18nkey="sqlSource.rdbms" />
                            </div>
                            <div class="col-lg-6">
                                <@input name="sqlSource.host" help="i18n"/>
                            </div>
                            <div class="col-lg-6">
                                <@input name="sqlSource.database" help="i18n"/>
                            </div>
                            <div class="col-lg-6">
                                <@input name="sqlSource.username" />
                            </div>
                            <div class="col-lg-6">
                                <@input name="sqlSource.password" type="password" />
                            </div>
                            <div class="col-12">
                                <@text name="sqlSource.sql" help="i18n"/>
                                <#if sqlSource.sql?has_content>
                                    <@label i18nkey="sqlSource.sqlLimited" >
                                        ${sqlSource.getSqlLimited(10)}
                                    </@label>
                                </#if>
                            </div>
                            <div class="col-lg-6">
                                <@encoding/>
                            </div>
                            <div class="col-lg-6">
                                <@dateFormat/>
                            </div>
                            <div class="col-lg-6">
                                <@multivalue/>
                            </div>

                        <#-- excel source -->
                        <#elseif source.isExcelSource()>
                            <div class="col-lg-6">
                                <@headerLines/>
                            </div>
                            <div class="col-lg-6">
                                <@select name="source.sheetIdx" options=source.sheets() value="${source.sheetIdx}" i18nkey="excelSource.sheets" />
                            </div>
                            <div class="col-lg-6">
                                <@multivalue/>
                            </div>

                        <#-- file source -->
                        <#else>
                            <div class="col-lg-6">
                                <@headerLines/>
                            </div>
                            <div class="col-lg-6">
                                <@input name="fileSource.fieldsTerminatedByEscaped" help="i18n" helpOptions={"\\t":"[ \\t ] Tab",",":"[ , ] Comma",";":"[ ; ] Semicolon","|":"[ | ] Pipe"}/>
                            </div>
                            <div class="col-lg-6">
                                <@input name="fileSource.fieldsEnclosedByEscaped" help="i18n" helpOptions={"":"None","&quot;":"Double Quote","a":"Single Quote"}/>
                            </div>
                            <div class="col-lg-6">
                                <@multivalue/>
                            </div>
                            <div class="col-lg-6">
                                <@encoding/>
                            </div>
                            <div class="col-lg-6">
                                <@dateFormat/>
                            </div>
                        </#if>

                        <div class="col-12">
                            <@s.submit cssClass="btn btn-outline-gbif-primary" name="save" key="button.save"/>
                            <@s.submit cssClass="btn btn-outline-secondary my-1" name="cancel" key="button.cancel"/>
                            <#if id?has_content>
                                <@s.submit cssClass="confirm btn btn-outline-gbif-danger my-1" name="delete" key="button.delete.source.file"/>
                            </#if>
                        </div>
                    <#else>
                        <div class="col-12">
                            <@s.submit cssClass="btn btn-outline-secondary" name="cancel" key="button.back"/>
                        </div>
                    </#if>
                </div>
            </form>
        </div>
    </main>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
