<?xml version='1.0' encoding='utf-8'?>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page"%>
<%@ taglib uri="/struts-tags" prefix="s" %>
<response xmlns="http://rs.tdwg.org/tapir/1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://rs.tdwg.org/tapir/1.0  http://rs.tdwg.org/tapir/1.0/schema/tapir.xsd"
	  xmlns:ipt="http://www.gbif.org/ipt"
	  xmlns:dc="http://purl.org/dc/elements/1.1/"
	  xmlns:dct="http://purl.org/dc/terms/"
	  xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#"
	  xmlns:vcard="http://www.w3.org/2001/vcard-rdf/3.0#">
    <header>
        <source accesspoint="http://accesspoint_url" sendtime="<s:property value="%{now}"/>"></source>
        <decorator:getProperty property="tapir.header" default=""/>
    </header>
    <decorator:getProperty property="tapir.content" />
    <diagnostics>
    	<diagnostic></diagnostic>
    </diagnostics>
</response>