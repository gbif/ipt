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
  <title><@s.text name="metadata.heading.keywordSet"/></title>
  <meta name="resource" content="${resource.title!}"/>
  <meta name="menu" content="ManagerMenu"/>
  <meta name="submenu" content="manage_resource"/>
  <meta name="heading" content="<@s.text name='metadata.heading.keywordSet'/>"/>  

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

  <script language="Javascript"
    type="text/javascript">

//<![CDATA[  

    google.load("jquery", "1.4.2");

    function HideKeywordSetClone() {
      $('#removeLink').hide();
      $('#cloneKeywordSet').hide();
    }
  
    function GetKeywordSets() {
      var keywordSets = new Array();
      <#if (eml.keywords ? size > 0)>
        <@s.iterator value="eml.keywords" status="stat">
          keywordSets[${stat.index}] = new KeywordSet()
            .keywordThesaurus("<@s.property value="keywordThesaurus"/>")
            .keywordsString("<@s.property value="keywordsString"/>");
        </@s.iterator>
      </#if>
      return keywordSets;
    }

    function OnLoad() {  
      HideKeywordSetClone();
      var keywordSetPanel = new KeywordSetPanel();
      var keywordSetWidget;
      $('#plus').click(function() {
        keywordSetPanel.add(new KeywordSetWidget());
      });
      var keywordSets = GetKeywordSets();
      for (keywordSet in keywordSets) {
        keywordSetWidget = new KeywordSetWidget(keywordSets[keywordSet]);
        keywordSetPanel.add(keywordSetWidget);
      }
    }

    google.setOnLoadCallback(OnLoad);
    
//]]>

  </script>

</head>

<div class="break10"></div>
<p class="explMt"><@s.text name='metadata.description.keywordSet'/></p>
<@s.form id="emlForm" action="keywords" enctype="multipart/form-data" method="post">

<fieldset>
  <@s.hidden name="resourceId" value="${(resource.id)!}"/>
  <@s.hidden name="resourceType" value="${(resourceType)!}"/>
  <@s.hidden name="guid" value="${(resource.guid)!}"/>
  <@s.hidden name="nextPage" value="additionalMetadata"/>
  <@s.hidden name="method" value="keywordSets"/>
  <div id="keywordSetPanel" class="newline">
    <!-- The cloneKeywordSet DIV is not attached to the DOM. It's used as a template
       for cloning KeywordSet UI widgets. 
    -->
    <div id="cloneKeywordSet">
      <div id="separator" class="horizontal_dotted_line_large_foo"></div>
      <div class="newline"></div>
      <div class="right">
        <a id="removeLink" href="" onclick="return false;">[ <@s.text name='metadata.removethis'/> <@s.text name='eml.keywordSet'/> ]</a>
      </div>
      <div class="newline"></div>

      <div id="KeywordSetDiv">
        <div id="keywordThesaurusDiv">
          <@s.textfield id="keywordThesaurus" key="" 
          label="%{getText('keywordSet.keywordThesaurus')}" required="false" cssClass="text xlarge slim"/>
        </div>
        <div id="keywordsStringDiv">
          <@s.textarea id="keywordsString" key="" 
          label="%{getText('keywordSet.keywordsString')}" required="false" cssClass="text text xlarge slim"/>
        </div>
      </div>
      <div class="newline"></div>
      <div class="newline"></div>
      <div class="newline"></div>
      <div class="newline"></div>
    </div>
  </div>

  <div class="left">
    <a id="plus" href="" onclick="return false;"><@s.text name='metadata.addnew'/> <@s.text name='eml.keywordSet'/></a>
  </div>
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

</@s.form>