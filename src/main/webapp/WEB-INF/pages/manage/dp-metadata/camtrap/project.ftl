<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.datapackagemetadata.camtrap.project.title'/></title>
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
    <script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <script>
        $(document).ready(function(){
            $('select#metadata\\.project\\.captureMethod').select2({
                placeholder: '${action.getText("datapackagemetadata.project.captureMethod.select")?js_string}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                width: "100%",
                allowClear: true,
                multiple: true,
                theme: 'bootstrap4'
            });
            $('select#metadata\\.project\\.observationLevel').select2({
                placeholder: '${action.getText("datapackagemetadata.project.observationLevel.select")?js_string}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                width: "100%",
                allowClear: true,
                multiple: true,
                theme: 'bootstrap4'
            });
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
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>
    <#include "/WEB-INF/pages/macros/popover.ftl"/>

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
                            <@s.text name='manage.datapackagemetadata.camtrap.project.title'/>
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
                            <p class="mb-5"><@s.text name="datapackagemetadata.project.intro"/></p>

                            <div class="row g-3">
                                <div class="col-12">
                                    <@input name="metadata.project.title" help="i18n" i18nkey="datapackagemetadata.project.title" requiredField=true />
                                </div>

                                <div class="col-12">
                                    <@text name="metadata.project.description" help="i18n" i18nkey="datapackagemetadata.project.description" />
                                </div>

                                <div class="col-lg-6">
                                    <@input name="metadata.project.acronym" help="i18n" i18nkey="datapackagemetadata.project.acronym" />
                                </div>

                                <div class="col-lg-6">
                                    <@input name="metadata.project.path" help="i18n" i18nkey="datapackagemetadata.project.path" />
                                </div>

                                <div class="col-lg-6">
                                    <#if (metadata.project.samplingDesign)??>
                                        <@select name="metadata.project.samplingDesign" help="i18n" includeEmpty=true compareValues=true options=samplingDesigns i18nkey="datapackagemetadata.project.samplingDesign" value="${metadata.project.samplingDesign!}" requiredField=true />
                                    <#else>
                                        <@select name="metadata.project.samplingDesign" help="i18n" includeEmpty=true compareValues=true options=samplingDesigns i18nkey="datapackagemetadata.project.samplingDesign" value="" requiredField=true />
                                    </#if>
                                </div>

                                <div class="col-lg-6">
                                    <div class="form-group">
                                        <@popoverPropertyInfo "datapackagemetadata.project.captureMethod.help"/>
                                        <label for="metadata.project.captureMethod" class="form-label" style="margin-bottom: 6px !important;">
                                            <@s.text name="datapackagemetadata.project.captureMethod"/> <span class="text-gbif-danger">&#42;</span>
                                        </label>
                                        <select name="metadata.project.captureMethod" id="metadata.project.captureMethod" class="form-select" required multiple>
                                            <#if captureMethods?has_content>
                                                <#list captureMethods as key, value>
                                                    <option value="${key}" <#if (metadata.project.captureMethod)?has_content && metadata.project.captureMethod?contains(value)>selected</#if> >${value}</option>
                                                </#list>
                                            </#if>
                                        </select>
                                        <@s.fielderror id="field-error-metadata.project.captureMethod" cssClass="invalid-feedback list-unstyled field-error my-1" fieldName="metadata.project.captureMethod"/>
                                    </div>
                                </div>

                                <div class="col-lg-6">
                                    <div class="form-group">
                                        <@popoverPropertyInfo "datapackagemetadata.project.observationLevel.help"/>
                                        <label for="metadata.project.observationLevel" class="form-label" style="margin-bottom: 6px !important;">
                                            <@s.text name="datapackagemetadata.project.observationLevel"/> <span class="text-gbif-danger">&#42;</span>
                                        </label>
                                        <select name="metadata.project.observationLevel" id="metadata.project.observationLevel" class="form-select" required multiple>
                                            <#if observationLevels?has_content>
                                                <#list observationLevels as key, value>
                                                    <option value="${key}" <#if (metadata.project.observationLevel)?has_content && metadata.project.observationLevel?contains(value)>selected</#if> >${value}</option>
                                                </#list>
                                            </#if>
                                        </select>
                                        <@s.fielderror id="field-error-metadata.project.observationLevel" cssClass="invalid-feedback list-unstyled field-error my-1" fieldName="metadata.project.observationLevel"/>
                                    </div>
                                </div>

                                <div class="col-12">
                                    <#if (metadata.project.individualAnimals)??>
                                        <@checkbox name="metadata.project.individualAnimals" i18nkey="datapackagemetadata.project.individualAnimals" value="${metadata.project.individualAnimals?c}" help="i18n"/>
                                    <#else>
                                        <@checkbox name="metadata.project.individualAnimals" i18nkey="datapackagemetadata.project.individualAnimals" value="false" help="i18n"/>
                                    </#if>
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
