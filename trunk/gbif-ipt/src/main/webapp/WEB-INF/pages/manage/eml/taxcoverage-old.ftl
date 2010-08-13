<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name='manage.metadata.taxcoverage.title'/></title>
	<#assign sideMenuEml=true />
<#include "/WEB-INF/pages/inc/menu.ftl">

<head>
  <title><@s.text name="metadata.heading.taxcoverages"/></title>
  <!-- meta name="resource" content="${resource.title!}"/ -->
  <meta name="menu" content="ManagerMenu"/>
  <meta name="submenu" content="manage_resource"/>
  <meta name="heading" content="<@s.text name='metadata.heading.taxcoverages'/>"/>    

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

    function HideTaxonomicCoverageClone() {
      $('#removeLink').hide();
      $('#cloneTaxonomicCoverage').hide();
    }
  
    function GetTaxonomicCoverages() {
      var taxonomicCoverages = new Array();
      <#if (eml.taxonomicCoverages ? size > 0)>
        <@s.iterator value="eml.taxonomicCoverages" status="stat">
          taxonomicCoverages[${stat.index}] = new TaxonomicCoverage()
            .description("<@s.property value="description"/>")
            .scientificName("<@s.property value="taxonKeyword.scientificName"/>")
            .rank("<@s.property value="taxonKeyword.rank"/>")
            .commonName("<@s.property value="taxonKeyword.commonName"/>");
        </@s.iterator>
      </#if>
      return taxonomicCoverages;
    }
    
<!--
    function OnLoad() { 
      $('#roleDiv').hide();
      $('#role').attr('value', $('#roleEnum').attr('value'));
    };
-->
    function OnLoad() {  
      HideTaxonomicCoverageClone();
      var taxonomicCoveragePanel = new TaxonomicCoveragePanel();
      var taxonomicCoverageWidget;
      $('#plus').click(function() {
        taxonomicCoveragePanel.add(new TaxonomicCoverageWidget());
      });
      var taxonomicCoverages = GetTaxonomicCoverages();
      for (taxonomicCoverage in taxonomicCoverages) {
        taxonomicCoverageWidget = new TaxonomicCoverageWidget(taxonomicCoverages[taxonomicCoverage]);
        taxonomicCoveragePanel.add(taxonomicCoverageWidget);
      }
    }

    google.setOnLoadCallback(OnLoad);
    
//]]>

  </script>
  
  <script language="Javascript"
    type="text/javascript">
    
  	function AddForm() {
    	$("#break10").append("PRUEBA <br />");
    }
  </script>

</head>

<div class="break10"></div>
<p class="explMt"><@s.text name='metadata.description.taxcoverage'/></p>
<@s.form id="emlForm" action="taxcoverage" method="post" validate="false">

<fieldset>
  <@s.hidden name="resourceId" value="${(resource.id)!}"/>
  <@s.hidden name="resourceType" value="${(resourceType)!}"/>
  <@s.hidden name="guid" value="${(resource.guid)!}"/>
  <@s.hidden name="nextPage" value="tempcoverage"/>
  <@s.hidden name="method" value="taxonomicCoverages"/>
    
  <div id="taxonomicCoveragePanel" class="newline">
    <!-- The cloneTaxonomicCoverage DIV is not attached to the DOM. It's used as a template
       for cloning TaxonomicCoverage UI widgets. 
    -->
    <div id="cloneTaxonomicCoverage">
      <div id="separator" class="horizontal_dotted_line_large_foo"></div>
      <div class="newline"></div>
      <div class="right">
        <a id="removeLink" href="" onclick="return false;">[ <@s.text name='metadata.removethis'/> <@s.text name='eml.taxonomicCoverage'/> ]</a>
      </div>
      <div class="newline"></div>

      <div id="descriptionDiv">
        <@s.textarea id="description" key="" 
          label="%{getText('taxonomicCoverage.description')}" required="false" cssClass="text xlarge slim"/>
      </div>
      <div id="TaxonomicCoverageDiv">
        <div id="scientificNameDiv" class="leftxhalf">
          <@s.textfield id="scientificName" key="" 
          label="%{getText('taxonomicCoverage.scientificName')}" required="true" cssClass="text xhalf slim"/>
        </div>
        <div id="commonNameDiv" class="leftxhalf">
          <@s.textfield id="commonName" key="" 
          label="%{getText('taxonomicCoverage.commonName')}" required="false" cssClass="text xhalf slim"/>
        </div>
        <div class="leftxhalf">
          <@s.select id="rank" key="" label="%{getText('taxonomicCoverage.rank')}"
            list="taxonHigherRankList" required="true"/>
        </div>
      </div>
      <div class="newline"></div>
      <div class="newline"></div>
      <div class="newline"></div>
      <div class="newline"></div>
    </div>
  </div>

  <div class="left">
    <a id="plus" href="" onclick="AddForm(); return false;"><@s.text name='metadata.addnew'/> <@s.text name='eml.taxonomicCoverage'/>
    </a>
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