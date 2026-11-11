<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.OverviewAction" -->
<#-- Visibility & registration section of the resource overview: the current visibility in one line, the actions that change it,
     and what stops registration. Needs overview_status.ftl -->

<#-- the checks resource-registerResource.do makes, in the order #registration-modal reports them.
     label: short, reads right whether the check passes or not; noKey: what to do when it fails -->
<#assign registerChecks = []>
<#macro registerCheck ok label noKey escape=false href="" linkKey="">
    <#local labelText><@s.text name=label/></#local>
    <#local text><#if !ok><@s.text name=noKey escapeHtml=escape/></#if></#local>
    <#local linkText><#if !ok && linkKey?has_content><@s.text name=linkKey/></#if></#local>
    <#assign registerChecks = registerChecks + [{"ok": ok, "label": labelText, "text": text, "href": href, "linkText": linkText}]>
</#macro>
<#assign registrationTypeSupported = !resource.dataPackage || resource.coreType == "camtrap-dp" || resource.coreType == "coldp" || resource.coreType == "dwc-dp">
<@registerCheck ok=currentUser.hasRegistrationRights() label="manage.overview.sharing.req.rights" noKey="manage.overview.sharing.check.rights.no"/>
<#-- only listed for the data package types GBIF doesn't take yet -->
<#if !registrationTypeSupported>
    <@registerCheck ok=false label="manage.overview.sharing.req.type" noKey="manage.resource.status.registration.forbiddenTypes"/>
</#if>
<@registerCheck ok=!missingValidPublishingOrganisation label="manage.overview.sharing.req.organisation" noKey="manage.overview.sharing.check.organisation.no"
    href="publication-settings.do?r=${resource.shortname}" linkKey="manage.overview.publication.settings"/>
<@registerCheck ok=!missingRegistrationMetadata label="manage.overview.sharing.req.metadata" noKey="manage.overview.sharing.check.metadata.no"
    href="metadata-basic.do?r=${resource.shortname}" linkKey="submenu.basic"/>
<@registerCheck ok=resource.isLastPublishedVersionPublic() label="manage.overview.sharing.req.public" noKey="manage.overview.prevented.resource.registration.notPublic"
    href="#anchor-publish" linkKey="manage.overview.published"/>
<@registerCheck ok=action.isLastPublishedVersionAssignedGBIFSupportedLicense(resource) label="manage.overview.sharing.req.licence" noKey="manage.overview.prevented.resource.registration.noGBIFLicense" escape=true
    href="metadata-basic.do?r=${resource.shortname}" linkKey="submenu.basic"/>
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
        <span class="ov-sp"></span>
        <#if !isDeleted && !isStatusPending>
            <#if resource.status == "PRIVATE">
                <#if action.canBeMadePublic()>
                    <form action='resource-makePublic.do' method='post'>
                        <input name="r" type="hidden" value="${resource.shortname}"/>
                        <button id="makePublic" class="ov-hact" type="submit"><@s.text name="manage.overview.sharing.makePublic"/></button>
                    </form>
                <#else>
                    <button id="show-visibility-disabled-modal" class="ov-hact ov-blocked" type="button"><@s.text name="manage.overview.sharing.makePublic"/></button>
                </#if>
            <#elseif resource.status == "PUBLIC">
                <#if canRegister>
                    <form action="resource-registerResource.do" method="post">
                        <input name="r" type="hidden" value="${resource.shortname}"/>
                        <#if isDwcDp>
                            <input id="dwcDpDatasetType" name="dwcDpDatasetType" type="hidden" value=""/>
                        </#if>
                        <button id="register-resource-button" class="confirmRegistration ov-hact" type="submit"><@s.text name="manage.overview.sharing.register"/></button>
                    </form>
                <#else>
                    <#-- opens the checklist below, which says what's missing -->
                    <button id="register-blocked" class="ov-hact ov-blocked" type="button" title="<@s.text name='manage.overview.sharing.registerBlocked'/>"><@s.text name="manage.overview.sharing.register"/></button>
                </#if>
                <#if action.canBeMadePrivate()>
                    <form action='resource-makePrivate.do' method='post'>
                        <input name="r" type="hidden" value="${resource.shortname}"/>
                        <input name="unpublish" type="hidden" value="Change"/>
                        <button id="makePrivate" class="ov-hact" type="submit"><@s.text name="manage.overview.sharing.makePrivate"/></button>
                    </form>
                </#if>
            </#if>
        </#if>
    </div>

    <#-- what the current visibility means, and what's about to change -->
    <div class="ov-sub mb-0">
        <#if isDeleted>
            <@s.text name="manage.overview.sharing.deleted"/>
        <#else>
            <b><@s.text name="resource.status.${resource.status?lower_case}"/></b> ·
            <#if resource.status == "PRIVATE" && resource.makePublicDate?has_content>
                <@s.text name="manage.overview.sharing.desc.privateScheduled"><@s.param>${resource.makePublicDate?datetime?string.long_short}</@s.param></@s.text>
            <#else>
                <@s.text name="manage.overview.sharing.desc.${resource.status?lower_case}"/>
            </#if>
            <#if isStatusPending>
                <span class="ov-pending">
                    <@s.text name="manage.overview.sharing.pending"><@s.param><@s.text name="resource.status.${resource.pendingStatus?lower_case}"/></@s.param></@s.text>
                    <button id="cancelVisibilityChange" class="ov-link-btn ms-1" type="button"><@s.text name="manage.overview.sharing.cancelChange"/></button>
                </span>
            </#if>
        </#if>
    </div>

    <#if resource.status == "PUBLIC" && !isDeleted && !isStatusPending>
        <#-- always collapsed: staying public without registering is fine. The greyed-out Register link opens it -->
        <#assign failedChecks = registerChecks?filter(c -> !c.ok)?size>
        <details class="ov-checks-box" id="registration-checks">
            <summary>
                <#if failedChecks == 0>
                    <@icon name="check_circle" class="text-gbif-primary"/> <@s.text name="manage.overview.sharing.checks.ready"/>
                <#else>
                    <@s.text name="manage.overview.sharing.checks.missing"><@s.param>${failedChecks}</@s.param><@s.param>${registerChecks?size}</@s.param></@s.text>
                </#if>
            </summary>
        <ul class="ov-checks">
            <#-- failed checks first: they take the full width for their explanation -->
            <#list registerChecks?filter(c -> !c.ok) + registerChecks?filter(c -> c.ok) as c>
                <li class="${c.ok?then('ok', 'no')}">
                    <@icon name=c.ok?then("check", "close")/>
                    <span>
                        ${c.label}
                        <#if !c.ok>
                            <span class="ov-small">${c.text}<#if c.href?has_content> <a href="${c.href}">${c.linkText} →</a></#if></span>
                        </#if>
                    </span>
                </li>
            </#list>
        </ul>
        </details>
    </#if>

    <#if resource.status == "REGISTERED" && !isDeleted>
        <div class="ov-rows mt-2">
            <#if resource.key??>
                <div class="ov-r">
                    <span><@s.text name="manage.overview.sharing.gbifDataset"/></span>
                    <span><a href="${cfg.portalUrl}/dataset/${resource.key}" title="<@s.text name='manage.overview.registration.view'/>">${resource.key?string?substring(0, 8)}… <@icon name="open_in_new"/></a></span>
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
                                <a data-post href="resource-deleteNetwork.do?r=${resource.shortname}&id=${n.key!}" class="text-muted" title="<@s.text name='button.delete'/>"><@icon name="close"/></a></span>
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
