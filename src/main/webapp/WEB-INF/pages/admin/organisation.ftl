<#ftl output_format="HTML">
<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.OrganisationsAction" -->
<#include "/WEB-INF/pages/inc/header.ftl">
<#assign existing = id?has_content && organisation?? && organisation.key??/>
<title><#if existing>${organisation.alias!organisation.name!}<#else><@s.text name="admin.organisation.add.title"/></#if></title>
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/settings.css">
<link rel="stylesheet" href="${baseURL}/styles/users.css">
<link rel="stylesheet" href="${baseURL}/styles/organisations.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2-ipt.css">
<script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>
<script src="${baseURL}/js/jconfirmation.jquery.js"></script>
<script src="${baseURL}/js/organisation.js"></script>

<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/icon.ftl"/>

<#function hasError name><#return (fieldErrors[name])?? && fieldErrors[name]?has_content></#function>

<#macro fieldLabel for key optional=false>
    <label for="${for}" class="st-label"><@s.text name=key/><#if optional> <span class="st-opt"><@s.text name="admin.config.optional"/></span></#if></label>
</#macro>

<#-- a field: label, input, its server error, a hint -->
<#macro field name key value="" optional=false hint="" type="text" extra="" reveal=false placeholder="">
    <div class="st-field">
        <@fieldLabel for=name key=key optional=optional/>
        <#if reveal>
            <#-- Show / Hide (js/show-password.js): the field starts empty, so only what was typed is shown -->
            <div class="input-group input-group-sm">
                <input type="${type}" class="form-control form-control-sm<#if hasError(name)> is-invalid</#if>" id="${name}" name="${name}" value="${value}"<#if placeholder?has_content> placeholder="${placeholder}"</#if> spellcheck="false" ${extra?no_esc}>
                <button type="button" class="btn btn-outline-secondary" data-show-password aria-controls="${name}" aria-pressed="false"
                        data-show-text="<@s.text name='login.password.show'/>" data-hide-text="<@s.text name='login.password.hide'/>"><@s.text name="login.password.show"/></button>
            </div>
        <#else>
            <input type="${type}" class="form-control form-control-sm<#if hasError(name)> is-invalid</#if>" id="${name}" name="${name}" value="${value}"<#if placeholder?has_content> placeholder="${placeholder}"</#if> spellcheck="false" ${extra?no_esc}>
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

<#if existing>
    <#assign orgKey = organisation.key?string/>
    <#assign label = organisation.alias!organisation.name!orgKey/>
    <#assign hosting = orgKey == (hostingOrganisationKey!"")/>
    <#assign actionUrl>${baseURL}/admin/organisation.do?id=${orgKey}</#assign>
</#if>
<#assign ownAccount = organisation.doiRegistrationAgency??/>
<#assign otherActive = activeDoiOrganisation?? && (!existing || activeDoiOrganisation.key?string != orgKey)/>

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
                            <li class="breadcrumb-item"><a href="${baseURL}/admin/organisations.do"><@s.text name="breadcrumb.admin.organisations"/></a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.admin.organisations.organisation"/></li>
                        </ol>
                    </nav>
                </div>
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal">
                    <#if existing>${label}<#else><@s.text name="admin.organisation.add.title"/></#if>
                </h1>
                <#if existing>
                    <div class="ov-meta">
                        <#if hosting><span class="us-you ms-0"><@s.text name="admin.organisations.hosting"/></span></#if>
                        <a href="${portalUrl}/publisher/${orgKey}" target="_blank" rel="noopener"><@s.text name="about.link"/></a>
                    </div>
                </#if>
            </div>
            <div class="ov-acts">
                <button type="submit" id="save" name="save" value="save" form="organisationsForm" class="btn btn-sm btn-gbif-solid top-button"><@icon name="save"/> <@s.text name="button.save"/></button>
                <a href="${baseURL}/admin/organisations.do" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="button.back"/></a>
                <#if existing>
                    <div class="dropdown us-acts">
                        <button id="btnGroup" type="button" class="btn btn-sm btn-outline-secondary top-button ov-more-btn" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='manage.overview.moreActions'/>" aria-label="<@s.text name='manage.overview.moreActions'/>"><@icon name="more_horiz"/></button>
                        <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="btnGroup">
                            <#if deleteBlockedReason??>
                                <li>
                                    <span class="dropdown-item disabled" aria-disabled="true"><@icon name="delete"/> <@s.text name="button.remove"/>
                                        <small>${deleteBlockedReason}</small>
                                    </span>
                                </li>
                            <#else>
                                <li><a class="custom-link dropdown-item us-danger og-remove" href="${actionUrl}&amp;deleteFlag=true" data-post data-name="${label}"
                                       data-title="<@s.text name='dialog.organisation.delete.title'/>" data-text="<@s.text name='dialog.organisation.delete.text'/>"
                                       data-yes="<@s.text name='button.remove'/>" data-cancel="<@s.text name='button.cancel'/>"><@icon name="delete"/> <@s.text name="button.remove"/></a></li>
                            </#if>
                        </ul>
                    </div>
                </#if>
            </div>
        </div>
    </div>
