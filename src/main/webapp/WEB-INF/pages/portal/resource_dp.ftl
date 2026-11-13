<#-- @ftlvariable name="" type="org.gbif.ipt.action.portal.ResourceAction" -->
<#include "/WEB-INF/pages/inc/header.ftl">
<title>${dpMetadata.title!"IPT"}</title>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<#include "/WEB-INF/pages/macros/portal_resource.ftl"/>
<link rel="stylesheet" href="${baseURL}/styles/leaflet/leaflet-1.9.4.css">
<link rel="stylesheet" href="${baseURL}/styles/portal-resource.css">

<#assign download_dp_url>${baseURL}/archive.do?r=${resource.shortname}<#if version??>&v=${version.toPlainString()}</#if></#assign>
<#assign download_metadata_url>${baseURL}/metadata.do?r=${resource.shortname}&v=<#if version??>${version.toPlainString()}<#else>${resource.metadataVersion.toPlainString()}</#if></#assign>
<#assign isPreviewPage = action.isPreview() />

<style>
    :root {
        --navbar-height: 90px;
    }

    .section {
        scroll-margin-top: var(--navbar-height);
    }
</style>

<script src="${baseURL}/js/scrollspy.js"></script>
<script>
    $(document).ready(function () {
        // Initialize scrollspy
        IptScrollSpy.init({tocSelector: '.bd-toc nav a'});
    })
</script>

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<@previewNotice nextVersion=resource.metadataVersion.toPlainString()/>

<div class="container-fluid bg-body border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3">
            <div class="text-center fs-smaller">
                <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);"
                     aria-label="breadcrumb">
                    <ol class="breadcrumb justify-content-center mb-0">
                        <li class="breadcrumb-item"><a href="${baseURL}"><@s.text name="breadcrumb.home"/></a></li>
                        <li class="breadcrumb-item active"
                            aria-current="page"><@s.text name="breadcrumb.resource"/></li>
                    </ol>
                </nav>
            </div>

            <div class="text-center">
                <h1 class="rtitle pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    ${dpMetadata.title!resource.shortname}
                </h1>

                <div class="text-center">
                    <span class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill type-${resourceTypeLowerCase} me-1"><@s.text name="portal.resource.type.${resourceTypeLowerCase}"/></span>
                </div>

                <@versionLine latestVersion=resource.metadataVersion.toPlainString() pubDate=(dpMetadata.created)!""/>

                <#if resource.lastPublished??>
                    <div class="mt-2">
                        <@dataPackageDownloadButtons metadataFile="datapackage.json" metadataKey="portal.resource.download.datapackageJson.description"/>
                        <#if managerRights>
                            <a href="${baseURL}/manage/resource.do?r=${resource.shortname}"
                               class="btn btn-sm btn-outline-secondary mt-1 me-xl-1 top-button">
                                <@s.text name='button.edit'/>
                            </a>
                        </#if>
                    </div>
                </#if>
            </div>
        </div>
    </div>
</div>

<#-- a GeoJSON geometry's coordinates (nested lists of numbers) as JSON -->
<#function coordinatesJson value>
    <#if value?is_number><#return value?c></#if>
    <#if value?is_enumerable>
        <#local parts = []/>
        <#list value as v><#local parts = parts + [coordinatesJson(v)]/></#list>
        <#return "[" + parts?join(",") + "]"/>
    </#if>
    <#return "null"/>
</#function>

<#assign isPublished = resource.lastPublished??/>
<#assign isOldVersion = version?? && version.toPlainString() != resource.metadataVersion.toPlainString()/>
<#assign hasCitation = (dpMetadata.bibliographicCitation)?has_content/>
<#assign hasLicenses = (dpMetadata.licenses)?has_content/>
<#assign spatial = (dpMetadata.spatial)!/>
<#assign hasSpatial = spatial?has_content && (spatial.coordinates)?has_content/>
<#assign hasTemporal = (dpMetadata.temporal)?? && (dpMetadata.temporal.start)?has_content/>
<#assign hasTaxonomic = (dpMetadata.taxonomic)?has_content/>
<#assign hasContributors = (dpMetadata.contributors)?has_content/>
<#assign hasProject = (dpMetadata.project)??/>
<#assign isRegistered = resource.status == "REGISTERED" && resource.key??/>
<#assign hasMore = isRegistered || (dpMetadata.keywords)?has_content || (dpMetadata.sources)?has_content || (dpMetadata.references)?has_content || (dpMetadata.relatedIdentifiers)?has_content/>

