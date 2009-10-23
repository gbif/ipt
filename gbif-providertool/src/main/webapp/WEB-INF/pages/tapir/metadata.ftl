<?xml version='1.0' encoding='utf-8'?>
<#include "/WEB-INF/pages/inc/globalVars.ftl">  
<#if envelope>
 <#include "/WEB-INF/pages/tapir/header.ftl">  
 <metadata>
<#else>
 <metadata ${nsr.xmlnsDef()}>  
</#if>
<#escape x as x?xml>
    <dc:title>${resource.title}</dc:title>
    <dc:type>http://purl.org/dc/dcmitype/Service</dc:type>
    <accesspoint>${cfg.getTapirEndpoint(resourceId)}</accesspoint>
    <dc:description>${resource.description!}</dc:description>
    <dc:language>${eml.language!}</dc:language>
    <dc:subject><#list eml.keywords as k>${k} </#list></dc:subject>
    <dc:rights>${eml.intellectualRights!}</dc:rights>
    <dc:modified>${resource.modified?datetime?string(xmlDateFormat)}</dc:modified>
    <dc:created>${resource.created?datetime?string(xmlDateFormat)}</dc:created>
    <relatedEntity>
      <role>data supplier</role>
      <entity>
        <name>${resource.meta.contactName!}</name>
        <hasContact>
          <role>data administrator</role>
          <vcard:VCARD>
            <vcard:FN>${resource.meta.contactName!}</vcard:FN>
            <vcard:EMAIL>${resource.meta.contactEmail!}</vcard:EMAIL>
          </vcard:VCARD>
        </hasContact>
      </entity>
    </relatedEntity>
    <relatedEntity>
      <role>technical host</role>
      <entity>
        <name>${cfg.ipt.title!}</name>
        <description>${cfg.ipt.description!}</description>
        <hasContact>
          <role>system administrator</role>
          <vcard:VCARD>
            <vcard:FN>${cfg.ipt.contactName!}</vcard:FN>
            <vcard:EMAIL>${cfg.ipt.contactEmail!}</vcard:EMAIL>
          </vcard:VCARD>
        </hasContact>
        <#if (cfg.ipt.location.latitude)??>
        <geo:Point>
          <geo:lat>${cfg.ipt.location.latitude}</geo:lat>
          <geo:long>${cfg.ipt.location.longitude}</geo:long>
        </geo:Point>
        </#if>
      </entity>
    </relatedEntity>
    <custom>
    	<ipt:logoURL>${cfg.getResourceLogoUrl(resourceId)}</ipt:logoURL>‚‚
    </custom>
  </metadata>
</#escape>
<#if envelope>
 <#include "/WEB-INF/pages/tapir/footer.ftl">  
</#if>
