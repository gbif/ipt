<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name="admin.home.manageExtensions"/></title>
    <script type="text/javascript" src="${baseURL}/js/jconfirmation.jquery.js"></script>

    <script type="text/javascript">
        $(document).ready(function(){
            initHelp();
            $('.confirm').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='admin.extension.update.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>"});
        });
    </script>

    <#macro extensionRow ext>
        <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
            <div class="col-md-3">
                <a name="${ext.rowType}"></a>
                <div class="title">
                    <div class="head">
                        <a href="extension.do?id=${ext.rowType?url}">${ext.title}</a>
                        <#if !ext.isLatest()>
                            <span data-bs-container="body" data-bs-toggle="popover" data-bs-placement="top" data-bs-html="true" data-bs-content="<@s.text name="admin.extension.version.warning" escapeHtml=true/>">
                                <i class="bi bi-exclamation-triangle-fill text-warning"></i>
                            </span>
                        </#if>
                    </div>
                    <div class="actions d-flex">
                        <#if !ext.isLatest()>
                            <form action='updateExtension.do' method='post'>
                                <input type='hidden' name='id' value='${ext.rowType}' />
                                <@s.submit cssClass="confirm btn btn-sm btn-outline-gbif-primary mt-1 me-1" name="update" key="button.update"/>
                            </form>
                        </#if>
                        <form action='extension.do' method='post'>
                            <input type='hidden' name='id' value='${ext.rowType}' />
                            <@s.submit name="delete" cssClass="btn btn-sm btn-outline-danger mt-1" key="button.remove"/>
                        </form>
                    </div>
                </div>
            </div>

            <div class="col-md-9">
                <div class="definition">
                    <div class="body">
                        <div>
                            <p>${ext.description!}
                                <#if ext.link?has_content>
                                    <br/><div class="text-truncate" ><@s.text name="basic.seealso"/> <a  href="${ext.link}">${ext.link}</a></div>
                                </#if>
                            </p>
                        </div>
                        <div class="details table-responsive">
                            <table class="table table-sm table-borderless">
                                <#if ext.issued??>
                                    <tr><th class="col-3"><@s.text name="basic.issued"/></th><td>${ext.issued?date?string.medium}</td></tr>
                                </#if>
                                <tr><th class="col-3"><@s.text name="extension.properties"/></th><td>${ext.properties?size}</td></tr>
                                <tr><th><@s.text name="basic.name"/></th><td>${ext.name}</td></tr>
                                <tr><th><@s.text name="basic.namespace"/></th><td>${ext.namespace}</td></tr>
                                <tr><th><@s.text name="extension.rowtype"/></th><td>${ext.rowType}</td></tr>
                                <#if ext.subject?has_content>
                                    <tr><th><@s.text name="basic.keywords"/></th><td>${ext.subject!}</td></tr>
                                </#if>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </#macro>

    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">

    <main class="container">
        <div class="row g-3">
            <div class="my-3 p-3 bg-body rounded shadow-sm">
                <#include "/WEB-INF/pages/inc/action_alerts.ftl">

                <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header text-center">
                    <@s.text name="admin.extension.coreTypes"/>
                </h5>

                <p class="text-muted mx-md-4 mx-2">
                    <@s.text name="admin.extension.no.coreTypes.installed.help"><@s.param>${cfg.registryUrl}</@s.param></@s.text>
                </p>

                <#assign count=0>

                <#list extensions as ext>
                    <#if ext.core>
                        <#assign count=count+1>
                        <@extensionRow ext/>
                    </#if>
                </#list>

                <#if count=0>
                    <p class="text-danger mx-md-4 mx-2 mb-0">
                        <@s.text name="admin.extension.no.coreTypes.installed"/>
                    </p>
                    <p class="text-muted mx-md-4 mx-2">
                        <span class="badge rounded-pill bg-warning">
                            <i class="bi bi-exclamation-triangle" style="color: black;"></i>
                        </span>
                        <@s.text name="admin.extension.no.coreTypes.installed.debug"><@s.param>${cfg.registryUrl}</@s.param></@s.text>
                    </p>
                </#if>

            </div>

            <div class="my-3 p-3 bg-body rounded shadow-sm">
                <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header">
                    <@s.text name="admin.extension.extensions"/>
                </h5>

                <p class="text-muted mx-md-4 mx-2 mb-0">
                    <@s.text name="admin.extension.no.extensions.installed.help"><@s.param>${cfg.registryUrl}</@s.param></@s.text>
                </p>

                <#assign count=0>
                <#list extensions as ext>
                    <#if !ext.core>
                        <#assign count=count+1>
                        <@extensionRow ext/>
                    </#if>
                </#list>
                <#if count=0>
                    <p class="text-danger mx-md-4 mx-2">
                        <@s.text name="admin.extension.no.extensions.installed"/>
                    </p>
                </#if>
            </div>

            <div class="my-3 p-3 bg-body rounded shadow-sm">
                <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header">
                    <@s.text name="extension.synchronise.title"/>
                </h5>

                <p class="text-muted mx-md-4 mx-2 mb-0">
                    <@s.text name="admin.extensions.synchronise.help"/>
                </p>

                <#if lastSynchronised?has_content>
                    <p><@s.text name="extension.last.synchronised"><@s.param>${lastSynchronised?date?string("yyyy-MM-dd HH:mm:ss")}</@s.param></@s.text></p>
                </#if>

                <form action='extensions.do' method='post'>
                    <div class="col-12 mt-2 mx-md-4 mx-2">
                        <@s.submit name="synchronise" cssClass="btn btn-outline-gbif-primary" key="button.synchronise"/>
                    </div>
                </form>
            </div>

            <div class="my-3 p-3 bg-body rounded shadow-sm">
                <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header">
                    <@s.text name="extension.further.title"/>
                </h5>

                <p class="text-muted mx-md-4 mx-2 mb-0">
                    <@s.text name="extension.further.title.help"/>
                </p>

                <#assign count=0>
                <#list newExtensions as ext>
                    <#assign count=count+1>
                    <div class="row mx-md-3 mx-1 p-2 pb-2 g-2 border-bottom">
                        <div class="col-md-3">
                            <div class="title">
                                <div class="head">
                                    ${ext.title}
                                </div>
                                <div class="actions">
                                    <form action='extension.do' method='post'>
                                        <input type='hidden' name='url' value='${ext.url}' />
                                        <@s.submit name="install" cssClass="btn btn-sm btn-outline-gbif-primary" key="button.install"/>
                                    </form>
                                </div>
                            </div>
                        </div>

                        <div class="col-md-9">
                            <div class="definition">
                                <div class="body">
                                    <div>
                                        <p>${ext.description!}</p>
                                    </div>
                                    <div class="details table-responsive">
                                        <table class="table table-sm table-borderless">
                                            <tr><th class="col-3"><@s.text name="extension.rowtype"/></th><td>${ext.rowType!}</td></tr>
                                            <tr><th><@s.text name="basic.keywords"/></th><td>${ext.subject!}</td></tr>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </#list>

                <#if count=0>
                    <p class="text-gbif-primary mx-md-4 mx-2 mt-2">
                        <@s.text name="extension.already.installed"/>
                    </p>
                </#if>

            </div>
        </div>
    </main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
