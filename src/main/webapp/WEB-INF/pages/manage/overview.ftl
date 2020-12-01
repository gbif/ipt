<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.OverviewAction" -->
<#escape x as x?html>
<#macro dwcaValidator>
  <#if (resource.recordsPublished>0)><a href="https://tools.gbif.org/dwca-validator/?archiveUrl=${baseURL}/archive.do?r=${resource.shortname}" title="<@s.text name="manage.overview.publishing.validator"/>" target="_blank" class="icon icon-validate"/></#if>
</#macro>
<#macro nextDoiButtonTD>

    <!-- The organisation with DOI account activated must exist,
    the mandatory metadata must have been filled in,
    and the user must have registration rights for any DOI operation made possible -->
    <#if !organisationWithPrimaryDoiAccount??>

      <img class="infoImg" src="${baseURL}/images/warning.gif" />
      <div class="info">
        <@s.text name="manage.overview.publishing.doi.reserve.prevented.noOrganisation"/>
      </div>

    <#elseif !currentUser.hasRegistrationRights()>

      <img class="infoImg" src="${baseURL}/images/warning.gif" />
      <div class="info">
        <@s.text name="manage.resource.status.doi.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/>
      </div>

    <#elseif resource.identifierStatus == "UNRESERVED">
      <form action='resource-reserveDoi.do' method='post'>
          <input name="r" type="hidden" value="${resource.shortname}"/>
        <@s.submit cssClass="confirmReserveDoi" name="reserveDoi" key="button.reserve" disabled="${missingMetadata?string}"/>
          <img class="infoImg" src="${baseURL}/images/info.gif" />
          <div class="info">
            <@s.text name="manage.overview.publishing.doi.reserve.help"/>
          </div>
      </form>

    <#elseif resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION">

      <form action='resource-deleteDoi.do' method='post'>
        <input name="r" type="hidden" value="${resource.shortname}"/>
        <@s.submit cssClass="confirmDeleteDoi" name="deleteDoi" key="button.delete" disabled="${missingMetadata?string}"/>
        <img class="infoImg" src="${baseURL}/images/info.gif" />
        <div class="info">
          <@s.text name="manage.overview.publishing.doi.delete.help"/>
        </div>
      </form>

    <#elseif resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi() >

      <form action='resource-reserveDoi.do' method='post'>
        <input name="r" type="hidden" value="${resource.shortname}"/>
        <@s.submit cssClass="confirmReserveDoi" name="reserveDoi" key="button.reserve.new" disabled="${missingMetadata?string}"/>
        <img class="infoImg" src="${baseURL}/images/info.gif" />
        <div class="info">
          <@s.text name="manage.overview.publishing.doi.reserve.new.help"/>
        </div>
      </form>

  </#if>
</#macro>

<#macro description text maxLength>
   	<#if (text?length>maxLength)>
   		${(text)?substring(0,maxLength)}...
   	<#else>
   		${(text)}
   	</#if>
</#macro>

<!-- The short form of the license for display in the versions table -->
<#macro shortLicense licenseUrl="">
    <#if licenseUrl == "http://creativecommons.org/publicdomain/zero/1.0/legalcode">
      CC0 1.0
    <#elseif licenseUrl == "http://creativecommons.org/licenses/by/4.0/legalcode">
      CC-BY 4.0
    <#elseif licenseUrl == "http://creativecommons.org/licenses/by-nc/4.0/legalcode">
      CC-BY-NC 4.0
    <#elseif licenseUrl == "http://www.opendatacommons.org/licenses/pddl/1.0">
      ODC PDDL 1.0
    <#elseif licenseUrl == "http://www.opendatacommons.org/licenses/by/1.0">
      ODC-By 1.0
    <#elseif licenseUrl?has_content>
      <@s.text name='manage.overview.noGBIFLicense'/>
    <#else>
      -
    </#if>
</#macro>
<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="manage.overview.title"/>: ${resource.title!resource.shortname}</title>
	<script type="text/javascript" src="${baseURL}/js/jconfirmation.jquery.js"></script>
