<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.OverviewAction" -->
<#-- Publication and Resource managers sections of the resource overview. Needs overview_status.ftl -->

<#-- the licence of the next version -->
<#macro nextLicence>
    <#if isEmlBased>
        <#if (resource.eml.parseLicenseUrl())?has_content><@shortLicense resource.eml.parseLicenseUrl()/><#else><@s.text name="manage.overview.published.licenseNotSet"/></#if>
    <#elseif !(resource.dataPackageMetadata.licenses)?has_content && !(resource.dataPackageMetadata.license)?has_content>
        <@s.text name="manage.overview.published.licenseNotSet"/>
    <#elseif resource.coreType == "camtrap-dp">
        <#list resource.dataPackageMetadata.licenses as license><#if license.scope?? && license.scope == "data">${(license.name)!}</#if></#list>
    <#elseif isColDp>
        ${(resource.dataPackageMetadata.license)!}
    </#if>
</#macro>

<#function initials name>
    <#local parts = name?trim?split(" ")?filter(p -> p?has_content)>
    <#if !parts?has_content><#return "?"></#if>
    <#return (parts[0][0] + (parts?size > 1)?then(parts?last[0], ""))?upper_case>
</#function>

<#macro visibleTo status>
    <#if status == "PRIVATE"><@s.text name="manage.overview.publication.managersOnly"/><#else><@s.text name="manage.overview.publication.everyone"/></#if>
</#macro>

