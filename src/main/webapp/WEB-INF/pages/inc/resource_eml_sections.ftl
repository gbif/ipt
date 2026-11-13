<#-- Sections below the heading box of the public resource page, for resources described by EML:
     DwC-A (portal/resource_new.ftl) and DwC-DP (portal/resource_dwc_dp.ftl). Ends with the footer.
     Needs macros/forms.ftl, macros/icon.ftl, macros/portal_resource.ftl and docbook_editor.ftl included, and isPreviewPage assigned. -->
<#assign isDataPackage = resource.isDataPackage()/>
<#assign isPublished = resource.lastPublished??/>
<#assign isOldVersion = version?? && version.toPlainString() != resource.metadataVersion.toPlainString()/>
<#assign hasCitation = eml.citation?? && (eml.citation.citation?has_content || eml.citation.identifier?has_content)/>
<#assign hasRights = eml.intellectualRights?has_content/>
<#assign hasGeo = eml.geospatialCoverages?has_content/>
<#assign hasTax = organizedCoverages?has_content/>
<#assign hasTemporal = eml.temporalCoverages?has_content/>
<#assign hasContacts = (eml.contacts?size>0) || (eml.creators?size>0) || (eml.metadataProviders?size>0) || (eml.associatedParties?size>0)/>
<#assign hasMethods = eml.studyExtent?has_content || eml.sampleDescription?has_content || eml.qualityControl?has_content || (eml.methodSteps?? && (eml.methodSteps?size>=1) && eml.methodSteps[0]?has_content)/>
<#assign hasProject = eml.project?? && eml.project.title?has_content/>
<#assign hasExternal = (eml.physicalData?size > 0)/>
<#assign hasCollections = eml.collections?? && (eml.collections?size > 0) && eml.collections[0].collectionName?has_content/>
<#assign bibliography = (eml.bibliographicCitationSet.bibliographicCitations)![]/>
<#assign hasBibliography = bibliography?filter(c -> c.citation?has_content)?has_content/>
<#assign updateFrequency = (eml.updateFrequency?? && eml.updateFrequency.identifier?has_content && frequencies[eml.updateFrequency.identifier]?has_content)?then(frequencies[eml.updateFrequency.identifier]?cap_first, "")/>
<#assign hasAdditional = eml.introduction?has_content || eml.gettingStarted?has_content || eml.acknowledgements?has_content || eml.additionalInfo?has_content || eml.purpose?has_content || eml.updateFrequencyDescription?has_content || (eml.alternateIdentifiers?size > 0)
    || updateFrequency?has_content || eml.distributionUrl?has_content/>
<#assign hasMore = (isPublished && (hasBibliography || hasCollections || hasExternal)) || hasAdditional/>
<#-- the keywords, without the dataset type and subtype IPT adds itself (shown as pills in the heading) -->
<#assign shownKeywords = []/>
<#list eml.keywords as ks>
    <#if !(ks.keywordThesaurus!"")?starts_with("GBIF Dataset Type Vocabulary") && !(ks.keywordThesaurus!"")?starts_with("GBIF Dataset Subtype Vocabulary")><#assign shownKeywords = shownKeywords + ks.keywords/></#if>
</#list>
<#assign doi = (action.findDoiAssignedToPublishedVersion())!/>
<#assign hasDoi = doi?has_content && (doi.getUrl())??/>
<#assign gbifLicense = action.getGbifLicense(eml)!""/>

<#-- The role names of a merged contact: citation role first, then contact and metadata provider, then the others -->
<#function roleLabels roleSet>
    <#-- roleSet is a Java Set: copy it into a sequence for ?seq_contains and ?sort -->
    <#local roleSeq = []/>
    <#list roleSet as r><#if r?has_content><#local roleSeq = roleSeq + [r]/></#if></#list>
    <#local first = ["originator", "pointOfContact", "metadataProvider"]/>
    <#local ordered = first?filter(r -> roleSeq?seq_contains(r)) + roleSeq?sort?filter(r -> !first?seq_contains(r))/>
    <#return ordered?map(r -> roles[r]?has_content?then(roles[r]?cap_first, r))/>
</#function>

