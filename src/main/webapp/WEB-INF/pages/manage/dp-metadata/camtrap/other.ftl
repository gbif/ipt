<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.CamtrapMetadataAction" -->
    <#include "/WEB-INF/pages/macros/icon.ftl"/>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.datapackagemetadata.camtrap.other.title'/></title>
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2-ipt.css">
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <link rel="stylesheet" href="${baseURL}/styles/contacts.css">
    <link rel="stylesheet" href="${baseURL}/styles/overview.css">
    <link rel="stylesheet" href="${baseURL}/styles/eml.css">
    <script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>
    <script src="${baseURL}/js/dp-metadata.js"></script>
    <script>
        $(document).ready(function () {
            var placeholders = {
                relationType: '${action.getText("datapackagemetadata.other.relationType.select")?js_string}',
                resourceTypeGeneral: '${action.getText("datapackagemetadata.other.resourceTypeGeneral.select")?js_string}',
                relatedIdentifierType: '${action.getText("datapackagemetadata.other.relatedIdentifierType.select")?js_string}'
            };

            function initSelects($scope) {
                Object.keys(placeholders).forEach(function (field) {
                    $scope.find('select[id$=".' + field + '"]').select2({
                        placeholder: placeholders[field],
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
            }

            dpList({key: 'relatedIdentifier', prefix: 'metadata.relatedIdentifiers', onAdd: initSelects});
            dpList({key: 'reference', prefix: 'metadata.references'});
            initSelects($('#relatedIdentifier-items'));

            // scroll to the first error
            var $invalid = $(".is-invalid").first();
            if ($invalid.length) {
                $('body, html').animate({scrollTop: $invalid.offset().top - 100});
            }

            makeSureResourceParameterIsPresentInURL('${resource.shortname}');
        });
    </script>
    <#assign currentMenu="manage"/>
    <#assign currentMetadataPage = "other"/>
    <#assign savebarAction = "camtrap-metadata-other"/>
    <#assign materialHelpIcons = true/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <#function t key>
        <#return action.getText(key)/>
    </#function>

    <#assign removeRelatedIdentifierLink = (t('manage.metadata.removethis') + " " + t('datapackagemetadata.other.relatedIdentifier'))?lower_case?cap_first/>
    <#assign addRelatedIdentifierLink = (t('manage.metadata.addnew') + " " + t('datapackagemetadata.other.relatedIdentifier'))?lower_case?cap_first/>
    <#assign removeReferenceLink = (t('manage.metadata.removethis') + " " + t('datapackagemetadata.other.reference'))?lower_case?cap_first/>
    <#assign addReferenceLink = (t('manage.metadata.addnew') + " " + t('datapackagemetadata.other.reference'))?lower_case?cap_first/>

    <#-- one related identifier; index "" is the template -->
    <#macro relatedIdentifierCard index="" item={}>
        <#local base = index?has_content?then("metadata.relatedIdentifiers[" + index + "]", "metadata.relatedIdentifiers")/>
        <div <#if index?has_content>id="relatedIdentifier-item-${index}"<#else>id="baseItem-relatedIdentifier" style="display: none;"</#if> class="item dp-card">
            <div class="dp-card-head">
                <span class="dp-card-title" data-card-title data-title-from='input[id$="relatedIdentifier"]' data-empty="${t('datapackagemetadata.other.relatedIdentifier')}"></span>
                <a id="relatedIdentifier-removeLink<#if index?has_content>-${index}</#if>" href="" class="party-icon-btn party-icon-danger remove-item" title="${removeRelatedIdentifierLink}" aria-label="${removeRelatedIdentifierLink}"><@icon name="delete"/></a>
            </div>
            <div class="row g-3">
                <div class="col-lg-6">
                    <@input name="${base}.relatedIdentifier" value=item.relatedIdentifier!"" help="i18n" i18nkey="datapackagemetadata.other.relatedIdentifier" requiredField=true />
                </div>
                <div class="col-lg-6">
                    <@select name="${base}.relatedIdentifierType" help="i18n" includeEmpty=true compareValues=true options=relatedIdentifierTypes! i18nkey="datapackagemetadata.other.relatedIdentifierType" value=(item.relatedIdentifierType)!"" requiredField=true />
                </div>
                <div class="col-lg-6">
                    <@select name="${base}.relationType" help="i18n" includeEmpty=true compareValues=true options=relationTypes! i18nkey="datapackagemetadata.other.relationType" value=(item.relationType)!"" requiredField=true />
                </div>
                <div class="col-lg-6">
                    <@select name="${base}.resourceTypeGeneral" help="i18n" includeEmpty=true compareValues=true options=resourceTypeGenerals! i18nkey="datapackagemetadata.other.resourceTypeGeneral" value=(item.resourceTypeGeneral)!"" />
                </div>
            </div>
        </div>
    </#macro>

    <#-- one reference: a line with its remove button; index "" is the template -->
    <#macro referenceLine index="" value="">
        <div <#if index?has_content>id="reference-item-${index}"<#else>id="baseItem-reference" style="display: none;"</#if> class="item dp-line">
            <@input name=index?has_content?then("metadata.references[" + index + "]", "metadata.references") value=value i18nkey="datapackagemetadata.other.reference" withLabel=false />
            <a id="reference-removeLink<#if index?has_content>-${index}</#if>" href="" class="party-icon-btn party-icon-danger remove-item" title="${removeReferenceLink}" aria-label="${removeReferenceLink}"><@icon name="delete"/></a>
        </div>
    </#macro>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form id="other-form" class="needs-validation track-unsaved" action="camtrap-metadata-${section}.do" method="post" novalidate>
        <input type="hidden" name="r" value="${resource.shortname}" />

        <#include "metadata_heading.ftl"/>

        <div class="container-fluid bg-body">
            <div class="container bd-layout main-content-container">
                <main class="bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "metadata_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">
                        <div class="my-md-3 p-3" id="s-gbif-ingestion" data-toc="${t('datapackagemetadata.other.gbifIngestion')}">
                            <@textinline name="datapackagemetadata.other.gbifIngestion" help="i18n"/>
                            <div class="row g-3 mt-0">
                                <div class="col-lg-6">
                                    <@select name="metadata.gbifIngestion.observationLevel" help="i18n" includeEmpty=true compareValues=true options=observationLevels i18nkey="datapackagemetadata.other.gbifIngestion.observationLevel" value="${(metadata.gbifIngestion.observationLevel)!}" />
                                </div>
                            </div>
                        </div>

                        <div class="my-3 p-3" id="s-related-identifiers" data-toc="${t('datapackagemetadata.other.relatedIdentifiers')}">
                            <@textinline name="datapackagemetadata.other.relatedIdentifiers" help="i18n"/>
                            <div id="relatedIdentifier-items" class="dp-cards">
                                <#list (metadata.relatedIdentifiers)! as item>
                                    <@relatedIdentifierCard index=item_index item=item/>
                                </#list>
                            </div>
                            <div class="add-area">
                                <a id="plus-relatedIdentifier" href=""><@icon name="add"/> <span>${addRelatedIdentifierLink}</span></a>
                            </div>
                        </div>

                        <div class="my-3 p-3" id="s-references" data-toc="${t('datapackagemetadata.other.references')}">
                            <@textinline name="datapackagemetadata.other.references" help="i18n"/>
                            <div id="reference-items" class="dp-lines">
                                <#list (metadata.references)! as item>
                                    <@referenceLine index=item_index value=item!""/>
                                </#list>
                            </div>
                            <div class="add-area">
                                <a id="plus-reference" href=""><@icon name="add"/> <span>${addReferenceLink}</span></a>
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </div>

        <#include "/WEB-INF/pages/manage/eml/eml_savebar.ftl"/>
    </form>

    <@relatedIdentifierCard/>
    <@referenceLine/>

    <#include "/WEB-INF/pages/manage/eml/unsaved_changes_modal.ftl">

    <#include "/WEB-INF/pages/inc/footer.ftl">