<span class="anchor anchor-overview-page" id="anchor-publish"></span>
<section class="ov-card section" id="publish">
    <div class="ov-ch">
        <h2>
            <#assign overviewTitleInfo>
                <#if isMetadataOnly>
                    <@s.text name="manage.overview.published.description.metadataOnly"/>
                <#elseif isDataPackage>
                    <@s.text name="manage.overview.published.description.dp"/>
                <#else>
                    <@s.text name="manage.overview.published.description"/>
                </#if>
                <br/><br/>
                <#if displayDoiFunctionality>
                    <@s.text name='manage.overview.published.description.doiAccount'><@s.param>${organisationWithPrimaryDoiAccount.doiRegistrationAgency}</@s.param><@s.param>${organisationWithPrimaryDoiAccount.name}</@s.param><@s.param>${organisationWithPrimaryDoiAccount.doiPrefix}</@s.param></@s.text>
                <#else>
                    <@s.text name="manage.overview.published.description.noDoiAccount"/>
                </#if>
            </#assign>
            <@popoverTextInfo overviewTitleInfo/>
            <@s.text name='manage.overview.published'/>
        </h2>
        <#if isDeleted>
            <span class="ov-pill bad"><@s.text name="resource.status.deleted"/></span>
        <#elseif publishBlockers?has_content>
            <span class="ov-pill bad"><@s.text name="manage.overview.publication.blocked"/></span>
        <#elseif !isPublished>
            <span class="ov-pill warn"><@s.text name="manage.overview.publication.notPublished"/></span>
        <#elseif hasChanges>
            <span class="ov-pill warn"><@s.text name="manage.overview.publication.changes"/></span>
        <#else>
            <span class="ov-pill ok"><@s.text name="manage.overview.publication.upToDate"/></span>
        </#if>
        <span class="ov-sp"></span>
        <a id="edit-publication-button" class="ov-hact" href="publication-settings.do?r=${resource.shortname}"><@icon name="edit"/> <@s.text name="manage.overview.publication.settings"/></a>
    </div>

    <#-- the publishing organization is set in the publication settings -->
    <div class="ov-autop mt-2">
        <span class="text-muted"><@s.text name="eml.publishingOrganisation"/>:</span>
        <#if (resource.organisation.name)?has_content>
            <span>${resource.organisation.name}</span>
        <#else>
            <a class="ov-missing" href="publication-settings.do?r=${resource.shortname}"><@s.text name="manage.overview.metadata.notSelected"/></a>
        </#if>
    </div>
    <div class="ov-autop">
        <#if usesAutoPublishing>
            <#if autoPublicationSuspendedForThisResource>
                <span class="ov-pill bad"><@s.text name="manage.overview.autopublish.title"/>: <@s.text name="manage.overview.autopublish.suspended"/></span>
                <span class="text-gbif-danger"><@s.text name="manage.overview.autopublish.intro.suspended"/></span>
            <#else>
                <span class="ov-pill ok"><@s.text name="manage.overview.autopublish.title"/> · ${autoPublishFrequencies.get(resource.updateFrequency.identifier)!}</span>
            </#if>
        <#elseif !isDeleted>
            <span class="ov-pill grey"><@s.text name="manage.overview.publication.byHand"/></span>
            <#if autoPublicationEnabledForIPT>
                <a href="publication-settings.do?r=${resource.shortname}"><@s.text name="manage.overview.publication.setUpAuto"/></a>
            </#if>
        </#if>
        <#if displayDoiFunctionality>
            <span class="ov-pill grey"><@s.text name="manage.overview.publication.doiVia"><@s.param>${organisationWithPrimaryDoiAccount.doiRegistrationAgency}</@s.param></@s.text></span>
        </#if>
    </div>

    <div class="ov-vers"<#if isDeleted> style="grid-template-columns: 1fr;"</#if>>
        <#-- current version -->
        <#if isPublished>
            <div class="ov-ver">
                <h3><@s.text name="manage.overview.publication.version"><@s.param>${currentVersion}</@s.param></@s.text>
                    <span class="ov-pill ${isDeleted?then('bad', 'ok')}"><#if isDeleted><@s.text name="resource.status.deleted"/><#else><@s.text name="manage.overview.published.last.publication.intro"/></#if></span></h3>
                <dl class="ov-kv">
                    <dt><@s.text name="manage.overview.published.released"/></dt>
                    <dd>${resource.lastPublished?datetime?string.medium}</dd>
                    <dt><@s.text name="manage.overview.publication.records"/></dt>
                    <dd><#if isMetadataOnly><@s.text name="manage.overview.publication.metadataOnly"/><#else>${resource.recordsPublished?string.number}</#if></dd>
                    <dt><@s.text name="manage.overview.publication.visibleTo"/></dt>
                    <dd><@visibleTo resource.getLastPublishedVersionsPublicationStatus()?string/></dd>
                    <dt><@s.text name="eml.intellectualRights.license"/></dt>
                    <dd>
                        <#if isEmlBased>
                            <@shortLicense action.getLastPublishedVersionAssignedLicense(resource)!/>
                        <#else>
                            <@nextLicence/>
                        </#if>
                    </dd>
                    <#if resource.isAlreadyAssignedDoi()>
                        <dt>DOI</dt>
                        <dd>${resource.versionHistory[0].doi!}</dd>
                    </#if>
                </dl>
                <div class="ov-links">
                    <a href="${baseURL}/resource?r=${resource.shortname}"><@s.text name="button.view"/></a>
                    <#if !isMetadataOnly>
                        <a href="${baseURL}/publicationlog.do?r=${resource.shortname}"><@s.text name="portal.publication.log"/></a>
                    </#if>
                    <#if report??>
                        <a id="toggleReport" href="#anchor-publish"><span id="toggleReport-text"><@s.text name="manage.overview.published.report.show"/></span></a>
                    </#if>
                </div>
            </div>
        <#else>
            <div class="ov-ver text-muted">
                <h3 class="text-body"><@s.text name="manage.overview.publication.noVersion.title"/></h3>
                <@s.text name="manage.overview.publication.noVersion.text"/>
            </div>
        </#if>

        <#if !isDeleted>
            <div class="ov-arr" aria-hidden="true"><@icon name="arrow_forward"/></div>

            <#-- next version -->
            <#assign nothingNew = isPublished && !hasChanges>
            <div class="ov-ver next<#if nothingNew && canPublish> same</#if>">
                <h3><@s.text name="manage.overview.publication.version"><@s.param>${nextVersion}</@s.param></@s.text>
                    <#if !canPublish>
                        <span class="ov-pill bad"><@s.text name="manage.overview.publication.blocked"/></span>
                    <#elseif nothingNew>
                        <span class="ov-pill grey"><@s.text name="manage.overview.publication.nothingNew"/></span>
                    <#else>
                        <span class="ov-pill warn"><@s.text name="manage.overview.published.next.publication.intro"/></span>
                    </#if>
                </h3>
                <#if nothingNew>
                    <div class="text-smaller text-muted"><@s.text name="manage.overview.publication.sameText"><@s.param>${currentVersion}</@s.param><@s.param>${nextVersion}</@s.param></@s.text></div>
                <#elseif isPublished>
                    <div class="text-smaller"><@s.text name="manage.overview.publication.changesSince"><@s.param>${currentVersion}</@s.param></@s.text></div>
                    <ul class="ov-chlist"><#list changeList as c><li>${c}</li></#list></ul>
                <#else>
                    <div class="text-smaller"><@s.text name="manage.overview.publication.willInclude"/></div>
                    <ul class="ov-chlist">
                        <#if !isMetadataOnly>
                            <li><@s.text name="manage.overview.publication.includesData"><@s.param>${resource.sources?size}</@s.param><@s.param>${isDataPackage?then((resource.dataPackageMappings![])?size, classRows?size)}</@s.param></@s.text></li>
                        </#if>
                        <li><@s.text name="manage.overview.metadata"/></li>
                    </ul>
                </#if>
                <dl class="ov-kv">
                    <dt><@s.text name="manage.overview.publication.visibleTo"/></dt>
                    <dd><@visibleTo nextVisibility/></dd>
                    <dt><@s.text name="eml.intellectualRights.license"/></dt>
                    <dd><@nextLicence/></dd>
                    <#if resource.doi?? && resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION">
                        <dt>DOI</dt>
                        <dd>${resource.doi} <span class="ov-small d-inline"><@s.text name="manage.overview.publication.doiReserved"/></span></dd>
                    </#if>
                    <#if usesAutoPublishing && !autoPublicationSuspendedForThisResource && resource.nextPublished??>
                        <dt><@s.text name="manage.overview.publication.scheduled"/></dt>
                        <dd>${resource.nextPublished?datetime?string.medium}</dd>
                    </#if>
                </dl>
                <#if validMetadata>
                    <div class="ov-links"><a href="${baseURL}/resource/preview?r=${resource.shortname}"><@s.text name="button.preview"/></a></div>
                </#if>
            </div>
        </#if>
    </div>

    <#if report??>
        <div id="dwcaReport" class="mt-3" style="display: none;">
            <div class="report text-smaller">
                <ul class="simple list-unstyled">
                    <#list report.messages as msg>
                        <li class="${msg.level}"><span class="small">${msg.date?time?string}</span> ${msg.message}</li>
                    </#list>
                </ul>
                <#if cfg.debug() && report.hasException()>
                    <ul class="simple">
                        <li><strong>Exception</strong> ${report.exceptionMessage!}</li>
                        <#list report.exceptionStacktrace as msg>
                            <li>${msg}</li>
                        </#list>
                    </ul>
                </#if>
            </div>
        </div>
    </#if>
