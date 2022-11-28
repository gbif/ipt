<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name="admin.home.manageExtensions"/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>

    <script>
        $(document).ready(function(){
            $('.confirm').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", question : "<@s.text name='admin.extension.update.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", buttonType: "primary"});
        });

        $("#synchronise").on("click", displayProcessing);
    </script>

    <#macro extensionRow ext currentIndex numberOfExtensions>
        <div class="row py-2 pb-2 g-2 border-bottom">
            <div class="col-md-2 col-lg-3">
                <div class="title">
                    <div class="head">
                        <a href="extension.do?id=${ext.rowType?url}" class="fw-bold">${ext.title}</a>
                        <#if !ext.isLatest()>
                            <a tabindex="0" role="button"
                               class="popover-link"
                               data-bs-toggle="popover"
                               data-bs-trigger="focus"
                               data-bs-html="true"
                               data-bs-content="<@s.text name="admin.extension.version.warning" escapeHtml=true/>">
                                <i class="bi bi-exclamation-triangle-fill text-warning"></i>
                            </a>
                        </#if>
                    </div>
                </div>
            </div>

            <div class="col-md-7 col-lg-7 text-smaller">
                <div class="definition">
                    <div class="body">
                        <div>
                            <p class="overflow-x-auto">
                                ${ext.description!}
                            </p>
                            <#if ext.link?has_content>
                                <p class="overflow-x-auto">
                                    <@s.text name="basic.seealso"/> <a href="${ext.link}">${ext.link}</a>
                                </p>
                            </#if>
                        </div>
                        <div class="details table-responsive">
                            <table class="table table-sm table-borderless">
                                <#if ext.issued??>
                                    <tr><th class="col-3 py-0"><@s.text name="basic.issued"/></th><td class="py-0">${ext.issued?date?string.long}</td></tr>
                                </#if>
                                <tr><th class="col-3 py-0"><@s.text name="extension.properties"/></th><td class="py-0">${ext.properties?size}</td></tr>
                                <tr><th class="py-0"><@s.text name="basic.name"/></th><td class="py-0">${ext.name}</td></tr>
                                <tr><th class="py-0"><@s.text name="basic.namespace"/></th><td class="py-0">${ext.namespace}</td></tr>
                                <tr><th class="py-0"><@s.text name="extension.rowtype"/></th><td class="py-0">${ext.rowType}</td></tr>
                                <#if ext.subject?has_content>
                                    <tr><th class="py-0"><@s.text name="basic.keywords"/></th><td class="py-0">${ext.subject!}</td></tr>
                                </#if>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-3 col-lg-2 text-smaller">
                <div class="actions d-flex justify-content-end">
                    <#if !ext.isLatest()>
                        <form action='updateExtension.do' method='post'>
                            <input type='hidden' name='id' value='${ext.rowType}' />
                            <@s.submit cssClass="confirm btn btn-sm btn-outline-gbif-primary me-1" name="update" key="button.update"/>
                        </form>
                    </#if>
                    <form action='extension.do' method='post'>
                        <input type='hidden' name='id' value='${ext.rowType}' />
                        <@s.submit name="delete" cssClass="btn btn-sm btn-outline-gbif-danger" key="button.remove"/>
                    </form>
                </div>
            </div>
        </div>
    </#macro>

    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">

    <div class="container-fluid bg-body border-bottom">
        <div class="container my-3">
            <#include "/WEB-INF/pages/inc/action_alerts.ftl">
        </div>

        <div class="container my-3 p-3">
            <div class="text-center">
                <div class="text-uppercase fw-bold fs-smaller-2">
                    <span><@s.text name="menu.admin"/></span>
                </div>

                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    <@s.text name="admin.home.manageExtensions"/>
                </h1>

                <div class="mt-2">
                    <a href="${baseURL}" class="btn btn-sm btn-outline-secondary top-button">
                        <@s.text name="button.cancel"/>
                    </a>
                </div>
            </div>
        </div>
    </div>

    <main class="container">
        <div class="my-3 p-3">
            <h5 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                <@s.text name="admin.extension.coreTypes"/>
            </h5>

            <p>
                <@s.text name="admin.extension.no.coreTypes.installed.help"><@s.param>${cfg.registryUrl}</@s.param></@s.text>
            </p>

            <#assign count=0>
            <#assign numberOfCoreExtensions=0>

            <#list extensions as ext>
                <#if ext.core>
                    <#assign numberOfCoreExtensions=numberOfCoreExtensions+1>
                </#if>
            </#list>

            <#list extensions as ext>
                <#if ext.core>
                    <#assign count=count+1>
                    <@extensionRow ext count numberOfCoreExtensions/>
                </#if>
            </#list>

            <#if count=0>
                <div class="callout callout-warning">
                    <@s.text name="admin.extension.no.coreTypes.installed"/>
                    <@s.text name="admin.extension.no.coreTypes.installed.debug"><@s.param>${cfg.registryUrl}</@s.param></@s.text>
                </div>
            </#if>

        </div>

        <div class="my-3 p-3">
            <h5 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                <@s.text name="admin.extension.extensions"/>
            </h5>

            <p class="mb-0">
                <@s.text name="admin.extension.no.extensions.installed.help"><@s.param>${cfg.registryUrl}</@s.param></@s.text>
            </p>

            <#assign count=0>
            <#assign numberOfExtensions=0>

            <#list extensions as ext>
                <#if !ext.core>
                    <#assign numberOfExtensions=numberOfExtensions+1>
                </#if>
            </#list>

            <#list extensions as ext>
                <#if !ext.core>
                    <#assign count=count+1>
                    <@extensionRow ext count numberOfExtensions/>
                </#if>
            </#list>
            <#if count=0>
                <div class="callout callout-warning">
                    <@s.text name="admin.extension.no.extensions.installed"/>
                </div>
            </#if>
        </div>

        <div class="my-3 p-3">
            <h5 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                <@s.text name="extension.synchronise.title"/>
            </h5>

            <p class="mb-0">
                <@s.text name="admin.extensions.synchronise.help"/>
            </p>

            <#if lastSynchronised?has_content>
                <p><@s.text name="extension.last.synchronised"><@s.param>${lastSynchronised?datetime?string.long_short}</@s.param></@s.text></p>
            </#if>

            <form action='extensions.do' method='post'>
                <div class="col-12 mt-2">
                    <@s.submit name="synchronise" cssClass="btn btn-outline-gbif-primary" key="button.synchronise"/>
                </div>
            </form>
        </div>

        <div class="my-3 p-3">
            <h5 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                <@s.text name="extension.further.title"/>
            </h5>

            <p class="mb-0">
                <@s.text name="extension.further.title.help"/>
            </p>

            <#assign count=0>
            <#list newExtensions as ext>
                <#assign count=count+1>
                <div class="row py-2 pb-2 g-2 border-bottom">
                    <div class="col-md-3">
                        <div class="title">
                            <div class="head fw-bold">
                                ${ext.title}
                            </div>
                        </div>
                    </div>

                    <div class="col-md-8 text-smaller">
                        <div class="definition">
                            <div class="body">
                                <div>
                                    <p class="overflow-x-auto">${ext.description!}</p>
                                </div>
                                <div class="details table-responsive">
                                    <table class="table table-sm table-borderless">
                                        <tr><th class="col-3 py-0"><@s.text name="extension.rowtype"/></th><td class="py-0">${ext.rowType!}</td></tr>
                                        <tr><th class="py-0"><@s.text name="basic.keywords"/></th><td class="py-0">${ext.subject!}</td></tr>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-1 text-smaller">
                        <div class="actions d-flex justify-content-end">
                            <form action='extension.do' method='post'>
                                <input type='hidden' name='url' value='${ext.url}' />
                                <@s.submit name="install" cssClass="btn btn-sm btn-outline-gbif-primary" key="button.install"/>
                            </form>
                        </div>
                    </div>
                </div>
            </#list>

            <#if count=0>
                <div class="callout callout-info">
                    <@s.text name="extension.already.installed"/>
                </div>
            </#if>

        </div>
    </main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
