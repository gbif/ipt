    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.metadata.collections.title'/></title>
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2-ipt.css">
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <link rel="stylesheet" href="${baseURL}/styles/contacts.css">
    <script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>
    <#-- runs on DOMContentLoaded, after Bootstrap and html5sortable from the footer -->
    <script src="${baseURL}/js/eml-cards.js"></script>
    <script>
        $(document).ready(function () {
            $('#metadata-section').change(function () {
                var metadataSection = $('#metadata-section').find(':selected').val()
                $(location).attr('href', 'metadata-' + metadataSection + '.do?r=${resource.shortname!r!}');
            });

            $('#eml\\.specimenPreservationMethods').select2({
                placeholder: '${action.getText("eml.preservation.methods.selection")?js_string}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                width: "100%",
                closeOnSelect: false,
                theme: 'bootstrap4'
            });

            // scroll to the first error, if any
            var invalid = $(".is-invalid").first();
            if (invalid.length) {
                $('body, html').animate({scrollTop: invalid.offset().top - 120});
            }

            makeSureResourceParameterIsPresentInURL('${resource.shortname}');
        });
    </script>

    <#assign currentMetadataPage = "collections"/>
    <#assign currentMenu="manage"/>
    <#include "/WEB-INF/pages/macros/icon.ftl"/>
    <#assign materialHelpIcons = true/>
    <link rel="stylesheet" href="${baseURL}/styles/overview.css">
    <link rel="stylesheet" href="${baseURL}/styles/eml.css">
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>
    <#include "eml_cards.ftl"/>

    <#macro collectionCard index=-1 item={}>
        <#local base = (index >= 0)?then("eml.collections[" + index?c + "]", "TPL")/>
        <div class="ms-step" data-card>
            <@cardHead titleKey="manage.metadata.collections.collection" index=index/>
            <div class="row g-3">
                <div class="col-12">
                    <@input name="${base}.collectionName" value=(item.collectionName)!"" i18nkey="eml.collectionName" help="i18n" requiredField=true/>
                </div>
                <div class="col-lg-6">
                    <@input name="${base}.collectionId" value=(item.collectionId)!"" i18nkey="eml.collectionId" help="i18n"/>
                </div>
                <div class="col-lg-6">
                    <@input name="${base}.parentCollectionId" value=(item.parentCollectionId)!"" i18nkey="eml.parentCollectionId" help="i18n"/>
                </div>
            </div>
        </div>
    </#macro>

    <#-- a count range (between, and) or a count with uncertainty (count, +/-); only the chosen type's fields are posted -->
    <#macro unitCard index=-1 item={}>
        <#local base = (index >= 0)?then("eml.jgtiCuratorialUnits[" + index?c + "]", "TPL")/>
        <#local type = (index >= 0)?then("${item.type}", "COUNT_RANGE")/>
        <div class="ms-step" data-card data-type="${type}">
            <@cardHead titleKey="manage.metadata.collections.unit" index=index/>
            <div class="temp-types mb-2" role="radiogroup" aria-label="${action.getText('eml.jgtiCuratorialUnits.type')}" data-switch>
                <#list ["COUNT_RANGE", "COUNT_WITH_UNCERTAINTY"] as t>
                    <button type="button" class="temp-type<#if t == type> on</#if>" data-type="${t}" role="radio" aria-checked="${(t == type)?c}"><@s.text name="jgtiCuratorialUnitType.${t}"/></button>
                </#list>
            </div>
            <div class="row g-3">
                <div class="col-lg-6">
                    <div class="row g-3" data-type-fields="COUNT_RANGE" <#if type != "COUNT_RANGE">hidden</#if>>
                        <div class="col-6">
                            <@input name="${base}.rangeStart" value=(item.rangeStart?c)!"" i18nkey="eml.jgtiCuratorialUnits.rangeStart" type="number" requiredField=true/>
                        </div>
                        <div class="col-6">
                            <@input name="${base}.rangeEnd" value=(item.rangeEnd?c)!"" i18nkey="eml.jgtiCuratorialUnits.rangeEnd" type="number" requiredField=true/>
                        </div>
                    </div>
                    <div class="row g-3" data-type-fields="COUNT_WITH_UNCERTAINTY" <#if type != "COUNT_WITH_UNCERTAINTY">hidden</#if>>
                        <div class="col-6">
                            <@input name="${base}.rangeMean" value=(item.rangeMean?c)!"" i18nkey="eml.jgtiCuratorialUnits.rangeMean" type="number" requiredField=true/>
                        </div>
                        <div class="col-6">
                            <@input name="${base}.uncertaintyMeasure" value=(item.uncertaintyMeasure?c)!"" i18nkey="eml.jgtiCuratorialUnits.uncertaintyMeasure" type="number" requiredField=true/>
                        </div>
                    </div>
                </div>
                <div class="col-lg-6">
                    <@input name="${base}.unitType" value=(item.unitType)!"" i18nkey="eml.jgtiCuratorialUnits.unitType" requiredField=true/>
                </div>
            </div>
        </div>
    </#macro>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form class="needs-validation track-unsaved" action="metadata-${section}.do" method="post" novalidate>
        <#include "eml_heading.ftl"/>

        <#include "metadata_section_select.ftl"/>

        <div class="container-fluid bg-body">
            <div class="container bd-layout main-content-container">
                <main class="bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "eml_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">
                        <div class="my-md-3 p-3" id="s-collections" data-toc="${action.getText('eml.collection.plural')}">
                            <@textinline name="eml.collection.plural"/>
                            <p class="party-intro"><@s.text name="eml.collection.plural.help"/></p>

                            <@cardList base="eml.collections" titleKey="manage.metadata.collections.collection"
                                       addKey="manage.metadata.collections.addCollection" emptyKey="manage.metadata.collections.noCollection"; part>
                                <#if part == "cards">
                                    <#list eml.collections as item>
                                        <@collectionCard index=item?index item=item/>
                                    </#list>
                                <#else>
                                    <@collectionCard/>
                                </#if>
                            </@cardList>
                        </div>

                        <div class="my-3 p-3" id="s-preservation" data-toc="${action.getText('eml.specimenPreservationMethod.plural')}">
                            <@textinline name="eml.specimenPreservationMethod.plural" i18nkey="eml.specimenPreservationMethod" help="i18n"/>
                            <p class="party-intro"><@s.text name="eml.specimenPreservationMethod.plural.help"/></p>
                            <#assign storedMethods = eml.specimenPreservationMethods![]/>
                            <label class="visually-hidden" for="eml.specimenPreservationMethods"><@s.text name="eml.specimenPreservationMethod.plural"/></label>
                            <select class="form-select" id="eml.specimenPreservationMethods" name="eml.specimenPreservationMethods" multiple>
                                <#list preservationMethods as key, label>
                                    <#if key?has_content>
                                        <option value="${key}" <#if storedMethods?seq_contains(key)>selected</#if>>${label}</option>
                                    </#if>
                                </#list>
                                <#-- a method the vocabulary doesn't list is kept, so saving doesn't drop it -->
                                <#list storedMethods as method>
                                    <#if method?has_content && !(preservationMethods[method])??>
                                        <option value="${method}" selected>${method}</option>
                                    </#if>
                                </#list>
                            </select>
                        </div>

                        <div class="my-3 p-3" id="s-curatorial-units" data-toc="${action.getText('manage.metadata.collections.curatorialUnits.title')}">
                            <@textinline name="manage.metadata.collections.curatorialUnits.title"/>
                            <p class="party-intro"><@s.text name="manage.metadata.collections.curatorialUnits.title.help"/></p>

                            <@cardList base="eml.jgtiCuratorialUnits" titleKey="manage.metadata.collections.unit"
                                       addKey="manage.metadata.collections.addUnit" emptyKey="manage.metadata.collections.noUnit"; part>
                                <#if part == "cards">
                                    <#list eml.jgtiCuratorialUnits as item>
                                        <@unitCard index=item?index item=item/>
                                    </#list>
                                <#else>
                                    <@unitCard/>
                                </#if>
                            </@cardList>
                        </div>

                        <!-- internal parameter -->
                        <input name="r" type="hidden" value="${resource.shortname}" />
                    </div>
                </main>
            </div>
        </div>

        <#include "eml_savebar.ftl"/>
    </form>

    <#include "/WEB-INF/pages/manage/eml/unsaved_changes_modal.ftl">

    <div class="party-toast" id="party-toast" role="status" aria-live="polite"></div>

    <#include "/WEB-INF/pages/inc/footer.ftl">
