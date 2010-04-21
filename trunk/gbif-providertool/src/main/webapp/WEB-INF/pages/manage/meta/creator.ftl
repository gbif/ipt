<head>
    <title>EML - <@s.text name="eml.resourceCreator"/></title>
    <meta name="resource" content="{resource.title!}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
    <meta name="heading" content="<@s.text name='eml.resourceCreator'/>"/>    
    <script>
    $(document).ready(function(){
        var url = '<@s.url value="/ajax/vocSelect.html"/>';
        // load language codes
        var params = {uri:"${languageVocUri}",alpha:true,empty:true};
        var id = "languageSelect";
        ajaxSelectVocabulary(url, id, params);
        // load country codes
        params = {uri:"${countryVocUri}",alpha:true,empty:true};
        id = "countrySelect";
        ajaxSelectVocabulary(url, id, params);
    });
    </script>

<script
  src="http://www.google.com/jsapi?key=ABQIAAAAQmTfPsuZgXDEr012HM6trBT2yXp_ZAY8_ufC3CFXhHIE1NvwkxQTBMMPM0apn-CWBZ8nUq7oUL6nMQ"
  type="text/javascript">
</script>

<script 
  src="<@s.url value='/scripts/dto.js'/>"
  type="text/javascript">
</script>

<script 
  src="<@s.url value='/scripts/widgets.js'/>"
  type="text/javascript">
</script>

<script
  language="Javascript"
  type="text/javascript">

//<![CDATA[  

google.load("jquery", "1.4.2");

var countriesSelect = null;

function LoadCountriesAsync(callback) {
  if (countriesSelect != null) {
    callback(countriesSelect);
    return;
  }
  var url = '<@s.url value="/ajax/vocSelect.html"/>';
  params = {uri:"${countryVocUri}",alpha:true,empty:true};
  $.get(url, params, function(data) { 
    countriesSelect = $(data);
    callback(countriesSelect);
  });
}

function OnLoad() {  
  LoadCountriesAsync(function(elem) {
    var e = elem.clone();
    var id = 'creatorCountry';
    var idElem = $('#' + id);
    var name = idElem.attr('name');
    var value = idElem.attr('value');
    e.attr('name', 'eml.resourceCreator.address.country');
    e.attr('value', value)
    e.attr('id', id);
    $('#' + id).replaceWith(e);
    
    e = elem.clone();
    id = 'metadataProviderCountry';
    idElem = $('#' + id);
    name = idElem.attr('name');
    value = idElem.attr('value');
    e.attr('name', 'eml.metadataProvider.address.country');
    e.attr('value', value)
    e.attr('id', id);
    $('#' + id).replaceWith(e);
  });
}

google.setOnLoadCallback(OnLoad);

//]]>

</script>   
</head>

<div class="break10"></div>
<@s.form id="emlForm" action="creator" method="post" validate="false">
<fieldset>
    <div>
        <@s.select id="languageSelect" key="eml.language" list="{'${eml.language!}'}" required="true" cssClass="text medium"/>
    </div>
    <@s.hidden name="resourceId" value="${resourceId?c}"/>
    <@s.hidden name="nextPage" value="associatedParties"/>

