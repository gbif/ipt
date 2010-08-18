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

<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false"></script>

<script type="text/javascript">
    $(document).ready(function(){
      // Global variables
      var map;
      var marker1;
      var marker2;
      var rectangle;
      var dfminx=65;
      var dfminy=-10;
      var dfmaxx=71;
      var dfmaxy=10;
      var bboxBase="eml\\.geographicCoverage\\.boundingCoordinates\\.";

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
          position: new google.maps.LatLng(dfminx, dfminy),
          draggable: true,
          title: 'marker1'
        });
        marker2 = new google.maps.Marker({
          map: map,
          position: new google.maps.LatLng(dfmaxx, dfmaxy),
          draggable: true,
          title: 'marker2'
        }); 
        
        // Allow user to drag each marker to resize the size of the Rectangle.
        google.maps.event.addListener(marker1, 'drag', redrawAndFill);
        google.maps.event.addListener(marker2, 'drag', redrawAndFill);
        
        // Create a new Rectangle overlay and place it on the map.  Size
        // will be determined by the LatLngBounds based on the two Marker
        // positions.
        rectangle = new google.maps.Rectangle({
          map: map
        });
        redrawAndFill();
      }
      
       $("#bbox input").keyup(function() {
  			var maxy=parseFloat($("#"+bboxBase+"max\\.latitude").attr("value"));
            var miny=parseFloat($("#"+bboxBase+"min\\.latitude").attr("value"));
            var maxx=parseFloat($("#"+bboxBase+"max\\.longitude").attr("value"));
            var minx=parseFloat($("#"+bboxBase+"min\\.longitude").attr("value"));
  			
  			if(isNaN(maxy))	maxy=dfmaxy;
  				else dfmaxy=maxy;
  			if(isNaN(miny))miny=dfminy;
  				else dfminy=miny;
  			if(isNaN(maxx))maxx=dfmaxx;
  				else dfmaxx=maxx;
  			if(isNaN(minx))minx=dfminx;
  				else dfminx=minx;
  			
  			var tminy=miny < maxy ? miny : maxy;
        	var tmaxy=miny > maxy ? miny : maxy;
        	var tminx=minx < maxx ? minx : maxx;
        	var tmaxx=minx > maxx ? minx : maxx;
       
       		marker1.setPosition(new google.maps.LatLng(tminy, tminx));
      	 	marker2.setPosition(new google.maps.LatLng(tmaxy, tmaxx));
       		redraw();
		});
                     
       function redrawAndFill() {
       	redraw();
       	fill();
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
       }

      // Register an event listener to fire when the page finishes loading.
      google.maps.event.addDomListener(window, 'load', init);
      
      
      function fill(){
          var tminy=marker1.getPosition().lat() < marker2.getPosition().lat() ? marker1.getPosition().lat() : marker2.getPosition().lat();
          var tmaxy=marker1.getPosition().lat() < marker2.getPosition().lat() ? marker2.getPosition().lat() : marker1.getPosition().lat();

          $("#"+bboxBase+"max\\.latitude").attr("value",tmaxy);
          $("#"+bboxBase+"min\\.latitude").attr("value",tminy);
          $("#"+bboxBase+"min\\.longitude").attr("value",marker1.getPosition().lng());
          $("#"+bboxBase+"max\\.longitude").attr("value",marker2.getPosition().lng());
 	   }  
     
});
</script>

</head>

<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<@s.text name='manage.metadata.geocoverage.map.message'/>
<div id="map"></div>
<@s.form id="geoForm" action="geocoverage" method="post" validate="false">

  <@s.hidden name="resourceId" value="${(resource.id)!}"/>
  <@s.hidden name="resourceType" value="${(resourceType)!}"/>
  <@s.hidden name="guid" value="${(resource.guid)!}"/>
  <@s.hidden name="nextPage" value="taxcoverage"/>
  <@s.hidden name="method" value="geographicCoverages"/>

<div id="bbox">
  <@input name="eml.geographicCoverage.boundingCoordinates.min.longitude"/>
  <@input name="eml.geographicCoverage.boundingCoordinates.max.longitude" />
  <@input name="eml.geographicCoverage.boundingCoordinates.min.latitude" />
  <@input name="eml.geographicCoverage.boundingCoordinates.max.latitude" />
</div>
<div class="newline"></div>
<@text name="eml.geographicCoverage.description" />

<div class="break">
  <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
  <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
</div>
</@s.form>

<#include "/WEB-INF/pages/inc/footer.ftl">