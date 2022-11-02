<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.DataPackageMetadataAction" -->
<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.metadata.basic.title'/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <script>
        $(document).ready(function(){
            var taxonItems = calcNumberOfItems("taxon");

            function calcNumberOfItems(name) {
                var lastItem = $("#" + name + "-items .item:last-child").attr("id");
                if (lastItem !== undefined)
                    return parseInt(lastItem.split("-")[2]);
                else
                    return -1;
            }

            $("#plus-taxon").click(function (event) {
                event.preventDefault();
                addNewTaxonItem(true);
            });

            function addNewTaxonItem(effects) {
                var newItem = $('#baseItem-taxon').clone();
                if (effects) newItem.hide();
                newItem.appendTo('#taxon-items');

                if (effects) {
                    newItem.slideDown('slow');
                }

                setTaxonItemIndex(newItem, ++taxonItems);
            }

            function removeTaxonItem(event) {
                event.preventDefault();
                var $target = $(event.target);
                if (!$target.is('a')) {
                    $target = $(event.target).closest('a');
                }
                $('#taxon-item-' + $target.attr("id").split("-")[2]).slideUp('slow', function () {
                    $(this).remove();
                    $("#taxon-items .item").each(function (index) {
                        setTaxonItemIndex($(this), index);
                    });
                    calcNumberOfItems("taxon");
                });
            }

            function setTaxonItemIndex(item, index) {
                item.attr("id", "taxon-item-" + index);

                $("#taxon-item-" + index + " [id^='taxon-removeLink']").attr("id", "taxon-removeLink-" + index);
                $("#taxon-removeLink-" + index).click(function (event) {
                    removeTaxonItem(event);
                });

                $("#taxon-item-" + index + " [id$='taxonID']").attr("id", "metadata.taxonomic[" + index + "].taxonID").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#taxon-item-" + index + " [for$='taxonID']").attr("for", "metadata.taxonomic[" + index + "].taxonID");

                $("#taxon-item-" + index + " [id$='taxonIDReference']").attr("id", "metadata.taxonomic[" + index + "].taxonIDReference").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#taxon-item-" + index + " [for$='taxonIDReference']").attr("for", "metadata.taxonomic[" + index + "].taxonIDReference");

                $("#taxon-item-" + index + " [id$='scientificName']").attr("id", "metadata.taxonomic[" + index + "].scientificName").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#taxon-item-" + index + " [for$='scientificName']").attr("for", "metadata.taxonomic[" + index + "].scientificName");

                $("#taxon-item-" + index + " [id$='taxonRank']").attr("id", "metadata.taxonomic[" + index + "].taxonRank").attr("name", function () {
                    return $(this).attr("id");
                });
                $("#taxon-item-" + index + " [for$='taxonRank']").attr("for", "metadata.taxonomic[" + index + "].taxonRank");
            }

            $(".removeTaxonLink").click(function (event) {
                removeTaxonItem(event);
            });
        });
    </script>
    <#assign currentMenu="manage"/>
    <#assign currentMetadataPage = "taxonomic"/>
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
                        <@s.text name='manage.datapackagemetadata.taxonomic.title'/>
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
                            <#assign removeTaxonLink><@s.text name='manage.metadata.removethis'/> <@s.text name='datapackagemetadata.taxon'/></#assign>
                            <#assign addTaxonLink><@s.text name='manage.metadata.addnew'/> <@s.text name='datapackagemetadata.taxon'/></#assign>

                            <!-- List of Sources -->
                            <div>
                                <div id="taxon-items">
                                    <#if (metadata.taxonomic)?has_content>
                                        <#list metadata.taxonomic as item>
                                            <div id="taxon-item-${item_index}" class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                                <div class="columnLinks mt-2 d-flex justify-content-end">
                                                    <a id="taxon-removeLink-${item_index}" href="" class="removeTaxonLink text-smaller">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${removeTaxonLink?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <div class="col-lg-6">
                                                    <@input name="metadata.taxonomic[${item_index}].taxonID" i18nkey="datapackagemetadata.taxonomic.taxonId" requiredField=true />
                                                </div>
                                                <div class="col-lg-6">
                                                    <@input name="metadata.taxonomic[${item_index}].taxonIDReference" i18nkey="datapackagemetadata.taxonomic.taxonIdReference" requiredField=true />
                                                </div>
                                                <div class="col-lg-6">
                                                    <@input name="metadata.taxonomic[${item_index}].scientificName" i18nkey="datapackagemetadata.taxonomic.scientificName" requiredField=true />
                                                </div>
                                                <div class="col-lg-6">
                                                    <#if (metadata.taxonomic[item_index].taxonRank)??>
                                                        <@select name="metadata.taxonomic[${item_index}].taxonRank" includeEmpty=true compareValues=true options=taxonRanks i18nkey="datapackagemetadata.taxonomic.taxonRank" value="${metadata.taxonomic[item_index].taxonRank!}"/>
                                                    <#else>
                                                        <@select name="metadata.taxonomic[${item_index}].taxonRank" includeEmpty=true compareValues=true options=taxonRanks i18nkey="datapackagemetadata.taxonomic.taxonRank" value=""/>
                                                    </#if>
                                                </div>
                                            </div>
                                        </#list>
                                    </#if>
                                </div>
                                <div class="addNew col-12 mt-2">
                                    <a id="plus-taxon" class="text-smaller" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span>${addTaxonLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </div>
    </form>

    <div id="baseItem-taxon" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display: none;">
        <div class="columnLinks mt-2 d-flex justify-content-end">
            <a id="taxon-removeLink" href="" class="removeTaxonLink text-smaller">
                <span>
                    <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                    </svg>
                </span>
                <span>${removeTaxonLink?lower_case?cap_first}</span>
            </a>
        </div>
        <div class="col-lg-6">
            <@input name="metadata.taxonomic.taxonID" i18nkey="datapackagemetadata.taxonomic.taxonId" requiredField=true />
        </div>
        <div class="col-lg-6">
            <@input name="metadata.taxonomic.taxonIDReference" i18nkey="datapackagemetadata.taxonomic.taxonIdReference" requiredField=true />
        </div>
        <div class="col-lg-6">
            <@input name="metadata.taxonomic.scientificName" i18nkey="datapackagemetadata.taxonomic.scientificName" requiredField=true />
        </div>
        <div class="col-lg-6">
            <@select name="metadata.taxonomic.taxonRank" includeEmpty=true options=taxonRanks i18nkey="datapackagemetadata.taxonomic.taxonRank" value=""/>
        </div>
    </div>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
