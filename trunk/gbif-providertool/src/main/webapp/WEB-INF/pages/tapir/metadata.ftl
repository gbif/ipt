<#assign page=JspTaglibs["http://www.opensymphony.com/sitemesh/page"]>
<@page.applyDecorator name="tapir" title="ping">
<@page.param name="tapir.content">
<metadata>
    <dc:title>Global Dragonflies Database</dc:title>
    <dc:type>http://purl.org/dc/dcmitype/Service</dc:type>
    <accesspoint>http://example.net/tapir.cgi</accesspoint>
    <dc:description>Global database about Dragonflies observation and specimen records</dc:description>
    <dc:language>en</dc:language>
    <dc:subject>dragonflies dragonfly observation specimen arthropoda insecta odonata</dc:subject>
    <dct:bibliographicCitation>Global Dragonflies Database</dct:bibliographicCitation>
    <dc:rights>Creative Commons License</dc:rights>
    <dct:modified>2006-07-01T09:35:14+01:00</dct:modified>
    <dct:created>2006-01-01T00:00:00+01:00</dct:created>
    <indexingPreferences startTime="01:30:00Z" maxDuration="PT1H" frequency="P1M" />
    <relatedEntity>
      <role>data supplier</role>
      <entity>
        <identifier>http://purl.org/biodiv/myorg</identifier>
        <name>My Organisation</name>
        <acronym>MYORG</acronym>
        <logoURL>http://example.net/myorg.png</logoURL>
        <description>My Organisation hosts and maintains biodiversity databases</description>
        <relatedInformation>http://example.net/myorg</relatedInformation>
        <hasContact>
          <role>data administrator</role>
          <vcard:VCARD>
            <vcard:FN>My Name</vcard:FN>
            <vcard:TITLE>Director</vcard:TITLE>
            <vcard:TEL>11 11 11111111</vcard:TEL>
            <vcard:EMAIL>myname@example.net</vcard:EMAIL>
          </vcard:VCARD>
        </hasContact>
        <geo:Point>
          <geo:lat>45.256</geo:lat>
          <geo:long>-71.92</geo:long>
        </geo:Point>
      </entity>
    </relatedEntity>
  </metadata>
</@page.param>
</@page.applyDecorator>  