<?xml version='1.0' encoding='utf-8'?>
<#assign xmlDateFormat="yyyy-MM-dd'T'hh:mm:ss"/>
<response xmlns="http://rs.tdwg.org/tapir/1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://rs.tdwg.org/tapir/1.0  http://rs.tdwg.org/tapir/1.0/schema/tapir.xsd"
 ${nsr.xmlnsDef()}>
    <header>
        <source accesspoint="${cfg.getTapirEndpoint(resource_id)}" sendtime="${now?datetime?string(xmlDateFormat)}">
          <software name="GBIF Integrated Publishing Toolkit" version="<@s.text name="webapp.version"/>"/>
        </source>
    </header>