<script type="text/javascript">
$(document).ready(function(){
  initHelp();
	<#if confirmOverwrite>
		showConfirmOverwrite();
	</#if>
	var $registered = false;

	$('.confirm').jConfirmAction({question : "<@s.text name='basic.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>"});
	$('.confirmRegistration').jConfirmAction({question : "<@s.text name='manage.overview.visibility.confirm.registration'/> <@s.text name='manage.resource.delete.confirm.registered'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", checkboxText: "<@s.text name='manage.overview.visibility.confirm.agreement'/>"});
	$('.confirmDeletion').jConfirmAction({question : "<#if resource.isAlreadyAssignedDoi()><@s.text name='manage.resource.delete.confirm.doi'/></br></br></#if><#if resource.status=='REGISTERED'><@s.text name='manage.resource.delete.confirm.registered'/></br></br></#if><@s.text name='manage.resource.delete.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>"});
  $('.confirmUndeletion').jConfirmAction({question : "<@s.text name='manage.resource.undoDelete.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>"});

  $('.confirmReserveDoi').jConfirmAction({question : "<@s.text name='manage.overview.publishing.doi.reserve.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>"});
  $('.confirmDeleteDoi').jConfirmAction({question : "<@s.text name='manage.overview.publishing.doi.delete.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>"});
  $('.confirmPublishMinorVersion').jConfirmAction({question : "<@s.text name='manage.overview.publishing.doi.minorVersion.confirm'/></br></br><@s.text name='manage.overview.publishing.doi.summary'/></br></br><@s.text name='manage.overview.publishing.doi.confirm.end'/>", yesAnswer : "<@s.text name='basic.yes'/>", summary : "<@s.text name='manage.overview.publishing.doi.summary.placeholder'/>", cancelAnswer : "<@s.text name='basic.no'/>"});
  $('.confirmPublishMajorVersion').jConfirmAction({question : "<@s.text name='manage.overview.publishing.doi.majorVersion.confirm'/></br></br><@s.text name='manage.overview.publishing.doi.summary'/></br></br><@s.text name='manage.overview.publishing.doi.confirm.end'/>", yesAnswer : "<@s.text name='basic.yes'/>", summary : "<@s.text name='manage.overview.publishing.doi.summary.placeholder'/>", cancelAnswer : "<@s.text name='basic.no'/>", checkboxText: "<@s.text name='manage.overview.publishing.doi.register.agreement'/>"});
  $('.confirmPublishMajorVersionWithoutDOI').jConfirmAction({question : "<@s.text name='manage.overview.publishing.withoutDoi.majorVersion.confirm'/></br></br><@s.text name='manage.overview.publishing.doi.summary'/></br></br><@s.text name='manage.overview.publishing.doi.confirm.end'/>", yesAnswer : "<@s.text name='basic.yes'/>", summary : "<@s.text name='manage.overview.publishing.doi.summary.placeholder'/>", cancelAnswer : "<@s.text name='basic.no'/>"});

    var showReport=false;
	$("#toggleReport").click(function() {
		if(showReport){
			showReport=false;
			$("#toggleReport").text("<@s.text name='basic.show'/>");
			$('#dwcaReport').hide();
		}else{
			showReport=true;
			$("#toggleReport").text("<@s.text name='basic.hide'/>");
			$('#dwcaReport').show();
		}
	});
	//Hack needed for Internet Explorer X.*x
	$('.edit').each(function() {
		$(this).click(function() {
			window.location = $(this).parent('a').attr('href');
		});
	});
	$('.submit').each(function() {
		$(this).click(function() {
			$(this).parent('form').submit();
		});
	});
	$("#file").change(function() {
		var usedFileName = $("#file").prop("value");
		if(usedFileName != "") {
			$("#add").attr("value", '<@s.text name="button.add"/>');
		}
	});
	$("#clear").click(function(event) {
		event.preventDefault();
		$("#file").prop("value", "");
		$("#add").attr("value", '<@s.text name="button.connectDB"/>');
	});

  $(function() {
    $('.icon-validate').tooltip({track: true});
  });

	function showConfirmOverwrite() {
	   var question='<p><@s.text name="manage.resource.addSource.confirm"/></p>';
	   $('#dialog').html(question);
		$("#dialog").dialog({
			'modal'     : true,
			'title'		: '<@s.text name="basic.confirm"/>',
			'buttons'   : {
				'<@s.text name="basic.yes"/>': function(){
					$(this).dialog("close");
					$("#add").click();
				},
				'<@s.text name="basic.no"/>' : function(){
					$(this).dialog("close");
					$("#cancel").click();
				}
			}
		});
	}

    // load a preview of the mapping in the modal window
    $(".peekBtn").click(function(e) {
        e.preventDefault();
        var addressValue = $(this).attr("href");
        $("#modalcontent").load(addressValue);
        $("#modalbox").show();
    });
    $("#modalbox").click(function(e) {
        e.preventDefault();
        $("#modalbox").hide();
    });

    // change the doi prefix input value, as per the selected organisation
    $( "#doi_select" ).change(function() {
        $("#doi_prefix").prop("value", $( this).val());
    });

    $("#doi_edit").click(function() {
        $('.doiButton').hide();
        $('#doi_edit_block').show();
    });

    $('#doi_edit_cancel').click(function() {
        $('.doiButton').show();
        $('#doi_edit_block').hide();
    });

});
</script>

