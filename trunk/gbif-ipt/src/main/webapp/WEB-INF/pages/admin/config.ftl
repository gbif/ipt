<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
	<style>
	#location > input{
		width: 150px;
		float: left;
		margin-left: 2em;
	}
	#locationMap{
		clear:both;
		padding-top: 1em;
		margin-left: 2em;
		height: 95px;
		width: 325px;
	}
	</style>
	
<script type="text/javascript">
$(document).ready(function(){
  initHelp();	
});
</script>
	
	
  <#if latitude?? && longitude??>	
	<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false"></script>
	<script type="text/javascript">
	$(document).ready(function(){
    var latlng = new google.maps.LatLng(${latitude}, ${longitude});
    var myOptions = {
      zoom: 3,
      center: latlng,
      disableDefaultUI: true,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    var map = new google.maps.Map(document.getElementById("locationMap"), myOptions);
    var marker = new google.maps.Marker({
        position: latlng, 
        map: map,
        title:"IPT Server"
    });
	});
	</script>
  </#if>
 <#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.config.title"/></h1>

<#include "/WEB-INF/pages/macros/forms.ftl">
<@s.form cssClass="topForm half" action="config" method="post">

	<@input name="baseUrl" i18nkey="admin.config.baseUrl" help="i18n" size=80/>
	<@input name="proxy" i18nkey="admin.config.proxy" help="i18n" size=80/>
	<@input name="analyticsKey" i18nkey="admin.config.analyticsKey" size=80/>  
	<@checkbox name="analyticsGbif" i18nkey="admin.config.analyticsGbif" />  
	<@checkbox name="debug" i18nkey="admin.config.debug" />

  <div id="location">
	<label for="latitude"><@s.text name="admin.config.server.location"/></label>
	<div class="newline"></div>
	<div class="halfcolumn">
		<input type="text" id="latitude" name="latitude" value="${latitude!}" size="10" />
	</div>
	<div class="halfcolumn">
		<input type="text" id="longitude" name="longitude" value="${longitude!}" size="10" />
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

<#include "/WEB-INF/pages/inc/footer.ftl">
