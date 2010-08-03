<@s.text name="manage.resource.create.intro"/>

<@s.form cssClass="ftlTopForm" action="create.do" method="post" enctype="multipart/form-data">
  <@input name="shortname" keyBase="resource." errorField="resource.shortname" size=40/>  
  <div>
	<@s.fielderror cssClass="fielderror" fieldName="file"/>
	<label for="file"><@s.text name="manage.resource.create.file"/></label>
    <@s.file name="file" key="manage.resource.create.file" required="false"/>
  </div>  
  
  <div class="buttons">
   	<@s.submit name="create" key="button.create"/>
  </div>	
      
</@s.form>