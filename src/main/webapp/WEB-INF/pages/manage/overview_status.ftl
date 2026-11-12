<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.OverviewAction" -->
<#--
  What the resource overview shows up front, worked out once and used by the banner, the sidebar and the sections:
  - publishBlockers: why the next version can't be published (mirrors macros/manage/publish.ftl and #publication-modal)
  - attention: everything that needs a look, worst first
  - section statuses: the dots in "On this page"
-->
<#assign isMetadataOnly = resource.coreType?has_content && resource.coreType == metadataType>
<#assign isDwcDp = resource.isDwcDp()>
<#assign isColDp = resource.coreType?has_content && resource.coreType == "coldp">
<#assign isEmlBased = !isDataPackage || isDwcDp>
<#assign isDeleted = resource.status == "DELETED">
<#assign isPublished = resource.lastPublished??>
<#assign lastPublishedMs = isPublished?then(resource.lastPublished?long, 0)>
<#assign currentVersion = isDataPackage?then((resource.metadataVersion)!"", (resource.emlVersion.toPlainString())!"")>
<#assign nextVersion = resource.nextVersionPlainString>
<#assign autoPublicationSuspendedForThisResource = action.hasMaxProcessFailures()>
<#assign autoPublicationEnabledForIPT = cfg.autoPublishingEnabled/>
<#assign usesAutoPublishing = resource.usesAutoPublishing() && autoPublicationEnabledForIPT>
<#assign displayDoiFunctionality = (organisationWithPrimaryDoiAccount.key)?has_content && (resource.organisation.key)?has_content
    && (organisationWithPrimaryDoiAccount.key == resource.organisation.key || currentUser.role == "Admin" || resourceOrganisationAssociatedWithDoiAgency)
    && currentUser.hasRegistrationRights()>
<#assign isStatusPending = resource.pendingStatus?has_content && resource.status != resource.pendingStatus>
<#assign nextVisibility = isStatusPending?then(resource.pendingStatus, resource.status)>

<#-- sources and how they're used -->
<#function isSourceChanged src>
    <#return isPublished && (src.lastModified)?? && (src.lastModified?long > lastPublishedMs)>
</#function>
<#function isMappingChanged m>
    <#return isPublished && (m.lastModified)?? && (m.lastModified?long > lastPublishedMs)>
</#function>
<#-- a data package mapping needs review when it was made against another schema version, or its table or fields are gone -->
<#function dataPackageMappingHasIssues m>
    <#return ((m.version!"") != ((m.dataPackageSchema.version)!"")) || m.tableSchemaMissing || m.unresolvedFieldNames?has_content>
</#function>

<#assign mappedSourceNames = []>
<#if isDataPackage>
    <#list resource.dataPackageMappings! as m>
        <#if (m.source.name)??><#assign mappedSourceNames = mappedSourceNames + [m.source.name]></#if>
    </#list>
<#else>
    <#list resource.mappings! as m>
        <#if (m.source.name)??><#assign mappedSourceNames = mappedSourceNames + [m.source.name]></#if>
    </#list>
</#if>
<#assign unmappedSources = resource.sources?filter(src -> !mappedSourceNames?seq_contains(src.name))>
<#assign unreadableSources = resource.sources?filter(src -> !src.readable)>
<#assign changedSources = resource.sources?filter(src -> isSourceChanged(src))>
<#-- mapped URL and SQL sources are read again at each publication: changes in their data can't be seen from dates -->
<#assign liveSources = resource.sources?filter(src -> (src.isUrlSource() || src.isSqlSource()) && mappedSourceNames?seq_contains(src.name))>
<#assign liveSourceNames = liveSources?map(src -> src.name)>
<#-- names a few, counts many -->
<#assign liveSourcesNote><#if (liveSources?size > 3)><@s.text name="manage.overview.data.liveSourcesCount"><@s.param>${liveSources?size}</@s.param></@s.text><#elseif liveSources?has_content><@s.text name="manage.overview.data.liveSources"><@s.param><#list liveSources as src>"${src.name}"<#sep>, </#sep></#list></@s.param></@s.text></#if></#assign>

