<p class="mx-md-4 mx-2 pt-2"><@s.text name="manage.resource.create.intro"/></p>

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
            <@s.submit cssClass="btn btn-outline-gbif-primary" name="create" key="button.create"/>
        <#else>
            <!-- Disable create button and show warning: must be at least one organization able to host -->
            <@s.submit cssClass="btn btn-outline-secondary mx-1" name="create" key="button.create" disabled="true"/>

            <span data-bs-container="body" data-bs-toggle="popover" data-bs-placement="top" data-bs-html="true" data-bs-content="<@s.text name="manage.resource.create.forbidden"/>">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="#ffc107" class="bi bi-exclamation-circle" viewBox="0 0 16 16">
                    <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                    <path d="M7.002 11a1 1 0 1 1 2 0 1 1 0 0 1-2 0zM7.1 4.995a.905.905 0 1 1 1.8 0l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 4.995z"/>
                </svg>
            </span>
        </#if>
        </div>
    </div>
</form>