</div>

<div class="container-fluid bg-body">
    <div class="container main-content-container">
        <main class="pt-4 pb-5 st-main">
            <form id="organisationsForm" action="organisation.do" method="post" novalidate>
<#if existing>
                <input type="hidden" name="id" value="${orgKey}">
                <#-- Registry details, kept as they are: Synchronize on the list page refreshes them -->
                <input type="hidden" name="organisation.key" value="${orgKey}">
                <input type="hidden" name="organisation.name" value="${organisation.name!}">
                <input type="hidden" name="organisation.description" value="${organisation.description!}">
                <input type="hidden" name="organisation.homepageURL" value="${organisation.homepageURL!}">
                <input type="hidden" name="organisation.nodeKey" value="${organisation.nodeKey!}">
                <input type="hidden" name="organisation.nodeName" value="${organisation.nodeName!}">
                <input type="hidden" name="organisation.nodeContactEmail" value="${organisation.nodeContactEmail!}">
                <input type="hidden" name="organisation.primaryContactType" value="${organisation.primaryContactType!}">
                <input type="hidden" name="organisation.primaryContactName" value="${organisation.primaryContactName!}">
                <input type="hidden" name="organisation.primaryContactFirstName" value="${organisation.primaryContactFirstName!}">
                <input type="hidden" name="organisation.primaryContactLastName" value="${organisation.primaryContactLastName!}">
                <input type="hidden" name="organisation.primaryContactEmail" value="${organisation.primaryContactEmail!}">
                <input type="hidden" name="organisation.primaryContactPhone" value="${organisation.primaryContactPhone!}">
                <input type="hidden" name="organisation.primaryContactAddress" value="${organisation.primaryContactAddress!}">
                <input type="hidden" name="organisation.primaryContactDescription" value="${organisation.primaryContactDescription!}">

                <section class="ov-card" aria-labelledby="og-registry-title">
                    <div class="ov-ch"><h2 id="og-registry-title"><@s.text name="admin.registration.section.registry"/></h2></div>
                    <#if organisation.description?has_content>
                        <p class="og-desc">${organisation.description}</p>
                    </#if>
                    <div class="st-facts">
                        <@fact key="admin.organisation.key">
                            <a href="${portalUrl}/publisher/${orgKey}" target="_blank" rel="noopener">${organisation.name!orgKey}</a>
                        </@fact>
                        <@fact key="admin.organisation.registryKey">
                            <span class="st-fact-text">${orgKey}</span><button type="button" class="st-copy" data-copy="${orgKey}" title="<@s.text name='button.copy'/>" aria-label="<@s.text name='button.copy'/>: <@s.text name='admin.organisation.registryKey'/>"><@icon name="content_copy"/></button>
                        </@fact>
                        <#if organisation.nodeName?has_content>
                            <@fact key="admin.organisation.node">${organisation.nodeName}</@fact>
                        </#if>
                        <#if organisation.primaryContactName?has_content || organisation.primaryContactEmail?has_content>
                            <@fact key="admin.organisation.contact">
                                ${organisation.primaryContactName!}
                                <#if organisation.primaryContactEmail?has_content>
                                    <#if organisation.primaryContactName?has_content><span class="st-fact-note"></#if><a href="mailto:${organisation.primaryContactEmail}">${organisation.primaryContactEmail}</a><#if organisation.primaryContactName?has_content></span></#if>
                                </#if>
                            </@fact>
                        </#if>
                        <#if (organisation.homepageURL!"")?remove_beginning("[")?remove_ending("]")?trim?has_content>
                            <#-- the Registry gives a list, stored as "[a, b]" -->
                            <@fact key="admin.organisation.homepage">
                                <#list organisation.homepageURL?remove_beginning("[")?remove_ending("]")?split(",") as u>
                                    <#if u?trim?has_content><a href="${u?trim}" target="_blank" rel="noopener">${u?trim}</a><#sep><br></#sep></#if>
                                </#list>
                            </@fact>
                        </#if>
                    </div>
                    <div class="st-hint mt-2"><@s.text name="admin.organisation.registry.sync"/></div>
                </section>
