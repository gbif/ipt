<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="manage.resource.create.title"/></title>
 <#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<#include "/WEB-INF/pages/macros/popover.ftl">

<form id="create" class="needs-validation" action="create.do" method="post" enctype="multipart/form-data" novalidate>
    <div class="container-fluid bg-body border-bottom">
        <div class="container my-3">
            <#include "/WEB-INF/pages/inc/action_alerts.ftl">
        </div>

        <div class="container my-3 p-3">
            <div class="text-center text-uppercase fw-bold fs-smaller-2">
                <@s.text name="menu.manage.short"/>
            </div>

            <div class="text-center">
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    <@s.text name="manage.resource.create.title"/>
                </h1>

                <#if (organisations?size==0)>
                    <div class="text-smaller text-gbif-danger">
                        <@s.text name="manage.resource.create.forbidden"/>
                    </div>
                </#if>

                <div class="mt-2">
                    <#if (organisations?size>0) >
                        <@s.submit form="create" cssClass="btn btn-sm btn-outline-gbif-primary top-button" name="create" key="button.create"/>
                    </#if>
                    <a href="${baseURL}/manage/" class="btn btn-sm btn-outline-secondary top-button">
                        <@s.text name="button.cancel"/>
                    </a>
                </div>
            </div>
        </div>
    </div>

    <main class="container">
        <div class="my-3 p-3">
            <p class="pt-2"><@s.text name="manage.resource.create.intro"/></p>

            <script>
                $(document).ready(function() {
                    /** This function updates the map each time the global coverage checkbox is checked or unchecked  */
                    $(":checkbox").click(function() {
                        if($("#importDwca").is(":checked")) {
                            $("#import-dwca-section").slideDown('slow');
                        } else {
                            $("#file").attr("value", '');
                            $("#import-dwca-section").slideUp('slow');
                        }
                    });
                    $("#import-dwca-section").slideUp('fast');
                });
            </script>

            <div class="row g-3 mt-0 mb-2">
                <div class="col-sm-6">
                    <@input name="shortname" i18nkey="resource.shortname" help="i18n" errorfield="resource.shortname" size=40/>
                </div>

                <div class="col-sm-6">
                    <@select name="resourceType" i18nkey="manage.resource.create.coreType" help="i18n" options=types value="" />
                </div>

                <div class="col-12">
                    <@checkbox name="importDwca" help="i18n" i18nkey="manage.resource.create.archive"/>
                </div>

                <div id="import-dwca-section" class="col-12">
                    <@s.fielderror cssClass="fielderror" fieldName="file"/>
                    <label for="file" class="form-label"><@s.text name="manage.resource.create.file"/>: </label>
                    <@s.file name="file" cssClass="form-control" key="manage.resource.create.file" />
                </div>
            </div>
        </div>
    </main>
</form>

<#include "/WEB-INF/pages/inc/footer.ftl">