<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<#include "/WEB-INF/pages/macros/manage/publish.ftl"/>
<#assign metadataType = "metadata"/>
<div class="container_24">
  <div class="grid_18 suffix_6">
    <h1>
        <img class="infoImg" src="${baseURL}/images/info.gif" />
        <div class="info autop">
          <#if resource.coreType?has_content && resource.coreType==metadataType>
            <@s.text name="manage.overview.intro.metadataOnly"><@s.param>${resource.title!resource.shortname}</@s.param></@s.text>
          <#else>
            <@s.text name="manage.overview.intro"><@s.param>${resource.title!resource.shortname}</@s.param></@s.text>
          </#if>
        </div>
        <span class="resourceOverviewTitle"><@s.text name="manage.overview.title"/>: </span><a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a></h1>
    <p>
      <@s.text name="manage.overview.description"><@s.param>${resource.title!resource.shortname}</@s.param></@s.text>
    </p>
  </div>
</div>

<!-- when resource is of type metadata-only, there is no need to show source data and mapping sections -->
<#if resource.coreType?has_content && resource.coreType==metadataType>
  <#include "/WEB-INF/pages/manage/overview_metadata.ftl"/>
<#else>
  <#include "/WEB-INF/pages/manage/overview_data.ftl"/>
  <#include "/WEB-INF/pages/manage/overview_metadata.ftl"/>
</#if>

