<p><@s.text name="manage.resource.create.intro"/></p>

<@s.form cssClass="topForm half" action="create.do" method="post" enctype="multipart/form-data">
  <@input name="shortname" i18nkey="resource.shortname" errorfield="resource.shortname" size=40/>
  <@select name="resourceType" i18nkey="manage.resource.create.coreType" help="i18n" options=types value="" />
  <div>
	  <@s.fielderror cssClass="fielderror" fieldName="file"/>
	  <label for="file"><@s.text name="manage.resource.create.file"/>: </label>
    <@s.file name="file" key="manage.resource.create.file" />
  </div>

<div id="create-button" class="buttons">
   	<@s.submit cssClass="button" name="create" key="button.create"/>
  </div>	
      
</@s.form>