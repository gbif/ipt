<head>
    <title>EML - <@s.text name="eml.methods"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
    <meta name="heading" content="<@s.text name='eml.methods'/>"/>  
    
    <script src="http://code.jquery.com/jquery-latest.js"></script>
    <script type="text/javascript" src="http://jquery-dynamic-form.googlecode.com/svn/trunk/jquery-dynamic-form.js"></script>   
    <script>    
     $(document).ready(function() {   
      $("#methodDiv").dynamicForm("#plus", "#minus", {limit:5});
     });    
    </script>
    
</head>

<div class="break10"></div>
<@s.form id="emlForm" action="methods" method="get" validate="false">
<fieldset>
    <@s.hidden name="resourceId" value="${resourceId}"/>    
    <div id="methodDiv">
      <@s.textarea 
        key="eml.stepDescription" 
        required="false" 
        cssClass="text xlarge slim"/>
      
      <@s.textarea 
        key="eml.studyExtent" 
        required="false" 
        cssClass="text xlarge slim"/>
      
      <@s.textarea 
        key="eml.sampleDescription" 
        required="false" 
        cssClass="text xlarge slim"/>
      
      <@s.textarea 
        key="eml.qualityControl" 
        required="false" 
        cssClass="text xlarge slim"/>
      <span>
        <a id="minus" href="" onclick="return false;">[-]</a> 
        <a id="plus" href="" onclick="return false;">[+]</a>
      </span> 
      <div class="horizontal_dotted_line_large_foo"></div> 
    </div>
</fieldset>
<div class="breakRightButtons">
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
      <@s.submit cssClass="button" key="button.done" name="next" theme="simple"/>
</div>    
</@s.form>