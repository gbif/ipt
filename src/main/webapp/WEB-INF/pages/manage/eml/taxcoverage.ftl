<#escape x as x?html>
    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header.ftl">
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <#include "/WEB-INF/pages/macros/popover.ftl"/>
    <script>
        $(document).ready(function(){
            $('#plus').click(function () {
                var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'))
                var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
                    return new bootstrap.Popover(popoverTriggerEl)
                })
                try {
                    var popover = new bootstrap.Popover(document.querySelector('.popover-dismiss'), {
                        trigger: 'focus'
                    })
                } catch (TypeError) {
                }
            });

            $('#metadata-section').change(function () {
                var metadataSection = $('#metadata-section').find(':selected').val()
                $(location).attr('href', 'metadata-' + metadataSection + '.do?r=${resource.shortname!r!}');
            });

            // scroll to the error if present
            var invalidElements = $(".is-invalid");

            if (invalidElements !== undefined && invalidElements.length > 0) {
                var invalidElement = invalidElements.first();
                var pos = invalidElement.offset().top - 100;
                // scroll to the element
                $('body, html').animate({scrollTop: pos});
            }

            function initAndGetSortable(selector) {
                return sortable(selector, {
                    forcePlaceholderSize: true,
                    placeholderClass: 'border',
                    handle: '.handle'
                });
            }

            const sortable_taxcoverages = initAndGetSortable('#items');
            sortable_taxcoverages[0].addEventListener('sortupdate', changeInputNamesTaxCoverageAfterDragging);
            sortable_taxcoverages[0].addEventListener('drag', dragScroll);

            $("[id^='subItems-']").each(function (index) {
                const sortable_taxa = initAndGetSortable("#" + $(this)[0].id);
                sortable_taxa[0].addEventListener('sortupdate', changeInputNamesTaxonAfterDragging);
                sortable_taxa[0].addEventListener('drag', dragScroll);
            });

            function dragScroll(e) {
                var cursor = e.pageY;
                var parentWindow = parent.window;
                var pixelsToTop = $(parentWindow).scrollTop();
                var screenHeight = $(parentWindow).height();

                if ((cursor - pixelsToTop) > screenHeight * 0.9) {
                    parentWindow.scrollBy(0, (screenHeight / 30));
                } else if ((cursor - pixelsToTop) < screenHeight * 0.1) {
                    parentWindow.scrollBy(0, -(screenHeight / 30));
                }
            }

            function changeInputNamesTaxonAfterDragging(e) {
                displayProcessing();
                var targetId = e.target.id;
                var parentIndex = targetId.split("-")[1];
                var items = $("#" + e.target.id + " div.sub-item");

                items.each(function (index) {
                    var elementId = $(this)[0].id;

                    $("div#" + elementId + " input[id$='scientificName']").attr("name", "eml.taxonomicCoverages[" + parentIndex + "].taxonKeywords[" + index + "].scientificName");
                    $("div#" + elementId + " input[id$='commonName']").attr("name", "eml.taxonomicCoverages[" + parentIndex + "].taxonKeywords[" + index + "].commonName");
                    $("div#" + elementId + " select[id$='rank']").attr("name", "eml.taxonomicCoverages[" + parentIndex + "].taxonKeywords[" + index + "].rank");
                });

                hideProcessing();
            }

            function changeInputNamesTaxCoverageAfterDragging(e) {
                displayProcessing();
                var items = $("#items div.item");

                items.each(function (index) {
                    var elementId = $(this)[0].id;

                    $("div#" + elementId + " textarea[id$='description']").attr("name", "eml.taxonomicCoverages[" + index + "].description");
                    console.log("div#" + elementId + " input[id^='add-button']");
                    console.log($("div#" + elementId + " input[id^='add-button']"));
                    $("div#" + elementId + " input[id^='add-button']").attr("name", "add-button-" + index);

                    var scientificNames = $("#" + elementId + " input[id$='scientificName']");
                    scientificNames.each(function (sub_index) {
                        $(this).attr("name", "eml.taxonomicCoverages[" + index + "].taxonKeywords[" + sub_index + "].scientificName");
                    });

                    var commonNames = $("#" + elementId + " input[id$='commonName']");
                    commonNames.each(function (sub_index) {
                        $(this).attr("name", "eml.taxonomicCoverages[" + index + "].taxonKeywords[" + sub_index + "].commonName");
                    });

                    var ranks = $("#" + elementId + " select[id$='rank']");
                    ranks.each(function (sub_index) {
                        $(this).attr("name", "eml.taxonomicCoverages[" + index + "].taxonKeywords[" + sub_index + "].rank");
                    });
                });

                hideProcessing();
            }
        });
    </script>
    <title><@s.text name='manage.metadata.taxcoverage.title'/></title>
    <#assign currentMetadataPage = "taxcoverage"/>
    <#assign currentMenu="manage"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <form class="needs-validation" action="metadata-${section}.do" method="post" novalidate>

        <div class="container-fluid bg-body border-bottom">
            <div class="container pt-2">
                <#include "/WEB-INF/pages/inc/action_alerts.ftl">

                <div id="taxcoverage-no-available-data-warning" class="alert alert-warning mt-2 alert-dismissible fade show d-flex" style="display: none !important;" role="alert">
                    <div class="me-3">
                        <i class="bi bi-exclamation-triangle alert-orange-2 fs-bigger-2 me-2"></i>
                    </div>
                    <div class="overflow-x-hidden pt-1">
                        <span><@s.text name="eml.warning.reinfer"/></span>
                    </div>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>

                <#if (inferredMetadata.inferredTaxonomicCoverage)??>
                    <#list inferredMetadata.inferredTaxonomicCoverage.errors as error>
                        <div class="alert alert-danger mt-2 alert-dismissible fade show d-flex metadata-error-alert" role="alert" style="display: none !important;">
                            <div class="me-3">
                                <i class="bi bi-exclamation-circle alert-red-2 fs-bigger-2 me-2"></i>
                            </div>
                            <div class="overflow-x-hidden pt-1">
                                <span><@s.text name="${error}"/></span>
                            </div>
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </#list>
                </#if>
            </div>

            <div class="container my-3 p-3">
                <div class="text-center text-uppercase fw-bold fs-smaller-2">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb justify-content-center mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                            <li class="breadcrumb-item"><a href="resource?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.metadata"/></li>
                        </ol>
                    </nav>
                </div>

                <div class="text-center">
                    <h1 class="py-2 mb-0 text-gbif-header fs-2 fw-normal">
                        <@s.text name='manage.metadata.taxcoverage.title'/>
                    </h1>
                </div>

                <div class="text-center fs-smaller">
                    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                </div>

                <div class="text-center mt-2">
                    <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save"/>
                    <@s.submit cssClass="button btn btn-sm btn-outline-secondary top-button" name="cancel" key="button.back"/>
                </div>
            </div>
        </div>

        <#include "metadata_section_select.ftl"/>

        <div class="container-fluid bg-body">
            <div class="container bd-layout">

                <main class="bd-main bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "eml_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">

                        <div class="my-md-3 p-3">
                            <div class="row g-2 mt-0">
                                <div class="col-md-6">
                                    <@checkbox name="inferTaxonomicCoverageAutomatically" value="${inferTaxonomicCoverageAutomatically?c}" i18nkey="eml.inferAutomatically"/>
                                </div>

                                <div id="preview-links" class="col-md-6">
                                    <div class="d-flex justify-content-end">
                                        <a id="preview-inferred-taxonomic" class="text-smaller" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                            </svg>
                                        </span>
                                            <span><@s.text name="eml.previewInferred"/></span>
                                        </a>
                                    </div>
                                    <div id="dateInferred" class="text-smaller mt-0 d-flex justify-content-end" style="display: none !important;">
                                        ${(inferredMetadata.lastModified?datetime?string.medium)!}&nbsp;
                                        <a href="metadata-taxcoverage.do?r=${resource.shortname}&amp;reinferMetadata=true">
                                        <span>
                                            <svg class="link-icon" viewBox="0 0 24 24">
                                                <path d="m19 8-4 4h3c0 3.31-2.69 6-6 6-1.01 0-1.97-.25-2.8-.7l-1.46 1.46C8.97 19.54 10.43 20 12 20c4.42 0 8-3.58 8-8h3l-4-4zM6 12c0-3.31 2.69-6 6-6 1.01 0 1.97.25 2.8.7l1.46-1.46C15.03 4.46 13.57 4 12 4c-4.42 0-8 3.58-8 8H1l4 4 4-4H6z"></path>
                                            </svg>
                                        </span>
                                            <span><@s.text name="eml.reinfer"/></span>
                                        </a>
                                    </div>
                                </div>
                            </div>

                            <p class="my-3 intro">
                                <@s.text name='manage.metadata.taxcoverage.intro'/>
                            </p>

                            <div id="items">
                                <!-- Adding the taxonomic coverages that already exists on the file -->
                                <#assign next_agent_index=0 />
                                <#list eml.taxonomicCoverages as item>
                                    <div id='item-${item_index}' data-ipt-item-index="${item_index}" class="item border-bottom">
                                        <div class="handle d-flex justify-content-end mt-2">
                                            <a id="removeLink-${item_index}" class="removeLink text-smaller" href="">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon">
                                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                    </svg>
                                                </span>
                                                <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.taxcoverage.item'/></span>
                                            </a>
                                        </div>

                                        <div class="mt-2">
                                            <@text i18nkey="eml.taxonomicCoverages.description" help="i18n" name="eml.taxonomicCoverages[${item_index}].description" />
                                        </div>

                                        <!-- Taxon list-->
                                        <div class="my-2 text-smaller">
                                            <a id="taxonsLink-${item_index}" class="show-taxonList mt-1" href="">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon">
                                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                    </svg>
                                                </span>
                                                <span>
                                                    <@s.text name="manage.metadata.taxcoverage.addSeveralTaxa"/>
                                                </span>
                                            </a>
                                            <@popoverPropertyInfo "manage.metadata.taxcoverage.addSeveralTaxa.help" />
                                        </div>

                                        <div id="list-${item_index}" class="half addSeveralTaxa mt-2" style="display:none">
                                            <@text i18nkey="eml.taxonomicCoverages.taxonList" help="i18n" name="taxon-list-${item_index}" value="" />
                                            <div id="addSeveralTaxaButtons" class="buttons mt-2">
                                                <input type="submit" value="<@s.text name='button.add'/>" id="add-button-${item_index}" name="add-button-${item_index}" class="button btn btn-outline-gbif-primary">
                                            </div>
                                        </div>
                                        <div id="subItems-${item_index}" class="mt-2">
                                            <#if (item.taxonKeywords)??>
                                                <#list item.taxonKeywords as subItem>
                                                    <div id="subItem-${item_index}-${subItem_index}" class="sub-item mt-3" data-ipt-item-index="${item_index}">
                                                        <div class="handle d-flex justify-content-end mt-2 py-1">
                                                            <a id="trash-${item_index}-${subItem_index}" class="text-smaller" href="">
                                                                <span>
                                                                    <svg viewBox="0 0 24 24" class="link-icon">
                                                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                                    </svg>
                                                                </span>
                                                                <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.taxcoverage.taxon.item'/></span>
                                                            </a>
                                                        </div>

                                                        <div class="row flex-grow-1 g-3">
                                                            <div class="col-lg-4">
                                                                <@input i18nkey="eml.taxonomicCoverages.taxonKeyword.scientificName" name="eml.taxonomicCoverages[${item_index}].taxonKeywords[${subItem_index}].scientificName" requiredField=true />
                                                            </div>

                                                            <div class="col-lg-4">
                                                                <@input i18nkey="eml.taxonomicCoverages.taxonKeyword.commonName" name="eml.taxonomicCoverages[${item_index}].taxonKeywords[${subItem_index}].commonName" />
                                                            </div>

                                                            <div class="col-lg-4">
                                                                <@select i18nkey="eml.taxonomicCoverages.taxonKeyword.rank"  name="eml.taxonomicCoverages[${item_index}].taxonKeywords[${subItem_index}].rank" options=ranks! value="${(eml.taxonomicCoverages[item_index].taxonKeywords[subItem_index].rank)!?lower_case}"/>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </#list>
                                            </#if>
                                        </div>
                                        <div class="pb-1 mt-3">
                                            <a id="plus-subItem-${item_index}" href="" class="text-smaller">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon">
                                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                    </svg>
                                                </span>
                                                <span><@s.text name='manage.metadata.addnew' /> <@s.text name='manage.metadata.taxcoverage.taxon.item' /></span>
                                            </a>
                                        </div>
                                    </div>
                                </#list>
                            </div>

                            <div class="addNew mt-2">
                                <a id="plus" class="plus plus-taxcoverage text-smaller" href="">
                                    <span>
                                        <svg viewBox="0 0 24 24" class="link-icon">
                                            <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                        </svg>
                                    </span>
                                    <span><@s.text name='manage.metadata.addnew' /> <@s.text name='manage.metadata.taxcoverage.item' /></span>
                                </a>
                            </div>

                            <!-- Static data -->
                            <div id="static-taxanomic" class="mt-4" style="display: none;">
                                <!-- Data is inferred, preview -->
                                <#if (inferredMetadata.inferredTaxonomicCoverage.organizedData.keywords)??>
                                    <div class="table-responsive">
                                        <table class="table table-sm table-borderless">
                                            <#list inferredMetadata.inferredTaxonomicCoverage.organizedData.keywords as k>
                                                <#if k.rank?has_content && ranks[k.rank?string]?has_content && (k.displayNames?size > 0) >
                                                    <tr>
                                                        <#-- 1st col, write rank name once. Avoid problem accessing "class" from map - it displays "java.util.LinkedHashMap" -->
                                                        <#if k.rank?lower_case == "class">
                                                            <th class="col-4">Class</th>
                                                        <#else>
                                                            <th class="col-4">${ranks[k.rank?html]?cap_first!}</th>
                                                        </#if>
                                                        <#-- 2nd col, write comma separated list of names in format: scientific name (common name) -->
                                                        <td>
                                                            <#list k.displayNames as name>
                                                                &nbsp;${name}<#if name_has_next>,</#if>
                                                            </#list>
                                                        </td>
                                                    </tr>
                                                </#if>
                                            </#list>
                                        </table>
                                    </div>
                                <!-- Data infer finished, but there are errors/warnings -->
                                <#elseif (inferredMetadata.inferredTaxonomicCoverage)?? && inferredMetadata.inferredTaxonomicCoverage.errors?size != 0>
                                    <#list inferredMetadata.inferredTaxonomicCoverage.errors as error>
                                        <div class="callout callout-danger text-smaller">
                                            <@s.text name="${error}"/>
                                        </div>
                                    </#list>
                                <!-- Other -->
                                <#else>
                                    <div class="callout callout-warning text-smaller">
                                        <@s.text name="eml.warning.reinfer"/>
                                    </div>
                                </#if>
                            </div>

                            <!-- internal parameter -->
                            <input name="r" type="hidden" value="${resource.shortname}" />

                            <!-- The base form that is going to be cloned every time a user click on the 'add' link -->
                            <!-- The next divs are hidden. -->
                            <div id="baseItem" class="item clearfix" style="display:none">
                                <div class="handle d-flex justify-content-end mt-2">
                                    <a id="removeLink" class="removeLink text-smaller" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                            </svg>
                                        </span>
                                        <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.taxcoverage.item'/></span>
                                    </a>
                                </div>

                                <div class="mt-2">
                                    <@text i18nkey="eml.taxonomicCoverages.description" help="i18n" name="description" />
                                </div>

                                <!-- Taxon list-->
                                <div class="addNew mt-1">
                                    <a id="taxonsLink" class="show-taxonList text-smaller" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span><@s.text name='manage.metadata.taxcoverage.addSeveralTaxa' /></span>
                                    </a>
                                </div>
                                <div id="list" class="mt-2" style="display:none">
                                    <@text i18nkey="eml.taxonomicCoverages.taxonList" help="i18n" name="taxon-list" value="" />
                                    <div class="buttons taxon-list my-2">
                                        <input type="submit" value='<@s.text name="button.add"/>' id="add-button" name="add-button" class="button btn btn-outline-gbif-primary">
                                    </div>
                                </div>
                                <div id="subItems" class="my-2"></div>
                                <div class="addNew border-bottom pb-1 mt-1">
                                    <a id="plus-subItem" href="" class="text-smaller">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span><@s.text name='manage.metadata.addnew' /> <@s.text name='manage.metadata.taxcoverage.taxon.item' /></span>
                                    </a>
                                </div>
                            </div>

                            <div id="subItem-9999" class="sub-item mt-3" style="display:none">
                                <div class="handle d-flex justify-content-end mt-2 py-1">
                                    <a id="trash" class="text-smaller" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                            </svg>
                                        </span>
                                        <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.taxcoverage.taxon.item'/></span>
                                    </a>
                                </div>

                                <div class="row g-3">
                                    <div class="col-lg-4">
                                        <@input i18nkey="eml.taxonomicCoverages.taxonKeyword.scientificName" name="scientificName" requiredField=true />
                                    </div>

                                    <div class="col-lg-4">
                                        <@input i18nkey="eml.taxonomicCoverages.taxonKeyword.commonName" name="commonName" />
                                    </div>

                                    <div class="col-lg-4">
                                        <@select i18nkey="eml.taxonomicCoverages.taxonKeyword.rank"  name="rank" options=ranks! />
                                    </div>
                                </div>
                            </div>

                        </div>
                    </div>
                </main>
            </div>
        </div>

    </form>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
