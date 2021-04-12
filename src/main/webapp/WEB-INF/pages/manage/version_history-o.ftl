<#escape x as x?html>
  <#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.source.title'/></title>
<script type="text/javascript" src="${baseURL}/js/jconfirmation.jquery.js"></script>
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<div class="grid_18 suffix_6">
  <h1><span class="superscript"><@s.text name='manage.overview.title.label'/></span>
    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
  </h1>
</div>
<div class="grid_24">
    <form action="history.do" method="post">

       <#if resource?? && version?? && resource.versionHistory??>
          <input type="hidden" name="r" value="${resource.shortname}" />
          <input type="hidden" name="v" value="${version.toPlainString()}" />
          <h2 class="subTitle"><@s.text name='manage.history.title'/></h2>

          <p><@s.text name="manage.history.intro"><@s.param>${version.toPlainString()}</@s.param><@s.param>${resource.title!resource.shortname}</@s.param></@s.text></p>

          <#assign versionTitle><@s.text name="manage.overview.published.version"/></#assign>
          <#assign releasedTitle><@s.text name="manage.overview.published.released"/></#assign>
          <#assign recordsTitle><@s.text name="portal.home.records"/></#assign>
          <#assign modifiedTitle><@s.text name="portal.home.modifiedBy"/></#assign>
          <#assign summaryTitle><@s.text name="portal.home.summary"/></#assign>
          <#assign doiTitle><@s.text name="portal.home.doi"/></#assign>
          <#assign none><@s.text name="basic.none"/></#assign>
          <#assign emptyPlaceholder="-"/>

          <div class="clearfix">
              <#list resource.versionHistory as history>
                <#if history.version == version.toPlainString()>
                    <table id="${history.version}" class="simple history">
                        <tr>
                            <th>${versionTitle?cap_first}</th>
                            <td>${history.version}</td>
                        </tr>
                        <tr>
                            <th>${doiTitle}</th>
                          <#if history.status! == "PUBLIC">
                            <#if history.doi??>
                                <td>${history.doi.getDoiName()}</td>
                            <#else>
                                <td>${none}</td>
                            </#if>
                          <#else>
                              <td>${emptyPlaceholder}</td>
                          </#if>
                        </tr>
                        <tr>
                            <th>${releasedTitle?cap_first}</th>
                            <#if history.released??>
                              <td>${history.released?date!}</td>
                            <#else>
                              <td>${emptyPlaceholder}</td>
                            </#if>
                        </tr>
                        <tr>
                            <th>${recordsTitle?cap_first}</th>
                            <td>${history.recordsPublished}</td>
                        </tr>
                        <tr>
                            <th>${modifiedTitle?cap_first}</th>
                            <#if history.modifiedBy??>
                              <td>${history.modifiedBy.firstname!} ${history.modifiedBy.lastname!}</td>
                            <#else>
                              <td>${emptyPlaceholder}</td>
                            </#if>
                        </tr>
                        <tr>
                            <th>${summaryTitle?cap_first}</th>
                            <td>
                                <textarea name="summary" cols="60" rows="5" placeholder="Please summarize what has changed in this version">${history.changeSummary!}</textarea>
                            </td>
                        </tr>
                    </table>
                </#if>
              </#list>
          </div>

          <div class="buttons">
            <@s.submit cssClass="button" name="save" key="button.save"/>
	          <@s.submit cssClass="button" name="back" key="button.cancel"/>
          </div>
       <#else>
          <div class="buttons">
            <@s.submit cssClass="button" name="cancel" key="button.back"/>
          </div>
       </#if>
    </form>
</div>

  <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>