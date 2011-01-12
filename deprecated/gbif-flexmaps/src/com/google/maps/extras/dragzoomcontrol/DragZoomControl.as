/* 
 * DragZoomControl v1.0
 * Author: Brian Richardson
 * Email: irieb@mac.com
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Magifying glass image pulled from the following source:
 * http://commons.wikimedia.org/wiki/File:Gnome-zoom-in.svg
 * Used in this component under their GNU General Public License
 *
 * Enables drag zoom functionality 
 *
 * The control is enabled when a user clicks the magnifying 
 * glass image.
 *
 * When the control is enable the control listens for a mouse click
 * then mouse move to render the selection area
 *
 * On mouseUp the map will center the selection and zoom to the
 * highest possible level on the selected bounds.
 *
 * A second magnifying glass will appear to enable the user to
 * back out to the last map position
 */
package com.google.maps.extras.dragzoomcontrol
{
	import com.google.maps.Color;
	import com.google.maps.LatLng;
	import com.google.maps.LatLngBounds;
	import com.google.maps.MapMouseEvent;
	import com.google.maps.controls.ControlBase;
	import com.google.maps.controls.ControlPosition;
	import com.google.maps.interfaces.IMap;
	import com.google.maps.overlays.Polygon;
	import com.google.maps.overlays.PolygonOptions;
	import com.google.maps.overlays.Polyline;
	
	import flash.display.BitmapData;
	import flash.display.DisplayObject;
	import flash.display.Shape;
	import flash.display.Sprite;
	import flash.events.MouseEvent;
	import flash.external.ExternalInterface;
	import flash.filters.BevelFilter;
	import flash.filters.BitmapFilterQuality;
	import flash.filters.BitmapFilterType;
	import flash.geom.Matrix;
	import flash.geom.Point;
	import flash.text.TextField;
	import flash.text.TextFieldAutoSize;
	import flash.text.TextFormat;
	
	import mx.core.Application;
	
	/**
	 *  Dispatched when a Zoom is committed
	 * 
	 *  @eventType mx.events.FlexEvent.DATA_CHANGE
	 */
	[Event(name="zoomCommit", type="com.google.maps.extras.dragzoomcontrol.events.DragZoomEvent")]
	public class DragZoomControl extends ControlBase {
				
		public var selectedArea:Polygon;		
				
		//static constants
		private static const ACTIVE_ALPHA:Number = 1;
		private static const INACTIVE_ALPHA:Number = .7;
		
		//Default configurable properties values
		public static const DEFAULT_SELECTION_BG_COLOR:Number = 0xFFFFFF;
		public static const DEFAULT_SELECTION_ALPHA:Number = 0.5;
		public static const DEFAUT_LINE_COLOR:Number = 0xFFFFFF;	
		public static const DEFAUT_DRAG_ZOOM_MSG:String = "click and drag mouse to select the area";								
		public static const DEFAULT_MARGIN_TOP:int = 7;
		public static const DEFAULT_MARGIN_LEFT:int = 7;
		
		//zoom-in image
		[Embed(source="/assets/images/zoom-in.png")]
		private var zoomInImg_:Class;
		
		//zoom-out image
		[Embed(source="/assets/images/zoom-out.png")]
		private var zoomOutImg_:Class;	
		
		//configurable properties
		private var selectionBGColor_:Number;
		private var selectionAlpha_:Number;
		private var selectionLineColor_:Number;	
		private var dragZoomMsg_:String;	
		private var marginTop_:int;
		private var margingLeft_:int;			
		
		//private
		private var map_:IMap;
		private var drawArea_:Sprite;
		private var zoomArea_:Shape;
		private var startXPos_:int;
		private var startYPos_:int;
		private var zoomState_:Boolean = false;
		
		private var nwPoint_:Point;
		private var swPoint_:Point;
		private var nePoint_:Point;
		private var sePoint_:Point;
		
		private var zoomInBtn_:Sprite;
		private var zoomOutBtn_:Sprite;
		private var msg_:Sprite;
		
		private var mapBitmapData_:BitmapData;
		private var mapDisplayObject_:DisplayObject;		

		/**
		 * Creates the DragZoom control
		 *
		 * @constructor
		 * @param {int} The top margin of the control in points. 
		 *   @see #DEFAULT_MARGIN_TOP
		 * @param {int} The left margin of the control in points. 
		 *   @see #DEFAULT_MARGIN_LEFT
		 * @param {Number} The background color message text. 
		 *   @see #DEFAULT_SELECTION_BG_COLOR
		 * @param {Number} The line color of the selection area. 
		 *   @see #DEFAUT_LINE_COLOR
		 * @param {Number} The alpha value of the selection area. 
		 *   @see #DEFAULT_SELECTION_ALPHA
		 * @param {String} The message displayed on screen when the control is enabled. 
		 *   @see #DEFAUT_DRAG_ZOOM_MSG
		 */
		public function DragZoomControl(
				optMarginTop:int = DEFAULT_MARGIN_TOP,
				optMarginLeft:int = DEFAULT_MARGIN_LEFT,
				optSelectionBGColor:Number = DEFAULT_SELECTION_BG_COLOR,
				optSelectionLineColor:Number = DEFAUT_LINE_COLOR,
				optSelectionAlpha:Number = DEFAULT_SELECTION_ALPHA,
				optDragZoomMsg:String = DEFAUT_DRAG_ZOOM_MSG) {	
								
			super(new ControlPosition(ControlPosition.ANCHOR_TOP_LEFT, optMarginTop, optMarginLeft));
			selectionBGColor_ = optSelectionBGColor;
			selectionAlpha_ = optSelectionAlpha;
			selectionLineColor_ = optSelectionLineColor;	
			dragZoomMsg_ = optDragZoomMsg;	
			marginTop_ = optMarginTop;
			margingLeft_ = optMarginLeft;	 
			
			//default selection effect - possibly make this configurable
			var bf:BevelFilter = new BevelFilter(1,
			   45,
			   0xFFFFFF,
			   0.8,
			   0x000000,
			   0.8,
			   5,
			   5,
			   5,
			   BitmapFilterQuality.HIGH,
			   BitmapFilterType.INNER,
			   true);
			   
			drawArea_ =  new Sprite();
			//drawArea_.filters = [bf];
			addChild(drawArea_);
		}		

		/**
		 * Initialize the control
		 *
		 * @param {IMap} The instance of the Map the control is being 
		 *   added to
		 * @private
		 */		
		public override function initControlWithMap(pMap:IMap):void {
			super.initControlWithMap(map);
			map_ = pMap;
			addControlButton();		
		}
		
		//*****************
		//private functions
		//*****************
		
		/**
		 * @private
		 * Sets the state of the control to listen
		 * for Map events in order to render the selection area
		 * 
		 * Map dragging is disabled as it would interfere with
		 * the drag control of the selection area
		 *
		 * @param {MouseEvent} event The mouse event that triggered the call
		 */			
		public function enableDragZoom(event:MouseEvent):void {
			msg_.visible = true;
			map_.disableDragging();	
			map_.addEventListener(MapMouseEvent.MOUSE_DOWN, startZoom);
			map_.addEventListener(MapMouseEvent.MOUSE_UP, commitZoom);
			map_.addEventListener(MapMouseEvent.MOUSE_MOVE, updateZoom);
			
			mapDisplayObject_ = map_ as DisplayObject;				
			mapBitmapData_= new BitmapData(mapDisplayObject_.width, mapDisplayObject_.height);	
			//this wil have to wait - sandbox security exception thrown
			//we can do all kinds of cool effects once this is supported
			//_mapBitmapData.draw(_mapDisplayObject);					
		}
		
		/**
		 * @private
		 * Returns the Map to the last saved position
		 *
		 * @param {MouseEvent} event The mouse event
		 */			
		private function returnToSavedPosition(event:MouseEvent):void {
			zoomOutBtn_.visible = false;
			map_.returnToSavedPosition();			
		}

		/**
		 * @private
		 * Removes Map event listener and enables map dragging
		 */			
		private function disableZoom():void {			
			map_.removeEventListener(MapMouseEvent.MOUSE_DOWN, startZoom);
			map_.removeEventListener(MapMouseEvent.MOUSE_UP, commitZoom);
			map_.removeEventListener(MapMouseEvent.MOUSE_MOVE, updateZoom);
			map_.enableDragging();
		}		

		/**
		 * @private
		 * Updates the selection area.
		 * Cooridinates are calculated using the latitude/longitude
		 * provided by the MapMouseEvent
		 *
		 * @param {MapMouseEvent} event The mouse event
		 */			
		private function updateZoom(event:MapMouseEvent):void {
			if (zoomState_) {
				var latLgn:LatLng = event.latLng;
				var point:Point = map_.fromLatLngToViewport(latLgn);
							
				var zoomWidth:int = (point.x - startXPos_);
				var zoomHeight:int = (point.y - startYPos_);
				
				resetDrawArea();

				zoomArea_ = new Shape();
				
				var recX:int = (point.x - zoomWidth)-marginTop_;
				var recY:int = (point.y - zoomHeight)-margingLeft_;
				
				var myMatrix:Matrix = new Matrix();
				myMatrix.tx = -(margingLeft_);
				myMatrix.ty = -(marginTop_);
				

				//zoomArea_.graphics.beginBitmapFill(mapBitmapData_,myMatrix); 
				zoomArea_.graphics.beginFill(Color.WHITE,0.4); 
				zoomArea_.graphics.lineStyle(1, selectionLineColor_);
				zoomArea_.graphics.drawRect(recX, recY, zoomWidth, zoomHeight);
				zoomArea_.graphics.endFill();
				drawArea_.addChild(zoomArea_);	
				
				recX = point.x - zoomWidth;
				recY = point.y - zoomHeight; 
				
				nwPoint_ = new Point(recX, recY);
				swPoint_ = new Point(recX, (recY + zoomArea_.height));
				sePoint_ = new Point((recX + zoomArea_.width), (recY + zoomArea_.height));
				nePoint_ = new Point((recX + zoomArea_.width), recY);
			}		
		}
		
		/**
		 * @private
		 * Commits the Zoom based on the selection area
		 *
		 * @param {MapMouseEvent} event The mouse event
		 */			
		private function commitZoom(event:MapMouseEvent):void {	
			disableZoom();	
			map_.savePosition();	
			zoomState_ = false;
			resetDrawArea();
						
			var latLngBounds:LatLngBounds = calculatePolyline();
			positionMap(latLngBounds);			
			
			zoomOutBtn_.visible = false;
			msg_.visible = false;
			mapBitmapData_ = null;
			mapDisplayObject_ = null;
			
			var evt:DragZoomEvent = new DragZoomEvent(DragZoomEvent.ZOOM_COMMIT);
			evt.bounds = latLngBounds;			
			dispatchEvent(evt);	
			//dispatchEvent(new Event("draggedZoomChanged",true));
		}
		
		/**
		 * @private
		 * Position the Map
		 *
		 * @param {LatLngBounds} pLatLngBounds Latitude/Longtitude bounds
		 * used to postion the map
		 */			
		private function positionMap(pLatLngBounds:LatLngBounds):void {
			map_.setCenter(pLatLngBounds.getCenter());
			map_.setZoom(map_.getBoundsZoomLevel(pLatLngBounds));			
		}	
		
		/**
		 * @private
		 * Sets the x/y point of the initial mouse click
		 * on the Map
		 *
		 * @param {MapMouseEvent} event The mouse event
		 */			
		private function startZoom(event:MapMouseEvent):void {
			zoomState_ = true;			
			var point:Point = map_.fromLatLngToViewport(event.latLng);
			startXPos_ = point.x;
			startYPos_ = point.y;		
		}
		
		/**
		 * Removes the selection graphic
		 *
		 */			
		private function resetDrawArea():void {
			if (zoomArea_) {				
				drawArea_.removeChild(zoomArea_);
				zoomArea_ = null;
			}					
		}
		
		/**
		 * @private
		 * Creates a Polyline based on the graphic selection
		 * 
		 * The Polyline is then used to create latitude/longtitude bounds
		 * that are used to center the Map and set the highest possible
		 * Zoom level for the selection
		 *
		 */			
		private function calculatePolyline():LatLngBounds {	  						
			var lines:Array = 
				[map_.fromViewportToLatLng(nwPoint_),
				map_.fromViewportToLatLng(swPoint_),
				map_.fromViewportToLatLng(sePoint_),
				map_.fromViewportToLatLng(nePoint_),
				map_.fromViewportToLatLng(nwPoint_)];	
			
			if (Application.application.bbox!=null)
				map_.removeOverlay(Application.application.bbox);
			
			
			var polOpt:PolygonOptions = new PolygonOptions({
				  strokeStyle: {
				    thickness: 1
				  }
		    	});
			var b:Polygon= new Polygon(lines,polOpt);
			Application.application.bbox=b;
			
			map_.addOverlay(Application.application.bbox);		
			
			ExternalInterface.call("selectBoundigBox",
				b.getLatLngBounds().getWest(),
				b.getLatLngBounds().getSouth(),
				b.getLatLngBounds().getEast(),
				b.getLatLngBounds().getNorth());
				
			var polyLine:Polyline = new Polyline(lines);
			return polyLine.getLatLngBounds();
		}	
		
		/**
		 * @private
		 * Used to create the rollover effect
		 *
		 * @param {MouseEvent} event The mouse event
		 */			
		private function mouseOver(event:MouseEvent):void {
			if (event.target is Sprite) {
				var s:Sprite = Sprite(event.target);
				s.alpha = ACTIVE_ALPHA;				
			}
			return;		
		}
		
		/**
		 * @private
		 * Used to create the rollout effect
		 *
		 * @param {MouseEvent} event The mouse event
		 */		
		private function mouseOut(event:MouseEvent):void {
			if (event.target is Sprite) {
				var s:Sprite = Sprite(event.target);
				s.alpha = INACTIVE_ALPHA;				
			}
			return;		
		}		
		
		/**
		 * @private
		 * Creates the control buttons (zoom in/zoom out)
		 *
		 */			
		private function addControlButton():void {			
			zoomInBtn_ = new Sprite();
			zoomInBtn_.x = 0;
			zoomInBtn_.y = 0;	   			    		    		    	
			//zoomInBtn_.addChild(DisplayObject(new zoomInImg_()));
			//zoomInBtn_.addEventListener(MouseEvent.CLICK, enableDragZoom);
			//zoomInBtn_.addEventListener(MouseEvent.MOUSE_OVER, mouseOver);
			//zoomInBtn_.addEventListener(MouseEvent.MOUSE_OUT, mouseOut);
			zoomInBtn_.alpha = 0;
						
			zoomOutBtn_ = new Sprite();
			zoomOutBtn_.x = zoomInBtn_.width;
			zoomOutBtn_.y = 0;									    		    
			//zoomOutBtn_.addChild(DisplayObject(new zoomOutImg_()));
			//zoomOutBtn_.addEventListener(MouseEvent.CLICK, returnToSavedPosition);
			//zoomOutBtn_.addEventListener(MouseEvent.MOUSE_OVER, mouseOver);
			//zoomOutBtn_.addEventListener(MouseEvent.MOUSE_OUT, mouseOut);		    
			zoomOutBtn_.alpha = INACTIVE_ALPHA;
			zoomOutBtn_.visible = false;
			
			var center:Point = map_.fromLatLngToViewport(map_.getCenter());
			msg_ = new Sprite();
			msg_.x = center.x;
			msg_.y = 10;			    		    		
			var label:TextField = new TextField();
			label.text = dragZoomMsg_;
			label.selectable = false;
			label.autoSize = TextFieldAutoSize.CENTER;
			var format:TextFormat = new TextFormat("Verdana");
			label.setTextFormat(format);	
			
			var background:Shape = new Shape();
			background.graphics.beginFill(selectionBGColor_, selectionAlpha_);
			background.graphics.lineStyle(1, selectionLineColor_);
			background.graphics.drawRoundRect(label.x, label.y, label.width, label.height, 4);
			background.graphics.endFill();
			
			msg_.addChild(background);	
			msg_.addChild(label);
			msg_.visible = false;				   	    	    
			
			addChild(msg_);
			addChild(zoomInBtn_);
			addChild(zoomOutBtn_);			
		}		
		
	}
}