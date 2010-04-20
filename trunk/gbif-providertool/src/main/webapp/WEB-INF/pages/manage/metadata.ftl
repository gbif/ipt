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
  <title><@s.text name="dataResource.metadata"/></title>
  <meta name="resource" content="${resource.title!}"/>
  <meta name="menu" content="ManagerMenu"/>
  <meta name="submenu" content="manage_resource"/>  
  <meta name="heading" content="<@s.text name='metadata.heading'/>"/> 
  
  <script src="http://www.google.com/jsapi?key=ABQIAAAAQmTfPsuZgXDEr012HM6trBT2yXp_ZAY8_ufC3CFXhHIE1NvwkxQTBMMPM0apn-CWBZ8nUq7oUL6nMQ" type="text/javascript"></script>
  
  <script type="text/javascript" src="<@s.url value='/scripts/metadata.js'/>"></script>  
  <script type="text/javascript" src="http://jquery-domec.googlecode.com/svn/trunk/jquery.domec.js"></script>
  <script src="http://code.jquery.com/jquery-latest.js"></script>
  <script type="text/javascript" src="<@s.url value='/scripts/jquery.autocomplete.min.js'/>"></script>
  <link rel="stylesheet" type="text/css" href="<@s.url value='/scripts/jquery.autocomplete.css'/>" />
  <script type="text/javascript" src="http://jquery-dynamic-form.googlecode.com/svn/trunk/jquery-dynamic-form.js"></script>

  <script>
    google.load("jqueryui", "1.8.0");
    $(function() {
        $("#accordion").accordion();
    });

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
    
    <#-- UI Services. -->
    var registryUrl = "<@s.url value='/ajax/proxy.do?uri=${registryOrgUrl}/'/>";
    var registryService = new RegistryServiceAsync(registryUrl);  
    
    <#-- UI Widgets. -->
    var orgTitleDiv = new Widget('orgTitleDiv');
    var orgTitleLoadingDiv = new Widget('orgTitleLoadingDiv');
    var orgChangeLinkDiv = new Widget('orgChangeLinkDiv');
    var orgUuidDiv = new Widget('orgUuidDiv');
    var orgPasswordDiv = new Widget('orgPasswordDiv');
    var orgResendPasswordDiv = new Widget('orgResendPasswordDiv');
    var orgTitleTextBox = new TextBox('orgTitleTextBox');
    var orgUuidTextBox = new TextBox('orgUuidTextBox');
    var orgPasswordTextBox = new TextBox('orgPasswordTextBox');
    
    <#--
      - Configures the organisation title textbox to support autocomplete selections
      - of organisation titles. If a title is selected, it loads the organisation
      - from the registry and updates the UUID textbox with the organisation key.
      - 
      - @param orgs collection of JSON encoded organisations
      -->
    function configOrgTitle(orgs) {    
      selectionSuccess = function(selection) {    
        loadSuccess = function(org) {
          orgTitleTextBox.setValue(org.name);      
          orgUuidTextBox.setValue(org.key);
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
      orgTitleTextBox.setAutoComplete(orgs, selectCb);
      orgTitleTextBox.setReadOnly(false);
    }
      
    <#--
      - Handles the change organisation click event by showing the organisation
      - UUID and password textboxes in the UI. Loads all organisations from the 
      - registry and configures the organisation title textbox to support 
      - autocomplete selections.
      -->
    function handleChangeOrgClick() {
      orgTitleTextBox.setReadOnly(true);    
      orgTitleLoadingDiv.setVisible(true);
      orgUuidDiv.setVisible(true);
      orgPasswordDiv.setVisible(true);
      orgResendPasswordDiv.setVisible(true);
      orgChangeLinkDiv.setVisible(false);
        
      loadOrgsSuccess = function(orgs) {
        orgTitleLoadingDiv.setVisible(false);      
        configOrgTitle(orgs);      
      };       
      loadOrgsFailure = function(error) {
        handleError(error);
        orgTitleLoadingDiv.setVisible(false);
        orgTitleTextBox.setVisible(false);        
      };    
      loadCb = new AsyncCallback(loadOrgsSuccess, loadOrgsFailure);
      registryService.loadAllOrgsAsync(loadCb);
    } 
      
    <#-- 
      - Initializes widget state after the page renders. 
      -->
    $(document).ready(function() {  
      <#if config.isOrgRegistered()>
        orgTitleTextBox.setValue("${(registryOrgTitle)!}");
        orgUuidTextBox.setValue("${(cfg.cfg.org.uuidID)!}");
        orgPasswordTextBox.setValue("${(cfg.cfg.orgPassword)!}");
      </#if>
      if (orgTitleTextBox.getValue() == "") {
        orgUuidTextBox.setValue("");    
        orgPasswordTextBox.setValue("");    
      } 
      orgTitleTextBox.setReadOnly(true);
      orgTitleLoadingDiv.setVisible(false);
      orgUuidDiv.setVisible(false);
      orgPasswordDiv.setVisible(false);
      orgResendPasswordDiv.setVisible(false);
    });
    
    <#--
      - Enables dynamic form element creation for:
      - 
      - Creators
      - Metadata Providers
      - Associated Parties
      -->
    $(document).ready(function() {   
      $("#associatedPartyDiv").dynamicForm("#plus", "#minus", {limit:5});
    });
    
    
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
    
      
<p class="explMt"><@s.text name='metadata.describe'/></p>
                 
<@s.form id="resourceForm" 
  action="saveResource" 
  enctype="multipart/form-data" 
  method="post">
  
  <fieldset>
    <@s.hidden name="resourceId" value="${(resource.id)!}"/>
    <@s.hidden name="resourceType" value="${(resourceType)!}"/>
    <@s.hidden name="guid" value="${(resource.guid)!}"/>    
    
    <div id="accordion">
    
        <h4><a href="#">About the Resource</a></h4>
        <div>
          <div>
            <div class="leftxhalf">
                <!-- Resource title textbox. -->
                <@s.textfield id="resourceTitleTextBox" 
                    key="resource.title"                         
                    required="true" 
                    cssClass="text xhalf"/>
            </div>
            <div class="leftxhalf">            
                <!-- Resource abstract textbox. -->
                <@s.textfield id="abstractTextBox"
                    key="resource.abstract"          
                    value="${(resource.description)!}"               
                    required="true" 
                    cssClass="text xhalf"/>
            </div>
          </div>
          <div class="newline">
            <div class="leftxhalf">
                <!-- Resource type selectbox. -->
                <@s.select id="resourceTypeSelectBox"
                     key="resource.type" 
                     list="resourceTypeMap" 
                     required="true" 
                     cssClass="text medium"/>

            </div>
            <div class="leftxhalf">
                <!-- Resource logo file box. -->
                <@s.file id="selectLogoFile"
                    key="resource.selectLogoFile" 
                    name="file"                    
                    cssClass="text file" 
                    required="false" />
            </div>
          </div>
          <div class="newline">            
            <div id="orgTitleDiv">             
                <!-- Resource organisation title textbox. -->
                <@s.textfield id="orgTitleTextBox"
                    key="resource.orgTitle"                         
                    required="true" 
                    readonly="true" 
                    cssClass="text large-foo"/>
            </div>
            <div id="orgTitleLoadingDiv"> 
                <!-- Loader animated GIF. -->
                <img src="<@s.url value="/images/ajax-loader.gif"/>"/>
            </div>
            <div id="orgChangeLinkDiv"> 
                <!-- Link for changing organisation. -->
                <a id="changeOrgLink" 
                    onclick="handleChangeOrgClick(); return false;"  
                    class="desc" href="">Change organisation
                </a>
            </div> 
            <div id="orgUuidDiv"> 
                <!-- Resource organisation UUID textbox. -->
                <@s.textfield id="orgUuidTextBox" 
                    key="resource.orgUuid" 
                    value="${(resource.orgUuid)!}" 
                    required="true" 
                    readonly="true" 
                    cssClass="text large organisationKey"/>
            </div>
            <div id="orgPasswordDiv">    
                <!-- Resource organisation password textbox. -->   
                <@s.textfield id="orgPasswordTextBox"
                    key="resource.orgPassword"
                    value="${(resource.orgPassword)!}" 
                    required="true" 
                    cssClass="text medium"/>
            </div>
            <div  id="orgResendPasswordDiv">                        
                <a id="btnResend" target="_blank" href="#">
                    <!-- Resource organisation password reset Link. -->
                    <@s.text name='configorg.password.resend'/>
                </a>
            </div>            
          </div>
          <div class="newline>
             <!-- Resource description textarea. -->
             <@s.textarea id="descriptionTextBox"
                key="resource.description" 
                cssClass="text xlarge"/>
          </div>                    
        </div>
        
    <h4><a href="#">Resource Creator</a></h4> 
    <div>
    <div class="newline">
        <div class="leftxhalf">
            <@s.textfield key="eml.resourceCreator.firstName" 
            label="%{getText('agent.firstName')}" required="true" 
            cssClass="text xhalf" />
        </div>
        <div class="leftxhalf">
            <@s.textfield key="eml.resourceCreator.lastName" 
            label="%{getText('agent.lastName')}" required="true" 
            cssClass="text xhalf" />
        </div>
    </div>
    <div>
        <div class="leftxhalf">
            <@s.textfield key="eml.resourceCreator.organisation" 
            label="%{getText('agent.organisation')}" required="false" 
            cssClass="text xhalf"/>
        </div>
        <div class="leftxhalf">
            <@s.textfield key="eml.resourceCreator.position" 
            label="%{getText('agent.position')}" required="false" 
            cssClass="text xhalf"/>
        </div>
    </div>
    <div>
        <div class="leftxhalf">
            <@s.textfield key="eml.resourceCreator.phone" 
            label="%{getText('agent.phone')}" required="false" 
            cssClass="text xhalf"/>
        </div>
        <div class="leftxhalf">
            <@s.textfield key="eml.resourceCreator.email" 
            label="%{getText('agent.email')}" required="true" 
            cssClass="text xhalf"/>
        </div>
    </div>
    <div>
    <div>
        <@s.textfield key="eml.resourceCreator.homepage" 
        label="%{getText('agent.homepage')}" required="false" 
        cssClass="text xlarge"/>
    </div>
    <div>   
        <@s.textfield key="eml.resourceCreator.address.address" 
        label="%{getText('agent.address.address')}" required="false" 
        cssClass="text xlarge"/>
    </div>  
    <div>
        <div class="leftMedium">
            <@s.textfield key="eml.resourceCreator.address.postalCode" 
            label="%{getText('agent.address.postalCode')}" required="false" 
            cssClass="text medium"/>
        </div>
        <div class="leftLarge">
            <@s.textfield key="eml.resourceCreator.address.city" 
            label="%{getText('agent.address.city')}" required="false" 
            cssClass="text large"/>
        </div>
    </div>
    <div>
        <div class="leftxhalf">
            <@s.textfield key="eml.resourceCreator.address.province" 
            label="%{getText('agent.address.province')}" required="false" 
            cssClass="text xhalf"/>
        </div>
        <div class="leftxhalf">
            <@s.select id="countrySelect" 
            key="eml.resourceCreator.address.country" 
            list="{'${(eml.getResourceCreator().address.country)!}'}" 
            required="true" cssClass="text xhalf"/>
        </div>
    </div>
    <div>
        <!-- Associated party role select box. -->
        <@s.select id="associatedPartyRoleSelectBox"
            key="eml.associatedParties.roleName"           
            list="agentRoleMap.entrySet()"
            value="role.name()"
            listKey="key"
            listValue="value"  
            required="true"                              
            cssClass="text medium"/>
    </div>
    <div class="breakRight"></div>
    </div>
    </div>
    
    <h4><a href="#">Metadata Provider</a></h4> 
    <div>
    <div class="newline">
        <div class="leftxhalf">
            <@s.textfield key="eml.metadataProvider.firstName" 
            label="%{getText('agent.firstName')}" required="true" 
            cssClass="text xhalf" />
        </div>
        <div class="leftxhalf">
            <@s.textfield key="eml.metadataProvider.lastName" 
            label="%{getText('agent.lastName')}" required="true" 
            cssClass="text xhalf" />
        </div>
    </div>
    <div>
        <div class="leftxhalf">
            <@s.textfield key="eml.metadataProvider.organisation" 
            label="%{getText('agent.organisation')}" required="false" 
            cssClass="text xhalf"/>
        </div>
        <div class="leftxhalf">
            <@s.textfield key="eml.metadataProvider.position" 
            label="%{getText('agent.position')}" required="false" 
            cssClass="text xhalf"/>
        </div>
    </div>
    <div>
        <div class="leftxhalf">
            <@s.textfield key="eml.metadataProvider.phone" 
            label="%{getText('agent.phone')}" required="false" 
            cssClass="text xhalf"/>
        </div>
        <div class="leftxhalf">
            <@s.textfield key="eml.metadataProvider.email" 
            label="%{getText('agent.email')}" required="true" 
            cssClass="text xhalf"/>
        </div>
    </div>
    <div>
    <div>
        <@s.textfield key="eml.metadataProvider.homepage" 
        label="%{getText('agent.homepage')}" required="false" 
        cssClass="text xlarge"/>
    </div>
    <div>   
        <@s.textfield key="eml.metadataProvider.address.address" 
        label="%{getText('agent.address.address')}" required="false" 
        cssClass="text xlarge"/>
    </div>  
    <div>
        <div class="leftMedium">
            <@s.textfield key="eml.metadataProvider.address.postalCode" 
            label="%{getText('agent.address.postalCode')}" required="false" 
            cssClass="text medium"/>
        </div>
        <div class="leftLarge">
            <@s.textfield key="eml.metadataProvider.address.city" 
            label="%{getText('agent.address.city')}" required="false" 
            cssClass="text large"/>
        </div>
    </div>
    <div>
        <div class="leftxhalf">
            <@s.textfield key="eml.metadataProvider.address.province" 
            label="%{getText('agent.address.province')}" required="false" 
            cssClass="text xhalf"/>
        </div>
        <div class="leftxhalf">
            <@s.select id="countrySelect" 
            key="eml.metadataProvider.address.country" 
            list="{'${(eml.getResourceCreator().address.country)!}'}" 
            required="true" cssClass="text xhalf"/>
        </div>
    </div>
    <div>
        <!-- Associated party role select box. -->
        <@s.select id="associatedPartyRoleSelectBox"
            key="eml.associatedParties.roleName"           
            list="agentRoleMap.entrySet()"
            value="role.name()"
            listKey="key"
            listValue="value"  
            required="true"                              
            cssClass="text medium"/>
    </div>
    <div class="breakRight"></div>
    </div>
    </div>
    
    <h4><a href="#">Associated Parites</a></h4>
    <div>
        <#if (eml.associatedParties ? size > 0)>
            <@s.iterator value="eml.associatedParties" status="agent">
                <div id="associatedPartyDiv">
                
                    <div class="newline">
                        <div class="leftxhalf">
                            <@s.textfield key="eml.resourceCreator.firstName" 
                                label="%{getText('agent.firstName')}" 
                                required="true" cssClass="text xhalf" />
                        </div>
                        <div class="leftxhalf">
                            <@s.textfield key="eml.resourceCreator.lastName" 
                                label="%{getText('agent.lastName')}" 
                                required="true" cssClass="text xhalf" />
                        </div>
                    </div>
                
                    <div>
                        <div class="leftxhalf">
                            <@s.textfield key="eml.resourceCreator.organisation" 
                                label="%{getText('agent.organisation')}" 
                                required="false" cssClass="text xhalf"/>
                        </div>
                        <div class="leftxhalf">
                            <@s.textfield key="eml.resourceCreator.position" 
                                label="%{getText('agent.position')}" 
                                required="false" cssClass="text xhalf"/>
                        </div>
                    </div>
                
                    <div>
                        <div class="leftxhalf">
                            <@s.textfield key="eml.resourceCreator.phone" 
                                label="%{getText('agent.phone')}" 
                                required="false" cssClass="text xhalf"/>
                        </div>
                        <div class="leftxhalf">
                            <@s.textfield key="eml.resourceCreator.email" 
                                label="%{getText('agent.email')}" required="true" 
                                cssClass="text xhalf"/>
                        </div>
                    </div>
                    
                    <div>
                        <div>
                            <@s.textfield key="eml.resourceCreator.homepage" 
                                label="%{getText('agent.homepage')}" 
                                required="false" cssClass="text xlarge"/>
                        </div>
                        <div>   
                            <@s.textfield key="eml.resourceCreator.address.address" 
                                label="%{getText('agent.address.address')}" 
                                required="false" cssClass="text xlarge"/>
                        </div>  
                        <div>
                            <div class="leftMedium">
                                <@s.textfield key="eml.resourceCreator.address.postalCode" 
                                    label="%{getText('agent.address.postalCode')}" 
                                    required="false" cssClass="text medium"/>
                            </div>
                            <div class="leftLarge">
                                <@s.textfield key="eml.resourceCreator.address.city" 
                                    label="%{getText('agent.address.city')}" 
                                    required="false" cssClass="text large"/>
                            </div>
                        </div>
                        <div>
                            <div class="leftxhalf">
                                <@s.textfield key="eml.resourceCreator.address.province" 
                                    label="%{getText('agent.address.province')}" 
                                    required="false" cssClass="text xhalf"/>
                            </div>
                            <div class="leftxhalf">
                                <@s.select id="countrySelect" 
                                    key="eml.resourceCreator.address.country" 
                                    list="{'${(eml.getResourceCreator().address.country)!}'}" 
                                    required="true" cssClass="text xhalf"/>
                            </div>
                        </div>
                    </div>
                    <div>
                        <!-- Associated party role select box. -->
                        <@s.select id="associatedPartyRoleSelectBox"
                            key="eml.associatedParties.roleName"           
                            list="agentRoleMap.entrySet()"
                            value="role.name()"
                            listKey="key"
                            listValue="value"  
                            required="true"                              
                            cssClass="text medium"/>
                    </div>
                    <#if agent.last == true>
                    <div>
                        <span>
                            <a id="minus" href="" onclick="return false;">[-]</a> 
                            <a id="plus" href="" onclick="return false;">[+]</a>
                        </span>
                    </div>
                    </#if>     
            </@s.iterator>
            <div class="breakRight"></div> 
            <div class="newline"></div>                                
        <#else>      
          <div id="associatedPartyDiv">
                    <div class="newline">
                    <p>
                        <div class="leftxhalf">
                            <@s.textfield key="eml.resourceCreator.firstName" 
                                label="%{getText('agent.firstName')}" 
                                required="true" cssClass="text xhalf" />
                        </div>
                        <div class="leftxhalf">
                            <@s.textfield key="eml.resourceCreator.lastName" 
                                label="%{getText('agent.lastName')}" 
                                required="true" cssClass="text xhalf" />
                        </div>
                    </div>
                
                    <div>
                        <div class="leftxhalf">
                            <@s.textfield key="eml.resourceCreator.organisation" 
                                label="%{getText('agent.organisation')}" 
                                required="false" cssClass="text xhalf"/>
                        </div>
                        <div class="leftxhalf">
                            <@s.textfield key="eml.resourceCreator.position" 
                                label="%{getText('agent.position')}" 
                                required="false" cssClass="text xhalf"/>
                        </div>
                    </div>
                
                    <div>
                        <div class="leftxhalf">
                            <@s.textfield key="eml.resourceCreator.phone" 
                                label="%{getText('agent.phone')}" 
                                required="false" cssClass="text xhalf"/>
                        </div>
                        <div class="leftxhalf">
                            <@s.textfield key="eml.resourceCreator.email" 
                                label="%{getText('agent.email')}" required="true" 
                                cssClass="text xhalf"/>
                        </div>
                    </div>
                    
                    <div>
                        <div>
                            <@s.textfield key="eml.resourceCreator.homepage" 
                                label="%{getText('agent.homepage')}" 
                                required="false" cssClass="text xlarge"/>
                        </div>
                        <div>   
                            <@s.textfield key="eml.resourceCreator.address.address" 
                                label="%{getText('agent.address.address')}" 
                                required="false" cssClass="text xlarge"/>
                        </div>  
                        <div>
                            <div class="leftMedium">
                                <@s.textfield key="eml.resourceCreator.address.postalCode" 
                                    label="%{getText('agent.address.postalCode')}" 
                                    required="false" cssClass="text medium"/>
                            </div>
                            <div class="leftLarge">
                                <@s.textfield key="eml.resourceCreator.address.city" 
                                    label="%{getText('agent.address.city')}" 
                                    required="false" cssClass="text large"/>
                            </div>
                        </div>
                        <div>
                            <div class="leftxhalf">
                                <@s.textfield key="eml.resourceCreator.address.province" 
                                    label="%{getText('agent.address.province')}" 
                                    required="false" cssClass="text xhalf"/>
                            </div>
                            <div class="leftxhalf">
                                <@s.select id="countrySelect" 
                                    key="eml.resourceCreator.address.country" 
                                    list="{'${(eml.getResourceCreator().address.country)!}'}" 
                                    required="true" cssClass="text xhalf"/>
                            </div>
                        </div>
                    </div>
                </div>
                 <div>
                        <!-- Associated party role select box. -->
                        <@s.select id="associatedPartyRoleSelectBox"
                            key="eml.associatedParties.roleName"           
                            list="agentRoleMap.entrySet()"
                            value="role.name()"
                            listKey="key"
                            listValue="value"  
                            required="true"                              
                            cssClass="text medium"/>
                </div>
                <div class="breakRight"></div>                    
                <div>
                <span>
                    <a id="minus" href="" onclick="return false;">[-]</a> 
                    <a id="plus" href="" onclick="return false;">[+]</a>
                </span>
           </div>
           <div class="breakRight"></div>                    
        </#if>       
        <div class="breakRight"></div>                    
    </div>    
</div>


  <#if resource.modified??>
    <div class="linebreak">
    <div class="modifiedDate">
      <@s.text name="dataResource.lastModified"/> 
        ${resource.modified?datetime?string} 
        <#if resource.modifier??>
          by ${resource.modifier.getFullName()}
        </#if>
    </div>
  </#if> 
  <div class="newline">
   <@s.submit id="saveButton"
                     key="button.save" 
                     cssClass="button" 
                     name="save"                      
                     theme="simple"/>
          <@s.submit id="cancelButton"
                     key="button.cancel" 
                     cssClass="button" 
                     method="cancel"                      
                     theme="simple"/>
          <#if resource.id??>
            <@s.submit id="deleteButton"
                       key="button.delete" 
                       cssClass="button" 
                       method="delete"                        
                       onclick="return confirmDelete('resource')" 
                       theme="simple"/>
          </#if>
   
</fieldset>  
</@s.form>