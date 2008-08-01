<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceOverview.title"/></title>
    <meta name="heading" content="<s:property value="occResource.title"/>"/>
	<s:head theme="ajax" debug="true"/>
</head>


<div id="metadata">
	<s:form>
		<s:label key="occResource.description"/>
		<s:label key="occResource.numberOfRecords"/>
		<s:label key="occResource.lastUpload.executionDate"/>
	</s:form>
</div>



<div id="byLocation" class="clearfix">
	<h2>By Location</h2>
	
	<div id="loc-countries" class="chart map">
		<label>World map of countries colored by occurrence quantity</label>
		<img src="http://chart.apis.google.com/chart?chs=320x160&cht=t&chtm=world&chco=cccccc,fff5f0,99000d&chld=SENLAUFONZKRCALUJPUSBMLIISBBNOSIGBFIDKEEMCCHHKATITJMBELVDEFRMYESSGCYGUMOBNAGSKBYIEMQADHRNCBSMTHUAECLQAREPTCZVIKWSCPLLTILMUAWPRROGFPFGYCRBHUYBABGLBGPGRARBRMXDOPETRMARUSTMDVNJOFMVETTSATHOMIRZACOMNUABZTNSVZWVCCNFJAZSDGTMKECIDEGSRHTPKPSPAGECVALTGSYDZBJPHINAMKGBOGASNKZNASZLYGMNGHNVUBWWSUZPYKEBTTOLSZMKMNIGWKIPGGHERLKUGCUDJCMGQCGYESOCITZAFTMMZSBNPMRGNAORWBFMLBIMGMWTDLABDCFKHTJETCDNEMM&chd=t:58.1,56.4,53.6,53.5,52,52,51.7,51.6,50.7,50.5,49.9,48.2,47.4,45.3,44.6,42.2,41,40.6,40,39.5,38.9,38.8,38.2,37.3,36.7,35.4,34.8,34,32.9,32.9,32.3,30.8,30.3,29.7,29.3,28.2,27.5,27.1,26.9,26.5,25.8,25.1,24.8,24.7,24.5,24.3,24.2,22.6,22.4,22,21.5,21.3,20.7,20.6,20.4,19.9,19.8,19.7,19.7,18.6,18.4,18.4,17.6,16.8,16.8,16.4,16.2,16.2,16.2,15.7,15.7,15.7,14.9,14.5,13.7,13.6,13.1,12.9,12.8,12.5,11.7,11.6,11.6,11.2,10,9.7,9.6,9.6,9.5,9.5,9.3,8.6,8.5,8.3,8.2,7.9,7.7,7.5,7.3,7.2,7.1,6.4,6.4,6.4,6.3,6.2,6.1,6,6,5.6,5.5,5.5,5.4,5.3,5.2,5,4.9,4.6,4.6,4.6,4.5,4.4,4.4,4.3,4.2,4.1,4,4,4,3.7,3.5,3.1,3,3,3,2.9,2.9,2.7,2.7,2.6,2.6,2.5,2.5,2.4,2.4,2.3,2.2,2.2,1.9,1.8,1.8,1.7,1.4,1.4,1.4,1.3,1.3,1.3,1.1,1.1,1.1,1,0.8,0.8,0.8,0.8,0.8,0.8,0.7,0.6,0.6,0.5,0.5,0.5,0.5,0.4,0.4,0.4,0.4,0.3,0.3,0.3,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.1&chf=bg,s,e0f2ff" />
	</div>


	<div id="loc-tax" class="chart map">
		<label>Species per country</label>
		<img src="http://chart.apis.google.com/chart?chs=320x160&cht=t&chtm=world&chco=cccccc,fff5f0,99000d&chld=SENLAUFONZKRCALUJPUSBMLIISBBNOSIGBFIDKEEMCCHHKATITJMBELVDEFRMYESSGCYGUMOBNAGSKBYIEMQADHRNCBSMTHUAECLQAREPTCZVIKWSCPLLTILMUAWPRROGFPFGYCRBHUYBABGLBGPGRARBRMXDOPETRMARUSTMDVNJOFMVETTSATHOMIRZACOMNUABZTNSVZWVCCNFJAZSDGTMKECIDEGSRHTPKPSPAGECVALTGSYDZBJPHINAMKGBOGASNKZNASZLYGMNGHNVUBWWSUZPYKEBTTOLSZMKMNIGWKIPGGHERLKUGCUDJCMGQCGYESOCITZAFTMMZSBNPMRGNAORWBFMLBIMGMWTDLABDCFKHTJETCDNEMM&chd=t:58.1,56.4,53.6,53.5,52,52,51.7,51.6,50.7,50.5,49.9,48.2,47.4,45.3,44.6,42.2,41,40.6,40,39.5,38.9,38.8,38.2,37.3,36.7,35.4,34.8,34,32.9,32.9,32.3,30.8,30.3,29.7,29.3,28.2,27.5,27.1,26.9,26.5,25.8,25.1,24.8,24.7,24.5,24.3,24.2,22.6,22.4,22,21.5,21.3,20.7,20.6,20.4,19.9,19.8,19.7,19.7,18.6,18.4,18.4,17.6,16.8,16.8,16.4,16.2,16.2,16.2,15.7,15.7,15.7,14.9,14.5,13.7,13.6,13.1,12.9,12.8,12.5,11.7,11.6,11.6,11.2,10,9.7,9.6,9.6,9.5,9.5,9.3,8.6,8.5,8.3,8.2,7.9,7.7,7.5,7.3,7.2,7.1,6.4,6.4,6.4,6.3,6.2,6.1,6,6,5.6,5.5,5.5,5.4,5.3,5.2,5,4.9,4.6,4.6,4.6,4.5,4.4,4.4,4.3,4.2,4.1,4,4,4,3.7,3.5,3.1,3,3,3,2.9,2.9,2.7,2.7,2.6,2.6,2.5,2.5,2.4,2.4,2.3,2.2,2.2,1.9,1.8,1.8,1.7,1.4,1.4,1.4,1.3,1.3,1.3,1.1,1.1,1.1,1,0.8,0.8,0.8,0.8,0.8,0.8,0.7,0.6,0.6,0.5,0.5,0.5,0.5,0.4,0.4,0.4,0.4,0.3,0.3,0.3,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.1&chf=bg,s,e0f2ff" />
	</div>

	<div id="loc-pie" class="chart">
		<label>Occurrences per geographic region</label>
		<s:select name="locType" list="locTypes" theme="simple"/>
		<img src="http://chart.apis.google.com/chart?cht=p3&chs=320x160&chl=USA|Canada|Mexico&chts=000000,16&chco=CACACA,DF7417,01A1DB&chd=e:czczGa" />
	</div>

	<div id="loc-geoserver" class="chart map">
		<label>GeoServer point map</label>
		<img src="http://chart.apis.google.com/chart?cht=t&chs=320x160&chd=s:_&chtm=world" />
	</div>
			
