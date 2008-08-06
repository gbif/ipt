<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceOverview.title"/></title>
    <meta name="heading" content="<s:property value="occResource.title"/>"/>
    <meta name="submenu" content="search"/>
	<script src="/scripts/FancyZoom.js" type="text/javascript"></script>
	<script src="/scripts/FancyZoomHTML.js" type="text/javascript"></script>
	<s:head theme="ajax" debug="true"/>
</head>



<body onload="setupZoom()">

<s:form>
<fieldset>
	<legend><s:text name="occResource.description"/></legend>
	<div id="metadata">
		<s:property value="occResource.description"/>
		<s:label key="occResourceOverview.cache" value="%{occResource.lastUpload.recordsUploaded} records uploaded %{occResource.lastUpload.executionDate}"/>
		<ul class="minimenu">
			<li>
				<a onclick="Effect.toggle('services', 'blind', { duration: 0.3 }); return false;">(<s:text name="occResourceOverview.services"/>)</a>
			</li>
		</ul>
		<div id="services" style="display:none">
			<s:label key="occResourceOverview.tabfile" value="%{occResource.getDumpArchiveUrl()}"/>
			<s:label key="occResourceOverview.tapir" value="%{occResource.getTapirEndpoint()}"/>
			<s:label key="occResourceOverview.wfs" value="%{occResource.getWfsEndpoint()}"/>
		</div>
	</div>
</s:form>


<div id="loc-stats" class="stats clearfix">
	<label>Geographic Stats</label>
	<ul class="plain">
		<li>3421 with coordinates, precision from 0 to 10000 meter</li> 
		<li>1673 with altitude, precision from 0 to 10000 meter</li> 
	</ul>
</div>


<div class="clearfix">
</div>


<div id="loc-countries" class="stats map">
	<label tip="hallo">Occurrences by Country</label>
	<img src="http://chart.apis.google.com/chart?chs=320x160&cht=t&chtm=world&chco=cccccc,fff5f0,99000d&chld=SENLAUFONZKRCALUJPUSBMLIISBBNOSIGBFIDKEEMCCHHKATITJMBELVDEFRMYESSGCYGUMOBNAGSKBYIEMQADHRNCBSMTHUAECLQAREPTCZVIKWSCPLLTILMUAWPRROGFPFGYCRBHUYBABGLBGPGRARBRMXDOPETRMARUSTMDVNJOFMVETTSATHOMIRZACOMNUABZTNSVZWVCCNFJAZSDGTMKECIDEGSRHTPKPSPAGECVALTGSYDZBJPHINAMKGBOGASNKZNASZLYGMNGHNVUBWWSUZPYKEBTTOLSZMKMNIGWKIPGGHERLKUGCUDJCMGQCGYESOCITZAFTMMZSBNPMRGNAORWBFMLBIMGMWTDLABDCFKHTJETCDNEMM&chd=t:58.1,56.4,53.6,53.5,52,52,51.7,51.6,50.7,50.5,49.9,48.2,47.4,45.3,44.6,42.2,41,40.6,40,39.5,38.9,38.8,38.2,37.3,36.7,35.4,34.8,34,32.9,32.9,32.3,30.8,30.3,29.7,29.3,28.2,27.5,27.1,26.9,26.5,25.8,25.1,24.8,24.7,24.5,24.3,24.2,22.6,22.4,22,21.5,21.3,20.7,20.6,20.4,19.9,19.8,19.7,19.7,18.6,18.4,18.4,17.6,16.8,16.8,16.4,16.2,16.2,16.2,15.7,15.7,15.7,14.9,14.5,13.7,13.6,13.1,12.9,12.8,12.5,11.7,11.6,11.6,11.2,10,9.7,9.6,9.6,9.5,9.5,9.3,8.6,8.5,8.3,8.2,7.9,7.7,7.5,7.3,7.2,7.1,6.4,6.4,6.4,6.3,6.2,6.1,6,6,5.6,5.5,5.5,5.4,5.3,5.2,5,4.9,4.6,4.6,4.6,4.5,4.4,4.4,4.3,4.2,4.1,4,4,4,3.7,3.5,3.1,3,3,3,2.9,2.9,2.7,2.7,2.6,2.6,2.5,2.5,2.4,2.4,2.3,2.2,2.2,1.9,1.8,1.8,1.7,1.4,1.4,1.4,1.3,1.3,1.3,1.1,1.1,1.1,1,0.8,0.8,0.8,0.8,0.8,0.8,0.7,0.6,0.6,0.5,0.5,0.5,0.5,0.4,0.4,0.4,0.4,0.3,0.3,0.3,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.1&chf=bg,s,e0f2ff" />
