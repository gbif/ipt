<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.DataPackageMetadataAction" -->
<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.metadata.basic.title'/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <script>
        $(document).ready(function(){
            var contributorItems = calcNumberOfItems("contributor");
            var sourcesItems = calcNumberOfItems("source");
            var licensesItems = calcNumberOfItems("license");

            function calcNumberOfItems(name) {
                var lastItem = $("#" + name + "-items .item:last-child").attr("id");
                if (lastItem !== undefined)
                    return parseInt(lastItem.split("-")[2]);
                else
                    return -1;
            }

            $("#plus-contributor").click(function (event) {
                event.preventDefault();
                addNewContributorItem(true);
            });

            $("#plus-source").click(function (event) {
                event.preventDefault();
                console.log("plus source triggered")
                addNewSourceItem(true);
            });

            $("#plus-license").click(function (event) {
                event.preventDefault();
                addNewLicenseItem(true);
            });

            function addNewContributorItem(effects) {
                var newItem = $('#baseItem-contributor').clone();
                if (effects) newItem.hide();
                newItem.appendTo('#contributor-items');

                if (effects) {
                    newItem.slideDown('slow');
                }

                setContributorItemIndex(newItem, ++contributorItems);

                initInfoPopovers(newItem[0]);
            }

            function addNewSourceItem(effects) {
                console.log("add new source triggered")
                var newItem = $('#baseItem-source').clone();
                if (effects) newItem.hide();
                newItem.appendTo('#source-items');

                if (effects) {
                    newItem.slideDown('slow');
                }

                setSourceItemIndex(newItem, ++sourcesItems);

                initInfoPopovers(newItem[0]);
            }

            function addNewLicenseItem(effects) {
                var newItem = $('#baseItem-license').clone();
                if (effects) newItem.hide();
                newItem.appendTo('#license-items');

                if (effects) {
                    newItem.slideDown('slow');
                }

                setLicenseItemIndex(newItem, ++licensesItems);

                initInfoPopovers(newItem[0]);
            }

            function removeContributorItem(event) {
                event.preventDefault();
                var $target = $(event.target);
                if (!$target.is('a')) {
                    $target = $(event.target).closest('a');
                }
                $('#contributor-item-' + $target.attr("id").split("-")[2]).slideUp('slow', function () {
                    $(this).remove();
                    $("#contributor-items .item").each(function (index) {
                        setContributorItemIndex($(this), index);
                    });
                    calcNumberOfItems("contributor");
                });
            }

            function removeSourceItem(event) {
                event.preventDefault();
                var $target = $(event.target);
                if (!$target.is('a')) {
                    $target = $(event.target).closest('a');
                }
                $('#source-item-' + $target.attr("id").split("-")[2]).slideUp('slow', function () {
                    $(this).remove();
                    $("#source-items .item").each(function (index) {
                        setSourceItemIndex($(this), index);
                    });
                    calcNumberOfItems("source");
                });
            }

            function removeLicenseItem(event) {
                event.preventDefault();
                var $target = $(event.target);
                if (!$target.is('a')) {
                    $target = $(event.target).closest('a');
                }
                $('#license-item-' + $target.attr("id").split("-")[2]).slideUp('slow', function () {
                    $(this).remove();
                    $("#license-items .item").each(function (index) {
                        setLicenseItemIndex($(this), index);
                    });
                    calcNumberOfItems("license");
                });
            }

            function setContributorItemIndex(item, index) {
                item.attr("id", "contributor-item-" + index);

                $("#contributor-item-" + index + " [id^='contributor-removeLink']").attr("id", "contributor-removeLink-" + index);
                $("#contributor-removeLink-" + index).click(function (event) {
                    removeContributorItem(event);
                });

                $("#contributor-item-" + index + " [id$='title']").attr("id", "metadata.contributors[" + index + "].title").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#contributor-item-" + index + " [for$='title']").attr("for", "metadata.contributors[" + index + "].title");

                $("#contributor-item-" + index + " [id$='path']").attr("id", "metadata.contributors[" + index + "].path").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#contributor-item-" + index + " [for$='path']").attr("for", "metadata.contributors[" + index + "].path");

                $("#contributor-item-" + index + " [id$='email']").attr("id", "metadata.contributors[" + index + "].email").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#contributor-item-" + index + " [for$='email']").attr("for", "metadata.contributors[" + index + "].email");

                $("#contributor-item-" + index + " [id$='role']").attr("id", "metadata.contributors[" + index + "].role").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#contributor-item-" + index + " [for$='role']").attr("for", "metadata.contributors[" + index + "].role");

                $("#contributor-item-" + index + " [id$='organization']").attr("id", "metadata.contributors[" + index + "].organization").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#contributor-item-" + index + " [for$='organization']").attr("for", "metadata.contributors[" + index + "].organization");
            }

            function setSourceItemIndex(item, index) {
                item.attr("id", "source-item-" + index);

                $("#source-item-" + index + " [id^='source-removeLink']").attr("id", "source-removeLink-" + index);
                $("#source-removeLink-" + index).click(function (event) {
                    removeSourceItem(event);
                });

                $("#source-item-" + index + " [id$='title']").attr("id", "metadata.sources[" + index + "].title").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#source-item-" + index + " [for$='title']").attr("for", "metadata.sources[" + index + "].title");

                $("#source-item-" + index + " [id$='path']").attr("id", "metadata.sources[" + index + "].path").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#source-item-" + index + " [for$='path']").attr("for", "metadata.sources[" + index + "].path");

                $("#source-item-" + index + " [id$='email']").attr("id", "metadata.sources[" + index + "].email").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#source-item-" + index + " [for$='email']").attr("for", "metadata.sources[" + index + "].email");
            }

            function setLicenseItemIndex(item, index) {
                item.attr("id", "license-item-" + index);

                $("#license-item-" + index + " [id^='license-removeLink']").attr("id", "license-removeLink-" + index);
                $("#license-removeLink-" + index).click(function (event) {
                    removeLicenseItem(event);
                });

                $("#license-item-" + index + " [id$='title']").attr("id", "metadata.licenses[" + index + "].title").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#license-item-" + index + " [for$='title']").attr("for", "metadata.licenses[" + index + "].title");

                $("#license-item-" + index + " [id$='path']").attr("id", "metadata.licenses[" + index + "].path").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#license-item-" + index + " [for$='path']").attr("for", "metadata.licenses[" + index + "].path");

                $("#license-item-" + index + " [id$='name']").attr("id", "metadata.licenses[" + index + "].name").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#license-item-" + index + " [for$='name']").attr("for", "metadata.licenses[" + index + "].name");

                $("#license-item-" + index + " [id$='scope']").attr("id", "metadata.licenses[" + index + "].scope").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#license-item-" + index + " [for$='scope']").attr("for", "metadata.licenses[" + index + "].scope");
            }

            $(".removeContributorLink").click(function (event) {
                removeContributorItem(event);
            });

            $(".removeSourceLink").click(function (event) {
                removeSourceItem(event);
            });

            $(".removeLicenseLink").click(function (event) {
                removeLicenseItem(event);
            });

            // scroll to the error if present
            var invalidElements = $(".is-invalid");

            if (invalidElements !== undefined && invalidElements.length > 0) {
                var invalidElement = invalidElements.first();
                var pos = invalidElement.offset().top - 100;
                // scroll to the element
                $('body, html').animate({scrollTop: pos});
            }
        });
    </script>
    <#assign currentMenu="manage"/>
    <#assign currentMetadataPage = "basic"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <form class="needs-validation" action="datapackage-metadata-${section}.do" method="post" novalidate>
        <div class="container-fluid bg-body border-bottom">
            <div class="container pt-2">
                <#include "/WEB-INF/pages/inc/action_alerts.ftl">
            </div>

            <div class="container my-3 p-3">
                <div class="text-center text-uppercase fw-bold fs-smaller-2">
                    <div class="text-center text-uppercase fw-bold fs-smaller-2">
                        <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                            <ol class="breadcrumb justify-content-center mb-0">
                                <li class="breadcrumb-item"><a href="/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                                <li class="breadcrumb-item"><a href="resource?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                                <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.metadata"/></li>
                            </ol>
                        </nav>
                    </div>
                </div>

                <div class="text-center">
                    <h1 class="py-2 mb-0 text-gbif-header fs-2 fw-normal">
                        <@s.text name='manage.datapackagemetadata.basic.title'/>
                    </h1>
                </div>

                <div class="text-center fs-smaller">
                    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">
                        <#if resource.title?has_content>
                            ${resource.title}
                        <#else>
                            ${resource.shortname}
                        </#if>
                    </a>
                </div>

                <div class="text-center mt-2">
                    <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save"/>
                    <@s.submit cssClass="button btn btn-sm btn-outline-secondary top-button" name="cancel" key="button.back"/>
                </div>
            </div>
        </div>