<#-- A person or organisation as a row that opens to the address, emails, phones, home pages and other identifiers -->
<#macro personCard con roleSet>
    <#local name><#if con.lastName?has_content>${con.firstName!} ${con.lastName}<#elseif con.organisation?has_content>${con.organisation}<#elseif con.position?has_content><#list con.position as p>${p}<#sep>, </#sep></#list></#if></#local>
    <#local orcidUrl = ""/>
    <#list con.userIds![] as userId>
        <#if !orcidUrl?has_content && userId.directory?has_content && userId.identifier?has_content && userId.directory?contains("orcid.org")><#local orcidUrl = userId.directory + userId.identifier/></#if>
    </#list>
    <#local orgLine = []/>
    <#if con.organisation?has_content && con.lastName?has_content><#local orgLine = orgLine + [con.organisation]/></#if>
    <#if con.position?has_content && (con.lastName?has_content || con.organisation?has_content)><#local orgLine = orgLine + con.position/></#if>
    <#local place = []/>
    <#list con.address.address![] as a><#local place = place + [a]/></#list>
    <#if con.address.postalCode?has_content || con.address.city?has_content><#local place = place + [((con.address.postalCode!) + " " + (con.address.city!))?trim]/></#if>
    <#if con.address.province?has_content><#local place = place + [con.address.province]/></#if>
    <#if con.address.country?has_content && con.address.country != 'UNKNOWN'><#local place = place + [countryName(con.address.country)]/></#if>
    <@personRow name=name orcidUrl=orcidUrl org=orgLine?join(" · ") roleLabels=roleLabels(roleSet) email=(con.email![])?first!"">
        <@personDetail label=action.getText('eml.contact.address.address')><#list place as p>${p}<#sep>, </#sep></#list></@personDetail>
        <@personDetail label=action.getText('eml.contact.email')><#list con.email![] as e><div><a href="mailto:${e}" class="break-all">${e}</a></div></#list></@personDetail>
        <@personDetail label=action.getText('eml.contact.phone')><#list con.phone![] as p><div>${p}</div></#list></@personDetail>
        <@personDetail label=action.getText('eml.contact.homepage')><#list con.homepage![] as h><div><a href="${h}" class="break-all">${h}</a></div></#list></@personDetail>
        <@personDetail label=action.getText('portal.resource.contact.identifier')><#list con.userIds![] as userId><#if userId.directory?has_content && userId.identifier?has_content><div><a href="${userId.directory}${userId.identifier}" class="break-all" target="_blank" rel="noopener">${userId.directory}${userId.identifier}</a></div></#if></#list></@personDetail>
    </@personRow>
</#macro>

