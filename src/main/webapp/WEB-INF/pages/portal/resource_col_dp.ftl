<#-- @ftlvariable name="" type="org.gbif.ipt.action.portal.ResourceAction" -->
<#include "/WEB-INF/pages/inc/header.ftl">
<title>${dpMetadata.title!"IPT"}</title>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<#include "/WEB-INF/pages/macros/portal_resource.ftl"/>
<#-- ColDP issued: a date in possibly truncated ISO format (2024, 2024-05, 2024-05-21), in the visitor's format -->
<#assign colDpIssued = ((dpMetadata.issued)?string)!""/>
<#if colDpIssued?matches("\\d{4}-\\d{2}-\\d{2}")>
    <#assign colDpIssued = colDpIssued?date("yyyy-MM-dd")?string.long/>
<#elseif colDpIssued?matches("\\d{4}-\\d{2}")>
    <#assign colDpIssued = colDpIssued?date("yyyy-MM")?string("MMMM yyyy")/>
</#if>
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
                <h1 property="dc:title" class="rtitle pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    ${dpMetadata.title!resource.shortname}
                </h1>

                <div class="text-center">
                    <span class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill type-${resourceTypeLowerCase} me-1"><@s.text name="portal.resource.type.${resourceTypeLowerCase}"/></span>
                </div>

                <@versionLine latestVersion=resource.metadataVersion.toPlainString() pubDateText=colDpIssued/>

                <#if resource.lastPublished??>
                    <div class="mt-2">
                        <@dataPackageDownloadButtons metadataFile="metadata.yaml" metadataKey="portal.resource.download.metadataYaml.description"/>
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

<#-- the ccLicense code of a ColDP license, read as MetadataUtils.colDpLicenseToGbifCode reads it; "" for any other -->
<#assign colDpLicenseCodes = {"CC0": "CC0-1.0", "CC01.0": "CC0-1.0", "CCBY": "CC-BY-4.0", "CCBY4.0": "CC-BY-4.0", "CCBYNC": "CC-BY-NC-4.0", "CCBYNC4.0": "CC-BY-NC-4.0"}/>
<#assign colDpLicense = (dpMetadata.license)!""/>
<#assign colDpLicenseCode = colDpLicenseCodes[colDpLicense?upper_case?replace("[\\s_-]", "", "r")]!""/>

<#-- an ORCID or ROR given as an identifier or a URL, as a URL -->
<#function idUrl id base>
    <#if id?starts_with("http")><#return id/></#if>
    <#return base + id/>
</#function>

<#-- people: each agent once (by name, or by organisation), with all their roles in the order of the roles below -->
<#assign peopleKeys = []/>
<#assign peopleAgents = {}/>
<#assign peopleRoles = {}/>
<#macro addAgent a role>
    <#if a?is_hash>
        <#local key = ((a.given!"") + "|" + (a.family!"") + "|" + ((a.given?has_content || a.family?has_content)?then("", a.organisation!"")))?lower_case?trim/>
        <#if key != "||">
            <#if !peopleKeys?seq_contains(key)>
                <#assign peopleKeys = peopleKeys + [key]/>
                <#assign peopleAgents = peopleAgents + {key: a}/>
                <#assign peopleRoles = peopleRoles + {key: [role]}/>
            <#elseif !peopleRoles[key]?seq_contains(role)>
                <#assign peopleRoles = peopleRoles + {key: peopleRoles[key] + [role]}/>
            </#if>
        </#if>
    </#if>
</#macro>
<#list (dpMetadata.creator)![] as c><@addAgent a=c role="creator"/></#list>
<@addAgent a=(dpMetadata.contact)!"" role="contact"/>
<#list (dpMetadata.editor)![] as c><@addAgent a=c role="editor"/></#list>
<@addAgent a=(dpMetadata.publisher)!"" role="publisher"/>
<#list (dpMetadata.contributor)![] as c><@addAgent a=c role="contributor"/></#list>

<#assign isPublished = resource.lastPublished??/>
<#assign doi = ((dpMetadata.doi)??)?then(dpMetadata.doi?string, "")/>
<#assign temporalScope = (dpMetadata.temporalScope)!""/>
<#assign hasCoverage = (dpMetadata.geographicScope)?has_content || temporalScope?has_content || (dpMetadata.taxonomicScope)?has_content/>
<#assign hasMore = (dpMetadata.source)?has_content || (dpMetadata.identifier)?has_content || (dpMetadata.notes)?has_content
    || (dpMetadata.alias)?has_content || (dpMetadata.url)?? || (dpMetadata.issn)?has_content || (dpMetadata.completeness)?? || (dpMetadata.confidence)??/>

