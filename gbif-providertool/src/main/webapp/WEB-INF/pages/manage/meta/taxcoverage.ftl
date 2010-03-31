<head>
    <title>EML - <@s.text name="eml.taxonomicCoverage"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
    <meta name="heading" content="<@s.text name='eml.taxonomicMetadata'/>"/>    
     <script src="http://code.jquery.com/jquery-latest.js"></script>
    <script type="text/javascript" src="http://jquery-dynamic-form.googlecode.com/svn/trunk/jquery-dynamic-form.js"></script>   
    <script>
     $(document).ready(function() {   
      $("#taxonCoverageDiv").dynamicForm("#plus", "#minus", {limit:5});
     });    
    $(document).ready(function(){
        var url = '<@s.url value="/ajax/vocSelect.html"/>';
        // load ranks
        var params = {uri:"${rankVocUri}",alpha:false,empty:true};
        var id = "rankSelect";
        ajaxSelectVocabulary(url, id, params);
    });
    
    </script>
</head>

<div class="break10"></div>
<@s.form id="emlForm" action="taxcoverage" method="post" validate="false">
<fieldset>
    <@s.hidden name="resourceId" value="${resourceId?c}"/>
    <@s.hidden name="nextPage" value="tempcoverage"/>
    
<fieldset style="padding-top:10px;">
    <legend><!--<@s.text name="eml.lowestCommonTaxon"/>--></legend>
    <div id="taxonCoverageDiv">
        <div class="text medium">
            <@s.textfield key="eml.description"  required="true" cssClass="text large" />
        </div>
        <div class="text medium">
            <@s.textfield key="eml.scientificName"  required="false" cssClass="text medium" />
        </div>
        <div class="text medium">
            <@s.textfield key="eml.commonName" label="%{getText('taxonKeyword.commonName')}" required="false" cssClass="text medium" />
        </div>  
        <div class="text medium">
            <@s.select id="rankSelect" key="eml.Rank"  list="{'${(eml.getLowestCommonTaxon().rank)!}'}" cssClass="text small"/>
        </div>
        <span>
        <a id="minus" href="" onclick="return false;">[-]</a> 
        <a id="plus" href="" onclick="return false;">[+]</a>
      </span> 
      <div class="horizontal_dotted_line_large_foo"></div>  
    </div>
</fieldset>

    <div class="breakRightButtons">
        <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
      <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
    </div>
</@s.form>
<div class="break20"></div>