</div>




<div id="byTaxonomy" class="clearfix">
	<h2>By Taxonomy</h2>

	<div id="tax-pie" class="chart">
		<label>Occurrences per taxon</label>
		<s:select name="taxType" title="rank" list="taxTypes" theme="simple"/>
		<img src="http://chart.apis.google.com/chart?cht=p3&chs=320x160&chl=Abies|Pinus|Fagus&chts=000000,16&chco=CACACA,DF7417,01A1DB&chd=e:czczGa" />
	</div>

</div>


<div id="byCollection" class="clearfix">
	<h2>By Hosting Body</h2>
	<div id="inst-pie" class="chart">
		<label>Occurrences per institution</label>
		<img src="http://chart.apis.google.com/chart?cht=p3&chs=320x160&chl=BGBM|RBGK|NYBG&chts=000000,16&chco=CACACA,DF7417,01A1DB&chd=e:czczGa" />
	</div>

	<div id="col-pie" class="chart">
		<label>Occurrences per collection</label>
		<img src="http://chart.apis.google.com/chart?cht=p3&chs=320x160&chl=B|K|NY&chts=000000,16&chco=CACACA,DF7417,01A1DB&chd=e:czczGa" />
	</div>

</div>


<div id="byTime" class="clearfix">
	<h2>By Time</h2>
	<div id="time-pie" class="chart">
		<label>Occurrences by collection year</label>		
		<img src="http://chart.apis.google.com/chart?cht=bvs&chs=420x160&chd=t:10,50,60,40,50,60,100,40,20,80,40,77,20,50,60,100,40,20,80,40,7,15,5,9,55,7850,40,50,60,100,40,20,60,100,13,56,48,13,20,10,50,78,60,80,40,50,60,100,40,20,40,50,60,0,80,40,50,60,100,40,20&chco=c6d9fd&chbh=3" />
	</div>

</div>

<br/>
