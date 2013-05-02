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
  <script type="text/javascript" src="${baseURL}/js/jconfirmation_publish.js"></script>
<script type="text/javascript">
$(document).ready(function(){	
	<#if confirmOverwrite>
		showConfirmOverwrite();
	</#if>	
	var $registered = false;

	$('.confirm').jConfirmAction({question : "<@s.text name='basic.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>"});
	$('.confirmRegistration').jConfirmAction({question : "<@s.text name='manage.overview.visibility.confirm.registration'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>", checkboxText: "<@s.text name='manage.overview.visibility.confirm.agreement'/>"});	
	$('.confirmDeletion').jConfirmAction({question : "<#if resource.status=='REGISTERED'><@s.text name='manage.resource.delete.confirm.registered'/><#else><@s.text name='basic.confirm'/></#if>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>"});

  // dialog used to configure auto-publishing
  <#if action.qualifiesForAutoPublishing()>
      $('.confirmAutoPublish').jConfirmPublishAction({checkboxTextOff : "<@s.text name='autopublish.never'/>", checkboxText : "<@s.text name='autopublish.on'/>", yesAnswerChecked : "<@s.text name="autopublish.yesChecked"><@s.param>${resource.emlVersion + 1}</@s.param></@s.text>", question : "<@s.text name="autopublish.confirm"><@s.param>${resource.updateFrequency.identifier}</@s.param></@s.text>", yesAnswer : "<@s.text name="autopublish.yes"><@s.param>${resource.emlVersion + 1}</@s.param></@s.text>", cancelAnswer : "<@s.text name='button.cancel'/>"});
  </#if>

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
<div class="container_24">
  <div class="grid_18 suffix_6">
    <h1><span class="resourceOverviewTitle"><@s.text name="manage.overview.title"/>: </span>${resource.title!resource.shortname}</h1>
    <p>
      <@s.text name="manage.overview.intro"><@s.param>${resource.title!resource.shortname}</@s.param></@s.text>
    </p>
  </div>
</div>

<!-- when resource is of type metadata-only, there is no need to show source data and mapping sections -->
<#assign metadataType = "metadata"/>
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
    <#else>
      <#if resource.status=="REGISTERED">
        <#if !currentUser.hasRegistrationRights()>
            <div>
                <img class="info" src="${baseURL}/images/info.gif"/>
                <em><@s.text name="manage.resource.status.registration.forbidden"/>
                    &nbsp;<@s.text name="manage.resource.publish.forbidden"/>
                    &nbsp;<@s.text name="manage.resource.role.change"/></em>
            </div>
        </#if>
      </#if>
      <#if !resource.usesAutoPublishing() && !resource.hasDisabledAutoPublishing()>
          <div>
              <img class="info" src="${baseURL}/images/info.gif"/>
              <em><@s.text name='autopublish.intro'><@s.param><a
                      href="${baseURL}/manage/metadata-basic.do?r=${resource.shortname}&amp;edit=Edit"><@s.text name="submenu.basic"/></a></@s.param></@s.text>
              </em>
          </div>
      </#if>
    </#if>
      <br/>
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
                ${resource.lastPublished?datetime?string}
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
              <a href="${baseURL}/eml.do?r=${resource.shortname}"><@s.text name="manage.overview.published.download"/></a>
              <a href="${baseURL}/resource.do?r=${resource.shortname}"><@s.text name="manage.overview.published.view"/></a>
              (${emlFormattedSize})
            </td>
          </tr>
          <tr>
            <th><@s.text name="portal.resource.published.rtf"/></th>
            <td>
              <a href="${baseURL}/rtf.do?r=${resource.shortname}"><@s.text name="manage.overview.published.download"/></a>
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
      <em class="<#if resource.status=="PRIVATE">RED<#else>green</#if>"><@s.text name="resource.status.${resource.status?lower_case}"/></em>
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
