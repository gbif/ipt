/** 
 * MarkerTracker v1.0
 * Author:  Michael Menzel
 * Email:   mugglmenzel@gmail.com
 * 
 * Copyright 2009 Michael Menzel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 */  
 
 package com.google.maps.extras.markertracker
{
	public class MarkerTrackerOptions {
		
		
	 /**
	  * Scales the icon size by this value, 0 = no icon.
	  * @see MarkerTracker#DEFAUT_ICON_SCALE
	  */
	 public var iconScale:Number;
	 
	 /**
      * The padding between the arrow and the edge of the map.
      * @see MarkerTracker#DEFAULT_PADDING
      */
	 public var padding:Number;
	 
	 /**
	  * The color of the arrow.
	  * @see MarkerTracker#DEFAUT_ARROW_COLOR
	  */
	 public var arrowColor:Number;
	 
	 /**
      * The thickness of the lines that make up the arrows.
      * @see MarkerTracker#DEFAUT_ARROW_WEIGHT
      */
	 public var arrowWeight:Number;
	 
	 /**
      * The length of the arrow.
      * @see MarkerTracker#DEFAUT_ARROW_LENGTH
      */
	 public var arrowLength:Number;
	 
	 /**
      * The opacity of the arrow.
      * @see MarkerTracker#DEFAUT_ARROW_OPACITY
      */
	 public var arrowOpacity:Number;
	 
	 /**
      * The Map event that triggers the arrows to update.
      * @see MarkerTracker#DEFAUT_UPDATE_EVENT
      */
	 public var updateEvent:String;
	 
	 /**
      * The Marker event that triggers a quick zoom to the tracked marker. 
      * @see MarkerTracker#DEFAUT_PAN_EVENT
      */
	 public var panEvent:String;
	 
	 /**
      * Setting this value to false will disable a quick pan.
      * @see MarkerTracker#DEFAUT_QUICK_PAN_ENABLED
      */
	 public var quickPanEnabled:Boolean;
	 
	 public function MarkerTrackerOptions(params:Object = null) {
	 	if (params == null) return;
	 	
	 	var propList:Array = ["iconScale", "padding", "arrowColor", "arrowWeight", 
	 	     "arrowLength", "updateEvent", "panEvent", "quickPanEnabled"];
        for (var i:Number = 0; i < propList.length; i++) {
            var propName:String = propList[i];
            this[propName] = params[propName];
        }
    }
  }
}