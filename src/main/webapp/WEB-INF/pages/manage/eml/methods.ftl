<#escape x as x?html>
    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.metadata.methods.title'/></title>
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <script>
        $(document).ready(function(){
            $("#removeLink-0").hide();
            if("${eml.methodSteps?size}" == 0){
                $("#plus").click();
            }

            $('#metadata-section').change(function () {
                var metadataSection = $('#metadata-section').find(':selected').val()
                $(location).attr('href', 'metadata-' + metadataSection + '.do?r=${resource.shortname!r!}');
            });
        });
    </script>
    <#assign currentMetadataPage = "methods"/>
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
                        <@s.text name='manage.metadata.methods.title'/>
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
                                <@s.text name='manage.metadata.methods.intro'/>
                            </p>

                            <div id="sampling" class="row g-3 mt-1">
                                <div class="col-lg-12">
                                    <@text name="eml.studyExtent"  i18nkey="eml.studyExtent" help="i18n" requiredField=true />
                                </div>

                                <div class="col-lg-12">
                                    <@text name="eml.sampleDescription" i18nkey="eml.sampleDescription" help="i18n" requiredField=true />
                                </div>
                            </div>

                            <div id="qualitycontrol" class="row g-3 mt-1">
                                <div class="col-lg-12">
                                    <@text name="eml.qualityControl" i18nkey="eml.qualityControl" help="i18n"/>
                                </div>
                            </div>

                            <div id="items">
                                <#if eml.methodSteps??>
                                    <#list eml.methodSteps as item>
                                        <div id="item-${item_index}" class="item row g-3 border-bottom pb-3 mt-1">
                                            <div class="mt-3 d-flex justify-content-end">
                                                <a id="removeLink-${item_index}" class="removeLink" href=""><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.methods.item'/></a>
                                            </div>
                                            <div class="col-lg-12">
                                                <@text name="eml.methodSteps[${item_index}]" i18nkey="eml.methodSteps" help="i18n" requiredField=true/>
                                            </div>
                                        </div>
                                    </#list>
                                </#if>
                            </div>

                            <div class="addNew col-12 mt-1">
                                <a id="plus" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.methods.item'/></a>
                            </div>

                            <div class="buttons col-12 mt-3">
                                <@s.submit cssClass="button btn btn-outline-gbif-primary" name="save" key="button.save" />
                                <@s.submit cssClass="button btn btn-outline-secondary" name="cancel" key="button.back" />
                            </div>

                            <!-- internal parameter -->
                            <input name="r" type="hidden" value="${resource.shortname}" />

                            <div id="baseItem" class="item row g-3 border-bottom pb-3 mt-1" style="display:none">
                                <div class="d-flex justify-content-end mt-1">
                                    <a id="removeLink" class="removeLink" href=""><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.methods.item'/></a>
                                </div>
                                <div class="col-lg-12">
                                    <@text name="" i18nkey="eml.methodSteps" help="i18n" requiredField=true />
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
