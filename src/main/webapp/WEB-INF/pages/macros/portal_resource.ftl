<#-- Shared parts of the public resource pages (portal/resource_*.ftl), styled by styles/portal-resource.css -->

<#-- whether ccLicense can show this licence code -->
<#function isCcLicense code>
    <#local c = code?upper_case?trim/>
    <#return c?matches("CC0-1\\.0") || c?matches("CC-BY(-NC)?(-ND|-SA)?-[0-9.]+")/>
</#function>

<#-- the ccLicense code of a GBIF licence (CC_BY_4_0, CC_BY_NC_4_0, CC0_1_0), "" for any other -->
<#function gbifLicenseToCc gbifCode>
    <#return {"CC_BY_NC_4_0": "CC-BY-NC-4.0", "CC_BY_4_0": "CC-BY-4.0", "CC0_1_0": "CC0-1.0"}[gbifCode!""]!""/>
</#function>

<#-- A Creative Commons licence as the CC icons (images/icons/cc, from the CC press kit) with its short name,
     linking to the licence deed. code: SPDX-style identifier, e.g. "CC-BY-4.0", "CC-BY-NC-SA-4.0", "CC0-1.0";
     anything else renders nothing. -->
<#macro ccLicense code>
    <#local ccCode = code?upper_case?trim/>
    <#if ccCode?matches("CC0-1\\.0")>
        <#local ccIconNames = ["cc", "zero"]/>
        <#local ccLabel = "CC0 1.0"/>
        <#local ccDeed = "https://creativecommons.org/publicdomain/zero/1.0/"/>
    <#elseif ccCode?matches("CC-BY(-NC)?(-ND|-SA)?-[0-9.]+")>
        <#local ccParts = ccCode?keep_after("CC-")?split("-")/>
        <#local ccVersion = ccParts?last/>
        <#local ccTerms = ccParts[0..<(ccParts?size - 1)]?map(t -> t?lower_case)?sequence/>
        <#local ccIconNames = ["cc"] + ccTerms/>
        <#local ccLabel = "CC " + ccTerms?map(t -> t?upper_case)?sequence?join("-") + " " + ccVersion/>
        <#local ccDeed = "https://creativecommons.org/licenses/" + ccTerms?join("-") + "/" + ccVersion + "/"/>
    <#else>
        <#return>
    </#if>
    <a rel="license" class="rp-license-icons" href="${ccDeed}" title="${ccLabel}">
        <span class="rp-cc-icons"><#list ccIconNames as i><img src="${baseURL}/images/icons/cc/${i}.svg" alt=""></#list></span>
        <span>${ccLabel}</span>
    </a>
</#macro>

<#-- On a preview page: a notice fixed under the navbar, with the way back to the resource overview.
     Fixed, not sticky: body is only one screen tall (h-100), which ends a sticky element after the first screen.
     The spacer keeps its place in the page. nextVersion: the version being previewed -->
<#macro previewNotice nextVersion>
    <#if isPreviewPage!false>
        <div class="rp-preview-spacer"></div>
        <div class="rp-preview-sticky"><div class="container px-0">
            <div class="rp-preview-notice">
                <@icon name="visibility"/>
                <span><@s.text name='portal.resource.preview.notice'><@s.param>${nextVersion}</@s.param></@s.text></span>
                <a href="${baseURL}/manage/resource.do?r=${resource.shortname}"><@s.text name='portal.resource.preview.back'/></a>
            </div>
        </div></div>
    </#if>
</#macro>

<#-- A country's name in the metadata's language (English if unknown) from its ISO code (ResourceAction.getCountryName);
     anything that isn't a known code, e.g. a name already, as given -->
<#function countryName code>
    <#return action.getCountryName(code)!code/>
</#function>

<#-- The resource logo, floated at the start of the description so the text runs beside and below it; nothing without a URL.
     A logo that fails to load removes itself. url: EML logoUrl, Camtrap DP image or ColDP logo -->
<#macro resourceLogo url="">
    <#if url?has_content>
        <div class="rp-logo"><img src="${url}" alt="" onerror="this.parentNode.remove()"></div>
    </#if>
</#macro>

<#-- The version line under the heading's type pills: "Version 2.2 (latest) · Published 11 November 2026 by <publisher>",
     on an old version "… · Latest is 2.2", on a preview "Version 2.3 (preview)".
     latestVersion: the latest published version, e.g. "2.2"
     pubDate: the publication date from the metadata (EML pubDate, Camtrap DP created), in the visitor's long format
     pubDateText: an already formatted date, used when there is no pubDate (ColDP issued may be just a year)
     The publisher is the resource's organisation, linked to GBIF.org; none for the default ("no organisation") one. -->
