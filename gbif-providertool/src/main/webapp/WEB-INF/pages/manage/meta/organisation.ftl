<!--
/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
-->

<head>
<title><@s.text name="metadata.heading.organisation"/></title>
<meta name="resource" content="${resource.title!}"/>
<meta name="menu" content="ManagerMenu"/>
<meta name="submenu" content="manage_resource"/>  
<meta name="heading" content="<@s.text name='metadata.heading.organisation'/>"/> 
  
<script
  src="http://www.google.com/jsapi?key=ABQIAAAAQmTfPsuZgXDEr012HM6trBT2yXp_ZAY8_ufC3CFXhHIE1NvwkxQTBMMPM0apn-CWBZ8nUq7oUL6nMQ"
  type="text/javascript">
</script>

<script src="http://code.jquery.com/jquery-latest.js"></script>
  
<script 
  src="<@s.url value='/scripts/dto.js'/>"
  type="text/javascript">
</script>

<script 
  src="<@s.url value='/scripts/metadata.js'/>"
  type="text/javascript">
</script>

<script 
  src="http://view.jquery.com/trunk/plugins/autocomplete/jquery.autocomplete.js"
  type="text/javascript">   
</script>

<script 
  src="http://view.jquery.com/trunk/plugins/autocomplete/jquery.autocomplete.js"
  type="text/javascript"> 
</script>

<script 
  src="http://view.jquery.com/trunk/plugins/autocomplete/jquery.autocomplete.js"
  type="text/javascript"> 
</script>

<link rel="stylesheet" type="text/css" href="<@s.url value='/scripts/jquery.autocomplete.css'/>" />

<script
  language="Javascript"
  type="text/javascript">

//<![CDATA[  

//google.load("jquery", "1.4.2");

var registryUrl = "<@s.url value='/ajax/proxy.do?uri=${registryOrgUrl}/'/>";
var registryService = new RegistryServiceAsync(registryUrl);  
var titleWidget = new Widget('titleWidget');
var titleTextBox = new TextBox('titleTextBox');
var loadingImage = new Widget('loadingImage');
var changeLinkWidget = new Widget('changeLinkWidget');
var changeLink = new Widget('changeLink');
var uuidWidget = new Widget('uuidWidget');
var uuidTextBox = new TextBox('uuidTextBox');
var passwordWidget = new Widget('passwordWidget');
var resendPasswordWidget = new Widget('resendPasswordWidget');


var orgPasswordTextBox = new TextBox('orgPasswordTextBox');  

/**
 * Configures the organization title textbox to support autocomplete selections
 * of organization titles. If a title is selected, it loads the organization
 * from the registry and updates the UUID textbox with the organization key.
 * 
 * @param orgs
 *          collection of JSON encoded organizations
 * @return
 */
function configAutoComplete(orgs) {    
  selectionSuccess = function(selection) {    
    loadSuccess = function(org) {
      titleTextBox.setValue(org.name);            
      uuidTextBox.setValue(org.key);
    }
    loadFailure = function(error) {
      handleError(error);
    }
    loadCb = new AsyncCallback(loadSuccess, loadFailure);
    orgKey = selection.key;
    registryService.loadOrgAsync(orgKey, loadCb);  
  } 
     
  selectionFailure = function(error) {
    handleError(error);
  }
  
  selectCb = new AsyncCallback(selectionSuccess, selectionFailure);    
  titleTextBox.setAutoComplete(orgs, selectCb);
  titleTextBox.setReadOnly(false);
}
  


/**
 * Handles the change organization click event by showing the organization UUID
 * and password textboxes in the UI. Loads all organizations from the registry
 * and configures the organization title textbox to support autocomplete
 * selections.
 * 
 * @return
 */
