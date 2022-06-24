<#escape x as x?html>
    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.metadata.physical.title'/></title>
    <script>
        $(document).ready(function () {
            $('#metadata-section').change(function () {
                var metadataSection = $('#metadata-section').find(':selected').val()
                $(location).attr('href', 'metadata-' + metadataSection + '.do?r=${resource.shortname!r!}');
            });
        });
    </script>

    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <#assign currentMetadataPage = "physical"/>
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
                    <@s.text name="manage.overview.metadata"/>
                </div>

                <div class="text-center">
                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                        <@s.text name='manage.metadata.physical.title'/>
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
                                <@s.text name='manage.metadata.physical.intro'/>
                            </p>

                            <div class="mt-2">
                                <@input name="eml.distributionUrl" i18nkey="eml.distributionUrl" type="url" />
                            </div>
                        </div>

                        <div class="my-md-3 p-3">

                            <div class="listBlock">
                                <@textinline name="eml.physicalData.other" help="i18n"/>

                                <div id="items">
                                    <#list eml.physicalData as item>
                                        <div id="item-${item_index}" class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                            <div class="mt-1 d-flex justify-content-end">
                                                <a id="removeLink-${item_index}" class="removeLink text-smaller" href="">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" class="link-icon">
                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                        </svg>
                                                    </span>
                                                    <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.physical.item'/></span>
                                                </a>
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="eml.physicalData[${item_index}].name" i18nkey="eml.physicalData.name" requiredField=true />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="eml.physicalData[${item_index}].charset" i18nkey="eml.physicalData.charset" help="i18n" requiredField=true />
                                            </div>
                                            <div class="fullcolumn">
                                                <@input name="eml.physicalData[${item_index}].distributionUrl" i18nkey="eml.physicalData.distributionUrl" help="i18n" requiredField=true type="url" />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="eml.physicalData[${item_index}].format" i18nkey="eml.physicalData.format" help="i18n" requiredField=true />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="eml.physicalData[${item_index}].formatVersion" i18nkey="eml.physicalData.formatVersion" help="i18n"/>
                                            </div>
                                        </div>
                                    </#list>
                                </div>

                                <div class="addNew col-12 mt-1">
                                    <a id="plus" href="" class="text-smaller">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.physical.item'/></span>
                                    </a>
                                </div>
                            </div>

                            <!-- internal parameter -->
                            <input name="r" type="hidden" value="${resource.shortname}" />

                            <div id="baseItem" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display:none;">
                                <div class="mt-1 d-flex justify-content-end">
                                    <a id="removeLink" class="removeLink text-smaller" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                            </svg>
                                        </span>
                                        <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.physical.item'/></span>
                                    </a>
                                </div>
                                <div class="col-lg-6">
                                    <@input name="name" i18nkey="eml.physicalData.name" requiredField=true />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="charset" i18nkey="eml.physicalData.charset" help="i18n" requiredField=true />
                                </div>
                                <@input name="distributionUrl" i18nkey="eml.physicalData.distributionUrl" help="i18n" requiredField=true />
                                <div class="col-lg-6">
                                    <@input name="format" i18nkey="eml.physicalData.format" help="i18n" requiredField=true />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="formatVersion" i18nkey="eml.physicalData.formatVersion" help="i18n"/>
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
