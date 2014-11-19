<#escape x as x?html>
  <#include "/WEB-INF/pages/inc/header.ftl">
  <#include "/WEB-INF/pages/inc/menu.ftl">
  <#include "/WEB-INF/pages/macros/forms.ftl"/>
  <#include "/WEB-INF/pages/macros/versionsTable.ftl"/>
<#--
	Construct a Contact. Parameters are the actual contact object, and the contact type
-->
<#macro contact con type>
  <div class="contact">

    <div class="contactType">
      <#if con.role?has_content>
        ${roles[con.role]?cap_first!}
      <#elseif type?has_content>
        ${type}
      </#if>
    </div>

    <#-- minimum info is the last name, organisation name, or position name -->
    <div class="contactName">
      <#if con.lastName?has_content>
        ${con.firstName!} ${con.lastName!}
      <#elseif con.organisation?has_content>
        ${con.organisation}
      <#elseif con.positionName?has_content>
        ${con.position!}
      </#if>
    </div>
    <#-- we use this div to toggle the grouped information -->
    <div>
      <#if con.position?has_content>
        <div class="contactPosition">
          ${con.position!}
        </div>
      </#if>
        <div class="address">
          <#if con.organisation?has_content>
              <span>${con.organisation}</span>
          </#if>

          <#if con.address.address?has_content>
              <span>${con.address.address!}</span>
          </#if>

          <#if con.address.postalCode?has_content || con.address.city?has_content>
            <span class="city">
               <#if con.address.postalCode?has_content>
                ${con.address.postalCode!}
              </#if>
              ${con.address.city!}
            </span>
          </#if>

          <#if con.address.province?has_content>
              <span class="province">${con.address.province}</span>
          </#if>

          <#if con.address.country?has_content && con.address.country != 'UNKNOWN'>
              <span class="country">${con.address.country}</span>
          </#if>

          <#if con.email?has_content>
            <span class="email"><a href="mailto:${con.email}" title="email">${con.email}</a></span>
          </#if>

          <#if con.phone?has_content>
            <span class="phone">${con.phone}</span>
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
                <a href="${directory}${identifier}">${directory}${identifier}</a>
            </div>
        </#if>
      </#if>
    </div>
  </div>
</#macro>

<#-- Creates a column list of contacts, defaults to 2 columns -->
  <#macro contactList contacts contactType="" columns=2>
    <#list contacts as c>
      <#if c_index%columns==0>
      <div class="contact_row col${columns}">
      </#if>
      <@contact con=c type=contactType/>
      <#if c_index%columns==columns-1 || !c_has_next >
          <!-- end of row -->
      </div>
      </#if>
    </#list>
  </#macro>

  <#assign anchor_versions>#versions</#assign>
  <#assign anchor_rights>#rights</#assign>
  <#assign anchor_citation>#citation</#assign>

