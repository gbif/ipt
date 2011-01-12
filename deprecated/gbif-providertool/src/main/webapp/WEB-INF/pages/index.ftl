<#assign display=JspTaglibs["http://displaytag.sf.net"] />
<#include "/WEB-INF/pages/inc/globalVars.ftl">  

<!--
  -- IMPORTANT IMPLEMENTATION NOTE
  --
  -- The default database value for PROVIDER_CFG.IPT_META_DESCRIPTION (accessible
  -- here using "config.ipt.description") is an empty string. When rendering this 
  -- template, there are two cases to handle depending on this value:
  --
  -- 1) If the value is an empty string, we assume the user hasn't completed the
  -- IPT installation yet, so we render default *internationalized* messages 
  -- (accessible here using 'webapp.installed') that confirms a successful 
  -- installation along with a link to the administration section where they can
  -- complete the installation and configure/customize their IPT instance.
  --
  -- 2) If the value is a non-empty string, then we simply render the their
  -- "cfg.ipt.description" value directly from the database and we're done.
--> 

<head>
    <title>
      <#if cfg.ipt.description == ''>
        <@s.text name="webapp.header"/>        
      <#else>
        ${cfg.ipt.title}
      </#if>
    </title>
    <meta name="menu" content="HomeMenu"/>
    <meta name="submenu" content="metadata"/>
    <meta name="decorator" content="fullsize"/>
</head>

<h1>
<#if cfg.ipt.description == ''>
  <@s.text name="webapp.header"/>        
<#else>
  ${cfg.ipt.title}
</#if>
</h1>
<div id="about">
    <#if cfg.getDescriptionImage() != ''>
        <img class="right" src="${cfg.getDescriptionImage()}" />
    </#if>
    <#if cfg.ipt.description == ''>
        <@s.text name="webapp.installed"/>
        <p>
        <a href='admin/configIpt.html'><@s.text name="webapp.configure"/></a>
    <#else>
        ${cfg.ipt.description}
    </#if>
</div>

<div class="newline"></div>

<h3><@s.text name='index.hostedresources'/></h3>
<div class="horizontal_dotted_line_xlarge_soft"></div>
<div style="height: 56px; clear: both"></div>
<#include "/WEB-INF/pages/inc/dataResourceList.ftl">  

<@s.url id="metaRepoUrl" action="resources"/>
<p class="tableMoreLink">
<@s.text name='index.additional'/> <a href="${metaRepoUrl}"><@s.text name='index.localrepository'/></a>.
</p>
