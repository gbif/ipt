<#ftl output_format="HTML">
<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.ConfigAction" -->
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="admin.home.editConfig"/></title>
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/settings.css">
<link rel="stylesheet" href="${baseURL}/styles/leaflet/leaflet-1.9.4.css">
<script src="${baseURL}/js/leaflet/leaflet-1.9.4.js"></script>
<script src="${baseURL}/js/settings.js"></script>

<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/icon.ftl"/>

<#function hasError name><#return (fieldErrors[name])?? && fieldErrors[name]?has_content></#function>

<#-- a label, with "optional" for fields that can stay empty -->
<#macro fieldLabel for key optional=false>
    <label for="${for}" class="st-label"><@s.text name=key/><#if optional> <span class="st-opt"><@s.text name="admin.config.optional"/></span></#if></label>
</#macro>

<#-- a text field: label, input, its server error, a hint -->
<#macro field name key value="" optional=false hint="" type="text" extra="">
    <div class="st-field">
        <@fieldLabel for=name key=key optional=optional/>
        <input type="${type}" class="form-control form-control-sm<#if hasError(name)> is-invalid</#if>" id="${name}" name="${name}" value="${value}" autocomplete="off" spellcheck="false" ${extra?no_esc}>
        <@s.fielderror cssClass="invalid-feedback d-block list-unstyled my-1" fieldName="${name}"/>
        <#if hint?has_content><div class="st-hint"><@s.text name=hint/></div></#if>
        <#nested>
    </div>
</#macro>

<#-- one fact in the installation card, with a copy button when it's a path or a URL -->
<#macro fact key value copy=false>
    <div class="st-fact-k"><@s.text name=key/></div>
    <div class="st-fact-v"><span class="st-fact-text">${value}</span><#if copy><button type="button" class="st-copy" data-copy="${value}" title="<@s.text name='button.copy'/>" aria-label="<@s.text name='button.copy'/>: <@s.text name=key/>"><@icon name="content_copy"/></button></#if></div>
