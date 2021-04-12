<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>

<link rel="stylesheet" href="${baseURL}/styles/leaflet/leaflet.css" />
<link rel="stylesheet" href="${baseURL}/styles/leaflet/locationfilter.css" />
<script type="text/javascript" src="${baseURL}/js/leaflet/leaflet.js"></script>
<script type="text/javascript" src="${baseURL}/js/leaflet/tile.stamen.js"></script>
<script type="text/javascript">
$(document).ready(function(){
  initHelp();	
});
</script>	
  <#if latitude?? && longitude??>	
	<script type="text/javascript">
	$(document).ready(function(){
      var map = L.map('locationMap').setView([${latitude}, ${longitude}], 10).setMaxBounds(L.latLngBounds(L.latLng(-90, -180), L.latLng(90, 180)));
      var layer = new L.StamenTileLayer("terrain");
      map.addLayer(layer, {
          detectRetina: true
      });
      L.Icon.Default.imagePath = '${baseURL}/images/leaflet';
      var marker = L.marker([${latitude}, ${longitude}], {iconUrl: 'marker-icon-2x.png'}).addTo(map);
	});
	</script>
  </#if>
 <#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<div class="grid_18 suffix_6">
<h1><@s.text name="admin.home.editConfig"/></h1>

<#include "/WEB-INF/pages/macros/forms.ftl">

<@s.form cssClass="topForm half" action="config" method="post" namespace="" includeContext="false">

   <@readonly name="dataDir" i18nkey="admin.config.server.data.dir" value="${dataDir}" help="i18n"/>
	<@input name="baseUrl" i18nkey="admin.config.baseUrl" help="i18n" size=80/>
	<@input name="proxy" i18nkey="admin.config.proxy" help="i18n" size=80/>
  <@input name="analyticsKey" i18nkey="admin.config.analyticsKey" help="i18n" size=80/>
	<@checkbox name="analyticsGbif" i18nkey="admin.config.analyticsGbif" value="${analyticsGbif?c}" help="i18n"/>
	<@checkbox name="debug" i18nkey="admin.config.debug" value="${debug?c}" help="i18n"/>
  <@checkbox name="archivalMode" i18nkey="admin.config.archivalMode" value="${archivalMode?c}" help="i18n"/>
  <@input name="archivalLimit" i18nkey="admin.config.archivalLimit" help="i18n" type="number"/>
  <@readonly name="logDir" i18nkey="admin.config.server.log.dir" value="${logDir}" help="i18n"/>
  <@readonly name="registryUrl" i18nkey="admin.config.registry.url" value="${registryUrl}" help="i18n"/>

  <div id="location">
	<label for="latitude"><@s.text name="admin.config.server.location"/></label>
	<div class="halfcolumn">
		<@input name="latitude" i18nkey="admin.config.server.latitude" help="i18n" />
	</div>
	<div class="halfcolumn">
		<@input name="longitude" i18nkey="admin.config.server.longitude" help="i18n" />
	</div>
  </div>
  <div id="locationMap">
	<#-- the map -->
	<#if latitude?? && longitude??>
	<#else>
	<img src="${baseURL}/images/ipt_no_location_map.gif" />
	</#if>
  </div>
  <div class="buttons">
 	<@s.submit cssClass="button" name="save" key="button.save"/>
 	<@s.submit cssClass="button" name="cancel" key="button.cancel"/>
  </div>	

</@s.form>
</div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
