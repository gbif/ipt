<#include "/WEB-INF/pages/tapir/header.ftl">  
<#escape x as x?xml>
<metadata>
    <dc:title>${resource.title}</dc:title>
    <dc:type>http://purl.org/dc/dcmitype/Service</dc:type>
    <accesspoint>${cfg.getTapirEndpoint(resource_id)}</accesspoint>
    <dc:description>${resource.description}</dc:description>
    <dc:language>${eml.language!}</dc:language>
    <dc:subject><#list eml.keywords as k>${k} </#list></dc:subject>
    <dc:rights>${eml.intellectualRights!}</dc:rights>
    <dct:modified>${resource.modified?datetime?string(xmlDateFormat)}</dct:modified>
    <dct:created>${resource.created?datetime?string(xmlDateFormat)}</dct:created>
    <relatedEntity>
      <role>data supplier</role>
      <entity>
        <name>${resource.meta.contactName}</name>
        <hasContact>
          <role>data administrator</role>
          <vcard:VCARD>
            <vcard:FN>${resource.meta.contactName}</vcard:FN>
            <vcard:EMAIL>${resource.meta.contactEmail}</vcard:EMAIL>
          </vcard:VCARD>
        </hasContact>
      </entity>
    </relatedEntity>
    <relatedEntity>
      <role>technical host</role>
      <entity>
        <name>${cfg.title}</name>
        <description>${cfg.description}</description>
        <hasContact>
          <role>system administrator</role>
          <vcard:VCARD>
            <vcard:FN>${cfg.contactName}</vcard:FN>
            <vcard:EMAIL>${cfg.contactEmail}</vcard:EMAIL>
          </vcard:VCARD>
        </hasContact>
        <geo:Point>
          <geo:lat>${cfg.meta.location.latitude}</geo:lat>
          <geo:long>${cfg.meta.location.longitude}</geo:long>
        </geo:Point>
      </entity>
    </relatedEntity>
    <custom>
    	<ipt:logoURL>${cfg.getResourceLogoUrl(resource_id)}</ipt:logoURL>
    </custom>
  </metadata>
</#escape>
<#include "/WEB-INF/pages/tapir/footer.ftl">  