<div id="sections" class="container-fluid bg-body">
    <div class="container mb-md-4 bd-layout main-content-container">
        <main class="bd-main">
            <div class="bd-toc mt-4 pt-3 ps-3 mb-lg-5 text-muted">
                <nav id="sidebar-content">
                    <ul>
                        <li class="rp-toc-group"><@s.text name='portal.resource.toc.about'/></li>
                        <li><a href="#anchor-description" class="sidebar-navigation-link"><@s.text name='portal.resource.description'/></a></li>
                        <#if isPublished && (colDpLicense?has_content || doi?has_content)>
                            <li><a href="#anchor-citation" class="sidebar-navigation-link"><@s.text name='portal.resource.citationLicense'/></a></li>
                        </#if>
                        <#if isPublished>
                            <li class="rp-toc-group"><@s.text name='portal.resource.toc.data'/></li>
                            <li><a href="#anchor-dataRecords" class="sidebar-navigation-link"><@s.text name='portal.resource.dataRecords'/></a></li>
                            <#if resource.versionHistory??>
                                <li><a href="#anchor-versions" class="sidebar-navigation-link"><@s.text name='portal.resource.versions'/></a></li>
                            </#if>
                            <#if hasCoverage>
                                <li><a href="#anchor-coverage" class="sidebar-navigation-link"><@s.text name='portal.resource.coverage'/></a></li>
                            </#if>
                            <#if peopleKeys?has_content || hasMore>
                                <li class="rp-toc-group"><@s.text name='portal.resource.toc.context'/></li>
                                <#if peopleKeys?has_content>
                                    <li><a href="#anchor-contacts" class="sidebar-navigation-link"><@s.text name='portal.resource.contacts'/></a></li>
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
                        <@resourceLogo url=((dpMetadata.logo)?string)!""/>
                        <div property="dc:abstract" class="rp-prose">
                            <#if (dpMetadata.description)?has_content>
                                ${dpMetadata.description?no_esc}
                            <#else>
                                <p><@s.text name='portal.resource.no.description'/></p>
                            </#if>
                        </div>
                    </div>
                    <#if isPublished><@keywordChips keywords=(dpMetadata.keyword)![]/></#if>
                </section>

                <#if isPublished>
                    <#-- licence and DOI (ColDP metadata has no ready-made citation) -->
                    <#if colDpLicense?has_content || doi?has_content>
                        <span class="anchor anchor-home-resource-page" id="anchor-citation"></span>
                        <section id="citation" class="rp-section section">
                            <h2 class="rp-h"><@s.text name='portal.resource.citationLicense'/></h2>
                            <#if doi?has_content>
                                <p class="mb-2">DOI <a property="dc:identifier" href="https://doi.org/${doi}">${doi}</a></p>
                            </#if>
                            <#if colDpLicense?has_content>
                                <div class="rp-license">
                                    <#if colDpLicenseCode?has_content>
                                        <@ccLicense colDpLicenseCode/>
                                    <#else>
                                        <p class="mb-0"><@s.text name='portal.resource.license'/>: ${colDpLicense}</p>
                                    </#if>
                                </div>
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

                    <#-- coverage: ColDP has it as text; a year or a year range also gets the time bar -->
                    <#if hasCoverage>
                        <span class="anchor anchor-home-resource-page" id="anchor-coverage"></span>
                        <section id="coverage" class="rp-section section">
                            <h2 class="rp-h"><@s.text name='portal.resource.coverage'/></h2>
                            <#if temporalScope?matches("\\s*\\d{4}\\s*([-–]\\s*\\d{4}\\s*)?")>
                                <#assign years = temporalScope?replace("[^0-9]+", " ", "r")?trim?split(" ")/>
                                <div id="temporal-bar" class="rp-timeline mb-3" aria-hidden="true" data-ranges="[[&quot;${years?first}-01-01&quot;,&quot;${years?last}-12-31&quot;]]"></div>
                            </#if>
                            <dl class="rp-kv">
                                <#if (dpMetadata.geographicScope)?has_content>
                                    <dt><@s.text name='portal.resource.geographic'/></dt><dd property="dc:spatial">${dpMetadata.geographicScope}</dd>
                                </#if>
                                <#if temporalScope?has_content>
                                    <dt><@s.text name='portal.resource.temporal'/></dt><dd property="dc:temporal">${temporalScope}</dd>
                                </#if>
                                <#if (dpMetadata.taxonomicScope)?has_content>
                                    <dt><@s.text name='portal.resource.taxonomic'/></dt>
                                    <dd>
                                        ${dpMetadata.taxonomicScope}
                                        <#if (dpMetadata.taxonomicScopeInEnglish)?has_content && dpMetadata.taxonomicScopeInEnglish != dpMetadata.taxonomicScope>
                                            <span class="text-muted">(${dpMetadata.taxonomicScopeInEnglish})</span>
                                        </#if>
                                    </dd>
                                </#if>
                            </dl>
                        </section>
                    </#if>

                    <#-- people: each person once, with all their roles -->
                    <#if peopleKeys?has_content>
                        <span class="anchor anchor-home-resource-page" id="anchor-contacts"></span>
                        <section id="contacts" class="rp-section section">
                            <h2 class="rp-h"><@s.text name='portal.resource.contacts'/></h2>
                            <div class="rp-people">
                                <#list peopleKeys as key>
                                    <#assign a = peopleAgents[key]/>
                                    <#assign isPerson = a.given?has_content || a.family?has_content/>
                                    <#assign orgLine = []/>
                                    <#if isPerson && a.organisation?has_content><#assign orgLine = orgLine + [a.organisation]/></#if>
                                    <#if a.department?has_content><#assign orgLine = orgLine + [a.department]/></#if>
                                    <#assign place = [a.city!, a.state!, a.country?has_content?then(countryName(a.country), "")]?filter(p -> p?has_content)/>
                                    <@personRow name=isPerson?then(((a.given!"") + " " + (a.family!""))?trim, a.organisation!"")
                                            orcidUrl=a.orcid?has_content?then(idUrl(a.orcid, 'https://orcid.org/'), "")
                                            org=orgLine?join(" · ")
                                            roleLabels=peopleRoles[key]?map(r -> action.getText('portal.resource.role.' + r))
                                            email=a.email!"">
                                        <@personDetail label=action.getText('eml.contact.address.address')><#list place as p>${p}<#sep>, </#sep></#list></@personDetail>
                                        <@personDetail label=action.getText('eml.contact.email')><#if a.email?has_content><a href="mailto:${a.email}" class="break-all">${a.email}</a></#if></@personDetail>
                                        <@personDetail label=action.getText('eml.contact.homepage')><#if (a.url)??><a href="${a.url}" class="break-all">${a.url}</a></#if></@personDetail>
                                        <@personDetail label="ROR"><#if a.rorid?has_content><a href="${idUrl(a.rorid, 'https://ror.org/')}" class="break-all" target="_blank" rel="noopener">${idUrl(a.rorid, 'https://ror.org/')}</a></#if></@personDetail>
                                        <@personDetail label=action.getText('portal.resource.notes')><#if a.note?has_content>${a.note}</#if></@personDetail>
                                    </@personRow>
                                </#list>
                            </div>
                        </section>
                    </#if>

                    <#-- the long tail: folded -->
                    <#if hasMore>
                        <span class="anchor anchor-home-resource-page" id="anchor-more"></span>
                        <section id="more" class="rp-section section">
                            <h2 class="rp-h"><@s.text name='portal.resource.more'/></h2>
                            <div class="rp-rows">

                            <#if (dpMetadata.source)?has_content>
                                <@moreRow id="sources" title=action.getText('portal.resource.sources') hint=dpMetadata.source?size?c>
                                    <ol class="overflow-x-auto mb-0">
                                        <#list dpMetadata.source as src>
                                            <li property="dc:references">
                                                <#assign people = ((src.author)![]) + ((src.editor)![])/>
                                                <#if people?has_content><#list people as p><#if p.literal?has_content>${p.literal}<#else>${p.family!} ${p.given!}</#if><#sep>, </#sep></#list>. </#if>
                                                <#if (src.issued)??>(${src.issued?string}). </#if>
                                                <strong>${src.title!src.id!}</strong><#if src.version?has_content>, ${src.version}</#if>.
                                                <#if src.containerTitle?has_content><em>${src.containerTitle}</em><#if src.volume?has_content> ${src.volume}</#if><#if src.issue?has_content>(${src.issue})</#if><#if src.page?has_content>: ${src.page}</#if>. </#if>
                                                <#if src.publisher?has_content>${src.publisher}<#if src.publisherPlace?has_content>, ${src.publisherPlace}</#if>. </#if>
                                                <#if (src.doi)??><a href="https://doi.org/${src.doi?string}">https://doi.org/${src.doi?string}</a><#elseif (src.url)??><a href="${src.url}" class="break-all">${src.url}</a></#if>
                                            </li>
                                        </#list>
                                    </ol>
                                </@moreRow>
                            </#if>

                            <#if (dpMetadata.notes)?has_content>
                                <@moreRow id="notes" title=action.getText('portal.resource.notes')>
                                    <div class="rp-prose">${dpMetadata.notes}</div>
                                </@moreRow>
                            </#if>

                            <#if (dpMetadata.alias)?has_content || (dpMetadata.url)?? || (dpMetadata.identifier)?has_content || (dpMetadata.issn)?has_content || (dpMetadata.completeness)?? || (dpMetadata.confidence)??>
                                <#assign additionalFields = []/>
                                <#if (dpMetadata.alias)?has_content><#assign additionalFields = additionalFields + [action.getText('portal.resource.alias')]/></#if>
                                <#if (dpMetadata.url)??><#assign additionalFields = additionalFields + [action.getText('portal.resource.website')]/></#if>
                                <#if (dpMetadata.identifier)?has_content><#assign additionalFields = additionalFields + [action.getText('portal.resource.identifiers')]/></#if>
                                <#if (dpMetadata.issn)?has_content><#assign additionalFields = additionalFields + ["ISSN"]/></#if>
                                <#if (dpMetadata.completeness)??><#assign additionalFields = additionalFields + [action.getText('portal.resource.completeness')]/></#if>
                                <#if (dpMetadata.confidence)??><#assign additionalFields = additionalFields + [action.getText('portal.resource.confidence')]/></#if>
                                <@moreRow id="additionalMetadata" title=action.getText('manage.metadata.additional.title') hint=additionalFields?join(", ")>
                                    <dl class="rp-kv">
                                        <#if (dpMetadata.alias)?has_content><dt><@s.text name='portal.resource.alias'/></dt><dd>${dpMetadata.alias}</dd></#if>
                                        <#if (dpMetadata.url)??><dt><@s.text name='portal.resource.website'/></dt><dd><a href="${dpMetadata.url}" class="break-all">${dpMetadata.url}</a></dd></#if>
                                        <#if (dpMetadata.identifier)?has_content><dt><@s.text name='portal.resource.identifiers'/></dt><dd><#list dpMetadata.identifier as id><div class="break-all">${id}</div></#list></dd></#if>
                                        <#if (dpMetadata.issn)?has_content><dt>ISSN</dt><dd>${dpMetadata.issn}</dd></#if>
                                        <#if (dpMetadata.completeness)??><dt><@s.text name='portal.resource.completeness'/></dt><dd>${dpMetadata.completeness}%</dd></#if>
                                        <#if (dpMetadata.confidence)??><dt><@s.text name='portal.resource.confidence'/></dt><dd>${dpMetadata.confidence} / 5</dd></#if>
                                    </dl>
                                </@moreRow>
                            </#if>
                            </div>
                        </section>
                    </#if>
                </#if>
            </div>
        </main>
    </div>
</div>


<#include "/WEB-INF/pages/inc/footer.ftl">

<script src="${baseURL}/js/jconfirmation.jquery.js"></script>
<script src="${baseURL}/js/portal-resource.js"></script>

<script>
    $('.confirmDeleteVersion').jConfirmAction({
        titleQuestion: "<@s.text name="basic.confirm"/>",
        question: "<@s.text name='portal.resource.confirm.delete.version'/><br><br><@s.text name='portal.resource.confirm.delete.version.warning.citation'/><br><br><@s.text name='portal.resource.confirm.delete.version.warning.undone'/>",
        yesAnswer: "<@s.text name='basic.yes'/>",
        cancelAnswer: "<@s.text name='basic.no'/>",
        buttonType: "danger",
        baseUrl: "${baseURL}"
    });
</script>
