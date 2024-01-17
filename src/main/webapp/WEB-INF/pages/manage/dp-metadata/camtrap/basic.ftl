<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.DataPackageMetadataAction" -->
<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.datapackagemetadata.camtrap.basic.title'/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
    <script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>
    <script>
        $(document).ready(function () {
            var nextContributorIndex;
            getNextContributorIndex();
            var sourcesItems = calcNumberOfItems("source");

            function calcNumberOfItems(name) {
                var lastItem = $("#" + name + "-items .item:last-child").attr("id");
                if (lastItem !== undefined)
                    return parseInt(lastItem.split("-")[2]);
                else
                    return -1;
            }

            function getNextContributorIndex() {
                var lastItem = $("#contributor-items .item:last-child").attr("id");
                if (lastItem !== undefined) {
                    nextContributorIndex = parseInt(lastItem.split("-")[2]);

                    if (nextContributorIndex || nextContributorIndex === 0) {
                        nextContributorIndex = nextContributorIndex + 1;
                    } else {
                        nextContributorIndex = 0;
                    }
                } else {
                    nextContributorIndex = 0;
                }
            }

            $("#plus-contributor").click(function (event) {
                event.preventDefault();
                addNewContributorItem(true);
            });

            $("#plus-source").click(function (event) {
                event.preventDefault();
                addNewSourceItem(true);
            });

            function addNewContributorItem(effects) {
                var newItem = $('#baseItem-contributor').clone();
                if (effects) newItem.hide();
                newItem.appendTo('#contributor-items');

                if (effects) {
                    newItem.slideDown('slow');
                }

                setContributorItemIndex(newItem, nextContributorIndex);
                getNextContributorIndex();

                initInfoPopovers(newItem[0]);
            }

            function addNewSourceItem(effects) {
                var newItem = $('#baseItem-source').clone();
                if (effects) newItem.hide();
                newItem.appendTo('#source-items');

                if (effects) {
                    newItem.slideDown('slow');
                }

                setSourceItemIndex(newItem, ++sourcesItems);

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

            function setContributorItemIndex(item, index) {
                item.attr("id", "contributor-item-" + index);

                $("#contributor-item-" + index + " [id^='contributor-removeLink']").attr("id", "contributor-removeLink-" + index);
                $("#contributor-removeLink-" + index).click(function (event) {
                    removeContributorItem(event);
                });

                $("#contributor-item-" + index + " [id$='titleDisplay']").attr("id", "contributor[" + index + "].titleDisplay").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#contributor-item-" + index + " [for$='titleDisplay']").attr("for", "contributor[" + index + "].titleDisplay");
                $("#contributor-item-" + index + " [id$='title']").attr("id", "metadata.contributors[" + index + "].title").attr("name", function () {
                    return $(this).attr("id");
                });

                $("#contributor-item-" + index + " [id$='firstName']").attr("id", "metadata.contributors[" + index + "].firstName").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#contributor-item-" + index + " [for$='firstName']").attr("for", "metadata.contributors[" + index + "].firstName");

                $("#contributor-item-" + index + " [id$='lastName']").attr("id", "metadata.contributors[" + index + "].lastName").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#contributor-item-" + index + " [for$='lastName']").attr("for", "metadata.contributors[" + index + "].lastName");

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
                $("#contributor-item-" + index + " [id$='role']").select2({
                    placeholder: '${action.getText("datapackagemetadata.contributor.role.select")?js_string}',
                    language: {
                        noResults: function () {
                            return '${selectNoResultsFound}';
                        }
                    },
                    minimumResultsForSearch: 15,
                    width: "100%",
                    allowClear: true,
                    theme: 'bootstrap4'
                });

                $("#contributor-item-" + index + " [id$='organization']").attr("id", "metadata.contributors[" + index + "].organization").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#contributor-item-" + index + " [for$='organization']").attr("for", "metadata.contributors[" + index + "].organization");

                // disable title, show last name and first name
                $("#metadata\\.contributors\\[" + index + "\\]\\.role").on("change", function () {
                    var selectedRole = $(this).val();

                    var $title = $("#metadata\\.contributors\\[" + index + "\\]\\.title");
                    var $displayedTitle = $("#contributor\\[" + index + "\\]\\.titleDisplay");
                    var $firstName = $("#metadata\\.contributors\\[" + index + "\\]\\.firstName");
                    var $lastName= $("#metadata\\.contributors\\[" + index + "\\]\\.lastName");

                    if (selectedRole === 'CONTACT' || selectedRole === 'PRINCIPAL_INVESTIGATOR' || selectedRole === 'CONTRIBUTOR') {
                        // title should be disabled
                        $displayedTitle.prop("readonly", true);
                        $displayedTitle.val($firstName.val() + " " + $lastName.val());
                        $title.val($firstName.val() + " " + $lastName.val());

                        // first name input and label should be displayed
                        $firstName.show();
                        $('label[for="metadata\\.contributors\\[' + index + '\\]\\.firstName"]').show();

                        // last name input and label should be displayed
                        $lastName.show();
                        $('label[for="metadata\\.contributors\\[' + index + '\\]\\.lastName"]').show();


                        $firstName.on('input', function() {
                            $title.val($(this).val() + " " + $lastName.val());
                        });

                        $lastName.on('input', function() {
                            $title.val($firstName.val() + " " + $(this).val());
                        });

                    } else if (selectedRole === 'PUBLISHER' || selectedRole === 'RIGHTS_HOLDER') {
                        $displayedTitle.prop("readonly", false);
                        $title.val($displayedTitle.val());
                        $displayedTitle.on('input', function() {
                            $title.val($displayedTitle.val());
                        });

                        $firstName.hide();
                        $('label[for="metadata\\.contributors\\[' + index + '\\]\\.firstName"]').hide();

                        $lastName.hide();
                        $('label[for="metadata\\.contributors\\[' + index + '\\]\\.lastName"]').hide();
                    }
                });
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

            $(".removeContributorLink").click(function (event) {
                removeContributorItem(event);
            });

            $(".removeSourceLink").click(function (event) {
                removeSourceItem(event);
            });

            // scroll to the error if present
            var invalidElements = $(".is-invalid");

            if (invalidElements !== undefined && invalidElements.length > 0) {
                var invalidElement = invalidElements.first();
                var pos = invalidElement.offset().top - 100;
                // scroll to the element
                $('body, html').animate({scrollTop: pos});
            }

            $('input[id^="metadata\\.contributors"][id$="\\.firstName"]').on('input', function() {
                var index = $(this)[0].id.match(/\d+/)[0];
                var $title = $("#metadata\\.contributors\\[" + index + "\\]\\.title");
                var lastName = $("#metadata\\.contributors\\[" + index + "\\]\\.lastName").val();

                if (lastName) {
                    $title.val($(this).val() + " " + lastName);
                } else {
                    $title.val($(this).val());
                }
            });

            $('input[id^="metadata\\.contributors"][id$="\\.lastName"]').on('input', function() {
                var index = $(this)[0].id.match(/\d+/)[0];
                var $title = $("#metadata\\.contributors\\[" + index + "\\]\\.title");
                var firstName = $("#metadata\\.contributors\\[" + index + "\\]\\.firstName").val();

                if (firstName) {
                    $title.val(firstName + " " + $(this).val());
                } else {
                    $title.val($(this).val());
                }
            });

            // go through the existing contributors, hide first/last name fields if needed
            $('select[id^="metadata\\.contributors"][id$="\\.role"]').each(function () {
                var id = $(this)[0].id;
                var idNumbersMatch = id.match(/\d+/);

                if (idNumbersMatch) {
                    var index = idNumbersMatch[0];

                    var role = $(this).val();

                    var $title = $("#metadata\\.contributors\\[" + index + "\\]\\.title");
                    var $displayedTitle = $("#contributor\\[" + index + "\\]\\.titleDisplay");
                    var $firstName = $("#metadata\\.contributors\\[" + index + "\\]\\.firstName");
                    var $lastName= $("#metadata\\.contributors\\[" + index + "\\]\\.lastName");

                    if (role === 'CONTACT' || role === 'PRINCIPAL_INVESTIGATOR' || role === 'CONTRIBUTOR') {
                        // first name input and label should be displayed
                        $firstName.show();
                        $('label[for="metadata\\.contributors\\[' + index + '\\]\\.firstName"]').show();

                        // last name input and label should be displayed
                        $lastName.show();
                        $('label[for="metadata\\.contributors\\[' + index + '\\]\\.lastName"]').show();
                    } else {
                        $displayedTitle.prop("disabled", false);
                        $firstName.hide();
                        $('label[for="metadata\\.contributors\\[' + index + '\\]\\.firstName"]').hide();

                        $lastName.hide();
                        $('label[for="metadata\\.contributors\\[' + index + '\\]\\.lastName"]').hide();
                    }
                }
            });

            // existing contributors - hide/display first/last name fields when role changes
            $('select[id^="metadata\\.contributors"][id$="\\.role"]').on("change", function () {
                var selectedRole = $(this).val();
                var index = $(this)[0].id.match(/\d+/)[0];

                var $title = $("#metadata\\.contributors\\[" + index + "\\]\\.title");
                var $firstName = $("#metadata\\.contributors\\[" + index + "\\]\\.firstName");
                var $lastName= $("#metadata\\.contributors\\[" + index + "\\]\\.lastName");

                if (selectedRole === 'CONTACT' || selectedRole === 'PRINCIPAL_INVESTIGATOR' || selectedRole === 'CONTRIBUTOR') {
                    // title should be disabled (read-only)
                    $("#contributor\\[" + index + "\\]\\.titleDisplay").prop("readonly", true);

                    // first name input and label should be displayed
                    $firstName.show();
                    $('label[for="metadata\\.contributors\\[' + index + '\\]\\.firstName"]').show();

                    // last name input and label should be displayed
                    $lastName.show();
                    $('label[for="metadata\\.contributors\\[' + index + '\\]\\.lastName"]').show();


                    $firstName.on('input', function() {
                        $title.val($(this).val() + " " + $lastName.val());
                    });

                    $lastName.on('input', function() {
                        $title.val($firstName.val() + " " + $(this).val());
                    });

                } else if (selectedRole === 'PUBLISHER' || selectedRole === 'RIGHTS_HOLDER') {
                    $("#contributor\\[" + index + "\\]\\.titleDisplay").prop("readonly", false);

                    $firstName.hide();
                    $firstName.val('');
                    $('label[for="metadata\\.contributors\\[' + index + '\\]\\.firstName"]').hide();

                    $lastName.hide();
                    $lastName.val('');
                    $('label[for="metadata\\.contributors\\[' + index + '\\]\\.lastName"]').hide();
                }
            });



            $('#metadata\\.licenses\\[0\\]\\.name').select2({
                placeholder: '${action.getText("datapackagemetadata.license.select")?js_string}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                minimumResultsForSearch: 'Infinity',
                width: "100%",
                allowClear: true,
                theme: 'bootstrap4'
            });
            $('#metadata\\.licenses\\[1\\]\\.name').select2({
                placeholder: '${action.getText("datapackagemetadata.license.select")?js_string}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                minimumResultsForSearch: 15,
                width: "100%",
                allowClear: true,
                theme: 'bootstrap4'
            });
            $('[id^="metadata.contributors["][id$=".role"]').select2({
                placeholder: '${action.getText("datapackagemetadata.contributor.role.select")?js_string}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                minimumResultsForSearch: 15,
                width: "100%",
                allowClear: true,
                theme: 'bootstrap4'
            });
        });
    </script>
    <#assign currentMenu="manage"/>
    <#assign currentMetadataPage = "basic"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form class="needs-validation" action="camtrap-metadata-${section}.do" method="post" novalidate>
        <div class="container-fluid bg-body border-bottom">
            <div class="container bg-body border rounded-2 mb-4">
                <div class="container my-3 p-3">
                    <div class="text-center fs-smaller">
                        <div class="text-center fs-smaller">
                            <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);"
                                 aria-label="breadcrumb">
                                <ol class="breadcrumb justify-content-center mb-0">
                                    <li class="breadcrumb-item">
                                        <a href="/manage/"><@s.text name="breadcrumb.manage"/></a>
                                    </li>
                                    <li class="breadcrumb-item">
                                        <a href="resource?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a>
                                    </li>
                                    <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.metadata"/></li>
                                </ol>
                            </nav>
                        </div>
                    </div>

                    <div class="text-center">
                        <h1 class="py-2 mb-0 text-gbif-header fs-2 fw-normal">
                            <@s.text name='manage.datapackagemetadata.camtrap.basic.title'/>
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
        </div>

        <#--        <#include "metadata_section_select.ftl"/>-->

        <div class="container-fluid bg-body">
            <div class="container bd-layout main-content-container">
                <main class="bd-main bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "metadata_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">

                        <div class="my-md-3 p-3">
                            <div class="row g-3">
                                <div class="col-12">
                                    <@input name="metadata.title" help="i18n" i18nkey="datapackagemetadata.title" requiredField=true />
                                </div>

                                <div class="col-12">
                                    <@text name="metadata.description" help="i18n" i18nkey="datapackagemetadata.description" requiredField=true />
                                </div>

                                <div class="col-lg-6">
                                    <@input name="metadata.homepage" help="i18n" i18nkey="datapackagemetadata.homepage" type="url" />
                                </div>

                                <div class="col-lg-6">
                                    <@input name="metadata.image" help="i18n" i18nkey="datapackagemetadata.image" type="url" />
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
                                                <a id="contributor-removeLink-${item_index}" href=""
                                                   class="metadata-action-link removeContributorLink">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                        </svg>
                                                    </span>
                                                    <span>${removeContributorLink?lower_case?cap_first}</span>
                                                </a>
                                            </div>
                                            <div class="col-12">
                                                <#if (item.role)?? && (item.role == "contributor" || item.role == "contact" || item.role == "principalInvestigator")>
                                                    <@input name="contributor[${item_index}].titleDisplay" value="${item.firstName!} ${item.lastName!}" help="i18n" i18nkey="datapackagemetadata.contributor.title" requiredField=true disabled=true />
                                                <#else>
                                                    <@input name="contributor[${item_index}].titleDisplay" value="${item.title!}" help="i18n" i18nkey="datapackagemetadata.contributor.title" requiredField=true />
                                                </#if>
                                                <input type='hidden' id='metadata.contributors[${item_index}].title' name='metadata.contributors[${item_index}].title' value='${item.title!}' />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.contributors[${item_index}].firstName" i18nkey="datapackagemetadata.contributor.firstName" requiredField=true />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.contributors[${item_index}].lastName" i18nkey="datapackagemetadata.contributor.lastName" requiredField=true />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.contributors[${item_index}].path" help="i18n" i18nkey="datapackagemetadata.contributor.path" />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.contributors[${item_index}].email" help="i18n" i18nkey="datapackagemetadata.contributor.email" />
                                            </div>
                                            <div class="col-lg-6">
                                                <#if (item.role)??>
                                                    <@select name="metadata.contributors[${item_index}].role" help="i18n" includeEmpty=true compareValues=true options=contributorRoles i18nkey="datapackagemetadata.contributor.role" value="${item.role}" />
                                                <#else>
                                                    <@select name="metadata.contributors[${item_index}].role" help="i18n" includeEmpty=true compareValues=true options=contributorRoles i18nkey="datapackagemetadata.contributor.role" value="" />
                                                </#if>
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.contributors[${item_index}].organization" help="i18n" i18nkey="datapackagemetadata.contributor.organization" />
                                            </div>
                                        </div>
                                    </#list>
                                </div>
                                <div class="addNew col-12 mt-2">
                                    <a id="plus-contributor" class="metadata-action-link" href="">
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
                            <#assign removeLicenseLink><@s.text name='manage.metadata.removethis'/> <@s.text name='datapackagemetadata.license'/></#assign>
                            <#assign addLicenseLink><@s.text name='manage.metadata.addnew'/> <@s.text name='datapackagemetadata.license'/></#assign>

                            <#assign dataLicenseItemIndex = -1 />
                            <#assign mediaLicenseItemIndex = -1 />

                            <#list metadata.licenses! as item>
                                <#if (item.scope)?has_content && item.scope == "data">
                                    <#assign dataLicenseItemIndex = item_index />
                                <#elseif (item.scope)?has_content && item.scope == "media">
                                    <#assign mediaLicenseItemIndex = item_index />
                                </#if>
                            </#list>

                            <!-- List of Licenses -->
                            <div>
                                <@textinline name="datapackagemetadata.licenses" help="i18n" requiredField=true />
                                <div id="license-items">
                                    <div class="row g-3 mt-1">
                                        <div class="col-lg-6">
                                            <#if (dataLicenseItemIndex > -1)>
                                                <input type="hidden"
                                                       name="metadata.licenses[${dataLicenseItemIndex}].scope"
                                                       value="DATA" required>
                                                <@select name="metadata.licenses[${dataLicenseItemIndex}].name" help="i18n" includeEmpty=true options=gbifSupportedLicenseNames i18nkey="datapackagemetadata.license.name.data" value="${(metadata.licenses[dataLicenseItemIndex].name)!''}" requiredField=true/>
                                            <#else>
                                                <input type="hidden" name="metadata.licenses[0].scope" value="DATA"
                                                       required>
                                                <@select name="metadata.licenses[0].name" help="i18n" includeEmpty=true options=gbifSupportedLicenseNames i18nkey="datapackagemetadata.license.name.data" value="" requiredField=true/>
                                            </#if>
                                        </div>

                                        <div class="col-lg-6">
                                            <#if (mediaLicenseItemIndex > -1)>
                                                <input type="hidden"
                                                       name="metadata.licenses[${mediaLicenseItemIndex}].scope"
                                                       value="MEDIA" required>
                                                <@select name="metadata.licenses[${mediaLicenseItemIndex}].name" help="i18n" includeEmpty=true options=openDefinitionLicenseNames i18nkey="datapackagemetadata.license.name.media" value="${(metadata.licenses[mediaLicenseItemIndex].name)!''}" requiredField=true />
                                            <#else>
                                                <input type="hidden" name="metadata.licenses[1].scope" value="MEDIA"
                                                       required>
                                                <@select name="metadata.licenses[1].name" help="i18n" includeEmpty=true options=openDefinitionLicenseNames i18nkey="datapackagemetadata.license.name.media" value="" requiredField=true />
                                            </#if>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="my-md-3 p-3">
                            <#assign removeSourceLink><@s.text name='manage.metadata.removethis'/> <@s.text name='datapackagemetadata.source'/></#assign>
                            <#assign addSourceLink><@s.text name='manage.metadata.addnew'/> <@s.text name='datapackagemetadata.source'/></#assign>

                            <!-- List of Sources -->
                            <div>
                                <@textinline name="datapackagemetadata.sources" help="i18n" />
                                <div id="source-items">
                                    <#list metadata.sources as item>
                                        <div id="source-item-${item_index}"
                                             class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                            <div class="columnLinks mt-2 d-flex justify-content-end">
                                                <a id="source-removeLink-${item_index}" href=""
                                                   class="metadata-action-link removeSourceLink">
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
                                    <a id="plus-source" class="metadata-action-link" href="">
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
                    </div>
                </main>
            </div>
        </div>
    </form>

    <div id="baseItem-contributor" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display: none;">
        <div class="columnLinks mt-2 d-flex justify-content-end">
            <a id="contributor-removeLink" href="" class="metadata-action-link removeContributorLink">
                <span>
                    <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                    </svg>
                </span>
                <span>${removeContributorLink?lower_case?cap_first}</span>
            </a>
        </div>
        <div class="col-12">
            <@input name="contributor.titleDisplay" help="i18n" i18nkey="datapackagemetadata.contributor.title" requiredField=true/>
            <input type='hidden' id='metadata.contributors.title' name='metadata.contributors.title' value='' />
        </div>
        <div class="col-lg-6">
            <@input name="metadata.contributors.firstName" i18nkey="datapackagemetadata.contributor.firstName" requiredField=true />
        </div>
        <div class="col-lg-6">
            <@input name="metadata.contributors.lastName" i18nkey="datapackagemetadata.contributor.lastName" requiredField=true />
        </div>
        <div class="col-lg-6">
            <@input name="metadata.contributors.path" help="i18n" i18nkey="datapackagemetadata.contributor.path" />
        </div>
        <div class="col-lg-6">
            <@input name="metadata.contributors.email" help="i18n" i18nkey="datapackagemetadata.contributor.email" />
        </div>
        <div class="col-lg-6">
            <@select name="metadata.contributors.role" help="i18n" includeEmpty=true compareValues=true options=contributorRoles i18nkey="datapackagemetadata.contributor.role" value="" />
        </div>
        <div class="col-lg-6">
            <@input name="metadata.contributors.organization" help="i18n" i18nkey="datapackagemetadata.contributor.organization" />
        </div>
    </div>

    <div id="baseItem-source" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display: none;">
        <div class="columnLinks mt-2 d-flex justify-content-end">
            <a id="source-removeLink" href="" class="metadata-action-link removeSourceLink">
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
            <a id="license-removeLink" href="" class="metadata-action-link removeLicenseLink">
                <span>
                    <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                    </svg>
                </span>
                <span>${removeLicenseLink?lower_case?cap_first}</span>
            </a>
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
