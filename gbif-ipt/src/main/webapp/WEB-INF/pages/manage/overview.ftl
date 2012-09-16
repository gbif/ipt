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
			$("#toggleReport").text("Show Report");
			$('#dwcaReport').hide();
		}else{
			showReport=true;
			$("#toggleReport").text("Hide Report");
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
<div class="container_24">
  <div class="grid_18 suffix_6">
    <h1><span class="resourceOverviewTitle"><@s.text name="manage.overview.title"/>: </span>${resource.title!resource.shortname}</h1>
    <p>
      <@s.text name="manage.overview.intro"><@s.param>${resource.title!resource.shortname}</@s.param></@s.text>
    </p>
  </div>
</div>

<div class="resourceOverview" id="metadata">
  <div class="titleOverview">
    <div class="head">
      <@s.text name='manage.overview.metadata'/>
    </div>
    <div class="actions">
      <form action='metadata-basic.do' method='get'>
        <input name="r" type="hidden" value="${resource.shortname}"/>
        <@s.submit name="edit" key="button.edit"/>
      </form>
    </div>
    <#if missingMetadata>
      <p class="warn">
        <@s.text name='manage.overview.missing.metadata'/>
      </p>
    </#if>
  </div>
  <div class="bodyOverview">
    <p>
      <#assign no_description><@s.text name='manage.overview.no.description'/></#assign>
			<@description resource.description!no_description 100/>
    </p>

    <div class="details">
      <table>
        <#if resource.eml.subject?has_content>
          <tr>
            <th><@s.text name='portal.resource.summary.keywords'/></th>
            <td><@description resource.eml.subject!no_description 90/></td>
          </tr>
        </#if>
        <#assign text>
          <#list resource.eml.taxonomicCoverages as tc>
            <#list tc.taxonKeywords as k>
              ${k.scientificName!}<#if k_has_next>, </#if>
            </#list>
            <#if tc_has_next>; </#if>
          </#list>
        </#assign>
        <#if resource.eml.taxonomicCoverages?has_content>
          <tr>
            <th><@s.text name='portal.resource.summary.taxcoverage'/></th>
            <td><@description text!no_description 90/></td>
          </tr>
        </#if>
        <#assign text>
          <#list resource.eml.geospatialCoverages as geo>
            ${geo.description!}<#if geo_has_next>; </#if>
          </#list>
        </#assign>
        <#if resource.eml.geospatialCoverages?has_content>
          <tr>
            <th><@s.text name='portal.resource.summary.geocoverage'/></th>
            <td><@description text!no_description 90/></td>
          </tr></#if>
      </table>
    </div>
  </div>
</div>

<div class="resourceOverview" id="sources">
  <div class="titleOverview">
    <div class="head">
      <@s.text name='manage.overview.source.data'/>
    </div>
    <div class="actions">
      <form action='addsource.do' method='post' enctype="multipart/form-data">
        <input name="r" type="hidden" value="${resource.shortname}"/>
        <input name="validate" type="hidden" value="false"/>
        <@s.file name="file" key="manage.resource.create.file"/>
        <@s.submit name="add" key="button.connectDB"/>
        <@s.submit name="clear" key="button.clear"/>
        <div style="display: none;">
          <@s.submit name="cancel" key="button.cancel" method="cancelOverwrite"/>
        </div>
      </form>
    </div>
  </div>
  <div class="bodyOverview">
    <p>
      <@s.text name='manage.overview.source.description1'><@s.param><@s.text name="button.add"/></@s.param></@s.text>
      &nbsp;
      <@s.text name='manage.overview.source.description2'><@s.param><@s.text name="button.connectDB"/></@s.param></@s.text>
    </p>

    <div class="details">
      <table>
        <#list resource.sources as src>
          <tr>
            <#if src.rows?exists>
              <th>${src.name} <@s.text name='manage.overview.source.file'/></th>
              <td>${src.fileSizeFormatted},&nbsp;${src.rows}&nbsp;<@s.text name='manage.overview.source.rows'/>,&nbsp;${src.columns}&nbsp;<@s.text name='manage.overview.source.columns'/>.&nbsp;${(src.lastModified?datetime?string)!}<#if !src.readable>&nbsp;<img src="${baseURL}/images/warning.gif"/></#if></td>
            <#else>
              <th>${src.name} <@s.text name='manage.overview.source.sql'/></th>
              <td>db=${src.database!"..."},&nbsp;${src.columns}&nbsp;<@s.text name='manage.overview.source.columns'/>.<#if !src.readable>&nbsp;<img src="${baseURL}/images/warning.gif"/></#if></td>
            </#if>
            <td>
              <a class="button" href="source.do?r=${resource.shortname}&id=${src.name}">
                <input class="button" type="button" value='<@s.text name='button.edit'/>'/>
              </a>
            </td>
          </tr>
        </#list>
      </table>
    </div>
  </div>
</div>

<div class="resourceOverview" id="mappings">
  <div class="titleOverview">
    <div class="head">
      <@s.text name='manage.overview.DwC.Mappings'/>
    </div>
    <div class="actions">
      <#if (potentialExtensions?size>0)>
        <form action='mapping.do' method='post'>
          <input name="r" type="hidden" value="${resource.shortname}"/>
          <select name="id" id="rowType" size="1">
            <#-- if core hasn't been selected yet add help text to help user choose core type -->
            <#if !resource.coreType?has_content || resource.coreType?lower_case == "other" >
              <option><@s.text name='manage.overview.DwC.Mappings.select'/></option>
            </#if>
            <#list potentialExtensions as e>
              <option value="${e.rowType}">${e.title}</option>
            </#list>
          </select>
          <@s.submit name="add" key="button.add"/>
        </form>
      <#else>
        <#if (resource.sources?size>0) && !resource.hasCore()>
          <div class="warn">
            <@s.text name='manage.overview.no.DwC.extensions'/>
          </div>
        </#if>
      </#if>
    </div>
  </div>
  <div class="bodyOverview">
    <p>
      <@s.text name='manage.overview.DwC.Mappings.description'/>
    </p>
    <#-- if core hasn't been selected yet add help text to help user understand how to choose core type -->
    <#if (potentialExtensions?size>0) && (!resource.coreType?has_content || resource.coreType?lower_case == "other") >
      <p>
        <img class="info" src="${baseURL}/images/info.gif"/>
        <em><@s.text name='manage.overview.DwC.Mappings.coretype.description'/>
      </p>
    </#if>

    <div class="details">
      <table>
        <#list resource.coreMappings as m>
          <tr>
            <th><#if m_index==0>${m.extension.title}</#if></th>
            <td>${m.fields?size} <@s.text name='manage.overview.DwC.Mappings.terms'/> ${(m.source.name)!}</td>
            <td>
              <a class="button" href="mapping.do?r=${resource.shortname}&id=${m.extension.rowType}&mid=${m_index}">
                <input class="button" type="button" value='<@s.text name='button.edit'/>'/>
              </a>
            </td>
          </tr>
        </#list>
        <#list resource.getMappedExtensions() as ext>
          <#if !ext.isCore()>
            <#list resource.getMappings(ext.rowType) as m>
              <tr>
                <th><#if m_index==0>${ext.title}</#if></th>
                <td>${m.fields?size} <@s.text name='manage.overview.DwC.Mappings.terms'/> ${(m.source.name)!}</td>
                <td>
                  <a class="button" href="mapping.do?r=${resource.shortname}&id=${ext.rowType}&mid=${m_index}">
                    <input class="button" type="button" value='<@s.text name='button.edit'/>'/>
                  </a>
                </td>
              </tr>
            </#list>
          </#if>
        </#list>
      </table>
    </div>
  </div>
</div>

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
        <form action='publish.do' method='post'>
          <input name="r" type="hidden" value="${resource.shortname}"/>
          <@s.submit name="publish" key="button.publish" />
        </form>
      </#if>
    </div>
  </div>
  <div class="bodyOverview">
    <p>
      <@s.text name="manage.overview.published.description"/>
    </p>
    <#if missingMetadata>
      <div>
        <img class="info" src="${baseURL}/images/info.gif"/>
        <em><@s.text name="manage.overview.published.missing.metadata"/></em>
      </div>
    </#if>
    <div class="details">
      <table>
        <#if resource.lastPublished??>
          <tr>
            <th><@s.text name="manage.overview.published.last.publication"/></th>
            <td>
              <@s.text name="manage.overview.published.version"/>
              ${resource.eml.emlVersion}
              <@s.text name="manage.overview.published.from"/>
              ${resource.lastPublished?datetime?string}
              <!-- reserve a little extra space here to highlight see report link -->
              &nbsp;
              <#if report??>
                <a id="toggleReport" href="#">
                  <@s.text name="manage.overview.published.see.report"/>
                </a>
                <!-- ensure a space separates see report link, and publication log link-->
                &nbsp;
              </#if>
              <a href="${baseURL}/publicationlog.do?r=${resource.shortname}"><@s.text name='portal.publication.log'/></a>
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
        <div>
          <img class="info" src="${baseURL}/images/info.gif"/>
          <em><@s.text name="manage.resource.status.intro.public.migration"/></em>
        </div>
        <#if organisations?size==0>
          <div>
            <img class="info" src="${baseURL}/images/info.gif"/>
            <em><@s.text name="manage.overview.visibility.no.organisations"/></em>
          </div>
        </#if>
      <#else>
        <div>
          <img class="info" src="${baseURL}/images/info.gif"/>
          <em><@s.text name="manage.resource.status.registration.forbidden"/></em>
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
          <#if resource.organisation?exists>
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