<!-- Sidebar -->
<div id="sidebar-wrapper">

    <ul class="sidebar-nav">
        <li>
          <input href="#menu-toggle" class="contract" type="submit" id="menu-toggle" name="submit" alt="expand" value=""/>
        </li>
        <li>
            <a class="sidebar-anchor work" href="#"><@s.text name='portal.resource.summary'/></a>
        </li>
        <!-- Dataset must have been published to show versions, downloads, and how to cite sections -->
        <#if resource.lastPublished??>
        <li>
          <a class="sidebar-anchor" href="#downloads"><@s.text name='portal.resource.downloads'/></a>
        </li>
        <li>
            <a class="sidebar-anchor" href="${anchor_versions}"><@s.text name='portal.resource.versions'/></a>
        </li>
        <li>
            <a class="sidebar-anchor" href="${anchor_citation}"><@s.text name='portal.resource.cite.howTo'/></a>
        </li>
        <li>
          <a class="sidebar-anchor" href="${anchor_rights}"><@s.text name='eml.intellectualRights.simple'/></a>
        </li>
        <li>
          <a class="sidebar-anchor" href="#gbif"><@s.text name='portal.resource.organisation.key'/></a>
        </li>
      </#if>
        <!-- Keywords section -->
        <#if eml.subject?has_content>
            <li>
                <a class="sidebar-anchor" href="#keywords"><@s.text name='portal.resource.summary.keywords'/></a>
            </li>
        </#if>
        <!-- External data section -->
      <#if (eml.physicalData?size > 0 )>
          <li>
              <a class="sidebar-anchor" href="#external"><@s.text name='manage.metadata.physical.alternativeTitle'/></a>
          </li>
      </#if>
        <!-- Contacts section -->
        <#if (eml.contacts?size>0) || (eml.creators?size>0) || (eml.metadataProviders?size>0) || (eml.associatedParties?size>0)>
          <li>
            <a class="sidebar-anchor" href="#contacts"><@s.text name='portal.resource.contacts'/></a>
          </li>
        </#if>
        <!-- Geo coverage section -->
      <#if eml.geospatialCoverages[0]??>
        <li>
            <a class="sidebar-anchor" href="#geospatial"><@s.text name='portal.resource.summary.geocoverage'/></a>
        </li>
      </#if>
        <!-- Taxonomic coverage sections -->
      <#if ((organizedCoverages?size > 0))>
          <li>
              <a class="sidebar-anchor" href="#taxanomic"><@s.text name='manage.metadata.taxcoverage.title'/></a>
          </li>
      </#if>
    <!-- Temporal coverages section -->
  <#if ((eml.temporalCoverages?size > 0))>
        <li>
            <a class="sidebar-anchor" href="#temporal"><@s.text name='manage.metadata.tempcoverage.title'/></a>
        </li>
  </#if>
        <!-- Project section -->
      <#if eml.project?? && eml.project.title?has_content>
          <li>
              <a class="sidebar-anchor" href="#project"><@s.text name='manage.metadata.project.title'/></a>
          </li>
      </#if>
        <!-- Sampling methods section -->
      <#if eml.studyExtent?has_content || eml.sampleDescription?has_content || eml.qualityControl?has_content || (eml.methodSteps?? && (eml.methodSteps?size>=1) && eml.methodSteps[0]?has_content) >
          <li>
              <a class="sidebar-anchor" href="#methods"><@s.text name='manage.metadata.methods.title'/></a>
          </li>
      </#if>
        <!-- Collections section -->
      <#if eml.collections?? && (eml.collections?size > 0) && eml.collections[0].collectionName?has_content >
          <li>
              <a class="sidebar-anchor" href="#collection"><@s.text name='manage.metadata.collections.title'/></a>
          </li>
      </#if>
        <!-- bibliographic citations section -->
      <#if eml.bibliographicCitationSet?? && (eml.bibliographicCitationSet.bibliographicCitations?has_content)>
          <li>
              <a class="sidebar-anchor" href="#references"><@s.text name='manage.metadata.citations.bibliography'/></a>
          </li>
      </#if>
        <!-- Additional metadata section -->
      <#if eml.additionalInfo?has_content || eml.purpose?has_content || (eml.alternateIdentifiers?size > 0 )>
          <li>
              <a class="sidebar-anchor" href="#additional"><@s.text name='manage.metadata.additional.title'/></a>
          </li>
      </#if>
    </ul>
