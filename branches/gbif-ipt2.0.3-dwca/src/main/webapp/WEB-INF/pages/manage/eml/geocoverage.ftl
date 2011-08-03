<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.metadata.geocoverage.title'/></title>
 <#assign sideMenuEml=true />
 <#assign currentMenu="manage"/>

<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false"></script>

<script type="text/javascript">
	$(document).ready(function(){
		initHelp();
    // Global variables
    var map;
    var marker1;
    var marker2;
    var rectangle;
    var dfminx=-10;
    var dfminy=-10;
    var dfmaxx=10;
    var dfmaxy=10;
    var bboxBase="eml\\.geospatialCoverages\\[0\\]\\.boundingCoordinates\\.";    

	/**
     * Called on the initial page load.
     */
	function init() {
    	var maxy=parseFloat($("#"+bboxBase+"max\\.latitude").attr("value"));
       	var miny=parseFloat($("#"+bboxBase+"min\\.latitude").attr("value"));
        var maxx=parseFloat($("#"+bboxBase+"max\\.longitude").attr("value"));
        var minx=parseFloat($("#"+bboxBase+"min\\.longitude").attr("value"));  			
  		var isFilled=true;
  		if(isNaN(maxy)){maxy=dfmaxy;isFilled=false;}
  			else dfmaxy=maxy;
  		if(isNaN(miny)){miny=dfminy;isFilled=false;}
  			else dfminy=miny;
  		if(isNaN(maxx)){maxx=dfmaxx;isFilled=false;}
  			else dfmaxx=maxx;
  		if(isNaN(minx)){minx=dfminx;isFilled=false;}
  			else dfminx=minx;
  			
  		var mapOptions = {
   			 zoom: 2,
   			 center: new google.maps.LatLng((maxy+miny)/2, (maxx+minx)/2),
  			 scaleControl: true,
  			 scaleControlOptions: {
  			 	position: google.maps.ControlPosition.TOP_LEFT
  			 },
  			 mapTypeControl: true,
  			 mapTypeControlOptions: {
      			style: google.maps.MapTypeControlStyle.DROPDOWN_MENU
    		 },
    		 navigationControl: true,
    		 navigationControlOptions: {
     			style: google.maps.NavigationControlStyle.ANDROID,
     			position: google.maps.ControlPosition.BOTTOM_LEFT
   			 },
   			 mapTypeId: google.maps.MapTypeId.TERRAIN
  		}			
        map = new google.maps.Map(document.getElementById('map'), mapOptions);  				
        
        var markerIcon = 'http://www.google.com/intl/en_us/mapfiles/ms/micons/green-dot.png';
		
	    // Plot two markers to represent the Rectangle's bounds.
	    marker1 = new google.maps.Marker({
	    	icon: markerIcon,
	    	map: map,
	        position: new google.maps.LatLng(miny, minx),
	        draggable: true,
	        title: 'marker1'
	    });
	    marker2 = new google.maps.Marker({
	    	icon: markerIcon,
	    	map: map,
	        position: new google.maps.LatLng(maxy, maxx),
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
	    redraw();
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
  		
       	marker1.setPosition(new google.maps.LatLng(miny, minx));
      	marker2.setPosition(new google.maps.LatLng(maxy, maxx));
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
		var tminy;
        var tmaxy;
        if(marker1.getPosition().lat() < -84.9999){
        	tminy=-84.9999;
        }else{
        	if(marker1.getPosition().lat() > 84.9999){
        		tminy=84.9999;
        	}else{
        		tminy=marker1.getPosition().lat();
        	}
        }
        
        if(marker2.getPosition().lat() > 84.9999){
        	tmaxy=84.9999;
        }else{
        	if(marker2.getPosition().lat() < -84.9999){
        		tmaxy=-84.9999;
        	}else{
        		tmaxy=marker2.getPosition().lat();
        	}
        }
        
        var tminx= marker1.getPosition().lng();
        var tmaxx= marker2.getPosition().lng();
            
        marker1.setPosition(new google.maps.LatLng(tminy, tminx));
      	marker2.setPosition(new google.maps.LatLng(tmaxy, tmaxx));
    
    	var latLngBounds = new google.maps.LatLngBounds(
        marker1.getPosition(),
        marker2.getPosition());
        
        rectangle.setBounds(latLngBounds);
    }

    // Register an event listener to fire when the page finishes loading.
    google.maps.event.addDomListener(window, 'load', init);      
      
    function fill(){
		var miny=marker1.getPosition().lat() < marker2.getPosition().lat() ? marker1.getPosition().lat() : marker2.getPosition().lat();
        var maxy=marker1.getPosition().lat() < marker2.getPosition().lat() ? marker2.getPosition().lat() : marker1.getPosition().lat();
		var minx=marker1.getPosition().lng();
		var maxx=marker2.getPosition().lng();
		if(maxx == -180) maxx = 179.9999;		
        $("#"+bboxBase+"min\\.latitude").attr("value",Math.round(miny*100)/100);
        $("#"+bboxBase+"max\\.latitude").attr("value",Math.round(maxy*100)/100);
        $("#"+bboxBase+"min\\.longitude").attr("value",Math.round(minx*100)/100);
        $("#"+bboxBase+"max\\.longitude").attr("value",Math.round(maxx*100)/100);
	}
	
	
	$(":checkbox").click(function() {
		if($("#globalCoverage").is(":checked")) {
       		$("#coordinates").slideUp('slow');				
	        marker1.setPosition(new google.maps.LatLng(-89.9999, -179.9999));
      		marker2.setPosition(new google.maps.LatLng(89.9999, 179.9999));
       		redrawAndFill();	    			
       		atribute=false;
		} else {	
       		marker1.setPosition(new google.maps.LatLng(dfminy, dfminx));
      		marker2.setPosition(new google.maps.LatLng(dfmaxy, dfmaxx));       		
       		redrawAndFill();			
       		$("#coordinates").slideDown('slow'); 
	     }		
	});	
	
     
});
</script>

<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<h1><@s.text name='manage.metadata.geocoverage.title'/>: <a href="resource.do?r=${resource.shortname}"><em>${resource.title!resource.shortname}</em></a> </h1>
<@s.text name='manage.metadata.geocoverage.intro'/>
<div id="map"></div>
<form class="topForm" action="metadata-${section}.do" method="post">
	<div id="bbox">
		<@checkbox name="globalCoverage" help="i18n" i18nkey="eml.geospatialCoverages.globalCoverage"/>
	 <div id="coordinates">
		<div class="halfcolumn">
  			<@input name="eml.geospatialCoverages[0].boundingCoordinates.min.longitude" value="${(eml.geospatialCoverages[0].boundingCoordinates.min.longitude)!}" i18nkey="eml.geospatialCoverages.boundingCoordinates.min.longitude"/>
  		</div>
  		<div class="halfcolumn">
  		<@input name="eml.geospatialCoverages[0].boundingCoordinates.max.longitude" value="${(eml.geospatialCoverages[0].boundingCoordinates.max.longitude)!}" i18nkey="eml.geospatialCoverages.boundingCoordinates.max.longitude"/>
  		</div>
  		<div class="newline"></div>	
  		<div class="halfcolumn">
  			<@input name="eml.geospatialCoverages[0].boundingCoordinates.min.latitude" value="${(eml.geospatialCoverages[0].boundingCoordinates.min.latitude)!}" i18nkey="eml.geospatialCoverages.boundingCoordinates.min.latitude"/>
  		</div>
  		<div class="halfcolumn">
  			<@input name="eml.geospatialCoverages[0].boundingCoordinates.max.latitude" value="${(eml.geospatialCoverages[0].boundingCoordinates.max.latitude)!}" i18nkey="eml.geospatialCoverages.boundingCoordinates.max.latitude"/>
  		</div>
  	 </div>	
	</div>
	<div class="newline"></div>
		<@text name="eml.geospatialCoverages[0].description" value="${(eml.geospatialCoverages[0].description)!}" i18nkey="eml.geospatialCoverages.description"/>
	<div class="buttons">
  		<@s.submit cssClass="button" name="save" key="button.save" />
  		<@s.submit cssClass="button" name="cancel" key="button.cancel" />
	</div>
	
	<!-- internal parameter -->
	<input name="r" type="hidden" value="${resource.shortname}" />
</form>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>