<#--        <#include "metadata_section_select.ftl"/>-->

        <div class="container-fluid bg-body">
            <div class="container bd-layout">

                <main class="bd-main bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "metadata_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">

                        <div class="my-md-3 p-3">
                            <div class="row g-3">
                                <div class="col-12">
                                    <@input name="metadata.title" help="i18n" i18nkey="datapackagemetadata.title" />
                                </div>

                                <div class="col-12">
                                    <@text name="metadata.description" help="i18n" i18nkey="datapackagemetadata.description" />
                                </div>

                                <div class="col-lg-6">
                                    <#if resource.organisation??>
                                        <@select name="id" help="i18n" i18nkey="eml.publishingOrganisation" options=organisations value="${resource.organisation.key!''}" requiredField=true />
                                    <#else>
                                        <@select name="id" help="i18n" i18nkey="eml.publishingOrganisation" options=organisations requiredField=true />
                                    </#if>
                                </div>

                                <div class="col-lg-6">
                                    <@input name="metadata.homepage" help="i18n" i18nkey="datapackagemetadata.homepage" type="url" />
                                </div>
                            </div>
                        </div>

                        <div class="my-md-3 p-3">
                            <#assign removeContributorLink><@s.text name='manage.metadata.removethis'/> <@s.text name='datapackagemetadata.contributor'/></#assign>
                            <#assign addContributorLink><@s.text name='manage.metadata.addnew'/> <@s.text name='datapackagemetadata.contributor'/></#assign>

                            <!-- List of Contributors -->
                            <div>
                                <@textinline name="datapackagemetadata.contributors" help="i18n" requiredField=true />
                                <div id="contributor-items">
                                    <#list metadata.contributors as item>
                                        <div id="contributor-item-${item_index}" class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                            <div class="columnLinks mt-2 d-flex justify-content-end">
                                                <a id="contributor-removeLink-${item_index}" href="" class="removeContributorLink text-smaller">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                        </svg>
                                                    </span>
                                                    <span>${removeContributorLink?lower_case?cap_first}</span>
                                                </a>
                                            </div>
                                            <div>
                                                <@input name="metadata.contributors[${item_index}].title" help="i18n" i18nkey="datapackagemetadata.contributor.title" requiredField=true />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.contributors[${item_index}].path" help="i18n" i18nkey="datapackagemetadata.contributor.path" />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.contributors[${item_index}].email" help="i18n" i18nkey="datapackagemetadata.contributor.email" />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.contributors[${item_index}].role" help="i18n" i18nkey="datapackagemetadata.contributor.role" />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.contributors[${item_index}].organization" help="i18n" i18nkey="datapackagemetadata.contributor.organization" />
                                            </div>
                                        </div>
                                    </#list>
                                </div>
                                <div class="addNew col-12 mt-2">
                                    <a id="plus-contributor" class="text-smaller" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span>${addContributorLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                            </div>
                        </div>

                        <div class="my-md-3 p-3">
                            <#assign removeSourceLink><@s.text name='manage.metadata.removethis'/> <@s.text name='datapackagemetadata.source'/></#assign>
                            <#assign addSourceLink><@s.text name='manage.metadata.addnew'/> <@s.text name='datapackagemetadata.source'/></#assign>

                            <!-- List of Sources -->
                            <div>
                                <@textinline name="datapackagemetadata.sources" help="i18n" requiredField=true />
                                <div id="source-items">
                                    <#list metadata.sources as item>
                                        <div id="source-item-${item_index}" class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                            <div class="columnLinks mt-2 d-flex justify-content-end">
                                                <a id="source-removeLink-${item_index}" href="" class="removeSourceLink text-smaller">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                        </svg>
                                                    </span>
                                                    <span>${removeSourceLink?lower_case?cap_first}</span>
                                                </a>
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.sources[${item_index}].title" help="i18n" i18nkey="datapackagemetadata.source.title" requiredField=true />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.sources[${item_index}].path" help="i18n" i18nkey="datapackagemetadata.source.path" />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.sources[${item_index}].email" help="i18n" i18nkey="datapackagemetadata.source.email" />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.sources[${item_index}].version" help="i18n" i18nkey="datapackagemetadata.source.version" />
                                            </div>
                                        </div>
                                    </#list>
                                </div>
                                <div class="addNew col-12 mt-2">
                                    <a id="plus-source" class="text-smaller" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span>${addSourceLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                            </div>
                        </div>

                        <div class="my-md-3 p-3">
                            <#assign removeLicenseLink><@s.text name='manage.metadata.removethis'/> <@s.text name='datapackagemetadata.license'/></#assign>
                            <#assign addLicenseLink><@s.text name='manage.metadata.addnew'/> <@s.text name='datapackagemetadata.license'/></#assign>

                            <!-- List of Licenses -->
                            <div>
                                <@textinline name="datapackagemetadata.licenses" help="i18n" requiredField=true />
                                <div id="license-items">
                                    <#list metadata.licenses as item>
                                        <div id="license-item-${item_index}" class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                            <div class="columnLinks mt-2 d-flex justify-content-end">
                                                <a id="license-removeLink-${item_index}" href="" class="removeLicenseLink text-smaller">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                        </svg>
                                                    </span>
                                                    <span>${removeSourceLink?lower_case?cap_first}</span>
                                                </a>
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.licenses[${item_index}].title" help="i18n" i18nkey="datapackagemetadata.license.title" />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.licenses[${item_index}].path" help="i18n" i18nkey="datapackagemetadata.license.path" requiredField=true />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.licenses[${item_index}].name" help="i18n" i18nkey="datapackagemetadata.license.name" requiredField=true />
                                            </div>
                                            <div class="col-lg-6">
                                                <#if (metadata.licenses[item_index].scope)??>
                                                    <@select name="metadata.licenses[${item_index}].scope" help="i18n" includeEmpty=true compareValues=true options=licenseScopes i18nkey="datapackagemetadata.license.scope" value="${metadata.licenses[item_index].scope!}" requiredField=true/>
                                                <#else>
                                                    <@select name="metadata.licenses[${item_index}].scope" help="i18n" includeEmpty=true compareValues=true options=licenseScopes i18nkey="datapackagemetadata.license.scope" value="" requiredField=true/>
                                                </#if>
                                            </div>
                                        </div>
                                    </#list>
                                </div>
                                <div class="addNew col-12 mt-2">
                                    <a id="plus-license" class="text-smaller" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span>${addLicenseLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </div>
    </form>

    <div id="baseItem-contributor" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display: none;">
        <div class="columnLinks mt-2 d-flex justify-content-end">
            <a id="contributor-removeLink" href="" class="removeContributorLink text-smaller">
                <span>
                    <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                    </svg>
                </span>
                <span>${removeContributorLink?lower_case?cap_first}</span>
            </a>
        </div>
        <div>
            <@input name="metadata.contributors.title" help="i18n" i18nkey="datapackagemetadata.contributor.title" requiredField=true/>
        </div>
        <div class="col-lg-6">
            <@input name="metadata.contributors.path" help="i18n" i18nkey="datapackagemetadata.contributor.path" />
        </div>
        <div class="col-lg-6">
            <@input name="metadata.contributors.email" help="i18n" i18nkey="datapackagemetadata.contributor.email" />
        </div>
        <div class="col-lg-6">
            <@input name="metadata.contributors.role" help="i18n" i18nkey="datapackagemetadata.contributor.role" />
        </div>
        <div class="col-lg-6">
            <@input name="metadata.contributors.organization" help="i18n" i18nkey="datapackagemetadata.contributor.organization" />
        </div>
    </div>

    <div id="baseItem-source" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display: none;">
        <div class="columnLinks mt-2 d-flex justify-content-end">
            <a id="source-removeLink" href="" class="removeSourceLink text-smaller">
                <span>
                    <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                    </svg>
                </span>
                <span>${removeSourceLink?lower_case?cap_first}</span>
            </a>
        </div>
        <div class="col-lg-6">
            <@input name="metadata.sources.title" help="i18n" i18nkey="datapackagemetadata.source.title" requiredField=true />
        </div>
        <div class="col-lg-6">
            <@input name="metadata.sources.path" help="i18n" i18nkey="datapackagemetadata.source.path" />
        </div>
        <div class="col-lg-6">
            <@input name="metadata.sources.email" help="i18n" i18nkey="datapackagemetadata.source.email" />
        </div>
        <div class="col-lg-6">
            <@input name="metadata.sources.version" help="i18n" i18nkey="datapackagemetadata.source.version" />
        </div>
    </div>

    <div id="baseItem-license" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display: none;">
        <div class="columnLinks mt-2 d-flex justify-content-end">
            <a id="license-removeLink" href="" class="removeLicenseLink text-smaller">
                <span>
                    <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                    </svg>
                </span>
                <span>${removeSourceLink?lower_case?cap_first}</span>
            </a>
        </div>
        <div class="col-lg-6">
            <@input name="metadata.licenses.title" help="i18n" i18nkey="datapackagemetadata.license.title" />
        </div>
        <div class="col-lg-6">
            <@input name="metadata.licenses.path" help="i18n" i18nkey="datapackagemetadata.license.path" requiredField=true />
        </div>
        <div class="col-lg-6">
            <@input name="metadata.licenses.name" help="i18n" i18nkey="datapackagemetadata.license.name" requiredField=true />
        </div>
        <div class="col-lg-6">
            <@select name="metadata.licenses.scope" help="i18n" includeEmpty=true options=licenseScopes i18nkey="datapackagemetadata.license.scope" value="" requiredField=true/>
        </div>
    </div>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
