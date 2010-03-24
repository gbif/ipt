<#include "/WEB-INF/pages/inc/globalVars.ftl">  
<response xmlns="http://rs.tdwg.org/tapir/1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://rs.tdwg.org/tapir/1.0  http://rs.tdwg.org/tapir/1.0/schema/tapir.xsd"
 ${nsr.xmlnsDef()}>
    <header>
<#escape x as x?xml>
        <source accesspoint="${cfg.getTapirEndpoint(resourceId)}" sendtime="${now?datetime?string(xmlDateFormat)}">
          <software name="GBIF Integrated Publishing Toolkit" version="${cfg.version}"/>
        </source>
</#escape>
    </header>
