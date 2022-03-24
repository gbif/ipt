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

                <div class="text-center">
                    <h5 class="pt-2 text-gbif-header fs-4 fw-400 text-center">
                        <@s.text name='manage.metadata.collections.title'/>
                    </h5>
                </div>

                <div class="text-center fs-smaller">
                    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                </div>
            </div>
        </div>

        <#include "metadata_section_select.ftl"/>

        <div class="container-fluid bg-body">
            <div class="container bd-layout">

                <main class="bd-main bd-main-right">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "eml_sidebar.ftl"/>
                    </div>

                    <div class="bd-content ps-lg-4">
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
                                            <div class="columnLinks mt-1 d-flex justify-content-end">
                                                <a id="collection-removeLink-${item_index}" class="removeCollectionLink" href="">${removeCollectionLink?lower_case?cap_first}</a>
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
                                <div class="addNew col-12 mt-1">
                                    <a id="plus-collection" href="">${addCollectionLink?lower_case?cap_first}</a>
                                </div>
                            </div>
                        </div>

                        <div class="my-md-3 p-3">
                            <!-- List of Collections -->
                            <div class="listBlock">
                                <@textinline name="eml.specimenPreservationMethod.plural" help="i18n"/>
                                <div id="specimenPreservationMethod-items">
                                    <#list eml.specimenPreservationMethods as item>
                                        <div id="specimenPreservationMethod-item-${item_index}" class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                            <div class="columnLinks mt-1 d-flex justify-content-end">
                                                <a id="specimenPreservationMethod-removeLink-${item_index}" class="removeSpecimenPreservationMethodLink" href="">${removeSpecimenPreservationMethodLink?lower_case?cap_first}</a>
                                            </div>
                                            <div class="col-lg-6">
                                                <@select name="eml.specimenPreservationMethods[${item_index}]" i18nkey="eml.specimenPreservationMethod" help="i18n" options=preservationMethods value="${eml.specimenPreservationMethods[item_index]!}" />
                                            </div>
                                        </div>
                                    </#list>
                                </div>
                                <div class="addNew col-12 mt-1">
                                    <a id="plus-specimenPreservationMethod" href="">${addSpecimenPreservationMethodLink?lower_case?cap_first}</a>
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
                                            <div class="mt-1 d-flex justify-content-end">
                                                <a id="removeLink-${item_index}" class="removeLink" href=""><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.collections.curatorialUnits.item'/></a>
                                            </div>

                                            <div class="col-lg-6">
                                                <@select name="type-${item_index}" i18nkey="eml.jgtiCuratorialUnits.type" value=type options=JGTICuratorialUnitTypeOptions />
                                            </div>

                                            <div class="col-lg-12 mt-3">
                                                <div class="row">
                                                    <div id="subitem-${item_index}" class="subitem col-lg-6">
                                                        <#if type=="COUNT_RANGE">
                                                            <div id="range-${item_index}" class="row">
                                                                <div class="col-lg-6">
                                                                    <@input name="eml.jgtiCuratorialUnits[${item_index}].rangeStart" i18nkey="eml.jgtiCuratorialUnits.rangeStart" size=40/>
                                                                </div>
                                                                <div class="col-lg-6">
                                                                    <@input name="eml.jgtiCuratorialUnits[${item_index}].rangeEnd" i18nkey="eml.jgtiCuratorialUnits.rangeEnd" size=40/>
                                                                </div>
                                                            </div>
                                                        <#elseif type=="COUNT_WITH_UNCERTAINTY">
                                                            <div id="uncertainty-${item_index}" class="row">
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
                                <div class="addNew col-12 mt-1">
                                    <a id="plus" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.collections.curatorialUnits.item'/></a>
                                </div>
                            </div>

                            <div class="buttons col-12 mt-3">
                                <@s.submit cssClass="button btn btn-outline-gbif-primary" name="save" key="button.save" />
                                <@s.submit cssClass="button btn btn-outline-secondary" name="cancel" key="button.back" />
                            </div>

                            <!-- internal parameter -->
                            <input name="r" type="hidden" value="${resource.shortname}" />

                            <div id="baseItem" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display:none;">
                                <div class="mt-1 d-flex justify-content-end">
                                    <a id="removeLink" class="removeLink" href=""><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.collections.curatorialUnits.item'/></a>
                                </div>

                                <div class="col-lg-6">
                                    <@select name="type" i18nkey="eml.jgtiCuratorialUnits.type" value="COUNT_RANGE" options=JGTICuratorialUnitTypeOptions />
                                </div>

                                <div class="col-lg-12 mt-3">
                                    <div class="row">
                                        <div class="subitem col-lg-6">
                                            <!-- The sub-form is here -->
                                            <div id="range-99999" class="row" style="display:none" >
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
                                <div class="columnLinks mt-1 d-flex justify-content-end">
                                    <a id="collection-removeLink" class="removeCollectionLink" href="">${removeCollectionLink?lower_case?cap_first}</a>
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
                                <div class="columnLinks mt-1 d-flex justify-content-end">
                                    <a id="specimenPreservationMethod-removeLink" class="removeSpecimenPreservationLink" href="">${removeSpecimenPreservationMethodLink?lower_case?cap_first}</a>
                                </div>
                                <div class="col-lg-6">
                                    <@select name="specimenPreservationMethods" i18nkey="eml.specimenPreservationMethod" help="i18n" options=preservationMethods />
                                </div>
                            </div>

                            <div id="range-99999" class="row" style="display:none" >
                                <div class="col-lg-6">
                                    <@input name="rangeStart" i18nkey="eml.jgtiCuratorialUnits.rangeStart" size=40/>
                                </div>
                                <div class="col-lg-6">
                                    <@input name="rangeEnd" i18nkey="eml.jgtiCuratorialUnits.rangeEnd" size=40/>
                                </div>
                            </div>
                            <div id="uncertainty-99999" class="row" style="display:none" >
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
