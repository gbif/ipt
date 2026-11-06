<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.OverviewAction" -->
<#-- Visibility & registration section of the resource overview: private → public → registered, with what registering needs. Needs overview_status.ftl -->
<#assign pencilIcon><svg viewBox="0 0 24 24" class="overview-action-button-icon"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path></svg></#assign>

<#-- the checks resource-registerResource.do makes, in the order #registration-modal reports them -->
<#assign registerChecks = []>
<#macro registerCheck ok okKey noKey escape=false>
    <#local text><#if ok><@s.text name=okKey/><#else><@s.text name=noKey escapeHtml=escape/></#if></#local>
    <#assign registerChecks = registerChecks + [{"ok": ok, "text": text}]>
</#macro>
<#assign registrationTypeSupported = !resource.dataPackage || resource.coreType == "camtrap-dp" || resource.coreType == "coldp" || resource.coreType == "dwc-dp">
<@registerCheck currentUser.hasRegistrationRights() "manage.overview.sharing.check.rights.ok" "manage.overview.sharing.check.rights.no"/>
<#if !registrationTypeSupported>
    <@registerCheck false "" "manage.resource.status.registration.forbiddenTypes"/>
</#if>
<@registerCheck !missingValidPublishingOrganisation "manage.overview.sharing.check.organisation.ok" "manage.overview.visibility.missing.organisation"/>
<@registerCheck !missingRegistrationMetadata "manage.overview.sharing.check.metadata.ok" "manage.overview.sharing.check.metadata.no"/>
<@registerCheck resource.isLastPublishedVersionPublic() "manage.overview.sharing.check.public.ok" "manage.overview.prevented.resource.registration.notPublic"/>
<@registerCheck action.isLastPublishedVersionAssignedGBIFSupportedLicense(resource) "manage.overview.sharing.check.licence.ok" "manage.overview.prevented.resource.registration.noGBIFLicense" true/>
<#assign canRegister = resource.status == "PUBLIC" && !registerChecks?filter(c -> !c.ok)?has_content>

