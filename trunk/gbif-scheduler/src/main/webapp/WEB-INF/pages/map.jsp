<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="map.title"/></title>
    <meta name="heading" content="<fmt:message key='map.heading'/>"/>

<script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=ABQIAAAAUdCLksixDAgnY8JZ7a6YJBTwM0brOpm-All5BF6PoaKBxRWWERR6JPVWKY7v479Rs9E0obxncZMKRQ"
      type="text/javascript"></script>
    <script src="../src/dragzoom_packed.js" type="text/javascript"></script>
    <script type="text/javascript">
    
   <script type="text/javascript">

    //<![CDATA[
	//http://code.google.com/p/gmaps-utility-library/
    function load() {
      if (GBrowserIsCompatible()) {
        var map = new GMap2(document.getElementById("map"));
        map.setCenter(new GLatLng(37.4419, -122.1419), 13);
        map.addControl(new GSmallMapControl());
        /* first set of options is for the visual overlay.*/
        var boxStyleOpts = {
          opacity: .2,
          border: "2px solid red"
        }

        /* second set of options is for everything else */
        var otherOpts = {
          buttonHTML: "<img src='images/zoom-button.gif' />",
          buttonZoomingHTML: "<img src='images/zoom-button-activated.gif' />",
          buttonStartingStyle: {width: '24px', height: '24px'}
        };

        /* third set of options specifies callbacks */
        var callbacks = {
          buttonclick: function(){GLog.write("Looks like you activated DragZoom!")},
          dragstart: function(){GLog.write("Started to Drag . . .")},
          dragging: function(x1,y1,x2,y2){GLog.write("Dragging, currently x="+x2+",y="+y2)},
          dragend: function(nw,ne,se,sw,nwpx,nepx,sepx,swpx){GLog.write("Zoom! NE="+ne+";SW="+sw)}
        };
  
        map.addControl(new DragZoomControl(boxStyleOpts, otherOpts, callbacks));
      }
    }

    //]]>
    </script>
</head>
    
  <body onload="load()" onunload="GUnload()">
    <div id="map" style="width: 500px; height: 400px"></div>
  </body>        
    