<div id="agentPanel" class="newline">
  <div>
    <h2 class="explMt">Resource Creator</h2>
  <div>
  <div id="creator">    
    <div class="newline"></div>
    <div class="leftxhalf">
      <@s.textfield id="firstName" key="eml.resourceCreator.firstName" 
        required="true" cssClass="text xhalf"/>
    </div>
    <div class="leftxhalf">
      <@s.textfield id="lastName" key="eml.resourceCreator.lastName" 
        required="true" cssClass="text xhalf"/>
    </div>
    <div class="newline"></div>
    <div class="leftxhalf">
      <@s.textfield id="position" key="eml.resourceCreator.position"  
        cssClass="text xhalf"/>
    </div>       
    <div class="leftxhalf">
       <@s.select id="role" key="eml.resourceCreator.role"           
         list="agentRoleMap.entrySet()" value="role.name()" listKey="key"
         listValue="value"/>
    </div>
    <div class="newline"></div>
    <div>
      <@s.textfield id="organization" key="eml.resourceCreator.organisation"  
        cssClass="text xlarge"/>
    </div>
    <div class="newline"></div>
    <div class="leftxhalf">
      <@s.textfield id="phone" key="eml.resourceCreator.phone"  
        cssClass="text xhalf"/>
    </div>
    <div class="leftxhalf">
      <@s.textfield id="email" key="eml.resourceCreator.email"  
        required="true" cssClass="text xhalf"/>
    </div>
    <div class="newline"></div>
    <div>
      <@s.textfield id="homepage" key="eml.resourceCreator.homepage"  
        cssClass="text xlarge"/>
    </div>
    <div class="newline"></div>
    <div>
      <@s.textfield id="address" key="eml.resourceCreator.address.address"  
        cssClass="text xlarge"/>
    </div>
    <div class="newline"></div>
    <div class="leftxhalf">
      <@s.textfield id="postalCode" key="eml.resourceCreator.address.postalCode"  
        cssClass="text xhalf"/>
    </div>
    <div class="leftxhalf">
      <@s.textfield id="city" key="eml.resourceCreator.address.city"  
        cssClass="text xhalf"/>
    </div>
    <div class="newline"></div>
    <div class="leftxhalf">
      <@s.textfield id="province" key="eml.resourceCreator.address.province"  
        cssClass="text xhalf"/>
    </div>
    <div class="leftxhalf">
      <@s.select id="creatorCountry" key="" list="eml.resourceCreator.address.country"
        required="true" cssClass="text xhalf"/>
    </div>    
    <div class="newline"></div>
    <div class="newline"></div>
    <div class="newline"></div>
    <div class="newline"></div>
  </div>
  <div>
    <h2 class="explMt">Resource Metadata Provider</h2>
  <div>
  <div id="creator">    
    <div class="newline"></div>
    <div class="leftxhalf">
      <@s.textfield id="firstName" key="eml.metadataProvider.firstName" 
        required="true" cssClass="text xhalf"/>
    </div>
    <div class="leftxhalf">
      <@s.textfield id="lastName" key="eml.metadataProvider.lastName" 
        required="true" cssClass="text xhalf"/>
    </div>
    <div class="newline"></div>
    <div class="leftxhalf">
      <@s.textfield id="position" key="eml.metadataProvider.position"  
        cssClass="text xhalf"/>
    </div>       
    <div class="leftxhalf">
       <@s.select id="role" key="eml.metadataProvider.role"           
         list="agentRoleMap.entrySet()" value="role.name()" listKey="key"
         listValue="value"/>
    </div>
    <div class="newline"></div>
    <div>
      <@s.textfield id="organization" key="eml.metadataProvider.organisation"  
        cssClass="text xlarge"/>
    </div>
    <div class="newline"></div>
    <div class="leftxhalf">
      <@s.textfield id="phone" key="eml.metadataProvider.phone"  
        cssClass="text xhalf"/>
    </div>
    <div class="leftxhalf">
      <@s.textfield id="email" key="eml.metadataProvider.email"  
        required="true" cssClass="text xhalf"/>
    </div>
    <div class="newline"></div>
    <div>
      <@s.textfield id="homepage" key="eml.metadataProvider.homepage"  
        cssClass="text xlarge"/>
    </div>
    <div class="newline"></div>
    <div>
      <@s.textfield id="address" key="eml.metadataProvider.address.address"  
        cssClass="text xlarge"/>
    </div>
    <div class="newline"></div>
    <div class="leftxhalf">
      <@s.textfield id="postalCode" key="eml.metadataProvider.address.postalCode"  
        cssClass="text xhalf"/>
    </div>
    <div class="leftxhalf">
      <@s.textfield id="city" key="eml.metadataProvider.address.city"  
        cssClass="text xhalf"/>
    </div>
    <div class="newline"></div>
    <div class="leftxhalf">
      <@s.textfield id="province" key="eml.metadataProvider.address.province"  
        cssClass="text xhalf"/>
    </div>
    <div class="leftxhalf">
      <@s.select id="metadataProviderCountry" key="" list="eml.resourceCreator.address.country"
        required="true" cssClass="text xhalf"/>
    </div>    
    <div class="newline"></div>
    <div class="newline"></div>
    <div class="newline"></div>
    <div class="newline"></div>
  </div>
</div>
    <div class="breakRight"></div>
    <div class="breakRightButtons">
        <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
        <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
    </div>
</fieldset>
</@s.form>
