<#-- @ftlvariable name="" type="org.gbif.ipt.action.portal.ResourceAction" -->
<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
<title>${eml.title!"IPT"}</title>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<#include "/WEB-INF/pages/macros/versionsTable.ftl"/>

<#--Construct a Contact. Parameters are the actual contact object, the contact type, and the Dublin Core Property Type -->
<#macro contact con type dcPropertyType>
    <div class="contact">

        <div class="contactType">
            <#if con.role?? && con.role?has_content && roles[con.role]??>
                ${roles[con.role]?cap_first!}
            <#elseif type?has_content>
                ${type}
            </#if>
        </div>

        <#-- minimum info is the last name, organisation name, or position name -->
        <div <#if dcPropertyType?has_content>property="dc:${dcPropertyType}" </#if> class="contactName mb-1">
            <#if con.lastName?has_content>
                ${con.firstName!} ${con.lastName!}
            <#elseif con.organisation?has_content>
                ${con.organisation}
            <#elseif con.positionName?has_content>
                ${con.position!}
            </#if>
        </div>
        <#-- we use this div to toggle the grouped information -->
        <div class="text-smaller text-discreet">
            <#if con.position?has_content>
                <div class="contactPosition">
                    ${con.position!}
                </div>
            </#if>
            <div class="address">
                <#if con.organisation?has_content>
                    <div>${con.organisation}</div>
                </#if>

                <#if con.address.address?has_content>
                    <div>${con.address.address!}</div>
                </#if>

                <#if con.address.postalCode?has_content || con.address.city?has_content>
                    <div class="city">
                       <#if con.address.postalCode?has_content>
                           ${con.address.postalCode!}
                       </#if>
                        ${con.address.city!}
                    </div>
                </#if>

                <#if con.address.province?has_content>
                    <div class="province">${con.address.province}</div>
                </#if>

                <#if con.address.country?has_content && con.address.country != 'UNKNOWN'>
                    <div class="country">${con.address.country}</div>
                </#if>

                <#if con.email?has_content>
                    <div class="email"><a href="mailto:${con.email}" title="email">${con.email}</a></div>
                </#if>

                <#if con.phone?has_content>
                    <div class="phone">${con.phone}</div>
                </#if>

            </div>
            <#if con.homepage?has_content>
                <a href="${con.homepage}">${con.homepage}</a>
            </#if>
            <#if (con.userIds?size > 0)>
                <#assign directory>${con.userIds[0].directory}</#assign>
                <#assign identifier>${con.userIds[0].identifier}</#assign>
                <#if directory?has_content && identifier?has_content>
                    <div>
                        <a href="${directory}${identifier}" target="_blank">${directory}${identifier}</a>
                    </div>
                </#if>
            </#if>
        </div>
    </div>
</#macro>

<#-- Creates a column list of contacts -->
<#macro contactList contacts contactType="" dcPropertyType="" >
    <#list contacts as c>
        <@contact con=c type=contactType dcPropertyType=dcPropertyType />
    </#list>
</#macro>

<#-- Displays the name of an extension inside a span, with title set to extension description (if it exists) in order to display it in tooltip -->
<#macro extensionLink ext isCore=false>
    <#if ext?? && ext.name?has_content>
        <#if ext.description?has_content>
            <#assign coreText><@s.text name='manage.overview.DwC.Mappings.cores.select'/></#assign>
            <div class="col-lg-3 ps-0 ext-tooltip" title="${ext.description}">${ext.name}&nbsp;<#if isCore>&#40;${coreText?lower_case}&#41;</#if></div>
        <#else>
            <div class="col-lg-3 ps-0">${ext.name}</div>
        </#if>
    </#if>
</#macro>

<#assign anchor_versions>#anchor-versions</#assign>
<#assign anchor_rights>#anchor-rights</#assign>
<#assign anchor_citation>#anchor-citation</#assign>
<#assign no_description><@s.text name='portal.resource.no.description'/></#assign>
<#assign updateFrequencyTitle><@s.text name='eml.updateFrequency'/></#assign>
<#assign publishedOnText><@s.text name='manage.overview.published.released'/></#assign>
<#assign download_dwca_url>${baseURL}/archive.do?r=${resource.shortname}<#if version??>&v=${version.toPlainString()}</#if></#assign>
<#assign download_eml_url>${baseURL}/eml.do?r=${resource.shortname}&v=<#if version??>${version.toPlainString()}<#else>${resource.emlVersion.toPlainString()}</#if></#assign>
<#assign download_rtf_url>${baseURL}/rtf.do?r=${resource.shortname}&v=<#if version??>${version.toPlainString()}<#else>${resource.emlVersion.toPlainString()}</#if></#assign>
<#assign isPreviewPage = action.isPreview() />

<style>
    <#-- For HTML headers inside description -->
    h1, h2, h3, h4, h5 {
        font-size: 1.25rem !important;
    }
</style>

<script src="${baseURL}/js/jquery/jquery-3.5.1.min.js"></script>
<script src="${baseURL}/js/jquery/jquery.dataTables-1.10.23.min.js"></script>
<script src="${baseURL}/js/jquery/dataTables.bootstrap5-1.10.23.min.js"></script>
<script>
    $(document).ready(function() {
        // spy scroll and manage sidebar menu
        $(window).scroll(function () {
            var scrollPosition = $(document).scrollTop();

            $('.bd-toc nav a').each(function () {
                var currentLink = $(this);
                var anchor = $(currentLink.attr("href"));
                var sectionId = anchor[0].id.replace("anchor-", "");
                var section = $("#" + sectionId);

                var sectionsContainer = $("#sections");

                if (sectionsContainer.position().top - 50 > scrollPosition) {
                    var removeActiveFromThisLink = $('.bd-toc nav a.active');
                    removeActiveFromThisLink.removeClass('active');
                } else if (section.position().top - 50 <= scrollPosition
                    && section.position().top + section.height() > scrollPosition) {
                    if (!currentLink.hasClass("active")) {
                        var removeFromThisLink = $('.bd-toc nav a.active');
                        removeFromThisLink.removeClass('active');
                        $(this).addClass('active');
                    }
                }
            });
        })
    })
</script>

