<p class="text-muted mx-md-4 mx-2 pt-2"><@s.text name="manage.resource.create.intro"/></p>

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

<form class="needs-validation" action="create.do" method="post" enctype="multipart/form-data" novalidate>
    <div class="row g-3 mx-md-4 mx-2 mt-0 mb-2">
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

        <div class="col-12">
        <#if (organisations?size>0) >
            <@s.submit cssClass="btn btn-outline-success" name="create" key="button.create"/>
        <#else>
            <!-- Disable create button and show warning: must be at least one organization able to host -->
            <@s.submit cssClass="btn btn-outline-success" name="create" key="button.create" disabled="true"/>
            <img class="infoImg" src="${baseURL}/images/warning.gif"/>
            <div class="info autop">
                <@s.text name="manage.resource.create.forbidden"/>
            </div>
        </#if>
        </div>
    </div>
</form>