<div class="resourceOverview" id="publish">
  <div class="titleOverview">
    <div class="head">
      <img class="infoImg" src="${baseURL}/images/info.gif" />
      <div class="info autop">
        <#if resource.coreType?has_content && resource.coreType==metadataType>
          <@s.text name="manage.overview.published.description.metadataOnly"/>
        <#else>
          <@s.text name="manage.overview.published.description"/>
        </#if>
        </br></br>
        <#if organisationWithPrimaryDoiAccount??>
          <@s.text name='manage.overview.published.description.doiAccount'><@s.param>${organisationWithPrimaryDoiAccount.doiRegistrationAgency}</@s.param><@s.param>${organisationWithPrimaryDoiAccount.name}</@s.param><@s.param>${organisationWithPrimaryDoiAccount.doiPrefix}</@s.param></@s.text>
        <#else>
        <@s.text name="manage.overview.published.description.noDoiAccount"/>
      </#if>
      </div>
      <@s.text name="manage.overview.published"/>
    </div>
    <div class="actions">
      <@publish resource/>
    </div>
  </div>
  <div class="bodyOverview">

      <p>
        <@s.text name="manage.overview.published.intro"/>
      </p>

      <div class="details">
        <#assign lastPublishedTitle><@s.text name="manage.overview.published.last.publication.intro"/></#assign>
        <#assign nextPublishedTitle><@s.text name="manage.overview.published.next.publication.intro"/></#assign>
        <#assign versionTitle><@s.text name="manage.overview.published.version"/></#assign>
        <#assign releasedTitle><@s.text name="manage.overview.published.released"/></#assign>
        <#assign pubLogTitle><@s.text name="portal.publication.log"/></#assign>
        <#assign pubRepTitle><@s.text name="manage.overview.published.report"/></#assign>
        <#assign downloadTitle><@s.text name='manage.overview.published.download'/></#assign>
        <#assign showTitle><@s.text name="basic.show"/></#assign>
        <#assign viewTitle><@s.text name='button.view'/></#assign>
        <#assign previewTitle><@s.text name='button.preview'/></#assign>
        <#assign emptyCell="-"/>
        <#assign visibilityTitle><@s.text name='manage.overview.visibility'/></#assign>
        <#assign licenseTitle><@s.text name='eml.intellectualRights.license'/></#assign>

          <table class="publishedRelease">
              <tr class="mapping_head headz">
                  <th></th><#if resource.lastPublished??><td class="green">${lastPublishedTitle?cap_first}</td></#if><td class="left_padding">${nextPublishedTitle?cap_first}</td>
              </tr>
              <tr>
                  <th>${versionTitle?cap_first}</th><#if resource.lastPublished??><td class="separator green">${resource.emlVersion.toPlainString()}&nbsp;<a class="button" href="${baseURL}/resource?r=${resource.shortname}"><input class="button" type="button" value='${viewTitle?cap_first}'/></a><@dwcaValidator/></td></#if><td class="left_padding">${resource.getNextVersion().toPlainString()}&nbsp;<a class="button" href="${baseURL}/resource/preview?r=${resource.shortname}"><input class="button" type="button" value='${previewTitle?cap_first}' <#if missingMetadata>disabled="disabled"</#if>/></a></td>
              </tr>
              <!-- hide visibility row if 1) a DOI has already been assigned to the resource since any resource with a DOI has to be public, 2) the resource is registered, or 3) the visibility of the currenct version and next version are the same -->
              <#if !resource.isAlreadyAssignedDoi() && !resource.isRegistered() && (resource.getStatus()?lower_case != resource.getLastPublishedVersionsPublicationStatus()?lower_case) || !resource.lastPublished?? >
                <tr>
                  <th>${visibilityTitle?cap_first}</th><#if resource.lastPublished??><td class="separator green">${resource.getLastPublishedVersionsPublicationStatus()?lower_case?cap_first}</td></#if><td class="left_padding">${resource.status?lower_case?cap_first}</td>
                </tr>
              </#if>
              <!-- hide DOI row if no organisation with DOI account has been activated yet -->
              <#if organisationWithPrimaryDoiAccount??>
                <tr>
                  <th>DOI</th><#if resource.lastPublished??><td class="separator green"><#if resource.isAlreadyAssignedDoi()>${resource.versionHistory[0].doi!}<#else>${emptyCell}</#if></td></#if><td class="left_padding"><#if (resource.isAlreadyAssignedDoi() && resource.versionHistory[0].doi != resource.doi!"") || (!resource.isAlreadyAssignedDoi() && resource.doi?has_content)><em>${resource.doi!emptyCell}</em>&nbsp;</#if><@nextDoiButtonTD/></td>
                </tr>
              </#if>
              <!-- TODO: hide license row if the current version and next version have both been assigned the same license -->
              <#if (resource.lastPublished?? && !action.isLastPublishedVersionAssignedGBIFSupportedLicense(resource)) || !resource.lastPublished?? || !resource.isAssignedGBIFSupportedLicense()>
                <tr>
                    <th>${licenseTitle?cap_first}</th><#if resource.lastPublished??><td class="separator green"><@shortLicense action.getLastPublishedVersionAssignedLicense(resource)!/></td></#if><td class="left_padding"><@shortLicense resource.getEml().parseLicenseUrl()/></td>
                </tr>
              </#if>
              <tr>
                  <th>${releasedTitle?cap_first}</th><#if resource.lastPublished??><td class="separator green">${resource.lastPublished?date?string.medium}</td></#if><td class="left_padding"><#if resource.nextPublished??>${resource.nextPublished?date?string("MMM d, yyyy, HH:mm:ss")}<#else>${emptyCell}</#if></td>
              </tr>
              <#if resource.lastPublished??>
              <tr>
                  <th>${pubLogTitle?cap_first}</th><td class="separator"><a class="button" target="_blank" href="${baseURL}/publicationlog.do?r=${resource.shortname}"><input class="button" type="button" value='${downloadTitle?cap_first}'/></a></td><td class="left_padding">${emptyCell}</td>
              </tr>
              </#if>
            <#if report??>
                <tr>
                    <th>${pubRepTitle?cap_first}</th><td class="separator"><#if report?? && (report.state?contains('cancelled') || report.exception?has_content) ><em>${report.state}</em>&nbsp;</#if><a id="toggleReport" href="#">${showTitle?cap_first}</a></td><td class="left_padding">${emptyCell}</td>
                </tr>
            </#if>
          </table>
          <#if report??>
            <table>
              <tr id="dwcaReport" style="display: none;">
                  <td colspan="2">
                      <div class="report">
                          <ul class="simple">
                            <#list report.messages as msg>
                                <li class="${msg.level}">${msg.message} <span class="small">${msg.date?time?string}</span></li>
                            </#list>
                          </ul>
                        <#if cfg.debug() && report.hasException()>
                            <br/>
                            <ul class="simple">
                                <li><strong>Exception</strong> ${report.exceptionMessage!}</li>
                              <#list report.exceptionStacktrace as msg>
                                  <li>${msg}</li>
                              </#list>
                            </ul>
                        </#if>
                      </div>
                  </td>
              </tr>
            </table>
          </#if>
      </div>
  </div>