<div class="container-fluid bg-body border-bottom">
    <div class="container my-3">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <#-- display watermark for preview pages -->
    <#if isPreviewPage>
        <div id="watermark" class="text-center text-uppercase fs-1 mb-2">
            <@s.text name='manage.overview.metadata.preview'><@s.param>${resource.emlVersion.toPlainString()}</@s.param></@s.text>
        </div>
    </#if>

    <div class="container my-3 p-3">
        <div class="text-center text-uppercase fw-bold fs-smaller-2">
            <span>${coreType}</span>
        </div>

        <div class="text-center">
            <h1 property="dc:title" class="rtitle pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                ${eml.title!resource.shortname}
            </h1>

            <#if resource.lastPublished?? && resource.organisation??>
                <div class="text-gbif-primary text-smaller">
                    <span>
                        <#-- the existence of parameter version means the version is not equal to the latest published version -->
                        <#if version?? && version.toPlainString() != resource.emlVersion.toPlainString()>
                            <em><@s.text name='portal.resource.version'/>&nbsp;${version.toPlainString()}</em>
                        <#else>
                            <@s.text name='portal.resource.latest.version'/>
                        </#if>

                        <#if action.getDefaultOrganisation()?? && resource.organisation.key.toString() == action.getDefaultOrganisation().key.toString()>
                            ${publishedOnText?lower_case}&nbsp;<span property="dc:issued">${eml.pubDate?date?string.long}</span>
                            <br>
                            <em class="text-gbif-danger"><@s.text name='manage.home.not.registered.verbose'/></em>
                        <#else>
                            <@s.text name='portal.resource.publishedOn'><@s.param>${resource.organisation.name}</@s.param></@s.text> <span property="dc:issued">${eml.pubDate?date?string.long_short}</span>
                            <span property="dc:publisher" style="display: none">${resource.organisation.name}</span>
                        </#if>
                    </span>
                </div>
            <#else>
                <div class="text-gbif-danger text-smaller">
                    <@s.text name='portal.resource.published.never.long'/>
                </div>
            </#if>

            <#if eml.distributionUrl?has_content || resource.lastPublished??>
                <div class="mt-2">

                    <#if managerRights>
                        <a href="${baseURL}/manage/resource.do?r=${resource.shortname}" class="btn btn-sm btn-outline-gbif-primary mt-1 me-xl-1 top-button">
                            <@s.text name='button.edit'/>
                        </a>
                    </#if>
                    <#if version?? && version.toPlainString() != resource.emlVersion.toPlainString()>
                        <#if adminRights>
                            <a class="confirmDeleteVersion btn btn-sm btn-outline-gbif-danger mt-1 me-xl-1 top-button" href="${baseURL}/admin/deleteVersion.do?r=${resource.shortname}&v=${version.toPlainString()}">
                                <@s.text name='button.delete.version'/>
                            </a>
                        </#if>
                    </#if>
                </div>
            </#if>
        </div>
    </div>
</div>

<#assign isLogoPresent=eml.logoUrl?has_content/>

