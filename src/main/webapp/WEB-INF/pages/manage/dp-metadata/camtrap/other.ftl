<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.datapackagemetadata.camtrap.other.title'/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>
    <script>
        $(document).ready(function(){
            var relatedIdentifierItems = calcNumberOfItems("relatedIdentifier");
            var referenceItems = calcNumberOfItems("reference");

            function calcNumberOfItems(name) {
                var lastItem = $("#" + name + "-items .item:last-child").attr("id");
                if (lastItem !== undefined)
                    return parseInt(lastItem.split("-")[2]);
                else
                    return -1;
            }

            $("#plus-relatedIdentifier").click(function (e) {
                e.preventDefault();
                addNewRelatedIdentifierItem(true);
            });

            function addNewRelatedIdentifierItem(effects) {
                var newItem = $('#baseItem-relatedIdentifier').clone();
                if (effects) newItem.hide();
                newItem.appendTo('#relatedIdentifier-items');

                if (effects) {
                    newItem.slideDown('slow');
                }

                setRelatedIdentifierItemIndex(newItem, ++relatedIdentifierItems);

                initInfoPopovers(newItem[0]);
            }

            function addNewReferenceItem(effects) {
                var newItem = $('#baseItem-reference').clone();
                if (effects) newItem.hide();
                newItem.appendTo('#reference-items');

                if (effects) {
                    newItem.slideDown('slow');
                }

                setReferenceItemIndex(newItem, ++referenceItems);
            }

            function removeRelatedIdentifierItem(e) {
                e.preventDefault();
                var $target = $(e.target);
                if (!$target.is('a')) {
                    $target = $(e.target).closest('a');
                }
                $('#relatedIdentifier-item-' + $target.attr("id").split("-")[2]).slideUp('slow', function () {
                    $(this).remove();
                    $("#relatedIdentifier-items .item").each(function (index) {
                        setRelatedIdentifierItemIndex($(this), index);
                    });
                    calcNumberOfItems("relatedIdentifier");
                });
            }

            function removeReferenceItem(e) {
                e.preventDefault();
                var $target = $(e.target);
                if (!$target.is('a')) {
                    $target = $(e.target).closest('a');
                }
                $('#reference-item-' + $target.attr("id").split("-")[2]).slideUp('slow', function () {
                    $(this).remove();
                    $("#reference-items .item").each(function (index) {
                        setReferenceItemIndex($(this), index);
                    });
                    calcNumberOfItems("reference");
                });
            }

            function setRelatedIdentifierItemIndex(item, index) {
                item.attr("id", "relatedIdentifier-item-" + index);

                $("#relatedIdentifier-item-" + index + " [id^='relatedIdentifier-removeLink']").attr("id", "relatedIdentifier-removeLink-" + index);
                $("#relatedIdentifier-removeLink-" + index).click(function (event) {
                    removeRelatedIdentifierItem(event);
                });

                $("#relatedIdentifier-item-" + index + " [id$='relationType']").attr("id", "metadata.relatedIdentifiers[" + index + "].relationType").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#relatedIdentifier-item-" + index + " [for$='relationType']").attr("for", "metadata.relatedIdentifiers[" + index + "].relationType");
                $("#relatedIdentifier-item-" + index + " [id$='relationType']").select2({
                    placeholder: '${action.getText("datapackagemetadata.other.relationType.select")?js_string}',
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

                $("#relatedIdentifier-item-" + index + " [id$='relatedIdentifier']").attr("id", "metadata.relatedIdentifiers[" + index + "].relatedIdentifier").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#relatedIdentifier-item-" + index + " [for$='relatedIdentifier']").attr("for", "metadata.relatedIdentifiers[" + index + "].relatedIdentifier");

                $("#relatedIdentifier-item-" + index + " [id$='resourceTypeGeneral']").attr("id", "metadata.relatedIdentifiers[" + index + "].resourceTypeGeneral").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#relatedIdentifier-item-" + index + " [for$='resourceTypeGeneral']").attr("for", "metadata.relatedIdentifiers[" + index + "].resourceTypeGeneral");
                $("#relatedIdentifier-item-" + index + " [id$='resourceTypeGeneral']").select2({
                    placeholder: '${action.getText("datapackagemetadata.other.resourceTypeGeneral.select")?js_string}',
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

                $("#relatedIdentifier-item-" + index + " [id$='relatedIdentifierType']").attr("id", "metadata.relatedIdentifiers[" + index + "].relatedIdentifierType").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#relatedIdentifier-item-" + index + " [for$='relatedIdentifierType']").attr("for", "metadata.relatedIdentifiers[" + index + "].relatedIdentifierType");
                $("#relatedIdentifier-item-" + index + " [id$='relatedIdentifierType']").select2({
                    placeholder: '${action.getText("datapackagemetadata.other.relatedIdentifierType.select")?js_string}',
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
            }

            function setReferenceItemIndex(item, index) {
                item.attr("id", "reference-item-" + index);

                $("#reference-item-" + index + " [id^='reference-removeLink']").attr("id", "reference-removeLink-" + index);
                $("#reference-removeLink-" + index).click(function (event) {
                    removeReferenceItem(event);
                });

                $("#reference-item-" + index + " input").attr("id", "metadata.references[" + index + "]").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#reference-item-" + index + " label").attr("for", "metadata.references[" + index + "]");
            }

            $(".removeRelatedIdentifierLink").click(function (e) {
                removeRelatedIdentifierItem(e);
            });

            $(".removeReferenceLink").click(function (e) {
                removeReferenceItem(e);
            });

            $("#plus-reference").click(function (e) {
                e.preventDefault();
                addNewReferenceItem(true);
            });

            // scroll to the error if present
            var invalidElements = $(".is-invalid");

            if (invalidElements !== undefined && invalidElements.length > 0) {
                var invalidElement = invalidElements.first();
                var pos = invalidElement.offset().top - 100;
                // scroll to the element
                $('body, html').animate({scrollTop: pos});
            }

            $('[id^="metadata.relatedIdentifiers["][id$=".relationType"]').select2({
                placeholder: '${action.getText("datapackagemetadata.other.relationType.select")?js_string}',
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
            $('[id^="metadata.relatedIdentifiers["][id$=".resourceTypeGeneral"]').select2({
                placeholder: '${action.getText("datapackagemetadata.other.resourceTypeGeneral.select")?js_string}',
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
            $('[id^="metadata.relatedIdentifiers["][id$=".relatedIdentifierType"]').select2({
                placeholder: '${action.getText("datapackagemetadata.other.relatedIdentifierType.select")?js_string}',
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

            makeSureResourceParameterIsPresentInURL('${resource.shortname}');
        });
    </script>
    <#assign currentMenu="manage"/>
    <#assign currentMetadataPage = "other"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form class="needs-validation" action="camtrap-metadata-${section}.do" method="post" novalidate>
        <input type="hidden" name="r" value="${resource.shortname}" />

        <div class="container-fluid bg-body border-bottom">
            <div class="container bg-body border rounded-2 mb-4">
                <div class="container my-3 p-3">
                    <div class="text-center fs-smaller">
                        <div class="text-center fs-smaller">
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
                            <@s.text name='manage.datapackagemetadata.camtrap.other.title'/>
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
                            <#assign removeRelatedIdentifierLink><@s.text name='manage.metadata.removethis'/> <@s.text name='datapackagemetadata.other.relatedIdentifier'/></#assign>
                            <#assign addRelatedIdentifierLink><@s.text name='manage.metadata.addnew'/> <@s.text name='datapackagemetadata.other.relatedIdentifier'/></#assign>

                            <!-- List of Related identifiers -->
                            <div>
                                <@textinline name="datapackagemetadata.other.relatedIdentifiers" help="i18n"/>
                                <div id="relatedIdentifier-items">
                                    <#list (metadata.relatedIdentifiers)! as item>
                                        <div id="relatedIdentifier-item-${item_index}" class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                            <div class="columnLinks mt-2 d-flex justify-content-end">
                                                <a id="relatedIdentifier-removeLink-${item_index}" href="" class="removeRelatedIdentifierLink metadata-action-link custom-link">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                        </svg>
                                                    </span>
                                                    <span>${removeRelatedIdentifierLink?lower_case?cap_first}</span>
                                                </a>
                                            </div>
                                            <div class="col-lg-6">
                                                <@select name="metadata.relatedIdentifiers[${item_index}].relationType" help="i18n" includeEmpty=true compareValues=true options=relationTypes! i18nkey="datapackagemetadata.other.relationType" value="${(metadata.relatedIdentifiers[item_index].relationType)!}" requiredField=true />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.relatedIdentifiers[${item_index}].relatedIdentifier" help="i18n" i18nkey="datapackagemetadata.other.relatedIdentifier" requiredField=true />
                                            </div>
                                            <div class="col-lg-6">
                                                <@select name="metadata.relatedIdentifiers[${item_index}].resourceTypeGeneral" help="i18n" includeEmpty=true compareValues=true options=resourceTypeGenerals! i18nkey="datapackagemetadata.other.resourceTypeGeneral" value="${(metadata.relatedIdentifiers[item_index].resourceTypeGeneral)!}" />
                                            </div>
                                            <div class="col-lg-6">
                                                <@select name="metadata.relatedIdentifiers[${item_index}].relatedIdentifierType" help="i18n" includeEmpty=true compareValues=true options=relatedIdentifierTypes! i18nkey="datapackagemetadata.other.relatedIdentifierType" value="${(metadata.relatedIdentifiers[item_index].relatedIdentifierType)!}" requiredField=true />
                                            </div>
                                        </div>
                                    </#list>
                                </div>
                                <div class="addNew col-12 mt-2">
                                    <a id="plus-relatedIdentifier" class="metadata-action-link custom-link" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span>${addRelatedIdentifierLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                            </div>
                        </div>

                        <div class="my-md-3 p-3">
                            <#assign removeReferenceLink><@s.text name='manage.metadata.removethis'/> <@s.text name='datapackagemetadata.other.reference'/></#assign>
                            <#assign addReferenceLink><@s.text name='manage.metadata.addnew'/> <@s.text name='datapackagemetadata.other.reference'/></#assign>

                            <!-- List of References -->
                            <div>
                                <@textinline name="datapackagemetadata.other.references" help="i18n"/>
                                <div id="reference-items">
                                    <#list (metadata.references)! as item>
                                        <div id="reference-item-${item_index}" class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                            <div class="col-12 d-flex">
                                                <div class="flex-grow-1">
                                                    <@input name="metadata.references[${item_index}]" i18nkey="datapackagemetadata.other.reference" withLabel=false />
                                                </div>
                                                <div>
                                                    <a id="reference-removeLink-${item_index}" href="" class="removeReferenceLink metadata-action-link custom-link">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-neutral">
                                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                            </svg>
                                                        </span>
                                                    </a>
                                                </div>
                                            </div>
                                        </div>
                                    </#list>
                                </div>
                                <div class="addNew col-12 mt-2">
                                    <a id="plus-reference" class="metadata-action-link custom-link" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span>${addReferenceLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </div>
    </form>

    <div id="baseItem-relatedIdentifier" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display: none;">
        <div class="columnLinks mt-2 d-flex justify-content-end">
            <a id="relatedIdentifier-removeLink" href="" class="removeRelatedIdentifierLink metadata-action-link custom-link">
                <span>
                    <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                    </svg>
                </span>
                <span>${removeRelatedIdentifierLink?lower_case?cap_first}</span>
            </a>
        </div>
        <div class="col-lg-6">
            <@select name="metadata.relatedIdentifiers.relationType" help="i18n" includeEmpty=true compareValues=true options=relationTypes! i18nkey="datapackagemetadata.other.relationType" value="" requiredField=true />
        </div>
        <div class="col-lg-6">
            <@input name="metadata.relatedIdentifiers.relatedIdentifier" help="i18n" i18nkey="datapackagemetadata.other.relatedIdentifier" requiredField=true />
        </div>
        <div class="col-lg-6">
            <@select name="metadata.relatedIdentifiers.resourceTypeGeneral" help="i18n" includeEmpty=true compareValues=true options=resourceTypeGenerals! i18nkey="datapackagemetadata.other.resourceTypeGeneral" value="" />
        </div>
        <div class="col-lg-6">
            <@select name="metadata.relatedIdentifiers.relatedIdentifierType" help="i18n" includeEmpty=true compareValues=true options=relatedIdentifierTypes! i18nkey="datapackagemetadata.other.relatedIdentifierType" value="" requiredField=true />
        </div>
    </div>

    <div id="baseItem-reference" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display: none;">
        <div class="col-12 d-flex">
            <div class="flex-grow-1">
                <@input name="metadata.references" i18nkey="datapackagemetadata.other.reference" value="" withLabel=false />
            </div>
            <div>
                <a id="reference-removeLink" href="" class="removeReferenceLink metadata-action-link custom-link">
                    <span>
                        <svg viewBox="0 0 24 24" class="link-icon link-icon-neutral">
                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                        </svg>
                    </span>
                </a>
            </div>
        </div>
    </div>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
