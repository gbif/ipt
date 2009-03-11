/**
 * Copyright (c)2005-2008 Matt Kruse (javascripttoolbox.com)
 * 
 * Dual licensed under the MIT and GPL licenses. 
 * This basically means you can use this code however you want for
 * free, but don't claim to have written it yourself!
 * Donations always accepted: http://www.JavascriptToolbox.com/donate/
 * 
 * Please do not link to the .js files on javascripttoolbox.com from
 * your site. Copy the files locally to your server instead.
 * 
 */
/* $VERSION: 1.1 */
var Selectbox = {};

Selectbox.hasOptions = function(obj) {
	return (obj!=null && typeof(obj.options)!="undefined" && obj.options!=null);
};

Selectbox.selectUnselectMatchingOptions = function(obj,regex,which,only) {
	if (window.RegExp) {
		if (!this.hasOptions(obj)) { return false; }
		var only = !(typeof(only)=="undefined" || only==null);
		var re = new RegExp(regex);
		for (var i=0; i<obj.options.length; i++) {
			if (re.test(obj.options[i].text)) {
				obj.options[i].selected = (which=="select");
			}
			else if (only) {
				obj.options[i].selected = (which=="unselect");
			}
		}
		return true;
	}
	return false;
};

Selectbox.selectOptions = function(obj,regex) {
	return this.selectUnselectMatchingOptions(obj,regex,"select",false);
};

Selectbox.selectOnlyOptions = function(obj,regex) {
	return this.selectUnselectMatchingOptions(obj,regex,"select",true);
};

Selectbox.unselectOptions = function(obj,regex) {
	return this.selectUnselectMatchingOptions(obj,regex,"unselect",false);
};

Selectbox.sort = function(obj) {
	var o = [];
	if (!this.hasOptions(obj)) { return false; }
	for (var i=0; i<obj.options.length; i++) {
		o[o.length] = new Option( obj.options[i].text, obj.options[i].value, obj.options[i].defaultSelected, obj.options[i].selected) ;
	}
	if (o.length==0) { return true; }
	o = o.sort( 
		function(a,b) { 
			if ((a.text+"") < (b.text+"")) { return -1; }
			if ((a.text+"") > (b.text+"")) { return 1; }
			return 0;
		} 
	);

	for (var i=0; i<o.length; i++) {
		obj.options[i] = new Option(o[i].text, o[i].value, o[i].defaultSelected, o[i].selected);
	}
	return true;
};

Selectbox.selectAllOptions = function(obj) {
	if (!this.hasOptions(obj)) { return false; }
	for (var i=0; i<obj.options.length; i++) {
		obj.options[i].selected = true;
	}
	return true;
};

Selectbox.moveSelectedOptions = function(from,to) {
	if (!this.hasOptions(from)) { return false; }
	// Unselect matching options, if required
	if (arguments.length>3) {
		var regex = arguments[3];
		if (regex != "") {
			if (!this.unselectOptions(from,regex)) {
				return false;
			}
		}
	}
	// Move them over
	for (var i=0; i<from.options.length; i++) {
		var o = from.options[i];
		if (o.selected) {
			var index = (!this.hasOptions(to))?0:to.options.length;
			to.options[index] = new Option(o.text, o.value, false, false);
		}
	}
	// Delete them from original
	for (var i=(from.options.length-1); i>=0; i--) {
		var o = from.options[i];
		if (o.selected) {
			from.options[i] = null;
		}
	}
	if ((arguments.length<3) || (arguments[2])) {
		this.sort(from);
		this.sort(to);
	}
	from.selectedIndex = to.selectedIndex = -1;
	return true;
};

