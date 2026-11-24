<#ftl output_format="HTML">
<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.RegistrationAction" -->
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="admin.home.editRegistration"/></title>
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/settings.css">
<link rel="stylesheet" href="${baseURL}/styles/registration.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2-ipt.css">
<script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>
<script src="${baseURL}/js/registration.js"></script>

<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/icon.ftl"/>

<#function hasError name><#return (fieldErrors[name])?? && fieldErrors[name]?has_content></#function>

<#macro fieldLabel for key optional=false>
    <label for="${for}" class="st-label"><@s.text name=key/><#if optional> <span class="st-opt"><@s.text name="admin.config.optional"/></span></#if></label>
</#macro>

<#-- a field: label, input (or textarea), its server error, a hint -->
<#macro field name key value="" optional=false hint="" type="text" extra="" wide=false>
    <div class="st-field<#if wide> rg-wide</#if>">
        <@fieldLabel for=name key=key optional=optional/>
        <#if type == "textarea">
            <textarea class="form-control form-control-sm<#if hasError(name)> is-invalid</#if>" id="${name}" name="${name}" rows="5" ${extra?no_esc}>${value}</textarea>
        <#else>
            <input type="${type}" class="form-control form-control-sm<#if hasError(name)> is-invalid</#if>" id="${name}" name="${name}" value="${value}" spellcheck="false" ${extra?no_esc}>
        </#if>
        <@s.fielderror cssClass="invalid-feedback d-block list-unstyled my-1" fieldName="${name}"/>
        <#if hint?has_content><div class="st-hint"><@s.text name=hint/></div></#if>
        <#nested>
    </div>
</#macro>

<#macro fact key>
    <div class="st-fact-k"><@s.text name=key/></div>
    <div class="st-fact-v"><#nested></div>
</#macro>

<#assign registered = hostingOrganisation?has_content>

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<div class="container-fluid border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3 ov-head ov-head-left">
            <div class="ov-grow">
                <div class="fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.admin.registration"/></li>
                        </ol>
                    </nav>
                </div>
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal"><@s.text name="admin.home.editRegistration"/></h1>
                <div class="ov-meta">
                    <#if registered>
                        <span><@icon name="check_circle" class="rg-ok"/> <@s.text name="admin.registration.state.registered"/></span>
                    <#else>
                        <span><@s.text name="admin.registration.state.unregistered"/></span>
                    </#if>
                </div>
            </div>
            <div class="ov-acts">
                <#if registered>
                    <button type="submit" id="update" name="update" value="update" form="registration" class="btn btn-sm btn-gbif-solid top-button"><@icon name="save"/> <@s.text name="button.save"/></button>
                <#else>
                    <button type="submit" id="save" name="save" value="save" form="registrationForm" class="btn btn-sm btn-gbif-solid top-button"<#if !validatedBaseURL> disabled</#if>><@s.text name="button.register"/></button>
                </#if>
                <a href="${baseURL}/admin/" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="button.back"/></a>
            </div>
        </div>
    </div>
</div>

<div class="container-fluid bg-body">
    <div class="container main-content-container">
        <main class="pt-4 pb-5 st-main">
