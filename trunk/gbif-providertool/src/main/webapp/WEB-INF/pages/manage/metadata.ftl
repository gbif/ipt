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
  
  <script type="text/javascript" src="<@s.url value='/scripts/metadata.js'/>"></script>  
  <script type="text/javascript" src="http://jquery-domec.googlecode.com/svn/trunk/jquery.domec.js"></script>
  <script src="http://code.jquery.com/jquery-latest.js"></script>
  <script type="text/javascript" src="<@s.url value='/scripts/jquery.autocomplete.min.js'/>"></script>
  <link rel="stylesheet" type="text/css" href="<@s.url value='/scripts/jquery.autocomplete.css'/>" />
  <script type="text/javascript" src="http://jquery-dynamic-form.googlecode.com/svn/trunk/jquery-dynamic-form.js"></script>

  <script>
    
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
    
    <table>
      <tr>
        <td>
          <!-- Resource logo image. 
          <#if resourceId??>
            <img class="rightf" src="${cfg.getResourceLogoUrl(resourceId)}" />
          </#if>
          -->
        </td>
      </tr>
      <tr>
        <td>
          <!-- Resource title textbox. -->
          <@s.textfield id="resourceTitleTextBox"
                        key="resource.title"                         
                        required="true" 
                        cssClass="text large-foo"/>
        </td>
      </tr>
      <tr>
        <td>
          <!-- Resource abstract textbox. -->
          <@s.textfield id="abstractTextBox"
                        key="resource.abstract"          
                        value="${(resource.description)!}"               
                        required="true" 
                        cssClass="text large-foo"/>
        </td>
      </tr>
      <div id="creatorDiv">
      <tr>        
        <td>        
          <!-- Creator name textbox. -->
          <@s.textfield id="creatorNameTextBox"
                        key="resource.creatorName"          
                        value="${(resourceCreator.name)!}"               
                        required="true"           
                        cssClass="text medium"/>
          <!-- Creator role textbox. -->
          <@s.select id="creatorRoleSelectBox"
                        key="resource.creatorRole"          
                        list="agentRoleMap"             
                        required="true" 
                        cssClass="text medium"/>                        
        </td>
      </tr>
      </div>
      <tr>       
        <td>
          <!-- Metadata provider name textbox. -->
          <@s.textfield id="metadataProviderNameTextBox"
                        key="resource.metadataProviderName"          
                        value="${(metadataProvider.name)!}"               
                        required="true" 
                        cssClass="text medium"/>   
          <!-- Metadata provider role textbox. -->
          <@s.select id="metadataProviderRoleSelectBox"
                        key="resource.metadataProviderRole"          
                        list="agentRoleMap"             
                        required="true" 
                        cssClass="text medium"/>                             
        </td>
      <tr>       
        <td>
        <div id="associatedPartyDiv">
          <!-- Associated party name textbox. -->
          <@s.textfield id="asscociatedPartyNameTextBox"
                        key="resource.associatedPartyName"          
                        value="${(metadataProvider.name)!}"               
                        required="true" 
                        cssClass="text mediumÅ"/>
          <!-- Associated party role select box. -->
          <@s.select id="associatedPartyRoleSelectBox"
                        key="resource.associatedPartyRole"          
                        list="agentRoleMap"             
                        required="true" 
                        cssClass="text medium"/>                        

          <span>
            <a id="minus" href="" onclick="return false;">[-]</a> 
            <a id="plus" href="" onclick="return false;">[+]</a>
          </span>
        </div>
        </td>
      </tr>
      </tr>
      <tr>
        <td>
          <!-- Resource type selectbox. -->
          <@s.select id="resourceTypeSelectBox"
                     key="resource.type" 
                     list="resourceTypeMap" 
                     required="true" 
                     cssClass="text medium"/>
        </td>
      </tr>
      <tr>
        <td>          
          <div id="orgTitleDiv">             
            <!-- Resource organization title textbox. -->
            <@s.textfield id="orgTitleTextBox"
                          key="resource.orgTitle"                         
                          required="true" 
                          readonly="true" 
                          cssClass="text large-foo"/>
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
        </td>  
      </tr>
      <tr>
        <td>          
          <div id="orgChangeLinkDiv"> 
            <!-- Link for changing organization. -->
            <a id="changeOrgLink" 
               onclick="handleChangeOrgClick(); return false;"  
               class="desc" href="">Change organization
            </a>
          </div> 
        </td>
      <tr>
        <td>           
          <div id="orgUuidDiv"> 
            <!-- Resource organization UUID textbox. -->
            <@s.textfield id="orgUuidTextBox" 
                          key="resource.orgUuid" 
                          value="${(resource.orgUuid)!}" 
                          required="true" 
                          readonly="true" 
                          cssClass="text large organisationKey"/>
          </div>
        </td>
      </tr>          
      <tr>
        <td>          
          <div id="orgPasswordDiv">    
            <!-- Resource organization password textbox. -->   
            <@s.textfield id="orgPasswordTextBox"
                          key="resource.orgPassword"
                          value="${(resource.orgPassword)!}" 
                          required="true" 
                          cssClass="text medium"/>
          </div>
        </td>           
      </tr>
      <tr>
        <td padding="8">          
          <div id="orgResendPasswordDiv">                        
            <a id="btnResend" target="_blank" href="#">
              <!-- Resource organization password reset Link. -->
              <@s.text name='configorg.password.resend'/>
            </a>
          </div>
        </td>            
      </tr>    
      <tr>
        <td>
          <!-- Resource contact name textbox. -->
          <@s.textfield id="contactNameTextBox"
                        key="resource.contactName" 
                        required="true" 
                        cssClass="text medium"/>
        </td>
      </tr>
      <tr>
        <td>
          <!-- Resource contact email textbox. -->
          <@s.textfield id="contactEmailTextBox"
                        key="resource.contactEmail" 
                        required="true" 
                        cssClass="text medium"/>
        </td>
      </tr>
      <tr>
      <tr>
        <td>
          <!-- Resource logo file box. -->
          <@s.file id="selectLogoFile"
                   key="resource.selectLogoFile" 
                   name="file"                    
                   cssClass="text file" 
                   required="false" />
        </td>
      </tr>
      <tr>
        <td>
          <!-- Resource description textarea. -->
          <@s.textarea id="descriptionTextBox"
                       key="resource.description" 
                       cssClass="text xlarge"/>
        </td>
      </tr>
      <tr>
        <td>
          <!-- Form buttons. -->
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
        </td>
      </tr>                                      
    </table>                        
  </fieldset>

  <#if resource.modified??>
    <div class="modifiedDate">
      <@s.text name="dataResource.lastModified"/> 
        ${resource.modified?datetime?string} 
        <#if resource.modifier??>
          by ${resource.modifier.getFullName()}
        </#if>
    </div>
  </#if>  
</@s.form>