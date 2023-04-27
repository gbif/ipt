<#-- @ftlvariable name="" type="org.gbif.ipt.action.portal.ResourceAction" -->
<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
  <title>${dpMetadata.title!"IPT"}</title>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>
    <#include "/WEB-INF/pages/macros/versionsTable.ftl"/>

<#-- Construct an Agent -->
    <#macro agent entity>
      <div class="contact">
        <div class="contactName mb-1">
            <#if entity.given?has_content || entity.family?has_content>
                ${entity.given!} ${entity.family!}
            <#else>
                ${entity.organisation!}
            </#if>

        </div>

        <div class="text-smaller text-discreet">
          <div class="contactType fst-italic">
              ${entity.role!}
          </div>

          <#if (entity.given?has_content || entity.family?has_content) && entity.organisation?has_content>
            <div class="organisation">
                ${entity.organisation!}
            </div>
          </#if>

          <div class="address">
              <#if entity.city?has_content>
                <div class="city">
                    ${entity.city}
                </div>
              </#if>

              <#if entity.state?has_content>
                <div class="state">${entity.state}</div>
              </#if>

              <#if entity.country?has_content>
                <div class="country">${entity.country}</div>
              </#if>
          </div>

            <#if entity.email?has_content>
              <div>
                <a href="mailto:${entity.email}">${entity.email}</a>
              </div>
            </#if>

            <#if entity.url?has_content>
              <div class="overflow-wrap">
                <a href="${entity.url}">${entity.url}</a>
              </div>
            </#if>

            <#if entity.rorid?has_content>
              <div class="overflow-wrap">
                rorid: ${entity.rorid}
              </div>
            </#if>

            <#if entity.orcid?has_content>
              <div class="overflow-wrap">
                orcid: ${entity.orcid}
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
  <script src="${baseURL}/js/jquery/jquery.dataTables-1.13.1.min.js"></script>
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
                    ${resource.dataPackageType!}
                </span>
      </div>

      <div class="text-center">
        <h1 property="dc:title" class="rtitle pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
            ${dpMetadata.title!resource.shortname}
        </h1>

          <#if resource.lastPublished?? && resource.organisation??>
            <div class="text-gbif-primary text-smaller">
              <span>
                  <#-- the existence of parameter version means the version is not equal to the latest published version -->
                  <#if version?? && version.toPlainString() != resource.metadataVersion.toPlainString()>
                    <em><@s.text name='portal.resource.version'/>&nbsp;${version.toPlainString()}</em>
                  <#else>
                      <@s.text name='portal.resource.latest.version'/>
                  </#if>

                  <#if dpMetadata.created?has_content>
                      <#assign createdLongDate>
                          ${dpMetadata.created?date?string.long}
                      </#assign>
                      <#assign createdLongShortDate>
                          ${dpMetadata.created?date?string.long_short}
                      </#assign>
                  </#if>

                  <#if action.getDefaultOrganisation()?? && resource.organisation.key.toString() == action.getDefaultOrganisation().key.toString()>
                      ${publishedOnText?lower_case}&nbsp;<span>${createdLongDate!}</span>
                    <br>
                    <em class="text-gbif-danger"><@s.text name='manage.home.not.registered.verbose'/></em>
                  <#else>
                      <@s.text name='portal.resource.publishedOn'><@s.param>${resource.organisation.name}</@s.param></@s.text> <span>${createdLongShortDate!}</span>
                    <span style="display: none">${resource.organisation.name}</span>
                  </#if>
              </span>
            </div>
          <#else>
            <div class="text-gbif-danger text-smaller">
                <@s.text name='portal.resource.published.never.long'/>
            </div>
          </#if>

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
                  <#if (dpMetadata.issued)??>
                    <div>
                      <dt><@s.text name='portal.resource.publicationDate'/>:</dt>
                      <dd>${dpMetadata.issued?date?string.long}</dd>
                    </div>
                  </#if>
              </dl>
              <dl class="inline mb-0">
                  <#if (dpMetadata.issued)?? && resource.organisation?has_content>
                    <div>
                      <dt><@s.text name='portal.resource.publishedBy'/>:</dt>
                      <dd>
                        <a href="${cfg.portalUrl}/publisher/${resource.organisation.key}" target="_blank">${resource.organisation.name!"Organisation"}</a>
                      </dd>
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
                      <a href="${download_metadata_url}" onClick="_gaq.push(['_trackEvent', 'Metadata', 'Download', '${resource.shortname}']);" download>
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
                    <#if (dpMetadata.identifier)?has_content>
                      <li><a href="#anchor-identifiers" class="sidebar-navigation-link"><@s.text name='portal.resource.identifiers'/></a></li>
                    </#if>
                    <#if (dpMetadata.keyword)?has_content>
                      <li><a href="#anchor-keywords" class="sidebar-navigation-link"><@s.text name='portal.resource.keywords'/></a></li>
                    </#if>
                    <#if (dpMetadata.contact)?has_content>
                      <li><a href="#anchor-contact" class="sidebar-navigation-link"><@s.text name='portal.resource.contact'/></a></li>
                    </#if>
                    <#if (dpMetadata.creator)?has_content>
                      <li><a href="#anchor-creators" class="sidebar-navigation-link"><@s.text name='portal.resource.creators'/></a></li>
                    </#if>
                    <#if (dpMetadata.editor)?has_content>
                      <li><a href="#anchor-editors" class="sidebar-navigation-link"><@s.text name='portal.resource.editors'/></a></li>
                    </#if>
                    <#if (dpMetadata.publisher)?has_content>
                      <li><a href="#anchor-publisher" class="sidebar-navigation-link"><@s.text name='portal.resource.publisher'/></a></li>
                    </#if>
                    <#if (dpMetadata.contributor)?has_content>
                      <li><a href="#anchor-contributors" class="sidebar-navigation-link"><@s.text name='portal.resource.contributors'/></a></li>
                    </#if>
                    <#if (dpMetadata.geographicScope)?has_content>
                      <li><a href="#anchor-geographic" class="sidebar-navigation-link"><@s.text name='portal.resource.geographic'/></a></li>
                    </#if>
                    <#if (dpMetadata.taxonomicScope)?has_content>
                      <li><a href="#anchor-taxonomic" class="sidebar-navigation-link"><@s.text name='portal.resource.taxonomic'/></a></li>
                    </#if>
                    <#if (dpMetadata.temporalScope)?has_content>
                      <li><a href="#anchor-temporal" class="sidebar-navigation-link"><@s.text name='portal.resource.temporal'/></a></li>
                    </#if>
                    <#if (dpMetadata.notes)?has_content>
                      <li><a href="#anchor-notes" class="sidebar-navigation-link"><@s.text name='portal.resource.notes'/></a></li>
                    </#if>
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

          <!-- Identifiers section -->
            <#if (dpMetadata.identifier.additionalProperties)?has_content>
              <span class="anchor anchor-home-resource-page" id="anchor-identifiers"></span>
              <div id="identifiers" class="mt-5 section">
                <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                    <@s.text name='portal.resource.identifiers'/>
                </h4>

                <p>
                    <#list dpMetadata.identifier.additionalProperties as key, value>
                      ${key}: ${value}<br>
                    </#list>
                </p>
              </div>
            </#if>

          <!-- Keywords section -->
            <#if (dpMetadata.keyword)?has_content>
              <span class="anchor anchor-home-resource-page" id="anchor-keywords"></span>
              <div id="keywords" class="mt-5 section">
                <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                    <@s.text name='portal.resource.keywords'/>
                </h4>

                <p>
                    <#list dpMetadata.keyword as keyword>
                        ${keyword}<#sep>;</#sep>
                    </#list>
                </p>
              </div>
            </#if>

          <!-- Contact section -->
            <#if (dpMetadata.contact)?has_content>
              <span class="anchor anchor-resource-page" id="anchor-contact"></span>
              <div id="contact" class="mt-5 section">
                <h4 class="pb-2 mb-4 pt-2 text-gbif-header-2 fw-400">
                    <@s.text name='portal.resource.contact'/>
                </h4>

                <div class="row g-3 border">
                  <div class="col-lg-4 mt-0">
                      <@agent entity=dpMetadata.contact />
                  </div>
                </div>
              </div>
            </#if>

          <!-- Creators section -->
            <#if (dpMetadata.creator)?has_content>
              <span class="anchor anchor-resource-page" id="anchor-creators"></span>
              <div id="creators" class="mt-5 section">
                <h4 class="pb-2 mb-4 pt-2 text-gbif-header-2 fw-400">
                    <@s.text name='portal.resource.creators'/>
                </h4>

                <div class="row g-3 border">
                    <#if (dpMetadata.creator?size>0)>
                        <#list dpMetadata.creator as c>
                          <div class="col-lg-4 mt-0">
                              <@agent entity=c />
                          </div>
                        </#list>
                    </#if>
                </div>
              </div>
            </#if>

          <!-- Editors section -->
            <#if (dpMetadata.editor)?has_content>
              <span class="anchor anchor-resource-page" id="anchor-editors"></span>
              <div id="editors" class="mt-5 section">
                <h4 class="pb-2 mb-4 pt-2 text-gbif-header-2 fw-400">
                    <@s.text name='portal.resource.editors'/>
                </h4>

                <div class="row g-3 border">
                    <#if (dpMetadata.editor?size>0)>
                        <#list dpMetadata.editor as c>
                          <div class="col-lg-4 mt-0">
                              <@agent entity=c />
                          </div>
                        </#list>
                    </#if>
                </div>
              </div>
            </#if>

          <!-- Publishers section -->
            <#if (dpMetadata.publisher)?has_content>
              <span class="anchor anchor-resource-page" id="anchor-publisher"></span>
              <div id="publisher" class="mt-5 section">
                <h4 class="pb-2 mb-4 pt-2 text-gbif-header-2 fw-400">
                    <@s.text name='portal.resource.publisher'/>
                </h4>

                <div class="row g-3 border">
                    <div class="col-lg-4 mt-0">
                        <@agent entity=dpMetadata.publisher />
                    </div>
                </div>
              </div>
            </#if>

          <!-- Contributors section -->
            <#if (dpMetadata.contributor)?has_content>
              <span class="anchor anchor-resource-page" id="anchor-contributors"></span>
              <div id="contributors" class="mt-5 section">
                <h4 class="pb-2 mb-4 pt-2 text-gbif-header-2 fw-400">
                    <@s.text name='portal.resource.contributors'/>
                </h4>

                <div class="row g-3 border">
                    <#if (dpMetadata.contributor?size>0)>
                        <#list dpMetadata.contributor as c>
                          <div class="col-lg-4 mt-0">
                              <@agent entity=c />
                          </div>
                        </#list>
                    </#if>
                </div>
              </div>
            </#if>

          <!-- Sources section -->
            <#if (dpMetadata.sources)?has_content>
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
            </#if>

          <!-- Licenses section -->
            <#if (dpMetadata.licenses)?has_content>
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
            </#if>

          <!-- Geographic scope section -->
            <#if (dpMetadata.geographicScope)??>
              <span class="anchor anchor-home-resource-page" id="anchor-geographic"></span>
              <div id="geographic" class="mt-5 section">
                <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                    <@s.text name='portal.resource.geographic'/>
                </h4>

                <p>
                  ${dpMetadata.geographicScope}
                </p>
              </div>
            </#if>

          <!-- Taxonomic scope section -->
            <#if (dpMetadata.taxonomicScope)??>
              <span class="anchor anchor-home-resource-page" id="anchor-taxonomic"></span>
              <div id="taxonomic" class="mt-5 section">
                <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                    <@s.text name='portal.resource.taxonomic'/>
                </h4>

                <p>
                    ${dpMetadata.taxonomicScope}
                </p>
              </div>
            </#if>

          <!-- Temporal scope section -->
            <#if (dpMetadata.temporalScope)??>
              <span class="anchor anchor-home-resource-page" id="anchor-temporal"></span>
              <div id="temporal" class="mt-5 section">
                <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                    <@s.text name='portal.resource.temporal'/>
                </h4>

                <p>
                    ${dpMetadata.temporalScope}
                </p>
              </div>
            </#if>

          <!-- Notes section -->
          <#if (dpMetadata.notes)??>
            <span class="anchor anchor-home-resource-page" id="anchor-notes"></span>
            <div id="notes" class="mt-5 section">
              <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                  <@s.text name='portal.resource.notes'/>
              </h4>

              <p>
                  ${dpMetadata.notes}
              </p>
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