</div>

<div class="resourceOverview" id="autopublish">
  <div class="titleOverview">
    <div class="head">
      <img class="infoImg" src="${baseURL}/images/info.gif" />
      <div class="info autop">
        <@s.text name="manage.overview.autopublish.description"/>
      </div>
      <@s.text name="manage.overview.autopublish.title"/>
    </div>
    <div class="actions">
      <form action='auto-publish.do' method='get'>
          <input name="r" type="hidden" value="${resource.shortname}"/>
          <#if resource.isDeprecatedAutoPublishingConfiguration()>
            <@s.submit name="edit" key="button.edit"/>
            <img class="infoImg" src="${baseURL}/images/warning.gif"/>
            <div class="info autop">
              <@s.text name="manage.overview.autopublish.deprecated.warning.button"/>
            </div>
          <#else>
            <@s.submit name="edit" key="button.edit"/>
          </#if>
      </form>
    </div>
  </div>
  <div class="bodyOverview">

    <p>
      <#if resource.usesAutoPublishing()>
        <@s.text name="manage.overview.autopublish.intro.activated"/>
      <#else>
        <@s.text name="manage.overview.autopublish.intro.deactivated"/>
      </#if>
    </p>

    <div class="details">
      <table>
        <#if resource.usesAutoPublishing()>
          <tr>
            <th><@s.text name='manage.overview.autopublish.publication.frequency'/></th>
            <td><@s.text name="${autoPublishFrequencies.get(resource.updateFrequency.identifier)}"/></td>
          </tr>
          <tr>
            <th><@s.text name='manage.overview.autopublish.publication.next.date'/></th>
            <td>${resource.nextPublished?date?string("MMM d, yyyy, HH:mm:ss")}</td>
          </tr>
        </#if>
      </table>
    </div>
  </div>
</div>