<#macro versionLine latestVersion pubDate="" pubDateText="">
    <#-- a preview shows the next version, which is neither published nor the latest yet: no date, publisher or "latest" -->
    <#if isPreviewPage!false>
        <div class="rp-version-line mt-2">
            <span><@s.text name='portal.resource.versionLine.versionPreview'><@s.param><#if version??>${version.toPlainString()}<#else>${latestVersion}</#if></@s.param></@s.text></span>
        </div>
        <#return>
    </#if>
    <#if !resource.lastPublished??>
        <div class="text-gbif-danger text-smaller mt-2">
            <@s.text name='portal.resource.published.never.long'/>
        </div>
        <#return>
    </#if>
    <#local shownVersion><#if version??>${version.toPlainString()}<#else>${latestVersion}</#if></#local>
    <#local shownVersion = shownVersion?markup_string/>
    <#local isLatest = shownVersion == latestVersion/>
    <#local dateHtml>
        <#if pubDate?is_date><span property="dc:issued">${pubDate?date?string.long}</span><#elseif pubDateText?has_content><span property="dc:issued">${pubDateText}</span></#if>
    </#local>
    <#local hasOrg = resource.organisation?? && !(action.getDefaultOrganisation()?? && resource.organisation.key.toString() == action.getDefaultOrganisation().key.toString())/>
    <#local orgHtml>
        <#if hasOrg><a property="dc:publisher" href="${cfg.portalUrl}/publisher/${resource.organisation.key}">${resource.organisation.name}</a></#if>
    </#local>
    <div class="rp-version-line mt-2">
        <span><#if isLatest><@s.text name='portal.resource.versionLine.versionLatest'><@s.param>${shownVersion}</@s.param></@s.text><#else><@s.text name='portal.resource.version'/>&nbsp;${shownVersion}</#if></span>
        <#if dateHtml?markup_string?trim?has_content && hasOrg>
            <span class="rp-sep">·</span>
            <span><@s.text name='portal.resource.versionLine.publishedOnBy'><@s.param>${dateHtml}</@s.param><@s.param>${orgHtml}</@s.param></@s.text></span>
        <#elseif dateHtml?markup_string?trim?has_content>
            <span class="rp-sep">·</span>
            <span><@s.text name='portal.resource.versionLine.publishedOn'><@s.param>${dateHtml}</@s.param></@s.text></span>
        <#elseif hasOrg>
            <span class="rp-sep">·</span>
            <span><@s.text name='portal.resource.versionLine.publishedBy'><@s.param>${orgHtml}</@s.param></@s.text></span>
        </#if>
        <#if !isLatest>
            <span class="rp-sep">·</span>
            <a href="${baseURL}/resource?r=${resource.shortname}"><@s.text name='portal.resource.versionLine.latestIs'><@s.param>${latestVersion}</@s.param></@s.text></a>
        </#if>
        <#if resource.status == "REGISTERED" && resource.key??>
            <span class="rp-sep">·</span>
            <a href="${cfg.portalUrl}/dataset/${resource.key}"><@s.text name='portal.resource.versionLine.viewOnGbif'/></a>
        </#if>
    </div>
</#macro>

<#-- The keywords as chips under the description; nothing without keywords. keywords: a sequence of strings -->
<#macro keywordChips keywords>
    <#local shown = keywords?filter(k -> k?has_content)/>
    <#if shown?has_content>
        <div id="keywords" class="rp-keywords" property="dc:subject" role="list" aria-label="${action.getText('portal.resource.summary.keywords')}">
            <#list shown as kw><span role="listitem">${kw}</span></#list>
        </div>
    </#if>
</#macro>

<#-- A folded block in the "More" section, a row like the contacts rows: the title, a short hint on what it holds
     (e.g. a count, or the names of the fields given) and a chevron; it opens to the nested content -->
<#macro moreRow id title hint="">
    <details id="${id}" class="rp-person-row">
        <summary class="rp-pr-head rp-more-head">
            <span class="rp-pr-name">${title}</span>
            <span class="rp-more-hint">${hint}</span>
            <@icon name="expand_more" class="rp-pr-chevron"/>
        </summary>
        <div class="rp-more-body"><#nested></div>
    </details>
</#macro>

<#-- A person or organisation in the contacts list: one row with the name (+ ORCID), organisation, roles and an email
     icon, that opens to the details given as nested content (personDetail items); without details it doesn't open.
     roleLabels: role names, the citation role first; email: the first email address, for the icon -->
