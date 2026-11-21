<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.CamtrapMetadataAction" -->
    <#include "/WEB-INF/pages/macros/icon.ftl"/>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.datapackagemetadata.camtrap.project.title'/></title>
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2-ipt.css">
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <link rel="stylesheet" href="${baseURL}/styles/contacts.css">
    <link rel="stylesheet" href="${baseURL}/styles/overview.css">
    <link rel="stylesheet" href="${baseURL}/styles/eml.css">
    <script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>
    <script>
        $(document).ready(function () {
            $('#metadata\\.project\\.samplingDesign').select2({
                placeholder: '${action.getText("datapackagemetadata.project.samplingDesign.select")?js_string}',
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
    <#assign currentMetadataPage = "project"/>
    <#assign savebarAction = "camtrap-metadata-project"/>
    <#assign materialHelpIcons = true/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <#function t key>
        <#return action.getText(key)/>
    </#function>

    <#-- several values as checkbox chips, posted under one name; options: key -> value, current: the stored values -->
    <#macro choiceChips name i18nkey options current>
        <div class="d-flex text-smaller">
            <a tabindex="0" role="button" class="popover-link" data-bs-toggle="popover" data-bs-trigger="focus" data-bs-html="true"
               data-bs-content="${t(i18nkey + '.help')}"><@icon name="info_outlined" class="text-gbif-primary help-icon-m"/></a>
            <span class="form-label" id="${name}-label"><@s.text name=i18nkey/> <span class="text-gbif-danger">&#42;</span></span>
        </div>
        <div class="dp-chips" role="group" aria-labelledby="${name}-label" id="${name}">
            <#list options as key, value>
                <label class="dp-chip">
                    <input type="checkbox" class="form-check-input" name="${name}" value="${key}"<#if current?has_content && current?contains(value)> checked</#if>>
                    <span>${value}</span>
                </label>
            </#list>
        </div>
        <@s.fielderror id="field-error-${name}" cssClass="invalid-feedback list-unstyled field-error d-block my-1" fieldName="${name}"/>
    </#macro>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form id="project-form" class="needs-validation track-unsaved" action="camtrap-metadata-${section}.do" method="post" novalidate>
        <input type="hidden" name="r" value="${resource.shortname}" />

        <#include "metadata_heading.ftl"/>

        <div class="container-fluid bg-body">
            <div class="container bd-layout main-content-container">
                <main class="bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "metadata_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">
                        <div class="my-md-3 p-3" id="s-project" data-toc="${t('manage.metadata.section.project')}">
                            <@textinline name="manage.metadata.section.project"/>
                            <p class="party-intro"><@s.text name="datapackagemetadata.project.intro"/></p>
                            <div class="row g-3">
                                <div class="col-12">
                                    <@input name="metadata.project.title" value=(metadata.project.title)! help="i18n" i18nkey="datapackagemetadata.project.title" requiredField=true />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="metadata.project.acronym" value=(metadata.project.acronym)! help="i18n" i18nkey="datapackagemetadata.project.acronym" />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="metadata.project.id" value=(metadata.project.id)! help="i18n" i18nkey="datapackagemetadata.project.id" />
                                </div>
                                <div class="col-12">
                                    <@text name="metadata.project.description" value=(metadata.project.description)! help="i18n" i18nkey="datapackagemetadata.project.description" />
                                </div>
                                <div class="col-12">
                                    <@input name="metadata.project.path" value=(metadata.project.path)! help="i18n" i18nkey="datapackagemetadata.project.path" type="url" />
                                </div>
                            </div>
                        </div>

                        <div class="my-3 p-3" id="s-sampling" data-toc="${t('manage.datapackagemetadata.camtrap.section.sampling')}">
                            <@textinline name="manage.datapackagemetadata.camtrap.section.sampling"/>
                            <div class="row g-3 mt-0">
                                <div class="col-lg-6">
                                    <@select name="metadata.project.samplingDesign" help="i18n" includeEmpty=true compareValues=true options=samplingDesigns i18nkey="datapackagemetadata.project.samplingDesign" value=(metadata.project.samplingDesign)!"" requiredField=true />
                                </div>
                                <div class="col-12">
                                    <@choiceChips name="metadata.project.captureMethod" i18nkey="datapackagemetadata.project.captureMethod" options=captureMethods!{} current=(metadata.project.captureMethod)!""/>
                                </div>
                                <div class="col-12">
                                    <@choiceChips name="metadata.project.observationLevel" i18nkey="datapackagemetadata.project.observationLevel" options=observationLevels!{} current=(metadata.project.observationLevel)!""/>
                                </div>
                                <div class="col-12 fs-smaller">
                                    <@checkbox name="metadata.project.individualAnimals" value=(metadata.project.individualAnimals)!false i18nkey="datapackagemetadata.project.individualAnimals" help="i18n"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </div>

        <#include "/WEB-INF/pages/manage/eml/eml_savebar.ftl"/>
    </form>

    <#include "/WEB-INF/pages/manage/eml/unsaved_changes_modal.ftl">

    <#include "/WEB-INF/pages/inc/footer.ftl">