function changeOrg() {
  titleTextBox.setReadOnly(true);    
  loadingImage.setVisible(true);
  
  uuidWidget.setVisible(true);
  passwordWidget.setVisible(true);
  resendPasswordWidget.setVisible(true);
  changeLink.setVisible(false);
    
  loadOrgsSuccess = function(orgs) {
    loadingImage.setVisible(false);      
    configAutoComplete(orgs);      
  };       
 
  loadOrgsFailure = function(error) {
    handleError(error);
    loadingImage.setVisible(false);
    titleTextBox.setVisible(false);        
  };    
 
  loadCb = new AsyncCallback(loadOrgsSuccess, loadOrgsFailure);
  registryService.loadAllOrgsAsync(loadCb);
  return false;
} 



function OnModuleLoaded() {  
  <#if config.isOrgRegistered()>
    titleTextBox.setValue("${(registryOrgTitle)!}");
    uuidTextBox.setValue("${(cfg.cfg.org.uuidID)!}");
    orgPasswordTextBox.setValue("${(cfg.cfg.orgPassword)!}");
  </#if>
  if (titleTextBox.getValue() == "") {
    uuidTextBox.setValue("");    
    orgPasswordTextBox.setValue("");    
  } 
  titleTextBox.setReadOnly(true);
  loadingImage.setVisible(false);
  uuidWidget.setVisible(false);
  passwordWidget.setVisible(false);
  resendPasswordWidget.setVisible(false);
  
 changeLink.addClickHandler(changeOrg);
}

google.setOnLoadCallback(OnModuleLoaded);
  
//]]>
</script>
  
<style>
  #btnResend {
    position: relative;
    left: 4px;
  #nodeLoading{
    position: relative;
    bottom: 26px;
    left: 12px;
  }
</style>

</head>
<@s.form id="emlForm" action="organisation" enctype="multipart/form-data" 
  method="post"> 
<fieldset>
<@s.hidden name="resourceId" value="${(resource.id)!}"/>
<@s.hidden name="resourceType" value="${(resourceType)!}"/>
<@s.hidden name="guid" value="${(resource.guid)!}"/>    
<@s.hidden name="nextPage" value="AssociatedParties"/>
<@s.hidden name="method" value="organisation"/>    

<div class="newline"></div>
<div id="titleWidget" class="leftxhalf">
  <@s.textfield id="titleTextBox" key="resource.orgTitle" required="true" 
    readonly="true" cssClass="text xhalf"/>
  <span id="loadingImage" class="newline">
    <img src='<@s.url value="/images/ajax-loader.gif"/>'/>
  </span>
</div>
<div id="uuidWidget" class="leftxhalf">
  <@s.textfield id="uuidTextBox" key="resource.orgUuid" 
    value="${(resource.orgUuid)!}" required="true" readonly="true" 
    cssClass="text xhalf"/>
</div>
<div class="newline"></div>
<div id="passwordWidget" class="leftxhalf">    
  <@s.textfield id="orgPasswordTextBox" key="resource.orgPassword"
    value="${(resource.orgPassword)!}" required="true" cssClass="text xhalf"/>
</div>
<div class="newline"></div>
<div id="resendPasswordWidget" class="leftxhalf">                        
  <a id="btnResend" target="_blank" href="#">
    <@s.text name='configorg.password.resend'/></a>
</div>
<div id="changeLinkWidget" class="left">
  <a id="changeLink" href="">Change this organization</a>
</div>

<div class="newline"></div>
<div class="newline"></div>
<div class="newline"></div>
<div class="newline"></div>
<div class="newline"></div>
<div class="newline"></div>
<div class="breakLeftButtons">
  <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
  <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
</div>
</fieldset>
<div class="newline"></div>
<div class="newline"></div>
<div class="newline"></div>
<div class="newline"></div>
<div class="newline"></div>
<#if resource.modified??>
  <div id="separator" class="horizontal_dotted_line_large_foo"></div>
  <div class="newline"></div>
  <div class="left">
    <@s.text name="dataResource.lastModified"/> 
      ${resource.modified?datetime?string} 
  <#if resource.modifier??>
    by ${resource.modifier.getFullName()}
  </#if>
  </div>
</#if>  
</@s.form>