<#macro personRow name orcidUrl="" org="" roleLabels=[] email="">
    <#local details><#nested></#local>
    <#local hasDetails = details?markup_string?trim?has_content/>
    <#if hasDetails><details class="rp-person-row"><summary class="rp-pr-head"><#else><div class="rp-person-row"><div class="rp-pr-head"></#if>
        <span class="rp-pr-who">
            <span class="rp-pr-name">${name}<#if orcidUrl?has_content> <a href="${orcidUrl}" target="_blank" rel="noopener" title="ORCID"><img src="${baseURL}/images/icons/orcid_16x16.gif" class="orcid-small" alt="ORCID"></a></#if></span>
            <#if org?has_content><span class="rp-pr-org">${org}</span></#if>
        </span>
        <span class="rp-pr-roles"><#list roleLabels as r>${r}<#sep>, </#sep></#list></span>
        <span class="rp-pr-actions">
            <#if email?has_content><a href="mailto:${email}" class="rp-pr-mail" title="${email}"><@icon name="mail" title=email/></a></#if>
            <#if hasDetails><@icon name="expand_more" class="rp-pr-chevron"/></#if>
        </span>
    <#if hasDetails></summary><dl class="rp-pr-details">${details}</dl></details><#else></div></div></#if>
</#macro>

<#-- One label and value in a personRow's details; the value is the nested content, left out when empty -->
<#macro personDetail label>
    <#local value><#nested></#local>
    <#if value?markup_string?trim?has_content><dt>${label}</dt><dd>${value}</dd></#if>
</#macro>

<#-- The versions section as a timeline. latestVersion: the latest published version, e.g. "2.1" -->
<#macro versionsSection latestVersion>
    <#if resource.versionHistory??>
        <#-- only released versions; private ones only for managers -->
        <#local shownVersions = []/>
        <#list resource.getVersionHistory() as v>
            <#if v.released?? && ((v.publicationStatus == 'PUBLIC' || v.publicationStatus == 'REGISTERED') || managerRights)>
                <#local shownVersions = shownVersions + [v]/>
            </#if>
        </#list>
        <#local currentVersion><#if version??>${version.toPlainString()}<#else>${latestVersion}</#if></#local>
        <#local currentVersion = currentVersion?markup_string/>
        <span class="anchor anchor-home-resource-page" id="anchor-versions"></span>
        <section id="versions" class="rp-section section">
            <h2 class="rp-h"><@s.text name='portal.resource.versions'/></h2>
            <p class="text-muted text-smaller">
                <#if managerRights><@s.text name='portal.resource.versions.list.manager'/><#else><@s.text name='portal.resource.versions.list'/></#if>
            </p>
            <#if shownVersions?has_content>
                <ol class="rp-versions list-unstyled">
                    <#list shownVersions as v>
                        <li class="rp-version<#if v.version == currentVersion> rp-version-current</#if><#if (v?index >= 5)> rp-version-more</#if>"<#if (v?index >= 5)> hidden</#if>>
                            <div class="rp-version-head">
                                <#if v.version == currentVersion>
                                    <b>${v.version}</b>
                                <#else>
                                    <a href="${baseURL}/resource?r=${resource.shortname}&amp;v=${v.version}"><b>${v.version}</b></a>
                                </#if>
                                <span>${v.released?date?string.medium}</span>
                                <span><@s.text name='portal.home.records'/>: ${v.recordsPublished}</span>
                                <#if v.doi?? && v.status == "PUBLIC"><span>DOI <a href="${v.doi.getUrl()}">${v.doi}</a></span></#if>
                                <#if v.version == latestVersion><span class="rp-pill"><@s.text name='portal.resource.latest'/></span></#if>
                                <#if managerRights && v.publicationStatus != 'PUBLIC' && v.publicationStatus != 'REGISTERED'><span class="rp-pill"><@s.text name='resource.status.private'/></span></#if>
                            </div>
                            <div class="rp-version-summary">
                                <#if v.changeSummary?has_content>${v.changeSummary}<#else><@s.text name="publishing.changeSummary.default"/></#if>
                                <#if managerRights && !isPreviewPage>
                                    <a class="ms-1" href="${baseURL}/manage/history.do?r=${resource.shortname}&amp;v=${v.version}"><@s.text name='button.edit'/></a>
                                </#if>
                                <#-- deleting a version is admin only (struts-admin.xml); never the latest -->
                                <#if adminRights && !isPreviewPage && v.version != latestVersion>
                                    <a class="confirmDeleteVersion rp-version-delete ms-1" data-post href="${baseURL}/admin/deleteVersion.do?r=${resource.shortname}&amp;v=${v.version}"><@s.text name='button.delete'/></a>
                                </#if>
                            </div>
                        </li>
                    </#list>
                </ol>
                <#if (shownVersions?size > 5)>
                    <button type="button" class="btn btn-sm btn-outline-secondary" id="rp-versions-all"><@s.text name='portal.resource.versions.all'><@s.param>${shownVersions?size}</@s.param></@s.text></button>
                </#if>
            <#else>
                <p><@s.text name='dataTables.sEmptyTable.versions'/></p>
            </#if>
        </section>
    </#if>
