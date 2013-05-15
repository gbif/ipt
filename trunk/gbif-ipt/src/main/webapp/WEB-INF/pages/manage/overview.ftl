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
		var usedFileName = $("#file").attr("value");	
		if(usedFileName != "") {			
			$("#add").attr("value", '<@s.text name="button.add"/>');
		}
	});
	$("#clear").click(function(event) {
		event.preventDefault();
		$("#file").attr("value", "");
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
      <#if cfg.devMode() && cfg.getRegistryType()!='PRODUCTION'>
          <p class="warn">
            <@s.text name="manage.overview.published.testmode.warning"/>
          </p>
      </#if>
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
    <p>
      <#if resource.coreType?has_content && resource.coreType==metadataType>
        <@s.text name="manage.overview.published.description.metadataOnly"/>
      <#else>
        <@s.text name="manage.overview.published.description"/>
      </#if>
    </p>

    <#if missingMetadata>
        <div>
            <img class="info" src="${baseURL}/images/info.gif"/>
            <em><@s.text name="manage.overview.published.missing.metadata"/></em>
        </div>
        <br/>
    <#else>
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
      <div class="details">
      <table>
        <#if resource.lastPublished??>
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
                  <#if report.hasException()>
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
        </#if>
      </table>
    </div>
  </div>
</div>

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
        <#if resource.status=="PUBLIC">
          <#if currentUser.hasRegistrationRights() && (organisations?size>0)>
            <select name="id" id="org" size="1">
              <#list organisations as o>
                <option value="${o.key}">${o.alias!o.name}</option>
              </#list>
            </select>

            <@s.submit cssClass="confirmRegistration" name="publish" key="button.register" disabled="${missingRegistrationMetadata?string}"/>
            <#if missingRegistrationMetadata>
              <p class="warn">
                <@s.text name="manage.overview.visibility.missing.metadata"/>
              </p>
            </#if>
          </#if>
        <#else>
          <#if resource.status=="PRIVATE">
            <@s.submit name="publish" key="button.public"/>
          </#if>
        </#if>
      </form>

      <#if resource.status=="PUBLIC">
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
    <#if resource.status=="PUBLIC">
      <#if currentUser.hasRegistrationRights()>
        <@s.text name="manage.resource.status.registration.intro"/>
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
    </p>

    <div class="details">
      <table>
        <#if resource.status=="REGISTERED" && resource.key??>
          <tr>
            <th><@s.text name="manage.overview.visibility.resource.key"/></th>
            <td>${resource.key} <a href="${cfg.registryUrl}/browse/agent?uuid=${resource.key}" target="_blank">GBRDS</a>
            </td>
          </tr>
          <#if resource.organisation??>
            <tr>
              <th><@s.text name="manage.overview.visibility.organisation"/></th>
              <td>${resource.organisation.name!}</td>
            </tr>
            <tr>
              <th><@s.text name="manage.overview.visibility.organisation.contact"/></th>
              <td>${resource.organisation.primaryContactName!}, ${resource.organisation.primaryContactEmail!}</td>
            </tr>
            <tr>
              <th><@s.text name="manage.overview.visibility.organisation.key"/></th>
              <td>${resource.organisation.key!}</td>
            </tr>
            <tr>
              <th><@s.text name="manage.overview.visibility.endorsing.node"/></th>
              <td>${resource.organisation.nodeName!}</td>
            </tr>
          </#if>
        </#if>
      </table>
    </div>
  </div>
</div>

<div class="resourceOverview" id="managers">
  <div class="titleOverview">
    <div class="head">
      <@s.text name="manage.overview.resource.managers"/>
    </div>
    <div class="actions">
      <#if (potentialManagers?size>0)>
        <form action='resource-addmanager.do' method='post'>
          <input name="r" type="hidden" value="${resource.shortname}"/>
          <select name="id" id="manager" size="1">
            <option value=""></option>
            <#list potentialManagers?sort_by("name") as u>
              <option value="${u.email}">${u.name}</option>
            </#list>
          </select>
          <@s.submit name="add" key="button.add"/>
        </form>
      </#if>
    </div>
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
        <#list resource.managers as u>
        ${u}
          <tr>
            <th><@s.text name="manage.overview.resource.managers.manager"/></th>
            <td>${u.name}, ${u.email}&nbsp;
              <a class="button" href="resource-delmanager.do?r=${resource.shortname}&id=${u.email}">
                <input class="button" type="button" value='<@s.text name='button.delete'/>'/>
              </a>
            </td>
          </tr>
        </#list>
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