</section>

<span class="anchor anchor-overview-page" id="anchor-managers"></span>
<section class="ov-card section" id="managers">
    <div class="ov-ch">
        <h2>
            <@popoverPropertyInfo "manage.overview.resource.managers.description"/>
            <@s.text name="manage.overview.resource.managers"/>
        </h2>
        <span class="ov-pill grey">${resource.managers?size + 1}</span>
        <span class="ov-sp"></span>
        <a id="add-manager-button" class="ov-hact" href="#"><@icon name="add"/> <@s.text name="manage.overview.managers.add"/></a>
    </div>
    <div class="ov-sub"><@s.text name="manage.overview.resource.managers.intro"><@s.param>${resource.shortname}</@s.param></@s.text></div>

    <div class="ov-people">
        <div class="ov-person">
            <span class="ov-av" aria-hidden="true">${initials((resource.creator.name)!"")}</span>
            <div class="ov-grow">
                <b>${(resource.creator.name)!}</b>
                <span class="ov-small"><@s.text name="manage.overview.resource.managers.creator"/> · ${(resource.creator.email)!}</span>
            </div>
        </div>
        <#list resource.managers as u>
            <div class="ov-person">
                <span class="ov-av" aria-hidden="true">${initials(u.name!"")}</span>
                <div class="ov-grow">
                    <b>${u.name}</b>
                    <span class="ov-small"><@s.text name="manage.overview.resource.managers.manager"/> · ${u.email}</span>
                </div>
                <a class="text-gbif-danger text-smaller" data-post href="resource-deleteManager.do?r=${resource.shortname}&id=${u.email?url}"><@s.text name="manage.overview.managers.remove"/></a>
            </div>
        </#list>
    </div>
</section>