</#macro>

<#-- The heading's Download menu (green outline, the main action), before Edit. Each file shows its name, a line on
     what it holds, and its size.
     files: [{"url", "label", "description", "size", "download" (true for a file the browser saves as is)}], data first -->
<#macro downloadButtons files>
    <#if files?has_content>
        <div class="dropdown d-inline-block mt-1 me-xl-1">
            <button type="button" class="btn btn-sm btn-outline-gbif-primary dropdown-toggle top-button rp-download" data-bs-toggle="dropdown" aria-expanded="false">
                <@icon "download"/> <@s.text name='portal.resource.downloadButton'/>
            </button>
            <ul class="dropdown-menu rp-download-menu">
                <#list files as f>
                    <li>
                        <a class="dropdown-item rp-menu-file" href="${f.url}"<#if f.download!false> download</#if>>
                            <span class="rp-menu-file-name">${f.label}<#if f.description?has_content><span class="rp-menu-file-desc">${f.description}</span></#if></span>
                            <#if f.size?has_content><small>${f.size}</small></#if>
                        </a>
                    </li>
                </#list>
            </ul>
        </div>
    </#if>
</#macro>

<#-- the records line added to a data file's description, e.g. "Records: 26,403" -->
<#function recordsNote>
    <#return action.getText('portal.home.records') + ": " + (recordsPublishedForVersion!0)?string/>
</#function>

<#-- The heading's Download menu for resources described by EML: DwC-A (resource_new.ftl) and DwC-DP (resource_dwc_dp.ftl) -->
<#macro emlDownloadButtons>
    <#local emlFile = {"url": download_eml_url, "label": "EML", "description": action.getText('portal.resource.download.eml.description'), "size": emlSizeForVersion!"", "download": true}/>
    <#local rtfFile = {"url": download_rtf_url, "label": "RTF", "description": action.getText('portal.resource.download.rtf.description'), "size": rtfSizeForVersion!""}/>
    <#if resource.isDataPackage()>
        <#local files = [emlFile]/>
        <#if archiveSizeForVersion??>
            <#local files = [{"url": download_archive_url, "label": action.getText('portal.resource.download.dwcdp'),
                "description": action.getText('portal.resource.download.dataPackage.description') + " · " + recordsNote(), "size": archiveSizeForVersion}] + files/>
        </#if>
    <#elseif metadataOnly>
        <#local files = [emlFile, rtfFile]/>
    <#else>
        <#local files = [{"url": download_dwca_url, "label": action.getText('portal.resource.download.dwca'),
            "description": action.getText('portal.resource.download.dwca.description') + " · " + recordsNote(), "size": archiveSizeForVersion!""}, emlFile, rtfFile]/>
    </#if>
    <@downloadButtons files=files/>
</#macro>

<#-- The heading's Download menu for data packages described by their own metadata: Camtrap DP (resource_dp.ftl) and ColDP (resource_col_dp.ftl).
     metadataFile: the metadata file's name, e.g. datapackage.json; metadataKey: the text key describing it
     The data package is left out when there is none (dataPackageSizeForVersion unset), e.g. on a preview. -->
<#macro dataPackageDownloadButtons metadataFile metadataKey>
    <#local files = [{"url": download_metadata_url, "label": metadataFile, "description": action.getText(metadataKey), "size": metadataSizeForVersion!"", "download": true}]/>
    <#if dataPackageSizeForVersion??>
        <#local files = [{"url": download_dp_url, "label": action.getText("portal.resource.type." + resourceTypeLowerCase),
            "description": action.getText('portal.resource.download.dataPackage.description') + " · " + recordsNote(), "size": dataPackageSizeForVersion}] + files/>
    </#if>
    <@downloadButtons files=files/>
</#macro>
