<#escape x as x?html>
  <#include "/WEB-INF/pages/inc/header.ftl">
  <#include "/WEB-INF/pages/inc/menu.ftl">
  <#include "/WEB-INF/pages/macros/forms.ftl"/>
  <#include "/WEB-INF/pages/macros/versionsTable.ftl"/>
    <style>
        #wrapper {
            padding-left: 0;
            -webkit-transition: all 0.5s ease;
            -moz-transition: all 0.5s ease;
            -o-transition: all 0.5s ease;
            transition: all 0.5s ease;
        }

        #wrapper.toggled {
            padding-left: 220px;
        }

        #sidebar-wrapper {
            z-index: 1000;
            position: fixed;
            width: 0;
            height: auto;
            overflow-y: visible;
            background: #dbf0e8;
            -webkit-transition: all 0.5s ease;
            -moz-transition: all 0.5s ease;
            -o-transition: all 0.5s ease;
            transition: all 0.5s ease;
        }

        #sidebar-wrapper.toggled {
            display: none;
        }

        #page-content-wrapper {
            width: 100%;
            padding: 0;
            margin: 0;
        }

        #wrapper.toggled #page-content-wrapper {
            z-index: 10000;
            position: absolute;
            margin-right: -220px;
        }

        /* Sidebar Styles */

        .sidebar-nav {
            position: absolute;
            top: 0;
            right: 25px;
            width: 220px;
            margin: 0;
            padding: 0;
            list-style: none;
        }

        .sidebar-nav li {
            text-indent: 10px;
            line-height: 30px;
        }

        .sidebar-nav li a {
            display: block;
            text-decoration: none;
            color: #999999;
        }

        .sidebar-nav > li a {
            display: block;
            text-decoration: none;
            color: #999999;
        }

        .sidebar-nav li a:hover {
            text-decoration: none;
            color: #666;
            background: #dbf0e8;
        }

        .sidebar-nav li a:active,
        .sidebar-nav li a:focus {
            text-decoration: none;
        }

        .sidebar-nav-selected {
            font-size:1.1em;
            text-indent:5px;
        }

        .sidebar-nav > .sidebar-brand a {
            color: #999999;
        }

        .sidebar-nav > .sidebar-brand a:hover {
            color: #fff;
            background: none;
        }

        @media(min-width:900px) {
            #wrapper {
                padding-left: 220px;
            }

            #wrapper.toggled {
                padding-left: 0;
            }

            #sidebar-wrapper {
                width: 220px;
            }

            #wrapper.toggled #sidebar-wrapper {
                width: 0;
            }

            #page-content-wrapper {
                padding: 0;
            }

            #wrapper.toggled #page-content-wrapper {
                position: relative;
                margin-right: 0;
            }
        }

        input.expand {
            background:url(/images/icons/list-menu.png);
            background-repeat: no-repeat;
            width:20px;
            height:20px;
            border: 0;
        }

        input.contract {
            background:url(/images/icons/arrows-expand.png);
            background-repeat: no-repeat;
            width:20px;
            height:20px;
            border: 0;
        }

        div#vtableContainer {
            font-size:0.9em;
        }

        h1.rtitle {
            margin-bottom: 5px;
        }
        p.undertitle {
            font-style: italic;
            font-size: 0.9em
        }

        tr.even td.sorting_1 {
            background-color: transparent;
        }

        tr.odd td.sorting_1 {
            background-color: transparent;
        }

        tr.odd {
            background-color: transparent;
        }

        .downloads th, .downloads td {
            font-size: 0.9em;
        }

        .downloads th {
            font-weight: bold;
            padding-right: 20px;
        }

        p.howtocite {
            padding: 10px 20px 10px 20px;
            border-style: solid;
            border-width: 1px;
            font-style: italic;
        }

        div.contact {
            word-wrap: break-word;
            text-align: left;
        }

        .contact_row, .row {
            clear: both;
        }

        div.contact .contactType {
            color: #333333;
            margin-bottom: 0 !important;
            text-shadow: 0 1px white;
            text-transform: uppercase;
        }

        div.contact .contactName {
            font-style: italic;
            text-decoration: underline;
        }

        address span, .address span {
            display: block;
        }

        .address a {
            color: #999999;
            text-decoration: none;
        }

        div.contact .email + .phone {
            padding-top: 0;
        }

        .fullwidth {
            font-size: 0.9em;
            float: left;
            width: 700px;
        }

        .fullwidth .contact_row.col2 > div {
            width: 340px;
        }

        .contact_row > div {
            float: left;
            padding-bottom: 20px;
            padding-right: 10px;
        }

    </style>

