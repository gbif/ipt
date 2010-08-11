<!--
/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
-->
<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name='manage.metadata.geocoverage.title'/></title>
	<#assign sideMenuEml=true />
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<head>
    <title><@s.text name="metadata.heading.geocoverages"/></title>
    <meta name="resource" content=""/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
    <meta name="heading" content="<@s.text name='eml.geographicCoverage'/>"/>    
    
<script
  src="http://www.google.com/jsapi?key=ABQIAAAAQmTfPsuZgXDEr012HM6trBT2yXp_ZAY8_ufC3CFXhHIE1NvwkxQTBMMPM0apn-CWBZ8nUq7oUL6nMQ"
  type="text/javascript">
</script>

    <style type="text/css">
      #map {
        width: 690px;
        height: 250px;
      }
    </style>

    <script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false"></script>

    <script type="text/javascript">
      // Global variables
      var map;
      var marker1;
      var marker2;
      var rectangle;

      /**
       * Called on the initial page load.
       */
      function init() {
        map = new google.maps.Map(document.getElementById('map'), {
          'zoom': 3,
          'center': new google.maps.LatLng(70, 0),
          'mapTypeId': google.maps.MapTypeId.ROADMAP
        });

        // Plot two markers to represent the Rectangle's bounds.
        marker1 = new google.maps.Marker({
          map: map,
          position: new google.maps.LatLng(65, -10),
          draggable: true,
          title: 'Drag me!'
        });
        marker2 = new google.maps.Marker({
          map: map,
          position: new google.maps.LatLng(71, 10),
          draggable: true,
          title: 'Drag me!'
        });
        
        // Allow user to drag each marker to resize the size of the Rectangle.
        google.maps.event.addListener(marker1, 'drag', redraw);
        google.maps.event.addListener(marker2, 'drag', redraw);
        
        // Create a new Rectangle overlay and place it on the map.  Size
        // will be determined by the LatLngBounds based on the two Marker
        // positions.
        rectangle = new google.maps.Rectangle({
          map: map
        });
        redraw();
      }
      
      /**
       * Updates the Rectangle's bounds to resize its dimensions.
       */
      function redraw() {
        var latLngBounds = new google.maps.LatLngBounds(
          marker1.getPosition(),
          marker2.getPosition()
        );
        rectangle.setBounds(latLngBounds);
        var tminy=marker1.getPosition().lat()<marker2.getPosition().lat()?marker1.getPosition().lat():marker2.getPosition().lat();
        var tmaxy=marker1.getPosition().lat()<marker2.getPosition().lat()?marker2.getPosition().lat():marker1.getPosition().lat();
        selectBoundigBox(marker1.getPosition().lng(), tminy, marker2.getPosition().lng(), tmaxy);
      }

      // Register an event listener to fire when the page finishes loading.
      google.maps.event.addDomListener(window, 'load', init);
      
      
      function selectBoundigBox(minx,miny,maxx,maxy){
                $("#maxy").val(maxy);
                $("#miny").val(miny);
                $("#minx").val(minx);
                $("#maxx").val(maxx);
 	   }
      
    </script>

</head>
	<@s.text name='manage.metadata.geocoverage.map.message'/>
    <div id="map"></div>
    <div class="break10"></div>
<p class="explMt"><@s.text name='manage.metadata.geocoverage.description'/></p>
<@s.form id="geoForm" action="geocoverage" method="post" validate="false">

<fieldset>
  <@s.hidden name="resourceId" value="${(resource.id)!}"/>
  <@s.hidden name="resourceType" value="${(resourceType)!}"/>
  <@s.hidden name="guid" value="${(resource.guid)!}"/>
  <@s.hidden name="nextPage" value="taxcoverage"/>
  <@s.hidden name="method" value="geographicCoverages"/>

<div id="clone">
  <@s.hidden id="minx" key="eml.geographicCoverage.boundingCoordinates.min.longitude"/>
  <@s.hidden id="maxx" key="eml.geographicCoverage.boundingCoordinates.max.longitude" />
  <@s.hidden id="miny" key="eml.geographicCoverage.boundingCoordinates.min.latitude" />
  <@s.hidden id="maxy" key="eml.geographicCoverage.boundingCoordinates.max.latitude" />
  <div class="newline"></div>
  <div id="map"></div>
  <@s.textarea key="eml.geographicCoverage.description" required="false" 
    cssClass="text xlarge"/>
</div>
</fieldset>
<div class="break">
  <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
  <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
</div>
</@s.form>

<#include "/WEB-INF/pages/inc/footer.ftl">