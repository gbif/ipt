<p><@s.text name="manage.resource.create.intro"/></p>

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

<@s.form cssClass="topForm half" action="create.do" method="post" enctype="multipart/form-data" namespace="" includeContext="false">
  <@input name="shortname" i18nkey="resource.shortname" help="i18n" errorfield="resource.shortname" size=40/>
  <@select name="resourceType" i18nkey="manage.resource.create.coreType" help="i18n" options=types value="" />
  <@checkbox name="importDwca" help="i18n" i18nkey="manage.resource.create.archive"/>
  <div id="import-dwca-section">
	  <@s.fielderror cssClass="fielderror" fieldName="file"/>
	  <label for="file"><@s.text name="manage.resource.create.file"/>: </label>
    <@s.file name="file" key="manage.resource.create.file" />
  </div>

  <div id="create-button" class="buttons">
    <#if (organisations?size>0) >
      <@s.submit cssClass="button" name="create" key="button.create"/>
    <#else>
      <!-- Disable create button and show warning: must be at least one organization able to host -->
      <@s.submit cssClass="button" name="create" key="button.create" disabled="true"/>
      <img class="infoImg" src="${baseURL}/images/warning.gif"/>
      <div class="info autop">
        <@s.text name="manage.resource.create.forbidden"/>
      </div>
    </#if>

  </div>

</@s.form>