<#--
	Construct a Contact. Parameters are the actual contact object, and the contact type
-->
<#macro contact con type>
  <div class="contact">

    <div class="contactType">
      <#if con.role?has_content>
        ${roles[con.role]?cap_first!}
      <#else>
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

          <#if con.homepage?has_content>
              <a href="${con.homepage}">${con.homepage}</a>
          </#if>

        </div>
    </div>
  </div>
</#macro>

<#-- Creates a column list of contacts, defaults to 2 columns -->
  <#macro contactList contacts columns=2>
    <#list contacts as c>
      <#if c_index%columns==0>
      <div class="contact_row col${columns}">
      </#if>
      <@contact con=c type="contact"/>
      <#if c_index%columns==columns-1 || !c_has_next >
          <!-- end of row -->
      </div>
      </#if>
    </#list>
  </#macro>

<!-- Sidebar -->
<div id="sidebar-wrapper">

    <ul class="sidebar-nav">
        <li>
          <input href="#menu-toggle" class="contract" type="submit" id="menu-toggle" name="submit" alt="expand" value=""/>
        </li>
        <li>
            <a class="sidebar-anchor" href="#one"><@s.text name='portal.resource.summary'/></a>
        </li>
        <!-- Dataset must have been published to show versions, downloads, and how to cite sections -->
        <#if resource.lastPublished??>
        <li>
            <a class="sidebar-anchor" href="#two"><@s.text name='portal.resource.versions'/></a>
        </li>
        <li>
            <a class="sidebar-anchor" href="#three"><@s.text name='portal.resource.downloads'/></a>
        </li>
        <li>
            <a class="sidebar-anchor" href="#four"><@s.text name='portal.resource.cite.howTo'/></a>
        </li>
        </#if>
        <!-- Keywords section -->
        <#if eml.subject?has_content>
            <li>
                <a class="sidebar-anchor" href="#five"><@s.text name='portal.resource.summary.keywords'/></a>
            </li>
        </#if>
        <li>
            <a class="sidebar-anchor" href="#six"><@s.text name='portal.resource.contacts'/></a>
        </li>
        <li>
            <a class="sidebar-anchor" href="#seven"><@s.text name='portal.resource.summary.geocoverage'/></a>
        </li>
        <li>
            <a class="sidebar-anchor" href="#eight"><@s.text name='portal.resource.summary.taxcoverage'/></a>
        </li>
    </ul>
