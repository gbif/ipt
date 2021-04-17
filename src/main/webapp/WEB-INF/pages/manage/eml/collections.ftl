<#escape x as x?html>
    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.metadata.collections.title'/></title>
    <script type="text/javascript">
        $(document).ready(function(){
            initHelp();
        });
    </script>
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <#assign auxTopNavbar=true />
    <#assign auxTopNavbarPage = "metadata" />
    <#assign currentMenu="manage"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms-bootstrap.ftl"/>

    <main class="container">
        <div class="row g-3">
            <div class="p-3 bg-body rounded shadow-sm">

                <#include "/WEB-INF/pages/inc/action_alerts.ftl">

                <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header text-center">
                    <@s.text name='manage.metadata.collections.title'/>:
                    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                </h5>

                <p class="text-muted mx-md-4 mx-2 mb-0">
                    <@s.text name='manage.metadata.collections.intro'/>
                </p>

                <!-- retrieve some link names one time -->
                <#assign removeCollectionLink><@s.text name='manage.metadata.removethis'/> <@s.text name='eml.collection'/></#assign>
                <#assign addCollectionLink><@s.text name='manage.metadata.addnew'/> <@s.text name='eml.collection'/></#assign>
                <#assign removeSpecimenPreservationMethodLink><@s.text name='manage.metadata.removethis'/> <@s.text name='eml.specimenPreservationMethod.short'/></#assign>
                <#assign addSpecimenPreservationMethodLink><@s.text name='manage.metadata.addnew'/> <@s.text name='eml.specimenPreservationMethod.short'/></#assign>

            </div>
        </div>

        <div class="row g-3 mt-1">
            <div class="col-lg-12 p-3 bg-body rounded shadow-sm">
                <!-- List of Collections -->
                <div>
                    <@textinline name="eml.collection.plural" help="i18n"/>
                    <div id="collection-items">
                        <#list eml.collections as item>
                            <div id="collection-item-${item_index}" class="item clearfix row g-3 mx-md-3 mx-1 border-bottom pb-3 mt-1">
                                <div class="columnLinks mt-1 d-flex justify-content-end">
                                    <a id="collection-removeLink-${item_index}" class="removeCollectionLink" href="">[ ${removeCollectionLink?lower_case?cap_first} ]</a>
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
                    <div class="addNew col-12 mx-md-4 mx-2 mt-1">
                        <a id="plus-collection" href="">${addCollectionLink?lower_case?cap_first}</a>
                    </div>
                </div>
            </div>
        </div>

        <div class="row g-3 mt-1">
            <div class="col-lg-12 p-3 bg-body rounded shadow-sm">
                <!-- List of Collections -->
                <div class="listBlock">
                    <@textinline name="eml.specimenPreservationMethod.plural" help="i18n"/>
                    <div id="specimenPreservationMethod-items">
                        <#list eml.specimenPreservationMethods as item>
                            <div id="specimenPreservationMethod-item-${item_index}" class="item clearfix row g-3 mx-md-3 mx-1 border-bottom pb-3 mt-1">
                                <div class="columnLinks mt-1 d-flex justify-content-end">
                                    <a id="specimenPreservationMethod-removeLink-${item_index}" class="removeSpecimenPreservationMethodLink" href="">[ ${removeSpecimenPreservationMethodLink?lower_case?cap_first} ]</a>
                                </div>
                                <div class="col-lg-6">
                                    <@select name="eml.specimenPreservationMethods[${item_index}]" i18nkey="eml.specimenPreservationMethod" help="i18n" options=preservationMethods value="${eml.specimenPreservationMethods[item_index]!}" />
                                </div>
                            </div>
                        </#list>
                    </div>
                    <div class="addNew col-12 mx-md-4 mx-2 mt-1">
                        <a id="plus-specimenPreservationMethod" href="">${addSpecimenPreservationMethodLink?lower_case?cap_first}</a>
                    </div>
                </div>
            </div>
        </div>

        <div class="row g-3 mt-1">
            <div class="col-lg-12 p-3 bg-body rounded shadow-sm">
                <!-- List of Curatorial Units -->
                <div class="listBlock">
                    <@textinline name="manage.metadata.collections.curatorialUnits.title" help="i18n"/>
                    <div id="items">
                        <#list eml.jgtiCuratorialUnits as item>
                            <#assign type="${eml.jgtiCuratorialUnits[item_index].type}"/>
                            <div id="item-${item_index}" class="item clearfix row g-3 mx-md-3 mx-1 border-bottom pb-3 mt-1">
                                <div class="mt-1 d-flex justify-content-end">
                                    <a id="removeLink-${item_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.collections.curatorialUnits.item'/> ]</a>
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
                    <div class="addNew col-12 mx-md-4 mx-2 mt-1">
                        <a id="plus" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.collections.curatorialUnits.item'/></a>
                    </div>
                </div>

                <div class="buttons col-12 mx-md-4 mx-2 mt-3">
                    <@s.submit cssClass="button btn btn-outline-gbif-primary" name="save" key="button.save" />
                    <@s.submit cssClass="button btn btn-outline-secondary" name="cancel" key="button.cancel" />
                </div>

                <!-- internal parameter -->
                <input name="r" type="hidden" value="${resource.shortname}" />

                <div id="baseItem" class="item clearfix row g-3 mx-md-3 mx-1 border-bottom pb-3 mt-1" style="display:none;">
                    <div class="mt-1 d-flex justify-content-end">
                        <a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.collections.curatorialUnits.item'/> ]</a>
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

                <div id="baseItem-collection" class="item clearfix row g-3 mx-md-3 mx-1 border-bottom pb-3 mt-1" style="display:none;">
                    <div class="columnLinks mt-1 d-flex justify-content-end">
                        <a id="collection-removeLink" class="removeCollectionLink" href="">[ ${removeCollectionLink?lower_case?cap_first} ]</a>
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

                <div id="baseItem-specimenPreservationMethod" class="item clearfix row g-3 mx-md-3 mx-1 border-bottom pb-3 mt-1" style="display:none;">
                    <div class="columnLinks mt-1 d-flex justify-content-end">
                        <a id="specimenPreservationMethod-removeLink" class="removeSpecimenPreservationLink" href="">[ ${removeSpecimenPreservationMethodLink?lower_case?cap_first} ]</a>
                    </div>
                    <div class="col-lg-6">
                        <@select name="specimenPreservationMethods" i18nkey="eml.specimenPreservationMethod" help="i18n" options=preservationMethods />
                    </div>
                </div>

                <div id="range-99999" style="display:none" >
                    <div class="col-lg-6">
                        <@input name="rangeStart" i18nkey="eml.jgtiCuratorialUnits.rangeStart" size=40/>
                    </div>
                    <div class="col-lg-6">
                        <@input name="rangeEnd" i18nkey="eml.jgtiCuratorialUnits.rangeEnd" size=40/>
                    </div>
                </div>
                <div id="uncertainty-99999"  style="display:none" >
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
    </form>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
