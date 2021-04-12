<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
 <title><@s.text name="admin.home.manageExtensions"/></title>
 <script type="text/javascript" src="${baseURL}/js/jconfirmation.jquery.js"></script>

<script type="text/javascript">
    $(document).ready(function(){
        initHelp();
        $('.confirm').jConfirmAction({question : "<@s.text name='admin.extension.update.confirm'/>", yesAnswer : "<@s.text name='basic.yes'/>", cancelAnswer : "<@s.text name='basic.no'/>"});
    });
</script>

<#macro extensionRow ext>
  <a name="${ext.rowType}"></a>
  <div class="definition">
      <div class="title">
          <div class="head">
              <a href="extension.do?id=${ext.rowType?url}">${ext.title}</a>
						<#if !ext.isLatest()>
                <img class="infoImg" src="${baseURL}/images/warning.gif"/>
                <div class="info autop">
									<@s.text name="admin.extension.version.warning"/>
                </div>
						</#if>
          </div>
          <div class="actions">
						<#if !ext.isLatest()>
                <form action='updateExtension.do' method='post'>
                    <input type='hidden' name='id' value='${ext.rowType}' />
									<@s.submit cssClass="confirm" name="update" key="button.update"/>
                </form>
						</#if>
              <form action='extension.do' method='post'>
                  <input type='hidden' name='id' value='${ext.rowType}' />
								<@s.submit name="delete" key="button.remove"/>
              </form>
          </div>
      </div>
      <div class="body">
          <div>
              <p>${ext.description!}
								<#if ext.link?has_content><br/><@s.text name="basic.seealso"/> <a href="${ext.link}">${ext.link}</a></#if></p>
          </div>
          <div class="details">
              <table>
								<#if ext.issued??>
                    <tr><th><@s.text name="basic.issued"/></th><td>${ext.issued?date?string.medium}</td></tr>
								</#if>
                  <tr><th><@s.text name="extension.properties"/></th><td>${ext.properties?size}</td></tr>
                  <tr><th><@s.text name="basic.name"/></th><td>${ext.name}</td></tr>
                  <tr><th><@s.text name="basic.namespace"/></th><td>${ext.namespace}</td></tr>
                  <tr><th><@s.text name="extension.rowtype"/></th><td>${ext.rowType}</td></tr>
								<#if ext.subject?has_content>
                    <tr><th><@s.text name="basic.keywords"/></th><td>${ext.subject!}</td></tr>
								</#if>
              </table>
          </div>
      </div>
  </div>
</#macro>

	<#assign currentMenu = "admin"/>
	<#include "/WEB-INF/pages/inc/menu.ftl">

<div class="grid_23">
  <h1><@s.text name="admin.extension.coreTypes"/></h1>
  <p>
    <@s.text name="admin.extension.no.coreTypes.installed.help"><@s.param>${cfg.registryUrl}</@s.param></@s.text>
  </p>

<#assign count=0>
<#list extensions as ext>
  <#if ext.core>
    <#assign count=count+1>
    <@extensionRow ext/>
  </#if>
</#list>
<#if count=0>
  <p class="warn">
    <@s.text name="admin.extension.no.coreTypes.installed"/>
  </p>
  <p>
    <img src="${baseURL}/images/warning.gif"/>
    <@s.text name="admin.extension.no.coreTypes.installed.debug"><@s.param>${cfg.registryUrl}</@s.param></@s.text>
  </p>
</#if>
</div>

<div class="grid_23">
<h1><@s.text name="admin.extension.extensions"/></h1>
  <p>
  <@s.text name="admin.extension.no.extensions.installed.help"><@s.param>${cfg.registryUrl}</@s.param></@s.text>
  </p>
<#assign count=0>
<#list extensions as ext>
  <#if !ext.core>
    <#assign count=count+1>
	  <@extensionRow ext/>
  </#if>
</#list>
<#if count=0>
  <p class="warn">
    <@s.text name="admin.extension.no.extensions.installed"/>
  </p>
</#if>

</div>
<hr/>
<div class="grid_23">
  <h3><@s.text name="extension.synchronise.title"/></h3>
  <p><@s.text name="admin.extensions.synchronise.help"/></p>
	<#if lastSynchronised?has_content>
      <p><@s.text name="extension.last.synchronised"><@s.param>${lastSynchronised?date?string("yyyy-MM-dd HH:mm:ss")}</@s.param></@s.text></p>
	</#if>
	<form action='extensions.do' method='post'>
    <@s.submit name="synchronise" key="button.synchronise"/>
  </form><br/>
</div>
<hr/>

<div class="grid_23">
<h3><@s.text name="extension.further.title"/></h3>
  <p>
    <@s.text name="extension.further.title.help"/>
  </p>

<#assign count=0>
<#list newExtensions as ext>
<#assign count=count+1>
<div class="definition">	
  <div class="title">
  	<div class="head">
		${ext.title}
  	</div>
  	<div class="actions">
	  <form action='extension.do' method='post'>
		<input type='hidden' name='url' value='${ext.url}' />
		<@s.submit name="install" key="button.install"/>
  	  </form>
  	</div>
  </div>
  <div class="body">
      	<div>
		<p>${ext.description!}</p>
      	</div>
      	<div class="details">
      		<table>
          		<tr><th><@s.text name="extension.rowtype"/></th><td>${ext.rowType!}</td></tr>
          		<tr><th><@s.text name="basic.keywords"/></th><td>${ext.subject!}</td></tr>
      		</table>
      	</div>
  </div>
</div>
</#list>
<#if count=0>
	<@s.text name="extension.already.installed"/>
</#if>
</div>
<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>