<div class="container-fluid bg-light border-bottom">
    <div class="container">
        <div class="my-4 px-4 py-4 bg-body border rounded shadow-sm">
            <span class="anchor anchor-home-resource-page-2 mb-3" id="anchor-downloads"></span>
            <div class="mx-md-4 mx-2">
                <div class="row">
                    <div class="<#if isLogoPresent>col-lg-3-5 col-md-10 col-sm-9 col-8<#else>col-lg-4</#if> text-smaller px-0 pb-lg-max-3 ps-lg-3 order-lg-2">
                        <dl class="inline mb-0">
                            <#if eml.distributionUrl?has_content>
                                <div>
                                    <dt><@s.text name='eml.distributionUrl.short'/>:</dt>
                                    <dd><a href="${eml.distributionUrl}"><@s.text name='basic.link'/></a></dd>
                                </div>
                            </#if>

                            <#if resource.status=="REGISTERED" && resource.key??>
                                <div>
                                    <dt>GBIF UUID:</dt>
                                    <dd><a href="${cfg.portalUrl}/dataset/${resource.key}">${resource.key}</a></dd>
                                </div>
                            </#if>

                            <#if eml.pubDate??>
                                <div>
                                    <dt><@s.text name='portal.resource.publicationDate'/>:</dt>
                                    <dd>${eml.pubDate?date?string.long}</dd>
                                </div>
                            </#if>

                            <#if resource.organisation??>
                                <div>
                                    <dt><@s.text name='portal.resource.hostedBy'/>:</dt>
                                    <dd>
                                        <a href="${cfg.portalUrl}/publisher/${resource.organisation.key}" target="_blank">${resource.organisation.name!"Organisation"}</a>
                                    </dd>
                                </div>
                            </#if>

                            <#if eml.intellectualRights?has_content>
                                <div>
                                    <dt><@s.text name='portal.resource.license'/>:</dt>
                                    <dd>
                                        <#if eml.intellectualRights.contains("CC-BY-NC")>
                                            <a href="http://creativecommons.org/licenses/by-nc/4.0/legalcode" target="_blank">CC-BY-NC 4.0</a>
                                        <#elseif eml.intellectualRights.contains("CC-BY")>
                                            <a href="http://creativecommons.org/licenses/by/4.0/legalcode" target="_blank">CC-BY 4.0</a>
                                        <#elseif eml.intellectualRights.contains("CC0")>
                                            <a href="http://creativecommons.org/publicdomain/zero/1.0/legalcode" target="_blank">CC0 1.0</a>
                                        </#if>
                                    </dd>
                                </div>
                            </#if>

                            <div>
                                <#if eml.citation?? && (eml.citation.citation?has_content || eml.citation.identifier?has_content)>
                                    <a href="#anchor-citation" class="doi" dir="ltr">
                                        <span class="gb-icon gb-icon-quote"></span>
                                        <span dir="auto"><@s.text name='portal.resource.cite.howTo'/></span>
                                    </a>
                                </#if>

                                <#assign doi>${action.findDoiAssignedToPublishedVersion()!}</#assign>
                                <#if doi?has_content>
                                    <#assign doiUrl>${action.findDoiAssignedToPublishedVersion().getUrl()!}</#assign>
                                </#if>

                                <#if doi?has_content && doiUrl?has_content>
                                    <doi link="${doi}">
                                        <a property="dc:identifier" dir="ltr" class="doi" href="${doiUrl!}">
                                            <span>DOI</span>
                                            <span>${doi}</span>
                                        </a>
                                    </doi>
                                </#if>
                            </div>
                        </dl>
                    </div>

                    <#if isLogoPresent>
                        <div class="col-lg-1-5 col-md-2 col-sm-3 col-4 text-smaller px-0 pb-lg-max-3 order-lg-3">
                            <div class="logoImg text-end">
                                <img src="${eml.logoUrl}"/>
                            </div>
                        </div>
                    </#if>

                    <div class="<#if isLogoPresent>col-lg-7<#else>col-lg-8</#if> text-smaller px-0 pt-lg-max-3 border-lg-max-top order-lg-1">
                        <#if metadataOnly == true>
                            <p class="mb-1"><@s.text name='portal.resource.downloads.metadataOnly.verbose'/></p>
                        <#elseif resource.schemaIdentifier??>
                            <p class="mb-1"><@s.text name='portal.resource.downloads.dataSchema.verbose'/></p>
                        <#else>
                            <p class="mb-1"><@s.text name='portal.resource.downloads.verbose'/></p>
                        </#if>

                        <div class="table-responsive">
                            <table class="downloads text-smaller table table-sm table-borderless mb-0">
                                <#-- Archive, EML, and RTF download links include Google Analytics event tracking -->
                                <#-- e.g. Archive event tracking includes components: _trackEvent method, category, action, label, (int) value -->
                                <#-- EML and RTF versions can always be retrieved by version number but DWCA versions are only stored if IPT Archive Mode is on -->
                                <#if metadataOnly == false>
                                    <tr>
                                        <th class="col-4 p-0">
                                            <#if resource.schemaIdentifier??>
                                                <@s.text name='portal.resource.dataPackage.verbose'/>
                                            <#else>
                                                <@s.text name='portal.resource.dwca.verbose'/>
                                            </#if>
                                        </th>
                                        <#if version?? && version.toPlainString() != resource.emlVersion.toPlainString() && recordsPublishedForVersion??>
                                            <td class="p-0">
                                                <a href="${download_dwca_url}" onClick="_gaq.push(['_trackEvent', 'Archive', 'Download', '${resource.shortname}', ${recordsPublishedForVersion!0?c} ]);">
                                                    <@s.text name='portal.resource.download'/>
                                                </a>
                                                <#if !resource.schemaIdentifier??>${recordsPublishedForVersion!0?c} <@s.text name='portal.resource.records'/>&nbsp;</#if><#if eml.language?has_content && languages[eml.language]?has_content><@s.text name='eml.language.available'><@s.param>${languages[eml.language]?cap_first!}</@s.param></@s.text></#if> (${dwcaSizeForVersion!}) <#if eml.updateFrequency?has_content && eml.updateFrequency.identifier?has_content && frequencies[eml.updateFrequency.identifier]?has_content>&nbsp;-&nbsp;${updateFrequencyTitle?lower_case?cap_first}:&nbsp;${frequencies[eml.updateFrequency.identifier]?lower_case}</#if>
                                            </td>
                                        <#else>
                                            <td class="p-0">
                                                <a href="${download_dwca_url}" onClick="_gaq.push(['_trackEvent', 'Archive', 'Download', '${resource.shortname}', ${resource.recordsPublished!0?c} ]);">
                                                    <@s.text name='portal.resource.download'/>
                                                </a>
                                                <#if !resource.schemaIdentifier??>${resource.recordsPublished!0?c} <@s.text name='portal.resource.records'/>&nbsp;</#if><#if eml.language?has_content && languages[eml.language]?has_content><@s.text name='eml.language.available'><@s.param>${languages[eml.language]?cap_first!}</@s.param></@s.text></#if> (${dwcaSizeForVersion!})<#if eml.updateFrequency?has_content && eml.updateFrequency.identifier?has_content && frequencies[eml.updateFrequency.identifier]?has_content>&nbsp;-&nbsp;${updateFrequencyTitle?lower_case?cap_first}:&nbsp;${frequencies[eml.updateFrequency.identifier]?lower_case}</#if>
                                            </td>
                                        </#if>
                                    </tr>
                                </#if>
                                <tr>
                                    <th class="p-0"><@s.text name='portal.resource.metadata.verbose'/></th>
                                    <td class="p-0"><a href="${download_eml_url}" onClick="_gaq.push(['_trackEvent', 'EML', 'Download', '${resource.shortname}']);" download><@s.text name='portal.resource.download'/></a>
                                        <#if eml.metadataLanguage?has_content && languages[eml.metadataLanguage]?has_content><@s.text name='eml.language.available'><@s.param>${languages[eml.metadataLanguage]?cap_first!}</@s.param></@s.text></#if> (${emlSizeForVersion})
                                    </td>
                                </tr>

                                <tr>
                                    <th class="p-0"><@s.text name='portal.resource.rtf.verbose'/></th>
                                    <td class="p-0"><a href="${download_rtf_url}" onClick="_gaq.push(['_trackEvent', 'RTF', 'Download', '${resource.shortname}']);"><@s.text name='portal.resource.download'/></a>
                                        <#if eml.metadataLanguage?has_content && languages[eml.metadataLanguage]?has_content><@s.text name='eml.language.available'><@s.param>${languages[eml.metadataLanguage]?cap_first!}</@s.param></@s.text></#if> (${rtfSizeForVersion})
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>