</#macro>

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<form id="settings-form" action="config.do" method="post" novalidate
      data-production="${productionMode?c}"
      data-saved-url="${savedBaseUrl!}"
      data-unsaved="${(action.hasFieldErrors() || action.hasActionErrors())?c}"
      data-text-leave="<@s.text name='admin.config.savebar.leave'/>"
      data-text-copied="<@s.text name='admin.config.copied'/>"
      data-text-url-title="<@s.text name='admin.config.baseUrl.confirm.title'/>"
      data-text-url-text="<@s.text name='admin.config.baseUrl.confirm.text'/>"
      data-text-url-confirm="<@s.text name='admin.config.baseUrl.confirm.button'/>"
      data-text-both="<@s.text name='admin.config.server.location.both'/>"
      data-text-lat="<@s.text name='admin.config.server.latitude.invalid'/>"
      data-text-lon="<@s.text name='admin.config.server.longitude.invalid'/>"
      data-text-limit="<@s.text name='admin.config.archivalLimit.invalid'/>"
      data-text-url="<@s.text name='admin.config.error.invalidBaseURL'/>">

    <div class="container-fluid border-bottom">
        <div class="container bg-body border rounded-2 mb-4">
            <div class="container my-3 p-3 ov-head ov-head-left">
                <div class="ov-grow">
                    <div class="fs-smaller">
                        <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                            <ol class="breadcrumb mb-0">
                                <li class="breadcrumb-item"><a href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                                <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.admin.settings"/></li>
                            </ol>
                        </nav>
                    </div>
                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal"><@s.text name="admin.home.editConfig"/></h1>
                </div>
                <div class="ov-acts">
                    <button type="submit" id="save" name="save" value="save" class="btn btn-sm btn-gbif-solid top-button"><@icon name="save"/> <@s.text name="button.save"/></button>
                    <a href="${baseURL}/admin/" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="button.back"/></a>
                </div>
            </div>
        </div>
    </div>

    <div class="container-fluid bg-body">
        <div class="container main-content-container">
            <main class="pt-4 pb-5 st-main">

                <#-- facts that can't be changed here: what the help desk asks for first -->
                <section class="ov-card" aria-labelledby="st-facts-title">
                    <div class="ov-ch"><h2 id="st-facts-title"><@s.text name="admin.config.section.installation"/></h2></div>
                    <div class="st-facts">
                        <div class="st-fact-k"><@s.text name="admin.config.setup2.mode"/></div>
                        <div class="st-fact-v">
                            <#if productionMode><@s.text name="admin.config.setup2.mode.production.label"/><#else><@s.text name="admin.config.setup2.mode.test.label"/></#if>
                            <span class="st-fact-note"><@s.text name="admin.config.mode.final"/></span>
                        </div>
                        <@fact key="admin.config.version" value=version!""/>
                        <@fact key="admin.config.registry.url" value=registryUrl!"" copy=true/>
                        <@fact key="admin.config.server.data.dir" value=dataDir copy=true/>
                        <@fact key="admin.config.server.log.dir" value=logDir copy=true/>
                    </div>
                </section>

                <section class="ov-card" id="st-address" aria-labelledby="st-address-title">
                    <div class="ov-ch"><h2 id="st-address-title"><@s.text name="admin.config.section.address"/></h2></div>
                    <div class="ov-sub"><@s.text name="admin.config.section.address.help"/></div>
                    <@field name="baseUrl" key="admin.config.baseUrl" value=baseUrl!"" hint="admin.config.baseUrl.hint" extra='inputmode="url"'>
                        <div class="st-note" id="st-url-note" hidden>
                            <@icon name="info_outlined" class="st-note-ic"/>
                            <div><@s.text name="admin.config.baseUrl.changeNote"/></div>
                        </div>
                        <div class="st-note st-note-warn" id="st-url-localhost" hidden>
                            <@icon name="warning" class="st-note-ic"/>
                            <div><@s.text name="admin.config.baseUrl.invalidBaseURL"/></div>
                        </div>
                    </@field>
                    <@field name="proxy" key="admin.config.proxy" value=proxy!"" optional=true hint="admin.config.proxy.hint" extra='inputmode="url" placeholder="http://proxy.example.org:8080"'/>
                </section>

                <section class="ov-card" id="st-email" aria-labelledby="st-email-title">
                    <div class="ov-ch"><h2 id="st-email-title"><@s.text name="admin.config.section.contact"/></h2></div>
                    <div class="ov-sub"><@s.text name="admin.config.section.contact.help"/></div>
                    <@field name="adminEmail" key="admin.config.adminEmail" value=adminEmail!"" hint="admin.config.adminEmail.hint" type="email"/>

                    <div class="st-grid">
                        <@field name="mailSmtpHost" key="admin.config.mailSmtpHost" value=mailSmtpHost!"" optional=true hint="admin.config.mailSmtpHost.hint"/>
                        <@field name="mailSmtpPort" key="admin.config.mailSmtpPort" value=mailSmtpPort!"" hint="admin.config.mailSmtpPort.hint" extra='inputmode="numeric" placeholder="25"'/>
                        <@field name="mailSmtpUsername" key="admin.config.mailSmtpUsername" value=mailSmtpUsername!"" optional=true/>

                        <div class="st-field">
                            <@fieldLabel for="mailSmtpPassword" key="admin.config.mailSmtpPassword" optional=true/>
                            <input type="hidden" name="mailSmtpPasswordRemove" id="mailSmtpPasswordRemove" value="${mailSmtpPasswordRemove?c}">
                            <#if mailPasswordSaved>
                                <div class="st-saved" id="st-pw-saved"<#if mailSmtpPasswordRemove> hidden</#if>>
                                    <@icon name="lock" class="st-saved-ic"/>
                                    <span><@s.text name="admin.config.mailSmtpPassword.saved"/></span>
                                    <button type="button" class="btn btn-link st-link" id="st-pw-change"><@s.text name="admin.config.mailSmtpPassword.change"/></button>
                                    <button type="button" class="btn btn-link st-link st-link-danger" id="st-pw-remove"><@s.text name="admin.config.mailSmtpPassword.remove"/></button>
                                </div>
                                <div class="st-saved" id="st-pw-removed"<#if !mailSmtpPasswordRemove> hidden</#if>>
                                    <span><@s.text name="admin.config.mailSmtpPassword.removed"/></span>
                                    <button type="button" class="btn btn-link st-link" id="st-pw-undo"><@s.text name="admin.config.mailSmtpPassword.undo"/></button>
                                </div>
                            </#if>
                            <input type="password" class="form-control form-control-sm" id="mailSmtpPassword" name="mailSmtpPassword" autocomplete="new-password"<#if mailPasswordSaved> hidden disabled</#if>>
                        </div>
                    </div>

                    <div class="form-check form-switch st-switch">
                        <input class="form-check-input" type="checkbox" role="switch" id="mailSmtpStartTlsEnable" name="mailSmtpStartTlsEnable" value="true"<#if mailSmtpStartTlsEnable!false> checked</#if>>
                        <input type="hidden" name="mailSmtpStartTlsEnable" value="false">
                        <label class="form-check-label" for="mailSmtpStartTlsEnable"><@s.text name="admin.config.mailSmtpStartTlsEnable"/></label>
                        <div class="st-hint"><@s.text name="admin.config.mailSmtpStartTlsEnable.hint"/></div>
                    </div>

                    <#-- whether publication failure emails go out: needs a host and an administrator email (PublicationFailureEmailUtils) -->
                    <div class="st-state" id="st-mail-state" aria-live="polite">
                        <span class="st-state-on"<#if !failureEmailsOn> hidden</#if>><@icon name="check_circle" class="st-ok"/> <@s.text name="admin.config.failureEmails.on"/></span>
                        <span class="st-state-off"<#if failureEmailsOn> hidden</#if>><@icon name="info_outlined" class="st-muted-ic"/> <@s.text name="admin.config.failureEmails.off"/></span>
                    </div>
                </section>

                <section class="ov-card" id="st-language" aria-labelledby="st-language-title">
                    <div class="ov-ch"><h2 id="st-language-title"><@s.text name="admin.config.defaultLocale"/></h2></div>
                    <div class="ov-sub"><@s.text name="admin.config.defaultLocale.hint"/></div>
                    <div class="st-chips" role="radiogroup" aria-labelledby="st-language-title">
                        <#list languages as code, name>
                            <label class="st-chip">
                                <input type="radio" name="defaultLocale" value="${code}"<#if code == (defaultLocale!"en")> checked</#if>>
                                <span lang="${code}">${name}</span>
                            </label>
                        </#list>
                    </div>
                </section>

                <section class="ov-card" id="st-archiving" aria-labelledby="st-archiving-title">
                    <div class="ov-ch"><h2 id="st-archiving-title"><@s.text name="admin.config.section.archiving"/></h2></div>
                    <div class="ov-sub"><@s.text name="admin.config.section.archiving.help"/></div>
                    <#assign doiOrg = doiAccountOrganisation!>
                    <div class="form-check form-switch st-switch">
                        <input class="form-check-input" type="checkbox" role="switch" id="archivalMode" name="archivalMode" value="true"<#if archivalMode!false> checked</#if><#if doiOrg?has_content> disabled</#if>>
                        <#if !doiOrg?has_content><input type="hidden" name="archivalMode" value="false"></#if>
                        <label class="form-check-label" for="archivalMode"><@s.text name="admin.config.archivalMode"/></label>
                        <#if doiOrg?has_content>
                            <div class="st-hint st-lock"><@icon name="lock" class="st-lock-ic"/> <@s.text name="admin.config.archivalMode.locked"><@s.param>${doiOrg.name!}</@s.param></@s.text></div>
                        <#else>
                            <div class="st-hint"><@s.text name="admin.config.archivalMode.hint"/></div>
                        </#if>
                        <@s.fielderror cssClass="invalid-feedback d-block list-unstyled my-1" fieldName="archivalMode"/>
                    </div>

                    <fieldset class="st-keep" id="st-keep"<#if !(archivalMode!false)> disabled</#if>>
                        <legend class="visually-hidden"><@s.text name="admin.config.archivalLimit"/></legend>
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="archivalKeep" id="archivalKeepAll" value="all"<#if archivalKeep != "last"> checked</#if>>
                            <label class="form-check-label" for="archivalKeepAll"><@s.text name="admin.config.archivalLimit.all"/></label>
                        </div>
                        <div class="form-check st-keep-last">
                            <input class="form-check-input" type="radio" name="archivalKeep" id="archivalKeepLast" value="last"<#if archivalKeep == "last"> checked</#if>>
                            <label class="form-check-label" for="archivalKeepLast"><@s.text name="admin.config.archivalLimit.last"/></label>
                            <input type="text" inputmode="numeric" class="form-control form-control-sm st-num<#if hasError('archivalLimit')> is-invalid</#if>" id="archivalLimit" name="archivalLimit" value="${archivalLimit!}" aria-label="<@s.text name='admin.config.archivalLimit'/>">
                            <span><@s.text name="admin.config.archivalLimit.versions"/></span>
                        </div>
                        <@s.fielderror cssClass="invalid-feedback d-block list-unstyled my-1" fieldName="archivalLimit"/>
                        <div class="st-hint"><@s.text name="admin.config.archivalLimit.hint"/></div>
                    </fieldset>
                </section>

                <section class="ov-card" id="st-location" aria-labelledby="st-location-title">
                    <div class="ov-ch"><h2 id="st-location-title"><@s.text name="admin.config.section.serverLocation"/> <span class="st-opt"><@s.text name="admin.config.optional"/></span></h2></div>
                    <div class="ov-sub"><@s.text name="admin.config.section.serverLocation.help"/></div>
                    <div class="st-location">
                        <div class="st-location-fields">
                            <@field name="latitude" key="admin.config.server.latitude" value=latitude!"" hint="admin.config.server.latitude.hint" extra='inputmode="decimal"'/>
                            <@field name="longitude" key="admin.config.server.longitude" value=longitude!"" hint="admin.config.server.longitude.hint" extra='inputmode="decimal"'/>
                            <button type="button" class="btn btn-sm btn-outline-secondary" id="st-clear-location"><@s.text name="admin.config.server.location.clear"/></button>
                        </div>
                        <div class="st-map-wrap">
                            <div id="st-map" class="st-map" data-tiles="https://tile.gbif.org/3857/omt/{z}/{x}/{y}@{r}x.png?style=osm-bright" data-images="${baseURL}/images/leaflet/"></div>
                            <div class="st-hint"><@s.text name="admin.config.server.location.mapHint"/></div>
                        </div>
                    </div>
                </section>

                <section class="ov-card" id="st-analytics" aria-labelledby="st-analytics-title">
                    <div class="ov-ch"><h2 id="st-analytics-title"><@s.text name="admin.config.section.analytics"/></h2></div>
                    <@field name="analyticsKey" key="admin.config.analyticsKey" value=analyticsKey!"" optional=true hint="admin.config.analyticsKey.hint" extra='placeholder="G-XXXXXXXXXX"'>
                        <div class="st-note st-note-warn" id="st-ua-note" hidden>
                            <@icon name="warning" class="st-note-ic"/>
                            <div><@s.text name="admin.config.analyticsKey.ua"/></div>
                        </div>
                    </@field>
                </section>

                <section class="ov-card" id="st-debugging" aria-labelledby="st-debugging-title">
                    <div class="ov-ch"><h2 id="st-debugging-title"><@s.text name="admin.config.section.debugging"/></h2></div>
                    <div class="form-check form-switch st-switch">
                        <input class="form-check-input" type="checkbox" role="switch" id="debug" name="debug" value="true"<#if debug!false> checked</#if>>
                        <input type="hidden" name="debug" value="false">
                        <label class="form-check-label" for="debug"><@s.text name="admin.config.debug"/></label>
                        <div class="st-hint"><@s.text name="admin.config.debug.hint"/> <a href="${baseURL}/admin/logs.do"><@s.text name="admin.config.debug.logs"/></a></div>
                    </div>
                </section>
            </main>
        </div>
    </div>

    <div class="st-savebar" id="st-savebar" aria-hidden="true">
        <div class="container">
            <span class="st-grow"><@s.text name="admin.config.savebar.unsaved"/></span>
            <a class="btn btn-sm btn-outline-secondary" href="${baseURL}/admin/config.do" id="st-discard"><@s.text name="admin.config.savebar.discard"/></a>
            <button type="submit" class="btn btn-sm btn-gbif-solid" name="save" value="save"><@icon name="save"/> <@s.text name="button.save"/></button>
        </div>
    </div>
</form>

<#include "/WEB-INF/pages/inc/footer.ftl">