<div class="resourceOverview" id="visibility">
  <div class="titleOverview">
    <div class="head">
      <img class="infoImg" src="${baseURL}/images/info.gif" />
      <div class="info autop">
        <@s.text name='manage.overview.visibility.description'/>
        </br></br>
        <@s.text name='manage.resource.status.intro.public.migration'><@s.param><a href="${baseURL}/manage/metadata-additional.do?r=${resource.shortname}&amp;edit=Edit"><@s.text name="submenu.additional"/></a></@s.param></@s.text></br></br><@s.text name='manage.resource.status.intro.public.gbifWarning'/>
      </div>
      <@s.text name='manage.overview.visibility'/>
      <em class="<#if resource.status=="PRIVATE">red<#else>green</#if>"><@s.text name="resource.status.${resource.status?lower_case}"/></em>
    </div>
    <div class="actions">
      <#assign actionMethod>registerResource</#assign>
      <#if resource.status=="PRIVATE">
        <#assign actionMethod>makePublic</#assign>
      </#if>

      <form action='resource-${actionMethod}.do' method='post'>
        <input name="r" type="hidden" value="${resource.shortname}"/>
        <#if resource.status=="PUBLIC">
          <#if !currentUser.hasRegistrationRights()>
            <!-- Disable register button and show warning: user must have registration rights -->
            <@s.submit cssClass="confirmRegistration" name="register" key="button.register" disabled="true"/>
            <img class="infoImg" src="${baseURL}/images/warning.gif"/>
            <div class="info autop">
              <@s.text name="manage.resource.status.registration.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/>
            </div>
          <#elseif missingValidPublishingOrganisation?string == "true">
            <!-- Disable register button and show warning: user must assign valid publishing organisation -->
            <@s.submit cssClass="confirmRegistration" name="register" key="button.register" disabled="true"/>
            <img class="infoImg" src="${baseURL}/images/warning.gif"/>
            <div class="info autop">
              <@s.text name="manage.overview.visibility.missing.organisation"/>
            </div>
          <#elseif missingRegistrationMetadata?string == "true">
            <!-- Disable register button and show warning: user must fill in minimum registration metadata -->
            <@s.submit cssClass="confirmRegistration" name="register" key="button.register" disabled="true"/>
            <img class="infoImg" src="${baseURL}/images/warning.gif"/>
            <div class="info autop">
              <@s.text name="manage.overview.visibility.missing.metadata"/>
            </div>
          <#elseif !resource.isLastPublishedVersionPublic()>
            <!-- Disable register button and show warning: last published version must be publicly available to register -->
            <@s.submit cssClass="confirmRegistration" name="register" key="button.register" disabled="true"/>
            <img class="infoImg" src="${baseURL}/images/warning.gif"/>
            <div class="info autop">
              <@s.text name="manage.overview.prevented.resource.registration.notPublic"/>
            </div>
          <#elseif !action.isLastPublishedVersionAssignedGBIFSupportedLicense(resource)>
            <!-- Disable register button and show warning: resource must be assigned a GBIF-supported license to register if resource has occurrence data -->
            <@s.submit cssClass="confirmRegistration" name="register" key="button.register" disabled="true"/>
            <img class="infoImg" src="${baseURL}/images/warning.gif"/>
            <div class="info autop">
              <@s.text name="manage.overview.prevented.resource.registration.noGBIFLicense"/>
            </div>
          <#else>
            <@s.submit cssClass="confirmRegistration" name="register" key="button.register"/>
          </#if>
        <#else>
          <#if resource.status=="PRIVATE">
            <@s.submit name="makePrivate" key="button.public"/>
          </#if>
        </#if>
      </form>

      <#if resource.status=="PUBLIC" && (resource.identifierStatus=="PUBLIC_PENDING_PUBLICATION" || resource.identifierStatus == "UNRESERVED")>
        <#assign actionMethod>makePrivate</#assign>
        <form action='resource-${actionMethod}.do' method='post'>
          <@s.submit cssClass="confirm" name="unpublish" key="button.private" />
        </form>
      </#if>
    </div>
  </div>
  <div class="bodyOverview">
    <p>
      <@s.text name="manage.resource.status.intro.${resource.status?lower_case}"/>
    </p>

      <#if cfg.devMode() && cfg.getRegistryType()!='PRODUCTION'>
        <div class="twenty_bottom twenty_top">
            <img class="info" src="${baseURL}/images/warning.gif"/>
            <em><@s.text name="manage.overview.published.testmode.warning"/></em>
        </div>
      </#if>

      <#if resource.status=="REGISTERED" && resource.key??>
        <div class="details">
          <table>
            <tr>
              <th>GBIF UUID</th>
              <td><a href="${cfg.portalUrl}/dataset/${resource.key}" target="_blank">${resource.key}</a>
              </td>
            </tr>
            <#if resource.organisation??>
              <#-- in prod mode link goes to /publisher (GBIF Portal), in dev mode link goes to /publisher (GBIF UAT Portal) -->
              <tr>
                <th><@s.text name="manage.overview.visibility.organisation"/></th>
                <td><a href="${cfg.portalUrl}/publisher/${resource.organisation.key}" target="_blank">${resource.organisation.name!"Organisation"}</a></td>
              </tr>
              <tr>
                <th><@s.text name="manage.overview.visibility.organisation.contact"/></th>
                <td>${resource.organisation.primaryContactName!}, ${resource.organisation.primaryContactEmail!}</td>
              </tr>
              <tr>
                <th><@s.text name="manage.overview.visibility.endorsing.node"/></th>
                <td><a href="${cfg.portalUrl}/node/${resource.organisation.nodeKey!"#"}" target="_blank">${resource.organisation.nodeName!}</a></td>
              </tr>
            </#if>
          </table>
        </div>
      </#if>
  </div>
