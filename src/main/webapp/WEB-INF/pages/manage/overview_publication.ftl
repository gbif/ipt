<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.OverviewAction" -->
<#-- Publication and Resource managers sections of the resource overview. Needs overview_status.ftl -->

<#function initials name>
    <#local parts = name?trim?split(" ")?filter(p -> p?has_content)>
    <#if !parts?has_content><#return "?"></#if>
    <#return (parts[0][0] + (parts?size > 1)?then(parts?last[0], ""))?upper_case>
</#function>

<#macro visibleTo status>
    <#if status == "PRIVATE"><@s.text name="manage.overview.publication.managersOnly"/><#else><@s.text name="manage.overview.publication.everyone"/></#if>
</#macro>

<#-- shows or hides the report of the last publication run (#dwcaReport) -->
<#macro reportToggle class="">
    <#if report??>
        <a class="ov-toggle-report ${class}" href="#anchor-publish"><span class="ov-toggle-report-text"><@s.text name="manage.overview.published.report.show"/></span></a>
    </#if>
</#macro>
<#assign hasPublicationLog = isPublished && !isMetadataOnly>
<#assign hasValidatorLink = !isDataPackage && (resource.recordsPublished > 0)>

<#-- visibility of the published and of the next version; licenses are worked out in overview_status.ftl -->
<#if isPublished>
    <#assign currentVisibleTo><@visibleTo resource.getLastPublishedVersionsPublicationStatus()?string/></#assign>