<#-- Darwin Core mappings as rows: core first, then extensions. "mid" is the mapping's index within its row type, as mapping.do expects -->
<#assign classRows = []>
<#if !isDataPackage>
    <#list resource.getMappedExtensions() as ext>
        <#list resource.getMappings(ext.rowType) as m>
            <#assign classRows = classRows + [{"m": m, "mid": m_index, "ext": ext, "core": ext.rowType == (resource.coreRowType)!""}]>
        </#list>
    </#list>
    <#assign classRows = classRows?filter(r -> r.core) + classRows?filter(r -> !r.core)>
</#if>
<#assign emptyMappings = classRows?filter(r -> r.m.fields?size == 0)>
<#assign hasCoreMapping = classRows?filter(r -> r.core)?has_content>
<#assign dataPackageMappingsWithIssues = (isDataPackage)?then(resource.dataPackageMappings!?filter(m -> dataPackageMappingHasIssues(m)), [])>

<#-- why the next version can't be published -->
<#assign publishBlockers = []>
<#macro blocker key short href="" linkKey="" linkClass="" detail="">
    <#local text><@s.text name=key/></#local>
    <#local shortText><@s.text name=short/></#local>
    <#local linkText><#if linkKey?has_content><@s.text name=linkKey/></#if></#local>
    <#assign publishBlockers = publishBlockers + [{"text": text, "short": shortText, "href": href, "linkText": linkText, "linkClass": linkClass, "detail": detail}]>