<#else>
                <input type="hidden" id="organisation.name" name="organisation.name" value="${organisation.name!}">
                <input type="hidden" id="organisation.nodeKey" name="organisation.nodeKey" value="${organisation.nodeKey!}">
                <input type="hidden" id="organisation.nodeName" name="organisation.nodeName" value="${organisation.nodeName!}">
                <input type="hidden" id="organisation.primaryContactType" name="organisation.primaryContactType" value="${organisation.primaryContactType!}">
                <input type="hidden" id="organisation.primaryContactName" name="organisation.primaryContactName" value="${organisation.primaryContactName!}">
                <input type="hidden" id="organisation.primaryContactEmail" name="organisation.primaryContactEmail" value="${organisation.primaryContactEmail!}">
                <input type="hidden" id="organisation.homepageURL" name="organisation.homepageURL" value="${organisation.homepageURL!}">
                <input type="hidden" id="organisation.description" name="organisation.description" value="${organisation.description!}">

                <section class="ov-card" aria-labelledby="og-org-title">
                    <div class="ov-ch"><h2 id="og-org-title"><@s.text name="admin.registration.section.registry"/></h2></div>
                    <div class="st-field">
                        <@fieldLabel for="organisation.key" key="admin.organisation.key"/>
                        <select id="organisation.key" name="organisation.key" class="form-select form-select-sm<#if hasError('organisation.key')> is-invalid</#if>"
                                data-placeholder="<@s.text name='admin.organisation.name.select'/>"
                                data-registry="${registryURL}"
                                data-text-request="${action.getText('admin.registration.token.request')}">
                            <#list organisations as o>
                                <#if o.key??><option value="${o.key}"<#if ((organisation.key)!"")?string == o.key?string> selected</#if>>${o.name!}</option><#else><option value=""></option></#if>
                            </#list>
                        </select>
                        <@s.fielderror cssClass="invalid-feedback d-block list-unstyled my-1" fieldName="organisation.key"/>
                        <div class="st-hint"><@s.text name="admin.registration.intro"/></div>
                    </div>
                </section>