<div id="sections" class="container-fluid bg-body">
    <div class="container mb-md-4 bd-layout main-content-container">
        <main class="bd-main">
            <div class="bd-toc mt-4 pt-3 ps-3 mb-lg-5 text-muted">
                <nav id="sidebar-content">
                    <ul>
                        <li class="rp-toc-group"><@s.text name='portal.resource.toc.about'/></li>
                        <li><a href="#anchor-description" class="sidebar-navigation-link"><@s.text name='portal.resource.description'/></a></li>
                        <#if isPublished && (hasCitation || hasLicenses)>
                            <li><a href="#anchor-citation" class="sidebar-navigation-link"><@s.text name='portal.resource.citationLicense'/></a></li>
                        </#if>
                        <#if isPublished>
                            <li class="rp-toc-group"><@s.text name='portal.resource.toc.data'/></li>
                            <li><a href="#anchor-dataRecords" class="sidebar-navigation-link"><@s.text name='portal.resource.dataRecords'/></a></li>
                            <#if resource.versionHistory??>
                                <li><a href="#anchor-versions" class="sidebar-navigation-link"><@s.text name='portal.resource.versions'/></a></li>
                            </#if>
                            <#if hasSpatial || hasTemporal || hasTaxonomic>
                                <li><a href="#anchor-coverage" class="sidebar-navigation-link"><@s.text name='portal.resource.coverage'/></a></li>
                            </#if>
                            <#if hasContributors || hasProject || hasMore>
                                <li class="rp-toc-group"><@s.text name='portal.resource.toc.context'/></li>
                                <#if hasContributors>
                                    <li><a href="#anchor-contributors" class="sidebar-navigation-link"><@s.text name='portal.resource.contributors'/></a></li>
                                </#if>
                                <#if hasProject>
                                    <li><a href="#anchor-project" class="sidebar-navigation-link"><@s.text name='portal.resource.project'/></a></li>
                                </#if>
                                <#if hasMore>
                                    <li><a href="#anchor-more" class="sidebar-navigation-link"><@s.text name='portal.resource.more'/></a></li>
                                </#if>
                            </#if>
                        </#if>
                    </ul>
                </nav>
            </div>

            <div class="bd-content ps-lg-4 pt-5">
                <span class="anchor anchor-home-resource-page" id="anchor-description"></span>
                <section id="description" class="rp-section section">
                    <h2 class="rp-h"><@s.text name='portal.resource.description'/></h2>
                    <div class="rp-description overflow-x-auto">
                        <@resourceLogo url=(dpMetadata.image)!""/>
                        <div class="rp-prose">
                            <#if (dpMetadata.description)?has_content>
                                <p>${dpMetadata.description?no_esc}</p>
                            <#else>
                                <p><@s.text name='portal.resource.no.description'/></p>
                            </#if>
                        </div>
                    </div>
                </section>

                <#if isPublished>
                    <#if hasCitation || hasLicenses>
                        <span class="anchor anchor-home-resource-page" id="anchor-citation"></span>
                        <section id="citation" class="rp-section section">
                            <h2 class="rp-h"><@s.text name='portal.resource.citationLicense'/></h2>
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
                                    <p id="rp-citation-text" class="howtocite overflow-x-auto mb-0">${dpMetadata.bibliographicCitation}</p>
                                </div>
                            </#if>
                            <#if hasLicenses>
                                <#list dpMetadata.licenses as license>
                                    <#if license??>
                                        <div class="rp-license">
                                            <@ccLicense (license.name)!""/>
                                            <p class="mb-0">
                                                <#if (license.scope)??><span class="text-muted"><@s.text name="portal.resource.license.scope"/>:</span> ${license.scope}<#if !isCcLicense((license.name)!"")>.</#if></#if>
                                                <#if !isCcLicense((license.name)!"")><#if license.title?has_content>${license.title}<#elseif license.name?has_content>${license.name}</#if></#if>
                                                <#if license.path?has_content><a href="${license.path}" class="break-all">${license.path}</a></#if>
                                            </p>
                                        </div>
                                    </#if>
                                </#list>
                            </#if>
                        </section>
                    </#if>

                    <#-- data records: one bar per table, largest first -->
                    <#assign recordsByExtensionOrdered = action.getRecordsByExtensionOrdered()/>
                    <#assign maxRecords = action.getMaxRecordsInExtension()/>
                    <span class="anchor anchor-home-resource-page" id="anchor-dataRecords"></span>
                    <section id="dataRecords" class="rp-section section">
                        <h2 class="rp-h"><@s.text name='portal.resource.dataRecords'/></h2>
                        <p class="text-muted text-smaller"><@s.text name='portal.resource.dataRecords.dataPackageSchema.intro'/></p>
                        <div class="rp-bars">
                            <#list recordsByExtensionOrdered?keys as k>
                                <#assign tableCount = recordsByExtensionOrdered.get(k)!0/>
                                <div class="rp-bar-label">${k}</div>
                                <div class="rp-bar"><i style="width:<#if (maxRecords > 0)>${(tableCount * 100 / maxRecords)?c}<#else>0</#if>%"></i></div>
                                <div class="rp-bar-n">${tableCount}</div>
                            </#list>
                        </div>
                    </section>

                    <@versionsSection latestVersion=resource.metadataVersion.toPlainString()/>

                    <#if hasSpatial || hasTemporal || hasTaxonomic>
                        <span class="anchor anchor-home-resource-page" id="anchor-coverage"></span>
                        <section id="coverage" class="rp-section section">
                            <h2 class="rp-h"><@s.text name='portal.resource.coverage'/></h2>
                            <div class="rp-coverage<#if hasSpatial && (hasTaxonomic || hasTemporal)> rp-coverage-2</#if>">
                                <#if hasSpatial>
                                    <div id="geographic">
                                        <h3 class="rp-h3"><@s.text name='portal.resource.geographic'/></h3>
                                        <div id="rp-map" class="rp-map" data-geojson="{&quot;type&quot;:&quot;${spatial.type}&quot;,&quot;coordinates&quot;:${coordinatesJson(spatial.coordinates)}}"></div>
                                        <#if (dpMetadata.coordinatePrecision)??>
                                            <div class="rp-cov-desc"><@s.text name='portal.resource.spatial.coordinatePrecision'/>: ${dpMetadata.coordinatePrecision?c}</div>
                                        </#if>
                                    </div>
                                </#if>

                                <#if hasTemporal || hasTaxonomic>
                                    <div>
                                        <#if hasTemporal>
                                            <#assign tStart = dpMetadata.temporal.start/>
                                            <#assign tEnd = (dpMetadata.temporal.end)!tStart/>
                                            <div id="temporal">
                                                <h3 class="rp-h3"><@s.text name='portal.resource.temporal'/></h3>
                                                <#if tStart?matches("\\d{4}-\\d{2}-\\d{2}.*") && tEnd?matches("\\d{4}-\\d{2}-\\d{2}.*")>
                                                    <div class="rp-timeline" aria-hidden="true" data-ranges="[[&quot;${tStart[0..9]}&quot;,&quot;${tEnd[0..9]}&quot;]]"></div>
                                                </#if>
                                                <ul class="rp-cov-desc list-unstyled">
                                                    <li>${tStart} – ${tEnd}</li>
                                                </ul>
                                            </div>
                                        </#if>

                                        <#if hasTaxonomic>
                                            <#-- taxa grouped by rank, in the order the ranks first appear -->
                                            <#assign taxonRanks = []/>
                                            <#list dpMetadata.taxonomic as tx>
                                                <#assign r = (tx.taxonRank)???then(tx.taxonRank?string, "")/>
                                                <#if !taxonRanks?seq_contains(r)><#assign taxonRanks = taxonRanks + [r]/></#if>
                                            </#list>
                                            <div id="taxonomic">
                                                <h3 class="rp-h3"><@s.text name='portal.resource.taxonomic'/></h3>
                                                <div class="rp-taxa">
                                                    <#list taxonRanks as r>
                                                        <#assign taxa = dpMetadata.taxonomic?filter(tx -> ((tx.taxonRank)???then(tx.taxonRank?string, "")) == r)/>
                                                        <div class="rp-rank">
                                                            <span class="rp-rank-name"><#if r?has_content>${r?cap_first}<#else><@s.text name='portal.resource.taxonomic.scientificName'/></#if></span>
                                                            <span class="rp-chips">
                                                                <#list taxa as tx>
                                                                    <span class="rp-chip"<#if (tx?index >= 12)> hidden</#if><#if tx.taxonID?has_content> title="${tx.taxonID}"</#if>><i>${tx.scientificName!}</i><#if (tx.vernacularNames)?has_content> <small class="text-muted"><#list tx.vernacularNames as lang, name>${name}<#sep>, </#sep></#list></small></#if></span>
                                                                </#list>
                                                                <#if (taxa?size > 12)>
                                                                    <button type="button" class="rp-chip rp-chip-more">+${taxa?size - 12}</button>
                                                                </#if>
                                                            </span>
                                                        </div>
                                                    </#list>
                                                </div>
                                            </div>
                                        </#if>
                                    </div>
                                </#if>
                            </div>
                        </section>
                    </#if>

                    <#if hasContributors>
                        <span class="anchor anchor-home-resource-page" id="anchor-contributors"></span>
                        <section id="contributors" class="rp-section section">
                            <h2 class="rp-h"><@s.text name='portal.resource.contributors'/></h2>
                            <div class="rp-people">
                                <#list dpMetadata.contributors as c>
                                    <#if c??>
                                        <#assign cOrg = (c.organization?has_content && c.organization != c.title!"")?then(c.organization, "")/>
                                        <@personRow name=(c.title)!"" org=cOrg roleLabels=c.role?has_content?then([c.role?cap_first], []) email=c.email!"">
                                            <@personDetail label=action.getText('eml.contact.email')><#if c.email?has_content><a href="mailto:${c.email}" class="break-all">${c.email}</a></#if></@personDetail>
                                            <@personDetail label=action.getText('eml.contact.homepage')><#if c.path?has_content><a href="${c.path}" class="break-all">${c.path}</a></#if></@personDetail>
                                        </@personRow>
                                    </#if>
                                </#list>
                            </div>
                        </section>
                    </#if>

                    <#if hasProject>
                        <#assign project = dpMetadata.project/>
                        <span class="anchor anchor-home-resource-page" id="anchor-project"></span>
                        <section id="project" class="rp-section section">
                            <h2 class="rp-h"><@s.text name='portal.resource.project'/></h2>
                            <#if project.description?has_content>
                                <div class="rp-prose mb-3">${project.description}</div>
                            </#if>
                            <dl class="rp-kv">
                                <#if project.title?has_content><dt><@s.text name='portal.resource.project.title'/></dt><dd>${project.title}</dd></#if>
                                <#if project.id?has_content><dt><@s.text name='portal.resource.project.id'/></dt><dd>${project.id}</dd></#if>
                                <#if project.path?has_content><dt><@s.text name='portal.resource.project.path'/></dt><dd><a href="${project.path}" class="break-all">${project.path}</a></dd></#if>
                                <#if (project.samplingDesign)??><dt><@s.text name='portal.resource.project.samplingDesign'/></dt><dd>${project.samplingDesign}</dd></#if>
                                <#if project.captureMethod?has_content><dt><@s.text name='portal.resource.project.captureMethod'/></dt><dd><#list project.captureMethod as m>${m}<#sep>, </#sep></#list></dd></#if>
                                <#if project.observationLevel?has_content><dt><@s.text name='portal.resource.project.observationLevel'/></dt><dd><#list project.observationLevel as l>${l}<#sep>, </#sep></#list></dd></#if>
                                <#if (project.individualAnimals)??><dt><@s.text name='portal.resource.project.individualAnimals'/></dt><dd><#if project.individualAnimals><@s.text name='basic.yes'/><#else><@s.text name='basic.no'/></#if></dd></#if>
                            </dl>
                        </section>
                    </#if>

                    <#-- the long tail: folded -->
                    <#if hasMore>
                        <span class="anchor anchor-home-resource-page" id="anchor-more"></span>
                        <section id="more" class="rp-section section">
                            <h2 class="rp-h"><@s.text name='portal.resource.more'/></h2>

                            <#if (dpMetadata.keywords)?has_content>
                                <details id="keywords" class="rp-more" open>
                                    <summary><@s.text name='portal.resource.keywords'/></summary>
                                    <div class="rp-keywords">
                                        <#list dpMetadata.keywords as kw><#if kw?has_content><span>${kw}</span></#if></#list>
                                    </div>
                                </details>
                            </#if>

                            <#if (dpMetadata.sources)?has_content>
                                <details id="sources" class="rp-more">
                                    <summary><@s.text name='portal.resource.sources'/> (${dpMetadata.sources?size})</summary>
                                    <#list dpMetadata.sources as source>
                                        <dl class="rp-kv<#if source?has_next> mb-3</#if>">
                                            <dt><@s.text name="portal.resource.source.title"/></dt><dd>${source.title!}</dd>
                                            <#if source.path?has_content><dt><@s.text name="portal.resource.source.path"/></dt><dd><a href="${source.path}" class="break-all">${source.path}</a></dd></#if>
                                            <#if source.email?has_content><dt><@s.text name="portal.resource.source.email"/></dt><dd><a href="mailto:${source.email}">${source.email}</a></dd></#if>
                                            <#assign sourceVersion = (source.version)!((source.additionalProperties['version'])!"")/>
                                            <#if sourceVersion?has_content><dt><@s.text name="portal.resource.source.version"/></dt><dd>${sourceVersion}</dd></#if>
                                        </dl>
                                    </#list>
                                </details>
                            </#if>

                            <#if (dpMetadata.references)?has_content>
                                <details id="references" class="rp-more">
                                    <summary><@s.text name='portal.resource.references'/> (${dpMetadata.references?size})</summary>
                                    <ol class="overflow-x-auto">
                                        <#list dpMetadata.references as ref><li><@textWithFormattedLink ref/></li></#list>
                                    </ol>
                                </details>
                            </#if>

                            <#if (dpMetadata.relatedIdentifiers)?has_content>
                                <details id="relatedidentifiers" class="rp-more">
                                    <summary><@s.text name='portal.resource.relatedIdentifiers'/> (${dpMetadata.relatedIdentifiers?size})</summary>
                                    <#list dpMetadata.relatedIdentifiers as ri>
                                        <dl class="rp-kv<#if ri?has_next> mb-3</#if>">
                                            <dt><@s.text name="portal.resource.relatedIdentifier.relatedIdentifier"/></dt><dd class="break-all"><@textWithFormattedLink ri.relatedIdentifier!/></dd>
                                            <#if (ri.relationType)??><dt><@s.text name="portal.resource.relatedIdentifier.relationType"/></dt><dd>${ri.relationType}</dd></#if>
                                            <#if (ri.resourceTypeGeneral)??><dt><@s.text name="portal.resource.relatedIdentifier.resourceTypeGeneral"/></dt><dd>${ri.resourceTypeGeneral}</dd></#if>
                                            <#if (ri.relatedIdentifierType)??><dt><@s.text name="portal.resource.relatedIdentifier.relatedIdentifierType"/></dt><dd>${ri.relatedIdentifierType}</dd></#if>
                                        </dl>
                                    </#list>
                                </details>
                            </#if>
                            <#if isRegistered>
                                <div id="gbif" class="rp-machine">
                                    <span class="fw-bold"><@s.text name='portal.resource.organisation.key'/>:</span>
                                    <a href="${cfg.portalUrl}/dataset/${resource.key}">${cfg.portalUrl}/dataset/${resource.key}</a>
                                </div>
                            </#if>
                        </section>
                    </#if>
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
        titleQuestion: "<@s.text name="basic.confirm"/>",
        question: "<@s.text name='portal.resource.confirm.delete.version'/></br></br><@s.text name='portal.resource.confirm.delete.version.warning.citation'/></br></br><@s.text name='portal.resource.confirm.delete.version.warning.undone'/>",
        yesAnswer: "<@s.text name='basic.yes'/>",
        cancelAnswer: "<@s.text name='basic.no'/>",
        buttonType: "danger",
        baseUrl: "${baseURL}"
    });
</script>