<div id="sections" class="container-fluid bg-body">
    <div class="container mb-md-4 bd-layout main-content-container">
        <main class="bd-main">
            <div class="bd-toc mt-4 pt-3 ps-3 mb-lg-5 text-muted">
                <nav id="sidebar-content">
                    <ul>
                        <li class="rp-toc-group"><@s.text name='portal.resource.toc.about'/></li>
                        <li><a href="#anchor-description" class="sidebar-navigation-link"><@s.text name='portal.resource.description'/></a></li>
                        <#if isPublished && (hasCitation || hasRights || hasDoi)>
                            <li><a href="#anchor-citation" class="sidebar-navigation-link"><@s.text name='portal.resource.citationLicense'/></a></li>
                        </#if>
                        <#if isPublished && (metadataOnly != true || resource.versionHistory?? || hasGeo || hasTax || hasTemporal)>
                            <li class="rp-toc-group"><@s.text name='portal.resource.toc.data'/></li>
                            <#if metadataOnly != true>
                                <li><a href="#anchor-dataRecords" class="sidebar-navigation-link"><@s.text name='portal.resource.dataRecords'/></a></li>
                            </#if>
                            <#if resource.versionHistory??>
                                <li><a href="#anchor-versions" class="sidebar-navigation-link"><@s.text name='portal.resource.versions'/></a></li>
                            </#if>
                        </#if>
                        <#if isPublished && (hasGeo || hasTax || hasTemporal)>
                            <li><a href="#anchor-coverage" class="sidebar-navigation-link"><@s.text name='portal.resource.coverage'/></a></li>
                        </#if>
                        <#if (isPublished && (hasContacts || hasMethods || hasProject)) || hasMore>
                            <li class="rp-toc-group"><@s.text name='portal.resource.toc.context'/></li>
                        </#if>
                        <#if isPublished>
                            <#if hasContacts>
                                <li><a href="#anchor-contacts" class="sidebar-navigation-link"><@s.text name='portal.resource.contacts'/></a></li>
                            </#if>
                            <#if hasMethods>
                                <li><a href="#anchor-methods" class="sidebar-navigation-link"><@s.text name='manage.metadata.methods.title'/></a></li>
                            </#if>
                            <#if hasProject>
                                <li><a href="#anchor-project" class="sidebar-navigation-link"><@s.text name='manage.metadata.project.title'/></a></li>
                            </#if>
                        </#if>
                        <#if hasMore>
                            <li><a href="#anchor-more" class="sidebar-navigation-link"><@s.text name='portal.resource.more'/></a></li>
                        </#if>
                    </ul>
                </nav>
            </div>

            <div class="bd-content ps-lg-4 pt-5">
                <span class="anchor anchor-home-resource-page" id="anchor-description"></span>
                <section id="description" class="rp-section section">
                    <h2 class="rp-h"><@s.text name='portal.resource.description'/></h2>
                    <#-- filled for translated metadata, see IptMultilingualFields.translationNote -->
                    <p id="metadata-translations" class="metadata-translations text-muted mb-0" hidden></p>
                    <div class="rp-description overflow-x-auto">
                        <@resourceLogo url=(eml.logoUrl)!""/>
                        <div id="description-container" property="dc:abstract" class="rp-prose"<#if eml.description?has_content> data-docbook="${eml.description}"</#if>>
                            <#if !eml.description?has_content>
                                <p><@s.text name='portal.resource.no.description'/></p>
                            </#if>
                        </div>
                    </div>
                    <#if isPublished><@keywordChips keywords=shownKeywords/></#if>
                </section>

                <#-- citation to project only once published -->
                <#if isPublished>
                    <#if hasCitation || hasRights || hasDoi>
                        <span class="anchor anchor-home-resource-page" id="anchor-citation"></span>
                        <section id="citation" class="rp-section section">
                            <h2 class="rp-h"><@s.text name='portal.resource.citationLicense'/></h2>
                            <#if hasDoi>
                                <p class="mb-2">
                                    DOI <a property="dc:identifier" id="rp-doi" href="${doi.getUrl()}" dir="ltr">${doi}</a>
                                    <button type="button" class="btn btn-sm btn-link p-0 ms-1 rp-copy" data-copy-target="#rp-doi" data-copied="<@s.text name='portal.resource.copied'/>" title="<@s.text name='button.copy'/>">
                                        <@icon "content_copy"/> <span class="visually-hidden"><@s.text name='button.copy'/></span>
                                    </button>
                                </p>
                            </#if>
                            <#if hasCitation>
                                <#if isOldVersion>
                                    <p class="warn mb-2"><em><@s.text name='portal.resource.latest.version.warning'/></em></p>
                                </#if>
                                <div class="rp-cite">
                                    <div class="rp-cite-top">
                                        <span class="text-muted"><@s.text name='portal.resource.cite.help'/>:</span>
                                        <button type="button" class="btn btn-sm btn-outline-secondary rp-copy" data-copy-target="#rp-citation-text" data-copied="<@s.text name='portal.resource.copied'/>">
                                            <@icon "content_copy"/> <span><@s.text name='button.copy'/></span>
                                        </button>
                                    </div>
                                    <p id="rp-citation-text" property="dc:bibliographicCitation" class="howtocite overflow-x-auto mb-0">
                                        <@textWithFormattedLink eml.citation.citation/>
                                    </p>
                                </div>
                            </#if>
                            <#if hasRights>
                                <div id="rights" class="rp-license">
                                    <@ccLicense gbifLicenseToCc(gbifLicense)/>
                                    <p property="dc:license" class="mb-0">
                                        <#if resource.organisation?? && action.getDefaultOrganisation()?? && resource.organisation.key.toString() != action.getDefaultOrganisation().key.toString()>
                                            <@s.text name='portal.resource.rights.organisation'><@s.param>${resource.organisation.name}</@s.param></@s.text>
                                        </#if>
                                        <#if gbifLicense == "CC_BY_NC_4_0">
                                            <@s.text name='eml.intellectualRights.licence.ccbync'/>
                                        <#elseif gbifLicense == "CC_BY_4_0">
                                            <@s.text name='eml.intellectualRights.licence.ccby'/>
                                        <#elseif gbifLicense == "CC0_1_0">
                                            <@s.text name='eml.intellectualRights.licence.cczero'/>
                                        <#else>
                                            ${action.getLicenseLabel(eml)}
                                        </#if>
                                    </p>
                                </div>
                            </#if>
                        </section>
                    </#if>

                    <#-- data records as bars, not shown for metadata-only resources -->
                    <#assign recordsByExtensionOrdered = action.getRecordsByExtensionOrdered()/>
                    <#assign coreRowType = resource.getCoreRowType()!""/>
                    <#assign coreExt = action.getExtensionManager().get(coreRowType)!/>
                    <#assign coreCount = recordsByExtensionOrdered.get(coreRowType)!recordsPublishedForVersion!0/>
                    <#assign maxRecords = action.getMaxRecordsInExtension()/>
                    <#if !isDataPackage && (maxRecords < coreCount)><#assign maxRecords = coreCount/></#if>

                    <#if metadataOnly != true>
                        <span class="anchor anchor-home-resource-page" id="anchor-dataRecords"></span>
                        <section id="dataRecords" class="rp-section section">
                            <h2 class="rp-h"><@s.text name='portal.resource.dataRecords'/></h2>
                            <p class="text-muted text-smaller">
                                <#if isDataPackage>
                                    <@s.text name='portal.resource.dataRecords.dataPackageSchema.intro'/>
                                <#else>
                                    <@s.text name='portal.resource.dataRecords.intro'><@s.param>${action.getCoreType()?lower_case}</@s.param></@s.text>
                                </#if>
                            </p>
                            <div class="rp-bars">
                                <#if isDataPackage>
                                    <#-- data package: one bar per table, largest first -->
                                    <#list recordsByExtensionOrdered?keys as k>
                                        <#assign tableCount = recordsByExtensionOrdered.get(k)!0/>
                                        <div class="rp-bar-label">${k}</div>
                                        <div class="rp-bar"><i style="width:<#if (maxRecords > 0)>${(tableCount * 100 / maxRecords)?c}<#else>0</#if>%"></i></div>
                                        <div class="rp-bar-n">${tableCount}</div>
                                    </#list>
                                <#else>
                                <#if coreExt?? && coreExt.name?has_content>
                                    <div class="rp-bar-label"<#if coreExt.description?has_content> title="${coreExt.description}"</#if>>${coreExt.name} <small><@s.text name='portal.resource.dataRecords.core.label'/></small></div>
                                    <div class="rp-bar"><i style="width:<#if (maxRecords > 0)>${(coreCount * 100 / maxRecords)?c}<#else>0</#if>%"></i></div>
                                    <div class="rp-bar-n">${coreCount}</div>
                                </#if>
                                <#list recordsByExtensionOrdered?keys as k>
                                    <#assign ext = action.getExtensionManager().get(k)!/>
                                    <#assign extCount = recordsByExtensionOrdered.get(k)!0/>
                                    <#if coreRowType?has_content && k != coreRowType && ext?? && ext.name?has_content>
                                        <div class="rp-bar-label"<#if ext.description?has_content> title="${ext.description}"</#if>>${ext.name} <small><@s.text name='portal.resource.dataRecords.extension.label'/></small></div>
                                        <div class="rp-bar"><i style="width:<#if (maxRecords > 0)>${(extCount * 100 / maxRecords)?c}<#else>0</#if>%"></i></div>
                                        <div class="rp-bar-n">${extCount}</div>
                                    </#if>
                                </#list>
                                </#if>
                            </div>
                        </section>
                    </#if>

                    <@versionsSection latestVersion=resource.metadataVersion.toPlainString()/>

                    <#if hasGeo || hasTax || hasTemporal>
                        <span class="anchor anchor-home-resource-page" id="anchor-coverage"></span>
                        <section id="coverage" class="rp-section section">
                            <h2 class="rp-h"><@s.text name='portal.resource.coverage'/></h2>
                            <div class="rp-coverage<#if hasGeo && (hasTax || hasTemporal)> rp-coverage-2</#if>">
                                <#if hasGeo>
                                    <div id="geospatial">
                                        <h3 class="rp-h3"><@s.text name='portal.resource.summary.geocoverage'/></h3>
                                        <#assign boxes = []/>
                                        <#list eml.geospatialCoverages as g>
                                            <#assign bc = g.boundingCoordinates!/>
                                            <#if bc?has_content && bc.min?? && bc.max?? && bc.min.latitude?? && bc.min.longitude?? && bc.max.latitude?? && bc.max.longitude??>
                                                <#assign boxes = boxes + [[bc.min.latitude, bc.min.longitude, bc.max.latitude, bc.max.longitude]]/>
                                            </#if>
                                        </#list>
                                        <#if boxes?has_content>
                                            <div id="rp-map" class="rp-map" data-boxes="[<#list boxes as b>[${b[0]?c},${b[1]?c},${b[2]?c},${b[3]?c}]<#sep>,</#sep></#list>]"></div>
                                        </#if>
                                        <#list eml.geospatialCoverages as g>
                                            <div class="rp-cov-desc" property="dc:spatial">
                                                <#if g.description?has_content><div class="rp-clamp" data-show-more="${action.getText('portal.resource.showMore')}">${g.description?no_esc}</div></#if>
                                                <#assign bc = g.boundingCoordinates!/>
                                                <#if bc?has_content && bc.min?? && bc.max?? && bc.min.latitude?? && bc.min.longitude?? && bc.max.latitude?? && bc.max.longitude??>
                                                    <span class="rp-coords" dir="ltr"><@s.text name='eml.geospatialCoverages.boundingCoordinates.min.latitude'/> ${bc.min.latitude?c}, <@s.text name='eml.geospatialCoverages.boundingCoordinates.max.latitude'/> ${bc.max.latitude?c}, <@s.text name='eml.geospatialCoverages.boundingCoordinates.min.longitude'/> ${bc.min.longitude?c}, <@s.text name='eml.geospatialCoverages.boundingCoordinates.max.longitude'/> ${bc.max.longitude?c}</span>
                                                </#if>
                                            </div>
                                        </#list>
                                    </div>
                                </#if>

                                <#if hasTemporal || hasTax>
                                    <div>
                                        <#if hasTemporal>
                                            <div id="temporal">
                                                <h3 class="rp-h3"><@s.text name='manage.metadata.tempcoverage.title'/></h3>
                                                <#assign ranges = []/>
                                                <#list eml.temporalCoverages as t>
                                                    <#if "${t.type}" == "DATE_RANGE" && t.startDate?? && t.endDate??>
                                                        <#assign ranges = ranges + [[t.startDate?string("yyyy-MM-dd"), t.endDate?string("yyyy-MM-dd")]]/>
                                                    <#elseif "${t.type}" == "SINGLE_DATE" && t.startDate??>
                                                        <#assign ranges = ranges + [[t.startDate?string("yyyy-MM-dd"), t.startDate?string("yyyy-MM-dd")]]/>
                                                    </#if>
                                                </#list>
                                                <#if ranges?has_content>
                                                    <div class="rp-timeline" aria-hidden="true" data-ranges="[<#list ranges as r>[&quot;${r[0]}&quot;,&quot;${r[1]}&quot;]<#sep>,</#sep></#list>]"></div>
                                                </#if>
                                                <ul class="rp-cov-desc list-unstyled">
                                                    <#list eml.temporalCoverages as t>
                                                        <#if "${t.type}" == "DATE_RANGE" && t.startDate?? && t.endDate??>
                                                            <li property="dc:temporal">${t.startDate?date} – ${t.endDate?date}</li>
                                                        <#elseif "${t.type}" == "SINGLE_DATE" && t.startDate??>
                                                            <li property="dc:temporal">${t.startDate?date}</li>
                                                        <#elseif "${t.type}" == "FORMATION_PERIOD" && t.formationPeriod??>
                                                            <li property="dc:temporal"><@s.text name='eml.temporalCoverages.formationPeriod'/>: ${t.formationPeriod}</li>
                                                        <#elseif t.livingTimePeriod??>
                                                            <li property="dc:temporal"><@s.text name='eml.temporalCoverages.livingTimePeriod'/>: ${t.livingTimePeriod}</li>
                                                        </#if>
                                                    </#list>
                                                </ul>
                                            </div>
                                        </#if>

                                        <#if hasTax>
                                            <div id="taxanomic">
                                                <h3 class="rp-h3"><@s.text name='manage.metadata.taxcoverage.title'/></h3>
                                                <#list organizedCoverages as item>
                                                    <div class="rp-taxa">
                                                        <#list item.keywords as k>
                                                            <#if k.displayNames?has_content>
                                                                <div class="rp-rank">
                                                                    <#-- avoid accessing "class" from the map, it displays "java.util.LinkedHashMap" -->
                                                                    <span class="rp-rank-name"><#if k.rank?lower_case == "class"><@s.text name='rank.class'/><#elseif k.rank?has_content && ranks[k.rank]?has_content>${ranks[k.rank]?cap_first}<#else>${k.rank!}</#if></span>
                                                                    <span class="rp-chips">
                                                                        <#list k.displayNames as name>
                                                                            <span class="rp-chip"<#if (name?index >= 12)> hidden</#if>>${name}</span>
                                                                        </#list>
                                                                        <#if (k.displayNames?size > 12)>
                                                                            <button type="button" class="rp-chip rp-chip-more">+${k.displayNames?size - 12}</button>
                                                                        </#if>
                                                                    </span>
                                                                </div>
                                                            </#if>
                                                        </#list>
                                                        <#if item.description?has_content>
                                                            <div class="rp-cov-desc">${item.description}</div>
                                                        </#if>
                                                    </div>
                                                </#list>
                                            </div>
                                        </#if>
                                    </div>
                                </#if>
                            </div>
                        </section>
                    </#if>

                    <#-- people: each person once, with all their roles -->
                    <#if hasContacts>
                        <span class="anchor anchor-home-resource-page" id="anchor-contacts"></span>
                        <section id="contacts" class="rp-section section">
                            <h2 class="rp-h"><@s.text name='portal.resource.contacts'/></h2>
                            <div class="rp-people">
                                <#list mergedContacts as c>
                                    <@personCard con=c roleSet=(contactRoles[c.fullName!])![]/>
                                </#list>
                            </div>
                        </section>
                    </#if>

                    <#if hasMethods>
                        <span class="anchor anchor-home-resource-page" id="anchor-methods"></span>
                        <section id="methods" class="rp-section section">
                            <h2 class="rp-h"><@s.text name='manage.metadata.methods.title'/></h2>
                            <#if (eml.methodSteps?? && (eml.methodSteps?size>=1) && eml.methodSteps[0]?has_content)>
                                <h3 class="rp-h3"><@s.text name='rtf.methods.description'/></h3>
                                <ol class="rp-steps overflow-x-auto">
                                    <#list eml.methodSteps as step>
                                        <#if step?has_content><li>${step?no_esc}</li></#if>
                                    </#list>
                                </ol>
                            </#if>
                            <#if eml.sampleDescription?has_content || eml.studyExtent?has_content || eml.qualityControl?has_content>
                                <dl class="rp-kv">
                                    <#if eml.studyExtent?has_content>
                                        <dt><@s.text name='eml.studyExtent'/></dt><dd>${eml.studyExtent?no_esc}</dd>
                                    </#if>
                                    <#if eml.sampleDescription?has_content>
                                        <dt><@s.text name='eml.sampleDescription'/></dt><dd>${eml.sampleDescription?no_esc}</dd>
                                    </#if>
                                    <#if eml.qualityControl?has_content>
                                        <dt><@s.text name='eml.qualityControl'/></dt><dd>${eml.qualityControl?no_esc}</dd>
                                    </#if>
                                </dl>
                            </#if>
                        </section>
                    </#if>

                    <#if hasProject>
                        <span class="anchor anchor-home-resource-page" id="anchor-project"></span>
                        <section id="project" class="rp-section section">
                            <h2 class="rp-h"><@s.text name='manage.metadata.project.title'/></h2>
                            <#if eml.project.description?has_content>
                                <div class="rp-prose mb-3">${eml.project.description?no_esc}</div>
                            </#if>
                            <dl class="rp-kv">
                                <dt><@s.text name='eml.project.title'/></dt><dd>${eml.project.title}</dd>
                                <#if eml.project.identifier?has_content>
                                    <dt><@s.text name='eml.project.identifier'/></dt><dd>${eml.project.identifier}</dd>
                                </#if>
                                <#if eml.project.funding?has_content>
                                    <dt><@s.text name='eml.project.funding'/></dt><dd>${eml.project.funding}</dd>
                                </#if>
                                <#if eml.project.studyAreaDescription.descriptorValue?has_content>
                                    <dt><@s.text name='eml.project.studyAreaDescription.descriptorValue'/></dt><dd>${eml.project.studyAreaDescription.descriptorValue?no_esc}</dd>
                                </#if>
                                <#if eml.project.designDescription?has_content>
                                    <dt><@s.text name='eml.project.designDescription'/></dt><dd>${eml.project.designDescription?no_esc}</dd>
                                </#if>
                                <#if eml.project.awards?has_content>
                                    <dt><@s.text name='eml.project.award'/></dt>
                                    <dd>
                                        <#list eml.project.awards as award>
                                            <div class="rp-award">
                                                <strong>${award.title!}</strong>
                                                <#if award.funderName?has_content><div>${award.funderName}</div></#if>
                                                <#if award.funderIdentifiers?has_content><div class="text-muted"><#list award.funderIdentifiers as fi>${fi}<#sep>, </#sep></#list></div></#if>
                                                <#if award.awardNumber?has_content><div>${award.awardNumber}</div></#if>
                                                <#if award.awardUrl?has_content><div><a href="${award.awardUrl}" class="break-all">${award.awardUrl}</a></div></#if>
                                            </div>
                                        </#list>
                                    </dd>
                                </#if>
                                <#if eml.project.relatedProjects?has_content>
                                    <dt><@s.text name='eml.project.relatedProjects'/></dt>
                                    <dd>
                                        <#list eml.project.relatedProjects as relatedProject>
                                            <div class="rp-award">
                                                <strong>${relatedProject.title}</strong>
                                                <#if relatedProject.identifier?has_content><div>${relatedProject.identifier}</div></#if>
                                                <#if relatedProject.description?has_content><div>${relatedProject.description}</div></#if>
                                            </div>
                                        </#list>
                                    </dd>
                                </#if>
                            </dl>
                            <#if (eml.project.personnel?size > 0)>
                                <h3 class="rp-h3"><@s.text name='eml.project.personnel.intro'/></h3>
                                <div class="rp-people">
                                    <#list deduplicatedProjectPersonnel as c>
                                        <@personCard con=c roleSet=(projectPersonnelRoles[c.fullName!])![]/>
                                    </#list>
                                </div>
                            </#if>
                        </section>
                    </#if>
                </#if>

                <#-- the long tail: folded -->
                <#if hasMore>
                <span class="anchor anchor-home-resource-page" id="anchor-more"></span>
                <section id="more" class="rp-section section">
                    <h2 class="rp-h"><@s.text name='portal.resource.more'/></h2>
                    <div class="rp-rows">

                    <#if isPublished && hasBibliography>
                        <@moreRow id="reference" title=action.getText('manage.metadata.citations.bibliography') hint=bibliography?filter(c -> c.citation?has_content)?size?c>
                            <ol class="overflow-x-auto">
                                <#list eml.bibliographicCitationSet.bibliographicCitations as item>
                                    <#if item.citation?has_content>
                                        <li property="dc:references">${item.citation} <@textWithFormattedLink item.identifier!/></li>
                                    </#if>
                                </#list>
                            </ol>
                        </@moreRow>
                    </#if>

                    <#if isPublished && hasCollections>
                        <@moreRow id="collection" title=action.getText('manage.metadata.collections.title') hint=eml.collections?map(c -> c.collectionName!"")?filter(n -> n?has_content)?join(", ")>
                            <dl class="rp-kv">
                                <#list eml.collections as item>
                                    <#if item.collectionName?has_content><dt><@s.text name='eml.collectionName'/></dt><dd>${item.collectionName}</dd></#if>
                                    <#if item.collectionId?has_content><dt><@s.text name='eml.collectionId'/></dt><dd>${item.collectionId}</dd></#if>
                                    <#if item.parentCollectionId?has_content><dt><@s.text name='eml.parentCollectionId'/></dt><dd>${item.parentCollectionId}</dd></#if>
                                </#list>
                                <#if eml.specimenPreservationMethods?? && (eml.specimenPreservationMethods?size>0) && eml.specimenPreservationMethods[0]?has_content>
                                    <dt><@s.text name='eml.specimenPreservationMethod.plural'/></dt>
                                    <dd><#list eml.specimenPreservationMethods as item>${(preservationMethods[item])!?cap_first}<#sep>, </#sep></#list></dd>
                                </#if>
                                <#if eml.jgtiCuratorialUnits?? && (eml.jgtiCuratorialUnits?size>0) && eml.jgtiCuratorialUnits[0]?has_content>
                                    <dt><@s.text name='manage.metadata.collections.curatorialUnits.title'/></dt>
                                    <dd>
                                        <#list eml.jgtiCuratorialUnits as item>
                                            <div>
                                                <#if item.type=="COUNT_RANGE">
                                                    <@s.text name='eml.jgtiCuratorialUnits.rangeStart'/>&nbsp;${item.rangeStart}
                                                    <@s.text name='eml.jgtiCuratorialUnits.rangeEnd'/>&nbsp;${item.rangeEnd}
                                                    ${item.unitType}
                                                <#else>
                                                    <@s.text name='eml.jgtiCuratorialUnits.rangeMean'/>&nbsp;${item.rangeMean}
                                                    <@s.text name='eml.jgtiCuratorialUnits.uncertaintyMeasure'/>&nbsp;${item.uncertaintyMeasure}
                                                    ${item.unitType}
                                                </#if>
                                            </div>
                                        </#list>
                                    </dd>
                                </#if>
                            </dl>
                        </@moreRow>
                    </#if>

                    <#if isPublished && hasExternal>
                        <@moreRow id="external" title=action.getText('manage.metadata.physical.alternativeTitle') hint=eml.physicalData?size?c>
                            <p class="text-muted"><@s.text name='portal.resource.otherFormats'/></p>
                            <dl class="rp-kv">
                                <#list eml.physicalData as link>
                                    <dt property="dc:isFormatOf">${link.name!}</dt>
                                    <dd>
                                        <a href="${link.distributionUrl}" class="break-all">${link.distributionUrl!"?"}</a>
                                        <#if link.charset?? || link.format?? || link.formatVersion??>${link.charset!} ${link.format!} ${link.formatVersion!}</#if>
                                    </dd>
                                </#list>
                            </dl>
                        </@moreRow>
                    </#if>

                    <#if hasAdditional>
                        <#-- the hint names the fields given -->
                        <#assign additionalFields = []/>
                        <#if eml.additionalInfo?has_content><#assign additionalFields = additionalFields + [action.getText('eml.additionalInfo')]/></#if>
                        <#if eml.acknowledgements?has_content><#assign additionalFields = additionalFields + [action.getText('manage.metadata.acknowledgements')]/></#if>
                        <#if eml.introduction?has_content><#assign additionalFields = additionalFields + [action.getText('manage.metadata.introduction')]/></#if>
                        <#if eml.gettingStarted?has_content><#assign additionalFields = additionalFields + [action.getText('manage.metadata.gettingStarted')]/></#if>
                        <#if eml.purpose?has_content><#assign additionalFields = additionalFields + [action.getText('eml.purpose')]/></#if>
                        <#if updateFrequency?has_content><#assign additionalFields = additionalFields + [action.getText('eml.updateFrequency')]/></#if>
                        <#if eml.distributionUrl?has_content><#assign additionalFields = additionalFields + [action.getText('eml.distributionUrl.short')]/></#if>
                        <#if eml.updateFrequencyDescription?has_content><#assign additionalFields = additionalFields + [action.getText('eml.updateFrequencyDescription')]/></#if>
                        <#if (eml.alternateIdentifiers?size > 0)><#assign additionalFields = additionalFields + [action.getText('manage.metadata.alternateIdentifiers.title')]/></#if>
                        <@moreRow id="additionalMetadata" title=action.getText('manage.metadata.additional.title') hint=additionalFields?join(", ")>
                            <#if eml.additionalInfo?has_content>
                                <div class="rp-prose overflow-x-auto mb-2">${eml.additionalInfo?no_esc}</div>
                            </#if>
                            <dl class="rp-kv">
                                <#if eml.acknowledgements?has_content>
                                    <dt><@s.text name='manage.metadata.acknowledgements'/></dt>
                                    <dd><div id="acknowledgements-container" data-docbook="${eml.acknowledgements}"></div></dd>
                                </#if>
                                <#if eml.introduction?has_content>
                                    <dt><@s.text name='manage.metadata.introduction'/></dt>
                                    <dd><div id="introduction-container" data-docbook="${eml.introduction}"></div></dd>
                                </#if>
                                <#if eml.gettingStarted?has_content>
                                    <dt><@s.text name='manage.metadata.gettingStarted'/></dt>
                                    <dd><div id="gettingStarted-container" data-docbook="${eml.gettingStarted}"></div></dd>
                                </#if>
                                <#if eml.purpose?has_content>
                                    <dt><@s.text name='eml.purpose'/></dt>
                                    <dd><div id="purpose-container" data-docbook="${eml.purpose}"></div></dd>
                                </#if>
                                <#if updateFrequency?has_content>
                                    <dt><@s.text name='eml.updateFrequency'/></dt>
                                    <dd>${updateFrequency}</dd>
                                </#if>
                                <#if eml.distributionUrl?has_content>
                                    <dt><@s.text name='eml.distributionUrl.short'/></dt>
                                    <dd><a href="${eml.distributionUrl}" class="break-all">${eml.distributionUrl}</a></dd>
                                </#if>
                                <#if eml.updateFrequencyDescription?has_content>
                                    <dt><@s.text name='eml.updateFrequencyDescription'/></dt>
                                    <dd>${eml.updateFrequencyDescription?no_esc}</dd>
                                </#if>
                                <#if (eml.alternateIdentifiers?size > 0)>
                                    <dt><@s.text name='manage.metadata.alternateIdentifiers.title'/></dt>
                                    <dd><#list eml.alternateIdentifiers as item><div class="break-all"><@textWithFormattedLink item!/></div></#list></dd>
                                </#if>
                            </dl>
                        </@moreRow>
                    </#if>
                    </div>
                </section>
                </#if>
            </div>
        </main>
    </div>
</div>


<#include "/WEB-INF/pages/inc/footer.ftl">

<script src="${baseURL}/js/jconfirmation.jquery.js"></script>
<script src="${baseURL}/js/leaflet/leaflet-1.9.4.js"></script>
<script src="${baseURL}/js/portal-resource.js"></script>

<script>
    $('.confirmDeleteVersion').jConfirmAction({
        titleQuestion : "<@s.text name="basic.confirm"/>",
        question : "<@s.text name='portal.resource.confirm.delete.version'/><br><br><@s.text name='portal.resource.confirm.delete.version.warning.citation'/><br><br><@s.text name='portal.resource.confirm.delete.version.warning.undone'/>",
        yesAnswer : "<@s.text name='basic.yes'/>",
        cancelAnswer : "<@s.text name='basic.no'/>",
        buttonType: "danger",
        baseUrl: "${baseURL}"
    });
</script>
