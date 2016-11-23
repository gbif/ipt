<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>

<link rel="stylesheet" href="/styles/leaflet/leaflet.css" />
<link rel="stylesheet" href="/styles/leaflet/locationfilter.css" />
<script type="text/javascript" src="/js/leaflet/leaflet.js"></script>
<script type="text/javascript" src="/js/leaflet/cartodb.core.js"></script>
<script type="text/javascript" src="/js/leaflet/locationfilter.js"></script>
<script type="text/javascript">
$(document).ready(function(){
  initHelp();	
});
</script>	
  <#if latitude?? && longitude??>	
	<script type="text/javascript">
	$(document).ready(function(){
      var map = L.map('locationMap', {crs: L.CRS.EPSG4326}).setView([${latitude}, ${longitude}], 2).setMaxBounds(L.latLngBounds(L.latLng(-90, -180), L.latLng(90, 180)));
      // Using cartodb to get a baselayer.
      // TODO: Consider a better service?
      var sqlWGS84 = "SELECT ST_SCALE(the_geom, 111319.44444444444444, 111319.44444444444444) AS the_geom_webmercator FROM world_borders";
      // style it here
      var cartoCssGBIF = "#layer { polygon-fill: #02393D; polygon-opacity: 1; line-width:0}";
      cartodb.Tiles.getTiles({
          user_name: 'gbif', sublayers: [{
              sql: sqlWGS84, cartocss: cartoCssGBIF
          }]
      }, function (tileTemplate) {
          L.tileLayer(tileTemplate.tiles[0], {
              attribution: 'Natural Earth data, map by CartoDB',
          }).addTo(map);
      });
      L.Icon.Default.imagePath = '/images/leaflet';
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
	<@checkbox name="analyticsGbif" i18nkey="admin.config.analyticsGbif" help="i18n"/>
	<@checkbox name="debug" i18nkey="admin.config.debug" help="i18n"/>
  <@checkbox name="archivalMode" i18nkey="admin.config.archivalMode" help="i18n"/>
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
