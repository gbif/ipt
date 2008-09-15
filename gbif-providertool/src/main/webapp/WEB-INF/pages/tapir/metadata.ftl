<#assign page=JspTaglibs["http://www.opensymphony.com/sitemesh/page"]>
<@page.applyDecorator name="tapir" title="ping">
<@page.param name="tapir.content">
<#escape x as x?xml>
<metadata>
    <dc:title>${resource.title}</dc:title>
    <dc:type>http://purl.org/dc/dcmitype/Service</dc:type>
    <accesspoint>${cfg.getTapirEndpoint(resource_id)}</accesspoint>
    <dc:description>${resource.description}</dc:description>
    <dc:language>en</dc:language>
    <!--
    <dc:subject>dragonflies dragonfly observation specimen arthropoda insecta odonata</dc:subject>
    <dct:bibliographicCitation>Global Dragonflies Database</dct:bibliographicCitation>
    <dc:rights>Creative Commons License</dc:rights>
    -->
    <dct:modified>${resource.modified?datetime?string.medium}</dct:modified>
    <dct:created>2006-01-01T00:00:00+01:00</dct:created>
    <!--
    <indexingPreferences startTime="01:30:00Z" maxDuration="PT1H" frequency="P1M" />
    -->
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
        <!--
        <geo:Point>
          <geo:lat>45.256</geo:lat>
          <geo:long>-71.92</geo:long>
        </geo:Point>
        -->
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
      </entity>
    </relatedEntity>
    <custom>
    	<ipt:logoURL>${cfg.getResourceLogoUrl(resource_id)}</ipt:logoURL>
    </custom>
  </metadata>
</#escape>
</@page.param>
</@page.applyDecorator>  