</#if>

                <section class="ov-card" aria-labelledby="og-ipt-title">
                    <div class="ov-ch"><h2 id="og-ipt-title"><@s.text name="admin.organisation.section.ipt"/></h2></div>
                    <div class="st-grid">
                        <#-- the stored token is never sent to the page: an existing organisation changes it in its own card -->
                        <#if !existing>
                            <@field name="organisation.password" key="admin.organisation.password" type="password" reveal=true hint="admin.organisation.password.help" extra='autocomplete="off"'>
                                <div id="requestDetails" class="st-hint" hidden></div>
                            </@field>
                        </#if>
                        <@field name="organisation.alias" key="admin.organisation.alias" value=organisation.alias! optional=true hint="admin.registration.alias.hint"/>
                    </div>
                    <div class="form-check form-switch st-switch">
                        <input class="form-check-input" type="checkbox" role="switch" id="organisation.canHost" name="organisation.canHost" value="true"<#if organisation.canHost || (!existing && !action.hasFieldErrors() && !action.hasActionErrors())> checked</#if>>
                        <label class="form-check-label" for="organisation.canHost"><@s.text name="admin.registration.canPublish"/></label>
                        <div class="st-hint"><@s.text name="admin.organisation.canPublish.help"/></div>
                    </div>
                </section>

                <#if existing>
                    <#-- its inputs belong to the og-token form below (form attribute): forms can't nest -->
                    <section class="ov-card" id="og-token-card" aria-labelledby="og-token-title">
                        <div class="ov-ch"><h2 id="og-token-title"><@s.text name="admin.organisation.token.title"/></h2></div>
                        <div class="ov-sub"><@s.text name="admin.organisation.token.intro"/></div>
                        <@field name="newToken" key="admin.organisation.token.new" type="password" reveal=true extra='form="og-token" autocomplete="off"'>
                            <#if organisation.primaryContactEmail?has_content>
                                <#assign who = organisation.primaryContactName!organisation.primaryContactEmail/>
                                <#assign orgName = organisation.name!orgKey/>
                                <#assign mailSubject = "Shared token request for " + orgName/>
                                <#assign mailBody = "Dear " + (organisation.primaryContactName!"colleague") + ",\n\nI administer an Integrated Publishing Toolkit (IPT) that publishes datasets under " + orgName + ". For this I need the organization's shared token. Could you please send it to me?\n\nThank you."/>
                                <div class="st-hint"><a href="mailto:${organisation.primaryContactEmail}?subject=${mailSubject?url}&amp;body=${mailBody?url}"><@s.text name="admin.registration.token.request"><@s.param>${who}</@s.param></@s.text></a></div>
                            </#if>
                        </@field>
                        <div class="og-card-acts">
                            <button type="submit" form="og-token" class="btn btn-sm btn-outline-gbif-primary"><@s.text name="admin.organisation.token.change"/></button>
                        </div>
                    </section>
                </#if>

                <section class="ov-card" aria-labelledby="og-doi-title">
                    <div class="ov-ch"><h2 id="og-doi-title"><@s.text name="admin.organisation.section.doi"/></h2></div>
                    <div class="ov-sub"><@s.text name="admin.organisation.doiRegistrationAgency.help"/></div>
                    <div class="form-check form-switch st-switch">
                        <input class="form-check-input" type="checkbox" role="switch" id="organisation.associatedWithDoiRegistrationAgency" name="organisation.associatedWithDoiRegistrationAgency" value="true"<#if organisation.associatedWithDoiRegistrationAgency> checked</#if>>
                        <label class="form-check-label" for="organisation.associatedWithDoiRegistrationAgency"><@s.text name="admin.organisation.doi.associated"/></label>
                        <div class="st-hint"><@s.text name="admin.organisation.doi.associated.help"/></div>
                    </div>

                    <#-- independent of the association, as before; off: the account isn't sent, so saving clears it -->
                    <div class="form-check form-switch st-switch og-own">
                        <input class="form-check-input" type="checkbox" role="switch" id="og-own-account"<#if ownAccount> checked</#if> data-toggles="og-account" aria-controls="og-account">
                        <label class="form-check-label" for="og-own-account"><@s.text name="admin.organisation.doi.has"/></label>
                    </div>

                    <fieldset id="og-account" class="og-doi"<#if !ownAccount> hidden disabled</#if>>
                        <#-- DataCite is the only agency -->
                        <input type="hidden" name="organisation.doiRegistrationAgency" value="DATACITE">
                        <@s.fielderror cssClass="invalid-feedback d-block list-unstyled my-1" fieldName="organisation.doiRegistrationAgency"/>
                        <div class="st-grid">
                            <@field name="organisation.doiPrefix" key="admin.organisation.doiRegistrationAgency.prefix" value=organisation.doiPrefix! hint=(cfg.registryType! == "PRODUCTION")?then("", "admin.organisation.doi.prefix.hint") extra='placeholder="10.xxxx"'/>
                            <@field name="organisation.agencyAccountUsername" key="admin.organisation.doiRegistrationAgency.username" value=organisation.agencyAccountUsername! hint="admin.organisation.doiRegistrationAgency.username.help" extra='autocomplete="off"'/>
                            <#-- never sent to the page; empty keeps the stored one -->
                            <@field name="organisation.agencyAccountPassword" key="admin.organisation.doiRegistrationAgency.password" type="password" reveal=true extra='autocomplete="new-password"'
                                    placeholder=(existing && organisation.agencyAccountPassword?has_content)?then(action.getText("admin.organisation.secret.saved"), "")/>
                        </div>

                        <#assign activeLocked = otherActive && !organisation.agencyAccountPrimary/>
                        <div class="form-check form-switch st-switch">
                            <input class="form-check-input" type="checkbox" role="switch" id="organisation.agencyAccountPrimary" name="organisation.agencyAccountPrimary" value="true"<#if organisation.agencyAccountPrimary> checked</#if><#if activeLocked> disabled</#if>>
                            <label class="form-check-label" for="organisation.agencyAccountPrimary"><@s.text name="admin.organisation.doi.activate"/></label>
                            <@s.fielderror cssClass="invalid-feedback d-block list-unstyled my-1" fieldName="organisation.agencyAccountPrimary"/>
                            <div class="st-hint"><@s.text name="admin.organisation.doi.activate.help"/></div>
                            <#if activeLocked>
                                <div class="st-note"><@icon name="lock" class="st-note-ic"/><div>
                                    <@s.text name="admin.organisation.doi.activeElsewhere"/>
                                    <a href="${baseURL}/admin/organisation.do?id=${activeDoiOrganisation.key}">${activeDoiOrganisation.alias!activeDoiOrganisation.name}</a>
                                </div></div>
                            <#elseif !cfg.archivalMode>
                                <div class="st-note st-note-warn"><@icon name="warning" class="st-note-ic"/><div>
                                    <@s.text name="admin.organisation.doi.archivalMode"><@s.param>${baseURL}/admin/config.do</@s.param></@s.text>
                                </div></div>
                            </#if>
                        </div>
                    </fieldset>
                </section>
            </form>
            <#if existing>
                <form id="og-token" action="organisationToken.do" method="post" novalidate>
                    <input type="hidden" name="id" value="${orgKey}">
                </form>
            </#if>