</div>
<div id="loc-tax" class="stats map">
	<label>Species per country</label>
	<img src="http://chart.apis.google.com/chart?chs=320x160&cht=t&chtm=world&chco=cccccc,fff5f0,99000d&chld=SENLAUFONZKRCALUJPUSBMLIISBBNOSIGBFIDKEEMCCHHKATITJMBELVDEFRMYESSGCYGUMOBNAGSKBYIEMQADHRNCBSMTHUAECLQAREPTCZVIKWSCPLLTILMUAWPRROGFPFGYCRBHUYBABGLBGPGRARBRMXDOPETRMARUSTMDVNJOFMVETTSATHOMIRZACOMNUABZTNSVZWVCCNFJAZSDGTMKECIDEGSRHTPKPSPAGECVALTGSYDZBJPHINAMKGBOGASNKZNASZLYGMNGHNVUBWWSUZPYKEBTTOLSZMKMNIGWKIPGGHERLKUGCUDJCMGQCGYESOCITZAFTMMZSBNPMRGNAORWBFMLBIMGMWTDLABDCFKHTJETCDNEMM&chd=t:58.1,56.4,53.6,53.5,52,52,51.7,51.6,50.7,50.5,49.9,48.2,47.4,45.3,44.6,42.2,41,40.6,40,39.5,38.9,38.8,38.2,37.3,36.7,35.4,34.8,34,32.9,32.9,32.3,30.8,30.3,29.7,29.3,28.2,27.5,27.1,26.9,26.5,25.8,25.1,24.8,24.7,24.5,24.3,24.2,22.6,22.4,22,21.5,21.3,20.7,20.6,20.4,19.9,19.8,19.7,19.7,18.6,18.4,18.4,17.6,16.8,16.8,16.4,16.2,16.2,16.2,15.7,15.7,15.7,14.9,14.5,13.7,13.6,13.1,12.9,12.8,12.5,11.7,11.6,11.6,11.2,10,9.7,9.6,9.6,9.5,9.5,9.3,8.6,8.5,8.3,8.2,7.9,7.7,7.5,7.3,7.2,7.1,6.4,6.4,6.4,6.3,6.2,6.1,6,6,5.6,5.5,5.5,5.4,5.3,5.2,5,4.9,4.6,4.6,4.6,4.5,4.4,4.4,4.3,4.2,4.1,4,4,4,3.7,3.5,3.1,3,3,3,2.9,2.9,2.7,2.7,2.6,2.6,2.5,2.5,2.4,2.4,2.3,2.2,2.2,1.9,1.8,1.8,1.7,1.4,1.4,1.4,1.3,1.3,1.3,1.1,1.1,1.1,1,0.8,0.8,0.8,0.8,0.8,0.8,0.7,0.6,0.6,0.5,0.5,0.5,0.5,0.4,0.4,0.4,0.4,0.3,0.3,0.3,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.1&chf=bg,s,e0f2ff" />
</div>

<div id="loc-pie" class="stats chart">
	<label>Occurrences by Region</label>
	<s:select name="locType" list="locTypes" value="locDefault.columnName" theme="simple"/>

	<s:url id="occResourceStatsByRegionUrl" action="occResourceStatsByRegion">
		<s:param name="resource_id" value="resource_id" />
	</s:url>
	<s:a href="%{occResourceStatsByRegionUrl}" >
		<s:action name="occResourceChartByRegion" namespace="/ajax" executeResult="true"/>
	</s:a>

	<!--
	<s:div id="recordCount" theme="ajax" href="%{recordCountUrl}">
		<s:property value="occResource.getRecordCount()"/>
	</s:div>
	<img src="<s:property value="occByRegionUrl"/>" />
	-->
</div>
<div id="loc-geoserver" class="stats map">
	<label>GeoServer point map</label>
	<img src="http://chart.apis.google.com/chart?cht=t&chs=320x160&chd=s:_&chtm=world" />
</div>
			


<div id="loc-stats" class="stats clearfix">
	<label>Taxonomic Stats</label>
	<ul class="plain">
		<li>673 Species or infraspecific</li> 
		<li>673 Genera</li> 
	</ul>
</div>

<div class="clearfix">
</div>

<div id="tax-pie" class="stats chart">
	<label>Occurrences by Taxon</label>
	<s:select name="taxType" title="rank" list="taxTypes" value="taxDefault.columnName" theme="simple"/>
	<img src="<s:property value="occByTaxonUrl"/>" />
</div>
<div id="tax2-pie" class="stats chart">
	<label>Top 10 Taxa</label>
	<s:select name="taxType" title="rank" list="taxTypes" value="taxDefault.columnName" theme="simple"/>
	<img src="<s:property value="occByTop10TaxaUrl"/>" />
</div>


<div class="clearfix">
</div>


<div id="inst-pie" class="stats chart">
	<label>Occurrences by Institution</label>
	<img src="<s:property value="occByInstitutionUrl"/>" />
</div>
<div id="col-pie" class="stats chart">
	<label>Occurrences by Collection</label>
	<img src="<s:property value="occByCollectionUrl"/>" />
</div>



<div id="recordbasis-pie" class="stats chart">
	<label>Occurrences by Basis of Record</label>		
	<img src="<s:property value="occByBasisOfRecordUrl"/>" />
</div>
<div id="time-pie" class="stats chart">
	<label>Occurrences by Year Collected</label>		
	<img src="http://chart.apis.google.com/chart?cht=bvs&chs=320x160&chd=t:10,50,60,40,50,60,100,40,20,80,40,77,20,50,60,100,40,20,80,40,7,15,5,9,55,7850,40,50,60,100,40,20,60,100,13,56,48,13,20,10,50,78,60,80,40,50,60,100,40,20,40,50,60,0,80,40,50,60,100,40,20&chco=c6d9fd&chbh=3" />
</div>

<br/>

</body>
