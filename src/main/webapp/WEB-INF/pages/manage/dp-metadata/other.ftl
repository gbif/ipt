<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.metadata.basic.title'/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <script>
        $(document).ready(function(){
            var relatedIdentifierItems = calcNumberOfItems("relatedIdentifier");

            function calcNumberOfItems(name) {
                var lastItem = $("#" + name + "-items .item:last-child").attr("id");
                if (lastItem !== undefined)
                    return parseInt(lastItem.split("-")[2]);
                else
                    return -1;
            }

            $("#plus-relatedIdentifier").click(function (event) {
                event.preventDefault();
                console.log("plus")
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

            function removeRelatedIdentifierItem(event) {
                event.preventDefault();
                var $target = $(event.target);
                if (!$target.is('a')) {
                    $target = $(event.target).closest('a');
                }
                $('#relatedIdentifier-item-' + $target.attr("id").split("-")[2]).slideUp('slow', function () {
                    $(this).remove();
                    $("#relatedIdentifier-items .item").each(function (index) {
                        setRelatedIdentifierItemIndex($(this), index);
                    });
                    calcNumberOfItems("relatedIdentifier");
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

                $("#relatedIdentifier-item-" + index + " [id$='relatedIdentifier']").attr("id", "metadata.relatedIdentifiers[" + index + "].relatedIdentifier").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#relatedIdentifier-item-" + index + " [for$='relatedIdentifier']").attr("for", "metadata.relatedIdentifiers[" + index + "].relatedIdentifier");

                $("#relatedIdentifier-item-" + index + " [id$='resourceTypeGeneral']").attr("id", "metadata.relatedIdentifiers[" + index + "].resourceTypeGeneral").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#relatedIdentifier-item-" + index + " [for$='resourceTypeGeneral']").attr("for", "metadata.relatedIdentifiers[" + index + "].resourceTypeGeneral");

                $("#relatedIdentifier-item-" + index + " [id$='relatedIdentifierType']").attr("id", "metadata.relatedIdentifiers[" + index + "].relatedIdentifierType").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#relatedIdentifier-item-" + index + " [for$='relatedIdentifierType']").attr("for", "metadata.relatedIdentifiers[" + index + "].relatedIdentifierType");
            }

            $(".removeRelatedIdentifierLink").click(function (event) {
                removeRelatedIdentifierItem(event);
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
    <#assign currentMetadataPage = "other"/>
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
                        <@s.text name='manage.datapackagemetadata.other.title'/>
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
                                    <@text name="metadata.bibliographicCitation" help="i18n" i18nkey="datapackagemetadata.other.bibliographicCitation" />
                                </div>
                            </div>
                        </div>

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
                                                <a id="relatedIdentifier-removeLink-${item_index}" href="" class="removeRelatedIdentifierLink text-smaller">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
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
                                    <a id="plus-relatedIdentifier" class="text-smaller" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span>${addRelatedIdentifierLink?lower_case?cap_first}</span>
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
            <a id="relatedIdentifier-removeLink" href="" class="removeRelatedIdentifierLink text-smaller">
                <span>
                    <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
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

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