<#if registered>
            <#-- registered: what GBIF knows about this IPT, then the parts that can still be changed -->
            <section class="ov-card" aria-labelledby="rg-registry-title">
                <div class="ov-ch"><h2 id="rg-registry-title"><@s.text name="admin.registration.section.registry"/></h2></div>
                <div class="st-facts">
                    <@fact key="admin.registration.installation">
                        <a href="${cfg.portalUrl}/installation/${registeredIpt.key}" target="_blank" rel="noopener">${registeredIpt.name!registeredIpt.key}</a>
                    </@fact>
                    <@fact key="admin.registration.installationKey">
                        <span class="st-fact-text">${registeredIpt.key}</span><button type="button" class="st-copy" data-copy="${registeredIpt.key}" title="<@s.text name='button.copy'/>" aria-label="<@s.text name='button.copy'/>: <@s.text name='admin.registration.installationKey'/>"><@icon name="content_copy"/></button>
                    </@fact>
                    <@fact key="admin.registration.hostingOrganisation">
                        <a href="${cfg.portalUrl}/publisher/${hostingOrganisation.key}" target="_blank" rel="noopener">${hostingOrganisation.name!hostingOrganisation.key}</a>
                        <div class="st-hint"><@s.text name="admin.registration.hostingOrganisation.fixed"/></div>
                    </@fact>
                    <@fact key="admin.ipt.network">
                        <#if (network.name)?has_content>${network.name}<#else><span class="text-muted"><@s.text name="admin.registration.network.none"/></span></#if>
                    </@fact>
                </div>
            </section>

            <section class="ov-card" aria-labelledby="rg-details-title">
                <div class="ov-ch"><h2 id="rg-details-title"><@s.text name="admin.registration.section.details"/></h2></div>
                <div class="ov-sub"><@s.text name="admin.registration.section.details.help"/></div>
                <form id="registration" action="updateRegistration.do" method="post" novalidate>
                    <@field name="registeredIpt.name" key="admin.ipt.name" value=(registeredIpt.name)! extra='maxlength="255"'/>
                    <@field name="registeredIpt.description" key="admin.ipt.description" value=(registeredIpt.description)! type="textarea" wide=true/>
                    <div class="st-grid">
                        <@field name="registeredIpt.primaryContactName" key="admin.ipt.primaryContactName" value=(registeredIpt.primaryContactName)! extra='maxlength="255"'/>
                        <@field name="registeredIpt.primaryContactEmail" key="admin.ipt.primaryContactEmail" value=(registeredIpt.primaryContactEmail)! type="email" extra='maxlength="254"'/>
                    </div>
                    <#if (registeredResourcesCount > 0)>
                        <div class="st-hint"><@s.text name="admin.registration.details.resources"/> <@s.text name="admin.registration.registeredResources"/>: ${registeredResourcesCount}</div>
                    </#if>
                </form>
            </section>

            <section class="ov-card" aria-labelledby="rg-network-title">
                <div class="ov-ch"><h2 id="rg-network-title"><@s.text name="admin.ipt.network"/></h2></div>
                <div class="ov-sub"><@s.text name="admin.ipt.network.intro"/></div>
                <form id="networkForm" action="associateWithNetwork.do" method="post" novalidate>
                    <input type="hidden" name="associateNetwork" value="true">
                    <div class="st-field">
                        <label for="networkKey" class="visually-hidden"><@s.text name="admin.ipt.network"/></label>
                        <select id="networkKey" name="networkKey" class="form-select form-select-sm" data-placeholder="<@s.text name='admin.ipt.network.selection'/>">
                            <option value=""></option>
                            <#list networks as key, name>
                                <#if key?has_content><option value="${key}"<#if key == ((network.key)!"")?string> selected</#if>>${name}</option></#if>
                            </#list>
                        </select>
                        <div class="st-hint"><@s.text name="admin.registration.network.clear"/></div>
                    </div>
                    <#if (registeredResourcesCount > 0)>
                    <div class="form-check form-switch st-switch">
                        <input class="form-check-input" type="checkbox" role="switch" id="applyToExistingResources" name="applyToExistingResources" value="true">
                        <label class="form-check-label" for="applyToExistingResources"><@s.text name="admin.ipt.network.applyToExisting"/></label>
                        <div class="st-hint"><@s.text name="admin.registration.registeredResources"/>: ${registeredResourcesCount}</div>
                    </div>
                    </#if>
                    <div class="rg-card-acts">
                        <button type="submit" id="network" name="network" value="network" class="btn btn-sm btn-outline-gbif-primary"><@s.text name="admin.registration.network.save"/></button>
                    </div>
                </form>
            </section>

            <section class="ov-card" id="rg-tokens" aria-labelledby="rg-tokens-title">
                <div class="ov-ch"><h2 id="rg-tokens-title"><@s.text name="admin.ipt.tokens.title"/></h2></div>
                <div class="ov-sub"><@s.text name="admin.ipt.tokens.intro"/></div>
                <form id="changeTokens" action="changeTokens.do" method="post" novalidate>
                    <input type="hidden" name="tokenChange" value="true">
                    <div class="st-grid">
                        <@field name="hostingOrganisationToken" key="admin.organisation.password" type="password" optional=true hint="admin.registration.token.hint" extra='autocomplete="off"'/>
                        <@field name="registeredIptPassword" key="admin.ipt.password" type="password" optional=true hint="admin.registration.iptPassword.hint" extra='autocomplete="new-password"'/>
                    </div>
                    <div class="rg-card-acts">
                        <button type="submit" id="tokens" name="tokens" value="tokens" class="btn btn-sm btn-outline-gbif-primary"><@s.text name="admin.registration.tokens.change"/></button>
                    </div>
                </form>
            </section>