<span class="anchor anchor-overview-page" id="anchor-visibility"></span>
<section class="ov-card section<#if isDeleted> dim</#if>" id="visibility">
    <div class="ov-ch">
        <h2>
            <#assign visibilityTitleInfo>
                <@s.text name='manage.overview.visibility.description'/><br><br>
                <@s.text name="manage.resource.status.intro.registration"/>
                <#if !resource.dataPackage??><br><br><@s.text name='manage.resource.status.intro.public.migration'><@s.param><a href="${baseURL}/manage/metadata-additional.do?r=${resource.shortname}&amp;edit=Edit"><@s.text name="submenu.additional"/></a></@s.param></@s.text></#if>
                <br><br><@s.text name='manage.resource.status.intro.public.gbifWarning'/>
            </#assign>
            <@popoverTextInfo visibilityTitleInfo/>
            <@s.text name="manage.overview.sharing.title"/>
        </h2>
        <#if resource.status == "REGISTERED">
            <span class="ov-pill ok"><@s.text name="resource.status.registered"/></span>
        <#elseif resource.status == "PUBLIC">
            <span class="ov-pill warn"><@s.text name="manage.overview.sharing.notRegistered"/></span>
        </#if>
    </div>
    <div class="ov-sub"><@s.text name="manage.overview.sharing.sub"/></div>

    <#assign steps = [["PRIVATE", "private"], ["PUBLIC", "public"], ["REGISTERED", "registered"]]>
    <#assign currentStep = ["PRIVATE", "PUBLIC", "REGISTERED"]?seq_index_of(resource.status?string)>
    <div class="ov-steps">
        <#list steps as step>
            <#if step?index == currentStep>
                <#assign stepClass = "on">
            <#elseif !isDeleted && ((resource.pendingStatus?string)! == step[0] || (!isStatusPending && step?index == currentStep + 1))>
                <#assign stepClass = "next">
            <#else>
                <#assign stepClass = "">
            </#if>
            <div class="${stepClass}">
                <b><#if step?index == currentStep>● </#if><@s.text name="resource.status.${step[1]}"/></b>
                <@s.text name="manage.overview.sharing.step.${step[1]}"/>
            </div>
        </#list>
    </div>

    <#if isDeleted>
        <p class="text-smaller text-muted mt-2 mb-0"><@s.text name="manage.overview.sharing.deleted"/></p>

    <#elseif isStatusPending>
        <p class="text-smaller mt-2 mb-0">
            <@s.text name="manage.overview.sharing.pending"><@s.param><@s.text name="resource.status.${resource.pendingStatus?lower_case}"/></@s.param></@s.text>
            <button id="cancelVisibilityChange" class="ov-link-btn ms-1" type="button"><@s.text name="manage.overview.sharing.cancelChange"/></button>
        </p>

    <#elseif resource.status == "PRIVATE">
        <p class="text-smaller mt-2 mb-2">
            <#if resource.makePublicDate?has_content>
                <@s.text name="manage.resource.status.intro.private.public.scheduled"><@s.param>${resource.makePublicDate?datetime?string.long_short}</@s.param></@s.text>
            <#else>
                <@s.text name="manage.resource.status.intro.private"/>
            </#if>
        </p>
        <#if action.canBeMadePublic()>
            <form action='resource-makePublic.do' method='post' class="d-inline">
                <input name="r" type="hidden" value="${resource.shortname}"/>
                <button id="makePublic" class="btn btn-sm btn-outline-gbif-primary" type="submit"><@s.text name="manage.overview.sharing.makePublic"/></button>
            </form>
        <#else>
            <button id="show-visibility-disabled-modal" class="btn btn-sm btn-outline-gbif-primary ov-blocked" type="button"><@s.text name="manage.overview.sharing.makePublic"/></button>
        </#if>

    <#elseif resource.status == "PUBLIC">
        <div class="ov-h4"><@s.text name="manage.overview.sharing.checks"/></div>
        <ul class="ov-checks">
            <#list registerChecks as c>
                <li><span class="${c.ok?then('y', 'n')}">${c.ok?then('✓', '✗')}</span><span class="${c.ok?then('muted', '')}">${c.text}</span></li>
            </#list>
        </ul>
        <div class="d-flex gap-2 mt-2 flex-wrap">
            <#if canRegister>
                <form action="resource-registerResource.do" method="post" class="d-inline">
                    <input name="r" type="hidden" value="${resource.shortname}"/>
                    <#if isDwcDp>
                        <input id="dwcDpDatasetType" name="dwcDpDatasetType" type="hidden" value=""/>
                    </#if>
                    <button id="register-resource-button" class="confirmRegistration btn btn-sm btn-gbif-solid" type="submit"><@s.text name="manage.overview.sharing.register"/></button>
                </form>
            <#else>
                <button id="show-registration-disabled-modal" class="btn btn-sm btn-outline-gbif-primary ov-blocked" type="button"><@s.text name="manage.overview.sharing.register"/></button>
            </#if>
            <#if action.canBeMadePrivate()>
                <form action='resource-makePrivate.do' method='post' class="d-inline">
                    <input name="r" type="hidden" value="${resource.shortname}"/>
                    <input name="unpublish" type="hidden" value="Change"/>
                    <button id="makePrivate" class="btn btn-sm btn-outline-secondary" type="submit"><@s.text name="manage.overview.sharing.makePrivate"/></button>
                </form>
            </#if>
        </div>

    <#elseif resource.status == "REGISTERED">
        <div class="ov-rows mt-2">
            <#if resource.key??>
                <div class="ov-r">
                    <span><@s.text name="manage.overview.sharing.gbifDataset"/></span>
                    <span><a href="${cfg.portalUrl}/dataset/${resource.key}" title="<@s.text name='manage.overview.registration.view'/>">${resource.key?string?substring(0, 8)}… <i class="bi bi-box-arrow-up-right"></i></a></span>
                </div>
            </#if>
            <#if isDwcDp>
                <div class="ov-r">
                    <span><@s.text name="manage.overview.sharing.datasetType"/></span>
                    <span>
                        <#if resource.dwcDpDatasetType??>
                            <@s.text name="portal.resource.type.${resource.dwcDpDatasetType.name()?lower_case?replace('_', '')}"/>
                        <#else>
                            <span class="ov-missing"><@s.text name="manage.overview.metadata.notSet"/></span>
                        </#if>
                        <#if resource.key?? && currentUser.hasRegistrationRights()>
                            · <button class="change-dwc-dp-dataset-type-button ov-link-btn" type="button"><@s.text name='manage.overview.registration.datasetType.change'/></button>
                        </#if>
                    </span>
                </div>
            </#if>
            <#if resource.isAlreadyAssignedDoi()>
                <div class="ov-r">
                    <span>DOI</span>
                    <span><a href="${(resource.versionHistory[0].doi.url)!}">${resource.versionHistory[0].doi!}</a></span>
                </div>
            </#if>
            <div class="ov-r">
                <span>
                    <@popoverPropertyInfo "manage.overview.networks.description"/>
                    <@s.text name="manage.overview.networks.title"/>
                </span>
                <span>
                    <#assign networks = resourceNetworks>
                    <#if networks?has_content>
                        <#list networks as n>
                            <span class="ov-pill grey"><a href="${cfg.portalUrl}/network/${n.key!}" class="ov-name">${n.title!n.key!""}</a>
                                <a href="resource-deleteNetwork.do?r=${resource.shortname}&id=${n.key!}" class="text-muted" title="<@s.text name='button.delete'/>"><i class="bi bi-x"></i></a></span>
                        </#list>
                    <#else>
                        <@s.text name="manage.overview.metadata.none"/>
                    </#if>
                    · <a id="add-network-button" href="#"><@s.text name="button.add"/></a>
                </span>
            </div>
        </div>
    </#if>
</section>