<div id="sections" class="container-fluid bg-body">
    <div class="container my-md-4 bd-layout">

        <main class="bd-main">
            <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                <nav id="sidebar-content">
                    <ul>
                        <li><a href="#anchor-description" class="sidebar-navigation-link"><@s.text name='portal.resource.description'/></a></li>
                        <#if resource.lastPublished??>
                            <#if metadataOnly != true>
                                <li><a href="#anchor-dataRecords" class="sidebar-navigation-link"><@s.text name='portal.resource.dataRecords'/></a></li>
                            </#if>
                            <#if resource.versionHistory??>
                                <li><a href="#anchor-versions" class="sidebar-navigation-link"><@s.text name='portal.resource.versions'/></a></li>
                            </#if>
                            <#if eml.citation?? && (eml.citation.citation?has_content || eml.citation.identifier?has_content)>
                                <li><a href="#anchor-citation" class="sidebar-navigation-link"><@s.text name='portal.resource.cite.howTo'/></a></li>
                            </#if>
                            <#if eml.intellectualRights?has_content>
                                <li><a href="#anchor-rights" class="sidebar-navigation-link"><@s.text name='eml.intellectualRights.simple'/></a></li>
                            </#if>
                            <li><a href="#anchor-gbif" class="sidebar-navigation-link"><@s.text name='portal.resource.organisation.key'/></a></li>
                            <#if eml.subject?has_content>
                                <li><a href="#anchor-keywords" class="sidebar-navigation-link"><@s.text name='portal.resource.summary.keywords'/></a></li>
                            </#if>
                            <#if (eml.physicalData?size > 0 )>
                                <li><a href="#anchor-external" class="sidebar-navigation-link"><@s.text name='manage.metadata.physical.alternativeTitle'/></a></li>
                            </#if>
                            <#if (eml.contacts?size>0) || (eml.creators?size>0) || (eml.metadataProviders?size>0) || (eml.associatedParties?size>0)>
                                <li><a href="#anchor-contacts" class="sidebar-navigation-link"><@s.text name='portal.resource.contacts'/></a></li>
                            </#if>
                            <#if eml.geospatialCoverages[0]??>
                                <li><a href="#anchor-geospatial" class="sidebar-navigation-link"><@s.text name='portal.resource.summary.geocoverage'/></a></li>
                            </#if>
                            <#if ((organizedCoverages?size > 0))>
                                <li><a href="#anchor-taxanomic" class="sidebar-navigation-link"><@s.text name='manage.metadata.taxcoverage.title'/></a></li>
                            </#if>
                            <#if ((eml.temporalCoverages?size > 0))>
                                <li><a href="#anchor-temporal" class="sidebar-navigation-link"><@s.text name='manage.metadata.tempcoverage.title'/></a></li>
                            </#if>
                            <#if eml.project?? && eml.project.title?has_content>
                                <li><a href="#anchor-project" class="sidebar-navigation-link"><@s.text name='manage.metadata.project.title'/></a></li>
                            </#if>
                            <#if eml.studyExtent?has_content || eml.sampleDescription?has_content || eml.qualityControl?has_content || (eml.methodSteps?? && (eml.methodSteps?size>=1) && eml.methodSteps[0]?has_content) >
                                <li><a href="#anchor-methods" class="sidebar-navigation-link"><@s.text name='manage.metadata.methods.title'/></a></li>
                            </#if>
                            <#if eml.collections?? && (eml.collections?size > 0) && eml.collections[0].collectionName?has_content >
                                <li><a href="#anchor-collection" class="sidebar-navigation-link"><@s.text name='manage.metadata.collections.title'/></a></li>
                            </#if>
                            <#if eml.bibliographicCitationSet?? && (eml.bibliographicCitationSet.bibliographicCitations?has_content)>
                                <li><a href="#anchor-reference" class="sidebar-navigation-link"><@s.text name='manage.metadata.citations.bibliography'/></a></li>
                            </#if>
                        </#if>
                        <li><a href="#anchor-additional" class="sidebar-navigation-link"><@s.text name='manage.metadata.additional.title'/></a></li>
                    </ul>
                </nav>
            </div>

            <div class="bd-content ps-lg-4">
                <span class="anchor anchor-home-resource-page" id="anchor-description"></span>
                <div id="description" class="mt-5 section">
                    <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                        <@s.text name='portal.resource.description'/>
                    </h4>
                    <div property="dc:abstract" class="mt-3 overflow-x-auto">
                        <#if (eml.description?size>0)>
                            <#list eml.description as para>
                                <#if para?has_content>
                                    <p>
                                        <@para?interpret />
                                    </p>
                                </#if>
                            </#list>
                        <#else>
                            <p><@s.text name='portal.resource.no.description'/></p>
                        </#if>
                    </div>
                </div>


                <!-- Dataset must have been published for versions, downloads, and how to cite sections to show -->
                <#if resource.lastPublished??>

                    <!-- data records section, not shown for metadata-only resources -->
                    <#assign recordsByExtensionOrdered = action.getRecordsByExtensionOrdered()/>
                    <#assign recordsByExtensionOrderedNumber = recordsByExtensionOrdered?keys?size -1/>
                    <#assign coreRowType = resource.getCoreRowType()!""/>
                    <#assign coreExt = action.getExtensionManager().get(coreRowType)!/>
                    <#assign coreCount = recordsByExtensionOrdered.get(coreRowType)!recordsPublishedForVersion!0?c/>

                    <#if metadataOnly != true>
                        <span class="anchor anchor-home-resource-page" id="anchor-dataRecords"></span>
                        <div id="dataRecords" class="mt-5 section">
                            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                                <@s.text name='portal.resource.dataRecords'/>
                            </h4>

                            <p>
                                <#if resource.schemaIdentifier??>
                                    <@s.text name='portal.resource.dataRecords.dataSchema.intro'/>
                                <#else>
                                    <@s.text name='portal.resource.dataRecords.intro'><@s.param>${action.getCoreType()?lower_case}</@s.param></@s.text>
                                </#if>
                                <#if coreExt?? && coreExt.name?has_content && coreCount?has_content>
                                    <@s.text name='portal.resource.dataRecords.core'><@s.param>${coreCount}</@s.param></@s.text>
                                </#if>
                            </p>

                            <#if recordsByExtensionOrderedNumber gt 0>
                                <p>
                                    <@s.text name='portal.resource.dataRecords.extensions'><@s.param>${recordsByExtensionOrderedNumber}</@s.param></@s.text>&nbsp;<@s.text name='portal.resource.dataRecords.extensions.coverage'/>
                                </p>

                                <div id="record_graph" class="mb-3 ps-sm-5 ps-3">
                                    <div class="record_graph_inner">
                                        <!-- at top, show bar for core record count to enable comparison against extensions -->
                                        <#if coreExt?? && coreExt.name?has_content && coreCount?has_content>
                                            <div class="row record_graph_row">
                                                <@extensionLink coreExt true/>
                                                <div class="col-lg-8 color-bar">
                                                    ${coreCount?c}
                                                </div>
                                            </div>
                                        </#if>

                                        <!-- below bar for core record count, show bars for extension record counts -->
                                        <#list recordsByExtensionOrdered?keys as k>
                                            <#assign ext = action.getExtensionManager().get(k)!/>
                                            <#assign extCount = recordsByExtensionOrdered.get(k)!/>
                                            <#if coreRowType?has_content && k != coreRowType && ext?? && ext.name?has_content && extCount?has_content>
                                                <div class="row record_graph_row">
                                                    <@extensionLink ext/>
                                                    <div class="col-lg-8 color-bar">
                                                        ${extCount?c}
                                                    </div>
                                                </div>
                                            </#if>
                                        </#list>
                                    </div>
                                </div>
                            </#if>

                            <p>
                                <@s.text name='portal.resource.dataRecords.repository'/>
                            </p>
                        </div>
                    </#if>

                    <!-- versions section -->
                    <#if resource.versionHistory??>
                        <span class="anchor anchor-resource-page" id="anchor-versions"></span>
                        <div id ="versions" class="mt-5 section">
                            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                                <@s.text name='portal.resource.versions'/>
                            </h4>

                            <#if managerRights>
                                <p><@s.text name='portal.resource.versions.verbose.manager'/></p>
                            <#else>
                                <p><@s.text name='portal.resource.versions.verbose'/></p>
                            </#if>
                            <@versionsTable numVersionsShown=3 sEmptyTable="dataTables.sEmptyTable.versions" baseURL=baseURL shortname=resource.shortname />
                            <div id="vtableContainer" class="table-responsive text-smaller"></div>
                        </div>
                    </#if>

                    <!-- citation section -->
                    <#if eml.citation?? && (eml.citation.citation?has_content || eml.citation.identifier?has_content)>
                        <span class="anchor anchor-resource-page" id="anchor-citation"></span>
                        <div id="citation" class="mt-5 section">
                            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                                <@s.text name='portal.resource.cite.howTo'/>
                            </h4>

                            <p>
                                <#if version?? && version.toPlainString() != resource.emlVersion.toPlainString()>
                                    <em class="warn"><@s.text name='portal.resource.latest.version.warning'/>&nbsp;</em>
                                </#if>
                                <@s.text name='portal.resource.cite.help'/>:
                            </p>
                            <p property="dc:bibliographicCitation" class="howtocite mt-3 p-3 overflow-x-auto">
                                <@textWithFormattedLink eml.citation.citation/>
                            </p>
                        </div>
                    </#if>

                    <!-- rights section -->
                    <#if eml.intellectualRights?has_content>
                        <span class="anchor anchor-resource-page" id="anchor-rights"></span>
                        <div id="rights" class="mt-5 section">
                            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                                <@s.text name='eml.intellectualRights.simple'/>
                            </h4>

                            <p><@s.text name='portal.resource.rights.help'/>:</p>
                            <@licenseLogoClass eml.intellectualRights!/>
                            <p property="dc:license">
                                <#if resource.organisation?? && action.getDefaultOrganisation()?? && resource.organisation.key.toString() != action.getDefaultOrganisation().key.toString()>
                                    <@s.text name='portal.resource.rights.organisation'><@s.param>${resource.organisation.name}</@s.param></@s.text>
                                </#if>
                                <#if eml.intellectualRights.contains("CC-BY-NC")>
                                    <#noescape><@s.text name='eml.intellectualRights.licence.ccbync'/></#noescape>
                                <#elseif eml.intellectualRights.contains("CC-BY")>
                                    <#noescape><@s.text name='eml.intellectualRights.licence.ccby'/></#noescape>
                                <#elseif eml.intellectualRights.contains("CC0")>
                                    <#noescape><@s.text name='eml.intellectualRights.licence.cczero'/></#noescape>
                                <#else>
                                    <#noescape>${eml.intellectualRights!}</#noescape>
                                </#if>
                            </p>
                        </div>
                    </#if>

                    <!-- GBIF Registration section -->
                    <span class="anchor anchor-resource-page" id="anchor-gbif"></span>
                    <div id="gbif" class="mt-5 section">
                        <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                            <@s.text name='portal.resource.organisation.key'/>
                        </h4>

                        <#if resource.status=="REGISTERED" && resource.organisation??>
                            <p>
                                <@s.text name='manage.home.registered.verbose'><@s.param>${cfg.portalUrl}/dataset/${resource.key}</@s.param><@s.param>${resource.key}</@s.param></@s.text>
                                <#-- in prod mode link goes to /publisher (GBIF Portal), in dev mode link goes to /publisher (GBIF UAT Portal) -->
                                &nbsp;<@s.text name='manage.home.published.verbose'><@s.param>${cfg.portalUrl}/publisher/${resource.organisation.key}</@s.param><@s.param>${resource.organisation.name}</@s.param><@s.param>${cfg.portalUrl}/node/${resource.organisation.nodeKey!"#"}</@s.param><@s.param>${resource.organisation.nodeName!}</@s.param></@s.text>
                            </p>
                        <#else>
                            <p><@s.text name='manage.home.not.registered.verbose'/></p>
                        </#if>
                    </div>

                    <!-- Keywords section -->
                    <#if eml.subject?has_content>
                        <span class="anchor anchor-resource-page" id="anchor-keywords"></span>
                        <div id="keywords" class="mt-5 section">
                            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                                <@s.text name='portal.resource.summary.keywords'/>
                            </h4>

                            <p property="dc:subject"><@textWithFormattedLink eml.subject!no_description/></p>
                        </div>
                    </#if>

                    <!-- External data section -->
                    <#if (eml.physicalData?size > 0 )>
                        <span class="anchor anchor-resource-page" id="anchor-external"></span>
                        <div id="external" class="mt-5 section">
                            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                                <@s.text name='manage.metadata.physical.alternativeTitle'/>
                            </h4>

                            <p><@s.text name='portal.resource.otherFormats'/></p>

                            <div class="table-responsive">
                                <table class="text-smaller table table-sm table-borderless">
                                    <#list eml.physicalData as item>
                                        <#assign link=eml.physicalData[item_index]/>
                                        <tr property="dc:isFormatOf">
                                            <th class="col-4">${link.name!}</th>
                                            <td>
                                                <a href="${link.distributionUrl}">${link.distributionUrl!"?"}</a>
                                                <#if link.charset?? || link.format?? || link.formatVersion??>
                                                    ${link.charset!} ${link.format!} ${link.formatVersion!}
                                                </#if>
                                            </td>
                                        </tr>
                                    </#list>
                                </table>
                            </div>
                        </div>
                    </#if>

                    <!-- Contacts section -->
                    <#if (eml.contacts?size>0) || (eml.creators?size>0) || (eml.metadataProviders?size>0) || (eml.associatedParties?size>0)>
                        <span class="anchor anchor-resource-page" id="anchor-contacts"></span>
                        <div id="contacts" class="mt-5 section">
                            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                                <@s.text name='portal.resource.contacts'/>
                            </h4>

                            <div class="row g-3 overflow-x-auto">
                                <div class="col-lg-6 col-xl-4">
                                    <p class="text-smaller fw-bold"><@s.text name='portal.resource.creator.intro'/>:</p>
                                    <div>
                                        <@contactList contacts=eml.creators dcPropertyType='creator'/>
                                    </div>
                                </div>

                                <div class="col-lg-6 col-xl-4">
                                    <p class="text-smaller fw-bold"><@s.text name='portal.resource.contact.intro'/>:</p>
                                    <div>
                                        <@contactList contacts=eml.contacts dcPropertyType='mediator'/>
                                    </div>
                                </div>

                                <div class="col-lg-6 col-xl-4">
                                    <p class="text-smaller fw-bold"><@s.text name='portal.metadata.provider.intro'/>:</p>
                                    <div>
                                        <@contactList contacts=eml.metadataProviders dcPropertyType='contributor'/>
                                    </div>
                                </div>

                                <#if (eml.associatedParties?size>0)>
                                    <div class="col-lg-6 col-xl-4">
                                        <p class="text-smaller fw-bold"><@s.text name='portal.associatedParties.intro'/>:</p>
                                        <div>
                                            <@contactList contacts=eml.associatedParties dcPropertyType='contributor'/>
                                        </div>
                                    </div>
                                </#if>
                            </div>
                        </div>
                    </#if>

                    <!-- Geo coverage section -->
                    <#if eml.geospatialCoverages[0]??>
                        <span class="anchor anchor-resource-page" id="anchor-geospatial"></span>
                        <div id="geospatial" class="mt-5 section">
                            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                                <@s.text name='portal.resource.summary.geocoverage'/>
                            </h4>

                            <p property="dc:spatial"><@textWithFormattedLink eml.geospatialCoverages[0].description!no_description/></p>

                            <div class="table-responsive">
                                <table class="text-smaller table table-sm table-borderless">
                                    <tr>
                                        <th class="col-4"><@s.text name='eml.geospatialCoverages.boundingCoordinates'/></th>
                                        <td><@s.text name='eml.geospatialCoverages.boundingCoordinates.min.latitude'/>&nbsp;<@s.text name='eml.geospatialCoverages.boundingCoordinates.min.longitude'/>&nbsp;&#91;${eml.geospatialCoverages[0].boundingCoordinates.min.latitude},&nbsp;${eml.geospatialCoverages[0].boundingCoordinates.min.longitude}&#93;&#44;&nbsp;<@s.text name='eml.geospatialCoverages.boundingCoordinates.max.latitude'/>&nbsp;<@s.text name='eml.geospatialCoverages.boundingCoordinates.max.longitude'/>&nbsp;&#91;${eml.geospatialCoverages[0].boundingCoordinates.max.latitude},&nbsp;${eml.geospatialCoverages[0].boundingCoordinates.max.longitude}&#93;</td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                    </#if>

                    <!-- Taxonomic coverage sections -->
                    <#if ((organizedCoverages?size > 0))>
                        <span class="anchor anchor-resource-page" id="anchor-taxanomic"></span>
                        <div id="taxanomic" class="mt-5 section">
                            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                                <@s.text name='manage.metadata.taxcoverage.title'/>
                            </h4>

                            <#list organizedCoverages as item>
                                <p><@textWithFormattedLink item.description!no_description/></p>

                                <div class="table-responsive">
                                    <table class="text-smaller table table-sm table-borderless">
                                        <#list item.keywords as k>
                                            <#if k.rank?has_content && ranks[k.rank?string]?has_content && (k.displayNames?size > 0) >
                                                <tr>
                                                    <#-- 1st col, write rank name once. Avoid problem accessing "class" from map - it displays "java.util.LinkedHashMap" -->
                                                    <#if k.rank?lower_case == "class">
                                                        <th class="col-4">Class</th>
                                                    <#else>
                                                        <th class="col-4">${ranks[k.rank?html]?cap_first!}</th>
                                                    </#if>
                                                    <#-- 2nd col, write comma separated list of names in format: scientific name (common name) -->
                                                    <td>
                                                        <#list k.displayNames as name>
                                                            &nbsp;${name}<#if name_has_next>,</#if>
                                                        </#list>
                                                    </td>
                                                </tr>
                                            </#if>
                                        </#list>
                                    </table>
                                </div>
                                <#-- give some space between taxonomic coverages -->
                                <#if item_has_next><br></#if>
                            </#list>
                        </div>
                    </#if>

                    <!-- Temporal coverages section -->
                    <#if ((eml.temporalCoverages?size > 0))>
                        <span class="anchor anchor-resource-page" id="anchor-temporal"></span>
                        <div id="temporal" class="mt-5 section">
                            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                                <@s.text name='manage.metadata.tempcoverage.title'/>
                            </h4>

                            <#list eml.temporalCoverages as item>
                                <div class="table-responsive">
                                    <table class="text-smaller table table-sm table-borderless">
                                        <#if ("${item.type}" == "DATE_RANGE") && eml.temporalCoverages[item_index].startDate?? && eml.temporalCoverages[item_index].endDate?? >
                                            <tr>
                                                <th class="col-4"><@s.text name='eml.temporalCoverages.startDate'/> / <@s.text name='eml.temporalCoverages.endDate'/></th>
                                                <td property="dc:temporal">${eml.temporalCoverages[item_index].startDate?date} / ${eml.temporalCoverages[item_index].endDate?date}</td>
                                            </tr>
                                        <#elseif "${item.type}" == "SINGLE_DATE" && eml.temporalCoverages[item_index].startDate?? >
                                            <tr>
                                                <th class="col-4"><@s.text name='eml.temporalCoverages.startDate'/></th>
                                                <td property="dc:temporal">${eml.temporalCoverages[item_index].startDate?date}</td>
                                            </tr>
                                        <#elseif "${item.type}" == "FORMATION_PERIOD" && eml.temporalCoverages[item_index].formationPeriod?? >
                                            <tr>
                                                <th class="col-4"><@s.text name='eml.temporalCoverages.formationPeriod'/></th>
                                                <td property="dc:temporal">${eml.temporalCoverages[item_index].formationPeriod}</td>
                                            </tr>
                                        <#elseif eml.temporalCoverages[item_index].livingTimePeriod??> <!-- LIVING_TIME_PERIOD -->
                                            <tr>
                                                <th class="col-4"><@s.text name='eml.temporalCoverages.livingTimePeriod'/></th>
                                                <td property="dc:temporal">${eml.temporalCoverages[item_index].livingTimePeriod!}</td>
                                            </tr>
                                        </#if>
                                    </table>
                                </div>
                            </#list>
                        </div>
                    </#if>

                    <!-- Project section -->
                    <#if eml.project?? && eml.project.title?has_content>
                        <span class="anchor anchor-resource-page" id="anchor-project"></span>
                        <div id="project" class="mt-5 section">
                            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                                <@s.text name='manage.metadata.project.title'/>
                            </h4>

                            <p><@textWithFormattedLink eml.project.description!no_description/></p>

                            <div class="table-responsive">
                                <table class="text-smaller table table-sm table-borderless">
                                    <#if eml.project.title?has_content>
                                        <tr>
                                            <th class="col-4"><@s.text name='eml.project.title'/></th>
                                            <td><@textWithFormattedLink eml.project.title!/></td>
                                        </tr>
                                    </#if>
                                    <#if eml.project.identifier?has_content>
                                        <tr>
                                            <th class="col-4"><@s.text name='eml.project.identifier'/></th>
                                            <td><@textWithFormattedLink eml.project.identifier!/></td>
                                        </tr>
                                    </#if>
                                    <#if eml.project.funding?has_content>
                                        <tr>
                                            <th class="col-4"><@s.text name='eml.project.funding'/></th>
                                            <td><@textWithFormattedLink eml.project.funding/></td>
                                        </tr>
                                    </#if>
                                    <#if eml.project.studyAreaDescription.descriptorValue?has_content>
                                        <tr>
                                            <th class="col-4"><@s.text name='eml.project.studyAreaDescription.descriptorValue'/></th>
                                            <td><@textWithFormattedLink eml.project.studyAreaDescription.descriptorValue/></td>
                                        </tr>
                                    </#if>
                                    <#if eml.project.designDescription?has_content>
                                        <tr>
                                            <th class="col-4"><@s.text name='eml.project.designDescription'/></th>
                                            <td><@textWithFormattedLink eml.project.designDescription/></td>
                                        </tr>
                                    </#if>
                                </table>
                            </div>

                            <#if (eml.project.personnel?size >0)>
                                <br>
                                <p class="text-smaller fw-bold"><@s.text name='eml.project.personnel.intro'/>:</p>
                                <div>
                                    <@contactList eml.project.personnel/>
                                </div>
                                <div class="clearfix"></div>
                            </#if>
                        </div>
                    </#if>

                    <!-- Sampling methods section -->
                    <#if eml.studyExtent?has_content || eml.sampleDescription?has_content || eml.qualityControl?has_content || (eml.methodSteps?? && (eml.methodSteps?size>=1) && eml.methodSteps[0]?has_content) >
                        <span class="anchor anchor-resource-page" id="anchor-methods"></span>
                        <div id="methods" class="mt-5 section">
                            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                                <@s.text name='manage.metadata.methods.title'/>
                            </h4>

                            <p class="overflow-x-auto">
                                <@textWithFormattedLink eml.sampleDescription!no_description/>
                            </p>

                            <div class="table-responsive">
                                <table class="text-smaller table table-sm table-borderless">
                                    <#if eml.studyExtent?has_content>
                                        <tr>
                                            <th class="col-4"><@s.text name='eml.studyExtent'/></th>
                                            <td><@textWithFormattedLink eml.studyExtent/></td>
                                        </tr>
                                    </#if>

                                    <#if eml.qualityControl?has_content>
                                        <tr>
                                            <th class="col-4"><@s.text name='eml.qualityControl'/></th>
                                            <td><@textWithFormattedLink eml.qualityControl/></td>
                                        </tr>
                                    </#if>
                                </table>
                            </div>

                            <#if (eml.methodSteps?? && (eml.methodSteps?size>=1) && eml.methodSteps[0]?has_content)>
                                <p class="overflow-x-auto">
                                    <@s.text name='rtf.methods.description'/>&#58;
                                </p>
                                <ol class="overflow-x-auto">
                                    <#list eml.methodSteps as item>
                                        <#if (eml.methodSteps[item_index]?has_content)>
                                            <li>
                                                <@textWithFormattedLink eml.methodSteps[item_index]/>
                                            </li>
                                        </#if>
                                    </#list>
                                </ol>
                            </#if>
                        </div>
                    </#if>

                    <!-- Collections section -->
                    <#if eml.collections?? && (eml.collections?size > 0) && eml.collections[0].collectionName?has_content >
                        <span class="anchor anchor-resource-page" id="anchor-collection"></span>
                        <div id="collection" class="mt-5 section">
                            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                                <@s.text name='manage.metadata.collections.title'/>
                            </h4>

                            <#list eml.collections as item>
                                <div class="table-responsive">
                                    <table class="text-smaller table table-sm table-borderless">
                                        <#if item.collectionName?has_content>
                                            <tr>
                                                <th class="col-4"><@s.text name='eml.collectionName'/></th>
                                                <td>${item.collectionName!}</td>
                                            </tr>
                                        </#if>
                                        <#if item.collectionId?has_content>
                                            <tr>
                                                <th class="col-4"><@s.text name='eml.collectionId'/></th>
                                                <td>${item.collectionId!}</td>
                                            </tr>
                                        </#if>
                                        <#if item.parentCollectionId?has_content>
                                            <tr>
                                                <th class="col-4"><@s.text name='eml.parentCollectionId'/></th>
                                                <td>${item.parentCollectionId!}</td>
                                            </tr>
                                        </#if>
                                    </table>
                                </div>
                            </#list>

                            <#if eml.specimenPreservationMethods?? && (eml.specimenPreservationMethods?size>0) && eml.specimenPreservationMethods[0]?has_content >
                                <div class="table-responsive">
                                    <table class="text-smaller table table-sm table-borderless">
                                            <tr>
                                                <th class="col-4"><@s.text name='eml.specimenPreservationMethod.plural'/></th>
                                                <td>
                                                    <#list eml.specimenPreservationMethods as item>
                                                        ${preservationMethods[item]?cap_first!}<#if item_has_next>,&nbsp;</#if>
                                                    </#list>
                                                </td>
                                            </tr>
                                        </table>
                                </div>
                            </#if>

                            <#if eml.jgtiCuratorialUnits?? && (eml.jgtiCuratorialUnits?size>0) && eml.jgtiCuratorialUnits[0]?has_content>
                                <div class="table-responsive">
                                    <table class="text-smaller table table-sm table-borderless">
                                            <tr>
                                                <th class="col-4"><@s.text name='manage.metadata.collections.curatorialUnits.title'/></th>
                                                <td>
                                                    <#list eml.jgtiCuratorialUnits as item>
                                                        <#if item.type=="COUNT_RANGE">
                                                            <@s.text name='eml.jgtiCuratorialUnits.rangeStart'/>&nbsp;${eml.jgtiCuratorialUnits[item_index].rangeStart}
                                                            <@s.text name='eml.jgtiCuratorialUnits.rangeEnd'/>&nbsp;${eml.jgtiCuratorialUnits[item_index].rangeEnd}
                                                            ${eml.jgtiCuratorialUnits[item_index].unitType}
                                                        <#else>
                                                            <@s.text name='eml.jgtiCuratorialUnits.rangeMean'/>&nbsp;${eml.jgtiCuratorialUnits[item_index].rangeMean}
                                                            <@s.text name='eml.jgtiCuratorialUnits.uncertaintyMeasure'/>&nbsp;${eml.jgtiCuratorialUnits[item_index].uncertaintyMeasure}
                                                            ${eml.jgtiCuratorialUnits[item_index].unitType}
                                                        </#if>
                                                        <#if item_has_next>,&nbsp;</#if>
                                                    </#list>
                                                </td>
                                            </tr>
                                        </table>
                                </div>
                            </#if>
                        </div>
                    </#if>

                    <!-- bibliographic citations section -->
                    <#if eml.bibliographicCitationSet?? && (eml.bibliographicCitationSet.bibliographicCitations?has_content)>
                        <span class="anchor anchor-resource-page" id="anchor-reference"></span>
                        <div id="reference" class="mt-5 section">
                            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                                <@s.text name='manage.metadata.citations.bibliography'/>
                            </h4>

                            <ol class="overflow-x-auto">
                                <#list eml.bibliographicCitationSet.bibliographicCitations as item>
                                    <#if item.citation?has_content>
                                        <li property="dc:references">
                                            <@textWithFormattedLink item.citation/>
                                            <@textWithFormattedLink item.identifier!/>
                                        </li>
                                    </#if>
                                </#list>
                            </ol>
                        </div>
                    </#if>
                </#if>

                <!-- Additional metadata section -->
                <#if eml.additionalInfo?has_content || eml.purpose?has_content || (eml.alternateIdentifiers?size > 0 )>
                    <span class="anchor anchor-resource-page" id="anchor-additional"></span>
                    <div id="additional" class="mt-5 section">

                        <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                            <@s.text name='manage.metadata.additional.title'/>
                        </h4>

                        <div>
                            <#if eml.additionalInfo?has_content>
                                <p class="overflow-x-auto"><@textWithFormattedLink eml.additionalInfo/></p>
                            </#if>
                            <div class="table-responsive">
                                <table class="text-smaller table table-sm table-borderless">
                                    <#if eml.purpose?has_content>
                                        <tr>
                                            <th class="col-4"><@s.text name='eml.purpose'/></th>
                                            <td><@textWithFormattedLink eml.purpose/></td>
                                        </tr>
                                    </#if>
                                    <#if eml.updateFrequencyDescription?has_content>
                                        <tr>
                                            <th class="col-4"><@s.text name='eml.updateFrequencyDescription'/></th>
                                            <td><@textWithFormattedLink eml.updateFrequencyDescription/></td>
                                        </tr>
                                    </#if>
                                    <#if (eml.alternateIdentifiers?size > 0)>
                                        <#list eml.alternateIdentifiers as item>
                                            <tr>
                                                <th class="col-4"><#if item_index ==0><@s.text name='manage.metadata.alternateIdentifiers.title'/></#if></th>
                                                <td><@textWithFormattedLink eml.alternateIdentifiers[item_index]!/></td>
                                            </tr>
                                        </#list>
                                    </#if>
                                </table>
                            </div>
                        </div>
                    </div>
                </#if>

            </div>
        </main>
    </div>