Selectbox.copySelectedOptions = function(from,to) {
	if (!this.hasOptions(from)) { return false; }
	var options = new Object();
	if (this.hasOptions(to)) {
		for (var i=0; i<to.options.length; i++) {
			options[to.options[i].value] = to.options[i].text;
		}
	}
	for (var i=0; i<from.options.length; i++) {
		var o = from.options[i];
		if (o.selected) {
			if (typeof(options[o.value])=="undefined" || options[o.value]==null || options[o.value]!=o.text) {
				var index = (!this.hasOptions(to))?0:to.options.length;
				to.options[index] = new Option( o.text, o.value, false, false);
			}
		}
	}
	if ((arguments.length<3) || (arguments[2]==true)) {
		this.sort(to);
	}
	from.selectedIndex = to.selectedIndex = -1;
	return true;
};

Selectbox.moveAllOptions = function(from,to) {
	this.selectAllOptions(from);
	if (arguments.length==2) {
		this.moveSelectedOptions(from,to);
	}
	else if (arguments.length==3) {
		this.moveSelectedOptions(from,to,arguments[2]);
		}
	else if (arguments.length==4) {
		this.moveSelectedOptions(from,to,arguments[2],arguments[3]);
	}
};

Selectbox.copyAllOptions = function(from,to) {
	this.selectAllOptions(from);
	if (arguments.length==2) {
		this.copySelectedOptions(from,to);
	}
	else if (arguments.length==3) {
		this.copySelectedOptions(from,to,arguments[2]);
	}
};

Selectbox.swapOptions = function(obj,i,j) {
	if (!this.hasOptions(obj)) { return false; }
	var o = obj.options;
	if (i<0 || i>=o.length || j<0 || j>=o.length) { return false; }
	var i_selected = o[i].selected;
	var j_selected = o[j].selected;
	var temp = new Option(o[i].text, o[i].value, o[i].defaultSelected, o[i].selected);
	var temp2= new Option(o[j].text, o[j].value, o[j].defaultSelected, o[j].selected);
	o[i] = temp2;
	o[j] = temp;
	o[i].selected = j_selected;
	o[j].selected = i_selected;
	return true;
};

Selectbox.moveOptionUp = function(obj) {
	if (!this.hasOptions(obj)) { return false; }
	for (i=0; i<obj.options.length; i++) {
		if (obj.options[i].selected) {
			if (i>0 && !obj.options[i-1].selected) {
				this.swapOptions(obj,i,i-1);
				obj.options[i-1].selected = true;
			}
		}
	}
	return true;
};

Selectbox.moveOptionDown = function(obj) {
	if (!this.hasOptions(obj)) { return false; }
	for (i=obj.options.length-1; i>=0; i--) {
		if (obj.options[i].selected) {
			if (i != (obj.options.length-1) && ! obj.options[i+1].selected) {
				this.swapOptions(obj,i,i+1);
				obj.options[i+1].selected = true;
			}
		}
	}
	return true;
};

Selectbox.removeSelectedOptions = function(from) { 
	if (!this.hasOptions(from)) { return false; }
	if (from.type=="select-one" && from.selectedIndex>=0) {
		from.options[from.selectedIndex] = null;
	}
	else {
		for (var i=(from.options.length-1); i>=0; i--) { 
			var o=from.options[i]; 
			if (o.selected) { 
				from.options[i] = null; 
			} 
		}
	}
	from.selectedIndex = -1; 
};

Selectbox.removeAllOptions = function(from) { 
	if (!this.hasOptions(from)) { return false; }
	for (var i=(from.options.length-1); i>=0; i--) { 
		from.options[i] = null; 
	} 
	from.selectedIndex = -1; 
	return true;
};

Selectbox.addOption = function(obj,text,value,selected) {
	if (obj!=null && obj.options!=null) {
		obj.options[obj.options.length] = new Option(text, value, false, selected);
	}
};

// Create a jQuery Plugin wrapper around the functions
if (jQuery) {
	var fn;
	jQuery.fn.Selectbox = function(fn) {
		return function() {
			var args = Array.prototype.slice.apply(arguments);
			return this.each(
				function() { Selectbox[fn].apply(Selectbox,([this]).concat(args)); }
			);
		};
	};	
	for (fn in Selectbox) {
		jQuery.fn[(fn=="sort")?"sortOptions":fn] = jQuery.fn.Selectbox(fn);
	}
};
