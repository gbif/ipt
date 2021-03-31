<#escape x as x?html>
    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header-bootstrap.ftl">
    <title><@s.text name='manage.metadata.physical.title'/></title>
    <script type="text/javascript">
        $(document).ready(function(){
            initHelp();
        });
    </script>
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <#assign sideMenuEml=true />
    <#assign currentMenu="manage"/>
    <#include "/WEB-INF/pages/inc/menu-bootstrap.ftl">
    <#include "/WEB-INF/pages/macros/forms-bootstrap.ftl"/>


    <main class="container">

        <form class="needs-validation" action="metadata-${section}.do" method="post" novalidate>
            <div class="row g-3">
                <#include "/WEB-INF/pages/manage/eml/section-bootstrap.ftl">

                <div class="col-lg-9 p-3 bg-body rounded shadow-sm">

                    <#include "/WEB-INF/pages/inc/action_alerts-bootstrap.ftl">

                    <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-success text-center">
                        <@s.text name='manage.metadata.physical.title'/>:
                        <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                    </h5>

                    <p class="text-muted mx-md-4 mx-2 mb-0">
                        <@s.text name='manage.metadata.physical.intro'/>
                    </p>

                    <div class="mx-md-4 mx-2 mt-2">
                        <@input name="eml.distributionUrl" i18nkey="eml.distributionUrl" type="url" />
                    </div>
                </div>
            </div>

            <div class="row g-3 mt-1">
                <div class="col-lg-12 p-3 bg-body rounded shadow-sm">

                    <div class="listBlock">
                        <@textinline name="eml.physicalData.other" help="i18n"/>
                        
                        <div id="items">
                            <#list eml.physicalData as item>
                                <div id="item-${item_index}" class="item clearfix row g-3 mx-md-3 mx-1 border-bottom pb-3 mt-1">
                                    <div class="mt-1 d-flex justify-content-end">
                                        <a id="removeLink-${item_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.physical.item'/> ]</a>
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
                        
                        <div class="addNew col-12 mx-md-4 mx-2 mt-1">
                            <a id="plus" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.physical.item'/></a>
                        </div>
                    </div>

                    <div class="buttons col-12 mx-md-4 mx-2 mt-3">
                        <@s.submit cssClass="button btn btn-outline-success" name="save" key="button.save" />
                        <@s.submit cssClass="button btn btn-outline-secondary" name="cancel" key="button.cancel" />
                    </div>
                    
                    <!-- internal parameter -->
                    <input name="r" type="hidden" value="${resource.shortname}" />

                    <div id="baseItem" class="item clearfix row g-3 mx-md-3 mx-1 border-bottom pb-3 mt-1" style="display:none;">
                        <div class="mt-1 d-flex justify-content-end">
                            <a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.physical.item'/> ]</a>
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
        </form>
    </main>


    <#include "/WEB-INF/pages/inc/footer-bootstrap.ftl">
</#escape>
