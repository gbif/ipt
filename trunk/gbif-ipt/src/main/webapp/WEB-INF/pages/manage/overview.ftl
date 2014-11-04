<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.OverviewAction" -->
<#escape x as x?html>
<#macro description text maxLength>
   	<#if (text?length>maxLength)>
   		${(text)?substring(0,maxLength)}...
   	<#else>
   		${(text)}
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
	$('.confirmRegistration').jConfirmAction({question : "<@s.text name='manage.overview.visibility.confirm.registration'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", checkboxText: "<@s.text name='manage.overview.visibility.confirm.agreement'/>"});	
	$('.confirmDeletion').jConfirmAction({question : "<#if resource.status=='REGISTERED'><@s.text name='manage.resource.delete.confirm.registered'/><#else><@s.text name='basic.confirm'/></#if>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>"});

    var showReport=false;
	$("#toggleReport").click(function() {
		if(showReport){
			showReport=false;
			$("#toggleReport").text("<@s.text name='manage.overview.published.see.report'/>");
			$('#dwcaReport').hide();
		}else{
			showReport=true;
			$("#toggleReport").text("<@s.text name='manage.overview.published.hide.report'/>");
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


    // on select of publishing frequency set parameter for publishing frequency
    $('#autopublish').change(function () {
        var str = "";
        $("#autopublish option:selected").each(function () {
            str += $(this).val();
        });
        // set selected frequency
        $("#pubFreq").val(str);

        // gather current publishing frequency
        var currFreq = $("#currPubFreq").val();

        // gather current auto-publishing mode
        var currMode = $("#currPubMode").val();

        // when auto-publishing is off, and a frequency is selected
        if (currMode=="AUTO_PUBLISH_OFF" && currMode !="" && str!="") {
            $('#publishButton').val("<@s.text name='autopublish.activate'/>");
            $("#pubMode").val("AUTO_PUBLISH_ON");
        }
        // when auto-publishing is on, and the user wants to disable auto-publishing
        else if(currMode=="AUTO_PUBLISH_ON" && str=="off") {
            $('#publishButton').val("<@s.text name='autopublish.disable'/>");
            $("#pubMode").val("AUTO_PUBLISH_OFF");
        }
        // when auto-publishing is on, and the user wants to change the frequency
        else if(currMode=="AUTO_PUBLISH_ON" && currFreq!=str) {
            $('#publishButton').val("<@s.text name='autopublish.update'/>");
            $("#pubMode").val("AUTO_PUBLISH_ON");
        }
        // when either auto-publishing is on, and the user is happy with the current settings,
        // or when auto-publishing is off and no frequency selected
        else {
            $('#publishButton').val("<@s.text name='button.publish'/>");
        }
    }).change();

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
    <h1><span class="resourceOverviewTitle"><@s.text name="manage.overview.title"/>: </span>${resource.title!resource.shortname}</h1>
    <p>
      <#if resource.coreType?has_content && resource.coreType==metadataType>
        <@s.text name="manage.overview.intro.metadataOnly"><@s.param>${resource.title!resource.shortname}</@s.param></@s.text>
      <#else>
        <@s.text name="manage.overview.intro"><@s.param>${resource.title!resource.shortname}</@s.param></@s.text>
      </#if>
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
      <@s.text name="manage.overview.published"/>
    </div>
    <div class="actions">
      <#if !missingMetadata>
        <#if resource.status=="REGISTERED">
          <#if currentUser.hasRegistrationRights()>
            <@publish resource/>
          </#if>
        <#else>
          <@publish resource/>
        </#if>
      </#if>
    </div>
  </div>
  <div class="bodyOverview">

    <#if missingMetadata>
      <div>
        <img class="info" src="${baseURL}/images/info.gif"/>
        <em><@s.text name="manage.overview.published.missing.metadata"/></em>
      </div>
    <#else>
      <p>
        <#if resource.coreType?has_content && resource.coreType==metadataType>
          <@s.text name="manage.overview.published.description.metadataOnly"/>
        <#else>
          <@s.text name="manage.overview.published.description"/>
        </#if>
      </p>
      <#if resource.status=="REGISTERED">
        <#if !currentUser.hasRegistrationRights()>
            <div>
                <img class="info" src="${baseURL}/images/info.gif"/>
                <em><@s.text name="manage.resource.status.registration.forbidden"/>
                    &nbsp;<@s.text name="manage.resource.publish.forbidden"/>
                    &nbsp;<@s.text name="manage.resource.role.change"/></em>
            </div>
            <br/>
        </#if>
      </#if>
    </#if>

    <#if resource.lastPublished??>
      <div class="details">
        <table>
          <tr>
            <th><@s.text name="manage.overview.published.last.publication"/></th>
            <td>
              <!-- show either "Version #" or report state, e.g. "Failed. Fatal error" -->
              <#if report?? && (report.state?contains('cancelled') || report.exception?has_content) >
                <em>${report.state}</em>
              <#else>
                <@s.text name="manage.overview.published.version"/>
                ${resource.emlVersion}
                <@s.text name="manage.overview.published.from"/>
                ${resource.lastPublished?date?string.medium}
              </#if>
              &nbsp;
              <#if report??>
                <a id="toggleReport" href="#">
                  <@s.text name="manage.overview.published.see.report"/>
                </a>
                <!-- ensure a space separates see report link, and publication log link-->
                &nbsp;
              </#if>
              <!-- ensure publication log not shown for metadata-only resources because the log reflects dwca generation only-->
              <#if resource.coreType?has_content && resource.coreType!=metadataType>
                  <a href="${baseURL}/publicationlog.do?r=${resource.shortname}"><@s.text name='portal.publication.log'/></a>
              </#if>
            </td>
          </tr>
          <#if report??>
            <tr id="dwcaReport" style="display: none;">
              <td colspan="2">
                <div class="report">
                  <ul class="simple">
                    <#list report.messages as msg>
                      <li>${msg.message} <span class="small">${msg.date?time?string}</span></li>
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
          </#if>
          <#if (resource.recordsPublished>0)>
            <tr>
              <th><@s.text name="manage.overview.published.archive"/></th>
              <td>
                <a href="${baseURL}/archive.do?r=${resource.shortname}"><@s.text name="manage.overview.published.download"/></a>
                (${dwcaFormattedSize}) ${resource.recordsPublished} <@s.text name="manage.overview.published.records"/>
              </td>
            </tr>
          </#if>
          <tr>
            <th><@s.text name="manage.overview.published.eml"/></th>
            <td>
              <a href="${baseURL}/eml.do?r=${resource.shortname}&v=${resource.emlVersion}"><@s.text name="manage.overview.published.download"/></a>
              <a href="${baseURL}/resource.do?r=${resource.shortname}"><@s.text name="manage.overview.published.view"/></a>
              (${emlFormattedSize})
            </td>
          </tr>
          <tr>
            <th><@s.text name="portal.resource.published.rtf"/></th>
            <td>
              <a href="${baseURL}/rtf.do?r=${resource.shortname}&v=${resource.emlVersion}"><@s.text name="manage.overview.published.download"/></a>
              (${rtfFormattedSize})
            </td>
          </tr>
        </table>
      </div>
    </#if>
  </div>
</div>

<#--<div class="resourceOverview" id="identifierStatus">-->
    <#--<div class="titleOverview">-->
       <#--<#if missingMetadata>-->
         <#--<div class="head">-->
             <#--DOI Status-->
         <#--</div>-->
      <#--<#else>-->
          <#--<div class="head">-->
              <#--DOI Status-->
              <#--<em class="<#if resource.identifierStatus=="UNAVAILABLE">red<#elseif resource.identifierStatus=="RESERVED">yellow<#else>green</#if>"><@s.text name="resource.identifierStatus.${resource.identifierStatus?lower_case}"/></em>-->
          <#--</div>-->

          <#--<div class="actions">-->

            <#--<#if resource.identifierStatus=="RESERVED">-->
                <#--<div>-->
                    <#--<input type="button" class="doiButton" id="doi_edit" name="edit" value="<@s.text name='manage.resource.identifierStatus.edit'/>"/>-->
                <#--</div>-->

                <#--<form action='resource-updateDoi.do' method='post'>-->
                    <#--<div id="doi_edit_block" style="display:none">-->
                        <#--<span class="doi_colon">doi:</span>-->
                        <#--<input class="identifier_part" name="doi_prefix" id="doi_prefix" value="${action.getDoiPrefix()!""}" />-->
                        <#--<input class="identifier_part" name="doi_suffix" value="${action.getDoiSuffix()!""}" />-->
                        <#--<select name="id" id="doi_select" size="1">-->
                          <#--<#list organisationsWithDoiAccount as o>-->
                              <#--<option value="${o.doiPrefix!""?string}" <#if o.doiPrefix==action.getDoiPrefix()!"">selected</#if> >${o.name}</option>-->
                          <#--</#list>-->
                        <#--</select>-->
                        <#--<div>-->
                          <#--<@s.submit name="update" key="button.save"/>-->
                            <#--<input type="button" id="doi_edit_cancel" name="cancel" value="<@s.text name='button.cancel'/>"/>-->
                        <#--</div>-->
                    <#--</div>-->
                <#--</form>-->
            <#--</#if>-->

            <#--<#if resource.identifierStatus=="RESERVED" || resource.identifierStatus=="UNAVAILABLE">-->
              <#--<#assign action>registerDoi</#assign>-->
            <#--<#else>-->
              <#--<#assign action>makeDoiUnavailable</#assign>-->
            <#--</#if>-->

              <#--<!-- The resource must be public before making its DOI public &ndash;&gt;-->
            <#--<#if resource.status!="PRIVATE">-->
                <#--<form action='resource-${action}.do' method='post'>-->
                    <#--<input name="r" type="hidden" value="${resource.shortname}"/>-->
                  <#--<#if currentUser.hasRegistrationRights() && (organisationsWithDoiAccount?size>0)>-->
                    <#--<#if (resource.identifierStatus=="RESERVED" || resource.identifierStatus=="UNAVAILABLE") && resource.status!="PRIVATE">-->
                      <#--<#if resource.identifierStatus=="RESERVED">-->
                        <#--<@s.submit cssClass="doiButton" name="publish" key="button.register.doi"/>-->
                      <#--<#else>-->
                        <#--<@s.submit cssClass="doiButton" name="publish" key="button.makeAvailable"/>-->
                      <#--</#if>-->
                    <#--<#else>-->
                      <#--<@s.submit cssClass="doiButton" name="publish" key="button.makeUnavailable"/>-->
                    <#--</#if>-->
                  <#--</#if>-->
                <#--</form>-->
            <#--</#if>-->
          <#--</div>-->
      <#--</#if>-->
    <#--</div>-->
    <#--<div class="bodyOverview">-->
      <#--<#if missingMetadata>-->
        <#--<div>-->
          <#--<img class="info" src="${baseURL}/images/info.gif"/>-->
          <#--<em><@s.text name="manage.overview.identifierStatus.missing.metadata"/></em>-->
        <#--</div>-->
      <#--<#else>-->
        <#--<p>-->
          <#--<@s.text name="manage.resource.identifierStatus.intro.${resource.identifierStatus?lower_case}"/>-->
        <#--</p>-->

        <#--<#if resource.identifierStatus=="RESERVED"  || resource.identifierStatus=="UNAVAILABLE" >-->
            <#--<!-- Before making a DOI public, the resource's visibility must be set to public or reserved &ndash;&gt;-->
          <#--<#if resource.status=="PRIVATE">-->
              <#--<div>-->
                  <#--<img class="info" src="${baseURL}/images/info.gif"/>-->
                  <#--<em><@s.text name="manage.resource.identifierStatus.reserved.forbidden"/></em>-->
              <#--</div>-->
              <#--<br/>-->
          <#--</#if>-->

          <#--<#if currentUser.hasRegistrationRights()>-->
            <#--<#if organisationsWithDoiAccount?size==0>-->
                <#--<div>-->
                    <#--<img class="info" src="${baseURL}/images/info.gif"/>-->
                    <#--<em><@s.text name="manage.overview.identifierStatus.no.organisations"/></em>-->
                <#--</div>-->
                <#--<br/>-->
            <#--</#if>-->
          <#--<#else>-->
              <#--<div>-->
                  <#--<img class="info" src="${baseURL}/images/info.gif"/>-->
                  <#--<em><@s.text name="manage.resource.identifierStatus.registration.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/></em>-->
              <#--</div>-->
              <#--<br/>-->
          <#--</#if>-->
        <#--</#if>-->
          <#--<div class="details">-->
              <#--<table>-->
                <#--<#if resource.doi?has_content>-->
                    <#--<tr>-->
                        <#--<th>DOI</th>-->
                        <#--<td>doi:${resource.doi!}</td>-->
                    <#--</tr>-->
                    <#--<tr>-->
                        <#--<th>DOI Registered to</th>-->
                        <#--<td>${doiOrganisationName!}</td>-->
                    <#--</tr>-->
                <#--</#if>-->
              <#--</table>-->
          <#--</div>-->
      <#--</#if>-->
    <#--</div>-->
<#--</div>-->

<div class="resourceOverview" id="visibility">
  <div class="titleOverview">
    <div class="head">
      <@s.text name='manage.overview.visibility'/>
      <em class="<#if resource.status=="PRIVATE">red<#else>green</#if>"><@s.text name="resource.status.${resource.status?lower_case}"/></em>
    </div>
    <div class="actions">
      <#assign action>registerResource</#assign>
      <#if resource.status=="PRIVATE">
        <#assign action>makePublic</#assign>
      </#if>

      <form action='resource-${action}.do' method='post'>
        <input name="r" type="hidden" value="${resource.shortname}"/>
        <#if resource.status=="PUBLIC"  <#--&& resource.identifierStatus?? && resource.identifierStatus!="UNAVAILABLE"-->>
          <#if currentUser.hasRegistrationRights() && (organisations?size>0)>
            <select name="id" id="org" size="1">
              <#list organisations as o>
                <option value="${o.key}">${o.name}</option>
              </#list>
            </select>

            <@s.submit cssClass="confirmRegistration" name="publish" key="button.register" disabled="${missingRegistrationMetadata?string}"/>
          </#if>
        <#else>
          <#if resource.status=="PRIVATE">
            <@s.submit name="publish" key="button.public"/>
          </#if>
        </#if>
      </form>

      <#if resource.status=="PUBLIC" <#--&& resource.identifierStatus=="RESERVED"-->>
        <#assign action>makePrivate</#assign>
        <form action='resource-${action}.do' method='post'>
          <@s.submit cssClass="confirm" name="unpublish" key="button.private" />
        </form>
      </#if>
    </div>
  </div>
  <div class="bodyOverview">
    <p>
      <@s.text name="manage.resource.status.intro.${resource.status?lower_case}"/>
    </p>

    <#if resource.status=="PUBLIC" <#--&& resource.identifierStatus!="UNAVAILABLE"-->>
      <#if missingRegistrationMetadata>
        <div>
          <img class="info" src="${baseURL}/images/warning.gif"/>
          <em><@s.text name="manage.overview.visibility.missing.metadata"/></em>
        </div>
      <#else>
        <#if currentUser.hasRegistrationRights()>
            <p>
              <@s.text name="manage.resource.status.registration.intro"/>
            </p>
          <#if organisations?size==0>
              <div>
                  <img class="info" src="${baseURL}/images/info.gif"/>
                  <em><@s.text name="manage.overview.visibility.no.organisations"/></em>
              </div>
          </#if>
            <div>
                <img class="info" src="${baseURL}/images/info.gif"/>
                <em><@s.text name='manage.resource.status.intro.public.migration'><@s.param><a href="${baseURL}/manage/metadata-additional.do?r=${resource.shortname}&amp;edit=Edit"><@s.text name="submenu.additional"/></a></@s.param></@s.text></em>
            </div>
        <#else>
            <div>
                <img class="info" src="${baseURL}/images/info.gif"/>
                <em><@s.text name="manage.resource.status.registration.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/></em>
            </div>
        </#if>
      </#if>

      <#--<#elseif resource.status=="PUBLIC" && resource.identifierStatus=="UNAVAILABLE">-->
        <#--<p>-->
          <#--<@s.text name="manage.resource.status.unavailable"/>-->
        <#--</p>-->
      </#if>

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
              <th><@s.text name='portal.resource.organisation.key'/></th>
              <td><a href="${cfg.portalUrl}/dataset/${resource.key}" target="_blank">${resource.key}</a>
              </td>
            </tr>
            <#if resource.organisation??>
              <#-- Warning: in dev mode organization link goes to /organization (GBIF Registry console), in prod mode the link goes to /publisher (GBIF Portal) -->
              <tr>
                <th><@s.text name="manage.overview.visibility.organisation"/></th>
                <#if cfg.getRegistryType() =='DEVELOPMENT'>
                  <td><a href="${cfg.portalUrl}/organization/${resource.organisation.key}" target="_blank">${resource.organisation.name!"Organisation"}</a></td>
                <#else>
                  <td><a href="${cfg.portalUrl}/publisher/${resource.organisation.key}" target="_blank">${resource.organisation.name!"Organisation"}</a></td>
                </#if>
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
      <@s.text name="manage.overview.resource.managers.description"><@s.param>${resource.shortname}</@s.param></@s.text>
    </p>

    <div class="details">
      <table>
        <tr>
          <th><@s.text name="manage.overview.resource.managers.creator"/></th>
          <td>${resource.creator.name}, ${resource.creator.email}</td>
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
  <form action='resource-delete.do' method='post'>
    <input name="r" type="hidden" value="${resource.shortname}" />
   	<@s.submit cssClass="button confirmDeletion" name="delete" key="button.delete"/>
  </form>
</div>
<div id="dialog"></div>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
