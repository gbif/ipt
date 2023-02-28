<#escape x as x?html>
    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.metadata.collections.title'/></title>
    <script>
        $(document).ready(function () {
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

            // reordering
            function initAndGetSortable(selector) {
                return sortable(selector, {
                    forcePlaceholderSize: true,
                    placeholderClass: 'border',
                    handle: '.handle'
                });
            }

            const sortable_collections = initAndGetSortable('#collection-items');
            const sortable_preservation_methods = initAndGetSortable('#specimenPreservationMethod-items');
            const sortable_units = initAndGetSortable('#items');

            sortable_collections[0].addEventListener('sortupdate', changeInputNamesCollectionsAfterDragging);
            sortable_collections[0].addEventListener('drag', dragScroll);
            sortable_preservation_methods[0].addEventListener('sortupdate', changeInputNamesPreservationMethodsAfterDragging);
            sortable_preservation_methods[0].addEventListener('drag', dragScroll);
            sortable_units[0].addEventListener('sortupdate', changeInputNamesUnitsAfterDragging);
            sortable_units[0].addEventListener('drag', dragScroll);

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

            function changeInputNamesCollectionsAfterDragging(e) {
                displayProcessing();
                var contactItems = $("#collection-items div.item");

                contactItems.each(function (index) {
                    var elementId = $(this)[0].id;

                    $("div#" + elementId + " input[id$='collectionName']").attr("name", "eml.collections[" + index + "].collectionName");
                    $("div#" + elementId + " input[id$='collectionId']").attr("name", "eml.collections[" + index + "].collectionId");
                    $("div#" + elementId + " input[id$='parentCollectionId']").attr("name", "eml.collections[" + index + "].parentCollectionId");
                });

                hideProcessing();
            }

            function changeInputNamesPreservationMethodsAfterDragging(e) {
                displayProcessing();
                var contactItems = $("#specimenPreservationMethod-items div.item");

                contactItems.each(function (index) {
                    var elementId = $(this)[0].id;

                    $("div#" + elementId + " select").attr("name", "eml.specimenPreservationMethods[" + index + "]");
                });

                hideProcessing();
            }

            function changeInputNamesUnitsAfterDragging(e) {
                displayProcessing();
                var contactItems = $("#items div.item");

                contactItems.each(function (index) {
                    var elementId = $(this)[0].id;

                    $("div#" + elementId + " select").attr("name", "type-" + index);
                    $("div#" + elementId + " input[id$='rangeMean']").attr("name", "eml.jgtiCuratorialUnits[" + index + "].rangeMean");
                    $("div#" + elementId + " input[id$='uncertaintyMeasure']").attr("name", "eml.jgtiCuratorialUnits[" + index + "].uncertaintyMeasure");
                    $("div#" + elementId + " input[id$='rangeStart']").attr("name", "eml.jgtiCuratorialUnits[" + index + "].rangeStart");
                    $("div#" + elementId + " input[id$='rangeEnd']").attr("name", "eml.jgtiCuratorialUnits[" + index + "].rangeEnd");
                    $("div#" + elementId + " input[id$='unitType']").attr("name", "eml.jgtiCuratorialUnits[" + index + "].unitType");
                });

                hideProcessing();
            }
        });
    </script>

    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <#assign currentMetadataPage = "collections"/>
    <#assign currentMenu="manage"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <form class="needs-validation" action="metadata-${section}.do" method="post" novalidate>
        <div class="container-fluid bg-body border-bottom">
            <div class="container pt-2">
                <#include "/WEB-INF/pages/inc/action_alerts.ftl">
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
                        <@s.text name='manage.metadata.collections.title'/>
                    </h1>
                </div>

                <div class="text-center fs-smaller">
                    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                </div>

                <div class="text-center mt-2">
                    <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save" />
                    <@s.submit cssClass="button btn btn-sm btn-outline-secondary top-button" name="cancel" key="button.back" />
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
                            <p class="mb-0">
                                <@s.text name='manage.metadata.collections.intro'/>
                            </p>

                            <!-- retrieve some link names one time -->
                            <#assign removeCollectionLink><@s.text name='manage.metadata.removethis'/> <@s.text name='eml.collection'/></#assign>
                            <#assign addCollectionLink><@s.text name='manage.metadata.addnew'/> <@s.text name='eml.collection'/></#assign>
                            <#assign removeSpecimenPreservationMethodLink><@s.text name='manage.metadata.removethis'/> <@s.text name='eml.specimenPreservationMethod.short'/></#assign>
                            <#assign addSpecimenPreservationMethodLink><@s.text name='manage.metadata.addnew'/> <@s.text name='eml.specimenPreservationMethod.short'/></#assign>

                        </div>

                        <div class="my-md-3 p-3">
                            <!-- List of Collections -->
                            <div>
                                <@textinline name="eml.collection.plural" help="i18n"/>
                                <div id="collection-items">
                                    <#list eml.collections as item>
                                        <div id="collection-item-${item_index}" class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                            <div class="handle columnLinks mt-2 d-flex justify-content-end">
                                                <a id="collection-removeLink-${item_index}" href="" class="removeCollectionLink metadata-action-link">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                        </svg>
                                                    </span>
                                                    <span>${removeCollectionLink?lower_case?cap_first}</span>
                                                </a>
                                            </div>
                                            <div>
                                                <@input name="eml.collections[${item_index}].collectionName" help="i18n" i18nkey="eml.collectionName" requiredField=true/>
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="eml.collections[${item_index}].collectionId" help="i18n" i18nkey="eml.collectionId"/>
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="eml.collections[${item_index}].parentCollectionId" help="i18n" i18nkey="eml.parentCollectionId" />
                                            </div>
                                        </div>
                                    </#list>
                                </div>
                                <div class="addNew col-12 mt-2">
                                    <a id="plus-collection" class="metadata-action-link" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span>${addCollectionLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                            </div>
                        </div>

                        <div class="my-md-3 p-3">
                            <!-- List of Specimen preservation methods -->
                            <div class="listBlock">
                                <@textinline name="eml.specimenPreservationMethod.plural" help="i18n"/>
                                <div id="specimenPreservationMethod-items">
                                    <#list eml.specimenPreservationMethods as item>
                                        <div id="specimenPreservationMethod-item-${item_index}" class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                            <div class="handle columnLinks mt-2 d-flex justify-content-end">
                                                <a id="specimenPreservationMethod-removeLink-${item_index}" class="removeSpecimenPreservationMethodLink metadata-action-link" href="">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" class="link-icon">
                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                        </svg>
                                                    </span>
                                                    <span>${removeSpecimenPreservationMethodLink?lower_case?cap_first}</span>
                                                </a>
                                            </div>
                                            <div class="col-lg-6">
                                                <@select name="eml.specimenPreservationMethods[${item_index}]" i18nkey="eml.specimenPreservationMethod" help="i18n" options=preservationMethods value="${eml.specimenPreservationMethods[item_index]!}" />
                                            </div>
                                        </div>
                                    </#list>
                                </div>
                                <div class="addNew col-12 mt-2">
                                    <a id="plus-specimenPreservationMethod" class="metadata-action-link" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span>${addSpecimenPreservationMethodLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                            </div>
                        </div>

                        <div class="my-md-3 p-3">
                            <!-- List of Curatorial Units -->
                            <div class="listBlock">
                                <@textinline name="manage.metadata.collections.curatorialUnits.title" help="i18n"/>
                                <div id="items">
                                    <#list eml.jgtiCuratorialUnits as item>
                                        <#assign type="${eml.jgtiCuratorialUnits[item_index].type}"/>
                                        <div id="item-${item_index}" class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                            <div class="handle mt-2 d-flex justify-content-end">
                                                <a id="removeLink-${item_index}" href="" class="removeLink metadata-action-link d-flex align-items-center" style="display: inline-block !important;">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                        </svg>
                                                    </span>
                                                    <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.collections.curatorialUnits.item'/></span>
                                                </a>
                                            </div>

                                            <div class="col-lg-6">
                                                <@select name="type-${item_index}" i18nkey="eml.jgtiCuratorialUnits.type" value=type options=JGTICuratorialUnitTypeOptions />
                                            </div>

                                            <div class="col-lg-12 mt-3">
                                                <div class="row g-3">
                                                    <div id="subitem-${item_index}" class="subitem col-lg-6">
                                                        <#if type=="COUNT_RANGE">
                                                            <div id="range-${item_index}" class="row g-3">
                                                                <div class="col-lg-6">
                                                                    <@input name="eml.jgtiCuratorialUnits[${item_index}].rangeStart" i18nkey="eml.jgtiCuratorialUnits.rangeStart" size=40/>
                                                                </div>
                                                                <div class="col-lg-6">
                                                                    <@input name="eml.jgtiCuratorialUnits[${item_index}].rangeEnd" i18nkey="eml.jgtiCuratorialUnits.rangeEnd" size=40/>
                                                                </div>
                                                            </div>
                                                        <#elseif type=="COUNT_WITH_UNCERTAINTY">
                                                            <div id="uncertainty-${item_index}" class="row g-3">
                                                                <div class="col-lg-6">
                                                                    <@input name="eml.jgtiCuratorialUnits[${item_index}].rangeMean" i18nkey="eml.jgtiCuratorialUnits.rangeMean" size=40/>
                                                                </div>
                                                                <div class="col-lg-6">
                                                                    <@input name="eml.jgtiCuratorialUnits[${item_index}].uncertaintyMeasure" i18nkey="eml.jgtiCuratorialUnits.uncertaintyMeasure" size=40/>
                                                                </div>
                                                            </div>
                                                        </#if>
                                                    </div>
                                                    <div class="unittype col-lg-6">
                                                        <@input name="eml.jgtiCuratorialUnits[${item_index}].unitType" i18nkey="eml.jgtiCuratorialUnits.unitType" size=40/>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </#list>
                                </div>
                                <div class="addNew col-12 mt-2">
                                    <a id="plus" class="metadata-action-link" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.collections.curatorialUnits.item'/></span>
                                    </a>
                                </div>
                            </div>

                            <!-- internal parameter -->
                            <input name="r" type="hidden" value="${resource.shortname}" />

                            <div id="baseItem" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display:none;">
                                <div class="handle mt-2 d-flex justify-content-end">
                                    <a id="removeLink" class="removeLink metadata-action-link" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                            </svg>
                                        </span>
                                        <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.collections.curatorialUnits.item'/></span>
                                    </a>
                                </div>

                                <div class="col-lg-6">
                                    <@select name="type" i18nkey="eml.jgtiCuratorialUnits.type" value="COUNT_RANGE" options=JGTICuratorialUnitTypeOptions />
                                </div>

                                <div class="col-lg-12 mt-3">
                                    <div class="row g-3">
                                        <div class="subitem col-lg-6">
                                            <!-- The sub-form is here -->
                                            <div id="range-99999" class="row g-3" style="display:none" >
                                                <div class="col-lg-6">
                                                    <@input name="rangeStart" i18nkey="eml.jgtiCuratorialUnits.rangeStart" size=40/>
                                                </div>
                                                <div class="col-lg-6">
                                                    <@input name="rangeEnd" i18nkey="eml.jgtiCuratorialUnits.rangeEnd" size=40/>
                                                </div>
                                            </div>
                                        </div>

                                        <div class="unittype col-lg-6">
                                            <@input name="unitType" i18nkey="eml.jgtiCuratorialUnits.unitType" size=40/>
                                        </div>
                                    </div>
                                </div>

                            </div>

                            <div id="baseItem-collection" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display:none;">
                                <div class="handle columnLinks mt-2 d-flex justify-content-end">
                                    <a id="collection-removeLink" class="removeCollectionLink metadata-action-link" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                            </svg>
                                        </span>
                                        <span>${removeCollectionLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                                <div class="col-12">
                                    <@input name="collectionName" help="i18n" i18nkey="eml.collectionName" requiredField=true/>
                                </div>
                                <div class="col-lg-6">
                                    <@input name="collectionId" help="i18n" i18nkey="eml.collectionId"/>
                                </div>
                                <div class="col-lg-6">
                                    <@input name="parentCollectionId" help="i18n" i18nkey="eml.parentCollectionId" />
                                </div>
                            </div>

                            <div id="baseItem-specimenPreservationMethod" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display:none;">
                                <div class="handle columnLinks mt-2 d-flex justify-content-end">
                                    <a id="specimenPreservationMethod-removeLink" class="removeSpecimenPreservationLink metadata-action-link" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                            </svg>
                                        </span>
                                        <span>${removeSpecimenPreservationMethodLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                                <div class="col-lg-6">
                                    <@select name="specimenPreservationMethods" i18nkey="eml.specimenPreservationMethod" help="i18n" options=preservationMethods />
                                </div>
                            </div>

                            <div id="range-99999" class="row g-3" style="display:none" >
                                <div class="col-lg-6">
                                    <@input name="rangeStart" i18nkey="eml.jgtiCuratorialUnits.rangeStart" size=40/>
                                </div>
                                <div class="col-lg-6">
                                    <@input name="rangeEnd" i18nkey="eml.jgtiCuratorialUnits.rangeEnd" size=40/>
                                </div>
                            </div>
                            <div id="uncertainty-99999" class="row g-3" style="display:none" >
                                <div class="col-lg-6">
                                    <@input name="rangeMean" i18nkey="eml.jgtiCuratorialUnits.rangeMean" size=40/>
                                </div>
                                <div class="col-lg-6">
                                    <@input name="uncertaintyMeasure" i18nkey="eml.jgtiCuratorialUnits.uncertaintyMeasure" size=40/>
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