<#else>
            <#-- not registered: check the public URL, then choose the hosting organization and describe this installation -->
            <section class="ov-card rg-step" id="rg-url" aria-labelledby="rg-url-title">
                <div class="ov-ch"><h2 id="rg-url-title"><span class="rg-num">1</span><@s.text name="admin.registration.baseURL"/></h2></div>
                <div class="ov-sub"><@s.text name="admin.registration.step.url.help"/></div>
                <div class="rg-url-row">
                    <span class="st-fact-text rg-url">${baseURL}/</span>
                    <button type="button" id="validate" class="btn btn-sm btn-outline-gbif-primary"
                            data-check-url="https://tools.gbif.org/ws-validurl/?url=${baseURL?url}/&callback=?"
                            data-development="${(cfg.registryType == "DEVELOPMENT")?c}">
                        <span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true" hidden></span><@s.text name="admin.registration.validate"/>
                    </button>
                </div>
                <div class="st-hint"><@s.text name="admin.registration.baseURL.change"><@s.param>${baseURL}/admin/config.do</@s.param></@s.text></div>
                <div aria-live="polite">
                    <div class="st-state" id="validation-success"<#if !validatedBaseURL> hidden</#if>><@icon name="check_circle" class="st-ok"/> <@s.text name="admin.registration.validate.success"/></div>
                    <div class="st-note st-note-warn" id="validation-failed-development" hidden><@icon name="warning" class="st-note-ic"/><div><@s.text name="admin.registration.validate.failed.development"/></div></div>
                    <div class="st-note rg-note-bad" id="validation-failed" hidden><@icon name="error" class="st-note-ic"/><div><@s.text name="admin.registration.validate.failed"/></div></div>
                </div>
            </section>

            <form id="registrationForm" action="registration.do" method="post" novalidate>
                <fieldset id="rg-steps" class="rg-steps"<#if !validatedBaseURL> disabled</#if>>
                    <div class="rg-locked st-hint" id="rg-locked"<#if validatedBaseURL> hidden</#if>><@icon name="lock" class="st-lock-ic"/> <@s.text name="admin.registration.locked"/></div>

                    <section class="ov-card rg-step" aria-labelledby="rg-org-title">
                        <div class="ov-ch"><h2 id="rg-org-title"><span class="rg-num">2</span><@s.text name="admin.registration.hostingOrganisation"/></h2></div>
                        <div class="ov-sub"><@s.text name="admin.registration.step.organisation.help"/></div>

                        <div class="st-field">
                            <@fieldLabel for="organisation.key" key="admin.organisation.key"/>
                            <select id="organisation.key" name="organisation.key" class="form-select form-select-sm<#if hasError('organisation.key')> is-invalid</#if>"
                                    data-placeholder="<@s.text name='admin.organisation.name.select'/>"
                                    data-registry="${registryURL}"
                                    data-api="${(cfg.registryUrl!)?contains("gbif-test")?then("https://api.gbif-test.org/v1/", "https://api.gbif.org/v1/")}"
                                    data-portal="${cfg.portalUrl!}"
                                    data-text-request="${action.getText('admin.registration.token.request')}"
                                    data-text-duplicate="${action.getText('admin.registration.duplicate.warning')}">
                                <#list organisations as o>
                                    <#if o.key??><option value="${o.key}"<#if ((organisation.key)!"")?string == o.key?string> selected</#if>>${o.name!}</option><#else><option value=""></option></#if>
                                </#list>
                            </select>
                            <@s.fielderror cssClass="invalid-feedback d-block list-unstyled my-1" fieldName="organisation.key"/>
                            <div class="st-hint"><@s.text name="admin.registration.intro"/></div>
                            <div class="st-note st-note-warn" id="installations-warning" hidden><@icon name="warning" class="st-note-ic"/><div class="rg-dup-text"></div></div>
                        </div>

                        <div class="st-grid">
                            <@field name="organisation.password" key="admin.organisation.password" type="password" hint="admin.organisation.password.help" extra='autocomplete="off"'>
                                <div id="requestDetails" class="st-hint" hidden></div>
                            </@field>
                            <@field name="organisation.alias" key="admin.organisation.alias" value=(organisation.alias)! optional=true hint="admin.registration.alias.hint"/>
                        </div>

                        <div class="form-check form-switch st-switch">
                            <input class="form-check-input" type="checkbox" role="switch" id="organisation.canHost" name="organisation.canHost" value="true"<#if !(organisation??) || organisation.canHost> checked</#if>>
                            <input type="hidden" name="organisation.canHost" value="false">
                            <label class="form-check-label" for="organisation.canHost"><@s.text name="admin.registration.canPublish"/></label>
                            <div class="st-hint"><@s.text name="admin.organisation.canPublish.help"/></div>
                        </div>

                        <input type="hidden" id="organisation.name" name="organisation.name" value="${(organisation.name)!}">
                        <input type="hidden" id="organisation.primaryContactType" name="organisation.primaryContactType" value="${(organisation.primaryContactType)!}">
                        <input type="hidden" id="organisation.primaryContactName" name="organisation.primaryContactName" value="${(organisation.primaryContactName)!}">
                        <input type="hidden" id="organisation.primaryContactEmail" name="organisation.primaryContactEmail" value="${(organisation.primaryContactEmail)!}">
                        <input type="hidden" id="organisation.nodeKey" name="organisation.nodeKey" value="${(organisation.nodeKey)!}">
                        <input type="hidden" id="organisation.nodeName" name="organisation.nodeName" value="${(organisation.nodeName)!}">
                        <input type="hidden" id="ipt.organisationKey" name="ipt.organisationKey" value="${(ipt.organisationKey)!}">
                    </section>

                    <section class="ov-card rg-step" aria-labelledby="rg-ipt-title">
                        <div class="ov-ch"><h2 id="rg-ipt-title"><span class="rg-num">3</span><@s.text name="admin.registration.section.installation"/></h2></div>
                        <div class="ov-sub"><@s.text name="admin.registration.section.details.help"/></div>
                        <@field name="ipt.name" key="admin.ipt.name" value=(ipt.name)! extra='maxlength="255"'/>
                        <@field name="ipt.description" key="admin.ipt.description" value=(ipt.description)! type="textarea" wide=true/>
                        <div class="st-grid">
                            <@field name="ipt.primaryContactName" key="admin.ipt.primaryContactName" value=(ipt.primaryContactName)! extra='maxlength="255"'/>
                            <@field name="ipt.primaryContactEmail" key="admin.ipt.primaryContactEmail" value=(ipt.primaryContactEmail)! type="email" extra='maxlength="254"'/>
                            <@field name="ipt.wsPassword" key="admin.ipt.password" type="password" hint="admin.ipt.password.help" extra='autocomplete="new-password"'/>
                        </div>
                        <input type="hidden" name="ipt.primaryContactType" value="technical">
                    </section>

                    <div class="ov-banner info compact">
                        <@icon name="info_outlined" class="ov-ic"/>
                        <div class="ov-grow"><@s.text name="admin.registration.intro2"/></div>
                    </div>
                    <div class="rg-card-acts">
                        <button type="submit" name="save" value="save" class="btn btn-sm btn-gbif-solid"<#if !validatedBaseURL> disabled</#if>><@s.text name="button.register"/></button>
                    </div>
                </fieldset>
            </form>
</#if>
        </main>
    </div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