</div>
<!-- /#sidebar-wrapper -->


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
                            <#-- the existence of parameter version means the version is not equal to the latest published version -->
                            <#if version?? && version!=resource.emlVersion>
                              <em class="warn"><@s.text name='portal.resource.version'/>&nbsp;${version}</em>
                            <#else>
                              <@s.text name='portal.resource.latest.version'/>&nbsp;[2.1]
                            </#if>
                            <@s.text name='portal.resource.publishedOn'/> ${eml.pubDate?date?string.medium}, doi:${versions[3].doi!} - <a href="#four"><@s.text name='portal.resource.cite'/></a>
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
                          <#if resource.description?has_content>
                            <@textWithFormattedLink resource.description!no_description/>
                          <#else>
                            <@s.text name='portal.resource.no.description'/>
                          </#if>
                        </p>

                    </div>
                </div>

              <!-- Dataset must have been published for versions, downloads, and how to cite sections to show -->
              <#if resource.lastPublished??>

                <!-- versions section -->
                <div id ="two" class="row">
                    <div>
                        <h1><@s.text name='portal.resource.versions'/></h1>
                        <@versionsTable numVersionsShown=2 sEmptyTable="dataTables.sEmptyTable.versions" columnToSortOn=0 sortOrder="desc" baseURL=baseURL shortname=resource.shortname />
                        <div id="vtableContainer"></div>
                        <p>
                          <div class="clearfix"></div>
                        </p>
                    </div>
                </div>

                  <!-- downloads section -->
                  <div id="three" class="row">
                    <div>
                        <h1><@s.text name='portal.resource.downloads'/></h1>
                        <p>Download the latest version of the dataset as a Darwin Core Archive or the dataset metadata as EML or RTF:</p>

                        <table class="downloads">
                            <#-- Archive, EML, and RTF download links include Google Analytics event tracking -->
                            <#-- e.g. Archive event tracking includes components: _trackEvent method, category, action, label, (int) value -->
                            <#-- EML and RTF versions can always be retrieved by version number but DWCA versions are only stored if IPT Archive Mode is on -->
                            <#if metadataOnly>
                                <tr>
                                    <th>Dataset -

                                      <#if version?? && version!=resource.emlVersion>
                                        <#if recordsPublishedForVersion?? && recordsPublishedForVersion!= 0>
                                            ${recordsPublishedForVersion?c} <@s.text name='portal.resource.records'/>
                                        </#if>
                                      <#else>
                                        ${resource.recordsPublished?c!0} <@s.text name='portal.resource.records'/>
                                      </#if>

                                    </th>
                                    <td><a href="${baseURL}/archive.do?r=${resource.shortname}<#if version??>&v=${version}</#if>"
                                           onClick="_gaq.push(['_trackEvent', 'Archive', 'Download', '${resource.shortname}', ${resource.recordsPublished?c!0} ]);"><@s.text name='portal.resource.download'/></a>
                                        (${dwcaFormattedSize})
                                    </td>
                                </tr>
                            </#if>
                              <tr>
                                  <th>Metadata as an EML file</th>
                                  <td><a href="${baseURL}/eml.do?r=${resource.shortname}&v=<#if version??>${version}<#else>${resource.emlVersion}</#if>"
                                         onClick="_gaq.push(['_trackEvent', 'EML', 'Download', '${resource.shortname}']);"><@s.text name='portal.resource.download'/></a>
                                      (${emlFormattedSize})
                                  </td>
                              </tr>

                              <tr>
                                  <th>Metadata as an RTF file</th>
                                  <td><a href="${baseURL}/rtf.do?r=${resource.shortname}&v=<#if version??>${version}<#else>${resource.emlVersion}</#if>"
                                         onClick="_gaq.push(['_trackEvent', 'RTF', 'Download', '${resource.shortname}']);"><@s.text name='portal.resource.download'/></a>
                                      (${rtfFormattedSize})
                                  </td>
                              </tr>
                        </table>
                    </div>
                  </div>


                <!-- citation section -->
                <#if eml.citation?? && (eml.citation.citation?has_content || eml.citation.identifier?has_content)>
                    <div id="four" class="row">
                        <div>
                            <h1><@s.text name='portal.resource.cite.howTo'/></h1>
                            <p>
                              <#if version?? && version!=resource.emlVersion>
                                  <em class="warn"><@s.text name='portal.resource.latest.version.warning'/>&nbsp;</em>
                              </#if>
                              <@s.text name='portal.resource.cite.help'/>:</p>
                            <p class="howtocite"><@textWithFormattedLink eml.citation.citation/>&nbsp;<@textWithFormattedLink eml.citation.identifier!/></p>
                        </div>
                    </div>
                </#if>

              </#if>

              <!-- Keywords section -->
              <#if eml.subject?has_content>
                  <div id="five" class="row">
                      <div>
                          <h1><@s.text name='portal.resource.summary.keywords'/></h1>
                          <p><@textWithFormattedLink eml.subject!no_description/></p>
                      </div>
                  </div>
              </#if>


                <!-- Contacts section -->
                <div id="six" class="row">
                    <div>
                      <h1><@s.text name='portal.resource.contacts'/></h1>
                        <div class="fullwidth">
                          <@contactList contacts/>
                        </div>
                    </div>
                </div>
                <div id="seven" class="row">
                    <div>
                        <h1><@s.text name='portal.resource.summary.geocoverage'/></h1>
                        <p>This template has a responsive menu toggling system. The menu will appear collapsed on smaller screens, and will appear non-collapsed on larger screens. When toggled using the button below, the menu will appear/disappear. On small screens, the page content will be pushed off canvas.</p>
                    </div>
                </div>
                <div id="eight" class="row">
                    <div>
                        <h1><@s.text name='portal.resource.summary.taxcoverage'/></h1>
                        <p>This template has a responsive menu toggling system. The menu will appear collapsed on smaller screens, and will appear non-collapsed on larger screens. When toggled using the button below, the menu will appear/disappear. On small screens, the page content will be pushed off canvas.</p>
                    </div>
                </div>
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