</div>

<div class="resourceOverview" id="managers">
  <div class="titleOverview">
    <div class="head">
      <img class="infoImg" src="${baseURL}/images/info.gif" />
      <div class="info autop">
        <@s.text name='manage.overview.resource.managers.description'/>
      </div>
      <@s.text name="manage.overview.resource.managers"/>
    </div>

    <#if (potentialManagers?size>0)>
      <div class="actions">
        <!-- Warning: method name match is case sensitive therefore must be addManager -->
        <form action='resource-addManager.do' method='post'>
          <input name="r" type="hidden" value="${resource.shortname}"/>
          <select name="id" id="manager" size="1">
            <option value=""></option>
            <#list potentialManagers?sort_by("name") as u>
              <option value="${u.email}">${u.name}</option>
            </#list>
          </select>
          <@s.submit name="add" key="button.add"/>
        </form>
      </div>
    </#if>
  </div>
  <div class="bodyOverview">
    <p>
      <@s.text name="manage.overview.resource.managers.intro"><@s.param>${resource.shortname}</@s.param></@s.text>
    </p>

    <div class="details">
      <table>
        <tr>
          <th><@s.text name="manage.overview.resource.managers.creator"/></th>
          <td>${resource.creator.name!}, ${resource.creator.email}</td>
        </tr>
        <#if (resource.managers?size>0)>
          <#list resource.managers as u>
              <tr>
                  <th><@s.text name="manage.overview.resource.managers.manager"/></th>
                  <!-- Warning: method name match is case sensitive therefore must be deleteManager -->
                  <td>${u.name}, ${u.email}&nbsp;
                      <a class="button" href="resource-deleteManager.do?r=${resource.shortname}&id=${u.email}">
                          <input class="button" type="button" value='<@s.text name='button.delete'/>'/>
                      </a>
                  </td>
              </tr>
          </#list>
        </#if>
      </table>
    </div>
  </div>
</div>

<div>
  <#if resource.isAlreadyAssignedDoi()?string == "false" && resource.status != "REGISTERED">
    <#assign disableRegistrationRights="false"/>
  <#elseif currentUser.hasRegistrationRights()?string == "true">
    <#assign disableRegistrationRights="false"/>
  <#else>
    <#assign disableRegistrationRights="true"/>
  </#if>
  <#if resource.status == "DELETED">
      <form action='resource-undelete.do' method='post'>
        <input name="r" type="hidden" value="${resource.shortname}" />
        <@s.submit cssClass="button confirmUndeletion" name="undelete" key="button.undelete" disabled='${disableRegistrationRights?string}' />
        <#if !currentUser.hasRegistrationRights()>
            <img class="infoImg" src="${baseURL}/images/warning.gif"/>
            <div class="info autop">
              <@s.text name="manage.resource.status.undeletion.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/>
            </div>
        </#if>
      </form>
  <#else>
      <form action='resource-delete.do' method='post'>
        <input name="r" type="hidden" value="${resource.shortname}" />
        <@s.submit cssClass="button confirmDeletion" name="delete" key="button.delete" disabled='${disableRegistrationRights?string}'/>
        <#if !currentUser.hasRegistrationRights() && (resource.isAlreadyAssignedDoi()?string == "true" || resource.status == "REGISTERED")>
        <img class="infoImg" src="${baseURL}/images/warning.gif"/>
          <div class="info autop">
            <@s.text name="manage.resource.status.deletion.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/>
          </div>
        </#if>
      </form>
  </#if>

</div>
<div id="dialog"></div>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
