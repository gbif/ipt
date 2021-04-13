<#escape x as x?html>
    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header-bootstrap.ftl">
    <title><@s.text name='manage.metadata.methods.title'/></title>
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <script type="text/javascript">
        $(document).ready(function(){
            initHelp();
            $("#removeLink-0").hide();
            if("${eml.methodSteps?size}" == 0){
                $("#plus").click();
            }
        });
    </script>
    <#assign auxTopNavbar=true />
    <#assign auxTopNavbarPage = "metadata" />
    <#assign currentMenu="manage"/>
    <#include "/WEB-INF/pages/inc/menu-bootstrap.ftl">
    <#include "/WEB-INF/pages/macros/forms-bootstrap.ftl"/>

    <main class="container">
        <div class="row g-3">
            <div class="p-3 bg-body rounded shadow-sm">
            <#include "/WEB-INF/pages/inc/action_alerts-bootstrap.ftl">

                <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-primary text-center">
                    <@s.text name='manage.metadata.methods.title'/>:
                    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                </h5>

                <p class="text-muted mx-md-4 mx-2 mb-0">
                    <@s.text name='manage.metadata.methods.intro'/>
                </p>

                <div id="sampling" class="row g-3 mx-md-3 mx-1 mt-1">
                    <div class="col-lg-12">
                        <@text name="eml.studyExtent"  i18nkey="eml.studyExtent" help="i18n" requiredField=true />
                    </div>

                    <div class="col-lg-12">
                        <@text name="eml.sampleDescription" i18nkey="eml.sampleDescription" help="i18n" requiredField=true />
                    </div>
                </div>

                <div id="qualitycontrol" class="row g-3 mx-md-3 mx-1 mt-1">
                    <div class="col-lg-12">
                        <@text name="eml.qualityControl" i18nkey="eml.qualityControl" help="i18n"/>
                    </div>
                </div>

                <div id="items">
                    <#if eml.methodSteps??>
                        <#list eml.methodSteps as item>
                            <div id="item-${item_index}" class="item row g-3 mx-md-3 mx-1 border-bottom pb-3 mt-1">
                                <div class="mt-3 d-flex justify-content-end">
                                    <a id="removeLink-${item_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.methods.item'/> ]</a>
                                </div>
                                <div class="col-lg-12">
                                    <@text name="eml.methodSteps[${item_index}]" i18nkey="eml.methodSteps" help="i18n" requiredField=true/>
                                </div>
                            </div>
                        </#list>
                    </#if>
                </div>

                <div class="addNew col-12 mx-md-4 mx-2 mt-1">
                    <a id="plus" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.methods.item'/></a>
                </div>

                <div class="buttons col-12 mx-md-4 mx-2 mt-3">
                    <@s.submit cssClass="button btn btn-outline-gbif-primary" name="save" key="button.save" />
                    <@s.submit cssClass="button btn btn-outline-secondary" name="cancel" key="button.cancel" />
                </div>

                <!-- internal parameter -->
                <input name="r" type="hidden" value="${resource.shortname}" />

                <div id="baseItem" class="item row g-3 mx-md-3 mx-1 border-bottom pb-3 mt-1" style="display:none">
                    <div class="d-flex justify-content-end mt-1">
                        <a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.methods.item'/> ]</a>
                    </div>
                    <div class="col-lg-12">
                        <@text name="" i18nkey="eml.methodSteps" help="i18n" requiredField=true />
                    </div>
                </div>

            </div>
        </div>
    </main>
    </form>

    <#include "/WEB-INF/pages/inc/footer-bootstrap.ftl">
</#escape>
