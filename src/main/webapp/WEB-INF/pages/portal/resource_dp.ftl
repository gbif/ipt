<#-- @ftlvariable name="" type="org.gbif.ipt.action.portal.ResourceAction" -->
<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title>${dpMetadata.title!"IPT"}</title>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>
    <#include "/WEB-INF/pages/macros/versionsTable.ftl"/>

    <#-- Construct a Contributor -->
    <#macro contributor contributor>
        <div class="contact">

            <#-- minimum info is the last name, organisation name, or position name -->
            <div class="contactName mb-1">
                ${contributor.title!}
            </div>

            <#-- we use this div to toggle the grouped information -->
            <div class="text-smaller text-discreet">
                <div class="contactType fst-italic">
                    ${contributor.role!}
                </div>

                <#if contributor.email?has_content>
                    <div>
                        <a href="mailto:${contributor.email}">${contributor.email}</a>
                    </div>
                </#if>

                <#if contributor.organization?has_content>
                    <div>
                        ${contributor.organization}
                    </div>
                </#if>

                <#if contributor.path?has_content>
                    <div class="overflow-wrap">
                        <a href="${contributor.path}">${contributor.path}</a>
                    </div>
                </#if>
            </div>
        </div>
    </#macro>

    <#assign anchor_versions>#anchor-versions</#assign>
    <#assign anchor_rights>#anchor-rights</#assign>
    <#assign anchor_citation>#anchor-citation</#assign>
    <#assign no_description><@s.text name='portal.resource.no.description'/></#assign>
    <#assign publishedOnText><@s.text name='manage.overview.published.released'/></#assign>
    <#assign download_dp_url>${baseURL}/archive.do?r=${resource.shortname}<#if version??>&v=${version.toPlainString()}</#if></#assign>
    <#assign download_metadata_url>${baseURL}/metadata.do?r=${resource.shortname}&v=<#if version??>${version.toPlainString()}<#else>${resource.metadataVersion.toPlainString()}</#if></#assign>
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
                <@s.text name='manage.overview.metadata.preview'><@s.param>${resource.metadataVersion.toPlainString()}</@s.param></@s.text>
            </div>
        </#if>

        <div class="container my-3 p-3">
            <div class="text-center text-uppercase fw-bold fs-smaller-2">
                <span>
                    ${resource.dataPackageType}
                </span>
            </div>

            <div class="text-center">
                <h1 property="dc:title" class="rtitle pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    ${dpMetadata.title!resource.shortname}
                </h1>

                <#if resource.lastPublished??>
                    <div class="mt-2">

                        <#if managerRights>
                            <a href="${baseURL}/manage/resource.do?r=${resource.shortname}" class="btn btn-sm btn-outline-gbif-primary mt-1 me-xl-1 top-button">
                                <@s.text name='button.edit'/>
                            </a>
                        </#if>
                        <#if version?? && version.toPlainString() != resource.metadataVersion.toPlainString()>
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

    <div class="container-fluid bg-light border-bottom">
        <div class="container">
            <div class="my-4 px-4 py-4 bg-body border rounded shadow-sm">
                <span class="anchor anchor-home-resource-page-2 mb-3" id="anchor-downloads"></span>
                <div class="mx-md-4 mx-2">
                    <div class="row">
                        <div class="col-lg-4 text-smaller px-0 pb-lg-max-3 ps-lg-3 order-lg-2">
                            <dl class="inline mb-0">
                                <#if (dpMetadata.created)??>
                                    <div>
                                        <dt><@s.text name='portal.resource.publicationDate'/>:</dt>
                                        <dd>${dpMetadata.created?date?string.long}</dd>
                                    </div>
                                </#if>
                            </dl>
                        </div>

                        <div class="col-lg-8 text-smaller px-0 pt-lg-max-3 border-lg-max-top order-lg-1">
                            <p class="mb-1"><@s.text name='portal.resource.downloads.datapackage.verbose'/></p>

                            <div class="table-responsive">
                                <table class="downloads text-smaller table table-sm table-borderless mb-0">
                                    <tr>
                                        <th class="col-4 p-0">
                                            <@s.text name='portal.resource.dataPackage.verbose'/>
                                        </th>

                                        <#if version?? && version.toPlainString() != resource.metadataVersion.toPlainString()>
                                            <td class="p-0">
                                                <a href="${download_dp_url}" onClick="_gaq.push(['_trackEvent', 'Archive', 'Download', '${resource.shortname}' ]);">
                                                    <svg class="link-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24" data-testid="DownloadIcon"><path d="M5 20h14v-2H5v2zM19 9h-4V3H9v6H5l7 7 7-7z"></path></svg>
                                                    <@s.text name='portal.resource.download'/>
                                                </a>
                                            </td>
                                        <#else>
                                            <td class="p-0">
                                                <a href="${download_dp_url}" onClick="_gaq.push(['_trackEvent', 'Archive', 'Download', '${resource.shortname}' ]);">
                                                    <svg class="link-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24" data-testid="DownloadIcon"><path d="M5 20h14v-2H5v2zM19 9h-4V3H9v6H5l7 7 7-7z"></path></svg>
                                                    <@s.text name='portal.resource.download'/>
                                                </a>
                                            </td>
                                        </#if>
                                    </tr>
                                    <tr>
                                        <th class="p-0"><@s.text name='portal.resource.datapackage.metadata.verbose'/></th>
                                        <td class="p-0">
                                            <a href="${download_metadata_url}" onClick="_gaq.push(['_trackEvent', 'EML', 'Download', '${resource.shortname}']);" download>
                                                <svg class="link-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24" data-testid="DownloadIcon"><path d="M5 20h14v-2H5v2zM19 9h-4V3H9v6H5l7 7 7-7z"></path></svg>
                                                <@s.text name='portal.resource.download'/>
                                            </a>
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
                                <#if resource.versionHistory??>
                                    <li><a href="#anchor-versions" class="sidebar-navigation-link"><@s.text name='portal.resource.versions'/></a></li>
                                </#if>
                                <li><a href="#anchor-keywords" class="sidebar-navigation-link"><@s.text name='portal.resource.keywords'/></a></li>
                                <li><a href="#anchor-contributors" class="sidebar-navigation-link"><@s.text name='portal.resource.contributors'/></a></li>
                                <li><a href="#anchor-sources" class="sidebar-navigation-link"><@s.text name='portal.resource.sources'/></a></li>
                                <li><a href="#anchor-licenses" class="sidebar-navigation-link"><@s.text name='portal.resource.licenses'/></a></li>
                                <#if (dpMetadata.spatial)?has_content>
                                    <li><a href="#anchor-geographic" class="sidebar-navigation-link"><@s.text name='portal.resource.geographic'/></a></li>
                                </#if>
                                <#if (dpMetadata.taxonomic)?has_content>
                                    <li><a href="#anchor-taxonomic" class="sidebar-navigation-link"><@s.text name='portal.resource.taxonomic'/></a></li>
                                </#if>
                                <#if (dpMetadata.temporal)?has_content>
                                    <li><a href="#anchor-temporal" class="sidebar-navigation-link"><@s.text name='portal.resource.temporal'/></a></li>
                                </#if>
                                <li><a href="#anchor-other" class="sidebar-navigation-link"><@s.text name='portal.resource.other'/></a></li>
                            </#if>
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
                            <#if (dpMetadata.description)?has_content>
                                <p>
                                    <@dpMetadata.description?interpret />
                                </p>
                            <#else>
                                <p><@s.text name='portal.resource.no.description'/></p>
                            </#if>
                        </div>
                    </div>

                    <!-- Dataset must have been published for versions, downloads, and how to cite sections to show -->
                    <#if resource.lastPublished??>
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
                    </#if>

                    <!-- Keywords section -->
                    <span class="anchor anchor-home-resource-page" id="anchor-keywords"></span>
                    <div id="keywords" class="mt-5 section">
                        <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                            <@s.text name='portal.resource.keywords'/>
                        </h4>

                        <p>
                            <#list dpMetadata.keywords as keyword>
                                ${keyword}<#sep>;</#sep>
                            </#list>
                        </p>
                    </div>

                    <!-- Contributors section -->
                    <span class="anchor anchor-resource-page" id="anchor-contributors"></span>
                    <div id="contributors" class="mt-5 section">
                        <h4 class="pb-2 mb-4 pt-2 text-gbif-header-2 fw-400">
                            <@s.text name='portal.resource.contributors'/>
                        </h4>

                        <div class="row g-3 border">
                            <#if (dpMetadata.contributors?size>0)>
                                <#list dpMetadata.contributors as c>
                                    <div class="col-lg-4 mt-0">
                                        <@contributor contributor=c />
                                    </div>
                                </#list>
                            </#if>
                        </div>
                    </div>

                    <!-- Sources section -->
                    <span class="anchor anchor-home-resource-page" id="anchor-sources"></span>
                    <div id="sources" class="mt-5 section">
                        <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                            <@s.text name='portal.resource.sources'/>
                        </h4>

                        <#list dpMetadata.sources as source>
                            <div class="table-responsive">
                                <table class="text-smaller table table-sm table-borderless">
                                    <tr>
                                        <th class="col-4">Title</th>
                                        <td>${source.title!}</td>
                                    </tr>
                                    <#if source.path??>
                                        <tr>
                                            <th class="col-4">Path</th>
                                            <td><a href="${source.path}">${source.path}</a></td>
                                        </tr>
                                    </#if>
                                    <#if source.email??>
                                        <tr>
                                            <th class="col-4">Email</th>
                                            <td><a href="mailto:${source.email}">${source.email}</a> </td>
                                        </tr>
                                    </#if>
                                    <tr>
                                        <th class="col-4">Version</th>
                                        <td>${source.version!source.additionalProperties['version']!"-"}</td>
                                    </tr>
                                </table>
                            </div>
                        </#list>
                    </div>

                    <!-- Licenses section -->
                    <span class="anchor anchor-home-resource-page" id="anchor-licenses"></span>
                    <div id="licenses" class="mt-5 section">
                        <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                            <@s.text name='portal.resource.licenses'/>
                        </h4>

                        <#list dpMetadata.licenses as license>
                            <div class="table-responsive">
                                <table class="text-smaller table table-sm table-borderless">
                                    <#if license.name??>
                                        <tr>
                                            <th class="col-4">Name</th>
                                            <td>${license.name}</td>
                                        </tr>
                                    </#if>
                                    <#if license.title??>
                                        <tr>
                                            <th class="col-4">Title</th>
                                            <td>${license.title}</td>
                                        </tr>
                                    </#if>
                                    <#if license.path??>
                                        <tr>
                                            <th class="col-4">Path</th>
                                            <td>${license.path}</td>
                                        </tr>
                                    </#if>
                                    <tr>
                                        <th class="col-4">Scope</th>
                                        <td>${license.scope!}</td>
                                    </tr>
                                </table>
                            </div>
                        </#list>
                    </div>

                    <!-- Geographic scope section -->
                    <#if (dpMetadata.spatial)??>
                        <span class="anchor anchor-home-resource-page" id="anchor-geographic"></span>
                        <div id="geographic" class="mt-5 section">
                            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                                <@s.text name='portal.resource.geographic'/>
                            </h4>

                            <div class="table-responsive">
                                <table class="text-smaller table table-sm table-borderless">
                                    <tr>
                                        <th class="col-4"><@s.text name='eml.geospatialCoverages.boundingCoordinates'/></th>
                                        <td>${dpMetadata.spatial.bbox}</td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                    </#if>

                    <!-- Taxonomic scope section -->
                    <#if (dpMetadata.taxonomic)??>
                        <span class="anchor anchor-home-resource-page" id="anchor-taxonomic"></span>
                        <div id="taxonomic" class="mt-5 section">
                            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                                <@s.text name='portal.resource.taxonomic'/>
                            </h4>

                            <#list dpMetadata.taxonomic as tx>
                                <div class="table-responsive">
                                    <table class="text-smaller table table-sm table-borderless">
                                        <tr>
                                            <th class="col-4">Taxon id</th>
                                            <td>${tx.taxonID!}</td>
                                        </tr>
                                        <tr>
                                            <th class="col-4">Taxon id reference</th>
                                            <td>${tx.taxonIDReference!}</td>
                                        </tr>
                                        <tr>
                                            <th class="col-4">Scientific name</th>
                                            <td>${tx.scientificName!}</td>
                                        </tr>
                                        <tr>
                                            <th class="col-4">Taxon rank</th>
                                            <td>${tx.taxonRank!}</td>
                                        </tr>
                                        <tr>
                                            <th class="col-4">Vernacular names</th>
                                            <td><#if tx.vernacularNames?has_content><#list tx.vernacularNames as key, value>${value} [${key}]<#sep>, </#sep></#list></#if></td>
                                        </tr>
                                    </table>
                                </div>
                            </#list>
                        </div>
                    </#if>

                    <!-- Temporal scope section -->
                    <#if (dpMetadata.temporal)??>
                        <span class="anchor anchor-home-resource-page" id="anchor-temporal"></span>
                        <div id="temporal" class="mt-5 section">
                            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                                <@s.text name='portal.resource.temporal'/>
                            </h4>

                            <div class="table-responsive">
                                <table class="text-smaller table table-sm table-borderless">
                                    <tr>
                                        <th class="col-4"><@s.text name='eml.temporalCoverages.startDate'/> / <@s.text name='eml.temporalCoverages.endDate'/></th>
                                        <td property="dc:temporal">${dpMetadata.temporal.start?date} / ${dpMetadata.temporal.end?date}</td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                    </#if>

                    <!-- Other metadata section -->
                    <span class="anchor anchor-home-resource-page" id="anchor-other"></span>
                    <div id="other" class="mt-5 section">
                        <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                            <@s.text name='portal.resource.other'/>
                        </h4>
                        <div class="mt-3 overflow-x-auto">

                        </div>
                    </div>

                </div>
            </main>
        </div>
    </div>


    <#include "/WEB-INF/pages/inc/footer.ftl">

    <!-- data record line chart -->
    <script src="${baseURL}/js/graphs.js"></script>

    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>

    <script>
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