</div>


<#include "/WEB-INF/pages/inc/footer.ftl">

<!-- data record line chart -->
<script src="${baseURL}/js/graphs.js"></script>

<script src="${baseURL}/js/jconfirmation.jquery.js"></script>

<!-- Menu Toggle Script -->
<script>
    // hide and make contact addresses toggable
    $(".contactName").next().hide();
    $(".contactName").click(function(e){
        $(this).next().slideToggle("fast");
    });

    $('.confirmDeleteVersion').jConfirmAction({
        titleQuestion : "<@s.text name="basic.confirm"/>",
        question : "<@s.text name='portal.resource.confirm.delete.version'/></br></br><@s.text name='portal.resource.confirm.delete.version.warning.citation'/></br></br><@s.text name='portal.resource.confirm.delete.version.warning.undone'/>",
        yesAnswer : "<@s.text name='basic.yes'/>",
        cancelAnswer : "<@s.text name='basic.no'/>",
        buttonType: "danger"
    });

    $(function() {
        <#if action.getRecordsByExtensionOrdered()?size gt 1>
        var graph = $("#record_graph");
        var maxRecords = ${action.getMaxRecordsInExtension()?c!5000};
        // max 350px
        graph.bindRecordBars( (350-((maxRecords+"").length)*10) / maxRecords);
        </#if>
    });

    // show extension description in tooltip in data records section
    $(function() {
        $('.ext-tooltip').tooltip({track: true});
    });

</script>

</#escape>