</div>
<!-- /#sidebar-wrapper -->
  <#assign no_description><@s.text name='portal.resource.no.description'/></#assign>
  <#assign download_dwca_url>${baseURL}/archive.do?r=${resource.shortname}<#if version??>&v=${version.toPlainString()}</#if></#assign>
  <#assign download_eml_url>${baseURL}/eml.do?r=${resource.shortname}&v=<#if version??>${version.toPlainString()}<#else>${resource.emlVersion.toPlainString()}</#if></#assign>
  <#assign download_rtf_url>${baseURL}/rtf.do?r=${resource.shortname}&v=<#if version??>${version.toPlainString()}<#else>${resource.emlVersion.toPlainString()}</#if></#assign>

    <div id="wrapper">
        <#if managerRights><a href="${baseURL}/manage/resource.do?r=${resource.shortname}"><@s.text name='button.edit'/></a></#if>
        <input href="#menu-toggle2" style="display:none" class="expand" type="submit" id="menu-toggle2" name="submit" alt="expand" value=""/>
        <img class="latestVersion"/>
        <!-- Page Content -->
        <div id="page-content-wrapper">
            <div class="container-fluid">
                <div id="one" class="row">
                    <div>
                        <h1 class="rtitle">${eml.title!resource.shortname}</h1>
                        <p class="undertitle">
                          <#if resource.lastPublished??>
                            <#assign doi>${action.findDoiAssignedToPublishedVersion()!}</#assign>
                            <#-- the existence of parameter version means the version is not equal to the latest published version -->
                            <#if version?? && version.toPlainString() != resource.emlVersion.toPlainString()>
                              <em class="warn"><@s.text name='portal.resource.version'/>&nbsp;${version.toPlainString()}</em>
                              <@s.text name='portal.resource.publishedOn'/> ${eml.pubDate?date?string.medium} <#if doi?has_content>, doi:${doi}</#if>
                            <#else>
                              <@s.text name='portal.resource.latest.version'/>&nbsp;[${resource.emlVersion.toPlainString()!}]
                              <@s.text name='portal.resource.publishedOn'/> ${eml.pubDate?date?string.medium} <#if doi?has_content>, doi:${doi}</#if>
                            </#if>
                          <#else>
                            <@s.text name='portal.resource.published.never.long'/>
                          </#if>
                        </p>
                        <#if eml.logoUrl?has_content>
                          <div id="resourcelogo">
                            <img src="${eml.logoUrl}" />
                          </div>
                        </#if>
                        <p>
                          <#if eml.description?has_content>
                            <@textWithFormattedLink eml.description!no_description/>
                          <#else>
                            <@s.text name='portal.resource.no.description'/>
                          </#if>
                        </p>
                          <#if eml.distributionUrl?has_content || resource.lastPublished??>
                              <ul class="horizontal-list">
                                <#if eml.distributionUrl?has_content>
                                    <li class="box"><a href="${eml.distributionUrl}" class="icon icon-homepage"><@s.text name='eml.distributionUrl.short'/></a></li>
                                </#if>
                                <#if resource.status=="REGISTERED" && resource.key??>
                                  <li class="box"><a href="${cfg.portalUrl}/dataset/${resource.key}" class="icon icon-gbif"><@s.text name='portal.resource.gbif.page.short'/></a></li>
                                </#if>
                                <#if metadataOnly == false>
                                  <li class="box"><a href="${download_dwca_url}" class="icon icon-download"><@s.text name='portal.resource.published.dwca'/></a></li>
                                </#if>
                                <#if resource.lastPublished??>
                                  <li class="box"><a href="${download_eml_url}" class="icon icon-download"><@s.text name='portal.resource.published.eml'/></a></li>
                                  <li class="box"><a href="${download_rtf_url}" class="icon icon-download"><@s.text name='portal.resource.published.rtf'/></a></li>
                                  <#if resource.versionHistory??>
                                      <li class="box"><a href="${anchor_versions}" class="icon icon-clock"><@s.text name='portal.resource.versions'/></a></li>
                                  </#if>
                                  <li class="box"><a href="${anchor_rights}" class="icon icon-key"><@s.text name='eml.intellectualRights.simple'/></a></li>
                                  <li class="box"><a href="${anchor_citation}" class="icon icon-book"><@s.text name='portal.resource.cite'/></a></li>
                                </#if>
                              </ul>
                          </#if>

                        <div class="clearfix"></div>
                    </div>
                </div>

              <!-- Dataset must have been published for versions, downloads, and how to cite sections to show -->
              <#if resource.lastPublished??>

                  <!-- downloads section -->
                  <div id="downloads" class="row">
                     <div>
                        <h1><@s.text name='portal.resource.downloads'/></h1>
                        <#if metadataOnly == true>
                            <p><@s.text name='portal.resource.downloads.metadataOnly.verbose'/></p>
                        <#else>
                            <p><@s.text name='portal.resource.downloads.verbose'/></p>
                        </#if>

                        <table class="downloads">
                            <#-- Archive, EML, and RTF download links include Google Analytics event tracking -->
                            <#-- e.g. Archive event tracking includes components: _trackEvent method, category, action, label, (int) value -->
                            <#-- EML and RTF versions can always be retrieved by version number but DWCA versions are only stored if IPT Archive Mode is on -->
                            <#if metadataOnly == false>
                                <tr>
                                    <th><@s.text name='portal.resource.dwca.verbose'/></th>
                                    <#if version?? && version.toPlainString() != resource.emlVersion.toPlainString() && recordsPublishedForVersion??>
                                      <td><a href="${download_dwca_url}" onClick="_gaq.push(['_trackEvent', 'Archive', 'Download', '${resource.shortname}', ${recordsPublishedForVersion?c!0} ]);"><@s.text name='portal.resource.download'/></a>
                                        ${recordsPublishedForVersion?c!0} <@s.text name='portal.resource.records'/>&nbsp;<#if eml.language?has_content && languages[eml.language]?has_content><@s.text name='eml.language.available'><@s.param>${languages[eml.language]?cap_first!}</@s.param></@s.text></#if> (${dwcaSizeForVersion!})
                                      </td>
                                    <#else>
                                      <td><a href="${download_dwca_url}" onClick="_gaq.push(['_trackEvent', 'Archive', 'Download', '${resource.shortname}', ${resource.recordsPublished?c!0} ]);"><@s.text name='portal.resource.download'/></a>
                                        ${resource.recordsPublished?c!0} <@s.text name='portal.resource.records'/>&nbsp;<#if eml.language?has_content && languages[eml.language]?has_content><@s.text name='eml.language.available'><@s.param>${languages[eml.language]?cap_first!}</@s.param></@s.text></#if> (${dwcaFormattedSize!})
                                      </td>
                                    </#if>
                                </tr>
                            </#if>
                              <tr>
                                  <th><@s.text name='portal.resource.metadata.verbose'/></th>
                                  <td><a href="${download_eml_url}" onClick="_gaq.push(['_trackEvent', 'EML', 'Download', '${resource.shortname}']);"><@s.text name='portal.resource.download'/></a>
                                      <#if eml.metadataLanguage?has_content && languages[eml.metadataLanguage]?has_content><@s.text name='eml.language.available'><@s.param>${languages[eml.metadataLanguage]?cap_first!}</@s.param></@s.text></#if> (${emlFormattedSize})
                                  </td>
                              </tr>

                              <tr>
                                  <th><@s.text name='portal.resource.rtf.verbose'/></th>
                                  <td><a href="${download_rtf_url}" onClick="_gaq.push(['_trackEvent', 'RTF', 'Download', '${resource.shortname}']);"><@s.text name='portal.resource.download'/></a>
                                      <#if eml.metadataLanguage?has_content && languages[eml.metadataLanguage]?has_content><@s.text name='eml.language.available'><@s.param>${languages[eml.metadataLanguage]?cap_first!}</@s.param></@s.text></#if> (${rtfFormattedSize})
                                  </td>
                              </tr>
                        </table>
                     </div>
                  </div>

                <!-- versions section -->
                <#if resource.versionHistory??>
                <div id ="versions" class="row">
                    <div>
                        <h1><@s.text name='portal.resource.versions'/></h1>
                      <@versionsTable numVersionsShown=3 sEmptyTable="dataTables.sEmptyTable.versions" baseURL=baseURL shortname=resource.shortname />
                        <div id="vtableContainer"></div>
                        <p>
                        <div class="clearfix"></div>
                        </p>
                    </div>
                </div>
                </#if>

                <!-- citation section -->
                <#if eml.citation?? && (eml.citation.citation?has_content || eml.citation.identifier?has_content)>
                    <div id="citation" class="row">
                        <div>
                            <h1><@s.text name='portal.resource.cite.howTo'/></h1>
                            <p>
                              <#if version?? && version.toPlainString() != resource.emlVersion.toPlainString()>
                                  <em class="warn"><@s.text name='portal.resource.latest.version.warning'/>&nbsp;</em>
                              </#if>
                              <@s.text name='portal.resource.cite.help'/>:
                            </p>
                            <p class="howtocite"><@textWithFormattedLink eml.citation.citation/></p>
                        </div>
                    </div>
                </#if>

                <!-- rights section -->
                <#if eml.intellectualRights?has_content>
                <div id="rights" class="row">
                    <div>
                        <h1><@s.text name='eml.intellectualRights.simple'/></h1>
                        <p><@s.text name='portal.resource.rights.help'/>:</p>
                        <@licenseLogoClass eml.intellectualRights!/>
                        <p><#noescape>${eml.intellectualRights!}</#noescape></p>
                    </div>
                </div>
                </#if>

              <!-- GBIF Registration section -->
              <div id="gbif" class="row">
                  <div>
                      <h1><@s.text name='portal.resource.organisation.key'/></h1>
                    <#if resource.status=="REGISTERED" && resource.organisation??>
                        <p>
                          <@s.text name='manage.home.registered.verbose'><@s.param>${cfg.portalUrl}/dataset/${resource.key}</@s.param><@s.param>${resource.key}</@s.param></@s.text>
                        <#-- Warning: in dev mode organization link goes to /organization (GBIF Registry console), in prod mode the link goes to /publisher (GBIF Portal) -->
                          <#if cfg.getRegistryType() =='DEVELOPMENT'>
                              &nbsp;<@s.text name='manage.home.published.verbose'><@s.param>${cfg.portalUrl}/organization/${resource.organisation.key}</@s.param><@s.param>${resource.organisation.name}</@s.param><@s.param>${cfg.portalUrl}/node/${resource.organisation.nodeKey!"#"}</@s.param><@s.param>${resource.organisation.nodeName!}</@s.param></@s.text>
                          <#else>
                              &nbsp;<@s.text name='manage.home.published.verbose'><@s.param>${cfg.portalUrl}/publisher/${resource.organisation.key}</@s.param><@s.param>${resource.organisation.key}</@s.param><@s.param>${cfg.portalUrl}/node/${resource.organisation.nodeKey!"#"}</@s.param><@s.param>${resource.organisation.nodeName!}</@s.param></@s.text>
                          </#if>
                        </p>
                    <#else>
                        <p><@s.text name='manage.home.not.registered.verbose'/></p>
                    </#if>
                  </div>
              </div>

              <!-- Keywords section -->
              <#if eml.subject?has_content>
                  <div id="keywords" class="row">
                      <div>
                          <h1><@s.text name='portal.resource.summary.keywords'/></h1>
                          <p><@textWithFormattedLink eml.subject!no_description/></p>
                      </div>
                  </div>
              </#if>

              <!-- External data section -->
                <#if (eml.physicalData?size > 0 )>
                <div id="external" class="row">
                    <div>
                        <h1><@s.text name='manage.metadata.physical.alternativeTitle'/></h1>
                        <p>The resource data is also availalble in other formats:</p>
                        <table>
                          <#list eml.physicalData as item>
                            <#assign link=eml.physicalData[item_index]/>
                              <tr><th>${link.name!}</th><td><a href="${link.distributionUrl}">${link.distributionUrl!"?"}</a>
                                <#if link.charset?? || link.format?? || link.formatVersion??>
                                ${link.charset!} ${link.format!} ${link.formatVersion!}
                                </#if>
                              </td></tr>
                          </#list>
                        </table>
                    </div>
                </div>
                </#if>

                <!-- Contacts section -->
                <#if (eml.contacts?size>0) || (eml.creators?size>0) || (eml.metadataProviders?size>0) || (eml.associatedParties?size>0)>
                  <div id="contacts" class="row">
                    <div>
                        <h1><@s.text name='portal.resource.contacts'/></h1>
                        <p><@s.text name='portal.resource.creator.intro'/>:</p>
                        <div class="fullwidth">
                          <@contactList eml.creators/>
                        </div>
                        <div class="clearfix"></div>

                        <p class="twenty_top"><@s.text name='portal.resource.contact.intro'/>:</p>
                        <div class="fullwidth">
                          <@contactList eml.contacts/>
                        </div>
                        <div class="clearfix"></div>

                        <p class="twenty_top"><@s.text name='portal.metadata.provider.intro'/>:</p>
                        <div class="fullwidth">
                          <@contactList eml.metadataProviders/>
                        </div>
                        <div class="clearfix"></div>

                      <#if (eml.associatedParties?size>0)>
                          <p class="twenty_top"><@s.text name='portal.associatedParties.intro'/>:</p>
                          <div class="fullwidth">
                            <@contactList eml.associatedParties/>
                          </div>
                          <div class="clearfix"></div>
                      </#if>
                    </div>
                  </div>
                </#if>

                <!-- Geo coverage section -->
                <#if eml.geospatialCoverages[0]??>
                    <div id="geospatial" class="row">
                        <div>
                            <h1><@s.text name='portal.resource.summary.geocoverage'/></h1>
                            <p><@textWithFormattedLink eml.geospatialCoverages[0].description!no_description/></p>
                            <table>
                                    <tr>
                                        <th><@s.text name='eml.geospatialCoverages.boundingCoordinates'/></th>
                                        <td>${eml.geospatialCoverages[0].boundingCoordinates.min.latitude}, ${eml.geospatialCoverages[0].boundingCoordinates.max.latitude} / ${eml.geospatialCoverages[0].boundingCoordinates.min.longitude}, ${eml.geospatialCoverages[0].boundingCoordinates.max.longitude} <@s.text name='eml.geospatialCoverages.boundingCoordinates.indicator'/></td>
                                    </tr>
                            </table>
                        </div>
                    </div>
                </#if>

                <!-- Taxonomic coverage sections -->
                <#if ((organizedCoverages?size > 0))>
                    <div id="taxanomic" class="row">
                        <div>
                            <h1><@s.text name='manage.metadata.taxcoverage.title'/></h1>
                            <#list organizedCoverages as item>
                              <p><@textWithFormattedLink item.description!no_description/></p>
                                <table>
                                  <#list item.keywords as k>
                                    <#if k.rank?has_content && ranks[k.rank?string]?has_content && (k.displayNames?size > 0) >
                                        <tr>
                                        <#-- 1st col, write rank name once. Avoid problem accessing "class" from map - it displays "java.util.LinkedHashMap" -->
                                          <#if k.rank?lower_case == "class">
                                              <th>Class</th>
                                          <#else>
                                              <th>${ranks[k.rank?html]?cap_first!}</th>
                                          </#if>
                                        <#-- 2nd col, write comma separated list of names in format: scientific name (common name) -->
                                            <td>
                                              <#list k.displayNames as name>
                                              ${name}<#if name_has_next>, </#if>
                                              </#list>
                                            </td>
                                        </tr>
                                    </#if>
                                  </#list>
                                </table>
                                <#-- give some space between taxonomic coverages -->
                                <#if item_has_next></br></#if>
                            </#list>
                        </div>
                    </div>
                </#if>

                <!-- Temporal coverages section -->
                  <#if ((eml.temporalCoverages?size > 0))>
                  <div id="temporal" class="row">
                      <div>
                          <h1><@s.text name='manage.metadata.tempcoverage.title'/></h1>
                        <#list eml.temporalCoverages as item>
                            <table>
                              <#if ("${item.type}" == "DATE_RANGE") && eml.temporalCoverages[item_index].startDate?? && eml.temporalCoverages[item_index].endDate?? >
                                  <tr>
                                      <th><@s.text name='eml.temporalCoverages.startDate'/> / <@s.text name='eml.temporalCoverages.endDate'/></th>
                                      <td>${eml.temporalCoverages[item_index].startDate?date} / ${eml.temporalCoverages[item_index].endDate?date}</td>
                                  </tr>
                              <#elseif "${item.type}" == "SINGLE_DATE" && eml.temporalCoverages[item_index].startDate?? >
                                  <tr>
                                      <th><@s.text name='eml.temporalCoverages.startDate'/></th>
                                      <td>${eml.temporalCoverages[item_index].startDate?date}</td>
                                  </tr>
                              <#elseif "${item.type}" == "FORMATION_PERIOD" && eml.temporalCoverages[item_index].formationPeriod?? >
                                  <tr>
                                      <th><@s.text name='eml.temporalCoverages.formationPeriod'/></th>
                                      <td>${eml.temporalCoverages[item_index].formationPeriod}</td>
                                  </tr>
                              <#elseif eml.temporalCoverages[item_index].livingTimePeriod??> <!-- LIVING_TIME_PERIOD -->
                                  <tr>
                                      <th><@s.text name='eml.temporalCoverages.livingTimePeriod'/></th>
                                      <td>${eml.temporalCoverages[item_index].livingTimePeriod!}</td>
                                  </tr>
                              </#if>
                            </table>
                        </#list>
                      </div>
                  </div>
                  </#if>


                <!-- Project section -->
                  <#if eml.project?? && eml.project.title?has_content>
                  <div id="project" class="row">
                      <div>
                          <h1><@s.text name='manage.metadata.project.title'/></h1>
                          <p><@textWithFormattedLink eml.project.description!no_description/></p>
                          <table>
                            <#if eml.project.title?has_content>
                                <tr>
                                    <th><@s.text name='eml.project.title'/></th>
                                    <td><@textWithFormattedLink eml.project.title!/></td>
                                </tr>
                            </#if>
                            <#if eml.project.identifier?has_content>
                                <tr>
                                    <th><@s.text name='eml.project.identifier'/></th>
                                    <td><@textWithFormattedLink eml.project.identifier!/></td>
                                </tr>
                            </#if>
                            <#if eml.project.funding?has_content>
                                <tr>
                                    <th><@s.text name='eml.project.funding'/></th>
                                    <td><@textWithFormattedLink eml.project.funding/></td>
                                </tr>
                            </#if>
                            <#if eml.project.studyAreaDescription.descriptorValue?has_content>
                                <tr>
                                    <th><@s.text name='eml.project.studyAreaDescription.descriptorValue'/></th>
                                    <td><@textWithFormattedLink eml.project.studyAreaDescription.descriptorValue/></td>
                                </tr>
                            </#if>
                            <#if eml.project.designDescription?has_content>
                                <tr>
                                    <th><@s.text name='eml.project.designDescription'/></th>
                                    <td><@textWithFormattedLink eml.project.designDescription/></td>
                                </tr>
                            </#if>
                            <#if (eml.project.personnel?size >0)>
                            <tr>
                                <th><@s.text name='eml.project.personnel'/></th>
                                <td>
                                    <@contactList eml.project.personnel "" 1/>
                                </td>
                            </tr>
                            </#if>
                          </table>
                      </div>
                  </div>
                  </#if>

                <!-- Sampling methods section -->
                  <#if eml.studyExtent?has_content || eml.sampleDescription?has_content || eml.qualityControl?has_content || (eml.methodSteps?? && (eml.methodSteps?size>=1) && eml.methodSteps[0]?has_content) >
                  <div id="methods" class="row">
                      <div>
                          <h1><@s.text name='manage.metadata.methods.title'/></h1>
                          <p><@textWithFormattedLink eml.sampleDescription!no_description/></p>
                          <table>
                            <#if eml.studyExtent?has_content>
                                <tr>
                                    <th><@s.text name='eml.studyExtent'/></th>
                                    <td><@textWithFormattedLink eml.studyExtent/></td>
                                </tr>
                            </#if>

                            <#if eml.qualityControl?has_content>
                                <tr>
                                    <th><@s.text name='eml.qualityControl'/></th>
                                    <td><@textWithFormattedLink eml.qualityControl/></td>
                                </tr>
                            </#if>
                            <#list eml.methodSteps as item>
                              <#if (eml.methodSteps[item_index]?has_content) >
                                  <tr>
                                      <th><@s.text name='eml.methodSteps'/> ${item_index+1}</th>
                                      <td><@textWithFormattedLink eml.methodSteps[item_index]/></td>
                                  </tr>
                              </#if>
                            </#list>
                          </table>
                      </div>
                  </div>
                  </#if>

                <!-- Collections section -->
                  <#if eml.collections?? && (eml.collections?size > 0) && eml.collections[0].collectionName?has_content >
                  <div id="collection" class="row">
                      <div>
                          <h1><@s.text name='manage.metadata.collections.title'/></h1>
                          <#list eml.collections as item>
                              <table>
                                <#if item.collectionName?has_content>
                                    <tr>
                                        <th><@s.text name='eml.collectionName'/></th>
                                        <td>${item.collectionName!}</td>
                                    </tr>
                                </#if>
                                <#if item.collectionId?has_content>
                                    <tr>
                                        <th><@s.text name='eml.collectionId'/></th>
                                        <td>${item.collectionId!}</td>
                                    </tr>
                                </#if>
                                <#if item.parentCollectionId?has_content>
                                    <tr>
                                        <th><@s.text name='eml.parentCollectionId'/></th>
                                        <td>${item.parentCollectionId!}</td>
                                    </tr>
                                </#if>
                              </table>
                          </#list>

                          <table>
                              <#if eml.specimenPreservationMethods?? && (eml.specimenPreservationMethods?size>0) && eml.specimenPreservationMethods[0]?has_content >
                                  <tr>
                                    <th><@s.text name='eml.specimenPreservationMethod.plural'/></th>
                                    <td>
                                      <#list eml.specimenPreservationMethods as item>
                                        ${preservationMethods[item]?cap_first!}<#if item_has_next>,&nbsp;</#if>
                                      </#list>
                                    </td>
                                  </tr>
                              </#if>
                          </table>

                          <table>

                                <tr>
                                    <th><@s.text name='manage.metadata.collections.curatorialUnits.title'/></th>
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
                  </div>
                </#if>

              <!-- bibliographic citations section -->
                <#if eml.bibliographicCitationSet?? && (eml.bibliographicCitationSet.bibliographicCitations?has_content)>
                <div id="references" class="row">
                    <h1><@s.text name='manage.metadata.citations.bibliography'/></h1>
                    <ol>
                      <#list eml.bibliographicCitationSet.bibliographicCitations as item>
                        <#if item.citation?has_content>
                            <li>
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
                  <div id="additional" class="row">
                      <div>
                          <h1><@s.text name='manage.metadata.additional.title'/></h1>
                          <#if eml.additionalInfo?has_content>
                              <p><@textWithFormattedLink eml.additionalInfo/></p>
                          </#if>

                          <table>
                            <#if eml.purpose?has_content>
                                <tr>
                                    <th><@s.text name='eml.purpose'/></th>
                                    <td><@textWithFormattedLink eml.purpose/></td>
                                </tr>
                            </#if>
                            <#if eml.updateFrequencyDescription?has_content>
                                <tr>
                                    <th><@s.text name='eml.updateFrequencyDescription'/></th>
                                    <td><@textWithFormattedLink eml.updateFrequencyDescription/></td>
                                </tr>
                            </#if>
                            <#if (eml.alternateIdentifiers?size > 0)>
                              <#list eml.alternateIdentifiers as item>
                              <tr>
                                  <th><#if item_index ==0><@s.text name='manage.metadata.alternateIdentifiers.title'/></#if></th>
                                  <td><@textWithFormattedLink eml.alternateIdentifiers[item_index]!/></td>
                              </tr>
                              </#list>
                            </#if>
                          </table>
                      </div>
                  </div>
                  </#if>


            </div>
        </div>
        <!-- /#page-content-wrapper -->

    </div>
    <!-- /#wrapper -->

    <#include "/WEB-INF/pages/inc/footer.ftl">

    <!-- jQuery v1.11.1 -->
    <script type="text/javascript" src="${baseURL}/js/jquery/jquery-1.11.1.min.js"></script>
    <!-- DataTables v1.9.4 -->
    <script type="text/javascript" language="javascript" src="${baseURL}/js/jquery/jquery.dataTables.js"></script>

    <!-- Menu Toggle Script -->
    <script>
        $("#menu-toggle").click(function(e) {
            e.preventDefault();
            $("#wrapper").toggleClass("toggled");
            $("#sidebar-wrapper").toggleClass("toggled");
            $("#menu-toggle").hide();
            $("#menu-toggle2").show();
        });
        $("#menu-toggle2").click(function(e) {
            e.preventDefault();
            $("#wrapper").toggleClass("toggled");
            $("#sidebar-wrapper").toggleClass("toggled");
            $("#menu-toggle").show();
            $("#menu-toggle2").hide();
        });

        $(".sidebar-anchor").click(function(e) {
            $("a").removeClass("sidebar-nav-selected");
            $(this).addClass("sidebar-nav-selected");
        });

        // hide and make contact addresses toggable
        $(".contactName").next().hide();
        $(".contactName").click(function(e){
            $(this).next().slideToggle("fast");
        });

    </script>

</#escape>