</#macro>
<#if !isDeleted>
    <#if missingBasicMetadata>
        <@blocker key="manage.overview.published.missing.metadata" short="manage.overview.attention.basicMetadata"
            href="metadata-basic.do?r=${resource.shortname}" linkKey="submenu.basic"/>
    <#elseif !validMetadata>
        <@blocker key="manage.overview.published.metadata.invalid" short="manage.overview.attention.invalidMetadata"
            href="#" linkKey="manage.overview.metadata.modal.validation.report" linkClass="show-metadata-validation-result"/>
    </#if>
    <#if !isDataPackage && !resource.organisation?has_content>
        <@blocker key="manage.overview.published.missing.organisation" short="manage.overview.attention.noOrganisation"
            href="publication-settings.do?r=${resource.shortname}" linkKey="manage.overview.publication.settings"/>
    </#if>
    <#if isDataPackage && dataPackageMappingsMissing>
        <@blocker key="manage.overview.published.missing.mappings" short="manage.overview.attention.noMappings"
            href="#anchor-data" linkKey="manage.overview.go.mappings"/>
    </#if>
    <#if isDataPackage && !dataPackageMappingsMissing && !allRequiredFieldsMappedDataPackage>
        <#assign requiredFieldsDetail><#list missingRequiredFieldsDataPackage as key, value><br><strong>${key}</strong>: <#list value as field><code>${field.name}</code><#sep>, </#sep></#list></#list></#assign>
        <@blocker key="manage.overview.published.missing.requiredFields" short="manage.overview.attention.requiredFields"
            href="#anchor-data" linkKey="manage.overview.go.mappings" detail=requiredFieldsDetail/>
    </#if>
    <#if isDataPackage && dataPackageMappingsWithMissingTable?has_content>
        <#assign missingTablesDetail><#list dataPackageMappingsWithMissingTable as mappingLabel><br><code>${mappingLabel}</code></#list></#assign>
        <@blocker key="manage.overview.published.missing.tables" short="manage.overview.attention.missingTables"
            href="#anchor-data" linkKey="manage.overview.go.mappings" detail=missingTablesDetail/>
    </#if>
    <#if resource.isRegistered() && !resource.isAssignedGBIFSupportedLicense()>
        <@blocker key="manage.overview.prevented.resource.publishing.noGBIFLicense" short="manage.overview.attention.noGbifLicense"
            href="metadata-basic.do?r=${resource.shortname}" linkKey="submenu.basic"/>
    </#if>
    <#assign needsRegistrationRights = resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION"
        || (resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi())
        || resource.status == "REGISTERED">
    <#if needsRegistrationRights && !currentUser.hasRegistrationRights() && !resource.isManagedBy(currentUser)>
        <@blocker key="manage.resource.status.publication.forbidden" short="manage.overview.attention.noRights"/>
    <#elseif needsRegistrationRights && resource.isAlreadyAssignedDoi()
        && (resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION" || resource.identifierStatus == "PUBLIC")
        && !organisationWithPrimaryDoiAccount??>
        <@blocker key="manage.resource.status.publication.forbidden.account.missing" short="manage.overview.attention.noDoiAccount"/>
    </#if>
</#if>
<#assign canPublish = !isDeleted && !publishBlockers?has_content>
<#-- the last publication run failed: the report of a failed run stays until the next run or a restart -->
<#assign lastPublicationFailed = !isDeleted && report?? && report.completed && report.hasException()>

<#-- the license of the next version -->
<#macro nextLicense>
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

<#if isPublished>
    <#assign currentLicense><#if isEmlBased><@shortLicense action.getLastPublishedVersionAssignedLicense(resource)!/><#else><@nextLicense/></#if></#assign>
</#if>
<#assign nextLicense><@nextLicense/></#assign>

<#-- what changed since the last version -->
<#assign changeList = []>
<#if isPublished>
    <#list changedSources as src>
        <#assign c><@s.text name="manage.overview.changes.source"><@s.param>${src.name}</@s.param></@s.text></#assign>
        <#assign changeList = changeList + [c]>
    </#list>
    <#if mappingsModifiedSinceLastPublication>
        <#assign c><@s.text name="manage.overview.changes.mappings"/></#assign>
        <#assign changeList = changeList + [c]>
    </#if>
    <#if metadataModifiedSinceLastPublication>
        <#assign c><@s.text name="manage.overview.changes.metadata"/></#assign>
        <#assign changeList = changeList + [c]>
    </#if>
    <#if currentLicense?markup_string?trim != nextLicense?markup_string?trim>
        <#assign c><@s.text name="manage.overview.changes.license"><@s.param>${currentLicense?markup_string?trim}</@s.param><@s.param>${nextLicense?markup_string?trim}</@s.param></@s.text></#assign>
        <#assign changeList = changeList + [c]>
    </#if>
    <#if isStatusPending>
        <#assign c><@s.text name="manage.overview.changes.visibility"><@s.param><@s.text name="resource.status.${resource.status?lower_case}"/></@s.param><@s.param><@s.text name="resource.status.${resource.pendingStatus?lower_case}"/></@s.param></@s.text></#assign>
        <#assign changeList = changeList + [c]>
    </#if>
</#if>
<#-- sources changed but no single source shows as changed (e.g. a source was deleted) -->
<#if isPublished && sourcesModifiedSinceLastPublication && !changedSources?has_content>
    <#assign c><@s.text name="manage.overview.changes.sources"/></#assign>
    <#assign changeList = [c] + changeList>
</#if>
<#assign hasChanges = changeList?has_content>

<#-- needs attention: [level, text] -->
<#assign attention = []>
<#if !isMetadataOnly && !isDeleted>
    <#if !resource.sources?has_content>
        <#assign t><@s.text name="manage.overview.attention.noSources"/></#assign>
        <#assign attention = attention + [["warn", t]]>
    </#if>
    <#list unreadableSources as src>
        <#assign t><@s.text name="manage.overview.attention.unreadableSource"><@s.param>${src.name}</@s.param></@s.text></#assign>
        <#assign attention = attention + [["bad", t]]>
    </#list>
    <#if unmappedSources?has_content>
        <#assign t><@s.text name="manage.overview.attention.unmappedSources"><@s.param>${unmappedSources?size}</@s.param></@s.text></#assign>
        <#assign attention = attention + [["warn", t]]>
    </#if>
    <#if !isDataPackage && resource.sources?has_content && !hasCoreMapping>
        <#assign t><@s.text name="manage.overview.attention.noCoreMapping"/></#assign>
        <#assign attention = attention + [["warn", t]]>
    </#if>
    <#list emptyMappings as r>
        <#assign t><@s.text name="manage.overview.attention.emptyMapping"><@s.param>${r.ext.title}</@s.param></@s.text></#assign>
        <#assign attention = attention + [["warn", t]]>
    </#list>
    <#if dataPackageMappingsWithIssues?has_content>
        <#assign t><@s.text name="manage.overview.attention.mappingIssues"><@s.param>${dataPackageMappingsWithIssues?size}</@s.param></@s.text></#assign>
        <#assign attention = attention + [["warn", t]]>
    </#if>
    <#if outdatedExtensions>
        <#assign t><@s.text name="manage.overview.attention.outdatedExtensions"/></#assign>
        <#assign attention = attention + [["warn", t]]>
    </#if>
</#if>
<#list publishBlockers as b>
    <#assign attention = attention + [["bad", b.short]]>
</#list>
<#if !isDeleted && usesAutoPublishing && autoPublicationSuspendedForThisResource>
    <#assign t><@s.text name="manage.overview.attention.autopublishSuspended"/></#assign>
    <#assign attention = attention + [["bad", t]]>
</#if>
<#if !isPublished && canPublish>
    <#assign t><@s.text name="manage.overview.attention.notPublished"/></#assign>
    <#assign attention = attention + [["warn", t]]>
</#if>
<#if lastPublicationFailed>
    <#assign t><@s.text name="manage.overview.publication.failed"/></#assign>
    <#assign attention = attention + [["bad", t]]>
</#if>
<#if hasChanges && canPublish>
    <#assign t><@s.text name="manage.overview.attention.unpublishedChanges"/></#assign>
    <#assign attention = attention + [["warn", t]]>
</#if>
<#if isDwcDp && resource.status == "REGISTERED" && !resource.dwcDpDatasetType??>
    <#assign t><@s.text name="manage.overview.attention.datasetTypeMissing"/></#assign>
    <#assign attention = attention + [["warn", t]]>
</#if>
<#assign attention = attention?filter(a -> a[0] == "bad") + attention?filter(a -> a[0] == "warn")>

<#-- section statuses -->
<#if !resource.sources?has_content>
    <#assign dataStatus = "off">
<#elseif unreadableSources?has_content || (isDataPackage && (dataPackageMappingsMissing || !allRequiredFieldsMappedDataPackage || dataPackageMappingsWithMissingTable?has_content))>
    <#assign dataStatus = "bad">
<#elseif unmappedSources?has_content || emptyMappings?has_content || dataPackageMappingsWithIssues?has_content || (!isDataPackage && !hasCoreMapping)>
    <#assign dataStatus = "warn">
<#else>
    <#assign dataStatus = "ok">
</#if>
<#assign metadataStatus = (missingBasicMetadata || !validMetadata)?then("bad", "ok")>
<#-- private, public and registered are all fine; grey only marks a resource nobody else can see -->
<#if resource.status == "REGISTERED" || resource.status == "PUBLIC">
    <#assign sharingStatus = "ok">
<#else>
    <#assign sharingStatus = "off">
</#if>
<#if isDeleted>
    <#assign publicationStatus = "off">
<#elseif publishBlockers?has_content || lastPublicationFailed || (usesAutoPublishing && autoPublicationSuspendedForThisResource)>
    <#assign publicationStatus = "bad">
<#elseif !isPublished || hasChanges>
    <#assign publicationStatus = "warn">
<#else>
    <#assign publicationStatus = "ok">
</#if>