<#if existing>
            <#assign orgResources = organisationResources/>
            <section class="ov-card" aria-labelledby="og-res-title">
                <div class="ov-ch">
                    <h2 id="og-res-title"><@s.text name="admin.users.resources"/></h2>
                    <span class="ov-sub m-0">${orgResources?size}</span>
                </div>
                <#if orgResources?has_content>
                    <table class="ov-table us-res">
                        <thead>
                        <tr>
                            <th><@s.text name="manage.home.resource"/></th>
                            <th><span class="visually-hidden"><@s.text name="admin.organisation.resources.state"/></span></th>
                        </tr>
                        </thead>
                        <tbody>
                        <#list orgResources as r>
                            <#assign title = r.title!r.shortname/>
                            <tr<#if (r?index >= 10)> class="us-more" hidden</#if>>
                                <td>
                                    <a href="${baseURL}/manage/resource.do?r=${r.shortname?url}" class="resource-table-link">${title}</a>
                                    <#if title != r.shortname><div class="mh-shortname">${r.shortname}</div></#if>
                                </td>
                                <td class="text-nowrap text-end">
                                    <#if r.registered><span class="us-tag"><@s.text name="admin.organisation.resources.registered"/></span></#if>
                                    <#if r.doi?? && (r.doiOrganisationKey?string)! == orgKey><span class="us-tag">DOI</span></#if>
                                </td>
                            </tr>
                        </#list>
                        </tbody>
                    </table>
                    <#if (orgResources?size > 10)>
                        <button type="button" class="us-show-all" id="us-show-all"><@s.text name="admin.extension.usedBy.showAll"/>: ${orgResources?size}</button>
                    </#if>
                <#else>
                    <p class="ov-sub mb-0"><@s.text name="admin.organisation.resources.none"/></p>
                </#if>
            </section>
</#if>
        </main>
    </div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