</#if>
<#assign nextVisibleTo><@visibleTo nextVisibility/></#assign>

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
        <#-- the one status of this card -->
        <#if isDeleted>
            <span class="ov-pill bad"><@s.text name="resource.status.deleted"/></span>
        <#elseif publishBlockers?has_content>
            <span class="ov-pill bad"><@s.text name="manage.overview.publication.blocked"/></span>
        <#elseif lastPublicationFailed>
            <span class="ov-pill bad"><@s.text name="manage.overview.publication.failed"/></span>
        <#elseif !isPublished>
            <span class="ov-pill warn"><@s.text name="manage.overview.publication.notPublished"/></span>
        <#elseif hasChanges>
            <span class="ov-pill warn"><@s.text name="manage.overview.publication.changes"/></span>
        <#else>
            <span class="ov-pill ok"><@s.text name="manage.overview.publication.upToDate"/></span>
        </#if>
        <span class="ov-sp"></span>
        <a id="edit-publication-button" class="ov-hact" href="publication-settings.do?r=${resource.shortname}"><@icon name="edit"/> <@s.text name="manage.overview.publication.settings"/></a>
        <#if hasPublicationLog || report?? || validationReportAvailable || hasValidatorLink>
            <div class="dropdown">
                <button class="ov-hmenu" type="button" id="publication-menu" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='manage.overview.publication.actions'/>" aria-label="<@s.text name='manage.overview.publication.actions'/>">
                    <@icon name="more_horiz"/>
                </button>
                <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="publication-menu">
                    <#if hasPublicationLog>
                        <li><a class="dropdown-item" href="${baseURL}/publicationlog.do?r=${resource.shortname}"><@s.text name="portal.publication.log"/></a></li>
                    </#if>
                    <#if report??>
                        <li><@reportToggle class="dropdown-item"/></li>
                    </#if>
                    <#if validationReportAvailable>
                        <li><a class="dropdown-item" href="${baseURL}/manage/validationReport.do?r=${resource.shortname}"><@s.text name="manage.overview.menu.validationReport"/></a></li>
                    </#if>
                    <#if hasValidatorLink>
                        <li><a class="dropdown-item" href="https://tools.gbif.org/dwca-validator/?archiveUrl=${baseURL}/archive.do?r=${resource.shortname}" target="_blank" rel="noopener"><@s.text name="manage.overview.menu.validator"/> <@icon name="open_in_new"/></a></li>
                    </#if>
                </ul>
            </div>
        </#if>
    </div>

    <#-- the publication settings in one line: organization, how it's published, DOIs -->
    <div class="ov-sub">
        <#if (resource.organisation.name)?has_content>
            ${resource.organisation.name}
        <#else>
            <a class="ov-missing" href="publication-settings.do?r=${resource.shortname}"><@s.text name="manage.overview.publication.noOrganisation"/></a>
        </#if>
        <#if !isDeleted>
            ·
            <#if usesAutoPublishing && autoPublicationSuspendedForThisResource>
                <span class="ov-missing"><@s.text name="manage.overview.publication.autoSuspended"/></span>
            <#elseif usesAutoPublishing>
                <@s.text name="manage.overview.publication.auto"><@s.param>${(autoPublishFrequencies.get(resource.updateFrequency.identifier)!"")?lower_case}</@s.param></@s.text>
            <#else>
                <@s.text name="manage.overview.publication.byHand"/>
            </#if>
        </#if>
        <#if displayDoiFunctionality>
            · <@s.text name="manage.overview.publication.doiVia"><@s.param>${organisationWithPrimaryDoiAccount.doiRegistrationAgency}</@s.param></@s.text>
        </#if>
    </div>

    <#if lastPublicationFailed && !isDeleted>
        <div class="ov-failed" role="status">
            <@icon name="error"/>
            <span>${report.state} <span class="ov-small d-inline">· ${report.timestamp?number_to_datetime?string.medium}</span> · <@reportToggle/></span>
        </div>
    </#if>

    <#-- the next version only when there's something to publish, or nothing is published yet -->
    <#assign nothingNew = isPublished && !hasChanges>
    <#assign showNext = !isDeleted && !nothingNew>
    <div class="ov-vers">
        <#-- current version -->
        <#if isPublished>
            <div class="ov-ver">
                <h3><@s.text name="manage.overview.publication.version"><@s.param>${currentVersion}</@s.param></@s.text></h3>
                <dl class="ov-kv">
                    <dt><@s.text name="manage.overview.published.released"/></dt>
                    <dd>${resource.lastPublished?datetime?string.medium}</dd>
                    <dt><@s.text name="manage.overview.publication.records"/></dt>
                    <dd><#if isMetadataOnly><@s.text name="manage.overview.publication.metadataOnly"/><#else>${resource.recordsPublished?string.number}</#if></dd>
                    <dt><@s.text name="manage.overview.publication.visibleTo"/></dt>
                    <dd>${currentVisibleTo}</dd>
                    <dt><@s.text name="eml.intellectualRights.license"/></dt>
                    <dd>${currentLicense}</dd>
                    <#if resource.isAlreadyAssignedDoi()>
                        <dt>DOI</dt>
                        <dd>${resource.versionHistory[0].doi!}</dd>
                    </#if>
                    <#if nothingNew && usesAutoPublishing && !autoPublicationSuspendedForThisResource && resource.nextPublished??>
                        <dt><@s.text name="manage.overview.publication.scheduled"/></dt>
                        <dd>${resource.nextPublished?datetime?string.medium}</dd>
                    </#if>
                </dl>
            </div>
        <#else>
            <div class="ov-ver text-muted">
                <h3 class="text-body"><@s.text name="manage.overview.publication.noVersion.title"/></h3>
                <@s.text name="manage.overview.publication.noVersion.text"/>
            </div>
        </#if>

        <#if showNext>
            <div class="ov-arr" aria-hidden="true"><@icon name="arrow_forward"/></div>

            <#-- next version: what changes -->
            <div class="ov-ver next<#if !canPublish> blocked</#if>">
                <h3><@s.text name="manage.overview.publication.version"><@s.param>${nextVersion}</@s.param></@s.text></h3>
                <#-- still shows what's changed, the reasons are in the banner at the top -->
                <#if !canPublish>
                    <div class="ov-blocked-note"><@s.text name="manage.overview.publication.blockedNote"/> <a href="#overview-status" class="ov-see-blockers"><@s.text name="manage.overview.publication.seeWhy"/> ↑</a></div>
                </#if>
                <#if isPublished>
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
                    <#-- a visibility change is in the list of changes -->
                    <#if !isPublished>
                        <dt><@s.text name="manage.overview.publication.visibleTo"/></dt>
                        <dd>${nextVisibleTo}</dd>
                    </#if>
                    <#-- a license change is in the list of changes -->
                    <#if !isPublished>
                        <dt><@s.text name="eml.intellectualRights.license"/></dt>
                        <dd>${nextLicense}</dd>
                    </#if>
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
